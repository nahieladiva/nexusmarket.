package application.domain.models;

import application.domain.enums.ProductStatus;
import application.domain.enums.ProductType;
import application.domain.exceptions.InvalidStatusTransitionException;
import application.domain.valueobjects.Money;
import application.domain.valueobjects.ProductCode;
import application.domain.valueobjects.ProductId;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Producto del catálogo publicado por un vendedor.
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>El id, el código, el vendedor y el tipo son inmutables.</li>
 *   <li>Un producto nace {@code PUBLISHED}.</li>
 *   <li>Un producto {@code DISCONTINUED} no admite cambios.</li>
 *   <li>Solo los productos {@code PHYSICAL} manejan inventario en bodega.</li>
 * </ul>
 */
public final class Product {

    private final ProductId id;
    private final ProductCode code;
    private String name;
    private String description;
    private Money price;
    private final UserId sellerId;
    private final ProductType type;
    private ProductStatus status;
    private final LocalDateTime createdAt;

    public Product(ProductId id, ProductCode code, String name, String description,
                   Money price, UserId sellerId, ProductType type, ProductStatus status,
                   LocalDateTime createdAt) {
        this.id = Objects.requireNonNull(id, "El id de producto es obligatorio");
        this.code = Objects.requireNonNull(code, "El código de producto es obligatorio");
        this.name = requireNotBlank(name, "El nombre del producto es obligatorio");
        this.description = description;
        this.price = Objects.requireNonNull(price, "El precio es obligatorio");
        this.sellerId = Objects.requireNonNull(sellerId, "El vendedor es obligatorio");
        this.type = Objects.requireNonNull(type, "El tipo de producto es obligatorio");
        this.status = status == null ? ProductStatus.PUBLISHED : status;
        this.createdAt = createdAt == null ? LocalDateTime.now() : createdAt;
    }

    public static Product create(ProductCode code, String name, String description,
                                 Money price, UserId sellerId, ProductType type) {
        return new Product(ProductId.random(), code, name, description, price, sellerId,
            type, ProductStatus.PUBLISHED, LocalDateTime.now());
    }

    private static String requireNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }

    public void changePrice(Money newPrice) {
        requireNotDiscontinued();
        this.price = Objects.requireNonNull(newPrice, "El precio es obligatorio");
    }

    public void changeDescription(String newDescription) {
        requireNotDiscontinued();
        this.description = newDescription;
    }

    public void publish() {
        transitionTo(ProductStatus.PUBLISHED);
    }

    public void suspend() {
        transitionTo(ProductStatus.SUSPENDED);
    }

    public void discontinue() {
        transitionTo(ProductStatus.DISCONTINUED);
    }

    private void transitionTo(ProductStatus target) {
        if (!status.canTransitionTo(target)) {
            throw new InvalidStatusTransitionException("Producto", status, target);
        }
        this.status = target;
    }

    private void requireNotDiscontinued() {
        if (status == ProductStatus.DISCONTINUED) {
            throw new IllegalStateException("El producto " + id + " está descontinuado");
        }
    }

    public boolean isSellable() {
        return status.isSellable();
    }

    public boolean requiresInventory() {
        return type.requiresInventory();
    }

    public ProductId getId() {
        return id;
    }

    public ProductCode getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public UserId getSellerId() {
        return sellerId;
    }

    public ProductType getType() {
        return type;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
