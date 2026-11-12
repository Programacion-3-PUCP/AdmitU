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
    medio_pago ENUM('TRANSFERENCIA') NOT NULL,
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
    CONSTRAINT fk_docpost_requisito FOREIGN KEY (requisito_aplicable_id) REFERENCES requisito_convocatoria_modalidad(id),
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

-- =====================================================================
-- ADMITU - DML
-- Datos de prueba inventados (coherentes entre tablas)
-- Ejecutar DESPUÉS del script DDL. Se usan IDs explícitos para que las
-- referencias entre tablas sean legibles y verificables.
-- =====================================================================
USE admitu_db;

START TRANSACTION;

-- ---------------------------------------------------------------------
-- CATÁLOGOS
-- ---------------------------------------------------------------------

-- Países
INSERT INTO pais (id, codigo_iso2, nombre, activo) VALUES
(1, 'PE', 'Perú',      TRUE),
(2, 'CO', 'Colombia',  TRUE),
(3, 'VE', 'Venezuela', TRUE),
(4, 'CL', 'Chile',     TRUE),
(5, 'AR', 'Argentina', FALSE);

-- Sedes
INSERT INTO sede (id, codigo, nombre, direccion, activo) VALUES
(1, 'SED-SM',  'Campus San Miguel',  'Av. Universitaria 1801, San Miguel', TRUE),
(2, 'SED-MIR', 'Sede Miraflores',    'Av. Larco 345, Miraflores',          TRUE),
(3, 'SED-SUR', 'Sede Surco',         'Av. Primavera 1270, Santiago de Surco', TRUE),
(4, 'SED-CHO', 'Sede Chorrillos',    'Av. Huaylas 980, Chorrillos',        FALSE);

-- Facultades
INSERT INTO facultad (id, codigo, nombre, activo) VALUES
(1, 'FCI',  'Facultad de Ciencias e Ingeniería',      TRUE),
(2, 'FCS',  'Facultad de Ciencias Sociales',          TRUE),
(3, 'FGAD', 'Facultad de Gestión y Alta Dirección',   TRUE),
(4, 'FAU',  'Facultad de Arquitectura y Urbanismo',   TRUE),
(5, 'FDER', 'Facultad de Derecho',                    TRUE);

-- Etapas (catálogo genérico; las fechas reales van en convocatoria_etapa)
INSERT INTO etapa (id, fecha_inicio, fecha_fin, codigo_etapa, nombre, descripcion, activo) VALUES
(1, NULL, NULL, 'INSCRIPCION', 'Inscripción',             'Registro de datos y elección de modalidad/carrera', TRUE),
(2, NULL, NULL, 'PAGO',        'Pago de derecho',         'Pago del derecho de inscripción por transferencia', TRUE),
(3, NULL, NULL, 'CARGA_DOCS',  'Carga de documentos',     'Subida de los requisitos de la modalidad',          TRUE),
(4, NULL, NULL, 'EVAL_DOCS',   'Evaluación documentaria', 'Revisión de documentos por evaluadores',            TRUE),
(5, NULL, NULL, 'SUBSANACION', 'Subsanación',             'Corrección de documentos observados',               TRUE),
(6, NULL, NULL, 'EXAMEN',      'Examen de admisión',      'Rendición de la prueba',                            TRUE),
(7, NULL, NULL, 'RESULTADOS',  'Publicación de resultados','Publicación de la lista de ingresantes',           TRUE);

-- Requisitos (tamaños en bytes: 1 MB = 1048576)
INSERT INTO requisito (id, codigo_requisito, nombre, descripcion, tipo_archivo_requerido, tamanio_maximo_bytes, activo) VALUES
(1, 'REQ-DNI',     'Documento de identidad',              'Imagen legible por ambas caras',                 'IMAGEN',     2097152,  TRUE),
(2, 'REQ-FOTO',    'Fotografía tamaño carné',             'Fondo blanco, sin lentes',                       'IMAGEN',     1048576,  TRUE),
(3, 'REQ-CERTSEC', 'Certificado de estudios secundarios', 'De 1.° a 4.° o 5.° año según modalidad',         'PDF',        5242880,  TRUE),
(4, 'REQ-TERCIO',  'Constancia de tercio superior',       'Emitida por el colegio de procedencia',          'PDF',        2097152,  TRUE),
(5, 'REQ-CERTUNI', 'Certificado de notas universitarias', 'Original o legalizado; apostillado si es extranjero', 'PDF',    5242880,  TRUE),
(6, 'REQ-SILABOS', 'Sílabos de cursos aprobados',         'Visados por la universidad de origen',           'PDF',        10485760, TRUE),
(7, 'REQ-CONADIS', 'Carné CONADIS',                       'Solo para postulantes con discapacidad',         'CUALQUIERA', 3145728,  TRUE),
(8, 'REQ-DJ',      'Declaración jurada',                  'Firmada por el postulante o su apoderado',       'PDF',        1048576,  TRUE);

-- Modalidades
INSERT INTO modalidad (id, codigo_modalidad, nombre, descripcion, requiere_colegio, requiere_universidad, activo) VALUES
(1, 'EVA-TAL',  'Evaluación del Talento',  'Para alumnos de 5.° de secundaria',              TRUE,  FALSE, TRUE),
(2, 'PRIM-OPC', 'Primera Opción',          'Para egresados de secundaria',                    TRUE,  FALSE, TRUE),
(3, 'TERCIO',   'Tercio Superior',         'Egresados en el tercio superior de su promoción', TRUE,  FALSE, TRUE),
(4, 'TRAS-EXT', 'Traslado Externo',        'Estudiantes de otras universidades',              FALSE, TRUE,  TRUE),
(5, 'GRAD-TIT', 'Graduados y Titulados',   'Bachilleres o titulados universitarios',          FALSE, TRUE,  TRUE);

-- Estados de postulación
INSERT INTO estado_postulacion (id, codigo, nombre, descripcion) VALUES
(1, 'BORRADOR',    'Borrador',            'Registrada, aún no enviada'),
(2, 'PEND_PAGO',   'Pendiente de pago',   'Enviada, a la espera del pago'),
(3, 'EN_REVISION', 'En revisión',         'Pago aprobado, documentos en evaluación'),
(4, 'OBSERVADA',   'Observada',           'Tiene documentos observados por subsanar'),
(5, 'APTA',        'Apta',                'Todos los documentos aprobados'),
(6, 'FINALIZADA',  'Finalizada',          'Inscripción concluida y carné emitido'),
(7, 'RECHAZADA',   'Rechazada',           'No cumple los requisitos'),
(8, 'ANULADA',     'Anulada',             'Anulada por vencimiento de plazo o a pedido');

-- ---------------------------------------------------------------------
-- PERSONAS (jerarquía: persona -> evaluador / apoderado / postulante)
-- ids 1-4 evaluadores, 5-8 apoderados, 9-16 postulantes
-- ---------------------------------------------------------------------
INSERT INTO persona (id, nombres, apellido_paterno, apellido_materno, correo, tipo_documento, numero_documento, telefono) VALUES
(1,  'Carmen Rosa',     'Villanueva', 'Paredes',   'carmen.villanueva@admitu.edu.pe', 'DNI', '40125873', '987451236'),
(2,  'Jorge Luis',      'Quispe',     'Mamani',    'jorge.quispe@admitu.edu.pe',      'DNI', '41870236', '986321457'),
(3,  'Patricia Elena',  'Salazar',    'Torres',    'patricia.salazar@admitu.edu.pe',  'DNI', '43561092', '985214763'),
(4,  'Miguel Ángel',    'Huamán',     'Rojas',     'miguel.huaman@admitu.edu.pe',     'DNI', '39784521', '984125896'),
(5,  'Rosa María',      'Flores',     'Gutiérrez', 'rosa.flores@mail.com',            'DNI', '42369874', '991234567'),
(6,  'Luis Alberto',    'Herrera',    'Castro',    'luis.herrera@mail.com',           'DNI', '40998712', '992345678'),
(7,  'Teresa',          'Ccori',      'Huanca',    'teresa.ccori@mail.com',           'DNI', '41235698', '993456789'),
(8,  'Ricardo',         'Díaz',       'Vega',      'ricardo.diaz@mail.com',           'DNI', '38745612', '994567890'),
(9,  'Lucía Fernanda',  'Mendoza',    'Flores',    'lucia.mendoza@mail.com',          'DNI', '78451236', '951236478'),
(10, 'Diego Alonso',    'Díaz',       'Ríos',      'diego.diaz@mail.com',             'DNI', '78965412', '952347851'),
(11, 'Valeria Sofía',   'Ramírez',    'Ccori',     'valeria.ramirez@mail.com',        'DNI', '79123654', '953458962'),
(12, 'Andrés Felipe',   'Gómez',      'Restrepo',  'andres.gomez@mail.com',           'CE',  '001845217', '954569873'),
(13, 'Camila Andrea',   'Torres',     'Benavides', 'camila.torres@mail.com',          'DNI', '76321458', '955670984'),
(14, 'Sebastián',       'Rojas',      'Paredes',   'sebastian.rojas@mail.com',        'DNI', '77412589', '956781095'),
(15, 'María José',      'Paz',        'Urdaneta',  'mariajose.paz@mail.com',          'CPP', '000781245', '957892106'),
(16, 'Joaquín',         'Herrera',    'Salas',     'joaquin.herrera@mail.com',        'DNI', '79854123', NULL);

INSERT INTO evaluador (persona_id, cargo, activo) VALUES
(1, 'Coordinadora de Admisión',  TRUE),
(2, 'Evaluador documentario',    TRUE),
(3, 'Evaluadora documentaria',   TRUE),
(4, 'Evaluador documentario',    FALSE);

INSERT INTO apoderado (persona_id, parentesco, activo) VALUES
(5, 'MADRE', TRUE),
(6, 'PADRE', TRUE),
(7, 'TUTOR', TRUE),
(8, 'PADRE', TRUE);

-- Menores de edad (9, 10, 11, 16) tienen apoderado; los mayores no.
INSERT INTO postulante (persona_id, apoderado_id, fecha_nacimiento, tiene_discapacidad, numero_carnet_conadis, correo_validado, fecha_validacion_correo) VALUES
(9,  5,    '2009-03-14', FALSE, NULL,              TRUE,  '2026-09-05'),
(10, 8,    '2009-08-02', FALSE, NULL,              TRUE,  '2026-05-18'),
(11, 7,    '2010-01-20', TRUE,  'CONADIS-0254781', TRUE,  '2026-09-10'),
(12, NULL, '2004-05-11', FALSE, NULL,              TRUE,  '2026-09-15'),
(13, NULL, '2007-11-30', FALSE, NULL,              TRUE,  '2026-05-01'),
(14, NULL, '2008-06-22', FALSE, NULL,              TRUE,  '2026-09-20'),
(15, NULL, '1999-07-15', FALSE, NULL,              TRUE,  '2026-09-25'),
(16, 6,    '2009-12-01', FALSE, NULL,              FALSE, NULL);

-- ---------------------------------------------------------------------
-- INSTITUCIONES Y ANTECEDENTES
-- ---------------------------------------------------------------------
INSERT INTO institucion_educativa (id, pais_id, codigo_externo, nombre, tipo_institucion, activo) VALUES
(1, 1, '1234567',    'Colegio Santa Rosa de Lima',          'COLEGIO',     TRUE),
(2, 1, '0456789',    'I.E. José María Arguedas',            'COLEGIO',     TRUE),
(3, 1, '0789123',    'Colegio Los Andes',                   'COLEGIO',     TRUE),
(4, 1, '1098765',    'Colegio Miraflores Bilingüe',         'COLEGIO',     TRUE),
(5, 1, 'UNIV-PE-045','Universidad Nacional de Ingeniería',  'UNIVERSIDAD', TRUE),
(6, 2, 'UNIV-CO-011','Universidad Nacional de Colombia',    'UNIVERSIDAD', TRUE),
(7, 3, 'UNIV-VE-003','Universidad Central de Venezuela',    'UNIVERSIDAD', TRUE);

INSERT INTO antecedente_academico (id, postulante_id, institucion_id, anio_inicio, anio_fin, descripcion, activo) VALUES
(1, 9,  1, 2022, 2026, 'Secundaria - cursando 5.° año',                 TRUE),
(2, 10, 2, 2022, 2026, 'Secundaria - cursando 5.° año',                 TRUE),
(3, 11, 3, 2022, 2026, 'Secundaria - cursando 5.° año',                 TRUE),
(4, 12, 6, 2022, 2026, 'Ingeniería Civil - 7 semestres aprobados',      TRUE),
(5, 13, 4, 2020, 2024, 'Secundaria completa',                           TRUE),
(6, 14, 2, 2021, 2025, 'Secundaria completa - tercio superior',         TRUE),
(7, 15, 7, 2016, 2021, 'Bachiller en Administración Comercial',         TRUE),
(8, 16, 1, 2022, 2026, 'Secundaria - cursando 5.° año',                 TRUE);

-- ---------------------------------------------------------------------
-- CARRERAS
-- ---------------------------------------------------------------------
INSERT INTO carrera (id, facultad_id, codigo_carrera, nombre, activo) VALUES
(1, 1, 'ING-INF', 'Ingeniería Informática',  TRUE),
(2, 1, 'ING-IND', 'Ingeniería Industrial',   TRUE),
(3, 1, 'ING-CIV', 'Ingeniería Civil',        TRUE),
(4, 2, 'ECO',     'Economía',                TRUE),
(5, 3, 'GES',     'Gestión',                 TRUE),
(6, 4, 'ARQ',     'Arquitectura',            TRUE),
(7, 5, 'DER',     'Derecho',                 TRUE),
(8, 1, 'ING-MEC', 'Ingeniería Mecánica',     TRUE);

-- ---------------------------------------------------------------------
-- CONVOCATORIAS Y SU CONFIGURACIÓN
-- ---------------------------------------------------------------------
INSERT INTO convocatoria (id, codigo_convocatoria, nombre, periodo, fecha_inicio, fecha_fin, estado, descripcion, activo) VALUES
(1, 'ADM-2026-2',  'Admisión 2026-2',              '2026-2', '2026-04-01', '2026-07-31', 'CERRADA',   'Proceso de admisión del segundo semestre 2026', TRUE),
(2, 'ADM-2027-1',  'Admisión 2027-1',              '2027-1', '2026-09-01', '2027-02-28', 'PUBLICADA', 'Proceso de admisión del primer semestre 2027',  TRUE),
(3, 'ADM-2027-2',  'Admisión 2027-2',              '2027-2', '2027-03-01', '2027-07-31', 'BORRADOR',  'En configuración',                              TRUE),
(4, 'ADM-EXT-2026','Admisión Extraordinaria 2026', '2026-E', '2026-08-01', '2026-08-31', 'ANULADA',   'Anulada por resolución rectoral',               FALSE);

-- Oferta de carreras por convocatoria
INSERT INTO oferta_carrera (id, convocatoria_id, carrera_id, cantidad_vacantes) VALUES
-- 2026-2
(1, 1, 1, 60), (2, 1, 2, 80), (3, 1, 4, 50), (4, 1, 7, 70),
-- 2027-1
(5, 2, 1, 70), (6, 2, 2, 90), (7, 2, 3, 60), (8, 2, 4, 55), (9, 2, 5, 65), (10, 2, 6, 40), (11, 2, 7, 75),
-- 2027-2 (borrador)
(12, 3, 1, 50), (13, 3, 2, 60), (14, 3, 8, 30);

-- Modalidades habilitadas por convocatoria
INSERT INTO convocatoria_modalidad (id, convocatoria_id, modalidad_id, costo_inscripcion, observacion) VALUES
(1,  1, 1, 450.00, NULL),
(2,  1, 2, 500.00, NULL),
(3,  1, 4, 650.00, 'Incluye convalidación de cursos'),
(4,  2, 1, 480.00, NULL),
(5,  2, 2, 520.00, NULL),
(6,  2, 3, 350.00, 'Costo reducido por mérito'),
(7,  2, 4, 680.00, 'Incluye convalidación de cursos'),
(8,  2, 5, 700.00, NULL),
(9,  3, 1, 480.00, NULL),
(10, 3, 2, 520.00, NULL);

-- Cronograma de etapas por convocatoria
INSERT INTO convocatoria_etapa (id, convocatoria_id, etapa_id, fecha_inicio, fecha_fin) VALUES
-- 2026-2
(1,  1, 1, '2026-04-01', '2026-06-10'),
(2,  1, 2, '2026-04-01', '2026-06-15'),
(3,  1, 3, '2026-04-01', '2026-06-15'),
(4,  1, 4, '2026-04-10', '2026-06-25'),
(5,  1, 5, '2026-04-10', '2026-06-30'),
(6,  1, 6, '2026-07-12', '2026-07-12'),
(7,  1, 7, '2026-07-24', '2026-07-24'),
-- 2027-1
(8,  2, 1, '2026-09-01', '2026-12-15'),
(9,  2, 2, '2026-09-01', '2026-12-20'),
(10, 2, 3, '2026-09-01', '2026-12-20'),
(11, 2, 4, '2026-09-15', '2027-01-10'),
(12, 2, 5, '2026-09-15', '2027-01-15'),
(13, 2, 6, '2027-01-24', '2027-01-24'),
(14, 2, 7, '2027-02-05', '2027-02-05'),
-- 2027-2 (borrador, cronograma parcial)
(15, 3, 1, '2027-03-01', '2027-05-31'),
(16, 3, 6, '2027-07-04', '2027-07-04');

-- Requisitos por convocatoria-modalidad
INSERT INTO requisito_convocatoria_modalidad (id, convocatoria_modalidad_id, requisito_id, obligatorio, orden_presentacion) VALUES
-- cm1: 2026-2 Evaluación del Talento
(1,  1, 1, TRUE, 1), (2,  1, 2, TRUE, 2), (3,  1, 3, TRUE, 3),
-- cm2: 2026-2 Primera Opción
(4,  2, 1, TRUE, 1), (5,  2, 2, TRUE, 2), (6,  2, 3, TRUE, 3),
-- cm3: 2026-2 Traslado Externo
(7,  3, 1, TRUE, 1), (8,  3, 2, TRUE, 2), (9,  3, 5, TRUE, 3), (10, 3, 6, TRUE, 4),
-- cm4: 2027-1 Evaluación del Talento
(11, 4, 1, TRUE, 1), (12, 4, 2, TRUE, 2), (13, 4, 3, TRUE, 3), (14, 4, 8, TRUE, 4), (15, 4, 7, FALSE, 5),
-- cm5: 2027-1 Primera Opción
(16, 5, 1, TRUE, 1), (17, 5, 2, TRUE, 2), (18, 5, 3, TRUE, 3), (19, 5, 8, TRUE, 4), (20, 5, 7, FALSE, 5),
-- cm6: 2027-1 Tercio Superior
(21, 6, 1, TRUE, 1), (22, 6, 2, TRUE, 2), (23, 6, 3, TRUE, 3), (24, 6, 4, TRUE, 4), (25, 6, 8, TRUE, 5),
-- cm7: 2027-1 Traslado Externo
(26, 7, 1, TRUE, 1), (27, 7, 2, TRUE, 2), (28, 7, 5, TRUE, 3), (29, 7, 6, TRUE, 4), (30, 7, 8, TRUE, 5),
-- cm8: 2027-1 Graduados y Titulados
(31, 8, 1, TRUE, 1), (32, 8, 2, TRUE, 2), (33, 8, 5, TRUE, 3), (34, 8, 8, TRUE, 4);

-- Inscritos por convocatoria (N:M) - consistente con las postulaciones
INSERT INTO convocatoria_postulante (convocatoria_id, postulante_id) VALUES
(1, 13), (1, 10),
(2, 9), (2, 10), (2, 11), (2, 12), (2, 13), (2, 14), (2, 15), (2, 16);

-- ---------------------------------------------------------------------
-- POSTULACIONES
-- modalidad_elegida_id y carrera_elegida_id pertenecen a la MISMA convocatoria
-- ---------------------------------------------------------------------
INSERT INTO postulacion (id, postulante_id, convocatoria_id, modalidad_elegida_id, carrera_elegida_id, estado_actual_id,
                         fecha_registro, fecha_envio, fecha_finalizacion, codigo_inscripcion, observacion_general) VALUES
-- Convocatoria 2026-2 (cerrada)
(1,  13, 1, 2, 1,    6, '2026-05-01', '2026-05-02', '2026-06-01', 'INS-2026-2-0001', NULL),
(2,  10, 1, 1, 2,    8, '2026-05-18', '2026-05-20', NULL,         'INS-2026-2-0002', 'Anulada: pago no realizado en plazo'),
-- Convocatoria 2027-1 (en curso)
(3,  9,  2, 4, 5,    6, '2026-09-05', '2026-09-06', '2026-10-02', 'INS-2027-1-0001', NULL),
(4,  11, 2, 5, 11,   4, '2026-09-10', '2026-09-12', NULL,         'INS-2027-1-0002', 'Requiere aula accesible'),
(5,  12, 2, 7, 7,    3, '2026-09-15', '2026-09-18', NULL,         'INS-2027-1-0003', 'Traslado desde el extranjero'),
(6,  13, 2, 5, 6,    2, '2026-10-25', '2026-10-28', NULL,         'INS-2027-1-0004', 'Segunda postulación del postulante'),
(7,  14, 2, 6, 8,    5, '2026-09-20', '2026-09-21', NULL,         'INS-2027-1-0005', NULL),
(8,  15, 2, 8, 9,    7, '2026-09-25', '2026-09-26', NULL,         'INS-2027-1-0006', 'Certificado sin apostillar'),
(9,  16, 2, 4, NULL, 1, '2026-11-08', NULL,         NULL,         NULL,              'Borrador sin carrera elegida'),
(10, 10, 2, 4, 5,    6, '2026-09-07', '2026-09-07', '2026-10-05', 'INS-2027-1-0007', 'Segunda postulación del postulante');

-- Historial de estados (el último registro coincide con estado_actual_id)
INSERT INTO postulacion_historial (id, postulacion_id, estado_anterior_id, estado_actual_id, fecha_cambio, responsable_cambio, motivo_cambio) VALUES
-- P1 Camila (2026-2): finalizada
(1,  1, NULL, 1, '2026-05-01', 'camila.torres@mail.com',          'Registro de postulación'),
(2,  1, 1,    2, '2026-05-02', 'camila.torres@mail.com',          'Envío de postulación'),
(3,  1, 2,    3, '2026-05-04', 'SISTEMA',                         'Pago aprobado'),
(4,  1, 3,    5, '2026-05-15', 'jorge.quispe@admitu.edu.pe',      'Documentos aprobados'),
(5,  1, 5,    6, '2026-06-01', 'SISTEMA',                         'Carné emitido'),
-- P2 Diego (2026-2): anulada
(6,  2, NULL, 1, '2026-05-18', 'diego.diaz@mail.com',             'Registro de postulación'),
(7,  2, 1,    2, '2026-05-20', 'diego.diaz@mail.com',             'Envío de postulación'),
(8,  2, 2,    8, '2026-06-16', 'SISTEMA',                         'Pago vencido'),
-- P3 Lucía: finalizada
(9,  3, NULL, 1, '2026-09-05', 'lucia.mendoza@mail.com',          'Registro de postulación'),
(10, 3, 1,    2, '2026-09-06', 'lucia.mendoza@mail.com',          'Envío de postulación'),
(11, 3, 2,    3, '2026-09-08', 'SISTEMA',                         'Pago aprobado'),
(12, 3, 3,    5, '2026-09-25', 'patricia.salazar@admitu.edu.pe',  'Documentos aprobados'),
(13, 3, 5,    6, '2026-10-02', 'SISTEMA',                         'Carné emitido'),
-- P4 Valeria: observada
(14, 4, NULL, 1, '2026-09-10', 'valeria.ramirez@mail.com',        'Registro de postulación'),
(15, 4, 1,    2, '2026-09-12', 'valeria.ramirez@mail.com',        'Envío de postulación'),
(16, 4, 2,    3, '2026-09-13', 'SISTEMA',                         'Pago aprobado'),
(17, 4, 3,    4, '2026-10-20', 'jorge.quispe@admitu.edu.pe',      'Certificado de estudios ilegible'),
-- P5 Andrés: observada y subsanada, vuelve a revisión
(18, 5, NULL, 1, '2026-09-15', 'andres.gomez@mail.com',           'Registro de postulación'),
(19, 5, 1,    2, '2026-09-18', 'andres.gomez@mail.com',           'Envío de postulación'),
(20, 5, 2,    3, '2026-09-19', 'SISTEMA',                         'Pago aprobado'),
(21, 5, 3,    4, '2026-10-08', 'patricia.salazar@admitu.edu.pe',  'Sílabos incompletos'),
(22, 5, 4,    3, '2026-10-14', 'andres.gomez@mail.com',           'Observación subsanada'),
-- P6 Camila (2027-1): pendiente de pago
(23, 6, NULL, 1, '2026-10-25', 'camila.torres@mail.com',          'Registro de postulación'),
(24, 6, 1,    2, '2026-10-28', 'camila.torres@mail.com',          'Envío de postulación'),
-- P7 Sebastián: apta
(25, 7, NULL, 1, '2026-09-20', 'sebastian.rojas@mail.com',        'Registro de postulación'),
(26, 7, 1,    2, '2026-09-21', 'sebastian.rojas@mail.com',        'Envío de postulación'),
(27, 7, 2,    3, '2026-09-22', 'SISTEMA',                         'Pago aprobado'),
(28, 7, 3,    5, '2026-10-15', 'carmen.villanueva@admitu.edu.pe', 'Documentos aprobados'),
-- P8 María José: rechazada
(29, 8, NULL, 1, '2026-09-25', 'mariajose.paz@mail.com',          'Registro de postulación'),
(30, 8, 1,    2, '2026-09-26', 'mariajose.paz@mail.com',          'Envío de postulación'),
(31, 8, 2,    3, '2026-09-30', 'SISTEMA',                         'Pago aprobado (segundo intento)'),
(32, 8, 3,    4, '2026-10-05', 'jorge.quispe@admitu.edu.pe',      'Certificado universitario sin apostilla'),
(33, 8, 4,    7, '2026-10-20', 'carmen.villanueva@admitu.edu.pe', 'No subsanó dentro del plazo'),
-- P9 Joaquín: borrador
(34, 9, NULL, 1, '2026-11-08', 'joaquin.herrera@mail.com',        'Registro de postulación'),
-- P10 Diego (2027-1): finalizada
(35, 10, NULL, 1, '2026-09-07', 'diego.diaz@mail.com',            'Registro de postulación'),
(36, 10, 1,    2, '2026-09-07', 'diego.diaz@mail.com',            'Envío de postulación'),
(37, 10, 2,    3, '2026-09-09', 'SISTEMA',                        'Pago aprobado'),
(38, 10, 3,    5, '2026-09-28', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(39, 10, 5,    6, '2026-10-05', 'SISTEMA',                        'Carné emitido');

-- ---------------------------------------------------------------------
-- PAGOS (monto = costo_inscripcion de la modalidad elegida)
-- ---------------------------------------------------------------------
INSERT INTO pago (id, postulacion_id, medio_pago, monto, fecha_generacion, fecha_pago, codigo_pago, referencia_pasarela, estado_pago, ruta_voucher) VALUES
(1,  1,  'TRANSFERENCIA', 500.00, '2026-05-02', '2026-05-03', 'PAG-000001', 'OP-84512378', 'APROBADO',  '/vouchers/PAG-000001.pdf'),
(2,  2,  'TRANSFERENCIA', 450.00, '2026-05-20', NULL,         'PAG-000002', NULL,          'VENCIDO',   NULL),
(3,  3,  'TRANSFERENCIA', 480.00, '2026-09-06', '2026-09-07', 'PAG-000003', 'OP-85120394', 'APROBADO',  '/vouchers/PAG-000003.pdf'),
(4,  4,  'TRANSFERENCIA', 520.00, '2026-09-12', '2026-09-12', 'PAG-000004', 'OP-85234710', 'APROBADO',  '/vouchers/PAG-000004.pdf'),
(5,  5,  'TRANSFERENCIA', 680.00, '2026-09-18', '2026-09-18', 'PAG-000005', 'OP-85301256', 'APROBADO',  '/vouchers/PAG-000005.pdf'),
(6,  6,  'TRANSFERENCIA', 520.00, '2026-10-28', '2026-11-08', 'PAG-000006', 'OP-86904417', 'PENDIENTE', '/vouchers/PAG-000006.jpg'),
(7,  7,  'TRANSFERENCIA', 350.00, '2026-09-21', '2026-09-21', 'PAG-000007', 'OP-85412963', 'APROBADO',  '/vouchers/PAG-000007.pdf'),
(8,  8,  'TRANSFERENCIA', 700.00, '2026-09-26', '2026-09-27', 'PAG-000008', 'OP-85500021', 'RECHAZADO', '/vouchers/PAG-000008.jpg'),
(9,  8,  'TRANSFERENCIA', 700.00, '2026-09-28', '2026-09-29', 'PAG-000009', 'OP-85533870', 'APROBADO',  '/vouchers/PAG-000009.pdf'),
(10, 10, 'TRANSFERENCIA', 480.00, '2026-09-07', '2026-09-08', 'PAG-000010', 'OP-85139902', 'APROBADO',  '/vouchers/PAG-000010.pdf');

-- ---------------------------------------------------------------------
-- DOCUMENTOS (requisito_aplicable_id pertenece a la modalidad de la postulación;
-- tipo y tamaño respetan el requisito)
-- ---------------------------------------------------------------------
INSERT INTO documento_postulacion (id, postulacion_id, requisito_aplicable_id, numero_version, nombre_archivo, tipo_archivo, tamanio_archivo,
                                   ruta_archivo, fecha_carga, estado_documento, fecha_evaluacion, comentario_evaluacion) VALUES
-- P1 (cm2: rcm 4,5,6)
(1,  1, 4,  1, 'dni.jpg',                 'IMAGEN', 845312,  '/docs/INS-2026-2-0001/dni.jpg',                 '2026-05-02', 'APROBADO',  '2026-05-15', NULL),
(2,  1, 5,  1, 'foto.jpg',                'IMAGEN', 412876,  '/docs/INS-2026-2-0001/foto.jpg',                '2026-05-02', 'APROBADO',  '2026-05-15', NULL),
(3,  1, 6,  1, 'certificado_estudios.pdf','PDF',    2345678, '/docs/INS-2026-2-0001/certificado_estudios.pdf','2026-05-02', 'APROBADO',  '2026-05-15', NULL),
-- P3 (cm4: rcm 11-14)
(4,  3, 11, 1, 'dni.png',                 'IMAGEN', 1203456, '/docs/INS-2027-1-0001/dni.png',                 '2026-09-06', 'APROBADO',  '2026-09-25', NULL),
(5,  3, 12, 1, 'foto.jpg',                'IMAGEN', 356789,  '/docs/INS-2027-1-0001/foto.jpg',                '2026-09-06', 'APROBADO',  '2026-09-25', NULL),
(6,  3, 13, 1, 'certificado_estudios.pdf','PDF',    1987654, '/docs/INS-2027-1-0001/certificado_estudios.pdf','2026-09-06', 'APROBADO',  '2026-09-25', NULL),
(7,  3, 14, 1, 'declaracion_jurada.pdf',  'PDF',    245678,  '/docs/INS-2027-1-0001/declaracion_jurada.pdf',  '2026-09-06', 'APROBADO',  '2026-09-25', NULL),
-- P4 (cm5: rcm 16-20, incluye CONADIS opcional)
(8,  4, 16, 1, 'dni.jpg',                 'IMAGEN', 987123,  '/docs/INS-2027-1-0002/dni.jpg',                 '2026-09-12', 'APROBADO',  '2026-10-20', NULL),
(9,  4, 17, 1, 'foto.jpg',                'IMAGEN', 401234,  '/docs/INS-2027-1-0002/foto.jpg',                '2026-09-12', 'APROBADO',  '2026-10-20', NULL),
(10, 4, 18, 1, 'certificado_estudios.pdf','PDF',    3456123, '/docs/INS-2027-1-0002/certificado_estudios.pdf','2026-09-12', 'OBSERVADO', '2026-10-20', 'Escaneo ilegible'),
(11, 4, 19, 1, 'declaracion_jurada.pdf',  'PDF',    198765,  '/docs/INS-2027-1-0002/declaracion_jurada.pdf',  '2026-09-12', 'APROBADO',  '2026-10-20', NULL),
(12, 4, 20, 1, 'carne_conadis.pdf',       'PDF',    754321,  '/docs/INS-2027-1-0002/carne_conadis.pdf',       '2026-09-12', 'APROBADO',  '2026-10-20', NULL),
-- P5 (cm7: rcm 26-30; sílabos con versión 2)
(13, 5, 26, 1, 'carne_extranjeria.jpg',   'IMAGEN', 1543210, '/docs/INS-2027-1-0003/carne_extranjeria.jpg',   '2026-09-18', 'APROBADO',  '2026-10-08', NULL),
(14, 5, 27, 1, 'foto.jpg',                'IMAGEN', 389012,  '/docs/INS-2027-1-0003/foto.jpg',                '2026-09-18', 'APROBADO',  '2026-10-08', NULL),
(15, 5, 28, 1, 'certificado_notas.pdf',   'PDF',    4123456, '/docs/INS-2027-1-0003/certificado_notas.pdf',   '2026-09-18', 'APROBADO',  '2026-10-08', NULL),
(16, 5, 29, 1, 'silabos.pdf',             'PDF',    6234567, '/docs/INS-2027-1-0003/silabos_v1.pdf',          '2026-09-18', 'OBSERVADO', '2026-10-08', 'Faltan sílabos'),
(17, 5, 29, 2, 'silabos_completos.pdf',   'PDF',    8912345, '/docs/INS-2027-1-0003/silabos_v2.pdf',          '2026-10-14', 'PENDIENTE', NULL,         NULL),
(18, 5, 30, 1, 'declaracion_jurada.pdf',  'PDF',    210987,  '/docs/INS-2027-1-0003/declaracion_jurada.pdf',  '2026-09-18', 'APROBADO',  '2026-10-08', NULL),
-- P6 (cm5: rcm 16-19, aún sin evaluar)
(19, 6, 16, 1, 'dni.jpg',                 'IMAGEN', 902345,  '/docs/INS-2027-1-0004/dni.jpg',                 '2026-10-28', 'PENDIENTE', NULL,         NULL),
(20, 6, 17, 1, 'foto.jpg',                'IMAGEN', 367890,  '/docs/INS-2027-1-0004/foto.jpg',                '2026-10-28', 'PENDIENTE', NULL,         NULL),
(21, 6, 18, 1, 'certificado_estudios.pdf','PDF',    2678901, '/docs/INS-2027-1-0004/certificado_estudios.pdf','2026-10-28', 'PENDIENTE', NULL,         NULL),
(22, 6, 19, 1, 'declaracion_jurada.pdf',  'PDF',    223456,  '/docs/INS-2027-1-0004/declaracion_jurada.pdf',  '2026-10-28', 'PENDIENTE', NULL,         NULL),
-- P7 (cm6: rcm 21-25)
(23, 7, 21, 1, 'dni.jpg',                 'IMAGEN', 876543,  '/docs/INS-2027-1-0005/dni.jpg',                 '2026-09-21', 'APROBADO',  '2026-10-15', NULL),
(24, 7, 22, 1, 'foto.jpg',                'IMAGEN', 345678,  '/docs/INS-2027-1-0005/foto.jpg',                '2026-09-21', 'APROBADO',  '2026-10-15', NULL),
(25, 7, 23, 1, 'certificado_estudios.pdf','PDF',    2890123, '/docs/INS-2027-1-0005/certificado_estudios.pdf','2026-09-21', 'APROBADO',  '2026-10-15', NULL),
(26, 7, 24, 1, 'constancia_tercio.pdf',   'PDF',    567890,  '/docs/INS-2027-1-0005/constancia_tercio.pdf',   '2026-09-21', 'APROBADO',  '2026-10-15', NULL),
(27, 7, 25, 1, 'declaracion_jurada.pdf',  'PDF',    201234,  '/docs/INS-2027-1-0005/declaracion_jurada.pdf',  '2026-09-21', 'APROBADO',  '2026-10-15', NULL),
-- P8 (cm8: rcm 31-34)
(28, 8, 31, 1, 'cpp.jpg',                 'IMAGEN', 1123456, '/docs/INS-2027-1-0006/cpp.jpg',                 '2026-09-26', 'APROBADO',  '2026-10-05', NULL),
(29, 8, 32, 1, 'foto.jpg',                'IMAGEN', 398765,  '/docs/INS-2027-1-0006/foto.jpg',                '2026-09-26', 'APROBADO',  '2026-10-05', NULL),
(30, 8, 33, 1, 'certificado_notas.pdf',   'PDF',    3987654, '/docs/INS-2027-1-0006/certificado_notas.pdf',   '2026-09-26', 'RECHAZADO', '2026-10-20', 'Sin apostilla; no subsanado en plazo'),
(31, 8, 34, 1, 'declaracion_jurada.pdf',  'PDF',    187654,  '/docs/INS-2027-1-0006/declaracion_jurada.pdf',  '2026-09-26', 'APROBADO',  '2026-10-05', NULL),
-- P9 (cm4: borrador con carga parcial)
(32, 9, 11, 1, 'dni.jpg',                 'IMAGEN', 934567,  '/docs/BORRADOR-9/dni.jpg',                      '2026-11-08', 'PENDIENTE', NULL,         NULL),
(33, 9, 12, 1, 'foto.jpg',                'IMAGEN', 378901,  '/docs/BORRADOR-9/foto.jpg',                     '2026-11-08', 'PENDIENTE', NULL,         NULL),
-- P10 (cm4: rcm 11-14)
(34, 10, 11, 1, 'dni.jpg',                'IMAGEN', 912345,  '/docs/INS-2027-1-0007/dni.jpg',                 '2026-09-07', 'APROBADO',  '2026-09-28', NULL),
(35, 10, 12, 1, 'foto.jpg',               'IMAGEN', 334567,  '/docs/INS-2027-1-0007/foto.jpg',                '2026-09-07', 'APROBADO',  '2026-09-28', NULL),
(36, 10, 13, 1, 'certificado_estudios.pdf','PDF',   2134567, '/docs/INS-2027-1-0007/certificado_estudios.pdf','2026-09-07', 'APROBADO',  '2026-09-28', NULL),
(37, 10, 14, 1, 'declaracion_jurada.pdf', 'PDF',    219876,  '/docs/INS-2027-1-0007/declaracion_jurada.pdf',  '2026-09-07', 'APROBADO',  '2026-09-28', NULL);

-- Observaciones de documentos
INSERT INTO documento_observacion (id, documento_id, evaluador_id, tipo_observacion, descripcion, fecha_observacion,
                                   estado_observacion, fecha_subsanacion, comentario_subsanacion) VALUES
(1, 10, 2, 'ILEGIBLE', 'No se leen las notas de 4.° y 5.° año',         '2026-10-20', 'PENDIENTE', NULL,         NULL),
(2, 16, 3, 'FALTANTE', 'Faltan sílabos de Topografía y Resistencia de Materiales', '2026-10-08', 'SUBSANADA', '2026-10-14', 'Se cargó la versión 2 con todos los sílabos'),
(3, 30, 2, 'OTRO',     'Certificado extranjero sin apostilla de La Haya', '2026-10-05', 'PENDIENTE', NULL,         NULL);

-- ---------------------------------------------------------------------
-- CARNÉS (solo postulaciones FINALIZADAS)
-- ---------------------------------------------------------------------
INSERT INTO carne_postulante (id, postulacion_id, sede_id, codigo_carne, fecha_generacion, fecha_inicio_vigencia, fecha_fin_vigencia, aula_examen) VALUES
(1, 1,  1, 'CRN-2026-2-0001', '2026-06-01', '2026-06-01', '2026-07-12', 'A-101'),
(2, 3,  1, 'CRN-2027-1-0001', '2026-10-02', '2026-10-02', '2027-01-24', 'E-203'),
(3, 10, 2, 'CRN-2027-1-0002', '2026-10-05', '2026-10-05', '2027-01-24', 'M-105');

-- ---------------------------------------------------------------------
-- NOTIFICACIONES
-- ---------------------------------------------------------------------
INSERT INTO notificacion (id, postulacion_id, observacion_origen_id, medio_notificacion, tipo_notificacion, destinatario, asunto, mensaje,
                          fecha_programada, fecha_envio, estado_envio, leida, fecha_lectura) VALUES
(1,  1,  NULL, 'BANDEJA_SISTEMA', 'CARNE',        'camila.torres@mail.com',   'Tu carné de postulante está listo', 'Descarga tu carné. Aula A-101, Campus San Miguel.', '2026-06-01', '2026-06-01', 'ENVIADA', TRUE,  '2026-06-02'),
(2,  3,  NULL, 'CORREO',          'VALIDACION',   'lucia.mendoza@mail.com',   'Valida tu correo',                  'Ingresa al enlace para validar tu correo.',           '2026-09-05', '2026-09-05', 'ENVIADA', TRUE,  '2026-09-05'),
(3,  3,  NULL, 'CORREO',          'PAGO',         'lucia.mendoza@mail.com',   'Pago aprobado',                     'Tu pago PAG-000003 fue aprobado.',                    '2026-09-08', '2026-09-08', 'ENVIADA', TRUE,  '2026-09-08'),
(4,  3,  NULL, 'CORREO',          'FINALIZACION', 'rosa.flores@mail.com',     'Inscripción finalizada',            'La inscripción de Lucía Mendoza fue completada.',     '2026-10-02', '2026-10-02', 'ENVIADA', TRUE,  '2026-10-03'),
(5,  3,  NULL, 'BANDEJA_SISTEMA', 'CARNE',        'lucia.mendoza@mail.com',   'Tu carné de postulante está listo', 'Aula E-203, Campus San Miguel.',                      '2026-10-02', '2026-10-02', 'ENVIADA', FALSE, NULL),
(6,  4,  1,    'CORREO',          'OBSERVACION',  'valeria.ramirez@mail.com', 'Documento observado',               'Tu certificado de estudios es ilegible. Súbelo nuevamente.', '2026-10-20', '2026-10-20', 'ENVIADA', TRUE, '2026-10-21'),
(7,  4,  1,    'BANDEJA_SISTEMA', 'OBSERVACION',  'valeria.ramirez@mail.com', 'Documento observado',               'Tienes una observación pendiente de subsanar.',       '2026-10-20', '2026-10-20', 'ENVIADA', FALSE, NULL),
(8,  5,  2,    'CORREO',          'OBSERVACION',  'andres.gomez@mail.com',    'Documento observado',               'Faltan sílabos en tu expediente.',                    '2026-10-08', '2026-10-08', 'ENVIADA', TRUE,  '2026-10-09'),
(9,  6,  NULL, 'CORREO',          'PAGO',         'camila.torres@mail.com',   'Código de pago generado',           'Realiza la transferencia con el código PAG-000006.',  '2026-10-28', '2026-10-28', 'ENVIADA', TRUE,  '2026-10-29'),
(10, 8,  NULL, 'CORREO',          'PAGO',         'mariajose.paz@mail.com',   'Pago rechazado',                    'El voucher adjunto no es legible.',                   '2026-09-27', '2026-09-27', 'ENVIADA', TRUE,  '2026-09-27'),
(11, 8,  3,    'CORREO',          'OBSERVACION',  'mariajose.paz@mail.com',   'Documento observado',               'Tu certificado de notas requiere apostilla.',         '2026-10-05', '2026-10-05', 'ENVIADA', TRUE,  '2026-10-06'),
(12, 9,  NULL, 'CORREO',          'VALIDACION',   'joaquin.herrera@mail.com', 'Valida tu correo',                  'Ingresa al enlace para validar tu correo.',           '2026-11-08', NULL,         'FALLIDA', FALSE, NULL),
(13, 9,  NULL, 'CORREO',          'VALIDACION',   'joaquin.herrera@mail.com', 'Valida tu correo (reintento)',      'Ingresa al enlace para validar tu correo.',           '2026-11-11', NULL,         'PENDIENTE', FALSE, NULL),
(14, 10, NULL, 'BANDEJA_SISTEMA', 'CARNE',        'diego.diaz@mail.com',      'Tu carné de postulante está listo', 'Aula M-105, Sede Miraflores.',                        '2026-10-05', '2026-10-05', 'ENVIADA', TRUE,  '2026-10-05');

-- =====================================================================
-- DATOS ADICIONALES (volumen) - generados con semilla fija, coherentes
-- con el bloque anterior. IDs continúan desde los del bloque manual.
-- =====================================================================

-- Facultades adicionales
INSERT INTO facultad (id, codigo, nombre, activo) VALUES
(6, 'FLCH', 'Facultad de Letras y Ciencias Humanas', TRUE),
(7, 'FCC', 'Facultad de Ciencias Contables', TRUE),
(8, 'FCAC', 'Facultad de Ciencias y Artes de la Comunicación', TRUE);


-- Carreras adicionales
INSERT INTO carrera (id, facultad_id, codigo_carrera, nombre, activo) VALUES
(9, 1, 'ING-ELE', 'Ingeniería Electrónica', TRUE),
(10, 1, 'ING-MCT', 'Ingeniería Mecatrónica', TRUE),
(11, 1, 'ING-MIN', 'Ingeniería de Minas', TRUE),
(12, 6, 'PSI', 'Psicología', TRUE),
(13, 2, 'SOC', 'Sociología', TRUE),
(14, 7, 'CON', 'Contabilidad', TRUE),
(15, 8, 'COM-AV', 'Comunicación Audiovisual', TRUE),
(16, 8, 'PER', 'Periodismo', TRUE),
(17, 6, 'FIL', 'Filosofía', TRUE);


-- Oferta adicional
INSERT INTO oferta_carrera (id, convocatoria_id, carrera_id, cantidad_vacantes) VALUES
(15, 1, 12, 45),
(16, 1, 14, 40),
(17, 2, 8, 40),
(18, 2, 9, 60),
(19, 2, 10, 35),
(20, 2, 11, 50),
(21, 2, 12, 60),
(22, 2, 13, 60),
(23, 2, 14, 45),
(24, 2, 15, 60),
(25, 2, 16, 60),
(26, 2, 17, 40),
(27, 3, 12, 40);


-- Instituciones adicionales
INSERT INTO institucion_educativa (id, pais_id, codigo_externo, nombre, tipo_institucion, activo) VALUES
(8, 1, '0512347', 'I.E. Mariscal Ramón Castilla', 'COLEGIO', TRUE),
(9, 1, '0623458', 'Colegio Nuestra Señora del Carmen', 'COLEGIO', TRUE),
(10, 1, '0734569', 'I.E. Túpac Amaru II', 'COLEGIO', TRUE),
(11, 1, '0845670', 'Colegio San Francisco de Asís', 'COLEGIO', TRUE),
(12, 1, '0956781', 'I.E. Ricardo Palma', 'COLEGIO', TRUE),
(13, 1, '1067892', 'Colegio Santa María Reina', 'COLEGIO', TRUE),
(14, 1, '1178903', 'I.E. César Vallejo', 'COLEGIO', TRUE),
(15, 1, '1289014', 'Colegio Cristo Rey', 'COLEGIO', TRUE),
(16, 1, '1390125', 'I.E. Micaela Bastidas', 'COLEGIO', TRUE),
(17, 1, '1401236', 'Colegio San José Obrero', 'COLEGIO', TRUE),
(18, 1, '1512347', 'I.E. Inca Garcilaso de la Vega', 'COLEGIO', TRUE),
(19, 1, '1623458', 'Colegio Virgen de Fátima', 'COLEGIO', TRUE),
(20, 2, 'COL-CO-0871', 'Colegio Distrital La Candelaria', 'COLEGIO', TRUE),
(21, 3, 'COL-VE-0452', 'U.E. Simón Bolívar', 'COLEGIO', TRUE),
(22, 4, 'COL-CL-1290', 'Liceo Bicentenario Valparaíso', 'COLEGIO', TRUE),
(23, 1, 'UNIV-PE-001', 'Universidad Nacional Mayor de San Marcos', 'UNIVERSIDAD', TRUE),
(24, 1, 'UNIV-PE-012', 'Universidad Nacional Agraria La Molina', 'UNIVERSIDAD', TRUE),
(25, 1, 'UNIV-PE-031', 'Universidad de Lima', 'UNIVERSIDAD', TRUE),
(26, 1, 'UNIV-PE-008', 'Universidad Nacional de San Agustín de Arequipa', 'UNIVERSIDAD', TRUE),
(27, 2, 'UNIV-CO-024', 'Universidad de Antioquia', 'UNIVERSIDAD', TRUE),
(28, 4, 'UNIV-CL-002', 'Universidad de Chile', 'UNIVERSIDAD', TRUE),
(29, 3, 'UNIV-VE-017', 'Universidad del Zulia', 'UNIVERSIDAD', TRUE),
(30, 1, '1734569', 'Colegio Los Pinos', 'COLEGIO', FALSE);


-- Personas: 17-20 evaluadores, 21-90 postulantes, 91-109 apoderados
INSERT INTO persona (id, nombres, apellido_paterno, apellido_materno, correo, tipo_documento, numero_documento, telefono) VALUES
(17, 'Rocío del Pilar', 'Medina', 'Alarcón', 'rocio.medina@admitu.edu.pe', 'DNI', '45285358', '986825976'),
(18, 'Fernando José', 'Cáceres', 'Lozano', 'fernando.caceres@admitu.edu.pe', 'DNI', '43297832', '923411470'),
(19, 'Gladys Marleny', 'Condori', 'Apaza', 'gladys.condori@admitu.edu.pe', 'DNI', '41263721', '938761920'),
(20, 'Hugo César', 'Zevallos', 'Pinto', 'hugo.zevallos@admitu.edu.pe', 'DNI', '41452374', '921309490'),
(21, 'Andrea', 'Pacheco', 'Urdaneta', 'andrea.pacheco@mail.com', 'CE', '002737040', '956285000'),
(22, 'Kevin', 'Araya', 'Muñoz', 'kevin.araya@mail.com', 'CE', '008323561', '901863351'),
(23, 'Isabella', 'Morales', 'Romero', 'isabella.morales@mail.com', 'DNI', '79187932', '902680308'),
(24, 'Luciana', 'Salas', 'Vargas', 'luciana.salas@mail.com', 'DNI', '76481387', '900482845'),
(25, 'Juan Pablo', 'Lozano', 'Ríos', 'juan.lozano@mail.com', 'DNI', '78487100', '963506620'),
(26, 'Juan Pablo', 'Medina', 'Vargas', 'juan.medina@mail.com', 'DNI', '76621938', '936966418'),
(27, 'Matías', 'Ruiz', 'Zevallos', 'matias.ruiz@mail.com', 'DNI', '77199626', '929225925'),
(28, 'Rodrigo', 'Zevallos', 'Condori', 'rodrigo.zevallos@mail.com', 'DNI', '75978002', '926928647'),
(29, 'Rodrigo', 'Romero', 'Cáceres', 'rodrigo.romero@mail.com', 'DNI', '76470390', '917221269'),
(30, 'Bruno', 'Tello', 'Flores', 'bruno.tello@mail.com', 'DNI', '74956210', '907535607'),
(31, 'Valentina', 'Valdivia', 'Zevallos', 'valentina.valdivia@mail.com', 'DNI', '75777329', '914851821'),
(32, 'Carlos Eduardo', 'Montoya', 'Soto', 'carlos.montoya@mail.com', 'DNI', '75329437', '960552243'),
(33, 'Daniela', 'Mamani', 'Ortiz', 'daniela.mamani@mail.com', 'DNI', '75385071', '957165132'),
(34, 'Renato', 'Soto', 'Tello', 'renato.soto@mail.com', 'DNI', '79593458', '958372288'),
(35, 'Adriana', 'Medina', 'Vargas', 'adriana.medina@mail.com', 'DNI', '73543594', '954442932'),
(36, 'Emilio', 'Ortiz', 'Castillo', 'emilio.ortiz@mail.com', 'DNI', '74730379', '900409986'),
(37, 'Josué', 'Sepúlveda', 'Riquelme', 'josue.sepulveda@mail.com', 'PAS', 'F7105397', '974178446'),
(38, 'Gabriela', 'Reyes', 'Campos', 'gabriela.reyes@mail.com', 'DNI', '75216934', '922960291'),
(39, 'Santiago', 'Sepúlveda', 'Valenzuela', 'santiago.sepulveda@mail.com', 'PAS', 'F5865338', '999197240'),
(40, 'Alessandra', 'Ruiz', 'Sánchez', 'alessandra.ruiz@mail.com', 'DNI', '76945366', '948993077'),
(41, 'Piero', 'Vásquez', 'Tello', 'piero.vasquez@mail.com', 'DNI', '73302618', '991199777'),
(42, 'Luciana', 'Vásquez', 'Arias', 'luciana.vasquez@mail.com', 'DNI', '71715238', '938608653'),
(43, 'Carolina', 'Vásquez', 'Alarcón', 'carolina.vasquez@mail.com', 'DNI', '70483364', '948176366'),
(44, 'Marco Antonio', 'Aguilar', 'Espinoza', 'marco.aguilar@mail.com', 'DNI', '75978284', '933215700'),
(45, 'Christian', 'Condori', 'Mamani', 'christian.condori@mail.com', 'DNI', '75577094', '992559675'),
(46, 'Franco', 'Gutiérrez', 'Valdivia', 'franco.gutierrez@mail.com', 'DNI', '78098643', '952846395'),
(47, 'Juan Pablo', 'Ruiz', 'Morales', 'juan.ruiz@mail.com', 'DNI', '76157788', '988669991'),
(48, 'Andrea', 'Rodríguez', 'Navarro', 'andrea.rodriguez@mail.com', 'DNI', '75067420', '977762651'),
(49, 'Nicole', 'Ríos', 'Campos', 'nicole.rios@mail.com', 'DNI', '72303056', '963131584'),
(50, 'Franco', 'Soto', 'Pérez', 'franco.soto@mail.com', 'DNI', '77225993', '963845135'),
(51, 'Nicole', 'Cáceres', 'Ríos', 'nicole.caceres@mail.com', 'DNI', '76093542', '992846879'),
(52, 'Alejandro', 'Arias', 'Vásquez', 'alejandro.arias@mail.com', 'DNI', '71586321', '902201337'),
(53, 'Renata', 'Palomino', 'Cáceres', 'renata.palomino@mail.com', 'DNI', '77382874', '972986843'),
(54, 'Christian', 'Rodríguez', 'Zevallos', 'christian.rodriguez@mail.com', 'DNI', '73772312', '919809406'),
(55, 'Santiago', 'Huamán', 'Salas', 'santiago.huaman@mail.com', 'DNI', '78123211', '921347901'),
(56, 'Franco', 'Sánchez', 'Flores', 'franco.sanchez@mail.com', 'DNI', '76467476', '923137741'),
(57, 'Sofía', 'Ortiz', 'Condori', 'sofia.ortiz@mail.com', 'DNI', '74954511', '930476168'),
(58, 'Gabriel', 'Soto', 'Cárdenas', 'gabriel.soto@mail.com', 'DNI', '73836206', '962296905'),
(59, 'Alejandro', 'Ramos', 'Zevallos', 'alejandro.ramos@mail.com', 'DNI', '70424892', '914483636'),
(60, 'Mariana', 'Chávez', 'Cáceres', 'mariana.chavez@mail.com', 'DNI', '78206804', '902793093'),
(61, 'Micaela', 'Aguilar', 'Espinoza', 'micaela.aguilar@mail.com', 'DNI', '73893216', '900302573'),
(62, 'Renato', 'Yupanqui', 'Navarro', 'renato.yupanqui@mail.com', 'DNI', '74454303', '904466383'),
(63, 'Jimena', 'Aguilar', 'Campos', 'jimena.aguilar@mail.com', 'DNI', '78487452', '930046501'),
(64, 'Adriana', 'Briceño', 'Rangel', 'adriana.briceno@mail.com', 'CE', '006953163', '957390354'),
(65, 'Renato', 'Medina', 'Campos', 'renato.medina@mail.com', 'DNI', '77186462', '996732473'),
(66, 'Matías', 'Vargas', 'Ugarte', 'matias.vargas@mail.com', 'DNI', '71828953', '942357661'),
(67, 'Luis Fernando', 'Romero', 'Valdivia', 'luis.romero@mail.com', 'DNI', '77656672', '991600618'),
(68, 'Renata', 'Mamani', 'Medina', 'renata.mamani@mail.com', 'DNI', '71240900', '935784694'),
(69, 'Diana Carolina', 'Romero', 'Ramos', 'diana.romero@mail.com', 'DNI', '77748347', '986034033'),
(70, 'Brenda', 'Gutiérrez', 'Ruiz', 'brenda.gutierrez@mail.com', 'DNI', '74373440', '988284615'),
(71, 'Renato', 'Aguilar', 'León', 'renato.aguilar@mail.com', 'DNI', '73314292', '984733606'),
(72, 'Gonzalo', 'Cáceres', 'Montoya', 'gonzalo.caceres@mail.com', 'DNI', '70019045', '953403899'),
(73, 'Santiago', 'Salas', 'Yupanqui', 'santiago.salas@mail.com', 'DNI', '72863752', '903927463'),
(74, 'Jimena', 'Huamán', 'Mamani', 'jimena.huaman@mail.com', 'DNI', '75203371', '928879305'),
(75, 'Luciana', 'Pinto', 'Lozano', 'luciana.pinto@mail.com', 'DNI', '75662634', '995897512'),
(76, 'Daniel', 'Cárdenas', 'Espinoza', 'daniel.cardenas@mail.com', 'DNI', '77592809', '946250115'),
(77, 'Mariana', 'Mamani', 'Ortiz', 'mariana.mamani@mail.com', 'DNI', '77297861', NULL),
(78, 'Isabella', 'Delgado', 'Reyes', 'isabella.delgado@mail.com', 'DNI', '76861344', '900197690'),
(79, 'Kiara', 'Tello', 'Huamán', 'kiara.tello@mail.com', 'DNI', '78682416', '900444713'),
(80, 'Alessandra', 'Navarro', 'Paucar', 'alessandra.navarro@mail.com', 'DNI', '78224050', '977057175'),
(81, 'Micaela', 'León', 'Zevallos', 'micaela.leon@mail.com', 'DNI', '70833615', '932077838'),
(82, 'Kiara', 'Medina', 'Huamán', 'kiara.medina@mail.com', 'DNI', '72503649', '914412680'),
(83, 'Juan Pablo', 'Paucar', 'Valdivia', 'juan.paucar@mail.com', 'DNI', '72694102', '922718045'),
(84, 'Franco', 'Espinoza', 'Flores', 'franco.espinoza@mail.com', 'DNI', '75184413', '976733464'),
(85, 'Franco', 'Sepúlveda', 'Araya', 'franco.sepulveda@mail.com', 'PAS', 'F2841900', '947867847'),
(86, 'Luciana', 'Aguilar', 'Sánchez', 'luciana.aguilar@mail.com', 'DNI', '73747054', '958931830'),
(87, 'Ana Lucía', 'Condori', 'Medina', 'ana.condori@mail.com', 'DNI', '76684130', '966198644'),
(88, 'Kevin', 'Ríos', 'Zevallos', 'kevin.rios@mail.com', 'DNI', '78391631', '990252705'),
(89, 'Sofía', 'Salas', 'Campos', 'sofia.salas@mail.com', 'DNI', '70073195', '933067434'),
(90, 'Kiara', 'Rodríguez', 'Morales', 'kiara.rodriguez@mail.com', 'DNI', '77280842', '944380279'),
(91, 'Alberto', 'Condori', 'Ramos', 'alberto.condori@mail.com', 'DNI', '40764303', '920801573'),
(92, 'César', 'Mamani', 'Sánchez', 'cesar.mamani@mail.com', 'DNI', '40677170', '918757282'),
(93, 'Pedro', 'Vásquez', 'Chávez', 'pedro.vasquez@mail.com', 'DNI', '41562426', '994444296'),
(94, 'Liliana', 'Espinoza', 'Ugarte', 'liliana.espinoza@mail.com', 'DNI', '46983246', '943960426'),
(95, 'Félix', 'Yupanqui', 'Cárdenas', 'felix.yupanqui@mail.com', 'DNI', '46927544', '912775754'),
(96, 'Liliana', 'Campos', 'Bustamante', 'liliana.campos@mail.com', 'DNI', '47454379', '961812554'),
(97, 'Edwin', 'Huamán', 'Zevallos', 'edwin.huaman@mail.com', 'DNI', '40122189', '961729755'),
(98, 'Norma', 'Navarro', 'León', 'norma.navarro@mail.com', 'DNI', '43719205', '999176681'),
(99, 'Norma', 'Cáceres', 'Paucar', 'norma.caceres@mail.com', 'DNI', '43387385', '947558087'),
(100, 'Pedro', 'Yupanqui', 'Rodríguez', 'pedro.yupanqui@mail.com', 'DNI', '42925531', '937659773'),
(101, 'Elena', 'Campos', 'Ruiz', 'elena.campos@mail.com', 'DNI', '43386562', '928592065'),
(102, 'Edwin', 'Briceño', 'Palomino', 'edwin.briceno@mail.com', 'DNI', '42614718', '990732764'),
(103, 'Víctor', 'Medina', 'Ríos', 'victor.medina@mail.com', 'DNI', '42750901', '983473748'),
(104, 'Manuel', 'Romero', 'Condori', 'manuel.romero@mail.com', 'DNI', '45540418', '920361915'),
(105, 'Elena', 'Medina', 'Bustamante', 'elena.medina@mail.com', 'DNI', '40243050', '933431737'),
(106, 'Patricia', 'Ramos', 'Castillo', 'patricia.ramos@mail.com', 'DNI', '42705250', '947651876'),
(107, 'Edwin', 'Delgado', 'Huamán', 'edwin.delgado@mail.com', 'DNI', '49789490', '941000245'),
(108, 'Edwin', 'León', 'Vásquez', 'edwin.leon@mail.com', 'DNI', '40936544', '987681923'),
(109, 'Rosa', 'Zevallos', 'Aguilar', 'rosa.zevallos@mail.com', 'DNI', '49156317', '980128183');


-- Evaluadores adicionales
INSERT INTO evaluador (persona_id, cargo, activo) VALUES
(17, 'Evaluadora documentaria', TRUE),
(18, 'Evaluador documentario', TRUE),
(19, 'Asistente de admisión', TRUE),
(20, 'Evaluador documentario', TRUE);


-- Apoderados de los postulantes menores de edad
INSERT INTO apoderado (persona_id, parentesco, activo) VALUES
(91, 'TUTOR', TRUE),
(92, 'PADRE', TRUE),
(93, 'PADRE', TRUE),
(94, 'MADRE', TRUE),
(95, 'TUTOR', TRUE),
(96, 'MADRE', TRUE),
(97, 'PADRE', TRUE),
(98, 'TUTOR', TRUE),
(99, 'MADRE', TRUE),
(100, 'PADRE', TRUE),
(101, 'MADRE', TRUE),
(102, 'PADRE', TRUE),
(103, 'PADRE', TRUE),
(104, 'PADRE', TRUE),
(105, 'MADRE', TRUE),
(106, 'MADRE', TRUE),
(107, 'PADRE', TRUE),
(108, 'PADRE', TRUE),
(109, 'MADRE', TRUE);


-- Postulantes adicionales
INSERT INTO postulante (persona_id, apoderado_id, fecha_nacimiento, tiene_discapacidad, numero_carnet_conadis, correo_validado, fecha_validacion_correo) VALUES
(21, NULL, '2003-10-06', FALSE, NULL, TRUE, '2026-09-20'),
(22, NULL, '2008-02-08', FALSE, NULL, TRUE, '2026-10-05'),
(23, NULL, '2007-11-18', FALSE, NULL, TRUE, '2026-10-17'),
(24, NULL, '2008-03-05', FALSE, NULL, TRUE, '2026-09-10'),
(25, NULL, '2007-09-06', FALSE, NULL, TRUE, '2026-09-10'),
(26, 91, '2009-04-15', TRUE, 'CONADIS-0718435', FALSE, NULL),
(27, NULL, '1997-08-07', TRUE, 'CONADIS-0857260', TRUE, '2026-10-17'),
(28, NULL, '2008-02-21', FALSE, NULL, TRUE, '2026-09-17'),
(29, NULL, '2007-07-03', FALSE, NULL, TRUE, '2026-10-29'),
(30, NULL, '2006-08-12', FALSE, NULL, TRUE, '2026-09-11'),
(31, NULL, '2007-06-20', FALSE, NULL, TRUE, '2026-10-04'),
(32, NULL, '2006-08-22', FALSE, NULL, TRUE, '2026-09-06'),
(33, 92, '2009-08-15', FALSE, NULL, TRUE, '2026-09-28'),
(34, NULL, '2007-07-07', FALSE, NULL, TRUE, '2026-09-24'),
(35, 91, '2009-07-14', FALSE, NULL, TRUE, '2026-10-18'),
(36, NULL, '2007-05-17', FALSE, NULL, TRUE, '2026-10-12'),
(37, NULL, '1995-02-19', FALSE, NULL, TRUE, '2026-09-23'),
(38, NULL, '1996-01-26', FALSE, NULL, TRUE, '2026-10-02'),
(39, NULL, '2007-02-20', FALSE, NULL, TRUE, '2026-10-06'),
(40, NULL, '2005-05-14', FALSE, NULL, TRUE, '2026-11-01'),
(41, 93, '2009-05-24', FALSE, NULL, TRUE, '2026-10-05'),
(42, NULL, '2003-05-21', TRUE, 'CONADIS-0746049', TRUE, '2026-09-06'),
(43, NULL, '2000-12-27', FALSE, NULL, TRUE, '2026-10-15'),
(44, 94, '2008-12-05', TRUE, 'CONADIS-0384142', TRUE, '2026-10-27'),
(45, 95, '2008-12-03', FALSE, NULL, TRUE, '2026-09-24'),
(46, NULL, '1996-06-10', FALSE, NULL, TRUE, '2026-10-03'),
(47, NULL, '2007-05-25', FALSE, NULL, TRUE, '2026-10-01'),
(48, NULL, '2005-05-02', FALSE, NULL, TRUE, '2026-09-08'),
(49, 96, '2009-08-23', FALSE, NULL, TRUE, '2026-09-09'),
(50, NULL, '2003-12-26', FALSE, NULL, TRUE, '2026-09-26'),
(51, NULL, '1994-09-01', FALSE, NULL, TRUE, '2026-09-30'),
(52, NULL, '2006-08-03', FALSE, NULL, TRUE, '2026-09-03'),
(53, NULL, '2006-11-16', FALSE, NULL, TRUE, '2026-10-17'),
(54, NULL, '2003-10-29', FALSE, NULL, TRUE, '2026-10-21'),
(55, 97, '2010-02-15', FALSE, NULL, TRUE, '2026-10-26'),
(56, 98, '2009-01-16', FALSE, NULL, TRUE, '2026-09-20'),
(57, NULL, '2007-09-16', FALSE, NULL, TRUE, '2026-10-02'),
(58, NULL, '2007-09-17', FALSE, NULL, TRUE, '2026-10-09'),
(59, NULL, '2008-01-18', FALSE, NULL, TRUE, '2026-10-09'),
(60, 99, '2009-07-02', FALSE, NULL, TRUE, '2026-10-16'),
(61, 94, '2010-03-13', FALSE, NULL, TRUE, '2026-10-11'),
(62, 100, '2009-04-04', TRUE, 'CONADIS-0423042', TRUE, '2026-10-28'),
(63, 101, '2009-02-05', FALSE, NULL, TRUE, '2026-09-21'),
(64, 102, '2009-07-10', FALSE, NULL, TRUE, '2026-09-28'),
(65, 103, '2009-10-02', FALSE, NULL, TRUE, '2026-10-03'),
(66, NULL, '2006-10-28', FALSE, NULL, TRUE, '2026-10-08'),
(67, 104, '2009-11-30', FALSE, NULL, TRUE, '2026-10-22'),
(68, 105, '2009-03-14', FALSE, NULL, TRUE, '2026-11-06'),
(69, 106, '2008-05-09', FALSE, NULL, TRUE, '2026-10-01'),
(70, NULL, '2007-12-18', FALSE, NULL, TRUE, '2026-11-01'),
(71, NULL, '2006-09-07', FALSE, NULL, TRUE, '2026-09-11'),
(72, NULL, '2006-06-18', FALSE, NULL, TRUE, '2026-10-31'),
(73, NULL, '2003-08-27', FALSE, NULL, TRUE, '2026-10-05'),
(74, NULL, '2008-03-30', FALSE, NULL, TRUE, '2026-09-15'),
(75, NULL, '2007-06-30', FALSE, NULL, TRUE, '2026-09-12'),
(76, NULL, '2008-02-02', FALSE, NULL, TRUE, '2026-04-21'),
(77, 92, '2010-01-03', FALSE, NULL, TRUE, '2026-04-20'),
(78, 107, '2008-10-23', FALSE, NULL, TRUE, '2026-05-31'),
(79, NULL, '2008-03-08', FALSE, NULL, TRUE, '2026-06-09'),
(80, NULL, '2008-03-24', FALSE, NULL, TRUE, '2026-04-03'),
(81, 108, '2009-01-15', FALSE, NULL, TRUE, '2026-04-04'),
(82, NULL, '2008-03-18', FALSE, NULL, TRUE, '2026-05-23'),
(83, NULL, '2006-06-06', FALSE, NULL, TRUE, '2026-05-22'),
(84, NULL, '2008-02-24', FALSE, NULL, TRUE, '2026-04-21'),
(85, NULL, '1998-06-21', FALSE, NULL, TRUE, '2026-06-15'),
(86, NULL, '2001-10-08', FALSE, NULL, TRUE, '2026-04-02'),
(87, NULL, '2006-04-18', FALSE, NULL, TRUE, '2026-04-24'),
(88, 109, '2009-04-17', FALSE, NULL, TRUE, '2026-04-04'),
(89, NULL, '2007-11-17', FALSE, NULL, TRUE, '2026-05-27'),
(90, NULL, '2007-07-21', FALSE, NULL, TRUE, '2026-04-10');


-- Antecedentes académicos
INSERT INTO antecedente_academico (id, postulante_id, institucion_id, anio_inicio, anio_fin, descripcion, activo) VALUES
(9, 21, 21, 2016, 2020, 'Secundaria completa', TRUE),
(10, 21, 7, 2021, 2026, 'Ingeniería Civil - 10 semestres aprobados', TRUE),
(11, 22, 22, 2021, 2025, 'Secundaria completa', TRUE),
(12, 23, 15, 2020, 2024, 'Secundaria completa', TRUE),
(13, 24, 3, 2021, 2025, 'Secundaria completa', TRUE),
(14, 25, 18, 2020, 2024, 'Secundaria completa', TRUE),
(15, 26, 18, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(16, 27, 10, 2010, 2014, 'Secundaria completa', TRUE),
(17, 27, 26, 2015, 2020, 'Titulado en Contabilidad', TRUE),
(18, 28, 12, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(19, 29, 19, 2020, 2024, 'Secundaria completa', TRUE),
(20, 30, 16, 2019, 2023, 'Secundaria completa', TRUE),
(21, 31, 3, 2020, 2024, 'Secundaria completa', TRUE),
(22, 32, 3, 2019, 2023, 'Secundaria completa', TRUE),
(23, 33, 10, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(24, 34, 3, 2020, 2024, 'Secundaria completa', TRUE),
(25, 35, 13, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(26, 36, 17, 2020, 2024, 'Secundaria completa', TRUE),
(27, 37, 22, 2008, 2012, 'Secundaria completa', TRUE),
(28, 37, 28, 2013, 2018, 'Bachiller en Ingeniería Industrial', TRUE),
(29, 38, 18, 2009, 2013, 'Secundaria completa', TRUE),
(30, 38, 23, 2014, 2019, 'Bachiller en Contabilidad', TRUE),
(31, 39, 22, 2020, 2024, 'Secundaria completa', TRUE),
(32, 40, 13, 2018, 2022, 'Secundaria completa', TRUE),
(33, 40, 25, 2023, 2026, 'Biología - 6 semestres aprobados', TRUE),
(34, 41, 2, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(35, 42, 2, 2016, 2020, 'Secundaria completa', TRUE),
(36, 42, 23, 2021, 2026, 'Ingeniería de Sistemas - 10 semestres aprobados', TRUE),
(37, 43, 10, 2013, 2017, 'Secundaria completa', TRUE),
(38, 43, 24, 2018, 2023, 'Bachiller en Ingeniería de Sistemas', TRUE),
(39, 44, 14, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(40, 45, 18, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(41, 46, 9, 2009, 2013, 'Secundaria completa', TRUE),
(42, 46, 25, 2014, 2019, 'Titulado en Biología', TRUE),
(43, 47, 1, 2020, 2024, 'Secundaria completa', TRUE),
(44, 48, 2, 2018, 2022, 'Secundaria completa', TRUE),
(45, 48, 23, 2023, 2026, 'Administración - 6 semestres aprobados', TRUE),
(46, 49, 3, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(47, 50, 9, 2016, 2020, 'Secundaria completa', TRUE),
(48, 50, 23, 2021, 2026, 'Psicología - 10 semestres aprobados', TRUE),
(49, 51, 2, 2007, 2011, 'Secundaria completa', TRUE),
(50, 51, 24, 2012, 2017, 'Titulado en Biología', TRUE),
(51, 52, 2, 2019, 2023, 'Secundaria completa', TRUE),
(52, 53, 9, 2019, 2023, 'Secundaria completa', TRUE),
(53, 54, 11, 2016, 2020, 'Secundaria completa', TRUE),
(54, 54, 24, 2021, 2026, 'Ingeniería de Sistemas - 10 semestres aprobados', TRUE),
(55, 55, 2, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(56, 56, 2, 2022, 2026, 'Secundaria - cursando 5.° año - tercio superior', TRUE),
(57, 57, 14, 2020, 2024, 'Secundaria completa', TRUE),
(58, 58, 2, 2020, 2024, 'Secundaria completa', TRUE),
(59, 59, 19, 2021, 2025, 'Secundaria completa', TRUE),
(60, 60, 19, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(61, 61, 18, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(62, 62, 9, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(63, 63, 13, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(64, 64, 21, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(65, 65, 17, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(66, 66, 9, 2019, 2023, 'Secundaria completa', TRUE),
(67, 67, 17, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(68, 68, 16, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(69, 69, 8, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(70, 70, 19, 2020, 2024, 'Secundaria completa', TRUE),
(71, 71, 16, 2019, 2023, 'Secundaria completa', TRUE),
(72, 72, 17, 2019, 2023, 'Secundaria completa', TRUE),
(73, 73, 2, 2016, 2020, 'Secundaria completa', TRUE),
(74, 73, 5, 2021, 2026, 'Ingeniería de Sistemas - 10 semestres aprobados', TRUE),
(75, 74, 9, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(76, 75, 17, 2020, 2024, 'Secundaria completa', TRUE),
(77, 76, 11, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(78, 77, 14, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(79, 78, 14, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(80, 79, 15, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(81, 80, 17, 2021, 2025, 'Secundaria completa', TRUE),
(82, 81, 14, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(83, 82, 18, 2021, 2025, 'Secundaria completa', TRUE),
(84, 83, 3, 2019, 2023, 'Secundaria completa', TRUE),
(85, 84, 17, 2021, 2025, 'Secundaria completa - tercio superior', TRUE),
(86, 85, 22, 2011, 2015, 'Secundaria completa', TRUE),
(87, 85, 28, 2016, 2021, 'Titulado en Psicología', TRUE),
(88, 86, 13, 2014, 2018, 'Secundaria completa', TRUE),
(89, 86, 25, 2019, 2024, 'Titulado en Biología', TRUE),
(90, 87, 19, 2019, 2023, 'Secundaria completa', TRUE),
(91, 88, 1, 2022, 2026, 'Secundaria - cursando 5.° año', TRUE),
(92, 89, 19, 2020, 2024, 'Secundaria completa', TRUE),
(93, 90, 13, 2020, 2024, 'Secundaria completa', TRUE);


-- Inscritos por convocatoria
INSERT INTO convocatoria_postulante (convocatoria_id, postulante_id) VALUES
(1, 76),
(1, 77),
(1, 78),
(1, 79),
(1, 80),
(1, 81),
(1, 82),
(1, 83),
(1, 84),
(1, 85),
(1, 86),
(1, 87),
(1, 88),
(1, 89),
(1, 90),
(2, 21),
(2, 22),
(2, 23),
(2, 24),
(2, 25),
(2, 26),
(2, 27),
(2, 28),
(2, 29),
(2, 30),
(2, 31),
(2, 32),
(2, 33),
(2, 34),
(2, 35),
(2, 36),
(2, 37),
(2, 38),
(2, 39),
(2, 40),
(2, 41),
(2, 42),
(2, 43),
(2, 44),
(2, 45),
(2, 46),
(2, 47),
(2, 48),
(2, 49),
(2, 50),
(2, 51),
(2, 52),
(2, 53),
(2, 54),
(2, 55),
(2, 56),
(2, 57),
(2, 58),
(2, 59),
(2, 60),
(2, 61),
(2, 62),
(2, 63),
(2, 64),
(2, 65),
(2, 66),
(2, 67),
(2, 68),
(2, 69),
(2, 70),
(2, 71),
(2, 72),
(2, 73),
(2, 74),
(2, 75),
(2, 76),
(2, 77),
(2, 78),
(2, 79),
(2, 80),
(2, 81),
(2, 82),
(2, 83),
(2, 84),
(2, 85);


-- Postulaciones adicionales
INSERT INTO postulacion (id, postulante_id, convocatoria_id, modalidad_elegida_id, carrera_elegida_id, estado_actual_id, fecha_registro, fecha_envio, fecha_finalizacion, codigo_inscripcion, observacion_general) VALUES
(11, 86, 1, 3, 3, 6, '2026-04-02', '2026-04-05', '2026-04-28', 'INS-2026-2-0005', NULL),
(12, 80, 1, 2, 2, 6, '2026-04-03', '2026-04-03', '2026-05-02', 'INS-2026-2-0003', NULL),
(13, 81, 1, 1, 4, 6, '2026-04-04', '2026-04-07', '2026-04-23', 'INS-2026-2-0006', NULL),
(14, 88, 1, 1, 15, 6, '2026-04-04', '2026-04-04', '2026-04-28', 'INS-2026-2-0004', NULL),
(15, 90, 1, 2, 2, 6, '2026-04-10', '2026-04-11', '2026-04-30', 'INS-2026-2-0007', NULL),
(16, 77, 1, 1, 4, 6, '2026-04-20', '2026-04-22', '2026-05-08', 'INS-2026-2-0008', NULL),
(17, 76, 1, 2, 16, 7, '2026-04-21', '2026-04-23', NULL, 'INS-2026-2-0009', 'Rechazada por documento no subsanado'),
(18, 84, 1, 2, 2, 6, '2026-04-21', '2026-04-23', '2026-06-06', 'INS-2026-2-0010', NULL),
(19, 87, 1, 2, 16, 6, '2026-04-24', '2026-04-27', '2026-06-18', 'INS-2026-2-0011', NULL),
(20, 83, 1, 2, 4, 8, '2026-05-22', '2026-05-24', NULL, 'INS-2026-2-0012', 'Anulada: pago no realizado en plazo'),
(21, 82, 1, 2, 1, 6, '2026-05-23', '2026-05-24', '2026-06-16', 'INS-2026-2-0013', NULL),
(22, 89, 1, 2, 3, 8, '2026-05-27', '2026-05-29', NULL, 'INS-2026-2-0014', 'Anulada: pago no realizado en plazo'),
(23, 78, 1, 2, 15, 7, '2026-05-31', '2026-06-02', NULL, 'INS-2026-2-0015', 'Rechazada por documento no subsanado'),
(24, 79, 1, 2, 15, 6, '2026-06-09', '2026-06-12', '2026-06-29', 'INS-2026-2-0016', NULL),
(25, 85, 1, 3, 16, 6, '2026-06-15', '2026-06-15', '2026-06-29', 'INS-2026-2-0017', NULL),
(26, 79, 2, 6, 26, 6, '2026-09-01', '2026-09-02', '2026-10-03', 'INS-2027-1-0008', 'Segunda postulación del postulante'),
(27, 52, 2, 5, 18, 6, '2026-09-03', '2026-09-05', '2026-09-21', 'INS-2027-1-0009', NULL),
(28, 77, 2, 4, 23, 6, '2026-09-04', '2026-09-05', '2026-10-06', 'INS-2027-1-0010', 'Segunda postulación del postulante'),
(29, 76, 2, 6, 8, 5, '2026-09-05', '2026-09-08', NULL, 'INS-2027-1-0011', 'Segunda postulación del postulante'),
(30, 32, 2, 5, 6, 6, '2026-09-06', '2026-09-09', '2026-10-24', 'INS-2027-1-0012', NULL),
(31, 42, 2, 7, 6, 5, '2026-09-06', '2026-09-09', NULL, 'INS-2027-1-0013', 'Requiere aula accesible'),
(32, 48, 2, 7, 9, 6, '2026-09-08', '2026-09-09', '2026-10-14', 'INS-2027-1-0014', NULL),
(33, 49, 2, 4, 8, 6, '2026-09-09', '2026-09-11', '2026-10-30', 'INS-2027-1-0016', NULL),
(34, 81, 2, 4, 11, 6, '2026-09-09', '2026-09-11', '2026-11-02', 'INS-2027-1-0017', 'Segunda postulación del postulante'),
(35, 24, 2, 5, 25, 8, '2026-09-10', '2026-09-10', NULL, 'INS-2027-1-0015', 'Anulada: pago no realizado en plazo'),
(36, 25, 2, 5, 26, 6, '2026-09-10', '2026-09-12', '2026-10-22', 'INS-2027-1-0019', NULL),
(37, 30, 2, 5, 19, 6, '2026-09-11', '2026-09-11', '2026-10-07', 'INS-2027-1-0018', NULL),
(38, 71, 2, 5, 26, 8, '2026-09-11', '2026-09-14', NULL, 'INS-2027-1-0021', 'Anulada: pago no realizado en plazo'),
(39, 75, 2, 5, 17, 6, '2026-09-12', '2026-09-12', '2026-11-02', 'INS-2027-1-0020', NULL),
(40, 78, 2, 6, 5, 7, '2026-09-14', '2026-09-16', NULL, 'INS-2027-1-0023', 'Rechazada por documento no subsanado'),
(41, 74, 2, 6, 10, 6, '2026-09-15', '2026-09-15', '2026-10-06', 'INS-2027-1-0022', NULL),
(42, 28, 2, 6, 24, 8, '2026-09-17', '2026-09-17', NULL, 'INS-2027-1-0024', 'Anulada: pago no realizado en plazo'),
(43, 21, 2, 7, 19, 5, '2026-09-20', '2026-09-22', NULL, 'INS-2027-1-0027', NULL),
(44, 56, 2, 6, 6, 5, '2026-09-20', '2026-09-21', NULL, 'INS-2027-1-0025', NULL),
(45, 63, 2, 4, 26, 6, '2026-09-21', '2026-09-21', '2026-10-08', 'INS-2027-1-0026', NULL),
(46, 37, 2, 8, 6, 6, '2026-09-23', '2026-09-26', '2026-10-25', 'INS-2027-1-0030', NULL),
(47, 34, 2, 5, 7, 5, '2026-09-24', '2026-09-25', NULL, 'INS-2027-1-0028', NULL),
(48, 45, 2, 6, 9, 6, '2026-09-24', '2026-09-25', '2026-10-26', 'INS-2027-1-0029', NULL),
(49, 50, 2, 7, 19, 6, '2026-09-26', '2026-09-26', '2026-10-12', 'INS-2027-1-0031', NULL),
(50, 33, 2, 4, 21, 6, '2026-09-28', '2026-10-01', '2026-10-17', 'INS-2027-1-0033', NULL),
(51, 64, 2, 4, 26, 6, '2026-09-28', '2026-09-30', '2026-11-02', 'INS-2027-1-0032', NULL),
(52, 51, 2, 8, 17, 5, '2026-09-30', '2026-10-02', NULL, 'INS-2027-1-0034', NULL),
(53, 47, 2, 5, 7, 3, '2026-10-01', '2026-10-04', NULL, 'INS-2027-1-0036', NULL),
(54, 69, 2, 6, 24, 3, '2026-10-01', '2026-10-04', NULL, 'INS-2027-1-0037', NULL),
(55, 38, 2, 8, 11, 8, '2026-10-02', '2026-10-04', NULL, 'INS-2027-1-0038', 'Anulada: pago no realizado en plazo'),
(56, 57, 2, 5, 24, 5, '2026-10-02', '2026-10-02', NULL, 'INS-2027-1-0035', NULL),
(57, 46, 2, 8, 26, 5, '2026-10-03', '2026-10-06', NULL, 'INS-2027-1-0041', NULL),
(58, 65, 2, 4, 5, 4, '2026-10-03', '2026-10-04', NULL, 'INS-2027-1-0039', NULL),
(59, 31, 2, 5, 20, 4, '2026-10-04', '2026-10-05', NULL, 'INS-2027-1-0040', NULL),
(60, 22, 2, 5, 20, 7, '2026-10-05', '2026-10-06', NULL, 'INS-2027-1-0042', 'Rechazada por documento no subsanado'),
(61, 41, 2, 4, 23, 3, '2026-10-05', '2026-10-07', NULL, 'INS-2027-1-0045', NULL),
(62, 73, 2, 7, 5, 4, '2026-10-05', '2026-10-06', NULL, 'INS-2027-1-0043', NULL),
(63, 39, 2, 5, 5, 6, '2026-10-06', '2026-10-06', '2026-10-27', 'INS-2027-1-0044', NULL),
(64, 83, 2, 5, 23, 4, '2026-10-06', '2026-10-08', NULL, 'INS-2027-1-0046', 'Segunda postulación del postulante'),
(65, 66, 2, 5, 20, 4, '2026-10-08', '2026-10-10', NULL, 'INS-2027-1-0047', NULL),
(66, 58, 2, 5, 19, 3, '2026-10-09', '2026-10-11', NULL, 'INS-2027-1-0048', NULL),
(67, 59, 2, 5, 23, 4, '2026-10-09', '2026-10-12', NULL, 'INS-2027-1-0049', NULL),
(68, 61, 2, 4, 22, 5, '2026-10-11', '2026-10-13', NULL, 'INS-2027-1-0050', NULL),
(69, 36, 2, 5, 20, 6, '2026-10-12', '2026-10-13', '2026-10-31', 'INS-2027-1-0051', NULL),
(70, 85, 2, 8, 20, 6, '2026-10-14', '2026-10-15', '2026-11-06', 'INS-2027-1-0052', 'Segunda postulación del postulante'),
(71, 43, 2, 8, 8, 8, '2026-10-15', '2026-10-17', NULL, 'INS-2027-1-0053', 'Anulada: pago no realizado en plazo'),
(72, 60, 2, 4, 10, 6, '2026-10-16', '2026-10-18', '2026-11-07', 'INS-2027-1-0054', NULL),
(73, 23, 2, 5, 10, 4, '2026-10-17', '2026-10-20', NULL, 'INS-2027-1-0057', NULL),
(74, 27, 2, 8, 22, 4, '2026-10-17', '2026-10-19', NULL, 'INS-2027-1-0056', 'Requiere aula accesible'),
(75, 53, 2, 5, 17, 6, '2026-10-17', '2026-10-20', '2026-11-08', 'INS-2027-1-0058', NULL),
(76, 35, 2, 4, 25, 3, '2026-10-18', '2026-10-18', NULL, 'INS-2027-1-0055', NULL),
(77, 54, 2, 7, 17, 3, '2026-10-21', '2026-10-24', NULL, 'INS-2027-1-0059', NULL),
(78, 67, 2, 4, 8, 4, '2026-10-22', '2026-10-25', NULL, 'INS-2027-1-0060', NULL),
(79, 55, 2, 4, 17, 2, '2026-10-26', '2026-10-28', NULL, 'INS-2027-1-0061', NULL),
(80, 44, 2, 6, 25, 2, '2026-10-27', '2026-10-28', NULL, 'INS-2027-1-0062', 'Requiere aula accesible'),
(81, 62, 2, 4, 22, 3, '2026-10-28', '2026-10-29', NULL, 'INS-2027-1-0064', 'Requiere aula accesible'),
(82, 80, 2, 5, 24, 2, '2026-10-28', '2026-10-31', NULL, 'INS-2027-1-0065', 'Segunda postulación del postulante'),
(83, 82, 2, 5, 26, 2, '2026-10-28', '2026-10-28', NULL, 'INS-2027-1-0063', 'Segunda postulación del postulante'),
(84, 29, 2, 5, 23, 3, '2026-10-29', '2026-11-01', NULL, 'INS-2027-1-0067', NULL),
(85, 72, 2, 5, 5, 2, '2026-10-31', '2026-10-31', NULL, 'INS-2027-1-0066', NULL),
(86, 26, 2, 4, NULL, 1, '2026-11-01', NULL, NULL, NULL, 'Requiere aula accesible'),
(87, 40, 2, 7, 24, 2, '2026-11-01', '2026-11-04', NULL, 'INS-2027-1-0068', NULL),
(88, 70, 2, 5, 20, 3, '2026-11-01', '2026-11-04', NULL, 'INS-2027-1-0069', NULL),
(89, 68, 2, 4, 19, 1, '2026-11-06', NULL, NULL, NULL, NULL),
(90, 84, 2, 6, 26, 3, '2026-11-06', '2026-11-06', NULL, 'INS-2027-1-0070', 'Segunda postulación del postulante');


-- Historial de estados
INSERT INTO postulacion_historial (id, postulacion_id, estado_anterior_id, estado_actual_id, fecha_cambio, responsable_cambio, motivo_cambio) VALUES
(40, 11, NULL, 1, '2026-04-02', 'luciana.aguilar@mail.com', 'Registro de postulación'),
(41, 11, 1, 2, '2026-04-05', 'luciana.aguilar@mail.com', 'Envío de postulación'),
(42, 11, 2, 3, '2026-04-09', 'SISTEMA', 'Pago aprobado'),
(43, 11, 3, 5, '2026-04-21', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(44, 11, 5, 6, '2026-04-28', 'SISTEMA', 'Carné emitido'),
(45, 12, NULL, 1, '2026-04-03', 'alessandra.navarro@mail.com', 'Registro de postulación'),
(46, 12, 1, 2, '2026-04-03', 'alessandra.navarro@mail.com', 'Envío de postulación'),
(47, 12, 2, 3, '2026-04-06', 'SISTEMA', 'Pago aprobado'),
(48, 12, 3, 5, '2026-04-24', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(49, 12, 5, 6, '2026-05-02', 'SISTEMA', 'Carné emitido'),
(50, 13, NULL, 1, '2026-04-04', 'micaela.leon@mail.com', 'Registro de postulación'),
(51, 13, 1, 2, '2026-04-07', 'micaela.leon@mail.com', 'Envío de postulación'),
(52, 13, 2, 3, '2026-04-08', 'SISTEMA', 'Pago aprobado'),
(53, 13, 3, 5, '2026-04-20', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(54, 13, 5, 6, '2026-04-23', 'SISTEMA', 'Carné emitido'),
(55, 14, NULL, 1, '2026-04-04', 'kevin.rios@mail.com', 'Registro de postulación'),
(56, 14, 1, 2, '2026-04-04', 'kevin.rios@mail.com', 'Envío de postulación'),
(57, 14, 2, 3, '2026-04-07', 'SISTEMA', 'Pago aprobado'),
(58, 14, 3, 5, '2026-04-21', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(59, 14, 5, 6, '2026-04-28', 'SISTEMA', 'Carné emitido'),
(60, 15, NULL, 1, '2026-04-10', 'kiara.rodriguez@mail.com', 'Registro de postulación'),
(61, 15, 1, 2, '2026-04-11', 'kiara.rodriguez@mail.com', 'Envío de postulación'),
(62, 15, 2, 3, '2026-04-14', 'SISTEMA', 'Pago aprobado'),
(63, 15, 3, 5, '2026-04-20', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(64, 15, 5, 6, '2026-04-30', 'SISTEMA', 'Carné emitido'),
(65, 16, NULL, 1, '2026-04-20', 'mariana.mamani@mail.com', 'Registro de postulación'),
(66, 16, 1, 2, '2026-04-22', 'mariana.mamani@mail.com', 'Envío de postulación'),
(67, 16, 2, 3, '2026-04-24', 'SISTEMA', 'Pago aprobado'),
(68, 16, 3, 5, '2026-05-04', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(69, 16, 5, 6, '2026-05-08', 'SISTEMA', 'Carné emitido'),
(70, 17, NULL, 1, '2026-04-21', 'daniel.cardenas@mail.com', 'Registro de postulación'),
(71, 17, 1, 2, '2026-04-23', 'daniel.cardenas@mail.com', 'Envío de postulación'),
(72, 17, 2, 3, '2026-04-27', 'SISTEMA', 'Pago aprobado'),
(73, 17, 3, 4, '2026-05-15', 'hugo.zevallos@admitu.edu.pe', 'Documento observado'),
(74, 17, 4, 7, '2026-05-28', 'carmen.villanueva@admitu.edu.pe', 'No subsanó dentro del plazo'),
(75, 18, NULL, 1, '2026-04-21', 'franco.espinoza@mail.com', 'Registro de postulación'),
(76, 18, 1, 2, '2026-04-23', 'franco.espinoza@mail.com', 'Envío de postulación'),
(77, 18, 2, 3, '2026-04-28', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(78, 18, 3, 4, '2026-05-15', 'hugo.zevallos@admitu.edu.pe', 'Documento observado'),
(79, 18, 4, 3, '2026-05-22', 'franco.espinoza@mail.com', 'Observación subsanada'),
(80, 18, 3, 5, '2026-05-29', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(81, 18, 5, 6, '2026-06-06', 'SISTEMA', 'Carné emitido'),
(82, 19, NULL, 1, '2026-04-24', 'ana.condori@mail.com', 'Registro de postulación'),
(83, 19, 1, 2, '2026-04-27', 'ana.condori@mail.com', 'Envío de postulación'),
(84, 19, 2, 3, '2026-04-29', 'SISTEMA', 'Pago aprobado'),
(85, 19, 3, 4, '2026-05-21', 'fernando.caceres@admitu.edu.pe', 'Documento observado'),
(86, 19, 4, 3, '2026-05-27', 'ana.condori@mail.com', 'Observación subsanada'),
(87, 19, 3, 5, '2026-06-08', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(88, 19, 5, 6, '2026-06-18', 'SISTEMA', 'Carné emitido'),
(89, 20, NULL, 1, '2026-05-22', 'juan.paucar@mail.com', 'Registro de postulación'),
(90, 20, 1, 2, '2026-05-24', 'juan.paucar@mail.com', 'Envío de postulación'),
(91, 20, 2, 8, '2026-06-09', 'SISTEMA', 'Pago vencido'),
(92, 21, NULL, 1, '2026-05-23', 'kiara.medina@mail.com', 'Registro de postulación'),
(93, 21, 1, 2, '2026-05-24', 'kiara.medina@mail.com', 'Envío de postulación'),
(94, 21, 2, 3, '2026-05-27', 'SISTEMA', 'Pago aprobado'),
(95, 21, 3, 5, '2026-06-08', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(96, 21, 5, 6, '2026-06-16', 'SISTEMA', 'Carné emitido'),
(97, 22, NULL, 1, '2026-05-27', 'sofia.salas@mail.com', 'Registro de postulación'),
(98, 22, 1, 2, '2026-05-29', 'sofia.salas@mail.com', 'Envío de postulación'),
(99, 22, 2, 8, '2026-06-14', 'SISTEMA', 'Pago vencido'),
(100, 23, NULL, 1, '2026-05-31', 'isabella.delgado@mail.com', 'Registro de postulación'),
(101, 23, 1, 2, '2026-06-02', 'isabella.delgado@mail.com', 'Envío de postulación'),
(102, 23, 2, 3, '2026-06-03', 'SISTEMA', 'Pago aprobado'),
(103, 23, 3, 4, '2026-06-16', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(104, 23, 4, 7, '2026-06-27', 'carmen.villanueva@admitu.edu.pe', 'No subsanó dentro del plazo'),
(105, 24, NULL, 1, '2026-06-09', 'kiara.tello@mail.com', 'Registro de postulación'),
(106, 24, 1, 2, '2026-06-12', 'kiara.tello@mail.com', 'Envío de postulación'),
(107, 24, 2, 3, '2026-06-13', 'SISTEMA', 'Pago aprobado'),
(108, 24, 3, 5, '2026-06-24', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(109, 24, 5, 6, '2026-06-29', 'SISTEMA', 'Carné emitido'),
(110, 25, NULL, 1, '2026-06-15', 'franco.sepulveda@mail.com', 'Registro de postulación'),
(111, 25, 1, 2, '2026-06-15', 'franco.sepulveda@mail.com', 'Envío de postulación'),
(112, 25, 2, 3, '2026-06-17', 'SISTEMA', 'Pago aprobado'),
(113, 25, 3, 5, '2026-06-26', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(114, 25, 5, 6, '2026-06-29', 'SISTEMA', 'Carné emitido'),
(115, 26, NULL, 1, '2026-09-01', 'kiara.tello@mail.com', 'Registro de postulación'),
(116, 26, 1, 2, '2026-09-02', 'kiara.tello@mail.com', 'Envío de postulación'),
(117, 26, 2, 3, '2026-09-05', 'SISTEMA', 'Pago aprobado'),
(118, 26, 3, 4, '2026-09-17', 'hugo.zevallos@admitu.edu.pe', 'Documento observado'),
(119, 26, 4, 3, '2026-09-20', 'kiara.tello@mail.com', 'Observación subsanada'),
(120, 26, 3, 5, '2026-09-30', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(121, 26, 5, 6, '2026-10-03', 'SISTEMA', 'Carné emitido'),
(122, 27, NULL, 1, '2026-09-03', 'alejandro.arias@mail.com', 'Registro de postulación'),
(123, 27, 1, 2, '2026-09-05', 'alejandro.arias@mail.com', 'Envío de postulación'),
(124, 27, 2, 3, '2026-09-09', 'SISTEMA', 'Pago aprobado'),
(125, 27, 3, 5, '2026-09-15', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(126, 27, 5, 6, '2026-09-21', 'SISTEMA', 'Carné emitido'),
(127, 28, NULL, 1, '2026-09-04', 'mariana.mamani@mail.com', 'Registro de postulación'),
(128, 28, 1, 2, '2026-09-05', 'mariana.mamani@mail.com', 'Envío de postulación'),
(129, 28, 2, 3, '2026-09-08', 'SISTEMA', 'Pago aprobado'),
(130, 28, 3, 5, '2026-09-27', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(131, 28, 5, 6, '2026-10-06', 'SISTEMA', 'Carné emitido'),
(132, 29, NULL, 1, '2026-09-05', 'daniel.cardenas@mail.com', 'Registro de postulación'),
(133, 29, 1, 2, '2026-09-08', 'daniel.cardenas@mail.com', 'Envío de postulación'),
(134, 29, 2, 3, '2026-09-12', 'SISTEMA', 'Pago aprobado'),
(135, 29, 3, 4, '2026-09-21', 'patricia.salazar@admitu.edu.pe', 'Documento observado'),
(136, 29, 4, 3, '2026-09-26', 'daniel.cardenas@mail.com', 'Observación subsanada'),
(137, 29, 3, 5, '2026-10-08', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(138, 30, NULL, 1, '2026-09-06', 'carlos.montoya@mail.com', 'Registro de postulación'),
(139, 30, 1, 2, '2026-09-09', 'carlos.montoya@mail.com', 'Envío de postulación'),
(140, 30, 2, 3, '2026-09-14', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(141, 30, 3, 4, '2026-09-27', 'patricia.salazar@admitu.edu.pe', 'Documento observado'),
(142, 30, 4, 3, '2026-10-01', 'carlos.montoya@mail.com', 'Observación subsanada'),
(143, 30, 3, 5, '2026-10-16', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(144, 30, 5, 6, '2026-10-24', 'SISTEMA', 'Carné emitido'),
(145, 31, NULL, 1, '2026-09-06', 'luciana.vasquez@mail.com', 'Registro de postulación'),
(146, 31, 1, 2, '2026-09-09', 'luciana.vasquez@mail.com', 'Envío de postulación'),
(147, 31, 2, 3, '2026-09-11', 'SISTEMA', 'Pago aprobado'),
(148, 31, 3, 5, '2026-09-29', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(149, 32, NULL, 1, '2026-09-08', 'andrea.rodriguez@mail.com', 'Registro de postulación'),
(150, 32, 1, 2, '2026-09-09', 'andrea.rodriguez@mail.com', 'Envío de postulación'),
(151, 32, 2, 3, '2026-09-13', 'SISTEMA', 'Pago aprobado'),
(152, 32, 3, 4, '2026-09-28', 'hugo.zevallos@admitu.edu.pe', 'Documento observado'),
(153, 32, 4, 3, '2026-10-01', 'andrea.rodriguez@mail.com', 'Observación subsanada'),
(154, 32, 3, 5, '2026-10-11', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(155, 32, 5, 6, '2026-10-14', 'SISTEMA', 'Carné emitido'),
(156, 33, NULL, 1, '2026-09-09', 'nicole.rios@mail.com', 'Registro de postulación'),
(157, 33, 1, 2, '2026-09-11', 'nicole.rios@mail.com', 'Envío de postulación'),
(158, 33, 2, 3, '2026-09-14', 'SISTEMA', 'Pago aprobado'),
(159, 33, 3, 4, '2026-10-06', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(160, 33, 4, 3, '2026-10-11', 'nicole.rios@mail.com', 'Observación subsanada'),
(161, 33, 3, 5, '2026-10-27', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(162, 33, 5, 6, '2026-10-30', 'SISTEMA', 'Carné emitido'),
(163, 34, NULL, 1, '2026-09-09', 'micaela.leon@mail.com', 'Registro de postulación'),
(164, 34, 1, 2, '2026-09-11', 'micaela.leon@mail.com', 'Envío de postulación'),
(165, 34, 2, 3, '2026-09-15', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(166, 34, 3, 4, '2026-10-06', 'fernando.caceres@admitu.edu.pe', 'Documento observado'),
(167, 34, 4, 3, '2026-10-12', 'micaela.leon@mail.com', 'Observación subsanada'),
(168, 34, 3, 5, '2026-10-26', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(169, 34, 5, 6, '2026-11-02', 'SISTEMA', 'Carné emitido'),
(170, 35, NULL, 1, '2026-09-10', 'luciana.salas@mail.com', 'Registro de postulación'),
(171, 35, 1, 2, '2026-09-10', 'luciana.salas@mail.com', 'Envío de postulación'),
(172, 35, 2, 8, '2026-09-26', 'SISTEMA', 'Pago vencido'),
(173, 36, NULL, 1, '2026-09-10', 'juan.lozano@mail.com', 'Registro de postulación'),
(174, 36, 1, 2, '2026-09-12', 'juan.lozano@mail.com', 'Envío de postulación'),
(175, 36, 2, 3, '2026-09-14', 'SISTEMA', 'Pago aprobado'),
(176, 36, 3, 4, '2026-09-27', 'patricia.salazar@admitu.edu.pe', 'Documento observado'),
(177, 36, 4, 3, '2026-10-04', 'juan.lozano@mail.com', 'Observación subsanada'),
(178, 36, 3, 5, '2026-10-19', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(179, 36, 5, 6, '2026-10-22', 'SISTEMA', 'Carné emitido'),
(180, 37, NULL, 1, '2026-09-11', 'bruno.tello@mail.com', 'Registro de postulación'),
(181, 37, 1, 2, '2026-09-11', 'bruno.tello@mail.com', 'Envío de postulación'),
(182, 37, 2, 3, '2026-09-15', 'SISTEMA', 'Pago aprobado'),
(183, 37, 3, 5, '2026-09-27', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(184, 37, 5, 6, '2026-10-07', 'SISTEMA', 'Carné emitido'),
(185, 38, NULL, 1, '2026-09-11', 'renato.aguilar@mail.com', 'Registro de postulación'),
(186, 38, 1, 2, '2026-09-14', 'renato.aguilar@mail.com', 'Envío de postulación'),
(187, 38, 2, 8, '2026-09-30', 'SISTEMA', 'Pago vencido'),
(188, 39, NULL, 1, '2026-09-12', 'luciana.pinto@mail.com', 'Registro de postulación'),
(189, 39, 1, 2, '2026-09-12', 'luciana.pinto@mail.com', 'Envío de postulación'),
(190, 39, 2, 3, '2026-09-16', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(191, 39, 3, 4, '2026-10-02', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(192, 39, 4, 3, '2026-10-10', 'luciana.pinto@mail.com', 'Observación subsanada'),
(193, 39, 3, 5, '2026-10-27', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(194, 39, 5, 6, '2026-11-02', 'SISTEMA', 'Carné emitido'),
(195, 40, NULL, 1, '2026-09-14', 'isabella.delgado@mail.com', 'Registro de postulación'),
(196, 40, 1, 2, '2026-09-16', 'isabella.delgado@mail.com', 'Envío de postulación'),
(197, 40, 2, 3, '2026-09-18', 'SISTEMA', 'Pago aprobado'),
(198, 40, 3, 4, '2026-10-04', 'fernando.caceres@admitu.edu.pe', 'Documento observado'),
(199, 40, 4, 7, '2026-10-16', 'carmen.villanueva@admitu.edu.pe', 'No subsanó dentro del plazo'),
(200, 41, NULL, 1, '2026-09-15', 'jimena.huaman@mail.com', 'Registro de postulación'),
(201, 41, 1, 2, '2026-09-15', 'jimena.huaman@mail.com', 'Envío de postulación'),
(202, 41, 2, 3, '2026-09-21', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(203, 41, 3, 5, '2026-09-28', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(204, 41, 5, 6, '2026-10-06', 'SISTEMA', 'Carné emitido'),
(205, 42, NULL, 1, '2026-09-17', 'rodrigo.zevallos@mail.com', 'Registro de postulación'),
(206, 42, 1, 2, '2026-09-17', 'rodrigo.zevallos@mail.com', 'Envío de postulación'),
(207, 42, 2, 8, '2026-10-03', 'SISTEMA', 'Pago vencido'),
(208, 43, NULL, 1, '2026-09-20', 'andrea.pacheco@mail.com', 'Registro de postulación'),
(209, 43, 1, 2, '2026-09-22', 'andrea.pacheco@mail.com', 'Envío de postulación'),
(210, 43, 2, 3, '2026-09-28', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(211, 43, 3, 5, '2026-10-16', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(212, 44, NULL, 1, '2026-09-20', 'franco.sanchez@mail.com', 'Registro de postulación'),
(213, 44, 1, 2, '2026-09-21', 'franco.sanchez@mail.com', 'Envío de postulación'),
(214, 44, 2, 3, '2026-09-22', 'SISTEMA', 'Pago aprobado'),
(215, 44, 3, 4, '2026-10-02', 'fernando.caceres@admitu.edu.pe', 'Documento observado'),
(216, 44, 4, 3, '2026-10-04', 'franco.sanchez@mail.com', 'Observación subsanada'),
(217, 44, 3, 5, '2026-10-23', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(218, 45, NULL, 1, '2026-09-21', 'jimena.aguilar@mail.com', 'Registro de postulación'),
(219, 45, 1, 2, '2026-09-21', 'jimena.aguilar@mail.com', 'Envío de postulación'),
(220, 45, 2, 3, '2026-09-23', 'SISTEMA', 'Pago aprobado'),
(221, 45, 3, 5, '2026-09-29', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(222, 45, 5, 6, '2026-10-08', 'SISTEMA', 'Carné emitido'),
(223, 46, NULL, 1, '2026-09-23', 'josue.sepulveda@mail.com', 'Registro de postulación'),
(224, 46, 1, 2, '2026-09-26', 'josue.sepulveda@mail.com', 'Envío de postulación'),
(225, 46, 2, 3, '2026-09-29', 'SISTEMA', 'Pago aprobado'),
(226, 46, 3, 5, '2026-10-15', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(227, 46, 5, 6, '2026-10-25', 'SISTEMA', 'Carné emitido'),
(228, 47, NULL, 1, '2026-09-24', 'renato.soto@mail.com', 'Registro de postulación'),
(229, 47, 1, 2, '2026-09-25', 'renato.soto@mail.com', 'Envío de postulación'),
(230, 47, 2, 3, '2026-09-29', 'SISTEMA', 'Pago aprobado'),
(231, 47, 3, 4, '2026-10-21', 'jorge.quispe@admitu.edu.pe', 'Documento observado'),
(232, 47, 4, 3, '2026-10-23', 'renato.soto@mail.com', 'Observación subsanada'),
(233, 47, 3, 5, '2026-11-08', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(234, 48, NULL, 1, '2026-09-24', 'christian.condori@mail.com', 'Registro de postulación'),
(235, 48, 1, 2, '2026-09-25', 'christian.condori@mail.com', 'Envío de postulación'),
(236, 48, 2, 3, '2026-09-28', 'SISTEMA', 'Pago aprobado'),
(237, 48, 3, 4, '2026-10-09', 'jorge.quispe@admitu.edu.pe', 'Documento observado'),
(238, 48, 4, 3, '2026-10-12', 'christian.condori@mail.com', 'Observación subsanada'),
(239, 48, 3, 5, '2026-10-23', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(240, 48, 5, 6, '2026-10-26', 'SISTEMA', 'Carné emitido'),
(241, 49, NULL, 1, '2026-09-26', 'franco.soto@mail.com', 'Registro de postulación'),
(242, 49, 1, 2, '2026-09-26', 'franco.soto@mail.com', 'Envío de postulación'),
(243, 49, 2, 3, '2026-09-29', 'SISTEMA', 'Pago aprobado'),
(244, 49, 3, 5, '2026-10-07', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(245, 49, 5, 6, '2026-10-12', 'SISTEMA', 'Carné emitido'),
(246, 50, NULL, 1, '2026-09-28', 'daniela.mamani@mail.com', 'Registro de postulación'),
(247, 50, 1, 2, '2026-10-01', 'daniela.mamani@mail.com', 'Envío de postulación'),
(248, 50, 2, 3, '2026-10-04', 'SISTEMA', 'Pago aprobado'),
(249, 50, 3, 5, '2026-10-11', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(250, 50, 5, 6, '2026-10-17', 'SISTEMA', 'Carné emitido'),
(251, 51, NULL, 1, '2026-09-28', 'adriana.briceno@mail.com', 'Registro de postulación'),
(252, 51, 1, 2, '2026-09-30', 'adriana.briceno@mail.com', 'Envío de postulación'),
(253, 51, 2, 3, '2026-10-04', 'SISTEMA', 'Pago aprobado'),
(254, 51, 3, 4, '2026-10-14', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(255, 51, 4, 3, '2026-10-18', 'adriana.briceno@mail.com', 'Observación subsanada'),
(256, 51, 3, 5, '2026-10-25', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(257, 51, 5, 6, '2026-11-02', 'SISTEMA', 'Carné emitido'),
(258, 52, NULL, 1, '2026-09-30', 'nicole.caceres@mail.com', 'Registro de postulación'),
(259, 52, 1, 2, '2026-10-02', 'nicole.caceres@mail.com', 'Envío de postulación'),
(260, 52, 2, 3, '2026-10-03', 'SISTEMA', 'Pago aprobado'),
(261, 52, 3, 4, '2026-10-15', 'fernando.caceres@admitu.edu.pe', 'Documento observado'),
(262, 52, 4, 3, '2026-10-21', 'nicole.caceres@mail.com', 'Observación subsanada'),
(263, 52, 3, 5, '2026-11-07', 'fernando.caceres@admitu.edu.pe', 'Documentos aprobados'),
(264, 53, NULL, 1, '2026-10-01', 'juan.ruiz@mail.com', 'Registro de postulación'),
(265, 53, 1, 2, '2026-10-04', 'juan.ruiz@mail.com', 'Envío de postulación'),
(266, 53, 2, 3, '2026-10-09', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(267, 53, 3, 4, '2026-10-23', 'hugo.zevallos@admitu.edu.pe', 'Documento observado'),
(268, 53, 4, 3, '2026-10-26', 'juan.ruiz@mail.com', 'Observación subsanada'),
(269, 54, NULL, 1, '2026-10-01', 'diana.romero@mail.com', 'Registro de postulación'),
(270, 54, 1, 2, '2026-10-04', 'diana.romero@mail.com', 'Envío de postulación'),
(271, 54, 2, 3, '2026-10-06', 'SISTEMA', 'Pago aprobado'),
(272, 54, 3, 4, '2026-10-18', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(273, 54, 4, 3, '2026-10-26', 'diana.romero@mail.com', 'Observación subsanada'),
(274, 55, NULL, 1, '2026-10-02', 'gabriela.reyes@mail.com', 'Registro de postulación'),
(275, 55, 1, 2, '2026-10-04', 'gabriela.reyes@mail.com', 'Envío de postulación'),
(276, 55, 2, 8, '2026-10-20', 'SISTEMA', 'Pago vencido'),
(277, 56, NULL, 1, '2026-10-02', 'sofia.ortiz@mail.com', 'Registro de postulación'),
(278, 56, 1, 2, '2026-10-02', 'sofia.ortiz@mail.com', 'Envío de postulación'),
(279, 56, 2, 3, '2026-10-05', 'SISTEMA', 'Pago aprobado'),
(280, 56, 3, 4, '2026-10-14', 'jorge.quispe@admitu.edu.pe', 'Documento observado'),
(281, 56, 4, 3, '2026-10-20', 'sofia.ortiz@mail.com', 'Observación subsanada'),
(282, 56, 3, 5, '2026-11-02', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(283, 57, NULL, 1, '2026-10-03', 'franco.gutierrez@mail.com', 'Registro de postulación'),
(284, 57, 1, 2, '2026-10-06', 'franco.gutierrez@mail.com', 'Envío de postulación'),
(285, 57, 2, 3, '2026-10-08', 'SISTEMA', 'Pago aprobado'),
(286, 57, 3, 5, '2026-10-25', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(287, 58, NULL, 1, '2026-10-03', 'renato.medina@mail.com', 'Registro de postulación'),
(288, 58, 1, 2, '2026-10-04', 'renato.medina@mail.com', 'Envío de postulación'),
(289, 58, 2, 3, '2026-10-05', 'SISTEMA', 'Pago aprobado'),
(290, 58, 3, 4, '2026-10-18', 'patricia.salazar@admitu.edu.pe', 'Documento observado'),
(291, 59, NULL, 1, '2026-10-04', 'valentina.valdivia@mail.com', 'Registro de postulación'),
(292, 59, 1, 2, '2026-10-05', 'valentina.valdivia@mail.com', 'Envío de postulación'),
(293, 59, 2, 3, '2026-10-08', 'SISTEMA', 'Pago aprobado'),
(294, 59, 3, 4, '2026-10-20', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(295, 60, NULL, 1, '2026-10-05', 'kevin.araya@mail.com', 'Registro de postulación'),
(296, 60, 1, 2, '2026-10-06', 'kevin.araya@mail.com', 'Envío de postulación'),
(297, 60, 2, 3, '2026-10-10', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(298, 60, 3, 4, '2026-10-18', 'hugo.zevallos@admitu.edu.pe', 'Documento observado'),
(299, 60, 4, 7, '2026-11-01', 'carmen.villanueva@admitu.edu.pe', 'No subsanó dentro del plazo'),
(300, 61, NULL, 1, '2026-10-05', 'piero.vasquez@mail.com', 'Registro de postulación'),
(301, 61, 1, 2, '2026-10-07', 'piero.vasquez@mail.com', 'Envío de postulación'),
(302, 61, 2, 3, '2026-10-11', 'SISTEMA', 'Pago aprobado'),
(303, 61, 3, 4, '2026-10-19', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(304, 61, 4, 3, '2026-10-26', 'piero.vasquez@mail.com', 'Observación subsanada'),
(305, 62, NULL, 1, '2026-10-05', 'santiago.salas@mail.com', 'Registro de postulación'),
(306, 62, 1, 2, '2026-10-06', 'santiago.salas@mail.com', 'Envío de postulación'),
(307, 62, 2, 3, '2026-10-07', 'SISTEMA', 'Pago aprobado'),
(308, 62, 3, 4, '2026-10-22', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(309, 63, NULL, 1, '2026-10-06', 'santiago.sepulveda@mail.com', 'Registro de postulación'),
(310, 63, 1, 2, '2026-10-06', 'santiago.sepulveda@mail.com', 'Envío de postulación'),
(311, 63, 2, 3, '2026-10-08', 'SISTEMA', 'Pago aprobado'),
(312, 63, 3, 5, '2026-10-23', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(313, 63, 5, 6, '2026-10-27', 'SISTEMA', 'Carné emitido'),
(314, 64, NULL, 1, '2026-10-06', 'juan.paucar@mail.com', 'Registro de postulación'),
(315, 64, 1, 2, '2026-10-08', 'juan.paucar@mail.com', 'Envío de postulación'),
(316, 64, 2, 3, '2026-10-11', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(317, 64, 3, 4, '2026-10-21', 'hugo.zevallos@admitu.edu.pe', 'Documento observado'),
(318, 65, NULL, 1, '2026-10-08', 'matias.vargas@mail.com', 'Registro de postulación'),
(319, 65, 1, 2, '2026-10-10', 'matias.vargas@mail.com', 'Envío de postulación'),
(320, 65, 2, 3, '2026-10-13', 'SISTEMA', 'Pago aprobado'),
(321, 65, 3, 4, '2026-10-22', 'jorge.quispe@admitu.edu.pe', 'Documento observado'),
(322, 66, NULL, 1, '2026-10-09', 'gabriel.soto@mail.com', 'Registro de postulación'),
(323, 66, 1, 2, '2026-10-11', 'gabriel.soto@mail.com', 'Envío de postulación'),
(324, 66, 2, 3, '2026-10-15', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(325, 66, 3, 4, '2026-11-03', 'patricia.salazar@admitu.edu.pe', 'Documento observado'),
(326, 66, 4, 3, '2026-11-07', 'gabriel.soto@mail.com', 'Observación subsanada'),
(327, 67, NULL, 1, '2026-10-09', 'alejandro.ramos@mail.com', 'Registro de postulación'),
(328, 67, 1, 2, '2026-10-12', 'alejandro.ramos@mail.com', 'Envío de postulación'),
(329, 67, 2, 3, '2026-10-13', 'SISTEMA', 'Pago aprobado'),
(330, 67, 3, 4, '2026-10-30', 'patricia.salazar@admitu.edu.pe', 'Documento observado'),
(331, 68, NULL, 1, '2026-10-11', 'micaela.aguilar@mail.com', 'Registro de postulación'),
(332, 68, 1, 2, '2026-10-13', 'micaela.aguilar@mail.com', 'Envío de postulación'),
(333, 68, 2, 3, '2026-10-15', 'SISTEMA', 'Pago aprobado'),
(334, 68, 3, 5, '2026-11-03', 'hugo.zevallos@admitu.edu.pe', 'Documentos aprobados'),
(335, 69, NULL, 1, '2026-10-12', 'emilio.ortiz@mail.com', 'Registro de postulación'),
(336, 69, 1, 2, '2026-10-13', 'emilio.ortiz@mail.com', 'Envío de postulación'),
(337, 69, 2, 3, '2026-10-14', 'SISTEMA', 'Pago aprobado'),
(338, 69, 3, 5, '2026-10-25', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(339, 69, 5, 6, '2026-10-31', 'SISTEMA', 'Carné emitido'),
(340, 70, NULL, 1, '2026-10-14', 'franco.sepulveda@mail.com', 'Registro de postulación'),
(341, 70, 1, 2, '2026-10-15', 'franco.sepulveda@mail.com', 'Envío de postulación'),
(342, 70, 2, 3, '2026-10-18', 'SISTEMA', 'Pago aprobado'),
(343, 70, 3, 5, '2026-11-02', 'patricia.salazar@admitu.edu.pe', 'Documentos aprobados'),
(344, 70, 5, 6, '2026-11-06', 'SISTEMA', 'Carné emitido'),
(345, 71, NULL, 1, '2026-10-15', 'carolina.vasquez@mail.com', 'Registro de postulación'),
(346, 71, 1, 2, '2026-10-17', 'carolina.vasquez@mail.com', 'Envío de postulación'),
(347, 71, 2, 8, '2026-11-02', 'SISTEMA', 'Pago vencido'),
(348, 72, NULL, 1, '2026-10-16', 'mariana.chavez@mail.com', 'Registro de postulación'),
(349, 72, 1, 2, '2026-10-18', 'mariana.chavez@mail.com', 'Envío de postulación'),
(350, 72, 2, 3, '2026-10-20', 'SISTEMA', 'Pago aprobado'),
(351, 72, 3, 5, '2026-10-30', 'rocio.medina@admitu.edu.pe', 'Documentos aprobados'),
(352, 72, 5, 6, '2026-11-07', 'SISTEMA', 'Carné emitido'),
(353, 73, NULL, 1, '2026-10-17', 'isabella.morales@mail.com', 'Registro de postulación'),
(354, 73, 1, 2, '2026-10-20', 'isabella.morales@mail.com', 'Envío de postulación'),
(355, 73, 2, 3, '2026-10-21', 'SISTEMA', 'Pago aprobado'),
(356, 73, 3, 4, '2026-11-05', 'rocio.medina@admitu.edu.pe', 'Documento observado'),
(357, 74, NULL, 1, '2026-10-17', 'matias.ruiz@mail.com', 'Registro de postulación'),
(358, 74, 1, 2, '2026-10-19', 'matias.ruiz@mail.com', 'Envío de postulación'),
(359, 74, 2, 3, '2026-10-20', 'SISTEMA', 'Pago aprobado'),
(360, 74, 3, 4, '2026-11-06', 'patricia.salazar@admitu.edu.pe', 'Documento observado'),
(361, 75, NULL, 1, '2026-10-17', 'renata.palomino@mail.com', 'Registro de postulación'),
(362, 75, 1, 2, '2026-10-20', 'renata.palomino@mail.com', 'Envío de postulación'),
(363, 75, 2, 3, '2026-10-21', 'SISTEMA', 'Pago aprobado'),
(364, 75, 3, 5, '2026-10-30', 'jorge.quispe@admitu.edu.pe', 'Documentos aprobados'),
(365, 75, 5, 6, '2026-11-08', 'SISTEMA', 'Carné emitido'),
(366, 76, NULL, 1, '2026-10-18', 'adriana.medina@mail.com', 'Registro de postulación'),
(367, 76, 1, 2, '2026-10-18', 'adriana.medina@mail.com', 'Envío de postulación'),
(368, 76, 2, 3, '2026-10-20', 'SISTEMA', 'Pago aprobado'),
(369, 76, 3, 4, '2026-10-28', 'fernando.caceres@admitu.edu.pe', 'Documento observado'),
(370, 76, 4, 3, '2026-11-04', 'adriana.medina@mail.com', 'Observación subsanada'),
(371, 77, NULL, 1, '2026-10-21', 'christian.rodriguez@mail.com', 'Registro de postulación'),
(372, 77, 1, 2, '2026-10-24', 'christian.rodriguez@mail.com', 'Envío de postulación'),
(373, 77, 2, 3, '2026-10-27', 'SISTEMA', 'Pago aprobado'),
(374, 78, NULL, 1, '2026-10-22', 'luis.romero@mail.com', 'Registro de postulación'),
(375, 78, 1, 2, '2026-10-25', 'luis.romero@mail.com', 'Envío de postulación'),
(376, 78, 2, 3, '2026-10-26', 'SISTEMA', 'Pago aprobado'),
(377, 78, 3, 4, '2026-11-08', 'fernando.caceres@admitu.edu.pe', 'Documento observado'),
(378, 79, NULL, 1, '2026-10-26', 'santiago.huaman@mail.com', 'Registro de postulación'),
(379, 79, 1, 2, '2026-10-28', 'santiago.huaman@mail.com', 'Envío de postulación'),
(380, 80, NULL, 1, '2026-10-27', 'marco.aguilar@mail.com', 'Registro de postulación'),
(381, 80, 1, 2, '2026-10-28', 'marco.aguilar@mail.com', 'Envío de postulación'),
(382, 81, NULL, 1, '2026-10-28', 'renato.yupanqui@mail.com', 'Registro de postulación'),
(383, 81, 1, 2, '2026-10-29', 'renato.yupanqui@mail.com', 'Envío de postulación'),
(384, 81, 2, 3, '2026-11-02', 'SISTEMA', 'Pago aprobado'),
(385, 82, NULL, 1, '2026-10-28', 'alessandra.navarro@mail.com', 'Registro de postulación'),
(386, 82, 1, 2, '2026-10-31', 'alessandra.navarro@mail.com', 'Envío de postulación'),
(387, 83, NULL, 1, '2026-10-28', 'kiara.medina@mail.com', 'Registro de postulación'),
(388, 83, 1, 2, '2026-10-28', 'kiara.medina@mail.com', 'Envío de postulación'),
(389, 84, NULL, 1, '2026-10-29', 'rodrigo.romero@mail.com', 'Registro de postulación'),
(390, 84, 1, 2, '2026-11-01', 'rodrigo.romero@mail.com', 'Envío de postulación'),
(391, 84, 2, 3, '2026-11-02', 'SISTEMA', 'Pago aprobado'),
(392, 85, NULL, 1, '2026-10-31', 'gonzalo.caceres@mail.com', 'Registro de postulación'),
(393, 85, 1, 2, '2026-10-31', 'gonzalo.caceres@mail.com', 'Envío de postulación'),
(394, 86, NULL, 1, '2026-11-01', 'juan.medina@mail.com', 'Registro de postulación'),
(395, 87, NULL, 1, '2026-11-01', 'alessandra.ruiz@mail.com', 'Registro de postulación'),
(396, 87, 1, 2, '2026-11-04', 'alessandra.ruiz@mail.com', 'Envío de postulación'),
(397, 88, NULL, 1, '2026-11-01', 'brenda.gutierrez@mail.com', 'Registro de postulación'),
(398, 88, 1, 2, '2026-11-04', 'brenda.gutierrez@mail.com', 'Envío de postulación'),
(399, 88, 2, 3, '2026-11-08', 'SISTEMA', 'Pago aprobado (segundo intento)'),
(400, 89, NULL, 1, '2026-11-06', 'renata.mamani@mail.com', 'Registro de postulación'),
(401, 90, NULL, 1, '2026-11-06', 'franco.espinoza@mail.com', 'Registro de postulación'),
(402, 90, 1, 2, '2026-11-06', 'franco.espinoza@mail.com', 'Envío de postulación'),
(403, 90, 2, 3, '2026-11-08', 'SISTEMA', 'Pago aprobado');


-- Pagos
INSERT INTO pago (id, postulacion_id, medio_pago, monto, fecha_generacion, fecha_pago, codigo_pago, referencia_pasarela, estado_pago, ruta_voucher) VALUES
(11, 11, 'TRANSFERENCIA', 650.00, '2026-04-05', '2026-04-08', 'PAG-000011', 'OP-89242195', 'APROBADO', '/vouchers/PAG-000011.pdf'),
(12, 12, 'TRANSFERENCIA', 500.00, '2026-04-03', '2026-04-06', 'PAG-000012', 'OP-89572732', 'APROBADO', '/vouchers/PAG-000012.jpg'),
(13, 13, 'TRANSFERENCIA', 450.00, '2026-04-07', '2026-04-08', 'PAG-000013', 'OP-86823933', 'APROBADO', '/vouchers/PAG-000013.jpg'),
(14, 14, 'TRANSFERENCIA', 450.00, '2026-04-04', '2026-04-07', 'PAG-000014', 'OP-88816197', 'APROBADO', '/vouchers/PAG-000014.jpg'),
(15, 15, 'TRANSFERENCIA', 500.00, '2026-04-11', '2026-04-14', 'PAG-000015', 'OP-89195693', 'APROBADO', '/vouchers/PAG-000015.jpg'),
(16, 16, 'TRANSFERENCIA', 450.00, '2026-04-22', '2026-04-23', 'PAG-000016', 'OP-86716959', 'APROBADO', '/vouchers/PAG-000016.jpg'),
(17, 17, 'TRANSFERENCIA', 500.00, '2026-04-23', '2026-04-27', 'PAG-000017', 'OP-87713876', 'APROBADO', '/vouchers/PAG-000017.pdf'),
(18, 18, 'TRANSFERENCIA', 500.00, '2026-04-23', '2026-04-24', 'PAG-000018', 'OP-89752777', 'RECHAZADO', '/vouchers/PAG-000018.jpg'),
(19, 18, 'TRANSFERENCIA', 500.00, '2026-04-25', '2026-04-27', 'PAG-000019', 'OP-88432434', 'APROBADO', '/vouchers/PAG-000019.jpg'),
(20, 19, 'TRANSFERENCIA', 500.00, '2026-04-27', '2026-04-28', 'PAG-000020', 'OP-87779994', 'APROBADO', '/vouchers/PAG-000020.pdf'),
(21, 20, 'TRANSFERENCIA', 500.00, '2026-05-24', NULL, 'PAG-000021', NULL, 'VENCIDO', NULL),
(22, 21, 'TRANSFERENCIA', 500.00, '2026-05-24', '2026-05-27', 'PAG-000022', 'OP-89263144', 'APROBADO', '/vouchers/PAG-000022.jpg'),
(23, 22, 'TRANSFERENCIA', 500.00, '2026-05-29', NULL, 'PAG-000023', NULL, 'VENCIDO', NULL),
(24, 23, 'TRANSFERENCIA', 500.00, '2026-06-02', '2026-06-02', 'PAG-000024', 'OP-86630825', 'APROBADO', '/vouchers/PAG-000024.jpg'),
(25, 24, 'TRANSFERENCIA', 500.00, '2026-06-12', '2026-06-12', 'PAG-000025', 'OP-89982472', 'APROBADO', '/vouchers/PAG-000025.jpg'),
(26, 25, 'TRANSFERENCIA', 650.00, '2026-06-15', '2026-06-16', 'PAG-000026', 'OP-87533477', 'APROBADO', '/vouchers/PAG-000026.jpg'),
(27, 26, 'TRANSFERENCIA', 350.00, '2026-09-02', '2026-09-04', 'PAG-000027', 'OP-88567168', 'APROBADO', '/vouchers/PAG-000027.pdf'),
(28, 27, 'TRANSFERENCIA', 520.00, '2026-09-05', '2026-09-08', 'PAG-000028', 'OP-87751605', 'APROBADO', '/vouchers/PAG-000028.pdf'),
(29, 28, 'TRANSFERENCIA', 480.00, '2026-09-05', '2026-09-08', 'PAG-000029', 'OP-86184255', 'APROBADO', '/vouchers/PAG-000029.pdf'),
(30, 29, 'TRANSFERENCIA', 350.00, '2026-09-08', '2026-09-12', 'PAG-000030', 'OP-89667140', 'APROBADO', '/vouchers/PAG-000030.jpg'),
(31, 30, 'TRANSFERENCIA', 520.00, '2026-09-09', '2026-09-10', 'PAG-000031', 'OP-86062304', 'RECHAZADO', '/vouchers/PAG-000031.jpg'),
(32, 30, 'TRANSFERENCIA', 520.00, '2026-09-11', '2026-09-13', 'PAG-000032', 'OP-87978555', 'APROBADO', '/vouchers/PAG-000032.jpg'),
(33, 31, 'TRANSFERENCIA', 680.00, '2026-09-09', '2026-09-11', 'PAG-000033', 'OP-87612305', 'APROBADO', '/vouchers/PAG-000033.pdf'),
(34, 32, 'TRANSFERENCIA', 680.00, '2026-09-09', '2026-09-13', 'PAG-000034', 'OP-87435413', 'APROBADO', '/vouchers/PAG-000034.jpg'),
(35, 33, 'TRANSFERENCIA', 480.00, '2026-09-11', '2026-09-14', 'PAG-000035', 'OP-89331968', 'APROBADO', '/vouchers/PAG-000035.pdf'),
(36, 34, 'TRANSFERENCIA', 480.00, '2026-09-11', '2026-09-12', 'PAG-000036', 'OP-89863524', 'RECHAZADO', '/vouchers/PAG-000036.pdf'),
(37, 34, 'TRANSFERENCIA', 480.00, '2026-09-13', '2026-09-14', 'PAG-000037', 'OP-89808962', 'APROBADO', '/vouchers/PAG-000037.jpg'),
(38, 35, 'TRANSFERENCIA', 520.00, '2026-09-10', NULL, 'PAG-000038', NULL, 'VENCIDO', NULL),
(39, 36, 'TRANSFERENCIA', 520.00, '2026-09-12', '2026-09-14', 'PAG-000039', 'OP-86564863', 'APROBADO', '/vouchers/PAG-000039.pdf'),
(40, 37, 'TRANSFERENCIA', 520.00, '2026-09-11', '2026-09-15', 'PAG-000040', 'OP-87588716', 'APROBADO', '/vouchers/PAG-000040.pdf'),
(41, 38, 'TRANSFERENCIA', 520.00, '2026-09-14', NULL, 'PAG-000041', NULL, 'VENCIDO', NULL),
(42, 39, 'TRANSFERENCIA', 520.00, '2026-09-12', '2026-09-13', 'PAG-000042', 'OP-88449085', 'RECHAZADO', '/vouchers/PAG-000042.jpg'),
(43, 39, 'TRANSFERENCIA', 520.00, '2026-09-14', '2026-09-15', 'PAG-000043', 'OP-89807678', 'APROBADO', '/vouchers/PAG-000043.pdf'),
(44, 40, 'TRANSFERENCIA', 350.00, '2026-09-16', '2026-09-17', 'PAG-000044', 'OP-88221677', 'APROBADO', '/vouchers/PAG-000044.pdf'),
(45, 41, 'TRANSFERENCIA', 350.00, '2026-09-15', '2026-09-16', 'PAG-000045', 'OP-88905151', 'RECHAZADO', '/vouchers/PAG-000045.pdf'),
(46, 41, 'TRANSFERENCIA', 350.00, '2026-09-17', '2026-09-20', 'PAG-000046', 'OP-88764587', 'APROBADO', '/vouchers/PAG-000046.pdf'),
(47, 42, 'TRANSFERENCIA', 350.00, '2026-09-17', NULL, 'PAG-000047', NULL, 'VENCIDO', NULL),
(48, 43, 'TRANSFERENCIA', 680.00, '2026-09-22', '2026-09-23', 'PAG-000048', 'OP-87912067', 'RECHAZADO', '/vouchers/PAG-000048.pdf'),
(49, 43, 'TRANSFERENCIA', 680.00, '2026-09-24', '2026-09-27', 'PAG-000049', 'OP-89470881', 'APROBADO', '/vouchers/PAG-000049.pdf'),
(50, 44, 'TRANSFERENCIA', 350.00, '2026-09-21', '2026-09-22', 'PAG-000050', 'OP-88138056', 'APROBADO', '/vouchers/PAG-000050.jpg'),
(51, 45, 'TRANSFERENCIA', 480.00, '2026-09-21', '2026-09-22', 'PAG-000051', 'OP-88754740', 'APROBADO', '/vouchers/PAG-000051.jpg'),
(52, 46, 'TRANSFERENCIA', 700.00, '2026-09-26', '2026-09-28', 'PAG-000052', 'OP-88078931', 'APROBADO', '/vouchers/PAG-000052.pdf'),
(53, 47, 'TRANSFERENCIA', 520.00, '2026-09-25', '2026-09-28', 'PAG-000053', 'OP-89526383', 'APROBADO', '/vouchers/PAG-000053.pdf'),
(54, 48, 'TRANSFERENCIA', 350.00, '2026-09-25', '2026-09-27', 'PAG-000054', 'OP-89965742', 'APROBADO', '/vouchers/PAG-000054.jpg'),
(55, 49, 'TRANSFERENCIA', 680.00, '2026-09-26', '2026-09-28', 'PAG-000055', 'OP-88313388', 'APROBADO', '/vouchers/PAG-000055.pdf'),
(56, 50, 'TRANSFERENCIA', 480.00, '2026-10-01', '2026-10-04', 'PAG-000056', 'OP-87257438', 'APROBADO', '/vouchers/PAG-000056.jpg'),
(57, 51, 'TRANSFERENCIA', 480.00, '2026-09-30', '2026-10-03', 'PAG-000057', 'OP-86873793', 'APROBADO', '/vouchers/PAG-000057.pdf'),
(58, 52, 'TRANSFERENCIA', 700.00, '2026-10-02', '2026-10-03', 'PAG-000058', 'OP-87244883', 'APROBADO', '/vouchers/PAG-000058.pdf'),
(59, 53, 'TRANSFERENCIA', 520.00, '2026-10-04', '2026-10-05', 'PAG-000059', 'OP-89595534', 'RECHAZADO', '/vouchers/PAG-000059.pdf'),
(60, 53, 'TRANSFERENCIA', 520.00, '2026-10-06', '2026-10-08', 'PAG-000060', 'OP-89313167', 'APROBADO', '/vouchers/PAG-000060.pdf'),
(61, 54, 'TRANSFERENCIA', 350.00, '2026-10-04', '2026-10-05', 'PAG-000061', 'OP-89885500', 'APROBADO', '/vouchers/PAG-000061.pdf'),
(62, 55, 'TRANSFERENCIA', 700.00, '2026-10-04', NULL, 'PAG-000062', NULL, 'VENCIDO', NULL),
(63, 56, 'TRANSFERENCIA', 520.00, '2026-10-02', '2026-10-04', 'PAG-000063', 'OP-88530624', 'APROBADO', '/vouchers/PAG-000063.pdf'),
(64, 57, 'TRANSFERENCIA', 700.00, '2026-10-06', '2026-10-07', 'PAG-000064', 'OP-88866675', 'APROBADO', '/vouchers/PAG-000064.pdf'),
(65, 58, 'TRANSFERENCIA', 480.00, '2026-10-04', '2026-10-04', 'PAG-000065', 'OP-86635001', 'APROBADO', '/vouchers/PAG-000065.jpg'),
(66, 59, 'TRANSFERENCIA', 520.00, '2026-10-05', '2026-10-07', 'PAG-000066', 'OP-89964805', 'APROBADO', '/vouchers/PAG-000066.pdf'),
(67, 60, 'TRANSFERENCIA', 520.00, '2026-10-06', '2026-10-07', 'PAG-000067', 'OP-89407614', 'RECHAZADO', '/vouchers/PAG-000067.pdf'),
(68, 60, 'TRANSFERENCIA', 520.00, '2026-10-08', '2026-10-09', 'PAG-000068', 'OP-88456528', 'APROBADO', '/vouchers/PAG-000068.jpg'),
(69, 61, 'TRANSFERENCIA', 480.00, '2026-10-07', '2026-10-11', 'PAG-000069', 'OP-87808596', 'APROBADO', '/vouchers/PAG-000069.jpg'),
(70, 62, 'TRANSFERENCIA', 680.00, '2026-10-06', '2026-10-07', 'PAG-000070', 'OP-86114153', 'APROBADO', '/vouchers/PAG-000070.pdf'),
(71, 63, 'TRANSFERENCIA', 520.00, '2026-10-06', '2026-10-07', 'PAG-000071', 'OP-89805487', 'APROBADO', '/vouchers/PAG-000071.jpg'),
(72, 64, 'TRANSFERENCIA', 520.00, '2026-10-08', '2026-10-09', 'PAG-000072', 'OP-89326960', 'RECHAZADO', '/vouchers/PAG-000072.jpg'),
(73, 64, 'TRANSFERENCIA', 520.00, '2026-10-10', '2026-10-10', 'PAG-000073', 'OP-89231848', 'APROBADO', '/vouchers/PAG-000073.pdf'),
(74, 65, 'TRANSFERENCIA', 520.00, '2026-10-10', '2026-10-12', 'PAG-000074', 'OP-89081637', 'APROBADO', '/vouchers/PAG-000074.pdf'),
(75, 66, 'TRANSFERENCIA', 520.00, '2026-10-11', '2026-10-12', 'PAG-000075', 'OP-87536958', 'RECHAZADO', '/vouchers/PAG-000075.pdf'),
(76, 66, 'TRANSFERENCIA', 520.00, '2026-10-13', '2026-10-14', 'PAG-000076', 'OP-86137488', 'APROBADO', '/vouchers/PAG-000076.jpg'),
(77, 67, 'TRANSFERENCIA', 520.00, '2026-10-12', '2026-10-13', 'PAG-000077', 'OP-89133075', 'APROBADO', '/vouchers/PAG-000077.pdf'),
(78, 68, 'TRANSFERENCIA', 480.00, '2026-10-13', '2026-10-14', 'PAG-000078', 'OP-89969739', 'APROBADO', '/vouchers/PAG-000078.jpg'),
(79, 69, 'TRANSFERENCIA', 520.00, '2026-10-13', '2026-10-13', 'PAG-000079', 'OP-87336933', 'APROBADO', '/vouchers/PAG-000079.pdf'),
(80, 70, 'TRANSFERENCIA', 700.00, '2026-10-15', '2026-10-18', 'PAG-000080', 'OP-87846001', 'APROBADO', '/vouchers/PAG-000080.pdf'),
(81, 71, 'TRANSFERENCIA', 700.00, '2026-10-17', NULL, 'PAG-000081', NULL, 'VENCIDO', NULL),
(82, 72, 'TRANSFERENCIA', 480.00, '2026-10-18', '2026-10-20', 'PAG-000082', 'OP-89339992', 'APROBADO', '/vouchers/PAG-000082.jpg'),
(83, 73, 'TRANSFERENCIA', 520.00, '2026-10-20', '2026-10-20', 'PAG-000083', 'OP-89470930', 'APROBADO', '/vouchers/PAG-000083.jpg'),
(84, 74, 'TRANSFERENCIA', 700.00, '2026-10-19', '2026-10-20', 'PAG-000084', 'OP-86681361', 'APROBADO', '/vouchers/PAG-000084.pdf'),
(85, 75, 'TRANSFERENCIA', 520.00, '2026-10-20', '2026-10-20', 'PAG-000085', 'OP-86776224', 'APROBADO', '/vouchers/PAG-000085.pdf'),
(86, 76, 'TRANSFERENCIA', 480.00, '2026-10-18', '2026-10-20', 'PAG-000086', 'OP-88875725', 'APROBADO', '/vouchers/PAG-000086.jpg'),
(87, 77, 'TRANSFERENCIA', 680.00, '2026-10-24', '2026-10-27', 'PAG-000087', 'OP-89754959', 'APROBADO', '/vouchers/PAG-000087.pdf'),
(88, 78, 'TRANSFERENCIA', 480.00, '2026-10-25', '2026-10-25', 'PAG-000088', 'OP-88347299', 'APROBADO', '/vouchers/PAG-000088.pdf'),
(89, 79, 'TRANSFERENCIA', 480.00, '2026-10-28', '2026-11-08', 'PAG-000089', 'OP-89021931', 'PENDIENTE', '/vouchers/PAG-000089.pdf'),
(90, 80, 'TRANSFERENCIA', 350.00, '2026-10-28', NULL, 'PAG-000090', NULL, 'GENERADO', NULL),
(91, 81, 'TRANSFERENCIA', 480.00, '2026-10-29', '2026-11-02', 'PAG-000091', 'OP-88744819', 'APROBADO', '/vouchers/PAG-000091.jpg'),
(92, 82, 'TRANSFERENCIA', 520.00, '2026-10-31', '2026-11-09', 'PAG-000092', 'OP-89578722', 'PENDIENTE', '/vouchers/PAG-000092.jpg'),
(93, 83, 'TRANSFERENCIA', 520.00, '2026-10-28', NULL, 'PAG-000093', NULL, 'GENERADO', NULL),
(94, 84, 'TRANSFERENCIA', 520.00, '2026-11-01', '2026-11-02', 'PAG-000094', 'OP-87675791', 'APROBADO', '/vouchers/PAG-000094.pdf'),
(95, 85, 'TRANSFERENCIA', 520.00, '2026-10-31', '2026-11-03', 'PAG-000095', 'OP-89683249', 'PENDIENTE', '/vouchers/PAG-000095.pdf'),
(96, 87, 'TRANSFERENCIA', 680.00, '2026-11-04', NULL, 'PAG-000096', NULL, 'GENERADO', NULL),
(97, 88, 'TRANSFERENCIA', 520.00, '2026-11-04', '2026-11-05', 'PAG-000097', 'OP-87952371', 'RECHAZADO', '/vouchers/PAG-000097.pdf'),
(98, 88, 'TRANSFERENCIA', 520.00, '2026-11-06', '2026-11-07', 'PAG-000098', 'OP-86033931', 'APROBADO', '/vouchers/PAG-000098.jpg'),
(99, 90, 'TRANSFERENCIA', 350.00, '2026-11-06', '2026-11-07', 'PAG-000099', 'OP-88241775', 'APROBADO', '/vouchers/PAG-000099.pdf');


-- Documentos
INSERT INTO documento_postulacion (id, postulacion_id, requisito_aplicable_id, numero_version, nombre_archivo, tipo_archivo, tamanio_archivo, ruta_archivo, fecha_carga, estado_documento, fecha_evaluacion, comentario_evaluacion) VALUES
(38, 11, 7, 1, 'dni.png', 'IMAGEN', 959790, '/docs/INS-2026-2-0005/dni.png', '2026-04-05', 'APROBADO', '2026-04-21', NULL),
(39, 11, 8, 1, 'foto.png', 'IMAGEN', 161879, '/docs/INS-2026-2-0005/foto.png', '2026-04-05', 'APROBADO', '2026-04-21', NULL),
(40, 11, 9, 1, 'certificado_notas.pdf', 'PDF', 2962391, '/docs/INS-2026-2-0005/certificado_notas.pdf', '2026-04-05', 'APROBADO', '2026-04-21', NULL),
(41, 11, 10, 1, 'silabos.pdf', 'PDF', 4096841, '/docs/INS-2026-2-0005/silabos.pdf', '2026-04-05', 'APROBADO', '2026-04-21', NULL),
(42, 12, 4, 1, 'dni.jpg', 'IMAGEN', 1174724, '/docs/INS-2026-2-0003/dni.jpg', '2026-04-03', 'APROBADO', '2026-04-24', NULL),
(43, 12, 5, 1, 'foto.png', 'IMAGEN', 789529, '/docs/INS-2026-2-0003/foto.png', '2026-04-03', 'APROBADO', '2026-04-24', NULL),
(44, 12, 6, 1, 'certificado_estudios.pdf', 'PDF', 4253169, '/docs/INS-2026-2-0003/certificado_estudios.pdf', '2026-04-03', 'APROBADO', '2026-04-24', NULL),
(45, 13, 1, 1, 'dni.jpg', 'IMAGEN', 1065460, '/docs/INS-2026-2-0006/dni.jpg', '2026-04-07', 'APROBADO', '2026-04-20', NULL),
(46, 13, 2, 1, 'foto.png', 'IMAGEN', 473099, '/docs/INS-2026-2-0006/foto.png', '2026-04-07', 'APROBADO', '2026-04-20', NULL),
(47, 13, 3, 1, 'certificado_estudios.pdf', 'PDF', 564728, '/docs/INS-2026-2-0006/certificado_estudios.pdf', '2026-04-07', 'APROBADO', '2026-04-20', NULL),
(48, 14, 1, 1, 'dni.jpg', 'IMAGEN', 455080, '/docs/INS-2026-2-0004/dni.jpg', '2026-04-04', 'APROBADO', '2026-04-21', NULL),
(49, 14, 2, 1, 'foto.jpg', 'IMAGEN', 508958, '/docs/INS-2026-2-0004/foto.jpg', '2026-04-04', 'APROBADO', '2026-04-21', NULL),
(50, 14, 3, 1, 'certificado_estudios.pdf', 'PDF', 3987646, '/docs/INS-2026-2-0004/certificado_estudios.pdf', '2026-04-04', 'APROBADO', '2026-04-21', NULL),
(51, 15, 4, 1, 'dni.png', 'IMAGEN', 950244, '/docs/INS-2026-2-0007/dni.png', '2026-04-11', 'APROBADO', '2026-04-20', NULL),
(52, 15, 5, 1, 'foto.jpg', 'IMAGEN', 383350, '/docs/INS-2026-2-0007/foto.jpg', '2026-04-11', 'APROBADO', '2026-04-20', NULL),
(53, 15, 6, 1, 'certificado_estudios.pdf', 'PDF', 3489007, '/docs/INS-2026-2-0007/certificado_estudios.pdf', '2026-04-11', 'APROBADO', '2026-04-20', NULL),
(54, 16, 1, 1, 'dni.png', 'IMAGEN', 526163, '/docs/INS-2026-2-0008/dni.png', '2026-04-22', 'APROBADO', '2026-05-04', NULL),
(55, 16, 2, 1, 'foto.jpg', 'IMAGEN', 818750, '/docs/INS-2026-2-0008/foto.jpg', '2026-04-22', 'APROBADO', '2026-05-04', NULL),
(56, 16, 3, 1, 'certificado_estudios.pdf', 'PDF', 2184195, '/docs/INS-2026-2-0008/certificado_estudios.pdf', '2026-04-22', 'APROBADO', '2026-05-04', NULL),
(57, 17, 4, 1, 'dni.png', 'IMAGEN', 887025, '/docs/INS-2026-2-0009/dni.png', '2026-04-23', 'APROBADO', '2026-05-15', NULL),
(58, 17, 5, 1, 'foto.jpg', 'IMAGEN', 353408, '/docs/INS-2026-2-0009/foto.jpg', '2026-04-23', 'APROBADO', '2026-05-15', NULL),
(59, 17, 6, 1, 'certificado_estudios.pdf', 'PDF', 1062618, '/docs/INS-2026-2-0009/certificado_estudios.pdf', '2026-04-23', 'RECHAZADO', '2026-05-28', 'No subsanado dentro del plazo'),
(60, 18, 4, 1, 'dni.jpg', 'IMAGEN', 851097, '/docs/INS-2026-2-0010/dni.jpg', '2026-04-23', 'APROBADO', '2026-05-15', NULL),
(61, 18, 5, 1, 'foto.jpg', 'IMAGEN', 459583, '/docs/INS-2026-2-0010/foto.jpg', '2026-04-23', 'OBSERVADO', '2026-05-15', 'La fotografía no tiene fondo blanco'),
(62, 18, 5, 2, 'foto_v2.jpg', 'IMAGEN', 712673, '/docs/INS-2026-2-0010/foto_v2.jpg', '2026-05-22', 'APROBADO', '2026-05-29', NULL),
(63, 18, 6, 1, 'certificado_estudios.pdf', 'PDF', 2285020, '/docs/INS-2026-2-0010/certificado_estudios.pdf', '2026-04-23', 'APROBADO', '2026-05-15', NULL),
(64, 19, 4, 1, 'dni.jpg', 'IMAGEN', 239485, '/docs/INS-2026-2-0011/dni.jpg', '2026-04-27', 'APROBADO', '2026-05-21', NULL),
(65, 19, 5, 1, 'foto.png', 'IMAGEN', 759472, '/docs/INS-2026-2-0011/foto.png', '2026-04-27', 'OBSERVADO', '2026-05-21', 'La fotografía no tiene fondo blanco'),
(66, 19, 5, 2, 'foto_v2.jpg', 'IMAGEN', 244489, '/docs/INS-2026-2-0011/foto_v2.jpg', '2026-05-27', 'APROBADO', '2026-06-08', NULL),
(67, 19, 6, 1, 'certificado_estudios.pdf', 'PDF', 1837205, '/docs/INS-2026-2-0011/certificado_estudios.pdf', '2026-04-27', 'APROBADO', '2026-05-21', NULL),
(68, 21, 4, 1, 'dni.png', 'IMAGEN', 874328, '/docs/INS-2026-2-0013/dni.png', '2026-05-24', 'APROBADO', '2026-06-08', NULL),
(69, 21, 5, 1, 'foto.png', 'IMAGEN', 342271, '/docs/INS-2026-2-0013/foto.png', '2026-05-24', 'APROBADO', '2026-06-08', NULL),
(70, 21, 6, 1, 'certificado_estudios.pdf', 'PDF', 4027487, '/docs/INS-2026-2-0013/certificado_estudios.pdf', '2026-05-24', 'APROBADO', '2026-06-08', NULL),
(71, 22, 4, 1, 'dni.png', 'IMAGEN', 1834207, '/docs/INS-2026-2-0014/dni.png', '2026-05-29', 'PENDIENTE', NULL, NULL),
(72, 22, 5, 1, 'foto.jpg', 'IMAGEN', 765908, '/docs/INS-2026-2-0014/foto.jpg', '2026-05-29', 'PENDIENTE', NULL, NULL),
(73, 22, 6, 1, 'certificado_estudios.pdf', 'PDF', 2543656, '/docs/INS-2026-2-0014/certificado_estudios.pdf', '2026-05-29', 'PENDIENTE', NULL, NULL),
(74, 23, 4, 1, 'dni.jpg', 'IMAGEN', 788534, '/docs/INS-2026-2-0015/dni.jpg', '2026-06-02', 'RECHAZADO', '2026-06-27', 'No subsanado dentro del plazo'),
(75, 23, 5, 1, 'foto.jpg', 'IMAGEN', 222191, '/docs/INS-2026-2-0015/foto.jpg', '2026-06-02', 'APROBADO', '2026-06-16', NULL),
(76, 23, 6, 1, 'certificado_estudios.pdf', 'PDF', 4342763, '/docs/INS-2026-2-0015/certificado_estudios.pdf', '2026-06-02', 'APROBADO', '2026-06-16', NULL),
(77, 24, 4, 1, 'dni.png', 'IMAGEN', 581514, '/docs/INS-2026-2-0016/dni.png', '2026-06-12', 'APROBADO', '2026-06-24', NULL),
(78, 24, 5, 1, 'foto.png', 'IMAGEN', 297213, '/docs/INS-2026-2-0016/foto.png', '2026-06-12', 'APROBADO', '2026-06-24', NULL),
(79, 24, 6, 1, 'certificado_estudios.pdf', 'PDF', 3743567, '/docs/INS-2026-2-0016/certificado_estudios.pdf', '2026-06-12', 'APROBADO', '2026-06-24', NULL),
(80, 25, 7, 1, 'dni.jpg', 'IMAGEN', 635286, '/docs/INS-2026-2-0017/dni.jpg', '2026-06-15', 'APROBADO', '2026-06-26', NULL),
(81, 25, 8, 1, 'foto.jpg', 'IMAGEN', 507630, '/docs/INS-2026-2-0017/foto.jpg', '2026-06-15', 'APROBADO', '2026-06-26', NULL),
(82, 25, 9, 1, 'certificado_notas.pdf', 'PDF', 808657, '/docs/INS-2026-2-0017/certificado_notas.pdf', '2026-06-15', 'APROBADO', '2026-06-26', NULL),
(83, 25, 10, 1, 'silabos.pdf', 'PDF', 4781705, '/docs/INS-2026-2-0017/silabos.pdf', '2026-06-15', 'APROBADO', '2026-06-26', NULL),
(84, 26, 21, 1, 'dni.jpg', 'IMAGEN', 1782967, '/docs/INS-2027-1-0008/dni.jpg', '2026-09-02', 'APROBADO', '2026-09-17', NULL),
(85, 26, 22, 1, 'foto.png', 'IMAGEN', 370541, '/docs/INS-2027-1-0008/foto.png', '2026-09-02', 'APROBADO', '2026-09-17', NULL),
(86, 26, 23, 1, 'certificado_estudios.pdf', 'PDF', 2124227, '/docs/INS-2027-1-0008/certificado_estudios.pdf', '2026-09-02', 'OBSERVADO', '2026-09-17', 'No se leen las calificaciones del certificado'),
(87, 26, 23, 2, 'certificado_estudios_v2.pdf', 'PDF', 1598570, '/docs/INS-2027-1-0008/certificado_estudios_v2.pdf', '2026-09-20', 'APROBADO', '2026-09-30', NULL),
(88, 26, 24, 1, 'constancia_tercio.pdf', 'PDF', 527275, '/docs/INS-2027-1-0008/constancia_tercio.pdf', '2026-09-02', 'APROBADO', '2026-09-17', NULL),
(89, 26, 25, 1, 'declaracion_jurada.pdf', 'PDF', 494992, '/docs/INS-2027-1-0008/declaracion_jurada.pdf', '2026-09-02', 'APROBADO', '2026-09-17', NULL),
(90, 27, 16, 1, 'dni.jpg', 'IMAGEN', 1113219, '/docs/INS-2027-1-0009/dni.jpg', '2026-09-05', 'APROBADO', '2026-09-15', NULL),
(91, 27, 17, 1, 'foto.jpg', 'IMAGEN', 397620, '/docs/INS-2027-1-0009/foto.jpg', '2026-09-05', 'APROBADO', '2026-09-15', NULL),
(92, 27, 18, 1, 'certificado_estudios.pdf', 'PDF', 4494139, '/docs/INS-2027-1-0009/certificado_estudios.pdf', '2026-09-05', 'APROBADO', '2026-09-15', NULL),
(93, 27, 19, 1, 'declaracion_jurada.pdf', 'PDF', 194707, '/docs/INS-2027-1-0009/declaracion_jurada.pdf', '2026-09-05', 'APROBADO', '2026-09-15', NULL),
(94, 28, 11, 1, 'dni.jpg', 'IMAGEN', 824743, '/docs/INS-2027-1-0010/dni.jpg', '2026-09-05', 'APROBADO', '2026-09-27', NULL),
(95, 28, 12, 1, 'foto.png', 'IMAGEN', 398989, '/docs/INS-2027-1-0010/foto.png', '2026-09-05', 'APROBADO', '2026-09-27', NULL),
(96, 28, 13, 1, 'certificado_estudios.pdf', 'PDF', 892015, '/docs/INS-2027-1-0010/certificado_estudios.pdf', '2026-09-05', 'APROBADO', '2026-09-27', NULL),
(97, 28, 14, 1, 'declaracion_jurada.pdf', 'PDF', 451305, '/docs/INS-2027-1-0010/declaracion_jurada.pdf', '2026-09-05', 'APROBADO', '2026-09-27', NULL),
(98, 29, 21, 1, 'dni.png', 'IMAGEN', 789712, '/docs/INS-2027-1-0011/dni.png', '2026-09-08', 'APROBADO', '2026-09-21', NULL),
(99, 29, 22, 1, 'foto.png', 'IMAGEN', 383214, '/docs/INS-2027-1-0011/foto.png', '2026-09-08', 'OBSERVADO', '2026-09-21', 'La fotografía no tiene fondo blanco'),
(100, 29, 22, 2, 'foto_v2.png', 'IMAGEN', 194530, '/docs/INS-2027-1-0011/foto_v2.png', '2026-09-26', 'APROBADO', '2026-10-08', NULL),
(101, 29, 23, 1, 'certificado_estudios.pdf', 'PDF', 3228825, '/docs/INS-2027-1-0011/certificado_estudios.pdf', '2026-09-08', 'APROBADO', '2026-09-21', NULL),
(102, 29, 24, 1, 'constancia_tercio.pdf', 'PDF', 436675, '/docs/INS-2027-1-0011/constancia_tercio.pdf', '2026-09-08', 'APROBADO', '2026-09-21', NULL),
(103, 29, 25, 1, 'declaracion_jurada.pdf', 'PDF', 782190, '/docs/INS-2027-1-0011/declaracion_jurada.pdf', '2026-09-08', 'APROBADO', '2026-09-21', NULL),
(104, 30, 16, 1, 'dni.png', 'IMAGEN', 1528548, '/docs/INS-2027-1-0012/dni.png', '2026-09-09', 'APROBADO', '2026-09-27', NULL),
(105, 30, 17, 1, 'foto.jpg', 'IMAGEN', 721521, '/docs/INS-2027-1-0012/foto.jpg', '2026-09-09', 'APROBADO', '2026-09-27', NULL),
(106, 30, 18, 1, 'certificado_estudios.pdf', 'PDF', 2893360, '/docs/INS-2027-1-0012/certificado_estudios.pdf', '2026-09-09', 'OBSERVADO', '2026-09-27', 'Falta el certificado de 5.° año'),
(107, 30, 18, 2, 'certificado_estudios_v2.pdf', 'PDF', 908666, '/docs/INS-2027-1-0012/certificado_estudios_v2.pdf', '2026-10-01', 'APROBADO', '2026-10-16', NULL),
(108, 30, 19, 1, 'declaracion_jurada.pdf', 'PDF', 818320, '/docs/INS-2027-1-0012/declaracion_jurada.pdf', '2026-09-09', 'APROBADO', '2026-09-27', NULL),
(109, 31, 26, 1, 'dni.png', 'IMAGEN', 1153638, '/docs/INS-2027-1-0013/dni.png', '2026-09-09', 'APROBADO', '2026-09-29', NULL),
(110, 31, 27, 1, 'foto.png', 'IMAGEN', 863205, '/docs/INS-2027-1-0013/foto.png', '2026-09-09', 'APROBADO', '2026-09-29', NULL),
(111, 31, 28, 1, 'certificado_notas.pdf', 'PDF', 1505529, '/docs/INS-2027-1-0013/certificado_notas.pdf', '2026-09-09', 'APROBADO', '2026-09-29', NULL),
(112, 31, 29, 1, 'silabos.pdf', 'PDF', 9410356, '/docs/INS-2027-1-0013/silabos.pdf', '2026-09-09', 'APROBADO', '2026-09-29', NULL),
(113, 31, 30, 1, 'declaracion_jurada.pdf', 'PDF', 598287, '/docs/INS-2027-1-0013/declaracion_jurada.pdf', '2026-09-09', 'APROBADO', '2026-09-29', NULL),
(114, 32, 26, 1, 'dni.jpg', 'IMAGEN', 1206560, '/docs/INS-2027-1-0014/dni.jpg', '2026-09-09', 'APROBADO', '2026-09-28', NULL),
(115, 32, 27, 1, 'foto.png', 'IMAGEN', 832892, '/docs/INS-2027-1-0014/foto.png', '2026-09-09', 'APROBADO', '2026-09-28', NULL),
(116, 32, 28, 1, 'certificado_notas.pdf', 'PDF', 4448281, '/docs/INS-2027-1-0014/certificado_notas.pdf', '2026-09-09', 'OBSERVADO', '2026-09-28', 'Certificado de notas incompleto o borroso'),
(117, 32, 28, 2, 'certificado_notas_v2.pdf', 'PDF', 1572506, '/docs/INS-2027-1-0014/certificado_notas_v2.pdf', '2026-10-01', 'APROBADO', '2026-10-11', NULL),
(118, 32, 29, 1, 'silabos.pdf', 'PDF', 2722857, '/docs/INS-2027-1-0014/silabos.pdf', '2026-09-09', 'APROBADO', '2026-09-28', NULL),
(119, 32, 30, 1, 'declaracion_jurada.pdf', 'PDF', 210520, '/docs/INS-2027-1-0014/declaracion_jurada.pdf', '2026-09-09', 'APROBADO', '2026-09-28', NULL),
(120, 33, 11, 1, 'dni.jpg', 'IMAGEN', 1102449, '/docs/INS-2027-1-0016/dni.jpg', '2026-09-11', 'OBSERVADO', '2026-10-06', 'Documento de identidad vencido'),
(121, 33, 11, 2, 'dni_v2.jpg', 'IMAGEN', 1339817, '/docs/INS-2027-1-0016/dni_v2.jpg', '2026-10-11', 'APROBADO', '2026-10-27', NULL),
(122, 33, 12, 1, 'foto.jpg', 'IMAGEN', 207273, '/docs/INS-2027-1-0016/foto.jpg', '2026-09-11', 'APROBADO', '2026-10-06', NULL),
(123, 33, 13, 1, 'certificado_estudios.pdf', 'PDF', 745007, '/docs/INS-2027-1-0016/certificado_estudios.pdf', '2026-09-11', 'APROBADO', '2026-10-06', NULL),
(124, 33, 14, 1, 'declaracion_jurada.pdf', 'PDF', 650290, '/docs/INS-2027-1-0016/declaracion_jurada.pdf', '2026-09-11', 'APROBADO', '2026-10-06', NULL),
(125, 34, 11, 1, 'dni.jpg', 'IMAGEN', 1886634, '/docs/INS-2027-1-0017/dni.jpg', '2026-09-11', 'APROBADO', '2026-10-06', NULL),
(126, 34, 12, 1, 'foto.jpg', 'IMAGEN', 256659, '/docs/INS-2027-1-0017/foto.jpg', '2026-09-11', 'APROBADO', '2026-10-06', NULL),
(127, 34, 13, 1, 'certificado_estudios.pdf', 'PDF', 548265, '/docs/INS-2027-1-0017/certificado_estudios.pdf', '2026-09-11', 'OBSERVADO', '2026-10-06', 'No se leen las calificaciones del certificado'),
(128, 34, 13, 2, 'certificado_estudios_v2.pdf', 'PDF', 230106, '/docs/INS-2027-1-0017/certificado_estudios_v2.pdf', '2026-10-12', 'APROBADO', '2026-10-26', NULL),
(129, 34, 14, 1, 'declaracion_jurada.pdf', 'PDF', 905187, '/docs/INS-2027-1-0017/declaracion_jurada.pdf', '2026-09-11', 'APROBADO', '2026-10-06', NULL),
(130, 35, 16, 1, 'dni.jpg', 'IMAGEN', 323891, '/docs/INS-2027-1-0015/dni.jpg', '2026-09-10', 'PENDIENTE', NULL, NULL),
(131, 35, 17, 1, 'foto.jpg', 'IMAGEN', 930523, '/docs/INS-2027-1-0015/foto.jpg', '2026-09-10', 'PENDIENTE', NULL, NULL),
(132, 35, 18, 1, 'certificado_estudios.pdf', 'PDF', 1104580, '/docs/INS-2027-1-0015/certificado_estudios.pdf', '2026-09-10', 'PENDIENTE', NULL, NULL),
(133, 35, 19, 1, 'declaracion_jurada.pdf', 'PDF', 329955, '/docs/INS-2027-1-0015/declaracion_jurada.pdf', '2026-09-10', 'PENDIENTE', NULL, NULL),
(134, 36, 16, 1, 'dni.jpg', 'IMAGEN', 1395702, '/docs/INS-2027-1-0019/dni.jpg', '2026-09-12', 'APROBADO', '2026-09-27', NULL),
(135, 36, 17, 1, 'foto.png', 'IMAGEN', 226082, '/docs/INS-2027-1-0019/foto.png', '2026-09-12', 'APROBADO', '2026-09-27', NULL),
(136, 36, 18, 1, 'certificado_estudios.pdf', 'PDF', 3082349, '/docs/INS-2027-1-0019/certificado_estudios.pdf', '2026-09-12', 'APROBADO', '2026-09-27', NULL),
(137, 36, 19, 1, 'declaracion_jurada.pdf', 'PDF', 367990, '/docs/INS-2027-1-0019/declaracion_jurada.pdf', '2026-09-12', 'OBSERVADO', '2026-09-27', 'Declaración jurada sin firma'),
(138, 36, 19, 2, 'declaracion_jurada_v2.pdf', 'PDF', 484231, '/docs/INS-2027-1-0019/declaracion_jurada_v2.pdf', '2026-10-04', 'APROBADO', '2026-10-19', NULL),
(139, 37, 16, 1, 'dni.png', 'IMAGEN', 775693, '/docs/INS-2027-1-0018/dni.png', '2026-09-11', 'APROBADO', '2026-09-27', NULL),
(140, 37, 17, 1, 'foto.jpg', 'IMAGEN', 445264, '/docs/INS-2027-1-0018/foto.jpg', '2026-09-11', 'APROBADO', '2026-09-27', NULL),
(141, 37, 18, 1, 'certificado_estudios.pdf', 'PDF', 2229415, '/docs/INS-2027-1-0018/certificado_estudios.pdf', '2026-09-11', 'APROBADO', '2026-09-27', NULL),
(142, 37, 19, 1, 'declaracion_jurada.pdf', 'PDF', 384412, '/docs/INS-2027-1-0018/declaracion_jurada.pdf', '2026-09-11', 'APROBADO', '2026-09-27', NULL),
(143, 39, 16, 1, 'dni.jpg', 'IMAGEN', 1068492, '/docs/INS-2027-1-0020/dni.jpg', '2026-09-12', 'APROBADO', '2026-10-02', NULL),
(144, 39, 17, 1, 'foto.jpg', 'IMAGEN', 210702, '/docs/INS-2027-1-0020/foto.jpg', '2026-09-12', 'APROBADO', '2026-10-02', NULL),
(145, 39, 18, 1, 'certificado_estudios.pdf', 'PDF', 1359628, '/docs/INS-2027-1-0020/certificado_estudios.pdf', '2026-09-12', 'OBSERVADO', '2026-10-02', 'Falta el certificado de 5.° año'),
(146, 39, 18, 2, 'certificado_estudios_v2.pdf', 'PDF', 3651216, '/docs/INS-2027-1-0020/certificado_estudios_v2.pdf', '2026-10-10', 'APROBADO', '2026-10-27', NULL),
(147, 39, 19, 1, 'declaracion_jurada.pdf', 'PDF', 558054, '/docs/INS-2027-1-0020/declaracion_jurada.pdf', '2026-09-12', 'APROBADO', '2026-10-02', NULL),
(148, 40, 21, 1, 'dni.png', 'IMAGEN', 1686536, '/docs/INS-2027-1-0023/dni.png', '2026-09-16', 'RECHAZADO', '2026-10-16', 'No subsanado dentro del plazo'),
(149, 40, 22, 1, 'foto.png', 'IMAGEN', 699446, '/docs/INS-2027-1-0023/foto.png', '2026-09-16', 'APROBADO', '2026-10-04', NULL),
(150, 40, 23, 1, 'certificado_estudios.pdf', 'PDF', 1121468, '/docs/INS-2027-1-0023/certificado_estudios.pdf', '2026-09-16', 'APROBADO', '2026-10-04', NULL),
(151, 40, 24, 1, 'constancia_tercio.pdf', 'PDF', 1759557, '/docs/INS-2027-1-0023/constancia_tercio.pdf', '2026-09-16', 'APROBADO', '2026-10-04', NULL),
(152, 40, 25, 1, 'declaracion_jurada.pdf', 'PDF', 381277, '/docs/INS-2027-1-0023/declaracion_jurada.pdf', '2026-09-16', 'APROBADO', '2026-10-04', NULL),
(153, 41, 21, 1, 'dni.jpg', 'IMAGEN', 618416, '/docs/INS-2027-1-0022/dni.jpg', '2026-09-15', 'APROBADO', '2026-09-28', NULL),
(154, 41, 22, 1, 'foto.jpg', 'IMAGEN', 853464, '/docs/INS-2027-1-0022/foto.jpg', '2026-09-15', 'APROBADO', '2026-09-28', NULL),
(155, 41, 23, 1, 'certificado_estudios.pdf', 'PDF', 1457748, '/docs/INS-2027-1-0022/certificado_estudios.pdf', '2026-09-15', 'APROBADO', '2026-09-28', NULL),
(156, 41, 24, 1, 'constancia_tercio.pdf', 'PDF', 1845610, '/docs/INS-2027-1-0022/constancia_tercio.pdf', '2026-09-15', 'APROBADO', '2026-09-28', NULL),
(157, 41, 25, 1, 'declaracion_jurada.pdf', 'PDF', 706020, '/docs/INS-2027-1-0022/declaracion_jurada.pdf', '2026-09-15', 'APROBADO', '2026-09-28', NULL),
(158, 43, 26, 1, 'dni.png', 'IMAGEN', 308047, '/docs/INS-2027-1-0027/dni.png', '2026-09-22', 'APROBADO', '2026-10-16', NULL),
(159, 43, 27, 1, 'foto.jpg', 'IMAGEN', 826037, '/docs/INS-2027-1-0027/foto.jpg', '2026-09-22', 'APROBADO', '2026-10-16', NULL),
(160, 43, 28, 1, 'certificado_notas.pdf', 'PDF', 1906145, '/docs/INS-2027-1-0027/certificado_notas.pdf', '2026-09-22', 'APROBADO', '2026-10-16', NULL),
(161, 43, 29, 1, 'silabos.pdf', 'PDF', 7581550, '/docs/INS-2027-1-0027/silabos.pdf', '2026-09-22', 'APROBADO', '2026-10-16', NULL),
(162, 43, 30, 1, 'declaracion_jurada.pdf', 'PDF', 511055, '/docs/INS-2027-1-0027/declaracion_jurada.pdf', '2026-09-22', 'APROBADO', '2026-10-16', NULL),
(163, 44, 21, 1, 'dni.png', 'IMAGEN', 599047, '/docs/INS-2027-1-0025/dni.png', '2026-09-21', 'APROBADO', '2026-10-02', NULL),
(164, 44, 22, 1, 'foto.png', 'IMAGEN', 825327, '/docs/INS-2027-1-0025/foto.png', '2026-09-21', 'OBSERVADO', '2026-10-02', 'La fotografía no tiene fondo blanco'),
(165, 44, 22, 2, 'foto_v2.png', 'IMAGEN', 770229, '/docs/INS-2027-1-0025/foto_v2.png', '2026-10-04', 'APROBADO', '2026-10-23', NULL),
(166, 44, 23, 1, 'certificado_estudios.pdf', 'PDF', 1883486, '/docs/INS-2027-1-0025/certificado_estudios.pdf', '2026-09-21', 'APROBADO', '2026-10-02', NULL),
(167, 44, 24, 1, 'constancia_tercio.pdf', 'PDF', 1674259, '/docs/INS-2027-1-0025/constancia_tercio.pdf', '2026-09-21', 'APROBADO', '2026-10-02', NULL),
(168, 44, 25, 1, 'declaracion_jurada.pdf', 'PDF', 327944, '/docs/INS-2027-1-0025/declaracion_jurada.pdf', '2026-09-21', 'APROBADO', '2026-10-02', NULL),
(169, 45, 11, 1, 'dni.png', 'IMAGEN', 1147069, '/docs/INS-2027-1-0026/dni.png', '2026-09-21', 'APROBADO', '2026-09-29', NULL),
(170, 45, 12, 1, 'foto.png', 'IMAGEN', 698830, '/docs/INS-2027-1-0026/foto.png', '2026-09-21', 'APROBADO', '2026-09-29', NULL),
(171, 45, 13, 1, 'certificado_estudios.pdf', 'PDF', 2932961, '/docs/INS-2027-1-0026/certificado_estudios.pdf', '2026-09-21', 'APROBADO', '2026-09-29', NULL),
(172, 45, 14, 1, 'declaracion_jurada.pdf', 'PDF', 717664, '/docs/INS-2027-1-0026/declaracion_jurada.pdf', '2026-09-21', 'APROBADO', '2026-09-29', NULL),
(173, 46, 31, 1, 'dni.png', 'IMAGEN', 710925, '/docs/INS-2027-1-0030/dni.png', '2026-09-26', 'APROBADO', '2026-10-15', NULL),
(174, 46, 32, 1, 'foto.png', 'IMAGEN', 732606, '/docs/INS-2027-1-0030/foto.png', '2026-09-26', 'APROBADO', '2026-10-15', NULL),
(175, 46, 33, 1, 'certificado_notas.pdf', 'PDF', 4562154, '/docs/INS-2027-1-0030/certificado_notas.pdf', '2026-09-26', 'APROBADO', '2026-10-15', NULL),
(176, 46, 34, 1, 'declaracion_jurada.pdf', 'PDF', 726706, '/docs/INS-2027-1-0030/declaracion_jurada.pdf', '2026-09-26', 'APROBADO', '2026-10-15', NULL),
(177, 47, 16, 1, 'dni.jpg', 'IMAGEN', 1769155, '/docs/INS-2027-1-0028/dni.jpg', '2026-09-25', 'OBSERVADO', '2026-10-21', 'Imagen borrosa del documento de identidad'),
(178, 47, 16, 2, 'dni_v2.jpg', 'IMAGEN', 1215893, '/docs/INS-2027-1-0028/dni_v2.jpg', '2026-10-23', 'APROBADO', '2026-11-08', NULL),
(179, 47, 17, 1, 'foto.jpg', 'IMAGEN', 519454, '/docs/INS-2027-1-0028/foto.jpg', '2026-09-25', 'APROBADO', '2026-10-21', NULL),
(180, 47, 18, 1, 'certificado_estudios.pdf', 'PDF', 4256648, '/docs/INS-2027-1-0028/certificado_estudios.pdf', '2026-09-25', 'APROBADO', '2026-10-21', NULL),
(181, 47, 19, 1, 'declaracion_jurada.pdf', 'PDF', 524898, '/docs/INS-2027-1-0028/declaracion_jurada.pdf', '2026-09-25', 'APROBADO', '2026-10-21', NULL),
(182, 48, 21, 1, 'dni.png', 'IMAGEN', 1713240, '/docs/INS-2027-1-0029/dni.png', '2026-09-25', 'APROBADO', '2026-10-09', NULL),
(183, 48, 22, 1, 'foto.png', 'IMAGEN', 166525, '/docs/INS-2027-1-0029/foto.png', '2026-09-25', 'APROBADO', '2026-10-09', NULL),
(184, 48, 23, 1, 'certificado_estudios.pdf', 'PDF', 2651894, '/docs/INS-2027-1-0029/certificado_estudios.pdf', '2026-09-25', 'APROBADO', '2026-10-09', NULL),
(185, 48, 24, 1, 'constancia_tercio.pdf', 'PDF', 1153775, '/docs/INS-2027-1-0029/constancia_tercio.pdf', '2026-09-25', 'APROBADO', '2026-10-09', NULL),
(186, 48, 25, 1, 'declaracion_jurada.pdf', 'PDF', 426589, '/docs/INS-2027-1-0029/declaracion_jurada.pdf', '2026-09-25', 'OBSERVADO', '2026-10-09', 'Declaración jurada sin firma'),
(187, 48, 25, 2, 'declaracion_jurada_v2.pdf', 'PDF', 850174, '/docs/INS-2027-1-0029/declaracion_jurada_v2.pdf', '2026-10-12', 'APROBADO', '2026-10-23', NULL),
(188, 49, 26, 1, 'dni.jpg', 'IMAGEN', 1487964, '/docs/INS-2027-1-0031/dni.jpg', '2026-09-26', 'APROBADO', '2026-10-07', NULL),
(189, 49, 27, 1, 'foto.png', 'IMAGEN', 613664, '/docs/INS-2027-1-0031/foto.png', '2026-09-26', 'APROBADO', '2026-10-07', NULL),
(190, 49, 28, 1, 'certificado_notas.pdf', 'PDF', 2986064, '/docs/INS-2027-1-0031/certificado_notas.pdf', '2026-09-26', 'APROBADO', '2026-10-07', NULL),
(191, 49, 29, 1, 'silabos.pdf', 'PDF', 2782420, '/docs/INS-2027-1-0031/silabos.pdf', '2026-09-26', 'APROBADO', '2026-10-07', NULL),
(192, 49, 30, 1, 'declaracion_jurada.pdf', 'PDF', 746953, '/docs/INS-2027-1-0031/declaracion_jurada.pdf', '2026-09-26', 'APROBADO', '2026-10-07', NULL),
(193, 50, 11, 1, 'dni.jpg', 'IMAGEN', 1230783, '/docs/INS-2027-1-0033/dni.jpg', '2026-10-01', 'APROBADO', '2026-10-11', NULL),
(194, 50, 12, 1, 'foto.jpg', 'IMAGEN', 566447, '/docs/INS-2027-1-0033/foto.jpg', '2026-10-01', 'APROBADO', '2026-10-11', NULL),
(195, 50, 13, 1, 'certificado_estudios.pdf', 'PDF', 1364638, '/docs/INS-2027-1-0033/certificado_estudios.pdf', '2026-10-01', 'APROBADO', '2026-10-11', NULL),
(196, 50, 14, 1, 'declaracion_jurada.pdf', 'PDF', 486892, '/docs/INS-2027-1-0033/declaracion_jurada.pdf', '2026-10-01', 'APROBADO', '2026-10-11', NULL),
(197, 51, 11, 1, 'dni.png', 'IMAGEN', 1067586, '/docs/INS-2027-1-0032/dni.png', '2026-09-30', 'APROBADO', '2026-10-14', NULL),
(198, 51, 12, 1, 'foto.jpg', 'IMAGEN', 868385, '/docs/INS-2027-1-0032/foto.jpg', '2026-09-30', 'APROBADO', '2026-10-14', NULL),
(199, 51, 13, 1, 'certificado_estudios.pdf', 'PDF', 2215304, '/docs/INS-2027-1-0032/certificado_estudios.pdf', '2026-09-30', 'APROBADO', '2026-10-14', NULL),
(200, 51, 14, 1, 'declaracion_jurada.pdf', 'PDF', 371142, '/docs/INS-2027-1-0032/declaracion_jurada.pdf', '2026-09-30', 'OBSERVADO', '2026-10-14', 'Declaración jurada sin firma'),
(201, 51, 14, 2, 'declaracion_jurada_v2.pdf', 'PDF', 818473, '/docs/INS-2027-1-0032/declaracion_jurada_v2.pdf', '2026-10-18', 'APROBADO', '2026-10-25', NULL),
(202, 52, 31, 1, 'dni.png', 'IMAGEN', 1508626, '/docs/INS-2027-1-0034/dni.png', '2026-10-02', 'APROBADO', '2026-10-15', NULL),
(203, 52, 32, 1, 'foto.jpg', 'IMAGEN', 159184, '/docs/INS-2027-1-0034/foto.jpg', '2026-10-02', 'OBSERVADO', '2026-10-15', 'La fotografía no tiene fondo blanco'),
(204, 52, 32, 2, 'foto_v2.png', 'IMAGEN', 661303, '/docs/INS-2027-1-0034/foto_v2.png', '2026-10-21', 'APROBADO', '2026-11-07', NULL),
(205, 52, 33, 1, 'certificado_notas.pdf', 'PDF', 4666832, '/docs/INS-2027-1-0034/certificado_notas.pdf', '2026-10-02', 'APROBADO', '2026-10-15', NULL),
(206, 52, 34, 1, 'declaracion_jurada.pdf', 'PDF', 746185, '/docs/INS-2027-1-0034/declaracion_jurada.pdf', '2026-10-02', 'APROBADO', '2026-10-15', NULL),
(207, 53, 16, 1, 'dni.png', 'IMAGEN', 1311120, '/docs/INS-2027-1-0036/dni.png', '2026-10-04', 'APROBADO', '2026-10-23', NULL),
(208, 53, 17, 1, 'foto.png', 'IMAGEN', 687021, '/docs/INS-2027-1-0036/foto.png', '2026-10-04', 'APROBADO', '2026-10-23', NULL),
(209, 53, 18, 1, 'certificado_estudios.pdf', 'PDF', 4159123, '/docs/INS-2027-1-0036/certificado_estudios.pdf', '2026-10-04', 'APROBADO', '2026-10-23', NULL),
(210, 53, 19, 1, 'declaracion_jurada.pdf', 'PDF', 556909, '/docs/INS-2027-1-0036/declaracion_jurada.pdf', '2026-10-04', 'OBSERVADO', '2026-10-23', 'Declaración jurada sin firma'),
(211, 53, 19, 2, 'declaracion_jurada_v2.pdf', 'PDF', 636567, '/docs/INS-2027-1-0036/declaracion_jurada_v2.pdf', '2026-10-26', 'PENDIENTE', NULL, NULL),
(212, 54, 21, 1, 'dni.jpg', 'IMAGEN', 597340, '/docs/INS-2027-1-0037/dni.jpg', '2026-10-04', 'APROBADO', '2026-10-18', NULL),
(213, 54, 22, 1, 'foto.png', 'IMAGEN', 909647, '/docs/INS-2027-1-0037/foto.png', '2026-10-04', 'APROBADO', '2026-10-18', NULL),
(214, 54, 23, 1, 'certificado_estudios.pdf', 'PDF', 2877016, '/docs/INS-2027-1-0037/certificado_estudios.pdf', '2026-10-04', 'OBSERVADO', '2026-10-18', 'Falta el certificado de 5.° año'),
(215, 54, 23, 2, 'certificado_estudios_v2.pdf', 'PDF', 1877671, '/docs/INS-2027-1-0037/certificado_estudios_v2.pdf', '2026-10-26', 'PENDIENTE', NULL, NULL),
(216, 54, 24, 1, 'constancia_tercio.pdf', 'PDF', 748946, '/docs/INS-2027-1-0037/constancia_tercio.pdf', '2026-10-04', 'APROBADO', '2026-10-18', NULL),
(217, 54, 25, 1, 'declaracion_jurada.pdf', 'PDF', 889022, '/docs/INS-2027-1-0037/declaracion_jurada.pdf', '2026-10-04', 'APROBADO', '2026-10-18', NULL),
(218, 56, 16, 1, 'dni.jpg', 'IMAGEN', 743313, '/docs/INS-2027-1-0035/dni.jpg', '2026-10-02', 'APROBADO', '2026-10-14', NULL),
(219, 56, 17, 1, 'foto.jpg', 'IMAGEN', 688680, '/docs/INS-2027-1-0035/foto.jpg', '2026-10-02', 'OBSERVADO', '2026-10-14', 'La fotografía no tiene fondo blanco'),
(220, 56, 17, 2, 'foto_v2.jpg', 'IMAGEN', 924671, '/docs/INS-2027-1-0035/foto_v2.jpg', '2026-10-20', 'APROBADO', '2026-11-02', NULL),
(221, 56, 18, 1, 'certificado_estudios.pdf', 'PDF', 2717843, '/docs/INS-2027-1-0035/certificado_estudios.pdf', '2026-10-02', 'APROBADO', '2026-10-14', NULL),
(222, 56, 19, 1, 'declaracion_jurada.pdf', 'PDF', 428114, '/docs/INS-2027-1-0035/declaracion_jurada.pdf', '2026-10-02', 'APROBADO', '2026-10-14', NULL),
(223, 57, 31, 1, 'dni.png', 'IMAGEN', 1573964, '/docs/INS-2027-1-0041/dni.png', '2026-10-06', 'APROBADO', '2026-10-25', NULL),
(224, 57, 32, 1, 'foto.jpg', 'IMAGEN', 414230, '/docs/INS-2027-1-0041/foto.jpg', '2026-10-06', 'APROBADO', '2026-10-25', NULL),
(225, 57, 33, 1, 'certificado_notas.pdf', 'PDF', 2095652, '/docs/INS-2027-1-0041/certificado_notas.pdf', '2026-10-06', 'APROBADO', '2026-10-25', NULL),
(226, 57, 34, 1, 'declaracion_jurada.pdf', 'PDF', 649959, '/docs/INS-2027-1-0041/declaracion_jurada.pdf', '2026-10-06', 'APROBADO', '2026-10-25', NULL),
(227, 58, 11, 1, 'dni.jpg', 'IMAGEN', 1037316, '/docs/INS-2027-1-0039/dni.jpg', '2026-10-04', 'OBSERVADO', '2026-10-18', 'Imagen borrosa del documento de identidad'),
(228, 58, 12, 1, 'foto.png', 'IMAGEN', 396897, '/docs/INS-2027-1-0039/foto.png', '2026-10-04', 'OBSERVADO', '2026-10-18', 'La fotografía no tiene fondo blanco'),
(229, 58, 13, 1, 'certificado_estudios.pdf', 'PDF', 1355780, '/docs/INS-2027-1-0039/certificado_estudios.pdf', '2026-10-04', 'APROBADO', '2026-10-18', NULL),
(230, 58, 14, 1, 'declaracion_jurada.pdf', 'PDF', 326677, '/docs/INS-2027-1-0039/declaracion_jurada.pdf', '2026-10-04', 'APROBADO', '2026-10-18', NULL),
(231, 59, 16, 1, 'dni.jpg', 'IMAGEN', 379745, '/docs/INS-2027-1-0040/dni.jpg', '2026-10-05', 'OBSERVADO', '2026-10-20', 'Documento de identidad vencido'),
(232, 59, 17, 1, 'foto.jpg', 'IMAGEN', 799671, '/docs/INS-2027-1-0040/foto.jpg', '2026-10-05', 'APROBADO', '2026-10-20', NULL),
(233, 59, 18, 1, 'certificado_estudios.pdf', 'PDF', 810973, '/docs/INS-2027-1-0040/certificado_estudios.pdf', '2026-10-05', 'APROBADO', '2026-10-20', NULL),
(234, 59, 19, 1, 'declaracion_jurada.pdf', 'PDF', 247474, '/docs/INS-2027-1-0040/declaracion_jurada.pdf', '2026-10-05', 'APROBADO', '2026-10-20', NULL),
(235, 60, 16, 1, 'dni.png', 'IMAGEN', 745831, '/docs/INS-2027-1-0042/dni.png', '2026-10-06', 'APROBADO', '2026-10-18', NULL),
(236, 60, 17, 1, 'foto.jpg', 'IMAGEN', 802187, '/docs/INS-2027-1-0042/foto.jpg', '2026-10-06', 'RECHAZADO', '2026-11-01', 'No subsanado dentro del plazo'),
(237, 60, 18, 1, 'certificado_estudios.pdf', 'PDF', 409022, '/docs/INS-2027-1-0042/certificado_estudios.pdf', '2026-10-06', 'APROBADO', '2026-10-18', NULL),
(238, 60, 19, 1, 'declaracion_jurada.pdf', 'PDF', 747803, '/docs/INS-2027-1-0042/declaracion_jurada.pdf', '2026-10-06', 'APROBADO', '2026-10-18', NULL),
(239, 61, 11, 1, 'dni.jpg', 'IMAGEN', 469617, '/docs/INS-2027-1-0045/dni.jpg', '2026-10-07', 'APROBADO', '2026-10-19', NULL),
(240, 61, 12, 1, 'foto.png', 'IMAGEN', 210233, '/docs/INS-2027-1-0045/foto.png', '2026-10-07', 'APROBADO', '2026-10-19', NULL),
(241, 61, 13, 1, 'certificado_estudios.pdf', 'PDF', 3327849, '/docs/INS-2027-1-0045/certificado_estudios.pdf', '2026-10-07', 'APROBADO', '2026-10-19', NULL),
(242, 61, 14, 1, 'declaracion_jurada.pdf', 'PDF', 754219, '/docs/INS-2027-1-0045/declaracion_jurada.pdf', '2026-10-07', 'OBSERVADO', '2026-10-19', 'Declaración jurada sin firma'),
(243, 61, 14, 2, 'declaracion_jurada_v2.pdf', 'PDF', 779323, '/docs/INS-2027-1-0045/declaracion_jurada_v2.pdf', '2026-10-26', 'PENDIENTE', NULL, NULL),
(244, 62, 26, 1, 'dni.jpg', 'IMAGEN', 1607095, '/docs/INS-2027-1-0043/dni.jpg', '2026-10-06', 'APROBADO', '2026-10-22', NULL),
(245, 62, 27, 1, 'foto.png', 'IMAGEN', 222867, '/docs/INS-2027-1-0043/foto.png', '2026-10-06', 'OBSERVADO', '2026-10-22', 'La fotografía no tiene fondo blanco'),
(246, 62, 28, 1, 'certificado_notas.pdf', 'PDF', 1380660, '/docs/INS-2027-1-0043/certificado_notas.pdf', '2026-10-06', 'APROBADO', '2026-10-22', NULL),
(247, 62, 29, 1, 'silabos.pdf', 'PDF', 2260067, '/docs/INS-2027-1-0043/silabos.pdf', '2026-10-06', 'APROBADO', '2026-10-22', NULL),
(248, 62, 30, 1, 'declaracion_jurada.pdf', 'PDF', 234459, '/docs/INS-2027-1-0043/declaracion_jurada.pdf', '2026-10-06', 'APROBADO', '2026-10-22', NULL),
(249, 63, 16, 1, 'dni.jpg', 'IMAGEN', 340078, '/docs/INS-2027-1-0044/dni.jpg', '2026-10-06', 'APROBADO', '2026-10-23', NULL),
(250, 63, 17, 1, 'foto.png', 'IMAGEN', 868588, '/docs/INS-2027-1-0044/foto.png', '2026-10-06', 'APROBADO', '2026-10-23', NULL),
(251, 63, 18, 1, 'certificado_estudios.pdf', 'PDF', 3107179, '/docs/INS-2027-1-0044/certificado_estudios.pdf', '2026-10-06', 'APROBADO', '2026-10-23', NULL),
(252, 63, 19, 1, 'declaracion_jurada.pdf', 'PDF', 488100, '/docs/INS-2027-1-0044/declaracion_jurada.pdf', '2026-10-06', 'APROBADO', '2026-10-23', NULL),
(253, 64, 16, 1, 'dni.png', 'IMAGEN', 157578, '/docs/INS-2027-1-0046/dni.png', '2026-10-08', 'APROBADO', '2026-10-21', NULL),
(254, 64, 17, 1, 'foto.jpg', 'IMAGEN', 743879, '/docs/INS-2027-1-0046/foto.jpg', '2026-10-08', 'APROBADO', '2026-10-21', NULL),
(255, 64, 18, 1, 'certificado_estudios.pdf', 'PDF', 1643821, '/docs/INS-2027-1-0046/certificado_estudios.pdf', '2026-10-08', 'APROBADO', '2026-10-21', NULL),
(256, 64, 19, 1, 'declaracion_jurada.pdf', 'PDF', 683216, '/docs/INS-2027-1-0046/declaracion_jurada.pdf', '2026-10-08', 'OBSERVADO', '2026-10-21', 'Declaración jurada sin firma'),
(257, 65, 16, 1, 'dni.jpg', 'IMAGEN', 1722164, '/docs/INS-2027-1-0047/dni.jpg', '2026-10-10', 'APROBADO', '2026-10-22', NULL),
(258, 65, 17, 1, 'foto.jpg', 'IMAGEN', 629285, '/docs/INS-2027-1-0047/foto.jpg', '2026-10-10', 'OBSERVADO', '2026-10-22', 'La fotografía no tiene fondo blanco'),
(259, 65, 18, 1, 'certificado_estudios.pdf', 'PDF', 1376384, '/docs/INS-2027-1-0047/certificado_estudios.pdf', '2026-10-10', 'APROBADO', '2026-10-22', NULL),
(260, 65, 19, 1, 'declaracion_jurada.pdf', 'PDF', 220574, '/docs/INS-2027-1-0047/declaracion_jurada.pdf', '2026-10-10', 'APROBADO', '2026-10-22', NULL),
(261, 66, 16, 1, 'dni.png', 'IMAGEN', 784445, '/docs/INS-2027-1-0048/dni.png', '2026-10-11', 'APROBADO', '2026-11-03', NULL),
(262, 66, 17, 1, 'foto.png', 'IMAGEN', 314534, '/docs/INS-2027-1-0048/foto.png', '2026-10-11', 'OBSERVADO', '2026-11-03', 'La fotografía no tiene fondo blanco'),
(263, 66, 17, 2, 'foto_v2.jpg', 'IMAGEN', 271332, '/docs/INS-2027-1-0048/foto_v2.jpg', '2026-11-07', 'PENDIENTE', NULL, NULL),
(264, 66, 18, 1, 'certificado_estudios.pdf', 'PDF', 2804047, '/docs/INS-2027-1-0048/certificado_estudios.pdf', '2026-10-11', 'APROBADO', '2026-11-03', NULL),
(265, 66, 19, 1, 'declaracion_jurada.pdf', 'PDF', 644857, '/docs/INS-2027-1-0048/declaracion_jurada.pdf', '2026-10-11', 'APROBADO', '2026-11-03', NULL),
(266, 67, 16, 1, 'dni.png', 'IMAGEN', 165383, '/docs/INS-2027-1-0049/dni.png', '2026-10-12', 'APROBADO', '2026-10-30', NULL),
(267, 67, 17, 1, 'foto.jpg', 'IMAGEN', 497156, '/docs/INS-2027-1-0049/foto.jpg', '2026-10-12', 'APROBADO', '2026-10-30', NULL),
(268, 67, 18, 1, 'certificado_estudios.pdf', 'PDF', 379164, '/docs/INS-2027-1-0049/certificado_estudios.pdf', '2026-10-12', 'OBSERVADO', '2026-10-30', 'Falta el certificado de 5.° año'),
(269, 67, 19, 1, 'declaracion_jurada.pdf', 'PDF', 478244, '/docs/INS-2027-1-0049/declaracion_jurada.pdf', '2026-10-12', 'APROBADO', '2026-10-30', NULL),
(270, 68, 11, 1, 'dni.jpg', 'IMAGEN', 1780806, '/docs/INS-2027-1-0050/dni.jpg', '2026-10-13', 'APROBADO', '2026-11-03', NULL),
(271, 68, 12, 1, 'foto.png', 'IMAGEN', 825210, '/docs/INS-2027-1-0050/foto.png', '2026-10-13', 'APROBADO', '2026-11-03', NULL),
(272, 68, 13, 1, 'certificado_estudios.pdf', 'PDF', 3383646, '/docs/INS-2027-1-0050/certificado_estudios.pdf', '2026-10-13', 'APROBADO', '2026-11-03', NULL),
(273, 68, 14, 1, 'declaracion_jurada.pdf', 'PDF', 809453, '/docs/INS-2027-1-0050/declaracion_jurada.pdf', '2026-10-13', 'APROBADO', '2026-11-03', NULL),
(274, 69, 16, 1, 'dni.png', 'IMAGEN', 638505, '/docs/INS-2027-1-0051/dni.png', '2026-10-13', 'APROBADO', '2026-10-25', NULL),
(275, 69, 17, 1, 'foto.jpg', 'IMAGEN', 312008, '/docs/INS-2027-1-0051/foto.jpg', '2026-10-13', 'APROBADO', '2026-10-25', NULL),
(276, 69, 18, 1, 'certificado_estudios.pdf', 'PDF', 3082997, '/docs/INS-2027-1-0051/certificado_estudios.pdf', '2026-10-13', 'APROBADO', '2026-10-25', NULL),
(277, 69, 19, 1, 'declaracion_jurada.pdf', 'PDF', 296988, '/docs/INS-2027-1-0051/declaracion_jurada.pdf', '2026-10-13', 'APROBADO', '2026-10-25', NULL),
(278, 70, 31, 1, 'dni.jpg', 'IMAGEN', 1465172, '/docs/INS-2027-1-0052/dni.jpg', '2026-10-15', 'APROBADO', '2026-11-02', NULL),
(279, 70, 32, 1, 'foto.png', 'IMAGEN', 679986, '/docs/INS-2027-1-0052/foto.png', '2026-10-15', 'APROBADO', '2026-11-02', NULL),
(280, 70, 33, 1, 'certificado_notas.pdf', 'PDF', 2651981, '/docs/INS-2027-1-0052/certificado_notas.pdf', '2026-10-15', 'APROBADO', '2026-11-02', NULL),
(281, 70, 34, 1, 'declaracion_jurada.pdf', 'PDF', 314697, '/docs/INS-2027-1-0052/declaracion_jurada.pdf', '2026-10-15', 'APROBADO', '2026-11-02', NULL),
(282, 71, 31, 1, 'dni.jpg', 'IMAGEN', 860314, '/docs/INS-2027-1-0053/dni.jpg', '2026-10-17', 'PENDIENTE', NULL, NULL),
(283, 71, 32, 1, 'foto.jpg', 'IMAGEN', 630899, '/docs/INS-2027-1-0053/foto.jpg', '2026-10-17', 'PENDIENTE', NULL, NULL),
(284, 71, 33, 1, 'certificado_notas.pdf', 'PDF', 2067626, '/docs/INS-2027-1-0053/certificado_notas.pdf', '2026-10-17', 'PENDIENTE', NULL, NULL),
(285, 71, 34, 1, 'declaracion_jurada.pdf', 'PDF', 688869, '/docs/INS-2027-1-0053/declaracion_jurada.pdf', '2026-10-17', 'PENDIENTE', NULL, NULL),
(286, 72, 11, 1, 'dni.jpg', 'IMAGEN', 184782, '/docs/INS-2027-1-0054/dni.jpg', '2026-10-18', 'APROBADO', '2026-10-30', NULL),
(287, 72, 12, 1, 'foto.jpg', 'IMAGEN', 649724, '/docs/INS-2027-1-0054/foto.jpg', '2026-10-18', 'APROBADO', '2026-10-30', NULL),
(288, 72, 13, 1, 'certificado_estudios.pdf', 'PDF', 3201973, '/docs/INS-2027-1-0054/certificado_estudios.pdf', '2026-10-18', 'APROBADO', '2026-10-30', NULL),
(289, 72, 14, 1, 'declaracion_jurada.pdf', 'PDF', 315987, '/docs/INS-2027-1-0054/declaracion_jurada.pdf', '2026-10-18', 'APROBADO', '2026-10-30', NULL),
(290, 73, 16, 1, 'dni.jpg', 'IMAGEN', 525913, '/docs/INS-2027-1-0057/dni.jpg', '2026-10-20', 'OBSERVADO', '2026-11-05', 'Imagen borrosa del documento de identidad'),
(291, 73, 17, 1, 'foto.jpg', 'IMAGEN', 817405, '/docs/INS-2027-1-0057/foto.jpg', '2026-10-20', 'APROBADO', '2026-11-05', NULL),
(292, 73, 18, 1, 'certificado_estudios.pdf', 'PDF', 3754433, '/docs/INS-2027-1-0057/certificado_estudios.pdf', '2026-10-20', 'APROBADO', '2026-11-05', NULL),
(293, 73, 19, 1, 'declaracion_jurada.pdf', 'PDF', 630553, '/docs/INS-2027-1-0057/declaracion_jurada.pdf', '2026-10-20', 'APROBADO', '2026-11-05', NULL),
(294, 74, 31, 1, 'dni.png', 'IMAGEN', 1043778, '/docs/INS-2027-1-0056/dni.png', '2026-10-19', 'APROBADO', '2026-11-06', NULL),
(295, 74, 32, 1, 'foto.png', 'IMAGEN', 893199, '/docs/INS-2027-1-0056/foto.png', '2026-10-19', 'APROBADO', '2026-11-06', NULL),
(296, 74, 33, 1, 'certificado_notas.pdf', 'PDF', 3490815, '/docs/INS-2027-1-0056/certificado_notas.pdf', '2026-10-19', 'OBSERVADO', '2026-11-06', 'Certificado de notas incompleto o borroso'),
(297, 74, 34, 1, 'declaracion_jurada.pdf', 'PDF', 810476, '/docs/INS-2027-1-0056/declaracion_jurada.pdf', '2026-10-19', 'APROBADO', '2026-11-06', NULL),
(298, 75, 16, 1, 'dni.jpg', 'IMAGEN', 1677035, '/docs/INS-2027-1-0058/dni.jpg', '2026-10-20', 'APROBADO', '2026-10-30', NULL),
(299, 75, 17, 1, 'foto.jpg', 'IMAGEN', 479679, '/docs/INS-2027-1-0058/foto.jpg', '2026-10-20', 'APROBADO', '2026-10-30', NULL),
(300, 75, 18, 1, 'certificado_estudios.pdf', 'PDF', 821188, '/docs/INS-2027-1-0058/certificado_estudios.pdf', '2026-10-20', 'APROBADO', '2026-10-30', NULL),
(301, 75, 19, 1, 'declaracion_jurada.pdf', 'PDF', 285605, '/docs/INS-2027-1-0058/declaracion_jurada.pdf', '2026-10-20', 'APROBADO', '2026-10-30', NULL),
(302, 76, 11, 1, 'dni.png', 'IMAGEN', 990443, '/docs/INS-2027-1-0055/dni.png', '2026-10-18', 'APROBADO', '2026-10-28', NULL),
(303, 76, 12, 1, 'foto.png', 'IMAGEN', 409127, '/docs/INS-2027-1-0055/foto.png', '2026-10-18', 'APROBADO', '2026-10-28', NULL),
(304, 76, 13, 1, 'certificado_estudios.pdf', 'PDF', 1374635, '/docs/INS-2027-1-0055/certificado_estudios.pdf', '2026-10-18', 'APROBADO', '2026-10-28', NULL),
(305, 76, 14, 1, 'declaracion_jurada.pdf', 'PDF', 188302, '/docs/INS-2027-1-0055/declaracion_jurada.pdf', '2026-10-18', 'OBSERVADO', '2026-10-28', 'Declaración jurada sin firma'),
(306, 76, 14, 2, 'declaracion_jurada_v2.pdf', 'PDF', 718112, '/docs/INS-2027-1-0055/declaracion_jurada_v2.pdf', '2026-11-04', 'PENDIENTE', NULL, NULL),
(307, 77, 26, 1, 'dni.png', 'IMAGEN', 555190, '/docs/INS-2027-1-0059/dni.png', '2026-10-24', 'APROBADO', '2026-11-04', NULL),
(308, 77, 27, 1, 'foto.png', 'IMAGEN', 509561, '/docs/INS-2027-1-0059/foto.png', '2026-10-24', 'APROBADO', '2026-10-30', NULL),
(309, 77, 28, 1, 'certificado_notas.pdf', 'PDF', 3286375, '/docs/INS-2027-1-0059/certificado_notas.pdf', '2026-10-24', 'APROBADO', '2026-11-10', NULL),
(310, 77, 29, 1, 'silabos.pdf', 'PDF', 6815587, '/docs/INS-2027-1-0059/silabos.pdf', '2026-10-24', 'PENDIENTE', NULL, NULL),
(311, 77, 30, 1, 'declaracion_jurada.pdf', 'PDF', 787025, '/docs/INS-2027-1-0059/declaracion_jurada.pdf', '2026-10-24', 'PENDIENTE', NULL, NULL),
(312, 78, 11, 1, 'dni.jpg', 'IMAGEN', 613676, '/docs/INS-2027-1-0060/dni.jpg', '2026-10-25', 'OBSERVADO', '2026-11-08', 'Imagen borrosa del documento de identidad'),
(313, 78, 12, 1, 'foto.jpg', 'IMAGEN', 261728, '/docs/INS-2027-1-0060/foto.jpg', '2026-10-25', 'APROBADO', '2026-11-08', NULL),
(314, 78, 13, 1, 'certificado_estudios.pdf', 'PDF', 4091787, '/docs/INS-2027-1-0060/certificado_estudios.pdf', '2026-10-25', 'APROBADO', '2026-11-08', NULL),
(315, 78, 14, 1, 'declaracion_jurada.pdf', 'PDF', 911428, '/docs/INS-2027-1-0060/declaracion_jurada.pdf', '2026-10-25', 'APROBADO', '2026-11-08', NULL),
(316, 79, 11, 1, 'dni.png', 'IMAGEN', 423212, '/docs/INS-2027-1-0061/dni.png', '2026-10-28', 'PENDIENTE', NULL, NULL),
(317, 79, 12, 1, 'foto.jpg', 'IMAGEN', 603544, '/docs/INS-2027-1-0061/foto.jpg', '2026-10-28', 'PENDIENTE', NULL, NULL),
(318, 79, 13, 1, 'certificado_estudios.pdf', 'PDF', 2605761, '/docs/INS-2027-1-0061/certificado_estudios.pdf', '2026-10-28', 'PENDIENTE', NULL, NULL),
(319, 79, 14, 1, 'declaracion_jurada.pdf', 'PDF', 261875, '/docs/INS-2027-1-0061/declaracion_jurada.pdf', '2026-10-28', 'PENDIENTE', NULL, NULL),
(320, 80, 21, 1, 'dni.jpg', 'IMAGEN', 1496010, '/docs/INS-2027-1-0062/dni.jpg', '2026-10-28', 'PENDIENTE', NULL, NULL),
(321, 80, 22, 1, 'foto.png', 'IMAGEN', 156031, '/docs/INS-2027-1-0062/foto.png', '2026-10-28', 'PENDIENTE', NULL, NULL),
(322, 80, 23, 1, 'certificado_estudios.pdf', 'PDF', 4478276, '/docs/INS-2027-1-0062/certificado_estudios.pdf', '2026-10-28', 'PENDIENTE', NULL, NULL),
(323, 80, 24, 1, 'constancia_tercio.pdf', 'PDF', 424528, '/docs/INS-2027-1-0062/constancia_tercio.pdf', '2026-10-28', 'PENDIENTE', NULL, NULL),
(324, 80, 25, 1, 'declaracion_jurada.pdf', 'PDF', 217188, '/docs/INS-2027-1-0062/declaracion_jurada.pdf', '2026-10-28', 'PENDIENTE', NULL, NULL),
(325, 81, 11, 1, 'dni.png', 'IMAGEN', 1619208, '/docs/INS-2027-1-0064/dni.png', '2026-10-29', 'APROBADO', '2026-11-05', NULL),
(326, 81, 12, 1, 'foto.png', 'IMAGEN', 338865, '/docs/INS-2027-1-0064/foto.png', '2026-10-29', 'PENDIENTE', NULL, NULL),
(327, 81, 13, 1, 'certificado_estudios.pdf', 'PDF', 3316937, '/docs/INS-2027-1-0064/certificado_estudios.pdf', '2026-10-29', 'PENDIENTE', NULL, NULL),
(328, 81, 14, 1, 'declaracion_jurada.pdf', 'PDF', 328359, '/docs/INS-2027-1-0064/declaracion_jurada.pdf', '2026-10-29', 'PENDIENTE', NULL, NULL),
(329, 81, 15, 1, 'carne_conadis.jpg', 'IMAGEN', 1829162, '/docs/INS-2027-1-0064/carne_conadis.jpg', '2026-10-29', 'PENDIENTE', NULL, NULL),
(330, 82, 16, 1, 'dni.jpg', 'IMAGEN', 1612318, '/docs/INS-2027-1-0065/dni.jpg', '2026-10-31', 'PENDIENTE', NULL, NULL),
(331, 82, 17, 1, 'foto.jpg', 'IMAGEN', 847788, '/docs/INS-2027-1-0065/foto.jpg', '2026-10-31', 'PENDIENTE', NULL, NULL),
(332, 82, 18, 1, 'certificado_estudios.pdf', 'PDF', 4085048, '/docs/INS-2027-1-0065/certificado_estudios.pdf', '2026-10-31', 'PENDIENTE', NULL, NULL),
(333, 82, 19, 1, 'declaracion_jurada.pdf', 'PDF', 928547, '/docs/INS-2027-1-0065/declaracion_jurada.pdf', '2026-10-31', 'PENDIENTE', NULL, NULL),
(334, 83, 16, 1, 'dni.jpg', 'IMAGEN', 1515837, '/docs/INS-2027-1-0063/dni.jpg', '2026-10-28', 'PENDIENTE', NULL, NULL),
(335, 83, 17, 1, 'foto.png', 'IMAGEN', 865441, '/docs/INS-2027-1-0063/foto.png', '2026-10-28', 'PENDIENTE', NULL, NULL),
(336, 83, 18, 1, 'certificado_estudios.pdf', 'PDF', 3785888, '/docs/INS-2027-1-0063/certificado_estudios.pdf', '2026-10-28', 'PENDIENTE', NULL, NULL),
(337, 83, 19, 1, 'declaracion_jurada.pdf', 'PDF', 415971, '/docs/INS-2027-1-0063/declaracion_jurada.pdf', '2026-10-28', 'PENDIENTE', NULL, NULL),
(338, 84, 16, 1, 'dni.png', 'IMAGEN', 1555834, '/docs/INS-2027-1-0067/dni.png', '2026-11-01', 'PENDIENTE', NULL, NULL),
(339, 84, 17, 1, 'foto.jpg', 'IMAGEN', 150378, '/docs/INS-2027-1-0067/foto.jpg', '2026-11-01', 'PENDIENTE', NULL, NULL),
(340, 84, 18, 1, 'certificado_estudios.pdf', 'PDF', 4201830, '/docs/INS-2027-1-0067/certificado_estudios.pdf', '2026-11-01', 'PENDIENTE', NULL, NULL),
(341, 84, 19, 1, 'declaracion_jurada.pdf', 'PDF', 805554, '/docs/INS-2027-1-0067/declaracion_jurada.pdf', '2026-11-01', 'PENDIENTE', NULL, NULL),
(342, 85, 16, 1, 'dni.png', 'IMAGEN', 733110, '/docs/INS-2027-1-0066/dni.png', '2026-10-31', 'PENDIENTE', NULL, NULL),
(343, 85, 17, 1, 'foto.jpg', 'IMAGEN', 221588, '/docs/INS-2027-1-0066/foto.jpg', '2026-10-31', 'PENDIENTE', NULL, NULL),
(344, 85, 18, 1, 'certificado_estudios.pdf', 'PDF', 2983141, '/docs/INS-2027-1-0066/certificado_estudios.pdf', '2026-10-31', 'PENDIENTE', NULL, NULL),
(345, 85, 19, 1, 'declaracion_jurada.pdf', 'PDF', 778162, '/docs/INS-2027-1-0066/declaracion_jurada.pdf', '2026-10-31', 'PENDIENTE', NULL, NULL),
(346, 86, 11, 1, 'dni.jpg', 'IMAGEN', 1141343, '/docs/BORRADOR-86/dni.jpg', '2026-11-01', 'PENDIENTE', NULL, NULL),
(347, 86, 12, 1, 'foto.jpg', 'IMAGEN', 925725, '/docs/BORRADOR-86/foto.jpg', '2026-11-01', 'PENDIENTE', NULL, NULL),
(348, 87, 26, 1, 'dni.png', 'IMAGEN', 1042061, '/docs/INS-2027-1-0068/dni.png', '2026-11-04', 'PENDIENTE', NULL, NULL),
(349, 87, 27, 1, 'foto.png', 'IMAGEN', 604269, '/docs/INS-2027-1-0068/foto.png', '2026-11-04', 'PENDIENTE', NULL, NULL),
(350, 87, 28, 1, 'certificado_notas.pdf', 'PDF', 3788275, '/docs/INS-2027-1-0068/certificado_notas.pdf', '2026-11-04', 'PENDIENTE', NULL, NULL),
(351, 87, 29, 1, 'silabos.pdf', 'PDF', 4919279, '/docs/INS-2027-1-0068/silabos.pdf', '2026-11-04', 'PENDIENTE', NULL, NULL),
(352, 87, 30, 1, 'declaracion_jurada.pdf', 'PDF', 740008, '/docs/INS-2027-1-0068/declaracion_jurada.pdf', '2026-11-04', 'PENDIENTE', NULL, NULL),
(353, 88, 16, 1, 'dni.png', 'IMAGEN', 1479353, '/docs/INS-2027-1-0069/dni.png', '2026-11-04', 'PENDIENTE', NULL, NULL),
(354, 88, 17, 1, 'foto.jpg', 'IMAGEN', 470651, '/docs/INS-2027-1-0069/foto.jpg', '2026-11-04', 'PENDIENTE', NULL, NULL),
(355, 88, 18, 1, 'certificado_estudios.pdf', 'PDF', 4407793, '/docs/INS-2027-1-0069/certificado_estudios.pdf', '2026-11-04', 'PENDIENTE', NULL, NULL),
(356, 88, 19, 1, 'declaracion_jurada.pdf', 'PDF', 233743, '/docs/INS-2027-1-0069/declaracion_jurada.pdf', '2026-11-04', 'PENDIENTE', NULL, NULL),
(357, 89, 11, 1, 'dni.png', 'IMAGEN', 1495368, '/docs/BORRADOR-89/dni.png', '2026-11-06', 'PENDIENTE', NULL, NULL),
(358, 89, 12, 1, 'foto.jpg', 'IMAGEN', 697405, '/docs/BORRADOR-89/foto.jpg', '2026-11-06', 'PENDIENTE', NULL, NULL),
(359, 89, 13, 1, 'certificado_estudios.pdf', 'PDF', 3612510, '/docs/BORRADOR-89/certificado_estudios.pdf', '2026-11-06', 'PENDIENTE', NULL, NULL),
(360, 90, 21, 1, 'dni.jpg', 'IMAGEN', 327687, '/docs/INS-2027-1-0070/dni.jpg', '2026-11-06', 'APROBADO', '2026-11-09', NULL),
(361, 90, 22, 1, 'foto.png', 'IMAGEN', 672272, '/docs/INS-2027-1-0070/foto.png', '2026-11-06', 'PENDIENTE', NULL, NULL),
(362, 90, 23, 1, 'certificado_estudios.pdf', 'PDF', 551915, '/docs/INS-2027-1-0070/certificado_estudios.pdf', '2026-11-06', 'PENDIENTE', NULL, NULL),
(363, 90, 24, 1, 'constancia_tercio.pdf', 'PDF', 1581011, '/docs/INS-2027-1-0070/constancia_tercio.pdf', '2026-11-06', 'PENDIENTE', NULL, NULL),
(364, 90, 25, 1, 'declaracion_jurada.pdf', 'PDF', 323360, '/docs/INS-2027-1-0070/declaracion_jurada.pdf', '2026-11-06', 'PENDIENTE', NULL, NULL);


-- Observaciones
INSERT INTO documento_observacion (id, documento_id, evaluador_id, tipo_observacion, descripcion, fecha_observacion, estado_observacion, fecha_subsanacion, comentario_subsanacion) VALUES
(4, 59, 20, 'FALTANTE', 'Falta el certificado de 5.° año', '2026-05-15', 'PENDIENTE', NULL, NULL),
(5, 61, 20, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-05-15', 'SUBSANADA', '2026-05-22', 'Se cargó una foto con fondo blanco'),
(6, 65, 18, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-05-21', 'SUBSANADA', '2026-05-27', 'Se cargó una foto con fondo blanco'),
(7, 74, 17, 'ILEGIBLE', 'Imagen borrosa del documento de identidad', '2026-06-16', 'PENDIENTE', NULL, NULL),
(8, 86, 20, 'ILEGIBLE', 'No se leen las calificaciones del certificado', '2026-09-17', 'SUBSANADA', '2026-09-20', 'Se cargó un escaneo legible'),
(9, 99, 3, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-09-21', 'SUBSANADA', '2026-09-26', 'Se cargó una foto con fondo blanco'),
(10, 106, 3, 'FALTANTE', 'Falta el certificado de 5.° año', '2026-09-27', 'SUBSANADA', '2026-10-01', 'Se cargó el certificado completo'),
(11, 116, 20, 'ILEGIBLE', 'Certificado de notas incompleto o borroso', '2026-09-28', 'SUBSANADA', '2026-10-01', 'Se cargó el certificado completo'),
(12, 120, 17, 'VENCIDO', 'Documento de identidad vencido', '2026-10-06', 'SUBSANADA', '2026-10-11', 'Se cargó el documento renovado'),
(13, 127, 18, 'ILEGIBLE', 'No se leen las calificaciones del certificado', '2026-10-06', 'SUBSANADA', '2026-10-12', 'Se cargó un escaneo legible'),
(14, 137, 3, 'OTRO', 'Declaración jurada sin firma', '2026-09-27', 'SUBSANADA', '2026-10-04', 'Se cargó la declaración firmada'),
(15, 145, 17, 'FALTANTE', 'Falta el certificado de 5.° año', '2026-10-02', 'SUBSANADA', '2026-10-10', 'Se cargó el certificado completo'),
(16, 148, 18, 'VENCIDO', 'Documento de identidad vencido', '2026-10-04', 'PENDIENTE', NULL, NULL),
(17, 164, 18, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-10-02', 'SUBSANADA', '2026-10-04', 'Se cargó una foto con fondo blanco'),
(18, 177, 2, 'ILEGIBLE', 'Imagen borrosa del documento de identidad', '2026-10-21', 'SUBSANADA', '2026-10-23', 'Se cargó una imagen nítida'),
(19, 186, 2, 'OTRO', 'Declaración jurada sin firma', '2026-10-09', 'SUBSANADA', '2026-10-12', 'Se cargó la declaración firmada'),
(20, 200, 17, 'OTRO', 'Declaración jurada sin firma', '2026-10-14', 'SUBSANADA', '2026-10-18', 'Se cargó la declaración firmada'),
(21, 203, 18, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-10-15', 'SUBSANADA', '2026-10-21', 'Se cargó una foto con fondo blanco'),
(22, 210, 20, 'OTRO', 'Declaración jurada sin firma', '2026-10-23', 'SUBSANADA', '2026-10-26', 'Se cargó la declaración firmada'),
(23, 214, 17, 'FALTANTE', 'Falta el certificado de 5.° año', '2026-10-18', 'SUBSANADA', '2026-10-26', 'Se cargó el certificado completo'),
(24, 219, 2, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-10-14', 'SUBSANADA', '2026-10-20', 'Se cargó una foto con fondo blanco'),
(25, 227, 3, 'ILEGIBLE', 'Imagen borrosa del documento de identidad', '2026-10-18', 'PENDIENTE', NULL, NULL),
(26, 228, 3, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-10-18', 'PENDIENTE', NULL, NULL),
(27, 231, 17, 'VENCIDO', 'Documento de identidad vencido', '2026-10-20', 'PENDIENTE', NULL, NULL),
(28, 236, 20, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-10-18', 'PENDIENTE', NULL, NULL),
(29, 242, 17, 'OTRO', 'Declaración jurada sin firma', '2026-10-19', 'SUBSANADA', '2026-10-26', 'Se cargó la declaración firmada'),
(30, 245, 17, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-10-22', 'PENDIENTE', NULL, NULL),
(31, 256, 20, 'OTRO', 'Declaración jurada sin firma', '2026-10-21', 'PENDIENTE', NULL, NULL),
(32, 258, 2, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-10-22', 'PENDIENTE', NULL, NULL),
(33, 262, 3, 'OTRO', 'La fotografía no tiene fondo blanco', '2026-11-03', 'SUBSANADA', '2026-11-07', 'Se cargó una foto con fondo blanco'),
(34, 268, 3, 'FALTANTE', 'Falta el certificado de 5.° año', '2026-10-30', 'PENDIENTE', NULL, NULL),
(35, 290, 17, 'ILEGIBLE', 'Imagen borrosa del documento de identidad', '2026-11-05', 'PENDIENTE', NULL, NULL),
(36, 296, 3, 'ILEGIBLE', 'Certificado de notas incompleto o borroso', '2026-11-06', 'PENDIENTE', NULL, NULL),
(37, 305, 18, 'OTRO', 'Declaración jurada sin firma', '2026-10-28', 'SUBSANADA', '2026-11-04', 'Se cargó la declaración firmada'),
(38, 312, 18, 'ILEGIBLE', 'Imagen borrosa del documento de identidad', '2026-11-08', 'PENDIENTE', NULL, NULL);


-- Carnés
INSERT INTO carne_postulante (id, postulacion_id, sede_id, codigo_carne, fecha_generacion, fecha_inicio_vigencia, fecha_fin_vigencia, aula_examen) VALUES
(4, 11, 1, 'CRN-2026-2-0002', '2026-04-28', '2026-04-28', '2026-07-12', 'E-314'),
(5, 12, 1, 'CRN-2026-2-0003', '2026-05-02', '2026-05-02', '2026-07-12', 'H-214'),
(6, 13, 1, 'CRN-2026-2-0004', '2026-04-23', '2026-04-23', '2026-07-12', 'H-313'),
(7, 14, 1, 'CRN-2026-2-0005', '2026-04-28', '2026-04-28', '2026-07-12', 'M-206'),
(8, 15, 1, 'CRN-2026-2-0006', '2026-04-30', '2026-04-30', '2026-07-12', 'M-304'),
(9, 16, 2, 'CRN-2026-2-0007', '2026-05-08', '2026-05-08', '2026-07-12', 'H-304'),
(10, 18, 2, 'CRN-2026-2-0008', '2026-06-06', '2026-06-06', '2026-07-12', 'M-204'),
(11, 19, 3, 'CRN-2026-2-0009', '2026-06-18', '2026-06-18', '2026-07-12', 'H-206'),
(12, 21, 1, 'CRN-2026-2-0010', '2026-06-16', '2026-06-16', '2026-07-12', 'A-202'),
(13, 24, 1, 'CRN-2026-2-0011', '2026-06-29', '2026-06-29', '2026-07-12', 'M-110'),
(14, 25, 1, 'CRN-2026-2-0012', '2026-06-29', '2026-06-29', '2026-07-12', 'B-202'),
(15, 26, 1, 'CRN-2027-1-0003', '2026-10-03', '2026-10-03', '2027-01-24', 'H-115'),
(16, 27, 1, 'CRN-2027-1-0004', '2026-09-21', '2026-09-21', '2027-01-24', 'B-212'),
(17, 28, 2, 'CRN-2027-1-0005', '2026-10-06', '2026-10-06', '2027-01-24', 'E-203'),
(18, 30, 1, 'CRN-2027-1-0006', '2026-10-24', '2026-10-24', '2027-01-24', 'B-204'),
(19, 32, 2, 'CRN-2027-1-0007', '2026-10-14', '2026-10-14', '2027-01-24', 'A-113'),
(20, 33, 3, 'CRN-2027-1-0008', '2026-10-30', '2026-10-30', '2027-01-24', 'B-214'),
(21, 34, 1, 'CRN-2027-1-0009', '2026-11-02', '2026-11-02', '2027-01-24', 'A-210'),
(22, 36, 2, 'CRN-2027-1-0010', '2026-10-22', '2026-10-22', '2027-01-24', 'H-211'),
(23, 37, 2, 'CRN-2027-1-0011', '2026-10-07', '2026-10-07', '2027-01-24', 'A-101'),
(24, 39, 1, 'CRN-2027-1-0012', '2026-11-02', '2026-11-02', '2027-01-24', 'E-311'),
(25, 41, 3, 'CRN-2027-1-0013', '2026-10-06', '2026-10-06', '2027-01-24', 'B-212'),
(26, 45, 1, 'CRN-2027-1-0014', '2026-10-08', '2026-10-08', '2027-01-24', 'M-312'),
(27, 46, 2, 'CRN-2027-1-0015', '2026-10-25', '2026-10-25', '2027-01-24', 'M-204'),
(28, 48, 1, 'CRN-2027-1-0016', '2026-10-26', '2026-10-26', '2027-01-24', 'M-207'),
(29, 49, 3, 'CRN-2027-1-0017', '2026-10-12', '2026-10-12', '2027-01-24', 'E-104'),
(30, 50, 1, 'CRN-2027-1-0018', '2026-10-17', '2026-10-17', '2027-01-24', 'M-205'),
(31, 51, 1, 'CRN-2027-1-0019', '2026-11-02', '2026-11-02', '2027-01-24', 'A-311'),
(32, 63, 2, 'CRN-2027-1-0020', '2026-10-27', '2026-10-27', '2027-01-24', 'H-203'),
(33, 69, 1, 'CRN-2027-1-0021', '2026-10-31', '2026-10-31', '2027-01-24', 'H-203'),
(34, 70, 3, 'CRN-2027-1-0022', '2026-11-06', '2026-11-06', '2027-01-24', 'A-102'),
(35, 72, 1, 'CRN-2027-1-0023', '2026-11-07', '2026-11-07', '2027-01-24', 'E-209'),
(36, 75, 3, 'CRN-2027-1-0024', '2026-11-08', '2026-11-08', '2027-01-24', 'B-102');


-- Notificaciones
INSERT INTO notificacion (id, postulacion_id, observacion_origen_id, medio_notificacion, tipo_notificacion, destinatario, asunto, mensaje, fecha_programada, fecha_envio, estado_envio, leida, fecha_lectura) VALUES
(15, 11, NULL, 'CORREO', 'VALIDACION', 'luciana.aguilar@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-02', '2026-04-02', 'ENVIADA', TRUE, '2026-04-02'),
(16, 11, NULL, 'CORREO', 'PAGO', 'luciana.aguilar@mail.com', 'Pago aprobado', 'Tu pago PAG-000011 fue aprobado.', '2026-04-09', '2026-04-09', 'ENVIADA', TRUE, '2026-04-09'),
(17, 11, NULL, 'CORREO', 'FINALIZACION', 'luciana.aguilar@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-04-28', '2026-04-28', 'ENVIADA', TRUE, '2026-04-29'),
(18, 11, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'luciana.aguilar@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula E-314.', '2026-04-28', '2026-04-28', 'ENVIADA', TRUE, '2026-04-28'),
(19, 12, NULL, 'CORREO', 'VALIDACION', 'alessandra.navarro@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-03', '2026-04-03', 'ENVIADA', TRUE, '2026-04-04'),
(20, 12, NULL, 'CORREO', 'PAGO', 'alessandra.navarro@mail.com', 'Pago aprobado', 'Tu pago PAG-000012 fue aprobado.', '2026-04-06', '2026-04-06', 'ENVIADA', TRUE, '2026-04-06'),
(21, 12, NULL, 'CORREO', 'FINALIZACION', 'alessandra.navarro@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-05-02', '2026-05-02', 'ENVIADA', TRUE, '2026-05-03'),
(22, 12, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'alessandra.navarro@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-214.', '2026-05-02', '2026-05-02', 'ENVIADA', TRUE, '2026-05-03'),
(23, 13, NULL, 'CORREO', 'VALIDACION', 'micaela.leon@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-04', '2026-04-04', 'ENVIADA', TRUE, '2026-04-05'),
(24, 13, NULL, 'CORREO', 'PAGO', 'micaela.leon@mail.com', 'Pago aprobado', 'Tu pago PAG-000013 fue aprobado.', '2026-04-08', '2026-04-08', 'ENVIADA', TRUE, '2026-04-08'),
(25, 13, NULL, 'CORREO', 'FINALIZACION', 'micaela.leon@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-04-23', '2026-04-23', 'ENVIADA', TRUE, '2026-04-25'),
(26, 13, NULL, 'CORREO', 'FINALIZACION', 'edwin.leon@mail.com', 'Inscripción finalizada', 'La inscripción de Micaela León fue completada.', '2026-04-23', '2026-04-23', 'ENVIADA', TRUE, '2026-04-25'),
(27, 13, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'micaela.leon@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-313.', '2026-04-23', '2026-04-23', 'ENVIADA', TRUE, '2026-04-25'),
(28, 14, NULL, 'CORREO', 'VALIDACION', 'kevin.rios@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-04', '2026-04-04', 'ENVIADA', TRUE, '2026-04-04'),
(29, 14, NULL, 'CORREO', 'PAGO', 'kevin.rios@mail.com', 'Pago aprobado', 'Tu pago PAG-000014 fue aprobado.', '2026-04-07', '2026-04-07', 'ENVIADA', FALSE, NULL),
(30, 14, NULL, 'CORREO', 'FINALIZACION', 'kevin.rios@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-04-28', '2026-04-28', 'ENVIADA', TRUE, '2026-04-30'),
(31, 14, NULL, 'CORREO', 'FINALIZACION', 'rosa.zevallos@mail.com', 'Inscripción finalizada', 'La inscripción de Kevin Ríos fue completada.', '2026-04-28', '2026-04-28', 'ENVIADA', TRUE, '2026-04-29'),
(32, 14, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'kevin.rios@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-206.', '2026-04-28', '2026-04-28', 'ENVIADA', TRUE, '2026-04-28'),
(33, 15, NULL, 'CORREO', 'VALIDACION', 'kiara.rodriguez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-10', '2026-04-10', 'ENVIADA', TRUE, '2026-04-11'),
(34, 15, NULL, 'CORREO', 'PAGO', 'kiara.rodriguez@mail.com', 'Pago aprobado', 'Tu pago PAG-000015 fue aprobado.', '2026-04-14', '2026-04-14', 'ENVIADA', TRUE, '2026-04-15'),
(35, 15, NULL, 'CORREO', 'FINALIZACION', 'kiara.rodriguez@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-04-30', '2026-04-30', 'ENVIADA', FALSE, NULL),
(36, 15, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'kiara.rodriguez@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-304.', '2026-04-30', '2026-04-30', 'ENVIADA', TRUE, '2026-05-02'),
(37, 16, NULL, 'CORREO', 'VALIDACION', 'mariana.mamani@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-20', '2026-04-20', 'ENVIADA', TRUE, '2026-04-22'),
(38, 16, NULL, 'CORREO', 'PAGO', 'mariana.mamani@mail.com', 'Pago aprobado', 'Tu pago PAG-000016 fue aprobado.', '2026-04-24', '2026-04-24', 'ENVIADA', TRUE, '2026-04-24'),
(39, 16, NULL, 'CORREO', 'FINALIZACION', 'mariana.mamani@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-05-08', '2026-05-08', 'ENVIADA', TRUE, '2026-05-09'),
(40, 16, NULL, 'CORREO', 'FINALIZACION', 'cesar.mamani@mail.com', 'Inscripción finalizada', 'La inscripción de Mariana Mamani fue completada.', '2026-05-08', '2026-05-08', 'ENVIADA', FALSE, NULL),
(41, 16, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'mariana.mamani@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-304.', '2026-05-08', '2026-05-08', 'ENVIADA', TRUE, '2026-05-10'),
(42, 17, NULL, 'CORREO', 'VALIDACION', 'daniel.cardenas@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-21', '2026-04-21', 'ENVIADA', TRUE, '2026-04-23'),
(43, 17, NULL, 'CORREO', 'PAGO', 'daniel.cardenas@mail.com', 'Pago aprobado', 'Tu pago PAG-000017 fue aprobado.', '2026-04-27', '2026-04-27', 'ENVIADA', TRUE, '2026-04-27'),
(44, 17, 4, 'CORREO', 'OBSERVACION', 'daniel.cardenas@mail.com', 'Documento observado', 'Falta el certificado de 5.° año. Súbelo nuevamente dentro del plazo.', '2026-05-15', '2026-05-15', 'ENVIADA', TRUE, '2026-05-16'),
(45, 18, NULL, 'CORREO', 'VALIDACION', 'franco.espinoza@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-21', '2026-04-21', 'ENVIADA', TRUE, '2026-04-23'),
(46, 18, NULL, 'CORREO', 'PAGO', 'franco.espinoza@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-04-24', '2026-04-24', 'ENVIADA', TRUE, '2026-04-24'),
(47, 18, NULL, 'CORREO', 'PAGO', 'franco.espinoza@mail.com', 'Pago aprobado', 'Tu pago PAG-000019 fue aprobado.', '2026-04-28', '2026-04-28', 'ENVIADA', TRUE, '2026-04-29'),
(48, 18, 5, 'CORREO', 'OBSERVACION', 'franco.espinoza@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-05-15', '2026-05-15', 'ENVIADA', TRUE, '2026-05-15'),
(49, 18, NULL, 'CORREO', 'FINALIZACION', 'franco.espinoza@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-06-06', '2026-06-06', 'ENVIADA', TRUE, '2026-06-07'),
(50, 18, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'franco.espinoza@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-204.', '2026-06-06', '2026-06-06', 'ENVIADA', TRUE, '2026-06-08'),
(51, 19, NULL, 'CORREO', 'VALIDACION', 'ana.condori@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-04-24', '2026-04-24', 'ENVIADA', FALSE, NULL),
(52, 19, NULL, 'CORREO', 'PAGO', 'ana.condori@mail.com', 'Pago aprobado', 'Tu pago PAG-000020 fue aprobado.', '2026-04-29', '2026-04-29', 'ENVIADA', FALSE, NULL),
(53, 19, 6, 'CORREO', 'OBSERVACION', 'ana.condori@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-05-21', '2026-05-21', 'ENVIADA', FALSE, NULL),
(54, 19, NULL, 'CORREO', 'FINALIZACION', 'ana.condori@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-06-18', '2026-06-18', 'ENVIADA', FALSE, NULL),
(55, 19, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'ana.condori@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-206.', '2026-06-18', '2026-06-18', 'ENVIADA', TRUE, '2026-06-20'),
(56, 20, NULL, 'CORREO', 'VALIDACION', 'juan.paucar@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-05-22', '2026-05-22', 'ENVIADA', TRUE, '2026-05-23'),
(57, 20, NULL, 'CORREO', 'PAGO', 'juan.paucar@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000021.', '2026-05-24', '2026-05-24', 'ENVIADA', TRUE, '2026-05-26'),
(58, 20, NULL, 'CORREO', 'PAGO', 'juan.paucar@mail.com', 'Pago vencido', 'El plazo de pago venció y la postulación fue anulada.', '2026-06-09', '2026-06-09', 'ENVIADA', TRUE, '2026-06-11'),
(59, 21, NULL, 'CORREO', 'VALIDACION', 'kiara.medina@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-05-23', '2026-05-23', 'ENVIADA', TRUE, '2026-05-23'),
(60, 21, NULL, 'CORREO', 'PAGO', 'kiara.medina@mail.com', 'Pago aprobado', 'Tu pago PAG-000022 fue aprobado.', '2026-05-27', '2026-05-27', 'ENVIADA', TRUE, '2026-05-28'),
(61, 21, NULL, 'CORREO', 'FINALIZACION', 'kiara.medina@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-06-16', '2026-06-16', 'ENVIADA', TRUE, '2026-06-18'),
(62, 21, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'kiara.medina@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula A-202.', '2026-06-16', '2026-06-16', 'ENVIADA', TRUE, '2026-06-17'),
(63, 22, NULL, 'CORREO', 'VALIDACION', 'sofia.salas@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-05-27', '2026-05-27', 'ENVIADA', FALSE, NULL),
(64, 22, NULL, 'CORREO', 'PAGO', 'sofia.salas@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000023.', '2026-05-29', '2026-05-29', 'ENVIADA', TRUE, '2026-05-29'),
(65, 22, NULL, 'CORREO', 'PAGO', 'sofia.salas@mail.com', 'Pago vencido', 'El plazo de pago venció y la postulación fue anulada.', '2026-06-14', '2026-06-14', 'ENVIADA', TRUE, '2026-06-16'),
(66, 23, NULL, 'CORREO', 'VALIDACION', 'isabella.delgado@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-05-31', '2026-05-31', 'ENVIADA', TRUE, '2026-06-02'),
(67, 23, NULL, 'CORREO', 'PAGO', 'isabella.delgado@mail.com', 'Pago aprobado', 'Tu pago PAG-000024 fue aprobado.', '2026-06-03', '2026-06-03', 'ENVIADA', TRUE, '2026-06-05'),
(68, 23, 7, 'CORREO', 'OBSERVACION', 'isabella.delgado@mail.com', 'Documento observado', 'Imagen borrosa del documento de identidad. Súbelo nuevamente dentro del plazo.', '2026-06-16', '2026-06-16', 'ENVIADA', FALSE, NULL),
(69, 23, 7, 'BANDEJA_SISTEMA', 'OBSERVACION', 'isabella.delgado@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-06-16', '2026-06-16', 'ENVIADA', TRUE, '2026-06-17'),
(70, 24, NULL, 'CORREO', 'VALIDACION', 'kiara.tello@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-06-09', '2026-06-09', 'ENVIADA', TRUE, '2026-06-10'),
(71, 24, NULL, 'CORREO', 'PAGO', 'kiara.tello@mail.com', 'Pago aprobado', 'Tu pago PAG-000025 fue aprobado.', '2026-06-13', '2026-06-13', 'ENVIADA', TRUE, '2026-06-14'),
(72, 24, NULL, 'CORREO', 'FINALIZACION', 'kiara.tello@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-06-29', '2026-06-29', 'ENVIADA', TRUE, '2026-06-30'),
(73, 24, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'kiara.tello@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-110.', '2026-06-29', '2026-06-29', 'ENVIADA', TRUE, '2026-06-29'),
(74, 25, NULL, 'CORREO', 'VALIDACION', 'franco.sepulveda@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-06-15', '2026-06-15', 'ENVIADA', TRUE, '2026-06-17'),
(75, 25, NULL, 'CORREO', 'PAGO', 'franco.sepulveda@mail.com', 'Pago aprobado', 'Tu pago PAG-000026 fue aprobado.', '2026-06-17', '2026-06-17', 'ENVIADA', TRUE, '2026-06-17'),
(76, 25, NULL, 'CORREO', 'FINALIZACION', 'franco.sepulveda@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-06-29', '2026-06-29', 'ENVIADA', FALSE, NULL),
(77, 25, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'franco.sepulveda@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula B-202.', '2026-06-29', '2026-06-29', 'ENVIADA', FALSE, NULL),
(78, 26, NULL, 'CORREO', 'PAGO', 'kiara.tello@mail.com', 'Pago aprobado', 'Tu pago PAG-000027 fue aprobado.', '2026-09-05', '2026-09-05', 'ENVIADA', FALSE, NULL),
(79, 26, 8, 'CORREO', 'OBSERVACION', 'kiara.tello@mail.com', 'Documento observado', 'No se leen las calificaciones del certificado. Súbelo nuevamente dentro del plazo.', '2026-09-17', '2026-09-17', 'ENVIADA', TRUE, '2026-09-17'),
(80, 26, 8, 'BANDEJA_SISTEMA', 'OBSERVACION', 'kiara.tello@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-09-17', '2026-09-17', 'ENVIADA', TRUE, '2026-09-17'),
(81, 26, NULL, 'CORREO', 'FINALIZACION', 'kiara.tello@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-03', '2026-10-03', 'ENVIADA', TRUE, '2026-10-03'),
(82, 26, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'kiara.tello@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-115.', '2026-10-03', '2026-10-03', 'ENVIADA', FALSE, NULL),
(83, 27, NULL, 'CORREO', 'VALIDACION', 'alejandro.arias@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-03', '2026-09-03', 'ENVIADA', TRUE, '2026-09-04'),
(84, 27, NULL, 'CORREO', 'PAGO', 'alejandro.arias@mail.com', 'Pago aprobado', 'Tu pago PAG-000028 fue aprobado.', '2026-09-09', '2026-09-09', 'ENVIADA', TRUE, '2026-09-09'),
(85, 27, NULL, 'CORREO', 'FINALIZACION', 'alejandro.arias@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-09-21', '2026-09-21', 'ENVIADA', FALSE, NULL),
(86, 27, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'alejandro.arias@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula B-212.', '2026-09-21', '2026-09-21', 'ENVIADA', TRUE, '2026-09-21'),
(87, 28, NULL, 'CORREO', 'PAGO', 'mariana.mamani@mail.com', 'Pago aprobado', 'Tu pago PAG-000029 fue aprobado.', '2026-09-08', '2026-09-08', 'ENVIADA', TRUE, '2026-09-08'),
(88, 28, NULL, 'CORREO', 'FINALIZACION', 'mariana.mamani@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-08'),
(89, 28, NULL, 'CORREO', 'FINALIZACION', 'cesar.mamani@mail.com', 'Inscripción finalizada', 'La inscripción de Mariana Mamani fue completada.', '2026-10-06', '2026-10-06', 'ENVIADA', FALSE, NULL),
(90, 28, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'mariana.mamani@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula E-203.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-06'),
(91, 29, NULL, 'CORREO', 'PAGO', 'daniel.cardenas@mail.com', 'Pago aprobado', 'Tu pago PAG-000030 fue aprobado.', '2026-09-12', '2026-09-12', 'ENVIADA', TRUE, '2026-09-13'),
(92, 29, 9, 'CORREO', 'OBSERVACION', 'daniel.cardenas@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-09-21', '2026-09-21', 'ENVIADA', TRUE, '2026-09-23'),
(93, 30, NULL, 'CORREO', 'VALIDACION', 'carlos.montoya@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-06', '2026-09-06', 'ENVIADA', TRUE, '2026-09-08'),
(94, 30, NULL, 'CORREO', 'PAGO', 'carlos.montoya@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-09-10', '2026-09-10', 'ENVIADA', TRUE, '2026-09-10'),
(95, 30, NULL, 'CORREO', 'PAGO', 'carlos.montoya@mail.com', 'Pago aprobado', 'Tu pago PAG-000032 fue aprobado.', '2026-09-14', '2026-09-14', 'ENVIADA', TRUE, '2026-09-16'),
(96, 30, 10, 'CORREO', 'OBSERVACION', 'carlos.montoya@mail.com', 'Documento observado', 'Falta el certificado de 5.° año. Súbelo nuevamente dentro del plazo.', '2026-09-27', '2026-09-27', 'ENVIADA', FALSE, NULL),
(97, 30, NULL, 'CORREO', 'FINALIZACION', 'carlos.montoya@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-24', '2026-10-24', 'ENVIADA', TRUE, '2026-10-26'),
(98, 30, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'carlos.montoya@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula B-204.', '2026-10-24', '2026-10-24', 'ENVIADA', TRUE, '2026-10-25'),
(99, 31, NULL, 'CORREO', 'VALIDACION', 'luciana.vasquez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-06', '2026-09-06', 'ENVIADA', TRUE, '2026-09-07'),
(100, 31, NULL, 'CORREO', 'PAGO', 'luciana.vasquez@mail.com', 'Pago aprobado', 'Tu pago PAG-000033 fue aprobado.', '2026-09-11', '2026-09-11', 'ENVIADA', FALSE, NULL),
(101, 32, NULL, 'CORREO', 'VALIDACION', 'andrea.rodriguez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-08', '2026-09-08', 'ENVIADA', TRUE, '2026-09-09'),
(102, 32, NULL, 'CORREO', 'PAGO', 'andrea.rodriguez@mail.com', 'Pago aprobado', 'Tu pago PAG-000034 fue aprobado.', '2026-09-13', '2026-09-13', 'ENVIADA', TRUE, '2026-09-14'),
(103, 32, 11, 'CORREO', 'OBSERVACION', 'andrea.rodriguez@mail.com', 'Documento observado', 'Certificado de notas incompleto o borroso. Súbelo nuevamente dentro del plazo.', '2026-09-28', '2026-09-28', 'ENVIADA', TRUE, '2026-09-29'),
(104, 32, NULL, 'CORREO', 'FINALIZACION', 'andrea.rodriguez@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-14', '2026-10-14', 'ENVIADA', TRUE, '2026-10-16'),
(105, 32, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'andrea.rodriguez@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula A-113.', '2026-10-14', '2026-10-14', 'ENVIADA', TRUE, '2026-10-15'),
(106, 33, NULL, 'CORREO', 'VALIDACION', 'nicole.rios@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-09', '2026-09-09', 'ENVIADA', TRUE, '2026-09-10'),
(107, 33, NULL, 'CORREO', 'PAGO', 'nicole.rios@mail.com', 'Pago aprobado', 'Tu pago PAG-000035 fue aprobado.', '2026-09-14', '2026-09-14', 'ENVIADA', FALSE, NULL),
(108, 33, 12, 'CORREO', 'OBSERVACION', 'nicole.rios@mail.com', 'Documento observado', 'Documento de identidad vencido. Súbelo nuevamente dentro del plazo.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-06'),
(109, 33, 12, 'BANDEJA_SISTEMA', 'OBSERVACION', 'nicole.rios@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-08'),
(110, 33, NULL, 'CORREO', 'FINALIZACION', 'nicole.rios@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-30', '2026-10-30', 'ENVIADA', TRUE, '2026-10-30'),
(111, 33, NULL, 'CORREO', 'FINALIZACION', 'liliana.campos@mail.com', 'Inscripción finalizada', 'La inscripción de Nicole Ríos fue completada.', '2026-10-30', '2026-10-30', 'ENVIADA', FALSE, NULL),
(112, 33, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'nicole.rios@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula B-214.', '2026-10-30', '2026-10-30', 'ENVIADA', FALSE, NULL),
(113, 34, NULL, 'CORREO', 'PAGO', 'micaela.leon@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-09-12', '2026-09-12', 'ENVIADA', TRUE, '2026-09-12'),
(114, 34, NULL, 'CORREO', 'PAGO', 'micaela.leon@mail.com', 'Pago aprobado', 'Tu pago PAG-000037 fue aprobado.', '2026-09-15', '2026-09-15', 'ENVIADA', TRUE, '2026-09-17'),
(115, 34, 13, 'CORREO', 'OBSERVACION', 'micaela.leon@mail.com', 'Documento observado', 'No se leen las calificaciones del certificado. Súbelo nuevamente dentro del plazo.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-06'),
(116, 34, NULL, 'CORREO', 'FINALIZACION', 'micaela.leon@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-03'),
(117, 34, NULL, 'CORREO', 'FINALIZACION', 'edwin.leon@mail.com', 'Inscripción finalizada', 'La inscripción de Micaela León fue completada.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-03'),
(118, 34, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'micaela.leon@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula A-210.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-03'),
(119, 35, NULL, 'CORREO', 'VALIDACION', 'luciana.salas@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-10', '2026-09-10', 'ENVIADA', TRUE, '2026-09-11'),
(120, 35, NULL, 'CORREO', 'PAGO', 'luciana.salas@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000038.', '2026-09-10', '2026-09-10', 'ENVIADA', FALSE, NULL),
(121, 35, NULL, 'CORREO', 'PAGO', 'luciana.salas@mail.com', 'Pago vencido', 'El plazo de pago venció y la postulación fue anulada.', '2026-09-26', '2026-09-26', 'ENVIADA', TRUE, '2026-09-26'),
(122, 36, NULL, 'CORREO', 'VALIDACION', 'juan.lozano@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-10', '2026-09-10', 'ENVIADA', TRUE, '2026-09-10'),
(123, 36, NULL, 'CORREO', 'PAGO', 'juan.lozano@mail.com', 'Pago aprobado', 'Tu pago PAG-000039 fue aprobado.', '2026-09-14', '2026-09-14', 'ENVIADA', TRUE, '2026-09-15'),
(124, 36, 14, 'CORREO', 'OBSERVACION', 'juan.lozano@mail.com', 'Documento observado', 'Declaración jurada sin firma. Súbelo nuevamente dentro del plazo.', '2026-09-27', '2026-09-27', 'ENVIADA', TRUE, '2026-09-29'),
(125, 36, 14, 'BANDEJA_SISTEMA', 'OBSERVACION', 'juan.lozano@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-09-27', '2026-09-27', 'ENVIADA', TRUE, '2026-09-29'),
(126, 36, NULL, 'CORREO', 'FINALIZACION', 'juan.lozano@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-22', '2026-10-22', 'ENVIADA', TRUE, '2026-10-24'),
(127, 36, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'juan.lozano@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-211.', '2026-10-22', '2026-10-22', 'ENVIADA', TRUE, '2026-10-23'),
(128, 37, NULL, 'CORREO', 'VALIDACION', 'bruno.tello@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-11', '2026-09-11', 'ENVIADA', TRUE, '2026-09-12'),
(129, 37, NULL, 'CORREO', 'PAGO', 'bruno.tello@mail.com', 'Pago aprobado', 'Tu pago PAG-000040 fue aprobado.', '2026-09-15', '2026-09-15', 'ENVIADA', TRUE, '2026-09-15'),
(130, 37, NULL, 'CORREO', 'FINALIZACION', 'bruno.tello@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-07', '2026-10-07', 'ENVIADA', TRUE, '2026-10-07'),
(131, 37, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'bruno.tello@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula A-101.', '2026-10-07', '2026-10-07', 'ENVIADA', TRUE, '2026-10-09'),
(132, 38, NULL, 'CORREO', 'VALIDACION', 'renato.aguilar@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-11', '2026-09-11', 'ENVIADA', TRUE, '2026-09-13'),
(133, 38, NULL, 'CORREO', 'PAGO', 'renato.aguilar@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000041.', '2026-09-14', '2026-09-14', 'ENVIADA', TRUE, '2026-09-14'),
(134, 38, NULL, 'CORREO', 'PAGO', 'renato.aguilar@mail.com', 'Pago vencido', 'El plazo de pago venció y la postulación fue anulada.', '2026-09-30', '2026-09-30', 'ENVIADA', FALSE, NULL),
(135, 39, NULL, 'CORREO', 'VALIDACION', 'luciana.pinto@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-12', '2026-09-12', 'ENVIADA', TRUE, '2026-09-13'),
(136, 39, NULL, 'CORREO', 'PAGO', 'luciana.pinto@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-09-13', '2026-09-13', 'ENVIADA', FALSE, NULL),
(137, 39, NULL, 'CORREO', 'PAGO', 'luciana.pinto@mail.com', 'Pago aprobado', 'Tu pago PAG-000043 fue aprobado.', '2026-09-16', '2026-09-16', 'ENVIADA', FALSE, NULL),
(138, 39, 15, 'CORREO', 'OBSERVACION', 'luciana.pinto@mail.com', 'Documento observado', 'Falta el certificado de 5.° año. Súbelo nuevamente dentro del plazo.', '2026-10-02', '2026-10-02', 'ENVIADA', TRUE, '2026-10-03'),
(139, 39, NULL, 'CORREO', 'FINALIZACION', 'luciana.pinto@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-02'),
(140, 39, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'luciana.pinto@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula E-311.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-02'),
(141, 40, NULL, 'CORREO', 'PAGO', 'isabella.delgado@mail.com', 'Pago aprobado', 'Tu pago PAG-000044 fue aprobado.', '2026-09-18', '2026-09-18', 'ENVIADA', TRUE, '2026-09-20'),
(142, 40, 16, 'CORREO', 'OBSERVACION', 'isabella.delgado@mail.com', 'Documento observado', 'Documento de identidad vencido. Súbelo nuevamente dentro del plazo.', '2026-10-04', '2026-10-04', 'ENVIADA', TRUE, '2026-10-05'),
(143, 40, 16, 'BANDEJA_SISTEMA', 'OBSERVACION', 'isabella.delgado@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-04', '2026-10-04', 'ENVIADA', TRUE, '2026-10-04'),
(144, 41, NULL, 'CORREO', 'VALIDACION', 'jimena.huaman@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-15', '2026-09-15', 'ENVIADA', FALSE, NULL),
(145, 41, NULL, 'CORREO', 'PAGO', 'jimena.huaman@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-09-16', '2026-09-16', 'ENVIADA', FALSE, NULL),
(146, 41, NULL, 'CORREO', 'PAGO', 'jimena.huaman@mail.com', 'Pago aprobado', 'Tu pago PAG-000046 fue aprobado.', '2026-09-21', '2026-09-21', 'ENVIADA', TRUE, '2026-09-23'),
(147, 41, NULL, 'CORREO', 'FINALIZACION', 'jimena.huaman@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-06'),
(148, 41, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'jimena.huaman@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula B-212.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-08'),
(149, 42, NULL, 'CORREO', 'VALIDACION', 'rodrigo.zevallos@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-17', '2026-09-17', 'ENVIADA', TRUE, '2026-09-19'),
(150, 42, NULL, 'CORREO', 'PAGO', 'rodrigo.zevallos@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000047.', '2026-09-17', '2026-09-17', 'ENVIADA', TRUE, '2026-09-19'),
(151, 42, NULL, 'CORREO', 'PAGO', 'rodrigo.zevallos@mail.com', 'Pago vencido', 'El plazo de pago venció y la postulación fue anulada.', '2026-10-03', '2026-10-03', 'ENVIADA', TRUE, '2026-10-05'),
(152, 43, NULL, 'CORREO', 'VALIDACION', 'andrea.pacheco@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-20', '2026-09-20', 'ENVIADA', TRUE, '2026-09-20'),
(153, 43, NULL, 'CORREO', 'PAGO', 'andrea.pacheco@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-09-23', '2026-09-23', 'ENVIADA', TRUE, '2026-09-23'),
(154, 43, NULL, 'CORREO', 'PAGO', 'andrea.pacheco@mail.com', 'Pago aprobado', 'Tu pago PAG-000049 fue aprobado.', '2026-09-28', '2026-09-28', 'ENVIADA', TRUE, '2026-09-28'),
(155, 44, NULL, 'CORREO', 'VALIDACION', 'franco.sanchez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-20', '2026-09-20', 'ENVIADA', TRUE, '2026-09-22'),
(156, 44, NULL, 'CORREO', 'PAGO', 'franco.sanchez@mail.com', 'Pago aprobado', 'Tu pago PAG-000050 fue aprobado.', '2026-09-22', '2026-09-22', 'ENVIADA', TRUE, '2026-09-23'),
(157, 44, 17, 'CORREO', 'OBSERVACION', 'franco.sanchez@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-10-02', '2026-10-02', 'ENVIADA', TRUE, '2026-10-04'),
(158, 45, NULL, 'CORREO', 'VALIDACION', 'jimena.aguilar@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-21', '2026-09-21', 'ENVIADA', TRUE, '2026-09-23'),
(159, 45, NULL, 'CORREO', 'PAGO', 'jimena.aguilar@mail.com', 'Pago aprobado', 'Tu pago PAG-000051 fue aprobado.', '2026-09-23', '2026-09-23', 'ENVIADA', TRUE, '2026-09-24'),
(160, 45, NULL, 'CORREO', 'FINALIZACION', 'jimena.aguilar@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-08', '2026-10-08', 'ENVIADA', TRUE, '2026-10-09'),
(161, 45, NULL, 'CORREO', 'FINALIZACION', 'elena.campos@mail.com', 'Inscripción finalizada', 'La inscripción de Jimena Aguilar fue completada.', '2026-10-08', '2026-10-08', 'ENVIADA', TRUE, '2026-10-10'),
(162, 45, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'jimena.aguilar@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-312.', '2026-10-08', '2026-10-08', 'ENVIADA', TRUE, '2026-10-09'),
(163, 46, NULL, 'CORREO', 'VALIDACION', 'josue.sepulveda@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-23', '2026-09-23', 'ENVIADA', FALSE, NULL),
(164, 46, NULL, 'CORREO', 'PAGO', 'josue.sepulveda@mail.com', 'Pago aprobado', 'Tu pago PAG-000052 fue aprobado.', '2026-09-29', '2026-09-29', 'ENVIADA', TRUE, '2026-10-01'),
(165, 46, NULL, 'CORREO', 'FINALIZACION', 'josue.sepulveda@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-25', '2026-10-25', 'ENVIADA', TRUE, '2026-10-25'),
(166, 46, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'josue.sepulveda@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-204.', '2026-10-25', '2026-10-25', 'ENVIADA', TRUE, '2026-10-27'),
(167, 47, NULL, 'CORREO', 'VALIDACION', 'renato.soto@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-24', '2026-09-24', 'ENVIADA', TRUE, '2026-09-25'),
(168, 47, NULL, 'CORREO', 'PAGO', 'renato.soto@mail.com', 'Pago aprobado', 'Tu pago PAG-000053 fue aprobado.', '2026-09-29', '2026-09-29', 'ENVIADA', TRUE, '2026-09-29'),
(169, 47, 18, 'CORREO', 'OBSERVACION', 'renato.soto@mail.com', 'Documento observado', 'Imagen borrosa del documento de identidad. Súbelo nuevamente dentro del plazo.', '2026-10-21', '2026-10-21', 'ENVIADA', FALSE, NULL),
(170, 48, NULL, 'CORREO', 'VALIDACION', 'christian.condori@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-24', '2026-09-24', 'ENVIADA', TRUE, '2026-09-24'),
(171, 48, NULL, 'CORREO', 'PAGO', 'christian.condori@mail.com', 'Pago aprobado', 'Tu pago PAG-000054 fue aprobado.', '2026-09-28', '2026-09-28', 'ENVIADA', TRUE, '2026-09-28'),
(172, 48, 19, 'CORREO', 'OBSERVACION', 'christian.condori@mail.com', 'Documento observado', 'Declaración jurada sin firma. Súbelo nuevamente dentro del plazo.', '2026-10-09', '2026-10-09', 'ENVIADA', TRUE, '2026-10-09'),
(173, 48, NULL, 'CORREO', 'FINALIZACION', 'christian.condori@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-26', '2026-10-26', 'ENVIADA', TRUE, '2026-10-28'),
(174, 48, NULL, 'CORREO', 'FINALIZACION', 'felix.yupanqui@mail.com', 'Inscripción finalizada', 'La inscripción de Christian Condori fue completada.', '2026-10-26', '2026-10-26', 'ENVIADA', TRUE, '2026-10-28'),
(175, 48, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'christian.condori@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-207.', '2026-10-26', '2026-10-26', 'ENVIADA', TRUE, '2026-10-27'),
(176, 49, NULL, 'CORREO', 'VALIDACION', 'franco.soto@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-26', '2026-09-26', 'ENVIADA', TRUE, '2026-09-26'),
(177, 49, NULL, 'CORREO', 'PAGO', 'franco.soto@mail.com', 'Pago aprobado', 'Tu pago PAG-000055 fue aprobado.', '2026-09-29', '2026-09-29', 'ENVIADA', FALSE, NULL),
(178, 49, NULL, 'CORREO', 'FINALIZACION', 'franco.soto@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-12', '2026-10-12', 'ENVIADA', TRUE, '2026-10-13'),
(179, 49, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'franco.soto@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula E-104.', '2026-10-12', '2026-10-12', 'ENVIADA', TRUE, '2026-10-13'),
(180, 50, NULL, 'CORREO', 'VALIDACION', 'daniela.mamani@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-28', '2026-09-28', 'ENVIADA', FALSE, NULL),
(181, 50, NULL, 'CORREO', 'PAGO', 'daniela.mamani@mail.com', 'Pago aprobado', 'Tu pago PAG-000056 fue aprobado.', '2026-10-04', '2026-10-04', 'ENVIADA', TRUE, '2026-10-05'),
(182, 50, NULL, 'CORREO', 'FINALIZACION', 'daniela.mamani@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-17', '2026-10-17', 'ENVIADA', TRUE, '2026-10-19'),
(183, 50, NULL, 'CORREO', 'FINALIZACION', 'cesar.mamani@mail.com', 'Inscripción finalizada', 'La inscripción de Daniela Mamani fue completada.', '2026-10-17', '2026-10-17', 'ENVIADA', TRUE, '2026-10-18'),
(184, 50, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'daniela.mamani@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula M-205.', '2026-10-17', '2026-10-17', 'ENVIADA', TRUE, '2026-10-19'),
(185, 51, NULL, 'CORREO', 'VALIDACION', 'adriana.briceno@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-28', '2026-09-28', 'ENVIADA', TRUE, '2026-09-29'),
(186, 51, NULL, 'CORREO', 'PAGO', 'adriana.briceno@mail.com', 'Pago aprobado', 'Tu pago PAG-000057 fue aprobado.', '2026-10-04', '2026-10-04', 'ENVIADA', FALSE, NULL),
(187, 51, 20, 'CORREO', 'OBSERVACION', 'adriana.briceno@mail.com', 'Documento observado', 'Declaración jurada sin firma. Súbelo nuevamente dentro del plazo.', '2026-10-14', '2026-10-14', 'ENVIADA', TRUE, '2026-10-16'),
(188, 51, 20, 'BANDEJA_SISTEMA', 'OBSERVACION', 'adriana.briceno@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-14', '2026-10-14', 'ENVIADA', TRUE, '2026-10-16'),
(189, 51, NULL, 'CORREO', 'FINALIZACION', 'adriana.briceno@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-04'),
(190, 51, NULL, 'CORREO', 'FINALIZACION', 'edwin.briceno@mail.com', 'Inscripción finalizada', 'La inscripción de Adriana Briceño fue completada.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-04'),
(191, 51, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'adriana.briceno@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula A-311.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-02'),
(192, 52, NULL, 'CORREO', 'VALIDACION', 'nicole.caceres@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-09-30', '2026-09-30', 'ENVIADA', TRUE, '2026-10-01'),
(193, 52, NULL, 'CORREO', 'PAGO', 'nicole.caceres@mail.com', 'Pago aprobado', 'Tu pago PAG-000058 fue aprobado.', '2026-10-03', '2026-10-03', 'ENVIADA', TRUE, '2026-10-04'),
(194, 52, 21, 'CORREO', 'OBSERVACION', 'nicole.caceres@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-10-15', '2026-10-15', 'ENVIADA', FALSE, NULL),
(195, 52, 21, 'BANDEJA_SISTEMA', 'OBSERVACION', 'nicole.caceres@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-15', '2026-10-15', 'ENVIADA', TRUE, '2026-10-17'),
(196, 53, NULL, 'CORREO', 'VALIDACION', 'juan.ruiz@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-01', '2026-10-01', 'ENVIADA', TRUE, '2026-10-03'),
(197, 53, NULL, 'CORREO', 'PAGO', 'juan.ruiz@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-10-05', '2026-10-05', 'ENVIADA', FALSE, NULL),
(198, 53, NULL, 'CORREO', 'PAGO', 'juan.ruiz@mail.com', 'Pago aprobado', 'Tu pago PAG-000060 fue aprobado.', '2026-10-09', '2026-10-09', 'ENVIADA', FALSE, NULL),
(199, 53, 22, 'CORREO', 'OBSERVACION', 'juan.ruiz@mail.com', 'Documento observado', 'Declaración jurada sin firma. Súbelo nuevamente dentro del plazo.', '2026-10-23', '2026-10-23', 'ENVIADA', TRUE, '2026-10-24'),
(200, 54, NULL, 'CORREO', 'VALIDACION', 'diana.romero@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-01', '2026-10-01', 'ENVIADA', TRUE, '2026-10-03'),
(201, 54, NULL, 'CORREO', 'PAGO', 'diana.romero@mail.com', 'Pago aprobado', 'Tu pago PAG-000061 fue aprobado.', '2026-10-06', '2026-10-06', 'ENVIADA', TRUE, '2026-10-08'),
(202, 54, 23, 'CORREO', 'OBSERVACION', 'diana.romero@mail.com', 'Documento observado', 'Falta el certificado de 5.° año. Súbelo nuevamente dentro del plazo.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-19'),
(203, 54, 23, 'BANDEJA_SISTEMA', 'OBSERVACION', 'diana.romero@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-19'),
(204, 55, NULL, 'CORREO', 'VALIDACION', 'gabriela.reyes@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-02', '2026-10-02', 'ENVIADA', TRUE, '2026-10-03'),
(205, 55, NULL, 'CORREO', 'PAGO', 'gabriela.reyes@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000062.', '2026-10-04', '2026-10-04', 'ENVIADA', TRUE, '2026-10-05'),
(206, 55, NULL, 'CORREO', 'PAGO', 'gabriela.reyes@mail.com', 'Pago vencido', 'El plazo de pago venció y la postulación fue anulada.', '2026-10-20', '2026-10-20', 'ENVIADA', TRUE, '2026-10-22'),
(207, 56, NULL, 'CORREO', 'VALIDACION', 'sofia.ortiz@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-02', '2026-10-02', 'ENVIADA', TRUE, '2026-10-02'),
(208, 56, NULL, 'CORREO', 'PAGO', 'sofia.ortiz@mail.com', 'Pago aprobado', 'Tu pago PAG-000063 fue aprobado.', '2026-10-05', '2026-10-05', 'ENVIADA', FALSE, NULL),
(209, 56, 24, 'CORREO', 'OBSERVACION', 'sofia.ortiz@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-10-14', '2026-10-14', 'ENVIADA', TRUE, '2026-10-16'),
(210, 56, 24, 'BANDEJA_SISTEMA', 'OBSERVACION', 'sofia.ortiz@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-14', '2026-10-14', 'ENVIADA', TRUE, '2026-10-14'),
(211, 57, NULL, 'CORREO', 'VALIDACION', 'franco.gutierrez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-03', '2026-10-03', 'ENVIADA', TRUE, '2026-10-03'),
(212, 57, NULL, 'CORREO', 'PAGO', 'franco.gutierrez@mail.com', 'Pago aprobado', 'Tu pago PAG-000064 fue aprobado.', '2026-10-08', '2026-10-08', 'ENVIADA', FALSE, NULL),
(213, 58, NULL, 'CORREO', 'VALIDACION', 'renato.medina@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-03', '2026-10-03', 'ENVIADA', TRUE, '2026-10-04'),
(214, 58, NULL, 'CORREO', 'PAGO', 'renato.medina@mail.com', 'Pago aprobado', 'Tu pago PAG-000065 fue aprobado.', '2026-10-05', '2026-10-05', 'ENVIADA', TRUE, '2026-10-07'),
(215, 58, 25, 'CORREO', 'OBSERVACION', 'renato.medina@mail.com', 'Documento observado', 'Imagen borrosa del documento de identidad. Súbelo nuevamente dentro del plazo.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-18'),
(216, 58, 25, 'BANDEJA_SISTEMA', 'OBSERVACION', 'renato.medina@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-19'),
(217, 58, 26, 'CORREO', 'OBSERVACION', 'renato.medina@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-10-18', '2026-10-18', 'ENVIADA', FALSE, NULL),
(218, 58, 26, 'BANDEJA_SISTEMA', 'OBSERVACION', 'renato.medina@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-18'),
(219, 59, NULL, 'CORREO', 'VALIDACION', 'valentina.valdivia@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-04', '2026-10-04', 'ENVIADA', TRUE, '2026-10-06'),
(220, 59, NULL, 'CORREO', 'PAGO', 'valentina.valdivia@mail.com', 'Pago aprobado', 'Tu pago PAG-000066 fue aprobado.', '2026-10-08', '2026-10-08', 'ENVIADA', TRUE, '2026-10-09'),
(221, 59, 27, 'CORREO', 'OBSERVACION', 'valentina.valdivia@mail.com', 'Documento observado', 'Documento de identidad vencido. Súbelo nuevamente dentro del plazo.', '2026-10-20', '2026-10-20', 'ENVIADA', FALSE, NULL),
(222, 59, 27, 'BANDEJA_SISTEMA', 'OBSERVACION', 'valentina.valdivia@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-20', '2026-10-20', 'ENVIADA', FALSE, NULL),
(223, 60, NULL, 'CORREO', 'VALIDACION', 'kevin.araya@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-05', '2026-10-05', 'ENVIADA', TRUE, '2026-10-06'),
(224, 60, NULL, 'CORREO', 'PAGO', 'kevin.araya@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-10-07', '2026-10-07', 'ENVIADA', FALSE, NULL),
(225, 60, NULL, 'CORREO', 'PAGO', 'kevin.araya@mail.com', 'Pago aprobado', 'Tu pago PAG-000068 fue aprobado.', '2026-10-10', '2026-10-10', 'ENVIADA', TRUE, '2026-10-10'),
(226, 60, 28, 'CORREO', 'OBSERVACION', 'kevin.araya@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-20'),
(227, 60, 28, 'BANDEJA_SISTEMA', 'OBSERVACION', 'kevin.araya@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-18'),
(228, 61, NULL, 'CORREO', 'VALIDACION', 'piero.vasquez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-05', '2026-10-05', 'ENVIADA', FALSE, NULL),
(229, 61, NULL, 'CORREO', 'PAGO', 'piero.vasquez@mail.com', 'Pago aprobado', 'Tu pago PAG-000069 fue aprobado.', '2026-10-11', '2026-10-11', 'ENVIADA', TRUE, '2026-10-13'),
(230, 61, 29, 'CORREO', 'OBSERVACION', 'piero.vasquez@mail.com', 'Documento observado', 'Declaración jurada sin firma. Súbelo nuevamente dentro del plazo.', '2026-10-19', '2026-10-19', 'ENVIADA', TRUE, '2026-10-19'),
(231, 61, 29, 'BANDEJA_SISTEMA', 'OBSERVACION', 'piero.vasquez@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-19', '2026-10-19', 'ENVIADA', FALSE, NULL),
(232, 62, NULL, 'CORREO', 'VALIDACION', 'santiago.salas@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-05', '2026-10-05', 'ENVIADA', TRUE, '2026-10-06'),
(233, 62, NULL, 'CORREO', 'PAGO', 'santiago.salas@mail.com', 'Pago aprobado', 'Tu pago PAG-000070 fue aprobado.', '2026-10-07', '2026-10-07', 'ENVIADA', TRUE, '2026-10-07'),
(234, 62, 30, 'CORREO', 'OBSERVACION', 'santiago.salas@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-10-22', '2026-10-22', 'ENVIADA', TRUE, '2026-10-24'),
(235, 63, NULL, 'CORREO', 'VALIDACION', 'santiago.sepulveda@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-06', '2026-10-06', 'ENVIADA', FALSE, NULL),
(236, 63, NULL, 'CORREO', 'PAGO', 'santiago.sepulveda@mail.com', 'Pago aprobado', 'Tu pago PAG-000071 fue aprobado.', '2026-10-08', '2026-10-08', 'ENVIADA', FALSE, NULL),
(237, 63, NULL, 'CORREO', 'FINALIZACION', 'santiago.sepulveda@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-27', '2026-10-27', 'ENVIADA', TRUE, '2026-10-28'),
(238, 63, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'santiago.sepulveda@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-203.', '2026-10-27', '2026-10-27', 'ENVIADA', TRUE, '2026-10-29'),
(239, 64, NULL, 'CORREO', 'PAGO', 'juan.paucar@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-10-09', '2026-10-09', 'ENVIADA', TRUE, '2026-10-10'),
(240, 64, NULL, 'CORREO', 'PAGO', 'juan.paucar@mail.com', 'Pago aprobado', 'Tu pago PAG-000073 fue aprobado.', '2026-10-11', '2026-10-11', 'ENVIADA', FALSE, NULL),
(241, 64, 31, 'CORREO', 'OBSERVACION', 'juan.paucar@mail.com', 'Documento observado', 'Declaración jurada sin firma. Súbelo nuevamente dentro del plazo.', '2026-10-21', '2026-10-21', 'ENVIADA', TRUE, '2026-10-21'),
(242, 65, NULL, 'CORREO', 'VALIDACION', 'matias.vargas@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-08', '2026-10-08', 'ENVIADA', TRUE, '2026-10-09'),
(243, 65, NULL, 'CORREO', 'PAGO', 'matias.vargas@mail.com', 'Pago aprobado', 'Tu pago PAG-000074 fue aprobado.', '2026-10-13', '2026-10-13', 'ENVIADA', TRUE, '2026-10-14'),
(244, 65, 32, 'CORREO', 'OBSERVACION', 'matias.vargas@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-10-22', '2026-10-22', 'ENVIADA', TRUE, '2026-10-22'),
(245, 66, NULL, 'CORREO', 'VALIDACION', 'gabriel.soto@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-09', '2026-10-09', 'ENVIADA', TRUE, '2026-10-10'),
(246, 66, NULL, 'CORREO', 'PAGO', 'gabriel.soto@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-10-12', '2026-10-12', 'ENVIADA', TRUE, '2026-10-14'),
(247, 66, NULL, 'CORREO', 'PAGO', 'gabriel.soto@mail.com', 'Pago aprobado', 'Tu pago PAG-000076 fue aprobado.', '2026-10-15', '2026-10-15', 'ENVIADA', TRUE, '2026-10-17'),
(248, 66, 33, 'CORREO', 'OBSERVACION', 'gabriel.soto@mail.com', 'Documento observado', 'La fotografía no tiene fondo blanco. Súbelo nuevamente dentro del plazo.', '2026-11-03', '2026-11-03', 'ENVIADA', TRUE, '2026-11-04'),
(249, 67, NULL, 'CORREO', 'VALIDACION', 'alejandro.ramos@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-09', '2026-10-09', 'ENVIADA', TRUE, '2026-10-10'),
(250, 67, NULL, 'CORREO', 'PAGO', 'alejandro.ramos@mail.com', 'Pago aprobado', 'Tu pago PAG-000077 fue aprobado.', '2026-10-13', '2026-10-13', 'ENVIADA', TRUE, '2026-10-15'),
(251, 67, 34, 'CORREO', 'OBSERVACION', 'alejandro.ramos@mail.com', 'Documento observado', 'Falta el certificado de 5.° año. Súbelo nuevamente dentro del plazo.', '2026-10-30', '2026-10-30', 'ENVIADA', TRUE, '2026-11-01'),
(252, 67, 34, 'BANDEJA_SISTEMA', 'OBSERVACION', 'alejandro.ramos@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-30', '2026-10-30', 'ENVIADA', TRUE, '2026-10-31'),
(253, 68, NULL, 'CORREO', 'VALIDACION', 'micaela.aguilar@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-11', '2026-10-11', 'ENVIADA', TRUE, '2026-10-13'),
(254, 68, NULL, 'CORREO', 'PAGO', 'micaela.aguilar@mail.com', 'Pago aprobado', 'Tu pago PAG-000078 fue aprobado.', '2026-10-15', '2026-10-15', 'ENVIADA', TRUE, '2026-10-16'),
(255, 69, NULL, 'CORREO', 'VALIDACION', 'emilio.ortiz@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-12', '2026-10-12', 'ENVIADA', TRUE, '2026-10-13'),
(256, 69, NULL, 'CORREO', 'PAGO', 'emilio.ortiz@mail.com', 'Pago aprobado', 'Tu pago PAG-000079 fue aprobado.', '2026-10-14', '2026-10-14', 'ENVIADA', TRUE, '2026-10-15'),
(257, 69, NULL, 'CORREO', 'FINALIZACION', 'emilio.ortiz@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-10-31', '2026-10-31', 'ENVIADA', TRUE, '2026-11-02'),
(258, 69, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'emilio.ortiz@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula H-203.', '2026-10-31', '2026-10-31', 'ENVIADA', TRUE, '2026-11-02'),
(259, 70, NULL, 'CORREO', 'PAGO', 'franco.sepulveda@mail.com', 'Pago aprobado', 'Tu pago PAG-000080 fue aprobado.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-19'),
(260, 70, NULL, 'CORREO', 'FINALIZACION', 'franco.sepulveda@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-11-06', '2026-11-06', 'ENVIADA', TRUE, '2026-11-08'),
(261, 70, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'franco.sepulveda@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula A-102.', '2026-11-06', '2026-11-06', 'ENVIADA', FALSE, NULL),
(262, 71, NULL, 'CORREO', 'VALIDACION', 'carolina.vasquez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-15', '2026-10-15', 'ENVIADA', TRUE, '2026-10-16'),
(263, 71, NULL, 'CORREO', 'PAGO', 'carolina.vasquez@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000081.', '2026-10-17', '2026-10-17', 'ENVIADA', TRUE, '2026-10-17'),
(264, 71, NULL, 'CORREO', 'PAGO', 'carolina.vasquez@mail.com', 'Pago vencido', 'El plazo de pago venció y la postulación fue anulada.', '2026-11-02', '2026-11-02', 'ENVIADA', FALSE, NULL),
(265, 72, NULL, 'CORREO', 'VALIDACION', 'mariana.chavez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-16', '2026-10-16', 'ENVIADA', TRUE, '2026-10-17'),
(266, 72, NULL, 'CORREO', 'PAGO', 'mariana.chavez@mail.com', 'Pago aprobado', 'Tu pago PAG-000082 fue aprobado.', '2026-10-20', '2026-10-20', 'ENVIADA', TRUE, '2026-10-21'),
(267, 72, NULL, 'CORREO', 'FINALIZACION', 'mariana.chavez@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-11-07', '2026-11-07', 'ENVIADA', TRUE, '2026-11-09'),
(268, 72, NULL, 'CORREO', 'FINALIZACION', 'norma.caceres@mail.com', 'Inscripción finalizada', 'La inscripción de Mariana Chávez fue completada.', '2026-11-07', '2026-11-07', 'ENVIADA', TRUE, '2026-11-09'),
(269, 72, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'mariana.chavez@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula E-209.', '2026-11-07', '2026-11-07', 'ENVIADA', TRUE, '2026-11-08'),
(270, 73, NULL, 'CORREO', 'VALIDACION', 'isabella.morales@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-17', '2026-10-17', 'ENVIADA', TRUE, '2026-10-18'),
(271, 73, NULL, 'CORREO', 'PAGO', 'isabella.morales@mail.com', 'Pago aprobado', 'Tu pago PAG-000083 fue aprobado.', '2026-10-21', '2026-10-21', 'ENVIADA', TRUE, '2026-10-22'),
(272, 73, 35, 'CORREO', 'OBSERVACION', 'isabella.morales@mail.com', 'Documento observado', 'Imagen borrosa del documento de identidad. Súbelo nuevamente dentro del plazo.', '2026-11-05', '2026-11-05', 'ENVIADA', TRUE, '2026-11-06'),
(273, 74, NULL, 'CORREO', 'VALIDACION', 'matias.ruiz@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-17', '2026-10-17', 'ENVIADA', TRUE, '2026-10-17'),
(274, 74, NULL, 'CORREO', 'PAGO', 'matias.ruiz@mail.com', 'Pago aprobado', 'Tu pago PAG-000084 fue aprobado.', '2026-10-20', '2026-10-20', 'ENVIADA', FALSE, NULL),
(275, 74, 36, 'CORREO', 'OBSERVACION', 'matias.ruiz@mail.com', 'Documento observado', 'Certificado de notas incompleto o borroso. Súbelo nuevamente dentro del plazo.', '2026-11-06', '2026-11-06', 'ENVIADA', TRUE, '2026-11-08'),
(276, 75, NULL, 'CORREO', 'VALIDACION', 'renata.palomino@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-17', '2026-10-17', 'ENVIADA', TRUE, '2026-10-18'),
(277, 75, NULL, 'CORREO', 'PAGO', 'renata.palomino@mail.com', 'Pago aprobado', 'Tu pago PAG-000085 fue aprobado.', '2026-10-21', '2026-10-21', 'ENVIADA', TRUE, '2026-10-21'),
(278, 75, NULL, 'CORREO', 'FINALIZACION', 'renata.palomino@mail.com', 'Inscripción finalizada', 'Tu inscripción fue completada con éxito.', '2026-11-08', '2026-11-08', 'ENVIADA', FALSE, NULL),
(279, 75, NULL, 'BANDEJA_SISTEMA', 'CARNE', 'renata.palomino@mail.com', 'Tu carné de postulante está listo', 'Descarga tu carné. Aula B-102.', '2026-11-08', '2026-11-08', 'ENVIADA', FALSE, NULL),
(280, 76, NULL, 'CORREO', 'VALIDACION', 'adriana.medina@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-18', '2026-10-18', 'ENVIADA', TRUE, '2026-10-18'),
(281, 76, NULL, 'CORREO', 'PAGO', 'adriana.medina@mail.com', 'Pago aprobado', 'Tu pago PAG-000086 fue aprobado.', '2026-10-20', '2026-10-20', 'ENVIADA', TRUE, '2026-10-20'),
(282, 76, 37, 'CORREO', 'OBSERVACION', 'adriana.medina@mail.com', 'Documento observado', 'Declaración jurada sin firma. Súbelo nuevamente dentro del plazo.', '2026-10-28', '2026-10-28', 'ENVIADA', TRUE, '2026-10-28'),
(283, 76, 37, 'BANDEJA_SISTEMA', 'OBSERVACION', 'adriana.medina@mail.com', 'Documento observado', 'Tienes una observación pendiente de subsanar.', '2026-10-28', '2026-10-28', 'ENVIADA', TRUE, '2026-10-28'),
(284, 77, NULL, 'CORREO', 'VALIDACION', 'christian.rodriguez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-21', '2026-10-21', 'ENVIADA', TRUE, '2026-10-23'),
(285, 77, NULL, 'CORREO', 'PAGO', 'christian.rodriguez@mail.com', 'Pago aprobado', 'Tu pago PAG-000087 fue aprobado.', '2026-10-27', '2026-10-27', 'ENVIADA', TRUE, '2026-10-29'),
(286, 78, NULL, 'CORREO', 'VALIDACION', 'luis.romero@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-22', '2026-10-22', 'ENVIADA', TRUE, '2026-10-23'),
(287, 78, NULL, 'CORREO', 'PAGO', 'luis.romero@mail.com', 'Pago aprobado', 'Tu pago PAG-000088 fue aprobado.', '2026-10-26', '2026-10-26', 'ENVIADA', TRUE, '2026-10-27'),
(288, 78, 38, 'CORREO', 'OBSERVACION', 'luis.romero@mail.com', 'Documento observado', 'Imagen borrosa del documento de identidad. Súbelo nuevamente dentro del plazo.', '2026-11-08', '2026-11-08', 'ENVIADA', TRUE, '2026-11-08'),
(289, 79, NULL, 'CORREO', 'VALIDACION', 'santiago.huaman@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-26', '2026-10-26', 'ENVIADA', TRUE, '2026-10-26'),
(290, 79, NULL, 'CORREO', 'PAGO', 'santiago.huaman@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000089.', '2026-10-28', '2026-10-28', 'ENVIADA', TRUE, '2026-10-30'),
(291, 80, NULL, 'CORREO', 'VALIDACION', 'marco.aguilar@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-27', '2026-10-27', 'ENVIADA', TRUE, '2026-10-27'),
(292, 80, NULL, 'CORREO', 'PAGO', 'marco.aguilar@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000090.', '2026-10-28', '2026-10-28', 'ENVIADA', FALSE, NULL),
(293, 81, NULL, 'CORREO', 'VALIDACION', 'renato.yupanqui@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-28', '2026-10-28', 'ENVIADA', TRUE, '2026-10-29'),
(294, 81, NULL, 'CORREO', 'PAGO', 'renato.yupanqui@mail.com', 'Pago aprobado', 'Tu pago PAG-000091 fue aprobado.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-04'),
(295, 82, NULL, 'CORREO', 'PAGO', 'alessandra.navarro@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000092.', '2026-10-31', '2026-10-31', 'ENVIADA', TRUE, '2026-10-31'),
(296, 83, NULL, 'CORREO', 'PAGO', 'kiara.medina@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000093.', '2026-10-28', '2026-10-28', 'ENVIADA', TRUE, '2026-10-29'),
(297, 84, NULL, 'CORREO', 'VALIDACION', 'rodrigo.romero@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-29', '2026-10-29', 'ENVIADA', TRUE, '2026-10-31'),
(298, 84, NULL, 'CORREO', 'PAGO', 'rodrigo.romero@mail.com', 'Pago aprobado', 'Tu pago PAG-000094 fue aprobado.', '2026-11-02', '2026-11-02', 'ENVIADA', TRUE, '2026-11-02'),
(299, 85, NULL, 'CORREO', 'VALIDACION', 'gonzalo.caceres@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-10-31', '2026-10-31', 'ENVIADA', TRUE, '2026-11-02'),
(300, 85, NULL, 'CORREO', 'PAGO', 'gonzalo.caceres@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000095.', '2026-10-31', '2026-10-31', 'ENVIADA', TRUE, '2026-11-01'),
(301, 86, NULL, 'CORREO', 'VALIDACION', 'juan.medina@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-11-01', NULL, 'PENDIENTE', FALSE, NULL),
(302, 87, NULL, 'CORREO', 'VALIDACION', 'alessandra.ruiz@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-11-01', '2026-11-01', 'ENVIADA', TRUE, '2026-11-02'),
(303, 87, NULL, 'CORREO', 'PAGO', 'alessandra.ruiz@mail.com', 'Código de pago generado', 'Realiza la transferencia con el código PAG-000096.', '2026-11-04', '2026-11-04', 'ENVIADA', TRUE, '2026-11-04'),
(304, 88, NULL, 'CORREO', 'VALIDACION', 'brenda.gutierrez@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-11-01', '2026-11-01', 'ENVIADA', TRUE, '2026-11-01'),
(305, 88, NULL, 'CORREO', 'PAGO', 'brenda.gutierrez@mail.com', 'Pago rechazado', 'El voucher adjunto no es válido. Genera un nuevo pago.', '2026-11-05', '2026-11-05', 'ENVIADA', TRUE, '2026-11-07'),
(306, 88, NULL, 'CORREO', 'PAGO', 'brenda.gutierrez@mail.com', 'Pago aprobado', 'Tu pago PAG-000098 fue aprobado.', '2026-11-08', '2026-11-08', 'ENVIADA', TRUE, '2026-11-08'),
(307, 89, NULL, 'CORREO', 'VALIDACION', 'renata.mamani@mail.com', 'Valida tu correo', 'Ingresa al enlace para validar tu correo.', '2026-11-06', '2026-11-06', 'ENVIADA', TRUE, '2026-11-07'),
(308, 90, NULL, 'CORREO', 'PAGO', 'franco.espinoza@mail.com', 'Pago aprobado', 'Tu pago PAG-000099 fue aprobado.', '2026-11-08', '2026-11-08', 'ENVIADA', TRUE, '2026-11-08');


COMMIT;

-- =====================================================
-- ADMITU LAB06 - PROCEDIMIENTOS ALMACENADOS (Callable)
-- 6 entidades demo: FACULTAD, CARRERA, CONVOCATORIA,
-- POSTULANTE (persona+postulante), POSTULACION, PAGO
-- =====================================================

-- Limpieza previa de procedimientos
DROP PROCEDURE IF EXISTS INSERTAR_FACULTAD;
DROP PROCEDURE IF EXISTS MODIFICAR_FACULTAD;
DROP PROCEDURE IF EXISTS ELIMINAR_FACULTAD;
DROP PROCEDURE IF EXISTS REACTIVAR_FACULTAD;
DROP PROCEDURE IF EXISTS LISTAR_FACULTAD_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_FACULTADES_TODAS;
DROP PROCEDURE IF EXISTS INSERTAR_CARRERA;
DROP PROCEDURE IF EXISTS MODIFICAR_CARRERA;
DROP PROCEDURE IF EXISTS ELIMINAR_CARRERA;
DROP PROCEDURE IF EXISTS REACTIVAR_CARRERA;
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
DROP PROCEDURE IF EXISTS REATIVAR_SEDE;
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
DROP PROCEDURE IF EXISTS REACTIVAR_REQUISITO;
DROP PROCEDURE IF EXISTS LISTAR_REQUISITO_X_ID;
DROP PROCEDURE IF EXISTS LISTAR_REQUISITOS_TODOS;
DROP PROCEDURE IF EXISTS INSERTAR_MODALIDAD;
DROP PROCEDURE IF EXISTS MODIFICAR_MODALIDAD;
DROP PROCEDURE IF EXISTS ELIMINAR_MODALIDAD;
DROP PROCEDURE IF EXISTS REACTIVAR_MODALIDAD;
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
DROP PROCEDURE IF EXISTS REACTIVAR_EVALUADOR;
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
DROP PROCEDURE IF EXISTS LISTAR_REQUISITOS_CONVOCATORIA_MODALIDAD_TODOS;
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

CREATE PROCEDURE REACTIVAR_FACULTAD(
    IN _id INT
)
BEGIN
    UPDATE facultad SET activo = 1 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_FACULTAD_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo, nombre, activo FROM facultad WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_FACULTADES_TODAS()
BEGIN
    SELECT id, codigo, nombre, activo FROM facultad;
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

CREATE PROCEDURE REACTIVAR_CARRERA(
    IN _id INT
)
BEGIN
    UPDATE carrera SET activo = 1 WHERE id = _id;
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
    IN _referencia VARCHAR(100),
    IN _voucher VARCHAR(255)
)
BEGIN
    UPDATE pago
    SET estado_pago = _estado,
        referencia_pasarela = _referencia,
        ruta_voucher = COALESCE(_voucher, ruta_voucher),
        fecha_pago = CASE WHEN _estado = 'APROBADO' THEN COALESCE(fecha_pago, CURDATE()) ELSE fecha_pago END
    WHERE id = _id;
END$

CREATE PROCEDURE ELIMINAR_PAGO(
    IN _id INT
)
BEGIN
    UPDATE pago SET estado_pago = 'VENCIDO' WHERE id = _id;
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

CREATE PROCEDURE REACTIVAR_SEDE(
    IN _id INT
)
BEGIN
    UPDATE sede SET activo = 1 WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_SEDE_X_ID(
    IN _id INT
)
BEGIN
    SELECT id, codigo, nombre, direccion, activo FROM sede WHERE id = _id;
END$

CREATE PROCEDURE LISTAR_SEDES_TODAS()
BEGIN
    SELECT id, codigo, nombre, direccion, activo FROM sede ORDER BY nombre;
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

CREATE PROCEDURE REACTIVAR_REQUISITO(
    IN _id INT
)
BEGIN
    UPDATE requisito SET activo = 1 WHERE id = _id;
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

CREATE PROCEDURE REACTIVAR_MODALIDAD(
    IN _id INT
)
BEGIN
    UPDATE modalidad SET activo = 1 WHERE id = _id;
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
    FROM modalidad ORDER BY codigo_modalidad;
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
    UPDATE evaluador SET activo = 0 WHERE persona_id = _id;
END$

CREATE PROCEDURE REACTIVAR_EVALUADOR(
    IN _id INT
)
BEGIN
    UPDATE evaluador SET activo = 1 WHERE persona_id = _id;
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
    SELECT d.id, d.postulacion_id, d.requisito_aplicable_id AS requisito_aplicable_id, r.id AS requisito_id, d.numero_version,
        d.nombre_archivo, d.tipo_archivo, d.tamanio_archivo, d.ruta_archivo, d.fecha_carga,
        d.estado_documento, d.fecha_evaluacion, d.comentario_evaluacion,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM documento_postulacion d
    INNER JOIN requisito_convocatoria_modalidad rcm ON rcm.id = d.requisito_aplicable_id
    INNER JOIN requisito r ON r.id = rcm.requisito_id
    WHERE d.id = _id;
END$

CREATE PROCEDURE LISTAR_DOCUMENTOS_POSTULACION_TODOS()
BEGIN
    SELECT d.id, d.postulacion_id, d.requisito_aplicable_id AS requisito_aplicable_id, r.id AS requisito_id, d.numero_version,
        d.nombre_archivo, d.tipo_archivo, d.tamanio_archivo, d.ruta_archivo, d.fecha_carga,
        d.estado_documento, d.fecha_evaluacion, d.comentario_evaluacion,
        r.codigo_requisito, r.nombre AS nombre_requisito, r.descripcion AS descripcion_requisito,
        r.tipo_archivo_requerido AS tipo_archivo_requisito, r.tamanio_maximo_bytes AS tamanio_maximo_requisito
    FROM documento_postulacion d
    INNER JOIN requisito_convocatoria_modalidad rcm ON rcm.id = d.requisito_aplicable_id
    INNER JOIN requisito r ON r.id = rcm.requisito_id
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
