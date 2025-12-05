-- ============================================================================
-- Migración V008: Refactorización de Ubicación en Solicitudes de Servicio
-- ============================================================================
-- Descripción: Agrega campos para ubicación geográfica estructurada,
--              modalidad de trabajo (remoto/presencial) y especialidad específica.
--
-- Cambios:
-- 1. Agregar campos de ubicación con IDs (departamento_id, provincia_id, distrito_id)
-- 2. Agregar campo tipo_prestacion (REMOTO/PRESENCIAL)
-- 3. Agregar campo especialidad_id
-- 4. Crear foreign keys
-- 5. Crear índices para optimización
--
-- Nota: El campo 'distrito' (texto) se mantiene por compatibilidad pero
--       quedará deprecado en favor de distrito_id.
-- ============================================================================

-- 1. Agregar nuevos campos a la tabla solicitudes_servicio
ALTER TABLE solicitudes_servicio
ADD COLUMN departamento_id INTEGER,
ADD COLUMN provincia_id INTEGER,
ADD COLUMN distrito_id INTEGER,
ADD COLUMN tipo_prestacion VARCHAR(20) CHECK (tipo_prestacion IN ('REMOTO', 'PRESENCIAL')),
ADD COLUMN especialidad_id INTEGER;

-- 2. Agregar foreign keys con restricciones
ALTER TABLE solicitudes_servicio
ADD CONSTRAINT fk_solicitudes_departamento
    FOREIGN KEY (departamento_id) REFERENCES departamentos(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,

ADD CONSTRAINT fk_solicitudes_provincia
    FOREIGN KEY (provincia_id) REFERENCES provincias(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,

ADD CONSTRAINT fk_solicitudes_distrito
    FOREIGN KEY (distrito_id) REFERENCES distritos(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE,

ADD CONSTRAINT fk_solicitudes_especialidad
    FOREIGN KEY (especialidad_id) REFERENCES especialidades_profesional(id)
    ON DELETE SET NULL
    ON UPDATE CASCADE;

-- 3. Crear índices para optimizar consultas
CREATE INDEX idx_solicitudes_departamento ON solicitudes_servicio(departamento_id);
CREATE INDEX idx_solicitudes_provincia ON solicitudes_servicio(provincia_id);
CREATE INDEX idx_solicitudes_distrito ON solicitudes_servicio(distrito_id);
CREATE INDEX idx_solicitudes_especialidad ON solicitudes_servicio(especialidad_id);
CREATE INDEX idx_solicitudes_tipo_prestacion ON solicitudes_servicio(tipo_prestacion);

-- 4. Agregar comentarios documentando los campos
COMMENT ON COLUMN solicitudes_servicio.departamento_id IS 'ID del departamento (ubicación geográfica estructurada). NULL si es trabajo remoto.';
COMMENT ON COLUMN solicitudes_servicio.provincia_id IS 'ID de la provincia (ubicación geográfica estructurada). NULL si es trabajo remoto.';
COMMENT ON COLUMN solicitudes_servicio.distrito_id IS 'ID del distrito (ubicación geográfica estructurada). NULL si es trabajo remoto. Reemplaza al campo distrito (texto).';
COMMENT ON COLUMN solicitudes_servicio.tipo_prestacion IS 'Modalidad del servicio: REMOTO (virtual) o PRESENCIAL (en domicilio). Determina si se requiere dirección física.';
COMMENT ON COLUMN solicitudes_servicio.especialidad_id IS 'FK a especialidad específica solicitada del profesional. Relaciona con especialidades_profesional.id.';

-- 5. Comentario de deprecación para campo distrito (texto)
COMMENT ON COLUMN solicitudes_servicio.distrito IS 'DEPRECADO: Campo de texto libre para distrito. Usar distrito_id (FK) en su lugar.';

-- ============================================================================
-- Fin de la migración V008
-- ============================================================================
