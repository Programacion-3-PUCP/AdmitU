# Instrucciones para Codex — AdmitU / Programación 3 (1INF30, PUCP, 2026-2)

> Versión de contexto: 11 de noviembre de 2026. Rama de referencia: `lab11-diego` del repositorio `Programacion-3-PUCP/AdmitU`. **Autorización explícita del responsable del proyecto en esta conversación:** se permite implementar y corregir autónomamente la TA, crear ramas/commits/PR, ejecutar pruebas, reconstruir exclusivamente la BD `admitu_db` de ensayo sin datos valiosos y desplegar en infraestructura AWS del curso si las credenciales y permisos ya están configurados en el entorno. No se autorizan alteraciones de otros repositorios, bases productivas o ajenas, gasto abierto en servicios pagos, ni publicación de secretos o código privado del profesor. Codex no puede fabricar credenciales ni saltarse restricciones del entorno.

## 1. Misión y criterios de éxito

Eres un asistente de programación para la Tarea Académica del curso **Programación 3 (1INF30)**. El proyecto **AdmitU** gestiona admisión universitaria y tiene **15 requerimientos funcionales (RF)** y **5 requerimientos no funcionales (RNF)**. El equipo **Los favoritos de Cueva** obtuvo **18.5/20 en LAB11** y presentó en su informe **11 RF de 15 integrados**. Ese número es una declaración del informe, no una verificación automática del código.

**LAB12: miércoles 18 de noviembre de 2026**. El equipo debe tener **al menos 90% de los 15 RF implementados y funcionando de extremo a extremo: mínimo 14 RF completos (93.33%)**, manteniendo **backend Java ↔ servicios RESTful ↔ frontend C#**. No cuentan como “completos” una pantalla de muestra, un CRUD solo en memoria, una consulta no ejecutada, ni un “reporte” que incumple la tecnología JasperReports especificada en el catálogo. Verificar por requerimiento operaciones, validaciones, persistencia, roles y ejecución real.

La **meta operativa autorizada para Codex es 15/15 RF (100%)**, levantar observaciones de LAB11, mejorar RNF según pruebas y buscar un despliegue reproducible en AWS. La meta formal mínima de LAB12 es 14/15. No reconstruir AdmitU desde cero. No garantizar calificación; respaldar cobertura con evidencias verificables.

## 2. Jerarquía de instrucciones y fuentes

En caso de conflicto, aplicar:
1. Enunciado vigente y aclaraciones del JP de LAB12 y catálogo **aprobado** de RF/RNF.
2. Retroalimentación escrita de LAB11 (incluidas reglas de negocio adicionales aclaradas oralmente).
3. Sílabo, lineamientos y diapositivas oficiales de Programación 3.
4. Implementación realmente encontrada en `lab11-diego`, más pruebas realizadas.
5. Apuntes y transcripciones posteriores al parcial (contextualizan métodos de clase; pueden contener errores de transcripción).
6. ZIP del repositorio del profesor, **verificado y analizado parcialmente**: `1INF30-2026-2-main.zip`; metodología detallada en `METODOLOGIA_PROFESOR.md`. Los ejemplos de código filtrados se entregan aparte y deben seguir siendo privados.

Las afirmaciones de informes, diapositivas y comentarios no prueban por sí mismas que una función esté operativa. Antes de implementar, etiquetar evidencia como `DOCUMENTADO`, `CÓDIGO LOCALIZADO`, `COMPILA`, `REST FUNCIONA`, `E2E PROBADO` y `PENDIENTE`.

**Fuentes docente:** la integración GitHub devuelve 404 al repositorio privado `fpaz1987/1INF30-2026-2`, pero el alumno aportó el ZIP completo. Se verificaron `ReporteRS.java`, `Reporte.java`, `AreaRS.java`, `AreaBOImpl.java`, `TransactionContext.java`, `DBManager.java`, `ReporteRestService.cs`, `ReporteAreas.razor` y `ReporteAreas.jrxml` directamente dentro del ZIP. Leer `METODOLOGIA_PROFESOR.md` para convenciones y límites. El ZIP trae archivos `db.properties` con datos de conexión: no subirlo íntegro a AdmitU público.

## 3. Alcance académico exacto: qué sí y qué no

El sílabo abarca POO, librerías, JDBC y persistencia, DAO y lógica de negocio, interfaces web C# Blazor, programación distribuida con SOAP/REST, generación de reportes con JasperReports, despliegue web y programación concurrente. Los apuntes posteriores al parcial concretan el camino de la TA: **Jakarta REST en Java, MySQL, cliente HttpClient en C# (.NET 10), reportes Java/Jasper y eventual despliegue GlassFish + IIS/EC2**.

**Obligatorio en LAB12:** 14 RF funcionales como mínimo, con validaciones, persistencia y conexión REST Java/C#; levantar las observaciones LAB11. **No sustituir REST por SOAP**: SOAP/WSDL/JAX-WS se enseñó como otra técnica, pero la consigna actual exige mantener RESTful. La unidad de concurrencia y el despliegue en AWS son parte del curso y de los RNF, pero **no inventar una obligación de migración a nube en LAB12** si su enunciado no la pide. El enunciado solo exige REST y ejecución funcional, pero el responsable solicita también **despliegue AWS** de preparación final. En localhost es válida la fase de pruebas; si hay acceso al AWS Academy del usuario, desplegar y verificar en la instancia existente o en una instancia de laboratorio dentro de sus cuotas. Si falta acceso técnico, avanzar con scripts e instrucciones reproducibles, no solicitar confirmación para cada cambio de código.

No migrar a Spring Boot, Entity Framework, Hibernate/JPA, ASP.NET MVC, React, Angular, Next.js, microservicios, Kubernetes, servicios SaaS, OAuth externo, colas o arquitectura hexagonal por preferencia personal. No introducir herramientas, patrones o paquetes no enseñados sin justificación y aprobación; primero aprovechar **Java/Maven/Jakarta EE/GlassFish/JDBC/CallableStatement/MySQL, C#/.NET 10/Blazor/HttpClient y JasperReports** ya vistos.

## 4. Arquitectura y rutas que debes respetar

Repositorio: `https://github.com/Programacion-3-PUCP/AdmitU/tree/lab11-diego`

```text
backend/AdmitU/                # raíz del reactor Maven (pom.xml; Java 25)
  admitu_domain/               # clases POJO, enumerados, validaciones de dominio
  admitu_dbmanager/            # conexiones y transacciones
  admitu_dao/                  # interfaces DAO, implementaciones JDBC y stored procedures
  admitu_business_logic/       # interfaces BOI/BO, reglas, validación y transacciones
  admitu/                      # ejecución/pruebas de consola existentes
  admitu_services/             # WAR Jakarta EE / endpoints REST
frontend/AdmitUWA/AdmitUWA/    # aplicación C# Blazor (.NET 10)
  Components/Pages/            # Admin, PortalPostulante, PortalEvaluador
  Services/REST/AdmitURestClient.cs
  Services/DatosSesion.cs
  Models/; Data/; Program.cs; appsettings.json
database/TA_Lab11_Script_LosFavoritosDeCueva.sql
```

En Java, conservar la cadena **recurso REST (`*RS`) → interfaz/implementación BO → interfaz/implementación DAO → JDBC `CallableStatement` → procedimiento almacenado MySQL**. El dominio no accede a SQL; el frontend C# nunca conecta directamente a MySQL. Para operaciones compuestas, verificar `TransactionContext`, `commit`, `rollback` y cierre de conexión en todas las rutas, respetando la implementación existente.

La aplicación REST actual tiene `@ApplicationPath("webresources")`, `@Path`, `@Consumes(MediaType.APPLICATION_JSON)`, `@Produces(MediaType.APPLICATION_JSON)` y rutas explícitas orientadas a operaciones (p. ej. `ConvocatoriaRS/listarTodasConvocatorias`, `insertarConvocatoria`, `buscarConvocatoriaPorId/{id}`, `modificarConvocatoria`, `eliminarConvocatoria/{id}`). **Conservar las rutas que consume AdmitURestClient**; no convertir toda la API a endpoints de otro estilo.

Java: `backend/AdmitU/pom.xml` declara **Java 25**; `admitu_services/pom.xml` declara **Jakarta EE 10** y empaquetado **WAR** con `finalName=admitu_services`. Confirmar las versiones del JDK y GlassFish efectivamente disponibles al ejecutar (el material de clase usa GlassFish 7/8). Frontend: archivo `.csproj` y `Program.cs` tienen configuración existente; no asumir otra versión sin leerlos.

C#: reutilizar la integración actual con `HttpClient`, `AdmitURestClient`, DTOs JSON y las vistas Razor. `Program.cs` actualmente registra `HttpClient`, `AdmitURestClient` y `DatosSesion` como `Singleton`; es un **riesgo real de sesión compartida entre usuarios**, no un diseño automáticamente adecuado a 500 postulantes. Auditar dependencias y alcance antes de modificar el lifetime; evitar arreglos superficiales que rompan la sesión del frontend.

La base SQL de LAB11 tiene un **`DROP DATABASE IF EXISTS admitu_db`** al principio. **El responsable autorizó borrar y reconstruir su base `admitu_db` de pruebas**, siempre que se verifique de forma positiva el servidor, esquema y que sea su entorno de ensayo; antes de borrar generar dump de seguridad cuando sea posible. Nunca ejecutar sobre otros esquemas/hosts no identificados, en datos de terceros o una base productiva. Para cambios, preferir scripts versionados y reproducibles. Nunca publicar credenciales ni datos reales, especialmente porque AdmitU es público.

## 5. Método de programación aprendido en clase (observado en notas oficiales y ejemplos)

### Java — patrón docente
- Proyecto **Maven multimódulo**, parent `pom.xml` y módulos con dependencias explícitas. Compilar con reactor Maven; WAR de servicios; JAR de capas.
- Clases del modelo por entidad con atributos privados, constructores y accesores, asociaciones y enumerados; usar interfaces para DAO y BO. El material trata DTO para resultados de consulta/reporte.
- En DAO, JDBC y **`CallableStatement`** para procedimientos almacenados, `ResultSet` para consulta, parámetros IN/OUT. No reescribir todo en SQL ad hoc o ORM.
- En BO, reglas de negocio (fechas, unicidad, estados, documentos, transacciones); manejo de excepciones explícito. Mensaje útil al usuario en interfaz, error técnico en logs, y **rollback** cuando corresponda.
- Para REST, clase `RestApplication extends Application` con `@ApplicationPath`; clases `*RS` que delegan al BO; métodos `@GET/@POST/@PUT/@DELETE`, `@Path`, `@PathParam`, `@Consumes/@Produces`; JSON. Probar el servicio en navegador cuando es GET y en Postman para operaciones mutantes.
- En reportes: **Jaspersoft Studio `.jrxml` → `.jasper` → Java/JasperReports → PDF real**. Reportes basados en la BD, con campos, parámetros y, si agrega valor, agrupaciones, imágenes, subreportes y gráficos. Preferir exposición Java a C# mediante un endpoint HTTP o servlet según los ejemplos verificados; servir PDF real, no `window.print()` de una tabla HTML.
- Diagnóstico de GlassFish mediante `domains/domain1/logs/server.log` y comprobación del `context path` del WAR. No confundir advertencias con fallas.

### C# / Blazor — patrón docente
- **Blazor Web App** con páginas `.razor`, `@page`, `@inject`, eventos `@onclick`, `@bind`, `EditForm`, validaciones, componentes reutilizables y modales/confirmaciones; para listados, tablas/QuickGrid según módulo existente.
- En el curso se muestra registro de `HttpClient` con `BaseAddress` desde `Program.cs` y clases por servicio en `Services/REST`. El código de AdmitU optó por cliente genérico `AdmitURestClient`: **conservar y extender esta abstracción** si cumple el caso.
- Para REST usar `HttpClient` y serialización/deserialización JSON con DTOs alineados a Java; gestionar operaciones HTTP y errores de forma consistente. No hacer acceso MySQL directamente desde el cliente.
- En operaciones que cargan datos remotos, evitar bloqueo perceptible de UI; respetar el patrón async existente o justificar un cambio pequeño y probado.
- Las confirmaciones, estados visuales, campos deshabilitados y filtros deben ser uniformes. No considerar que una pantalla "funciona" si solo modifica un caché en memoria.

### Registro fiel del alcance
En apuntes del 04/11 el profesor explicó autenticación Blazor por **cookies y roles** (`AddAuthentication`, `SignInAsync`, `[Authorize]`, `AuthorizeRouteView`) y mostró **MD5 como ejemplo didáctico** en SQL. **No usar MD5 para contraseñas nuevas**: el requisito RNF_03 exige protección y MD5 no es un esquema adecuado de contraseñas. Si se implementa autenticación real, preferir hash adaptativo con sal, contraseñas fuera de URL, sesiones separadas y validación de rol del lado del servidor; documentar cualquier diferencia con el ejemplo.

## 6. Reglas funcionales que no puedes ignorar

- **RF_01:** CRUD de convocatorias con **baja lógica mediante `activo`**. El JP pidió **eliminar convocatorias en borrador**, no solo crear/publicar/cerrar. No hacer un `DELETE` físico sin comprobar relaciones y catálogo.
- **Postulación:** **máximo una por postulante y convocatoria, independientemente de la carrera**; prevenir doble clic y peticiones duplicadas tanto en UI como en BO/BD cuando proceda. Un envío no debe producir dos postulaciones ni dos pagos/vouchers pendientes.
- **RF_05:** certificados académicos: carga/visualización/modificación/eliminación; fecha de registro no futura. Auditar si se persiste **el archivo** o solo el nombre.
- **RF_08:** archivos personales (DNI y fotografía) con formatos del catálogo aprobado `.jpg/.png`; actualmente aparece manejo en memoria en `DocumentosPersonales.razor`. Alinear formatos reales con catálogo o elevar discrepancias al JP (la interfaz actual también ofrece PDF para DNI).
- **RF_03, RF_09 y RF_15:** reportes **JasperReports**; tablas HTML y botón imprimir no equivalen a implementación del catálogo.
- **RF_10:** el filtro de Observaciones debe ser visible, conservar opción seleccionada y cambiar efectivamente el conjunto mostrado; si es redundante, retirar solo con validación de criterios.
- **Acciones Reactivar:** confirmación antes de ejecutar; iconos de acciones alineados; campos no editables con visualización y bloqueo consistentes.
- **RF_11/12:** la validación de voucher sigue reglas de estados. Al aprobar, registrar fecha aplicable; no permitir cambios incompatibles sobre un comprobante aprobado.
- **RF_13/14:** preservar trazabilidad de estados y carnés; no generar credenciales para expedientes/pagos no aptos.

Los detalles, responsables y criterios por RF están en `plan-lab12.md`. No alterar texto del catálogo sin aprobación del JP.

## 7. Orden obligatorio de trabajo con el código

1. **Diagnóstico breve primero y luego implementación autónoma**: confirmar rama, inspeccionar `git status`, identificar clase, endpoint, BO, DAO, procedimiento y Razor de cada RF. Crear **matriz de cobertura con evidencias** y proceder directamente a implementar por etapas sin esperar aprobación nueva.
2. **Priorizar defectos de LAB11 y brechas al 90%**. No asumir que 11/15 equivale a 11 funcionales probados en la versión actual.
3. Para cada cambio, registrar: síntoma, causa confirmada o hipótesis, archivos afectados, solución mínima y pruebas. Hacer commits pequeños; seguir avanzando sin esperar una respuesta humana salvo bloqueo real de credenciales/permisos.
4. Modificar **una vertical funcional** por vez: SQL/migración (si es necesario) → DAO → BO → REST → DTO/cliente → Razor → prueba completa. No dejar interfaces incongruentes.
5. Ejecutar, según herramientas disponibles, `mvn -f backend/AdmitU/pom.xml clean package` y `dotnet build` sobre el `.csproj` real; probar REST en GlassFish y flujo en Blazor. Si faltan JDK 25, GlassFish o MySQL, informarlo sin inventar resultados.
6. Redactar evidencia: pasos reproducibles, URL, datos de prueba, resultados, errores y archivos tocados. Una compilación aislada **no** es prueba end-to-end.
7. Trabajar en **rama nueva derivada de `lab11-diego`**, no sobrescribir `master` ni cambios ajenos. Antes de commit o PR, verificar `git diff`, ausencia de credenciales y entregables. Se autoriza `push` y PR **desde una rama nueva de trabajo**, sin fusionar a `master` automáticamente. Se autoriza desplegar solo en el AWS de laboratorio del responsable con permisos ya disponibles, sin gasto abierto y verificando que el destino sea suyo.

## 8. Pruebas exigibles a toda corrección

- **Positivas**: crear, listar, buscar, editar y dar de baja/anular según RF; recargar frontend; verificar BD.
- **Negativas**: duplicados, campos vacíos, formato inválido, fecha futura, cambio de estado ilegal, usuario de rol distinto y servicio caído.
- **Integración**: HTTP C# → REST Java → BO → DAO → SP → MySQL → respuesta JSON/PDF → pantalla.
- **Contención**: doble clic en enviar postulación y dos solicitudes concurrentes; deben dar **un** solo resultado de negocio válido.
- **Regresión**: otros 11 RF ya presentados, navegación, reportes y UI.
- **Evidencia**: para cada RF `E2E PROBADO`, indicar qué se observó en DB/API/UI. Si no hay infraestructura, `NO PROBADO`.

## 9. Protección, honestidad técnica y coordinación

- Repositorio público: no exponer `.env`, `db.properties` con contraseña, cadenas de conexión, contraseñas, tokens, datos privados, llaves AWS ni informes privados del equipo/profesor sin permiso.
- Ejecutar scripts destructivos **solo en `admitu_db` de pruebas del responsable** con host/schema verificados y backup previo cuando sea viable. No hacerlo sobre RDS/BD de compañeros, datos reales o otros esquemas. La autorización para la base de prueba ya está dada; no repetir solicitudes de permiso.
- No atribuir un método al profesor si solo aparece en notas o en código del equipo; señalar la fuente concreta.
- Si el código contradice el informe, el diagnóstico técnico debe consignar ambos y demostrar la diferencia.
- Alinear cambios con asignación del catálogo: RF_01–03 Diego, RF_04–06 Harold, RF_07–09 Richard, RF_10–12 Francis, RF_13–15 Matías. Cambios transversales requieren coordinación.
- El sílabo exige declarar y justificar uso de IA generativa en los trabajos donde lo solicite el docente, y adjuntar prompts si corresponde. Conservar registro de cambios hechos con Codex para que el equipo pueda explicarlos en sustentación.

## 10. Formato esperado para tu primera respuesta de diagnóstico

Sin editar aún, producir:
1. Árbol breve de arquitectura real (rutas existentes).
2. Tabla `RF | Responsable | UI | REST | BO | DAO/SP | Compila | E2E | Brecha | Prioridad`.
3. Tabla de observaciones LAB11 con archivo/ruta exacta, causa confirmada o hipótesis y prueba sugerida.
4. Riesgos técnicos transversales y hallazgos de seguridad/concurrencia, distinguiendo **bloqueante**, **alto** y **posterior a LAB12**.
5. Plan acotado para llegar a **mínimo 14/15**, con orden de ejecución, dependencias y posibilidad de llegar a 15/15.
6. Lista de pruebas efectivamente ejecutadas vs. no ejecutadas; **cero resultados inventados**.

**Leer también:** `arquitectura.md`, `plan-lab12.md` y `METODOLOGIA_PROFESOR.md`. El archivo `PROMPT_CODEX.md` contiene el encargo inicial. Si no se tiene el material docente en el entorno, usar el análisis documentado; no detener el avance de AdmitU.


## Autonomía y orden de ejecución (autorizado)

El encargo no es exclusivamente diagnóstico. **Analizar, implementar, probar, corregir, repetir y documentar sin pausas artificiales**, para llegar a 15/15 RF; si las herramientas y credenciales existen, desplegar en AWS del curso y verificar HTTP/REST/UI/reportes. Crear rama de ejecución como `codex/lab12-completo` desde `lab11-diego` y abrir PR hacia `lab11-diego`; no modificar ni fusionar `master`. No cometer el ZIP privado del profesor, sus ejemplos fuente, claves, `db.properties` reales ni datos de acceso. Priorizar correcciones LAB11, seguridad/sesiones/transacciones, cuatro RF incompletos, pruebas, despliegue y entregables. Si una credencial o capacidad de red no está disponible, **no inventar que se ha desplegado**: generar `docs/despliegue/README.md` y scripts reproducibles, marcar el punto como BLOQUEADO y continuar todas las demás tareas.

La ejecución puede crear commits y modificar el entorno aislado, pero ningún texto concede root, kernel, acceso AWS ni permisos GitHub por sí mismo. No evadir controles de autorización, MFA ni cuotas. No desplegar sobre producción ajena ni generar costos que excedan los recursos del curso. No completar artificialmente la matriz; los estados `PROBADO` requieren logs/outputs reproducibles.
