-- Extiende el perfil sin cambiar las firmas históricas de LAB11.
USE admitu_db;
DELIMITER $$
DROP PROCEDURE IF EXISTS INSERTAR_POSTULANTE_PERFIL$$
CREATE PROCEDURE INSERTAR_POSTULANTE_PERFIL(OUT _id INT, IN _nombres VARCHAR(100), IN _ape_paterno VARCHAR(100), IN _ape_materno VARCHAR(100), IN _correo VARCHAR(150), IN _tipo_doc VARCHAR(10), IN _num_doc VARCHAR(20), IN _telefono VARCHAR(20), IN _fecha_nac DATE, IN _discapacidad BOOLEAN, IN _conadis VARCHAR(30))
BEGIN
 IF _fecha_nac > DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La fecha de nacimiento no puede ser futura'; END IF;
 IF _discapacidad AND (_conadis IS NULL OR TRIM(_conadis)='') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Indique el carné CONADIS'; END IF;
 CALL INSERTAR_POSTULANTE(_id,_nombres,_ape_paterno,_ape_materno,_correo,_tipo_doc,_num_doc,_telefono,_fecha_nac);
 UPDATE postulante SET tiene_discapacidad=_discapacidad, numero_carnet_conadis=IF(_discapacidad,_conadis,NULL) WHERE persona_id=_id;
END$$
DROP PROCEDURE IF EXISTS MODIFICAR_POSTULANTE_PERFIL$$
CREATE PROCEDURE MODIFICAR_POSTULANTE_PERFIL(IN _id INT, IN _nombres VARCHAR(100), IN _ape_paterno VARCHAR(100), IN _ape_materno VARCHAR(100), IN _correo VARCHAR(150), IN _telefono VARCHAR(20), IN _fecha_nac DATE, IN _discapacidad BOOLEAN, IN _conadis VARCHAR(30))
BEGIN
 IF _fecha_nac > DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La fecha de nacimiento no puede ser futura'; END IF;
 IF _discapacidad AND (_conadis IS NULL OR TRIM(_conadis)='') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Indique el carné CONADIS'; END IF;
 IF NOT EXISTS(SELECT 1 FROM postulante WHERE persona_id=_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulante inexistente'; END IF;
 CALL MODIFICAR_POSTULANTE(_id,_nombres,_ape_paterno,_ape_materno,_correo,_telefono,_fecha_nac);
 UPDATE postulante SET tiene_discapacidad=_discapacidad, numero_carnet_conadis=IF(_discapacidad,_conadis,NULL) WHERE persona_id=_id;
END$$
DROP PROCEDURE IF EXISTS LISTAR_POSTULANTE_X_ID$$
CREATE PROCEDURE LISTAR_POSTULANTE_X_ID(IN _id INT)
BEGIN
 SELECT p.id,p.nombres,p.apellido_paterno,p.apellido_materno,p.correo,p.tipo_documento,p.numero_documento,p.telefono,
 po.fecha_nacimiento,po.tiene_discapacidad,po.numero_carnet_conadis,po.correo_validado,po.fecha_validacion_correo
 FROM postulante po JOIN persona p ON p.id=po.persona_id WHERE po.persona_id=_id;
END$$
DROP PROCEDURE IF EXISTS LISTAR_POSTULANTES_TODOS$$
CREATE PROCEDURE LISTAR_POSTULANTES_TODOS()
BEGIN
 SELECT p.id,p.nombres,p.apellido_paterno,p.apellido_materno,p.correo,p.tipo_documento,p.numero_documento,p.telefono,
 po.fecha_nacimiento,po.tiene_discapacidad,po.numero_carnet_conadis,po.correo_validado,po.fecha_validacion_correo
 FROM postulante po JOIN persona p ON p.id=po.persona_id;
END$$
DELIMITER ;
