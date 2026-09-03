package com.financialdistrict.ledger.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.financialdistrict.ledger.dto.AccountResponse;
import com.financialdistrict.ledger.services.LedgerService;
import com.financialdistrict.price.services.PriceService;

import lombok.RequiredArgsConstructor;

// Plain @Controller (not @RestController) so the returned view name is
// resolved against templates/index.html rather than serialized as JSON.
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final LedgerService ledgerService;
    private final PriceService priceService;

    @GetMapping("/")
    public String index(Model model) {
        List<AccountResponse> accounts = ledgerService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        model.addAttribute("priceHistorySummaries", priceService.getPriceHistorySummaries());
        return "index";
    }

    @GetMapping("/dummy-data")
    public String dummyData(Model model) {
        List<AccountResponse> accounts = ledgerService.getTestAccounts();
        model.addAttribute("accounts", accounts);
        return "dummy-data";
    }
}
