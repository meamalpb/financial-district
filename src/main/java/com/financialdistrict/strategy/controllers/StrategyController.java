package com.financialdistrict.strategy.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyEngineService strategyEngineService;

    @GetMapping("/internal/simulate-prototype-man")
    public String simulatePrototypeMan() {
        return strategyEngineService.simulateForPrototypeMan();
    }

    @GetMapping("/internal/simulate-sisyphus")
    public String simulateSisyphus() {
        return strategyEngineService.simulateForSisyphus();
    }

    @GetMapping("/internal/simulate-selling-man")
    public String simulateSellingMan() {
        return strategyEngineService.simulateSelling();
    }

    @GetMapping("/internal/simulate-drop-selling-man")
    public String simulateDropSellingMan() {
        return strategyEngineService.simulateDropSelling();
    }

}
