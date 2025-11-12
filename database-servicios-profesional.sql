-- ============================================================
-- SCRIPT SQL PARA SERVICIOS PROFESIONALES
-- Proyecto: Contacto Profesionales Web
-- Descripción: Tablas para gestionar especialidades, áreas de servicio y disponibilidad
-- ============================================================

-- Tabla: especialidades_profesional
-- Descripción: Almacena hasta 3 especialidades por profesional, destacando una como principal
CREATE TABLE IF NOT EXISTS especialidades_profesional (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL,
    nombre_especialidad VARCHAR(200) NOT NULL,
    descripcion TEXT,
    incluye_materiales BOOLEAN DEFAULT FALSE,
    costo DECIMAL(10, 2) NOT NULL,
    tipo_costo VARCHAR(20) NOT NULL CHECK (tipo_costo IN ('hora', 'dia', 'mes')),
    es_principal BOOLEAN DEFAULT FALSE,
    orden SMALLINT NOT NULL CHECK (orden BETWEEN 1 AND 3),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_especialidad_profesional FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id) ON DELETE CASCADE,
    CONSTRAINT uq_profesional_orden UNIQUE (profesional_id, orden),
    CONSTRAINT chk_max_especialidades CHECK (
        (SELECT COUNT(*) FROM especialidades_profesional
         WHERE profesional_id = especialidades_profesional.profesional_id
         AND activo = TRUE) <= 3
    )
);

-- Índices para especialidades_profesional
CREATE INDEX idx_especialidades_profesional_id ON especialidades_profesional(profesional_id);
CREATE INDEX idx_especialidades_principal ON especialidades_profesional(profesional_id, es_principal)
    WHERE es_principal = TRUE;

-- Comentarios
COMMENT ON TABLE especialidades_profesional IS 'Especialidades del profesional (máximo 3, con una principal)';
COMMENT ON COLUMN especialidades_profesional.es_principal IS 'Indica si es la especialidad principal del profesional';
COMMENT ON COLUMN especialidades_profesional.incluye_materiales IS 'Indica si el servicio incluye materiales';
COMMENT ON COLUMN especialidades_profesional.tipo_costo IS 'Tipo de tarifa: hora, dia o mes';
COMMENT ON COLUMN especialidades_profesional.orden IS 'Orden de la especialidad (1, 2 o 3)';

-- ============================================================

-- Tabla: areas_servicio
-- Descripción: Define las áreas geográficas donde el profesional brinda servicios
CREATE TABLE IF NOT EXISTS areas_servicio (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL,
    todo_pais BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_area_profesional FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id) ON DELETE CASCADE,
    CONSTRAINT uq_area_profesional UNIQUE (profesional_id)
);

-- Índice para areas_servicio
CREATE INDEX idx_areas_servicio_profesional ON areas_servicio(profesional_id);

COMMENT ON TABLE areas_servicio IS 'Configuración del área de servicio del profesional';
COMMENT ON COLUMN areas_servicio.todo_pais IS 'Si es TRUE, el profesional brinda servicios en todo el país';

-- ============================================================

-- Tabla: ubicaciones_servicio
-- Descripción: Ubicaciones específicas donde el profesional brinda servicios (máximo 10)
CREATE TABLE IF NOT EXISTS ubicaciones_servicio (
    id SERIAL PRIMARY KEY,
    area_servicio_id INTEGER NOT NULL,
    tipo_ubicacion VARCHAR(20) NOT NULL CHECK (tipo_ubicacion IN ('departamento', 'provincia', 'distrito')),
    departamento VARCHAR(100),
    provincia VARCHAR(100),
    distrito VARCHAR(100),
    orden SMALLINT NOT NULL CHECK (orden BETWEEN 1 AND 10),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_ubicacion_area FOREIGN KEY (area_servicio_id)
        REFERENCES areas_servicio(id) ON DELETE CASCADE,
    CONSTRAINT chk_max_ubicaciones CHECK (
        (SELECT COUNT(*) FROM ubicaciones_servicio
         WHERE area_servicio_id = ubicaciones_servicio.area_servicio_id
         AND activo = TRUE) <= 10
    )
);

-- Índices para ubicaciones_servicio
CREATE INDEX idx_ubicaciones_area ON ubicaciones_servicio(area_servicio_id);
CREATE INDEX idx_ubicaciones_tipo ON ubicaciones_servicio(tipo_ubicacion);

COMMENT ON TABLE ubicaciones_servicio IS 'Ubicaciones específicas de servicio (máximo 10, solo si todo_pais = FALSE)';
COMMENT ON COLUMN ubicaciones_servicio.tipo_ubicacion IS 'Nivel de granularidad: departamento, provincia o distrito';

-- ============================================================

-- Tabla: disponibilidad_horaria
-- Descripción: Define la disponibilidad del profesional por día de la semana
CREATE TABLE IF NOT EXISTS disponibilidad_horaria (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL,
    todo_tiempo BOOLEAN DEFAULT FALSE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_disponibilidad_profesional FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id) ON DELETE CASCADE,
    CONSTRAINT uq_disponibilidad_profesional UNIQUE (profesional_id)
);

-- Índice para disponibilidad_horaria
CREATE INDEX idx_disponibilidad_profesional ON disponibilidad_horaria(profesional_id);

COMMENT ON TABLE disponibilidad_horaria IS 'Configuración de disponibilidad horaria del profesional';
COMMENT ON COLUMN disponibilidad_horaria.todo_tiempo IS 'Si es TRUE, está disponible 24/7';

-- ============================================================

-- Tabla: horarios_dia
-- Descripción: Horarios específicos por día de la semana
CREATE TABLE IF NOT EXISTS horarios_dia (
    id SERIAL PRIMARY KEY,
    disponibilidad_id INTEGER NOT NULL,
    dia_semana VARCHAR(10) NOT NULL CHECK (dia_semana IN ('lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo')),
    tipo_jornada VARCHAR(10) NOT NULL CHECK (tipo_jornada IN ('8hrs', '24hrs')),
    hora_inicio TIME,
    hora_fin TIME,
    activo BOOLEAN DEFAULT TRUE,
    CONSTRAINT fk_horario_disponibilidad FOREIGN KEY (disponibilidad_id)
        REFERENCES disponibilidad_horaria(id) ON DELETE CASCADE,
    CONSTRAINT uq_dia_disponibilidad UNIQUE (disponibilidad_id, dia_semana)
);

-- Índices para horarios_dia
CREATE INDEX idx_horarios_disponibilidad ON horarios_dia(disponibilidad_id);
CREATE INDEX idx_horarios_dia_semana ON horarios_dia(dia_semana);

COMMENT ON TABLE horarios_dia IS 'Horarios específicos por día de la semana';
COMMENT ON COLUMN horarios_dia.dia_semana IS 'Día de la semana en español';
COMMENT ON COLUMN horarios_dia.tipo_jornada IS 'Duración de jornada: 8hrs o 24hrs';

-- ============================================================

-- TRIGGER: Actualizar fecha_actualizacion automáticamente
-- ============================================================

-- Función genérica para actualizar timestamp
CREATE OR REPLACE FUNCTION actualizar_fecha_actualizacion()
RETURNS TRIGGER AS $$
BEGIN
    NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Triggers para cada tabla
CREATE TRIGGER trg_especialidades_actualizar
    BEFORE UPDATE ON especialidades_profesional
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_actualizacion();

CREATE TRIGGER trg_areas_actualizar
    BEFORE UPDATE ON areas_servicio
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_actualizacion();

CREATE TRIGGER trg_disponibilidad_actualizar
    BEFORE UPDATE ON disponibilidad_horaria
    FOR EACH ROW
    EXECUTE FUNCTION actualizar_fecha_actualizacion();

-- ============================================================

-- FUNCIÓN: Validar que solo haya una especialidad principal por profesional
-- ============================================================

CREATE OR REPLACE FUNCTION validar_especialidad_principal()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.es_principal = TRUE THEN
        -- Desmarcar otras especialidades principales del mismo profesional
        UPDATE especialidades_profesional
        SET es_principal = FALSE
        WHERE profesional_id = NEW.profesional_id
          AND id != COALESCE(NEW.id, -1)
          AND es_principal = TRUE;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_especialidad_principal
    BEFORE INSERT OR UPDATE ON especialidades_profesional
    FOR EACH ROW
    EXECUTE FUNCTION validar_especialidad_principal();

-- ============================================================

-- DATOS INICIALES / EJEMPLOS (OPCIONAL - COMENTADO)
-- ============================================================

-- EJEMPLO: Insertar especialidades para un profesional con id=1
/*
INSERT INTO especialidades_profesional (profesional_id, nombre_especialidad, descripcion, incluye_materiales, costo, tipo_costo, es_principal, orden)
VALUES
    (1, 'Electricidad Residencial', 'Instalación y reparación de sistemas eléctricos en hogares', TRUE, 50.00, 'hora', TRUE, 1),
    (1, 'Electricidad Industrial', 'Mantenimiento de sistemas eléctricos en plantas industriales', FALSE, 80.00, 'hora', FALSE, 2),
    (1, 'Domótica', 'Instalación de sistemas de automatización del hogar', TRUE, 2500.00, 'mes', FALSE, 3);

-- EJEMPLO: Configurar área de servicio todo el país
INSERT INTO areas_servicio (profesional_id, todo_pais)
VALUES (1, TRUE);

-- EJEMPLO: Configurar área de servicio con ubicaciones específicas
INSERT INTO areas_servicio (profesional_id, todo_pais)
VALUES (2, FALSE);

INSERT INTO ubicaciones_servicio (area_servicio_id, tipo_ubicacion, departamento, provincia, distrito, orden)
VALUES
    ((SELECT id FROM areas_servicio WHERE profesional_id = 2), 'departamento', 'Lima', NULL, NULL, 1),
    ((SELECT id FROM areas_servicio WHERE profesional_id = 2), 'distrito', 'Lima', 'Lima', 'San Isidro', 2),
    ((SELECT id FROM areas_servicio WHERE profesional_id = 2), 'distrito', 'Lima', 'Lima', 'Miraflores', 3);

-- EJEMPLO: Disponibilidad todo el tiempo
INSERT INTO disponibilidad_horaria (profesional_id, todo_tiempo)
VALUES (1, TRUE);

-- EJEMPLO: Disponibilidad con horarios específicos
INSERT INTO disponibilidad_horaria (profesional_id, todo_tiempo)
VALUES (2, FALSE);

INSERT INTO horarios_dia (disponibilidad_id, dia_semana, tipo_jornada, hora_inicio, hora_fin)
VALUES
    ((SELECT id FROM disponibilidad_horaria WHERE profesional_id = 2), 'lunes', '8hrs', '08:00', '17:00'),
    ((SELECT id FROM disponibilidad_horaria WHERE profesional_id = 2), 'martes', '8hrs', '08:00', '17:00'),
    ((SELECT id FROM disponibilidad_horaria WHERE profesional_id = 2), 'miercoles', '8hrs', '08:00', '17:00'),
    ((SELECT id FROM disponibilidad_horaria WHERE profesional_id = 2), 'jueves', '8hrs', '08:00', '17:00'),
    ((SELECT id FROM disponibilidad_horaria WHERE profesional_id = 2), 'viernes', '8hrs', '08:00', '17:00'),
    ((SELECT id FROM disponibilidad_horaria WHERE profesional_id = 2), 'sabado', '24hrs', NULL, NULL);
*/

-- ============================================================
-- FIN DEL SCRIPT
-- ============================================================
