package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.RefundJpaEntity;
import application.domain.enums.RefundStatus;
import application.domain.models.Refund;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;

import org.springframework.stereotype.Component;

/**
 * Traduce entre {@link Refund} y {@link RefundJpaEntity}.
 */
@Component
public class RefundEntityMapper {

    public RefundJpaEntity toEntity(Refund refund) {
        RefundJpaEntity entity = new RefundJpaEntity();
        entity.setId(refund.getId().toString());
        entity.setReturnId(refund.getReturnId().toString());
        entity.setOrderId(refund.getOrderId().toString());
        entity.setAmount(refund.getAmount().getAmount());
        entity.setCurrency(refund.getAmount().getCurrency().getCurrencyCode());
        entity.setStatus(refund.getStatus().name());
        entity.setCreatedAt(refund.getCreatedAt());
        entity.setCompletedAt(refund.getCompletedAt());
        return entity;
    }

    public Refund toDomain(RefundJpaEntity entity) {
        return new Refund(RefundId.of(entity.getId()), ReturnId.of(entity.getReturnId()),
            OrderId.of(entity.getOrderId()),
            Money.of(entity.getAmount().toPlainString(), entity.getCurrency()),
            RefundStatus.valueOf(entity.getStatus()), entity.getCreatedAt(), entity.getCompletedAt());
    }
}
