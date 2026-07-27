package price_service.providers;

import java.math.BigDecimal;


public class MockPriceProvider
        implements PriceProvider{

            @Override
            public BigDecimal getCurrentPrice(String symbol){
                return new BigDecimal("74.32");
            }       

        }
        