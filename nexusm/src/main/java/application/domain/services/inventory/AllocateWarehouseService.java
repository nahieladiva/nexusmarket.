package application.domain.services.inventory;

import application.domain.exceptions.InsufficientStockException;
import application.domain.models.Inventory;
import application.domain.models.OrderItem;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepository;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Elige la bodega de la que sale un ítem: la primera bodega {@code ACTIVE}
 * con stock suficiente (reglas R6 y R9).
 */
@DomainService
public class AllocateWarehouseService {

    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;

    public AllocateWarehouseService(InventoryRepository inventoryRepository,
                                    WarehouseRepository warehouseRepository) {
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
    }

    public Inventory execute(OrderItem item) {
        Set<WarehouseId> activeWarehouses = warehouseRepository.findAll().stream()
            .filter(Warehouse::isActive)
            .map(Warehouse::getId)
            .collect(Collectors.toSet());
        List<Inventory> inventories = inventoryRepository.findByProductId(item.getProductId());
        return inventories.stream()
            .filter(inventory -> activeWarehouses.contains(inventory.getWarehouseId()))
            .filter(inventory -> inventory.isAvailable(item.getQuantity()))
            .findFirst()
            .orElseThrow(() -> new InsufficientStockException(item.getProductId(), null,
                item.getQuantity(), Quantity.of(inventories.stream()
                    .filter(inventory -> activeWarehouses.contains(inventory.getWarehouseId()))
                    .mapToInt(inventory -> inventory.getOnHand().getValue()).sum())));
    }
}
