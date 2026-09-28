package application.domain.events;

import application.domain.enums.OperationType;
import application.domain.valueobjects.UserId;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Evento genérico de auditoría: quién hizo qué operación, sobre qué agregado
 * y con qué detalles. Lo emite {@code RegisterAuditEventService}.
 *
 * @param operationType tipo de operación
 * @param actorId       usuario que ejecutó la operación ({@code null} en el registro público)
 * @param aggregateId   id de la entidad afectada
 * @param details       datos adicionales (estado anterior, estado nuevo, montos…)
 * @param occurredAt    momento de la operación
 */
public record BusinessOperationEvent(OperationType operationType, UserId actorId,
                                     String aggregateId, Map<String, String> details,
                                     LocalDateTime occurredAt) implements DomainEvent {

    public BusinessOperationEvent {
        Objects.requireNonNull(operationType, "operationType es obligatorio");
        Objects.requireNonNull(aggregateId, "aggregateId es obligatorio");
        Objects.requireNonNull(occurredAt, "occurredAt es obligatorio");
        details = details == null ? Map.of() : Map.copyOf(details);
    }
}
