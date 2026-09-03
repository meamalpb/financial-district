package com.financialdistrict.ledger.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.dto.AccountSnapshotResponse;
import com.financialdistrict.ledger.dto.BuyRequest;
import com.financialdistrict.ledger.services.LedgerService;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class LedgerController {

    private final LedgerService ledgerService;

    @GetMapping("/accounts/{manId}")
    public AccountResponse getAccount(@PathVariable String manId) {
        return ledgerService.getAccount(manId);
    }

    @PostMapping("/buy")
    public AccountResponse buy(@RequestBody BuyRequest request) {
        return ledgerService.applyBuy(request);
    }

    @GetMapping("/accounts/{manId}/snapshots")
    public List<AccountSnapshotResponse> getSnapshots(@PathVariable String manId) {
        return ledgerService.getSnapshots(manId);
    }

    @DeleteMapping("/accounts/{manId}")
    public void deleteAccount(@PathVariable String manId) {
        ledgerService.deleteAccount(manId);
    }
}
