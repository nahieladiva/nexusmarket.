package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.OrderStatus;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de {@link Cart} y {@link CartItem}.
 */
class CartTest {

    @Test
    void addingSameProductMergesQuantities() {
        Cart cart = Cart.createFor(UserId.random());
        ProductId product = ProductId.random();
        cart.addItem(product, Quantity.of(1), Money.of("10.00", "USD"));
        cart.addItem(product, Quantity.of(2), Money.of("10.00", "USD"));
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity().getValue());
        assertEquals("30.00", cart.total().getAmount().toPlainString());
    }

    @Test
    void checkoutCreatesPendingPaymentOrderAndEmptiesCart() {
        UserId buyer = UserId.random();
        Cart cart = Cart.createFor(buyer);
        cart.addItem(ProductId.random(), Quantity.of(2), Money.of("5.00", "USD"));
        Order order = cart.checkout();
        assertEquals(OrderStatus.PENDING_PAYMENT, order.getStatus());
        assertEquals(buyer, order.getBuyerId());
        assertTrue(cart.isEmpty());
    }

    @Test
    void emptyCartCannotBeCheckedOut() {
        Cart cart = Cart.createFor(UserId.random());
        assertThrows(IllegalStateException.class, cart::checkout);
    }
}
