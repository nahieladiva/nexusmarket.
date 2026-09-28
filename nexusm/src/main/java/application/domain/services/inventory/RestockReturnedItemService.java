package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.User;
import application.domain.services.DomainService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

/**
 * Reingresa a la bodega las unidades de una devolución recibida
 * (movimiento {@code RETURN}).
 */
@DomainService
public class RestockReturnedItemService {

    private final ConsultInventoryService consultInventoryService;
    private final RegisterInventoryMovementService registerInventoryMovementService;

    public RestockReturnedItemService(ConsultInventoryService consultInventoryService,
                                      RegisterInventoryMovementService registerInventoryMovementService) {
        this.consultInventoryService = consultInventoryService;
        this.registerInventoryMovementService = registerInventoryMovementService;
    }

    public Inventory execute(User operator, ProductId productId, WarehouseId warehouseId,
                             Quantity quantity) {
        Inventory inventory = consultInventoryService.findByProductAndWarehouse(productId, warehouseId);
        InventoryMovement movement = inventory.registerReturn(quantity);
        return registerInventoryMovementService.execute(operator, inventory, movement);
    }
}
