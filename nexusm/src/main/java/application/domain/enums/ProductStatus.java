package application.domain.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Estado de publicación de un producto.
 *
 * <p>Transiciones permitidas:</p>
 * <ul>
 *   <li>{@code PUBLISHED} -> {@code SUSPENDED}, {@code DISCONTINUED}</li>
 *   <li>{@code SUSPENDED} -> {@code PUBLISHED}, {@code DISCONTINUED}</li>
 *   <li>{@code DISCONTINUED} -> estado terminal</li>
 * </ul>
 */
public enum ProductStatus {

    PUBLISHED,
    SUSPENDED,
    DISCONTINUED;

    private static final Map<ProductStatus, Set<ProductStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<ProductStatus, Set<ProductStatus>> transitions = new EnumMap<>(ProductStatus.class);
        transitions.put(PUBLISHED, Set.of(SUSPENDED, DISCONTINUED));
        transitions.put(SUSPENDED, Set.of(PUBLISHED, DISCONTINUED));
        transitions.put(DISCONTINUED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    public boolean canTransitionTo(ProductStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }

    public boolean isSellable() {
        return this == PUBLISHED;
    }
}
