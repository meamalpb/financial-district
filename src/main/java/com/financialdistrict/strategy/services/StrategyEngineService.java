package com.financialdistrict.strategy.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.dto.SimulationBarEvent;
import com.financialdistrict.ledger.services.LedgerService;
import com.financialdistrict.price.dto.PriceBarResponse;
import com.financialdistrict.price.services.PriceService;
import com.financialdistrict.strategy.models.ManConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StrategyEngineService {

    private final PriceService priceService;
    private final LedgerService ledgerService;


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

    public String simulateForPrototypeMan() {
        ManConfig manConfig = getMan("prototype-man");
        String symbol = manConfig.getSymbols().get(0);

        List<PriceBarResponse> bars = priceService.getPriceHistory(symbol);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No backfilled price history for " + symbol);
        }

        // Running balance is folded in memory bar-by-bar (each bar's buy
        // decision depends on the balance after prior bars' contributions/buys),
        // then the whole run is handed to LedgerService in one shot so it can
        // persist every bar's effects via jdbcTemplate.batchUpdate instead of
        // this loop doing a DB round trip per bar.
        BigDecimal runningBalance = manConfig.getCurrentBalance();
        List<SimulationBarEvent> events = new ArrayList<>(bars.size());

        for (PriceBarResponse bar : bars) {
            LocalDateTime barTimestamp = bar.date().atStartOfDay();
            runningBalance = runningBalance.add(BigDecimal.ONE);

            boolean shouldBuy = false;
            BigDecimal amountToSpend = BigDecimal.ZERO;
            if (bar.close().compareTo(runningBalance) < 1) {
                int shares = runningBalance.divide(bar.close(), 8, RoundingMode.DOWN).intValue();
                amountToSpend = bar.close().multiply(BigDecimal.valueOf(shares));
                runningBalance = runningBalance.subtract(amountToSpend);
                shouldBuy = true;
            }

            events.add(new SimulationBarEvent(BigDecimal.ONE, bar.close(), shouldBuy, amountToSpend, barTimestamp));
        }

        ledgerService.processSimulationBatch(manConfig.getManId(), symbol, events);
        return "Check";
    }

}
