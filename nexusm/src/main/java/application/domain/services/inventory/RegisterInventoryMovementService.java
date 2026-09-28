package application.domain.services.inventory;

import application.domain.enums.OperationType;
import application.domain.events.LowStockEvent;
import application.domain.models.Inventory;
import application.domain.models.InventoryMovement;
import application.domain.models.User;
import application.domain.ports.out.InventoryRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Guarda el inventario tras un movimiento y deja la trazabilidad (regla R7):
 * audita el movimiento y, si el stock queda bajo el punto de reorden,
 * emite un {@link LowStockEvent}.
 */
@DomainService
public class RegisterInventoryMovementService {

    private final InventoryRepository inventoryRepository;
    private final RegisterAuditEventService registerAuditEventService;

    public RegisterInventoryMovementService(InventoryRepository inventoryRepository,
                                            RegisterAuditEventService registerAuditEventService) {
        this.inventoryRepository = inventoryRepository;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Inventory execute(User actor, Inventory inventory, InventoryMovement movement) {
        Inventory saved = inventoryRepository.save(inventory);
        registerAuditEventService.execute(OperationType.INVENTORY_MOVEMENT, actor, inventory.getId(),
            Map.of("productId", inventory.getProductId().toString(),
                "warehouseId", inventory.getWarehouseId().toString(),
                "movementType", movement.getType().name(),
                "quantity", String.valueOf(movement.getQuantity().getValue()),
                "resultingOnHand", String.valueOf(movement.getResultingOnHand().getValue()),
                "reason", movement.getReason() == null ? "" : movement.getReason()));
        if (inventory.isBelowReorderPoint()) {
            registerAuditEventService.execute(new LowStockEvent(inventory.getProductId(),
                inventory.getWarehouseId(), inventory.getOnHand(), inventory.getReorderThreshold(),
                LocalDateTime.now()));
        }
        return saved;
    }
}
