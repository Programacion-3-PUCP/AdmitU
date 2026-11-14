# Auditoría previa a AWS: indicadores, asignaciones y RF15

Rama `codex/auditoria-preaws`, base auditada `2ad4019`. Pruebas de esta corrección en Cloud; no representan acceso a WSL ni ejecución en AWS.

## H04 — CORREGIDO

`DatosSesion.DocumentosCompletos` delega en `IndicadoresExpediente`: cada asociación **obligatoria** de la convocatoria/modalidad debe tener un documento de esa postulación **APROBADO** con bytes activos. No basta un nombre o una ruta. El indicador `archivoActivo` lo calcula MySQL con `EXISTS`, verifica el propietario y `OCTET_LENGTH(contenido)>0`, y viaja por DAO → JSON → DTO → modelo; no depende de un booleano aportado por el cliente. Aplicar `database/lab12-auditoria-metricas.sql` antes de actualizar el WAR. La migración modifica solamente dos procedimientos de lectura, sin eliminar filas.

La definición coincide con la guarda de `MODIFICAR_POSTULACION`: los documentos opcionales no bloquean la aptitud; una modalidad sin requisitos obligatorios satisface la condición; una asociación obligatoria no desaparece al desactivar el catálogo del requisito. Se conserva esta semántica y se explicita para evitar dos reglas distintas.

`DatosSesion.EsApta` exige estado APTA/FINALIZADA/ADMITIDA, documentos completos y pago aprobado. NO_APTA/NO_ADMITIDA/ANULADA quedan excluidos. El resumen presenta por separado la cantidad de estados favorables inconsistentes que requieren revisión. No cambia esos estados en la base ni oculta registros históricos del listado.

El resumen y sus enlaces usan las mismas definiciones. «Enviadas» exige fecha de envío persistida y estado distinto de borrador. Las barras son **indicadores independientes**: no muestran conversiones entre pagos, documentos y aptitud. Desaparece el cálculo 34/20=170% entre cohortes no anidadas. Los porcentajes de aptitud respecto a enviadas sí usan una cohorte contenida.

Las alertas de antigüedad expresan días calendario, porque el esquema guarda DATE y no permite acreditar 48 horas exactas.

Prueba reproducible sin paquetes externos:

```bash
dotnet run --project scripts/pruebas/metricas/Metricas.csproj
```

Resultado ejecutado en Cloud: **14/14 PASS**, modelos de producción C#/.NET 10; [JSON sanitizado](resultados/auditoria-metricas-modelos.json). Cubre requisito ausente, aprobación histórica sin bytes, archivo de otro expediente, observado con bytes, opcional pendiente, catálogo desactivado, modalidad sin obligatorios, estados favorables/desfavorables y fecha de envío ausente. Esto prueba las reglas de modelos; no sustituye el contraste REST/SQL/navegador.

Contraste independiente: `database/auditoria-contraste.sql` calcula cada KPI por convocatoria, asignaciones, datos históricos sin bytes y conteos RF15. El informe principal de esta auditoría registra los resultados reales de su ejecución contra MySQL.

## H08 — diagnóstico sin alterar mapeos

El DAO `PostulacionImpl` lee `evaluador_id` y `DatosSesion` conserva `p.EvaluadorId`: no se encontró un fallo de ese mapeo. Un valor NULL produce «Sin asignar». El resumen de carga incluye solo expedientes efectivamente enviados de la convocatoria seleccionada; la alerta de pendientes sin evaluador incluye ENVIADA/EN_REVISION, no todos los estados. La consulta agregada de contraste separa convocatoria, evaluador y estado, para distinguir datos históricos de una regresión. La prueba de asignación admin → recarga → lectura con evaluador se registra en las pruebas integrales; no se afirma ejecución de WSL.

## H13 — REFUTADO CON EVIDENCIA del alcance documentado; límite de cupos explícito

`plan-lab12.md`, fila RF15, pide «vacantes vs postulaciones COMPLETADAS por modalidad/carrera»; `METODOLOGIA_PROFESOR.md` también dice **completadas**. `FINALIZADA` está definida en la semilla SQL como «Inscripción concluida y carné emitido». Por esas fuentes disponibles se conserva `LAB12_REPORTE_VACANTES` contando FINALIZADA, en lugar de todas las solicitudes/borradores. El catálogo aprobado original no está adjunto al repositorio; esta conclusión se limita al alcance documentado disponible y no atribuye una decisión nueva al JP.

`oferta_carrera` almacena un cupo por convocatoria/carrera, compartido por modalidades. El reporte ya lo rotula compartido y no suma copias por modalidad. No existe una reserva por modalidad en los datos y no se inventa su distribución. Si el JP exige reservas, seguirá pendiente definirlas y migrarlas con una configuración explícita. Las consultas finales del contraste permiten verificar FINALIZADA por modalidad y el cupo sin duplicarlo.

H10/versiones y H11/datos históricos se detallan en `auditoria-integridad.md`; no se conservan originales binarios por el mero incremento de `numero_version`.
