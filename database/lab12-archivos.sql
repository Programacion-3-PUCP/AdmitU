-- LAB12 RF05/08/11. Migración aditiva, únicamente admitu_db de ensayo verificada.
USE admitu_db;
CREATE TABLE IF NOT EXISTS archivo_digital (
 id INT AUTO_INCREMENT PRIMARY KEY,
 postulante_id INT NOT NULL,
 tipo ENUM('DNI','FOTOGRAFIA','CERTIFICADO','VOUCHER','DOCUMENTO') NOT NULL,
 referencia_id INT NOT NULL DEFAULT 0,
 nombre VARCHAR(150) NOT NULL,
 mime VARCHAR(100) NOT NULL,
 contenido MEDIUMBLOB NOT NULL,
 sha256 CHAR(64) NOT NULL,
 fecha_registro DATE NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 UNIQUE KEY uq_archivo_propietario_tipo(postulante_id,tipo,referencia_id),
 CONSTRAINT fk_archivo_postulante FOREIGN KEY(postulante_id) REFERENCES postulante(persona_id)
) ENGINE=InnoDB;
DELIMITER $$
DROP PROCEDURE IF EXISTS GUARDAR_CERTIFICADO_METADATA$$
CREATE PROCEDURE GUARDAR_CERTIFICADO_METADATA(OUT _id INT,IN _anterior INT,IN _postulante INT,IN _institucion VARCHAR(150),IN _inicio INT,IN _fin INT,IN _nombre VARCHAR(150))
BEGIN
 DECLARE _institucion_id INT DEFAULT NULL;
 DECLARE _persona INT;
 SELECT persona_id INTO _persona FROM postulante WHERE persona_id=_postulante FOR UPDATE;
 IF _persona IS NULL OR _institucion IS NULL OR CHAR_LENGTH(TRIM(_institucion))=0 OR _inicio<1900 OR _fin<_inicio OR _fin>YEAR(CURDATE()) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Datos de certificado inválidos';END IF;
 SELECT MIN(id) INTO _institucion_id FROM institucion_educativa WHERE nombre=TRIM(_institucion) AND activo=1;
 IF _institucion_id IS NULL THEN
  INSERT INTO institucion_educativa(pais_id,codigo_externo,nombre,tipo_institucion,activo) VALUES(1,NULL,TRIM(_institucion),'COLEGIO',1);
  SET _institucion_id=LAST_INSERT_ID();
 END IF;
 IF _anterior>0 THEN
  IF NOT EXISTS(SELECT 1 FROM antecedente_academico WHERE id=_anterior AND postulante_id=_postulante AND activo=1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Certificado no autorizado';END IF;
  UPDATE antecedente_academico SET institucion_id=_institucion_id,anio_inicio=_inicio,anio_fin=_fin,descripcion=_nombre WHERE id=_anterior;
  SET _id=_anterior;
 ELSE
  INSERT INTO antecedente_academico(postulante_id,institucion_id,anio_inicio,anio_fin,descripcion,activo) VALUES(_postulante,_institucion_id,_inicio,_fin,_nombre,1);
  SET _id=LAST_INSERT_ID();
 END IF;
END$$
DROP PROCEDURE IF EXISTS GUARDAR_VOUCHER_METADATA$$
CREATE PROCEDURE GUARDAR_VOUCHER_METADATA(IN _pago INT,IN _postulante INT,IN _operacion VARCHAR(30),IN _monto DECIMAL(10,2))
BEGIN
 DECLARE _estado VARCHAR(20) DEFAULT NULL;DECLARE _importe DECIMAL(10,2);DECLARE _persona INT DEFAULT NULL;
 SELECT persona_id INTO _persona FROM postulante WHERE persona_id=_postulante FOR UPDATE;
 IF _persona IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulante inexistente';END IF;
 SELECT p.estado_pago,p.monto INTO _estado,_importe FROM pago p JOIN postulacion x ON x.id=p.postulacion_id WHERE p.id=_pago AND x.postulante_id=_postulante FOR UPDATE;
 IF _estado IS NULL OR _estado NOT IN ('GENERADO','PENDIENTE','RECHAZADO') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El voucher solo se adjunta a un pago no aprobado';END IF;
 IF _monto<>_importe OR _monto<=0 OR _operacion IS NULL OR CHAR_LENGTH(_operacion)<4 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El importe o número de operación no es válido';END IF;
 IF EXISTS(SELECT 1 FROM pago WHERE id<>_pago AND referencia_pasarela=_operacion AND estado_pago IN('PENDIENTE','APROBADO')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El número de operación ya fue utilizado';END IF;
 UPDATE pago SET estado_pago='PENDIENTE',referencia_pasarela=_operacion,fecha_pago=CURDATE(),ruta_voucher=CONCAT('archivo:pago:',id) WHERE id=_pago;
END$$
DROP PROCEDURE IF EXISTS GUARDAR_ARCHIVO_DIGITAL$$
CREATE PROCEDURE GUARDAR_ARCHIVO_DIGITAL(OUT _id INT,IN _postulante INT,IN _tipo VARCHAR(20),IN _referencia INT,IN _nombre VARCHAR(150),IN _mime VARCHAR(100),IN _contenido MEDIUMBLOB,IN _sha CHAR(64),IN _fecha DATE,IN _requisito INT)
BEGIN
 DECLARE _persona INT DEFAULT NULL;DECLARE _postulacion INT DEFAULT NULL;DECLARE _estado VARCHAR(20);DECLARE _maximo INT;DECLARE _formato VARCHAR(20);
 SELECT persona_id INTO _persona FROM postulante WHERE persona_id=_postulante FOR UPDATE;
 IF _persona IS NULL OR _fecha IS NULL OR _fecha>CURDATE() OR OCTET_LENGTH(_contenido)=0 OR OCTET_LENGTH(_contenido)>5242880 THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Propietario, fecha o tamaño de archivo inválidos';END IF;
 IF _tipo IN('DNI','FOTOGRAFIA') THEN
  SET _referencia=0;
  IF _mime NOT IN('image/jpeg','image/png') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='DNI y fotografía deben ser JPG o PNG';END IF;
 ELSEIF _tipo='CERTIFICADO' THEN
  IF NOT EXISTS(SELECT 1 FROM antecedente_academico WHERE id=_referencia AND postulante_id=_postulante AND activo=1) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Certificado no autorizado';END IF;
 ELSEIF _tipo='VOUCHER' THEN
  IF NOT EXISTS(SELECT 1 FROM pago p JOIN postulacion x ON x.id=p.postulacion_id WHERE p.id=_referencia AND x.postulante_id=_postulante AND p.estado_pago='PENDIENTE') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Voucher no autorizado';END IF;
 ELSEIF _tipo='DOCUMENTO' THEN
  SELECT d.postulacion_id,d.estado_documento,r.tamanio_maximo_bytes,r.tipo_archivo_requerido INTO _postulacion,_estado,_maximo,_formato
   FROM documento_postulacion d JOIN postulacion x ON x.id=d.postulacion_id JOIN requisito_convocatoria_modalidad rm ON rm.id=d.requisito_aplicable_id JOIN requisito r ON r.id=rm.requisito_id
   WHERE d.id=_referencia AND x.postulante_id=_postulante AND rm.convocatoria_modalidad_id=x.modalidad_elegida_id FOR UPDATE;
  IF _postulacion IS NULL OR _estado='APROBADO' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Documento inexistente, ajeno o aprobado';END IF;
  IF OCTET_LENGTH(_contenido)>_maximo OR (_formato='PDF' AND _mime<>'application/pdf') OR (_formato='IMAGEN' AND _mime NOT IN('image/jpeg','image/png')) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='El archivo incumple el formato o tamaño del requisito';END IF;
  UPDATE documento_postulacion SET numero_version=numero_version+IF(_estado IN('OBSERVADO','RECHAZADO'),1,0),nombre_archivo=_nombre,tamanio_archivo=OCTET_LENGTH(_contenido),tipo_archivo=IF(_mime='application/pdf','PDF','IMAGEN'),ruta_archivo=CONCAT('archivo:documento:',id),fecha_carga=_fecha,estado_documento='PENDIENTE',fecha_evaluacion=NULL,comentario_evaluacion=NULL WHERE id=_referencia;
  IF _estado IN('OBSERVADO','RECHAZADO') THEN
   UPDATE documento_observacion SET estado_observacion='SUBSANADA',fecha_subsanacion=_fecha,comentario_subsanacion='Archivo reemplazado; pendiente de reevaluación' WHERE documento_id=_referencia AND estado_observacion='PENDIENTE';
   IF EXISTS(SELECT 1 FROM postulacion WHERE id=_postulacion AND estado_actual_id=4) AND EXISTS(SELECT 1 FROM pago WHERE postulacion_id=_postulacion AND estado_pago='APROBADO') AND NOT EXISTS(SELECT 1 FROM documento_observacion o JOIN documento_postulacion d ON d.id=o.documento_id WHERE d.postulacion_id=_postulacion AND o.estado_observacion='PENDIENTE') THEN CALL MODIFICAR_POSTULACION(_postulacion,3,'Documentos subsanados; pendiente de reevaluación');END IF;
  END IF;
 ELSE SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Tipo de archivo inválido';
 END IF;
 INSERT INTO archivo_digital(postulante_id,tipo,referencia_id,nombre,mime,contenido,sha256,fecha_registro,activo)
 VALUES(_postulante,_tipo,_referencia,_nombre,_mime,_contenido,_sha,_fecha,1)
 ON DUPLICATE KEY UPDATE id=LAST_INSERT_ID(id),nombre=_nombre,mime=_mime,contenido=_contenido,sha256=_sha,fecha_registro=_fecha,activo=1;
 SET _id=LAST_INSERT_ID();
END$$
DROP PROCEDURE IF EXISTS LISTAR_ARCHIVOS_POSTULANTE$$
CREATE PROCEDURE LISTAR_ARCHIVOS_POSTULANTE(IN _postulante INT)
BEGIN SELECT a.id,a.postulante_id,a.tipo,a.referencia_id,a.nombre,a.mime,a.sha256,a.fecha_registro,OCTET_LENGTH(a.contenido) AS tamanio,i.nombre AS institucion,aa.anio_inicio,aa.anio_fin FROM archivo_digital a LEFT JOIN antecedente_academico aa ON a.tipo='CERTIFICADO' AND aa.id=a.referencia_id LEFT JOIN institucion_educativa i ON i.id=aa.institucion_id WHERE a.postulante_id=_postulante AND a.activo=1 ORDER BY a.id;END$$
DROP PROCEDURE IF EXISTS BUSCAR_ARCHIVO_DIGITAL$$
CREATE PROCEDURE BUSCAR_ARCHIVO_DIGITAL(IN _id INT)
BEGIN SELECT a.*,OCTET_LENGTH(a.contenido) AS tamanio,i.nombre AS institucion,aa.anio_inicio,aa.anio_fin FROM archivo_digital a LEFT JOIN antecedente_academico aa ON a.tipo='CERTIFICADO' AND aa.id=a.referencia_id LEFT JOIN institucion_educativa i ON i.id=aa.institucion_id WHERE a.id=_id AND a.activo=1;END$$
DROP PROCEDURE IF EXISTS ELIMINAR_ARCHIVO_DIGITAL$$
CREATE PROCEDURE ELIMINAR_ARCHIVO_DIGITAL(IN _id INT)
BEGIN
 DECLARE _tipo VARCHAR(20);DECLARE _referencia INT;DECLARE _postulante INT;DECLARE _persona INT;DECLARE _estado_pago VARCHAR(20);DECLARE _estado_documento VARCHAR(20);
 -- Orden de locks compartido con carga: postulante -> pago -> archivo.
 SELECT postulante_id,tipo,referencia_id INTO _postulante,_tipo,_referencia FROM archivo_digital WHERE id=_id AND activo=1;
 IF _tipo IS NULL THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Archivo inexistente';END IF;
 SELECT persona_id INTO _persona FROM postulante WHERE persona_id=_postulante FOR UPDATE;
 IF _tipo='VOUCHER' THEN
  SELECT estado_pago INTO _estado_pago FROM pago WHERE id=_referencia FOR UPDATE;
  IF _estado_pago='APROBADO' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se puede eliminar un voucher aprobado';END IF;
  UPDATE pago SET estado_pago='GENERADO',referencia_pasarela=NULL,fecha_pago=NULL,ruta_voucher=NULL WHERE id=_referencia;
 END IF;
 SELECT tipo,referencia_id INTO _tipo,_referencia FROM archivo_digital WHERE id=_id AND activo=1 FOR UPDATE;
 IF _tipo='DOCUMENTO' THEN
  SELECT estado_documento INTO _estado_documento FROM documento_postulacion WHERE id=_referencia FOR UPDATE;
  IF _estado_documento='APROBADO' THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='No se puede eliminar un documento aprobado';END IF;
 END IF;
 IF _tipo='CERTIFICADO' THEN UPDATE antecedente_academico SET activo=0 WHERE id=_referencia;END IF;
 UPDATE archivo_digital SET activo=0 WHERE id=_id;
END$$
DELIMITER ;
