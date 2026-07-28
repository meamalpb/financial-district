package price_service.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quotes")
public class Quote {

    @Id
    private String id;

    private String symbol;
    private String name;
    private String exchange;
    private String currency;
    private LocalDate datetime;
    private long timestamp;

    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal close;
    private long volume;
    private BigDecimal previousClose;
    private BigDecimal change;
    private BigDecimal percentChange;
    private long averageVolume;
    private boolean isMarketOpen;

    private FiftyTwoWeekStats fiftyTwoWeek;

    // Metadata field to track when this document was saved
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FiftyTwoWeekStats {
        private BigDecimal low;
        private BigDecimal high;
        private BigDecimal lowChange;
        private BigDecimal highChange;
        private BigDecimal lowChangePercent;
        private BigDecimal highChangePercent;
        private String range;
    }
}