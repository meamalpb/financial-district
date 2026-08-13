package ledger_service.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import ledger_service.dto.BuyRequest;
import ledger_service.models.ManAccount;
import ledger_service.models.Transaction;
import ledger_service.repositories.ManAccountRepository;
import ledger_service.repositories.TransactionRepository;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final ManAccountRepository manAccountRepository;
    private final TransactionRepository transactionRepository;

    public ManAccount getAccount(String manId) {
        return manAccountRepository.findByManId(manId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No account found for manId: " + manId));
    }

    public ManAccount applyBuy(BuyRequest request) {
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
                .amountSpent(request.amountToSpend())
                .shares(shares)
                .price(request.price())
                .timestamp(LocalDateTime.now())
                .build();
        transactionRepository.save(transaction);

        return savedAccount;
    }
}
