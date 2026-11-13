# Trabajo ejecutado y cierre de LAB12

La rama `codex/lab12-completo` conserva el stack y las rutas del curso: seis módulos Maven con Java 25, Jakarta EE 10/GlassFish, BOI/BOImpl, DAO/CallableStatement/SP, MySQL y Blazor .NET 10/HttpClient. Los cambios se revisan mediante PR hacia `lab11-diego`, sin fusión automática ni cambios directos en `master` o en la rama base.

| Fase autorizada | Resultado concreto | Verificación |
|---|---|---|
| Recuperar trabajo | Rama remota recuperada, commits conservados y cambios locales comparados antes de continuar | [Registro de recuperación](recuperacion.md) y log Git |
| Corregir LAB11 | Confirmaciones de reactivación, SP reales, envío protegido, fechas, estilos, filtro visible y baja lógica de borradores | API, contención y flujos de navegador según [matriz](matriz-rf.md) |
| Persistir archivos | DNI/foto/certificados/vouchers y documentos con bytes MySQL, firma/MIME/tamaño, descarga y permisos | SHA-256/bytes, sustitución/baja, recarga y casos negativos |
| Completar reportes | Tres JRXML propios, Jasper PDF desde consultas MySQL, filtros, agrupación, visor y descarga | SQL frente a PDF, escenarios vacíos/negativos y roles |
| Seguridad y sesiones | Contraseñas saladas, cuentas y sesiones persistidas, cookies, roles/propietario y scoped C# | 17 API y 6 navegador; caída API sin datos demo |
| Integridad transaccional | Postulación/documentos/archivos/pago/historial atómicos, unicidad por postulante/convocatoria, carné apto y anulación trazable | 27 comprobaciones BO→DAO→SP→MySQL |
| Compilar y probar | Reactor Maven y Blazor compilan; API 21/21 con los 15 RF | No equivale por sí solo a toda la matriz E2E visual |
| Medir RNF | Lecturas de carreras en 1/20/500 conexiones | [Resultados y límite medido](rendimiento.md) |
| Preparar despliegue | Build, migración con backup/verificación y publicación sobre IIS/GlassFish existentes | Sintaxis/dry-run; AWS/IIS no ejecutados |
| Entregar para revisión | Matriz, evidencias, incidencias, guía de sustentación, diagrama y procedimientos | PR pendiente de creación al finalizar verificaciones |

## Observaciones LAB11

| ID | Cambio | Evidencia y estado |
|---|---|---|
| O1 Reactivar sin confirmar | `BotonAccion` solicita confirmación; Cancelar no invoca el SP | Navegador RF04 aprobó cancelar, confirmar y recargar |
| O2 Iconos desalineados | Estilo común de acciones/tamaño de botones | Implementado; revisión visual final pendiente |
| O3 Doble envío | Busy en Blazor, operación compuesta y restricciones en MySQL | Dos POST: un HTTP200 y un HTTP409; una postulación/un pago |
| O4 Fecha futura | Campo date limitado y validación BO/SP | Fecha futura rechazada en API y BO/MySQL |
| O5 Campos bloqueados | Estilo común de readonly/disabled y metadatos protegidos | PASS: perfil y documento bloqueado comprobados en navegador |
| O6 Alcance de Observaciones | Etiqueta Mostrar y opciones Mis expedientes/Todos | Se confirmó que bind bool dejaba la selección vacía; valores de texto mios/todos corregidos en fuente, pendientes de repetir el filtrado |
| O7 Varias carreras en una convocatoria | UNIQUE(postulante_id,convocatoria_id), validación BO y transacción | Unicidad SQL/BO comprobada; no depende de la carrera |
| O8 Borradores acumulados | Eliminar con confirmación y baja lógica; servidor restringe borradores | API y navegador RF01 aprobados |

Se detectó y corrigió en fuente una discrepancia entre los IDs de estado SQL y los estados Blazor: APTA y FINALIZADA podían interpretarse como estados distintos e impedir emitir el carné. La pantalla ya pasó la repetición: emisión de carné y estado FINALIZADA observados tanto en SQL como en Blazor.

El flujo visual aún pendiente, RF10, se detalla en la matriz; se actualizará el cierre con la repetición final. No se computa un requisito como aprobado solo porque tiene una pantalla o compila. No se promete una calificación.

El trabajo con IA y las responsabilidades por RF se describen en [sustentación](sustentacion.md). El material privado del profesor se usó para contrastar el método por capas y Jasper; no se incorpora su ZIP, fuentes ni configuraciones al repositorio.
