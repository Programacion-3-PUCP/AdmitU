-- Solo lectura. Ejecutar sobre admitu_db después de las migraciones LAB12.
-- Cada métrica es independiente; no calcular conversiones entre estas columnas.
USE admitu_db;
WITH indicadores AS (
 SELECT p.id,p.convocatoria_id,e.codigo,p.fecha_envio,
  NOT EXISTS(SELECT 1 FROM requisito_convocatoria_modalidad rm
    WHERE rm.convocatoria_modalidad_id=p.modalidad_elegida_id AND rm.obligatorio=1
    AND NOT EXISTS(SELECT 1 FROM documento_postulacion d JOIN archivo_digital a
      ON a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.postulante_id=p.postulante_id
      AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0
      WHERE d.postulacion_id=p.id AND d.requisito_aplicable_id=rm.id AND d.estado_documento='APROBADO')) AS documentos_completos,
  EXISTS(SELECT 1 FROM pago pg WHERE pg.postulacion_id=p.id AND pg.estado_pago='APROBADO') AS pago_aprobado,
  EXISTS(SELECT 1 FROM carne_postulante cp WHERE cp.postulacion_id=p.id AND (cp.aula_examen IS NULL OR cp.aula_examen NOT LIKE 'ANULADO%')) AS carne_vigente
 FROM postulacion p JOIN estado_postulacion e ON e.id=p.estado_actual_id
)
SELECT convocatoria_id,COUNT(*) AS registradas,
 SUM(codigo<>'BORRADOR' AND fecha_envio IS NOT NULL) AS enviadas,
 SUM(codigo<>'BORRADOR' AND fecha_envio IS NOT NULL AND pago_aprobado) AS pago_validado,
 SUM(codigo<>'BORRADOR' AND fecha_envio IS NOT NULL AND documentos_completos) AS documentos_completos,
 SUM(codigo IN('APTA','FINALIZADA','ADMITIDA') AND fecha_envio IS NOT NULL AND documentos_completos AND pago_aprobado) AS aptas_consistentes,
 SUM(codigo<>'BORRADOR' AND fecha_envio IS NOT NULL AND carne_vigente) AS con_carne,
 SUM(codigo IN('APTA','FINALIZADA','ADMITIDA') AND (NOT documentos_completos OR NOT pago_aprobado)) AS estados_aptos_inconsistentes
FROM indicadores GROUP BY convocatoria_id ORDER BY convocatoria_id;

-- H08: la carga refleja los evaluador_id existentes; NULL no es un fallo de mapeo.
SELECT p.convocatoria_id,p.evaluador_id,e.codigo,COUNT(*) AS cantidad
 FROM postulacion p JOIN estado_postulacion e ON e.id=p.estado_actual_id
 GROUP BY p.convocatoria_id,p.evaluador_id,e.codigo ORDER BY p.convocatoria_id,p.evaluador_id,e.codigo;

-- H11: no corregir ni borrar automáticamente estas filas.
SELECT COUNT(*) AS documentos_aprobados_sin_bytes
 FROM documento_postulacion d JOIN postulacion p ON p.id=d.postulacion_id
 WHERE d.estado_documento='APROBADO' AND NOT EXISTS(SELECT 1 FROM archivo_digital a
  WHERE a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.postulante_id=p.postulante_id AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0);
SELECT COUNT(*) AS pagos_aprobados_sin_voucher
 FROM pago pg JOIN postulacion p ON p.id=pg.postulacion_id
 WHERE pg.estado_pago='APROBADO' AND NOT EXISTS(SELECT 1 FROM archivo_digital a
  WHERE a.tipo='VOUCHER' AND a.referencia_id=pg.id AND a.postulante_id=p.postulante_id AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0);

-- H13: contraste RF15, cupo por carrera/convocatoria; no sumar copias por modalidad.
SELECT p.convocatoria_id,cm.modalidad_id,p.carrera_elegida_id,COUNT(*) AS completadas
 FROM postulacion p JOIN convocatoria_modalidad cm ON cm.id=p.modalidad_elegida_id
 JOIN estado_postulacion e ON e.id=p.estado_actual_id
 WHERE e.codigo='FINALIZADA' GROUP BY p.convocatoria_id,cm.modalidad_id,p.carrera_elegida_id;
SELECT convocatoria_id,carrera_id,cantidad_vacantes AS cupo_compartido FROM oferta_carrera ORDER BY convocatoria_id,carrera_id;
