package application.adapters.useCases;

import application.domain.enums.UserRole;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdminPort;
import application.domain.services.authorization.AuthorizeAdminOperationService;
import application.domain.services.user.ApproveSellerService;
import application.domain.services.user.ConsultUserService;
import application.domain.services.user.RegisterSellerService;
import application.domain.services.user.RegisterStaffUserService;
import application.domain.services.user.SuspendSellerService;
import application.domain.services.warehouse.ActivateWarehouseService;
import application.domain.services.warehouse.ConsultWarehouseService;
import application.domain.services.warehouse.CreateWarehouseService;
import application.domain.services.warehouse.DeactivateWarehouseService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;
import application.domain.valueobjects.WarehouseLocation;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementa {@link AdminPort}: carga al administrador y delega en los servicios de dominio.
 */
@Service
@Transactional
public class AdminUseCaseImpl implements AdminPort {

    private final ConsultUserService consultUserService;
    private final AuthorizeAdminOperationService authorizeAdminOperationService;
    private final RegisterSellerService registerSellerService;
    private final RegisterStaffUserService registerStaffUserService;
    private final ApproveSellerService approveSellerService;
    private final SuspendSellerService suspendSellerService;
    private final CreateWarehouseService createWarehouseService;
    private final ActivateWarehouseService activateWarehouseService;
    private final DeactivateWarehouseService deactivateWarehouseService;
    private final ConsultWarehouseService consultWarehouseService;

    public AdminUseCaseImpl(ConsultUserService consultUserService,
                            AuthorizeAdminOperationService authorizeAdminOperationService,
                            RegisterSellerService registerSellerService,
                            RegisterStaffUserService registerStaffUserService,
                            ApproveSellerService approveSellerService,
                            SuspendSellerService suspendSellerService,
                            CreateWarehouseService createWarehouseService,
                            ActivateWarehouseService activateWarehouseService,
                            DeactivateWarehouseService deactivateWarehouseService,
                            ConsultWarehouseService consultWarehouseService) {
        this.consultUserService = consultUserService;
        this.authorizeAdminOperationService = authorizeAdminOperationService;
        this.registerSellerService = registerSellerService;
        this.registerStaffUserService = registerStaffUserService;
        this.approveSellerService = approveSellerService;
        this.suspendSellerService = suspendSellerService;
        this.createWarehouseService = createWarehouseService;
        this.activateWarehouseService = activateWarehouseService;
        this.deactivateWarehouseService = deactivateWarehouseService;
        this.consultWarehouseService = consultWarehouseService;
    }

    private User admin(UserId adminId) {
        return consultUserService.findById(adminId);
    }

    @Override
    public User registerSeller(UserId adminId, IdentificationNumber identification, String fullName,
                               Email email, PhoneNumber phone, String passwordHash,
                               String businessName) {
        return registerSellerService.execute(admin(adminId), identification, fullName, email, phone,
            passwordHash, businessName);
    }

    @Override
    public User registerStaffUser(UserId adminId, IdentificationNumber identification,
                                  String fullName, Email email, PhoneNumber phone, UserRole role,
                                  String passwordHash) {
        return registerStaffUserService.execute(admin(adminId), identification, fullName, email,
            phone, role, passwordHash);
    }

    @Override
    public Seller approveSeller(UserId adminId, UserId sellerId) {
        return approveSellerService.execute(admin(adminId), sellerId);
    }

    @Override
    public Seller suspendSeller(UserId adminId, UserId sellerId) {
        return suspendSellerService.execute(admin(adminId), sellerId);
    }

    @Override
    public Warehouse createWarehouse(UserId adminId, String name, Address address,
                                     WarehouseLocation location) {
        return createWarehouseService.execute(admin(adminId), name, address, location);
    }

    @Override
    public Warehouse activateWarehouse(UserId adminId, WarehouseId warehouseId) {
        return activateWarehouseService.execute(admin(adminId), warehouseId);
    }

    @Override
    public Warehouse deactivateWarehouse(UserId adminId, WarehouseId warehouseId) {
        return deactivateWarehouseService.execute(admin(adminId), warehouseId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Warehouse> findWarehouses(UserId adminId) {
        User admin = admin(adminId);
        authorizeAdminOperationService.execute(admin);
        return consultWarehouseService.findAll(admin);
    }
}
