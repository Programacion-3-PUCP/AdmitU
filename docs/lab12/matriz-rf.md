# Matriz de evidencia LAB12

Corte local: 12/11/2026, rama `codex/lab12-completo`, base `lab11-diego` en `44851f3`. Esta matriz registra operaciones observadas; la presencia de código y la compilación se distinguen de las pruebas en navegador. No certifica una nota académica ni un despliegue remoto.

La regresión REST pasó **21/21 comprobaciones**, con casos de los **15 RF**. La última ejecución visual disponible pasó **12/15 flujos**; las tres repeticiones pendientes se indican abajo. No se declara cumplimiento completo de los 15 RF mientras falten esas verificaciones. Los resultados sanitizados están en [resultados/regresion-api.json](resultados/regresion-api.json); la evidencia visual final se incorporará al cerrar la repetición.

Raíces: UI=`frontend/AdmitUWA/AdmitUWA/Components/Pages`; REST=`backend/AdmitU/admitu_services/src/main/java/pe/edu/pucp/admitu/services`. Las clases BO/DAO se ubican en sus módulos Maven. Todas las rutas REST mantienen la raíz `admitu_services/webresources`.

| RF | Responsable | UI | REST → BO | DAO / procedimiento principal | API probada | Navegador | Alcance y límite |
|---|---|---|---|---|---|---|---|
| 01 | Diego | Admin/Convocatorias.razor | ConvocatoriaRS → ConvocatoriaBOImpl | ConvocatoriaImpl / INSERTAR, MODIFICAR y ELIMINAR_CONVOCATORIA | PASS: CRUD y baja de borrador | PASS: crear y eliminar con confirmación | La baja lógica conserva relaciones; la API protege las convocatorias con postulaciones. |
| 02 | Diego | Registro.razor; PortalPostulante/MiPerfil.razor | AutenticacionRS, PostulanteRS → AutenticacionBOImpl, PostulanteBOImpl | CuentaUsuarioImpl, PostulanteImpl / INSERTAR y MODIFICAR_POSTULANTE_PERFIL | PASS: cuenta, contraseña, perfil, discapacidad y CONADIS durables | PASS: perfil y recarga | Ruta real /postulante/perfil; DNI bloqueado y teléfono persistido. |
| 03 | Diego | Admin/ReporteIngresos.razor | ReporteRS/generarIngresos → ReporteBOImpl | ReporteImpl / LAB12_REPORTE_INGRESOS | PASS: PDF real filtrado y total contrastado | PASS: visor y descarga | Solo pagos APROBADOS. El filtro de fechas corresponde a fecha_pago; no existe fecha independiente de aprobación. |
| 04 | Harold | Admin/Modalidades.razor | ModalidadRS, CatalogoRS → ModalidadBOImpl, CatalogoBOImpl | ModalidadImpl, CatalogoImpl / REACTIVAR_MODALIDAD | PASS: CRUD, baja y reactivación | PASS: crear, cancelar confirmación, reactivar y recargar | Reactivar ejecuta el SP específico, no un cambio de caché. |
| 05 | Harold | PortalPostulante/Antecedentes.razor | ArchivoDigitalRS/guardarCertificado → ArchivoBOImpl | ArchivoImpl / GUARDAR_CERTIFICADO_METADATA, GUARDAR_ARCHIVO_DIGITAL | PASS: PDF, sustitución y baja | PENDIENTE repetir visor tras recarga | Fecha futura rechazada. COLEGIO/UNIVERSIDAD se conserva y valida; 27 pruebas BO pasan. Falta repetir el visor con el WAR actualizado. |
| 06 | Harold | Admin/Carreras.razor | CarreraRS, CatalogoRS → CarreraBOImpl, CatalogoBOImpl | CarreraImpl, CatalogoImpl / REACTIVAR_CARRERA | PASS: CRUD, baja y reactivación | PASS: crear y buscar tras recarga | La búsqueda se verifica tras completar la hidratación de Blazor. |
| 07 | Richard | Admin/Requisitos.razor | RequisitoRS, RequisitoConvocatoriaModalidadRS → BO de requisitos | RequisitoImpl, RequisitoConvocatoriaModalidadImpl / SP de requisito y asociación | PASS: requisito obligatorio, convocatoria/modalidad y oferta | PENDIENTE repetir interacción | Se ajusta la espera de hidratación del test; el envío del expediente verifica los requisitos obligatorios y sus formatos/tamaños. |
| 08 | Richard | PortalPostulante/DocumentosPersonales.razor | ArchivoDigitalRS → ArchivoBOImpl | ArchivoImpl / GUARDAR, BUSCAR y ELIMINAR_ARCHIVO_DIGITAL | PASS: bytes, descarga idéntica, formatos, fecha y baja | PASS: DNI/foto, recarga y previsualización | JPG/PNG hasta 5 MB; MySQL MEDIUMBLOB y SHA-256. No se confunde otra instancia/recarga con reinicio completo del servidor. |
| 09 | Richard | PortalEvaluador/ReporteObservados.razor | ReporteRS/generarObservados → ReporteBOImpl | ReporteImpl / LAB12_REPORTE_OBSERVADOS | PASS: PDF real filtrado y datos contrastados | PASS: visor y descarga | Cada fila es una observación PENDIENTE; un expediente puede ocupar varias filas. |
| 10 | Francis | PortalEvaluador/Observaciones.razor | DocumentoObservacionRS → DocumentoObservacionBOImpl | DocumentoObservacionImpl / SP de observaciones | PASS: registrar, consultar y modificar | PASS: observación y alcance visible | Mis expedientes filtra por evaluador asignado o autor de la observación; Todos amplía el conjunto. |
| 11 | Francis | PortalPostulante/Vouchers.razor; Postular.razor | ArchivoDigitalRS/guardarVoucher, PostulacionRS/enviarPostulacion → ArchivoBOImpl, PostulacionBOImpl | ArchivoImpl, PostulacionImpl / GUARDAR_VOUCHER_METADATA y envío compuesto | PASS: bytes y doble envío con un pago/voucher | PASS: wizard, documentos y voucher | El importe procede de convocatoria/modalidad. Un pendiente puede corregirse/retirarse; un aprobado queda protegido. |
| 12 | Francis | PortalEvaluador/ValidacionVouchers.razor | PagoRS → PagoBOImpl | PagoImpl / MODIFICAR_PAGO | PASS: aprobar con fecha y rechazar modificación posterior | PASS: aprobar desde pantalla | Se exige voucher durable; aprobar/retirar concurrentemente no permite perder el archivo aprobado. |
| 13 | Matías | Admin/HistorialEstados.razor | PostulacionRS, PostulacionHistorialRS → BO de postulación e historial | PostulacionImpl, PostulacionHistorialImpl / MODIFICAR_POSTULACION | PASS: transición e historial durable | PASS: consultar con filtro de convocatoria | El historial se guarda en la misma transacción SQL, sin segunda inserción desde C#. |
| 14 | Matías | Admin/Carnes.razor | CarnePostulanteRS → CarnePostulanteBOImpl | CarnePostulanteImpl / INSERTAR, MODIFICAR y ELIMINAR_CARNE_POSTULANTE | PASS: inelegible rechazado, emisión, búsqueda y anulación | PENDIENTE repetir tras corrección de estados | Se corrigió la equivalencia entre IDs SQL y estados Blazor antes de aprobar esta pantalla. La emisión exige APTA/pago aprobado y completa la inscripción; la anulación conserva la fila, código y titular. |
| 15 | Matías | Admin/ReporteVacantes.razor | ReporteRS/generarVacantes → ReporteBOImpl | ReporteImpl / LAB12_REPORTE_VACANTES | PASS: PDF real y conteo FINALIZADA contrastado | PASS: visor y descarga | El cupo es compartido por carrera/convocatoria; no se duplica al sumar modalidades ni se inventan reservas. |

## RNF y despliegue

| Requisito | Evidencia | Estado |
|---|---|---|
| RNF01 comunicación distribuida | Blazor y GlassFish en procesos distintos; JSON/PDF por HTTP y datos en MySQL | PROBADO local |
| RNF02 catálogo ≤3 s | Una lectura 0.033 s; 20 concurrentes p95 0.333 s | PROBADO en esos escenarios; no generalizar a 500 |
| RNF03 seguridad | PBKDF2 salado, roles/propietario, 17/17 API y 6/6 Chromium | PROBADO local; HTTPS público pendiente |
| RNF04 500 postulantes simultáneos | 500 GET públicos, 500 HTTP200, p95 8.308 s; cero identidades autenticadas | PARCIAL: no se probaron 500 trámites completos |
| RNF05 atomicidad | Fallo intermedio revierte postulación/pago; contención, archivos y carné en 27 pruebas BO/MySQL | PROBADO en los casos ejecutados |
| AWS Academy / IIS | Scripts y procedimiento preparados; sin cuenta/credenciales conectadas ni Windows/IIS en el entorno | BLOQUEADO POR ACCESO; no se declara online |

Ver [evidencias](evidencias.md), [incidencias](incidencias.md), [reportes](reportes.md), [seguridad](seguridad.md), [archivos y reglas](archivos-reglas.md) y [despliegue](../despliegue/README.md).
