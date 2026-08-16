package ledger_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// timestamp is optional: live buys omit it and get LedgerService's LocalDateTime.now();
// simulated historical buys set it to the price bar's date so the transaction is dated correctly.
public record BuyRequest(String manId, String symbol, BigDecimal amountToSpend, BigDecimal price, LocalDateTime timestamp) {}
