package ledger_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record AccountResponse(
        String manId,
        String symbol,
        BigDecimal bankBalance,
        BigDecimal sharesOwned,
        BigDecimal costBasis,
        BigDecimal marketValue,
        BigDecimal gain,
        BigDecimal gainPercent,
        LocalDateTime updatedAt) {
}
