# Integridad y datos históricos de la auditoría PRE-AWS

Verificación ejecutada en **Codex Cloud**, con Java 25 y MySQL 8.4.13 local propio.
Se conservan la base `codex/lab12-completo` y todos sus avances. Esta ejecución no
acredita acceso a WSL, Windows/IIS ni AWS.

## Pruebas de servidor ejecutadas

`PruebasAuditoriaPreaws` pasó **44/44 comprobaciones**. El test es opt-in: exige
`ADMITU_PRUEBAS_ESCRITURA=true`, configuración privada y esquema `admitu_db`.
Las nuevas personas, convocatorias, postulaciones, pagos, documentos y
observaciones se crean dentro de una transacción y se revierten al finalizar.

Se comprobaron BO/DAO de historial inmutables, contexto JDBC sin herencia entre
usuarios, administrador con cuenta y sin persona, cierre antes/el día/después en
Lima, tarifas 125/480 de una misma modalidad en dos convocatorias, restricciones
entre dos postulantes y dos evaluadores, actor del historial, autor y fecha de
observación del servidor, subsanación autorizada e imposibilidad de borrar su
traza. Cada rechazo SQL compara una huella SHA-256 de todos los campos y filas
de doce tablas de negocio; no usa `GROUP_CONCAT`, que podría truncarse.

Tras `rollback`, la huella global también coincide exactamente con la anterior
a la creación de las fixtures. No se imprimen filas personales, claves ni tokens.
Las secuencias autoincrementales pueden avanzar aun cuando MySQL revierte las
filas; esto no es pérdida ni modificación de registros históricos.

Reproducción después de compilar y aplicar las migraciones:

```bash
export ADMITU_DB_PROPERTIES=/ruta/privada/db.properties
export ADMITU_PRUEBAS_ESCRITURA=true
export ADMITU_JDBC_JAR=/ruta/mysql-connector-j-26.7.0.jar
scripts/pruebas/auditoria-reglas.sh
```

El runner compila Maven y ejecuta la prueba Java. En la reanudación se compiló
también la clase de prueba con `javac` en `work/auditoria-test-classes`, para no
interferir con la compilación final del reactor que realizaba otro agente.
Evidencia sanitizada: [44 casos, huellas y consultas](resultados/auditoria-integridad.json).

## H10 — archivo vigente y versiones

La implementación conserva **el contenido vigente**, no una biblioteca de
originales recuperables. `lab12-archivos.sql` tiene la restricción única
`(postulante_id,tipo,referencia_id)` y el guardado reemplaza ese registro con
`ON DUPLICATE KEY UPDATE`. Al subsanar un documento observado/rechazado aumenta
`numero_version` y permanece la trazabilidad de la observación y la transición,
pero el contador no conserva los bytes anteriores. El diagnóstico encontró
22 documentos con `numero_version>1` en la réplica de ensayo.

Las fuentes disponibles de RF05/RF08/RF11 describen carga, modificación,
visualización, eliminación y persistencia del archivo vigente; no establecen
una consulta o descarga de originales históricos. Por ello se conserva el
diseño vigente y se documenta su límite. No se presenta el contador como un
archivo de versiones. Si el catálogo aprobado o el JP exige conservar originales,
queda **pendiente** incorporar una tabla de versiones inmutables y definir sus
permisos y retención; no se atribuye esa decisión al docente sin la fuente.

## H11 — conteos reales y recarga controlada

El diagnóstico de solo lectura, posterior al rollback del test, obtuvo:

| Comprobación | Resultado Cloud |
|---|---:|
| Documentos APROBADOS sin bytes activos del propietario | 254 |
| Pagos APROBADOS sin voucher activo del propietario | 72 |
| Duplicados por postulante/convocatoria | 0 |
| Pagos vigentes duplicados por postulación | 0 |
| Archivos con referencia de propietario incorrecta | 0 |

Son cantidades de la reproducción del SQL LAB11 y las fixtures sintéticas de
esta sesión, **no cantidades medidas en la instalación del propietario**. Las
migraciones no pueden reconstruir bytes que nunca se guardaron. No se borraron
ni se degradaron masivamente esas aprobaciones. El panel detecta expedientes
favorables inconsistentes, y los indicadores de documentos y aptitud exigen
bytes activos del propietario.

Para revisar otra instalación se debe usar el procedimiento siguiente:

1. Verificar host, esquema y respaldo recuperable; ejecutar únicamente
   `database/auditoria-preaws-diagnostico.sql` para obtener sus propios conteos.
2. Identificar por separado los expedientes afectados y conservar un registro
   privado con sus identificadores, estado previo y motivo de la intervención.
3. Recargar los documentos o vouchers originales por sus titulares y volver a
   validarlos con el evaluador asignado. Comparar descarga y SHA-256 con el
   original; un nombre o una ruta no sirven como evidencia de contenido.
4. Las aprobaciones históricas están protegidas contra reemplazo. Si requieren
   recarga, hace falta una corrección administrativa **individual y justificada**
   del flujo antes de subir el archivo. No se ofrece aquí un UPDATE masivo ni se
   debilita la inmutabilidad de pagos aprobados para facilitar esa recarga.
5. Guardar el estado antes/después y repetir SQL, descargas y verificación de
   aptitud. La recarga de originales históricos sigue **pendiente del titular**.

La consulta de asignaciones separa NULL de evaluador real. En esta ejecución,
posterior a las fixtures REST y de tarifas, había 95 postulaciones sin evaluador
y 3 asignadas. Esa diferencia incluye fixtures nuevas y no demuestra un fallo de
mapeo ni altera las asignaciones históricas. La prueba REST de reasignación y
revocación se registra en la suite de autorización.
