package application.adapters.in.rest.mappers;

import application.adapters.in.rest.requests.PriceChangeRequest;
import application.adapters.in.rest.responses.ProductResponse;
import application.domain.models.Product;
import application.domain.valueobjects.Money;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

/**
 * Mapea entre DTOs REST y el modelo de dominio de productos.
 */
@Component
public class ProductMapper {

    public Money toDomain(PriceChangeRequest request) {
        return toDomain(request.price());
    }

    public Money toDomain(application.adapters.in.rest.requests.PriceRequest price) {
        return new Money(new BigDecimal(price.amount()),
            java.util.Currency.getInstance(price.currency()));
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
            product.getId().toString(),
            product.getCode().getValue(),
            product.getName(),
            product.getDescription(),
            product.getPrice().getAmount().toPlainString(),
            product.getPrice().getCurrency().getCurrencyCode(),
            product.getSellerId().toString(),
            product.getType().name(),
            product.getStatus().name(),
            product.getCreatedAt());
    }
}