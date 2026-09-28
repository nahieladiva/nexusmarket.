package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.User;
import application.domain.services.DomainService;
import application.domain.services.authorization.AuthorizeLogisticOperationService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

/**
 * Ajuste manual por conteo físico (movimiento {@code ADJUSTMENT}, delta ±).
 * El stock nunca puede quedar negativo (regla R6). Solo Operador Logístico.
 */
@DomainService
public class AdjustStockService {

    private final ConsultInventoryService consultInventoryService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final RegisterInventoryMovementService registerInventoryMovementService;

    public AdjustStockService(ConsultInventoryService consultInventoryService,
                              AuthorizeLogisticOperationService authorizeLogisticOperationService,
                              RegisterInventoryMovementService registerInventoryMovementService) {
        this.consultInventoryService = consultInventoryService;
        this.authorizeLogisticOperationService = authorizeLogisticOperationService;
        this.registerInventoryMovementService = registerInventoryMovementService;
    }

    public Inventory execute(User operator, ProductId productId, WarehouseId warehouseId,
                             int delta, String reason) {
        authorizeLogisticOperationService.execute(operator);
        Inventory inventory = consultInventoryService.findByProductAndWarehouse(productId, warehouseId);
        InventoryMovement movement = inventory.adjust(delta, reason);
        return registerInventoryMovementService.execute(operator, inventory, movement);
    }
}
