package ledger_service.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import ledger_service.models.ManAccount;

public interface ManAccountRepository extends MongoRepository<ManAccount, String> {
    Optional<ManAccount> findByManId(String manId);
}
