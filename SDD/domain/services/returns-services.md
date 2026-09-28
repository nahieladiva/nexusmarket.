# Servicios de Devoluciones

## Introducción

Gestionan la posventa: el comprador solicita la devolución, la supervisión la aprueba o rechaza, y el Operador Logístico recibe el producto y lo reingresa al inventario. El paquete Java se llama `returns` porque `return` es palabra reservada.

Los servicios de este subdominio viven en `application.domain.services.returns`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
ReturnRequest (id, orderId, buyerId, productId, quantity, reason, status : ReturnStatus, requestedAt, resolvedAt)
ReturnStatus : REQUESTED → APPROVED → RECEIVED
                        └→ REJECTED
```

Regla **R12**: solo se devuelve lo comprado en un pedido entregado (`DELIVERED`).

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

## 1. ConsultReturnService

### Descripción

Consulta de devoluciones.

### Firma

```java
ReturnRequest require(ReturnId id);
List<ReturnRequest> findMine(User buyer);
List<ReturnRequest> findAll(User actor);
```

### Autorización

`findMine`: comprador. `findAll`: supervisión.

### Dependencias

`ReturnRequestRepository`, `AuthorizeBuyerOperationService`, `AuthorizeSupervisionOperationService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 2. RequestReturnService

### Descripción

El comprador dueño solicita devolver un producto de un pedido entregado.

### Firma

```java
ReturnRequest execute(User buyer, OrderId orderId, ProductId productId, Quantity quantity, String reason);
```

### Autorización

`AuthorizeBuyerOperationService` + `ValidateOrderOwnershipService`.

### Dependencias

`ReturnRequestRepository`, `ConsultOrderService`, `AuthorizeBuyerOperationService`, `ValidateOrderOwnershipService`, `RegisterAuditEventService`

### Validaciones y reglas

1. El pedido debe estar `DELIVERED` y contener el producto (lo valida `ReturnRequest.request`).
2. La suma de las devoluciones no rechazadas de ese producto más la nueva no puede superar la cantidad comprada.
3. El motivo es obligatorio.

### Comportamiento de dominio

`ReturnRequest.request(order, productId, quantity, reason)` → `REQUESTED`.

### Persistencia

`ReturnRequestRepository.save`.

### Auditoría

`RETURN_REQUESTED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Cantidad excedida o producto no comprado | `IllegalArgumentException` |
| Pedido no entregado | `IllegalStateException` |

---

## 3. ApproveReturnService

### Descripción

Aprueba una devolución solicitada.

### Firma

```java
ReturnRequest execute(User actor, ReturnId returnId);
```

### Autorización

`AuthorizeSupervisionOperationService`.

### Dependencias

`ReturnRequestRepository`, `ConsultReturnService`, `AuthorizeSupervisionOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`ReturnRequest.approve()`: `REQUESTED → APPROVED`.

### Persistencia

`ReturnRequestRepository.save`.

### Auditoría

`RETURN_APPROVED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Transición inválida | `InvalidStatusTransitionException` |

---

## 4. RejectReturnService

### Descripción

Rechaza una devolución solicitada.

### Firma

```java
ReturnRequest execute(User actor, ReturnId returnId);
```

### Autorización

`AuthorizeSupervisionOperationService`.

### Dependencias

`ReturnRequestRepository`, `ConsultReturnService`, `AuthorizeSupervisionOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`ReturnRequest.reject()`: `REQUESTED → REJECTED`.

### Persistencia

`ReturnRequestRepository.save`.

### Auditoría

`RETURN_REJECTED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Transición inválida | `InvalidStatusTransitionException` |

---

## 5. ReceiveReturnService

### Descripción

El Operador Logístico recibe en bodega el producto devuelto.

### Firma

```java
ReturnRequest execute(User operator, ReturnId returnId);
```

### Autorización

`AuthorizeLogisticOperationService`.

### Dependencias

`ReturnRequestRepository`, `ConsultReturnService`, `ConsultOrderService`, `RestockReturnedItemService`, `AuthorizeLogisticOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`ReturnRequest.markAsReceived()` (`APPROVED → RECEIVED`). Si el ítem tiene bodega asignada, `RestockReturnedItemService` reingresa las unidades (movimiento `RETURN`).

### Persistencia

`ReturnRequestRepository.save` e inventario.

### Auditoría

`RETURN_RECEIVED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Transición inválida | `InvalidStatusTransitionException` |

---
