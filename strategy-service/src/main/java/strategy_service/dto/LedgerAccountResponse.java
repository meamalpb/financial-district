package strategy_service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LedgerAccountResponse {
    private String manId;
    private BigDecimal bankBalance;
    private BigDecimal sharesOwned;
    private BigDecimal marketValue;
}
