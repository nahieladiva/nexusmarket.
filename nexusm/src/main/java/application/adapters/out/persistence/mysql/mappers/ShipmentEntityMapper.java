package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.ShipmentJpaEntity;
import application.domain.enums.ShipmentStatus;
import application.domain.models.Shipment;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import org.springframework.stereotype.Component;

/**
 * Traduce entre {@link Shipment} y {@link ShipmentJpaEntity}.
 */
@Component
public class ShipmentEntityMapper {

    public ShipmentJpaEntity toEntity(Shipment shipment) {
        ShipmentJpaEntity entity = new ShipmentJpaEntity();
        entity.setId(shipment.getId().toString());
        entity.setOrderId(shipment.getOrderId().toString());
        entity.setWarehouseId(shipment.getWarehouseId().toString());
        Address address = shipment.getShippingAddress();
        entity.setStreet(address.getStreet());
        entity.setCity(address.getCity());
        entity.setState(address.getState());
        entity.setZipCode(address.getZipCode());
        entity.setCountry(address.getCountry());
        entity.setLogisticOperatorId(shipment.getLogisticOperatorId() == null
            ? null : shipment.getLogisticOperatorId().toString());
        entity.setTrackingNumber(shipment.getTrackingNumber());
        entity.setStatus(shipment.getStatus().name());
        entity.setCreatedAt(shipment.getCreatedAt());
        entity.setShippedAt(shipment.getShippedAt());
        entity.setDeliveredAt(shipment.getDeliveredAt());
        return entity;
    }

    public Shipment toDomain(ShipmentJpaEntity entity) {
        return new Shipment(ShipmentId.of(entity.getId()), OrderId.of(entity.getOrderId()),
            WarehouseId.of(entity.getWarehouseId()),
            Address.of(entity.getStreet(), entity.getCity(), entity.getState(),
                entity.getZipCode(), entity.getCountry()),
            entity.getLogisticOperatorId() == null ? null : UserId.of(entity.getLogisticOperatorId()),
            entity.getTrackingNumber(), ShipmentStatus.valueOf(entity.getStatus()),
            entity.getCreatedAt(), entity.getShippedAt(), entity.getDeliveredAt());
    }
}
