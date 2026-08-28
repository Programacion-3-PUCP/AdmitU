---
type: analysis
status: review
updated: 2026-09-02
sources:
  - wiki/sources/informe-admitu-lab02-prog3.md
  - wiki/sources/proyecto1-referencia-grupo07.md
  - raw/nivels/6/programacion-3/Horarios, Lineamientos y Cronograma - 1INF30-2026-2.pdf
---

# AdmitU — Catálogo para el informe (justificado, consensuable con el equipo)

Documento de entrega para compartir con el equipo. Contiene (1) la justificación de cada decisión de diseño del catálogo y (2) la tabla final lista para pegar en la sección "Catálogo de Requerimientos" del informe oficial (Lab 02 → labs 04/07/09/11/12).

---

## Parte A — Justificación de cada decisión

### A.1 Por qué el catálogo pasa de 15 RF + 5 RNF a 21 RF + 7 RNF

**Hecho verificado:** el modelo de datos del reference (Informe Grupo 07) tiene **29 tablas**. El catálogo de Lab 02 (15 RF + 5 RNF) solo gestionaba de forma directa **12 de esas tablas**. Las restantes **17 tablas** no tenían ningún requerimiento explícito que las gestionara.

Esto importa porque en los Labs 04/07/09/11/12 se exige implementar **el 100% del software** sobre esa base de datos. Si una tabla no la gestiona ningún RF, ese módulo quedaría sin implementar y no se completaría la cobertura. Verificado por barrido tabla por tabla (7+5+10+2+3+2 = 29).

**Decisión:** añadir requerimientos para cubrir las 17 tablas, **agrupando en el mínimo número de RF posibles** para no inflar innecesariamente el esfuerzo de implementación al 100%.

### A.2 Agrupaciones y su motivo

| Hueco detectado (tablas sin RF) | Cómo se cierra | Motivación |
|---|---|---|
| 7 catálogos (TIPOS_DOCUMENTO, PARENTESCOS, PAISES, FACULTADES, MEDIOS_PAGO, MEDIOS_NOTIFICACION, SEDES) + ETAPAS + ESTADOS_POSTULACION | **RF020** (gestionar catálogos) | Son tablas maestras de referencia/configuración con CRUD idéntico; agruparlas en un solo RF evita 9 RF triviales. |
| EVALUADORES | **RF021** (gestionar evaluadores) | Actor "Evaluador" existía sin su entidad; se cierra en un RF de CRUD. |
| POSTULACIONES (la entidad núcleo) | **RF016** (registrar postulación) | Sin este RF el corazón del sistema no tendría soporte. Es el más importante. |
| CONV_MODALIDADES, OFERTAS_CARRERAS, REQ_CONV_MODALIDADES, CONV_ETAPAS | **RF017** (configurar convocatoria) | Son las "habilitaciones" que arman una convocatoria a partir de los catálogos; forman una unidad funcional coherente. |
| NOTIFICACIONES | **RF019** (envío automático) | Comunicación transversal; el informe la declara como RF automático/documentado. |
| DECISIÓN de estado (no una tabla, sino una regla) | **RF018** (evaluación) | Cambia el estado de POSTULACIONES y registra en POSTULACION_HIST; es una transición, compatible con RF013 (consultar historial). |

**Ampliaciones (sin RF nuevo):**
- **RF002** se amplía para incluir **apoderado** (tabla ADM_APODERADOS).
- **RF005** se amplía para incluir **institución educativa** de procedencia (tabla ADM_INSTITUCIONES_EDUCATIVAS).

**RNF añadidos:**
- **RNF06** → autorización por rol (existe el actor Finanzas/Admisión/Evaluador; sin control de roles no se sostiene).
- **RNF07** → trazabilidad del historial de estados (respalda RF013 y la auditoría).

### A.3 Por qué 21 RF + 5 integrantes = mínimo respetado

- El lineamiento B.3 exige **mínimo 3 RF + 1 RNF por integrante** → 5 × 3 = **15 RF / 5 RNF** mínimo.
- El catálogo propone **21 RF / 7 RNF** → supera el mínimo en 6 RF y 2 RNF.
- **Sin inflar:** los 6 RF extra corresponden exactamente a los huecos de cobertura de las 29 tablas, no a requerimientos decorativos.

### A.4 Reportes dentro del límite duro

- El lineamiento limita los reportes a **mínimo 2, máximo 3**.
- Se mantienen **3 reportes** (RF003, RF009, RF015). No se añaden más, pese a que hay más tablas, para respetar el tope.

### A.5 Fronteras de ambigüedad (evitar duplicidad en la entrega)

| RF que podría confundirse | Límite definido |
|---|---|
| RF018 vs RF013 | RF018 ejecuta el cambio de estado (acción); RF013 consulta/audita el historial ya registrado. |
| RF018 vs RF010 | RF018 decide el estado; RF010 gestiona las observaciones documentales del expediente. |
| RF019 | Declarado explícitamente como comportamiento **automático** del sistema (no un CRUD), para que el equipo no lo cuente como acción manual duplicada. |

### A.6 Decisión clave: modelo de estados desde catálogo BD (no enum fijo)

El Lab 02 usaba un enum Java fijo (REGISTRADO/INSCRITO/OBSERVADO). El catálogo propone **gestionar los estados desde la tabla catálogo ADM_ESTADOS_POSTULACION** con historial (ADM_POSTULACION_HIST):

- Menos rígido: permite agregar estados sin recompilar.
- Auditable: cada transición queda registrada (RNF07).
- Coherente con el reference, que ya modela estas tablas.

### A.7 Justificación de cada Requerimiento No Funcional (RNF)

El Lab 02 partió de **5 RNF** (RNF01–RNF05); se añaden **RNF06 y RNF07** porque el análisis del catálogo detectó dos brechas de calidad relevantes (autorización por rol y trazabilidad de estados). Cada RNF de abajo justifica su origen y su motivo:

| Código | Origen | ¿Por qué? |
|---|---|---|
| RNF01 | Del Lab 02 | **No negociable del curso:** los lineamientos piden una aplicación distribuida Java (backend) + C# (frontend) con servicios web SOAP/REST. Es el requisito de arquitectura raíz que condiciona toda la implementación (Labs 09/11/12). |
| RNF02 | Del Lab 02 | Mide la **usabilidad/rendimiento** en la pantalla de mayor exposición al postulante. Se mantiene porque es medible y verificable; aunque el análisis sugirió que es estrecho, se conserva tal cual del informe original para no alterar entregables ya aprobados. |
| RNF03 | Del Lab 02 | **Seguridad de la información:** las credenciales de postulantes y administradores deben encriptarse. Obligatorio en un sistema que maneja datos personales sensibles (D.L. 29733 protección de datos). No se amplía a más datos porque el informe original lo limita a contraseñas. |
| RNF04 | Del Lab 02 | **Escalabilidad/concurrencia:** garantiza que el sistema soporte picos de carga (500 postulantes simultáneos) en cierre de inscripción. Directamente ligado a que un fallo en fechas clave rompa el proceso real de admisión. |
| RNF05 | Del Lab 02 | **Fiabilidad/transaccionalidad (reformulada hacia negocio):** evita pérdida de datos ante carga simultánea de documentos y pagos. Se mantiene porque la conciliación de pagos y la carga de documentos (RF008/RF011/RF012) son operandos críticos que no pueden corromperse. |
| RNF06 | **Nuevo** | **Control de acceso por rol.** El catálogo define 5 actores (Administrador, Postulante, Evaluador, Finanzas, Admisión) con acciones distintas. Sin un RNF de autorización no se garantiza que cada actor sólo ejecute sus RF, lo cual es base para que la gestión por rol (RF010/RF012/RF014/RF018) tenga sentido real. Lo sugiere el análisis original (observación 5). |
| RNF07 | **Nuevo** | **Trazabilidad de estados (auditoría).** El modelo de estados con historial (ADM_POSTULACION_HIST + trigger del reference) exige registrar cada transición. Sin este RNF, la auditoría de cambios (RF013) y la garantía de integridad en decisiones de evaluación (RF018) quedarían sin respaldo. Lo sugiere el análisis original (observaciones 2 y 6). |

**Resumen de la decisión:** los 5 RNF originales se conservan tal cual (ya estaban aprobados en Lab 02); los 2 nuevos (RNF06, RNF07) son los únicos que el dominio exige para que los RF de roles y de estados sean coherentes. Total: **7 RNF** = 5 conservados + 2 nuevos, ≥ el mínimo de 5 (1 por integrante).

### A.8 Veredicto

- 21 RF + 7 RNF cubren el **100% de las 29 tablas** (verificado: 7+5+10+2+3+2, sin huecos).
- Cumple y supera el mínimo del curso; respeta el tope de reportes.
- Base completa y trazable para Labs 04/07/09/11/12.
- Único punto a decidir en equipo: **reparto de los 6 RF nuevos entre los 5 integrantes** (propuesta sugerida en A.9).

### A.9 Distribución sugerida de los RF nuevos (para tu deliberación)

Esta propuesta mantiene a cada integrante con su mínimo de 3 RF (los RF originales del Lab 02 ya cubren 3 por integrante; los nuevos son extras equilibrados).

| Integrante | RF del Lab 02 | RF nuevos (sugeridos) | Total |
|---|---|---|---|
| Diego Osorio | RF001–RF003 | RF016, RF021 | 5 |
| Harold Manza | RF004–RF006 | RF017 | 4 |
| Richard León | RF007–RF009 | RF018 | 4 |
| Francis Huamani | RF010–RF012 | RF019 | 4 |
| Matías Peña | RF013–RF015 | RF020 | 4 |

*La distribución puede ajustarse según preferencias del equipo; el único requisito es que cada integrante tenga 3 RF.*

### A.10 Respuesta a la revisión externa (verificada contra el cronograma oficial)

Un revisor independiente planteó tres dudas. Todas se responden con texto del documento oficial `Horarios, Lineamientos y Cronograma - 1INF30-2026-2.pdf` (sección B.3 y C.2), verificado directamente:

**1) ¿Es cierto que el curso exige cubrir toda la BD (premisa de A.1)? — SÍ, con cita textual.**

B.3 (lineamientos de la TA):
> *"Toda la información que maneje la base de datos debe ser gestionada (registrada, modificada, eliminada, consultada) mediante el uso del producto de software."*

C.2 (L04): exige "Diagrama físico de la BD e implementación de la creación de tablas", "capa de persistencia (CRUD)", "Script SQL de los procedimientos almacenados relacionados a LISTAR, INSERTAR, ACTUALIZAR, ELIMINAR (CRUD) de las principales entidades del negocio".
- L07: "capa del dominio **al 100%**", "capa de persistencia **al 100%**", "capa de lógica de negocio **al 100%**", "Script SQL de **todos** los procedimientos almacenados".
- L09/11/12: "Implementación del software **al 40% / 80% / 100%** de **todos los requerimientos**".

Interpretación: si la base de datos tiene 29 tablas y *toda la información de la BD debe gestionarse por el software*, cada tabla debe quedar respaldada por un requerimiento funcional. Un catálogo de 15 RF que solo cubre 12 tablas dejaría sin gestionar 17 tablas, contraviniendo B.3. **La premisa central no es interpretación del revisor previo: está literalmente en el documento.**

**2) ¿RF019 con actor "(automático)" rompe el patrón de la rúbrica? — Sí, se corrigió.**

B.3 fija la redacción: *"El sistema permitirá a <actor o actores> <acción>"*. Por eso RF019 se reformuló a Actor "Sistema" con redacción "El sistema enviará…", que es la forma correcta para procesos automáticos del sistema (no un CRUD manual con actor humano). Anexo: RF019 sigue teniendo un responsable para la rúbrica (quien configura los medios/registra el envío) pero la notificación en sí es automática.

**3) ¿El salto de alcance (estados por catálogo, historial, roles) es una imposición del curso o una decisión? — Es una decisión recomendada, no un requisito textual.**

El cronograma NO obliga a gestionar estados desde catálogo BD. Es una **recomendación técnica** para la coherencia con el reference y para simplificar la auditoría. El equipo debe decidirlo conscientemente en A.11.

### A.11 Decisión de equipo pendiente (portar el modelo completo o un subconjunto)

El curso obliga a que **toda la BD se gestione por el software** (B.3) y a implementar **el 100% de los requerimientos** (C.2). Eso implica asumir la complejidad de las 29 tablas. Se recomienda **aceptar el modelo completo** (tal como lo plantea 21 RF), porque:
- Evita rehacer el diagrama físico y el CRUD en L04 si luego se descubre que faltan tablas.
- Es lo que ya modeló el reference del curso de Base de Datos.
- No infla el catálogo (agrupa CRUDs triviales en RF020).

Alternativa (más simple pero más riesgo): recortar el modelo a un subconjunto de tablas — pero eso **contradiría B.3** ("toda la información de la BD debe gestionarse") si el diagrama físico mostrara tablas sin gestión, y obligaría a justificar ante el docente por qué se modelaron tablas que no se usan. **Recomendación: aceptar el modelo completo.**

---

## Parte B — Tabla lista para pegar en el informe

### B.1 Catálogo de Requerimientos Funcionales (21 RF)

*Redactados según el patrón exigido en el lineamiento B.3: "El sistema permitirá a <actor o actores> <acción>".*

| Código | Requerimiento |
|---|---|
| RF001 | El sistema permitirá al Administrador gestionar las convocatorias académicas (registro, búsqueda, modificación y eliminación). |
| RF002 | El sistema permitirá al Postulante gestionar su información de usuario, incluidos sus datos personales y, opcionalmente, el apoderado asociado (registro, búsqueda, modificación y eliminación). |
| RF003 | El sistema permitirá al Administrador generar un reporte de pagos por convocatoria (JasperReports). |
| RF004 | El sistema permitirá al Administrador gestionar las modalidades de ingreso (registro, búsqueda, modificación y eliminación). |
| RF005 | El sistema permitirá al Postulante gestionar sus antecedentes académicos, incluida la institución educativa de procedencia (registro, búsqueda, modificación y eliminación). |
| RF006 | El sistema permitirá al Administrador gestionar las carreras ofrecidas y sus vacantes (registro, búsqueda, modificación y eliminación). |
| RF007 | El sistema permitirá al Administrador gestionar los requisitos documentales base (registro, búsqueda, modificación y eliminación). |
| RF008 | El sistema permitirá al Postulante gestionar la carga de sus documentos de postulación (registro, búsqueda, modificación y eliminación). |
| RF009 | El sistema permitirá al Administrador generar un reporte de postulantes observados (JasperReports). |
| RF010 | El sistema permitirá al Evaluador gestionar las observaciones documentales de los expedientes (registro, búsqueda, modificación y eliminación). |
| RF011 | El sistema permitirá al Postulante gestionar sus comprobantes de pago de inscripción (registro, búsqueda, modificación y eliminación). |
| RF012 | El sistema permitirá al Área de Finanzas gestionar la conciliación manual de pagos (registro, búsqueda, modificación y eliminación). |
| RF013 | El sistema permitirá al Administrador consultar y administrar el historial de cambios de estado de las postulaciones (auditoría). |
| RF014 | El sistema permitirá al Área de Admisión gestionar la emisión de carnés de postulante (solo para estado VÁLIDO). |
| RF015 | El sistema permitirá al Administrador generar un reporte general de vacantes y postulaciones completadas (JasperReports). |
| RF016 | El sistema permitirá al Postulante registrar su postulación a una convocatoria abierta seleccionando la modalidad de ingreso y la carrera ofertada, asignando el estado inicial POSTULADO. |
| RF017 | El sistema permitirá al Administrador configurar una convocatoria habilitando sus etapas, las modalidades de ingreso, las carreras con sus vacantes y los requisitos por modalidad. |
| RF018 | El sistema permitirá al Evaluador revisar el expediente del postulante, validar el cumplimiento de los requisitos documentales y registrar la decisión de evaluación que determina el nuevo estado (VÁLIDO u OBSERVADO). |
| RF019 | El sistema enviará notificaciones automáticas al Postulante (por los medios de notificación configurados) ante cada cambio de estado u observación registrada en su postulación. |
| RF020 | El sistema permitirá al Administrador gestionar los catálogos de referencia y de configuración (tipos de documento, parentescos, países, facultades, medios de pago, medios de notificación, sedes, etapas y estados de postulación). |
| RF021 | El sistema permitirá al Administrador gestionar los evaluadores (registro, búsqueda, modificación y eliminación). |

*Nota sobre RF019: es el único comportamiento automático del sistema; por ello no usa el patrón "permitirá a <actor>" sino "enviará automáticamente", conforme a la práctica para procesos automáticos (justificación en A.10).*

### B.2 Catálogo de Requerimientos No Funcionales (7 RNF)

| Código | Requerimiento |
|---|---|
| RNF01 | Back-end en JAVA y front-end en C#, comunicándose vía servicios web (SOAP/REST). |
| RNF02 | Tiempo de respuesta máximo de 3 segundos en el despliegue del catálogo de carreras para los postulantes. |
| RNF03 | Encriptación obligatoria de las contraseñas de postulantes y administradores. |
| RNF04 | Concurrencia mínima de 500 postulantes simultáneos en días de cierre de inscripción. |
| RNF05 | Mantener la integridad transaccional (commits/rollbacks) ante la carga simultánea de documentos. |
| RNF06 | Control de acceso por rol (Administrador, Postulante, Evaluador, Finanzas, Admisión). |
| RNF07 | Trazabilidad completa del historial de cambios de estado para auditoría. |

### B.3 Modelo de estados de una postulación (respaldo de RF016/018/014)

```
POSTULADO (RF016) → INSCRITO (RF011 + conciliación RF012)
                  → EN_EVALUACION (RF018 inicia revisión)
                  → VÁLIDO (RF018 aprueba)      → CARNE_EMITIDO (RF014)
                  → OBSERVADO (RF018 observa → genera observación RF010)
// cada transición: RF013 registra historial + RNF07 audita + RF019 notifica
```
