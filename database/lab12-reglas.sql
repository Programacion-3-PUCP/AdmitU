-- LAB12. Aplicar únicamente admitu_db de ensayo verificada. No elimina duplicados.
USE admitu_db;
DELIMITER $$
DROP PROCEDURE IF EXISTS LAB12_PREPARAR_REGLAS$$
CREATE PROCEDURE LAB12_PREPARAR_REGLAS()
BEGIN
 IF DATABASE()<>'admitu_db' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Esquema no autorizado';END IF;
 IF EXISTS(SELECT 1 FROM postulacion GROUP BY postulante_id,convocatoria_id HAVING COUNT(*)>1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Revisar postulaciones duplicadas antes de migrar';END IF;
 IF EXISTS(SELECT 1 FROM pago WHERE estado_pago IN('GENERADO','PENDIENTE','APROBADO') GROUP BY postulacion_id HAVING COUNT(*)>1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Revisar pagos vigentes duplicados antes de migrar';END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='postulacion' AND index_name='uq_postulante_convocatoria') THEN ALTER TABLE postulacion ADD CONSTRAINT uq_postulante_convocatoria UNIQUE(postulante_id,convocatoria_id);END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='pago' AND column_name='postulacion_pago_vigente') THEN ALTER TABLE pago ADD COLUMN postulacion_pago_vigente INT GENERATED ALWAYS AS(CASE WHEN estado_pago IN('GENERADO','PENDIENTE','APROBADO') THEN postulacion_id ELSE NULL END) STORED;END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema=DATABASE() AND table_name='pago' AND index_name='uq_pago_vigente') THEN ALTER TABLE pago ADD CONSTRAINT uq_pago_vigente UNIQUE(postulacion_pago_vigente);END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='postulacion' AND column_name='sede_id') THEN ALTER TABLE postulacion ADD COLUMN sede_id INT NULL, ADD CONSTRAINT fk_post_sede FOREIGN KEY(sede_id) REFERENCES sede(id);END IF;
 IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='postulacion' AND column_name='evaluador_id') THEN ALTER TABLE postulacion ADD COLUMN evaluador_id INT NULL, ADD CONSTRAINT fk_post_eval FOREIGN KEY(evaluador_id) REFERENCES evaluador(persona_id);END IF;
END$$
CALL LAB12_PREPARAR_REGLAS()$$
DROP PROCEDURE LAB12_PREPARAR_REGLAS$$
DROP PROCEDURE IF EXISTS INSERTAR_POSTULACION$$
CREATE PROCEDURE INSERTAR_POSTULACION(OUT _id INT,IN _postulante_id INT,IN _convocatoria_id INT,IN _modalidad_id INT,IN _oferta_id INT,IN _estado_id INT,IN _codigo VARCHAR(30),IN _observacion VARCHAR(255))
BEGIN
 DECLARE _persona INT DEFAULT NULL;DECLARE _costo DECIMAL(10,2);
 SELECT persona_id INTO _persona FROM postulante WHERE persona_id=_postulante_id FOR UPDATE;
 IF _persona IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulante inexistente';END IF;
 IF EXISTS(SELECT 1 FROM postulacion WHERE postulante_id=_postulante_id AND convocatoria_id=_convocatoria_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Ya existe una postulación en la convocatoria, independientemente de la carrera';END IF;
 IF NOT EXISTS(SELECT 1 FROM convocatoria WHERE id=_convocatoria_id AND activo=1 AND estado='PUBLICADA' AND (fecha_inicio IS NULL OR fecha_inicio<=DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)) AND (fecha_fin IS NULL OR fecha_fin>=DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR))) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Convocatoria cerrada o no publicada';END IF;
 IF NOT EXISTS(SELECT 1 FROM convocatoria_modalidad cm JOIN modalidad m ON m.id=cm.modalidad_id WHERE cm.id=_modalidad_id AND cm.convocatoria_id=_convocatoria_id AND m.activo=1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Modalidad no válida para la convocatoria';END IF;
 IF NOT EXISTS(SELECT 1 FROM oferta_carrera o JOIN carrera c ON c.id=o.carrera_id WHERE o.id=_oferta_id AND o.convocatoria_id=_convocatoria_id AND c.activo=1 AND o.cantidad_vacantes>0) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Carrera no ofertada';END IF;
 IF _estado_id NOT IN(1,2) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Estado inicial no permitido';END IF;
 SELECT costo_inscripcion INTO _costo FROM convocatoria_modalidad WHERE id=_modalidad_id;
 INSERT INTO postulacion(postulante_id,convocatoria_id,modalidad_elegida_id,carrera_elegida_id,estado_actual_id,fecha_registro,fecha_envio,codigo_inscripcion,observacion_general)
 VALUES(_postulante_id,_convocatoria_id,_modalidad_id,_oferta_id,_estado_id,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),IF(_estado_id=2,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),NULL),_codigo,_observacion);
 SET _id=LAST_INSERT_ID();
 INSERT INTO pago(postulacion_id,medio_pago,monto,fecha_generacion,codigo_pago,estado_pago) VALUES(_id,'TRANSFERENCIA',_costo,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),CONCAT('PAG-',LPAD(_id,12,'0')),'GENERADO');
 INSERT INTO postulacion_historial(postulacion_id,estado_anterior_id,estado_actual_id,fecha_cambio,responsable_cambio,motivo_cambio) VALUES(_id,NULL,_estado_id,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),'SISTEMA','Registro de postulación');
END$$
DROP PROCEDURE IF EXISTS VALIDAR_ENVIO_POSTULACION$$
CREATE PROCEDURE VALIDAR_ENVIO_POSTULACION(IN _id INT,IN _sede INT)
BEGIN
 IF NOT EXISTS(SELECT 1 FROM sede WHERE id=_sede AND activo=1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Selecciona una sede activa';END IF;
 IF EXISTS(SELECT 1 FROM requisito_convocatoria_modalidad rm JOIN postulacion p ON p.modalidad_elegida_id=rm.convocatoria_modalidad_id WHERE p.id=_id AND rm.obligatorio=1 AND NOT EXISTS(
  SELECT 1 FROM documento_postulacion d JOIN archivo_digital a ON a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.activo=1 WHERE d.postulacion_id=_id AND d.requisito_aplicable_id=rm.id AND OCTET_LENGTH(a.contenido)>0)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Faltan archivos obligatorios de la postulación';END IF;
 UPDATE postulacion SET sede_id=_sede WHERE id=_id;
END$$
DROP PROCEDURE IF EXISTS ASIGNAR_EVALUADOR_POSTULACION$$
CREATE PROCEDURE ASIGNAR_EVALUADOR_POSTULACION(IN _id INT,IN _evaluador INT)
BEGIN
 IF NOT EXISTS(SELECT 1 FROM evaluador WHERE persona_id=_evaluador AND activo=1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Evaluador no activo';END IF;
 IF NOT EXISTS(SELECT 1 FROM postulacion WHERE id=_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulación inexistente';END IF;
 UPDATE postulacion SET evaluador_id=_evaluador WHERE id=_id;
END$$
DROP PROCEDURE IF EXISTS LISTAR_POSTULACION_X_ID$$
CREATE PROCEDURE LISTAR_POSTULACION_X_ID(IN _id INT) BEGIN SELECT * FROM postulacion WHERE id=_id;END$$
DROP PROCEDURE IF EXISTS LISTAR_POSTULACIONES_TODAS$$
CREATE PROCEDURE LISTAR_POSTULACIONES_TODAS() BEGIN SELECT * FROM postulacion;END$$
DROP PROCEDURE IF EXISTS MODIFICAR_POSTULACION$$
CREATE PROCEDURE MODIFICAR_POSTULACION(IN _id INT,IN _estado_id INT,IN _observacion VARCHAR(255))
BEGIN
 DECLARE _anterior INT DEFAULT NULL;
 SELECT estado_actual_id INTO _anterior FROM postulacion WHERE id=_id FOR UPDATE;
 IF _anterior IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulación inexistente';END IF;
 IF _anterior<>_estado_id THEN
  IF NOT((_anterior=1 AND _estado_id IN(2,8)) OR (_anterior=2 AND _estado_id IN(3,7,8)) OR (_anterior=3 AND _estado_id IN(4,5,7,8)) OR (_anterior=4 AND _estado_id IN(3,5,7,8)) OR (_anterior=5 AND _estado_id IN(6,7,8))) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Transición de estado no permitida';END IF;
  IF _estado_id IN(3,5,6) AND NOT EXISTS(SELECT 1 FROM pago WHERE postulacion_id=_id AND estado_pago='APROBADO') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El pago debe estar aprobado';END IF;
  IF _estado_id IN(5,6) AND EXISTS(SELECT 1 FROM requisito_convocatoria_modalidad rm JOIN postulacion p ON p.modalidad_elegida_id=rm.convocatoria_modalidad_id WHERE p.id=_id AND rm.obligatorio=1 AND NOT EXISTS(SELECT 1 FROM documento_postulacion d JOIN archivo_digital a ON a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0 WHERE d.postulacion_id=_id AND d.requisito_aplicable_id=rm.id AND d.estado_documento='APROBADO')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Faltan documentos obligatorios aprobados';END IF;
  IF _estado_id=6 AND NOT EXISTS(SELECT 1 FROM carne_postulante WHERE postulacion_id=_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Primero genera el carné';END IF;
  INSERT INTO postulacion_historial(postulacion_id,estado_anterior_id,estado_actual_id,fecha_cambio,responsable_cambio,motivo_cambio) VALUES(_id,_anterior,_estado_id,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),'SERVICIO',COALESCE(_observacion,'Cambio de estado'));
 END IF;
 UPDATE postulacion SET estado_actual_id=_estado_id,observacion_general=_observacion,fecha_envio=IF(_estado_id=2,COALESCE(fecha_envio,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)),fecha_envio),fecha_finalizacion=IF(_estado_id=6,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),fecha_finalizacion) WHERE id=_id;
END$$
DROP PROCEDURE IF EXISTS ELIMINAR_CONVOCATORIA$$
CREATE PROCEDURE ELIMINAR_CONVOCATORIA(IN _id INT)
BEGIN
 IF NOT EXISTS(SELECT 1 FROM convocatoria WHERE id=_id AND estado='BORRADOR') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Solo se puede dar de baja un borrador';END IF;
 IF EXISTS(SELECT 1 FROM postulacion WHERE convocatoria_id=_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La convocatoria tiene postulaciones';END IF;
 UPDATE convocatoria SET activo=0 WHERE id=_id;
END$$
DROP PROCEDURE IF EXISTS INSERTAR_PAGO$$
CREATE PROCEDURE INSERTAR_PAGO(OUT _id INT,IN _postulacion_id INT,IN _medio VARCHAR(20),IN _monto DECIMAL(10,2),IN _codigo VARCHAR(30),IN _referencia VARCHAR(100),IN _estado VARCHAR(20),IN _voucher VARCHAR(255))
BEGIN
 DECLARE _costo DECIMAL(10,2) DEFAULT NULL;DECLARE _post INT;
 SELECT id INTO _post FROM postulacion WHERE id=_postulacion_id FOR UPDATE;
 SELECT cm.costo_inscripcion INTO _costo FROM postulacion p JOIN convocatoria_modalidad cm ON cm.id=p.modalidad_elegida_id WHERE p.id=_postulacion_id;
 IF _post IS NULL OR _estado<>'GENERADO' OR _medio<>'TRANSFERENCIA' OR _monto<>_costo THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Datos iniciales de pago inválidos';END IF;
 IF EXISTS(SELECT 1 FROM pago WHERE postulacion_id=_postulacion_id AND estado_pago IN('GENERADO','PENDIENTE','APROBADO')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La postulación ya tiene un pago vigente';END IF;
 INSERT INTO pago(postulacion_id,medio_pago,monto,fecha_generacion,codigo_pago,estado_pago) VALUES(_postulacion_id,'TRANSFERENCIA',_costo,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),_codigo,'GENERADO');SET _id=LAST_INSERT_ID();
END$$
DROP PROCEDURE IF EXISTS MODIFICAR_PAGO$$
CREATE PROCEDURE MODIFICAR_PAGO(IN _id INT,IN _estado VARCHAR(20),IN _referencia VARCHAR(100),IN _voucher VARCHAR(255))
BEGIN
 DECLARE _anterior VARCHAR(20) DEFAULT NULL;DECLARE _post INT;
 SELECT estado_pago,postulacion_id INTO _anterior,_post FROM pago WHERE id=_id FOR UPDATE;
 IF _anterior IS NULL OR _anterior='APROBADO' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El pago no existe o ya está aprobado';END IF;
 IF NOT((_anterior='PENDIENTE' AND _estado IN('APROBADO','RECHAZADO')) OR (_anterior IN('GENERADO','PENDIENTE','RECHAZADO') AND _estado='VENCIDO')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Transición de pago no permitida';END IF;
 IF _estado='APROBADO' AND NOT EXISTS(SELECT 1 FROM archivo_digital WHERE tipo='VOUCHER' AND referencia_id=_id AND activo=1 AND OCTET_LENGTH(contenido)>0) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Adjunta primero un voucher real';END IF;
 UPDATE pago SET estado_pago=_estado,fecha_pago=IF(_estado='APROBADO',COALESCE(fecha_pago,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)),fecha_pago) WHERE id=_id;
 IF _estado='APROBADO' AND EXISTS(SELECT 1 FROM postulacion WHERE id=_post AND estado_actual_id=2) THEN CALL MODIFICAR_POSTULACION(_post,3,'Pago aprobado');END IF;
END$$
DROP PROCEDURE IF EXISTS ELIMINAR_PAGO$$
CREATE PROCEDURE ELIMINAR_PAGO(IN _id INT)
BEGIN
 IF EXISTS(SELECT 1 FROM pago WHERE id=_id AND estado_pago='APROBADO') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Un pago aprobado es inmutable';END IF;
 UPDATE pago SET estado_pago='VENCIDO' WHERE id=_id;
END$$
DROP PROCEDURE IF EXISTS INSERTAR_CARNE_POSTULANTE$$
CREATE PROCEDURE INSERTAR_CARNE_POSTULANTE(OUT _id INT,IN _postulacion_id INT,IN _sede_id INT,IN _codigo_carne VARCHAR(30),IN _fecha_generacion DATE,IN _fecha_inicio_vigencia DATE,IN _fecha_fin_vigencia DATE,IN _aula_examen VARCHAR(30))
BEGIN
 DECLARE _estado INT DEFAULT NULL;
 SELECT estado_actual_id INTO _estado FROM postulacion WHERE id=_postulacion_id FOR UPDATE;
 IF _estado<>5 OR NOT EXISTS(SELECT 1 FROM pago WHERE postulacion_id=_postulacion_id AND estado_pago='APROBADO') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El expediente debe ser apto y tener pago aprobado';END IF;
 IF NOT EXISTS(SELECT 1 FROM sede WHERE id=_sede_id AND activo=1) OR _fecha_inicio_vigencia>_fecha_fin_vigencia THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Sede o vigencia inválida';END IF;
 INSERT INTO carne_postulante(postulacion_id,sede_id,codigo_carne,fecha_generacion,fecha_inicio_vigencia,fecha_fin_vigencia,aula_examen) VALUES(_postulacion_id,_sede_id,_codigo_carne,COALESCE(_fecha_generacion,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)),_fecha_inicio_vigencia,_fecha_fin_vigencia,_aula_examen);SET _id=LAST_INSERT_ID();
 CALL MODIFICAR_POSTULACION(_postulacion_id,6,'Carné emitido');
END$$
DROP PROCEDURE IF EXISTS MODIFICAR_CARNE_POSTULANTE$$
CREATE PROCEDURE MODIFICAR_CARNE_POSTULANTE(
    IN _id INT,
    IN _postulacion_id INT,
    IN _sede_id INT,
    IN _codigo_carne VARCHAR(30),
    IN _fecha_generacion DATE,
    IN _fecha_inicio_vigencia DATE,
    IN _fecha_fin_vigencia DATE,
    IN _aula_examen VARCHAR(30)
)
BEGIN
 IF NOT EXISTS(SELECT 1 FROM carne_postulante WHERE id=_id AND postulacion_id=_postulacion_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El carné no existe o se intenta trasladar a otra postulación';END IF;
 IF _aula_examen<>'ANULADO' AND (NOT EXISTS(SELECT 1 FROM postulacion WHERE id=_postulacion_id AND estado_actual_id IN(5,6)) OR NOT EXISTS(SELECT 1 FROM pago WHERE postulacion_id=_postulacion_id AND estado_pago='APROBADO')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El expediente no es apto o el pago no está aprobado';END IF;
 IF NOT EXISTS(SELECT 1 FROM sede WHERE id=_sede_id AND activo=1) OR _fecha_inicio_vigencia>_fecha_fin_vigencia THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Sede o vigencia no válida';END IF;
 UPDATE carne_postulante SET sede_id=_sede_id,codigo_carne=_codigo_carne,fecha_generacion=_fecha_generacion,fecha_inicio_vigencia=_fecha_inicio_vigencia,fecha_fin_vigencia=_fecha_fin_vigencia,aula_examen=_aula_examen WHERE id=_id;
END$$
DROP PROCEDURE IF EXISTS ELIMINAR_CARNE_POSTULANTE$$
CREATE PROCEDURE ELIMINAR_CARNE_POSTULANTE(IN _id INT)
BEGIN
 IF NOT EXISTS(SELECT 1 FROM carne_postulante WHERE id=_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Carné inexistente';END IF;
 -- El marcador existente es interpretado como ANULADO por el cliente Blazor.
 -- Conserva código, titular, vigencia y la traza de emisión.
 UPDATE carne_postulante SET aula_examen='ANULADO' WHERE id=_id;
END$$
DROP PROCEDURE IF EXISTS MODIFICAR_DOCUMENTO_POSTULACION$$
CREATE PROCEDURE MODIFICAR_DOCUMENTO_POSTULACION(
    IN _id INT,
    IN _postulacion_id INT,
    IN _requisito_id INT,
    IN _numero_version INT,
    IN _nombre_archivo VARCHAR(150),
    IN _tipo_archivo ENUM('PDF','IMAGEN','CUALQUIERA'),
    IN _tamanio_archivo BIGINT,
    IN _ruta_archivo VARCHAR(255),
    IN _fecha_carga DATE,
    IN _estado_documento ENUM('PENDIENTE','APROBADO','OBSERVADO','RECHAZADO'),
    IN _fecha_evaluacion DATE,
    IN _comentario_evaluacion VARCHAR(255)
)
BEGIN
    DECLARE _post_actual INT DEFAULT NULL;DECLARE _requisito_actual INT;DECLARE _estado_actual VARCHAR(20);
    SELECT postulacion_id,requisito_aplicable_id,estado_documento INTO _post_actual,_requisito_actual,_estado_actual FROM documento_postulacion WHERE id=_id FOR UPDATE;
    IF _post_actual IS NULL OR _post_actual<>_postulacion_id OR _requisito_actual<>_requisito_id THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se puede trasladar el documento a otro expediente o requisito';END IF;
    IF EXISTS(SELECT 1 FROM postulacion WHERE id=_post_actual AND estado_actual_id IN(5,6,7,8)) AND _estado_actual<>_estado_documento THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El expediente ya está resuelto';END IF;
    IF _estado_documento='APROBADO' AND NOT EXISTS(SELECT 1 FROM archivo_digital WHERE tipo='DOCUMENTO' AND referencia_id=_id AND activo=1 AND OCTET_LENGTH(contenido)>0) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se puede aprobar un documento sin archivo durable activo';END IF;
    UPDATE documento_postulacion SET postulacion_id = _postulacion_id, requisito_aplicable_id = _requisito_id,
        numero_version = _numero_version, nombre_archivo = _nombre_archivo, tipo_archivo = _tipo_archivo,
        tamanio_archivo = _tamanio_archivo, ruta_archivo = _ruta_archivo, fecha_carga = _fecha_carga,
        estado_documento = _estado_documento, fecha_evaluacion = _fecha_evaluacion,
        comentario_evaluacion = _comentario_evaluacion
    WHERE id = _id;
END$$
DELIMITER ;
