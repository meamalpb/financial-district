package price_service.providers;

import java.math.BigDecimal;

public interface PriceProvider {

    BigDecimal getCurrentPrice(String symbol);

}