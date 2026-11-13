# Auditoría previa a AWS: operación y reportes

## H12 — configuración corregida; validación operativa pendiente

`Program.cs` usa cookies de sesión y antiforgery `Secure=Always` en Production, HSTS y redirección HTTPS. Exige key ring persistente mediante `DataProtection:KeysPath`, protegido por DPAPI en Windows o por certificado explícito. Una API HTTP solo se admite en loopback en Production. La guía y el publicador Windows configuran ruta de claves fuera del sitio y DNS permitido; la ACL, certificados, variables del servicio y reinicio real siguen requiriendo verificación en Windows.

El migrador conserva dumps incompletos como `.partial` y genera SHA256 para backups completos. `restaurar.py` verifica hash y host/servidor/esquema, respalda primero el estado vigente y luego restaura exclusivamente el dump propio autorizado en `admitu_db`. No se ejecutó contra AWS ni WSL. DDL y restauración no son transacciones atómicas; el respaldo y el mantenimiento del servicio son necesarios.

Pruebas Cloud ejecutadas por esta revisión: `python3 -m unittest discover -s scripts/pruebas -p test_operaciones.py -v`, **7/7 PASS** con clientes MySQL simulados. Cubren dry-run sin conexión, hash incorrecto, esquema ajeno, host ajeno, servidor ajeno, backup fallido sin restauración y backup completo antes de restaurar. `py_compile`, dry-run del migrador y `git diff --check` también pasaron. Estas pruebas no acreditan recuperación MySQL real, sintaxis PowerShell, IIS, certificado, DPAPI ni ejecución de servicios Windows. La compilación y suites integradas se registran en el informe central.

## H13 — semántica del reporte comprobada contra las fuentes disponibles

`plan-lab12.md`, fila RF_15, pide «vacantes vs postulaciones COMPLETADAS por modalidad/carrera»; `METODOLOGIA_PROFESOR.md` conserva esa definición. El estado de inscripción completada es `FINALIZADA`, por lo que el predicado de `LAB12_REPORTE_VACANTES` coincide con esas fuentes. Incluir todos los borradores/enviados cambiaría esa métrica sin respaldo. No se modificó el SQL ni se inventó una distribución de cupos.

El cupo almacenado pertenece a `(convocatoria,carrera)` y se comparte entre modalidades. La plantilla lo rotula y no lo suma repetidamente. Falta una definición del JP si se requiere cupo reservado por modalidad; no hay información suficiente para distribuir el histórico.

## JasperReports y permisos

La revisión de código mantiene tres JRXML compilados por JasperReports, `JRMapCollectionDataSource` con datos de DAO/CallableStatement/SP, locale es-PE, zona America/Lima y salida PDF real. Cada petición utiliza dataset e impresión propios y las plantillas compiladas se conservan inmutables. Los filtros negativos y rango de fecha invertido se validan en BO; fechas inválidas se rechazan en REST. `Cache-Control:no-store` evita conservar documentos privados en cachés HTTP.

Ingresos y Vacantes solo admiten administrador. Observados admite administrador/evaluador como lectura del ámbito visible «Todos los expedientes» y contiene metadata de observaciones, sin bytes documentales. Esta lectura global se mantiene explícitamente; escritura y descarga sensible requieren asignación según la corrección de seguridad. Una observación pendiente produce una fila, por lo que varias observaciones pueden repetir el expediente. Los filtros de ingresos usan fecha_pago, y vacantes fecha_finalizacion.

Esta revisión estática no equivale a nuevos PDF descargados ni a contraste SQL ejecutado. Las pruebas reales y los conteos de la instancia Cloud se incluyen en el informe central; las cifras históricas de `reportes.md` siguen identificadas como ejecución anterior.
