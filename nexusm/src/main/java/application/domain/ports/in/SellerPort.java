package application.domain.ports.in;

import application.domain.enums.ProductType;
import application.domain.models.Product;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.util.List;

/**
 * Casos de uso del Vendedor (activo y aprobado).
 */
public interface SellerPort {

    Product createProduct(UserId sellerId, ProductCode code, String name, String description,
                          Money price, ProductType type);

    List<Product> findMyProducts(UserId sellerId);

    Product changePrice(UserId sellerId, ProductId productId, Money newPrice);

    Product suspendProduct(UserId sellerId, ProductId productId);

    Product publishProduct(UserId sellerId, ProductId productId);

    Product discontinueProduct(UserId sellerId, ProductId productId);
}
