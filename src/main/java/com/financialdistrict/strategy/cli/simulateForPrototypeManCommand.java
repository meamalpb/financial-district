package com.financialdistrict.strategy.cli;

import org.springframework.stereotype.Component;

import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.AllArgsConstructor;
import picocli.CommandLine.Command;

@Component
@Command(name = "simulate-prototype-man", description = "Simulate prototype man")
@AllArgsConstructor
public class simulateForPrototypeManCommand implements Runnable {

    private StrategyEngineService strategyEngineService;
    @Override
    public void run() {
        strategyEngineService.simulateForPrototypeMan();
    }
}