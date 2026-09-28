package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.User;
import application.domain.services.DomainService;
import application.domain.services.authorization.AuthorizeLogisticOperationService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

/**
 * Entrada de mercancía a una bodega (movimiento {@code INFLOW}). Solo Operador Logístico.
 */
@DomainService
public class ReceiveStockService {

    private final ConsultInventoryService consultInventoryService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final RegisterInventoryMovementService registerInventoryMovementService;

    public ReceiveStockService(ConsultInventoryService consultInventoryService,
                               AuthorizeLogisticOperationService authorizeLogisticOperationService,
                               RegisterInventoryMovementService registerInventoryMovementService) {
        this.consultInventoryService = consultInventoryService;
        this.authorizeLogisticOperationService = authorizeLogisticOperationService;
        this.registerInventoryMovementService = registerInventoryMovementService;
    }

    public Inventory execute(User operator, ProductId productId, WarehouseId warehouseId,
                             Quantity quantity) {
        authorizeLogisticOperationService.execute(operator);
        Inventory inventory = consultInventoryService.findByProductAndWarehouse(productId, warehouseId);
        InventoryMovement movement = inventory.receive(quantity);
        return registerInventoryMovementService.execute(operator, inventory, movement);
    }
}
