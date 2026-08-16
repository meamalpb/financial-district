package strategy_service.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class LedgerAccountResponse {
    private String manId;
    private BigDecimal bankBalance;
    private BigDecimal sharesOwned;
    private BigDecimal costBasis;
    private BigDecimal marketValue;
    private BigDecimal gain;
    private BigDecimal gainPercent;
}
