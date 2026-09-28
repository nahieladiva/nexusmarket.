# Servicios de Facturación

## Introducción

Emiten y consultan la factura de cada pedido. La factura se emite al crear el pedido y se marca como pagada o anulada según el pedido.

Los servicios de este subdominio viven en `application.domain.services.invoice`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Invoice (id, invoiceNumber, orderId, buyerId, total : Money, status : InvoiceStatus, issuedAt, paidAt)
InvoiceStatus : ISSUED → PAID | VOIDED
```

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

## 1. IssueInvoiceService

### Descripción

Emite la factura de un pedido pendiente de pago.

### Firma

```java
Invoice execute(User actor, Order order);
```

### Autorización

No aplica: lo invoca `PlaceOrderService`.

### Dependencias

`InvoiceRepository`, `RegisterAuditEventService`

### Validaciones y reglas

1. Un pedido tiene una sola factura.
2. Número: `FAC-yyyyMMdd-<8 primeros caracteres del id del pedido>`.

### Comportamiento de dominio

`Invoice.issueFor(order, number)`: exige que el pedido esté en `PENDING_PAYMENT`.

### Persistencia

`InvoiceRepository.save(invoice)`.

### Auditoría

`INVOICE_ISSUED`.

### Errores

| Situación | Excepción |
|---|---|
| Factura duplicada | `IllegalStateException` |

---

## 2. ConsultInvoiceService

### Descripción

Consulta la factura de un pedido.

### Firma

```java
Invoice require(OrderId orderId);
Invoice findByOrder(User actor, OrderId orderId);
```

### Autorización

Los mismos permisos que para ver el pedido (`ConsultOrderService.findById`).

### Dependencias

`InvoiceRepository`, `ConsultOrderService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---
