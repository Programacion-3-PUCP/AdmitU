# AdmitU — Qué cambió del Lab02 al Lab04 y por qué (guía del equipo)

**Equipo Los Favoritos de Cueva — Programación 3 (1INF30).**
Este documento describe, en lenguaje simple, qué se modificó en el código de
`proyecto/`, el motivo de cada cambio, y cómo se ordenará en paquetes.
El código quedó **sin comentarios** a propósito: la explicación está aquí.

> **Lectura rápida: sección 1 y tabla de la sección 4.**

## 1. Resumen en 10 líneas

1. El punto de partida son las **40 clases del Lab02** (nota 19.00/20).
2. El JP solicitó: atributo de **baja lógica** (`Estado`), definir qué es
   **CRUD y qué es precargado**, simplificar **pagos/finanzas** y definir el
   **canal de notificaciones**.
3. Un compañero propuso mejoras (carpeta `TA_...`, no se tocó): acertó en casi
   todo, pero su `Pago` simplificado **rompía el modelo** (lo explicamos abajo).
4. El equipo acordó: **Finanzas fuera**, pagos solo por **pasarela**
   (Yape/tarjeta/banco).
5. Se agregó `activo` (sí/no) a **9 clases** para la baja lógica.
6. `Pago` quedó en versión **híbrida de pasarela**: simple, pero con FK y
   referencia de transacción.
7. Notificaciones ahora son **correo + bandeja del sistema** (chau SMS/WhatsApp),
   con `leída/fechaLectura`.
8. Se blindaron las listas contra `null` (adiós NPE) y se limpiaron los imports muertos.
9. `pe.edu.pucp.main.Principal` ahora demuestra el flujo completo punta a punta.
10. Falta: compilar, aprobar este doc, **migrar a paquetes** (§7) y el SQL.

## 2. Glosario mínimo (para estar todos en la misma página)

| Palabra | Qué significa aquí, con ejemplo |
|---|---|
| **Baja lógica** | No borrar la fila, solo marcarla inactiva. Ej.: se deja de ofrecer Contabilidad → `contabilidad.setActivo(false)` en vez de eliminarla (se conserva historia). |
| **CRUD** | Crear, Leer, Actualizar, Eliminar (lógico). Ej.: carreras y modalidades tienen CRUD. |
| **Precargado** | Dato fijo que viene con el sistema, nadie lo crea por pantalla. Ej.: `Pais("PE","Peru")`, las facultades, las sedes. En el SQL serán `INSERTs`. |
| **Pasarela** | Servicio externo que cobra (Yape, tarjeta, banco). Del resultado solo se guarda: monto, referencia y voucher. Ya no existe un "área de Finanzas" que concilie a mano. |
| **FK** | Clave foránea: el campo que une dos tablas/clases. Ej.: `Pago.postulacion` dice "este pago es de esta postulación". Sin FK, el pago queda huérfano. |
| **Paquete** | Carpeta lógica de Java (`package ...`) que agrupa clases del mismo tema. Es lo que pide el Lab04. |
| **NPE** | `NullPointerException`: el error cuando usas una lista que es `null`. Lo eliminamos inicializando todo. |

## 3. Punto de partida: qué teníamos en el Lab02

40 clases planas (sin paquetes) que modelan la admisión: convocatoria y su
configuración (modalidades, carreras, requisitos, etapas), personas
(postulante, apoderado, evaluador), el trámite (postulación, documentos,
observaciones, carné), pagos, notificaciones y enums de estados/tipos.
`pe.edu.pucp.main.Principal.java` es un **demo**: arma un caso completo (María se postula a
Sistemas, paga, le observan el DNI, lo subsana) e imprime el resumen.
Ese demo es la prueba de que el dominio funciona.

## 4. Qué observó el JP y cómo se resolvió (tabla maestra)

| # | Observación del JP (19.00) | Dónde dolía (ejemplo) | Solución aplicada |
|---|---|---|---|
| 1 | Falta atributo `Estado` para baja lógica | No había forma de "desactivar" una `Carrera` sin borrarla | `boolean activo` (nace `true`) + `isActivo/setActivo` en 9 clases (§5) |
| 2 | Definir CRUD vs precargado | ¿`Pais`/`Sede` tienen pantalla propia o vienen fijos? | Decisión explícita: `Pais, Facultad, Sede` = **precargados**; resto de maestros = CRUD con `activo` |
| 3 | Simplificar pagos/finanzas + RF11 | `Pago` tenía 15 campos con validación manual de Finanzas | Finanzas fuera → `Pago` híbrido de pasarela (§6) |
| 4 | Canal de notificaciones | Había `EMAIL, SMS, WHATSAPP` (SMS/WhatsApp cuestan) | `CORREO, BANDEJA_SISTEMA` + `leida/fechaLectura` (§7) |
| 5 | RF5/RF8 sin documentos diferenciados | Todas las modalidades pedían lo mismo | Cada modalidad declara sus requisitos (ordinaria: DNI+foto; CEPRU: certificado), evidenciado en el demo |
| 6 | Alcance y roles | Proyecto muy amplio, roles sin definir | Dominio congelado en 40 clases; los 5 paquetes del §8 dan 5 frentes (uno por integrante) y los roles serán permisos sobre ellos en Lab07 |

## 5. Decisión 1: `activo` en 9 clases (baja lógica)

**El problema, con ejemplo:** si Admisión deja de ofrecer Contabilidad un año,
con el Lab02 había que borrar el objeto y se perdía historia. Ahora:

```java
// Antes (Lab02): no existía forma de desactivar
// Después (Lab04):
contabilidad.setActivo(false);   // sigue existiendo, pero no se ofrece
```

**Por qué `boolean` y no un enum `Estado`:** el JP pedía "atributo Estado",
pero para maestros simples el estándar es un flag (en SQL será
`ACTIVO NUMBER(1)`); los estados de *flujo* ya existen donde hacen falta
(`EstadoConvocatoria`, `EstadoPostulacion`, `EstadoDocumento`...). Ejemplo de
la diferencia: `Convocatoria` tiene **ambos** — `estado` dice *en qué etapa va*
(PUBLICADA), `activo` dice *si existe o fue dada de baja*. No es redundancia.

**Por qué no en todas:** poner `activo` en las 40 sería ruido. La regla:

* Maestros con CRUD (9) → `activo`.
* Precargados (`Pais, Facultad, Sede`, enums) → sin `activo` (son fijos).
* Transaccionales (postulación, documentos, pagos…) → ya tienen su `estado`.
* Tablas intermedias (ej. `OfertaCarrera`) → viven y mueren con su convocatoria.

## 6. Decisión 2: `Pago` híbrido (ni el de Lab02 ni el del compañero)

**El problema:** el `Pago` del Lab02 (15 campos) era del mundo con Finanzas
(`validadoPor`, `fechaValidacion`, descuentos…). El del compañero (5 campos)
era simple pero **perdía lo esencial**: ni siquiera guardaba *de qué
postulación era el pago* (FK huérfana → el SQL no podría unir PAGOS con
POSTULACIONES) ni la *referencia de la pasarela* (justo lo que Yape/tarjeta
devuelven).

```java
// Propuesta del compañero (rechazada): ¿de quién es este pago? No se sabe.
new Pago(150.00, fecha, MedioPago.TARJETA, EstadoPago.APROBADO, "/docs/voucher.pdf");

// Versión final (híbrida): simple como la del compañero, completa como Lab02.
new Pago(postulacion, MedioPago.TARJETA, 150.00,
         fechaGeneracion, fechaPago, "PAG-2026-0001", "TF-88231",
         EstadoPago.APROBADO, "/docs/voucher.pdf");
```

Campos finales: `postulacion, medioPago, monto, fechaGeneracion, fechaPago,
codigoPago, referenciaPasarela, estadoPago, rutaVoucher`.
**Y `MedioPago` quedó como `enum{TARJETA, YAPE, BANCO}`** (idea del compañero):
con pasarela cerrada el catálogo es fijo; si algún día entra otro medio, se
agrega una línea al enum. En SQL será un `VARCHAR2` con `CHECK`.

## 7. Decisión 3: notificaciones con bandeja de verdad

Cambiar el enum era lo fácil (`CORREO, BANDEJA_SISTEMA`); lo que faltaba era el
**comportamiento**: una notificación de bandeja necesita saber si fue leída.

```java
notifBandeja.setLeida(true);
notifBandeja.setFechaLectura(LocalDate.of(2026, 3, 9));
```

`CORREO` sigue usando `destinatario` como email. El demo envía **las dos** para
el mismo evento (DNI observado), así se ve el canal nuevo funcionando.

## 8. Decisión 4: blindaje anti-`null` (sin cambio funcional)

Dos trampas del Lab02 que habrían explotado en el Lab04/07:

1. `new Convocatoria()` y `new Postulacion()` dejaban sus listas en `null` →
   el primer `getPostulantes()` reventaba. Ahora nacen vacías.
2. Los setters guardaban la lista ajena por referencia (si quien llamaba pasaba
   `null` o la modificaba después, el objeto se corrompía). Ahora copian y
   aceptan `null` como "vacío". Los getters ya devolvían copias desde Lab02;
   se mantuvo ese estilo en todo el dominio.

Además se quitaron imports sin uso en 6 archivos. Nada de esto cambia el
comportamiento: solo hace el dominio **apto para persistencia**.

## 9. El demo `pe.edu.pucp.main.Principal`: qué demuestra ahora (sirve como guion de la expo)

1. Catálogos precargados (Perú, facultades, sedes).
2. Convocatoria ADM-2026 con **2 modalidades con requisitos distintos**
   (ordinaria: DNI+foto obligatorios; CEPRU: certificado) → responde RF5/RF8.
3. Postulante María + apoderado + **antecedente del colegio** (RF005: por fin el
   colegio deja de ser un objeto suelto).
4. Postulación → **pago por pasarela** con referencia `TF-88231` → carné.
5. DNI observado por la evaluadora → **correo + bandeja (leída)** → subsanación.
6. **Baja lógica**: Contabilidad queda `activo=false`.
7. Todo el grafo queda vinculado en ambos sentidos (la convocatoria conoce a
   sus postulantes y viceversa).

## 10. Árbol de paquetes propuesto (todavía NO aplicado)

**La idea en una frase:** guardar juntas las clases que cambian por el mismo
motivo, como cajones etiquetados de una oficina. Lo que pide la Sesión 04:
*alta cohesión* (mismo propósito), *bajo acoplamiento* (depender poco de otros),
*encapsulamiento*.

```
proyecto/src/pe/edu/pucp/admitu/
├── modelo/
│   ├── configuracion/                  # oferta académica (13)
│   │   ├── Convocatoria.java               # * importa de persona
│   │   ├── ConvocatoriaEtapa.java
│   │   ├── ConvocatoriaModalidad.java
│   │   ├── OfertaCarrera.java
│   │   ├── RequisitoConvocatoriaModalidad.java
│   │   ├── Modalidad.java
│   │   ├── Carrera.java
│   │   ├── Facultad.java
│   │   ├── Requisito.java
│   │   ├── Etapa.java
│   │   ├── Sede.java
│   │   ├── EstadoConvocatoria.java
│   │   └── TipoArchivo.java
│   ├── persona/                        # personas y trayectoria (10)
│   │   ├── Persona.java
│   │   ├── Postulante.java                 # * importa de postulacion
│   │   ├── Apoderado.java
│   │   ├── Evaluador.java
│   │   ├── AntecedenteAcademico.java
│   │   ├── InstitucionEducativa.java
│   │   ├── Pais.java
│   │   ├── TipoDocumento.java
│   │   ├── Parentesco.java
│   │   └── TipoInstitucion.java
│   ├── postulacion/                    # el trámite, corazón del sistema (9)
│   │   ├── Postulacion.java                # * importa de configuracion, persona, pago, notificacion
│   │   ├── PostulacionHistorial.java
│   │   ├── DocumentoPostulacion.java       # * importa de configuracion
│   │   ├── DocumentoObservacion.java       # * importa de persona
│   │   ├── CarnePostulante.java            # * importa de configuracion
│   │   ├── EstadoPostulacion.java
│   │   ├── EstadoDocumento.java
│   │   ├── EstadoObservacion.java
│   │   └── TipoObservacion.java
│   ├── pago/                           # pasarela externa (3)
│   │   ├── Pago.java                       # * importa de postulacion
│   │   ├── MedioPago.java
│   │   └── EstadoPago.java
│   └── notificacion/                   # correo + bandeja (4)
│       ├── Notificacion.java               # * importa de postulacion
│       ├── MedioNotificacion.java
│       ├── TipoNotificacion.java
│       └── EstadoEnvio.java
└── app/
    └── pe.edu.pucp.main.Principal.java                  # * importa de los 5 paquetes (1)
```

> `*` = necesitará líneas `import` (9 archivos). Los otros 31 solo cambian su
> línea `package`. Total: 13 + 10 + 9 + 3 + 4 + 1 = **40/40**.

```java
package pe.edu.pucp.admitu.modelo.pago;

import java.time.LocalDate;
import pe.edu.pucp.admitu.modelo.postulacion.Postulacion;

public class Pago {
    ...
}
```

**Nombre raíz `pe.edu.pucp.admitu`:** es el estándar Java (dominio invertido:
evita choques de nombres cuando esto se empaquete como librería `.jar`, que es
justo lo que enseña la Sesión 04). Las capas futuras (`persistencia`,
`logica`…) nacerán como *hermanas* de `modelo`, sin reescribir nada.

**Por qué estos 5 cajones y no otros:**

* Cada cajón cambia por **un solo motivo**: la oferta académica cambia cada
  proceso; pagos solo si cambia la pasarela; notificaciones si cambia el canal.
  Un cambio toca un cajón, no 40 archivos.
* Cada `enum` vive con su cajón (no habrá un cajón `enums/` genérico: ordenar
  por *forma* en vez de por *tema* es como ordenar la oficina por color de
  carpeta). `TipoArchivo` va en `configuracion` porque ahí vive `Requisito`,
  que es quien lo define.
* **Lo que se descartó y por qué:** un solo cajón (lo actual: sin orden);
  cajones por capa (`vista/logica/...`, no existen capas en Lab04: sería
  adivinar); cajones por actor (el Evaluador toca documentos, notificaciones y
  pagos a la vez: partir por actor rompería todo; los *roles* serán permisos
  sobre estos cajones en Lab07); un cajón por entidad (20 cajones = burocracia).
* **Cómo se hablan:** `postulacion` puede usar a los demás; `pago` y
  `notificacion` conocen a `postulacion` (y viceversa: es la relación normal
  "tiene muchos", en SQL serán FK); `configuracion` solo conoce a `persona`
  por la lista de inscritos. Verificado en el código: solo 9 de 40 archivos
  necesitarán `import`.
* **Para qué rinde:** 5 frentes paralelos (uno por integrante), cada cajón ≈ un
  grupo de tablas del SQL (≈29 del reference), espejo a namespaces C# (Lab07) y
  a un servicio web por cajón (Lab09).

**Aplicarlo (cuando el equipo lo apruebe):** mover con `git mv` a `src/...`,
agregar la línea `package` + los `import` (mapa ya calculado), compilar con
`javac -d out $(find src -name "*.java")` y correr
`java -cp out pe.edu.pucp.admitu.app.pe.edu.pucp.main.Principal` (misma salida que hoy).

## 11. Preguntas frecuentes (con respuesta lista)

* **¿Por qué `activo` boolean y no enum `Estado`?** Para maestros simples el flag
  basta y mapea a `ACTIVO NUMBER(1)`; los flujos ya tienen sus enums de estado.
* **¿Por qué `MedioPago` enum y no clase?** Catálogo cerrado de 3 valores
  impuesto por la pasarela; clase sería CRUD innecesario.
* **¿Por qué `Pago` guarda `postulacion` y `Postulacion` guarda `pagos` (doble
  referencia)?** Navegación en ambos sentidos, patrón normal JPA; en SQL serán
  FK. Sin el lado `Pago→Postulacion`, el pago quedaba huérfano (ese fue el
  error de la propuesta).
* **¿Por qué `Pais/Sede/Facultad` sin `activo`?** Decisión de equipo: son datos
  precargados fijos, no tienen pantalla de mantenimiento.
* **¿Se rompió algo del Lab02?** No: mismo comportamiento + campos nuevos con
  valor por defecto (`activo=true`, `leida=false`). El diff real es 22 archivos.

## 12. Checklist del equipo

- [ ] Cada uno compila y corre el demo:
  `javac proyecto/*.java && java -cp proyecto pe.edu.pucp.main.Principal`
- [ ] Aprobar este documento (es la defensa ante el JP).
- [ ] Aprobar el árbol del §10 y migrar.
- [ ] Redactar el `SCRIPT SQL` espejo (`ACTIVO` solo donde hay `activo`).
- [ ] Actualizar el informe: RF011 reescrito a pasarela, RF012 eliminado,
      precargados declarados, RF5/RF8 diferenciados, reporte de vacantes con
      modalidad y carreras.
