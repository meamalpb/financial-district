package com.financialdistrict.price.providers;

import java.math.BigDecimal;

public interface PriceProvider {

    BigDecimal getCurrentPrice(String symbol);

}
