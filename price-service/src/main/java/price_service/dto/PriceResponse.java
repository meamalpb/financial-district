package price_service.dto;

import java.math.BigDecimal;

public record PriceResponse(String symbol, BigDecimal price) {}