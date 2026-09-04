package com.financialdistrict.strategy.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.dto.SimulationBarEvent;
import com.financialdistrict.ledger.services.LedgerService;
import com.financialdistrict.price.dto.PriceBarResponse;
import com.financialdistrict.price.services.PriceService;
import com.financialdistrict.strategy.dto.SimulatePrototypeManRequest;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class SimulatePrototypeMan {
    private final LedgerService ledgerService;
    private final PriceService priceService;

    // Replays historical price bars against a Man's account, one buy decision
    // per bar, so the resulting transactions read as if this Man had actually
    // been running since the start date.
    public AccountResponse process(SimulatePrototypeManRequest request) {
        String symbol = SimulationRequests.require(request.symbol(), "symbol").toUpperCase();
        BigDecimal dailyIncome = SimulationRequests.require(request.dailyIncome(), "dailyIncome");

        String manId = ManIdSlug.build("prototype-man", symbol, "inc" + ManIdSlug.number(dailyIncome));
        if (ledgerService.accountExists(manId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A man account already exists for these exact parameters: " + manId);
        }

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
        BigDecimal runningBalance = BigDecimal.ZERO;
        List<SimulationBarEvent> events = new ArrayList<>(bars.size());

        for (PriceBarResponse bar : bars) {
            LocalDateTime barTimestamp = bar.date().atStartOfDay();
            BigDecimal closingPrice = bar.close();
            runningBalance = runningBalance.add(dailyIncome);

            boolean shouldBuy = false;
            BigDecimal amountToSpend = BigDecimal.ZERO;
            BigDecimal shares = BigDecimal.ZERO;
            if (closingPrice.compareTo(runningBalance) < 1) {
                shares = runningBalance.divide(closingPrice, 8, RoundingMode.DOWN);
                amountToSpend = closingPrice.multiply(shares);
                runningBalance = runningBalance.subtract(amountToSpend);
                shouldBuy = true;
            }

            events.add(new SimulationBarEvent(shares, dailyIncome, closingPrice, shouldBuy, false, amountToSpend,
                    barTimestamp));
        }
        return ledgerService.processSimulationBatch(manId, symbol, events);
    }
}
