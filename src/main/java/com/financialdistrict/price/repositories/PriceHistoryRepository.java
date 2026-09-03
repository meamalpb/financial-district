package com.financialdistrict.price.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.financialdistrict.price.dto.PriceHistorySummaryResponse;
import com.financialdistrict.price.models.PriceHistory;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {
    Optional<PriceHistory> findBySymbolAndDate(String symbol, LocalDate date);
    List<PriceHistory> findBySymbolOrderByDateAsc(String symbol);
    List<PriceHistory> findBySymbolAndDateBetweenOrderByDateAsc(String symbol, LocalDate from, LocalDate to);

    @Query("""
            SELECT new com.financialdistrict.price.dto.PriceHistorySummaryResponse(
                p.symbol, MIN(p.date), MAX(p.date), COUNT(p))
            FROM PriceHistory p
            GROUP BY p.symbol
            ORDER BY p.symbol
            """)
    List<PriceHistorySummaryResponse> getSummaries();
}
