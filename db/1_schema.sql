-- =====================================================
--  MediQR · Script de base de datos (MySQL 8)  v0.1
-- =====================================================
CREATE DATABASE IF NOT EXISTS mediqr_db
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mediqr_db;

CREATE TABLE IF NOT EXISTS usuario (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre         VARCHAR(100) NOT NULL,
  correo         VARCHAR(120) NOT NULL UNIQUE,
  password_hash  VARCHAR(255) NOT NULL,
  rol            VARCHAR(20)  NOT NULL DEFAULT 'PACIENTE',  -- PACIENTE | MEDICO | ADMIN
  creado_en      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Un usuario puede administrar varios perfiles (él, sus hijos, un adulto mayor)
CREATE TABLE IF NOT EXISTS perfil (
  id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
  usuario_id         BIGINT       NOT NULL,
  nombre             VARCHAR(100) NOT NULL,
  parentesco         VARCHAR(20)  NOT NULL DEFAULT 'TITULAR', -- TITULAR|HIJO|ADULTO_MAYOR|OTRO
  fecha_nacimiento   DATE,
  tipo_sangre        VARCHAR(5),
  codigo_emergencia  VARCHAR(36)  NOT NULL UNIQUE,            -- UUID del QR de emergencia
  CONSTRAINT fk_perfil_usuario FOREIGN KEY (usuario_id)
    REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contacto_emergencia (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id   BIGINT       NOT NULL,
  nombre      VARCHAR(100) NOT NULL,
  parentesco  VARCHAR(50),
  telefono    VARCHAR(20)  NOT NULL,
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS alergia (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id  BIGINT       NOT NULL,
  sustancia  VARCHAR(100) NOT NULL,
  reaccion   VARCHAR(200),
  gravedad   VARCHAR(10)  NOT NULL DEFAULT 'MEDIA',           -- LEVE | MEDIA | GRAVE
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS enfermedad (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id   BIGINT       NOT NULL,
  nombre      VARCHAR(120) NOT NULL,
  desde       DATE,
  cronica     BOOLEAN      NOT NULL DEFAULT FALSE,
  notas       VARCHAR(300),
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS receta (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id         BIGINT       NOT NULL,
  medicamento       VARCHAR(120) NOT NULL,
  dosis             VARCHAR(60),
  frecuencia_horas  INT,
  duracion_dias     INT,
  medico            VARCHAR(100),
  fecha_receta      DATE         NOT NULL,
  critico           BOOLEAN      NOT NULL DEFAULT FALSE,  -- aparece en el QR de emergencia
  activa            BOOLEAN      NOT NULL DEFAULT TRUE,
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS vacuna (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id         BIGINT       NOT NULL,
  nombre            VARCHAR(100) NOT NULL,
  fecha_aplicacion  DATE         NOT NULL,
  proxima_dosis     DATE,
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS examen (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id  BIGINT       NOT NULL,
  tipo       VARCHAR(100) NOT NULL,         -- Hemograma, Química sanguínea...
  fecha      DATE         NOT NULL,
  notas      VARCHAR(300),
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

-- Valores numéricos de laboratorio (base del cruce laboratorio-medicamento)
CREATE TABLE IF NOT EXISTS resultado_laboratorio (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id  BIGINT        NOT NULL,
  examen_id  BIGINT,
  fecha      DATE          NOT NULL,
  analito    VARCHAR(60)   NOT NULL,        -- CREATININA, GLUCOSA, ...
  valor      DECIMAL(10,2) NOT NULL,
  unidad     VARCHAR(20),
  ref_max    DECIMAL(10,2),                 -- límite superior normal
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE,
  FOREIGN KEY (examen_id) REFERENCES examen(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS documento (
  id               BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id        BIGINT       NOT NULL,
  nombre_original  VARCHAR(200) NOT NULL,
  tipo_mime        VARCHAR(80)  NOT NULL,
  nombre_archivo   VARCHAR(80)  NOT NULL,   -- UUID.bin (contenido cifrado en disco)
  creado_en        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

-- QR de consulta: temporal y de un solo uso
CREATE TABLE IF NOT EXISTS token_qr (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id  BIGINT      NOT NULL,
  codigo     VARCHAR(36) NOT NULL UNIQUE,
  expira_en  DATETIME    NOT NULL,
  usado      BOOLEAN     NOT NULL DEFAULT FALSE,
  creado_en  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

-- "¿Quién vio mi información y cuándo?"
CREATE TABLE IF NOT EXISTS auditoria_acceso (
  id          BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id   BIGINT      NOT NULL,
  tipo        VARCHAR(20) NOT NULL,         -- EMERGENCIA | CONSULTA
  fecha       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  ip          VARCHAR(45),
  user_agent  VARCHAR(255),
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS recordatorio (
  id         BIGINT AUTO_INCREMENT PRIMARY KEY,
  perfil_id  BIGINT       NOT NULL,
  titulo     VARCHAR(150) NOT NULL,
  fecha_hora DATETIME     NOT NULL,
  tipo       VARCHAR(20)  NOT NULL,         -- VACUNA | CONTROL | MEDICAMENTO
  enviado    BOOLEAN      NOT NULL DEFAULT FALSE,
  FOREIGN KEY (perfil_id) REFERENCES perfil(id) ON DELETE CASCADE
);

-- Catálogos para las alertas
CREATE TABLE IF NOT EXISTS medicamento_catalogo (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY,
  nombre            VARCHAR(120) NOT NULL UNIQUE,
  eliminacion_renal BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS interaccion_medicamento (
  id             BIGINT AUTO_INCREMENT PRIMARY KEY,
  medicamento_a  VARCHAR(120) NOT NULL,
  medicamento_b  VARCHAR(120) NOT NULL,
  gravedad       VARCHAR(10)  NOT NULL,     -- BAJA | MEDIA | ALTA
  mensaje        VARCHAR(300) NOT NULL
);

CREATE INDEX idx_receta_perfil  ON receta(perfil_id, activa);
CREATE INDEX idx_token_codigo   ON token_qr(codigo);
CREATE INDEX idx_lab_perfil     ON resultado_laboratorio(perfil_id, analito, fecha);
