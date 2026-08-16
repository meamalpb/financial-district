package ledger_service.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String manId;
    private String symbol;

    @Column(precision = 19, scale = 8)
    private BigDecimal amountSpent;
    @Column(precision = 19, scale = 8)
    private BigDecimal shares;
    @Column(precision = 19, scale = 8)
    private BigDecimal price;

    private LocalDateTime timestamp;
}
