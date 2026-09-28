package application.domain.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.UserId;

import org.junit.jupiter.api.Test;

/**
 * Pruebas del ciclo de vida de {@link Product}.
 */
class ProductTest {

    private Product product(ProductType type) {
        return Product.create(ProductCode.of("PRD-0001"), "Teclado", "Mecánico",
            Money.of("100.00", "USD"), UserId.random(), type);
    }

    @Test
    void newProductIsPublished() {
        Product product = product(ProductType.PHYSICAL);
        assertEquals(ProductStatus.PUBLISHED, product.getStatus());
        assertTrue(product.isSellable());
        assertTrue(product.requiresInventory());
    }

    @Test
    void digitalProductsDoNotRequireInventory() {
        assertFalse(product(ProductType.DIGITAL).requiresInventory());
    }

    @Test
    void suspendedProductCanBePublishedAgain() {
        Product product = product(ProductType.PHYSICAL);
        product.suspend();
        assertFalse(product.isSellable());
        product.publish();
        assertTrue(product.isSellable());
    }

    @Test
    void discontinuedProductIsTerminal() {
        Product product = product(ProductType.PHYSICAL);
        product.discontinue();
        assertThrows(InvalidStatusTransitionException.class, product::publish);
        assertThrows(IllegalStateException.class,
            () -> product.changePrice(Money.of("90.00", "USD")));
    }
}
