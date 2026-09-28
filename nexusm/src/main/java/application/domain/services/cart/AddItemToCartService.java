package application.domain.services.cart;

import application.domain.models.Cart;
import application.domain.models.Product;
import application.domain.models.User;
import application.domain.ports.out.CartRepository;
import application.domain.services.DomainService;
import application.domain.services.product.ConsultProductService;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;

/**
 * Agrega un producto al carrito. Solo se aceptan productos {@code PUBLISHED}
 * y el precio unitario se toma del catálogo, nunca del cliente.
 */
@DomainService
public class AddItemToCartService {

    private final CartRepository cartRepository;
    private final ConsultCartService consultCartService;
    private final ConsultProductService consultProductService;

    public AddItemToCartService(CartRepository cartRepository,
                                ConsultCartService consultCartService,
                                ConsultProductService consultProductService) {
        this.cartRepository = cartRepository;
        this.consultCartService = consultCartService;
        this.consultProductService = consultProductService;
    }

    public Cart execute(User buyer, ProductId productId, Quantity quantity) {
        Cart cart = consultCartService.execute(buyer);
        Product product = consultProductService.findById(productId);
        if (!product.isSellable()) {
            throw new IllegalStateException("El producto " + productId + " no está disponible para la venta");
        }
        cart.addItem(productId, quantity, product.getPrice());
        return cartRepository.save(cart);
    }
}
