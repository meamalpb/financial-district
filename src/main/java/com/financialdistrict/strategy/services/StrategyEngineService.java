package com.financialdistrict.strategy.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.dto.BuyRequest;
import com.financialdistrict.ledger.services.LedgerService;
import com.financialdistrict.price.dto.PriceBarResponse;
import com.financialdistrict.price.services.PriceService;
import com.financialdistrict.strategy.dto.SimulationResult;
import com.financialdistrict.strategy.events.BuyDecision;
import com.financialdistrict.strategy.models.ManConfig;
import com.financialdistrict.strategy.strategies.AlwaysBuyStrategy;
import com.financialdistrict.strategy.strategies.Strategy;

@Service
@RequiredArgsConstructor
public class StrategyEngineService {

    private final PriceService priceService;
    private final LedgerService ledgerService;

    // TEMP: hardcoded Man config, standing in for a ManConfigRepository until there's more than one Man to manage
    private ManConfig getTempManConfig() {
        ManConfig config = new ManConfig();
        config.setManId("temp-man-1");
        config.setSymbols(List.of("SPLG"));
        config.setStrategyName("ALWAYS_BUY");
        config.setActive(true);
        config.setCurrentBalance(new BigDecimal("1.00")); // stubbed, Ledger will own this later
        return config;
    }

    // TEMP: hardcoded strategy resolution, standing in for a real strategy registry/factory later
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
    public SimulationResult simulateForTempMan(LocalDate from, LocalDate to) {
        ManConfig manConfig = getTempManConfig();
        String symbol = manConfig.getSymbols().get(0);
        Strategy strategy = resolveStrategy(manConfig.getStrategyName());

        List<PriceBarResponse> bars = priceService.getPriceHistory(symbol, from, to);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No backfilled price history for " + symbol + " between " + from + " and " + to);
        }

        int buysExecuted = 0;
        AccountResponse account = null;
        for (PriceBarResponse bar : bars) {
            BuyDecision decision = strategy.decide(manConfig, bar.close());
            if (decision.isShouldBuy()) {
                account = applyBuy(decision, bar.date().atStartOfDay());
                buysExecuted++;
            }
        }

        return SimulationResult.builder()
                .manId(manConfig.getManId())
                .symbol(symbol)
                .from(from)
                .to(to)
                .barsProcessed(bars.size())
                .buysExecuted(buysExecuted)
                .account(account)
                .build();
    }

    private AccountResponse applyBuy(BuyDecision decision, java.time.LocalDateTime timestamp) {
        BuyRequest request = new BuyRequest(
                decision.getManId(), decision.getSymbol(), decision.getAmountToSpend(), decision.getPrice(), timestamp);
        return ledgerService.applyBuy(request);
    }
}
