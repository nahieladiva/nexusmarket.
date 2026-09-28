package application.domain.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Estado de un envío.
 */
public enum ShipmentStatus {

    PREPARING,
    IN_TRANSIT,
    DELIVERED;

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<ShipmentStatus, Set<ShipmentStatus>> transitions = new EnumMap<>(ShipmentStatus.class);
        transitions.put(PREPARING, Set.of(IN_TRANSIT));
        transitions.put(IN_TRANSIT, Set.of(DELIVERED));
        transitions.put(DELIVERED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    public boolean canTransitionTo(ShipmentStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
