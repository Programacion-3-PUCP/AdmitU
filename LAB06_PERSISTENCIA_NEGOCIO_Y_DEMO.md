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
6. Se agregaron **30 procedimientos** al mismo `admitu_db.sql` (5 por cada una de
   las 6 entidades, todo con `CallableStatement`).
7. Las 6 entidades de la demo son: **Facultad, Carrera, Convocatoria, Postulante,
   Postulación y Pago**.
8. Se corrigió lo del Lab04: `BANCO` pasó a ser `TRANSFERENCIA` y se agregó
   `activo` a `Pais`, `Facultad` y `Sede`.
9. El `Principal` ahora corre contra la base de datos de verdad (RDS), ya no
   es demo en memoria.
10. Falta: correr el script en el RDS, compilar con `mvn clean install` y correr
    el `Principal` (§8).

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
| 1 | Capa de persistencia (DAO) al 100% | Módulo `admitu_dao`: `IDAO` genérico + 6 DAO (`FacultadDAO`, `CarreraDAO`, `ConvocatoriaDAO`, `PostulanteDAO`, `PostulacionDAO`, `PagoDAO`) con sus 6 Impl todo-`CallableStatement` |
| 2 | Capa de lógica de negocio (business) al 100% | Módulo `admitu_business_logic`: `IBaseBO` genérico + 6 BO (`FacultadBOImpl`, `CarreraBOImpl`, `ConvocatoriaBOImpl`, `PostulanteBOImpl`, `PostulacionBOImpl`, `PagoBOImpl`) con validaciones |
| 3 | Artefactos previos corregidos | `BANCO` → `TRANSFERENCIA` y `activo` en `Pais/Facultad/Sede` (§5) |
| 4 | Main que evidencie el CRUD de 6 entidades | `admitu/.../main/Principal.java` hace insertar → buscar → modificar → eliminar de las 6 contra el RDS |
| 5 | Misma estructura del proyecto de clase | 5 módulos igual que SoftProg: `domain`, `dbmanager`, `dao`, `business_logic`, ejecución. `DBManager`, `TransactionContext`, `IDAO`, `IBaseBO` copiados del profe |

## 4. Archivos nuevos (28 Java + 1 properties + 3 pom)

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
| `.../configuracion/impl/FacultadImpl.java` | CRUD con `{call INSERTAR/MODIFICAR/ELIMINAR/LISTAR_FACULTAD...}` |
| `.../configuracion/impl/CarreraImpl.java` | CRUD con procedures de carrera (trae el nombre de la facultad con JOIN) |
| `.../configuracion/impl/ConvocatoriaImpl.java` | CRUD con procedures de convocatoria (convierte `LocalDate` ↔ `Date` y enum `EstadoConvocatoria`) |
| `.../persona/impl/PostulanteImpl.java` | Inserta en 2 tablas (`persona` + `postulante`) como el `INSERTAR_EMPLEADO` del profe |
| `.../postulacion/impl/PostulacionImpl.java` | CRUD con procedures de postulación |
| `.../pago/impl/PagoImpl.java` | CRUD con procedures de pago (convierte enums `MedioPago/EstadoPago`) |

Regla que se siguió (igual que el profe): el `insertar` usa `TransactionContext`
(transacción) y el resto (`modificar/eliminar/buscar/listar`) usa `DBManager`
directo. El `eliminar` es baja lógica (`activo = 0`, o `estado = 'RECHAZADO'`
en pago). Solo `Postulante` y `Postulación` borran físico porque sus tablas no
tienen columna `activo`.

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

## 5. Archivos modificados (12 del dominio + 3 pom + Principal + SQL)

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
id y hay que guardarlo en el objeto (igual que el `setIdArea` del profe):

| Archivo | Cambio |
|---|---|
| `.../configuracion/Carrera.java` | `id` ya no es `final` + `setId` que actualiza el correlativo |
| `.../configuracion/Convocatoria.java` | Lo mismo + ya tenía constructor vacío |
| `.../configuracion/ConvocatoriaModalidad.java` | Lo mismo + constructor vacío nuevo (lo usa el DAO y el demo) |
| `.../configuracion/OfertaCarrera.java` | Lo mismo + constructor vacío nuevo |
| `.../postulacion/EstadoPostulacion.java` | Lo mismo (`id` no final + `setId`) |
| `.../postulacion/Postulacion.java` | Lo mismo |
| `.../pago/Pago.java` | Lo mismo |
| `.../persona/Postulante.java` | `setId` ahora también actualiza su correlativo |

No cambia el comportamiento en memoria: los ids siguen autogenerándose igual;
solo que ahora el DAO puede pisarlos con el id real de la BD.

### POMs y ejecución

| Archivo | Cambio |
|---|---|
| `pom.xml` (padre) | Módulos: `admitu_domain`, `admitu_dbmanager`, `admitu_dao`, `admitu_business_logic`, `admitu` |
| `admitu/pom.xml` | Agregadas dependencias a `admitu_business_logic` y `mysql-connector-j:26.7.0` (la misma versión del profe) |
| `admitu/.../main/Principal.java` | Reescrito: antes demo en memoria, ahora CRUD de las 6 entidades vía BO contra el RDS (ver §7). Usa sufijo por timestamp para no chocar con los UNIQUE al correrlo varias veces |

### `admitu_db.sql` (pasó de 514 a 849 líneas, sigue siendo 1 solo archivo)

Se anexaron al final, sin tocar lo anterior: `DROP PROCEDURE IF EXISTS` de las
30 + bloque `DELIMITER $` con los 30 procedures + `DELIMITER ;`:

| Procedures | Qué hacen |
|---|---|
| `INSERTAR/MODIFICAR/ELIMINAR/LISTAR_FACULTAD_X_ID/LISTAR_FACULTADES_TODAS` | CRUD facultad (eliminar = `activo = 0`) |
| `..._CARRERA...` (5) | CRUD carrera con JOIN a facultad |
| `..._CONVOCATORIA...` (5) | CRUD convocatoria |
| `..._POSTULANTE...` (5) | Inserta en `persona` + `postulante` y devuelve el id (como el `INSERTAR_EMPLEADO` del profe) |
| `..._POSTULACION...` (5) | CRUD postulación (eliminar borra primero los pagos hijos, luego historial + postulación, para no chocar con el FK `fk_pago_postulacion`) |
| `..._PAGO...` (5) | CRUD pago (eliminar = `estado = 'RECHAZADO'`) |

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

## 8. Checklist del equipo (qué falta hacer, en orden)

- [ ] Correr `admitu_db.sql` completo en el RDS (`databaseleon.../admitu_db`) y
  verificar 27 tablas + 30 procedures (`SHOW PROCEDURE STATUS`).
- [ ] En IntelliJ: `mvn clean install` (compila los 5 módulos en orden).
- [ ] Correr `pe.edu.pucp.main.Principal` y verificar que termina en
  `Demo terminada` sin errores.
- [ ] Verificar en el Workbench: facultad `FL6-*` con `activo = 0`, pago
  `PAG-L06-*` y postulación demo ya borrados por la limpieza (el `RECHAZADO`
  del pago solo se ve en la consola a mitad de la demo).
- [ ] Repartir la expo: 1 integrante por entidad (6) o por capa
  (DAO / BO / procedures / demo).
- [ ] Llevar a la expo el Workbench abierto (tablas + procedures a la vista).
