package ledger_service.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "man_accounts")
public class ManAccount {

    @Id
    private String id;

    private String manId;
    private String symbol;

    private BigDecimal bankBalance;
    private BigDecimal sharesOwned;
    private BigDecimal costBasis;
    private BigDecimal marketValue;

    private LocalDateTime updatedAt;
}
