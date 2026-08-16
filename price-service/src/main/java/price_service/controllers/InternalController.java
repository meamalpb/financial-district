package price_service.controllers;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import price_service.dto.PriceBarResponse;
import price_service.models.Quote;
import price_service.services.PriceService;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
public class InternalController {

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
    public List<PriceBarResponse> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return priceService.getPriceHistory(symbol, from, to);
    }
}