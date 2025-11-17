-- ============================================================================
-- Script de Migración V003: Mejoras para Actualización Dinámica de Especialidades
-- Fecha: 2025-11-15
-- Descripción: Optimizaciones para permitir actualización dinámica con soft delete
-- ============================================================================

-- 1. Agregar índice compuesto para mejorar rendimiento en consultas de especialidades activas
CREATE INDEX IF NOT EXISTS idx_especialidades_profesional_activo
ON especialidades_profesional(profesional_id, activo)
WHERE activo = TRUE;

-- 2. Agregar índice para búsquedas por categoría (permite múltiples especialidades de misma categoría)
CREATE INDEX IF NOT EXISTS idx_especialidades_profesional_categoria
ON especialidades_profesional(categoria_id, activo);

-- 3. Modificar constraint de orden para que solo aplique a registros activos
-- Primero eliminar constraint existente si existe
ALTER TABLE especialidades_profesional
DROP CONSTRAINT IF EXISTS especialidades_profesional_orden_check;

-- Crear nuevo constraint que solo valide registros activos
ALTER TABLE especialidades_profesional
ADD CONSTRAINT especialidades_profesional_orden_activo_check
CHECK (
    (activo = FALSE) OR
    (activo = TRUE AND orden >= 1 AND orden <= 3)
);

-- 4. Añadir constraint para limitar máximo 3 especialidades ACTIVAS por profesional
-- Nota: Esto se manejará a nivel de aplicación, pero dejamos comentado para referencia
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_especialidades_max_3_activas
-- ON especialidades_profesional(profesional_id, orden)
-- WHERE activo = TRUE;

-- 5. Comentarios en la tabla para documentar el comportamiento
COMMENT ON COLUMN especialidades_profesional.activo IS
'Indica si la especialidad está activa (soft delete). TRUE = activa, FALSE = eliminada lógicamente';

COMMENT ON COLUMN especialidades_profesional.orden IS
'Orden de la especialidad (1-3). Solo aplica a especialidades activas. Puede haber huecos en numeración si se eliminan especialidades';

-- 6. Crear función para reordenar especialidades activas automáticamente
-- Esta función asegura que las especialidades activas tengan orden consecutivo 1,2,3
CREATE OR REPLACE FUNCTION reordenar_especialidades_activas()
RETURNS TRIGGER AS $$
BEGIN
    -- Solo ejecutar si se está desactivando una especialidad
    IF (TG_OP = 'UPDATE' AND OLD.activo = TRUE AND NEW.activo = FALSE) THEN
        -- Reordenar las especialidades restantes activas
        WITH especialidades_activas AS (
            SELECT id, ROW_NUMBER() OVER (ORDER BY orden) as nuevo_orden
            FROM especialidades_profesional
            WHERE profesional_id = NEW.profesional_id
            AND activo = TRUE
            AND id != NEW.id
        )
        UPDATE especialidades_profesional e
        SET orden = ea.nuevo_orden,
            fecha_actualizacion = NOW()
        FROM especialidades_activas ea
        WHERE e.id = ea.id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 7. Crear trigger para reordenamiento automático
DROP TRIGGER IF EXISTS trigger_reordenar_especialidades ON especialidades_profesional;

CREATE TRIGGER trigger_reordenar_especialidades
AFTER UPDATE ON especialidades_profesional
FOR EACH ROW
WHEN (OLD.activo IS DISTINCT FROM NEW.activo)
EXECUTE FUNCTION reordenar_especialidades_activas();

-- 8. Agregar columna de auditoría para rastrear quién eliminó (opcional, pero recomendado)
-- ALTER TABLE especialidades_profesional
-- ADD COLUMN IF NOT EXISTS fecha_eliminacion TIMESTAMP DEFAULT NULL;
--
-- COMMENT ON COLUMN especialidades_profesional.fecha_eliminacion IS
-- 'Fecha en que se marcó como inactiva la especialidad (soft delete)';

-- ============================================================================
-- NOTAS IMPORTANTES:
-- ============================================================================
-- 1. Las especialidades pueden ser de la MISMA o DIFERENTE categoría
-- 2. Un profesional puede tener hasta 3 especialidades ACTIVAS simultáneamente
-- 3. Las especialidades eliminadas (activo=FALSE) se mantienen en BD para auditoría
-- 4. El orden se reajusta automáticamente al eliminar una especialidad
-- 5. No hay restricción de unicidad en categoría - pueden haber duplicados
-- ============================================================================
