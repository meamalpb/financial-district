package com.financialdistrict.strategy.controllers;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.strategy.dto.SimulationResult;
import com.financialdistrict.strategy.events.BuyDecision;
import com.financialdistrict.strategy.services.StrategyEngineService;

@RestController
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyEngineService strategyEngineService;

    @GetMapping("/internal/run-temp-man")
    public BuyDecision runTempMan() {
        return strategyEngineService.runCycleForTempMan();
    }

    @GetMapping("/internal/simulate-temp-man")
    public SimulationResult simulateTempMan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return strategyEngineService.simulateForTempMan(from, to);
    }
}
