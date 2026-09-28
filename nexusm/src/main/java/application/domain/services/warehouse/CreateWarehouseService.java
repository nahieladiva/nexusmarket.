package application.domain.services.warehouse;

import application.domain.enums.OperationType;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeAdminOperationService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.WarehouseLocation;

import java.util.Map;

/**
 * Solo un Administrador activo crea bodegas. Toda bodega nace {@code ACTIVE}.
 */
@DomainService
public class CreateWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final AuthorizeAdminOperationService authorizeAdminOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public CreateWarehouseService(WarehouseRepository warehouseRepository,
                                  AuthorizeAdminOperationService authorizeAdminOperationService,
                                  RegisterAuditEventService registerAuditEventService) {
        this.warehouseRepository = warehouseRepository;
        this.authorizeAdminOperationService = authorizeAdminOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Warehouse execute(User admin, String name, Address address, WarehouseLocation location) {
        authorizeAdminOperationService.execute(admin);
        Warehouse warehouse = warehouseRepository.save(Warehouse.create(name, address, location));
        registerAuditEventService.execute(OperationType.WAREHOUSE_CREATED, admin, warehouse.getId(),
            Map.of("name", warehouse.getName()));
        return warehouse;
    }
}
