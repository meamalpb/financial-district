package com.financialdistrict.price.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.financialdistrict.price.dto.PriceBarResponse;
import com.financialdistrict.price.dto.PriceHistorySummaryResponse;
import com.financialdistrict.price.models.Quote;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final TwelveDataService twelveDataService;
    private final PriceHistoryService priceHistoryService;

    public BigDecimal twelveDataGetCurrentPrice(String symbol) {
        return twelveDataService.getCurrentPrice(symbol);
    }

    public Quote twelveDataGetQuote(String symbol) {
        return twelveDataService.getQuote(symbol);
    }

    public int backfillPriceHistory(String symbol) {
        return priceHistoryService.backfill(symbol);
    }

    public List<PriceBarResponse> getPriceHistory(String symbol) {
        return priceHistoryService.getHistory(symbol).stream()
                .map(bar -> new PriceBarResponse(bar.getSymbol(), bar.getDate(), bar.getClose()))
                .toList();
    }

    public List<PriceHistorySummaryResponse> getPriceHistorySummaries() {
        return priceHistoryService.getSummaries();
    }
}
