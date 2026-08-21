package com.financialdistrict.ledger.controllers;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.financialdistrict.ledger.dto.AccountSnapshotResponse;
import com.financialdistrict.ledger.services.LedgerService;

import lombok.RequiredArgsConstructor;

// Plain @Controller (not @RestController, which would force @ResponseBody
// and serialize the returned view name as a literal string instead of
// resolving it). Renders the same snapshot history as
// GET /internal/accounts/{manId}/snapshots as an HTML page instead of JSON,
// via the Thymeleaf template at templates/account-chart.html.
@Controller
@RequestMapping("/internal")
@RequiredArgsConstructor
public class AccountChartController {

    private final LedgerService ledgerService;

    @GetMapping("/accounts/{manId}/chart")
    public String getAccountChart(@PathVariable String manId, Model model) {
        List<AccountSnapshotResponse> snapshots = ledgerService.getSnapshots(manId);
        AccountSnapshotResponse latest = snapshots.isEmpty() ? null : snapshots.get(snapshots.size() - 1);

        model.addAttribute("manId", manId);
        model.addAttribute("snapshots", snapshots);
        model.addAttribute("latest", latest);
        return "account-chart";
    }
}
