package application.domain.ports.in;

import application.domain.enums.UserRole;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.List;

/**
 * Casos de uso exclusivos del Administrador: usuarios, vendedores y bodegas.
 * El Administrador también puede usar los casos de uso de {@link SupervisorPort}.
 */
public interface AdminPort {

    User registerSeller(UserId adminId, IdentificationNumber identification, String fullName,
                        Email email, PhoneNumber phone, String passwordHash, String businessName);

    User registerStaffUser(UserId adminId, IdentificationNumber identification, String fullName,
                           Email email, PhoneNumber phone, UserRole role, String passwordHash);

    Seller approveSeller(UserId adminId, UserId sellerId);

    Seller suspendSeller(UserId adminId, UserId sellerId);

    Warehouse createWarehouse(UserId adminId, String name, Address address,
                              WarehouseLocation location);

    Warehouse activateWarehouse(UserId adminId, WarehouseId warehouseId);

    Warehouse deactivateWarehouse(UserId adminId, WarehouseId warehouseId);

    List<Warehouse> findWarehouses(UserId adminId);
}
