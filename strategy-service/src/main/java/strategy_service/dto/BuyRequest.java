package strategy_service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BuyRequest {
    private String manId;
    private String symbol;
    private BigDecimal amountToSpend;
    private BigDecimal price;
}
