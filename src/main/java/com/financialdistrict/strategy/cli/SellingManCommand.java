package com.financialdistrict.strategy.cli;

import org.springframework.stereotype.Component;

import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.AllArgsConstructor;
import picocli.CommandLine.Command;

@Component
@Command(name = "simulate-selling-man", description = "Simulate selling man")
@AllArgsConstructor
public class SellingManCommand implements Runnable {

    private StrategyEngineService strategyEngineService;
    @Override
    public void run() {
        strategyEngineService.simulateSelling();
    }
}