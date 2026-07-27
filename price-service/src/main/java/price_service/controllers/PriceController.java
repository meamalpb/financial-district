package price_service.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import price_service.dto.PriceResponse;
import price_service.providers.PriceProvider;

@RestController
@RequestMapping("/prices")
public class PriceController {

    private final PriceProvider priceProvider;

    public PriceController(PriceProvider priceProvider) {
        this.priceProvider = priceProvider;
    }

    @GetMapping("/{symbol}")
    public PriceResponse getPrice(@PathVariable String symbol) {
        return new PriceResponse(
                symbol,
                priceProvider.getCurrentPrice(symbol)
        );
    }
}