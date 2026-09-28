package application.domain.models;

import application.domain.enums.RefundStatus;
import application.domain.enums.ReturnStatus;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.RefundId;
import application.domain.valueobjects.ReturnId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reembolso asociado a una devolución recibida.
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Solo se genera cuando la devolución está {@code RECEIVED}.</li>
 *   <li>El monto no puede ser cero.</li>
 * </ul>
 */
public final class Refund {

    private final RefundId id;
    private final ReturnId returnId;
    private final OrderId orderId;
    private final Money amount;
    private RefundStatus status;
    private final LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public Refund(RefundId id, ReturnId returnId, OrderId orderId, Money amount,
                  RefundStatus status, LocalDateTime createdAt, LocalDateTime completedAt) {
        this.id = Objects.requireNonNull(id, "El id del reembolso es obligatorio");
        this.returnId = Objects.requireNonNull(returnId, "La devolución es obligatoria");
        this.orderId = Objects.requireNonNull(orderId, "El pedido es obligatorio");
        this.amount = Objects.requireNonNull(amount, "El monto es obligatorio");
        if (amount.isZero()) {
            throw new IllegalArgumentException("El monto del reembolso debe ser mayor a cero");
        }
        this.status = status == null ? RefundStatus.PENDING : status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.completedAt = completedAt;
    }

    public static Refund createFor(ReturnRequest returnRequest, Money amount) {
        Objects.requireNonNull(returnRequest, "La devolución es obligatoria");
        if (returnRequest.getStatus() != ReturnStatus.RECEIVED) {
            throw new IllegalStateException("Solo se reembolsa una devolución recibida en bodega");
        }
        return new Refund(RefundId.random(), returnRequest.getId(), returnRequest.getOrderId(),
            amount, RefundStatus.PENDING, LocalDateTime.now(), null);
    }

    public void complete() {
        transitionTo(RefundStatus.COMPLETED);
        this.completedAt = LocalDateTime.now();
    }

    public void fail() {
        transitionTo(RefundStatus.FAILED);
    }

    private void transitionTo(RefundStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException("Reembolso", status, target);
        }
        this.status = target;
    }

    public RefundId getId() {
        return id;
    }

    public ReturnId getReturnId() {
        return returnId;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Money getAmount() {
        return amount;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
