# Plan integral de LAB12 — AdmitU | 18/11/2026

## Objetivo y autorización

**Meta de ejecución: 15/15 RF (100%)** con Java/Jakarta EE 10/GlassFish + C# Blazor .NET 10 vía REST y MySQL. La consigna mínima del miércoles 18 de noviembre es 90%: **14/15**, no 13/15; la meta del propietario es completar los 15, RNF razonables, correcciones LAB11, pruebas y despliegue AWS. Codex debe implementar autónomamente por etapas en nueva rama `codex/lab12-completo`, registrar decisiones, abrir PR a `lab11-diego` sin merge a `master` y entregar evidencias. Autorización para reconstruir **solo `admitu_db` de prueba propia** con host/verificación y respaldo previo cuando sea posible; acceso AWS únicamente si herramientas/credenciales existentes lo permiten, sin costos abiertos.

## Punto de partida objetivo

LAB11 obtuvo 18.5/20. Informe `TA_Lab11_Informe_LosFavoritosDeCueva`: **11/15 RF integrados** (autodeclarado), 27 tablas/131 SP (autodeclarado), tres reportes visuales sin JasperReports (RF_03, RF_09, RF_15), documentos personales RF_08 en memoria, login demostrativo sin validar credenciales. El informe es contexto, **no prueba automática**. La rama `lab11-diego` es fuente de verdad y requiere `mvn clean package`, `dotnet build`, SQL, REST y UI para validar.

### Matriz inicial de los 15 RF

Estado `INFORME LAB11` = declaración de cobertura, no hecho probado. `REVISAR` = no ha sido probado en la sesión de preparación.

| RF | Dueño según catálogo | Objetivo obligatorio / entregable verificable | Estado previo | Evidencia y componente de arranque |
|---|---|---|---|---|
| RF_01 | Diego | Admin registra, busca, edita, desactiva/elimina lógicamente convocatoria; puede depurar borradores; estados consistentes | Declarado integrado, observaciones | `ConvocatoriaRS.java`; `Admin/Convocatorias.razor` |
| RF_02 | Diego | Postulante registra/consulta/actualiza perfil con validación de DNI/correo | Declarado integrado | `PostulanteRS.java`; `PortalPostulante/MiPerfil.razor` |
| RF_03 | Diego | Jasper PDF ingresos por vouchers APROBADOS por convocatoria; cálculos y filtros correctos | PENDIENTE Jasper | `Admin/ReporteIngresos.razor`; ejemplo privado `ReporteRS.java` docente |
| RF_04 | Harold | CRUD modalidad y baja lógica reversible | Declarado integrado | `ModalidadRS.java`; `Admin/Modalidades.razor` |
| RF_05 | Harold | Antecedentes académicos: certificados durables, visualización, modificación y eliminación; fechas válidas | Declarado integrado, auditar bytes/fechas | `AntecedenteAcademicoRS.java`; `PortalPostulante/Antecedentes.razor` |
| RF_06 | Harold | CRUD carreras y baja lógica | Declarado integrado | `CarreraRS.java`; `Admin/Carreras.razor` |
| RF_07 | Richard | Requisitos por convocatoria-modalidad y documentos exigidos | Declarado integrado | `RequisitoConvocatoriaModalidadRS.java`; `Admin/Requisitos.razor` |
| RF_08 | Richard | DNI+foto JPG/PNG: carga de bytes, visualización, actualización, eliminación, conservación tras reinicio | **PENDIENTE persistencia** | `PortalPostulante/DocumentosPersonales.razor`, `ZonaCarga.razor` almacena solo nombre |
| RF_09 | Richard | Jasper PDF expedientes observados con filtros/datos reales | PENDIENTE Jasper | `PortalEvaluador/ReporteObservados.razor` |
| RF_10 | Francis | CRUD observaciones, tipificación; filtro visible y efectivo | Declarado integrado, observación filtro | `DocumentoObservacionRS.java`; `PortalEvaluador/Observaciones.razor` |
| RF_11 | Francis | Voucher digital de transferencia externa durable y sin duplicar pendientes | Declarado integrado, auditar bytes | `PagoRS.java`; `PortalPostulante/Vouchers.razor` |
| RF_12 | Francis | Validación voucher: PENDIENTE→APROBADO/RECHAZADO, fecha adecuada y bloqueo de acciones | Declarado integrado | `PagoRS.java`; `PortalEvaluador/ValidacionVouchers.razor` |
| RF_13 | Matías | Historial de transiciones real, búsquedas e integridad temporal | Declarado integrado | `PostulacionHistorialRS.java`; `Admin/HistorialEstados.razor` |
| RF_14 | Matías | Carné: generación, búsqueda y anulación de credencial para aptos | Declarado integrado | `CarnePostulanteRS.java`; `Admin/Carnes.razor` |
| RF_15 | Matías | Jasper PDF vacantes vs postulaciones COMPLETADAS por modalidad/carrera | PENDIENTE Jasper | `Admin/ReporteVacantes.razor` |

Para cada RF generar registro de: archivo Java RS, BO, DAO, stored procedure, Razor, operación REST, validaciones, prueba en DB, resultado HTTP, recarga de app, prueba negativa y estado: `SIN CÓDIGO / CÓDIGO / COMPILA / API / E2E / APROBADO`. **No contar como completo** el estado `CÓDIGO`.

## Retroalimentación LAB11: correcciones obligatorias

| ID | Observación | Comportamiento esperado / prueba de aceptación | Prioridad |
|---|---|---|---|
| O1 | Reactivar sin confirmación | Cualquier Reactivar abre diálogo antes de cambiar estado; Cancelar no modifica BD | Alta |
| O2 | Íconos desalineados | Alineación uniforme de botones/acciones en tablas y vistas responsivas | Media |
| O3 | Doble envío de postulación | Doble clic y dos POST simultáneos producen 1 postulación y como máximo 1 pago pendiente por (postulante, convocatoria) | Crítica |
| O4 | Antecedentes fecha futura | No aceptar fecha de registro posterior a hoy (UI, BO y/o BD); considerar que el modelo actual usa Hoy | Alta |
| O5 | Campos deshabilitados inconsistentes | Mismo estilo, `disabled/readonly` apropiado, ayuda coherente y validación servidor | Media |
| O6 | Filtro de Observaciones confuso | Mostrar opción `Mis expedientes / Todos`; selección filtra el dataset correcto, o retirar el control si no aplica | Alta |
| O7 | Varias postulaciones en misma convocatoria | Regla de negocio única por POSTULANTE y CONVOCATORIA, independientemente de carrera; UI+BO+restricción única/guardas concurrentes | Crítica |
| O8 | Borradores de convocatoria se acumulan | Acción de eliminar/desactivar convocatoria borrador, con confirmación y consideración de claves foráneas | Alta |

### Aceptación fuerte de unicidad

- Caso A: mismo postulante + misma convocatoria + misma carrera, clic dos veces → 1 registro.
- Caso B: mismo postulante + misma convocatoria + carrera distinta → rechazado, 1 registro.
- Caso C: dos peticiones concurrentes con idéntico (postulante, convocatoria) → 1 registro, otro error de negocio controlado; **no** 2 pagos.
- Caso D: mismo postulante + otra convocatoria → permitido si fechas/estado habilitan.
- Caso E: dos postulantes + misma convocatoria → permitidos.
- Restaurar/recrear BD desde script debe conservar restricción y procedimientos correctos. Antes de alterar tabla, auditar los datos de prueba y tratar duplicados.

## RNF: cinco pruebas separadas, no confundir con conteo de RF

- **RNF_01**: Java y C# en procesos desplegados distintos, mensajes HTTP REST JSON, ninguna consulta BD directa desde C#.
- **RNF_02**: medir listado de carreras bajo escenario especificado y verificar respuesta <= 3 s; registrar herramienta/cantidad/entorno.
- **RNF_03**: proteger contraseñas con hash apropiado y sal; roles reales y autorización backend, sin credenciales en URL ni en repo; HTTPS donde corresponda. El login actual solo elige un rol/correo: auditar diseño de cuentas. No copiar MD5 del ejemplo histórico como seguridad final.
- **RNF_04**: 500 postulantes simultáneos: probar con carga realista y analizar conexiones/recursos; no afirmar cumplimiento sin prueba y capacidad de infraestructura. Corregir `DatosSesion` singleton y `DBManager` mutable; medir con resultados y límites.
- **RNF_05**: registrar postulación+documentos+pago de forma atómica; provocar fallo intermedio y verificar `rollback`; usar `TransactionContext` en BO consistentemente.

## Orden de trabajo autónomo

**Fase 0 — Baseline (sin bloquear la ejecución):** `git status`, rama derivada `lab11-diego`, inventario de proyectos, dependencias y credenciales existentes sin leerlas en logs; ejecutar `mvn -version`, `dotnet --info`, builds. Crear `docs/lab12/diagnostico.md` y matriz RF inicial.

**Fase 1 — Correcciones LAB11:** O1–O8, en especial doble envío y unicidad por convocatoria. Preservar UX, datos existentes, dominio y endpoints. Pruebas de competencia/concurrencia en BD, validaciones frontend/BO y confirmaciones.

**Fase 2 — Persistencia de archivos:** primero RF_08, revisar RF_05 y RF_11. `InputFile` debe leer bytes reales con límites; servidor validar extensión, MIME, tamaño y tipo por firma donde aplique; almacenamiento persistente con referencias estables, eliminación/descarga autenticada. Elegir solución simple que se pueda desplegar en GlassFish/MySQL según curso. **No considerar guardar solo `f.Name` como carga completa**.

**Fase 3 — Reportes Jasper:** RF_03 → RF_09 → RF_15 o paralelizar cuando sea seguro; añadir archivos `.jrxml` y `.jasper`/compilación de reporte; Java retorna bytes `application/pdf`, C# consume bytes con `HttpClient` y UI permite previsualizar/descargar. Filtros por convocatoria, sumas y agrupamientos deben coincidir con MySQL. Ver ejemplos profesor en `METODOLOGIA_PROFESOR.md`.

**Fase 4 — Seguridad, transacciones, sesiones y RNF:** autenticación real si es viable con esquema actual; usuario/rol desde credenciales, no por selector arbitrario; proteger acceso a endpoints; arreglar singleton/manejo connection; resolver control de errores REST sin eliminar UI.

**Fase 5 — Pruebas y hardening:** compilar Maven reactor con Java 25 y .NET 10; pruebas de procedimientos SQL, endpoint por CRUD, los 15 flujos UI E2E, 8 observaciones, credenciales de prueba en archivo no público, 3 PDFs Jasper, límites de 3 s y 500 concurrentes con reporte honesto si la infraestructura no soporta la carga. Guardar evidencias resumidas (HTTP status y data ficticia) en `docs/lab12/evidencias.md`.

**Fase 6 — Despliegue AWS:** según apuntes 10–11/11 y PAIDEIA: `mvn clean package` en raíz, WAR `admitu_services.war` a GlassFish; `dotnet publish` a IIS Windows Server de EC2, configurando `ApiSettings:BaseUrl` para el servidor, JDK 25, .NET 10 hosting bundle, reglas Security Group/firewall y servicios necesarios. Preferir la infraestructura AWS Academy existente del responsable (no nueva cuenta). Desplegar solo en recursos de laboratorio autorizados, proteger credenciales, limitar exposición pública, activar HTTPS/proxy según viabilidad; registrar URL pública y prueba real; **si no hay AWS/GlassFish/IIS accesible, producir scripts/instrucciones sin afirmar éxito**.

**Fase 7 — Entregables y PR:** informe de 15 RF con estado y evidencias, correcciones, diagrama BD actualizado, script SQL versionado y de recreación de prueba, instrucciones de instalación, rutas, cuentas de demo entregadas **por canal privado**, lista de cambios; `git diff --check`; PR `codex/lab12-completo` → `lab11-diego`, no `master`. Registrar qué falta y riesgos reales.

## Checklist de validación que Codex debe ejecutar

```bash
# desde la raíz del repositorio
cd backend/AdmitU && mvn -B clean package
# luego
cd ../../frontend/AdmitUWA/AdmitUWA && dotnet build
# producción Windows en el servidor EC2, una vez configurado IIS:
# dotnet publish -c Release -o publish
```

La última ruta de `dotnet` en el ejemplo requiere comprobar el cwd, no copiar comandos a ciegas. Para despliegue usar el `pom` final y `appsettings` real. Al crear scripts, añadir `set -e`/manejo de errores, verificaciones de entorno y salida sin secretos.

## Riesgos / contradicciones a resolver por evidencia

1. LAB11 informa 11 integrados, pero la integración REST puede seguir usando cachés demo en algunos flujos y nombres de archivo en lugar de bytes. No falsear porcentaje.
2. Hay 4 pendientes declarados: RF_03/08/09/15. Aun si todos los demás están operativos, 14/15 requiere 3 mejoras; meta completa implica los 4.
3. La creación `EnviarNuevaPostulacion` y el check `Postular.razor` no garantizan unicidad por postulante+convocatoria con concurrencia. Backend/BD debe ser autoridad.
4. `DatosSesion` singleton contiene IDs actuales y listas; cambiar solo lifetime puede provocar errores de inicialización/cachés y requiere pruebas de varias sesiones.
5. `DBManager.getInstance()` guarda `con` como atributo mutable; eliminar conexión compartida entre hilos, preservando `TransactionContext`.
6. SQL en repo incluye `DROP DATABASE`; reconstruir exclusivamente `admitu_db` de prueba con host y esquema confirmados. No atacar datos ajenos.
7. AWS Academy puede no estar accesible desde Codex; producir comandos/scripts replicables para Windows, GlassFish/IIS y EC2; no publicar claves `.pem`, RDS o conexiones.
8. ZIP privado del profesor contiene configuraciones `db.properties` reales: conservar solo documentación y ejemplos sanitizados fuera del repo público; no se necesita publicar el ZIP para que Codex programe AdmitU.
