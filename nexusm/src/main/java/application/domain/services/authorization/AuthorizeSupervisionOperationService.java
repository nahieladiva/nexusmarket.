package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Autoriza operaciones de supervisión: Administrador o Supervisor activo.
 */
@DomainService
public class AuthorizeSupervisionOperationService {

    private final ValidateActiveUserService validateActiveUserService;
    private final ValidateRoleService validateRoleService;

    public AuthorizeSupervisionOperationService(ValidateActiveUserService validateActiveUserService,
            ValidateRoleService validateRoleService) {
        this.validateActiveUserService = validateActiveUserService;
        this.validateRoleService = validateRoleService;
    }

    public void execute(User actor) {
        validateActiveUserService.execute(actor);
        validateRoleService.execute(actor, UserRole.ADMIN, UserRole.SUPERVISOR);
    }
}
