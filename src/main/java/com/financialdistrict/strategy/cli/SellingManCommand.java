package com.financialdistrict.strategy.cli;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.financialdistrict.strategy.dto.SimulateSellingManRequest;
import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "simulate-selling-man", description = "Simulate selling man")
@RequiredArgsConstructor
public class SellingManCommand implements Runnable {

    private final StrategyEngineService strategyEngineService;

    @Option(names = "--symbol", defaultValue = "SPY")
    private String symbol;

    @Option(names = "--daily-income", defaultValue = "1")
    private BigDecimal dailyIncome;

    @Option(names = "--sell-amount", defaultValue = "5")
    private BigDecimal sellAmount;

    @Option(names = "--sell-day", defaultValue = "7")
    private Integer sellDay;

    @Override
    public void run() {
        strategyEngineService.simulateSelling(new SimulateSellingManRequest(symbol, dailyIncome, sellAmount, sellDay));
    }
}
