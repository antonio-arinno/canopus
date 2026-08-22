# Canopus

Aplicación web multiempresa para la gestión de proyectos de software: tiempos, evoluciones, productos, tecnologías y equipos.

## Arquitectura

- **Frontend**: Angular 19, standalone components, Angular Material. Carpeta `canopus-frontend`.
- **Backend**: Spring Boot 3.5.16, Java 21, Maven multi-módulo. Carpeta `canopus-backend`.
- **Base de datos**: MySQL, base de datos `db_canopus`.
- **Autenticación**: JWT (login emite token; `JwtValidationFilter` reconstruye la sesión en cada petición).

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
- **Pendiente**: hoy no existe ningún endpoint ni pantalla para dar de alta una empresa nueva. Las compañías se crean manualmente en la base de datos. `POST /user` crea usuarios dentro de la compañía del token de quien los crea, pero no hay forma de crear la primera empresa ni su primer usuario administrador de forma autónoma.

## Registro (`feature/register`) — inacabado

- Ruta: `/auth/register`.
- Componente: `RegisterComponent` (`canopus-frontend/src/app/features/auth/auth/register`).
- Estado actual: es una vista estática de marcador de posición. Solo muestra un mensaje "Próximamente..." y un enlace para volver al login. No tiene formulario, no llama a ningún servicio.
- Backend: no existe `CompanyController`, `RegisterRequest` ni ningún endpoint `/register` o `/company`.

### Trabajo pendiente para completar el alta de empresa

1. Backend:
   - Entidad/DTO de alta: nombre de empresa, datos del primer usuario administrador.
   - Endpoint público (sin JWT) `POST /register` o `POST /company` que cree la `Company` y su primer `User` con `ROLE_ADMIN`.
   - Validar que el nombre de empresa o el username no existan ya.
   - Igual que en el alta de usuario, cifrar la contraseña inicial con `PasswordEncoder`.
2. Frontend:
   - Formulario reactivo en `RegisterComponent` (nombre de empresa, nombre/apellidos, usuario, email, contraseña).
   - Servicio HTTP para consumir el nuevo endpoint público.
   - Redirección a login tras el alta correcta.
3. Seguridad:
   - Añadir la regla de `SpringSecurityConfig` para permitir `POST /register` sin autenticación, igual que ya ocurre con `/login`.

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
