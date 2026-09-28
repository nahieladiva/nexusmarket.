package application.domain.services.authorization;

import application.domain.exceptions.UnauthorizedOperationException;
import application.domain.models.Order;
import application.domain.models.User;
import application.domain.services.DomainService;

/**
 * Verifica que un pedido pertenezca al comprador que opera sobre él.
 */
@DomainService
public class ValidateOrderOwnershipService {

    public void execute(User buyer, Order order) {
        if (!order.belongsTo(buyer.getId())) {
            throw new UnauthorizedOperationException(
                "El pedido " + order.getId() + " no pertenece al usuario " + buyer.getId());
        }
    }
}
