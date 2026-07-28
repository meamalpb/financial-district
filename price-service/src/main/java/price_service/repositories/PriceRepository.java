package price_service.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;
import price_service.models.Price;

public interface PriceRepository extends MongoRepository<Price, String> {
}