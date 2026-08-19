package com.financialdistrict.price.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financialdistrict.price.models.PriceHistory;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    Optional<PriceHistory> findBySymbolAndDate(String symbol, LocalDate date);

    List<PriceHistory> findBySymbolAndDateBetweenOrderByDateAsc(String symbol, LocalDate from, LocalDate to);
}
