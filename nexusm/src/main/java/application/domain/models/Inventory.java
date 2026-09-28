package application.domain.models;

import application.domain.enums.MovementType;
import application.domain.exceptions.InsufficientStockException;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Inventario de un producto en una bodega (inventario distribuido).
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>Un par (producto, bodega) identifica de forma única el inventario.</li>
 *   <li>El stock nunca puede ser negativo: cualquier salida mayor al
 *       disponible lanza {@link InsufficientStockException}.</li>
 *   <li>Todo cambio de stock queda registrado como {@link InventoryMovement}
 *       con su {@link MovementType}.</li>
 * </ul>
 */
public final class Inventory {

    private final InventoryId id;
    private final ProductId productId;
    private final WarehouseId warehouseId;
    private Quantity onHand;
    private Quantity reorderThreshold;
    private WarehouseLocation location;
    private final List<InventoryMovement> movements = new ArrayList<>();

    public Inventory(InventoryId id, ProductId productId, WarehouseId warehouseId,
                     Quantity onHand, Quantity reorderThreshold, WarehouseLocation location) {
        this.id = Objects.requireNonNull(id, "El id de inventario es obligatorio");
        this.productId = Objects.requireNonNull(productId, "El producto es obligatorio");
        this.warehouseId = Objects.requireNonNull(warehouseId, "El almacén es obligatorio");
        this.onHand = Objects.requireNonNull(onHand, "La cantidad disponible es obligatoria");
        this.reorderThreshold =
            Objects.requireNonNull(reorderThreshold, "El punto de reorden es obligatorio");
        this.location = Objects.requireNonNull(location, "La ubicación es obligatoria");
    }

    public static Inventory create(ProductId productId, WarehouseId warehouseId,
                                   Quantity onHand, Quantity reorderThreshold,
                                   WarehouseLocation location) {
        return new Inventory(InventoryId.random(), productId, warehouseId,
            onHand, reorderThreshold, location);
    }

    /** Entrada de mercancía a la bodega. */
    public InventoryMovement receive(Quantity amount) {
        return add(MovementType.INFLOW, amount, "Entrada de mercancía");
    }

    /** Reserva de stock para un pedido. */
    public InventoryMovement reserve(Quantity amount) {
        return remove(MovementType.RESERVATION, amount, "Reserva para pedido");
    }

    /** Salida definitiva por venta. */
    public InventoryMovement registerSale(Quantity amount) {
        return remove(MovementType.SALE, amount, "Venta");
    }

    /** Reingreso de unidades por devolución. */
    public InventoryMovement registerReturn(Quantity amount) {
        return add(MovementType.RETURN, amount, "Devolución");
    }

    /**
     * Ajuste manual por conteo físico. Un delta positivo suma y uno negativo
     * resta; el resultado nunca puede quedar negativo.
     */
    public InventoryMovement adjust(int delta, String reason) {
        if (delta == 0) {
            throw new IllegalArgumentException("El ajuste de inventario no puede ser cero");
        }
        String motive = (reason == null || reason.isBlank()) ? "Ajuste manual" : reason.trim();
        return delta > 0
            ? add(MovementType.ADJUSTMENT, Quantity.of(delta), motive)
            : remove(MovementType.ADJUSTMENT, Quantity.of(-delta), motive);
    }

    private InventoryMovement add(MovementType type, Quantity amount, String reason) {
        requirePositive(amount);
        this.onHand = this.onHand.add(amount);
        return registerMovement(type, amount, reason);
    }

    private InventoryMovement remove(MovementType type, Quantity amount, String reason) {
        requirePositive(amount);
        if (!isAvailable(amount)) {
            throw new InsufficientStockException(productId, warehouseId, amount, onHand);
        }
        this.onHand = this.onHand.subtract(amount);
        return registerMovement(type, amount, reason);
    }

    private InventoryMovement registerMovement(MovementType type, Quantity amount, String reason) {
        InventoryMovement movement = InventoryMovement.record(id, type, amount, onHand, reason);
        movements.add(movement);
        return movement;
    }

    private static void requirePositive(Quantity amount) {
        Objects.requireNonNull(amount, "La cantidad es obligatoria");
        if (amount.isZero()) {
            throw new IllegalArgumentException("La cantidad del movimiento debe ser mayor a cero");
        }
    }

    public boolean isAvailable(Quantity requested) {
        Objects.requireNonNull(requested, "La cantidad es obligatoria");
        return this.onHand.compareTo(requested) >= 0;
    }

    public boolean isBelowReorderPoint() {
        return this.onHand.compareTo(this.reorderThreshold) < 0;
    }

    public void changeReorderThreshold(Quantity newThreshold) {
        this.reorderThreshold =
            Objects.requireNonNull(newThreshold, "El punto de reorden es obligatorio");
    }

    public InventoryId getId() {
        return id;
    }

    public ProductId getProductId() {
        return productId;
    }

    public WarehouseId getWarehouseId() {
        return warehouseId;
    }

    public Quantity getOnHand() {
        return onHand;
    }

    public Quantity getReorderThreshold() {
        return reorderThreshold;
    }

    public WarehouseLocation getLocation() {
        return location;
    }

    /** Movimientos registrados desde que se cargó el inventario. */
    public List<InventoryMovement> getMovements() {
        return Collections.unmodifiableList(movements);
    }
}
