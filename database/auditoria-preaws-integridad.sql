-- Auditoría previa a AWS. Aplicar después de las migraciones LAB12; no borra filas.
-- Las variables de conexión solo las establece DBManager a partir de ActorContext.
-- Los BO de consola sin identidad se registran como SISTEMA; no son clientes REST.
USE admitu_db;
DELIMITER $$
DROP PROCEDURE IF EXISTS AUDITORIA_EXIGIR_EXPEDIENTE$$
CREATE PROCEDURE AUDITORIA_EXIGIR_EXPEDIENTE(IN _id INT,IN _permitir_propietario BOOLEAN)
BEGIN
 DECLARE _evaluador INT DEFAULT NULL;
 DECLARE _postulante INT DEFAULT NULL;
 -- La asignación no puede cambiar entre esta comprobación y la escritura.
 SELECT postulante_id,evaluador_id INTO _postulante,_evaluador FROM postulacion WHERE id=_id FOR UPDATE;
 IF _postulante IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulación inexistente';END IF;
 IF @admitu_actor_rol='evaluador' AND NOT (_evaluador <=> @admitu_actor_persona) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El expediente no está asignado al evaluador autenticado';END IF;
 IF @admitu_actor_rol='postulante' AND (_permitir_propietario=0 OR NOT (_postulante <=> @admitu_actor_persona)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El postulante no puede ejecutar esta operación';END IF;
 IF @admitu_actor_rol IS NOT NULL AND @admitu_actor_rol NOT IN('admin','evaluador','postulante') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Rol de servidor inválido';END IF;
END$$
DROP PROCEDURE IF EXISTS INSERTAR_POSTULACION$$
CREATE PROCEDURE INSERTAR_POSTULACION(OUT _id INT,IN _postulante_id INT,IN _convocatoria_id INT,IN _modalidad_id INT,IN _oferta_id INT,IN _estado_id INT,IN _codigo VARCHAR(30),IN _observacion VARCHAR(255))
BEGIN
 DECLARE _persona INT DEFAULT NULL;DECLARE _costo DECIMAL(10,2);
 IF @admitu_actor_rol='evaluador' OR (@admitu_actor_rol='postulante' AND NOT (_postulante_id <=> @admitu_actor_persona)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El actor no puede crear esta postulación';END IF;
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
 INSERT INTO postulacion_historial(postulacion_id,estado_anterior_id,estado_actual_id,fecha_cambio,responsable_cambio,motivo_cambio) VALUES(_id,NULL,_estado_id,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),COALESCE(@admitu_actor,'SISTEMA'),'Registro de postulación');
END$$

DROP PROCEDURE IF EXISTS MODIFICAR_POSTULACION$$
CREATE PROCEDURE MODIFICAR_POSTULACION(IN _id INT,IN _estado_id INT,IN _observacion VARCHAR(255))
BEGIN
 DECLARE _anterior INT DEFAULT NULL;
 CALL AUDITORIA_EXIGIR_EXPEDIENTE(_id,1);
 SELECT estado_actual_id INTO _anterior FROM postulacion WHERE id=_id FOR UPDATE;
 IF _anterior IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulación inexistente';END IF;
 IF _anterior<>_estado_id THEN
  IF NOT((_anterior=1 AND _estado_id IN(2,8)) OR (_anterior=2 AND _estado_id IN(3,7,8)) OR (_anterior=3 AND _estado_id IN(4,5,7,8)) OR (_anterior=4 AND _estado_id IN(3,5,7,8)) OR (_anterior=5 AND _estado_id IN(6,7,8))) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Transición de estado no permitida';END IF;
  IF _estado_id IN(3,5,6) AND NOT EXISTS(SELECT 1 FROM pago WHERE postulacion_id=_id AND estado_pago='APROBADO') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El pago debe estar aprobado';END IF;
  IF _estado_id IN(5,6) AND EXISTS(SELECT 1 FROM requisito_convocatoria_modalidad rm JOIN postulacion p ON p.modalidad_elegida_id=rm.convocatoria_modalidad_id WHERE p.id=_id AND rm.obligatorio=1 AND NOT EXISTS(SELECT 1 FROM documento_postulacion d JOIN archivo_digital a ON a.tipo='DOCUMENTO' AND a.referencia_id=d.id AND a.activo=1 AND OCTET_LENGTH(a.contenido)>0 WHERE d.postulacion_id=_id AND d.requisito_aplicable_id=rm.id AND d.estado_documento='APROBADO')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Faltan documentos obligatorios aprobados';END IF;
  IF _estado_id=6 AND NOT EXISTS(SELECT 1 FROM carne_postulante WHERE postulacion_id=_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Primero genera el carné';END IF;
  INSERT INTO postulacion_historial(postulacion_id,estado_anterior_id,estado_actual_id,fecha_cambio,responsable_cambio,motivo_cambio) VALUES(_id,_anterior,_estado_id,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),COALESCE(@admitu_actor,'SISTEMA'),COALESCE(_observacion,'Cambio de estado'));
 END IF;
 UPDATE postulacion SET estado_actual_id=_estado_id,observacion_general=_observacion,fecha_envio=IF(_estado_id=2,COALESCE(fecha_envio,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)),fecha_envio),fecha_finalizacion=IF(_estado_id=6,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),fecha_finalizacion) WHERE id=_id;
END$$

DROP PROCEDURE IF EXISTS MODIFICAR_PAGO$$
CREATE PROCEDURE MODIFICAR_PAGO(IN _id INT,IN _estado VARCHAR(20),IN _referencia VARCHAR(100),IN _voucher VARCHAR(255))
BEGIN
 DECLARE _anterior VARCHAR(20) DEFAULT NULL;DECLARE _post INT;
 SELECT postulacion_id INTO _post FROM pago WHERE id=_id;
 CALL AUDITORIA_EXIGIR_EXPEDIENTE(_post,0);
 SELECT estado_pago,postulacion_id INTO _anterior,_post FROM pago WHERE id=_id FOR UPDATE;
 IF _anterior IS NULL OR _anterior='APROBADO' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El pago no existe o ya está aprobado';END IF;
 IF NOT((_anterior='PENDIENTE' AND _estado IN('APROBADO','RECHAZADO')) OR (_anterior IN('GENERADO','PENDIENTE','RECHAZADO') AND _estado='VENCIDO')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Transición de pago no permitida';END IF;
 IF _estado='APROBADO' AND NOT EXISTS(SELECT 1 FROM archivo_digital WHERE tipo='VOUCHER' AND referencia_id=_id AND activo=1 AND OCTET_LENGTH(contenido)>0) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Adjunta primero un voucher real';END IF;
 UPDATE pago SET estado_pago=_estado,fecha_pago=IF(_estado='APROBADO',COALESCE(fecha_pago,DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)),fecha_pago) WHERE id=_id;
 IF _estado='APROBADO' AND EXISTS(SELECT 1 FROM postulacion WHERE id=_post AND estado_actual_id=2) THEN CALL MODIFICAR_POSTULACION(_post,3,'Pago aprobado');END IF;
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
    SELECT postulacion_id INTO _post_actual FROM documento_postulacion WHERE id=_id;
    CALL AUDITORIA_EXIGIR_EXPEDIENTE(_post_actual,0);
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

DROP PROCEDURE IF EXISTS INSERTAR_POSTULACION_HISTORIAL$$
CREATE PROCEDURE INSERTAR_POSTULACION_HISTORIAL(
    OUT _id INT,
    IN _postulacion_id INT,
    IN _estado_anterior_id INT,
    IN _estado_actual_id INT,
    IN _fecha_cambio DATE,
    IN _responsable_cambio VARCHAR(150),
    IN _motivo_cambio VARCHAR(255)
)
BEGIN
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El historial es inmutable; use transiciones del servidor';
END$$

DROP PROCEDURE IF EXISTS MODIFICAR_POSTULACION_HISTORIAL$$
CREATE PROCEDURE MODIFICAR_POSTULACION_HISTORIAL(
    IN _id INT,
    IN _postulacion_id INT,
    IN _estado_anterior_id INT,
    IN _estado_actual_id INT,
    IN _fecha_cambio DATE,
    IN _responsable_cambio VARCHAR(150),
    IN _motivo_cambio VARCHAR(255)
)
BEGIN
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El historial es inmutable; use transiciones del servidor';
END$$

DROP PROCEDURE IF EXISTS ELIMINAR_POSTULACION_HISTORIAL$$
CREATE PROCEDURE ELIMINAR_POSTULACION_HISTORIAL(
    IN _id INT
)
BEGIN
 SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El historial es inmutable; use transiciones del servidor';
END$$

DROP PROCEDURE IF EXISTS INSERTAR_DOCUMENTO_OBSERVACION$$
CREATE PROCEDURE INSERTAR_DOCUMENTO_OBSERVACION(
    OUT _id INT,
    IN _documento_id INT,
    IN _evaluador_id INT,
    IN _tipo_observacion ENUM('FALTANTE','ILEGIBLE','VENCIDO','OTRO'),
    IN _descripcion VARCHAR(255),
    IN _fecha_observacion DATE,
    IN _estado_observacion ENUM('PENDIENTE','SUBSANADA'),
    IN _fecha_subsanacion DATE,
    IN _comentario_subsanacion VARCHAR(255)
)
BEGIN
    DECLARE _post INT DEFAULT NULL;
    SELECT postulacion_id INTO _post FROM documento_postulacion WHERE id=_documento_id;
    CALL AUDITORIA_EXIGIR_EXPEDIENTE(_post,0);
    IF @admitu_actor_rol IS NOT NULL AND (@admitu_actor_rol<>'evaluador' OR NOT (_evaluador_id <=> @admitu_actor_persona)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El autor debe coincidir con el evaluador autenticado';END IF;
    IF _estado_observacion<>'PENDIENTE' OR _fecha_subsanacion IS NOT NULL OR _comentario_subsanacion IS NOT NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Una observación nueva debe estar pendiente de subsanación';END IF;
    INSERT INTO documento_observacion(documento_id, evaluador_id, tipo_observacion, descripcion,
        fecha_observacion, estado_observacion, fecha_subsanacion, comentario_subsanacion)
    VALUES(_documento_id, _evaluador_id, _tipo_observacion, _descripcion, DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR),
        _estado_observacion, _fecha_subsanacion, _comentario_subsanacion);
    SET _id = LAST_INSERT_ID();
END$$

DROP PROCEDURE IF EXISTS MODIFICAR_DOCUMENTO_OBSERVACION$$
CREATE PROCEDURE MODIFICAR_DOCUMENTO_OBSERVACION(
    IN _id INT,
    IN _documento_id INT,
    IN _evaluador_id INT,
    IN _tipo_observacion ENUM('FALTANTE','ILEGIBLE','VENCIDO','OTRO'),
    IN _descripcion VARCHAR(255),
    IN _fecha_observacion DATE,
    IN _estado_observacion ENUM('PENDIENTE','SUBSANADA'),
    IN _fecha_subsanacion DATE,
    IN _comentario_subsanacion VARCHAR(255)
)
BEGIN
    DECLARE _post INT DEFAULT NULL;
    DECLARE _documento_anterior INT DEFAULT NULL;
    DECLARE _autor INT DEFAULT NULL;
    DECLARE _estado_anterior VARCHAR(20);
    DECLARE _subsanacion DATE;
    DECLARE _comentario VARCHAR(255);
    SELECT d.postulacion_id INTO _post FROM documento_observacion o JOIN documento_postulacion d ON d.id=o.documento_id WHERE o.id=_id;
    CALL AUDITORIA_EXIGIR_EXPEDIENTE(_post,0);
    SELECT documento_id,evaluador_id,estado_observacion,fecha_subsanacion,comentario_subsanacion INTO _documento_anterior,_autor,_estado_anterior,_subsanacion,_comentario FROM documento_observacion WHERE id=_id FOR UPDATE;
    IF _documento_anterior IS NULL OR _documento_anterior<>_documento_id OR _autor<>_evaluador_id THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se puede trasladar la observación ni sustituir su autor';END IF;
    IF @admitu_actor_rol IS NOT NULL AND (@admitu_actor_rol<>'evaluador' OR NOT (_autor <=> @admitu_actor_persona)) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Solo su autor autenticado puede editar la observación';END IF;
    IF _estado_anterior<>_estado_observacion OR NOT (_subsanacion <=> _fecha_subsanacion) OR NOT (_comentario <=> _comentario_subsanacion) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La subsanación solo se registra al reemplazar el archivo';END IF;
    UPDATE documento_observacion SET documento_id = _documento_id, evaluador_id = _evaluador_id,
        tipo_observacion = _tipo_observacion, descripcion = _descripcion,
        estado_observacion = _estado_observacion, fecha_subsanacion = _fecha_subsanacion,
        comentario_subsanacion = _comentario_subsanacion
    WHERE id = _id;
END$$
DROP PROCEDURE IF EXISTS BUSCAR_SESION_USUARIO$$
CREATE PROCEDURE BUSCAR_SESION_USUARIO(IN _hash CHAR(64))
BEGIN
 SELECT c.id,c.persona_id,c.correo,c.rol FROM sesion_usuario s JOIN cuenta_usuario c ON c.id=s.cuenta_id
 WHERE s.token_hash=_hash AND s.vence>UTC_TIMESTAMP() AND c.activo=TRUE
 AND (c.bloqueado_hasta IS NULL OR c.bloqueado_hasta<=UTC_TIMESTAMP())
 AND (c.rol<>'evaluador' OR EXISTS(SELECT 1 FROM evaluador e WHERE e.persona_id=c.persona_id AND e.activo=TRUE));
END$$
DELIMITER ;
