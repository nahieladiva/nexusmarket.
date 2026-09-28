package application.domain.models;

import application.domain.enums.InvoiceStatus;
import application.domain.enums.OrderStatus;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.InvoiceId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Factura de un pedido.
 *
 * <p>Reglas:</p>
 * <ul>
 *   <li>Solo se emite para un pedido en {@code PENDING_PAYMENT}.</li>
 *   <li>El total de la factura es el total del pedido.</li>
 *   <li>Una factura pagada o anulada no cambia más.</li>
 * </ul>
 */
public final class Invoice {

    private final InvoiceId id;
    private final String invoiceNumber;
    private final OrderId orderId;
    private final UserId buyerId;
    private final Money total;
    private InvoiceStatus status;
    private final LocalDateTime issuedAt;
    private LocalDateTime paidAt;

    public Invoice(InvoiceId id, String invoiceNumber, OrderId orderId, UserId buyerId,
                   Money total, InvoiceStatus status, LocalDateTime issuedAt,
                   LocalDateTime paidAt) {
        this.id = Objects.requireNonNull(id, "El id de la factura es obligatorio");
        if (invoiceNumber == null || invoiceNumber.isBlank()) {
            throw new IllegalArgumentException("El número de factura es obligatorio");
        }
        this.invoiceNumber = invoiceNumber.trim();
        this.orderId = Objects.requireNonNull(orderId, "El pedido es obligatorio");
        this.buyerId = Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        this.total = Objects.requireNonNull(total, "El total es obligatorio");
        this.status = status == null ? InvoiceStatus.ISSUED : status;
        this.issuedAt = issuedAt == null ? LocalDateTime.now() : issuedAt;
        this.paidAt = paidAt;
    }

    public static Invoice issueFor(Order order, String invoiceNumber) {
        Objects.requireNonNull(order, "El pedido es obligatorio");
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                "Solo se factura un pedido pendiente de pago (estado actual: " + order.getStatus() + ")");
        }
        return new Invoice(InvoiceId.random(), invoiceNumber, order.getId(), order.getBuyerId(),
            order.getTotal(), InvoiceStatus.ISSUED, LocalDateTime.now(), null);
    }

    public void markAsPaid() {
        transitionTo(InvoiceStatus.PAID);
        this.paidAt = LocalDateTime.now();
    }

    public void voidInvoice() {
        transitionTo(InvoiceStatus.VOIDED);
    }

    private void transitionTo(InvoiceStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException("Factura", status, target);
        }
        this.status = target;
    }

    public InvoiceId getId() {
        return id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public Money getTotal() {
        return total;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }
}
