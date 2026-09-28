# NexusMarket

Marketplace con inventario distribuido por bodegas, construido con **Java 17 + Spring Boot 4**, **Arquitectura Hexagonal** y **DDD**.

## Documentación (SDD)

La especificación completa está en la carpeta `SDD/`:

- `SDD/domain/Domain Model.md`: entidades, agregados, reglas de negocio y diagramas.
- `SDD/domain/Domain Value Objects.md`: Value Objects y enums.
- `SDD/domain/services/*-services.md`: un documento por subdominio con cada servicio de dominio (caso de uso): firma, autorización, reglas, persistencia y auditoría.
- `SDD/Software Architecture/Software Architecture.md`: capas, puertos por rol, casos de uso, adaptadores y API REST.

## Cómo ejecutar

1. Levanta las bases de datos:

   ```bash
   docker compose up -d
   ```

2. Compila y prueba:

   ```bash
   cd nexusm
   ./mvnw clean test
   ```

   En Windows usa `mvnw.cmd clean test`.

3. Ejecuta la aplicación:

   ```bash
   ./mvnw spring-boot:run
   ```

Al arrancar se crea un Administrador inicial, y su id aparece en la consola (`Administrador inicial: id=...`). Sus datos están en `application.properties`, bajo las propiedades `nexusmarket.admin.*`.

Toda ruta fuera de `/api/public/**` requiere la cabecera `X-User-Id` con el id de quien ejecuta la operación. Hay una ruta base por rol: `/api/buyer`, `/api/seller`, `/api/logistics`, `/api/admin` y `/api/supervisor`. En `PASO_A_PASO.md` hay un recorrido completo para Postman.
