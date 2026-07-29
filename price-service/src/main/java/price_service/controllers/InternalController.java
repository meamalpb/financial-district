package price_service.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
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
}