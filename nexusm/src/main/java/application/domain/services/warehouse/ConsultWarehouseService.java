package application.domain.services.warehouse;

import application.domain.exceptions.ResourceNotFoundException;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.out.WarehouseRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.ValidateActiveUserService;
import application.domain.valueobjects.WarehouseId;

import java.util.List;

/**
 * Consulta de bodegas.
 */
@DomainService
public class ConsultWarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final ValidateActiveUserService validateActiveUserService;

    public ConsultWarehouseService(WarehouseRepository warehouseRepository,
                                   ValidateActiveUserService validateActiveUserService) {
        this.warehouseRepository = warehouseRepository;
        this.validateActiveUserService = validateActiveUserService;
    }

    public Warehouse findById(WarehouseId id) {
        return warehouseRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Bodega", id.toString()));
    }

    /** Listado de bodegas para cualquier usuario activo del personal. */
    public List<Warehouse> findAll(User actor) {
        validateActiveUserService.execute(actor);
        return findAll();
    }

    public List<Warehouse> findAll() {
        return warehouseRepository.findAll();
    }
}
