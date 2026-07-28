package price_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonProperty;

public record QuoteResponse(
    String symbol,
    String name,
    String exchange,
    String currency,
    LocalDate datetime,
    long timestamp,    
    BigDecimal open,
    BigDecimal high,
    BigDecimal low,
    BigDecimal close,
    long volume,
    BigDecimal previousClose,
    BigDecimal change,
    
    @JsonProperty("percent_change") 
    BigDecimal percentChange,
    
    @JsonProperty("average_volume") 
    long averageVolume,
    
    @JsonProperty("is_market_open") 
    boolean isMarketOpen,
    
    @JsonProperty("fifty_two_week") 
    FiftyTwoWeek fiftyTwoWeek
) {
    public record FiftyTwoWeek(
        BigDecimal low,
        BigDecimal high,
        
        @JsonProperty("low_change") 
        BigDecimal lowChange,
        
        @JsonProperty("high_change") 
        BigDecimal highChange,
        
        @JsonProperty("low_change_percent") 
        BigDecimal lowChangePercent,
        
        @JsonProperty("high_change_percent") 
        BigDecimal highChangePercent,
        
        String range
    ) {}
}