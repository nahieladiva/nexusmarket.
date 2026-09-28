package application.domain.services.authorization;

import application.domain.enums.UserRole;
import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Seller;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Autoriza operaciones del Vendedor: debe estar activo, tener rol SELLER
 * y estar aprobado por un Administrador.
 */
@DomainService
public class AuthorizeSellerOperationService {

    private final ValidateActiveUserService validateActiveUserService;
    private final ValidateRoleService validateRoleService;

    public AuthorizeSellerOperationService(ValidateActiveUserService validateActiveUserService,
                                           ValidateRoleService validateRoleService) {
        this.validateActiveUserService = validateActiveUserService;
        this.validateRoleService = validateRoleService;
    }

    public Seller execute(User actor) {
        validateActiveUserService.execute(actor);
        validateRoleService.execute(actor, UserRole.SELLER);
        if (!(actor instanceof Seller seller)) {
            throw new UnauthorizedOperationException("El usuario no es un vendedor");
        }
        seller.requireApprovedToPublish();
        return seller;
    }
}
