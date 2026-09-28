package application.domain.models;

import application.domain.valueobjects.CartId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Carrito de compras de un comprador (Aggregate Root).
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>Pertenece a un único comprador.</li>
 *   <li>No tiene dos líneas del mismo producto: agregar un producto
 *       existente suma la cantidad.</li>
 *   <li>Un carrito vacío no puede convertirse en pedido.</li>
 * </ul>
 */
public final class Cart {

    private final CartId id;
    private final UserId buyerId;
    private final Map<ProductId, CartItem> items;
    private LocalDateTime updatedAt;

    public Cart(CartId id, UserId buyerId, List<CartItem> items, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "El id del carrito es obligatorio");
        this.buyerId = Objects.requireNonNull(buyerId, "El comprador es obligatorio");
        this.items = new LinkedHashMap<>();
        if (items != null) {
            items.forEach(this::merge);
        }
        this.updatedAt = updatedAt == null ? LocalDateTime.now() : updatedAt;
    }

    public static Cart createFor(UserId buyerId) {
        return new Cart(CartId.random(), buyerId, List.of(), LocalDateTime.now());
    }

    public void addItem(ProductId productId, Quantity quantity, Money unitPrice) {
        merge(new CartItem(productId, quantity, unitPrice));
        touch();
    }

    public void changeQuantity(ProductId productId, Quantity newQuantity) {
        CartItem current = requireItem(productId);
        if (newQuantity.isZero()) {
            items.remove(productId);
        } else {
            items.put(productId, current.withQuantity(newQuantity));
        }
        touch();
    }

    public void removeItem(ProductId productId) {
        requireItem(productId);
        items.remove(productId);
        touch();
    }

    public void clear() {
        items.clear();
        touch();
    }

    /**
     * Convierte el carrito en un pedido pendiente de pago y lo vacía.
     */
    public Order checkout() {
        if (items.isEmpty()) {
            throw new IllegalStateException("No se puede generar un pedido con el carrito vacío");
        }
        List<OrderItem> orderItems = new ArrayList<>();
        items.values().forEach(item -> orderItems.add(item.toOrderItem()));
        Order order = Order.create(buyerId, orderItems);
        order.checkout();
        clear();
        return order;
    }

    public Money total() {
        Money result = null;
        for (CartItem item : items.values()) {
            result = (result == null) ? item.subtotal() : result.add(item.subtotal());
        }
        return result;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    private void merge(CartItem item) {
        Objects.requireNonNull(item, "El item es obligatorio");
        CartItem existing = items.get(item.getProductId());
        if (existing == null) {
            items.put(item.getProductId(), item);
        } else {
            items.put(item.getProductId(),
                existing.withQuantity(existing.getQuantity().add(item.getQuantity())));
        }
    }

    private CartItem requireItem(ProductId productId) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new IllegalArgumentException("El producto " + productId + " no está en el carrito");
        }
        return item;
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    public CartId getId() {
        return id;
    }

    public UserId getBuyerId() {
        return buyerId;
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(new ArrayList<>(items.values()));
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
