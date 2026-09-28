package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Verifica que exista un usuario ejecutor y que esté activo (no bloqueado).
 */
@DomainService
public class ValidateActiveUserService {

    public void execute(User actor) {
        if (actor == null) {
            throw new UnauthorizedOperationException("Se requiere un usuario autenticado para esta operación");
        }
        actor.requireActive();
    }
}
