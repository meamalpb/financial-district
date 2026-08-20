package com.financialdistrict.price.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financialdistrict.price.dto.PriceBarResponse;
import com.financialdistrict.price.models.Quote;
import com.financialdistrict.price.services.PriceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @GetMapping("/prices/{symbol}")
    public Quote getQuote(@PathVariable String symbol) {
        return priceService.twelveDataGetQuote(symbol);
    }

    @PostMapping("/prices/{symbol}/backfill")
    public int backfillPriceHistory(@PathVariable String symbol) {
        return priceService.backfillPriceHistory(symbol);
    }

    @GetMapping("/prices/{symbol}/history")
    public List<PriceBarResponse> getPriceHistory(@PathVariable String symbol) {
        return priceService.getPriceHistory(symbol);
    }
}
