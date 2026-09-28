package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Libera el stock reservado de un pedido cancelado: devuelve a cada bodega
 * las unidades reservadas mediante un {@code ADJUSTMENT} positivo.
 */
@DomainService
public class ReleaseStockService {

    private final ConsultInventoryService consultInventoryService;
    private final RegisterInventoryMovementService registerInventoryMovementService;

    public ReleaseStockService(ConsultInventoryService consultInventoryService,
                               RegisterInventoryMovementService registerInventoryMovementService) {
        this.consultInventoryService = consultInventoryService;
        this.registerInventoryMovementService = registerInventoryMovementService;
    }

    public void execute(User actor, Order order) {
        for (OrderItem item : order.getItems()) {
            if (!item.hasWarehouse()) {
                continue;
            }
            Inventory inventory = consultInventoryService.findByProductAndWarehouse(
                item.getProductId(), item.getWarehouseId());
            InventoryMovement movement = inventory.adjust(item.getQuantity().getValue(),
                "Liberación de reserva del pedido " + order.getId());
            registerInventoryMovementService.execute(actor, inventory, movement);
        }
    }
}
