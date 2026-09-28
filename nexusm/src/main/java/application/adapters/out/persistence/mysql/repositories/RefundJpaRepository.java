package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.RefundJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA de Refund.
 */
public interface RefundJpaRepository extends JpaRepository<RefundJpaEntity, String> {

    Optional<RefundJpaEntity> findByReturnId(String returnId);
}
