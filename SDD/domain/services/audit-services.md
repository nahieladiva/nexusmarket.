# Servicios de Auditoría

## Introducción

Toda operación de negocio que cambia estado deja un registro en la colección `audit_logs` de MongoDB. Este servicio es el único punto de entrada a la auditoría. Es el equivalente a `RegisterOperationAndAuditService` del proyecto de referencia.

Los servicios de este subdominio viven en `application.domain.services.audit`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
DomainEvent (interfaz)
 ├── BusinessOperationEvent(operationType, actorId, aggregateId, details, occurredAt)
 └── LowStockEvent(productId, warehouseId, onHand, reorderThreshold, occurredAt)

AuditLogPort.record(DomainEvent)  →  AuditLogPersistenceAdapter  →  MongoDB audit_logs
```

`OperationType` (enum) define las operaciones auditables: `USER_REGISTERED`, `SELLER_APPROVED`, `SELLER_SUSPENDED`, `USER_BLOCKED`, `USER_ACTIVATED`, `PRODUCT_CREATED`, `PRODUCT_PRICE_CHANGED`, `PRODUCT_STATUS_CHANGED`, `WAREHOUSE_CREATED`, `WAREHOUSE_STATUS_CHANGED`, `INVENTORY_CREATED`, `INVENTORY_MOVEMENT`, `ORDER_PLACED`, `ORDER_PAID`, `ORDER_CANCELLED`, `INVOICE_ISSUED`, `SHIPMENT_CREATED`, `SHIPMENT_DISPATCHED`, `SHIPMENT_DELIVERED`, `RETURN_REQUESTED`, `RETURN_APPROVED`, `RETURN_REJECTED`, `RETURN_RECEIVED`, `REFUND_COMPLETED`.

---

## Patrón estándar de un servicio que cambia estado

```text
1. Recibir el usuario ejecutor y los identificadores de la operación
        │
        ▼
2. Autorizar (usuario activo + rol requerido)
        │
        ▼
3. Cargar el estado autoritativo desde los puertos de salida
        │
        ▼
4. Validar propiedad (pedido del comprador, producto del vendedor)
        │
        ▼
5. Validar reglas específicas de la operación
        │
        ▼
6. Ejecutar el comportamiento de dominio (métodos de la entidad)
        │
        ▼
7. Persistir mediante el puerto de salida
        │
        ▼
8. Registrar la auditoría (RegisterAuditEventService → AuditLogPort)
```

No todos los servicios ejecutan todos los pasos: solo los relevantes para la operación.

---

## 1. RegisterAuditEventService

### Descripción

Construye un `BusinessOperationEvent` y lo envía al puerto de auditoría. Una segunda variante recibe un `DomainEvent` ya construido (se usa para `LowStockEvent`).

### Firma

```java
void execute(OperationType type, User actor, Object aggregateId, Map<String, String> details);
void execute(DomainEvent event);
```

### Autorización

No aplica: lo invocan otros servicios después de autorizar.

### Dependencias

`AuditLogPort`

### Validaciones y reglas

1. `operationType`, `aggregateId` y `occurredAt` son obligatorios.
2. `actor` puede ser `null` solo en el registro público de compradores.
3. `details` se copia de forma inmutable.

### Comportamiento de dominio

El documento de MongoDB guarda `eventType` (el nombre del `OperationType`), `aggregateId`, `payload` y `occurredAt`.

### Persistencia

`AuditLogPort.record(event)`.

### Auditoría

Es el propio servicio de auditoría.

---
