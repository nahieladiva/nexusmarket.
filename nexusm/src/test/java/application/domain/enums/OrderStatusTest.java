package application.domain.enums;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Pruebas de la máquina de estados de {@link OrderStatus}.
 */
class OrderStatusTest {

    @Test
    void allowsCartTransitions() {
        assertTrue(OrderStatus.CART.canTransitionTo(OrderStatus.PENDING_PAYMENT));
        assertTrue(OrderStatus.CART.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void allowsPendingPaymentTransitions() {
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.PAID));
        assertTrue(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.CANCELLED));
    }

    @Test
    void allowsPaidToShippedAndShippedToDelivered() {
        assertTrue(OrderStatus.PAID.canTransitionTo(OrderStatus.SHIPPED));
        assertTrue(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED));
    }

    @Test
    void forbidsInvalidTransitions() {
        assertFalse(OrderStatus.CART.canTransitionTo(OrderStatus.PAID));
        assertFalse(OrderStatus.PENDING_PAYMENT.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.PAID.canTransitionTo(OrderStatus.CANCELLED));
        assertFalse(OrderStatus.DELIVERED.canTransitionTo(OrderStatus.SHIPPED));
        assertFalse(OrderStatus.CANCELLED.canTransitionTo(OrderStatus.CART));
    }

    @Test
    void finalStatesAreDeliveredAndCancelled() {
        assertTrue(OrderStatus.DELIVERED.isFinal());
        assertTrue(OrderStatus.CANCELLED.isFinal());
        assertFalse(OrderStatus.PAID.isFinal());
    }
}
