package application.domain.enums;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/**
 * Estado de una factura.
 */
public enum InvoiceStatus {

    ISSUED,
    PAID,
    VOIDED;

    private static final Map<InvoiceStatus, Set<InvoiceStatus>> ALLOWED_TRANSITIONS;

    static {
        Map<InvoiceStatus, Set<InvoiceStatus>> transitions = new EnumMap<>(InvoiceStatus.class);
        transitions.put(ISSUED, Set.of(PAID, VOIDED));
        transitions.put(PAID, Set.of());
        transitions.put(VOIDED, Set.of());
        ALLOWED_TRANSITIONS = Map.copyOf(transitions);
    }

    public boolean canTransitionTo(InvoiceStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(this, Set.of()).contains(target);
    }
}
