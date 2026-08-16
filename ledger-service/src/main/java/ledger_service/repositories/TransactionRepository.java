package ledger_service.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import ledger_service.models.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByManId(String manId);
}
