# Servicios de Pedidos

## Introducción

Orquestan el ciclo de vida del pedido: creación desde el carrito (checkout), pago, cancelación y consultas. Coordinan inventario, facturación y auditoría.

Los servicios de este subdominio viven en `application.domain.services.order`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Order (id, buyerId, items : List<OrderItem>, status : OrderStatus, createdAt, updatedAt)
OrderItem (productId, quantity, unitPrice : Money, warehouseId : WarehouseId | null)

CART → PENDING_PAYMENT → PAID → SHIPPED → DELIVERED (terminal, regla R8)
  └──────────┴──→ CANCELLED (solo antes del pago)
```

`OrderItem.warehouseId` registra de qué bodega se reservó el stock. Con ese dato se libera el stock al cancelar, se elige la bodega de origen del envío y se reingresan las devoluciones. Es `null` para productos digitales.

---

## Servicio de apoyo: PricingDomainService

`application.domain.services.pricing.PricingDomainService` calcula totales de líneas de pedido y aplica descuentos porcentuales (`Percentage`). No depende de ningún puerto.

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

## 1. ConsultOrderService

### Descripción

Consultas de pedidos.

### Firma

```java
Order require(OrderId id);
Order findById(User actor, OrderId id);
List<Order> findMine(User buyer);
List<Order> findAll(User actor);
```

### Autorización

`require` es de uso interno. `findById`: el comprador solo ve sus pedidos; Operador Logístico, Administrador y Supervisor ven cualquiera. `findMine`: comprador. `findAll`: supervisión.

### Dependencias

`OrderRepository`, `ValidateActiveUserService`, `ValidateRoleService`, `ValidateOrderOwnershipService`, `AuthorizeBuyerOperationService`, `AuthorizeSupervisionOperationService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 2. PlaceOrderService

### Descripción

Convierte el carrito del comprador en un pedido.

### Firma

```java
Order execute(User buyer);
```

### Autorización

`AuthorizeBuyerOperationService` (a través de `ConsultCartService`).

### Dependencias

`OrderRepository`, `CartRepository`, `ConsultCartService`, `ReserveStockService`, `IssueInvoiceService`, `RegisterAuditEventService`

### Validaciones y reglas

1. El carrito no puede estar vacío.
2. Debe haber stock suficiente en bodegas activas para cada producto físico (R6, R9).

### Comportamiento de dominio

1. `Cart.checkout()` crea el pedido en `PENDING_PAYMENT` y vacía el carrito.
2. `ReserveStockService` reserva el stock y asigna bodegas.
3. Se guardan el pedido y el carrito.
4. `IssueInvoiceService` emite la factura.

### Persistencia

`OrderRepository.save(order)`, `CartRepository.save(cart)`.

### Auditoría

`ORDER_PLACED` con total, número de ítems y número de factura.

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Carrito vacío | `IllegalStateException` |
| Stock insuficiente | `InsufficientStockException` |

---

## 3. PayOrderService

### Descripción

El comprador dueño paga su pedido.

### Firma

```java
Order execute(User buyer, OrderId orderId);
```

### Autorización

`AuthorizeBuyerOperationService` + `ValidateOrderOwnershipService`.

### Dependencias

`OrderRepository`, `InvoiceRepository`, `ConsultOrderService`, `ConsultInvoiceService`, `AuthorizeBuyerOperationService`, `ValidateOrderOwnershipService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Order.markAsPaid()` (`PENDING_PAYMENT → PAID`) e `Invoice.markAsPaid()` (`ISSUED → PAID`).

### Persistencia

`OrderRepository.save`, `InvoiceRepository.save`.

### Auditoría

`ORDER_PAID`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Transición inválida | `OrderStateTransitionException` |

---

## 4. CancelOrderService

### Descripción

Cancela un pedido no pagado, libera su stock y anula su factura.

### Firma

```java
Order execute(User actor, OrderId orderId);
```

### Autorización

El comprador dueño (`ValidateOrderOwnershipService`) o supervisión (`AuthorizeSupervisionOperationService`).

### Dependencias

`OrderRepository`, `InvoiceRepository`, `ConsultOrderService`, `ValidateActiveUserService`, `ValidateOrderOwnershipService`, `AuthorizeSupervisionOperationService`, `ReleaseStockService`, `RegisterAuditEventService`

### Validaciones y reglas

1. Solo se cancela en `CART` o `PENDING_PAYMENT`.

### Comportamiento de dominio

`Order.cancel()` → `ReleaseStockService` → `Invoice.voidInvoice()`.

### Persistencia

`OrderRepository.save`, `InvoiceRepository.save`.

### Auditoría

`ORDER_CANCELLED` con el estado anterior.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| El pedido ya fue pagado | `OrderStateTransitionException` |

---
