package ledger_service.dto;

import java.math.BigDecimal;

public record BuyRequest(String manId, String symbol, BigDecimal amountToSpend, BigDecimal price) {}
