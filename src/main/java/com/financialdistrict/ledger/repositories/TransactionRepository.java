package com.financialdistrict.ledger.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financialdistrict.ledger.models.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByManId(String manId);
}
