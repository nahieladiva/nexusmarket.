package application.adapters.out.persistence.mongodb.mappers;

import application.adapters.out.persistence.mongodb.documents.AuditLogDocument;
import application.domain.events.BusinessOperationEvent;
import application.domain.events.DomainEvent;
import application.domain.events.LowStockEvent;

import org.springframework.stereotype.Component;

/**
 * Traduce los eventos de dominio a documentos de la colección {@code audit_logs}.
 */
@Component
public class AuditLogMapper {

    public AuditLogDocument toDocument(DomainEvent event) {
        return new AuditLogDocument(
            eventType(event),
            extractAggregateId(event),
            serialize(event),
            event.occurredAt());
    }

    private String eventType(DomainEvent event) {
        if (event instanceof BusinessOperationEvent operation) {
            return operation.operationType().name();
        }
        return event.getClass().getSimpleName();
    }

    private String extractAggregateId(DomainEvent event) {
        if (event instanceof BusinessOperationEvent operation) {
            return operation.aggregateId();
        }
        if (event instanceof LowStockEvent lowStock) {
            return lowStock.productId() + "|" + lowStock.warehouseId();
        }
        return event.getClass().getSimpleName();
    }

    /**
     * Los eventos de dominio son records de Java, por lo que {@code toString()}
     * ya produce una representación legible con todos sus campos. Así el
     * adaptador no depende de Jackson (Spring Boot 4 usa Jackson 3).
     */
    private String serialize(DomainEvent event) {
        return event.toString();
    }
}
