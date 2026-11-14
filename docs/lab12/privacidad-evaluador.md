# H18: minimización de las lecturas del evaluador

Corrección específica sobre `codex/auditoria-preaws`, base de esta tarea
`069ef1f2e85676fa5097e779fa2099ec84f5d47e`, para actualizar el PR #2.

## Trabajo recuperado y estado

Se conservaron los cambios locales de la ejecución interrumpida. El backend y
Blazor compilaban antes de la interrupción y se vuelven a compilar al recuperar
el trabajo. Los scripts de privacidad están implementados; sus resultados reales
se incorporarán tras ejecutarlos. No se presenta esta etapa como prueba aprobada.

## Política implementada

La autorización global GET anterior devolvía entidades completas; llamarlas
«metadatos» en la documentación no evitaba la exposición. El nuevo contrato
separa los resúmenes globales de los detalles de expedientes asignados.

- `GET PostulacionRS/listarResumenesGlobales`: administrador/evaluador. DTO
  `ExpedienteResumen` con `id`, `codigoInscripcion`, `convocatoriaId`,
  `modalidadElegidaId`, `carreraElegidaId`, `estadoActualId`, `evaluadorId`,
  `fechaRegistro`, `fechaEnvio`, `fechaFinalizacion` y `observaciones`.
- Cada observación global contiene exclusivamente `id`, `postulacionId`,
  `requisitoAplicableId`, `evaluadorId`, `tipoObservacion`, `estadoObservacion`,
  `fechaObservacion` y `fechaSubsanacion`. Los valores nulos pueden omitirse.
- No contiene postulante ni su identificador, nombres, DNI, correo, teléfono,
  nacimiento, discapacidad, CONADIS, texto libre, pagos, antecedentes ni archivos.
- Los GET completos de los ocho recursos personales filtran los listados por
  asignación actual y rechazan con 403 los ID fuera de ese ámbito. Perfiles y
  antecedentes se autorizan por titular de un expediente asignado; pagos,
  documentos, observaciones, historial y carnés por expediente concreto.
- El evaluador consulta solo su propia fila en `EvaluadorRS`. El autor anidado
  de observaciones se representa como `{id}` para clientes no administradores.
- El Jasper de observados del evaluador incluye solo expedientes asignados en
  ese momento. El administrador conserva sus lecturas completas. El postulante
  conserva el acceso a sus propios datos.

El portal mantiene «Todos los expedientes» mediante una colección separada de
DTOs y una tabla de metadatos. «Mis expedientes» conserva los detalles y las
acciones autorizadas. No se fabrican entidades personales para llenar la tabla.

## Pruebas y protección del origen

Se conserva el clon aislado `admitu-preaws-privacidad`, MySQL 8.4, puerto externo
3308 y esquema `admitu_db`. La configuración JDBC y el cliente de pruebas son
privados. La base anterior no se recrea: se respalda por lectura y se registra
una huella de sus 30 tablas; su contenedor queda detenido durante las pruebas.
No se escriben fixtures en el origen. El clon conserva la collation de origen
`utf8mb4_unicode_ci` para evitar divergencias de parámetros de procedimientos.

Runners pendientes de ejecución al publicar el avance recuperado:

```bash
python3 scripts/pruebas/privacidad-evaluador.py --help
python3 scripts/pruebas/privacidad-ui.py --help
```

Incluyen dos evaluadores, dos postulantes y administrador, campos JSON recursivos,
listados, ID, reasignación/revocación y PDF. Las suites de autorización y los RF
afectados se repetirán sobre el clon. Los resultados anteriores siguen siendo
históricos y no prueban por sí solos esta nueva restricción por campo.

## Límites conservados

Las consultas BO/DAO internas aún cargan colecciones completas antes de filtrar
o proyectar; esas entidades no se serializan al cliente. Paginación y optimización
siguen en H06. El visor PDF integrado sigue sujeto a la política `data:` del
Chromium Cloud; sus bytes y su contenido se pueden verificar por separado.
No se prueba WSL ni Windows/IIS, no se despliega AWS y no se fusiona ningún PR.
