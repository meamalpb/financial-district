package price_service.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import price_service.models.Quote;

public interface QuoteRepository extends MongoRepository<Quote, String> {
}