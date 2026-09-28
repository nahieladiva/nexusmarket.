# Servicios de Envíos

## Introducción

Gestionan el despacho físico de los pedidos pagados. Los opera el Operador Logístico.

Los servicios de este subdominio viven en `application.domain.services.shipment`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Shipment (id, orderId, warehouseId, shippingAddress : Address, logisticOperatorId,
          trackingNumber, status : ShipmentStatus, createdAt, shippedAt, deliveredAt)
ShipmentStatus : PREPARING → IN_TRANSIT → DELIVERED
```

Reglas **R10** (solo se envían pedidos pagados) y **R11** (el despacho exige número de guía y operador logístico).

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

## 1. ConsultShipmentService

### Descripción

Consulta de envíos.

### Firma

```java
Shipment require(ShipmentId id);
Shipment findByOrder(User actor, OrderId orderId);
```

### Autorización

`findByOrder`: los mismos permisos que para ver el pedido.

### Dependencias

`ShipmentRepository`, `ConsultOrderService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 2. CreateShipmentService

### Descripción

Prepara el envío de un pedido pagado.

### Firma

```java
Shipment execute(User operator, OrderId orderId);
```

### Autorización

`AuthorizeLogisticOperationService`.

### Dependencias

`ShipmentRepository`, `ConsultOrderService`, `ConsultUserService`, `AuthorizeLogisticOperationService`, `RegisterAuditEventService`

### Validaciones y reglas

1. El pedido debe estar `PAID` (R10; lo valida `Shipment.prepareFor`).
2. Un pedido tiene un solo envío.
3. Debe tener al menos un producto físico. La bodega de origen es la asignada al primer ítem físico.
4. La dirección es la dirección de envío por defecto del comprador.

### Comportamiento de dominio

`Shipment.prepareFor(order, warehouseId, address)` → `PREPARING`.

### Persistencia

`ShipmentRepository.save(shipment)`.

### Auditoría

`SHIPMENT_CREATED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Pedido no pagado, ya enviado o solo digital | `IllegalStateException` |

---

## 3. DispatchShipmentService

### Descripción

Despacha el envío con su número de guía.

### Firma

```java
Shipment execute(User operator, ShipmentId shipmentId, String trackingNumber);
```

### Autorización

`AuthorizeLogisticOperationService`.

### Dependencias

`ShipmentRepository`, `OrderRepository`, `ConsultShipmentService`, `ConsultOrderService`, `AuthorizeLogisticOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Shipment.dispatch(operator, trackingNumber)` (`PREPARING → IN_TRANSIT`) y `Order.ship()` (`PAID → SHIPPED`).

### Persistencia

`ShipmentRepository.save`, `OrderRepository.save`.

### Auditoría

`SHIPMENT_DISPATCHED` con el número de guía.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Sin guía o transición inválida | `IllegalArgumentException / InvalidStatusTransitionException` |

---

## 4. ConfirmDeliveryService

### Descripción

Confirma la entrega al comprador.

### Firma

```java
Shipment execute(User operator, ShipmentId shipmentId);
```

### Autorización

`AuthorizeLogisticOperationService`.

### Dependencias

`ShipmentRepository`, `OrderRepository`, `ConsultShipmentService`, `ConsultOrderService`, `AuthorizeLogisticOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Shipment.confirmDelivery()` (`IN_TRANSIT → DELIVERED`) y `Order.deliver()` (`SHIPPED → DELIVERED`, terminal, R8).

### Persistencia

`ShipmentRepository.save`, `OrderRepository.save`.

### Auditoría

`SHIPMENT_DELIVERED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Transición inválida | `InvalidStatusTransitionException` |

---
