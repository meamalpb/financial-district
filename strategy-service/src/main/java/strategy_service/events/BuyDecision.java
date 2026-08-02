package strategy_service.events;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class BuyDecision {
    public String manId;
    public String symbol;
    public BigDecimal amountToSpend;
    public BigDecimal price;
    public boolean shouldBuy;
}