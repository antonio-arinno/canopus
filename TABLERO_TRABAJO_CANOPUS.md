# Tablero de trabajo de Canopus

Estado actualizado: 2026-08-30.

## Prioridad 1: Seguridad

| Tarea | Estado actualizado | Evidencia |
|---|---|---|
| Centralizar el usuario y la compañía actual | Hecho | `CurrentUserContext` obtiene la identidad desde `SecurityContext`; `JwtServiceImpl` delega en él. |
| Revisar repositorios para garantizar `company_id` | Hecho para las rutas revisadas | Productos, proyectos, tecnologías, usuarios y agregados de imputaciones relevantes filtran por compañía. |
| Añadir tests de aislamiento entre empresas | Hecho en parte | Pruebas H2 cubren productos por tecnología, usuarios por tecnología, proyectos por producto, agregados de imputación y listados por responsable. |
| Externalizar el secreto JWT | Hecho | Se usa `APP_JWT_SECRET`; no se genera un secreto nuevo al arrancar. |
| Eliminar la interpretación manual del token | Hecho | La identidad se obtiene desde el contexto que rellena `JwtValidationFilter`. |

## Prioridad 2: Contratos API

| Tarea | Estado actualizado | Evidencia |
|---|---|---|
| Crear DTOs para proyectos y tecnologías | Hecho | Existen `ProjectRequest` y `TechnologyRequest`. |
| No aceptar entidades JPA directamente | Hecho para proyectos y tecnologías | Sus `POST` y `PUT` reciben DTOs planos; aún queda aplicar el criterio a imputaciones. |
| Centralizar mapeadores | Hecho | Nuevo paquete `controllers.mapper` (`ProductMapper`, `ProjectMapper`, `TechnologyMapper`, `UserMapper`, `ImputationMapper`) concentra toda la conversión entidad↔respuesta; los controladores ya no construyen DTOs de respuesta directamente. |
| Normalizar formato de errores | Hecho | `ErrorResponseFactory` + `ErrorMessage` (`timestamp`, `status`, `error`, `message`, `fieldErrors`) unifican `ResponseStatusException`, `CustomException`, las excepciones de integridad, la validación automática de `@Valid` y las respuestas manuales de `UserController`/`AuthController`. |
| Añadir validaciones de relaciones y pertenencia a empresa | Hecho para proyectos y tecnologías | Producto, responsable y colaboradores se resuelven por ID dentro de la compañía autenticada. |

## Prioridad 3: Operación

| Tarea | Estado actualizado | Evidencia |
|---|---|---|
| Separar configuración por perfiles | Hecho | `dev` usa MySQL y `test` usa H2. |
| Eliminar credenciales del código | Hecho | Credenciales MySQL y secreto JWT retirados de configuración y guías; se cargan mediante `DB_USERNAME`, `DB_PASSWORD` y `APP_JWT_SECRET`. |
| Añadir Flyway o Liquibase | Hecho | Añadido Flyway (`flyway-core`, `flyway-mysql`) con script base `V1__initial_schema.sql` y `ddl-auto=validate`. |
| Preparar Docker y configuración de producción | Parcialmente hecho | Hay despliegue real con `systemd` y Nginx; falta una definición reproducible con Docker si se mantiene como objetivo. |
| Preparar configuración segura de producción | Pendiente | MySQL sin acceso público, usuario con permisos mínimos sobre `db_canopus` y HTTPS terminado en Nginx. |
| Verificar health checks y logs estructurados | Hecho | `/actuator/health` es público sin detalles sensibles (`show-details=never`); el resto de `/actuator/**` exige `ROLE_ADMIN`; sólo se exponen `health`, `info` y `metrics`; logs estructurados (ECS) escritos en `logs/canopus.log`. |
| Zona de monitorización sólo para administradores | Pendiente | Backend: `AdminMetricsController` que agrega métricas de Actuator (memoria, uptime, conexiones DB) en un DTO propio. Frontend: `adminGuard` + ruta `/pvt/admin/monitoring` visible sólo si `roles.includes('ROLE_ADMIN')`. |

## Prioridad 4: Calidad y rendimiento

| Tarea | Estado actualizado | Evidencia |
|---|---|---|
| Crear pruebas de integración | Hecho | `AuthenticationAndTenantIsolationIntegrationTest` valida por HTTP con H2: registro de dos empresas, login/JWT, permisos de administrador, alta de usuario, creación de tecnología/producto/proyecto y aislamiento entre empresas. |
| Añadir paginación | Parcialmente hecho | Usuarios usa `/user/page?page={page}&size={size}` con límites de 1-100, metadatos estables en `PageResponse` y `MatPaginator`. Pendiente extender el contrato a producto, proyecto y tecnología, cuyos listados agrupan resultados antes de renderizarlos. |
| Revisar consultas N+1 | Parcialmente hecho | La lista paginada de usuarios usa `UserListItem`: una proyección agregada calcula `countProducts` y `time` dentro de la consulta paginada, sin `countProjects` no mostrado. Pendiente revisar productos, proyectos y tecnologías. |
| Sustituir contadores individuales por proyecciones agregadas | Pendiente | Siguen existiendo cálculos y consultas separadas para cada producto, proyecto o tecnología. |
| Añadir CI para build y tests | Pendiente | No hay pipeline automático de compilación y pruebas. |

## Prioridad 5: Organización del backend por dominio

| Tarea | Estado actualizado | Evidencia |
|---|---|---|
| Reorganizar módulos Maven por dominio (no por capa técnica) | Pendiente | Estructura actual: `main`, `controllers`, `entities`, `repositories`, `services`, `auth`, `util`, `error` (organización técnica por capa). Objetivo: módulos/paquetes por dominio (ej. `user`, `product`, `project`, `technology`, `imputation`), cada uno con su controller, service, repository y entity. |

## Validación más reciente

- Backend: `./mvnw.cmd -q -pl main -am test -D"failIfNoTests=false"` desde `canopus-backend`: correcto, `EXIT:0`.
- Frontend: `npm run build` desde `canopus-frontend`: correcto.
