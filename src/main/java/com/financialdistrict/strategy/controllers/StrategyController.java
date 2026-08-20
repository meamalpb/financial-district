package com.financialdistrict.strategy.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financialdistrict.strategy.events.BuyDecision;
import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyEngineService strategyEngineService;

    @GetMapping("/internal/run-temp-man")
    public BuyDecision runTempMan() {
        return strategyEngineService.runCycleForTempMan();
    }

}
