-- ============================================================================
-- Migración: Eliminar constraint uk_profesional_categoria
-- Versión: V004
-- Fecha: 2025-11-15
-- Descripción: Permite que un profesional tenga múltiples especialidades
--              de la misma categoría, diferenciadas por servicio_profesional
-- ============================================================================
--
-- PROBLEMA ANTERIOR:
-- La constraint uk_profesional_categoria UNIQUE (profesional_id, categoria_id)
-- impedía que un profesional registrara más de una especialidad de la misma
-- categoría, limitando innecesariamente la flexibilidad del sistema.
--
-- EJEMPLO DE CASO DE USO:
-- Un profesional de fontanería puede ofrecer:
-- - Especialidad 1: categoria_id=10 (Fontanería), servicio="Fontanería residencial"
-- - Especialidad 2: categoria_id=10 (Fontanería), servicio="Fontanería industrial"
-- - Especialidad 3: categoria_id=15 (Electricidad), servicio="Instalaciones eléctricas"
--
-- NUEVA LÓGICA:
-- Las especialidades se diferencian por:
-- 1. El campo 'servicio_profesional' (nombre específico del servicio)
-- 2. El campo 'orden' (1, 2, 3) - máximo 3 especialidades activas por profesional
-- 3. El campo 'activo' (solo las activas cuentan para el límite)
--
-- ============================================================================

-- Eliminar la constraint de unicidad que impedía múltiples categorías iguales
ALTER TABLE especialidades_profesional
DROP CONSTRAINT IF EXISTS uk_profesional_categoria;

-- Comentario explicativo
COMMENT ON TABLE especialidades_profesional IS
'Tabla de especialidades de profesionales. Un profesional puede tener hasta 3 especialidades activas,
pudiendo ser de la misma o de diferentes categorías. Las especialidades se diferencian por el campo
servicio_profesional que describe el servicio específico que brindará.';

-- Verificar que la constraint fue eliminada
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.table_constraints
        WHERE table_name = 'especialidades_profesional'
        AND constraint_name = 'uk_profesional_categoria'
    ) THEN
        RAISE NOTICE '✓ Constraint uk_profesional_categoria eliminada exitosamente';
        RAISE NOTICE '✓ Ahora los profesionales pueden tener múltiples especialidades de la misma categoría';
    ELSE
        RAISE WARNING '✗ La constraint uk_profesional_categoria aún existe';
    END IF;
END $$;
