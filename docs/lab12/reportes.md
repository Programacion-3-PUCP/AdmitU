# RF03, RF09 y RF15 — JasperReports

Los tres PDF se generan en Java con JasperReports 6.21.3 a partir de procedimientos MySQL ejecutados mediante CallableStatement. Se conservan las capas RS → BOI/BOImpl → DAO/Impl → SP. El frontend consume application/pdf por HttpClient y muestra el documento en iframe con descarga.

Rutas GET, protegidas por el filtro de autenticación y por comprobación de rol en el recurso:

- `ReporteRS/generarIngresos`: administrador; pagos APROBADOS agrupados por convocatoria/modalidad. Los filtros de fecha usan `pago.fecha_pago`, porque el esquema no guarda una fecha independiente de validación.
- `ReporteRS/generarObservados`: administrador/evaluador; observaciones PENDIENTES por convocatoria y modalidad. Cada fila representa una observación; un expediente puede aparecer varias veces.
- `ReporteRS/generarVacantes`: administrador; cuenta postulaciones con código de estado FINALIZADA (completada), por convocatoria/modalidad/carrera. Los filtros de fecha usan fecha_finalizacion.

Parámetros: `convocatoriaId`, `modalidadId` (0 significa todos), `desde` y `hasta` opcionales en formato AAAA-MM-DD. Se rechazan rangos invertidos e identificadores negativos. Las plantillas originales JRXML se compilan al primer uso y se reutilizan como diseños inmutables; cada petición tiene su propio dataset e impresión.

## Límite del modelo RF15

La tabla oferta_carrera define un cupo por convocatoria/carrera, compartido por modalidades. El reporte rotula ese valor como compartido y no suma el cupo repetido entre filas de modalidades. Permite comparar postulaciones completadas por modalidad con el cupo de la carrera, pero no demuestra un cupo reservado por modalidad. Si el catálogo exige reservas separadas, se necesita una distribución explícita de cupos y su formulario de configuración; no se inventa un reparto histórico.

## Pruebas efectivamente ejecutadas en la reconstrucción

`mvn package` completó seis módulos; `dotnet build` pasó con 0 advertencias/errores. `scripts/pruebas/reportes.sh` ejecutó el BO real contra MySQL local de ensayo y verificó los tres PDF, PDF vacío, filtro convocatoria1/modalidad1, fecha invertida y filtro negativo. `pdftotext` contrastó el texto con consultas SQL independientes:

| Comprobación | SQL observado | Resultado PDF |
|---|---|---|
| Ingresos generales | 72 aprobados, S/37,380.00 | total coincide |
| Convocatoria1/modalidad1 | 3 aprobados, S/1,350.00 | total coincide |
| Observaciones pendientes | 16 | cantidad coincide |
| Postulaciones FINALIZADA | 36 | total completadas coincide |
| Filtro inexistente | 0 | total cero |

Estos valores son de las fixtures de LAB11 en el entorno local al ejecutar la prueba; pueden cambiar con nuevas pruebas. No constituyen evidencia de navegación Blazor ni de despliegue AWS. La evidencia del runner se guarda en `work/evidencias/`, ignorado por Git, para evitar publicar documentos con datos personales.

Reproducción: compilar el WAR, configurar `ADMITU_DB_PROPERTIES` con un archivo privado y ejecutar `scripts/pruebas/reportes.sh`. El script solo lee datos y escribe PDFs locales; nunca imprime configuraciones ni credenciales.
