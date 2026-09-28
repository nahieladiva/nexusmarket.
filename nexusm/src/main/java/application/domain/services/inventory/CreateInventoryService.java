package application.domain.services.inventory;

import application.domain.enums.OperationType;
import application.domain.models.Inventory;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.InventoryRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeLogisticOperationService;
import application.domain.services.product.ConsultProductService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Map;

/**
 * El Operador Logístico abre el inventario de un producto físico en una bodega activa.
 * Un par (producto, bodega) solo puede tener un inventario.
 */
@DomainService
public class CreateInventoryService {

    private final InventoryRepository inventoryRepository;
    private final ConsultProductService consultProductService;
    private final ConsultWarehouseService consultWarehouseService;
    private final AuthorizeLogisticOperationService authorizeLogisticOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public CreateInventoryService(InventoryRepository inventoryRepository,
                                  ConsultProductService consultProductService,
                                  ConsultWarehouseService consultWarehouseService,
                                  AuthorizeLogisticOperationService authorizeLogisticOperationService,
                                  RegisterAuditEventService registerAuditEventService) {
        this.inventoryRepository = inventoryRepository;
        this.consultProductService = consultProductService;
        this.consultWarehouseService = consultWarehouseService;
        this.authorizeLogisticOperationService = authorizeLogisticOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Inventory execute(User operator, ProductId productId, WarehouseId warehouseId,
                             Quantity initialStock, Quantity reorderThreshold,
                             WarehouseLocation location) {
        authorizeLogisticOperationService.execute(operator);
        Product product = consultProductService.findById(productId);
        if (!product.requiresInventory()) {
            throw new IllegalArgumentException("Los productos digitales no manejan inventario en bodega");
        }
        Warehouse warehouse = consultWarehouseService.findById(warehouseId);
        if (!warehouse.isActive()) {
            throw new IllegalStateException("La bodega " + warehouseId + " está inactiva");
        }
        if (inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId).isPresent()) {
            throw new IllegalArgumentException("Ya existe inventario de ese producto en esa bodega");
        }
        Inventory inventory = inventoryRepository.save(Inventory.create(productId, warehouseId,
            initialStock, reorderThreshold, location));
        registerAuditEventService.execute(OperationType.INVENTORY_CREATED, operator, inventory.getId(),
            Map.of("productId", productId.toString(), "warehouseId", warehouseId.toString(),
                "initialStock", String.valueOf(initialStock.getValue())));
        return inventory;
    }
}
