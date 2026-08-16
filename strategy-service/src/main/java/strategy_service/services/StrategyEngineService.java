package strategy_service.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import strategy_service.clients.LedgerServiceClient;
import strategy_service.clients.PriceServiceClient;
import strategy_service.dto.LedgerAccountResponse;
import strategy_service.dto.PriceBarResponse;
import strategy_service.dto.SimulationResult;
import strategy_service.events.BuyDecision;
import strategy_service.models.ManConfig;
import strategy_service.strategies.AlwaysBuyStrategy;
import strategy_service.strategies.Strategy;

@Service
public class StrategyEngineService {

    private final PriceServiceClient priceServiceClient;
    private final LedgerServiceClient ledgerServiceClient;

    @Autowired
    public StrategyEngineService(PriceServiceClient priceServiceClient, LedgerServiceClient ledgerServiceClient) {
        this.priceServiceClient = priceServiceClient;
        this.ledgerServiceClient = ledgerServiceClient;
    }

    // TEMP: hardcoded Man config, standing in for ManConfigRepository until Mongo is wired in
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

        BigDecimal currentPrice = priceServiceClient.getCurrentPrice(symbol);
        Strategy strategy = resolveStrategy(manConfig.getStrategyName());

        BuyDecision decision = strategy.decide(manConfig, currentPrice);

        System.out.println("Decision for " + manConfig.getManId() + ": " + decision);

        if (decision.isShouldBuy()) {
            LedgerAccountResponse account = ledgerServiceClient.applyBuy(decision);
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

        List<PriceBarResponse> bars = priceServiceClient.getHistory(symbol, from, to);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No backfilled price history for " + symbol + " between " + from + " and " + to);
        }

        int buysExecuted = 0;
        LedgerAccountResponse account = null;
        for (PriceBarResponse bar : bars) {
            BuyDecision decision = strategy.decide(manConfig, bar.getClose());
            if (decision.isShouldBuy()) {
                account = ledgerServiceClient.applyBuy(decision, bar.getDate().atStartOfDay());
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
}