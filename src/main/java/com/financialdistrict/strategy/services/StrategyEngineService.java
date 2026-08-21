package com.financialdistrict.strategy.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.dto.BuyRequest;
import com.financialdistrict.ledger.services.LedgerService;
import com.financialdistrict.price.dto.PriceBarResponse;
import com.financialdistrict.price.services.PriceService;
import com.financialdistrict.strategy.events.BuyDecision;
import com.financialdistrict.strategy.models.ManConfig;
import com.financialdistrict.strategy.strategies.AlwaysBuyStrategy;
import com.financialdistrict.strategy.strategies.Strategy;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StrategyEngineService {

    private final PriceService priceService;
    private final LedgerService ledgerService;

    // TEMP: hardcoded Man config, standing in for a ManConfigRepository until
    // there's more than one Man to manage
    private ManConfig getTempManConfig() {
        ManConfig config = new ManConfig();
        config.setManId("temp-man-1");
        config.setSymbols(List.of("SPLG"));
        config.setStrategyName("ALWAYS_BUY");
        config.setActive(true);
        config.setCurrentBalance(new BigDecimal("1.00")); // stubbed, Ledger will own this later
        return config;
    }

    // TEMP: hardcoded strategy resolution, standing in for a real strategy
    // registry/factory later
    private Strategy resolveStrategy(String strategyName) {
        return new AlwaysBuyStrategy(); // only one strategy exists right now
    }

    public BuyDecision runCycleForTempMan() {
        ManConfig manConfig = getTempManConfig();
        String symbol = manConfig.getSymbols().get(0);

        BigDecimal currentPrice = priceService.twelveDataGetQuote(symbol).getClose();
        Strategy strategy = resolveStrategy(manConfig.getStrategyName());

        BuyDecision decision = strategy.decide(manConfig, currentPrice);

        System.out.println("Decision for " + manConfig.getManId() + ": " + decision);

        if (decision.isShouldBuy()) {
            AccountResponse account = applyBuy(decision, null);
            System.out.println("Ledger updated for " + manConfig.getManId() + ": " + account);
        }

        return decision;
    }

    // Replays historical price bars through the same strategy/ledger path as the
    // live cycle above, one buy decision per bar, so the resulting transactions
    // read as if Temp Man had actually been running since the start date.
    public ManConfig getMan(String manId) {
        AccountResponse AccountResponse = ledgerService.getAccount(manId);
        ManConfig config = new ManConfig();
        config.setManId(AccountResponse.manId());
        config.setSymbols(List.of(AccountResponse.symbol()));
        config.setActive(true);
        config.setCurrentBalance(AccountResponse.bankBalance());
        return config;
    }

    public String simulateForTempMan() {
        ManConfig manConfig = getMan("prototype-man");
        String symbol = manConfig.getSymbols().get(0);

        List<PriceBarResponse> bars = priceService.getPriceHistory(symbol);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No backfilled price history for " + symbol);
        }

        for (PriceBarResponse bar : bars) {

            LocalDateTime barTimestamp = bar.date().atStartOfDay();

            BigDecimal newBalance = ledgerService.AddMoneyToManAccount(manConfig.getManId(), BigDecimal.ONE, barTimestamp);
            manConfig.setCurrentBalance(newBalance);

            if (bar.close().compareTo(manConfig.getCurrentBalance()) < 1) {
                int shares = manConfig.getCurrentBalance().divide(bar.close(), 8, RoundingMode.DOWN).intValue();
                BigDecimal amount_to_spend = bar.close().multiply(BigDecimal.valueOf(shares));

                ledgerService.recordBuyTransaction(manConfig.getManId(), symbol, amount_to_spend, bar.close(), barTimestamp);

                AccountResponse updated = ledgerService.getAccount(manConfig.getManId());
                manConfig.setCurrentBalance(updated.bankBalance());
            }

        }
        return "Check";
    }

    private AccountResponse applyBuy(BuyDecision decision, java.time.LocalDateTime timestamp) {
        BuyRequest request = new BuyRequest(
                decision.getManId(), decision.getSymbol(), decision.getAmountToSpend(), decision.getPrice(), timestamp);
        return ledgerService.applyBuy(request);
    }
}
