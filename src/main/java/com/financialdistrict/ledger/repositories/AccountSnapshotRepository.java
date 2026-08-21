package com.financialdistrict.ledger.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financialdistrict.ledger.models.AccountSnapshot;

public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, Long> {
    List<AccountSnapshot> findByManIdOrderByTimestampAsc(String manId);
}
