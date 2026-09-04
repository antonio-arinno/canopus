# Canopus

Aplicación web multiempresa para la gestión de proyectos de software: tiempos, evoluciones, productos, tecnologías y equipos.

## Arquitectura

- **Frontend**: Angular 19, standalone components, Angular Material. Carpeta `canopus-frontend`.
- **Backend**: Spring Boot 3.5.16, Java 21, Maven multi-módulo. Carpeta `canopus-backend`.
- **Base de datos**: MySQL, base de datos `db_canopus`. Migraciones gestionadas con Flyway (`db/migration/V1__initial_schema.sql`), con `spring.jpa.hibernate.ddl-auto=validate` en entorno dev.
- **Autenticación**: JWT (login emite token; `JwtValidationFilter` reconstruye la sesión en cada petición).
- **Clave JWT**: se carga desde la variable de entorno `APP_JWT_SECRET` en Base64; debe mantenerse estable entre reinicios e instancias.

### Módulos Maven del backend

`main`, `controllers`, `entities`, `repositories`, `services`, `auth`, `util`, `error`.

### Controladores REST existentes

`UserController`, `ProductController`, `ProjectController`, `TechnologyController`, `ImputationController`, `TestController`.

### Features del frontend

`auth`, `user`, `product`, `project`, `technology`, `imputation`.

## Modelo multiempresa

- La entidad `Company` (`id`, `name`, `description`, `createAt`) representa cada empresa cliente.
- `User` tiene una relación `@ManyToOne` obligatoria con `Company`.
- Todas las consultas de usuario, producto, tecnología, etc. filtran por la compañía obtenida del JWT (`jwtService.getCompanyFromToken(auth)`), de modo que una empresa nunca ve datos de otra.
- `POST /register` crea una compañía nueva junto a su primer usuario administrador (`ROLE_ADMIN`), sin necesidad de autenticación previa.

## Seguridad — estado actual

- Las consultas de productos por tecnología, usuarios por tecnología, proyectos por producto y proyectos de contribuidores aplican el filtro de compañía.
- `/user/me` comprueba también la compañía del usuario autenticado.
- El secreto JWT ya no se genera al arrancar; el backend requiere `APP_JWT_SECRET`.
- El usuario y la compañía actuales se resuelven desde el `SecurityContext` mediante `CurrentUserContext`; `JwtServiceImpl` mantiene el contrato existente delegando en ese contexto.
- Los agregados de imputaciones por producto y proyecto exigen también la compañía autenticada.
- Las altas y ediciones de proyectos y tecnologías verifican que producto, responsable y colaboradores pertenecen a la compañía autenticada antes de persistirlos.
- `ProjectRequest` y `TechnologyRequest` separan los contratos HTTP de las entidades JPA: reciben atributos editables e IDs de relaciones, que el backend resuelve dentro de la compañía autenticada.

## Registro de empresa (`feature/register`) — completado

- Ruta: `/auth/register`.
- Componente: `RegisterComponent` (`canopus-frontend/src/app/features/auth/auth/register`), formulario reactivo con nombre de empresa, nombre, apellidos, email, usuario y contraseña.
- Backend: `AuthController` (`POST /register`, público) valida los datos, comprueba que el nombre de empresa y el usuario no existan, crea la `Company` y el `User` administrador (contraseña cifrada con `PasswordEncoder`).
- Piezas nuevas: `RegisterRequest` (entities), `CompanyRepository` (repositories), `CompanyService`/`CompanyServiceImpl` (services), `AuthController` (controllers), regla `permitAll()` para `POST /register` en `SpringSecurityConfig`.
- Tras un registro correcto, el frontend redirige a `/auth/login` para que el nuevo administrador inicie sesión.

## Módulo de usuarios (ABM y perfil) — estado actual

- **Alta, baja y consulta administrativa**: `POST /user` y `DELETE /user/{id}` requieren `ROLE_ADMIN`. El alta usa `UserRequest`, valida los datos y asigna como contraseña inicial el propio nombre de usuario, cifrada con BCrypt.
- **Consulta**: `GET /user`, `GET /user/{id}`, `GET /user/me` requieren solo estar autenticado. El detalle de un usuario ajeno se muestra en modo solo lectura en el frontend (sin `input`, con tecnologías como etiquetas).
- **Perfil propio**: `PUT /user/me` actualiza nombre, apellidos, email y tecnologías del usuario autenticado (identificado por el JWT, nunca por un `id` recibido del cliente).
- **Cambio de contraseña**: `PUT /user/me/password`, requiere la contraseña actual y cifra la nueva con BCrypt. En el frontend está en una tarjeta colapsable dentro de `/pvt/user/profile`.
- Rutas frontend relevantes: `/pvt/user` (listado), `/pvt/user/detail/:id` (consulta), `/pvt/user/detail` (alta), `/pvt/user/profile` (perfil propio).

## Comandos de validación

- Frontend: desde `canopus-frontend`, `npm run build`.
- Backend: desde `canopus-backend`, `.\mvnw.cmd -q -DskipTests compile`.

## Pendientes conocidos

- Completar el registro de empresa (`feature/register`), descrito arriba.
- `npm start` (frontend) y el arranque de `Main` (backend) han terminado con código de salida 1 en las últimas sesiones; los builds compilan bien, pero el arranque en caliente no se ha verificado todavía.
