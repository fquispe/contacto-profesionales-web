-- =====================================================================
-- Migration Script: Add cost and materials fields to especialidades_profesional
-- =====================================================================
-- Adds columns for cost information, materials option, and ordering
-- to the especialidades_profesional table
-- =====================================================================

-- Add new columns to especialidades_profesional table
ALTER TABLE IF EXISTS especialidades_profesional
    ADD COLUMN IF NOT EXISTS costo DECIMAL(10, 2),
    ADD COLUMN IF NOT EXISTS tipo_costo VARCHAR(20) CHECK (tipo_costo IN ('hora', 'dia', 'mes')),
    ADD COLUMN IF NOT EXISTS incluye_materiales BOOLEAN DEFAULT FALSE NOT NULL,
    ADD COLUMN IF NOT EXISTS orden SMALLINT DEFAULT 1 CHECK (orden BETWEEN 1 AND 3);

-- Add comment to columns
COMMENT ON COLUMN especialidades_profesional.costo IS 'Costo del servicio profesional';
COMMENT ON COLUMN especialidades_profesional.tipo_costo IS 'Tipo de costo: hora, dia o mes';
COMMENT ON COLUMN especialidades_profesional.incluye_materiales IS 'Indica si el servicio incluye materiales';
COMMENT ON COLUMN especialidades_profesional.orden IS 'Orden de la especialidad (1-3) para un profesional';

-- Create index for better query performance
CREATE INDEX IF NOT EXISTS idx_especialidades_profesional_orden
    ON especialidades_profesional(profesional_id, orden);

-- Add constraint to ensure unique orden per professional
-- First, we need to handle existing data if any
DO $$
DECLARE
    v_profesional_id INT;
    v_contador INT;
BEGIN
    -- For each professional, assign orden values 1, 2, 3 to their existing especialidades
    FOR v_profesional_id IN
        SELECT DISTINCT profesional_id
        FROM especialidades_profesional
        WHERE orden IS NULL OR orden = 1
    LOOP
        v_contador := 1;
        UPDATE especialidades_profesional
        SET orden = v_contador
        WHERE id IN (
            SELECT id FROM especialidades_profesional
            WHERE profesional_id = v_profesional_id
            ORDER BY es_principal DESC, fecha_creacion ASC
            LIMIT 1
        );

        v_contador := v_contador + 1;

        UPDATE especialidades_profesional
        SET orden = v_contador
        WHERE id IN (
            SELECT id FROM especialidades_profesional
            WHERE profesional_id = v_profesional_id AND orden IS NULL
            ORDER BY es_principal DESC, fecha_creacion ASC
            LIMIT 1
        );

        v_contador := v_contador + 1;

        UPDATE especialidades_profesional
        SET orden = v_contador
        WHERE id IN (
            SELECT id FROM especialidades_profesional
            WHERE profesional_id = v_profesional_id AND orden IS NULL
            ORDER BY es_principal DESC, fecha_creacion ASC
            LIMIT 1
        );
    END LOOP;
END $$;

-- Now add the unique constraint
-- Note: This ensures each professional has unique orden values (1, 2, 3)
-- But doesn't enforce that they must be consecutive
ALTER TABLE especialidades_profesional
    ADD CONSTRAINT uq_profesional_orden UNIQUE (profesional_id, orden);

-- Verificar la estructura actualizada
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'especialidades_profesional'
ORDER BY ordinal_position;

-- Success message
DO $$
BEGIN
    RAISE NOTICE '✓ Migration completed successfully!';
    RAISE NOTICE '  - Added columns: costo, tipo_costo, incluye_materiales, orden';
    RAISE NOTICE '  - Added constraints and indexes';
    RAISE NOTICE '  - Updated existing data with proper orden values';
END $$;
