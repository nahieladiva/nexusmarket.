package application.domain.services.user;

import application.domain.enums.OperationType;
import application.domain.models.Seller;
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
 * Regla R4: solo un Administrador activo registra vendedores.
 * El vendedor queda en {@code PENDING_APPROVAL}.
 */
@DomainService
public class RegisterSellerService {

    private final UserRepository userRepository;
    private final AuthorizeAdminOperationService authorizeAdminOperationService;
    private final ValidateUserUniquenessService validateUserUniquenessService;
    private final RegisterAuditEventService registerAuditEventService;

    public RegisterSellerService(UserRepository userRepository,
                                 AuthorizeAdminOperationService authorizeAdminOperationService,
                                 ValidateUserUniquenessService validateUserUniquenessService,
                                 RegisterAuditEventService registerAuditEventService) {
        this.userRepository = userRepository;
        this.authorizeAdminOperationService = authorizeAdminOperationService;
        this.validateUserUniquenessService = validateUserUniquenessService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public User execute(User admin, IdentificationNumber identification, String fullName,
                        Email email, PhoneNumber phone, String passwordHash, String businessName) {
        authorizeAdminOperationService.execute(admin);
        validateUserUniquenessService.execute(email, identification);
        User seller = userRepository.save(Seller.create(identification, fullName, email, phone,
            passwordHash, businessName));
        registerAuditEventService.execute(OperationType.USER_REGISTERED, admin, seller.getId(),
            Map.of("role", seller.getRole().name()));
        return seller;
    }
}
