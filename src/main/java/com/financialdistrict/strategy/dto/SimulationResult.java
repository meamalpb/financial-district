package com.financialdistrict.strategy.dto;

import java.time.LocalDate;
import lombok.Builder;
import com.financialdistrict.ledger.dto.AccountResponse;

@Builder
public record SimulationResult(
        String manId,
        String symbol,
        LocalDate from,
        LocalDate to,
        int barsProcessed,
        int buysExecuted,
        AccountResponse account) {
}
