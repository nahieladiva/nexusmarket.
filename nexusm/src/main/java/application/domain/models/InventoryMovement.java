package application.domain.models;

import application.domain.enums.MovementType;
import application.domain.valueobjects.InventoryId;
import application.domain.valueobjects.MovementId;
import application.domain.valueobjects.Quantity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Registro inmutable de un movimiento de inventario (entrada, reserva,
 * venta, ajuste o devolución). Lo genera {@link Inventory} cada vez que
 * cambia el stock, para dejar trazabilidad.
 */
public final class InventoryMovement {

    private final MovementId id;
    private final InventoryId inventoryId;
    private final MovementType type;
    private final Quantity quantity;
    private final Quantity resultingOnHand;
    private final String reason;
    private final LocalDateTime occurredAt;

    public InventoryMovement(MovementId id, InventoryId inventoryId, MovementType type,
                             Quantity quantity, Quantity resultingOnHand, String reason,
                             LocalDateTime occurredAt) {
        this.id = Objects.requireNonNull(id, "El id del movimiento es obligatorio");
        this.inventoryId = Objects.requireNonNull(inventoryId, "El inventario es obligatorio");
        this.type = Objects.requireNonNull(type, "El tipo de movimiento es obligatorio");
        this.quantity = Objects.requireNonNull(quantity, "La cantidad es obligatoria");
        if (quantity.isZero()) {
            throw new IllegalArgumentException("Un movimiento de inventario no puede ser de cero unidades");
        }
        this.resultingOnHand =
            Objects.requireNonNull(resultingOnHand, "El stock resultante es obligatorio");
        this.reason = reason;
        this.occurredAt = occurredAt == null ? LocalDateTime.now() : occurredAt;
    }

    public static InventoryMovement record(InventoryId inventoryId, MovementType type,
                                           Quantity quantity, Quantity resultingOnHand,
                                           String reason) {
        return new InventoryMovement(MovementId.random(), inventoryId, type, quantity,
            resultingOnHand, reason, LocalDateTime.now());
    }

    public MovementId getId() {
        return id;
    }

    public InventoryId getInventoryId() {
        return inventoryId;
    }

    public MovementType getType() {
        return type;
    }

    public Quantity getQuantity() {
        return quantity;
    }

    public Quantity getResultingOnHand() {
        return resultingOnHand;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
