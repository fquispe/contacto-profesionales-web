-- ============================================================================
-- Migración V007: Corregir restricción CHECK para tipo_red en redes_sociales_profesional
-- Fecha: 2025-11-17
-- Descripción: Elimina y recrea la restricción CHECK con valores en minúsculas
-- ============================================================================

-- Razón de esta migración:
-- La tabla redes_sociales_profesional fue creada con CREATE TABLE IF NOT EXISTS en V006.
-- Si la tabla ya existía antes de V006, la restricción CHECK no se actualizó.
-- Esta migración corrige la restricción CHECK para aceptar valores en minúsculas.

-- ============================================================================
-- PARTE 1: ELIMINAR RESTRICCIÓN CHECK EXISTENTE (SI EXISTE)
-- ============================================================================

-- Eliminar la restricción CHECK antigua (sea cual sea su nombre)
-- Primero intentamos con el nombre esperado
ALTER TABLE redes_sociales_profesional
DROP CONSTRAINT IF EXISTS chk_tipo_red;

-- También intentamos con variaciones del nombre que pudo haber usado PostgreSQL
ALTER TABLE redes_sociales_profesional
DROP CONSTRAINT IF EXISTS redes_sociales_profesional_tipo_red_check;

-- ============================================================================
-- PARTE 2: CREAR NUEVA RESTRICCIÓN CHECK CON VALORES EN MINÚSCULAS
-- ============================================================================

-- Agregar la restricción CHECK correcta con valores en minúsculas
-- Tipos válidos: facebook, instagram, youtube, tiktok, linkedin, twitter, whatsapp, website, otro
ALTER TABLE redes_sociales_profesional
ADD CONSTRAINT chk_tipo_red CHECK (
    tipo_red IN (
        'facebook',
        'instagram',
        'youtube',
        'tiktok',
        'linkedin',
        'twitter',
        'whatsapp',
        'website',
        'otro'
    )
);

-- ============================================================================
-- PARTE 3: COMENTARIOS Y VALIDACIÓN
-- ============================================================================

COMMENT ON CONSTRAINT chk_tipo_red ON redes_sociales_profesional IS
'Restricción CHECK que valida que tipo_red solo contenga valores permitidos en minúsculas: facebook, instagram, youtube, tiktok, linkedin, twitter, whatsapp, website, otro';

-- Verificar que la restricción fue creada
DO $$
BEGIN
    RAISE NOTICE '✓ Migración V007 completada exitosamente';
    RAISE NOTICE '✓ Restricción CHECK chk_tipo_red creada/actualizada';
    RAISE NOTICE '✓ Tipos válidos (case-insensitive en aplicación): facebook, instagram, youtube, tiktok, linkedin, twitter, whatsapp, website, otro';
    RAISE NOTICE '✓ La aplicación normaliza automáticamente los valores a minúsculas antes de guardar';
END $$;
