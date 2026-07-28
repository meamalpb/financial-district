package price_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import price_service.dto.PriceResponse;
import price_service.models.Quote;
import price_service.services.PriceService;

@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceService priceService;

    @GetMapping("/{symbol}")
    public PriceResponse getLatestPrice(@PathVariable String symbol) {
        return new PriceResponse(
                symbol,
                priceService.twelveDataGetCurrentPrice(symbol));
    }

    @GetMapping("/quote/{symbol}")
    public Quote getQuote(@PathVariable String symbol) {
        return priceService.twelveDataGetQuote(symbol);
    }
}