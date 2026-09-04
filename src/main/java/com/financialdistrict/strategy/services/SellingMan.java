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
import com.financialdistrict.strategy.dto.SimulateSellingManRequest;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class SellingMan {
    private final LedgerService ledgerService;
    private final PriceService priceService;

    public AccountResponse process(SimulateSellingManRequest request) {
        String symbol = SimulationRequests.require(request.symbol(), "symbol").toUpperCase();
        BigDecimal dailyIncome = SimulationRequests.require(request.dailyIncome(), "dailyIncome");
        BigDecimal sellAmount = SimulationRequests.require(request.sellAmount(), "sellAmount");
        int sellDay = SimulationRequests.require(request.sellDay(), "sellDay");

        String manId = ManIdSlug.build("selling-man", symbol,
                "inc" + ManIdSlug.number(dailyIncome),
                "day" + sellDay,
                "sell" + ManIdSlug.number(sellAmount));
        if (ledgerService.accountExists(manId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A man account already exists for these exact parameters: " + manId);
        }

        List<PriceBarResponse> bars = priceService.getPriceHistory(symbol);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No backfilled price history for " + symbol);
        }

        BigDecimal runningBalance = BigDecimal.ZERO;
        List<SimulationBarEvent> events = new ArrayList<>(bars.size());
        int i = 0;
        for (PriceBarResponse bar : bars) {
            LocalDateTime barTimestamp = bar.date().atStartOfDay();
            BigDecimal closingPrice = bar.close();
            runningBalance = runningBalance.add(dailyIncome);

            if (i == sellDay) {
                // Daily income still needs its own row here: the sell event below
                // reuses the contribution field to carry sale proceeds instead.
                events.add(new SimulationBarEvent(BigDecimal.ZERO, dailyIncome, closingPrice, false, false,
                        BigDecimal.ZERO, barTimestamp));

                BigDecimal shares = sellAmount.divide(closingPrice, 8, RoundingMode.DOWN).negate();
                BigDecimal shareSellAmount = shares.multiply(closingPrice).abs();
                runningBalance = runningBalance.add(shareSellAmount);
                events.add(new SimulationBarEvent(shares, shareSellAmount, closingPrice, false, true,
                        BigDecimal.ZERO, barTimestamp));
            } else {
                BigDecimal shares = runningBalance.divide(closingPrice, 8, RoundingMode.DOWN);
                BigDecimal amountToSpend = closingPrice.multiply(shares);
                runningBalance = runningBalance.subtract(amountToSpend);
                events.add(new SimulationBarEvent(shares, dailyIncome, closingPrice, true, false, amountToSpend,
                        barTimestamp));
            }
            i++;
        }
        return ledgerService.processSimulationBatch(manId, symbol, events);
    }
}
