package application.adapters.out.persistence.mysql.mappers;

import application.adapters.out.persistence.mysql.entities.CartItemJpaEntity;
import application.adapters.out.persistence.mysql.entities.CartJpaEntity;
import application.domain.models.Cart;
import application.domain.models.CartItem;
import application.domain.valueobjects.CartId;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.Quantity;
import application.domain.valueobjects.UserId;
import java.util.List;

import org.springframework.stereotype.Component;

/**
 * Traduce entre {@link Cart} y {@link CartJpaEntity}.
 */
@Component
public class CartEntityMapper {

    public CartJpaEntity toEntity(Cart cart) {
        CartJpaEntity entity = new CartJpaEntity();
        entity.setId(cart.getId().toString());
        entity.setBuyerId(cart.getBuyerId().toString());
        entity.setUpdatedAt(cart.getUpdatedAt());
        cart.getItems().forEach(item -> {
            CartItemJpaEntity itemEntity = new CartItemJpaEntity();
            itemEntity.setCart(entity);
            itemEntity.setProductId(item.getProductId().toString());
            itemEntity.setQuantity(item.getQuantity().getValue());
            itemEntity.setUnitPriceAmount(item.getUnitPrice().getAmount());
            itemEntity.setUnitPriceCurrency(item.getUnitPrice().getCurrency().getCurrencyCode());
            entity.getItems().add(itemEntity);
        });
        return entity;
    }

    public Cart toDomain(CartJpaEntity entity) {
        List<CartItem> items = entity.getItems().stream()
            .map(item -> new CartItem(ProductId.of(item.getProductId()),
                Quantity.of(item.getQuantity()),
                Money.of(item.getUnitPriceAmount().toPlainString(), item.getUnitPriceCurrency())))
            .toList();
        return new Cart(CartId.of(entity.getId()), UserId.of(entity.getBuyerId()), items,
            entity.getUpdatedAt());
    }
}
