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
import com.financialdistrict.strategy.models.ManConfig;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Component
public class SellingMan {
    private final LedgerService ledgerService;
    private final PriceService priceService;

    public ManConfig getMan(String manId, String symbol) {
        AccountResponse accountResponse = ledgerService.getAccount(manId, symbol);
        ManConfig config = new ManConfig();
        config.setManId(accountResponse.manId());
        config.setSymbols(List.of(accountResponse.symbol()));
        config.setActive(true);
        config.setCurrentBalance(accountResponse.bankBalance());
        return config;
    }

    public void process() {
        int SELLDAY = 7;
        BigDecimal dailyIncome = BigDecimal.ONE;

        ManConfig manConfig = getMan("selling-ma", "SPY");
        String symbol = manConfig.getSymbols().get(0);

        List<PriceBarResponse> bars = priceService.getPriceHistory(symbol);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No backfilled price history for " + symbol);
        }

        BigDecimal runningBalance = manConfig.getCurrentBalance();
        List<SimulationBarEvent> events = new ArrayList<>(bars.size());
        int i = 0;
        for (PriceBarResponse bar : bars) {
            LocalDateTime barTimestamp = bar.date().atStartOfDay();

            BigDecimal closing_price = bar.close();
            BigDecimal contribution = dailyIncome;
            boolean shouldBuy = false;
            boolean shouldSell = false;
            BigDecimal amountToSpend = BigDecimal.ZERO;
            BigDecimal shares = BigDecimal.ZERO;
            BigDecimal SellAmount = BigDecimal.valueOf(5);

            runningBalance = runningBalance.add(contribution);
            events.add(new SimulationBarEvent(shares, contribution, closing_price, shouldBuy, shouldSell, amountToSpend,
                    barTimestamp));

            if (i == SELLDAY) {
                shouldSell = true;
                shares = SellAmount.divide(closing_price, 8, RoundingMode.DOWN).negate();
                BigDecimal shareSellAmount = shares.multiply(closing_price).abs();
                runningBalance = runningBalance.add(shareSellAmount);
                contribution = shareSellAmount;
            } else {
                shares = runningBalance.divide(closing_price, 8, RoundingMode.DOWN);
                amountToSpend = closing_price.multiply(shares);
                runningBalance = runningBalance.subtract(amountToSpend);
                shouldBuy = true;
                contribution = BigDecimal.ZERO;
            }
            events.add(new SimulationBarEvent(shares, contribution, closing_price, shouldBuy, shouldSell, amountToSpend,
                    barTimestamp));
            i++;
        }
        ledgerService.processSimulationBatch(manConfig.getManId(), symbol, events);

    }
}
