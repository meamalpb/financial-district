package strategy_service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PriceResponse {
    private String symbol;
    private BigDecimal close;
}