package com.financialdistrict.strategy.cli;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.financialdistrict.strategy.dto.SimulatePrototypeManRequest;
import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.RequiredArgsConstructor;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "simulate-prototype-man", description = "Simulate prototype man")
@RequiredArgsConstructor
public class SimulateForPrototypeManCommand implements Runnable {

    private final StrategyEngineService strategyEngineService;

    @Option(names = "--symbol", defaultValue = "SPY")
    private String symbol;

    @Option(names = "--daily-income", defaultValue = "1")
    private BigDecimal dailyIncome;

    @Override
    public void run() {
        strategyEngineService.simulateForPrototypeMan(new SimulatePrototypeManRequest(symbol, dailyIncome));
    }
}
