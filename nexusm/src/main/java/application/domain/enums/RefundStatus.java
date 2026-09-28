package application.domain.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Estado de un reembolso.
 */
public enum RefundStatus {

    PENDING,
    COMPLETED,
    FAILED;

    private static final Map<RefundStatus, Set<RefundStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<RefundStatus, Set<RefundStatus>> transitions = new EnumMap<>(RefundStatus.class);
        transitions.put(PENDING, Set.of(COMPLETED, FAILED));
        transitions.put(COMPLETED, Set.of());
        transitions.put(FAILED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    public boolean canTransitionTo(RefundStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
