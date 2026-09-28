package application.domain.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Estado de una solicitud de devolución.
 */
public enum ReturnStatus {

    REQUESTED,
    APPROVED,
    REJECTED,
    RECEIVED;

    private static final Map<ReturnStatus, Set<ReturnStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<ReturnStatus, Set<ReturnStatus>> transitions = new EnumMap<>(ReturnStatus.class);
        transitions.put(REQUESTED, Set.of(APPROVED, REJECTED));
        transitions.put(APPROVED, Set.of(RECEIVED));
        transitions.put(REJECTED, Set.of());
        transitions.put(RECEIVED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    public boolean canTransitionTo(ReturnStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
