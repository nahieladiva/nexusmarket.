package application.domain.services.user;

import application.domain.enums.OperationType;
import application.domain.models.Buyer;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.valueobjects.Address;
import application.domain.valueobjects.Email;
import application.domain.valueobjects.IdentificationNumber;
import application.domain.valueobjects.PhoneNumber;

import java.util.Map;

/**
 * Registro público de un comprador (no requiere usuario ejecutor).
 */
@DomainService
public class RegisterBuyerService {

    private final UserRepository userRepository;
    private final ValidateUserUniquenessService validateUserUniquenessService;
    private final RegisterAuditEventService registerAuditEventService;

    public RegisterBuyerService(UserRepository userRepository,
                                ValidateUserUniquenessService validateUserUniquenessService,
                                RegisterAuditEventService registerAuditEventService) {
        this.userRepository = userRepository;
        this.validateUserUniquenessService = validateUserUniquenessService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public User execute(IdentificationNumber identification, String fullName, Email email,
                        PhoneNumber phone, String passwordHash, Address defaultShippingAddress) {
        validateUserUniquenessService.execute(email, identification);
        User buyer = userRepository.save(Buyer.create(identification, fullName, email, phone,
            passwordHash, defaultShippingAddress));
        registerAuditEventService.execute(OperationType.USER_REGISTERED, null, buyer.getId(),
            Map.of("role", buyer.getRole().name()));
        return buyer;
    }
}
