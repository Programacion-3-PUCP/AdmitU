-- Migración aditiva; solo admitu_db de ensayo previamente verificada.
USE admitu_db;
CREATE TABLE IF NOT EXISTS cuenta_usuario (
 id INT AUTO_INCREMENT PRIMARY KEY, persona_id INT NULL,correo VARCHAR(150) NOT NULL UNIQUE,
 rol ENUM('admin','evaluador','postulante') NOT NULL,clave_hash VARCHAR(255) NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,fallos INT NOT NULL DEFAULT 0,bloqueado_hasta DATETIME NULL,
 FOREIGN KEY(persona_id) REFERENCES persona(id),CHECK(rol='admin' OR persona_id IS NOT NULL)
) ENGINE=InnoDB;
CREATE TABLE IF NOT EXISTS sesion_usuario (
 token_hash CHAR(64) PRIMARY KEY,cuenta_id INT NOT NULL,vence DATETIME NOT NULL,
 FOREIGN KEY(cuenta_id) REFERENCES cuenta_usuario(id) ON DELETE CASCADE,INDEX(vence)
) ENGINE=InnoDB;
DELIMITER $$
DROP PROCEDURE IF EXISTS BUSCAR_CUENTA_CORREO$$
CREATE PROCEDURE BUSCAR_CUENTA_CORREO(IN _correo VARCHAR(150))
BEGIN SELECT id,persona_id,correo,rol,clave_hash,activo,(bloqueado_hasta IS NOT NULL AND bloqueado_hasta>UTC_TIMESTAMP()) AS bloqueada FROM cuenta_usuario WHERE correo=LOWER(TRIM(_correo)); END$$
DROP PROCEDURE IF EXISTS INSERTAR_CUENTA_USUARIO$$
CREATE PROCEDURE INSERTAR_CUENTA_USUARIO(IN _persona INT,IN _correo VARCHAR(150),IN _rol VARCHAR(20),IN _hash VARCHAR(255))
BEGIN
 IF _rol<>'admin' AND (_persona IS NULL OR _persona<=0) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La cuenta requiere persona'; END IF;
 IF _rol='evaluador' AND NOT EXISTS(SELECT 1 FROM evaluador WHERE persona_id=_persona AND activo=TRUE) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Evaluador no activo';END IF;
 IF _rol='postulante' AND NOT EXISTS(SELECT 1 FROM postulante WHERE persona_id=_persona) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulante inexistente';END IF;
 INSERT INTO cuenta_usuario(persona_id,correo,rol,clave_hash)VALUES(_persona,LOWER(TRIM(_correo)),_rol,_hash);
END$$
DROP PROCEDURE IF EXISTS REGISTRAR_INTENTO_CUENTA$$
CREATE PROCEDURE REGISTRAR_INTENTO_CUENTA(IN _id INT,IN _correcto BOOLEAN)
BEGIN UPDATE cuenta_usuario SET bloqueado_hasta=CASE WHEN _correcto THEN NULL WHEN fallos>=4 THEN DATE_ADD(UTC_TIMESTAMP(),INTERVAL 5 MINUTE) ELSE bloqueado_hasta END,fallos=CASE WHEN _correcto THEN 0 ELSE fallos+1 END WHERE id=_id;END$$
DROP PROCEDURE IF EXISTS INSERTAR_SESION_USUARIO$$
CREATE PROCEDURE INSERTAR_SESION_USUARIO(IN _hash CHAR(64),IN _cuenta INT)
BEGIN DELETE FROM sesion_usuario WHERE vence<=UTC_TIMESTAMP();INSERT INTO sesion_usuario(token_hash,cuenta_id,vence)VALUES(_hash,_cuenta,DATE_ADD(UTC_TIMESTAMP(),INTERVAL 8 HOUR));END$$
DROP PROCEDURE IF EXISTS BUSCAR_SESION_USUARIO$$
CREATE PROCEDURE BUSCAR_SESION_USUARIO(IN _hash CHAR(64))
BEGIN SELECT c.id,c.persona_id,c.correo,c.rol FROM sesion_usuario s JOIN cuenta_usuario c ON c.id=s.cuenta_id WHERE s.token_hash=_hash AND s.vence>UTC_TIMESTAMP() AND c.activo=TRUE AND(c.rol<>'evaluador' OR EXISTS(SELECT 1 FROM evaluador e WHERE e.persona_id=c.persona_id AND e.activo=TRUE));END$$
DROP PROCEDURE IF EXISTS ELIMINAR_SESION_USUARIO$$
CREATE PROCEDURE ELIMINAR_SESION_USUARIO(IN _hash CHAR(64))BEGIN DELETE FROM sesion_usuario WHERE token_hash=_hash;END$$
DROP PROCEDURE IF EXISTS LISTAR_IDS_AUTORIZADOS$$
CREATE PROCEDURE LISTAR_IDS_AUTORIZADOS(IN _recurso VARCHAR(60),IN _persona INT)
BEGIN
 CASE _recurso
 WHEN 'PostulanteRS' THEN SELECT persona_id AS id FROM postulante WHERE persona_id=_persona;
 WHEN 'AntecedenteAcademicoRS' THEN SELECT id FROM antecedente_academico WHERE postulante_id=_persona;
 WHEN 'PostulacionRS' THEN SELECT id FROM postulacion WHERE postulante_id=_persona;
 WHEN 'PagoRS' THEN SELECT p.id FROM pago p JOIN postulacion x ON x.id=p.postulacion_id WHERE x.postulante_id=_persona;
 WHEN 'DocumentoPostulacionRS' THEN SELECT d.id FROM documento_postulacion d JOIN postulacion x ON x.id=d.postulacion_id WHERE x.postulante_id=_persona;
 WHEN 'DocumentoObservacionRS' THEN SELECT o.id FROM documento_observacion o JOIN documento_postulacion d ON d.id=o.documento_id JOIN postulacion x ON x.id=d.postulacion_id WHERE x.postulante_id=_persona;
 WHEN 'PostulacionHistorialRS' THEN SELECT h.id FROM postulacion_historial h JOIN postulacion x ON x.id=h.postulacion_id WHERE x.postulante_id=_persona;
 WHEN 'CarnePostulanteRS' THEN SELECT c.id FROM carne_postulante c JOIN postulacion x ON x.id=c.postulacion_id WHERE x.postulante_id=_persona;
 ELSE SELECT 0 AS id WHERE FALSE;END CASE;
END$$
DROP TRIGGER IF EXISTS SINCRONIZAR_CORREO_CUENTA$$
CREATE TRIGGER SINCRONIZAR_CORREO_CUENTA AFTER UPDATE ON persona FOR EACH ROW
BEGIN IF NEW.correo<>OLD.correo THEN UPDATE cuenta_usuario SET correo=LOWER(TRIM(NEW.correo)) WHERE persona_id=NEW.id;END IF;END$$
DELIMITER ;
