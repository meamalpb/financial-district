package com.financialdistrict.ledger.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// One simulated bar's ledger effects: a contribution credited to the bank
// balance, plus an optional buy. A whole simulation run's bars are handed to
// LedgerService as a list of these so it can persist them with
// jdbcTemplate.batchUpdate instead of one round trip per bar.
public record SimulationBarEvent(
        BigDecimal shares,
        BigDecimal contribution,
        BigDecimal price,
        boolean buy,
        boolean sell,
        BigDecimal amountToSpend,
        LocalDateTime timestamp) {}
