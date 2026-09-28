package application.domain.services.pricing;

import application.domain.models.OrderItem;
import application.domain.services.DomainService;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.Percentage;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio de dominio para el cálculo de precios y descuentos.
 */
@DomainService
public final class PricingDomainService {

    /**
     * Aplica un descuento porcentual a un precio.
     */
    public Money applyPercentageDiscount(Money price, Percentage discount) {
        BigDecimal factor = BigDecimal.ONE.subtract(discount.asFraction());
        return price.multiply(factor);
    }

    /**
     * Calcula el total de una lista de items, validando que compartan moneda.
     */
    public Money calculateOrderTotal(List<OrderItem> items) {
        Money total = null;
        for (OrderItem item : items) {
            total = (total == null) ? item.subtotal() : total.add(item.subtotal());
        }
        if (total == null) {
            throw new IllegalArgumentException("La orden debe tener al menos un item");
        }
        return total;
    }

    /**
     * Aplica un descuento porcentual por volumen cuando se supera el umbral de unidades.
     */
    public Money applyVolumeDiscount(List<OrderItem> items, int volumeThreshold,
                                     Percentage discount) {
        int totalUnits = items.stream()
            .mapToInt(item -> item.getQuantity().getValue())
            .sum();
        Money total = calculateOrderTotal(items);
        if (totalUnits >= volumeThreshold) {
            return applyPercentageDiscount(total, discount);
        }
        return total;
    }
}