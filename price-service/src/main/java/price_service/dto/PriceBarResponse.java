package price_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PriceBarResponse(String symbol, LocalDate date, BigDecimal close) {}
