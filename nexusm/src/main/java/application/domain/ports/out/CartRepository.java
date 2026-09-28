package application.domain.ports.out;

import application.domain.models.Cart;
import application.domain.valueobjects.CartId;
import application.domain.valueobjects.UserId;

import java.util.Optional;

/**
 * Puerto de salida para la persistencia de carritos.
 */
public interface CartRepository {

    Cart save(Cart cart);

    Optional<Cart> findById(CartId id);

    Optional<Cart> findByBuyerId(UserId buyerId);
}
