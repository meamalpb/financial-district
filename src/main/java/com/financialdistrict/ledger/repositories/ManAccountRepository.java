package com.financialdistrict.ledger.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financialdistrict.ledger.models.ManAccount;

public interface ManAccountRepository extends JpaRepository<ManAccount, Long> {
    Optional<ManAccount> findByManId(String manId);
    Optional<ManAccount> findByManIdAndSymbol(String manId, String symbol);
    
}
