# Matriz de evidencia LAB12 y auditoría PRE-AWS

Corte Cloud:13/11/2026. Rama `codex/auditoria-preaws`, derivada exclusivamente de
`codex/lab12-completo` (`2ad4019`). Se conservan las evidencias históricas LAB12;
las nuevas ejecuciones no corresponden a WSL del propietario ni a AWS.

La nueva regresión REST pasó **24/24**, con casos de los15 RF, y Chromium pasó
**15/15 flujos principales** con administrador, evaluador y postulante. Seguridad
REST pasó **77/77**, incluidos64rechazos401/403 con28 tablas de negocio intactas.
MySQL pasó32 reglas LAB12+44 de auditoría; JasperREST/SQL33/33 y contraste
KPI SQL/API/Chromium31/31. Estos escenarios no acreditan todas las combinaciones
posibles,500 trámites ni una exposición pública segura.

Evidencias actuales: [REST](resultados/preaws-regresion-api.json),
[Chromium](resultados/preaws-ui.json), [seguridad](resultados/auditoria-seguridad.json),
[MySQL](resultados/auditoria-integridad.json), [KPI](resultados/auditoria-kpi-sql.json),
[Jasper](resultados/auditoria-reportes.json) y [hallazgos17](auditoria-preaws.md).

Raíces: UI=`frontend/AdmitUWA/AdmitUWA/Components/Pages`; REST=`backend/AdmitU/admitu_services/src/main/java/pe/edu/pucp/admitu/services`. Las clases BO/DAO se ubican en sus módulos Maven. Todas las rutas REST mantienen la raíz `admitu_services/webresources`.

| RF | Responsable | UI | REST → BO | DAO / procedimiento principal | API probada | Navegador | Alcance y límite |
|---|---|---|---|---|---|---|---|
| 01 | Diego | Admin/Convocatorias.razor | ConvocatoriaRS → ConvocatoriaBOImpl | ConvocatoriaImpl / INSERTAR, MODIFICAR y ELIMINAR_CONVOCATORIA | PASS: CRUD y baja de borrador | PASS: crear y eliminar con confirmación | La baja lógica conserva relaciones; la API protege las convocatorias con postulaciones. |
| 02 | Diego | Registro.razor; PortalPostulante/MiPerfil.razor | AutenticacionRS, PostulanteRS → AutenticacionBOImpl, PostulanteBOImpl | CuentaUsuarioImpl, PostulanteImpl / INSERTAR y MODIFICAR_POSTULANTE_PERFIL | PASS: cuenta, contraseña, perfil, discapacidad y CONADIS durables | PASS: perfil y recarga | Ruta real /postulante/perfil; DNI bloqueado y teléfono persistido. |
| 03 | Diego | Admin/ReporteIngresos.razor | ReporteRS/generarIngresos → ReporteBOImpl | ReporteImpl / LAB12_REPORTE_INGRESOS | PASS: PDF real filtrado y total contrastado | PASS: bytes PDF, iframe y descarga; alcance visual en evidencia PRE-AWS | Solo pagos APROBADOS. El filtro de fechas corresponde a fecha_pago; no existe fecha independiente de aprobación. |
| 04 | Harold | Admin/Modalidades.razor | ModalidadRS, CatalogoRS → ModalidadBOImpl, CatalogoBOImpl | ModalidadImpl, CatalogoImpl / REACTIVAR_MODALIDAD | PASS: CRUD, baja y reactivación | PASS: crear, cancelar confirmación, reactivar y recargar | Reactivar ejecuta el SP específico, no un cambio de caché. |
| 05 | Harold | PortalPostulante/Antecedentes.razor | ArchivoDigitalRS/guardarCertificado → ArchivoBOImpl | ArchivoImpl / GUARDAR_CERTIFICADO_METADATA, GUARDAR_ARCHIVO_DIGITAL | PASS: PDF, sustitución y baja | PASS: cargar y recargar bytes; iframe preparado, render visual sujeto a política Cloud | Fecha futura rechazada. COLEGIO/UNIVERSIDAD se conserva y valida; 32 pruebas BO pasan. |
| 06 | Harold | Admin/Carreras.razor | CarreraRS, CatalogoRS → CarreraBOImpl, CatalogoBOImpl | CarreraImpl, CatalogoImpl / REACTIVAR_CARRERA | PASS: CRUD, baja y reactivación | PASS: crear y buscar tras recarga | La búsqueda se verifica tras completar la hidratación de Blazor. |
| 07 | Richard | Admin/Requisitos.razor | RequisitoRS, RequisitoConvocatoriaModalidadRS → BO de requisitos | RequisitoImpl, RequisitoConvocatoriaModalidadImpl / SP de requisito y asociación | PASS: requisito obligatorio, convocatoria/modalidad y oferta | PASS: crear requisito asociado | La interacción se verifica con hidratación estable; el envío del expediente verifica los requisitos obligatorios y sus formatos/tamaños. |
| 08 | Richard | PortalPostulante/DocumentosPersonales.razor | ArchivoDigitalRS → ArchivoBOImpl | ArchivoImpl / GUARDAR, BUSCAR y ELIMINAR_ARCHIVO_DIGITAL | PASS: bytes, descarga idéntica, formatos, fecha y baja | PASS: DNI/foto y bytes tras recarga; render visual sujeto a política Cloud | JPG/PNG hasta 5 MB; MySQL MEDIUMBLOB y SHA-256. El mismo SHA-256 se recuperó después de reiniciar completamente GlassFish y el frontend. |
| 09 | Richard | PortalEvaluador/ReporteObservados.razor | ReporteRS/generarObservados → ReporteBOImpl | ReporteImpl / LAB12_REPORTE_OBSERVADOS | PASS: PDF real filtrado y datos contrastados | PASS: bytes PDF, iframe y descarga; alcance visual en evidencia PRE-AWS | Cada fila es una observación PENDIENTE; un expediente puede ocupar varias filas. |
| 10 | Francis | PortalEvaluador/Observaciones.razor | DocumentoObservacionRS → DocumentoObservacionBOImpl | DocumentoObservacionImpl / SP de observaciones | PASS: autor del token, revisión asignada y subsanación legítima | PASS: crear observación, selección visible y cambio de conjunto | Se corrigió el bind booleano por valores mios/todos. Se comprobó selección retenida y conjuntos distintos (5 propios frente a 43 totales en la prueba específica). |
| 11 | Francis | PortalPostulante/Vouchers.razor; Postular.razor | ArchivoDigitalRS/guardarVoucher, PostulacionRS/enviarPostulacion → ArchivoBOImpl, PostulacionBOImpl | ArchivoImpl, PostulacionImpl / GUARDAR_VOUCHER_METADATA y envío compuesto | PASS: bytes y doble envío con un pago/voucher | PASS: wizard, documentos y voucher | El importe procede de convocatoria/modalidad. Un pendiente puede corregirse/retirarse; un aprobado queda protegido. |
| 12 | Francis | PortalEvaluador/ValidacionVouchers.razor | PagoRS → PagoBOImpl | PagoImpl / MODIFICAR_PAGO | PASS: aprobar con fecha y rechazar modificación posterior | PASS: aprobar desde pantalla | Se exige voucher durable; aprobar/retirar concurrentemente no permite perder el archivo aprobado. |
| 13 | Matías | Admin/HistorialEstados.razor | PostulacionRS, PostulacionHistorialRS → BO de postulación e historial | PostulacionImpl, PostulacionHistorialImpl / MODIFICAR_POSTULACION | PASS: transición e historial durable | PASS: consultar con filtro de convocatoria | El historial es solo lectura para clientes de todos los roles; las transiciones registran actor de servidor en la misma transacción SQL. |
| 14 | Matías | Admin/Carnes.razor | CarnePostulanteRS → CarnePostulanteBOImpl | CarnePostulanteImpl / INSERTAR, MODIFICAR y ELIMINAR_CARNE_POSTULANTE | PASS: inelegible rechazado, emisión, búsqueda y anulación | PASS: emitir y mostrar Finalizada | Se comprobó el estado FINALIZADA en SQL y en Blazor después de corregir el mapeo. La emisión exige APTA/pago aprobado y completa la inscripción; la anulación conserva la fila, código y titular. |
| 15 | Matías | Admin/ReporteVacantes.razor | ReporteRS/generarVacantes → ReporteBOImpl | ReporteImpl / LAB12_REPORTE_VACANTES | PASS: PDF real y conteo FINALIZADA contrastado | PASS: bytes PDF, iframe y descarga; alcance visual en evidencia PRE-AWS | El cupo es compartido por carrera/convocatoria; no se duplica al sumar modalidades ni se inventan reservas. |

## RNF y despliegue

| Requisito | Evidencia actual y límite | Estado |
|---|---|---|
| RNF01 comunicación distribuida | Blazor y GlassFish en procesos separados, RESTJSON/PDF y MySQL; tres perfiles reales | PROBADO Cloud en estos escenarios |
| RNF02 catálogo≤3 s | Nueva lectura de carreras a1y20 conexiones: ver [1](resultados/preaws-catalogo1.json), [20](resultados/preaws-catalogo20.json) | Medición acotada; no generalizar a500 |
| RNF03 seguridad | PBKDF2 salado,77 REST,64 rechazos conSQL intacto, historial inmutable, autor token, asignación y archivos; cookies/CSRF Chromium en evidencia PRE-AWS | PROBADO Cloud en casos enumerados; HTTPS/IIS público pendiente |
| RNF04 500 postulantes | Constructorasync,6 GET concurrentes por scope;300 GET privados con5 identidades a1/5/10 solicitudes, p95máx1,612 s | PARCIAL: no se probaron500 trámites; falta paginación yCRUDasync |
| RNF05 atomicidad | 32BO+44auditoríaMySQL, dosenvíosREST conunresultado, rollback conhash idéntico de12 tablas | PROBADO en escenarios ejecutados |
| AWS/Windows/IIS | SecureProduction, claves externas protegidas, backup/restore real en clonMySQL;3 negativos arranqueProduction | NO DESPLEGADO por instrucción; Windows/IIS/DPAPI/TLS público no probados |

Parciales H06/H12 y pendientes H10/H11 y validación posterior del propietario en
[informe PRE-AWS](auditoria-preaws.md). La restauración real preservó30 tablas,
153 SP y6blobs en un clon, sin escribir el principal. Los254 documentos/72 pagos
históricos sin bytes permanecen conservados y requieren originales y revisión
individual. Una cuenta de500 GET públicos histórica no acredita RNF04.

El login SSR final pasó 6/6 cookies/roles/CSRF y 2/2 variantes de hidratación. Ver [JSON](resultados/preaws-seguridad-ui.json) y [A/B](resultados/preaws-login-hidratacion.json). El visor integrado data: está bloqueado por política administrada Cloud; no se certifica su render visual local.

RF02 también pasó registro desde el formulario Blazor, retorno al login SSR, cookie y lectura del perfil persistido: [evidencia](resultados/preaws-registro-ui.json).
