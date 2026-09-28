package application.domain.models;

import application.domain.enums.OrderStatus;
import application.domain.enums.ShipmentStatus;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ShipmentId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Envío de un pedido pagado desde una bodega.
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Solo se crea para un pedido en {@code PAID}.</li>
 *   <li>Solo un Operador Logístico activo puede despachar el envío.</li>
 *   <li>Al despachar se exige número de guía.</li>
 * </ul>
 */
public final class Shipment {

    private final ShipmentId id;
    private final OrderId orderId;
    private final WarehouseId warehouseId;
    private final Address shippingAddress;
    private UserId logisticOperatorId;
    private String trackingNumber;
    private ShipmentStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    public Shipment(ShipmentId id, OrderId orderId, WarehouseId warehouseId,
                    Address shippingAddress, UserId logisticOperatorId, String trackingNumber,
                    ShipmentStatus status, LocalDateTime createdAt, LocalDateTime shippedAt,
                    LocalDateTime deliveredAt) {
        this.id = Objects.requireNonNull(id, "El id del envío es obligatorio");
        this.orderId = Objects.requireNonNull(orderId, "El pedido es obligatorio");
        this.warehouseId = Objects.requireNonNull(warehouseId, "La bodega de origen es obligatoria");
        this.shippingAddress =
            Objects.requireNonNull(shippingAddress, "La dirección de envío es obligatoria");
        this.logisticOperatorId = logisticOperatorId;
        this.trackingNumber = trackingNumber;
        this.status = status == null ? ShipmentStatus.PREPARING : status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.shippedAt = shippedAt;
        this.deliveredAt = deliveredAt;
    }

    public static Shipment prepareFor(Order order, WarehouseId warehouseId, Address shippingAddress) {
        Objects.requireNonNull(order, "El pedido es obligatorio");
        if (order.getStatus() != OrderStatus.PAID) {
            throw new IllegalStateException(
                "Solo se envía un pedido pagado (estado actual: " + order.getStatus() + ")");
        }
        return new Shipment(ShipmentId.random(), order.getId(), warehouseId, shippingAddress,
            null, null, ShipmentStatus.PREPARING, LocalDateTime.now(), null, null);
    }

    public void dispatch(User logisticOperator, String trackingNumber) {
        Objects.requireNonNull(logisticOperator, "El operador logístico es obligatorio");
        logisticOperator.requireActive();
        if (!logisticOperator.isLogisticOperator()) {
            throw new UnauthorizedOperationException("Solo un operador logístico puede despachar envíos");
        }
        if (trackingNumber == null || trackingNumber.isBlank()) {
            throw new IllegalArgumentException("El número de guía es obligatorio para despachar");
        }
        transitionTo(ShipmentStatus.IN_TRANSIT);
        this.logisticOperatorId = logisticOperator.getId();
        this.trackingNumber = trackingNumber.trim();
        this.shippedAt = LocalDateTime.now();
    }

    public void confirmDelivery() {
        transitionTo(ShipmentStatus.DELIVERED);
        this.deliveredAt = LocalDateTime.now();
    }

    private void transitionTo(ShipmentStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException("Envío", status, target);
        }
        this.status = target;
    }

    public ShipmentId getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public WarehouseId getWarehouseId() {
        return warehouseId;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public UserId getLogisticOperatorId() {
        return logisticOperatorId;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public ShipmentStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getShippedAt() {
        return shippedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }
}
