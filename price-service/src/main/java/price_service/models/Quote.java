package price_service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quotes")
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private String name;
    private String exchange;
    private String currency;
    private LocalDate datetime;
    private long timestamp;

    @Column(precision = 19, scale = 8)
    private BigDecimal open;
    @Column(precision = 19, scale = 8)
    private BigDecimal high;
    @Column(precision = 19, scale = 8)
    private BigDecimal low;
    @Column(precision = 19, scale = 8)
    private BigDecimal close;
    private long volume;
    @Column(precision = 19, scale = 8)
    private BigDecimal previousClose;
    @Column(precision = 19, scale = 8)
    private BigDecimal change;
    @Column(precision = 19, scale = 8)
    private BigDecimal percentChange;
    private long averageVolume;
    private boolean isMarketOpen;

    @Embedded
    private FiftyTwoWeekStats fiftyTwoWeek;

    // Metadata field to track when this row was saved
    private LocalDateTime createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class FiftyTwoWeekStats {
        @Column(name = "fifty_two_week_low", precision = 19, scale = 8)
        private BigDecimal low;
        @Column(name = "fifty_two_week_high", precision = 19, scale = 8)
        private BigDecimal high;
        @Column(name = "fifty_two_week_low_change", precision = 19, scale = 8)
        private BigDecimal lowChange;
        @Column(name = "fifty_two_week_high_change", precision = 19, scale = 8)
        private BigDecimal highChange;
        @Column(name = "fifty_two_week_low_change_percent", precision = 19, scale = 8)
        private BigDecimal lowChangePercent;
        @Column(name = "fifty_two_week_high_change_percent", precision = 19, scale = 8)
        private BigDecimal highChangePercent;
        @Column(name = "fifty_two_week_range")
        private String range;
    }
}