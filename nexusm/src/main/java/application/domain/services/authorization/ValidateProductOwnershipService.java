package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Verifica que un producto pertenezca al vendedor que opera sobre él.
 */
@DomainService
public class ValidateProductOwnershipService {

    public void execute(User seller, Product product) {
        if (!product.getSellerId().equals(seller.getId())) {
            throw new UnauthorizedOperationException(
                "El producto " + product.getId() + " no pertenece al vendedor " + seller.getId());
        }
    }
}
