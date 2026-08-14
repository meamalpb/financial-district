package price_service.repositories;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import price_service.models.PriceHistory;

public interface PriceHistoryRepository extends MongoRepository<PriceHistory, String> {
    Optional<PriceHistory> findBySymbolAndDate(String symbol, LocalDate date);
}
