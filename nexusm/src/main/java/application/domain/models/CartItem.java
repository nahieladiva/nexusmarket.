package application.domain.models;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;

import java.util.Objects;

/**
 * Línea del carrito de compras. Es inmutable: cambiar la cantidad
 * produce una nueva instancia.
 */
public final class CartItem {

    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;

    public CartItem(ProductId productId, Quantity quantity, Money unitPrice) {
        this.productId = Objects.requireNonNull(productId, "El producto es obligatorio");
        this.quantity = Objects.requireNonNull(quantity, "La cantidad es obligatoria");
        if (quantity.isZero()) {
            throw new IllegalArgumentException("La cantidad de un item del carrito debe ser mayor a cero");
        }
        this.unitPrice = Objects.requireNonNull(unitPrice, "El precio unitario es obligatorio");
    }

    public CartItem withQuantity(Quantity newQuantity) {
        return new CartItem(productId, newQuantity, unitPrice);
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity.getValue());
    }

    public OrderItem toOrderItem() {
        return new OrderItem(productId, quantity, unitPrice);
    }

    public ProductId getProductId() {
        return productId;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CartItem other)) {
            return false;
        }
        return productId.equals(other.productId) && quantity.equals(other.quantity)
            && unitPrice.equals(other.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }
}
