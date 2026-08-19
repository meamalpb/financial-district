package com.financialdistrict.price.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financialdistrict.price.models.Quote;

public interface QuoteRepository extends JpaRepository<Quote, Long> {
    Optional<Quote> findTopBySymbolOrderByCreatedAtDesc(String symbol);
}
