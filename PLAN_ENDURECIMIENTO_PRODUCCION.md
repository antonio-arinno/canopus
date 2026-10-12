# Plan de endurecimiento de producción

Estado: Propuesto, sin cambios aplicados al servidor.  
Fecha: 2026-10-04.

## Diagnóstico observado

- `instalacion_server.txt` e `instalacion_sql_server.txt` indican abrir 3306 desde cualquier origen y configurar MySQL con `bind-address = 0.0.0.0`.
- El usuario descrito se limita a `db_canopus` y `localhost`, lo cual es mejor que permisos globales, pero incluye permisos DDL que no necesita el runtime normal.
- El servicio systemd ejecuta con `User=ubuntu`; el entorno está en `/etc/canopus/canopus.env`.
- `application.properties` activa `dev` por defecto. El archivo dev apunta a `canopus`, mientras los ejemplos de instalación y configuración apuntan a `db_canopus`.
- La aplicación usa `ddl-auto=validate`, Flyway está habilitado y `spring.jpa.show-sql=true` aparece en la configuración común.
- Los archivos del repositorio sólo contienen placeholders para `DB_PASSWORD`; no permiten verificar la contraseña real guardada en producción.

## Fases

| Fase | Tarea | Estado | Criterio de salida |
|---:|---|---|---|
| 0 | Inventario y respaldo previo | Pendiente | Confirmar dónde corre MySQL, qué base usa el JAR desplegado, cuentas/hosts/grants existentes y acceso administrativo. Crear y comprobar un backup recuperable antes de cambiar red o usuarios. |
| 1 | Cerrar exposición de MySQL | Pendiente | Si aplicación y MySQL están en la misma EC2, quitar toda regla entrante 3306 y fijar `bind-address = 127.0.0.1`. Si están separados, permitir 3306 únicamente desde el Security Group del backend y usar IP privada. Probar conexión local y confirmar que desde Internet no se alcanza el puerto. Para Workbench, usar túnel SSH. |
| 2 | Separar las cuentas de aplicación y migración | Pendiente | Crear una cuenta de runtime, por ejemplo `canopus_app`@`localhost`, limitada a `db_canopus` y operaciones `SELECT`, `INSERT`, `UPDATE`, `DELETE`. Crear otra cuenta para Flyway, limitada al mismo esquema y con DDL requerido por V1-V4. Configurar `SPRING_FLYWAY_USER` y `SPRING_FLYWAY_PASSWORD`; probar despliegue en entorno de prueba antes del cambio productivo. Verificar `SHOW GRANTS` y que ninguna cuenta tenga grants globales sobre `*.*`. |
| 3 | Verificar/rotar el secreto de base de datos | Pendiente | Revisar el valor únicamente en el gestor seguro del servidor, sin pegarlo en chat, repositorio, tickets ni comandos que lo dejen en historial. Si es corto, predecible, reutilizado o estuvo expuesto, generar una contraseña aleatoria larga (p. ej. 32 bytes aleatorios), actualizar la cuenta nueva y el archivo de entorno con permisos restrictivos, desplegar y verificar. Revocar la cuenta antigua sólo después de confirmar el servicio. |
| 4 | Fijar perfil y configuración de producción | Pendiente | Quitar `spring.profiles.active=dev` como perfil predeterminado del artefacto y definir `SPRING_PROFILES_ACTIVE=prod` en systemd. Elegir explícitamente el nombre real de la base y configurar `SPRING_DATASOURCE_URL`; mantener usuario/password fuera de los `.properties`. Desactivar `spring.jpa.show-sql` en producción y conservar `ddl-auto=validate`. Verificar que Flyway usa la cuenta de migración y JPA la cuenta de runtime. |
| 5 | Endurecer el proceso y proteger secretos | Pendiente | Cambiar el servicio a una cuenta Linux dedicada sin login interactivo en vez de `ubuntu`; limitar lectura del EnvironmentFile a root/systemd, proteger directorios y aplicar opciones systemd de aislamiento compatibles con logs y despliegue. Reiniciar y validar health, conexión MySQL y permisos. |
| 6 | Backups y recuperación | Pendiente | Cifrar volúmenes y snapshots con KMS, habilitar backups automáticos y retención, proteger credenciales de backup y ensayar una restauración en una instancia/base separada. Documentar RPO/RTO. |
| 7 | Perímetro HTTP/TLS relacionado | Pendiente | Terminar HTTPS en Nginx con certificado renovable. Migrar el API detrás de Nginx y limitar el puerto 8080 a loopback/Security Group de Nginx; no cerrar 8080 hasta que frontend/API estén probados por HTTPS. |
| 8 | Verificación y operación | Pendiente | Confirmar desde fuera que 3306 está cerrado, comprobar que el backend sólo conecta con su usuario de runtime, que Flyway puede migrar con la cuenta separada, revisar logs sin secretos y repetir la prueba de restauración según calendario. |

## Orden seguro de ejecución

1. Backup y confirmar topología/DB activa.
2. Cerrar 3306 y revertir `bind-address`; comprobar conectividad local antes de continuar.
3. Crear cuentas nuevas y probarlas sin revocar todavía la cuenta actual.
4. Configurar perfil `prod`, nombre correcto de base y secretos nuevos en systemd.
5. Desplegar en ventana controlada; comprobar health, login, lecturas/escrituras y migración Flyway.
6. Revocar la cuenta anterior y validar que no quedan usuarios con grants innecesarios.
7. Completar TLS, hardening del servicio y ejercicios de recuperación.

## Sobre la contraseña actual

No se puede determinar su fortaleza a partir de estos archivos: contienen `DEFINE_UNA_CONTRASENA_SEGURA` y `${DB_PASSWORD}`, no el valor real. La ubicación prevista para el valor desplegado es `/etc/canopus/canopus.env`. Si no puedes confirmar que es aleatoria, única y suficientemente larga, la opción segura es rotarla mediante la fase 3. No incluyas el valor real en este documento.

## Referencias

- [instalacion_server.txt](instalacion_server.txt)
- [instalacion_sql_server.txt](instalacion_sql_server.txt)
- [application.properties](canopus-backend/main/src/main/resources/application.properties)
- [application-dev.properties](canopus-backend/main/src/main/resources/application-dev.properties)
- [canopus.service.txt](canopus.service.txt)
