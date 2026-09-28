package application.domain.services.inventory;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.Inventory;
import application.domain.models.User;
import application.domain.ports.out.InventoryRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.ValidateActiveUserService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;

/**
 * Consulta de inventario. Requiere un usuario activo.
 */
@DomainService
public class ConsultInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ValidateActiveUserService validateActiveUserService;

    public ConsultInventoryService(InventoryRepository inventoryRepository,
                                   ValidateActiveUserService validateActiveUserService) {
        this.inventoryRepository = inventoryRepository;
        this.validateActiveUserService = validateActiveUserService;
    }

    public Inventory findByProductAndWarehouse(ProductId productId, WarehouseId warehouseId) {
        return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Inventario", productId + "|" + warehouseId));
    }

    public List<Inventory> findByWarehouse(User actor, WarehouseId warehouseId) {
        validateActiveUserService.execute(actor);
        return inventoryRepository.findByWarehouseId(warehouseId);
    }

    public List<Inventory> findByProduct(User actor, ProductId productId) {
        validateActiveUserService.execute(actor);
        return inventoryRepository.findByProductId(productId);
    }
}
