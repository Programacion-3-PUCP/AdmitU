-- Migración aditiva e idempotente de cierre de auditoría LAB12 (pre-AWS).
-- Corrige listados de baja lógica, propagación de r.activo, topes de requisitos,
-- datos semilla de Admisión 2027-2, asignación de evaluadores y cuentas PBKDF2-HMAC-SHA256.
USE admitu_db;
SET SQL_SAFE_UPDATES = 0;

DELIMITER $$

-- 1. Listados administrativos sin filtro activo=1 para permitir reactivación (AD-04)
DROP PROCEDURE IF EXISTS LISTAR_CARRERAS_TODAS$$
CREATE PROCEDURE LISTAR_CARRERAS_TODAS()
BEGIN
    SELECT c.id,
           c.facultad_id,
           f.nombre AS nombre_facultad,
           c.codigo_carrera,
           c.nombre,
           c.activo
    FROM carrera c
    INNER JOIN facultad f ON c.facultad_id = f.id
    ORDER BY c.nombre, c.id;
END$$

DROP PROCEDURE IF EXISTS LISTAR_REQUISITOS_TODOS$$
CREATE PROCEDURE LISTAR_REQUISITOS_TODOS()
BEGIN
    SELECT id,
           codigo_requisito,
           nombre,
           descripcion,
           tipo_archivo_requerido,
           tamanio_maximo_bytes,
           activo
    FROM requisito
    ORDER BY codigo_requisito, id;
END$$

DROP PROCEDURE IF EXISTS LISTAR_EVALUADORES_TODOS$$
CREATE PROCEDURE LISTAR_EVALUADORES_TODOS()
BEGIN
    SELECT pe.id,
           pe.nombres,
           pe.apellido_paterno,
           pe.apellido_materno,
           pe.correo,
           pe.tipo_documento,
           pe.numero_documento,
           pe.telefono,
           e.cargo,
           e.activo
    FROM persona pe
    INNER JOIN evaluador e ON e.persona_id = pe.id
    ORDER BY pe.apellido_paterno, pe.apellido_materno, pe.id;
END$$

-- 2. Propagación de r.activo AS activo_requisito en tabla puente (AD-05 / A01)
DROP PROCEDURE IF EXISTS LISTAR_REQUISITO_CONVOCATORIA_MODALIDAD_X_ID$$
CREATE PROCEDURE LISTAR_REQUISITO_CONVOCATORIA_MODALIDAD_X_ID(IN _id INT)
BEGIN
    SELECT rc.id,
           rc.convocatoria_modalidad_id,
           rc.requisito_id,
           rc.obligatorio,
           rc.orden_presentacion,
           r.codigo_requisito,
           r.nombre AS nombre_requisito,
           r.descripcion AS descripcion_requisito,
           r.tipo_archivo_requerido AS tipo_archivo_requisito,
           r.tamanio_maximo_bytes AS tamanio_maximo_requisito,
           r.activo AS activo_requisito
    FROM requisito_convocatoria_modalidad rc
    INNER JOIN requisito r ON r.id = rc.requisito_id
    WHERE rc.id = _id;
END$$

DROP PROCEDURE IF EXISTS LISTAR_REQUISITOS_CONVOCATORIA_MODALIDAD_TODOS$$
CREATE PROCEDURE LISTAR_REQUISITOS_CONVOCATORIA_MODALIDAD_TODOS()
BEGIN
    SELECT rc.id,
           rc.convocatoria_modalidad_id,
           rc.requisito_id,
           rc.obligatorio,
           rc.orden_presentacion,
           r.codigo_requisito,
           r.nombre AS nombre_requisito,
           r.descripcion AS descripcion_requisito,
           r.tipo_archivo_requerido AS tipo_archivo_requisito,
           r.tamanio_maximo_bytes AS tamanio_maximo_requisito,
           r.activo AS activo_requisito
    FROM requisito_convocatoria_modalidad rc
    INNER JOIN requisito r ON r.id = rc.requisito_id
    ORDER BY rc.convocatoria_modalidad_id, rc.orden_presentacion;
END$$

-- Sincronización de correo en cuenta_usuario desde SP (compatible con AWS RDS sin SUPER/TRIGGER)
DROP PROCEDURE IF EXISTS MODIFICAR_POSTULANTE_PERFIL$$
CREATE PROCEDURE MODIFICAR_POSTULANTE_PERFIL(IN _id INT, IN _nombres VARCHAR(100), IN _ape_paterno VARCHAR(100), IN _ape_materno VARCHAR(100), IN _correo VARCHAR(150), IN _telefono VARCHAR(20), IN _fecha_nac DATE, IN _discapacidad BOOLEAN, IN _conadis VARCHAR(30))
BEGIN
    DECLARE _correo_anterior VARCHAR(150) DEFAULT NULL;
    IF _fecha_nac > DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='La fecha de nacimiento no puede ser futura'; END IF;
    IF _discapacidad AND (_conadis IS NULL OR TRIM(_conadis)='') THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Indique el carné CONADIS'; END IF;
    IF NOT EXISTS(SELECT 1 FROM postulante WHERE persona_id=_id) THEN SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Postulante inexistente'; END IF;
    SELECT correo INTO _correo_anterior FROM persona WHERE id=_id;
    CALL MODIFICAR_POSTULANTE(_id,_nombres,_ape_paterno,_ape_materno,_correo,_telefono,_fecha_nac);
    UPDATE postulante
    SET tiene_discapacidad = _discapacidad,
        numero_carnet_conadis = IF(_discapacidad, _conadis, NULL),
        correo_validado = IF(LOWER(TRIM(_correo)) = LOWER(TRIM(COALESCE(_correo_anterior, ''))), TRUE, FALSE),
        fecha_validacion_correo = IF(LOWER(TRIM(_correo)) = LOWER(TRIM(COALESCE(_correo_anterior, ''))), COALESCE(fecha_validacion_correo, DATE(UTC_TIMESTAMP() - INTERVAL 5 HOUR)), NULL)
    WHERE persona_id = _id;
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'cuenta_usuario') THEN
        UPDATE cuenta_usuario SET correo = LOWER(TRIM(_correo)) WHERE persona_id = _id;
    END IF;
END$$

DELIMITER ;

-- 3. Homogeneizar tamaño máximo de requisitos base a 5 MB como mínimo (P-01)
UPDATE requisito
SET tamanio_maximo_bytes = 5242880
WHERE id > 0 AND tamanio_maximo_bytes < 5242880;

-- 4. Completar requisitos aplicables faltantes en Admisión 2027-2 (cm9 y cm10) (AD-03)
INSERT INTO requisito_convocatoria_modalidad (convocatoria_modalidad_id, requisito_id, obligatorio, orden_presentacion)
SELECT cm.id, r.id, TRUE, req.orden
FROM convocatoria_modalidad cm
INNER JOIN (
    SELECT 1 AS id, 1 AS orden
    UNION ALL SELECT 2, 2
    UNION ALL SELECT 3, 3
    UNION ALL SELECT 8, 4
) req ON TRUE
INNER JOIN requisito r ON r.id = req.id
WHERE cm.id IN (9, 10)
  AND NOT EXISTS (
      SELECT 1
      FROM requisito_convocatoria_modalidad rc
      WHERE rc.convocatoria_modalidad_id = cm.id
        AND rc.requisito_id = r.id
  );

-- 5. Sincronizar asignación de evaluadores en expedientes semilla (E-03)
UPDATE postulacion p
INNER JOIN (
    SELECT d.postulacion_id, MIN(o.evaluador_id) AS eval_id
    FROM documento_observacion o
    INNER JOIN documento_postulacion d ON d.id = o.documento_id
    INNER JOIN evaluador e ON e.persona_id = o.evaluador_id AND e.activo = 1
    GROUP BY d.postulacion_id
) obs ON obs.postulacion_id = p.id
SET p.evaluador_id = obs.eval_id
WHERE p.id > 0 AND p.evaluador_id IS NULL;

UPDATE postulacion p
INNER JOIN (
    SELECT h.postulacion_id, MIN(pe.id) AS eval_id
    FROM postulacion_historial h
    INNER JOIN persona pe ON LOWER(TRIM(pe.correo)) = LOWER(TRIM(h.responsable_cambio))
    INNER JOIN evaluador e ON e.persona_id = pe.id AND e.activo = 1
    GROUP BY h.postulacion_id
) hist ON hist.postulacion_id = p.id
SET p.evaluador_id = hist.eval_id
WHERE p.id > 0 AND p.evaluador_id IS NULL;

UPDATE postulacion p
SET p.evaluador_id = CASE MOD(p.id, 3)
    WHEN 0 THEN 1
    WHEN 1 THEN 2
    ELSE 3
END
WHERE p.id > 0
  AND p.evaluador_id IS NULL
  AND p.estado_actual_id IN (2, 3, 4, 5, 6, 7)
  AND (SELECT COUNT(*) FROM evaluador WHERE persona_id IN (1, 2, 3) AND activo = 1) = 3;

-- 6. Cuentas semilla PBKDF2-HMAC-SHA256 (600,000 iteraciones, sin claves en texto plano) (B-01)
-- Usa ON DUPLICATE KEY UPDATE para garantizar el hash esperado y desbloquear cuentas (fallos=0).
INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
VALUES (NULL, 'admin@admitu.edu.pe', 'admin', 'pbkdf2_sha256$600000$Dht2V4gR/bJxDRPKJ5SgKA==$YEjYdzp4YUjJJXZ3J1sx4YMqx24n6pP08uAv3ePBtm8=', TRUE, 0, NULL)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 1, 'carmen.villanueva@admitu.edu.pe', 'evaluador', 'pbkdf2_sha256$600000$VbGvJUwGF4Rxq7GiWmH1DA==$zbVb6tdrpg3RjnFq+eehNK2wFbo1e1sciLeOWDA6eFs=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM evaluador WHERE persona_id = 1 AND activo = TRUE)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 2, 'jorge.quispe@admitu.edu.pe', 'evaluador', 'pbkdf2_sha256$600000$b+tBbj4h/ZeND4O2aqrI1g==$YnhCRdOKocGS2DD7lWOnihSLhEPW4/dm0OjamWQi5sY=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM evaluador WHERE persona_id = 2 AND activo = TRUE)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 3, 'patricia.salazar@admitu.edu.pe', 'evaluador', 'pbkdf2_sha256$600000$eW1s47ENiXh9SYKIKunUoQ==$Agk10Y3OknqB5xF+ipKH3U0v2WkX+IMrF5Py0ySMBTk=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM evaluador WHERE persona_id = 3 AND activo = TRUE)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 17, 'rocio.medina@admitu.edu.pe', 'evaluador', 'pbkdf2_sha256$600000$UBL4sGS2YXLX2uwOepX/6g==$dEIZhxcdzPTtmP25DzDYLlypwT85WhqNhchv5+WSouI=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM evaluador WHERE persona_id = 17 AND activo = TRUE)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 9, 'lucia.mendoza@mail.com', 'postulante', 'pbkdf2_sha256$600000$t634AWVwX100efXR1ReCFg==$ULHwT6HXeeAuEbTqOQIrVeB8/Tq7EdB80IGvAsfuZ5w=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 9)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 10, 'diego.diaz@mail.com', 'postulante', 'pbkdf2_sha256$600000$p6EDQOFZ4sCAzHxOYmF48A==$pwCXXD1Orl5e6izt5iCi7spnerFHhScO6q596PWayfI=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 10)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 11, 'valeria.ramirez@mail.com', 'postulante', 'pbkdf2_sha256$600000$1M205AE2ZXWc/NDMM3g7hQ==$uW2VzedSUWUqeOfYTrXXUtuirOyd9c9pRDeCpZUdsp8=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 11)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 12, 'andres.gomez@mail.com', 'postulante', 'pbkdf2_sha256$600000$tMKVfqibW/MRoR9JvGhg4Q==$8aBZGHvZb+2gVRyhw99Yhn9GFo9VncaStXdIWYyE2/g=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 12)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 13, 'camila.torres@mail.com', 'postulante', 'pbkdf2_sha256$600000$xbfKnjAgGY2TVF51nlsRVQ==$aqM3SSz1GOtG+3jL0xnjIbjoBypoj9xvLRZMXAQWfDo=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 13)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 14, 'sebastian.rojas@mail.com', 'postulante', 'pbkdf2_sha256$600000$CSXIVBlCJtlUQHD7lqlJlA==$51bsOv7CbnRodcqtkUu7x/XJ7hNJGkmMcSRMBJwgMPw=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 14)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 15, 'mariajose.paz@mail.com', 'postulante', 'pbkdf2_sha256$600000$/pl95Z+mDVZtE85ijCdjXQ==$cDeszckZ9+Op2e8zkIiJZuDu/L2Ne+Ak5Ada8RB2LbQ=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 15)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

INSERT INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT 16, 'joaquin.herrera@mail.com', 'postulante', 'pbkdf2_sha256$600000$VMKMjU9lDFo//vL4HddSnw==$fPqtFAOgIhbToduv7/+Ug0FRFxtiwkVVySn0CbIk7Vg=', TRUE, 0, NULL
FROM DUAL WHERE EXISTS (SELECT 1 FROM postulante WHERE persona_id = 16)
ON DUPLICATE KEY UPDATE persona_id = VALUES(persona_id), rol = VALUES(rol), clave_hash = VALUES(clave_hash), activo = TRUE, fallos = 0, bloqueado_hasta = NULL;

-- Resto de evaluadores activos (18..20) y postulantes de volumen (21..90)
INSERT IGNORE INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT pe.id, LOWER(TRIM(pe.correo)), 'evaluador', 'pbkdf2_sha256$600000$FHgoTPx7KT6I6S+cQMK+CA==$QuogOBljuXOiWE6fI7vZUqVaOA/+7CzL57ND1bzJ8+Q=', TRUE, 0, NULL
FROM evaluador ev
INNER JOIN persona pe ON pe.id = ev.persona_id
WHERE ev.activo = TRUE;

INSERT IGNORE INTO cuenta_usuario (persona_id, correo, rol, clave_hash, activo, fallos, bloqueado_hasta)
SELECT pe.id, LOWER(TRIM(pe.correo)), 'postulante', 'pbkdf2_sha256$600000$+HPYiYgNvAPPrQxT07CrBQ==$A4FIlbQT1UlOYPaNmriXMw0N97+8fWb45Vg8p06sJOM=', TRUE, 0, NULL
FROM postulante po
INNER JOIN persona pe ON pe.id = po.persona_id;
