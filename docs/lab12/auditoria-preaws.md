# Auditoría PRE-AWS de AdmitU

Trabajo del 13 de noviembre de 2026. Base exclusiva `codex/lab12-completo`,
commit `2ad401985177e8b26e16db4840c2e9c262cdb2b5`. Rama de corrección
`codex/auditoria-preaws`; destino del PR `codex/lab12-completo`.
Se leyeron íntegramente los dos adjuntos. El mensaje del propietario prevalece
sobre nombres de rama y autorizaciones antiguas: no se modifica master ni
lab11-diego, no se fusiona PR #1 y no se despliega AWS.

Las reanudaciones verificaron los HEAD publicados `719ef8d05` y `a4c2bf6785`;
se conservaron los doce commits publicados, los cambios locales y los servicios
existentes, sin recrear la base ni repetir las suites aprobadas innecesariamente.
Los resultados siguientes corresponden a Cloud propio, Java 25, Maven 3.9.11,
GlassFish 8, MySQL 8.4.13, .NET 10 y Chromium. No se accedió a WSL del propietario,
Windows/IIS ni AWS. Las evidencias antiguas de LAB12 se conservan separadas.

## Resultado y límites

Las correcciones de autorización, historial, autoría, fechas, tarifas, métricas y
metadatos están implementadas y tienen regresiones específicas. Se conservan la
cadena REST→BO→DAO→CallableStatement→SP, JasperReports real y los avances LAB12.
No se presenta «15/15» como certificación de todos los casos, de rendimiento con
500 trámites ni de aptitud para publicación pública. H06/H10/H11/H12 mantienen
los pendientes expresos que se detallan abajo.

## Hallazgos H01–H17

| ID | Estado | Cambio, evidencia y límite |
|---|---|---|
| H01 | CORREGIDO | `AutenticacionFiltro`, `Seguridad`, RS y `auditoria-preaws-integridad.sql`: lista explícita de verbo/ruta/rol, envío exclusivo del propietario postulante, revisión evaluador solo asignada; bloqueo SQL transaccional. Commits `340646d`, `bf1d002`. Suite [seguridad](resultados/auditoria-seguridad.json): creación/envío/asignación/cruces denegados sin cambios SQL. |
| H02 | CORREGIDO | `DatosSesion.Convocatorias.cs`: fecha_fin persistida como cierre inclusivo, sin restar 15 días; Java/SQL usan día Lima. UI conserva fechas ausentes. [Tres fronteras REST/SQL](resultados/preaws-fechas-tarifas.json) y [44 reglas](resultados/auditoria-integridad.json). |
| H03 | CORREGIDO | Tarifa por `(convocatoria,modalidad)`, wizard, resumen, voucher y edición admin de la convocatoria elegida; sin primer costo global ni 350 ficticio. [Dos/tres tarifas distintas](resultados/preaws-fechas-tarifas.json), SQL y pruebas C#. Commits `022b756`, `719ef8d`. |
| H04 | CORREGIDO | Requisitos obligatorios aprobados con bytes activos y propietario correcto; estados desfavorables excluidos. Indicadores independientes sin conversiones 170%; alerta histórica visible. [Modelos14/14](resultados/auditoria-metricas-modelos.json), `database/auditoria-contraste.sql` y contraste integrado de KPI. |
| H05 | CORREGIDO | Bandeja fuera de los 15 RF de las fuentes disponibles: avisos rotulados como temporales de esta sesión, sin prometer persistencia ni entrega de correo. Vacío no significa ausencia de actividad. [Decisión](auditoria-carga.md). No se implementó una bandeja durable ficticia. |
| H06 | PARCIAL | Corregidos constructor bloqueante, Task.Run/WaitAll iniciales y concurrencia de GET: inicialización async scoped y máximo 6. [35 pruebas](resultados/auditoria-carga-sesion.json), [comparación simulada](resultados/auditoria-carga-comparacion.json) y [300 GET autenticados](resultados/auditoria-carga-autenticada.json). Faltan paginación completa, CRUD heredados async y 500 trámites representativos. |
| H07 | CORREGIDO | Archivos documentales/vouchers requieren asignación del expediente concreto; DNI/foto/certificado requieren expediente del propietario asignado. Escritura valida dueño de referencia además de JSON. Pruebas de dos evaluadores, revocación y bytes en [77 casos](resultados/auditoria-seguridad.json). Lectura global de metadatos «Todos» queda explícita. |
| H08 | REFUTADO CON EVIDENCIA | DAO y DTO ya leían evaluador_id correctamente. Los 90 expedientes de la semilla LAB11 estaban sin asignar, 28 pendientes; no se cambió el mapeo. Asignación admin, lectura evaluador y reasignación pasan en la suite REST. [Diagnóstico](auditoria-integridad.md). |
| H09 | CORREGIDO | Eliminados cierre/fases inferidas, hora 10:00, cumpleaños/fechas/nombres de archivos inventados. Null se conserva y se muestra como ausente; fechas DATE no tienen hora fabricada; vigencia carné procede de la base. [Detalle y pruebas](preaws-fechas-tarifas.md), commit `ed18495`. |
| H10 | PENDIENTE | Se conserva archivo vigente y contador de versión; el catálogo disponible no exige recuperar originales de cada versión. Se documenta la limitación sin afirmar conservación histórica de bytes. Si el JP exige originales, falta definir retención y almacenamiento inmutable. [Decisión](auditoria-integridad.md). |
| H11 | PENDIENTE | Diagnóstico confirma 254 documentos aprobados y 72 pagos aprobados sin bytes en semilla LAB11. Se preservan las filas y no se fabrican descargas ni aptitud. Recuperación exige originales y revalidación individual por titulares/equipo. [Conteos y guía](auditoria-integridad.md). |
| H12 | PARCIAL | Cookies Secure en Production, keyring externo protegido, HTTPS remota, backups SHA256 y restauración real en clon están corregidos. [8 pruebas+restauración](resultados/auditoria-operaciones.json). Windows/IIS, DPAPI/certificados, PowerShell, reinicio de servicios y HTTPS público no se ejecutaron. AWS no se intentó. |
| H13 | REFUTADO CON EVIDENCIA | RF15 en plan/metodología disponibles exige COMPLETADAS: FINALIZADA es coherente. Se mantiene cupo compartido convocatoria/carrera, sin repartirlo entre modalidades. [Fuentes y límite](auditoria-metricas.md). El catálogo original del JP no está adjunto; una reserva por modalidad requeriría decisión y configuración nuevas. |
| H14 | CORREGIDO | [77/77 REST](resultados/auditoria-seguridad.json), 64 rechazos exactos 401/403 con huellas idénticas de 28 tablas, dos postulantes/dos evaluadores/admin, bloqueo, expiración y revocación. Pruebas de cookies/CSRF y 15 flujos Chromium se registran en la tabla de ejecución. No se amplían permisos para hacer pasar tests. |
| H15 | CORREGIDO | Diagnósticos DAO y mapper sanitizados conservan tipo/SQLState/código y referencia, sin mensajes SQL/PII. [6/6 pruebas](resultados/auditoria-diagnostico.json); espera inicial y errores UI de observación conservan memoria solo tras éxito REST. [Detalle](auditoria-errores.md). Accesibilidad exhaustiva y todos los CRUD async no se certifican. |
| H16 | CORREGIDO | Historial solo lectura para todos los clientes, incluido admin. BO/DAO/SP rechazan CRUD; transiciones servidor generan actor/fecha reales en misma transacción. [77 REST](resultados/auditoria-seguridad.json), [44 MySQL](resultados/auditoria-integridad.json). |
| H17 | CORREGIDO | Autor nuevo desde token; suplantación 403; autor/documento/fecha originales inmutables. Admin no firma por un evaluador; texto y subsanación legítima conservan ámbito y fecha servidor. Pruebas positivas/negativas y hashes SQL en las suites de seguridad e integridad. |

## Pruebas efectivamente ejecutadas

| Grupo | Resultado Cloud | Evidencia/reproducción |
|---|---|---|
| Maven Java25 | PASS | `mvn -B -f backend/AdmitU/pom.xml clean package` |
| Blazor .NET10 | PASS, 0 errores/0 advertencias | `dotnet build frontend/AdmitUWA/AdmitUWA/AdmitUWA.csproj` |
| BO/MySQL LAB12 | 32/32 PASS, repetidas tras cambios | `scripts/pruebas/reglas.sh`; fixtures propias retiradas en finally |
| Auditoría MySQL | 44/44 PASS | `scripts/pruebas/auditoria-reglas.sh`; [JSON](resultados/auditoria-integridad.json), 12 tablas con huella idéntica antes/después rollback |
| Regresión REST15 RF | 24/24 PASS | `scripts/pruebas/regresion.py --apply`; [JSON final](resultados/preaws-regresion-api.json), separado del histórico |
| Seguridad REST | 77/77 PASS | [JSON](resultados/auditoria-seguridad.json), 64 negativos con 28 tablas sin cambios; cuenta/sesión excluidas porque bloqueo/logout sí deben modificarlas |
| Cierre/tarifas | 3/3 REST/SQL PASS | [JSON](resultados/preaws-fechas-tarifas.json), fronteras mañana/hoy/ayer con misma modalidad y tarifas distintas |
| Reglas métricas C# | 14/14 PASS | [JSON](resultados/auditoria-metricas-modelos.json) |
| Carga/fechas/error en C# | 35/35 PASS | [JSON](resultados/auditoria-carga-sesion.json), transporte HTTP simulado claramente rotulado |
| Diagnóstico sin secretos | 6/6 PASS | [JSON](resultados/auditoria-diagnostico.json) |
| Backups y restauración | 8/8 simulados+restauración real PASS | [JSON](resultados/auditoria-operaciones.json); clon separado 30 tablas/153 SP/6 archivos, hashes iguales; principal solo leído |
| Arranque Production | 3 rechazos PASS | Claves ausentes, dentro del sitio y sin protección Linux; no prueba DPAPI/IIS |
| Carga autenticada | 300/300 HTTP200 | Cinco identidades, 100 GET por concurrencia 1/5/10; p95=0,198/0,710/1,612 s y p99=0,238/0,842/1,765 s. Lecturas, sin 500 trámites |
| Jasper/SQL | Tres PDF reales PASS | `scripts/pruebas/reportes.sh`; total/filtros/vacío/cupos compartidos/texto extenso contrastados con SQL |
| Jasper REST/roles final | 33/33 PASS | [JSON](resultados/auditoria-reportes.json): 9 roles, 12 PDF/SQL, 9 filtros inválidos y 3 anónimos |
| KPI SQL/API/Chromium final | 31/31 PASS | [JSON](resultados/auditoria-kpi-sql.json): 17 convocatorias SQL/REST, flags de archivo, 12 visibles en Chromium y asignaciones |
| Cookies/CSRF Chromium final | 6/6 PASS | [JSON](resultados/preaws-seguridad-ui.json); aislamiento, rechazo de perfil, CSRF y logout tras la corrección SSR |
| Registro y retorno a login | PASS | [JSON](resultados/preaws-registro-ui.json): formulario protegido antes de interactividad, cuenta real, cookie y perfil persistido |
| Chromium: 15 RF/tres perfiles | 15/15 flujos principales PASS | `scripts/pruebas/ui.py --apply`; [JSON](resultados/preaws-ui.json), capturas privadas; ver límites visuales del PDF |

Las credenciales, tokens, cookies, dumps, PDFs con datos y capturas permanecen
fuera de Git en `work/`. Solo se publican resúmenes sanitizados. Los fixtures
REST/UI se conservan con prefijos sintéticos; las pruebas transaccionales no
persisten sus filas. No hay borrado ni actualización masiva del histórico.

## Pendientes y validación del propietario antes de AWS

1. Conservar los originales y atender individualmente 254 documentos/72 vouchers
   históricos sin bytes; el código no puede reconstruirlos. No aprobarlos por lote.
2. Decidir con JP si H10 necesita archivos originales de todas las versiones y si
   RF15 necesita reservas de cupo por modalidad. No existe autorización para
   inventar esa distribución ni se altera el cupo actual.
3. Terminar paginación/carga filtrada y CRUD async donde corresponda; ejecutar una
   carga autorizada de 500 trámites realistas con archivos, escrituras y varias
   identidades. Las 300 lecturas actuales no acreditan RNF04.
4. Validar en Windows/IIS del propietario: ACL/keyring DPAPI/certificado, TLS real,
   cookies Secure, variables de servicio, puertos privados, backup/restore y reinicio.
   Estos pasos no fueron ejecutados en WSL ni AWS por esta auditoría.

Para validar en WSL, traer esta rama sin fusionar el PR y seguir
[despliegue](../despliegue/README.md). Compilar ambos proyectos; ejecutar el
migrador primero en dry-run y luego con host/servidor/esquema de ensayo
identificados y backup privado. Deben incluirse las dos migraciones de auditoría
antes de actualizar WAR. Proveer cuentas sintéticas y archivos privados según
[autorización](auditoria-autorizacion.md), ejecutar reglas, regresión, seguridad,
Chromium, KPI y Jasper. Los runners mutantes requieren opt-in; no apuntarlos a
producción. No iniciar AWS como parte de esta validación.

## Incidencias resueltas durante las pruebas

La suite nueva MySQL interpretaba 125.00 como entero textual: helper corregido y
repetición 44/44. La regresión RF10 editaba como admin en vez de autor evaluador:
fixture corregido y repetición 24/24. El primer intento de Jasper REST coincidió
con el redespliegue de WAR y recibió 500/404; se invalida como ejecución estable y
se repite. Chromium detectó ingreso evaluador intermitente: la hidratación SSR→InteractiveServer reemplazaba el formulario nativo durante la
entrada. La página de cookies queda en SSR estático y omite la carga pública de
toda la sesión; los portales conservan Blazor interactivo. La prueba final figura
en sus evidencias. Ninguno de esos intentos
fallidos se presenta como PASS.


## Límite visual del visor PDF en Cloud

Los tres PDF reales, bytes, descarga, permisos, filtros y cifras SQL pasan.
La inspección visual del contenido dentro del iframe está bloqueada por una
política administrada de Chromium Cloud ([diagnóstico reproducible](resultados/preaws-visor-pdf.json)): URLBlocklist contiene `*` y su allowlist
no permite el esquema data: usado por el visor docente. El navegador informa
pdfViewerEnabled=true, pero el iframe termina en chrome-error tanto en headless
como en las variantes headful ensayadas. No se alteró esa política ni se declara
vista visual aprobada. Verificar el visor con el navegador de WSL/Windows del
propietario antes de AWS; la descarga y el PDF Jasper no dependen de esa política.


## Cierre de la verificación SSR

La ejecución final de los 15 flujos Chromium terminó después del build que
incorpora `ExcludeFromInteractiveRouting` en el acceso y selección de renderizado
por página en `App.razor`. Los tres portales conservan interactividad. Pasaron
15/15 flujos, 6/6 cookies/roles/CSRF y las dos variantes de ingreso rápido/lento
([A/B final](resultados/preaws-login-hidratacion.json)). Se conserva el intento
anterior fallido en un JSON separado y no se mezcla con el resultado final.

Estado de los 17 hallazgos: **11 corregidos, 2 refutados según las fuentes
indicadas, 2 parciales (H06/H12) y 2 pendientes (H10/H11)**. H06/H12 conservan
las correcciones implementadas, con validación o trabajo adicional explícito.
La comprobación visual del visor integrado sigue pendiente en el navegador
local del propietario; el mismo PDF por HTTP loopback cargó el componente PDF,
lo que distingue el bloqueo del esquema data: de un archivo Jasper inválido.


La prueba adicional de registro descubrió un envío nativo antes de la activación
de Blazor. El formulario ahora está deshabilitado hasta quedar interactivo y
tiene FormName explícito. Tras esa corrección pasó el flujo completo de registro,
retorno al login SSR, ingreso y consulta del perfil persistido. El build .NET final
pasó con cero advertencias y errores. Solo se repitió esta regresión específica;
los 15 flujos y las suites aprobadas se conservaron.
