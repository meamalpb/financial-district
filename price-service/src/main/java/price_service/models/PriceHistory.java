package price_service.models;

import jakarta.persistence.Column;
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
@Table(name = "price_history")
public class PriceHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;
    private LocalDate date;

    @Column(precision = 19, scale = 8)
    private BigDecimal open;
    @Column(precision = 19, scale = 8)
    private BigDecimal high;
    @Column(precision = 19, scale = 8)
    private BigDecimal low;
    @Column(precision = 19, scale = 8)
    private BigDecimal close;
    private long volume;

    // Metadata field to track when this row was saved
    private LocalDateTime createdAt;
}
