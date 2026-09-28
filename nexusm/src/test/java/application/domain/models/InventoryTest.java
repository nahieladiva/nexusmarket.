package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import application.domain.enums.MovementType;
import application.domain.exceptions.InsufficientStockException;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link Inventory}: stock nunca negativo y trazabilidad de movimientos.
 */
class InventoryTest {

    private Inventory inventory(int onHand) {
        return Inventory.create(ProductId.random(), WarehouseId.random(), Quantity.of(onHand),
            Quantity.of(2), new WarehouseLocation("A1", "E2", "B3"));
    }

    @Test
    void everyMovementIsRecordedWithItsType() {
        Inventory inventory = inventory(10);
        inventory.receive(Quantity.of(5));
        inventory.reserve(Quantity.of(3));
        inventory.registerSale(Quantity.of(2));
        inventory.registerReturn(Quantity.of(1));
        inventory.adjust(-1, "Conteo físico");
        assertEquals(10, inventory.getOnHand().getValue());
        assertEquals(5, inventory.getMovements().size());
        assertEquals(MovementType.INFLOW, inventory.getMovements().get(0).getType());
        assertEquals(MovementType.ADJUSTMENT, inventory.getMovements().get(4).getType());
    }

    @Test
    void stockCanNeverBeNegative() {
        Inventory inventory = inventory(3);
        assertThrows(InsufficientStockException.class, () -> inventory.reserve(Quantity.of(4)));
        assertThrows(InsufficientStockException.class, () -> inventory.adjust(-5, "Merma"));
        assertEquals(3, inventory.getOnHand().getValue());
    }

    @Test
    void zeroMovementsAreRejected() {
        Inventory inventory = inventory(3);
        assertThrows(IllegalArgumentException.class, () -> inventory.receive(Quantity.zero()));
        assertThrows(IllegalArgumentException.class, () -> inventory.adjust(0, "Nada"));
    }
}
