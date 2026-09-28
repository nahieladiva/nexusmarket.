package application.adapters.in.rest.controllers;

import application.adapters.in.rest.mappers.UserMapper;
import application.adapters.in.rest.mappers.WarehouseMapper;
import application.adapters.in.rest.requests.CreateSellerRequest;
import application.adapters.in.rest.requests.CreateStaffUserRequest;
import application.adapters.in.rest.requests.CreateWarehouseRequest;
import application.adapters.in.rest.responses.UserResponse;
import application.adapters.in.rest.responses.WarehouseResponse;
import application.domain.enums.UserRole;
import application.domain.models.Warehouse;
import application.domain.ports.in.AdminPort;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;
import application.domain.valueobjects.UserId;
import application.domain.valueobjects.WarehouseId;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del Administrador. Se identifica con la cabecera {@code X-User-Id}.
 * Para bloquear usuarios, gestionar devoluciones o moderar productos, el
 * Administrador usa los endpoints de {@code /api/supervisor}.
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final String USER = "X-User-Id";

    private final AdminPort adminPort;
    private final UserMapper userMapper;
    private final WarehouseMapper warehouseMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminPort adminPort, UserMapper userMapper,
                           WarehouseMapper warehouseMapper, PasswordEncoder passwordEncoder) {
        this.adminPort = adminPort;
        this.userMapper = userMapper;
        this.warehouseMapper = warehouseMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/sellers")
    public ResponseEntity<UserResponse> registerSeller(@RequestHeader(USER) String adminId,
                                                       @RequestBody CreateSellerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(
            adminPort.registerSeller(UserId.of(adminId),
                IdentificationNumber.of(request.identification()), request.fullName(),
                Email.of(request.email()), PhoneNumber.of(request.phone()),
                passwordEncoder.encode(request.password()), request.businessName())));
    }

    @PostMapping("/staff")
    public ResponseEntity<UserResponse> registerStaff(@RequestHeader(USER) String adminId,
                                                      @RequestBody CreateStaffUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(
            adminPort.registerStaffUser(UserId.of(adminId),
                IdentificationNumber.of(request.identification()), request.fullName(),
                Email.of(request.email()), PhoneNumber.of(request.phone()),
                UserRole.fromString(request.role()), passwordEncoder.encode(request.password()))));
    }

    @PatchMapping("/sellers/{id}/approve")
    public ResponseEntity<UserResponse> approveSeller(@RequestHeader(USER) String adminId,
                                                      @PathVariable String id) {
        return ResponseEntity.ok(userMapper.toResponse(
            adminPort.approveSeller(UserId.of(adminId), UserId.of(id))));
    }

    @PatchMapping("/sellers/{id}/suspend")
    public ResponseEntity<UserResponse> suspendSeller(@RequestHeader(USER) String adminId,
                                                      @PathVariable String id) {
        return ResponseEntity.ok(userMapper.toResponse(
            adminPort.suspendSeller(UserId.of(adminId), UserId.of(id))));
    }

    @PostMapping("/warehouses")
    public ResponseEntity<WarehouseResponse> createWarehouse(@RequestHeader(USER) String adminId,
                                                             @RequestBody CreateWarehouseRequest request) {
        Warehouse data = warehouseMapper.toDomain(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(warehouseMapper.toResponse(
            adminPort.createWarehouse(UserId.of(adminId), data.getName(), data.getAddress(),
                data.getLocation())));
    }

    @PatchMapping("/warehouses/{id}/activate")
    public ResponseEntity<WarehouseResponse> activateWarehouse(@RequestHeader(USER) String adminId,
                                                               @PathVariable String id) {
        return ResponseEntity.ok(warehouseMapper.toResponse(
            adminPort.activateWarehouse(UserId.of(adminId), WarehouseId.of(id))));
    }

    @PatchMapping("/warehouses/{id}/deactivate")
    public ResponseEntity<WarehouseResponse> deactivateWarehouse(@RequestHeader(USER) String adminId,
                                                                 @PathVariable String id) {
        return ResponseEntity.ok(warehouseMapper.toResponse(
            adminPort.deactivateWarehouse(UserId.of(adminId), WarehouseId.of(id))));
    }

    @GetMapping("/warehouses")
    public ResponseEntity<List<WarehouseResponse>> findWarehouses(@RequestHeader(USER) String adminId) {
        return ResponseEntity.ok(adminPort.findWarehouses(UserId.of(adminId)).stream()
            .map(warehouseMapper::toResponse).toList());
    }
}
