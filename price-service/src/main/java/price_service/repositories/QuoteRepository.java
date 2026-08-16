package price_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import price_service.models.Quote;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
        Optional<Quote> findTopBySymbolOrderByCreatedAtDesc(String symbol);

}