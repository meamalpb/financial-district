package com.financialdistrict.strategy.dto;

import java.math.BigDecimal;

public record SimulateSellingManRequest(String symbol, BigDecimal dailyIncome, BigDecimal sellAmount,
        Integer sellDay) {
}
