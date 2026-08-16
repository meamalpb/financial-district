package ledger_service.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ledger_service.models.ManAccount;

public interface ManAccountRepository extends JpaRepository<ManAccount, Long> {
    Optional<ManAccount> findByManId(String manId);
}
