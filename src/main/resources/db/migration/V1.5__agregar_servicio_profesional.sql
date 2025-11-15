-- ============================================================================
-- Migración: Agregar campo servicio_profesional a especialidades_profesional
-- Versión: 1.5
-- Fecha: 2025-01-14
-- Descripción: Añade campo de texto libre para que el profesional describa
--              el servicio específico que brindará en cada especialidad
-- ============================================================================

-- Agregar columna servicio_profesional (obligatoria, texto)
ALTER TABLE especialidades_profesional
ADD COLUMN servicio_profesional VARCHAR(255) NOT NULL DEFAULT 'Servicio no especificado';

-- Comentario descriptivo en la columna
COMMENT ON COLUMN especialidades_profesional.servicio_profesional IS
'Nombre o descripción del servicio específico que brinda el profesional en esta especialidad';

-- Verificar que la columna se agregó correctamente
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'especialidades_profesional'
        AND column_name = 'servicio_profesional'
    ) THEN
        RAISE NOTICE 'Columna servicio_profesional agregada exitosamente';
    ELSE
        RAISE EXCEPTION 'Error: No se pudo agregar la columna servicio_profesional';
    END IF;
END $$;
