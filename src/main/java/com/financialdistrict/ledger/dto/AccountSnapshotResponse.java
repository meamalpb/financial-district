package com.financialdistrict.ledger.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record AccountSnapshotResponse(
        String manId,
        String symbol,
        Long transactionId,
        BigDecimal price,
        BigDecimal bankBalance,
        BigDecimal sharesOwned,
        BigDecimal costBasis,
        BigDecimal marketValue,
        BigDecimal gain,
        BigDecimal gainPercent,
        LocalDateTime timestamp) {
}
