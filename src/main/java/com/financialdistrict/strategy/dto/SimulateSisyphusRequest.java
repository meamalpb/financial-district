package com.financialdistrict.strategy.dto;

import java.math.BigDecimal;

public record SimulateSisyphusRequest(String symbol, BigDecimal dailyIncome) {
}
