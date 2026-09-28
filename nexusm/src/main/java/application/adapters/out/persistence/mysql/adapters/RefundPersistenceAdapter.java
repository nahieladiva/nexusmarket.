package application.adapters.out.persistence.mysql.adapters;

import application.adapters.out.persistence.mysql.mappers.RefundEntityMapper;
import application.adapters.out.persistence.mysql.repositories.RefundJpaRepository;
import application.domain.models.Refund;
import application.domain.ports.out.RefundRepository;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador de salida que implementa {@link RefundRepository} sobre MySQL.
 */
@Component
public class RefundPersistenceAdapter implements RefundRepository {

    private final RefundJpaRepository jpaRepository;
    private final RefundEntityMapper mapper;

    public RefundPersistenceAdapter(RefundJpaRepository jpaRepository, RefundEntityMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository es obligatorio");
        this.mapper = Objects.requireNonNull(mapper, "mapper es obligatorio");
    }

    @Override
    @Transactional
    public Refund save(Refund refund) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(refund)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Refund> findById(RefundId id) {
        return jpaRepository.findById(id.toString()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Refund> findByReturnId(ReturnId returnId) {
        return jpaRepository.findByReturnId(returnId.toString()).map(mapper::toDomain);
    }
}
