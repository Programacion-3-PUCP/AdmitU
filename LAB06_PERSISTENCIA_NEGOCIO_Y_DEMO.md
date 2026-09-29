# AdmitU — Qué se hizo para el Lab06 y por qué (guía del equipo)

**Equipo Los Favoritos de Cueva — Programación 3 (1INF30).**
Este documento describe, en lenguaje simple, qué se agregó y qué se modificó
para el Lab06, el motivo de cada cambio y cómo probarlo. Sigue el mismo molde
del proyecto SoftProg que el profesor avanzó en clase, pero adaptado a AdmitU:
todo con `CallableStatement` (procedimientos almacenados), nada con `Statement`.

> **Lectura rápida: sección 1 y tabla de la sección 3.**

## 1. Resumen en 10 líneas

1. El punto de partida es lo presentado en el **Lab04** (nota 19.50/20).
2. El profesor cambió el Lab06: de individual pasó a ser **avance grupal de la TA**.
3. Lo que pide: capa de **persistencia (DAO) al 100%**, capa de **lógica de negocio
   (business) al 100%** y un `main` que demuestre el **CRUD de 6 entidades**.
4. Se copiaron tal cual las clases generales del profesor: `DBManager`,
   `TransactionContext`, `IDAO`, `IBaseBO` (solo cambia el package).
5. Se crearon **3 módulos nuevos**: `admitu_dbmanager`, `admitu_dao`,
   `admitu_business_logic` (ese es el nombre exacto, igual que `softprog_business_logic`).
6. Las capas están completas para **las 25 entidades concretas**: 25 DAO + 25 Impl
   y 25 BO + 25 Impl, todo con `CallableStatement`.
7. Se agregaron **125 procedimientos** al mismo `admitu_db.sql` (5 por entidad:
   insertar, modificar, eliminar, buscar por id y listar todos).
8. Las 6 entidades de la **demo** son: **Facultad, Carrera, Convocatoria,
   Postulante, Postulación y Pago**.
9. Se corrigió lo del Lab04: `BANCO` pasó a ser `TRANSFERENCIA` y se agregó
   `activo` a `Pais`, `Facultad` y `Sede`.
10. **Verificado**: el script corrió en el RDS (27 tablas + 125 procedures), todo
    compila, el `Principal` corre con salida 0 y una prueba funcional de las
    capas nuevas dio **84/84 OK** sin fallos.

## 2. Glosario mínimo (para estar todos en la misma página)

| Palabra | Qué significa aquí, con ejemplo |
|---|---|
| **DAO** | Clase que habla con la base de datos y nada más. Ej.: `FacultadImpl` inserta una facultad llamando al procedure `INSERTAR_FACULTAD`. |
| **BO (business)** | Clase que valida las reglas del negocio antes de guardar. Ej.: `PagoBOImpl` no deja registrar una transferencia sin voucher. |
| **CallableStatement** | Forma de llamar a un procedimiento almacenado desde Java. Ej.: `{call INSERTAR_FACULTAD(?, ?, ?)}`. Es lo que pidió el profe para este lab. |
| **Procedure** | Función guardada en MySQL que hace el INSERT/UPDATE/SELECT. Ej.: `INSERTAR_PAGO` registra el pago y devuelve el id generado. |
| **Baja lógica** | No borrar la fila, solo marcarla inactiva. Ej.: `facultad.setActivo(false)` o `UPDATE facultad SET activo = 0`. |
| **TransactionContext** | Clase del profe que maneja la transacción (commit/rollback). Si algo falla a mitad, se deshace todo. |

## 3. Qué pidió el profesor y cómo se cumplió (tabla maestra)

| # | Lo que pide el comunicado del Lab06 | Dónde está cumplido |
|---|---|---|
| 1 | Capa de persistencia (DAO) al 100% | Módulo `admitu_dao`: `IDAO` genérico + **25 DAO** con sus **25 Impl** todo-`CallableStatement` (las 25 entidades concretas del dominio) |
| 2 | Capa de lógica de negocio (business) al 100% | Módulo `admitu_business_logic`: `IBaseBO` genérico + **25 BO** con sus **25 Impl** y validaciones por entidad |
| 3 | Artefactos previos corregidos | `BANCO` → `TRANSFERENCIA` y `activo` en `Pais/Facultad/Sede` (§5) |
| 4 | Main que evidencie el CRUD de 6 entidades | `admitu/.../main/Principal.java` hace insertar → buscar → modificar → eliminar de las 6 contra el RDS |
| 5 | Misma estructura del proyecto de clase | 5 módulos igual que SoftProg: `domain`, `dbmanager`, `dao`, `business_logic`, ejecución. `DBManager`, `TransactionContext`, `IDAO`, `IBaseBO` copiados del profe |

## 4. Archivos nuevos (100 Java + 1 properties + 3 pom)

### Módulo `admitu_dbmanager` (conexión, copiado del profe)

| Archivo | Qué es |
|---|---|
| `admitu_dbmanager/pom.xml` | POM del módulo (sin dependencias, igual que `softprog_dbmanager`) |
| `.../admitu/config/DBManager.java` | Singleton de conexión, copia exacta del profe (solo cambia el package) |
| `.../admitu/config/TransactionContext.java` | Manejo de transacción por hilo, copia exacta del profe |
| `admitu_dbmanager/src/main/resources/db.properties` | Credenciales del RDS: `databaseleon.cwmbjhvjuqez.us-east-1.rds.amazonaws.com:3306/admitu_db` |

### Módulo `admitu_dao` (persistencia, todo Callable)

| Archivo | Qué es |
|---|---|
| `admitu_dao/pom.xml` | Depende de `admitu_domain` + `admitu_dbmanager` (igual que el profe) |
| `.../admitu/dao/IDAO.java` | Interfaz genérica `insertar/modificar/eliminar/buscarPorId/listarTodos`, copia del profe |
| `.../configuracion/dao/FacultadDAO.java` | Interfaz, solo extiende `IDAO<Facultad>` |
| `.../configuracion/dao/CarreraDAO.java` | Interfaz, solo extiende `IDAO<Carrera>` |
| `.../configuracion/dao/ConvocatoriaDAO.java` | Interfaz, solo extiende `IDAO<Convocatoria>` |
| `.../persona/dao/PostulanteDAO.java` | Interfaz, solo extiende `IDAO<Postulante>` |
| `.../postulacion/dao/PostulacionDAO.java` | Interfaz, solo extiende `IDAO<Postulacion>` |
| `.../pago/dao/PagoDAO.java` | Interfaz, solo extiende `IDAO<Pago>` |

Los 19 DAO pares restantes siguen exactamente el mismo molde (interfaz que solo
extiende `IDAO<T>` + `Impl` con los 5 `CallableStatement`):

| Paquete | DAO + Impl nuevos |
|---|---|
| `configuracion` | `Sede`, `Etapa`, `Requisito`, `Modalidad`, `OfertaCarrera`, `ConvocatoriaEtapa`, `ConvocatoriaModalidad`, `RequisitoConvocatoriaModalidad` |
| `persona` | `Pais`, `InstitucionEducativa`, `Apoderado`, `Evaluador`, `AntecedenteAcademico` |
| `postulacion` | `EstadoPostulacion`, `PostulacionHistorial`, `DocumentoPostulacion`, `DocumentoObservacion`, `CarnePostulante` |
| `notificacion` | `Notificacion` |

| Archivo (de los 25) | Qué es |
|---|---|
| `.../configuracion/impl/FacultadImpl.java` | CRUD con `{call INSERTAR/MODIFICAR/ELIMINAR/LISTAR_FACULTAD...}` |
| `.../configuracion/impl/CarreraImpl.java` | CRUD con procedures de carrera (trae el nombre de la facultad con JOIN) |
| `.../configuracion/impl/ConvocatoriaImpl.java` | CRUD con procedures de convocatoria (convierte `LocalDate` ↔ `Date` y enum `EstadoConvocatoria`) |
| `.../persona/impl/PostulanteImpl.java` | Inserta en 2 tablas (`persona` + `postulante`) como el `INSERTAR_EMPLEADO` del profe |
| `.../postulacion/impl/PostulacionImpl.java` | CRUD con procedures de postulación |
| `.../pago/impl/PagoImpl.java` | CRUD con procedures de pago (convierte enums `MedioPago/EstadoPago`) |

Regla que se siguió (igual que el profe): el `insertar` usa `TransactionContext`
(transacción) y el resto (`modificar/eliminar/buscar/listar`) usa `DBManager`
directo. El `eliminar` es baja lógica (`activo = 0`, o `estado = 'RECHAZADO'`
en pago) en las tablas que tienen columna `activo`; borra físico donde no la
tienen (`estado_postulacion`, tablas pivote y las hijas de postulación).

### Módulo `admitu_business_logic` (reglas de negocio)

| Archivo | Qué es |
|---|---|
| `admitu_business_logic/pom.xml` | Depende de `admitu_domain` + `admitu_dao` (igual que el profe) |
| `.../admitu/bo/IBaseBO.java` | Interfaz genérica, copia del profe |
| `.../configuracion/boi/IFacultadBO.java` | Interfaz, solo extiende `IBaseBO<Facultad>` |
| `.../configuracion/boi/ICarreraBO.java` | Interfaz, solo extiende `IBaseBO<Carrera>` |
| `.../configuracion/boi/IConvocatoriaBO.java` | Interfaz, solo extiende `IBaseBO<Convocatoria>` |
| `.../persona/boi/IPostulanteBO.java` | Interfaz, solo extiende `IBaseBO<Postulante>` |
| `.../postulacion/boi/IPostulacionBO.java` | Interfaz, solo extiende `IBaseBO<Postulacion>` |
| `.../pago/boi/IPagoBO.java` | Interfaz, solo extiende `IBaseBO<Pago>` |
| `.../configuracion/bo/FacultadBOImpl.java` | Valida código/nombre no vacíos y largos; `insertar` con commit/rollback |
| `.../configuracion/bo/CarreraBOImpl.java` | Valida facultad válida + código/nombre |
| `.../configuracion/bo/ConvocatoriaBOImpl.java` | Valida código/nombre, estado no null y fecha inicio ≤ fecha fin |
| `.../persona/bo/PostulanteBOImpl.java` | Valida nombres, correo, tipo/numero de documento y fecha de nacimiento |
| `.../postulacion/bo/PostulacionBOImpl.java` | Valida postulante, convocatoria, modalidad, oferta, estado y código de inscripción |
| `.../pago/bo/PagoBOImpl.java` | Valida postulación, medio, monto ≥ 0, código y que la TRANSFERENCIA traiga voucher |

Los 19 BO pares restantes (interfaz `boi` + `*BOImpl`) aplican el mismo patrón
con las reglas propias de su entidad:

| BO | Validación principal |
|---|---|
| `PaisBOImpl` | ISO2 de 2 caracteres, nombre obligatorio |
| `SedeBOImpl` | código y nombre obligatorios, dirección ≤ 200 |
| `EtapaBOImpl` | código/nombre obligatorios, fecha inicio ≤ fecha fin |
| `RequisitoBOImpl` | tipo de archivo y tamaño máximo > 0 |
| `ModalidadBOImpl` | código/nombre obligatorios |
| `EstadoPostulacionBOImpl` | código y nombre obligatorios |
| `InstitucionEducativaBOImpl` | país válido, tipo de institución obligatorio |
| `OfertaCarreraBOImpl` | convocatoria y carrera válidas, vacantes ≥ 0 |
| `ConvocatoriaEtapaBOImpl` | convocatoria y etapa válidas, fechas coherentes |
| `ConvocatoriaModalidadBOImpl` | convocatoria y modalidad válidas, costo ≥ 0 |
| `RequisitoConvocatoriaModalidadBOImpl` | rel. válida y orden > 0 si es obligatorio |
| `ApoderadoBOImpl` | datos de persona + parentesco obligatorio |
| `EvaluadorBOImpl` | datos de persona + cargo obligatorio |
| `AntecedenteAcademicoBOImpl` | postulante e institución válidos, anios coherentes y no futuros |
| `PostulacionHistorialBOImpl` | postulación y estado actual válidos, fecha y motivo obligatorios |
| `DocumentoPostulacionBOImpl` | requisito válido, versión > 0, tamaño > 0, carga no futura |
| `DocumentoObservacionBOImpl` | documento y evaluador válidos, fecha de observación obligatoria |
| `CarnePostulanteBOImpl` | postulación y sede válidas, código y vigencia coherentes |
| `NotificacionBOImpl` | postulación válida, medio/tipo/estado obligatorios, no leída sin fecha |

## 5. Archivos modificados (24 del dominio + 3 pom + Principal + SQL)

### Correcciones del Lab04 (las que pidió el JP)

| Archivo | Cambio |
|---|---|
| `admitu_domain/.../pago/MedioPago.java` | `BANCO` → `TRANSFERENCIA` |
| `admitu_domain/.../persona/Pais.java` | Agregado `boolean activo` (nace `true`) + `isActivo/setActivo` |
| `admitu_domain/.../configuracion/Facultad.java` | Agregado `boolean activo` + `isActivo/setActivo` |
| `admitu_domain/.../configuracion/Sede.java` | Agregado `boolean activo` + `isActivo/setActivo` |
| `admitu_db.sql` (tabla `pago`) | `ENUM('TARJETA','YAPE','BANCO')` → `ENUM('TARJETA','YAPE','TRANSFERENCIA')` |
| `admitu_db.sql` (tablas `pais`, `sede`, `facultad`) | Agregada columna `activo BOOLEAN NOT NULL DEFAULT TRUE` |

### Ajustes del dominio para que el DAO pueda guardar/leer IDs de la BD

Antes el `id` era `final` y se generaba solo en memoria; ahora la BD devuelve el
id y hay que guardarlo en el objeto (igual que el `setIdArea` del profe).
Quedan **24 entidades con `id` no final y `setId` que actualiza el correlativo**:

| Ya lo tenían del Lab04 | Se les agregó en este lab (14) |
|---|---|
| `Carrera`, `Convocatoria`, `ConvocatoriaModalidad`, `OfertaCarrera`, `EstadoPostulacion`, `Postulacion`, `Pago`, `Postulante`, `Persona` | `ConvocatoriaEtapa`, `Etapa`, `Modalidad`, `Requisito`, `RequisitoConvocatoriaModalidad`, `Sede`, `Notificacion`, `AntecedenteAcademico`, `InstitucionEducativa`, `Pais`, `CarnePostulante`, `DocumentoObservacion`, `DocumentoPostulacion`, `PostulacionHistorial` |

Además se agregaron constructores vacíos donde el DAO los necesita
(`ConvocatoriaModalidad`, `OfertaCarrera`) y setters puntuales usadas por los
`mapear` de los DAO.

No cambia el comportamiento en memoria: los ids siguen autogenerándose igual;
solo que ahora el DAO puede pisarlos con el id real de la BD.

### POMs y ejecución

| Archivo | Cambio |
|---|---|
| `pom.xml` (padre) | Módulos: `admitu_domain`, `admitu_dbmanager`, `admitu_dao`, `admitu_business_logic`, `admitu` |
| `admitu/pom.xml` | Agregadas dependencias a `admitu_business_logic` y `mysql-connector-j:26.7.0` (la misma versión del profe) |
| `admitu/.../main/Principal.java` | Reescrito: antes demo en memoria, ahora CRUD de las 6 entidades vía BO contra el RDS (ver §7). Usa sufijo por timestamp para no chocar con los UNIQUE al correrlo varias veces |

### `admitu_db.sql` (pasó de 514 a 2060 líneas, sigue siendo 1 solo archivo)

Se anexaron al final, sin tocar lo anterior: `DROP PROCEDURE IF EXISTS` de los
**125** + bloque `DELIMITER $` con los **125 procedures** + `DELIMITER ;`
(5 por cada una de las 25 entidades concretas):

| Procedures | Qué hacen |
|---|---|
| `INSERTAR/MODIFICAR/ELIMINAR/LISTAR_FACULTAD_X_ID/LISTAR_FACULTADES_TODAS` | CRUD facultad (eliminar = `activo = 0`) |
| `..._CARRERA...` (5) | CRUD carrera con JOIN a facultad |
| `..._CONVOCATORIA...` (5) | CRUD convocatoria |
| `..._POSTULANTE...` (5) | Inserta en `persona` + `postulante` y devuelve el id (como el `INSERTAR_EMPLEADO` del profe) |
| `..._POSTULACION...` (5) | CRUD postulación (eliminar borra primero toda la cadena hija: notificaciones, observaciones, documentos, carne, pagos e historial, antes de la postulación) |
| `..._PAGO...` (5) | CRUD pago (eliminar = `estado = 'RECHAZADO'`) |
| `..._PAIS/SEDE/ETAPA/REQUISITO/MODALIDAD/ESTADO_POSTULACION...` (30) | CRUD de catálogos (baja lógica donde hay `activo`) |
| `..._INSTITUCION_EDUCATIVA/ANTECEDENTE_ACADEMICO/APODERADO/EVALUADOR...` (20) | Instituciones, antecedentes y personas; apoderado/evaluador escriben en `persona` + su tabla |
| `..._OFERTA_CARRERA/CONVOCATORIA_ETAPA/CONVOCATORIA_MODALIDAD/REQUISITO_CONVOCATORIA_MODALIDAD...` (20) | Tablas de configuración de la convocatoria, con JOIN para traer los datos del padre |
| `..._POSTULACION_HISTORIAL/DOCUMENTO_POSTULACION/DOCUMENTO_OBSERVACION/CARNE_POSTULANTE/NOTIFICACION...` (25) | Ciclo de vida de la postulación (eliminar encadena hijos para respetar los FK `RESTRICT`) |

## 6. Por qué las 6 entidades de la demo son esas

El comunicado deja que el equipo elija 6. Se eligieron las que arman el flujo
completo de admisión punta a punta:

1. `Facultad` (simple, sin FK: como el `Area` del profe, ideal para abrir la expo)
2. `Carrera` (1 FK a facultad: muestra el JOIN)
3. `Convocatoria` (muestra fechas y enum de estado)
4. `Postulante` (2 tablas `persona` + `postulante`: el caso difícil, igual que `Empleado`)
5. `Postulación` (une postulante + convocatoria + modalidad + oferta + estado)
6. `Pago` (muestra `TRANSFERENCIA` con voucher y el cambio `PENDIENTE` → `APROBADO`)

## 7. Qué hace el `Principal` ahora (guion de la expo)

1. Facultad: registra `FL6-*`, lista, busca, cambia el nombre, la desactiva y
   verifica `activo = 0`.
2. Carrera: registra `CL6-*` con la facultad 1 (seed), cambia el nombre, lista
   y la desactiva.
3. Convocatoria: registra `CV6-*` publicada, cambia la descripción y lista.
4. Postulante: registra con DNI/correo únicos, cambia el teléfono y lo verifica.
5. Postulación: registra con modalidad 1, oferta 1 y estado 1 (seeds), cambia a
   estado 2 y verifica la observación.
6. Pago: registra por `TRANSFERENCIA` con voucher en `PENDIENTE`, lo pasa a
   `APROBADO`, lista y lo elimina (en consola se ve `RECHAZADO`).
7. Limpieza: el `ELIMINAR_POSTULACION` borra los pagos hijos y el historial y
   recién la postulación; después se borran postulante y convocatoria de la demo.

Cada paso imprime en consola (`Facultad registrada: ...`, `Pago aprobado: ...`),
así en la expo se corre el `main` y se verifica cada línea con un `SELECT` en
el Workbench.

## 8. Checklist del equipo

Lo técnico ya está verificado en esta entrega:

- [x] `admitu_db.sql` corrido completo en el RDS (`databaseleon.../admitu_db`):
  **27 tablas y 125 procedures** (`information_schema.ROUTINES`).
- [x] Compilación completa de los 5 módulos sin errores.
- [x] `pe.edu.pucp.main.Principal` corre y termina en `Demo terminada` (salida 0).
- [x] Prueba funcional de las 25 capas vía BO: **84/84 casos OK** (inserciones,
  mapeo de IDs relacionados, búsquedas, modificaciones, borrados en cascada y
  validaciones de negocio).

Lo que falta antes de la expo:

- [ ] Repartir la expo: 1 integrante por capa (DAO / BO / procedures / demo) o
  por grupo de entidades.
- [ ] Llevar el Workbench abierto con las tablas y `SHOW PROCEDURE STATUS`
  (125 filas) a la vista.
- [ ] Ensayar quién explica qué, ya que la cobertura es de 25 entidades y no de 6.
