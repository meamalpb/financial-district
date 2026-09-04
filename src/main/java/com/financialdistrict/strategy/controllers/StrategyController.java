package com.financialdistrict.strategy.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.strategy.dto.SimulateDropSellingManRequest;
import com.financialdistrict.strategy.dto.SimulatePrototypeManRequest;
import com.financialdistrict.strategy.dto.SimulateSellingManRequest;
import com.financialdistrict.strategy.dto.SimulateSisyphusRequest;
import com.financialdistrict.strategy.services.StrategyEngineService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class StrategyController {

    private final StrategyEngineService strategyEngineService;

    @PostMapping("/internal/simulate-prototype-man")
    public AccountResponse simulatePrototypeMan(@RequestBody SimulatePrototypeManRequest request) {
        return strategyEngineService.simulateForPrototypeMan(request);
    }

    @PostMapping("/internal/simulate-sisyphus")
    public AccountResponse simulateSisyphus(@RequestBody SimulateSisyphusRequest request) {
        return strategyEngineService.simulateForSisyphus(request);
    }

    @PostMapping("/internal/simulate-selling-man")
    public AccountResponse simulateSellingMan(@RequestBody SimulateSellingManRequest request) {
        return strategyEngineService.simulateSelling(request);
    }

    @PostMapping("/internal/simulate-drop-selling-man")
    public AccountResponse simulateDropSellingMan(@RequestBody SimulateDropSellingManRequest request) {
        return strategyEngineService.simulateDropSelling(request);
    }

}
