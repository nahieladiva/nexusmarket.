# Servicios del Carrito

## Introducción

Gestionan el carrito de compras del comprador. Cada comprador tiene un único carrito, que se crea vacío la primera vez que se consulta.

Los servicios de este subdominio viven en `application.domain.services.cart`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Cart (id, buyerId, items : List<CartItem>, updatedAt)
CartItem (productId, quantity : Quantity, unitPrice : Money)
```

Si se agrega un producto que ya está en el carrito, las cantidades se suman. Todos los ítems deben estar en la misma moneda.

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

## 1. ConsultCartService

### Descripción

Devuelve el carrito del comprador, o uno nuevo y vacío si aún no tiene.

### Firma

```java
Cart execute(User buyer);
```

### Autorización

`AuthorizeBuyerOperationService`.

### Dependencias

`CartRepository`, `AuthorizeBuyerOperationService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 2. AddItemToCartService

### Descripción

Agrega un producto al carrito.

### Firma

```java
Cart execute(User buyer, ProductId productId, Quantity quantity);
```

### Autorización

`AuthorizeBuyerOperationService` (a través de `ConsultCartService`).

### Dependencias

`CartRepository`, `ConsultCartService`, `ConsultProductService`

### Validaciones y reglas

1. El producto debe estar `PUBLISHED`.
2. El precio unitario **se toma del catálogo**; el cliente no puede enviarlo.

### Comportamiento de dominio

`Cart.addItem(productId, quantity, product.price)`.

### Persistencia

`CartRepository.save(cart)`.

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Producto no vendible | `IllegalStateException` |

---

## 3. ChangeCartItemQuantityService

### Descripción

Cambia la cantidad de una línea; con cantidad cero la elimina.

### Firma

```java
Cart execute(User buyer, ProductId productId, Quantity newQuantity);
```

### Autorización

`AuthorizeBuyerOperationService`.

### Dependencias

`CartRepository`, `ConsultCartService`

### Comportamiento de dominio

`Cart.changeQuantity(...)`.

### Persistencia

`CartRepository.save(cart)`.

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| El producto no está en el carrito | `IllegalArgumentException` |

---

## 4. RemoveItemFromCartService

### Descripción

Quita un producto del carrito.

### Firma

```java
Cart execute(User buyer, ProductId productId);
```

### Autorización

`AuthorizeBuyerOperationService`.

### Dependencias

`CartRepository`, `ConsultCartService`

### Comportamiento de dominio

`Cart.removeItem(productId)`.

### Persistencia

`CartRepository.save(cart)`.

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| El producto no está en el carrito | `IllegalArgumentException` |

---
