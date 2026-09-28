package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.services.DomainService;

import java.util.Arrays;

/**
 * Verifica que el usuario tenga alguno de los roles permitidos.
 */
@DomainService
public class ValidateRoleService {

    public void execute(User actor, UserRole... allowedRoles) {
        boolean allowed = Arrays.stream(allowedRoles).anyMatch(role -> role == actor.getRole());
        if (!allowed) {
            throw new UnauthorizedOperationException("La operación requiere el rol "
                + Arrays.toString(allowedRoles) + "; el usuario tiene el rol " + actor.getRole());
        }
    }
}
