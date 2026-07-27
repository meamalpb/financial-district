package price_service.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import price_service.dto.PriceResponse;
import price_service.dto.TempLatestPriceDTO;
import price_service.providers.PriceProvider;
import price_service.providers.TwelveDataProvider;

@RestController
@RequestMapping("/prices")
@RequiredArgsConstructor
public class PriceController {

    private final PriceProvider priceProvider;
    private final TwelveDataProvider twelveDataProvider;



    @GetMapping("/{symbol}")
    public PriceResponse getPrice(@PathVariable String symbol) {
        return new PriceResponse(
                symbol,
                priceProvider.getCurrentPrice(symbol)
        );
    }

    @GetMapping("/check/{symbol}")
    public TempLatestPriceDTO getLatestPrice(@PathVariable String symbol) {
        return new TempLatestPriceDTO( twelveDataProvider.getLatestPrice(symbol) );
    }
}