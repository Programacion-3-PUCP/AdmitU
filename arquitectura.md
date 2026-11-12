# AdmitU — arquitectura técnica, convenciones del curso y estado comprobable de LAB11

**Fecha de corte:** 11/11/2026. **Base:** `Programacion-3-PUCP/AdmitU`, rama `lab11-diego` (código consultado); informe, guion y reporte de LAB11 en Drive; apuntes de segunda parte y diapositivas oficiales de PAIDEIA; sílabo 1INF30. **No es un reporte de ejecución:** no se ejecutó GlassFish/MySQL/Blazor desde este análisis.

## A. Propósito y límites del curso

Programación 3 (1INF30) enseña el desarrollo práctico de aplicaciones distribuidas: modelos orientados a objetos, bibliotecas/componentes, JDBC y DAO, lógica de negocio, interfaces de usuario, SOAP y REST, conexión Java/C#, JasperReports, despliegue y concurrencia. La tarea grupal aplica dichos conceptos a una **aplicación web Java (servidor) + C# (cliente) conectada vía servicios web**. El trabajo debe poder compilarse, explicarse y demostrar su funcionamiento frente al docente/JP. La unidad de concurrencia existe en el sílabo, pero no sustituye los RF establecidos ni implica introducir plataformas nuevas en LAB12.

**Tecnologías efectivamente especificadas o utilizadas:** Java 25 en el POM del equipo; Maven multimódulo; Jakarta EE 10/Jakarta REST; GlassFish como servidor de aplicaciones; JDBC + `CallableStatement`; MySQL y procedimientos almacenados; C#/.NET 10/Blazor Web App y `HttpClient`; JasperReports/Jaspersoft Studio para los reportes; Git/GitHub. AWS EC2 + IIS se enseñó para despliegue, no es requisito añadido por esta nota para el 18/11.

El sílabo lista las unidades: 1 Introducción; 2 POO; 3 Componentes reutilizables; 4 Acceso a BD; 5 Fundamentos web; 6 Interfaces gráficas; 7 Programación distribuida (SOAP, REST, despliegue); 8 Reportes; 9 Concurrencia; 10 Presentación de TA. **Evitar tecnologías de moda fuera de este marco salvo aprobación expresa.**

## B. Trazado real de la solución

```text
Navegador del usuario
  └─ AdmitUWA (Blazor Web App, C#/.NET 10)
       ├─ Components/Pages/Admin/
       ├─ Components/Pages/PortalPostulante/
       ├─ Components/Pages/PortalEvaluador/
       ├─ Services/DatosSesion.cs                  [estado/carga y orquestación]
       └─ Services/REST/AdmitURestClient.cs        [DTO + HttpClient]
             │ HTTP / JSON [para reportes: PDF desde Java, aún pendiente]
             ▼
      GlassFish / WAR `admitu_services`
       ├─ RestApplication.java @ApplicationPath("webresources")
       └─ clases *RS.java (Jakarta REST)             [rutas + delegación]
             ▼
       admitu_business_logic/ (BOI/BO)              [validaciones y transacciones]
             ▼
       admitu_dao/ (interfaces y implementaciones)  [JDBC/CallableStatement]
             ▼
       admitu_dbmanager/                            [conexión/TransactionContext]
             ▼
       MySQL `admitu_db`                             [tablas, SP, pruebas]
```

La existencia de capas y endpoints no garantiza que la persistencia sea completa. Algunos flujos en C# siguen utilizando datos/colecciones en memoria. Por ejemplo, los archivos personales actualmente no se transmiten a un almacenamiento persistente.

### Árbol validado

```text
AdmitU-Git/
├── backend/AdmitU/
│   ├── pom.xml                      [packaging pom; reactor Java 25]
│   ├── admitu_domain/
│   ├── admitu_dbmanager/
│   ├── admitu_dao/
│   ├── admitu_business_logic/
│   ├── admitu/                      [módulo de ejecución]
│   └── admitu_services/
│       ├── pom.xml                  [WAR; finalName admitu_services]
│       └── src/main/java/pe/edu/pucp/admitu/services/
│           ├── RestApplication.java
│           ├── ConvocatoriaRS.java, PostulanteRS.java, PagoRS.java, ...
│           └── [21 clases de recurso *RS, más RestApplication]
├── frontend/AdmitUWA/AdmitUWA/
│   ├── AdmitUWA.csproj
│   ├── Program.cs
│   ├── appsettings.json
│   ├── Services/REST/AdmitURestClient.cs
│   ├── Services/DatosSesion.cs
│   ├── Components/Pages/Admin/
│   ├── Components/Pages/PortalPostulante/
│   └── Components/Pages/PortalEvaluador/
└── database/
    └── TA_Lab11_Script_LosFavoritosDeCueva.sql
```

El informe LAB11 señala **27 tablas y 131 procedimientos almacenados**. Son cifras **documentadas por el equipo**, no un recuento independiente ejecutado sobre MySQL en este análisis. El código contiene el único script SQL visible en el directorio `database/` de la rama consultada; comienza con `DROP DATABASE IF EXISTS admitu_db`.

## C. Método docente específico (fuentes: apuntes de segunda parte, diapositivas y ejemplos de clase)

> **Procedencia:** esta sección describe el método **observado en los apuntes del 20/10, 21/10, 27/10, 28/10, 03/11, 04/11, 09/11, 10/11 y 11/11** y materiales REST/Blazor/Jasper. No afirma haber inspeccionado el repositorio `fpaz1987/1INF30-2026-2` (enlace retornó 404 desde el conector).

| Etapa | Procedimiento enseñado | Implicación para AdmitU |
|---|---|---|
| Modelo | POJOs Java, clases/relaciones, interfaces y enums | Respetar `admitu_domain` y los contratos JSON de sus objetos |
| Persistencia | JDBC, DAO por entidad, interfaces DAO, `CallableStatement` y SP con IN/OUT | Corregir consulta y SP en DAO, no meter SQL en Razor ni en el endpoint |
| Negocio | BOI + BOImpl; validaciones, propagación de errores, transacciones `commit/rollback` | La regla “una postulación por convocatoria” y las fechas deben vivir en servidor |
| API | Proyecto Jakarta EE Maven con WAR, `Application`, `@ApplicationPath`, `@Path`, verbos HTTP, JSON | Conservar contratos REST actuales y probar con GET/Postman |
| Cliente | Blazor Web App .NET 10, `Program.cs` registra `HttpClient`, clases REST y DTO; `@inject`, `@bind`, `@onclick`, formularios, tabla/QuickGrid, `NavigationManager` | Reusar `AdmitURestClient`, no repetir llamadas y no hacer SQL en C# |
| Reportes | Jaspersoft Studio: JRXML, campos, parámetros, expresiones, variables; compilación `.jasper`, uso de JasperReports para PDF; REST/Servlet Java | Generar PDF en Java alimentado por datos reales y exponerlo al frontend |
| Diagnóstico | GlassFish logs `domains/domain1/logs/server.log`, contexto del WAR, Postman y browser | Aislar si el error es JDBC/BO/REST o C#; mostrar trazas apropiadas |
| Despliegue | Maven `package` WAR, GlassFish, `.NET publish`, IIS/EC2 | Conservar guía, no imponer despliegue como nuevo RF del LAB12 |

**Particularidades de apuntes:** 27/10 se desarrolla SOAP `@WebService`, WSDL y referencia WCF C#. El 28/10 se pasa a REST, `@Path`, `@GET/@POST/@PUT/@DELETE`, JSON y llamada C#; el 03/11 se refactoriza al `HttpClient` compartido y clase de servicio REST; 04/11 se estudia login/cookies/roles; 09/11 y 11/11 se practica diseño Jasper; 10/11 y 11/11 se muestra deployment AWS EC2/GlassFish/IIS. En el proyecto TA actual **REST es obligatorio** y SOAP se considera antecedente didáctico, no reemplazo.

Las notas de clase mencionan MD5 como demostración de hash en SQL; **no considerarlo una solución de seguridad para contraseñas**. RNF_03 exige protección de contraseñas. Usar hash de contraseñas apropiado y validar diseño de cuentas, sin exponer clave en la URL.

## D. Evidencia puntual en el código actual (`lab11-diego`)

1. `backend/AdmitU/pom.xml`: seis módulos `admitu_domain`, `admitu_dbmanager`, `admitu_dao`, `admitu_business_logic`, `admitu`, `admitu_services`; `maven.compiler.source/target = 25`.
2. `admitu_services/pom.xml`: `packaging=war`, `finalName=admitu_services`, `jakarta.jakartaee-api 10.0.0`.
3. `RestApplication.java`: `@ApplicationPath("webresources")`.
4. `ConvocatoriaRS.java`: `@Path("ConvocatoriaRS")`, JSON y cinco métodos explícitos con `GET/POST/PUT/DELETE`; instancia un `IConvocatoriaBO` a través de `ConvocatoriaBOImpl`. Patrón similar en `DocumentoPostulacionRS` y `PagoRS`.
5. `frontend/.../Program.cs`: configura BaseUrl `ApiSettings:BaseUrl` con fallback `http://localhost:8080/admitu_services/webresources/`; registra **`HttpClient` singleton, `AdmitURestClient` singleton y `DatosSesion` singleton**. Riesgo de compartir estado de postulante/evaluador entre sesiones; no alterar sin prueba de dependencias.
6. `AdmitURestClient.cs`: DTOs C# alineados al JSON-B Java; punto de integración HTTP.
7. `DatosSesion.cs`: `CargarDesdeBackend()` realiza tareas de carga de múltiples entidades REST; también mantiene `DatosDemo` y colecciones para UI, las sincronizaciones deben auditarse por RF. `IniciarSesionComo` permite escoger rol por correo sin validar contraseña: **login demostrativo**.
8. `PortalPostulante/Postular.razor`: al continuar valida duplicidad por `PostulanteId + ConvocatoriaId + CarreraId`, pero LAB11 especifica `PostulanteId + ConvocatoriaId`, **independientemente de carrera**. Al enviar invoca `Datos.EnviarNuevaPostulacion` y navega a `/postulante/vouchers`; la redirección por sí sola no asegura idempotencia del servidor.
9. `PortalPostulante/DocumentosPersonales.razor`: en `Guardar()` agrega o cambia `Datos.DocumentosPersonales`; en `Eliminar()` quita de esa lista. No hay envío REST de contenido de archivo en ese componente. Por tanto RF_08 **no está completo** según el informe y el código localizado.
10. `PortalPostulante/Antecedentes.razor`: registra archivo por nombre y guarda `Fecha = Datos.Hoy`; validar si el certificado físico se persiste y es recuperable. El JP exige no aceptar fechas futuras; si solo se asigna Hoy, probar que ninguna otra ruta permite fechas futuras.
11. `Admin/ReporteIngresos.razor`, `PortalEvaluador/ReporteObservados.razor`, `Admin/ReporteVacantes.razor`: componen reportes/agrupaciones directamente con `DatosSesion`; aún no se observó creación de un PDF Jasper. El informe LAB11 declara tres reportes **pendientes de JasperReports**.
12. `Admin/Convocatorias.razor`: existen crear, editar, publicar, cerrar y configuración de ofertas; hay `ConfirmDialog` para publicar/cerrar, pero no se identificó en esta revisión un botón de **eliminar convocatoria borrador**. El JP lo solicita. Verificar procedimientos y relaciones antes de agregar baja lógica.
13. `PortalEvaluador/Observaciones.razor`: hay filtros de estado y `soloMias`; comparar el estado de selección visible y la lista renderizada (observación de LAB11).
14. SQL `TA_Lab11_Script_LosFavoritosDeCueva.sql`: recrea la base desde cero al inicio. **No aplicarlo sobre RDS de producción / BD compartida.**

### Riesgos transversales reales que el diagnóstico debe resolver

- **Estado por usuario:** `DatosSesion` singleton conserva `PostulanteActualId` / `EvaluadorActualId` mutable. Dos usuarios simultáneos pueden compartir selección/estado. Analizar paso a paso si cambiar a scoped y cómo afecta la carga, cachés, notificaciones y navegación.
- **Postulación concurrente:** el control de duplicados por carrera permite otra carrera dentro de la misma convocatoria. Incluso corregido en UI, se necesita defensa en BO/BD/transacción para evitar doble clic y peticiones simultáneas.
- **Errores en cadena:** `CargarDesdeBackend()` usa `Task.WaitAll` de muchas llamadas; fallo de un endpoint puede impedir inicializar toda la aplicación. Evaluar errores controlados sin sustituir silenciosamente datos reales por demo.
- **Archivos:** el nombre del fichero no equivale a bytes durables; comprobar RF_05/08/11, tamaños, tipos, persistencia, descarga y permisos. Si la propuesta de almacenamiento requiere infraestructura fuera del curso, presentar opción sencilla dentro de Java/MySQL/servidor y su costo.
- **Reportes:** generar un PDF Java Jasper genuino a partir de MySQL (filtro por convocatoria); exponerlo mediante endpoint/servlet y visualizar/descargar desde Blazor; probar tanto contenido como formato.
- **Seguridad:** pantalla de acceso ilustrativa, no autorización real, passwords sin tratamiento comprobado y roles no garantizados en REST. RNF_03 pendiente; no exponer servicios sensibles sin controles.
- **Transacciones:** el informe del equipo dice usar `TransactionContext`; verificar que todas las modificaciones compuestas gestionen transacción de forma consistente (no basta que un BO tenga `commit`).

## E. Trazabilidad inicial de módulos por RF (no confundir presencia con cumplimiento)

| RF | Dominio/UI localizados o previstos | Qué debe demostrar el diagnóstico |
|---|---|---|
| 01 | `ConvocatoriaRS`, `Admin/Convocatorias.razor` | CRUD, baja lógica, opción eliminar borradores, validación de estados |
| 02 | `PostulanteRS`, `PortalPostulante/MiPerfil.razor`, `Registro.razor` | Registro/modificación persistente, restricciones DNI/correo |
| 03 | `Admin/ReporteIngresos.razor` | PDF Jasper por convocatoria y datos reales de pagos aprobados |
| 04 | `ModalidadRS`, `Admin/Modalidades.razor` | CRUD y baja lógica sin perder referencias |
| 05 | `AntecedenteAcademicoRS`, `PortalPostulante/Antecedentes.razor` | Certificado físico/fecha válida/persistencia |
| 06 | `CarreraRS`, `Admin/Carreras.razor` | CRUD, búsqueda y baja lógica |
| 07 | `RequisitoRS`, `RequisitoConvocatoriaModalidadRS`, `Admin/Requisitos.razor` | CRUD requisitos por convocatoria-modalidad |
| 08 | `PortalPostulante/DocumentosPersonales.razor` | Carga real DNI y foto, formatos JPG/PNG y recuperación desde otra sesión |
| 09 | `PortalEvaluador/ReporteObservados.razor` | PDF Jasper de expedientes observados |
| 10 | `DocumentoObservacionRS`, `PortalEvaluador/Observaciones.razor` | Registrar/modificar/tipificar, filtros visibles y efectivos |
| 11 | `PagoRS`, `PortalPostulante/Vouchers.razor` | Voucher externo persistido, sin duplicación indebida |
| 12 | `PagoRS`, `PortalEvaluador/ValidacionVouchers.razor` | Aprobar/rechazar con fecha/estados/reglas |
| 13 | `PostulacionHistorialRS`, `EstadoPostulacionRS`, `Admin/HistorialEstados.razor` | Historial real, transiciones y rastreo |
| 14 | `CarnePostulanteRS`, `Admin/Carnes.razor` | Emisión/búsqueda/anulación con reglas de elegibilidad |
| 15 | `Admin/ReporteVacantes.razor` | PDF Jasper agrupado por modalidad y carrera, vacantes vs completadas |

## F. Reproducción, evidencias y artefactos

### Ruta HTTP de prueba indicada en guion LAB11

```text
http://localhost:8080/admitu_services/webresources/ConvocatoriaRS/listarTodasConvocatorias
```

**No afirmar que esa URL respondió ahora**: figura en el guion de exposición; verificar con GlassFish encendido y conexión MySQL funcional. Los formularios Blazor pueden abrirse con su URL local según `launchSettings.json`.

### Procedimiento mínimo de comprobación

1. Leer `pom.xml`, `AdmitUWA.csproj`, `appsettings.json` (sin divulgar secretos) y `.gitignore`.
2. Confirmar Java 25, Maven, MySQL, GlassFish y .NET 10 en el entorno, o anotar impedimento.
3. Compilar Java desde `backend/AdmitU`: `mvn clean package` (puede requerir JDK 25); compilar cliente desde la raíz del `.csproj`: `dotnet build`.
4. En BD **de prueba aislada** aplicar script solo si está autorizado a recrearla. Comparar número real de tablas y SP con el informe LAB11 y probar lectura/escritura/rollback.
5. Desplegar WAR en GlassFish; probar HTTP GET JSON y métodos POST/PUT/DELETE con datos de prueba; observar logs.
6. Arrancar Blazor; verificar navegación por roles, persistencia tras recargar y no duplicación en doble clic.
7. Generar reportes PDF con Jasper y confirmar encabezado, filtros, totales y resultado de consultas.
8. Guardar evidencias por RF, sin usuarios/contraseñas reales.

### Entregables y autorización

El JP en LAB11 exigió en PAIDEIA: Java servidor, C# cliente, script SQL, diagrama BD, catálogo marcado por RF, reporte de avance y credenciales de prueba si se implementó login. Antes de LAB12 verificar la consigna vigente: **no asumir que los artefactos exactos de LAB11 se repiten automáticamente**, aunque preparar versiones actualizadas resulta prudente.

## G. Fuentes consultadas y límites de certeza

- Sílabo oficial: `1INF30-2026-2-SILABO.PDF`, carpeta Programacion3.
- Apuntes posteriores al parcial (Drive):
  - 20/10: https://docs.google.com/document/d/1gCRenYQfYGxkqdA-lp3X9wg9v95cctdpZHmUTPXtquQ/edit
  - 21/10: https://docs.google.com/document/d/10la1ClouFKTtHm0NbwbSU5tIXqfYn0oMr7koMTGZOjg/edit
  - 27/10: https://docs.google.com/document/d/1_bj9TTN64Cm40ULWfH9yF2v1VnVYv3X9HYx_2kkB3Ug/edit
  - 28/10: https://docs.google.com/document/d/1erLIZ7eN0zaWaYMCl0vd18_ZLKJSD9amK3YsdtkYRJk/edit
  - 03/11: https://docs.google.com/document/d/1z4FFCFreLpIDSWA0EfioJQKwPCgJFRePhgqtFgQaJuY/edit
  - 04/11: https://docs.google.com/document/d/1WTVT1UMWxvOUpkDYakd4j9x6XeSdEmcmYKfv4Y2vXPI/edit
  - 09/11: https://docs.google.com/document/d/1EJOhJ_5F3uS-6WXUShAHr9PnqOz6GmH1yZ4Nh0vsMOc/edit
  - 10/11: https://docs.google.com/document/d/1D1BInDCB5PF_64FW2jJUL5yrJjkWrJxNysmDtkyjfWM/edit
  - 11/11: https://docs.google.com/document/d/1RRgdEfIPdL__kkrWdNuvPIxXK3M4eivYX8mP84vNRmc/edit
- Material PAIDEIA: “Creación de Servicios Web RESTFul con Jakarta REST”, “Desarrollo de Servicios Web SOAP con GlassFish e IntelliJ”, “Desarrollo de Aplicaciones Web con Blazor”, “Generación de Reportes y Documentos con Java”, “Despliegue de Componentes y aplicaciones en la nube”, “JDBC in a Nutshell” y “DTO & Business Layer”, todos encontrados en `Material/`.
- TA LAB11: informe https://docs.google.com/document/d/1HubEn02bPx3Lcmf0wDgC2phi7jQhOtCdUn5OkQHXHNg/edit ; guion https://docs.google.com/document/d/1PeHvdXnxtV84TuveJNAV75pW8yZcOKu5uqaKA_blw0c/edit ; reporte https://docs.google.com/document/d/1po9GprTeuavsgJ7x0HthXOLV8-xeH8pTuxcpQhEQ97s/edit ; apuntes https://docs.google.com/document/d/12bFK1XpAhlZs1TAPZUiJMegfwdcDqG1FqGzFcoje0kg/edit .
- Evolución TA: informes LAB02, LAB04, LAB06 y LAB09 consultados; el informe LAB09 explicita que Blazor era prototipo con datos demo y LAB11 introdujo REST. El catálogo LAB11 es el control de versión más reciente provisto.
- Código real: https://github.com/Programacion-3-PUCP/AdmitU/tree/lab11-diego
- **No disponible para lectura en esta sesión:** https://github.com/fpaz1987/1INF30-2026-2 (404/NOT_FOUND para conector). Su metodología de archivo concreto queda pendiente de verificación cuando sea accesible.

**Criterio de verdad:** el código inspeccionado autoriza afirmar que una ruta o clase **existe**, no que el despliegue se realizó satisfactoriamente. Los porcentajes y cifras históricas son declaraciones de informes hasta realizar pruebas reproducibles.


## H. Referencia docente ya verificada (ZIP del profesor)

Se han inspeccionado los ejemplos de `BackendJAVA_FrontendCSharp` del ZIP `1INF30-2026-2-main.zip`. Referencias técnicas: `JAVA/softprog_solution/softprog_services/src/main/java/pe/edu/pucp/softprog/services/rest/reportes/ReporteRS.java` produce bytes PDF desde Jasper (`JRLoader`, `JasperFillManager`, `JasperExportManager`) usando JDBC y devuelve `Response.ok(..., "application/pdf")`. `CSharp/SoftprogSolution/SoftprogWA/Servicios/REST/ReporteRestService.cs` realiza GET y obtiene `byte[]`; `Components/Pages/Reportes/ReporteAreas.razor` los presenta en `iframe` mediante data URI. `ReportesFuente/ReporteAreas.jrxml` es diseño Jasper. Se conserva esta secuencia pedagógica para adaptar los reportes RF_03/09/15, sin copiar nombres, consultas o credenciales del ejemplo.

**Hallazgos de ingeniería que exigen pruebas, no simples opiniones:** `Program.cs` registra `DatosSesion` como `Singleton` pese a contener `PostulanteActualId` y `EvaluadorActualId` mutables; `DBManager.java` mantiene `Connection con` como atributo de instancia y lo muta en `getConnection()`. Los ejemplos del profesor tampoco sustituyen una revisión de concurrencia: la instrucción es replicar la organización por capas y corregir problemas reales de seguridad, no copiar defectos inadvertidamente.
