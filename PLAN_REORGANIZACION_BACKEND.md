# Plan de reorganización del backend

Estado actualizado: 2026-10-03.

| Orden | Tarea | Estado | Criterio de salida |
|---:|---|---|---|
| 1 | Añadir CI para ejecutar backend y frontend automáticamente | Hecho | GitHub Actions ejecuta tests Maven y build Angular en cada push o pull request a `master`. |
| 2 | Definir la arquitectura objetivo y el grafo de dependencias | Hecho | [ARQUITECTURA_OBJETIVO_BACKEND.md](ARQUITECTURA_OBJETIVO_BACKEND.md) documenta capacidades, paquetes, reglas, grafo actual, ciclo `user`/`technology` y candidatos Maven acíclicos. |
| 3 | Reorganizar por paquetes dentro de los módulos actuales | Hecho | Completadas las subtareas 3.1 a 3.5: capacidades de organización, producto, proyecto e imputación reorganizadas, conservando los módulos Maven. |
| 4 | Validar arranque, tests y JAR | Hecho | `clean verify` pasa (22 tests, sin fallos), Angular compila, el JAR ejecutable contiene los módulos reorganizados y el arranque local con perfil `dev` responde HTTP 200 en `/actuator/health`. |
| 5 | Decidir qué dominios merecen módulos Maven separados | Pendiente | La decisión se basa en dependencias reales y evita ciclos entre módulos. |
| 6 | Retomar paginación y N+1 en la estructura nueva | Pendiente | Paginación y proyecciones agregadas se completan sobre la arquitectura reorganizada. |

## Subtareas del paso 3

| Orden | Subtarea | Estado | Dependencia | Criterio de salida |
|---:|---|---|---|---|
| 3.1 | Reorganizar `company` | Hecho | Paso 2 | `Company`, `CompanyRepository`, `CompanyService` y `CompanyServiceImpl` usan `organization.company.*`; pasan tests limpios, integración multiempresa, build frontend y empaquetado del JAR. |
| 3.2 | Introducir `UserTechnology` | Hecho | 3.1 | `users_technologies` se representa mediante una entidad explícita; la migración mantiene unicidad usuario-tecnología y aplica claves foráneas compuestas por compañía. Los tests backend, integración HTTP y build frontend pasan. |
| 3.3 | Eliminar `User.technologies` | Hecho | 3.2 | Perfil, alta de usuario y búsquedas por tecnología usan `UserTechnology`; las escrituras resuelven IDs dentro de la compañía, `User` ya no importa `Technology` y pasan pruebas de servicio, aislamiento HTTP y build frontend. |
| 3.4 | Reorganizar `user` y `technology` | Hecho | 3.3 | Entidades, contratos, repositorios, servicios, controllers y mappers usan `organization.user.*` y `organization.technology.*`; `User` no navega a `Technology`/`UserTechnology`, el ciclo Java queda roto y pasan pruebas de perfil, seguridad, aislamiento, integración HTTP y build frontend. |
| 3.5 | Reorganizar `product`, `project` e `imputation` | Hecho | 3.4 | Entidades, DTOs, repositorios, servicios, controllers y mappers usan `catalog.product.*`, `delivery.project.*` y `time.imputation.*`; pasan integración HTTP, pruebas de métricas/aislamiento, `clean verify` y build Angular. |

### Regla del paso 3

Cada subtarea debe terminar con tests backend, integración HTTP y build frontend correctos antes de comenzar la siguiente. Durante todo el paso 3 se conservan los módulos Maven actuales; la extracción de módulos pertenece al paso 5.

## Orden de ejecución

1. Añadir CI para ejecutar backend y frontend automáticamente.
2. Definir la arquitectura objetivo y el grafo de dependencias.
3. Reorganizar por paquetes dentro de los módulos actuales.
4. Validar arranque, tests y JAR.
5. Sólo después decidir qué dominios merecen módulos Maven separados.
6. Retomar paginación y N+1 en la estructura nueva.

## Arquitectura aprobada para la fase de paquetes

La fuente de verdad para los pasos 3 a 5 es [ARQUITECTURA_OBJETIVO_BACKEND.md](ARQUITECTURA_OBJETIVO_BACKEND.md). La primera reorganización conserva los módulos Maven actuales y agrupa paquetes por capacidad; no se extraen todavía módulos Maven por dominio.
