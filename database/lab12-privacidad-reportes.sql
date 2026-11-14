-- H18: identidad estable para autorizar el reporte, incluso con códigos NULL.
-- Aplicar antes del WAR actualizado. No modifica filas ni cambia parámetros del SP.
USE admitu_db;
DELIMITER $$
DROP PROCEDURE IF EXISTS LAB12_REPORTE_OBSERVADOS$$
CREATE PROCEDURE LAB12_REPORTE_OBSERVADOS(IN _conv INT,IN _mod INT,IN _desde DATE,IN _hasta DATE)
BEGIN
 SELECT p.id postulacion_id,o.id observacion_id,
 c.id convocatoria_id,c.nombre convocatoria,m.id modalidad_id,m.nombre modalidad,
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
DELIMITER ;
