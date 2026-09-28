package application.domain.models;

import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

import java.util.Objects;

/**
 * Entidad que representa una línea de una orden ({@link Order}).
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>La cantidad debe ser mayor a cero.</li>
 *   <li>El subtotal se calcula siempre como {@code unitPrice * quantity}.</li>
 * </ul>
 */
public final class OrderItem {

    private final ProductId productId;
    private final Quantity quantity;
    private final Money unitPrice;
    private final WarehouseId warehouseId;

    public OrderItem(ProductId productId, Quantity quantity, Money unitPrice) {
        this(productId, quantity, unitPrice, null);
    }

    /**
     * @param warehouseId bodega de la que se reservó el stock; {@code null} para
     *                    productos digitales o ítems aún sin asignar.
     */
    public OrderItem(ProductId productId, Quantity quantity, Money unitPrice,
                     WarehouseId warehouseId) {
        this.productId = Objects.requireNonNull(productId, "El producto es obligatorio");
        this.quantity = Objects.requireNonNull(quantity, "La cantidad es obligatoria");
        if (quantity.getValue() <= 0) {
            throw new IllegalArgumentException("La cantidad de un item debe ser mayor a cero");
        }
        this.unitPrice = Objects.requireNonNull(unitPrice, "El precio unitario es obligatorio");
        this.warehouseId = warehouseId;
    }

    /** Devuelve una copia del ítem asignada a la bodega indicada. */
    public OrderItem assignedTo(WarehouseId warehouse) {
        return new OrderItem(productId, quantity, unitPrice,
            Objects.requireNonNull(warehouse, "La bodega es obligatoria"));
    }

    public boolean hasWarehouse() {
        return warehouseId != null;
    }

    public WarehouseId getWarehouseId() {
        return warehouseId;
    }

    public Money subtotal() {
        return unitPrice.multiply(quantity.getValue());
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
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OrderItem orderItem = (OrderItem) o;
        return productId.equals(orderItem.productId)
            && quantity.equals(orderItem.quantity)
            && unitPrice.equals(orderItem.unitPrice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, quantity, unitPrice);
    }

    @Override
    public String toString() {
        return "OrderItem{product=" + productId + ", qty=" + quantity + ", unitPrice=" + unitPrice + '}';
    }
}