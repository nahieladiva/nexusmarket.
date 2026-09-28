# Servicios de Productos

## Introducción

Gestionan el catálogo: creación de productos por vendedores aprobados, cambio de precio, ciclo de vida de publicación y consultas públicas.

Los servicios de este subdominio viven en `application.domain.services.product`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
Product (id, code : ProductCode, name, description, price : Money, sellerId : UserId,
         type : ProductType, status : ProductStatus, createdAt, updatedAt)

ProductType   : PHYSICAL | DIGITAL
ProductStatus : PUBLISHED ⇄ SUSPENDED → DISCONTINUED (terminal)
```

Solo los productos `PUBLISHED` son vendibles (`Product.isSellable()`). Solo los `PHYSICAL` manejan inventario (`Product.requiresInventory()`).

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

## 1. ConsultProductService

### Descripción

Consultas del catálogo.

### Firma

```java
Product findById(ProductId id);
Product findPublished(ProductId id);
List<Product> searchCatalog(String keyword);
List<Product> findBySeller(User seller);
```

### Autorización

`findById` es de uso interno. `findPublished` y `searchCatalog` son públicas. `findBySeller` requiere `AuthorizeSellerOperationService`.

### Dependencias

`ProductRepository`, `AuthorizeSellerOperationService`

### Validaciones y reglas

1. El catálogo público (`searchCatalog`, `findPublished`) solo devuelve productos `PUBLISHED`.
2. `findBySeller` devuelve los productos del vendedor en cualquier estado.

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |

---

## 2. CreateProductService

### Descripción

Regla R5: un vendedor aprobado publica un producto. El vendedor dueño es el usuario ejecutor, no un dato enviado por el cliente.

### Firma

```java
Product execute(User actor, ProductCode code, String name, String description,
                Money price, ProductType type);
```

### Autorización

`AuthorizeSellerOperationService`.

### Dependencias

`ProductRepository`, `AuthorizeSellerOperationService`, `RegisterAuditEventService`

### Validaciones y reglas

1. El código (SKU) debe ser único.
2. El precio debe ser positivo (lo valida `Product`).

### Comportamiento de dominio

`Product.create(...)`: estado inicial `PUBLISHED`.

### Persistencia

`ProductRepository.save(product)`.

### Auditoría

`PRODUCT_CREATED` con código, tipo y precio.

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| SKU repetido | `IllegalArgumentException` |

---

## 3. ChangeProductPriceService

### Descripción

El vendedor dueño cambia el precio de su producto.

### Firma

```java
Product execute(User actor, ProductId productId, Money newPrice);
```

### Autorización

`AuthorizeSellerOperationService` + `ValidateProductOwnershipService`.

### Dependencias

`ProductRepository`, `ConsultProductService`, `AuthorizeSellerOperationService`, `ValidateProductOwnershipService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Product.changePrice(newPrice)`: no aplica a productos descontinuados.

### Persistencia

`ProductRepository.save(product)`.

### Auditoría

`PRODUCT_PRICE_CHANGED` con precio anterior y nuevo.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Producto descontinuado | `InvalidStatusTransitionException` |

---

## 4. SuspendProductService

### Descripción

Suspende un producto publicado. Lo hace el vendedor dueño o, como moderación, un Administrador o Supervisor.

### Firma

```java
Product execute(User actor, ProductId productId);
```

### Autorización

Si el actor es vendedor: `AuthorizeSellerOperationService` + `ValidateProductOwnershipService`. En otro caso: `AuthorizeSupervisionOperationService`.

### Dependencias

`ProductRepository`, `ConsultProductService`, `AuthorizeSellerOperationService`, `AuthorizeSupervisionOperationService`, `ValidateProductOwnershipService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Product.suspend()`: `PUBLISHED → SUSPENDED`.

### Persistencia

`ProductRepository.save(product)`.

### Auditoría

`PRODUCT_STATUS_CHANGED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Transición inválida | `InvalidStatusTransitionException` |

---

## 5. PublishProductService

### Descripción

Vuelve a publicar un producto suspendido.

### Firma

```java
Product execute(User actor, ProductId productId);
```

### Autorización

`AuthorizeSellerOperationService` + `ValidateProductOwnershipService`.

### Dependencias

`ProductRepository`, `ConsultProductService`, `AuthorizeSellerOperationService`, `ValidateProductOwnershipService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Product.publish()`: `SUSPENDED → PUBLISHED`.

### Persistencia

`ProductRepository.save(product)`.

### Auditoría

`PRODUCT_STATUS_CHANGED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Transición inválida | `InvalidStatusTransitionException` |

---

## 6. DiscontinueProductService

### Descripción

Descontinúa un producto de forma definitiva.

### Firma

```java
Product execute(User actor, ProductId productId);
```

### Autorización

`AuthorizeSellerOperationService` + `ValidateProductOwnershipService`.

### Dependencias

`ProductRepository`, `ConsultProductService`, `AuthorizeSellerOperationService`, `ValidateProductOwnershipService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Product.discontinue()`: `PUBLISHED | SUSPENDED → DISCONTINUED` (terminal).

### Persistencia

`ProductRepository.save(product)`.

### Auditoría

`PRODUCT_STATUS_CHANGED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Ya estaba descontinuado | `InvalidStatusTransitionException` |

---
