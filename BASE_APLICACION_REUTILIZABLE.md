# Base de aplicación reutilizable

Fecha de revisión: 2026-10-09. Estado: referencia para diseñar el scaffolder; todavía no implementado.

## Propósito y alcance

Documentar los aprendizajes de Canopus para generar aplicaciones independientes con compañía, usuarios, roles y autenticación. Es un starter o plantilla de aplicación, no un framework propio ni una copia completa de Canopus.

"Sin lógica de negocio" significa sin catálogo, proyectos ni imputaciones. La base sí necesita reglas de identidad: pertenencia a una compañía, permisos, unicidad, registro y cambio de contraseña.

Este documento distingue funcionalidades existentes de requisitos de la futura plantilla. No garantiza por sí solo que una aplicación generada las implemente.

## Stack y arquitectura

| Área | Base comprobada en Canopus | Criterio para la plantilla |
|---|---|---|
| Backend | Spring Boot 3.5.16, Java 21, Maven Wrapper | Versiones centralizadas; confirmar soporte antes de generar una aplicación nueva. |
| Arquitectura | Monolito modular por capacidades, inspirado en DDD | Separar dominio, aplicación, contratos, persistencia y API; no presentar entidades JPA y servicios CRUD como DDD estricto. |
| Persistencia | Spring Data JPA, MySQL, Flyway | Migraciones propias de la plantilla; Hibernate valida el esquema, no lo modifica automáticamente. |
| Frontend | Angular 19, TypeScript 5.6, RxJS 7.8, Angular Material | Componentes standalone, rutas por features y carga lazy; Signals para estado de UI, RxJS para HTTP. |
| Validación | H2 para pruebas backend, integración HTTP con MockMvc | Validar también compatibilidad con MySQL; H2 no garantiza equivalencia de dialecto. |
| Automatización | GitHub Actions, Java 21 y Node.js 22 | Tests backend y build frontend reproducibles con wrappers y lockfile. |

## Módulos mínimos

- `organization`: compañía, usuarios y roles; entidades, contratos, casos de uso y repositorios.
- `security`: Spring Security, JWT, filtros, PasswordEncoder e identidad/compañía actuales.
- `web-adapters`: registro, controladores HTTP y mappers puros.
- `shared`: errores y contratos técnicos reutilizados, incluido `PageResponse`; sin entidades ni reglas de dominio.
- `main`: ejecutable, configuración, perfiles, Flyway y composición.

Dependencias permitidas: security depende de organization; web-adapters depende de organization, security y shared; main ensambla la aplicación. Evitar ciclos y capacidades inferiores dependientes de controladores. Mantener starters cuando reduzcan mantenimiento; no descomponerlos sin una necesidad concreta.

No generar `catalog`, `delivery`, `time-tracking` ni `reporting` mientras no haya casos de uso que los justifiquen.

## Identidad y seguridad

### Funcionalidades que conservar

- Registro público de compañía y primer administrador, con validación y operación transaccional.
- Login JWT y autorización stateless mediante Spring Security.
- Roles `ROLE_USER` y `ROLE_ADMIN`; creación/borrado de usuarios restringidos a administrador.
- Perfil propio y cambio de contraseña con comprobación de contraseña actual; BCrypt mediante PasswordEncoder.
- Usuario y compañía actuales resueltos desde SecurityContext; nunca confiar en un companyId enviado por el cliente.
- Consultas y resoluciones por ID acotadas a la compañía autenticada; protección contra acceso a otra empresa.
- `401` sin autenticación válida y `403` con rol insuficiente; guards frontend complementarios, nunca sustitutos del backend.
- Secreto JWT externo y estable entre reinicios; no registrar tokens, passwords ni claves.

### Decisiones que revisar al generar

- Canopus asigna como contraseña inicial el username. NO copiarlo: definir contraseña segura de alta o un flujo de invitación/activación antes de generar ese caso de uso.
- Declarar si username/email serán únicos globalmente o por compañía; el login y las constraints deben seguir la misma decisión.
- Parametrizar CORS y URL del API; no copiar la IP de producción de Canopus.
- Documentar almacenamiento del token, expiración y logout. No afirmar que existe refresh-token, recuperación de contraseña, MFA o revocación si no se implementan.
- CSRF desactivado sólo bajo una estrategia explícita de tokens Bearer; revisar la protección si se introduce autenticación por cookies.

## Contratos y errores

- Request/Response DTOs en lugar de recibir o serializar entidades JPA como contrato público.
- IDs de relaciones validados dentro del tenant; validaciones Jakarta Bean Validation y reglas de aplicación complementarias.
- Mappers puros: sin consultas a base de datos ni llamadas de métricas ocultas.
- Modelo de error común: `timestamp`, `status`, `error`, `message`, `fieldErrors`.
- Handler centralizado para validación, recursos inexistentes, conflictos de unicidad e integridad; mensajes sin detalles internos ni secretos.
- Verificar también errores de filtros de seguridad: no todos pasan por el handler MVC.
- Mantener status y DTOs estables; documentar los contratos antes de añadir generadores de CRUD.

## Frontend reutilizable

- Separación `core` (auth/API/UI), `shared` (layouts/componentes) y `features` (auth/user).
- Formularios reactivos con validación, errores por campo y prevención de envíos repetidos.
- Signals privados y exposición readonly para carga/envío, errores y resultados; componentes con `inject` y listas con `track` estable.
- Material Snackbars para avisos de éxito/error, siguiendo `FormStateService`; mensajes de formulario y estado de petición coherentes.
- Interceptores funcionales para Bearer y errores: registrar la cadena una sola vez. Canopus tiene dos registros de `provideHttpClient` en app.config; no reproducir esa duplicación.
- Diferenciar 401 (sesión inválida, logout/login) de 403 (sin permisos, conservar sesión).
- Evitar snackbars duplicados entre interceptor y componente. Los errores de login deben quedarse en el flujo de autenticación.
- Estado de carga/error/vacío y navegación predecible; conservar diseño Material y layouts adaptables sin incorporar branding ni pantallas de negocio de Canopus.
- Evitar estado global de carga compartido que se cierre incorrectamente con peticiones concurrentes; definir propiedad de estado y cancelar respuestas obsoletas.
- Rutas de login, registro, lista de usuarios, alta, consulta, perfil y cambio de contraseña.

## Paginación y rendimiento

- `PageResponse`: content, totalElements, totalPages, size y number; límites server-side, orden estable y MatPaginator.
- La lista de usuarios de la plantilla NO incluye countProducts, countProjects, time ni tecnologías.
- Seleccionar sólo datos necesarios y precargar asociaciones utilizadas; evitar consultas por fila y fetch de colecciones con paginación que produzca paginación en memoria.
- No generar repositorios/reporting ni agregaciones de productos/proyectos como dependencia de organization.

## Configuración y operación

- Perfiles dev/test/prod explícitos, sin activar dev por defecto en producción.
- URL de BD, usuario, password y JWT por configuración externa; ejemplos sin secretos reales.
- Migraciones nuevas sólo para companies, users, roles y users_roles; NO copiar V1-V4 de Canopus y luego borrar tablas de negocio.
- Flyway y `ddl-auto=validate`; baseline sólo mediante decisión explícita para esquemas existentes.
- Health sin detalles sensibles, métricas restringidas y logs estructurados sin datos sensibles; SQL desactivado en producción.
- Documentar permisos separados de runtime/migración, HTTPS y backups como requisitos operativos. No afirmar que generar código configura EC2 automáticamente.

## Pruebas y gates de aceptación

1. Compilar el reactor limpio y ejecutar tests: registro, login válido/inválido, anónimos, JWT inválido, permisos USER/ADMIN y health público.
2. Probar aislamiento entre dos compañías, unicidad y rollback del registro; no basta llamar directamente al controlador con mocks.
3. Probar DTOs/errores, paginación estable y cambio de contraseña; sin datos ni contadores específicos de Canopus.
4. Build Angular y verificación de formularios, snackbars, sesión, guards, paginación y estados vacío/error en navegador desktop/mobile.
5. Arranque del JAR y validación de migraciones en una BD desechable MySQL, sin tocar producción.
6. Buscar referencias residuales a Canopus, productos, tecnologías, proyectos e imputaciones; comprobar que archivos generados no contienen secretos.

La CI actual ejecuta tests Maven y build Angular; las pruebas de navegador y validación MySQL de una plantilla son gates adicionales, no capacidades ya demostradas por esa CI.

## Parámetros del futuro scaffolder

- Nombre de aplicación, paquete Java, nombres de artefactos, carpeta destino y branding.
- Versiones soportadas y política de nombres/identidad global o por tenant.
- URL del API y orígenes CORS por entorno, sin pedir secretos en el chat.
- Estrategia de alta de usuarios y roles adicionales, si fueran necesarios.
- Generar fuera de Canopus, sin sobrescribir un proyecto existente sin autorización.
- Entregar instrucciones de arranque, configuración de entorno, contratos y resultados reales de validación.

## Referencias de implementación

- [Resumen de Canopus](APLICACION.md) y [arquitectura modular](ARQUITECTURA_OBJETIVO_BACKEND.md).
- [SecurityFilterChain](canopus-backend/security/src/main/java/com/arinno/canopus/security/SpringSecurityConfig.java).
- [Integración HTTP](canopus-backend/main/src/test/java/com/arinno/canopus/integration/AuthenticationAndTenantIsolationIntegrationTest.java).
- [Estado de formularios y snackbars](canopus-frontend/src/app/core/ui/form-state.service.ts).
- [Estado de peticiones](canopus-frontend/src/app/core/ui/request-state.service.ts) e [interceptor de errores](canopus-frontend/src/app/core/auth/interceptors/error.interceptor.ts).
- [Configuración Angular](canopus-frontend/src/app/app.config.ts) y [CI](.github/workflows/ci.yml).