package com.financialdistrict.price.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import com.financialdistrict.price.dto.PriceBarResponse;
import com.financialdistrict.price.models.Quote;

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

    public List<PriceBarResponse> getPriceHistory(String symbol, LocalDate from, LocalDate to) {
        return priceHistoryService.getHistory(symbol, from, to).stream()
                .map(bar -> new PriceBarResponse(bar.getSymbol(), bar.getDate(), bar.getClose()))
                .toList();
    }
}
