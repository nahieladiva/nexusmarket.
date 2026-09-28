package application.domain.services.user;

import application.domain.enums.OperationType;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.ports.out.UserRepository;
import application.domain.services.DomainService;
import application.domain.services.audit.RegisterAuditEventService;
import application.domain.services.authorization.AuthorizeSupervisionOperationService;
import application.domain.valueobjects.UserId;

import java.util.Map;

/**
 * Reactiva un usuario bloqueado ({@code BLOCKED} -> {@code ACTIVE}). Solo Administrador o Supervisor.
 */
@DomainService
public class ActivateUserService {

    private final UserRepository userRepository;
    private final ConsultUserService consultUserService;
    private final AuthorizeSupervisionOperationService authorizeSupervisionOperationService;
    private final RegisterAuditEventService registerAuditEventService;

    public ActivateUserService(UserRepository userRepository, ConsultUserService consultUserService,
                               AuthorizeSupervisionOperationService authorizeSupervisionOperationService,
                               RegisterAuditEventService registerAuditEventService) {
        this.userRepository = userRepository;
        this.consultUserService = consultUserService;
        this.authorizeSupervisionOperationService = authorizeSupervisionOperationService;
        this.registerAuditEventService = registerAuditEventService;
    }

    public User execute(User actor, UserId userId) {
        authorizeSupervisionOperationService.execute(actor);
        if (actor.getId().equals(userId)) {
            throw new UnauthorizedOperationException("Un usuario no puede cambiar su propio estado");
        }
        User user = consultUserService.findById(userId);
        user.activate();
        userRepository.save(user);
        registerAuditEventService.execute(OperationType.USER_ACTIVATED, actor, user.getId(),
            Map.of("newStatus", user.getStatus().name()));
        return user;
    }
}
