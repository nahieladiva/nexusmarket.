package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Autoriza operaciones exclusivas del Administrador (gestión de usuarios, vendedores y bodegas).
 */
@DomainService
public class AuthorizeAdminOperationService {

    private final ValidateActiveUserService validateActiveUserService;
    private final ValidateRoleService validateRoleService;

    public AuthorizeAdminOperationService(ValidateActiveUserService validateActiveUserService,
            ValidateRoleService validateRoleService) {
        this.validateActiveUserService = validateActiveUserService;
        this.validateRoleService = validateRoleService;
    }

    public void execute(User actor) {
        validateActiveUserService.execute(actor);
        validateRoleService.execute(actor, UserRole.ADMIN);
    }
}
