package price_service.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import price_service.models.Quote;

@Service
@RequiredArgsConstructor
public class PriceService {

    private final TwelveDataService twelveDataService;

    public BigDecimal twelveDataGetCurrentPrice(String symbol) {
        return twelveDataService.getCurrentPrice(symbol);
    }

    public Quote twelveDataGetQuote(String symbol) {
        return twelveDataService.getQuote(symbol);
    }
}
