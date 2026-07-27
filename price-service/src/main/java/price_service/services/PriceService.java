package price_service.services;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import price_service.providers.PriceProvider;

@RequiredArgsConstructor
@Service
public class PriceService {
    private final PriceProvider priceProvider;


    public BigDecimal twelveDataGetCurrentPrice(String symbol){
        return priceProvider.getCurrentPrice(symbol);
    }
    
}
