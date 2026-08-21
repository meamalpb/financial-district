package com.financialdistrict.strategy.cli;

import org.springframework.stereotype.Component;

import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.AllArgsConstructor;
import picocli.CommandLine.Command;

@Component
@Command(name = "create-user", description = "Create a new user")
@AllArgsConstructor
public class ExampleUserCommand implements Runnable {

    private StrategyEngineService strategyEngineService;
    @Override
    public void run() {
        strategyEngineService.simulateForTempMan();
    }
}