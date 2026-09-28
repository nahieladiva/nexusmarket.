package application.domain.services.inventory;

import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.Order;
import application.domain.models.OrderItem;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.services.DomainService;
import application.domain.services.product.ConsultProductService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Reserva el stock de los productos físicos de un pedido (movimiento
 * {@code RESERVATION}) y registra en el pedido la bodega de cada ítem.
 * Los productos digitales no reservan stock.
 */
@DomainService
public class ReserveStockService {

    private final ConsultProductService consultProductService;
    private final AllocateWarehouseService allocateWarehouseService;
    private final RegisterInventoryMovementService registerInventoryMovementService;

    public ReserveStockService(ConsultProductService consultProductService,
                               AllocateWarehouseService allocateWarehouseService,
                               RegisterInventoryMovementService registerInventoryMovementService) {
        this.consultProductService = consultProductService;
        this.allocateWarehouseService = allocateWarehouseService;
        this.registerInventoryMovementService = registerInventoryMovementService;
    }

    public void execute(User actor, Order order) {
        Map<ProductId, WarehouseId> allocation = new LinkedHashMap<>();
        for (OrderItem item : order.getItems()) {
            Product product = consultProductService.findById(item.getProductId());
            if (!product.requiresInventory()) {
                continue;
            }
            Inventory inventory = allocateWarehouseService.execute(item);
            InventoryMovement movement = inventory.reserve(item.getQuantity());
            registerInventoryMovementService.execute(actor, inventory, movement);
            allocation.put(item.getProductId(), inventory.getWarehouseId());
        }
        order.assignWarehouses(allocation);
    }
}
