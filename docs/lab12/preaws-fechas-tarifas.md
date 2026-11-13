# Fechas, tarifas y metadatos anteriores a AWS

La fuente de cierre de inscripción es `convocatoria.fecha_fin`, inclusiva en el día de Lima. `INSERTAR_POSTULACION` ya aplica esta regla con `DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)`. Blazor usa las mismas fechas y `America/Lima`; dejó de descontar quince días. Se conservan límites NULL como ausencia de límite, según SQL, sin inventar fechas. El formulario administrador edita apertura y cierre; las fechas de evaluación y publicación que solo existían en memoria se retiraron. Añadir esas fases como requisitos futuros necesita persistencia explícita, sin deducirlas de `fecha_fin`.

`TarifaInscripcion(convocatoriaId, modalidadId)` lee únicamente la asociación seleccionada y devuelve ausencia si no existe. El wizard, las tarjetas de convocatorias y la edición administrativa muestran ese importe. El administrador selecciona la convocatoria de la tarifa antes de editar una modalidad, y la modificación conserva las tarifas de otras convocatorias. Los vouchers y totales existentes siguen utilizando el monto del pago persistido por MySQL; no se recalculan con la tarifa de otra convocatoria.

Los modelos conservan NULL en las fechas de registro, carga, observación, pago, emisión y vigencia, y las vistas indican «No registrada». La anulación de carné conserva las fechas de vigencia almacenadas en vez de deducirlas. Los antecedentes sin PDF identificable ya no reciben un nombre inventado; años ausentes permanecen en cero y la fecha no almacenada permanece NULL. El historial conserva NULL y presenta `DATE` solo como fecha, sin `10:00` artificial. La falta de aula, autor histórico o nombre del voucher se presenta como ausencia. El estado VENCIDO permanece VENCIDO.

Pruebas reproducibles, exclusivamente Cloud:

```sh
source work/env.sh
dotnet build frontend/AdmitUWA/AdmitUWA/AdmitUWA.csproj
dotnet run --project scripts/pruebas/CargaSesion/CargaSesion.csproj
python3 scripts/pruebas/fechas-tarifas.py \
  --credentials-file work/test-credentials.json \
  --mysql-defaults work/private/mysql.cnf \
  --output docs/lab12/resultados/preaws-fechas-tarifas.json
```

La suite C# incluye antes, exactamente en y después del cierre, apertura, borrador, 23:59:59/00:00:00 Lima, dos convocatorias con la misma modalidad y costos distintos, y ausencia de metadatos. Usa HTTP simulado y no acredita SQL ni RNF04. La suite Python crea fixtures nuevas con prefijo FT y consulta REST real; con `--mysql-defaults` compara monto y tarifa SQL, y comprueba que el cierre vencido no cree postulación ni pago. No borra filas históricas. Los JSON de resultados establecen lo efectivamente ejecutado; no son pruebas de WSL ni AWS.

Las migraciones no reconstruyen fechas ni archivos históricos que nunca se almacenaron. Las nuevas operaciones continúan usando las fechas aplicables de servidor, mientras las filas antiguas conservan la ausencia.
