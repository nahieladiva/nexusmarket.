# Servicios de Bodegas

## Introducción

Gestionan las bodegas físicas donde se almacena el inventario. Solo el Administrador las crea, activa o desactiva.

Los servicios de este subdominio viven en `application.domain.services.warehouse`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Warehouse (id, name, address : Address, location : WarehouseLocation, status : WarehouseStatus)
WarehouseStatus : ACTIVE ⇄ INACTIVE
```

Regla **R9**: una bodega `INACTIVE` no participa en la asignación de pedidos ni recibe inventario nuevo.

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

## 1. ConsultWarehouseService

### Descripción

Consulta de bodegas.

### Firma

```java
Warehouse findById(WarehouseId id);
List<Warehouse> findAll();
List<Warehouse> findAll(User actor);
```

### Autorización

`findAll(actor)` requiere un usuario activo; los casos de uso de Administrador y Operador Logístico añaden la autorización de su rol.

### Dependencias

`WarehouseRepository`, `ValidateActiveUserService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |

---

## 2. CreateWarehouseService

### Descripción

Crea una bodega.

### Firma

```java
Warehouse execute(User admin, String name, Address address, WarehouseLocation location);
```

### Autorización

`AuthorizeAdminOperationService`.

### Dependencias

`WarehouseRepository`, `AuthorizeAdminOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Warehouse.create(...)`: estado inicial `ACTIVE`.

### Persistencia

`WarehouseRepository.save(warehouse)`.

### Auditoría

`WAREHOUSE_CREATED`.

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 3. ActivateWarehouseService

### Descripción

Reactiva una bodega.

### Firma

```java
Warehouse execute(User admin, WarehouseId warehouseId);
```

### Autorización

`AuthorizeAdminOperationService`.

### Dependencias

`WarehouseRepository`, `ConsultWarehouseService`, `AuthorizeAdminOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Warehouse.activate()`: `INACTIVE → ACTIVE`.

### Persistencia

`WarehouseRepository.save(warehouse)`.

### Auditoría

`WAREHOUSE_STATUS_CHANGED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Ya estaba activa | `InvalidStatusTransitionException` |

---

## 4. DeactivateWarehouseService

### Descripción

Desactiva una bodega (R9).

### Firma

```java
Warehouse execute(User admin, WarehouseId warehouseId);
```

### Autorización

`AuthorizeAdminOperationService`.

### Dependencias

`WarehouseRepository`, `ConsultWarehouseService`, `AuthorizeAdminOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Warehouse.deactivate()`: `ACTIVE → INACTIVE`.

### Persistencia

`WarehouseRepository.save(warehouse)`.

### Auditoría

`WAREHOUSE_STATUS_CHANGED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Ya estaba inactiva | `InvalidStatusTransitionException` |

---
