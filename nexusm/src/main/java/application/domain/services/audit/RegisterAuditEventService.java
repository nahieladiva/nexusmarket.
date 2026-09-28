package application.domain.services.audit;

import application.domain.enums.OperationType;
import application.domain.events.BusinessOperationEvent;
import application.domain.events.DomainEvent;
import application.domain.models.User;
import application.domain.ports.out.AuditLogPort;
import application.domain.services.DomainService;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Registra en la bitácora de auditoría (MongoDB) cada operación de negocio.
 */
@DomainService
public class RegisterAuditEventService {

    private final AuditLogPort auditLogPort;

    public RegisterAuditEventService(AuditLogPort auditLogPort) {
        this.auditLogPort = Objects.requireNonNull(auditLogPort, "auditLogPort es obligatorio");
    }

    public void execute(OperationType type, User actor, Object aggregateId,
                        Map<String, String> details) {
        auditLogPort.record(new BusinessOperationEvent(type,
            actor == null ? null : actor.getId(), String.valueOf(aggregateId), details,
            LocalDateTime.now()));
    }

    /** Registra un evento de dominio ya construido (p. ej. {@code LowStockEvent}). */
    public void execute(DomainEvent event) {
        auditLogPort.record(event);
    }
}
