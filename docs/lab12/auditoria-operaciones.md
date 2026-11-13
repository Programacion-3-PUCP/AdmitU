# Auditoría previa a AWS: operación y reportes

## H12 — configuración corregida; validación operativa pendiente

`Program.cs` usa cookies de sesión y antiforgery `Secure=Always` en Production, HSTS y redirección HTTPS. Exige key ring persistente mediante `DataProtection:KeysPath`, protegido por DPAPI en Windows o por certificado explícito. Una API HTTP solo se admite en loopback en Production. La guía y el publicador Windows configuran ruta de claves fuera del sitio y DNS permitido; la ACL, certificados, variables del servicio y reinicio real siguen requiriendo verificación en Windows.

El migrador aplica también las migraciones PRE-AWS de integridad y métricas antes de desplegar el WAR. Conserva dumps incompletos como `.partial` y genera SHA256 para backups completos. `restaurar.py` verifica hash y host/servidor/esquema, respalda primero el estado vigente y luego restaura exclusivamente el dump propio autorizado en `admitu_db`. No se ejecutó contra AWS ni WSL. DDL y restauración no son transacciones atómicas; el respaldo y el mantenimiento del servicio son necesarios.

Pruebas Cloud ejecutadas por esta revisión: `python3 -m unittest discover -s scripts/pruebas -p test_operaciones.py -v`, **8/8 PASS** con clientes MySQL simulados. Cubren dry-run sin conexión, hash incorrecto, esquema ajeno, host ajeno, servidor ajeno, backup fallido sin restauración backup completo antes de restaurar y dump con bytes binarios no UTF8. `py_compile`, dry-run del migrador y `git diff --check` también pasaron. También pasó el build aislado de Blazor con cero errores/advertencias y tres negativos de arranque Production: clave persistente ausente, clave dentro del sitio y Linux sin certificado. Estas pruebas no acreditan sintaxis PowerShell, IIS, certificado, DPAPI ni ejecución de servicios Windows. La compilación y suites integradas se registran en el informe central.

## Recuperación real de MySQL en Cloud — PASS

El 13/11/2026 se ejecutó una restauración completa en un segundo contenedor propio de MySQL **8.4.13**, con identidad de servidor distinta, puerto externo **3307** y TLS `VERIFY_CA`. La fuente principal de ensayo en 3306 solo se leyó para producir el dump; este ensayo no escribió en ella. Dumps, clientes y claves permanecen en `work/`, fuera de Git.

Se verificó el destino; se restauró el dump en el clon; se creó una convocatoria sintética en ese clon; se comprobó que cambió su checksum; y el restaurador volvió a crear un backup privado completo con SHA256 antes de recuperar. Se compararon todas las filas mediante checksums, conteos por tabla y hashes de las definiciones de procedimientos: **30 tablas y 153 SP idénticos**. Los **seis archivos, 1446 bytes en total**, conservaron su SHA256. No se alteró masivamente la base principal ni se eliminó su histórico.

El ensayo identificó un defecto real del restaurador: un dump puede contener bytes de PNG que no son UTF8. Se corrigió el preflight para examinar bytes y los nuevos backups usan `--hex-blob`; una prueba de regresión cubre ese caso. Los backups previos de una restauración también conservan su manifiesto SHA256.

Runner reproducible para un clon propio verificado:

```bash
# Valida dump y hash; no abre MySQL ni modifica el clon.
python3 scripts/pruebas/restauracion.py --backup-file /ruta/privada/clon.sql --sha256 SHA256_DEL_MANIFIESTO
# Solo en un clon separado de la base que atiende la aplicación:
python3 scripts/pruebas/restauracion.py --backup-file /ruta/privada/clon.sql --sha256 SHA256_DEL_MANIFIESTO --apply --defaults-file /ruta/privada/clon.cnf --expected-host HOST_DEL_CLON --expected-server HOSTNAME_DEL_CLON
```

El segundo comando restaura dos veces y conserva backups antes de ambas restauraciones. El operador debe haber confirmado que ese servidor/esquema es un clon propio sin tráfico de aplicación. [Resultados sanitizados](resultados/auditoria-operaciones.json). La recuperación aquí acreditada corresponde a MySQL en Cloud; IIS, DPAPI, Windows, WSL y AWS siguen sin probarse.

## H13 — semántica del reporte comprobada contra las fuentes disponibles

`plan-lab12.md`, fila RF_15, pide «vacantes vs postulaciones COMPLETADAS por modalidad/carrera»; `METODOLOGIA_PROFESOR.md` conserva esa definición. El estado de inscripción completada es `FINALIZADA`, por lo que el predicado de `LAB12_REPORTE_VACANTES` coincide con esas fuentes. Incluir todos los borradores/enviados cambiaría esa métrica sin respaldo. No se modificó el SQL ni se inventó una distribución de cupos.

El cupo almacenado pertenece a `(convocatoria,carrera)` y se comparte entre modalidades. La plantilla lo rotula y no lo suma repetidamente. Falta una definición del JP si se requiere cupo reservado por modalidad; no hay información suficiente para distribuir el histórico.

## JasperReports y permisos

La revisión de código mantiene tres JRXML compilados por JasperReports, `JRMapCollectionDataSource` con datos de DAO/CallableStatement/SP, locale es-PE, zona America/Lima y salida PDF real. Cada petición utiliza dataset e impresión propios y las plantillas compiladas se conservan inmutables. Los filtros negativos y rango de fecha invertido se validan en BO; fechas inválidas se rechazan en REST. `Cache-Control:no-store` evita conservar documentos privados en cachés HTTP.

Ingresos y Vacantes solo admiten administrador. Observados admite administrador/evaluador como lectura del ámbito visible «Todos los expedientes» y contiene metadata de observaciones, sin bytes documentales. Esta lectura global se mantiene explícitamente; escritura y descarga sensible requieren asignación según la corrección de seguridad. Una observación pendiente produce una fila, por lo que varias observaciones pueden repetir el expediente. Los filtros de ingresos usan fecha_pago, y vacantes fecha_finalizacion.

Esta revisión estática no equivale a nuevos PDF descargados ni a contraste SQL ejecutado. Las pruebas reales y los conteos de la instancia Cloud se incluyen en el informe central; las cifras históricas de `reportes.md` siguen identificadas como ejecución anterior.
