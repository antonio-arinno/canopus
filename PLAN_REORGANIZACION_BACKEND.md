# Plan de reorganización del backend

Estado actualizado: 2026-09-05.

| Orden | Tarea | Estado | Criterio de salida |
|---:|---|---|---|
| 1 | Añadir CI para ejecutar backend y frontend automáticamente | Hecho | GitHub Actions ejecuta tests Maven y build Angular en cada push o pull request a `master`. |
| 2 | Definir la arquitectura objetivo y el grafo de dependencias | Hecho | [ARQUITECTURA_OBJETIVO_BACKEND.md](ARQUITECTURA_OBJETIVO_BACKEND.md) documenta capacidades, paquetes, reglas, grafo actual, ciclo `user`/`technology` y candidatos Maven acíclicos. |
| 3 | Reorganizar por paquetes dentro de los módulos actuales | Parcialmente hecho | `organization.company` está migrado; quedan la asociación `UserTechnology` y las capacidades `user`, `technology`, `product`, `project` e `imputation`. |
| 4 | Validar arranque, tests y JAR | Pendiente | El backend arranca, pasan todos los tests, el frontend compila y el JAR ejecutable contiene todos los componentes. |
| 5 | Decidir qué dominios merecen módulos Maven separados | Pendiente | La decisión se basa en dependencias reales y evita ciclos entre módulos. |
| 6 | Retomar paginación y N+1 en la estructura nueva | Pendiente | Paginación y proyecciones agregadas se completan sobre la arquitectura reorganizada. |

## Subtareas del paso 3

| Orden | Subtarea | Estado | Dependencia | Criterio de salida |
|---:|---|---|---|---|
| 3.1 | Reorganizar `company` | Hecho | Paso 2 | `Company`, `CompanyRepository`, `CompanyService` y `CompanyServiceImpl` usan `organization.company.*`; pasan tests limpios, integración multiempresa, build frontend y empaquetado del JAR. |
| 3.2 | Introducir `UserTechnology` | Pendiente | 3.1 | La tabla `users_technologies` se representa mediante una asociación explícita, con restricciones por usuario, tecnología y compañía. |
| 3.3 | Eliminar `User.technologies` | Pendiente | 3.2 | Perfil, alta de usuario y búsquedas por tecnología usan la asociación; `User` deja de importar `Technology`. |
| 3.4 | Reorganizar `user` y `technology` | Pendiente | 3.3 | Las clases usan `organization.user.*` y `organization.technology.*`; el ciclo Java queda roto y pasan pruebas de perfil, seguridad y aislamiento. |
| 3.5 | Reorganizar `product`, `project` e `imputation` | Pendiente | 3.4 | Cada capacidad usa sus paquetes `catalog.product.*`, `delivery.project.*` y `time.imputation.*`, manteniendo DTOs, métricas y contratos HTTP. |

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
