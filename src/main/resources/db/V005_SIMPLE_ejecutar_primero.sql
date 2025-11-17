-- ============================================================================
-- SOLUCIÓN RÁPIDA: Corregir constraint de orden
-- Ejecutar este script primero si V005 completo falla
-- ============================================================================

-- 1. Eliminar constraint antigua
ALTER TABLE especialidades_profesional DROP CONSTRAINT IF EXISTS uq_profesional_orden;

-- 2. Eliminar índice si existe
DROP INDEX IF EXISTS uq_profesional_orden;

-- 3. Crear índice parcial (solo para activos)
CREATE UNIQUE INDEX idx_profesional_orden_activo
ON especialidades_profesional (profesional_id, orden)
WHERE activo = TRUE;

-- 4. Limpiar órdenes de registros inactivos
UPDATE especialidades_profesional SET orden = NULL WHERE activo = FALSE;

-- 5. Mensaje de confirmación
SELECT 'Migración V005 ejecutada correctamente' as mensaje;
