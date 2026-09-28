# Servicios de Usuarios

## Introducción

Gestionan el registro de compradores, vendedores y personal interno, la aprobación y suspensión de vendedores, y el bloqueo y reactivación de cuentas.

Los servicios de este subdominio viven en `application.domain.services.user`. Son clases Java puras marcadas con `@DomainService` (anotación propia del dominio). No importan Spring: `infrastructure/config/ApplicationConfig` las registra como beans.

Cada servicio implementa **un solo caso de uso** y expone un método `execute(...)` (o métodos de consulta con nombre explícito). Reciben el **usuario ejecutor** (`User`) ya cargado. La carga del usuario desde su `UserId` la hacen los casos de uso por rol (`adapters/useCases`).

---

## Contexto del modelo de dominio

```text
User (id, identification, fullName, email, phone, role, status, passwordHash, createdAt)
 ├── Buyer  (defaultShippingAddress : Address)
 └── Seller (businessName, sellerStatus : SellerStatus)
```

Reglas del enunciado cubiertas: **R1** (correo e identificación únicos), **R2** (un único rol por usuario), **R3** (usuario bloqueado no opera), **R4** (solo el administrador registra vendedores).

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

## 1. ValidateUserUniquenessService

### Descripción

Regla R1: el correo y la identificación no pueden repetirse en la plataforma.

### Firma

```java
void execute(Email email, IdentificationNumber identification);
```

### Autorización

No aplica.

### Dependencias

`UserRepository`

### Validaciones y reglas

1. `UserRepository.existsByEmail(email)` debe ser falso.
2. `UserRepository.existsByIdentification(identification)` debe ser falso.

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| Correo o identificación repetidos | `UserAlreadyExistsException` |

---

## 2. ConsultUserService

### Descripción

Consulta de usuarios.

### Firma

```java
User findById(UserId id);
User findById(User actor, UserId id);
List<User> findAll(User actor);
```

### Autorización

`findById(id)` es de uso interno. `findById(actor, id)` y `findAll(actor)` requieren supervisión (ADMIN o SUPERVISOR).

### Dependencias

`UserRepository`, `AuthorizeSupervisionOperationService`

### Auditoría

No registra auditoría (operación de consulta).

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |

---

## 3. RegisterBuyerService

### Descripción

Registro público de un comprador con su dirección de envío por defecto.

### Firma

```java
User execute(IdentificationNumber identification, String fullName, Email email,
             PhoneNumber phone, String passwordHash, Address defaultShippingAddress);
```

### Autorización

Público (no hay usuario ejecutor).

### Dependencias

`UserRepository`, `ValidateUserUniquenessService`, `RegisterAuditEventService`

### Validaciones y reglas

1. Unicidad de correo e identificación (R1).

### Comportamiento de dominio

`Buyer.create(...)`: rol `BUYER`, estado `ACTIVE`.

### Persistencia

`UserRepository.save(buyer)`.

### Auditoría

`USER_REGISTERED` con `role`.

### Errores

| Situación | Excepción |
|---|---|
| Correo o identificación repetidos | `UserAlreadyExistsException` |

---

## 4. RegisterSellerService

### Descripción

Regla R4: el Administrador registra un vendedor, que queda pendiente de aprobación.

### Firma

```java
User execute(User admin, IdentificationNumber identification, String fullName,
             Email email, PhoneNumber phone, String passwordHash, String businessName);
```

### Autorización

`AuthorizeAdminOperationService`.

### Dependencias

`UserRepository`, `AuthorizeAdminOperationService`, `ValidateUserUniquenessService`, `RegisterAuditEventService`

### Validaciones y reglas

1. Unicidad (R1).

### Comportamiento de dominio

`Seller.create(...)`: `sellerStatus = PENDING_APPROVAL`.

### Persistencia

`UserRepository.save(seller)`.

### Auditoría

`USER_REGISTERED` con `role = SELLER`.

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Correo o identificación repetidos | `UserAlreadyExistsException` |

---

## 5. RegisterStaffUserService

### Descripción

El Administrador registra personal interno: Operador Logístico, Supervisor u otro Administrador.

### Firma

```java
User execute(User admin, IdentificationNumber identification, String fullName,
             Email email, PhoneNumber phone, UserRole role, String passwordHash);
```

### Autorización

`AuthorizeAdminOperationService`.

### Dependencias

`UserRepository`, `AuthorizeAdminOperationService`, `ValidateUserUniquenessService`, `RegisterAuditEventService`

### Validaciones y reglas

1. Unicidad (R1).
2. `User.createStaff` rechaza los roles `BUYER` y `SELLER`, que tienen su propio registro.

### Persistencia

`UserRepository.save(staff)`.

### Auditoría

`USER_REGISTERED` con el rol.

### Errores

| Situación | Excepción |
|---|---|
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Rol no permitido para personal | `IllegalArgumentException` |

---

## 6. ApproveSellerService

### Descripción

Aprueba un vendedor para que pueda publicar productos.

### Firma

```java
Seller execute(User admin, UserId sellerId);
```

### Autorización

`AuthorizeAdminOperationService`.

### Dependencias

`UserRepository`, `ConsultUserService`, `AuthorizeAdminOperationService`, `RegisterAuditEventService`

### Validaciones y reglas

1. El usuario debe ser un `Seller`.

### Comportamiento de dominio

`Seller.approve()`: `PENDING_APPROVAL | SUSPENDED → APPROVED`.

### Persistencia

`UserRepository.save(seller)`.

### Auditoría

`SELLER_APPROVED` con estado anterior y nuevo.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| El usuario no es vendedor | `IllegalArgumentException` |
| Ya estaba aprobado | `InvalidStatusTransitionException` |

---

## 7. SuspendSellerService

### Descripción

Suspende un vendedor aprobado. Mientras esté suspendido no puede operar sobre productos.

### Firma

```java
Seller execute(User admin, UserId sellerId);
```

### Autorización

`AuthorizeAdminOperationService`.

### Dependencias

`UserRepository`, `ConsultUserService`, `AuthorizeAdminOperationService`, `RegisterAuditEventService`

### Comportamiento de dominio

`Seller.suspend()`: `APPROVED → SUSPENDED`.

### Persistencia

`UserRepository.save(seller)`.

### Auditoría

`SELLER_SUSPENDED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| El vendedor no está aprobado | `InvalidStatusTransitionException` |

---

## 8. BlockUserService

### Descripción

Regla R3: bloquea una cuenta. Un usuario bloqueado no puede ejecutar ninguna operación.

### Firma

```java
User execute(User actor, UserId userId);
```

### Autorización

`AuthorizeSupervisionOperationService` (ADMIN o SUPERVISOR).

### Dependencias

`UserRepository`, `ConsultUserService`, `AuthorizeSupervisionOperationService`, `RegisterAuditEventService`

### Validaciones y reglas

1. Nadie puede cambiar su propio estado.

### Comportamiento de dominio

`User.block()`: `ACTIVE → BLOCKED`.

### Persistencia

`UserRepository.save(user)`.

### Auditoría

`USER_BLOCKED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Ya estaba bloqueado | `InvalidStatusTransitionException` |

---

## 9. ActivateUserService

### Descripción

Reactiva una cuenta bloqueada.

### Firma

```java
User execute(User actor, UserId userId);
```

### Autorización

`AuthorizeSupervisionOperationService`.

### Dependencias

`UserRepository`, `ConsultUserService`, `AuthorizeSupervisionOperationService`, `RegisterAuditEventService`

### Validaciones y reglas

1. Nadie puede cambiar su propio estado.

### Comportamiento de dominio

`User.activate()`: `BLOCKED → ACTIVE`.

### Persistencia

`UserRepository.save(user)`.

### Auditoría

`USER_ACTIVATED`.

### Errores

| Situación | Excepción |
|---|---|
| El recurso no existe | `ResourceNotFoundException` |
| Usuario bloqueado o sin el rol requerido | `UnauthorizedOperationException` |
| Ya estaba activo | `InvalidStatusTransitionException` |

---
