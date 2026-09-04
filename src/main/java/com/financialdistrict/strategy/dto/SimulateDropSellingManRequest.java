package com.financialdistrict.strategy.dto;

import java.math.BigDecimal;

public record SimulateDropSellingManRequest(String symbol, BigDecimal dailyIncome, BigDecimal sellAmount,
        Integer trailingWindow) {
}
