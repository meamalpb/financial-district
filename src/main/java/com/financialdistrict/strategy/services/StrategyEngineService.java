package com.financialdistrict.strategy.services;

import java.util.List;

import org.springframework.stereotype.Service;

import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.services.LedgerService;
import com.financialdistrict.strategy.models.ManConfig;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StrategyEngineService {

    private final LedgerService ledgerService;
    private final SimulatePrototypeMan simulatePrototypeMan;

    public ManConfig getMan(String manId, String symbol) {
        AccountResponse accountResponse = ledgerService.getAccount(manId, symbol);
        ManConfig config = new ManConfig();
        config.setManId(accountResponse.manId());
        config.setSymbols(List.of(accountResponse.symbol()));
        config.setActive(true);
        config.setCurrentBalance(accountResponse.bankBalance());
        return config;
    }

    // Replays historical price bars against a Man's account, one buy decision
    // per bar, so the resulting transactions read as if this Man had actually
    // been running since the start date.
    public String simulateForPrototypeMan() {
        simulatePrototypeMan.process();
        return "Done";
    }

}
