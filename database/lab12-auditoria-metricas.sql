-- H04: metadatos de lectura; no modifica ni elimina filas históricas. Aplicar después de lab12-archivos.sql.
USE admitu_db;
DELIMITER $$
DROP PROCEDURE IF EXISTS LISTAR_DOCUMENTO_POSTULACION_X_ID$$
CREATE PROCEDURE LISTAR_DOCUMENTO_POSTULACION_X_ID(
    IN _id INT
)
BEGIN
    SELECT d.id, d.postulacion_id, d.requisito_aplicable_id AS requisito_aplicable_id, r.id AS requisito_id, d.numero_version,
        d.nombre_archivo, d.tipo_archivo, d.tamanio_archivo, d.ruta_archivo, d.fecha_carga,
        d.estado_documento, d.fecha_evaluacion, d.comentario_evaluacion,
        EXISTS(SELECT 1 FROM archivo_digital a JOIN postulacion p ON p.id=d.postulacion_id
            WHERE a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.postulante_id=p.postulante_id
            AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0) AS archivo_activo,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM documento_postulacion d
    INNER JOIN requisito_convocatoria_modalidad rcm ON rcm.id = d.requisito_aplicable_id
    INNER JOIN requisito r ON r.id = rcm.requisito_id
    WHERE d.id = _id;
END$$

DROP PROCEDURE IF EXISTS LISTAR_DOCUMENTOS_POSTULACION_TODOS$$
CREATE PROCEDURE LISTAR_DOCUMENTOS_POSTULACION_TODOS()
BEGIN
    SELECT d.id, d.postulacion_id, d.requisito_aplicable_id AS requisito_aplicable_id, r.id AS requisito_id, d.numero_version,
        d.nombre_archivo, d.tipo_archivo, d.tamanio_archivo, d.ruta_archivo, d.fecha_carga,
        d.estado_documento, d.fecha_evaluacion, d.comentario_evaluacion,
        EXISTS(SELECT 1 FROM archivo_digital a JOIN postulacion p ON p.id=d.postulacion_id
            WHERE a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.postulante_id=p.postulante_id
            AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0) AS archivo_activo,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM documento_postulacion d
    INNER JOIN requisito_convocatoria_modalidad rcm ON rcm.id = d.requisito_aplicable_id
    INNER JOIN requisito r ON r.id = rcm.requisito_id
    ORDER BY d.postulacion_id, d.numero_version;
END$$

DELIMITER ;
