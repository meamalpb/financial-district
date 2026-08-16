package strategy_service.dto;

import java.time.LocalDate;
import lombok.Builder;

@Builder
public record SimulationResult(
        String manId,
        String symbol,
        LocalDate from,
        LocalDate to,
        int barsProcessed,
        int buysExecuted,
        LedgerAccountResponse account) {
}
