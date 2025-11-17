-- ============================================================================
-- Migración: Corregir constraint de orden para que solo aplique a registros activos
-- Versión: V005
-- Fecha: 2025-11-15
-- Descripción: Soluciona error de "llave duplicada viola restricción de unicidad «uq_profesional_orden»"
--              al permitir que registros inactivos puedan tener el mismo orden sin conflicto
-- ============================================================================
--
-- PROBLEMA:
-- La constraint uq_profesional_orden UNIQUE (profesional_id, orden) aplica a TODOS
-- los registros, incluso los inactivos (soft deleted). Esto causa errores cuando:
-- 1. Se desactivan especialidades viejas (soft delete) con orden=1, orden=2
-- 2. Se intentan insertar nuevas especialidades con orden=1, orden=2
-- 3. La constraint falla porque aún existen registros inactivos con esos órdenes
--
-- SOLUCIÓN:
-- Usar un UNIQUE INDEX parcial que solo aplique a registros con activo = TRUE
-- Esto permite que múltiples registros inactivos tengan el mismo orden sin conflicto
--
-- ============================================================================

-- 1. Eliminar la constraint antigua que aplica a todos los registros
ALTER TABLE especialidades_profesional
DROP CONSTRAINT IF EXISTS uq_profesional_orden;

-- 2. También eliminar cualquier índice único que pueda existir con ese nombre
DROP INDEX IF EXISTS uq_profesional_orden;

-- 3. Crear índice único parcial que SOLO aplica a especialidades activas
-- Esto permite que un profesional tenga máximo 3 especialidades activas
-- con orden 1, 2, 3, pero registros inactivos pueden tener cualquier orden
CREATE UNIQUE INDEX IF NOT EXISTS idx_profesional_orden_activo
ON especialidades_profesional (profesional_id, orden)
WHERE activo = TRUE;

-- 4. Comentario explicativo
COMMENT ON INDEX idx_profesional_orden_activo IS
'Garantiza que un profesional solo tenga un registro activo por cada orden (1, 2, 3).
Los registros inactivos (soft deleted) pueden tener órdenes duplicados sin conflicto.';

-- 5. Limpiar órdenes de registros inactivos para evitar confusión
-- Establecer orden = NULL para especialidades inactivas
UPDATE especialidades_profesional
SET orden = NULL
WHERE activo = FALSE AND orden IS NOT NULL;

-- 6. Verificación
DO $$
DECLARE
    constraint_exists BOOLEAN;
    index_exists BOOLEAN;
BEGIN
    -- Verificar que la constraint antigua fue eliminada
    SELECT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'especialidades_profesional'
        AND constraint_name = 'uq_profesional_orden'
    ) INTO constraint_exists;

    -- Verificar que el nuevo índice existe
    SELECT EXISTS (
        SELECT 1
        FROM pg_indexes
        WHERE tablename = 'especialidades_profesional'
        AND indexname = 'idx_profesional_orden_activo'
    ) INTO index_exists;

    IF NOT constraint_exists AND index_exists THEN
        RAISE NOTICE '✓ Migración V005 exitosa';
        RAISE NOTICE '✓ Constraint uq_profesional_orden eliminada';
        RAISE NOTICE '✓ Índice parcial idx_profesional_orden_activo creado';
        RAISE NOTICE '✓ Órdenes de registros inactivos limpiados (NULL)';
    ELSE
        IF constraint_exists THEN
            RAISE WARNING '✗ La constraint uq_profesional_orden aún existe';
        END IF;
        IF NOT index_exists THEN
            RAISE WARNING '✗ El índice idx_profesional_orden_activo no fue creado';
        END IF;
    END IF;
END $$;

-- ============================================================================
-- RESULTADO ESPERADO:
-- ============================================================================
-- ANTES:
-- profesional_id=1, orden=1, activo=FALSE  (registro viejo desactivado)
-- profesional_id=1, orden=1, activo=TRUE   ← ERROR: llave duplicada
--
-- DESPUÉS:
-- profesional_id=1, orden=NULL, activo=FALSE  (registro viejo, orden limpiado)
-- profesional_id=1, orden=1, activo=TRUE      ✓ OK: Solo se valida unicidad en activos
-- ============================================================================
