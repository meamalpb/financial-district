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

@Component
@AllArgsConstructor
public class CanSellMan {
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
        ManConfig manConfig = getMan("can-sell-man", "SPY");
        String symbol = manConfig.getSymbols().get(0);

        List<PriceBarResponse> bars = priceService.getPriceHistory(symbol);
        if (bars.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No backfilled price history for " + symbol);
        }

        BigDecimal runningBalance = manConfig.getCurrentBalance();
        List<SimulationBarEvent> events = new ArrayList<>(bars.size());

        for (PriceBarResponse bar : bars) {
            LocalDateTime barTimestamp = bar.date().atStartOfDay();
            runningBalance = runningBalance.add(BigDecimal.ONE);

            boolean shouldBuy = false;
            boolean shouldsell = false;
            BigDecimal amountToSell = BigDecimal.ZERO;
            BigDecimal amountToSpend = BigDecimal.ZERO;
            BigDecimal contribution = BigDecimal.ZERO;
            int sell = 1;
            if (bar.close().compareTo(runningBalance) < 1) {

                if (sell == 7){
                    shouldsell =  true;
                    BigDecimal sellingAmount = BigDecimal.valueOf(5);
                    BigDecimal shares = sellingAmount.divide(bar.close(),8,RoundingMode.DOWN);
                    amountToSell = bar.close().multiply(shares);
                    contribution = amountToSell.add(BigDecimal.ONE);
                    runningBalance.add(amountToSell);
                    sell = 1;
                }
                else{
                 contribution = BigDecimal.ONE;
                BigDecimal shares = runningBalance.divide(bar.close(), 8, RoundingMode.DOWN);
                amountToSpend = bar.close().multiply(shares);
                runningBalance = runningBalance.subtract(amountToSpend);
                shouldBuy = true;
                }
            }
            events.add(new SimulationBarEvent(contribution, bar.close(), shouldBuy, shouldsell, amountToSell,
                    amountToSpend, barTimestamp));
        }
        ledgerService.processSimulationBatch(manConfig.getManId(), symbol, events);

    }
}
