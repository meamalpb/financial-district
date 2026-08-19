package com.financialdistrict.price.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TwelveDataTimeSeriesResponse(
    Meta meta,
    List<Bar> values,
    String status
) {
    public record Meta(
        String symbol,
        String interval,
        String currency,

        @JsonProperty("exchange_timezone")
        String exchangeTimezone,

        String exchange,

        @JsonProperty("mic_code")
        String micCode,

        String type
    ) {}

    public record Bar(
        LocalDate datetime,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        long volume
    ) {}
}
