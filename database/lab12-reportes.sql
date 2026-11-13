-- Reportes de lectura, únicamente admitu_db. No borra ni modifica filas del negocio.
USE admitu_db;
DELIMITER $$
DROP PROCEDURE IF EXISTS LAB12_REPORTE_INGRESOS$$
CREATE PROCEDURE LAB12_REPORTE_INGRESOS(IN _conv INT,IN _mod INT,IN _desde DATE,IN _hasta DATE)
BEGIN
 SELECT c.id convocatoria_id,c.nombre convocatoria,m.id modalidad_id,m.nombre modalidad,
 COUNT(*) cantidad,SUM(pg.monto) total
 FROM pago pg JOIN postulacion p ON p.id=pg.postulacion_id
 JOIN convocatoria c ON c.id=p.convocatoria_id
 JOIN convocatoria_modalidad cm ON cm.id=p.modalidad_elegida_id JOIN modalidad m ON m.id=cm.modalidad_id
 WHERE pg.estado_pago='APROBADO' AND (_conv=0 OR c.id=_conv) AND (_mod=0 OR m.id=_mod)
 AND (_desde IS NULL OR pg.fecha_pago>=_desde) AND (_hasta IS NULL OR pg.fecha_pago<=_hasta)
 GROUP BY c.id,c.nombre,m.id,m.nombre ORDER BY c.id,m.nombre,m.id;
END$$
DROP PROCEDURE IF EXISTS LAB12_REPORTE_OBSERVADOS$$
CREATE PROCEDURE LAB12_REPORTE_OBSERVADOS(IN _conv INT,IN _mod INT,IN _desde DATE,IN _hasta DATE)
BEGIN
 SELECT c.id convocatoria_id,c.nombre convocatoria,m.id modalidad_id,m.nombre modalidad,
 p.codigo_inscripcion expediente,CONCAT_WS(' ',per.nombres,per.apellido_paterno,per.apellido_materno) postulante,
 d.nombre_archivo documento,o.tipo_observacion tipo,o.descripcion descripcion,
 CAST(o.fecha_observacion AS CHAR) fecha,o.estado_observacion estado
 FROM documento_observacion o JOIN documento_postulacion d ON d.id=o.documento_id
 JOIN postulacion p ON p.id=d.postulacion_id JOIN persona per ON per.id=p.postulante_id
 JOIN convocatoria c ON c.id=p.convocatoria_id
 JOIN convocatoria_modalidad cm ON cm.id=p.modalidad_elegida_id JOIN modalidad m ON m.id=cm.modalidad_id
 WHERE o.estado_observacion='PENDIENTE' AND (_conv=0 OR c.id=_conv) AND (_mod=0 OR m.id=_mod)
 AND (_desde IS NULL OR o.fecha_observacion>=_desde) AND (_hasta IS NULL OR o.fecha_observacion<=_hasta)
 ORDER BY c.id,m.id,p.id,o.id;
END$$
DROP PROCEDURE IF EXISTS LAB12_REPORTE_VACANTES$$
CREATE PROCEDURE LAB12_REPORTE_VACANTES(IN _conv INT,IN _mod INT,IN _desde DATE,IN _hasta DATE)
BEGIN
 SELECT c.id convocatoria_id,c.nombre convocatoria,m.id modalidad_id,m.nombre modalidad,
 ca.id carrera_id,ca.nombre carrera,oc.cantidad_vacantes cupo_carrera,
 COUNT(p.id) completadas
 FROM oferta_carrera oc JOIN convocatoria c ON c.id=oc.convocatoria_id JOIN carrera ca ON ca.id=oc.carrera_id
 JOIN convocatoria_modalidad cm ON cm.convocatoria_id=c.id JOIN modalidad m ON m.id=cm.modalidad_id
 LEFT JOIN postulacion p ON p.carrera_elegida_id=oc.id AND p.modalidad_elegida_id=cm.id
 AND EXISTS(SELECT 1 FROM estado_postulacion e WHERE e.id=p.estado_actual_id AND e.codigo='FINALIZADA')
 AND (_desde IS NULL OR p.fecha_finalizacion>=_desde) AND (_hasta IS NULL OR p.fecha_finalizacion<=_hasta)
 WHERE (_conv=0 OR c.id=_conv) AND (_mod=0 OR m.id=_mod)
 GROUP BY c.id,c.nombre,m.id,m.nombre,ca.id,ca.nombre,oc.cantidad_vacantes
 ORDER BY c.id,m.id,ca.nombre,ca.id;
END$$
DELIMITER ;
