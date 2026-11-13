# Incidencias y límites pendientes LAB12

| Prioridad | Estado | Punto | Impacto / siguiente comprobación |
|---|---|---|---|
| Alta | RESUELTO Y PROBADO | RF10 / O6 alcance de Observaciones | Selector mios/todos con opción visible y dataset distinto, más corrida completa 15/15. |
| Alta | RESUELTO Y PROBADO | Mapeo de estados SQL/Blazor | Los IDs reales son 1 BORRADOR, 2 PEND_PAGO (UI Enviada), 3 EN_REVISION, 4 OBSERVADA, 5 APTA, 6 FINALIZADA, 7 RECHAZADA y 8 ANULADA. Se corrigieron ambos sentidos y las etiquetas; el navegador emitió el carné y mostró Finalizada con SQL en estado6. |
| Media | RESUELTO Y PROBADO | Alineación de acciones y bloqueo visual | Botones alineados a igual altura en escritorio y móvil; sin desbordamiento de página a 390 px. DNI del perfil bloqueado en el flujo visual. |
| Media | RESUELTO Y PROBADO | Tipo de institución nueva en RF05 | Java y .NET compilan y 32 pruebas BO pasan, incluida universidad nueva. La carga/recarga/previsualización en el WAR actualizado pasó; el procedimiento anterior mantiene compatibilidad. |
| Alta | RESUELTO Y PROBADO | Calendario de negocio Lima | Java y SQL usaban días distintos durante el cambio de fecha UTC; ahora validaciones, registro y pago usan Lima. Los timestamps de autenticación permanecen UTC. |
| Alta | PARCIAL | RNF04, 500 postulantes | Se probaron 500 GET públicos, todos HTTP200, p95 8.308s. Falta el escenario de 500 identidades/trámites completos; no certificado. |
| Alta | BLOQUEADO POR ACCESO | AWS Academy / EC2 Windows / IIS / HTTPS público | No hay credenciales/identidad de laboratorio conectadas. Ejecutar el procedimiento preparado en el destino propio y medir REST/UI/reportes/BD. No se ha declarado online. |
| Media | RESUELTO Y PROBADO | Persistencia tras reinicio completo | Se reiniciaron frontend y dominio GlassFish, y se recuperaron los mismos 68 bytes de DNI con SHA-256 idéntico. |
| Catálogo | DECISIÓN DOCUMENTADA | RF15 cupo compartido | El modelo vigente guarda vacantes por convocatoria/carrera y el reporte lo rotula compartido entre modalidades. Si el JP exige reservas separadas por modalidad, debe definirse y configurarse su distribución; no hay reparto histórico inventado. |
| Datos históricos | IDENTIFICADO | Archivos LAB11 que solo tenían rutas ficticias | Esos documentos/vouchers no adquieren bytes retroactivamente. El visor muestra que no hay archivo; deben recargarse por su titular antes de una validación nueva. |
| Operación | DOCUMENTADO | Migraciones y datos existentes | El DDL requiere backup; la migración se detiene si hay duplicados históricos. No los borra ni repara silenciosamente. |

No se conservan credenciales de prueba en este documento. Las cuentas se provisionan con el script privado y los resultados brutos permanecen fuera de Git. No hay calificación garantizada ni conteo basado únicamente en código escrito.
