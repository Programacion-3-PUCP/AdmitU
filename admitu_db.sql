DROP DATABASE IF EXISTS admitu_db;
CREATE DATABASE admitu_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE admitu_db;

SET FOREIGN_KEY_CHECKS = 0;

-- TABLAS MAESTRAS / CATÁLOGOS SIMPLES

CREATE TABLE pais (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_iso2 CHAR(2) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE sede (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200),
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE facultad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE etapa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    fecha_inicio DATE,
    fecha_fin DATE,
    codigo_etapa VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_etapa_fechas CHECK (fecha_inicio IS NULL OR fecha_fin IS NULL OR fecha_inicio <= fecha_fin)
) ENGINE=InnoDB;

CREATE TABLE requisito (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_requisito VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    descripcion VARCHAR(255),
    tipo_archivo_requerido ENUM('PDF','IMAGEN','CUALQUIERA') NOT NULL,
    tamanio_maximo_bytes INT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_requisito_tamanio CHECK (tamanio_maximo_bytes > 0)
) ENGINE=InnoDB;

CREATE TABLE modalidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_modalidad VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    requiere_colegio BOOLEAN NOT NULL DEFAULT FALSE,
    requiere_universidad BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;

CREATE TABLE estado_postulacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255)
) ENGINE=InnoDB;

-- JERARQUÍA PERSONA
-- PK del padre = PK y FK de los hijos

CREATE TABLE persona (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombres VARCHAR(100) NOT NULL,
    apellido_paterno VARCHAR(100) NOT NULL,
    apellido_materno VARCHAR(100),
    correo VARCHAR(150) NOT NULL UNIQUE,
    tipo_documento ENUM('DNI','CE','PAS','CPP') NOT NULL,
    numero_documento VARCHAR(20) NOT NULL,
    telefono VARCHAR(20),
    UNIQUE (tipo_documento, numero_documento)
) ENGINE=InnoDB;

CREATE TABLE apoderado (
    persona_id INT PRIMARY KEY,
    parentesco ENUM('PADRE','MADRE','TUTOR','OTRO') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_apoderado_persona FOREIGN KEY (persona_id) REFERENCES persona(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE postulante (
    persona_id INT PRIMARY KEY,
    apoderado_id INT NULL,
    fecha_nacimiento DATE,
    tiene_discapacidad BOOLEAN NOT NULL DEFAULT FALSE,
    numero_carnet_conadis VARCHAR(30),
    correo_validado BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_validacion_correo DATE,
    CONSTRAINT fk_postulante_persona FOREIGN KEY (persona_id) REFERENCES persona(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_postulante_apoderado FOREIGN KEY (apoderado_id) REFERENCES apoderado(persona_id)
        ON DELETE SET NULL,
    CONSTRAINT chk_postulante_conadis CHECK ((tiene_discapacidad = FALSE) OR (numero_carnet_conadis IS NOT NULL))
) ENGINE=InnoDB;

CREATE TABLE evaluador (
    persona_id INT PRIMARY KEY,
    cargo VARCHAR(100),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_evaluador_persona FOREIGN KEY (persona_id) REFERENCES persona(id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

-- INSTITUCIONES / ANTECEDENTES

CREATE TABLE institucion_educativa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    pais_id INT NOT NULL,
    codigo_externo VARCHAR(30),
    nombre VARCHAR(150) NOT NULL,
    tipo_institucion ENUM('COLEGIO','UNIVERSIDAD') NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_institucion_pais FOREIGN KEY (pais_id) REFERENCES pais(id)
) ENGINE=InnoDB;

CREATE TABLE antecedente_academico (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postulante_id INT NOT NULL,
    institucion_id INT NOT NULL,
    anio_inicio INT,
    anio_fin INT,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_antecedente_postulante FOREIGN KEY (postulante_id) REFERENCES postulante(persona_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_antecedente_institucion FOREIGN KEY (institucion_id) REFERENCES institucion_educativa(id),
    CONSTRAINT chk_antecedente_anios CHECK (anio_inicio IS NULL OR anio_fin IS NULL OR anio_inicio <= anio_fin)
) ENGINE=InnoDB;

-- CARRERAS

CREATE TABLE carrera (
    id INT AUTO_INCREMENT PRIMARY KEY,
    facultad_id INT NOT NULL,
    codigo_carrera VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_carrera_facultad FOREIGN KEY (facultad_id) REFERENCES facultad(id)
) ENGINE=InnoDB;

-- CONVOCATORIA Y CONFIGURACIÓN

CREATE TABLE convocatoria (
    id INT AUTO_INCREMENT PRIMARY KEY,
    codigo_convocatoria VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    periodo VARCHAR(20),
    fecha_inicio DATE,
    fecha_fin DATE,
    estado ENUM('BORRADOR','PUBLICADA','CERRADA','ANULADA') NOT NULL DEFAULT 'BORRADOR',
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_convocatoria_fechas CHECK (fecha_inicio IS NULL OR fecha_fin IS NULL OR fecha_inicio <= fecha_fin)
) ENGINE=InnoDB;

-- Relación N:M Convocatoria <-> Postulante
CREATE TABLE convocatoria_postulante (
    convocatoria_id INT NOT NULL,
    postulante_id INT NOT NULL,
    PRIMARY KEY (convocatoria_id, postulante_id),
    CONSTRAINT fk_convpost_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES convocatoria(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_convpost_postulante FOREIGN KEY (postulante_id) REFERENCES postulante(persona_id)
        ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE TABLE oferta_carrera (
    id INT AUTO_INCREMENT PRIMARY KEY,
    convocatoria_id INT NOT NULL,
    carrera_id INT NOT NULL,
    cantidad_vacantes INT NOT NULL,
    UNIQUE (convocatoria_id, carrera_id),
    CONSTRAINT fk_ofertacarrera_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES convocatoria(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_ofertacarrera_carrera FOREIGN KEY (carrera_id) REFERENCES carrera(id),
    CONSTRAINT chk_oferta_vacantes CHECK (cantidad_vacantes > 0)
) ENGINE=InnoDB;

CREATE TABLE convocatoria_modalidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    convocatoria_id INT NOT NULL,
    modalidad_id INT NOT NULL,
    costo_inscripcion DECIMAL(10,2) NOT NULL DEFAULT 0,
    observacion VARCHAR(255),
    UNIQUE (convocatoria_id, modalidad_id),
    CONSTRAINT fk_convmod_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES convocatoria(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_convmod_modalidad FOREIGN KEY (modalidad_id) REFERENCES modalidad(id),
    CONSTRAINT chk_convmod_costo CHECK (costo_inscripcion >= 0)
) ENGINE=InnoDB;

CREATE TABLE convocatoria_etapa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    convocatoria_id INT NOT NULL,
    etapa_id INT NOT NULL,
    fecha_inicio DATE,
    fecha_fin DATE,
    UNIQUE (convocatoria_id, etapa_id),
    CONSTRAINT fk_convetapa_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES convocatoria(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_convetapa_etapa FOREIGN KEY (etapa_id) REFERENCES etapa(id),
    CONSTRAINT chk_convetapa_fechas CHECK (fecha_inicio IS NULL OR fecha_fin IS NULL OR fecha_inicio <= fecha_fin)
) ENGINE=InnoDB;

CREATE TABLE requisito_convocatoria_modalidad (
    id INT AUTO_INCREMENT PRIMARY KEY,
    convocatoria_modalidad_id INT NOT NULL,
    requisito_id INT NOT NULL,
    obligatorio BOOLEAN NOT NULL DEFAULT TRUE,
    orden_presentacion INT,
    UNIQUE (convocatoria_modalidad_id, requisito_id),
    CONSTRAINT fk_reqconvmod_convmod FOREIGN KEY (convocatoria_modalidad_id) REFERENCES convocatoria_modalidad(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_reqconvmod_requisito FOREIGN KEY (requisito_id) REFERENCES requisito(id)
) ENGINE=InnoDB;

-- POSTULACIÓN Y SU CICLO DE VIDA

CREATE TABLE postulacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postulante_id INT NOT NULL,
    convocatoria_id INT NOT NULL,
    modalidad_elegida_id INT NULL,
    carrera_elegida_id INT NULL,
    estado_actual_id INT NOT NULL,
    fecha_registro DATE,
    fecha_envio DATE,
    fecha_finalizacion DATE,
    codigo_inscripcion VARCHAR(30) UNIQUE,
    observacion_general VARCHAR(255),
    CONSTRAINT fk_postulacion_postulante FOREIGN KEY (postulante_id) REFERENCES postulante(persona_id),
    CONSTRAINT fk_postulacion_convocatoria FOREIGN KEY (convocatoria_id) REFERENCES convocatoria(id),
    CONSTRAINT fk_postulacion_modalidad FOREIGN KEY (modalidad_elegida_id) REFERENCES convocatoria_modalidad(id),
    CONSTRAINT fk_postulacion_carrera FOREIGN KEY (carrera_elegida_id) REFERENCES oferta_carrera(id),
    CONSTRAINT fk_postulacion_estado FOREIGN KEY (estado_actual_id) REFERENCES estado_postulacion(id)
) ENGINE=InnoDB;

CREATE TABLE postulacion_historial (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postulacion_id INT NOT NULL,
    estado_anterior_id INT NULL,
    estado_actual_id INT NOT NULL,
    fecha_cambio DATE,
    responsable_cambio VARCHAR(150),
    motivo_cambio VARCHAR(255),
    CONSTRAINT fk_historial_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_historial_estadoanterior FOREIGN KEY (estado_anterior_id) REFERENCES estado_postulacion(id),
    CONSTRAINT fk_historial_estadoactual FOREIGN KEY (estado_actual_id) REFERENCES estado_postulacion(id)
) ENGINE=InnoDB;

CREATE TABLE pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postulacion_id INT NOT NULL,
    medio_pago ENUM('TARJETA','YAPE','TRANSFERENCIA') NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_generacion DATE,
    fecha_pago DATE,
    codigo_pago VARCHAR(30) UNIQUE,
    referencia_pasarela VARCHAR(100),
    estado_pago ENUM('GENERADO','PENDIENTE','APROBADO','RECHAZADO','VENCIDO') NOT NULL DEFAULT 'GENERADO',
    ruta_voucher VARCHAR(255),
    CONSTRAINT fk_pago_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
        ON DELETE RESTRICT,
    CONSTRAINT chk_pago_monto CHECK (monto >= 0)
) ENGINE=InnoDB;

CREATE TABLE documento_postulacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postulacion_id INT NOT NULL,
    requisito_aplicable_id INT NOT NULL,
    numero_version INT NOT NULL DEFAULT 1,
    nombre_archivo VARCHAR(150) NOT NULL,
    tipo_archivo ENUM('PDF','IMAGEN','CUALQUIERA') NOT NULL,
    tamanio_archivo BIGINT NOT NULL,
    ruta_archivo VARCHAR(255) NOT NULL,
    fecha_carga DATE,
    estado_documento ENUM('PENDIENTE','APROBADO','OBSERVADO','RECHAZADO') NOT NULL DEFAULT 'PENDIENTE',
    fecha_evaluacion DATE,
    comentario_evaluacion VARCHAR(255),
    UNIQUE (postulacion_id, requisito_aplicable_id, numero_version),
    CONSTRAINT fk_docpost_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_docpost_requisito FOREIGN KEY (requisito_aplicable_id) REFERENCES requisito(id),
    CONSTRAINT chk_docpost_tamanio CHECK (tamanio_archivo > 0)
) ENGINE=InnoDB;

CREATE TABLE documento_observacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    documento_id INT NOT NULL,
    evaluador_id INT NOT NULL,
    tipo_observacion ENUM('FALTANTE','ILEGIBLE','VENCIDO','OTRO') NOT NULL,
    descripcion VARCHAR(255),
    fecha_observacion DATE,
    estado_observacion ENUM('PENDIENTE','SUBSANADA') NOT NULL DEFAULT 'PENDIENTE',
    fecha_subsanacion DATE,
    comentario_subsanacion VARCHAR(255),
    CONSTRAINT fk_docobs_documento FOREIGN KEY (documento_id) REFERENCES documento_postulacion(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_docobs_evaluador FOREIGN KEY (evaluador_id) REFERENCES evaluador(persona_id)
) ENGINE=InnoDB;

CREATE TABLE carne_postulante (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postulacion_id INT NOT NULL UNIQUE,
    sede_id INT NOT NULL,
    codigo_carne VARCHAR(30) UNIQUE,
    fecha_generacion DATE,
    fecha_inicio_vigencia DATE,
    fecha_fin_vigencia DATE,
    aula_examen VARCHAR(30),
    CONSTRAINT fk_carne_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_carne_sede FOREIGN KEY (sede_id) REFERENCES sede(id),
    CONSTRAINT chk_carne_vigencia CHECK (fecha_inicio_vigencia IS NULL OR fecha_fin_vigencia IS NULL OR fecha_inicio_vigencia <= fecha_fin_vigencia)
) ENGINE=InnoDB;

CREATE TABLE notificacion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    postulacion_id INT NOT NULL,
    observacion_origen_id INT NULL,
    medio_notificacion ENUM('CORREO','BANDEJA_SISTEMA') NOT NULL,
    tipo_notificacion ENUM('VALIDACION','PAGO','OBSERVACION','FINALIZACION','CARNE') NOT NULL,
    destinatario VARCHAR(150) NOT NULL,
    asunto VARCHAR(150),
    mensaje TEXT,
    fecha_programada DATE,
    fecha_envio DATE,
    estado_envio ENUM('PENDIENTE','ENVIADA','FALLIDA') NOT NULL DEFAULT 'PENDIENTE',
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_lectura DATE,
    CONSTRAINT fk_notif_postulacion FOREIGN KEY (postulacion_id) REFERENCES postulacion(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_notif_observacion FOREIGN KEY (observacion_origen_id) REFERENCES documento_observacion(id)
) ENGINE=InnoDB;

-- Índices de apoyo a reportes
CREATE INDEX idx_pago_post_estado ON pago (postulacion_id, estado_pago);
CREATE INDEX idx_docpost_estado ON documento_postulacion (estado_documento);
CREATE INDEX idx_post_conv_estado ON postulacion (convocatoria_id, estado_actual_id);

SET FOREIGN_KEY_CHECKS = 1;

-- ADMITU - DML
-- Inserción de datos de prueba

-- Países
INSERT INTO pais (codigo_iso2, nombre) VALUES
('PE','Perú'),
('CO','Colombia'),
('CL','Chile');

-- Sedes
INSERT INTO sede (codigo, nombre, direccion) VALUES
('SEDE01','Campus PUCP San Miguel','Av. Universitaria 1801, Lima'),
('SEDE02','Sede Surco','Av. Primavera 200, Surco');

-- Facultades
INSERT INTO facultad (codigo, nombre) VALUES
('FIS','Facultad de Ciencias e Ingeniería'),
('FLETRAS','Facultad de Letras y Ciencias Humanas'),
('FGA','Facultad de Gestión y Alta Dirección');

-- Carreras
INSERT INTO carrera (facultad_id, codigo_carrera, nombre) VALUES
(1,'ING-INF','Ingeniería Informática'),
(1,'ING-IND','Ingeniería Industrial'),
(2,'PSICO','Psicología'),
(3,'GESTION','Gestión Empresarial');

-- Etapas
INSERT INTO etapa (fecha_inicio, fecha_fin, codigo_etapa, nombre, descripcion) VALUES
('2026-01-05','2026-02-15','INSC','Inscripción','Periodo de registro de postulantes'),
('2026-02-16','2026-02-28','EVAL','Evaluación de documentos','Revisión de expedientes'),
('2026-03-05','2026-03-05','EXAM','Examen de admisión','Aplicación de la prueba');

-- Requisitos
INSERT INTO requisito (codigo_requisito, nombre, descripcion, tipo_archivo_requerido, tamanio_maximo_bytes) VALUES
('REQ-DNI','Copia de DNI','Documento de identidad escaneado','IMAGEN',5242880),
('REQ-CERT','Certificado de estudios','Certificado de estudios secundarios','PDF',10485760),
('REQ-FOTO','Fotografía','Fotografía tamaño carné','IMAGEN',2097152);

-- Modalidades
INSERT INTO modalidad (codigo_modalidad, nombre, descripcion, requiere_colegio, requiere_universidad) VALUES
('ORD','Ordinario','Admisión regular para egresados de colegio', TRUE, FALSE),
('TRASLADO','Traslado externo','Postulantes con estudios universitarios previos', FALSE, TRUE),
('CEPRE','CEPREPUCP','Ingreso vía centro preuniversitario', TRUE, FALSE);

-- Estados de postulación
INSERT INTO estado_postulacion (codigo, nombre, descripcion) VALUES
('REGISTRADA','Registrada','Postulación creada, pendiente de envío'),
('ENVIADA','Enviada','Documentación enviada para evaluación'),
('OBSERVADA','Observada','Se encontraron observaciones en los documentos'),
('APROBADA','Aprobada','Postulación validada y aprobada'),
('FINALIZADA','Finalizada','Proceso de postulación concluido'),
('BORRADOR','Borrador','Postulación en edición'),
('COMPLETADA','Completada','Lista para enviar'),
('EN_PROCESO','En Proceso','Bajo evaluación');

-- Instituciones educativas
INSERT INTO institucion_educativa (pais_id, codigo_externo, nombre, tipo_institucion) VALUES
(1,'COL001','Colegio San José',       'COLEGIO'),
(1,'UNI001','Universidad Nacional Mayor de San Marcos','UNIVERSIDAD'),
(2,'COL002','Colegio Andino','COLEGIO');

-- Personas (base de la jerarquía)
INSERT INTO persona (nombres, apellido_paterno, apellido_materno, correo, tipo_documento, numero_documento, telefono) VALUES
('Ana Lucía','Torres','Vega','ana.torres@example.com','DNI','71234567','987654321'),      -- id 1 -> Postulante
('Carlos',   'Ramírez','Soto','carlos.ramirez@example.com','DNI','45678912','912345678'), -- id 2 -> Postulante
('María',    'Gómez','Paredes','maria.gomez@example.com','DNI','40001122','999888777'),   -- id 3 -> Apoderado
('Jorge',    'Fernández','Luna','jorge.fernandez@example.com','CE','X1234567','988776655'),-- id 4 -> Apoderado
('Rosa',     'Delgado','Chávez','rosa.delgado@pucp.edu.pe','DNI','30002211','955443322'),  -- id 5 -> Evaluador
('Luis',     'Quispe','Mamani','luis.quispe@pucp.edu.pe','DNI','30003344','955112233');    -- id 6 -> Evaluador

-- Apoderados
INSERT INTO apoderado (persona_id, parentesco) VALUES
(3,'MADRE'),
(4,'PADRE');

-- Postulantes
INSERT INTO postulante (persona_id, apoderado_id, fecha_nacimiento, tiene_discapacidad, numero_carnet_conadis, correo_validado, fecha_validacion_correo) VALUES
(1, 3, '2008-05-10', FALSE, NULL, TRUE, '2026-01-10'),
(2, 4, '2007-11-22', FALSE, NULL, FALSE, NULL);

-- Evaluadores
INSERT INTO evaluador (persona_id, cargo) VALUES
(5,'Evaluador de Expedientes'),
(6,'Coordinador de Admisión');

-- Antecedentes académicos
INSERT INTO antecedente_academico (postulante_id, institucion_id, anio_inicio, anio_fin, descripcion) VALUES
(1, 1, 2019, 2024, 'Educación secundaria completa'),
(2, 3, 2018, 2023, 'Educación secundaria completa');

-- Convocatoria
INSERT INTO convocatoria (codigo_convocatoria, nombre, periodo, fecha_inicio, fecha_fin, estado, descripcion) VALUES
('CONV-2026-1','Admisión 2026-I','2026-1','2026-01-05','2026-03-10','PUBLICADA','Proceso de admisión del primer semestre 2026');

-- Relación Convocatoria <-> Postulante
INSERT INTO convocatoria_postulante (convocatoria_id, postulante_id) VALUES
(1,1),
(1,2);

-- Ofertas de carrera
INSERT INTO oferta_carrera (convocatoria_id, carrera_id, cantidad_vacantes) VALUES
(1,1,60),
(1,2,40),
(1,3,30);

-- Convocatoria-Modalidad
INSERT INTO convocatoria_modalidad (convocatoria_id, modalidad_id, costo_inscripcion, observacion) VALUES
(1,1,350.00,'Modalidad ordinaria estándar'),
(1,2,400.00,'Requiere documentación universitaria previa');

-- Requisitos por Convocatoria-Modalidad
INSERT INTO requisito_convocatoria_modalidad (convocatoria_modalidad_id, requisito_id, obligatorio, orden_presentacion) VALUES
(1,1,TRUE,1),
(1,2,TRUE,2),
(1,3,FALSE,3),
(2,1,TRUE,1),
(2,2,TRUE,2);

-- Convocatoria-Etapa
INSERT INTO convocatoria_etapa (convocatoria_id, etapa_id, fecha_inicio, fecha_fin) VALUES
(1,1,'2026-01-05','2026-02-15'),
(1,2,'2026-02-16','2026-02-28'),
(1,3,'2026-03-05','2026-03-05');

-- Postulaciones
INSERT INTO postulacion (postulante_id, convocatoria_id, modalidad_elegida_id, carrera_elegida_id, estado_actual_id, fecha_registro, fecha_envio, fecha_finalizacion, codigo_inscripcion, observacion_general) VALUES
(1,1,1,1,2,'2026-01-10','2026-01-12',NULL,'INS-2026-0001','Documentación completa, en evaluación'),
(2,1,2,2,1,'2026-01-15',NULL,NULL,'INS-2026-0002',NULL);

-- Historial de postulación
INSERT INTO postulacion_historial (postulacion_id, estado_anterior_id, estado_actual_id, fecha_cambio, responsable_cambio, motivo_cambio) VALUES
(1, 1, 2, '2026-01-12', 'Sistema', 'Envío de documentación completada'),
(2, NULL, 1, '2026-01-15', 'Sistema', 'Registro inicial de la postulación');

-- Pagos
INSERT INTO pago (postulacion_id, medio_pago, monto, fecha_generacion, fecha_pago, codigo_pago, referencia_pasarela, estado_pago, ruta_voucher) VALUES
(1,'TARJETA',350.00,'2026-01-10','2026-01-11','PAG-0001','REF-VISA-9911','APROBADO','/vouchers/pag0001.pdf'),
(2,'YAPE',400.00,'2026-01-15',NULL,'PAG-0002',NULL,'PENDIENTE',NULL);

-- Documentos de postulación
INSERT INTO documento_postulacion (postulacion_id, requisito_aplicable_id, numero_version, nombre_archivo, tipo_archivo, tamanio_archivo, ruta_archivo, fecha_carga, estado_documento, fecha_evaluacion, comentario_evaluacion) VALUES
(1,1,1,'dni_ana_torres.jpg','IMAGEN',1048576,'/docs/1/dni_ana_torres.jpg','2026-01-11','APROBADO','2026-02-01','Documento legible y vigente'),
(1,2,1,'certificado_ana_torres.pdf','PDF',3145728,'/docs/1/certificado_ana_torres.pdf','2026-01-11','OBSERVADO','2026-02-01','Falta sello de la institución'),
(2,1,1,'dni_carlos_ramirez.jpg','IMAGEN',987654,'/docs/2/dni_carlos_ramirez.jpg','2026-01-15','PENDIENTE',NULL,NULL);

-- Observaciones de documentos
INSERT INTO documento_observacion (documento_id, evaluador_id, tipo_observacion, descripcion, fecha_observacion, estado_observacion, fecha_subsanacion, comentario_subsanacion) VALUES
(2, 5, 'ILEGIBLE', 'El sello de la institución no es visible', '2026-02-01', 'PENDIENTE', NULL, NULL);

-- Carné de postulante
INSERT INTO carne_postulante (postulacion_id, sede_id, codigo_carne, fecha_generacion, fecha_inicio_vigencia, fecha_fin_vigencia, aula_examen) VALUES
(1, 1, 'CARNE-0001', '2026-02-20', '2026-02-20', '2026-03-05', 'Aula 205 - Pabellón A');

-- Notificaciones
INSERT INTO notificacion (postulacion_id, observacion_origen_id, medio_notificacion, tipo_notificacion, destinatario, asunto, mensaje, fecha_programada, fecha_envio, estado_envio, leida, fecha_lectura) VALUES
(1, NULL, 'CORREO', 'PAGO', 'ana.torres@example.com', 'Confirmación de pago recibido', 'Tu pago de inscripción ha sido registrado correctamente.', '2026-01-11', '2026-01-11', 'ENVIADA', TRUE, '2026-01-11'),
(1, 1, 'CORREO', 'OBSERVACION', 'ana.torres@example.com', 'Documento observado', 'Tu certificado de estudios presenta una observación, por favor revísala.', '2026-02-01', '2026-02-01', 'ENVIADA', FALSE, NULL),
(2, NULL, 'BANDEJA_SISTEMA', 'VALIDACION', 'carlos.ramirez@example.com', 'Postulación registrada', 'Tu postulación fue registrada exitosamente.', '2026-01-15', NULL, 'PENDIENTE', FALSE, NULL);

-- =====================================================
-- ADMITU LAB06 - PROCEDIMIENTOS ALMACENADOS (Callable)
-- 6 entidades demo: FACULTAD, CARRERA, CONVOCATORIA,
-- POSTULANTE (persona+postulante), POSTULACION, PAGO
-- =====================================================

-- Limpieza previa de procedimientos
DROP PROCEDURE IF EXISTS INSERTAR_FACULTAD;
DROP PROCEDURE IF EXISTS MODIFICAR_FACULTAD;
DROP PROCEDURE IF EXISTS ELIMINAR_FACULTAD;
DROP PROCEDURE IF EXISTS LISTAR_FACULTAD_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_FACULTADES_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_CARRERA;
DROP PROCEDURE IF EXISTS MODIFICAR_CARRERA;
DROP PROCEDURE IF EXISTS ELIMINAR_CARRERA;
DROP PROCEDURE IF EXISTS LISTAR_CARRERA_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_CARRERAS_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_CONVOCATORIA;
DROP PROCEDURE IF EXISTS MODIFICAR_CONVOCATORIA;
DROP PROCEDURE IF EXISTS ELIMINAR_CONVOCATORIA;
DROP PROCEDURE IF EXISTS LISTAR_CONVOCATORIA_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_CONVOCATORIAS_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_POSTULANTE;
DROP PROCEDURE IF EXISTS MODIFICAR_POSTULANTE;
DROP PROCEDURE IF EXISTS ELIMINAR_POSTULANTE;
DROP PROCEDURE IF EXISTS LISTAR_POSTULANTE_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_POSTULANTES_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_POSTULACION;
DROP PROCEDURE IF EXISTS MODIFICAR_POSTULACION;
DROP PROCEDURE IF EXISTS ELIMINAR_POSTULACION;
DROP PROCEDURE IF EXISTS LISTAR_POSTULACION_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_POSTULACIONES_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_PAGO;
DROP PROCEDURE IF EXISTS MODIFICAR_PAGO;
DROP PROCEDURE IF EXISTS ELIMINAR_PAGO;
DROP PROCEDURE IF EXISTS LISTAR_PAGO_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_PAGOS_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_PAIS;
DROP PROCEDURE IF EXISTS MODIFICAR_PAIS;
DROP PROCEDURE IF EXISTS ELIMINAR_PAIS;
DROP PROCEDURE IF EXISTS LISTAR_PAIS_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_PAISES_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_SEDE;
DROP PROCEDURE IF EXISTS MODIFICAR_SEDE;
DROP PROCEDURE IF EXISTS ELIMINAR_SEDE;
DROP PROCEDURE IF EXISTS LISTAR_SEDE_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_SEDES_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_ETAPA;
DROP PROCEDURE IF EXISTS MODIFICAR_ETAPA;
DROP PROCEDURE IF EXISTS ELIMINAR_ETAPA;
DROP PROCEDURE IF EXISTS LISTAR_ETAPA_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_ETAPAS_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_REQUISITO;
DROP PROCEDURE IF EXISTS MODIFICAR_REQUISITO;
DROP PROCEDURE IF EXISTS ELIMINAR_REQUISITO;
DROP PROCEDURE IF EXISTS LISTAR_REQUISITO_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_REQUISITOS_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_MODALIDAD;
DROP PROCEDURE IF EXISTS MODIFICAR_MODALIDAD;
DROP PROCEDURE IF EXISTS ELIMINAR_MODALIDAD;
DROP PROCEDURE IF EXISTS LISTAR_MODALIDAD_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_MODALIDADES_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_ESTADO_POSTULACION;
DROP PROCEDURE IF EXISTS MODIFICAR_ESTADO_POSTULACION;
DROP PROCEDURE IF EXISTS ELIMINAR_ESTADO_POSTULACION;
DROP PROCEDURE IF EXISTS LISTAR_ESTADO_POSTULACION_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_ESTADOS_POSTULACION_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_INSTITUCION_EDUCATIVA;
DROP PROCEDURE IF EXISTS MODIFICAR_INSTITUCION_EDUCATIVA;
DROP PROCEDURE IF EXISTS ELIMINAR_INSTITUCION_EDUCATIVA;
DROP PROCEDURE IF EXISTS LISTAR_INSTITUCION_EDUCATIVA_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_INSTITUCIONES_EDUCATIVAS_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_APODERADO;
DROP PROCEDURE IF EXISTS MODIFICAR_APODERADO;
DROP PROCEDURE IF EXISTS ELIMINAR_APODERADO;
DROP PROCEDURE IF EXISTS LISTAR_APODERADO_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_APODERADOS_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_EVALUADOR;
DROP PROCEDURE IF EXISTS MODIFICAR_EVALUADOR;
DROP PROCEDURE IF EXISTS ELIMINAR_EVALUADOR;
DROP PROCEDURE IF EXISTS LISTAR_EVALUADOR_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_EVALUADORES_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_ANTECEDENTE_ACADEMICO;
DROP PROCEDURE IF EXISTS MODIFICAR_ANTECEDENTE_ACADEMICO;
DROP PROCEDURE IF EXISTS ELIMINAR_ANTECEDENTE_ACADEMICO;
DROP PROCEDURE IF EXISTS LISTAR_ANTECEDENTE_ACADEMICO_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_ANTECEDENTES_ACADEMICOS_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_OFERTA_CARRERA;
DROP PROCEDURE IF EXISTS MODIFICAR_OFERTA_CARRERA;
DROP PROCEDURE IF EXISTS ELIMINAR_OFERTA_CARRERA;
DROP PROCEDURE IF EXISTS LISTAR_OFERTA_CARRERA_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_OFERTAS_CARRERA_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_CONVOCATORIA_ETAPA;
DROP PROCEDURE IF EXISTS MODIFICAR_CONVOCATORIA_ETAPA;
DROP PROCEDURE IF EXISTS ELIMINAR_CONVOCATORIA_ETAPA;
DROP PROCEDURE IF EXISTS LISTAR_CONVOCATORIA_ETAPA_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_CONVOCATORIAS_ETAPA_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_CONVOCATORIA_MODALIDAD;
DROP PROCEDURE IF EXISTS MODIFICAR_CONVOCATORIA_MODALIDAD;
DROP PROCEDURE IF EXISTS ELIMINAR_CONVOCATORIA_MODALIDAD;
DROP PROCEDURE IF EXISTS LISTAR_CONVOCATORIA_MODALIDAD_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_CONVOCATORIAS_MODALIDAD_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_REQUISITO_CONVOCATORIA_MODALIDAD;
DROP PROCEDURE IF EXISTS MODIFICAR_REQUISITO_CONVOCATORIA_MODALIDAD;
DROP PROCEDURE IF EXISTS ELIMINAR_REQUISITO_CONVOCATORIA_MODALIDAD;
DROP PROCEDURE IF EXISTS LISTAR_REQUISITO_CONVOCATORIA_MODALIDAD_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_REQUISITO_CONVOCATORIA_MODALIDAD_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_POSTULACION_HISTORIAL;
DROP PROCEDURE IF EXISTS MODIFICAR_POSTULACION_HISTORIAL;
DROP PROCEDURE IF EXISTS ELIMINAR_POSTULACION_HISTORIAL;
DROP PROCEDURE IF EXISTS LISTAR_POSTULACION_HISTORIAL_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_POSTULACIONES_HISTORIAL_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_DOCUMENTO_POSTULACION;
DROP PROCEDURE IF EXISTS MODIFICAR_DOCUMENTO_POSTULACION;
DROP PROCEDURE IF EXISTS ELIMINAR_DOCUMENTO_POSTULACION;
DROP PROCEDURE IF EXISTS LISTAR_DOCUMENTO_POSTULACION_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_DOCUMENTOS_POSTULACION_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_DOCUMENTO_OBSERVACION;
DROP PROCEDURE IF EXISTS MODIFICAR_DOCUMENTO_OBSERVACION;
DROP PROCEDURE IF EXISTS ELIMINAR_DOCUMENTO_OBSERVACION;
DROP PROCEDURE IF EXISTS LISTAR_DOCUMENTO_OBSERVACION_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_DOCUMENTOS_OBSERVACION_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_CARNE_POSTULANTE;
DROP PROCEDURE IF EXISTS MODIFICAR_CARNE_POSTULANTE;
DROP PROCEDURE IF EXISTS ELIMINAR_CARNE_POSTULANTE;
DROP PROCEDURE IF EXISTS LISTAR_CARNE_POSTULANTE_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_CARNES_POSTULANTE_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_NOTIFICACION;
DROP PROCEDURE IF EXISTS MODIFICAR_NOTIFICACION;
DROP PROCEDURE IF EXISTS ELIMINAR_NOTIFICACION;
DROP PROCEDURE IF EXISTS LISTAR_NOTIFICACION_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_NOTIFICACIONES_TODAS;

DELIMITER $

-- ---------- FACULTAD ----------
CREATE PROCEDURE INSERTAR_FACULTAD(
    OUT _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(150)
)
BEGIN
    INSERT INTO facultad(codigo, nombre, activo) VALUES(_codigo, _nombre, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_FACULTAD(
    IN _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(150)
)
BEGIN
    UPDATE facultad SET codigo = _codigo, nombre = _nombre WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_FACULTAD(
    IN _id INT
)
BEGIN
    UPDATE facultad SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_FACULTAD_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo, nombre, activo FROM facultad WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_FACULTADES_TODAS()
BEGIN
    SELECT id, codigo, nombre, activo FROM facultad WHERE activo = 1;
END$

-- ---------- CARRERA ----------
CREATE PROCEDURE INSERTAR_CARRERA(
    OUT _id INT,
    IN _facultad_id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(150)
)
BEGIN
    INSERT INTO carrera(facultad_id, codigo_carrera, nombre, activo) VALUES(_facultad_id, _codigo, _nombre, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_CARRERA(
    IN _id INT,
    IN _facultad_id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(150)
)
BEGIN
    UPDATE carrera SET facultad_id = _facultad_id, codigo_carrera = _codigo, nombre = _nombre WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_CARRERA(
    IN _id INT
)
BEGIN
    UPDATE carrera SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_CARRERA_X_ID(
    IN _id INT
)
BEGIN
    SELECT c.id, c.facultad_id, f.nombre AS nombre_facultad, c.codigo_carrera, c.nombre, c.activo
    FROM carrera c INNER JOIN facultad f ON c.facultad_id = f.id WHERE c.id = _id;
END$

CREATE PROCEDURE LISTAR_CARRERAS_TODAS()
BEGIN
    SELECT c.id, c.facultad_id, f.nombre AS nombre_facultad, c.codigo_carrera, c.nombre, c.activo
    FROM carrera c INNER JOIN facultad f ON c.facultad_id = f.id WHERE c.activo = 1;
END$

-- ---------- CONVOCATORIA ----------
CREATE PROCEDURE INSERTAR_CONVOCATORIA(
    OUT _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(150),
    IN _periodo VARCHAR(20),
    IN _fecha_inicio DATE,
    IN _fecha_fin DATE,
    IN _estado VARCHAR(20),
    IN _descripcion VARCHAR(255)
)
BEGIN
    INSERT INTO convocatoria(codigo_convocatoria, nombre, periodo, fecha_inicio, fecha_fin, estado, descripcion, activo)
    VALUES(_codigo, _nombre, _periodo, _fecha_inicio, _fecha_fin, _estado, _descripcion, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_CONVOCATORIA(
    IN _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(150),
    IN _periodo VARCHAR(20),
    IN _fecha_inicio DATE,
    IN _fecha_fin DATE,
    IN _estado VARCHAR(20),
    IN _descripcion VARCHAR(255)
)
BEGIN
    UPDATE convocatoria SET codigo_convocatoria = _codigo, nombre = _nombre, periodo = _periodo,
        fecha_inicio = _fecha_inicio, fecha_fin = _fecha_fin, estado = _estado, descripcion = _descripcion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_CONVOCATORIA(
    IN _id INT
)
BEGIN
    UPDATE convocatoria SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_CONVOCATORIA_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo_convocatoria, nombre, periodo, fecha_inicio, fecha_fin, estado, descripcion, activo
    FROM convocatoria WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_CONVOCATORIAS_TODAS()
BEGIN
    SELECT id, codigo_convocatoria, nombre, periodo, fecha_inicio, fecha_fin, estado, descripcion, activo
    FROM convocatoria WHERE activo = 1;
END$

-- ---------- POSTULANTE (persona + postulante) ----------
CREATE PROCEDURE INSERTAR_POSTULANTE(
    OUT _id INT,
    IN _nombres VARCHAR(100),
    IN _ape_paterno VARCHAR(100),
    IN _ape_materno VARCHAR(100),
    IN _correo VARCHAR(150),
    IN _tipo_doc VARCHAR(10),
    IN _num_doc VARCHAR(20),
    IN _telefono VARCHAR(20),
    IN _fecha_nac DATE
)
BEGIN
    INSERT INTO persona(nombres, apellido_paterno, apellido_materno, correo, tipo_documento, numero_documento, telefono)
    VALUES(_nombres, _ape_paterno, _ape_materno, _correo, _tipo_doc, _num_doc, _telefono);
    SET _id = LAST_INSERT_ID();
    INSERT INTO postulante(persona_id, apoderado_id, fecha_nacimiento, tiene_discapacidad, numero_carnet_conadis, correo_validado, fecha_validacion_correo)
    VALUES(_id, NULL, _fecha_nac, 0, NULL, 0, NULL);
END$

CREATE PROCEDURE MODIFICAR_POSTULANTE(
    IN _id INT,
    IN _nombres VARCHAR(100),
    IN _ape_paterno VARCHAR(100),
    IN _ape_materno VARCHAR(100),
    IN _correo VARCHAR(150),
    IN _telefono VARCHAR(20),
    IN _fecha_nac DATE
)
BEGIN
    UPDATE persona SET nombres = _nombres, apellido_paterno = _ape_paterno, apellido_materno = _ape_materno,
        correo = _correo, telefono = _telefono WHERE id = _id;
    UPDATE postulante SET fecha_nacimiento = _fecha_nac WHERE persona_id = _id;
END$

CREATE PROCEDURE ELIMINAR_POSTULANTE(
    IN _id INT
)
BEGIN
    DELETE FROM postulacion WHERE postulante_id = _id;
    DELETE FROM persona WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_POSTULANTE_X_ID(
    IN _id INT
)
BEGIN
    SELECT p.id, p.nombres, p.apellido_paterno, p.apellido_materno, p.correo, p.tipo_documento,
        p.numero_documento, p.telefono, po.fecha_nacimiento
    FROM postulante po INNER JOIN persona p ON po.persona_id = p.id WHERE po.persona_id = _id;
END$

CREATE PROCEDURE LISTAR_POSTULANTES_TODOS()
BEGIN
    SELECT p.id, p.nombres, p.apellido_paterno, p.apellido_materno, p.correo, p.tipo_documento,
        p.numero_documento, p.telefono, po.fecha_nacimiento
    FROM postulante po INNER JOIN persona p ON po.persona_id = p.id;
END$

-- ---------- POSTULACION ----------
CREATE PROCEDURE INSERTAR_POSTULACION(
    OUT _id INT,
    IN _postulante_id INT,
    IN _convocatoria_id INT,
    IN _modalidad_id INT,
    IN _oferta_id INT,
    IN _estado_id INT,
    IN _codigo VARCHAR(30),
    IN _observacion VARCHAR(255)
)
BEGIN
    INSERT INTO postulacion(postulante_id, convocatoria_id, modalidad_elegida_id, carrera_elegida_id,
        estado_actual_id, fecha_registro, codigo_inscripcion, observacion_general)
    VALUES(_postulante_id, _convocatoria_id, _modalidad_id, _oferta_id, _estado_id, CURDATE(), _codigo, _observacion);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_POSTULACION(
    IN _id INT,
    IN _estado_id INT,
    IN _observacion VARCHAR(255)
)
BEGIN
    UPDATE postulacion SET estado_actual_id = _estado_id, observacion_general = _observacion WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_POSTULACION(
    IN _id INT
)
BEGIN
    DELETE FROM notificacion WHERE postulacion_id = _id;
    DELETE FROM documento_observacion WHERE documento_id IN (
        SELECT id FROM documento_postulacion WHERE postulacion_id = _id
    );
    DELETE FROM documento_postulacion WHERE postulacion_id = _id;
    DELETE FROM carne_postulante WHERE postulacion_id = _id;
    DELETE FROM pago WHERE postulacion_id = _id;
    DELETE FROM postulacion_historial WHERE postulacion_id = _id;
    DELETE FROM postulacion WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_POSTULACION_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, postulante_id, convocatoria_id, modalidad_elegida_id, carrera_elegida_id,
        estado_actual_id, fecha_registro, codigo_inscripcion, observacion_general
    FROM postulacion WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_POSTULACIONES_TODAS()
BEGIN
    SELECT id, postulante_id, convocatoria_id, modalidad_elegida_id, carrera_elegida_id,
        estado_actual_id, fecha_registro, codigo_inscripcion, observacion_general
    FROM postulacion;
END$

-- ---------- PAGO ----------
CREATE PROCEDURE INSERTAR_PAGO(
    OUT _id INT,
    IN _postulacion_id INT,
    IN _medio VARCHAR(20),
    IN _monto DECIMAL(10,2),
    IN _codigo VARCHAR(30),
    IN _referencia VARCHAR(100),
    IN _estado VARCHAR(20),
    IN _voucher VARCHAR(255)
)
BEGIN
    INSERT INTO pago(postulacion_id, medio_pago, monto, fecha_generacion, codigo_pago, referencia_pasarela, estado_pago, ruta_voucher)
    VALUES(_postulacion_id, _medio, _monto, CURDATE(), _codigo, _referencia, _estado, _voucher);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_PAGO(
    IN _id INT,
    IN _estado VARCHAR(20),
    IN _referencia VARCHAR(100)
)
BEGIN
    UPDATE pago SET estado_pago = _estado, referencia_pasarela = _referencia WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_PAGO(
    IN _id INT
)
BEGIN
    UPDATE pago SET estado_pago = 'RECHAZADO' WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_PAGO_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, postulacion_id, medio_pago, monto, fecha_generacion, fecha_pago, codigo_pago,
        referencia_pasarela, estado_pago, ruta_voucher FROM pago WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_PAGOS_TODOS()
BEGIN
    SELECT id, postulacion_id, medio_pago, monto, fecha_generacion, fecha_pago, codigo_pago,
        referencia_pasarela, estado_pago, ruta_voucher FROM pago;
END$

-- ---------- PAIS ----------
CREATE PROCEDURE INSERTAR_PAIS(
    OUT _id INT,
    IN _codigo_iso2 CHAR(2),
    IN _nombre VARCHAR(100)
)
BEGIN
    INSERT INTO pais(codigo_iso2, nombre, activo) VALUES(_codigo_iso2, _nombre, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_PAIS(
    IN _id INT,
    IN _codigo_iso2 CHAR(2),
    IN _nombre VARCHAR(100)
)
BEGIN
    UPDATE pais SET codigo_iso2 = _codigo_iso2, nombre = _nombre WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_PAIS(
    IN _id INT
)
BEGIN
    UPDATE pais SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_PAIS_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo_iso2, nombre, activo FROM pais WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_PAISES_TODOS()
BEGIN
    SELECT id, codigo_iso2, nombre, activo FROM pais WHERE activo = 1 ORDER BY nombre;
END$

-- ---------- SEDE ----------
CREATE PROCEDURE INSERTAR_SEDE(
    OUT _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _direccion VARCHAR(200)
)
BEGIN
    INSERT INTO sede(codigo, nombre, direccion, activo) VALUES(_codigo, _nombre, _direccion, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_SEDE(
    IN _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _direccion VARCHAR(200)
)
BEGIN
    UPDATE sede SET codigo = _codigo, nombre = _nombre, direccion = _direccion WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_SEDE(
    IN _id INT
)
BEGIN
    UPDATE sede SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_SEDE_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo, nombre, direccion, activo FROM sede WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_SEDES_TODAS()
BEGIN
    SELECT id, codigo, nombre, direccion, activo FROM sede WHERE activo = 1 ORDER BY nombre;
END$

-- ---------- ETAPA ----------
CREATE PROCEDURE INSERTAR_ETAPA(
    OUT _id INT,
    IN _fecha_inicio DATE,
    IN _fecha_fin DATE,
    IN _codigo_etapa VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _descripcion VARCHAR(255)
)
BEGIN
    INSERT INTO etapa(fecha_inicio, fecha_fin, codigo_etapa, nombre, descripcion, activo)
    VALUES(_fecha_inicio, _fecha_fin, _codigo_etapa, _nombre, _descripcion, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_ETAPA(
    IN _id INT,
    IN _fecha_inicio DATE,
    IN _fecha_fin DATE,
    IN _codigo_etapa VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _descripcion VARCHAR(255)
)
BEGIN
    UPDATE etapa SET fecha_inicio = _fecha_inicio, fecha_fin = _fecha_fin,
        codigo_etapa = _codigo_etapa, nombre = _nombre, descripcion = _descripcion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_ETAPA(
    IN _id INT
)
BEGIN
    UPDATE etapa SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_ETAPA_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, fecha_inicio, fecha_fin, codigo_etapa, nombre, descripcion, activo
    FROM etapa WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_ETAPAS_TODAS()
BEGIN
    SELECT id, fecha_inicio, fecha_fin, codigo_etapa, nombre, descripcion, activo
    FROM etapa WHERE activo = 1 ORDER BY codigo_etapa;
END$

-- ---------- REQUISITO ----------
CREATE PROCEDURE INSERTAR_REQUISITO(
    OUT _id INT,
    IN _codigo_requisito VARCHAR(20),
    IN _nombre VARCHAR(150),
    IN _descripcion VARCHAR(255),
    IN _tipo_archivo ENUM('PDF','IMAGEN','CUALQUIERA'),
    IN _tamanio_maximo INT
)
BEGIN
    INSERT INTO requisito(codigo_requisito, nombre, descripcion, tipo_archivo_requerido, tamanio_maximo_bytes, activo)
    VALUES(_codigo_requisito, _nombre, _descripcion, _tipo_archivo, _tamanio_maximo, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_REQUISITO(
    IN _id INT,
    IN _codigo_requisito VARCHAR(20),
    IN _nombre VARCHAR(150),
    IN _descripcion VARCHAR(255),
    IN _tipo_archivo ENUM('PDF','IMAGEN','CUALQUIERA'),
    IN _tamanio_maximo INT
)
BEGIN
    UPDATE requisito SET codigo_requisito = _codigo_requisito, nombre = _nombre, descripcion = _descripcion,
        tipo_archivo_requerido = _tipo_archivo, tamanio_maximo_bytes = _tamanio_maximo
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_REQUISITO(
    IN _id INT
)
BEGIN
    UPDATE requisito SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_REQUISITO_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo_requisito, nombre, descripcion, tipo_archivo_requerido, tamanio_maximo_bytes, activo
    FROM requisito WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_REQUISITOS_TODOS()
BEGIN
    SELECT id, codigo_requisito, nombre, descripcion, tipo_archivo_requerido, tamanio_maximo_bytes, activo
    FROM requisito WHERE activo = 1 ORDER BY codigo_requisito;
END$

-- ---------- MODALIDAD ----------
CREATE PROCEDURE INSERTAR_MODALIDAD(
    OUT _id INT,
    IN _codigo_modalidad VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _descripcion VARCHAR(255),
    IN _requiere_colegio BOOLEAN,
    IN _requiere_universidad BOOLEAN
)
BEGIN
    INSERT INTO modalidad(codigo_modalidad, nombre, descripcion, requiere_colegio, requiere_universidad, activo)
    VALUES(_codigo_modalidad, _nombre, _descripcion, _requiere_colegio, _requiere_universidad, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_MODALIDAD(
    IN _id INT,
    IN _codigo_modalidad VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _descripcion VARCHAR(255),
    IN _requiere_colegio BOOLEAN,
    IN _requiere_universidad BOOLEAN
)
BEGIN
    UPDATE modalidad SET codigo_modalidad = _codigo_modalidad, nombre = _nombre, descripcion = _descripcion,
        requiere_colegio = _requiere_colegio, requiere_universidad = _requiere_universidad
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_MODALIDAD(
    IN _id INT
)
BEGIN
    UPDATE modalidad SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_MODALIDAD_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo_modalidad, nombre, descripcion, requiere_colegio, requiere_universidad, activo
    FROM modalidad WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_MODALIDADES_TODAS()
BEGIN
    SELECT id, codigo_modalidad, nombre, descripcion, requiere_colegio, requiere_universidad, activo
    FROM modalidad WHERE activo = 1 ORDER BY codigo_modalidad;
END$

-- ---------- ESTADO_POSTULACION ----------
CREATE PROCEDURE INSERTAR_ESTADO_POSTULACION(
    OUT _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _descripcion VARCHAR(255)
)
BEGIN
    INSERT INTO estado_postulacion(codigo, nombre, descripcion) VALUES(_codigo, _nombre, _descripcion);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_ESTADO_POSTULACION(
    IN _id INT,
    IN _codigo VARCHAR(20),
    IN _nombre VARCHAR(100),
    IN _descripcion VARCHAR(255)
)
BEGIN
    UPDATE estado_postulacion SET codigo = _codigo, nombre = _nombre, descripcion = _descripcion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_ESTADO_POSTULACION(
    IN _id INT
)
BEGIN
    DELETE FROM estado_postulacion WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_ESTADO_POSTULACION_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo, nombre, descripcion FROM estado_postulacion WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_ESTADOS_POSTULACION_TODOS()
BEGIN
    SELECT id, codigo, nombre, descripcion FROM estado_postulacion ORDER BY id;
END$

-- ---------- INSTITUCION_EDUCATIVA ----------
CREATE PROCEDURE INSERTAR_INSTITUCION_EDUCATIVA(
    OUT _id INT,
    IN _pais_id INT,
    IN _codigo_externo VARCHAR(30),
    IN _nombre VARCHAR(150),
    IN _tipo_institucion ENUM('COLEGIO','UNIVERSIDAD')
)
BEGIN
    INSERT INTO institucion_educativa(pais_id, codigo_externo, nombre, tipo_institucion, activo)
    VALUES(_pais_id, _codigo_externo, _nombre, _tipo_institucion, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_INSTITUCION_EDUCATIVA(
    IN _id INT,
    IN _pais_id INT,
    IN _codigo_externo VARCHAR(30),
    IN _nombre VARCHAR(150),
    IN _tipo_institucion ENUM('COLEGIO','UNIVERSIDAD')
)
BEGIN
    UPDATE institucion_educativa SET pais_id = _pais_id, codigo_externo = _codigo_externo,
        nombre = _nombre, tipo_institucion = _tipo_institucion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_INSTITUCION_EDUCATIVA(
    IN _id INT
)
BEGIN
    UPDATE institucion_educativa SET activo = 0 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_INSTITUCION_EDUCATIVA_X_ID(
    IN _id INT
)
BEGIN
    SELECT i.id, i.pais_id, p.codigo_iso2 AS codigo_pais, p.nombre AS nombre_pais,
        i.codigo_externo, i.nombre, i.tipo_institucion, i.activo
    FROM institucion_educativa i
    INNER JOIN pais p ON p.id = i.pais_id
    WHERE i.id = _id;
END$

CREATE PROCEDURE LISTAR_INSTITUCIONES_EDUCATIVAS_TODAS()
BEGIN
    SELECT i.id, i.pais_id, p.codigo_iso2 AS codigo_pais, p.nombre AS nombre_pais,
        i.codigo_externo, i.nombre, i.tipo_institucion, i.activo
    FROM institucion_educativa i
    INNER JOIN pais p ON p.id = i.pais_id
    WHERE i.activo = 1
    ORDER BY i.nombre;
END$

-- ---------- APODERADO ----------
CREATE PROCEDURE INSERTAR_APODERADO(
    OUT _id INT,
    IN _nombres VARCHAR(100),
    IN _ape_paterno VARCHAR(100),
    IN _ape_materno VARCHAR(100),
    IN _correo VARCHAR(150),
    IN _tipo_doc ENUM('DNI','CE','PAS','CPP'),
    IN _num_doc VARCHAR(20),
    IN _telefono VARCHAR(20),
    IN _parentesco ENUM('PADRE','MADRE','TUTOR','OTRO')
)
BEGIN
    INSERT INTO persona(nombres, apellido_paterno, apellido_materno, correo, tipo_documento, numero_documento, telefono)
    VALUES(_nombres, _ape_paterno, _ape_materno, _correo, _tipo_doc, _num_doc, _telefono);
    SET _id = LAST_INSERT_ID();
    INSERT INTO apoderado(persona_id, parentesco, activo) VALUES(_id, _parentesco, 1);
END$

CREATE PROCEDURE MODIFICAR_APODERADO(
    IN _id INT,
    IN _nombres VARCHAR(100),
    IN _ape_paterno VARCHAR(100),
    IN _ape_materno VARCHAR(100),
    IN _correo VARCHAR(150),
    IN _num_doc VARCHAR(20),
    IN _telefono VARCHAR(20),
    IN _parentesco ENUM('PADRE','MADRE','TUTOR','OTRO')
)
BEGIN
    UPDATE persona SET nombres = _nombres, apellido_paterno = _ape_paterno, apellido_materno = _ape_materno,
        correo = _correo, numero_documento = _num_doc, telefono = _telefono
    WHERE id = _id;
    UPDATE apoderado SET parentesco = _parentesco WHERE persona_id = _id;
END$

CREATE PROCEDURE ELIMINAR_APODERADO(
    IN _id INT
)
BEGIN
    DELETE FROM postulante WHERE apoderado_id = _id;
    DELETE FROM persona WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_APODERADO_X_ID(
    IN _id INT
)
BEGIN
    SELECT pe.id, pe.nombres, pe.apellido_paterno, pe.apellido_materno, pe.correo,
        pe.tipo_documento, pe.numero_documento, pe.telefono, a.parentesco, a.activo
    FROM persona pe
    INNER JOIN apoderado a ON a.persona_id = pe.id
    WHERE pe.id = _id;
END$

CREATE PROCEDURE LISTAR_APODERADOS_TODOS()
BEGIN
    SELECT pe.id, pe.nombres, pe.apellido_paterno, pe.apellido_materno, pe.correo,
        pe.tipo_documento, pe.numero_documento, pe.telefono, a.parentesco, a.activo
    FROM persona pe
    INNER JOIN apoderado a ON a.persona_id = pe.id
    WHERE a.activo = 1
    ORDER BY pe.apellido_paterno, pe.apellido_materno;
END$

-- ---------- EVALUADOR ----------
CREATE PROCEDURE INSERTAR_EVALUADOR(
    OUT _id INT,
    IN _nombres VARCHAR(100),
    IN _ape_paterno VARCHAR(100),
    IN _ape_materno VARCHAR(100),
    IN _correo VARCHAR(150),
    IN _tipo_doc ENUM('DNI','CE','PAS','CPP'),
    IN _num_doc VARCHAR(20),
    IN _telefono VARCHAR(20),
    IN _cargo VARCHAR(100)
)
BEGIN
    INSERT INTO persona(nombres, apellido_paterno, apellido_materno, correo, tipo_documento, numero_documento, telefono)
    VALUES(_nombres, _ape_paterno, _ape_materno, _correo, _tipo_doc, _num_doc, _telefono);
    SET _id = LAST_INSERT_ID();
    INSERT INTO evaluador(persona_id, cargo, activo) VALUES(_id, _cargo, 1);
END$

CREATE PROCEDURE MODIFICAR_EVALUADOR(
    IN _id INT,
    IN _nombres VARCHAR(100),
    IN _ape_paterno VARCHAR(100),
    IN _ape_materno VARCHAR(100),
    IN _correo VARCHAR(150),
    IN _num_doc VARCHAR(20),
    IN _telefono VARCHAR(20),
    IN _cargo VARCHAR(100)
)
BEGIN
    UPDATE persona SET nombres = _nombres, apellido_paterno = _ape_paterno, apellido_materno = _ape_materno,
        correo = _correo, numero_documento = _num_doc, telefono = _telefono
    WHERE id = _id;
    UPDATE evaluador SET cargo = _cargo WHERE persona_id = _id;
END$

CREATE PROCEDURE ELIMINAR_EVALUADOR(
    IN _id INT
)
BEGIN
    DELETE FROM persona WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_EVALUADOR_X_ID(
    IN _id INT
)
BEGIN
    SELECT pe.id, pe.nombres, pe.apellido_paterno, pe.apellido_materno, pe.correo,
        pe.tipo_documento, pe.numero_documento, pe.telefono, e.cargo, e.activo
    FROM persona pe
    INNER JOIN evaluador e ON e.persona_id = pe.id
    WHERE pe.id = _id;
END$

CREATE PROCEDURE LISTAR_EVALUADORES_TODOS()
BEGIN
    SELECT pe.id, pe.nombres, pe.apellido_paterno, pe.apellido_materno, pe.correo,
        pe.tipo_documento, pe.numero_documento, pe.telefono, e.cargo, e.activo
    FROM persona pe
    INNER JOIN evaluador e ON e.persona_id = pe.id
    WHERE e.activo = 1
    ORDER BY pe.apellido_paterno, pe.apellido_materno;
END$

-- ---------- ANTECEDENTE_ACADEMICO ----------
CREATE PROCEDURE INSERTAR_ANTECEDENTE_ACADEMICO(
    OUT _id INT,
    IN _postulante_id INT,
    IN _institucion_id INT,
    IN _anio_inicio INT,
    IN _anio_fin INT,
    IN _descripcion VARCHAR(255)
)
BEGIN
    INSERT INTO antecedente_academico(postulante_id, institucion_id, anio_inicio, anio_fin, descripcion, activo)
    VALUES(_postulante_id, _institucion_id, _anio_inicio, _anio_fin, _descripcion, 1);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_ANTECEDENTE_ACADEMICO(
    IN _id INT,
    IN _postulante_id INT,
    IN _institucion_id INT,
    IN _anio_inicio INT,
    IN _anio_fin INT,
    IN _descripcion VARCHAR(255)
)
BEGIN
    UPDATE antecedente_academico SET postulante_id = _postulante_id, institucion_id = _institucion_id,
        anio_inicio = _anio_inicio, anio_fin = _anio_fin, descripcion = _descripcion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_ANTECEDENTE_ACADEMICO(
    IN _id INT
)
BEGIN
    DELETE FROM antecedente_academico WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_ANTECEDENTE_ACADEMICO_X_ID(
    IN _id INT
)
BEGIN
    SELECT a.id, a.postulante_id, a.institucion_id, a.anio_inicio, a.anio_fin, a.descripcion, a.activo,
        p.id AS pais_id, p.nombre AS nombre_pais,
        i.codigo_externo AS codigo_institucion, i.nombre AS nombre_institucion, i.tipo_institucion
    FROM antecedente_academico a
    INNER JOIN institucion_educativa i ON i.id = a.institucion_id
    INNER JOIN pais p ON p.id = i.pais_id
    WHERE a.id = _id;
END$

CREATE PROCEDURE LISTAR_ANTECEDENTES_ACADEMICOS_TODOS()
BEGIN
    SELECT a.id, a.postulante_id, a.institucion_id, a.anio_inicio, a.anio_fin, a.descripcion, a.activo,
        p.id AS pais_id, p.nombre AS nombre_pais,
        i.codigo_externo AS codigo_institucion, i.nombre AS nombre_institucion, i.tipo_institucion
    FROM antecedente_academico a
    INNER JOIN institucion_educativa i ON i.id = a.institucion_id
    INNER JOIN pais p ON p.id = i.pais_id
    WHERE a.activo = 1
    ORDER BY a.anio_fin DESC;
END$

-- ---------- OFERTA_CARRERA ----------
CREATE PROCEDURE INSERTAR_OFERTA_CARRERA(
    OUT _id INT,
    IN _convocatoria_id INT,
    IN _carrera_id INT,
    IN _cantidad_vacantes INT
)
BEGIN
    INSERT INTO oferta_carrera(convocatoria_id, carrera_id, cantidad_vacantes)
    VALUES(_convocatoria_id, _carrera_id, _cantidad_vacantes);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_OFERTA_CARRERA(
    IN _id INT,
    IN _convocatoria_id INT,
    IN _carrera_id INT,
    IN _cantidad_vacantes INT
)
BEGIN
    UPDATE oferta_carrera SET convocatoria_id = _convocatoria_id, carrera_id = _carrera_id,
        cantidad_vacantes = _cantidad_vacantes
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_OFERTA_CARRERA(
    IN _id INT
)
BEGIN
    DELETE FROM postulacion WHERE carrera_elegida_id = _id;
    DELETE FROM oferta_carrera WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_OFERTA_CARRERA_X_ID(
    IN _id INT
)
BEGIN
    SELECT o.id, o.convocatoria_id, o.carrera_id, o.cantidad_vacantes,
        c.codigo_carrera, c.nombre AS nombre_carrera, c.facultad_id, f.nombre AS nombre_facultad
    FROM oferta_carrera o
    INNER JOIN carrera c ON c.id = o.carrera_id
    INNER JOIN facultad f ON f.id = c.facultad_id
    WHERE o.id = _id;
END$

CREATE PROCEDURE LISTAR_OFERTAS_CARRERA_TODAS()
BEGIN
    SELECT o.id, o.convocatoria_id, o.carrera_id, o.cantidad_vacantes,
        c.codigo_carrera, c.nombre AS nombre_carrera, c.facultad_id, f.nombre AS nombre_facultad
    FROM oferta_carrera o
    INNER JOIN carrera c ON c.id = o.carrera_id
    INNER JOIN facultad f ON f.id = c.facultad_id
    ORDER BY c.nombre;
END$

-- ---------- CONVOCATORIA_ETAPA ----------
CREATE PROCEDURE INSERTAR_CONVOCATORIA_ETAPA(
    OUT _id INT,
    IN _convocatoria_id INT,
    IN _etapa_id INT,
    IN _fecha_inicio DATE,
    IN _fecha_fin DATE
)
BEGIN
    INSERT INTO convocatoria_etapa(convocatoria_id, etapa_id, fecha_inicio, fecha_fin)
    VALUES(_convocatoria_id, _etapa_id, _fecha_inicio, _fecha_fin);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_CONVOCATORIA_ETAPA(
    IN _id INT,
    IN _convocatoria_id INT,
    IN _etapa_id INT,
    IN _fecha_inicio DATE,
    IN _fecha_fin DATE
)
BEGIN
    UPDATE convocatoria_etapa SET convocatoria_id = _convocatoria_id, etapa_id = _etapa_id,
        fecha_inicio = _fecha_inicio, fecha_fin = _fecha_fin
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_CONVOCATORIA_ETAPA(
    IN _id INT
)
BEGIN
    DELETE FROM convocatoria_etapa WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_CONVOCATORIA_ETAPA_X_ID(
    IN _id INT
)
BEGIN
    SELECT ce.id, ce.convocatoria_id, ce.etapa_id, ce.fecha_inicio, ce.fecha_fin,
        e.fecha_inicio AS fecha_etapa_inicio, e.fecha_fin AS fecha_etapa_fin,
        e.codigo_etapa, e.nombre AS nombre_etapa, e.descripcion AS descripcion_etapa
    FROM convocatoria_etapa ce
    INNER JOIN etapa e ON e.id = ce.etapa_id
    WHERE ce.id = _id;
END$

CREATE PROCEDURE LISTAR_CONVOCATORIAS_ETAPA_TODAS()
BEGIN
    SELECT ce.id, ce.convocatoria_id, ce.etapa_id, ce.fecha_inicio, ce.fecha_fin,
        e.fecha_inicio AS fecha_etapa_inicio, e.fecha_fin AS fecha_etapa_fin,
        e.codigo_etapa, e.nombre AS nombre_etapa, e.descripcion AS descripcion_etapa
    FROM convocatoria_etapa ce
    INNER JOIN etapa e ON e.id = ce.etapa_id
    ORDER BY e.codigo_etapa;
END$

-- ---------- CONVOCATORIA_MODALIDAD ----------
CREATE PROCEDURE INSERTAR_CONVOCATORIA_MODALIDAD(
    OUT _id INT,
    IN _convocatoria_id INT,
    IN _modalidad_id INT,
    IN _costo_inscripcion DECIMAL(10,2),
    IN _observacion VARCHAR(255)
)
BEGIN
    INSERT INTO convocatoria_modalidad(convocatoria_id, modalidad_id, costo_inscripcion, observacion)
    VALUES(_convocatoria_id, _modalidad_id, _costo_inscripcion, _observacion);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_CONVOCATORIA_MODALIDAD(
    IN _id INT,
    IN _convocatoria_id INT,
    IN _modalidad_id INT,
    IN _costo_inscripcion DECIMAL(10,2),
    IN _observacion VARCHAR(255)
)
BEGIN
    UPDATE convocatoria_modalidad SET convocatoria_id = _convocatoria_id, modalidad_id = _modalidad_id,
        costo_inscripcion = _costo_inscripcion, observacion = _observacion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_CONVOCATORIA_MODALIDAD(
    IN _id INT
)
BEGIN
    DELETE FROM postulacion WHERE modalidad_elegida_id = _id;
    DELETE FROM convocatoria_modalidad WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_CONVOCATORIA_MODALIDAD_X_ID(
    IN _id INT
)
BEGIN
    SELECT cm.id, cm.convocatoria_id, cm.modalidad_id, cm.costo_inscripcion, cm.observacion,
        m.codigo_modalidad, m.nombre AS nombre_modalidad, m.descripcion AS descripcion_modalidad,
        m.requiere_colegio, m.requiere_universidad
    FROM convocatoria_modalidad cm
    INNER JOIN modalidad m ON m.id = cm.modalidad_id
    WHERE cm.id = _id;
END$

CREATE PROCEDURE LISTAR_CONVOCATORIAS_MODALIDAD_TODAS()
BEGIN
    SELECT cm.id, cm.convocatoria_id, cm.modalidad_id, cm.costo_inscripcion, cm.observacion,
        m.codigo_modalidad, m.nombre AS nombre_modalidad, m.descripcion AS descripcion_modalidad,
        m.requiere_colegio, m.requiere_universidad
    FROM convocatoria_modalidad cm
    INNER JOIN modalidad m ON m.id = cm.modalidad_id
    ORDER BY m.codigo_modalidad;
END$

-- ---------- REQUISITO_CONVOCATORIA_MODALIDAD ----------
CREATE PROCEDURE INSERTAR_REQUISITO_CONVOCATORIA_MODALIDAD(
    OUT _id INT,
    IN _convocatoria_modalidad_id INT,
    IN _requisito_id INT,
    IN _obligatorio BOOLEAN,
    IN _orden_presentacion INT
)
BEGIN
    INSERT INTO requisito_convocatoria_modalidad(convocatoria_modalidad_id, requisito_id, obligatorio, orden_presentacion)
    VALUES(_convocatoria_modalidad_id, _requisito_id, _obligatorio, _orden_presentacion);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_REQUISITO_CONVOCATORIA_MODALIDAD(
    IN _id INT,
    IN _convocatoria_modalidad_id INT,
    IN _requisito_id INT,
    IN _obligatorio BOOLEAN,
    IN _orden_presentacion INT
)
BEGIN
    UPDATE requisito_convocatoria_modalidad SET convocatoria_modalidad_id = _convocatoria_modalidad_id,
        requisito_id = _requisito_id, obligatorio = _obligatorio, orden_presentacion = _orden_presentacion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_REQUISITO_CONVOCATORIA_MODALIDAD(
    IN _id INT
)
BEGIN
    DELETE FROM requisito_convocatoria_modalidad WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_REQUISITO_CONVOCATORIA_MODALIDAD_X_ID(
    IN _id INT
)
BEGIN
    SELECT rc.id, rc.convocatoria_modalidad_id, rc.requisito_id, rc.obligatorio, rc.orden_presentacion,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM requisito_convocatoria_modalidad rc
    INNER JOIN requisito r ON r.id = rc.requisito_id
    WHERE rc.id = _id;
END$

CREATE PROCEDURE LISTAR_REQUISITOS_CONVOCATORIA_MODALIDAD_TODOS()
BEGIN
    SELECT rc.id, rc.convocatoria_modalidad_id, rc.requisito_id, rc.obligatorio, rc.orden_presentacion,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM requisito_convocatoria_modalidad rc
    INNER JOIN requisito r ON r.id = rc.requisito_id
    ORDER BY rc.convocatoria_modalidad_id, rc.orden_presentacion;
END$

-- ---------- POSTULACION_HISTORIAL ----------
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
    INSERT INTO postulacion_historial(postulacion_id, estado_anterior_id, estado_actual_id, fecha_cambio, responsable_cambio, motivo_cambio)
    VALUES(_postulacion_id, _estado_anterior_id, _estado_actual_id, _fecha_cambio, _responsable_cambio, _motivo_cambio);
    SET _id = LAST_INSERT_ID();
END$

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
    UPDATE postulacion_historial SET postulacion_id = _postulacion_id, estado_anterior_id = _estado_anterior_id,
        estado_actual_id = _estado_actual_id, fecha_cambio = _fecha_cambio,
        responsable_cambio = _responsable_cambio, motivo_cambio = _motivo_cambio
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_POSTULACION_HISTORIAL(
    IN _id INT
)
BEGIN
    DELETE FROM postulacion_historial WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_POSTULACION_HISTORIAL_X_ID(
    IN _id INT
)
BEGIN
    SELECT h.id, h.postulacion_id, h.estado_anterior_id, h.estado_actual_id, h.fecha_cambio,
        h.responsable_cambio, h.motivo_cambio,
        ea.codigo AS codigo_estado_anterior, ea.nombre AS nombre_estado_anterior,
        ec.codigo AS codigo_estado_actual, ec.nombre AS nombre_estado_actual
    FROM postulacion_historial h
    LEFT JOIN estado_postulacion ea ON ea.id = h.estado_anterior_id
    INNER JOIN estado_postulacion ec ON ec.id = h.estado_actual_id
    WHERE h.id = _id;
END$

CREATE PROCEDURE LISTAR_POSTULACIONES_HISTORIAL_TODAS()
BEGIN
    SELECT h.id, h.postulacion_id, h.estado_anterior_id, h.estado_actual_id, h.fecha_cambio,
        h.responsable_cambio, h.motivo_cambio,
        ea.codigo AS codigo_estado_anterior, ea.nombre AS nombre_estado_anterior,
        ec.codigo AS codigo_estado_actual, ec.nombre AS nombre_estado_actual
    FROM postulacion_historial h
    LEFT JOIN estado_postulacion ea ON ea.id = h.estado_anterior_id
    INNER JOIN estado_postulacion ec ON ec.id = h.estado_actual_id
    ORDER BY h.fecha_cambio DESC;
END$

-- ---------- DOCUMENTO_POSTULACION ----------
CREATE PROCEDURE INSERTAR_DOCUMENTO_POSTULACION(
    OUT _id INT,
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
    INSERT INTO documento_postulacion(postulacion_id, requisito_aplicable_id, numero_version, nombre_archivo,
        tipo_archivo, tamanio_archivo, ruta_archivo, fecha_carga, estado_documento, fecha_evaluacion, comentario_evaluacion)
    VALUES(_postulacion_id, _requisito_id, _numero_version, _nombre_archivo, _tipo_archivo, _tamanio_archivo,
        _ruta_archivo, _fecha_carga, _estado_documento, _fecha_evaluacion, _comentario_evaluacion);
    SET _id = LAST_INSERT_ID();
END$

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
    UPDATE documento_postulacion SET postulacion_id = _postulacion_id, requisito_aplicable_id = _requisito_id,
        numero_version = _numero_version, nombre_archivo = _nombre_archivo, tipo_archivo = _tipo_archivo,
        tamanio_archivo = _tamanio_archivo, ruta_archivo = _ruta_archivo, fecha_carga = _fecha_carga,
        estado_documento = _estado_documento, fecha_evaluacion = _fecha_evaluacion,
        comentario_evaluacion = _comentario_evaluacion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_DOCUMENTO_POSTULACION(
    IN _id INT
)
BEGIN
    DELETE FROM notificacion WHERE observacion_origen_id IN (
        SELECT id FROM documento_observacion WHERE documento_id = _id
    );
    DELETE FROM documento_postulacion WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_DOCUMENTO_POSTULACION_X_ID(
    IN _id INT
)
BEGIN
    SELECT d.id, d.postulacion_id, d.requisito_aplicable_id AS requisito_id, d.numero_version,
        d.nombre_archivo, d.tipo_archivo, d.tamanio_archivo, d.ruta_archivo, d.fecha_carga,
        d.estado_documento, d.fecha_evaluacion, d.comentario_evaluacion,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM documento_postulacion d
    INNER JOIN requisito r ON r.id = d.requisito_aplicable_id
    WHERE d.id = _id;
END$

CREATE PROCEDURE LISTAR_DOCUMENTOS_POSTULACION_TODOS()
BEGIN
    SELECT d.id, d.postulacion_id, d.requisito_aplicable_id AS requisito_id, d.numero_version,
        d.nombre_archivo, d.tipo_archivo, d.tamanio_archivo, d.ruta_archivo, d.fecha_carga,
        d.estado_documento, d.fecha_evaluacion, d.comentario_evaluacion,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM documento_postulacion d
    INNER JOIN requisito r ON r.id = d.requisito_aplicable_id
    ORDER BY d.postulacion_id, d.numero_version;
END$

-- ---------- DOCUMENTO_OBSERVACION ----------
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
    INSERT INTO documento_observacion(documento_id, evaluador_id, tipo_observacion, descripcion,
        fecha_observacion, estado_observacion, fecha_subsanacion, comentario_subsanacion)
    VALUES(_documento_id, _evaluador_id, _tipo_observacion, _descripcion, _fecha_observacion,
        _estado_observacion, _fecha_subsanacion, _comentario_subsanacion);
    SET _id = LAST_INSERT_ID();
END$

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
    UPDATE documento_observacion SET documento_id = _documento_id, evaluador_id = _evaluador_id,
        tipo_observacion = _tipo_observacion, descripcion = _descripcion, fecha_observacion = _fecha_observacion,
        estado_observacion = _estado_observacion, fecha_subsanacion = _fecha_subsanacion,
        comentario_subsanacion = _comentario_subsanacion
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_DOCUMENTO_OBSERVACION(
    IN _id INT
)
BEGIN
    DELETE FROM notificacion WHERE observacion_origen_id = _id;
    DELETE FROM documento_observacion WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_DOCUMENTO_OBSERVACION_X_ID(
    IN _id INT
)
BEGIN
    SELECT o.id, o.documento_id AS documento_postulacion_id, o.evaluador_id, o.tipo_observacion,
        o.descripcion, o.fecha_observacion, o.estado_observacion, o.fecha_subsanacion, o.comentario_subsanacion,
        d.numero_version, d.nombre_archivo, d.tamanio_archivo, d.ruta_archivo, d.estado_documento,
        pe.nombres AS nombres_evaluador, pe.apellido_paterno AS apellido_paterno_evaluador,
        pe.apellido_materno AS apellido_materno_evaluador, pe.correo AS correo_evaluador,
        pe.tipo_documento AS tipo_documento_evaluador, pe.numero_documento AS numero_documento_evaluador,
        pe.telefono AS telefono_evaluador, e.cargo AS cargo_evaluador
    FROM documento_observacion o
    INNER JOIN documento_postulacion d ON d.id = o.documento_id
    INNER JOIN persona pe ON pe.id = o.evaluador_id
    INNER JOIN evaluador e ON e.persona_id = pe.id
    WHERE o.id = _id;
END$

CREATE PROCEDURE LISTAR_DOCUMENTOS_OBSERVACION_TODOS()
BEGIN
    SELECT o.id, o.documento_id AS documento_postulacion_id, o.evaluador_id, o.tipo_observacion,
        o.descripcion, o.fecha_observacion, o.estado_observacion, o.fecha_subsanacion, o.comentario_subsanacion,
        d.numero_version, d.nombre_archivo, d.tamanio_archivo, d.ruta_archivo, d.estado_documento,
        pe.nombres AS nombres_evaluador, pe.apellido_paterno AS apellido_paterno_evaluador,
        pe.apellido_materno AS apellido_materno_evaluador, pe.correo AS correo_evaluador,
        pe.tipo_documento AS tipo_documento_evaluador, pe.numero_documento AS numero_documento_evaluador,
        pe.telefono AS telefono_evaluador, e.cargo AS cargo_evaluador
    FROM documento_observacion o
    INNER JOIN documento_postulacion d ON d.id = o.documento_id
    INNER JOIN persona pe ON pe.id = o.evaluador_id
    INNER JOIN evaluador e ON e.persona_id = pe.id
    ORDER BY o.fecha_observacion DESC;
END$

-- ---------- CARNE_POSTULANTE ----------
CREATE PROCEDURE INSERTAR_CARNE_POSTULANTE(
    OUT _id INT,
    IN _postulacion_id INT,
    IN _sede_id INT,
    IN _codigo_carne VARCHAR(30),
    IN _fecha_generacion DATE,
    IN _fecha_inicio_vigencia DATE,
    IN _fecha_fin_vigencia DATE,
    IN _aula_examen VARCHAR(30)
)
BEGIN
    INSERT INTO carne_postulante(postulacion_id, sede_id, codigo_carne, fecha_generacion,
        fecha_inicio_vigencia, fecha_fin_vigencia, aula_examen)
    VALUES(_postulacion_id, _sede_id, _codigo_carne, _fecha_generacion, _fecha_inicio_vigencia,
        _fecha_fin_vigencia, _aula_examen);
    SET _id = LAST_INSERT_ID();
END$

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
    UPDATE carne_postulante SET postulacion_id = _postulacion_id, sede_id = _sede_id,
        codigo_carne = _codigo_carne, fecha_generacion = _fecha_generacion,
        fecha_inicio_vigencia = _fecha_inicio_vigencia, fecha_fin_vigencia = _fecha_fin_vigencia,
        aula_examen = _aula_examen
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_CARNE_POSTULANTE(
    IN _id INT
)
BEGIN
    DELETE FROM carne_postulante WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_CARNE_POSTULANTE_X_ID(
    IN _id INT
)
BEGIN
    SELECT c.id, c.postulacion_id, c.sede_id, c.codigo_carne, c.fecha_generacion,
        c.fecha_inicio_vigencia, c.fecha_fin_vigencia, c.aula_examen,
        s.codigo AS codigo_sede, s.nombre AS nombre_sede, s.direccion AS direccion_sede
    FROM carne_postulante c
    INNER JOIN sede s ON s.id = c.sede_id
    WHERE c.id = _id;
END$

CREATE PROCEDURE LISTAR_CARNES_POSTULANTE_TODOS()
BEGIN
    SELECT c.id, c.postulacion_id, c.sede_id, c.codigo_carne, c.fecha_generacion,
        c.fecha_inicio_vigencia, c.fecha_fin_vigencia, c.aula_examen,
        s.codigo AS codigo_sede, s.nombre AS nombre_sede, s.direccion AS direccion_sede
    FROM carne_postulante c
    INNER JOIN sede s ON s.id = c.sede_id
    ORDER BY c.codigo_carne;
END$

-- ---------- NOTIFICACION ----------
CREATE PROCEDURE INSERTAR_NOTIFICACION(
    OUT _id INT,
    IN _postulacion_id INT,
    IN _observacion_origen_id INT,
    IN _medio_notificacion ENUM('CORREO','BANDEJA_SISTEMA'),
    IN _tipo_notificacion ENUM('VALIDACION','PAGO','OBSERVACION','FINALIZACION','CARNE'),
    IN _destinatario VARCHAR(150),
    IN _asunto VARCHAR(150),
    IN _mensaje TEXT,
    IN _fecha_programada DATE,
    IN _fecha_envio DATE,
    IN _estado_envio ENUM('PENDIENTE','ENVIADA','FALLIDA'),
    IN _leida BOOLEAN,
    IN _fecha_lectura DATE
)
BEGIN
    INSERT INTO notificacion(postulacion_id, observacion_origen_id, medio_notificacion, tipo_notificacion,
        destinatario, asunto, mensaje, fecha_programada, fecha_envio, estado_envio, leida, fecha_lectura)
    VALUES(_postulacion_id, _observacion_origen_id, _medio_notificacion, _tipo_notificacion, _destinatario,
        _asunto, _mensaje, _fecha_programada, _fecha_envio, _estado_envio, _leida, _fecha_lectura);
    SET _id = LAST_INSERT_ID();
END$

CREATE PROCEDURE MODIFICAR_NOTIFICACION(
    IN _id INT,
    IN _postulacion_id INT,
    IN _observacion_origen_id INT,
    IN _medio_notificacion ENUM('CORREO','BANDEJA_SISTEMA'),
    IN _tipo_notificacion ENUM('VALIDACION','PAGO','OBSERVACION','FINALIZACION','CARNE'),
    IN _destinatario VARCHAR(150),
    IN _asunto VARCHAR(150),
    IN _mensaje TEXT,
    IN _fecha_programada DATE,
    IN _fecha_envio DATE,
    IN _estado_envio ENUM('PENDIENTE','ENVIADA','FALLIDA'),
    IN _leida BOOLEAN,
    IN _fecha_lectura DATE
)
BEGIN
    UPDATE notificacion SET postulacion_id = _postulacion_id, observacion_origen_id = _observacion_origen_id,
        medio_notificacion = _medio_notificacion, tipo_notificacion = _tipo_notificacion, destinatario = _destinatario,
        asunto = _asunto, mensaje = _mensaje, fecha_programada = _fecha_programada, fecha_envio = _fecha_envio,
        estado_envio = _estado_envio, leida = _leida, fecha_lectura = _fecha_lectura
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_NOTIFICACION(
    IN _id INT
)
BEGIN
    DELETE FROM notificacion WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_NOTIFICACION_X_ID(
    IN _id INT
)
BEGIN
    SELECT n.id, n.postulacion_id, n.observacion_origen_id, n.medio_notificacion, n.tipo_notificacion,
        n.destinatario, n.asunto, n.mensaje, n.fecha_programada, n.fecha_envio, n.estado_envio,
        n.leida, n.fecha_lectura
    FROM notificacion n
    WHERE n.id = _id;
END$

CREATE PROCEDURE LISTAR_NOTIFICACIONES_TODAS()
BEGIN
    SELECT n.id, n.postulacion_id, n.observacion_origen_id, n.medio_notificacion, n.tipo_notificacion,
        n.destinatario, n.asunto, n.mensaje, n.fecha_programada, n.fecha_envio, n.estado_envio,
        n.leida, n.fecha_lectura
    FROM notificacion n
    ORDER BY n.fecha_programada DESC, n.id DESC;
END$

DELIMITER ;
