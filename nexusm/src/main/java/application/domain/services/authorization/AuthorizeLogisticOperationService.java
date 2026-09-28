package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Autoriza operaciones del Operador Logístico (inventario, envíos, recepción de devoluciones).
 */
@DomainService
public class AuthorizeLogisticOperationService {

    private final ValidateActiveUserService validateActiveUserService;
    private final ValidateRoleService validateRoleService;

    public AuthorizeLogisticOperationService(ValidateActiveUserService validateActiveUserService,
            ValidateRoleService validateRoleService) {
        this.validateActiveUserService = validateActiveUserService;
        this.validateRoleService = validateRoleService;
    }

    public void execute(User actor) {
        validateActiveUserService.execute(actor);
        validateRoleService.execute(actor, UserRole.LOGISTIC_OPERATOR);
    }
}
