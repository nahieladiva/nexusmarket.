package application.domain.models;

import application.domain.enums.OrderStatus;
import application.domain.exceptions.OrderStateTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.OrderId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Aggregate Root de la orden de compra.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>Una orden debe tener al menos un item.</li>
 *   <li>El total es siempre la suma de los subtotales de sus items.</li>
 *   <li>Los items solo se pueden agregar mientras la orden está en {@code CART}.</li>
 *   <li>Un pedido {@code DELIVERED} o {@code CANCELLED} está finalizado y es inmutable.</li>
 *   <li>Las transiciones de estado siguen la máquina de estados definida en
 *       {@link OrderStatus}; cualquier transición inválida lanza
 *       {@link OrderStateTransitionException}.</li>
 * </ul>
 */
public final class Order {

    private final OrderId id;
    private final UserId buyerId;
    private final List<OrderItem> items;
    private OrderStatus status;
    private Money total;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Order(OrderId id, UserId buyerId, List<OrderItem> items, OrderStatus status,
                 Money total, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El id de orden es obligatorio");
        this.buyerId = Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        this.items = new ArrayList<>(items == null ? List.of() : items);
        if (this.items.isEmpty()) {
            throw new IllegalArgumentException("Una orden debe tener al menos un item");
        }
        this.status = status == null ? OrderStatus.CART : status;
        this.total = total != null ? total : calculateTotal();
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
        this.updatedAt = updatedAt == null ? this.createdAt : updatedAt;
    }

    public static Order create(UserId buyerId, List<OrderItem> items) {
        LocalDateTime now = LocalDateTime.now();
        return new Order(OrderId.random(), buyerId, items, OrderStatus.CART,
            null, now, now);
    }

    /**
     * Solo se pueden agregar ítems mientras el pedido está en {@code CART}.
     */
    public void addItem(OrderItem item) {
        if (status != OrderStatus.CART) {
            throw new OrderStateTransitionException(id, status, status);
        }
        this.items.add(Objects.requireNonNull(item, "El item es obligatorio"));
        this.total = calculateTotal();
        this.updatedAt = LocalDateTime.now();
    }

    /** El comprador confirma el carrito: el pedido queda pendiente de pago. */
    public void checkout() {
        transitionTo(OrderStatus.PENDING_PAYMENT);
    }

    /**
     * Registra la bodega de la que sale cada producto físico. Solo se permite
     * mientras el pedido está pendiente de pago (justo después del checkout).
     */
    public void assignWarehouses(Map<ProductId, WarehouseId> allocation) {
        Objects.requireNonNull(allocation, "La asignación de bodegas es obligatoria");
        if (status != OrderStatus.PENDING_PAYMENT) {
            throw new OrderStateTransitionException(id, status, status);
        }
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            WarehouseId warehouse = allocation.get(item.getProductId());
            if (warehouse != null) {
                items.set(i, item.assignedTo(warehouse));
            }
        }
        this.updatedAt = LocalDateTime.now();
    }

    /** Busca la línea del pedido correspondiente a un producto. */
    public java.util.Optional<OrderItem> findItem(ProductId productId) {
        return items.stream().filter(item -> item.getProductId().equals(productId)).findFirst();
    }

    public boolean belongsTo(UserId userId) {
        return buyerId.equals(userId);
    }

    /** Se registra el pago del pedido. */
    public void markAsPaid() {
        transitionTo(OrderStatus.PAID);
    }

    public void ship() {
        transitionTo(OrderStatus.SHIPPED);
    }

    public void deliver() {
        transitionTo(OrderStatus.DELIVERED);
    }

    public void cancel() {
        transitionTo(OrderStatus.CANCELLED);
    }

    /**
     * Un pedido entregado o cancelado es inmutable.
     */
    public boolean isFinal() {
        return status.isFinal();
    }

    private void transitionTo(OrderStatus target) {
        if (!this.status.canTransitionTo(target)) {
            throw new OrderStateTransitionException(id, this.status, target);
        }
        this.status = target;
        this.updatedAt = LocalDateTime.now();
    }

    private Money calculateTotal() {
        Money result = null;
        for (OrderItem item : items) {
            result = (result == null) ? item.subtotal() : result.add(item.subtotal());
        }
        return result;
    }

    public OrderId getId() {
        return id;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Money getTotal() {
        return total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}