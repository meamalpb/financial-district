package com.financialdistrict.ledger.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.dto.AccountSnapshotResponse;
import com.financialdistrict.ledger.dto.BuyRequest;
import com.financialdistrict.ledger.dto.SimulationBarEvent;
import com.financialdistrict.ledger.models.AccountSnapshot;
import com.financialdistrict.ledger.models.ManAccount;
import com.financialdistrict.ledger.models.Transaction;
import com.financialdistrict.ledger.repositories.AccountSnapshotRepository;
import com.financialdistrict.ledger.repositories.ManAccountRepository;
import com.financialdistrict.ledger.repositories.TransactionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LedgerService {

    // Chunk size for batchUpdate calls in processSimulationBatch: a full
    // history replay can be thousands of bars, so rows are flushed to the DB
    // in bounded-size batches rather than one giant batch or one row at a time.
    private static final int SIMULATION_BATCH_SIZE = 500;

    private final ManAccountRepository manAccountRepository;
    private final TransactionRepository transactionRepository;
    private final AccountSnapshotRepository accountSnapshotRepository;
    private final JdbcTemplate jdbcTemplate;

public AccountResponse getAccount(String manId) {
        ManAccount account = manAccountRepository.findByManId(manId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No account found for manId: " + manId));
        return toAccountResponse(account);
    }

    public AccountResponse applyBuy(BuyRequest request) {
        ManAccount account = manAccountRepository.findByManId(request.manId())
                .orElseGet(() -> ManAccount.builder()
                        .manId(request.manId())
                        .symbol(request.symbol())
                        .bankBalance(BigDecimal.ZERO)
                        .sharesOwned(BigDecimal.ZERO)
                        .costBasis(BigDecimal.ZERO)
                        .marketValue(BigDecimal.ZERO)
                        .build());

        BigDecimal shares = request.amountToSpend().divide(request.price(), 8, RoundingMode.HALF_UP);

        // No daily contribution mechanic yet, so bankBalance can go negative until that's built.
        account.setBankBalance(account.getBankBalance().subtract(request.amountToSpend()));
        account.setSharesOwned(account.getSharesOwned().add(shares));
        account.setCostBasis(account.getCostBasis().add(request.amountToSpend()));
        account.setMarketValue(account.getSharesOwned().multiply(request.price()));
        account.setUpdatedAt(LocalDateTime.now());

        ManAccount savedAccount = manAccountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .manId(request.manId())
                .symbol(request.symbol())
                .amount(request.amountToSpend())
                .shares(shares)
                .price(request.price())
                .timestamp(request.timestamp() != null ? request.timestamp() : LocalDateTime.now())
                .build();
        Transaction savedTransaction = transactionRepository.save(transaction);

        saveSnapshot(savedAccount, savedTransaction);

        return toAccountResponse(savedAccount);
    }

    // Replays a whole simulation run's bars (contribution + optional buy each)
    // against a Man's account in one pass: account state is folded in memory
    // bar-by-bar (balance/shares/cost-basis all depend on the running total,
    // so that part stays sequential), then every Transaction and every
    // AccountSnapshot row is written with jdbcTemplate.batchUpdate in chunks
    // of SIMULATION_BATCH_SIZE, and ManAccount is saved once at the end -
    // instead of the 1 (upsert) + 1-3 (inserts) round trips per bar the old
    // per-bar recordBuyTransaction/AddMoneyToManAccount methods cost.
    public AccountResponse processSimulationBatch(String manId, String symbol, List<SimulationBarEvent> events) {
        ManAccount account = manAccountRepository.findByManId(manId)
                .orElseGet(() -> ManAccount.builder()
                        .manId(manId)
                        .symbol(symbol)
                        .bankBalance(BigDecimal.ZERO)
                        .sharesOwned(BigDecimal.ZERO)
                        .costBasis(BigDecimal.ZERO)
                        .marketValue(BigDecimal.ZERO)
                        .build());

        List<Object[]> transactionRows = new ArrayList<>();
        List<PendingSnapshot> pendingSnapshots = new ArrayList<>();

        for (SimulationBarEvent event : events) {
            account.setBankBalance(account.getBankBalance().add(event.contribution()));
            transactionRows.add(new Object[] {
                    manId, symbol, event.contribution(), null, null, "income", Timestamp.valueOf(event.timestamp())
            });

            if (event.buy()) {
                BigDecimal shares = event.amountToSpend().divide(event.price(), 8, RoundingMode.HALF_UP);
                account.setBankBalance(account.getBankBalance().subtract(event.amountToSpend()));
                account.setSharesOwned(account.getSharesOwned().add(shares));
                account.setCostBasis(account.getCostBasis().add(event.amountToSpend()));
                account.setMarketValue(account.getSharesOwned().multiply(event.price()));

                transactionRows.add(new Object[] {
                        manId, symbol, event.amountToSpend(), shares, event.price(), "buy",
                        Timestamp.valueOf(event.timestamp())
                });

                BigDecimal gain = calculateGain(account);
                BigDecimal gainPercent = calculateGainPercent(account, gain);
                pendingSnapshots.add(new PendingSnapshot(
                        event.price(), account.getBankBalance(), account.getSharesOwned(),
                        account.getCostBasis(), account.getMarketValue(), gain, gainPercent, event.timestamp()));
            }
        }

        account.setUpdatedAt(LocalDateTime.now());
        ManAccount savedAccount = manAccountRepository.save(account);

        batchInsertTransactions(transactionRows);
        if (!pendingSnapshots.isEmpty()) {
            List<Long> buyTransactionIds = fetchLatestBuyTransactionIds(manId, symbol, pendingSnapshots.size());
            batchInsertSnapshots(manId, symbol, pendingSnapshots, buyTransactionIds);
        }

        return toAccountResponse(savedAccount);
    }

    private void batchInsertTransactions(List<Object[]> transactionRows) {
        int[] argTypes = {
                Types.VARCHAR, Types.VARCHAR, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.VARCHAR, Types.TIMESTAMP
        };
        String sql = "INSERT INTO transactions (man_id, symbol, amount, shares, price, transaction_type, timestamp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        for (List<Object[]> chunk : partition(transactionRows, SIMULATION_BATCH_SIZE)) {
            jdbcTemplate.batchUpdate(sql, chunk, argTypes);
        }
    }

    // Batch-inserted transactions don't hand back their generated ids, so the
    // ids this simulation just created are looked up by grabbing the most
    // recent N "buy" rows for this Man/symbol - safe because a single batch
    // insert assigns IDENTITY values in ascending order matching input order,
    // so ordering by id here lines back up with pendingSnapshots' bar order.
    private List<Long> fetchLatestBuyTransactionIds(String manId, String symbol, int count) {
        List<Long> ids = jdbcTemplate.queryForList(
                "SELECT id FROM transactions WHERE man_id = ? AND symbol = ? AND transaction_type = 'buy' "
                        + "ORDER BY id DESC LIMIT ?",
                Long.class, manId, symbol, count);
        Collections.reverse(ids);
        return ids;
    }

    private void batchInsertSnapshots(String manId, String symbol, List<PendingSnapshot> pendingSnapshots,
            List<Long> buyTransactionIds) {
        List<Object[]> snapshotRows = new ArrayList<>(pendingSnapshots.size());
        for (int i = 0; i < pendingSnapshots.size(); i++) {
            PendingSnapshot snapshot = pendingSnapshots.get(i);
            snapshotRows.add(new Object[] {
                    manId, symbol, buyTransactionIds.get(i), snapshot.price(), snapshot.bankBalance(),
                    snapshot.sharesOwned(), snapshot.costBasis(), snapshot.marketValue(), snapshot.gain(),
                    snapshot.gainPercent(), Timestamp.valueOf(snapshot.timestamp())
            });
        }

        int[] argTypes = {
                Types.VARCHAR, Types.VARCHAR, Types.BIGINT, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC,
                Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.NUMERIC, Types.TIMESTAMP
        };
        String sql = "INSERT INTO account_snapshots (man_id, symbol, transaction_id, price, bank_balance, "
                + "shares_owned, cost_basis, market_value, gain, gain_percent, timestamp) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        for (List<Object[]> chunk : partition(snapshotRows, SIMULATION_BATCH_SIZE)) {
            jdbcTemplate.batchUpdate(sql, chunk, argTypes);
        }
    }

    private static <T> List<List<T>> partition(List<T> items, int size) {
        List<List<T>> chunks = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            chunks.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return chunks;
    }

    // Carries a buy's derived snapshot fields between the in-memory replay
    // loop and the batch insert, since the real transaction_id isn't known
    // until after the transactions batch has been flushed.
    private record PendingSnapshot(
            BigDecimal price, BigDecimal bankBalance, BigDecimal sharesOwned, BigDecimal costBasis,
            BigDecimal marketValue, BigDecimal gain, BigDecimal gainPercent, LocalDateTime timestamp) {
    }

    public List<AccountSnapshotResponse> getSnapshots(String manId) {
        return accountSnapshotRepository.findByManIdOrderByTimestampAsc(manId).stream()
                .map(this::toSnapshotResponse)
                .toList();
    }

    // Captures cost basis / market value / gain as they stood right after this
    // buy, so a Man's profit-or-loss history can be read back transaction by
    // transaction instead of only as the current live figures on ManAccount.
    private AccountSnapshot saveSnapshot(ManAccount account, Transaction transaction) {
        BigDecimal gain = calculateGain(account);
        BigDecimal gainPercent = calculateGainPercent(account, gain);

        AccountSnapshot snapshot = AccountSnapshot.builder()
                .manId(account.getManId())
                .symbol(account.getSymbol())
                .transactionId(transaction.getId())
                .price(transaction.getPrice())
                .bankBalance(account.getBankBalance())
                .sharesOwned(account.getSharesOwned())
                .costBasis(account.getCostBasis())
                .marketValue(account.getMarketValue())
                .gain(gain)
                .gainPercent(gainPercent)
                .timestamp(transaction.getTimestamp())
                .build();
        return accountSnapshotRepository.save(snapshot);
    }

    private BigDecimal calculateGain(ManAccount account) {
        return account.getMarketValue().subtract(account.getCostBasis());
    }

    private BigDecimal calculateGainPercent(ManAccount account, BigDecimal gain) {
        return account.getCostBasis().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : gain.divide(account.getCostBasis(), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
    }

    // Gain/loss is derived here, not persisted on ManAccount: marketValue only
    // reflects price as of the last buy (no daily mark-to-market job yet), so
    // this is a stopgap until analytics-service owns gain/loss reporting.
    private AccountResponse toAccountResponse(ManAccount account) {
        BigDecimal gain = calculateGain(account);
        BigDecimal gainPercent = calculateGainPercent(account, gain);

        return AccountResponse.builder()
                .manId(account.getManId())
                .symbol(account.getSymbol())
                .bankBalance(account.getBankBalance())
                .sharesOwned(account.getSharesOwned())
                .costBasis(account.getCostBasis())
                .marketValue(account.getMarketValue())
                .gain(gain)
                .gainPercent(gainPercent)
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    private AccountSnapshotResponse toSnapshotResponse(AccountSnapshot snapshot) {
        return AccountSnapshotResponse.builder()
                .manId(snapshot.getManId())
                .symbol(snapshot.getSymbol())
                .transactionId(snapshot.getTransactionId())
                .price(snapshot.getPrice())
                .bankBalance(snapshot.getBankBalance())
                .sharesOwned(snapshot.getSharesOwned())
                .costBasis(snapshot.getCostBasis())
                .marketValue(snapshot.getMarketValue())
                .gain(snapshot.getGain())
                .gainPercent(snapshot.getGainPercent())
                .timestamp(snapshot.getTimestamp())
                .build();
    }
}
