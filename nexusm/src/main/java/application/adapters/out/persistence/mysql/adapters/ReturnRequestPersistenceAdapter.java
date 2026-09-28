package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.ReturnRequestEntityMapper;
import application.adapters.out.persistence.mysql.repositories.ReturnRequestJpaRepository;
import application.domain.models.ReturnRequest;
import application.domain.ports.out.ReturnRequestRepository;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida que implementa {@link ReturnRequestRepository} sobre MySQL.
 */
@Component
public class ReturnRequestPersistenceAdapter implements ReturnRequestRepository {

    private final ReturnRequestJpaRepository jpaRepository;
    private final ReturnRequestEntityMapper mapper;

    public ReturnRequestPersistenceAdapter(ReturnRequestJpaRepository jpaRepository, ReturnRequestEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository es obligatorio");
        this.mapper = Objects.requireNonNull(mapper, "mapper es obligatorio");
    }

    @Override
    @Transactional
    public ReturnRequest save(ReturnRequest returnRequest) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(returnRequest)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReturnRequest> findById(ReturnId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findByOrderId(OrderId orderId) {
        return jpaRepository.findByOrderId(orderId.toString()).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findByBuyerId(UserId buyerId) {
        return jpaRepository.findByBuyerId(buyerId.toString()).stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReturnRequest> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }
}
