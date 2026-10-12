# Tablero de extracción de módulos Maven

Estado actualizado: 2026-10-04.

## Objetivo

Evolucionar desde los módulos técnicos actuales hacia módulos por capacidad, manteniendo un grafo acíclico y preservando contratos HTTP, esquema Flyway y comportamiento multiempresa. Cada fase se valida antes de abrir la siguiente.

## Fases

| Fase | Alcance | Estado | Criterio de salida |
|---:|---|---|---|
| 0 | Baseline y decisión de límites | Hecho | 23 tests backend sin fallos, build Angular correcto, JAR ejecutable probado; candidatos y grafo documentados en `ARQUITECTURA_OBJETIVO_BACKEND.md`. |
| 1 | Límites de dependencias Maven | Hecho | Las versiones JJWT están centralizadas en el parent; las dependencias externas requeridas se declaran por módulo; `entities` ya no recibía Web/Security/MySQL/Flyway. Los módulos técnicos vacíos y sus referencias se retiraron en fase 5; `clean verify` pasa. |
| 2 | Aislar reporting y métricas transversales | Hecho | Mappers de usuario/technology/product/project son conversiones puras; agregados de imputación están bajo `ReportingRepository`; se conservan respuestas HTTP y pasan integración multiempresa y `clean verify`. Paginación y agregaciones por lote completadas en fase 6. |
| 3 | Extraer módulos de capacidades | Hecho | `organization`, `catalog`, `delivery` y `time-tracking` son módulos Maven independientes en el grafo acíclico documentado; `clean verify` e integración HTTP pasan. |
| 4 | Extraer reporting, seguridad y ensamblado web | Hecho | `reporting`, `security` y `web-adapters` son artefactos separados; el módulo `main` actúa como bootstrap sin renombrar el JAR; `clean verify`, Angular, JAR y health pasan. |
| 5 | Retirar módulos técnicos vacíos y consolidar | Hecho | Contratos y errores compartidos trasladados; módulos técnicos vacíos (`entities`, `repositories`, `services`, `auth`, `controllers`, `util`, `error`) y referencias POM retirados. `clean verify` (27 tests), Angular, JAR consolidado y health `UP` pasan. |
| 6 | Optimizar paginación y consultas N+1 | Hecho | Listas de producto/proyecto/tecnología usan `/page` con `PageResponse`, paginación Material y `EntityGraph`; métricas de listas se cargan en lote y usuarios agrupa métricas/tecnologías. Se conservan rutas array. `clean verify` (26 tests activos), Angular e integración multiempresa pasan. |

### Fase 3: progreso por módulo

| Orden | Módulo | Estado | Gate |
|---:|---|---|---|
| 3.1 | `organization` | Hecho | `clean verify` pasa con 23 tests y sin cambiar contratos HTTP; el JAR arranca y health responde 200. |
| 3.2 | `catalog` (`technology` + `product`) | Hecho | Módulo depende de `organization` y `shared`; controllers/mappers permanecen en web; `clean verify` e integración pasan sin ciclos. |
| 3.3 | `delivery` | Hecho | Proyecto y repositorio/servicio extraídos; `clean verify`, build frontend y grafo `delivery -> catalog + organization` pasan. |
| 3.4 | `time-tracking` | Hecho | Imputation domain/contract/repository/application extraídos; depende de organization/catalog/delivery; `clean verify` e integración multiempresa pasan. |

### Fase 4: progreso por módulo

| Orden | Módulo | Estado | Gate |
|---:|---|---|---|
| 4.1 | `reporting` | Hecho | Contratos, repositorio y ReportingService extraídos; integración HTTP y aislamiento pasan, web lo consume sin ciclo. |
| 4.2 | `security` | Hecho | JWT, filtros y servicios de identidad extraídos; depende de organization y sus librerías. |
| 4.3 | `application/web-adapters` | Hecho | Controllers, mappers y registro reunidos como capa superior que depende de capacidades/reporting/security. |
| 4.4 | `bootstrap` | Hecho | El módulo ejecutable `main` ensambla los módulos nuevos; JAR arranca con MySQL/Flyway y health responde 200. |

## Decisiones

- `organization`: `company`, `user`, `role` y el puerto de asignaciones de tecnología.
- `catalog`: `technology` y `product`; no crear módulo `technology` separado por ahora.
- `delivery`: proyectos y colaboradores.
- `time-tracking`: imputaciones; depende de `delivery`, `catalog` y `organization`.
- `reporting`: consultas/read models que combinan capacidades; módulo ya extraído tras separar esas lecturas de mappers y repositorios de escritura.
- `security`: JWT, filtros y usuario actual; depende de `organization`.
- `web-adapters`: controllers, mappers HTTP y registro como capa superior de composición.
- `main`: conserva `Main`, perfiles, configuración, Flyway y el artefacto ejecutable.
- `shared`: módulo pequeño y técnico para contratos y errores reutilizados; extraído.
- Módulos técnicos vacíos: retirados del reactor en fase 5.

## Bloqueos conocidos

- Resueltos en fases 1/2: dependencias globales en el parent y llamadas de métricas desde mappers/repositorio de imputación.
- Resuelto en fase 6: reporting consulta métricas agrupadas para listas; las asociaciones usadas por mappers se precargan y los listados principales están paginados.
- Resuelto en fase 5: los POMs activos forman el reactor por capacidades y módulos de soporte; no quedan dependencias a los módulos técnicos retirados.

## Registro de ejecución

| Fecha | Fase | Resultado | Evidencia / observaciones |
|---|---|---|---|
| 2026-10-03 | 0 | Hecho | `mvn clean verify`: 23 tests; `npm run build`; JAR arrancó en perfil dev y `/actuator/health` devolvió HTTP 200. `entities` dependency tree confirma dependencias heredadas no deseadas. |
| 2026-10-03 | 1 | Hecho | `mvn clean verify`: 23 tests, 0 fallos. El dependency tree de `entities` quedó limitado a JPA, validation, Jackson annotations y Spring Data Commons. Se eliminaron `org.json` sin usos Java y las dependencias globales heredadas. |
| 2026-10-03 | 2 | Hecho | Read models y ReportingService centralizan métricas; mappers no llaman servicios de otras capacidades; agregados transversales de imputación se movieron a ReportingRepository. `clean verify`: 23 tests; integración multiempresa y build Angular pasan. |
| 2026-10-03 | 3.1 | Hecho | Nuevo artefacto organization contiene company/user/role, contratos, repositorios y aplicaciones; `clean verify`, Angular, JAR y health local pasan. |
| 2026-10-03 | 3.2 | Hecho | Nuevo artefacto catalog contiene technology/product, repositorios y aplicaciones; `clean verify` (23 tests), build Angular y arranque de JAR/health pasan. |
| 2026-10-03 | 3.3 | Hecho | Nuevo artefacto delivery contiene project, contratos, repositorio y aplicación; `clean verify` (23 tests) y build Angular pasan. |
| 2026-10-03 | 3.4 | Hecho | Nuevo artefacto time-tracking contiene imputaciones, contratos, repositorio y aplicación; `clean verify`: 23 tests, 0 fallos. |
| 2026-10-03 | 4 | Hecho | Security y web-adapters extraídos; main compone módulos de capacidades/reporting. `clean verify` (23 tests), Angular, JAR y health local pasan. |
| 2026-10-03 | 4.1 | Hecho | Reporting extraído como módulo Maven y consumido por web; test de integración multiempresa pasa con métricas centralizadas. |
| 2026-10-03 | 3.1-3.2 | Hecho | Añadidos módulos Maven `organization` y `catalog`; dependencias por capacidad y sin ciclo; `clean verify` pasa. |
| 2026-10-03 | 5 | Hecho | Retirados los POMs técnicos vacíos y sus referencias; `UserServiceTest` queda sólo en `organization`. Auditoría de POMs limpia; `mvnw.cmd -q clean verify` (27 tests, 0 fallos/errores), `npm run build`, inspección del JAR y arranque con MySQL/Flyway completados; `/actuator/health` devuelve `UP`. |
| 2026-10-04 | 6 | Hecho | Añadidos `/product/page`, `/project/page` y `/technology/page`; Angular consume `PageResponse` con filtros y paginadores. `ReportingRepository`/`ReportingService` agregan métricas por IDs en consultas de lote; `EntityGraph` y fetch joins cargan asociaciones de lectura. El GET array de usuarios también obtiene métricas y tecnologías en lote; contratos array existentes se mantienen. Pruebas HTTP verifican métricas, paginación y aislamiento. `clean verify`: 26 tests de módulos activos, 0 fallos/errores; `npm run build` y `git diff --check` pasan. |
