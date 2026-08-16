package strategy_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PriceBarResponse {
    private LocalDate date;
    private BigDecimal close;
}
