# Correcciones de auditoría previas a AWS

Encargo del 13/11/2026. Base exclusiva `codex/lab12-completo`, commit
`2ad401985177e8b26e16db4840c2e9c262cdb2b5`. Rama correctiva:
`codex/auditoria-preaws`; destino del PR: `codex/lab12-completo`.

Se leyeron íntegramente `AUDITORIA_ADMITU_LAB12.md` y
`PROMPT_CODEX_CORRECCIONES_PREAWS.md`, aportados por el propietario. El mensaje
actual prevalece sobre los nombres de rama y autorizaciones antiguas de esos
documentos y de AGENTS.md: no modificar master/lab11-diego, no fusionar PR #1,
no desplegar AWS. Se conservan los commits previos de LAB12.

Las nuevas pruebas se ejecutarán exclusivamente en Cloud con datos sintéticos,
en una base MySQL local de ensayo propia `admitu_db`. Ningún resultado acredita
la instalación WSL del propietario. Las evidencias de LAB12 anteriores se
conservan como históricas y no se presentan como una nueva ejecución.

## Registro de hallazgos

Estado inicial: diagnóstico y correcciones en curso; las pruebas aún no realizadas
se declaran pendientes. Esta tabla se actualizará con resultados reproducibles.

| Hallazgo | Estado inicial | Comprobación prevista |
|---|---|---|
| H01 | PENDIENTE | Roles/verbo/ruta/expediente, rechazo sin cambios SQL |
| H02 | PENDIENTE | Cierre inclusivo, antes/límite/después en Lima |
| H03 | PENDIENTE | Misma modalidad, dos convocatorias y tarifas distintas |
| H04 | PENDIENTE | Requisitos obligatorios, bytes y contraste KPI/SQL |
| H05 | PENDIENTE | Catálogo y alcance explícito de avisos temporales |
| H06 | PENDIENTE | Carga asíncrona y carga autenticada p95/p99 |
| H07 | PENDIENTE | Archivos con dos evaluadores y dos postulantes |
| H08 | PENDIENTE | Asignación persistida y recarga desde evaluador |
| H09 | PENDIENTE | Fechas, horas y nombres ausentes sin invención |
| H10 | PENDIENTE | Catálogo, versión vigente y trazabilidad |
| H11 | PENDIENTE | Conteos históricos sin bytes, sin borrado silencioso |
| H12 | PENDIENTE | Cookies, claves, TLS, backups y guía Windows |
| H13 | PENDIENTE | RF15 completadas/cupos compartidos según fuentes |
| H14 | PENDIENTE | Negativos REST, CSRF, expiración, bloqueo, revocación |
| H15 | PENDIENTE | Errores, espera y límites restantes documentados |
| H16 | PENDIENTE | Historial inmutable desde clientes de todos los roles |
| H17 | PENDIENTE | Autor de observación vinculado a identidad servidor |

No se certifican AWS, Windows/IIS, WSL ni 500 trámites concurrentes por preparar
scripts o por obtener respuestas HTTP200 en un catálogo público.


## Punto de reanudación

Se verificó por Git el HEAD local y remoto `719ef8d05d3df6784f22fb0c8f600618c62112c5`.
Los cinco commits de corrección publicados se conservan. La base remota sigue
siendo `2ad4019`; master y lab11-diego conservan sus referencias previas.
Se recuperaron los cambios locales y los servicios Cloud existentes sin recrear
la base ni reiniciar el desarrollo. Los agentes de la ejecución interrumpida ya
no estaban activos; las tareas pendientes se reasignaron.

Evidencia disponible antes de reanudar: build Maven y Blazor exitosos; 32/32 BO;
14/14 reglas de indicadores; 24 pruebas C# de carga/fechas/tarifas; tres límites
REST/SQL de cierre y tarifas; tres PDF Jasper con contraste SQL; carga autenticada
300/300 GET con cinco identidades. Estos resultados están acotados a sus scripts.
La regresión REST tenía un fallo RF10 por usar admin como autor, ya corregido en
el runner pero pendiente de repetición. Seguridad UI obtuvo 4/6 y requiere
investigar el tiempo de ingreso evaluador; no se declara aprobada. La nueva
suite MySQL se interrumpió por conversión decimal en su helper, pendiente de
repetición tras corregirlo. La suite negativa REST nueva y los 15 RF Chromium
siguen pendientes hasta ejecutar la versión integrada.
