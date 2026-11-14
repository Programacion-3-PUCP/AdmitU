-- Solo lectura. Ejecutar tras migraciones en admitu_db; no corrige ni elimina históricos.
USE admitu_db;
SELECT DATABASE() AS esquema, VERSION() AS version_mysql;
SELECT COUNT(*) AS documentos_aprobados_sin_bytes_activos
FROM documento_postulacion d JOIN postulacion p ON p.id=d.postulacion_id
WHERE d.estado_documento='APROBADO' AND NOT EXISTS (
 SELECT 1 FROM archivo_digital a WHERE a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.postulante_id=p.postulante_id AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0);
SELECT COUNT(*) AS pagos_aprobados_sin_voucher_activo
FROM pago p JOIN postulacion s ON s.id=p.postulacion_id WHERE p.estado_pago='APROBADO' AND NOT EXISTS (
 SELECT 1 FROM archivo_digital a WHERE a.tipo='VOUCHER' AND a.referencia_id=p.id AND a.postulante_id=s.postulante_id AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0);
SELECT COUNT(*) AS documentos_con_contador_version_mayor_uno
FROM documento_postulacion WHERE numero_version>1;
SELECT COUNT(*) AS archivos_vigentes,COALESCE(SUM(OCTET_LENGTH(contenido)),0) AS bytes_vigentes
FROM archivo_digital WHERE activo=1;
SELECT evaluador_id,COUNT(*) AS expedientes,
 SUM(estado_actual_id IN(2,3,4)) AS pendientes
FROM postulacion GROUP BY evaluador_id ORDER BY evaluador_id;
SELECT estado_actual_id,COUNT(*) AS expedientes_sin_asignacion
FROM postulacion WHERE evaluador_id IS NULL GROUP BY estado_actual_id ORDER BY estado_actual_id;
SELECT COUNT(*) AS postulaciones_duplicadas
FROM (SELECT postulante_id,convocatoria_id FROM postulacion GROUP BY postulante_id,convocatoria_id HAVING COUNT(*)>1) x;
SELECT COUNT(*) AS pagos_vigentes_duplicados
FROM (SELECT postulacion_id FROM pago WHERE estado_pago IN('GENERADO','PENDIENTE','APROBADO') GROUP BY postulacion_id HAVING COUNT(*)>1) x;
SELECT COUNT(*) AS archivos_sin_propietario_correcto
FROM archivo_digital a WHERE
 (a.tipo='DOCUMENTO' AND NOT EXISTS(SELECT 1 FROM documento_postulacion d JOIN postulacion p ON p.id=d.postulacion_id WHERE d.id=a.referencia_id AND p.postulante_id=a.postulante_id)) OR
 (a.tipo='VOUCHER' AND NOT EXISTS(SELECT 1 FROM pago g JOIN postulacion p ON p.id=g.postulacion_id WHERE g.id=a.referencia_id AND p.postulante_id=a.postulante_id)) OR
 (a.tipo='CERTIFICADO' AND NOT EXISTS(SELECT 1 FROM antecedente_academico h WHERE h.id=a.referencia_id AND h.postulante_id=a.postulante_id));
