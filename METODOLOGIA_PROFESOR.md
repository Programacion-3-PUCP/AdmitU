# Método de programación del profesor, contrastado con código real

**Fuente:** ZIP original `1INF30-2026-2-main.zip` de `fpaz1987/1INF30-2026-2` proporcionado por el estudiante. El repositorio remoto es privado para la conexión utilizada; los ejemplos descritos se leyeron del ZIP, no de GitHub. Apuntes posteriores al parcial y diapositivas respaldan el contexto.

## Qué enseña este curso y cómo se aplica a AdmitU

Programación 3 (PUCP, 1INF30, 2026-2) integra **POO y componentes reutilizables, MySQL/JDBC, DAO/BO/transacciones, Blazor Web App en C#, servicios SOAP y REST en Java, reportes JasperReports y despliegue con GlassFish e IIS**, además de fundamentos de concurrencia. La TA vigente demanda un servidor **Java** y cliente **C#** con **REST**. Aunque SOAP fue abordado en sesiones y laboratorios, no lo introduzcas para reemplazar las rutas REST del sistema. No introducir Spring Boot, Hibernate/ORM ni otro stack que omita las capas pedidas.

## Jerarquía exacta observada en Java

Directorio del ejemplo docente: `BackendJAVA_FrontendCSharp/JAVA/softprog_solution/`.

- `softprog_model`, `softprog_dbmanager`, `softprog_dao`, `softprog_business_logic`, `softprog_services`: módulos Maven diferenciados y dependencias declaradas en archivos `pom.xml`.
- **Modelo**: entidades Java con estado/atributos, getters y setters; relaciones entre entidades. Paquetes por contexto de negocio (`rrhh`, `ventas`, `almacen`, `gestclientes`).
- **DAO/implementación**: interfaces por entidad; implementaciones ejecutan procedimientos MySQL con JDBC y `CallableStatement`. La lógica SQL pertenece aquí o a procedimientos, nunca a Razor.
- **BO**: ejemplo real `rrhh/bo/AreaBOImpl.java`: `implements IAreaBO`, instancia `AreaImpl`/`EmpleadoImpl` a través de interfaces `AreaDAO`, `EmpleadoDAO`; valida null, ID positivo, cadenas vacías y longitud; para operación compuesta (área + empleados) ejecuta inserciones, `TransactionContext.commit()`; `catch` hace `rollback()`; `finally` hace `close()`.
- **Transacciones**: `softprog_dbmanager/.../TransactionContext.java` conserva `ThreadLocal<Connection>` y ofrece `getConnection`, `commit`, `rollback`, `close`. Importante: el BO de ejemplo NO usa el bloque try/commit/rollback/finally en todos los métodos; reproducir patrón para operaciones mutantes donde corresponde y validar la atomicidad, no copiar esas omisiones como una norma.
- **DBManager**: `ResourceBundle.getBundle("db")`, `DriverManager`, `getConnection`. El ejemplo tiene `private Connection con` mutable de instancia; el patrón de configuración es relevante, pero **no copiar almacenamiento de conexión mutable compartido** en aplicaciones concurrentes.

## Servicios REST exactos

En `softprog_services/src/main/java/.../services/rest/RestApplication.java` se configura la raíz `@ApplicationPath("webresources")`. El recurso `.../services/rest/rrhh/AreaRS.java` demuestra:

- `@Path("AreaRS")`, `@Consumes(MediaType.APPLICATION_JSON)` y `@Produces(MediaType.APPLICATION_JSON)` a nivel de clase.
- `private IAreaBO boArea;`, construido mediante `new AreaBOImpl()` en el constructor; servicio delgado que **delegue** al BO.
- `@GET @Path("listarTodasAreas")` retorna `List<Area>`.
- `@POST @Path("insertarArea")` recibe `Area` como JSON y devuelve `int`.
- `@GET @Path("buscarAreaPorId/{id}")`, `@PathParam("id")`.
- `@DELETE @Path("eliminarArea/{id}")`; `@PUT @Path("modificarArea")`.
- `@GET @Path("listarAreasPorNombre")` recibe `@QueryParam("nombre")`.

**Convención estricta para AdmitU:** conservar el estilo de rutas por acción ya adoptado (`ConvocatoriaRS/listarTodasConvocatorias`, `insertarConvocatoria`, etc.). Pueden añadirse rutas especializadas sin romper las que consume el cliente actual. Usar Postman/curl y logs de GlassFish para probar JSON, estados HTTP y validaciones.

## C# Blazor y consumo REST exactos

En `BackendJAVA_FrontendCSharp/CSharp/SoftprogSolution/SoftprogWA/` el profesor separa `Servicios/REST`, modelos/dominio y páginas `.razor`. `Servicios/REST/AreaRestService.cs` encapsula `HttpClient`, rutas Java, serialización JSON y llamadas del frontend. Las vistas usan `@page`, `@inject`, `@bind`, `@onclick`, eventos, formularios, validaciones, modales y grillas. `Program.cs` configura las dependencias. Los apuntes 03/11 explican la inyección de `HttpClient` con `BaseAddress` y la encapsulación de cada REST en una clase.

**Diferencia deliberada AdmitU:** el proyecto usa `AdmitURestClient.cs` como cliente genérico y `DatosSesion.cs` como puente. No imponer una clase por entidad solo para parecerse al profesor; conservar compatibilidad, mejorar pruebas y evitar cachés compartidas entre sesiones.

## JasperReports: ejemplo completamente concreto

1. Fuente del informe: `ReportesFuente/ReporteAreas.jrxml` (Jaspersoft Studio).
2. Archivo compilado `.jasper` en `WEB-INF/reportes` para carga desde Java.
3. REST `softprog_services/.../services/rest/reportes/ReporteRS.java` expone `@Path("ReporteRS")` + `@GET @Path("listarAreas") @Produces("application/pdf")`.
4. Código relevante: `JRLoader.loadObjectFromFile(...)` → `JasperFillManager.fillReport(reporte, parametros, con)` con `Connection` MySQL → `JasperExportManager.exportReportToPdf(jp)` → `Response.ok(pdf, "application/pdf")`.
5. Alternativa docente: `.../services/servlets/Reporte.java` (`@WebServlet("/reporte")`) carga `.jasper` y escribe PDF por `JasperExportManager.exportReportToPdfStream`.
6. C# `Servicios/REST/ReporteRestService.cs`: `client.GetAsync("ReporteRS/listarAreas")`, `EnsureSuccessStatusCode`, `ReadAsByteArrayAsync()`.
7. Blazor `Components/Pages/Reportes/ReporteAreas.razor`: llama a `GenerarReporteAreas`, transforma `byte[]` en `data:application/pdf;base64,...` y lo presenta en un `<iframe>`.

**Adaptación a AdmitU:** crear `ReporteIngresos.jrxml`, `ReporteObservados.jrxml`, `ReporteVacantes.jrxml`, sus versiones compiladas o compilación segura al desplegar, y tres endpoints Java que acepten filtros/roles y sirvan PDF. No usar una tabla C# imprimible como sustituto. Reutilizar el HttpClient y las pantallas existentes. Reportes de alto valor: totales de pagos **aprobados** por convocatoria, expedientes observados, vacantes versus postulaciones **completadas** por modalidad/carrera. Filtros y sumas deben concordar con SQL real. Revisar la consulta y tipo de conexión antes de generar.

**Precaución concreta:** en el ZIP, `ReporteRS.java` aparece bajo ruta `.../services/rest/reportes/` pero declara paquete `pe.edu.pucp.softprog.services.rs`; esa inconsistencia puede ser real o deberse a la estructura del ZIP. No copiar paquetes/importaciones sin compilar. Los ejemplos son material pedagógico, no necesariamente una entrega libre de fallas.

## Autenticación, seguridad y concurrencia

Apuntes del 04/11 enseñan autenticación de Blazor con cookie, `ClaimsIdentity`, `ClaimsPrincipal`, `AuthorizeRouteView`, `Authorize(Roles=...)` y configuración de `Program.cs`. Se menciona MD5 como explicación SQL; **MD5 no es adecuado como hash seguro de contraseñas**. RNF_03 requiere un esquema moderno de hashes salados en el backend, roles efectivos y comunicación segura en despliegue. La sesión de usuario debe estar aislada; no usar `Singleton` con IDs mutables de usuario.

Apuntes del 10–11/11 y material oficial enseñan generación de reportes, WAR y publicación: Java/JDK 25 → Maven `clean/package` → WAR en GlassFish; .NET 10 → publicar frontend → IIS; infraestructura de clase en **AWS Academy EC2 Windows Server**, con firewall, Security Groups, configuración GlassFish y reglas de puertos. El ejemplo del profesor **no prueba que AdmitU esté desplegado**: el despliegue es una tarea adicional que exige acceso al entorno y validación real.

## Reglas para Codex derivadas del código

- Inspeccionar clases concretas antes de afirmar el estándar; preservar `BOI/BOImpl`, `DAO/Impl`, `CallableStatement`, `TransactionContext`, `*RS`, `HttpClient`, Razor.
- Ante defectos técnicos reales, mejorar el diseño sin cambiar la pila: conexión mutable compartida, `Singleton` de sesión, ausencia de commit en operaciones mutantes, contraseñas y estados.
- Verificar las referencias cruzadas entre los seis módulos Maven y el contexto WAR; asegurar que el frontend no use base de datos directa.
- Usar el código del profesor como **fuente privada de comparación**, no copiarlo o publicarlo completo en `Programacion-3-PUCP/AdmitU` (repositorio público). En caso de duda, implementar solución equivalente con nombres propios y comentarios útiles.
