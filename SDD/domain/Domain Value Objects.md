# Domain Value Objects - NexusMarket

## 1. Introducción

### 1.1 Propósito
Este documento define los **Value Objects (VO)** y los **enums** del dominio de NexusMarket. Su objetivo es eliminar la *obsesión por primitivos*: ningún campo acotado (estado, rol, tipo) ni ningún valor con reglas de formato (correo, teléfono, dinero, cantidad) se modela como `String` o número abierto.

### 1.2 ¿Qué es un Value Object?
Es un objeto que se define por su valor y no por una identidad. Dos VO con el mismo valor son iguales.

### 1.3 Principios de diseño
- **Inmutabilidad:** todos los campos son `final`. Las operaciones devuelven nuevas instancias.
- **Auto-validación:** el constructor rechaza valores inválidos, así que un VO inválido no puede existir.
- **Igualdad por valor:** `equals` y `hashCode` comparan el valor.
- **Sin identidad:** los VO no tienen id propio. Los identificadores tipados (`UserId`…) son VO que *representan* la identidad de una entidad.

Todos los VO viven en `application.domain.valueobjects` y todos los enums en `application.domain.enums`.

---

## 2. Lista completa de Value Objects

| VO | Representa | Regla principal |
|---|---|---|
| `Email` | Correo electrónico | Formato válido, en minúsculas |
| `PhoneNumber` | Teléfono | Formato internacional E.164 |
| `Address` | Dirección postal | Calle, ciudad, estado, código postal y país obligatorios |
| `IdentificationNumber` | Cédula / NIT / pasaporte | 5 a 20 caracteres alfanuméricos o guion |
| `Money` | Monto con moneda | No negativo, 2 decimales, misma moneda |
| `Quantity` | Cantidad de unidades | Entero no negativo |
| `Percentage` | Porcentaje | Rango [0, 100] |
| `ProductCode` | SKU | Formato `XXX-0000` |
| `DateRange` | Rango de fechas | El inicio no puede ser posterior al fin |
| `WarehouseLocation` | Ubicación en bodega | Pasillo, estante y cajón |
| `UserId`, `ProductId`, `OrderId`, `WarehouseId`, `InventoryId`, `CartId`, `InvoiceId`, `ShipmentId`, `ReturnId`, `RefundId`, `MovementId` | Identidades tipadas | UUID no nulo |

---

## 3. Definiciones detalladas

### 3.1 Email
- **Estructura:** `value: String`.
- **Validación:** no puede ser nulo ni vacío. Debe cumplir `^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$` y se normaliza a minúsculas.
- **Fábrica:** `Email.of("ana@correo.com")`.
- **Uso:** `User.email`, que es único en la plataforma.

### 3.2 PhoneNumber
- **Estructura:** `value: String`.
- **Validación:** formato E.164 `^\+[1-9]\d{1,14}$`. Los espacios y guiones se eliminan antes de validar.
- **Fábrica:** `PhoneNumber.of("+573001112233")`.
- **Uso:** `User.phone`.

### 3.3 Address
- **Estructura:** `street`, `city`, `state`, `zipCode` y `country`.
- **Validación:** todos los componentes son obligatorios.
- **Operaciones:** `fullAddress()`.
- **Uso:**
  - `User.addresses` y `Buyer.defaultShippingAddress`
  - `Warehouse.address`
  - `Shipment.shippingAddress`

### 3.4 IdentificationNumber
- **Estructura:** `value: String`, normalizado a mayúsculas y sin espacios en los extremos.
- **Validación:** obligatorio y con formato `^[A-Za-z0-9-]{5,20}$`. Admite NIT con dígito de verificación, por ejemplo `900123456-7`.
- **Fábrica:** `IdentificationNumber.of("1017123456")`.
- **Uso:** `User.identification`, que es **único** en la plataforma.

### 3.5 Money
- **Estructura:** `amount: BigDecimal` y `currency: java.util.Currency`.
- **Validación:** el monto no puede ser nulo ni negativo, y la moneda es obligatoria. Se redondea a 2 decimales con `HALF_UP`.
- **Operaciones:** `add`, `subtract`, `multiply(int)`, `multiply(BigDecimal)`, `isGreaterThan`, `isLessThan` e `isZero`.
- **Manejo de monedas:** operar montos de monedas distintas lanza una excepción.
- **Fábrica:** `Money.of("19.99", "USD")` y `Money.zero("COP")`.
- **Uso:**
  - `Product.price`
  - `OrderItem.unitPrice`, `CartItem.unitPrice` y `Order.total`
  - `Invoice.total`
  - `Refund.amount`

### 3.6 Quantity
- **Estructura:** `value: int`.
- **Validación:** entero no negativo. Una resta que dé negativo lanza `IllegalArgumentException`.
- **Operaciones:** `add`, `subtract`, `isZero` y `compareTo`.
- **Fábrica:** `Quantity.of(5)` y `Quantity.zero()`.
- **Uso:**
  - `Inventory.onHand` y `Inventory.reorderThreshold`
  - `OrderItem.quantity` y `CartItem.quantity`
  - `ReturnRequest.quantity`
  - `InventoryMovement.quantity`

### 3.7 Percentage
- **Estructura:** `value: BigDecimal`.
- **Validación:** rango [0, 100], con 2 decimales.
- **Operaciones:** `asFraction()` (por ejemplo, 15 → 0.15).
- **Uso:** `PricingDomainService.applyPercentageDiscount`.

### 3.8 ProductCode (SKU)
- **Estructura:** `value: String`.
- **Validación:** `^[A-Z]{2,5}-\d{4,8}$`, por ejemplo `ELE-000123`.
- **Fábrica:** `ProductCode.of("PRD-0001")` y `ProductCode.generate("ELEC", 15)`.
- **Uso:** `Product.code`, que es único.

### 3.9 DateRange
- **Estructura:** `startDate` y `endDate` (`LocalDate`), inclusivos.
- **Operaciones:** `contains`, `overlaps` y `durationInDays`.

### 3.10 WarehouseLocation
- **Estructura:** `aisle`, `shelf` y `bin`.
- **Operaciones:** `code()`.
- **Uso:** `Warehouse.location` e `Inventory.location`.

### 3.11 Identificadores tipados
`UserId`, `ProductId`, `OrderId`, `WarehouseId`, `InventoryId`, `CartId`, `InvoiceId`, `ShipmentId`, `ReturnId`, `RefundId` y `MovementId`.

- **Estructura:** `value: UUID`, no nulo.
- **Fábricas:** `of(UUID)`, `of(String)` y `random()`.
- **Motivo:** evitan confundir, por ejemplo, un `ProductId` con un `UserId`, porque el compilador lo impide.

---

## 4. Enums (tipos acotados)

| Enum | Valores | Usado en |
|---|---|---|
| `UserRole` | `BUYER`, `SELLER`, `LOGISTIC_OPERATOR`, `ADMIN`, `SUPERVISOR` | `User.role` |
| `UserStatus` | `ACTIVE`, `BLOCKED` | `User.status` |
| `SellerStatus` | `PENDING_APPROVAL`, `APPROVED`, `SUSPENDED` | `Seller.sellerStatus` |
| `ProductType` | `PHYSICAL`, `DIGITAL` | `Product.type` |
| `ProductStatus` | `PUBLISHED`, `SUSPENDED`, `DISCONTINUED` | `Product.status` |
| `WarehouseStatus` | `ACTIVE`, `INACTIVE` | `Warehouse.status` |
| `MovementType` | `INFLOW`, `RESERVATION`, `SALE`, `ADJUSTMENT`, `RETURN` | `InventoryMovement.type` |
| `OrderStatus` | `CART`, `PENDING_PAYMENT`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED` | `Order.status` |
| `InvoiceStatus` | `ISSUED`, `PAID`, `VOIDED` | `Invoice.status` |
| `ShipmentStatus` | `PREPARING`, `IN_TRANSIT`, `DELIVERED` | `Shipment.status` |
| `ReturnStatus` | `REQUESTED`, `APPROVED`, `REJECTED`, `RECEIVED` | `ReturnRequest.status` |
| `RefundStatus` | `PENDING`, `COMPLETED`, `FAILED` | `Refund.status` |
| `OperationType` | `USER_REGISTERED`, `SELLER_APPROVED`, `SELLER_SUSPENDED`, `USER_BLOCKED`, `USER_ACTIVATED`, `PRODUCT_CREATED`, `PRODUCT_PRICE_CHANGED`, `PRODUCT_STATUS_CHANGED`, `WAREHOUSE_CREATED`, `WAREHOUSE_STATUS_CHANGED`, `INVENTORY_CREATED`, `INVENTORY_MOVEMENT`, `ORDER_PLACED`, `ORDER_PAID`, `ORDER_CANCELLED`, `INVOICE_ISSUED`, `SHIPMENT_CREATED`, `SHIPMENT_DISPATCHED`, `SHIPMENT_DELIVERED`, `RETURN_REQUESTED`, `RETURN_APPROVED`, `RETURN_REJECTED`, `RETURN_RECEIVED`, `REFUND_COMPLETED` | `BusinessOperationEvent.operationType` (auditoría) |

### 4.1 Comportamiento de los enums
- Los enums de estado con ciclo de vida exponen `canTransitionTo(target)`, con su tabla de transiciones permitidas. Son `OrderStatus`, `ProductStatus`, `InvoiceStatus`, `ShipmentStatus`, `ReturnStatus` y `RefundStatus`.
- `OrderStatus` expone además:
  - `isFinal()`: `DELIVERED` y `CANCELLED` son inmutables.
  - `requireCanTransitionTo(target)`.
  - `fromString(value)`.
- `UserRole.isStaff()` indica si el rol es de personal interno (`LOGISTIC_OPERATOR`, `ADMIN` o `SUPERVISOR`).
- `SellerStatus.canPublishProducts()` es verdadero solo en `APPROVED`.
- `ProductType.requiresInventory()` es verdadero solo en `PHYSICAL`.
- `ProductStatus.isSellable()` es verdadero solo en `PUBLISHED`.
- `MovementType.increasesStock()` es verdadero en `INFLOW` y `RETURN`. `MovementType.decreasesStock()` es verdadero en `RESERVATION` y `SALE`. `ADJUSTMENT` puede ir en ambos sentidos.

### 4.2 Tablas de transición

| Enum | Transiciones permitidas |
|---|---|
| `OrderStatus` | CART→PENDING_PAYMENT, CART→CANCELLED, PENDING_PAYMENT→PAID, PENDING_PAYMENT→CANCELLED, PAID→SHIPPED, SHIPPED→DELIVERED |
| `ProductStatus` | PUBLISHED→SUSPENDED, PUBLISHED→DISCONTINUED, SUSPENDED→PUBLISHED, SUSPENDED→DISCONTINUED |
| `InvoiceStatus` | ISSUED→PAID, ISSUED→VOIDED |
| `ShipmentStatus` | PREPARING→IN_TRANSIT, IN_TRANSIT→DELIVERED |
| `ReturnStatus` | REQUESTED→APPROVED, REQUESTED→REJECTED, APPROVED→RECEIVED |
| `RefundStatus` | PENDING→COMPLETED, PENDING→FAILED |

---

## 5. Verificación: cero `String` abiertos en campos acotados

| Campo | Antes | Ahora |
|---|---|---|
| Rol del usuario | `UserRole` (3 valores) | `UserRole` (5 valores) |
| Estado del usuario | No existía | `UserStatus` |
| Aprobación del vendedor | `boolean approved` | `SellerStatus` |
| Estado del producto | `boolean active` | `ProductStatus` |
| Tipo de producto | No existía | `ProductType` |
| Estado de la bodega | `boolean active` | `WarehouseStatus` |
| Tipo de movimiento | No existía | `MovementType` |
| Identificación del usuario | No existía | `IdentificationNumber` |

Los únicos `String` que quedan son textos libres sin dominio cerrado: nombres, descripciones, nombre comercial, motivo de devolución, número de factura y número de guía.
