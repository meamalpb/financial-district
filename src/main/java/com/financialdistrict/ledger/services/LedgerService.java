package com.financialdistrict.ledger.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.dto.BuyRequest;
import com.financialdistrict.ledger.models.ManAccount;
import com.financialdistrict.ledger.models.Transaction;
import com.financialdistrict.ledger.repositories.ManAccountRepository;
import com.financialdistrict.ledger.repositories.TransactionRepository;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final ManAccountRepository manAccountRepository;
    private final TransactionRepository transactionRepository;

public AccountResponse getAccount(String manId) {
        ManAccount account = manAccountRepository.findByManId(manId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No account found for manId: " + manId));
        return toAccountResponse(account);
    }

    public BigDecimal AddMoneyToManAccount(String manId,BigDecimal salary){
        ManAccount account = manAccountRepository.findByManId(manId)
                        .orElseGet(() -> ManAccount.builder()
                        .manId(manId)
                        .bankBalance(BigDecimal.ZERO)
                        .sharesOwned(BigDecimal.ZERO)
                        .costBasis(BigDecimal.ZERO)
                        .marketValue(BigDecimal.ZERO)
                        .build());
        BigDecimal new_balance = account.getBankBalance().add(BigDecimal.ONE);
        account.setBankBalance(new_balance);
        Transaction transaction = Transaction.builder()
                    .manId(account.getManId())
                    .transactionType("income")
                    .amount(BigDecimal.ONE)
                    .timestamp(LocalDateTime.now())
                    .build();
            transactionRepository.save(transaction);
        return new_balance;
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
        transactionRepository.save(transaction);

        return toAccountResponse(savedAccount);
    }

    public void recordBuyTransaction(String manId, String symbol, BigDecimal amountToSpend, BigDecimal price, LocalDateTime timestamp) {
        ManAccount account = manAccountRepository.findByManId(manId)
                .orElseGet(() -> ManAccount.builder()
                        .manId(manId)
                        .symbol(symbol)
                        .bankBalance(BigDecimal.ZERO)
                        .sharesOwned(BigDecimal.ZERO)
                        .costBasis(BigDecimal.ZERO)
                        .marketValue(BigDecimal.ZERO)
                        .build());

        BigDecimal shares = amountToSpend.divide(price, 8, RoundingMode.HALF_UP);

        account.setBankBalance(account.getBankBalance().subtract(amountToSpend));
        account.setSharesOwned(account.getSharesOwned().add(shares));
        account.setCostBasis(account.getCostBasis().add(amountToSpend));
        account.setMarketValue(account.getSharesOwned().multiply(price));
        account.setUpdatedAt(LocalDateTime.now());

        manAccountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .manId(manId)
                .symbol(symbol)
                .amount(amountToSpend)
                .shares(shares)
                .price(price)
                .transactionType("buy")
                .timestamp(timestamp)
                .build();
        transactionRepository.save(transaction);
    }

    // Gain/loss is derived here, not persisted on ManAccount: marketValue only
    // reflects price as of the last buy (no daily mark-to-market job yet), so
    // this is a stopgap until analytics-service owns gain/loss reporting.
    private AccountResponse toAccountResponse(ManAccount account) {
        BigDecimal gain = account.getMarketValue().subtract(account.getCostBasis());
        BigDecimal gainPercent = account.getCostBasis().compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : gain.divide(account.getCostBasis(), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);

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
}
