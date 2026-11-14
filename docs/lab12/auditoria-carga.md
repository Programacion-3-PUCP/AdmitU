# Auditoría previa a AWS: carga de sesión y avisos (H05/H06)

Cambios sobre `codex/lab12-completo`, sin modificación de su arquitectura Java REST/MySQL/Blazor. Estas comprobaciones se ejecutan en Cloud; no describen WSL ni AWS.

## H05 — Alcance visible de los avisos

`plan-lab12.md` y `docs/lab12/matriz-rf.md` describen los quince RF: ninguno exige una bandeja de notificaciones persistente ni correo. RF13 exige el historial durable de transiciones; permanece en MySQL y es consultable por REST. No se ha añadido un sistema de mensajería sin requisito de catálogo.

La funcionalidad existente `DatosSesion.Notificar` conserva avisos únicamente en el scope/circuito actual. Cada feed, incluidos los paneles de inicio, indica explícitamente: «Avisos temporales de esta sesión. No se conservan al volver a ingresar ni muestran acciones de otras sesiones». El vacío dice «Sin avisos en esta sesión» y remite al expediente/historial; se retiró «Todo al día», que podía interpretarse como comprobación global. El panel de avisos aclara que no hay bandeja persistente ni entrega de correo. Marcar leído afecta solo a esa sesión.

**Límite pendiente:** si el equipo pretende presentar notificaciones entre sesiones como capacidad evaluada, hace falta implementar destinatario, persistencia BO/DAO/SP/REST, lectura por identidad y pruebas de otra sesión. No se declara que exista esa capacidad.

## H06 — Carga inicial asíncrona

Antes, el constructor de `DatosSesion` esperaba la autenticación y diecinueve tareas `Task.Run` que hacían GET sincrónico, bloqueadas con `Task.WaitAll`. Ahora:

- El constructor crea las listas vacías y no inicia peticiones HTTP.
- `InicializarAsync` obtiene la identidad de `AuthenticationStateProvider` con `await` y mantiene una única tarea por scope. Las identidades y las listas siguen siendo scoped.
- `CargarDesdeBackendAsync` utiliza GET asíncrono y `Task.WhenAll`. El cliente limita a seis lecturas simultáneas por sesión, evitando una ráfaga de diecinueve conexiones por carga. Los recursos privados siguen sujetos a los filtros del servidor; el postulante no consulta evaluadores.
- `AreaShell` espera la inicialización y muestra un estado de carga antes de construir menús o páginas que requieren al titular. Acceso y registro también esperan su carga pública.
- Los flujos de archivos y envío compuesto esperan la recarga asíncrona. Un fallo de carga muestra un aviso y conserva vacías las listas iniciales; no aparece contenido de demostración.
- `UltimaCargaMilisegundos` permite medir la carga en el entorno de ejecución sin registrar contraseñas, tokens o contenido de expedientes.

**Pendientes explícitos:** las operaciones CRUD heredadas aún son sincrónicas. `CambiarEstado` conserva el wrapper sincrónico de recarga por compatibilidad; no se ha transformado toda la cadena de eventos CRUD en esta corrección. Tampoco se han añadido paginación de servidor, caché compartida de catálogos ni consultas por página. Las colecciones autorizadas siguen cargándose en conjunto. Por ello H06 y RNF04 permanecen parciales hasta medir y optimizar escenarios autenticados representativos.

## Pruebas reproducibles

```bash
dotnet run --project scripts/pruebas/CargaSesion/CargaSesion.csproj
```

Es una consola .NET 10 con referencia a la aplicación y `Microsoft.AspNetCore.App`; no añade paquetes ni frameworks. Usa `HttpMessageHandler` simulado con treinta milisegundos de espera por respuesta. Verifica constructor sin HTTP, devolución del control durante E/S, inicialización única, identidad, límite de concurrencia, separación de scopes/avisos y error de red sin datos de muestra. También incorpora pruebas de fechas y tarifas. El resultado JSON de esta ejecución se archiva en `resultados/auditoria-carga-sesion.json`.

La simulación demuestra comportamiento del código cliente y límite de solicitudes simultáneas. No acredita tiempos de GlassFish/MySQL, carga autenticada real, p95/p99 de tráfico ni 500 trámites simultáneos. La medición anterior de quinientos GET públicos conserva su carácter histórico y no es evidencia nueva de esta rama.

## Comparación controlada del cliente

```bash
python3 scripts/pruebas/medir-carga-sesion.py
```

El script extrae únicamente el frontend del commit auditado a un directorio temporal `work/`, compila ambas versiones y aplica el mismo manejador HTTP simulado (30 ms, 18 respuestas para postulante). No cambia ramas. Tras una vuelta inicial, guarda cinco muestras por versión. En esta ejecución la mediana del constructor fue 539,720 ms antes y 0,697 ms después; la mediana hasta disponer de datos fue 539,731 ms y 172,050 ms. El máximo observado de solicitudes en vuelo antes osciló entre 8 y 13 y después fue siempre 6. Evidencia: `resultados/auditoria-carga-comparacion.json`.

Estos tiempos incluyen el comportamiento del ThreadPool y la carga compartida de la máquina Cloud. Son una comparación controlada de bloqueo del cliente, no una predicción de latencia real de admisión.

## Lecturas autenticadas contra GlassFish/MySQL

```bash
python3 scripts/pruebas/carga-autenticada.py --apply \
  --credentials-file work/test-credentials.json \
  --output work/evidencias/carga-autenticada.json
```

Solo permite backend de loopback. Usa administrador y dos evaluadores del archivo privado; crea dos postulantes sintéticos exclusivos y conserva sus claves únicamente durante el proceso. Realiza cien lecturas por escenario con concurrencia 1/5/10, alternando postulaciones, pagos y documentos. Verifica respuesta JSON, registra HTTP, fallos, mediana/p95/p99 y cierra las cinco sesiones. El registro/login queda fuera de los tiempos. No cambia los expedientes, documentos ni pagos y no imprime secretos.

Los resultados ejecutados de esta rama se guardan en `resultados/auditoria-carga-autenticada.json`. Son lecturas privadas sobre datos de ensayo; los dos postulantes nuevos no tienen expediente. Diez solicitudes pueden compartir cinco identidades. No acreditan quinientos usuarios, envíos, archivos ni RNF04 completo.

Resultado ejecutado: **300/300 HTTP 200**, sin fallos. Un administrador, dos evaluadores y dos postulantes participaron en cada etapa (veinte solicitudes por identidad). Cada etapa incluyó 35 listados de postulaciones, 35 de pagos y 30 de documentos.

| Solicitudes concurrentes | Solicitudes | p95 (s) | p99 (s) | Fallos |
|---|---:|---:|---:|---:|
| 1 | 100 | 0,197516 | 0,237915 | 0 |
| 5 | 100 | 0,710090 | 0,841913 | 0 |
| 10 | 100 | 1,612049 | 1,764536 | 0 |

Estas muestras pertenecen al entorno Cloud compartido de esta ejecución y a su volumen actual de datos sintéticos. No autorizan extrapolar a producción o quinientos usuarios.
