# Servicios de Inventario

## Introducción

Gestionan el stock de cada producto físico en cada bodega: apertura de inventario, entradas, ajustes, reservas por pedidos, liberación por cancelaciones y reingreso por devoluciones. Todo cambio de stock queda trazado (regla R7).

Los servicios de este subdominio viven en `application.domain.services.inventory`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Inventory (id, productId, warehouseId, onHand : Quantity, reorderThreshold : Quantity, location)
   │  cada operación de stock devuelve un
   ▼
InventoryMovement (id, inventoryId, type : MovementType, quantity, resultingOnHand, reason, occurredAt)

MovementType : INFLOW | RESERVATION | SALE | ADJUSTMENT | RETURN
```

Regla **R6**: el stock nunca queda negativo (`Quantity` no admite valores negativos). Un par (producto, bodega) tiene un único inventario.

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

## 1. ConsultInventoryService

### Descripción

Consulta de inventario.

### Firma

```java
Inventory findByProductAndWarehouse(ProductId productId, WarehouseId warehouseId);
List<Inventory> findByWarehouse(User actor, WarehouseId warehouseId);
List<Inventory> findByProduct(User actor, ProductId productId);
```

### Autorización

Las consultas por bodega o producto requieren un usuario activo.

### Dependencias

`InventoryRepository`, `ValidateActiveUserService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |

---

## 2. RegisterInventoryMovementService

### Descripción

Servicio interno que guarda el inventario tras un movimiento, lo audita y emite la alerta de stock bajo.

### Firma

```java
Inventory execute(User actor, Inventory inventory, InventoryMovement movement);
```

### Autorización

No aplica: lo invocan otros servicios ya autorizados.

### Dependencias

`InventoryRepository`, `RegisterAuditEventService`

### Validaciones y reglas

1. Si tras el movimiento `onHand < reorderThreshold`, se emite `LowStockEvent`.

### Persistencia

`InventoryRepository.save(inventory)`.

### Auditoría

`INVENTORY_MOVEMENT` con tipo, cantidad, stock resultante y motivo. Además, `LowStockEvent` cuando aplica.

---

## 3. CreateInventoryService

### Descripción

El Operador Logístico abre el inventario de un producto físico en una bodega.

### Firma

```java
Inventory execute(User operator, ProductId productId, WarehouseId warehouseId,
                  Quantity initialStock, Quantity reorderThreshold, WarehouseLocation location);
```

### Autorización

`AuthorizeLogisticOperationService`.

### Dependencias

`InventoryRepository`, `ConsultProductService`, `ConsultWarehouseService`, `AuthorizeLogisticOperationService`, `RegisterAuditEventService`

### Validaciones y reglas

1. El producto debe ser `PHYSICAL`.
2. La bodega debe estar `ACTIVE` (R9).
3. No puede existir ya inventario para ese par (producto, bodega).

### Comportamiento de dominio

`Inventory.create(...)`.

### Persistencia

`InventoryRepository.save(inventory)`.

### Auditoría

`INVENTORY_CREATED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Producto digital o inventario duplicado | `IllegalArgumentException` |
| Bodega inactiva | `IllegalStateException` |

---

## 4. ReceiveStockService

### Descripción

Entrada de mercancía a bodega.

### Firma

```java
Inventory execute(User operator, ProductId productId, WarehouseId warehouseId, Quantity quantity);
```

### Autorización

`AuthorizeLogisticOperationService`.

### Dependencias

`ConsultInventoryService`, `AuthorizeLogisticOperationService`, `RegisterInventoryMovementService`

### Comportamiento de dominio

`Inventory.receive(quantity)` → movimiento `INFLOW`.

### Persistencia

Mediante `RegisterInventoryMovementService`.

### Auditoría

`INVENTORY_MOVEMENT`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 5. AdjustStockService

### Descripción

Ajuste manual por conteo físico (delta positivo o negativo).

### Firma

```java
Inventory execute(User operator, ProductId productId, WarehouseId warehouseId, int delta, String reason);
```

### Autorización

`AuthorizeLogisticOperationService`.

### Dependencias

`ConsultInventoryService`, `AuthorizeLogisticOperationService`, `RegisterInventoryMovementService`

### Validaciones y reglas

1. El resultado no puede ser negativo (R6).

### Comportamiento de dominio

`Inventory.adjust(delta, reason)` → movimiento `ADJUSTMENT`.

### Persistencia

Mediante `RegisterInventoryMovementService`.

### Auditoría

`INVENTORY_MOVEMENT`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| El stock quedaría negativo | `InsufficientStockException` |

---

## 6. AllocateWarehouseService

### Descripción

Elige de qué bodega sale un ítem de pedido.

### Firma

```java
Inventory execute(OrderItem item);
```

### Autorización

No aplica (servicio interno).

### Dependencias

`InventoryRepository`, `WarehouseRepository`

### Validaciones y reglas

1. Solo se consideran bodegas `ACTIVE` (R9).
2. Se elige el primer inventario con stock suficiente (`Inventory.isAvailable`).

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Ninguna bodega activa tiene stock suficiente | `InsufficientStockException` |

---

## 7. ReserveStockService

### Descripción

Reserva el stock de los productos físicos de un pedido recién creado.

### Firma

```java
void execute(User actor, Order order);
```

### Autorización

No aplica: lo invoca `PlaceOrderService`.

### Dependencias

`ConsultProductService`, `AllocateWarehouseService`, `RegisterInventoryMovementService`

### Validaciones y reglas

1. Los productos `DIGITAL` no reservan stock.

### Comportamiento de dominio

Por cada ítem físico: `AllocateWarehouseService` → `Inventory.reserve(qty)` (movimiento `RESERVATION`). Al final, `Order.assignWarehouses(asignación)` guarda en cada `OrderItem` la bodega elegida.

### Persistencia

Mediante `RegisterInventoryMovementService`.

### Auditoría

`INVENTORY_MOVEMENT` por ítem.

### Errores

| Situación | Excepción |
|---|---|
| Stock insuficiente | `InsufficientStockException` |

---

## 8. ReleaseStockService

### Descripción

Devuelve a su bodega el stock reservado por un pedido cancelado.

### Firma

```java
void execute(User actor, Order order);
```

### Autorización

No aplica: lo invoca `CancelOrderService`.

### Dependencias

`ConsultInventoryService`, `RegisterInventoryMovementService`

### Comportamiento de dominio

Por cada `OrderItem` con bodega asignada: `Inventory.adjust(+qty, "Liberación de reserva …")`.

### Persistencia

Mediante `RegisterInventoryMovementService`.

### Auditoría

`INVENTORY_MOVEMENT`.

---

## 9. RestockReturnedItemService

### Descripción

Reingresa a su bodega de origen las unidades devueltas.

### Firma

```java
Inventory execute(User operator, ProductId productId, WarehouseId warehouseId, Quantity quantity);
```

### Autorización

No aplica: lo invoca `ReceiveReturnService`.

### Dependencias

`ConsultInventoryService`, `RegisterInventoryMovementService`

### Comportamiento de dominio

`Inventory.registerReturn(quantity)` → movimiento `RETURN`.

### Persistencia

Mediante `RegisterInventoryMovementService`.

### Auditoría

`INVENTORY_MOVEMENT`.

---
