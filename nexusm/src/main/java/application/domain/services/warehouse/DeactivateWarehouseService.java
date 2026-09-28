package application.domain.services.warehouse;

import application.domain.enums.OperationType;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeAdminOperationService;
import application.domain.valueobjects.WarehouseId;

import java.util.Map;

/**
 * Desactiva una bodega: deja de recibir asignaciones de pedidos (regla R9). Solo Administrador.
 */
@DomainService
public class DeactivateWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ConsultWarehouseService consultWarehouseService;
    private final AuthorizeAdminOperationService authorizeAdminOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public DeactivateWarehouseService(WarehouseRepository warehouseRepository,
                                      ConsultWarehouseService consultWarehouseService,
                                      AuthorizeAdminOperationService authorizeAdminOperationService,
                                      RegisterAuditEventService registerAuditEventService) {
        this.warehouseRepository = warehouseRepository;
        this.consultWarehouseService = consultWarehouseService;
        this.authorizeAdminOperationService = authorizeAdminOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public Warehouse execute(User admin, WarehouseId warehouseId) {
        authorizeAdminOperationService.execute(admin);
        Warehouse warehouse = consultWarehouseService.findById(warehouseId);
        warehouse.deactivate();
        warehouseRepository.save(warehouse);
        registerAuditEventService.execute(OperationType.WAREHOUSE_STATUS_CHANGED, admin, warehouseId,
            Map.of("newStatus", warehouse.getStatus().name()));
        return warehouse;
    }
}
