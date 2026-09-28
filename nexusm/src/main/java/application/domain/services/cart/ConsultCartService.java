package application.domain.services.cart;

import application.domain.models.Cart;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.services.DomainService;
import application.domain.services.authorization.AuthorizeBuyerOperationService;

/**
 * Devuelve el carrito del comprador; si no tiene uno, crea uno vacío.
 */
@DomainService
public class ConsultCartService {

    private final CartRepository cartRepository;
    private final AuthorizeBuyerOperationService authorizeBuyerOperationService;

    public ConsultCartService(CartRepository cartRepository,
                              AuthorizeBuyerOperationService authorizeBuyerOperationService) {
        this.cartRepository = cartRepository;
        this.authorizeBuyerOperationService = authorizeBuyerOperationService;
    }

    public Cart execute(User buyer) {
        authorizeBuyerOperationService.execute(buyer);
        return cartRepository.findByBuyerId(buyer.getId())
            .orElseGet(() -> Cart.createFor(buyer.getId()));
    }
}
