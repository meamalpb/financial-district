package ledger_service.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ledger_service.dto.BuyRequest;
import ledger_service.models.ManAccount;
import ledger_service.services.LedgerService;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

    private final LedgerService ledgerService;

    @PostMapping("/buy")
    public ManAccount buy(@RequestBody BuyRequest request) {
        return ledgerService.applyBuy(request);
    }
}
