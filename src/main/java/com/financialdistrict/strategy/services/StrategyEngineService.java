package com.financialdistrict.strategy.services;

import org.springframework.stereotype.Service;

import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.strategy.dto.SimulateDropSellingManRequest;
import com.financialdistrict.strategy.dto.SimulatePrototypeManRequest;
import com.financialdistrict.strategy.dto.SimulateSellingManRequest;
import com.financialdistrict.strategy.dto.SimulateSisyphusRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StrategyEngineService {

    private final SimulatePrototypeMan simulatePrototypeMan;
    private final SisyphusMan sisyphusMan;
    private final SellingMan sellingMan;
    private final DropSellingMan dropSellingMan;

    // Replays historical price bars against a Man's account, one buy decision
    // per bar, so the resulting transactions read as if this Man had actually
    // been running since the start date.
    public AccountResponse simulateForPrototypeMan(SimulatePrototypeManRequest request) {
        return simulatePrototypeMan.process(request);
    }

    public AccountResponse simulateForSisyphus(SimulateSisyphusRequest request) {
        return sisyphusMan.process(request);
    }

    public AccountResponse simulateSelling(SimulateSellingManRequest request) {
        return sellingMan.process(request);
    }

    public AccountResponse simulateDropSelling(SimulateDropSellingManRequest request) {
        return dropSellingMan.process(request);
    }

}
