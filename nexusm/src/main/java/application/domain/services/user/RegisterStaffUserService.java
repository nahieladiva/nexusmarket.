package application.domain.services.user;

import application.domain.enums.OperationType;
import application.domain.enums.UserRole;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeAdminOperationService;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;

import java.util.Map;

/**
 * Solo un Administrador activo registra personal interno
 * (Operador Logístico, Administrador o Supervisor).
 */
@DomainService
public class RegisterStaffUserService {

    private final UserRepository userRepository;
    private final AuthorizeAdminOperationService authorizeAdminOperationService;
    private final ValidateUserUniquenessService validateUserUniquenessService;
    private final RegisterAuditEventService registerAuditEventService;

    public RegisterStaffUserService(UserRepository userRepository,
                                    AuthorizeAdminOperationService authorizeAdminOperationService,
                                    ValidateUserUniquenessService validateUserUniquenessService,
                                    RegisterAuditEventService registerAuditEventService) {
        this.userRepository = userRepository;
        this.authorizeAdminOperationService = authorizeAdminOperationService;
        this.validateUserUniquenessService = validateUserUniquenessService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public User execute(User admin, IdentificationNumber identification, String fullName,
                        Email email, PhoneNumber phone, UserRole role, String passwordHash) {
        authorizeAdminOperationService.execute(admin);
        validateUserUniquenessService.execute(email, identification);
        User staff = userRepository.save(User.createStaff(identification, fullName, email, phone,
            role, passwordHash));
        registerAuditEventService.execute(OperationType.USER_REGISTERED, admin, staff.getId(),
            Map.of("role", role.name()));
        return staff;
    }
}
