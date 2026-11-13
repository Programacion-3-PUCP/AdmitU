# Contraste de indicadores y JasperReports en Cloud

Estas pruebas usan la instancia de ensayo propia de Codex Cloud y conservan los
datos históricos. No representan acceso a WSL, Windows/IIS ni AWS. Los PDF,
textos extraídos y capturas permanecen privados en `work/evidencias`.

## Reproducción

Con el WAR y Blazor actualizados, credenciales sintéticas y un archivo MySQL
privado ya configurados:

```bash
python3 scripts/pruebas/auditoria-kpi.py \
  --credentials-file /ruta/privada/credenciales.json \
  --mysql-defaults-file /ruta/privada/mysql.cnf
python3 scripts/pruebas/auditoria-reportes.py \
  --credentials-file /ruta/privada/credenciales.json \
  --mysql-defaults-file /ruta/privada/mysql.cnf
scripts/pruebas/reportes.sh
```

Los dos runners nuevos sólo leen tablas de negocio. Sus inicios/cierres de sesión
escriben exclusivamente sesiones de sus cuentas sintéticas. No ejecutarlos
durante un redespliegue ni mientras otro runner cambia las cohortes contrastadas.
El runner de PDF comprueba que la consulta SQL conserve el mismo resultado antes
y después de descargar cada PDF.

## Ejecución de Jasper por BO/DAO/SP

La repetición de `reportes.sh` pasó: tres PDF reales, validación de filtros
inválidos, total general y filtrado, observaciones pendientes, FINALIZADA,
consulta vacía, rótulo de cupo compartido y descripción extensa sin truncar.
La ejecución inicial de esta reanudación leyó 73 pagos aprobados por 37480.00,
17 observaciones pendientes y 37 inscripciones FINALIZADA. Son cifras del
instante de la prueba; los siguientes fixtures pueden modificarlas.

El primer intento del nuevo runner REST coincidió con una sustitución del WAR:
recibió inicialmente HTTP500 y luego HTTP404 transitorios, mientras los últimos
casos volvieron a responder 200/400/401. Ese intento fallido se conserva en los
logs privados y no acredita permisos ni reportes correctos. La repetición con
servidor estable se registra en los resultados finales.

## Indicadores

`auditoria-kpi.py` contrasta seis indicadores por convocatoria mediante una
consulta SQL independiente, los datos recibidos por REST y los valores que
Chromium lee del resumen. También comprueba las cuatro tarjetas, la recaudación,
los avisos de estados favorables inconsistentes, la ausencia de conversiones
entre cohortes y las asignaciones de evaluadores persistidas. La comprobación
de `archivoActivo` compara todos los documentos recibidos con `EXISTS` sobre
bytes activos y propietario correcto en MySQL.

La etapa SQL/REST inicial pasó en las 13 convocatorias presentes y en el conjunto
de documentos. Tras finalizar las demás suites, la repetición integrada pasó
**31/31 comprobaciones**: 17 convocatorias por SQL/REST, todos los documentos,
las 12 convocatorias visibles en el selector de Chromium y la carga de
evaluadores. Se verificaron seis indicadores y las cuatro tarjetas por cada
convocatoria visible, incluido el importe recaudado. Las convocatorias inactivas
se contrastaron por SQL/REST aunque el selector no las ofrece.

Evidencia final: [resultados/auditoria-kpi-sql.json](resultados/auditoria-kpi-sql.json).
La prueba observó estados favorables históricos inconsistentes en la alerta y
comprobó que no elevan el conteo de aptos. No se alteró ninguna fila para hacer
coincidir los resultados; los valores iniciales no se reutilizan como si fueran
una comprobación del conjunto posterior.

## Resultado REST estable

La repetición pasó **33/33 comprobaciones**, con evidencia sanitizada en
[resultados/auditoria-reportes.json](resultados/auditoria-reportes.json):

- 9 combinaciones de administrador/evaluador/postulante y tres reportes, con
  HTTP200 o HTTP403 según permiso.
- 12 PDF reales contrastados con SQL: sin filtros, convocatoria/modalidad,
  convocatoria inexistente y rango de fechas para cada uno de los tres reportes.
- 9 filtros inválidos rechazados con HTTP400 y tres accesos anónimos con HTTP401.

Cada PDF permitido tuvo `application/pdf`, firma `%PDF-`, más de 1000 bytes y
`Cache-Control: no-store`. `pdftotext` permitió comprobar totales, observaciones
pendientes e inscripciones FINALIZADA. No se sustituyeron los reportes con HTML.
Los totales finales se conservan en el JSON para el instante de esta ejecución.

## Alcance de RF15 y permisos

Se conserva FINALIZADA como inscripción completada según `plan-lab12.md` y
`METODOLOGIA_PROFESOR.md`. El cupo sigue perteneciendo a convocatoria/carrera y
se comparte entre modalidades. No se distribuyeron cupos históricos ni se cambió
el catálogo del JP. Observados produce una fila por observación pendiente.
Ingresos y Vacantes son administrativos; Observados permite administrador y
evaluador para la consulta global explícita de metadatos. La lectura de un PDF
de observaciones no concede permiso para descargar documentos de un expediente
sin asignación.
