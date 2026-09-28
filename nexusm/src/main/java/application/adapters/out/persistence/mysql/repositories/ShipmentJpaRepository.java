package application.adapters.out.persistence.mysql.repositories;

import application.adapters.out.persistence.mysql.entities.ShipmentJpaEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio JPA de Shipment.
 */
public interface ShipmentJpaRepository extends JpaRepository<ShipmentJpaEntity, String> {

    Optional<ShipmentJpaEntity> findByOrderId(String orderId);
}
