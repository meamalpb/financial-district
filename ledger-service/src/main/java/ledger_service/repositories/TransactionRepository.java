package ledger_service.repositories;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import ledger_service.models.Transaction;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByManId(String manId);
}
