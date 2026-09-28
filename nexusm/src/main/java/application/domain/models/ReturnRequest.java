package application.domain.models;

import application.domain.enums.OrderStatus;
import application.domain.enums.ReturnStatus;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.ReturnId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Solicitud de devolución (Return) de un producto de un pedido entregado.
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Solo se solicita sobre un pedido {@code DELIVERED}.</li>
 *   <li>El producto debe pertenecer al pedido y la cantidad no puede superar
 *       la comprada.</li>
 *   <li>El pedido original no se modifica (es inmutable); la devolución es
 *       una entidad independiente.</li>
 * </ul>
 */
public final class ReturnRequest {

    private final ReturnId id;
    private final OrderId orderId;
    private final UserId buyerId;
    private final ProductId productId;
    private final Quantity quantity;
    private final String reason;
    private ReturnStatus status;
    private final LocalDateTime requestedAt;
    private LocalDateTime resolvedAt;

    public ReturnRequest(ReturnId id, OrderId orderId, UserId buyerId, ProductId productId,
                         Quantity quantity, String reason, ReturnStatus status,
                         LocalDateTime requestedAt, LocalDateTime resolvedAt) {
        this.id = Objects.requireNonNull(id, "El id de la devolución es obligatorio");
        this.orderId = Objects.requireNonNull(orderId, "El pedido es obligatorio");
        this.buyerId = Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        this.productId = Objects.requireNonNull(productId, "El producto es obligatorio");
        this.quantity = Objects.requireNonNull(quantity, "La cantidad es obligatoria");
        if (quantity.isZero()) {
            throw new IllegalArgumentException("La cantidad a devolver debe ser mayor a cero");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("El motivo de la devolución es obligatorio");
        }
        this.reason = reason.trim();
        this.status = status == null ? ReturnStatus.REQUESTED : status;
        this.requestedAt = requestedAt == null ? LocalDateTime.now() : requestedAt;
        this.resolvedAt = resolvedAt;
    }

    public static ReturnRequest request(Order order, ProductId productId, Quantity quantity,
                                        String reason) {
        Objects.requireNonNull(order, "El pedido es obligatorio");
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Solo se pueden devolver productos de pedidos entregados");
        }
        OrderItem purchased = order.getItems().stream()
            .filter(item -> item.getProductId().equals(productId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "El producto " + productId + " no pertenece al pedido " + order.getId()));
        if (quantity.compareTo(purchased.getQuantity()) > 0) {
            throw new IllegalArgumentException("No se puede devolver más de lo comprado");
        }
        return new ReturnRequest(ReturnId.random(), order.getId(), order.getBuyerId(), productId,
            quantity, reason, ReturnStatus.REQUESTED, LocalDateTime.now(), null);
    }

    public void approve() {
        transitionTo(ReturnStatus.APPROVED);
        this.resolvedAt = LocalDateTime.now();
    }

    public void reject() {
        transitionTo(ReturnStatus.REJECTED);
        this.resolvedAt = LocalDateTime.now();
    }

    /** La bodega recibe físicamente el producto devuelto. */
    public void markAsReceived() {
        transitionTo(ReturnStatus.RECEIVED);
    }

    private void transitionTo(ReturnStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException("Devolución", status, target);
        }
        this.status = target;
    }

    public ReturnId getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public String getReason() {
        return reason;
    }

    public ReturnStatus getStatus() {
        return status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
}
