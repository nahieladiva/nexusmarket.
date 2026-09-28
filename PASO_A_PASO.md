# Paso a paso para aplicar la nueva capa de servicios en NexusMarket

Este ZIP contiene el repositorio **completo**, con la misma estructura de carpetas que el tuyo. Incluye las correcciones de la entrega anterior y la nueva capa de servicios, construida con el mismo patrón que el proyecto del banco.

Rutas Java abreviadas: `…/` = `nexusm/src/main/java/application/`

---

## Paso 1. Respaldo
En la carpeta de tu repositorio `nexusmarket`:

```bash
git checkout -b servicios-dominio
```

Si algo sale mal, vuelves con `git checkout main`.

## Paso 2. Borrar las carpetas viejas (importante)
Esta vez **se eliminaron archivos**, por ejemplo la carpeta `…/services/`, los `*ManagementPort` y los controladores por recurso. Si solo copias encima, los archivos viejos se quedan y el proyecto no compila.

Borra por completo estas carpetas de tu repositorio:

- `nexusm/src/`
- `SDD/`

Si aún no aplicaste la entrega anterior, borra también:

- los archivos `build_main.log`, `dep_filter.log`, `dep_tree.log` y `_sdd_list.txt` de la raíz;
- la carpeta `…/infrastructure/database/`.

## Paso 3. Copiar desde el ZIP

| Del ZIP | A tu repositorio |
|---|---|
| `SDD/` | `SDD/` |
| `nexusm/src/` | `nexusm/src/` |
| `nexusm/pom.xml` | `nexusm/pom.xml` |
| `README.md`, `.gitignore`, `docker-compose.yml`, `PASO_A_PASO.md` | raíz del repositorio |

## Paso 4. Borrar la base de datos vieja
Hay tablas nuevas (`carts`, `cart_items`, `invoices`, `shipments`, `return_requests` y `refunds`) y una columna nueva (`order_items.warehouse_id`). Empieza con una base limpia:

```sql
DROP DATABASE IF EXISTS nexusmarket;
CREATE DATABASE nexusmarket;
```

En MongoDB puedes borrar la colección `audit_logs`. Los eventos viejos tienen otro formato.

## Paso 5. Levantar las bases de datos

```bash
docker compose up -d
```

Si ya tienes MySQL local con usuario `root` y contraseña `root`, puedes omitir este paso.

## Paso 6. Compilar y probar

```bash
cd nexusm
mvnw.cmd clean test
```

Usa `./mvnw clean test` en Mac o Linux. `NexusmarketApplicationTests` levanta Spring completo, así que necesita MySQL y MongoDB corriendo.

## Paso 7. Subir los cambios

```bash
git add -A
git commit -m "Servicios de dominio por caso de uso, puertos y casos de uso por rol, SDD de servicios"
git push -u origin servicios-dominio
```

---

## Qué se tomó del proyecto del banco y cómo se adaptó

| Banco | NexusMarket |
|---|---|
| `domain/services/<subdominio>/<Acción>Service`, un caso de uso por clase | Igual. Son 60 servicios en 13 subdominios: `authorization`, `audit`, `user`, `product`, `warehouse`, `inventory`, `cart`, `order`, `invoice`, `shipment`, `returns`, `refund` y `pricing` |
| `Authorize*Service` y `Validate*Service` reutilizables | `authorization/`: autorización por rol, usuario activo y propiedad de pedidos y productos |
| `RegisterOperationAndAuditService` | `audit/RegisterAuditEventService`, con el evento `BusinessOperationEvent` y el enum `OperationType` (24 operaciones auditables) |
| Puertos de entrada por rol (`NaturalCustomerPort`…) | `PublicAccessPort`, `BuyerPort`, `SellerPort`, `LogisticOperatorPort`, `AdminPort`, `SupervisorPort` |
| `adapters/useCases/<Rol>UseCaseImpl` | Los mismos seis `*UseCaseImpl`, con `@Transactional` |
| `SDD/domain/services/*-services.md` | 12 documentos en `SDD/domain/services/`, uno por subdominio |
| **Defecto:** `@Service` y Lombok dentro del dominio | **Corregido:** anotación propia `@DomainService` en Java puro, que `ApplicationConfig` escanea. El dominio sigue con 0 imports de Spring |

**Otros cambios**
- `OrderItem` guarda la bodega de la que se reservó el stock (`warehouseId`). Con ese dato:
  - al cancelar un pedido se libera el stock en la bodega correcta,
  - el envío sale de esa bodega,
  - las devoluciones reingresan ahí.
- Flujo de posventa completo por API: factura, envío, devolución y reembolso.
- El precio del carrito se toma del catálogo; el cliente ya no lo envía.
- El vendedor de un producto es quien ejecuta la operación (cabecera `X-User-Id`); ya no se envía `sellerId` en el cuerpo.
- `AdminSeeder` escribe en la consola el id del Administrador inicial al arrancar.
- Nueva prueba `MarketplaceFlowTest`, que ejecuta los servicios encadenados con repositorios en memoria.

**Eliminados**
- `…/services/`: los cinco `*ApplicationService`.
- `…/domain/ports/in/*ManagementPort`.
- `…/domain/services/InventoryDomainService`, `OrderFulfillmentDomainService` y `WarehouseAllocationService`, reemplazados por servicios por caso de uso.
- Los eventos `OrderCreatedEvent`, `OrderStatusChangedEvent`, `ProductAddedEvent` e `InventoryUpdatedEvent`, reemplazados por `BusinessOperationEvent`.
- Los controladores `UserController`, `ProductController`, `WarehouseController`, `InventoryController` y `OrderController`, reemplazados por un controlador por rol.
- Los DTO `CreateOrderRequest` y `OrderItemRequest`: ahora el pedido se crea desde el carrito.

---

## Probar en Postman

Todas las rutas, excepto `/api/public/**`, llevan la cabecera `X-User-Id` con el id de quien ejecuta la operación. La tabla completa de endpoints está en `SDD/Software Architecture/Software Architecture.md`, sección 7.

1. **Arranca la aplicación** y copia de la consola la línea `Administrador inicial: id=...`.

2. **Registra al personal.** Llama a `POST /api/admin/staff` con `X-User-Id: <admin>`, una vez con el rol `LOGISTIC_OPERATOR` y otra con `SUPERVISOR`:

   ```json
   { "identification": "1000000003", "fullName": "Operador", "email": "op@nx.com",
     "phone": "+573000000003", "password": "Clave123*", "role": "LOGISTIC_OPERATOR" }
   ```

3. **Registra y aprueba un vendedor.**
   - `POST /api/admin/sellers`, con `X-User-Id: <admin>`.
   - `PATCH /api/admin/sellers/{id}/approve`, con `X-User-Id: <admin>`.

4. **Crea una bodega.** Llama a `POST /api/admin/warehouses` con `X-User-Id: <admin>`.

5. **Publica un producto.** Llama a `POST /api/seller/products` con `X-User-Id: <vendedor>`:

   ```json
   { "code": "TEC-0001", "name": "Teclado", "description": "Mecánico",
     "price": { "amount": "100.00", "currency": "USD" }, "type": "PHYSICAL" }
   ```

6. **Crea el inventario.** Llama a `POST /api/logistics/inventory` con `X-User-Id: <operador>`:

   ```json
   { "productId": "...", "warehouseId": "...", "onHand": 10, "reorderThreshold": 2,
     "location": { "aisle": "A", "shelf": "1", "bin": "1" } }
   ```

7. **Registra un comprador.** Llama a `POST /api/public/buyers`. No lleva cabecera.

8. **Compra como comprador** (`X-User-Id: <comprador>`):
   - Agrega al carrito con `POST /api/buyer/cart/items`, cuerpo `{ "productId": "...", "quantity": 2 }`.
   - Crea el pedido con `POST /api/buyer/orders`. El pedido queda `PENDING_PAYMENT`, el stock se reserva y se emite la factura.
   - Paga con `PATCH /api/buyer/orders/{id}/pay`.

9. **Envía el pedido** (`X-User-Id: <operador>`):
   - `POST /api/logistics/shipments`, cuerpo `{ "orderId": "..." }`.
   - `PATCH /api/logistics/shipments/{id}/dispatch`, cuerpo `{ "trackingNumber": "GUIA-123" }`.
   - `PATCH /api/logistics/shipments/{id}/deliver`.

10. **Gestiona una devolución:**
    - El comprador la solicita con `POST /api/buyer/returns`, cuerpo `{ "orderId": "...", "productId": "...", "quantity": 1, "reason": "Defectuoso" }`.
    - El supervisor la aprueba con `PATCH /api/supervisor/returns/{id}/approve`.
    - El operador la recibe con `PATCH /api/logistics/returns/{id}/receive`. El stock reingresa a la bodega.
    - El supervisor reembolsa con `POST /api/supervisor/returns/{id}/refund`.

11. **Revisa la auditoría.** En MongoDB, la colección `audit_logs` tiene un documento por operación: `ORDER_PLACED`, `ORDER_PAID`, `REFUND_COMPLETED`…

### Equivalencia de rutas anteriores

| Antes | Ahora |
|---|---|
| `POST /api/users/buyers` | `POST /api/public/buyers` |
| `POST /api/users/sellers`, `/api/users/staff` | `POST /api/admin/sellers`, `/api/admin/staff` |
| `PATCH /api/users/sellers/{id}/approve` | `PATCH /api/admin/sellers/{id}/approve` |
| `PATCH /api/users/{id}/block` | `PATCH /api/supervisor/users/{id}/block` |
| `GET /api/users` | `GET /api/supervisor/users` |
| `POST /api/products` (con `sellerId`) | `POST /api/seller/products` (sin `sellerId`) |
| `GET /api/products` | `GET /api/public/products` |
| `POST /api/warehouses` | `POST /api/admin/warehouses` |
| `POST /api/inventory`, `PATCH /api/inventory/adjust` | `POST /api/logistics/inventory`, `PATCH /api/logistics/inventory/adjust` |
| `POST /api/orders` (con ítems) | Carrito (`/api/buyer/cart/items`) y luego `POST /api/buyer/orders` |
| `PATCH /api/orders/{id}/ship`, `/deliver` | `/api/logistics/shipments/{id}/dispatch`, `/deliver` |

---

## Correcciones de la entrega anterior (incluidas en este ZIP)

Esta lista describe la entrega anterior. Algunos de esos archivos (los `*ApplicationService`, los `*ManagementPort`, los servicios de dominio antiguos y `UserApplicationServiceTest`) fueron reemplazados después por la nueva capa de servicios descrita arriba.

#### Errores que impedían compilar o arrancar
- **`nexusm/pom.xml`:** `spring-boot-starter-mongodb` se cambió por `spring-boot-starter-data-mongodb`. El primero no trae Spring Data, así que no existían `MongoRepository` ni `@Document`.
- **`…/adapters/out/persistence/mongodb/mappers/AuditLogMapper.java`:** ya no usa `com.fasterxml.jackson`, que no existe en Spring Boot 4 porque ahora se usa Jackson 3.
- **`…/infrastructure/database/DatabaseConfig.java`:** eliminado.
- **`…/infrastructure/config/ApplicationConfig.java`:** registra los 4 servicios de dominio como beans. Sin esto, `OrderApplicationService` no podía crearse.
- **`…/adapters/out/persistence/mysql/mappers/UserEntityMapper.java`:** llamaba a un constructor `protected` de `User` desde otro paquete. Ese constructor ahora es público.
- **Ajuste negativo de inventario:** `Quantity.of(-5)` lanzaba una excepción. Ahora el ajuste recibe un `int delta`.

#### Nuevos (37 archivos)
- **`…/domain/enums/`:** `UserStatus`, `SellerStatus`, `ProductType`, `ProductStatus`, `WarehouseStatus`, `MovementType`, `InvoiceStatus`, `ShipmentStatus`, `ReturnStatus`, `RefundStatus`.
- **`…/domain/valueobjects/`:** `IdentificationNumber`, `CartId`, `InvoiceId`, `ShipmentId`, `ReturnId`, `RefundId`, `MovementId`.
- **`…/domain/models/`:** `Cart`, `CartItem`, `Invoice`, `Shipment`, `ReturnRequest`, `Refund`, `InventoryMovement`.
- **`…/domain/exceptions/`:** `InvalidStatusTransitionException`, `UnauthorizedOperationException`, `UserAlreadyExistsException`.
- **`…/adapters/in/rest/requests/`:** `CreateStaffUserRequest`.
- **`…/infrastructure/config/`:** `AdminSeeder`, que crea el primer Administrador.
- **`nexusm/src/test/java/application/domain/models/`:** `TestData`, `UserTest`, `ProductTest`, `InventoryTest`, `CartTest`, `PostSaleFlowTest`.
- **`nexusm/src/test/java/application/services/`:** `UserApplicationServiceTest`.
- **Raíz del repositorio:** `README.md`, `.gitignore`, `docker-compose.yml`.

#### Modificados

**Dominio**
- **Enums:** `UserRole` (agrega `LOGISTIC_OPERATOR` y `SUPERVISOR`) y `OrderStatus` (`CART`, `PENDING_PAYMENT`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
- **Modelos:**
  - `User`: identificación, estado y personal interno.
  - `Buyer` y `Seller`: `Seller` usa `SellerStatus`.
  - `Product`: tipo y estado.
  - `Warehouse`: `WarehouseStatus`.
  - `Inventory`: registra movimientos y nunca queda negativo.
  - `Order`: nuevo ciclo de vida.
- **Puertos:** `UserManagementPort`, `ProductManagementPort`, `OrderManagementPort`, `InventoryManagementPort` y `UserRepository`.
- **Servicios de dominio:** `InventoryDomainService`, `OrderFulfillmentDomainService` y `WarehouseAllocationService` (ahora ignora las bodegas inactivas).

**Casos de uso**
- `UserApplicationService`: unicidad y reglas de Administrador.
- `ProductApplicationService`: solo vendedores aprobados publican.
- `OrderApplicationService`: `payOrder`.
- `InventoryApplicationService`.

**REST**
- **Controllers:** `UserController`, `ProductController`, `OrderController`, `InventoryController` y `GlobalExceptionHandler`.
- **Mappers:** `UserMapper`, `ProductMapper` y `WarehouseMapper`.
- **Requests:** `CreateBuyerRequest`, `CreateSellerRequest`, `CreateProductRequest` y `AdjustInventoryRequest`.
- **Responses:** `UserResponse`, `ProductResponse` y `WarehouseResponse`.

**Persistencia**
- **Entidades:** `UserJpaEntity`, `ProductJpaEntity` y `WarehouseJpaEntity`.
- **Mappers:** `UserEntityMapper`, `ProductEntityMapper` y `WarehouseEntityMapper`.
- **Repositorio y adaptador:** `UserJpaRepository` y `UserPersistenceAdapter`.

**Configuración**
- `application.properties`: datos del administrador inicial.

**Pruebas**
- `OrderTest` y `OrderStatusTest`.

**SDD (reescritos por completo, 1:1 con el código)**
- `SDD/domain/Domain Model.md`
- `SDD/domain/Domain Value Objects.md`
- `SDD/Software Architecture/Software Architecture.md`

