package com.financialdistrict.strategy.cli;

import org.springframework.stereotype.Component;

import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.AllArgsConstructor;
import picocli.CommandLine.Command;

@Component
@Command(name = "simulate-sisyphus", description = "Simulate Sisyphus (daily DCA)")
@AllArgsConstructor
public class SisyphusCommand implements Runnable{
    private final StrategyEngineService strategyEngineService;

    @Override
    public void run() {
        strategyEngineService.simulateForSisyphus();
    }
}
