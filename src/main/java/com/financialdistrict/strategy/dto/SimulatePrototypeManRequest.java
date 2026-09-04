package com.financialdistrict.strategy.dto;

import java.math.BigDecimal;

public record SimulatePrototypeManRequest(String symbol, BigDecimal dailyIncome) {
}
