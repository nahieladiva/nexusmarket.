# Servicios de Reembolsos

## Introducción

Devuelven el dinero de una devolución recibida en bodega.

Los servicios de este subdominio viven en `application.domain.services.refund`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Refund (id, returnId, orderId, amount : Money, status : RefundStatus, createdAt, completedAt)
RefundStatus : PENDING → COMPLETED | FAILED
```

Regla **R13**: solo se reembolsa una devolución `RECEIVED`, y una sola vez.

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

## 1. ProcessRefundService

### Descripción

Calcula y completa el reembolso de una devolución.

### Firma

```java
Refund execute(User actor, ReturnId returnId);
```

### Autorización

`AuthorizeSupervisionOperationService`.

### Dependencias

`RefundRepository`, `ConsultReturnService`, `ConsultOrderService`, `AuthorizeSupervisionOperationService`, `RegisterAuditEventService`

### Validaciones y reglas

1. La devolución debe estar `RECEIVED` (lo valida `Refund.createFor`).
2. No puede existir otro reembolso para la misma devolución.
3. Monto = precio unitario pagado en el pedido × cantidad devuelta.

### Comportamiento de dominio

`Refund.createFor(returnRequest, amount)` → `PENDING`; luego `Refund.complete()` → `COMPLETED`.

### Persistencia

`RefundRepository.save(refund)`.

### Auditoría

`REFUND_COMPLETED` con monto, devolución y pedido.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Devolución no recibida o ya reembolsada | `IllegalStateException` |

---

## 2. ConsultRefundService

### Descripción

Consulta el reembolso de una devolución.

### Firma

```java
Refund findByReturn(User actor, ReturnId returnId);
```

### Autorización

Usuario activo. Un comprador solo ve los reembolsos de sus propias devoluciones.

### Dependencias

`RefundRepository`, `ConsultReturnService`, `ValidateActiveUserService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---
