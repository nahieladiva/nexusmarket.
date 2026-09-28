package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.ReturnRequestJpaEntity;
import application.domain.enums.ReturnStatus;
import application.domain.models.ReturnRequest;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import org.springframework.stereotype.Component;

/**
 * Traduce entre {@link ReturnRequest} y {@link ReturnRequestJpaEntity}.
 */
@Component
public class ReturnRequestEntityMapper {

    public ReturnRequestJpaEntity toEntity(ReturnRequest returnRequest) {
        ReturnRequestJpaEntity entity = new ReturnRequestJpaEntity();
        entity.setId(returnRequest.getId().toString());
        entity.setOrderId(returnRequest.getOrderId().toString());
        entity.setBuyerId(returnRequest.getBuyerId().toString());
        entity.setProductId(returnRequest.getProductId().toString());
        entity.setQuantity(returnRequest.getQuantity().getValue());
        entity.setReason(returnRequest.getReason());
        entity.setStatus(returnRequest.getStatus().name());
        entity.setRequestedAt(returnRequest.getRequestedAt());
        entity.setResolvedAt(returnRequest.getResolvedAt());
        return entity;
    }

    public ReturnRequest toDomain(ReturnRequestJpaEntity entity) {
        return new ReturnRequest(ReturnId.of(entity.getId()), OrderId.of(entity.getOrderId()),
            UserId.of(entity.getBuyerId()), ProductId.of(entity.getProductId()),
            Quantity.of(entity.getQuantity()), entity.getReason(),
            ReturnStatus.valueOf(entity.getStatus()), entity.getRequestedAt(), entity.getResolvedAt());
    }
}
