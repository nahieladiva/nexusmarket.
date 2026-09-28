# Servicios de Autorización

## Introducción

Este subdominio concentra las validaciones de **quién puede ejecutar qué**. Los demás servicios las reutilizan en lugar de repetir comprobaciones de rol y estado. Es el equivalente a los servicios `Authorize*` y `Validate*` del proyecto de referencia.

Los servicios de este subdominio viven en `application.domain.services.authorization`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
User
 ├── status : UserStatus   (ACTIVE | BLOCKED)
 └── role   : UserRole     (BUYER | SELLER | LOGISTIC_OPERATOR | ADMIN | SUPERVISOR)

Seller extends User
 └── sellerStatus : SellerStatus (PENDING_APPROVAL | APPROVED | SUSPENDED)
```

Matriz de permisos por rol:

| Operación | BUYER | SELLER | LOGISTIC_OPERATOR | SUPERVISOR | ADMIN |
|---|---|---|---|---|---|
| Carrito, pedidos propios, devoluciones propias | ✔ | | | | |
| Publicar y gestionar productos propios | | ✔ (aprobado) | | | |
| Inventario, envíos, recepción de devoluciones | | | ✔ | | |
| Bloquear/activar usuarios, aprobar devoluciones, reembolsos, moderar productos | | | | ✔ | ✔ |
| Registrar vendedores y personal, aprobar vendedores, gestionar bodegas | | | | | ✔ |

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

## 1. ValidateActiveUserService

### Descripción

Verifica que exista un usuario ejecutor y que no esté bloqueado. Es la base de todos los servicios de autorización.

### Firma

```java
void execute(User actor);
```

### Autorización

No aplica: es el primer filtro.

### Validaciones y reglas

1. Si `actor` es `null`, la operación se rechaza.
2. Si `actor.status` es `BLOCKED`, la operación se rechaza (`User.requireActive()`).

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| No hay usuario ejecutor o está bloqueado | `UnauthorizedOperationException` |

---

## 2. ValidateRoleService

### Descripción

Verifica que el rol del usuario esté entre los roles permitidos. Compara enums, nunca cadenas de texto.

### Firma

```java
void execute(User actor, UserRole... allowedRoles);
```

### Autorización

No aplica.

### Validaciones y reglas

1. `actor.role` debe ser uno de `allowedRoles`.

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Rol no permitido | `UnauthorizedOperationException` |

---

## 3. AuthorizeAdminOperationService

### Descripción

Autoriza operaciones exclusivas del Administrador.

### Firma

```java
void execute(User actor);
```

### Autorización

Usuario activo con rol `ADMIN`.

### Dependencias

`ValidateActiveUserService`, `ValidateRoleService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 4. AuthorizeSupervisionOperationService

### Descripción

Autoriza operaciones de supervisión.

### Firma

```java
void execute(User actor);
```

### Autorización

Usuario activo con rol `ADMIN` o `SUPERVISOR`.

### Dependencias

`ValidateActiveUserService`, `ValidateRoleService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 5. AuthorizeBuyerOperationService

### Descripción

Autoriza operaciones del Comprador.

### Firma

```java
void execute(User actor);
```

### Autorización

Usuario activo con rol `BUYER`.

### Dependencias

`ValidateActiveUserService`, `ValidateRoleService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 6. AuthorizeSellerOperationService

### Descripción

Autoriza operaciones del Vendedor y devuelve el `Seller` ya validado.

### Firma

```java
Seller execute(User actor);
```

### Autorización

Usuario activo con rol `SELLER` y `sellerStatus = APPROVED` (regla R5).

### Dependencias

`ValidateActiveUserService`, `ValidateRoleService`

### Validaciones y reglas

1. El usuario debe ser una instancia de `Seller`.
2. El vendedor debe estar aprobado (`Seller.requireApprovedToPublish()`).

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Vendedor pendiente de aprobación o suspendido | `UnauthorizedOperationException` |

---

## 7. AuthorizeLogisticOperationService

### Descripción

Autoriza operaciones del Operador Logístico.

### Firma

```java
void execute(User actor);
```

### Autorización

Usuario activo con rol `LOGISTIC_OPERATOR`.

### Dependencias

`ValidateActiveUserService`, `ValidateRoleService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 8. ValidateOrderOwnershipService

### Descripción

Verifica que el pedido pertenezca al comprador que opera sobre él.

### Firma

```java
void execute(User buyer, Order order);
```

### Autorización

No aplica.

### Validaciones y reglas

1. `order.buyerId` debe ser igual a `buyer.id` (`Order.belongsTo`).

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El pedido es de otro comprador | `UnauthorizedOperationException` |

---

## 9. ValidateProductOwnershipService

### Descripción

Verifica que el producto pertenezca al vendedor que opera sobre él.

### Firma

```java
void execute(User seller, Product product);
```

### Autorización

No aplica.

### Validaciones y reglas

1. `product.sellerId` debe ser igual a `seller.id`.

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El producto es de otro vendedor | `UnauthorizedOperationException` |

---
