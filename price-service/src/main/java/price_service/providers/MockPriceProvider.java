package price_service.providers;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

@Service
public class MockPriceProvider
        implements PriceProvider{

            @Override
            public BigDecimal getCurrentPrice(String symbol){
                return new BigDecimal("74.32");
            }       

        }
        