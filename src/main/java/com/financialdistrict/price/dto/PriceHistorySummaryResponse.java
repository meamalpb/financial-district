package com.financialdistrict.price.dto;

import java.time.LocalDate;

public record PriceHistorySummaryResponse(String symbol, LocalDate fromDate, LocalDate toDate, long barCount) {}
