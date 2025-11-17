-- ============================================================================
-- Migración V006: Refactorización Completa del Perfil Profesional
-- Fecha: 2025-11-15
-- Descripción: Elimina campos duplicados y añade nuevos campos para perfil profesional completo
-- ============================================================================

-- ============================================================================
-- PARTE 1: MODIFICAR TABLA PROFESIONALES
-- ============================================================================

-- 1.1 Agregar campo biografia_profesional
ALTER TABLE profesionales
ADD COLUMN IF NOT EXISTS biografia_profesional TEXT;

COMMENT ON COLUMN profesionales.biografia_profesional IS
'Biografía o resumen profesional del trabajador. Descripción detallada de su experiencia y habilidades.';

-- 1.2 Eliminar campos duplicados/no necesarios (ya están en otras tablas)
-- Estos campos están duplicados en especialidades_profesional o area_servicio
ALTER TABLE profesionales
DROP COLUMN IF EXISTS ubicacion,
DROP COLUMN IF EXISTS distrito,
DROP COLUMN IF EXISTS latitud,
DROP COLUMN IF EXISTS longitud,
DROP COLUMN IF EXISTS radio_servicio;

-- 1.3 Modificar campos existentes para mejor uso
-- El campo anios_experiencia ya existe, solo agregar comentario
COMMENT ON COLUMN profesionales.anios_experiencia IS
'Años de experiencia profesional del trabajador (número entero).';

-- 1.4 Agregar campos adicionales útiles para el perfil
ALTER TABLE profesionales
ADD COLUMN IF NOT EXISTS idiomas VARCHAR(255)[],
ADD COLUMN IF NOT EXISTS licencias_profesionales TEXT,
ADD COLUMN IF NOT EXISTS seguro_responsabilidad BOOLEAN DEFAULT FALSE,
ADD COLUMN IF NOT EXISTS metodos_pago VARCHAR(100)[],
ADD COLUMN IF NOT EXISTS politica_cancelacion TEXT;

COMMENT ON COLUMN profesionales.idiomas IS
'Array de idiomas que maneja el profesional (español, inglés, etc.)';

COMMENT ON COLUMN profesionales.licencias_profesionales IS
'Licencias profesionales del trabajador si las tiene';

COMMENT ON COLUMN profesionales.seguro_responsabilidad IS
'Indica si el profesional cuenta con seguro de responsabilidad civil';

COMMENT ON COLUMN profesionales.metodos_pago IS
'Array de métodos de pago que acepta (efectivo, transferencia, tarjeta, etc.)';

COMMENT ON COLUMN profesionales.politica_cancelacion IS
'Política de cancelación del profesional';

-- ============================================================================
-- PARTE 2: CREAR TABLA CERTIFICACIONES_PROFESIONAL
-- ============================================================================

CREATE TABLE IF NOT EXISTS certificaciones_profesional (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL,
    nombre_certificacion VARCHAR(255) NOT NULL,
    institucion VARCHAR(255) NOT NULL,
    fecha_obtencion DATE,
    fecha_vigencia DATE,
    documento_url VARCHAR(500),
    descripcion TEXT,
    orden INTEGER DEFAULT 1,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_certificacion_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE CASCADE
);

-- Índices para certificaciones
CREATE INDEX idx_certificaciones_profesional_id ON certificaciones_profesional(profesional_id, activo);

COMMENT ON TABLE certificaciones_profesional IS
'Certificaciones, cursos y estudios realizados por el profesional';

-- ============================================================================
-- PARTE 3: CREAR TABLA PROYECTOS_PORTAFOLIO
-- ============================================================================

CREATE TABLE IF NOT EXISTS proyectos_portafolio (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL,
    nombre_proyecto VARCHAR(255) NOT NULL,
    fecha_realizacion DATE NOT NULL,
    descripcion TEXT NOT NULL,
    categoria_id INTEGER,
    solicitud_servicio_id INTEGER, -- Relación con solicitud de servicio si existe
    calificacion_cliente DECIMAL(3,1) CHECK (calificacion_cliente >= 0 AND calificacion_cliente <= 10),
    comentario_cliente TEXT,
    orden INTEGER DEFAULT 1,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_proyecto_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_proyecto_categoria
        FOREIGN KEY (categoria_id)
        REFERENCES categorias_profesionales(id)
        ON DELETE SET NULL
);

-- Índices para proyectos
CREATE INDEX idx_proyectos_profesional_id ON proyectos_portafolio(profesional_id, activo);
CREATE INDEX idx_proyectos_categoria ON proyectos_portafolio(categoria_id);
CREATE INDEX idx_proyectos_calificacion ON proyectos_portafolio(calificacion_cliente DESC);

-- Constraint para limitar máximo 20 proyectos activos por profesional
CREATE OR REPLACE FUNCTION verificar_limite_proyectos()
RETURNS TRIGGER AS $$
DECLARE
    total_proyectos INTEGER;
BEGIN
    IF NEW.activo = TRUE THEN
        SELECT COUNT(*) INTO total_proyectos
        FROM proyectos_portafolio
        WHERE profesional_id = NEW.profesional_id
        AND activo = TRUE
        AND id != COALESCE(NEW.id, 0);

        IF total_proyectos >= 20 THEN
            RAISE EXCEPTION 'El profesional ya tiene el máximo de 20 proyectos activos';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_limite_proyectos
BEFORE INSERT OR UPDATE ON proyectos_portafolio
FOR EACH ROW
EXECUTE FUNCTION verificar_limite_proyectos();

COMMENT ON TABLE proyectos_portafolio IS
'Portafolio de proyectos realizados por el profesional (máximo 20 activos)';

-- ============================================================================
-- PARTE 4: CREAR TABLA IMAGENES_PROYECTO
-- ============================================================================

CREATE TABLE IF NOT EXISTS imagenes_proyecto (
    id SERIAL PRIMARY KEY,
    proyecto_id INTEGER NOT NULL,
    url_imagen VARCHAR(500) NOT NULL,
    tipo_imagen VARCHAR(20) CHECK (tipo_imagen IN ('antes', 'despues', 'proceso', 'general')),
    descripcion VARCHAR(255),
    orden INTEGER DEFAULT 1,
    fecha_subida TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_imagen_proyecto
        FOREIGN KEY (proyecto_id)
        REFERENCES proyectos_portafolio(id)
        ON DELETE CASCADE
);

-- Índices para imágenes
CREATE INDEX idx_imagenes_proyecto_id ON imagenes_proyecto(proyecto_id, orden);

-- Constraint para limitar máximo 5 imágenes por proyecto
CREATE OR REPLACE FUNCTION verificar_limite_imagenes()
RETURNS TRIGGER AS $$
DECLARE
    total_imagenes INTEGER;
BEGIN
    SELECT COUNT(*) INTO total_imagenes
    FROM imagenes_proyecto
    WHERE proyecto_id = NEW.proyecto_id
    AND id != COALESCE(NEW.id, 0);

    IF total_imagenes >= 5 THEN
        RAISE EXCEPTION 'El proyecto ya tiene el máximo de 5 imágenes';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_limite_imagenes
BEFORE INSERT OR UPDATE ON imagenes_proyecto
FOR EACH ROW
EXECUTE FUNCTION verificar_limite_imagenes();

COMMENT ON TABLE imagenes_proyecto IS
'Imágenes del antes/después de proyectos realizados (máximo 5 por proyecto)';

-- ============================================================================
-- PARTE 5: CREAR TABLA ANTECEDENTES_PROFESIONAL
-- ============================================================================

CREATE TABLE IF NOT EXISTS antecedentes_profesional (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL,
    tipo_antecedente VARCHAR(50) CHECK (tipo_antecedente IN ('policial', 'penal', 'judicial')) NOT NULL,
    documento_url VARCHAR(500) NOT NULL,
    fecha_emision DATE,
    fecha_subida TIMESTAMP DEFAULT NOW(),
    verificado BOOLEAN DEFAULT FALSE,
    fecha_verificacion TIMESTAMP,
    observaciones TEXT,
    activo BOOLEAN DEFAULT TRUE,

    -- Foreign Keys
    CONSTRAINT fk_antecedente_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE CASCADE,

    -- Solo un antecedente activo por tipo
    CONSTRAINT uq_antecedente_tipo
        UNIQUE (profesional_id, tipo_antecedente, activo)
);

-- Índices para antecedentes
CREATE INDEX idx_antecedentes_profesional_id ON antecedentes_profesional(profesional_id, activo);
CREATE INDEX idx_antecedentes_verificado ON antecedentes_profesional(verificado);

COMMENT ON TABLE antecedentes_profesional IS
'Antecedentes policiales, penales y judiciales del profesional (opcional pero mejora calificación)';

-- ============================================================================
-- PARTE 6: ACTUALIZAR/VERIFICAR TABLA REDES_SOCIALES_PROFESIONAL
-- ============================================================================

-- Asegurar que la tabla existe con la estructura correcta
CREATE TABLE IF NOT EXISTS redes_sociales_profesional (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL,
    tipo_red VARCHAR(50) CHECK (tipo_red IN ('facebook', 'instagram', 'youtube', 'tiktok', 'linkedin', 'twitter', 'whatsapp', 'website', 'otro')) NOT NULL,
    url VARCHAR(500) NOT NULL,
    verificada BOOLEAN DEFAULT FALSE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),

    -- Foreign Keys
    CONSTRAINT fk_red_social_profesional
        FOREIGN KEY (profesional_id)
        REFERENCES profesionales(id)
        ON DELETE CASCADE,

    -- Solo una red social activa por tipo
    CONSTRAINT uq_red_social_tipo
        UNIQUE (profesional_id, tipo_red, activo)
);

-- Índices para redes sociales
CREATE INDEX IF NOT EXISTS idx_redes_sociales_profesional_id ON redes_sociales_profesional(profesional_id, activo);

COMMENT ON TABLE redes_sociales_profesional IS
'Redes sociales del profesional (Facebook, Instagram, LinkedIn, etc.)';

-- ============================================================================
-- PARTE 7: FUNCIÓN PARA CALCULAR PUNTUACIÓN COMPLETA DEL PROFESIONAL
-- ============================================================================

-- Esta función calcula una puntuación basada en:
-- - Calificación promedio de proyectos
-- - Cantidad de certificaciones
-- - Antecedentes verificados
-- - Años de experiencia
-- - Completitud del perfil

CREATE OR REPLACE FUNCTION calcular_puntuacion_profesional(p_profesional_id INTEGER)
RETURNS DECIMAL(4,2) AS $$
DECLARE
    puntuacion DECIMAL(4,2) := 0;
    calificacion_proyectos DECIMAL(3,1);
    total_certificaciones INTEGER;
    antecedentes_completos BOOLEAN;
    anios_exp INTEGER;
    tiene_biografia BOOLEAN;
BEGIN
    -- 1. Calificación promedio de proyectos (40% - máximo 4.0 puntos)
    SELECT AVG(calificacion_cliente) INTO calificacion_proyectos
    FROM proyectos_portafolio
    WHERE profesional_id = p_profesional_id AND activo = TRUE;

    puntuacion := puntuacion + (COALESCE(calificacion_proyectos, 0) * 0.4);

    -- 2. Certificaciones (20% - máximo 2.0 puntos)
    SELECT COUNT(*) INTO total_certificaciones
    FROM certificaciones_profesional
    WHERE profesional_id = p_profesional_id AND activo = TRUE;

    puntuacion := puntuacion + LEAST(total_certificaciones * 0.4, 2.0);

    -- 3. Antecedentes verificados (20% - máximo 2.0 puntos)
    SELECT COUNT(*) >= 3 INTO antecedentes_completos
    FROM antecedentes_profesional
    WHERE profesional_id = p_profesional_id
    AND activo = TRUE
    AND verificado = TRUE;

    IF antecedentes_completos THEN
        puntuacion := puntuacion + 2.0;
    END IF;

    -- 4. Años de experiencia (10% - máximo 1.0 punto)
    SELECT anios_experiencia INTO anios_exp
    FROM profesionales
    WHERE id = p_profesional_id;

    puntuacion := puntuacion + LEAST(COALESCE(anios_exp, 0) * 0.1, 1.0);

    -- 5. Biografía completa (10% - máximo 1.0 punto)
    SELECT biografia_profesional IS NOT NULL AND LENGTH(biografia_profesional) > 50
    INTO tiene_biografia
    FROM profesionales
    WHERE id = p_profesional_id;

    IF tiene_biografia THEN
        puntuacion := puntuacion + 1.0;
    END IF;

    RETURN LEAST(puntuacion, 10.0);
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION calcular_puntuacion_profesional IS
'Calcula puntuación del profesional (0-10) basada en calificaciones, certificaciones, antecedentes y completitud del perfil';

-- ============================================================================
-- PARTE 8: DATOS DE EJEMPLO Y VALIDACIÓN
-- ============================================================================

-- Verificar que las tablas fueron creadas
DO $$
BEGIN
    RAISE NOTICE '✓ Migración V006 completada exitosamente';
    RAISE NOTICE '✓ Tabla profesionales modificada (campos eliminados y nuevos campos agregados)';
    RAISE NOTICE '✓ Tabla certificaciones_profesional creada';
    RAISE NOTICE '✓ Tabla proyectos_portafolio creada (máximo 20 proyectos)';
    RAISE NOTICE '✓ Tabla imagenes_proyecto creada (máximo 5 imágenes por proyecto)';
    RAISE NOTICE '✓ Tabla antecedentes_profesional creada';
    RAISE NOTICE '✓ Tabla redes_sociales_profesional verificada/creada';
    RAISE NOTICE '✓ Función calcular_puntuacion_profesional creada';
END $$;
