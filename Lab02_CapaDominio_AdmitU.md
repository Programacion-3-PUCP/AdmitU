# Lab 02 — Capa de Dominio (Capa del Modelo) del Sistema AdmitU

**Proyecto:** AdmitU — Sistema de gestión del proceso de admisión universitaria
**Curso:** Programación 3 (1INF30-0681) · Tarea Académica · Avance Laboratorio N° 02
**Ciclo:** 2026-2 · 5 integrantes (Los favoritos de Cueva)
**Documento:** Análisis exhaustivo de la capa de dominio y decisiones para el equipo.
**Fecha:** 2026-09-01

---

## 1. Fuentes utilizadas

| Fuente | Rol en este análisis |
|---|---|
| **Rúbrica del Laboratorio N° 02 — 2026-2** (`Rúbrica del Laboratorio N° 02 - 2026-2.pdf`) | Define QUÉ se evalúa en la capa del modelo (6/20 pts) y sus criterios literales. |
| **Informe de la TA del Lab 02 (avance actual)** (`Informe de la Tarea Académica.docx.pdf`) | Estado actual de las clases programadas (Anexo 6) y catálogo 15 RF + 5 RNF. |
| **Informe Grupo 07 — Base de Datos** (`Grupo07_02_Informe.docx.pdf`) | **Base informativa del dominio:** diccionario físico de **29 tablas / 214 columnas / 37 FKs** que define entidades, atributos y reglas. |
| **Material de clase (Programación 3):** Sesión 02 (POO, constructores, herencia, enums, `List`, `LocalDate`) y Sesión 04 (componentes reutilizables, paquetes, cohesión/acoplamiento) | Guía de **cómo** estructurar el código: herencia real, paquetes, tipos seguros. |
| **Conceptos de la wiki:** `poo`, `clases`, `componentes-reutilizables`, `java`, `herencia`, `polimorfismo` | Marco conceptual transversal. |
| **Análisis previos de la wiki (AdmitU):** análisis exhaustivo (21 RF + 7 RNF, mapeo 29 tablas), cumplimiento Lab 02, propuesta de catálogo | Soporte de trazabilidad entidad→RF y roadmap de Labs. |

---

## 2. Qué evalúa la rúbrica en la capa de dominio (base del análisis)

Rubro: **“Programación en JAVA de la capa del modelo (dominio)” — 6.00 puntos.**

Criterios literales de la rúbrica:

- Se han identificado y programado en JAVA **todas las entidades del negocio**, reflejadas en las clases del modelo.
- Se han programado **todos los atributos y relaciones** entre las mismas.
- La programación es **ordenada**, la clase está **correctamente estructurada** (posición de atributos, constructores, getters/setters y métodos correcta).
- Los nombres de los identificadores son **entendibles y siguen convenciones**.
- Las clases se relacionan correctamente y **permiten la navegabilidad** (listas y objetos).

Además, el material de clase (Sesión 04) importa para el criterio de orden: **alta cohesión** (una tarea por clase), **bajo acoplamiento**, **encapsulamiento** y **abstracción**, organizando el código en **paquetes** (componentes).

> **Conclusión de la rúbrica:** “todas las entidades del negocio” = las ~29 entidades del diccionario del informe BD. “Todos los atributos y relaciones” = los ~214 atributos/37 FKs modelados como estado y navegabilidad. Las 4 clases actuales del informe NO cumplen este rubro.

---

## 3. Estado actual (audit de lo que existe)

### 3.1 Lo que estaba en el informe (Anexo del avance)
`enum EstadoPostulante` (REGISTRADO/INSCRITO/OBSERVADO), `enum MetodoPago` (TARJETA_CREDITO/TRANSFERENCIA/EFECTIVO), `Convocatoria`, `Persona` (abstracta), `Postulante extends Persona`, `Pago`, `ProgramaPrincipal`. Solo 4 clases de negocio + 2 enums.

### 3.2 Lo que hay hoy en el directorio del proyecto (37 archivos `.java`, esqueleto en desarrollo)
| Archivo | Estado | Detalle del audit |
|---|---|---|
| `TipoDocumento` | ⚠ **CORREGIR** | Está como `enum`; la BD lo modela como **catálogo** `ADM_TIPOS_DOCUMENTO` (RF020). Debe ser clase entidad. |
| `Parentesco` | ⚠ **CORREGIR** | Igual: `ADM_PARENTESCOS` es catálogo (RF020), no enum. |
| `MedioNotificacion` | ⚠ **CORREGIR** | Igual: `ADM_MEDIOS_NOTIFICACION` es catálogo (RF020). |
| `Pais` | 🔶 **COMPLETAR** | Campos OK (`codigoIso2`, `activo`); faltan getters/setters y normalización del id. |
| `Facultad` | ⚠ **CORREGIR** | Falta atributo `activo` (BD: ACTIVO). |
| `Sede` | ⚠ **CORREGIR** | Falta atributo `activo` (BD: ACTIVO). |
| `MedioPago` | ⚠ **CORREGIR** | Falta atributo `activo` (BD: ACTIVO). `esPasarela` OK (clave para RF011/RF012). |
| `Persona` | ⚠ **CORREGIR** | `tipoDocumento`, `numeroDocumento`, `telefono` NO pertenecen a todas las personas (el `Evaluador` no los tiene en la BD). Moverse a `Postulante`/`Apoderado`. |
| `Postulante` | 🔶 **COMPLETAR** | Recibir `tipoDocumento`+`numeroDocumento`+`telefono`; falta `activo`, getters/setters, métodos de navegación. |
| `Apoderado` | 🔶 **COMPLETAR** | Recibir documento y teléfono; falta `activo`, getters/setters. |
| `Evaluador` | ⚠ **CORREGIR** | Quitar `cargo` (no existe en la BD); agregar `activo`. |
| `InstitucionEducativa` | 🔶 **COMPLETAR** | Falta `tipoGestion` (E/P) — nuevo enum `TipoGestion`; falta `activo`. |
| `AntecedenteAcademico` | 🔶 **COMPLETAR** | Falta `activo`; falta validación `anioInicio <= anioFin` (regla BD). |
| `Convocatoria` | ⚠ **CORREGIR** | Campo `Etapa etapa` es incorrecto (la BD vincula etapas vía **`ConvocatoriaEtapa`** con fechas). Sustituir por `List<ConvocatoriaEtapa>` + `List<Postulacion>`. |
| `Modalidad` | 🔶 **COMPLETAR** | Falta `activo` (BD: ACTIVO) y getters/setters. |
| `Carrera` | 🔶 **COMPLETAR** | Falta `activo` (BD: ACTIVO). |
| `Requisito` | 🔶 **COMPLETAR** | Falta `activo` (BD: ACTIVO). `tipoArchivoRequerido` y `tamanioMaximoBytes` OK. |
| `Etapa` | ⚠ **CORREGIR** | `Fecha_inicio`/`Fecha_fin` (String) NO pertenecen a `Etapa` (es catálogo); van en `ConvocatoriaEtapa` con `LocalDate`. Agregar `activo`. |
| `EstadoPostulacion` | 🔶 **COMPLETAR** | Falta `activo`. Reemplaza al `enum EstadoPostulante` del informe (decisión D2). |
| `ConvocatoriaModalidad` | ⚠ **CORREGIR** | `List<Requisito>` debe ser `List<RequisitoConvocatoriaModalidad>` (la BD vincula requisito↔modalidad de una convocatoria). Falta `activo`. |
| `OfertaCarrera` | 🔶 **COMPLETAR** | Falta `observacion`, `activo`. |
| `Postulacion` | 🔶 **COMPLETAR** | Estructura OK (núcleo con modalidad/carrera elegidas y estado). Faltan getters/setters + métodos `agregar*` y copias defensivas. |
| `PostulacionHistorial` | 🔄 **RENOMBRAR + COMPLETAR** | Nombre propuesto `HistorialPostulacion` (espejo de `ADM_POSTULACION_HIST`). |
| `Pago` | ⚠ **CORREGIR** | `estadoPago` es `double` (**error**): debe ser `enum EstadoPago`. |
| `DocumentoPostulacion` | ⚠ **CORREGIR** | `Requisito requisitoAplicable` debe ser **`RequisitoConvocatoriaModalidad`** (FK real REQ_CONV_MODALIDAD_ID). Faltan `evaluador` y `documentoAnterior` (versiones). |
| `DocumentoObservacion` | 🔄 **RENOMBRAR** | Nombre propuesto `ObservacionDocumento` (espejo de `ADM_OBSERVACIONES_DOCS`). |
| `Notificacion` | 🔶 **COMPLETAR** | Falta `detalleError` (BD: DETALLE_ERROR). |
| `CarnePostulante` | 🔶 **COMPLETAR** | Falta `activo` (BD: ACTIVO). |
| Enums `EstadoConvocatoria, EstadoPago, EstadoDocumento, EstadoObservacion, EstadoEnvio, TipoArchivo, TipoInstitucion, TipoNotificacion, TipoObservacion` | ✅ **OK** | Correctos como enums de flags `CHAR(1)` con reglas CHECK de la BD. |

### 3.3 Faltantes totales (archivos a crear)
`RequisitoConvocatoriaModalidad` (ADM_REQ_CONV_MODALIDADES), `ConvocatoriaEtapa` (ADM_CONV_ETAPAS), `TipoGestion` (flag TIPO_GESTION E/P), `ProgramaPrincipal` (demo de flujo completo).

---

## 4. Método aplicado

1. **Inventario del dominio:** extraído del diccionario físico del informe BD (29 tablas, 6 dominios funcionales).
2. **Mapeo tabla → clase:** 1 tabla = 1 clase de dominio (las tablas asociativas también son clases: representan el estado y la navegabilidad que pide la rúbrica).
3. **Atributos/relaciones:** columnas → atributos privados; FK → **objetos**; relaciones 1:N → **`List<T>`** (navegabilidad).
4. **Reglas base de la rúbrica:** encapsulamiento estricto, getters/setters, constructores, nombres entendibles, copias defensivas en colecciones/fechas.
5. **Contraste con material de clase:** LocalDate (Sesión 02), paquetes y cohesión/acoplamiento (Sesión 04), enums para tipos seguros.

---

## 5. Decisiones punto por punto (pedidas para confirmar con el equipo)

### D1. Los catálogos de referencia son ENTIDADES, no enums
`TipoDocumento`, `Parentesco`, `MedioNotificacion` (y `Pais`, `Facultad`, `MedioPago`, `Sede`) se modelan como **clases** porque en la BD son tablas (`ADM_*`) gestionadas por **RF020** (CRUD de catálogos). Un `enum` no permite CRUD de catálogo ni el flag `activo`.
> ✓ **Confirmado.**

### D2. El estado de postulación es ENTIDAD `EstadoPostulacion`, no `enum`
El informe usaba `enum EstadoPostulante` con 3 valores. El dominio real (Fig. 2 del informe BD y SCRIPT) tiene 8 estados con historial auditable (`ADM_POSTULACION_HIST`) y RF018/RF013/RNF07. Se gestiona como **catálogo** (RF020) y la `Postulacion` referencia un objeto `EstadoPostulacion`. *(Códigos conocidos al momento: BORRADOR, PAGO_PENDIENTE, DOCUMENTOS_PENDIENTES, EN_REVISION, OBSERVADA, RECHAZADA, FINALIZADA — validar conteo exacto contra el SCRIPT SQL del Lab 04; pendiente confirmar).*
> ✓ **Confirmado.**

### D3. El medio de pago es ENTIDAD `MedioPago`, no `enum`
El informe usaba `enum MetodoPago`. La BD guarda `ES_PASARELA` (distingue pasarela → RF011, de conciliación manual → RF012). Ese atributo se pierde en un enum.
> ✓ **Confirmado.** (Los enums `MetodoPago` y `EstadoPostulante` del informe se **eliminan** al incorporarse sus valores como datos de estas entidades.)

### D4. Jerarquía de herencia REAL y mínima
Solo se hereda donde existe "es un": `Postulante extends Persona`, `Apoderado extends Persona`, `Evaluador extends Persona`. NO se crea una base artificial para catálogos (bajo acoplamiento; la rúbrica valora herencia real, no forzada).
`Persona` (abstracta) contiene: `id`, `nombres`, `apellidoPaterno`, `apellidoMaterno`, `correo`. **NO** incluye `tipoDocumento/numeroDocumento/telefono` porque `Evaluador` no los tiene (decisión sobre el archivo actual).
> ✓ **Confirmado.**

### D5. Fechas con `LocalDate` (java.time), no `Date`
El material (Sesión 02) usa `LocalDate`/`LocalDateTime`. Es inmutable → **no hace falta copia defensiva de fechas**; las copias defensivas se aplican solo a las colecciones `List<T>`. Simplifica el código y mantiene la integridad del estado.
> ✓ **Confirmado.**

### D6. Identificadores uniformes
Cada clase con estado propio lleva: `private static int siguienteId = 1;` + `private final int id;` asignada en el constructor. Se unifica el esquema del `Persona` actual (que usaba setter) y de `Convocatoria`/`Pago` (que ya usaban `final`).
> ✓ **Confirmado.**

### D7. Encapsulamiento + copias defensivas en getters de colecciones
Todos los atributos `private`; getters/setters convencionales; los getters de `List` devuelven `new ArrayList<>(lista)` y los setters copian; métodos de navegación explícitos (`agregarXxx`, `removerXxx`) en lugar de exposición directa. (Requisito de la rúbrica sobre "listas y objetos".)
> ✓ **Confirmado.**

### D8. Navegabilidad fiel al modelo relacional
Cada FK del modelo BD se programa como objeto; cada relación 1:N como `List`. Decisiones concretas sobre lo existente:
- `Convocatoria` NO tiene `List<Postulante>` directa; navega vía **`List<Postulacion>`** (1:Postulante-N:Postulacion-N:1:Convocatoria).
- `Convocatoria` gestiona configuración con `List<ConvocatoriaModalidad>`, `List<OfertaCarrera>`, `List<ConvocatoriaEtapa>` (RF017).
- `Postulacion` posee `List<Pago>`, `List<DocumentoPostulacion>`, `List<PostulacionHistorial>`, `List<Notificacion>` y `CarnePostulante carne` (0..1).
- `DocumentoPostulacion` referencia al **requisito de la modalidad** (`RequisitoConvocatoriaModalidad`) y a su `DocumentoPostulacion documentoAnterior` (versiones).
- `Pago` pertenece a la **`Postulacion`** (POSTULACION_ID), no al `Postulante` directo.
> ✓ **Confirmado.**

### D9. Paquete de organización (componente reutilizable, Sesión 04)
Estructura `edu.pucp.admitu.model.<subdominio>` + `edu.pucp.admitu.app`. Subdominios espejo de los 6 dominios funcionales del informe BD: `catalogos`, `personas`, `configuracion`, `postulacion`, `pagos`, `comunicacion`; tipos de valor seguro en `model.enums`.
> ✓ **Confirmado.** (Nombre base alterable; consensuar: `com.xxx.admitu` o `model` simple.)

### D10. Enums solo para flags `CHAR(1)` con CHECK (y no catálogos con CRUD)
Se conservan/crean como enums: `EstadoConvocatoria` (B/P/C/A), `EstadoPago` (G/P/A/R/V), `EstadoDocumento` (P/A/O/R), `EstadoObservacion` (P/S), `EstadoEnvio` (P/E/F), `TipoArchivo` (P/I/A), `TipoInstitucion` (C/U), `TipoGestion` (E/P) — nuevo —, `TipoNotificacion` (V/P/O/F/C), `TipoObservacion` (F/I/V/O).
> ✓ **Confirmado.**

### D11. Los roles ADMINISTRADOR/FINANZAS/ADMISIÓN NO son clases de dominio
No existen tablas dedicadas en el modelo de 29; son roles de autenticación/autorización (**RNF06**) que se resuelven en la capa de acceso (Labs 07/09+). No crear `Administrador.java`.
> ✓ **Confirmado.**

### D12. Nombres en español y alineados al diccionario BD
Identificadores en español (atributos en camelCase, clases en PascalCase) trazables a las columnas del informe BD (`ADM_POSTULACION_HIST` → `HistorialPostulacion`, `ADM_OBSERVACIONES_DOCS` → `ObservacionDocumento`, `ADM_REQ_CONV_MODALIDADES` → `RequisitoConvocatoriaModalidad`).
> ✓ **Confirmado.**

### D13. Correcciones puntuales heredadas del esqueleto actual
- `Pago.estadoPago`: `double` → `EstadoPago` (error detectado).
- `Etapa`: quitar fechas (String) → catálogo limpio (`codigoEtapa`, `nombre`, `descripcion`, `activo`); fechas viven en `ConvocatoriaEtapa` (`LocalDate`).
- `Evaluador.cargo`: eliminar (no existe en BD); agregar `activo`.
- `Notificacion`: agregar `detalleError`.
- Todos los catálogos/entidades con `ACTIVO` en BD llevan `boolean activo`.
> ✓ **Confirmado.**

### D14. `ProgramaPrincipal` como demo del flujo completo
`ProgramaPrincipal` (en `app`) construye un escenario coherente (Catálogos → Convocatoria 2026-2 PUBLICADA → Postulante → Postulación BORRADOR → Pago → Documentos → EN_REVISION), usando la **navegabilidad** (listas/objetos). La rúbrica puntúa la exposición; el dato de prueba `fechaInicio==fechaFin` del informe se corrige.
> ✓ **Confirmado.**

### D15. Comportamiento en el dominio: `toString()` descriptivo (no `println`)
El informe usaba `mostrarInformacion()` con `System.out.println` (mezcla presentación en el dominio). Se reemplaza por `toString()` sobreescrito en las clases relevantes (deuda técnica detectada en el análisis de cumplimiento del Lab 02).
> ✓ **Confirmado.**

---

## 6. Árbol completo final de la capa de dominio

```
src/
└── edu/pucp/admitu/
    ├── model/
    │   ├── catalogos/
    │   │   ├── TipoDocumento.java          [CORREGIR: enum → entidad]   ADM_TIPOS_DOCUMENTO
    │   │   ├── Parentesco.java             [CORREGIR: enum → entidad]   ADM_PARENTESCOS
    │   │   ├── Pais.java                   [COMPLETAR]                 ADM_PAISES
    │   │   ├── Facultad.java               [CORREGIR: +activo]         ADM_FACULTADES
    │   │   ├── MedioPago.java              [CORREGIR: +activo]         ADM_MEDIOS_PAGO
    │   │   ├── MedioNotificacion.java      [CORREGIR: enum → entidad]  ADM_MEDIOS_NOTIFICACION
    │   │   └── Sede.java                   [CORREGIR: +activo]         ADM_SEDES
    │   ├── personas/
    │   │   ├── Persona.java                [CORREGIR: quitar doc/tel, abstracta]
    │   │   ├── Postulante.java             [COMPLETAR]                 ADM_POSTULANTES
    │   │   ├── Apoderado.java              [COMPLETAR]                 ADM_APODERADOS
    │   │   ├── Evaluador.java              [CORREGIR: −cargo +activo]  ADM_EVALUADORES
    │   │   ├── InstitucionEducativa.java   [COMPLETAR: +tipoGestion]   ADM_INSTITUCIONES_EDUCATIVAS
    │   │   └── AntecedenteAcademico.java   [COMPLETAR]                 ADM_ANTECEDENTES_ACADEMICOS
    │   ├── configuracion/
    │   │   ├── Convocatoria.java           [CORREGIR: List<ConvocatoriaEtapa>, List<Postulacion>]  ADM_CONVOCATORIAS
    │   │   ├── Modalidad.java              [COMPLETAR]                 ADM_MODALIDADES
    │   │   ├── Carrera.java                [COMPLETAR]                 ADM_CARRERAS
    │   │   ├── Requisito.java              [COMPLETAR]                 ADM_REQUISITOS
    │   │   ├── Etapa.java                  [CORREGIR: −fechas +activo] ADM_ETAPAS
    │   │   ├── EstadoPostulacion.java      [COMPLETAR: +activo]        ADM_ESTADOS_POSTULACION
    │   │   ├── ConvocatoriaModalidad.java  [CORREGIR: requisitos→RequisitoConvocatoriaModalidad, +activo]  ADM_CONV_MODALIDADES
    │   │   ├── OfertaCarrera.java          [COMPLETAR]                 ADM_OFERTAS_CARRERAS
    │   │   ├── RequisitoConvocatoriaModalidad.java  [CREAR]            ADM_REQ_CONV_MODALIDADES
    │   │   └── ConvocatoriaEtapa.java      [CREAR]                     ADM_CONV_ETAPAS
    │   ├── postulacion/
    │   │   ├── Postulacion.java            [COMPLETAR]                 ADM_POSTULACIONES (núcleo)
    │   │   └── HistorialPostulacion.java   [RENOMBRAR: PostulacionHistorial]  ADM_POSTULACION_HIST
    │   ├── pagos/
    │   │   ├── Pago.java                   [CORREGIR: estadoPago=EstadoPago]  ADM_PAGOS
    │   │   ├── DocumentoPostulacion.java   [CORREGIR: requisito→RequisitoConvocatoriaModalidad, +anterior/evaluador]  ADM_DOCUMENTOS_POSTULACION
    │   │   └── ObservacionDocumento.java   [RENOMBRAR: DocumentoObservacion]  ADM_OBSERVACIONES_DOCS
    │   ├── comunicacion/
    │   │   ├── Notificacion.java           [COMPLETAR: +detalleError]  ADM_NOTIFICACIONES
    │   │   └── CarnePostulante.java        [COMPLETAR]                 ADM_CARNES_POSTULANTES
    │   └── enums/
    │       ├── EstadoConvocatoria.java     [OK]   (B/P/C/A)
    │       ├── EstadoPago.java             [OK]   (G/P/A/R/V)
    │       ├── EstadoDocumento.java        [OK]   (P/A/O/R)
    │       ├── EstadoObservacion.java      [OK]   (P/S)
    │       ├── EstadoEnvio.java            [OK]   (P/E/F)
    │       ├── TipoArchivo.java            [OK]   (P/I/A)
    │       ├── TipoInstitucion.java        [OK]   (C/U)
    │       ├── TipoGestion.java            [CREAR]  (E/P)  ← institución educativa
    │       ├── TipoNotificacion.java       [OK]   (V/P/O/F/C)
    │       └── TipoObservacion.java        [OK]   (F/I/V/O)
    └── app/
        └── ProgramaPrincipal.java          [CREAR]  demo de flujo completo
```

**Totales: 30 clases entidad (incl. `Persona` abstracta) + 10 enums + 1 programa = 41 archivos.**
(Sobre los 37 existentes: 4 se crean, 2 se renombran, 3 pasan de enum→entidad, y el resto se completa/corrige.)

---

## 7. Mapeo de cobertura: 29 tablas del informe BD → clases (trazabilidad)

| Dominio (BD) | Tablas | RF | Clases Java |
|---|---|---|---|
| Catálogos de referencia (7) | ADM_TIPOS_DOCUMENTO, ADM_PARENTESCOS, ADM_PAISES, ADM_FACULTADES, ADM_MEDIOS_PAGO, ADM_MEDIOS_NOTIFICACION, ADM_SEDES | RF020 | `TipoDocumento`, `Parentesco`, `Pais`, `Facultad`, `MedioPago`, `MedioNotificacion`, `Sede` |
| Personas e instituciones (5) | ADM_APODERADOS, ADM_INSTITUCIONES_EDUCATIVAS, ADM_POSTULANTES, ADM_ANTECEDENTES_ACADEMICOS, ADM_EVALUADORES | RF002, RF005, RF021 | `Apoderado`, `InstitucionEducativa`, `Postulante`, `AntecedenteAcademico`, `Evaluador` (+`Persona`) |
| Configuración de admisión (10) | ADM_CONVOCATORIAS, ADM_MODALIDADES, ADM_CARRERAS, ADM_REQUISITOS, ADM_ETAPAS, ADM_ESTADOS_POSTULACION, ADM_CONV_MODALIDADES, ADM_OFERTAS_CARRERAS, ADM_REQ_CONV_MODALIDADES, ADM_CONV_ETAPAS | RF001, RF004, RF006, RF007, RF017, RF013 | `Convocatoria`, `Modalidad`, `Carrera`, `Requisito`, `Etapa`, `EstadoPostulacion`, `ConvocatoriaModalidad`, `OfertaCarrera`, `RequisitoConvocatoriaModalidad`, `ConvocatoriaEtapa` |
| Operación de postulación (2) | ADM_POSTULACIONES, ADM_POSTULACION_HIST | RF016, RF018, RNF07, RF013 | `Postulacion`, `HistorialPostulacion` |
| Pagos y documentos (3) | ADM_PAGOS, ADM_DOCUMENTOS_POSTULACION, ADM_OBSERVACIONES_DOCS | RF008, RF010, RF011, RF012, RF018 | `Pago`, `DocumentoPostulacion`, `ObservacionDocumento` |
| Comunicación y cierre (2) | ADM_NOTIFICACIONES, ADM_CARNES_POSTULANTES | RF019, RF014 | `Notificacion`, `CarnePostulante` |

---

## 8. Modelo de navegabilidad (asociaciones principales)

```
Postulante 1───* Postulacion *───1 Convocatoria
Postulante 1───* AntecedenteAcademico *───1 InstitucionEducativa
Postulante 0..1───1 Apoderado
Convocatoria 1───* ConvocatoriaModalidad *───1 Modalidad
Convocatoria 1───* OfertaCarrera *───1 Carrera
Convocatoria 1───* ConvocatoriaEtapa *───1 Etapa
ConvocatoriaModalidad 1───* RequisitoConvocatoriaModalidad *───1 Requisito
Postulacion *───1 ConvocatoriaModalidad  (modalidad de la MISMA convocatoria)
Postulacion *───1 OfertaCarrera         (carrera de la MISMA convocatoria)
Postulacion *───1 EstadoPostulacion     (estado actual)
Postulacion 1───* HistorialPostulacion  (estado anterior/actual)
Postulacion 1───* Pago *───1 MedioPago
Postulacion 1───* DocumentoPostulacion *───1 RequisitoConvocatoriaModalidad, 1┴0..1 evaluador, 0..1 anterior
DocumentoPostulacion 1───* ObservacionDocumento *───1 Evaluador
Postulacion 1───* Notificacion *───1 MedioNotificacion
Postulacion 0..1───1 CarnePostulante *───1 Sede
```

---

## 9. Mermaid (diagrama de clases — opcional para la exposición)

```mermaid
classDiagram
    class Persona { <<abstract>> +int id +String nombres +String apellidoPaterno +String apellidoMaterno +String correo }
    class Postulante
    class Apoderado
    class Evaluador
    Postulante --|> Persona
    Apoderado --|> Persona
    Evaluador --|> Persona
    Postulante "1" --> "0..1" Apoderado
    Postulante "1" --> "*" AntecedenteAcademico
    AntecedenteAcademico "*" --> "1" InstitucionEducativa
    InstitucionEducativa "*" --> "1" Pais
    Carrera "*" --> "1" Facultad

    class Convocatoria
    class ConvocatoriaModalidad
    class OfertaCarrera
    class ConvocatoriaEtapa
    class RequisitoConvocatoriaModalidad
    class EstadoPostulacion
    Convocatoria "1" --> "*" ConvocatoriaModalidad
    Convocatoria "1" --> "*" OfertaCarrera
    Convocatoria "1" --> "*" ConvocatoriaEtapa
    ConvocatoriaModalidad "1" --> "*" RequisitoConvocatoriaModalidad
    RequisitoConvocatoriaModalidad "*" --> "1" Requisito
    ConvocatoriaEtapa "*" --> "1" Etapa

    class Postulacion
    Postulante "1" --> "*" Postulacion
    Convocatoria "1" --> "*" Postulacion
    Postulacion "*" --> "1" ConvocatoriaModalidad
    Postulacion "*" --> "1" OfertaCarrera
    Postulacion "*" --> "1" EstadoPostulacion

    class HistorialPostulacion
    Postulacion "1" --> "*" HistorialPostulacion

    class Pago
    class DocumentoPostulacion
    class ObservacionDocumento
    Postulacion "1" --> "*" Pago
    Pago "*" --> "1" MedioPago
    Postulacion "1" --> "*" DocumentoPostulacion
    DocumentoPostulacion "*" --> "1" RequisitoConvocatoriaModalidad
    DocumentoPostulacion "0..1" --> "0..1" DocumentoPostulacion
    DocumentoPostulacion "1" --> "*" ObservacionDocumento
    HistorResumen omitted (detalle en sección 8)
```

---

## 10. Convenciones de código (checklist para todo `model/`)

- Atributos `private`; getters/setters públicos; `final` para `id`.
- `static int siguienteId` para secuencia por entidad (simula las secuencias SQL; Lab 04 las reemplazará por la BD).
- `LocalDate`/`LocalDateTime` (java.time) para fechas.
- Getters de colecciones: copia defensiva (`new ArrayList<>(lista)`); setters copian también.
- Métodos `agregarXxx`/`removerXxx` y `equals`/`hashCode` por `id` cuando se requiera.
- `toString()` descriptivo en clases principales (no `println`).
- Sin dependencias de UI ni de persistencia en `model/` (alta cohesión + bajo acoplamiento).
- Nombres en español, camelCase / PascalCase, trazables a las columnas del informe BD.

---

## 11. Reutilización en Labs futuros

| Lab | Qué habilita esta capa |
|---|---|
| **L04 (16/09)** | Mapeo directo 1:1 clase ↔ tabla para el SCRIPT SQL y la capa de persistencia CRUD (≥4 entidades). |
| **L07 (07/10)** | Dominio/persistencia/lógica de negocio al 100%: las reglas RF016/RF017/RF018 y RNF07 ya tienen su estado y navegabilidad. |
| **L09/11/12** | Serialización SOAP/REST del modelo de dominio (RNF01) y validaciones de negocio. |

---

## 12. Puntos a validar con el equipo / profesor

1. **Seed exacto de estados** de `ADM_ESTADOS_POSTULACION` (códigos y conteo — los 8 estados de la Fig. 2 del informe BD) → se fija en el Lab 04 con el SCRIPT SQL.
2. **Nombre base del paquete** (`edu.pucp.admitu...` u otro) — consensuar.
3. **Renombres propuestos:** `PostulacionHistorial`→`HistorialPostulacion`, `DocumentoObservacion`→`ObservacionDocumento` (alinear con el diccionario BD).
4. **`activo` (flag S/N)** se modela como `boolean` en Java (convención); en el Lab 04 se mapea a `CHAR(1)`.
5. El **diagrama de clases UML es opcional** en la rúbrica, pero se recomienda presentarlo en la exposición (apoya el rubro de navegabilidad y orden).

---

## 13. Fichas detalladas de los archivos de la PRÓXIMA iteración (alcance inmediato)

> Alcance acordado: **4 archivos faltantes + 3 conversiones enum→entidad + correcciones puntuales**. Lo demás (completar esqueletos) queda como plan para el equipo. Estas fichas bastan para implementar cada archivo sin consultar de nuevo las fuentes.

### 13.1 `model/configuracion/RequisitoConvocatoriaModalidad.java` — NUEVO
- **Tabla:** `ADM_REQ_CONV_MODALIDADES` · **RF:** RF017 (configuración de convocatoria) y RF018/RF008 (validación de requisitos).
- **Atributos:**
  - `private final int id;` (+ `private static int siguienteId = 1;`)
  - `private ConvocatoriaModalidad convocatoriaModalidad;` — FK/UQ (CONV_MODALIDAD_ID)
  - `private Requisito requisito;` — FK/UQ (REQUISITO_ID)
  - `private boolean obligatorio;` — OBLIGATORIO = S/N
  - `private int ordenPresentacion;` — ORDER_PRESENTACION (nullable)
  - `private boolean activo;` — ACTIVO
- **Reglas BD:** único (convocatoriaModalidad, requisito); `ordenPresentacion > 0`.
- **Relación clave:** es la clase que `DocumentoPostulacion.requisitoAplicable` debe referenciar (corrección D8). Navegabilidad: lista en `ConvocatoriaModalidad.requisitos`.

### 13.2 `model/configuracion/ConvocatoriaEtapa.java` — NUEVO
- **Tabla:** `ADM_CONV_ETAPAS` · **RF:** RF017 (cronograma de convocatoria).
- **Atributos:**
  - `private final int id;` (+ `siguienteId`)
  - `private Convocatoria convocatoria;` — FK/UQ
  - `private Etapa etapa;` — FK/UQ
  - `private LocalDate fechaInicio;` / `private LocalDate fechaFin;`
  - `private String descripcion;` — nullable
  - `private boolean activo;`
- **Reglas BD:** único (convocatoria, etapa); `fechaInicio <= fechaFin`.
- **Corrige el error del esqueleto:** las fechas NO van en `Etapa`; `Convocatoria.etapa` (objeto suelto) se reemplaza por `List<ConvocatoriaEtapa>`.

### 13.3 `model/enums/TipoGestion.java` — NUEVO
- **Tabla de origen:** flag `TIPO_GESTION` de `ADM_INSTITUCIONES_EDUCATIVAS`.
- `public enum TipoGestion { ESTATAL, PARTICULAR }` (mapa a CHAR E/P; documentar en el Lab 04).
- Lo usa `InstitucionEducativa` (falta en el esqueleto actual).

### 13.4 `app/ProgramaPrincipal.java` — NUEVO
- **Rol:** demostrar la capa (rubro "Exposición" del Lab 02). No es dominio; vive en `app`.
- **Flujo de ejemplo:**
  1. Crear catálogos: `TipoDocumento`, `Pais`, `Facultad`, `MedioPago`, `Sede`, `Etapa`, `EstadoPostulacion` (BORRADOR… FINALIZADA).
  2. Crear `Convocatoria` "2026-2" con `LocalDate` válidas y estado `PUBLICADA`.
  3. Configurar: `ConvocatoriaModalidad` (costo 350.0) → `RequisitoConvocatoriaModalidad` (obligatorio) → `OfertaCarrera` (vacantes) → `ConvocatoriaEtapa`.
  4. Crear `Postulante` → registrar `Postulacion` en estado `BORRADOR` eligiendo modalidad/carrera de la misma convocatoria.
  5. `Postulacion.agregarPago(...)` con `MedioPago` → `DocumentoPostulacion` con su requisito → transición a `EN_REVISION` registrando `HistorialPostulacion` → emisión de `CarnePostulante`.
  6. Imprimir el escenario con `toString()` (navegabilidad a la vista en la exposición).
- **Corrige el dato pobre del informe** (`fechaInicio == fechaFin` → fechas de 2026-2 reales).

### 13.5 `model/catalogos/TipoDocumento.java` — CONVERTIR (enum → entidad)
- **Tabla:** `ADM_TIPOS_DOCUMENTO` (RF020). Guarda el CRUD de catálogo que un enum no permite.
- `private final int id; private String codigo; private String nombre; private boolean activo;` + getters/setters. Los valores DNI/CE/PAS/CPP pasan a ser **datos** (seed), no constantes.

### 13.6 `model/catalogos/Parentesco.java` — CONVERTIR (enum → entidad)
- **Tabla:** `ADM_PARENTESCOS` (RF020). `id`, `codigo`, `nombre`, `activo`.
- Referenciado por `Apoderado.parentesco` (entidad).

### 13.7 `model/catalogos/MedioNotificacion.java` — CONVERTIR (enum → entidad)
- **Tabla:** `ADM_MEDIOS_NOTIFICACION` (RF020). `id`, `codigo`, `nombre`, `activo`.
- Referenciado por `Notificacion.medioNotificacion` (entidad).

### 13.8 Correcciones puntuales que SÍ se aplican (no se crean archivos nuevos)
| Archivo | Cambio |
|---|---|
| `Pago` | `estadoPago` de `double` → `EstadoPago estadoPago` (error). |
| `Etapa` | quitar `Fecha_inicio`/`Fecha_fin` (String) → solo catálogo + `activo`. |
| `Convocatoria` | `Etapa etapa` → `List<ConvocatoriaEtapa>`; añadir `List<Postulacion>`. |
| `Evaluador` | quitar `cargo`; añadir `activo`. |
| `Persona` | quitar `tipoDocumento/numeroDocumento/telefono` (pasan a `Postulante`/`Apoderado`). |
| `Notificacion` | añadir `detalleError`. |
| `DocumentoPostulacion` | `requisitoAplicable` → `RequisitoConvocatoriaModalidad`; añadir `evaluador` y `documentoAnterior`. |
| `InstitucionEducativa` | añadir `TipoGestion tipoGestion` y `activo`. |
| `ConvocatoriaModalidad` | `List<Requisito>` → `List<RequisitoConvocatoriaModalidad>`; añadir `activo`. |
| `CarnePostulante`, `Modalidad`, `Carrera`, `Requisito`, `OfertaCarrera`, `AntecedenteAcademico`, `Facultad`, `Sede`, `MedioPago` | añadir `activo` donde falta (BD: ACTIVO). |

---

## 14. CHECKLIST — Archivos para presentar en el Lab 02 (los 41)

> Columna **Estado**: `N` = nuevo a crear · `C` = convertir/corregir · `R` = renombrar · `OK` = queda igual. **(Aún NO se generan; esto es la lista final acordada.)**

| # | Ruta final | Archivo | Estado |
|---|---|---|---|
| 1 | model/catalogos | `TipoDocumento.java` | C (enum→entidad) |
| 2 | model/catalogos | `Parentesco.java` | C (enum→entidad) |
| 3 | model/catalogos | `MedioNotificacion.java` | C (enum→entidad) |
| 4 | model/catalogos | `Pais.java` | C (+getters/id) |
| 5 | model/catalogos | `Facultad.java` | C (+activo) |
| 6 | model/catalogos | `MedioPago.java` | C (+activo) |
| 7 | model/catalogos | `Sede.java` | C (+activo) |
| 8 | model/personas | `Persona.java` | C (quitar doc/tel) |
| 9 | model/personas | `Postulante.java` | C (+doc/tel, activo) |
| 10 | model/personas | `Apoderado.java` | C (+doc/tel, activo) |
| 11 | model/personas | `Evaluador.java` | C (−cargo, +activo) |
| 12 | model/personas | `InstitucionEducativa.java` | C (+TipoGestion, activo) |
| 13 | model/personas | `AntecedenteAcademico.java` | C (+activo) |
| 14 | model/configuracion | `Convocatoria.java` | C (+List<ConvocatoriaEtapa>, List<Postulacion>) |
| 15 | model/configuracion | `Modalidad.java` | C (+activo) |
| 16 | model/configuracion | `Carrera.java` | C (+activo) |
| 17 | model/configuracion | `Requisito.java` | C (+activo) |
| 18 | model/configuracion | `Etapa.java` | C (−fechas, +activo) |
| 19 | model/configuracion | `EstadoPostulacion.java` | C (+activo) |
| 20 | model/configuracion | `ConvocatoriaModalidad.java` | C (+requisitos→RequisitoConvocatoriaModalidad, activo) |
| 21 | model/configuracion | `OfertaCarrera.java` | C (+observacion, activo) |
| 22 | model/configuracion | `RequisitoConvocatoriaModalidad.java` | **N** |
| 23 | model/configuracion | `ConvocatoriaEtapa.java` | **N** |
| 24 | model/postulacion | `Postulacion.java` | C (completar métodos/copias) |
| 25 | model/postulacion | `HistorialPostulacion.java` | **R** (desde `PostulacionHistorial`) |
| 26 | model/pagos | `Pago.java` | C (estadoPago=EstadoPago) |
| 27 | model/pagos | `DocumentoPostulacion.java` | C (+requisito→RequisitoConvocatoriaModalidad, evaluador, anterior) |
| 28 | model/pagos | `ObservacionDocumento.java` | **R** (desde `DocumentoObservacion`) |
| 29 | model/comunicacion | `Notificacion.java` | C (+detalleError) |
| 30 | model/comunicacion | `CarnePostulante.java` | C (+activo) |
| 31 | model/enums | `EstadoConvocatoria.java` | OK |
| 32 | model/enums | `EstadoPago.java` | OK |
| 33 | model/enums | `EstadoDocumento.java` | OK |
| 34 | model/enums | `EstadoObservacion.java` | OK |
| 35 | model/enums | `EstadoEnvio.java` | OK |
| 36 | model/enums | `TipoArchivo.java` | OK |
| 37 | model/enums | `TipoInstitucion.java` | OK |
| 38 | model/enums | `TipoGestion.java` | **N** |
| 39 | model/enums | `TipoNotificacion.java` | OK |
| 40 | model/enums | `TipoObservacion.java` | OK |
| 41 | app | `ProgramaPrincipal.java` | **N** |

**Resumen:** 4 nuevos (`RequisitoConvocatoriaModalidad`, `ConvocatoriaEtapa`, `TipoGestion`, `ProgramaPrincipal`) · 2 renombres · 3 conversiones enum→entidad · 28 con corrección/completado · 10 enums OK.

---

## 15. Evaluación de una propuesta alternativa recibida del equipo (modelo con `Usuario`)

Se recibió una versión alternativa en un solo archivo (clases `Usuario`/`Administrador`/`Postulante extends Usuario`, `Convocatoria`, `Carrera`, `Modalidad`, `RequisitoDocumental`, `Documento`, `Observacion`, `Pago`, `Carne`, enums `EstadoPostulacion` y `MetodoPago`). **Veredicto: NO incorporarla tal cual.** La propuesta mejora la redacción OO (constructores con parámetros, copias defensivas) pero **pierde fidelidad con el informe BD**, que es la base informativa. Comparativa por clase:

| Elemento de la propuesta | Qué aporta | Problema contra el informe BD / el plan | Decisión |
|---|---|---|---|
| `enum EstadoPostulacion {REGISTRADO, INSCRITO, OBSERVADO, ADMITIDO}` | Enum legible | No coincide con los 8 estados reales; `ADMITIDO` NO existe en el dominio (el modelo excluye administración de vacantes); la BD gestiona el estado como **catálogo** (RF020) con historial (RF013/RNF07) | ❌ **Rechazar** → entidad `EstadoPostulacion` (D2) |
| `enum MetodoPago` | Enum legible | Pierde `ES_PASARELA` (RF011 vs RF012) | ❌ **Rechazar** → entidad `MedioPago` (D3) |
| `abstract class Usuario` (+`password`) | Base con credencial RNF03 | No existe tabla USUARIOS en el modelo de 29; mezcla **roles/autenticación** con **personas** del dominio; `Administrador` no tiene tabla; la encriptación es capa de seguridad (RNF03), no del dominio | ❌ **Rechazar en dominio** → `Persona` (D4) + roles a cargo de RNF06 en la capa de acceso (D11). *Pendiente de validar con el profe si quieren evidenciar RNF03 en el modelo* |
| `Postulante extends Usuario` con `List<Pago>`, `List<Documento>`, `Carne carneGenerado` | Más navegación que el informe | Los pagos/documentos/carne cuelgan de la **Postulación** (FK del BD), no del postulante; **omite la entidad núcleo `Postulacion`** y su historial | ❌ → `Postulante extends Persona` + navegación vía `Postulacion` (D8) |
| `Convocatoria` con `List<Carrera>`, `List<Modalidad>`, `List<Postulante>` | Catálogos en convocatoria | Pierde las asociativas `OfertaCarrera` (vacantes/costo por convocatoria), `ConvocatoriaModalidad`, `ConvocatoriaEtapa` (RF017); `List<Postulante>` directa es navegación incorrecta | ❌ → listas de asociativas (D8) |
| `Carrera` con `facultad` (String) y `vacantesTotales` | Compacta | La facultad es objeto (`Facultad`, RF020); las **vacantes son por convocatoria** (`OfertaCarrera`), no atributo de la carrera | ❌ → `Carrera` catálogo + `OfertaCarrera` |
| `Modalidad` con `List<RequisitoDocumental> requisitosBase` | Simple | Los requisitos se asignan por **convocatoria+modalidad** (`RequisitoConvocatoriaModalidad`); la modalidad lleva `requiereColegio/requiereUniversidad` (RF005) | ❌ → `ConvocatoriaModalidad` + `RequisitoConvocatoriaModalidad` |
| `RequisitoDocumental` con `nombreArchivoEsperado` y `esObligatorio` | Simple | `esObligatorio` es de la **asignación** (no del requisito); el catálogo `Requisito` tiene `tipoArchivoRequerido`/`tamanioMaximoBytes` | ❌ → `Requisito` (catálogo) + asignación |
| `Documento` | Esquema de versión | Le falta `estadoDocumento` (P/A/O/R), número de versión, `evaluador`, `documentoAnterior`, fechas/tamaño; referencia al requisito asignado | ❌ → `DocumentoPostulacion` |
| `Observacion` | Esquema de observación | Le falta `tipoObservacion` (F/I/V/O), `estado` (P/S), subsanación y evaluador | ❌ → `ObservacionDocumento` |
| `Pago` con `conciliado` (boolean) | Aproxima RF012 | Pierde la máquina de estados G/P/A/R/V, `numeroIntento`, montos/descuento, validación | ❌ → `Pago` completo + `EstadoPago` |
| `Carne` | Esquema de carné | Le falta `sede`, vigencia, `aulaExamen`, `codigoCarne`, vínculo a postulación FINALIZADA (gate RF014) | ❌ → `CarnePostulante` |
| Uso de `Date` + `clone()` | Copia defensiva | Material de clase usa `LocalDate` (D5) | ❌ → `LocalDate` |
| **Patrón de la propuesta:** constructores con parámetros + copias defensivas en `Date`/`List` | ✅ Buen estilo | Ya incorporado en el plan (D7) | ✔ **Salvar** el estilo |

**¿Algo se "ya estaba considerando"?** Sí: cada entidad de la propuesta (Postulante, Convocatoria, Carrera, Modalidad, Requisito, Documento, Observacion, Pago, Carne) tiene su equivalente **de mayor fidelidad** en el árbol del plan (secciones 6 y 14). Lo único nuevo que la propuesta introduce y el plan omitía intencionalmente es el **`Usuario`/`Administrador` con credencial** — única decisión abierta para el equipo (ver sección 12, punto 6).

**Acción recomendada:** conservar la **forma** (estilo OO, patrones) y adoptar la **estructura** del plan (29 tablas). Si el equipo insiste en `Usuario`, se propone **separarlo de las personas** (una clase `CredencialUsuario`/`Usuario` de autenticación en la capa de acceso de los Labs 07/09+), NUNCA como base de `Persona`.