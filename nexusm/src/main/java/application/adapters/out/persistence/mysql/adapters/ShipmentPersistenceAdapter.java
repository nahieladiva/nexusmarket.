package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.ShipmentEntityMapper;
import application.adapters.out.persistence.mysql.repositories.ShipmentJpaRepository;
import application.domain.models.Shipment;
import application.domain.ports.out.ShipmentRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida que implementa {@link ShipmentRepository} sobre MySQL.
 */
@Component
public class ShipmentPersistenceAdapter implements ShipmentRepository {

    private final ShipmentJpaRepository jpaRepository;
    private final ShipmentEntityMapper mapper;

    public ShipmentPersistenceAdapter(ShipmentJpaRepository jpaRepository, ShipmentEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository es obligatorio");
        this.mapper = Objects.requireNonNull(mapper, "mapper es obligatorio");
    }

    @Override
    @Transactional
    public Shipment save(Shipment shipment) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(shipment)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shipment> findById(ShipmentId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Shipment> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Shipment> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
