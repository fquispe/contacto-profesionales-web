# 🔧 Solución: Error `uq_profesional_orden` - Llave Duplicada

**Fecha:** 2025-11-15
**Error:** `llave duplicada viola restricción de unicidad «uq_profesional_orden»`
**Tipo:** Error de Base de Datos + Código Java

---

## ❌ Problema Identificado

### Error Completo:
```
ERROR: llave duplicada viola restricción de unicidad «uq_profesional_orden»
Detail: Ya existe la llave (profesional_id, orden)=(1, 1).
```

### Causa Raíz:

La tabla `especialidades_profesional` tiene una constraint:

```sql
CONSTRAINT uq_profesional_orden UNIQUE (profesional_id, orden)
```

Esta constraint aplica a **TODOS** los registros (activos E inactivos), causando errores cuando:

1. ✅ Se desactivan 2 especialidades viejas (soft delete) → quedan con `activo=FALSE, orden=1, orden=2`
2. ❌ Se intenta insertar nueva especialidad con `orden=1` → **FALLA** porque ya existe un registro inactivo con `orden=1`

### Flujo del Error:

```
REQUEST: Actualizar especialidades
  ↓
1. Desactivar especialidades viejas (id=1, id=2)
   profesional_id=1, id=1, orden=1, activo=FALSE ✅
   profesional_id=1, id=2, orden=2, activo=FALSE ✅
  ↓
2. Insertar nuevas especialidades
   profesional_id=1, orden=1, activo=TRUE ❌ ERROR

RAZÓN: Ya existe profesional_id=1, orden=1 (aunque esté inactivo)
```

---

## ✅ Solución Implementada

### Cambio 1: Base de Datos (SQL)

**Archivo:** `V005__corregir_constraint_orden_solo_activos.sql`

#### Acciones:

1. **Eliminar** constraint antigua que aplica a todos los registros
2. **Crear** índice único parcial que SOLO aplica a registros activos
3. **Limpiar** campo `orden` de registros inactivos (establecer a NULL)

#### SQL Esencial (versión rápida):

```sql
-- 1. Eliminar constraint antigua
ALTER TABLE especialidades_profesional
DROP CONSTRAINT IF EXISTS uq_profesional_orden;

-- 2. Eliminar índice si existe
DROP INDEX IF EXISTS uq_profesional_orden;

-- 3. Crear índice parcial (solo para activos)
CREATE UNIQUE INDEX idx_profesional_orden_activo
ON especialidades_profesional (profesional_id, orden)
WHERE activo = TRUE;

-- 4. Limpiar órdenes de registros inactivos
UPDATE especialidades_profesional
SET orden = NULL
WHERE activo = FALSE;
```

---

### Cambio 2: Código Java (DAO)

**Archivo:** `ServiciosProfesionalDAOImpl.java`

#### Modificaciones en 3 métodos:

**1. `actualizarEspecialidadesInterno()` - Línea 348**

```java
// ✅ ANTES:
String sqlDesactivar = "UPDATE especialidades_profesional " +
                      "SET activo = FALSE, fecha_actualizacion = NOW() " +
                      "WHERE profesional_id = ? AND activo = TRUE";

// ✅ DESPUÉS:
String sqlDesactivar = "UPDATE especialidades_profesional " +
                      "SET activo = FALSE, orden = NULL, fecha_actualizacion = NOW() " +
                      "WHERE profesional_id = ? AND activo = TRUE";
```

**2. `desactivarTodasEspecialidadesInterno()` - Línea 459**

```java
// ✅ ANTES:
String sql = "UPDATE especialidades_profesional SET activo = FALSE, fecha_actualizacion = NOW() " +
            "WHERE profesional_id = ? AND activo = TRUE";

// ✅ DESPUÉS:
String sql = "UPDATE especialidades_profesional SET activo = FALSE, orden = NULL, fecha_actualizacion = NOW() " +
            "WHERE profesional_id = ? AND activo = TRUE";
```

**3. `eliminarEspecialidadesPorProfesionalInterno()` - Línea 324**

```java
// ✅ ANTES:
String sql = "UPDATE especialidades_profesional SET activo = FALSE, fecha_actualizacion = NOW() WHERE profesional_id = ? AND activo = TRUE";

// ✅ DESPUÉS:
String sql = "UPDATE especialidades_profesional SET activo = FALSE, orden = NULL, fecha_actualizacion = NOW() WHERE profesional_id = ? AND activo = TRUE";
```

---

## 🔧 Pasos de Implementación

### Opción 1: Ejecutar SQL Completo

```bash
psql -U postgres -d contacto_profesionales_db -f "src/main/resources/db/V005__corregir_constraint_orden_solo_activos.sql"
```

### Opción 2: SQL Rápido (Recomendado)

**Abrir pgAdmin o tu cliente PostgreSQL** y ejecutar:

```sql
ALTER TABLE especialidades_profesional DROP CONSTRAINT IF EXISTS uq_profesional_orden;
DROP INDEX IF EXISTS uq_profesional_orden;
CREATE UNIQUE INDEX idx_profesional_orden_activo ON especialidades_profesional (profesional_id, orden) WHERE activo = TRUE;
UPDATE especialidades_profesional SET orden = NULL WHERE activo = FALSE;
```

### Opción 3: Archivo SQL Simple

```bash
psql -U postgres -d contacto_profesionales_db -f "src/main/resources/db/V005_SIMPLE_ejecutar_primero.sql"
```

---

## ✔️ Verificación

### 1. Verificar que la constraint antigua fue eliminada:

```sql
SELECT constraint_name
FROM information_schema.table_constraints
WHERE table_name = 'especialidades_profesional'
  AND constraint_name = 'uq_profesional_orden';
```

**Resultado esperado:** 0 filas (no existe)

### 2. Verificar que el índice parcial existe:

```sql
SELECT indexname, indexdef
FROM pg_indexes
WHERE tablename = 'especialidades_profesional'
  AND indexname = 'idx_profesional_orden_activo';
```

**Resultado esperado:** 1 fila mostrando el índice parcial con `WHERE (activo = true)`

### 3. Verificar registros inactivos tienen orden NULL:

```sql
SELECT id, profesional_id, orden, activo
FROM especialidades_profesional
WHERE activo = FALSE
  AND orden IS NOT NULL;
```

**Resultado esperado:** 0 filas (todos los inactivos tienen orden=NULL)

### 4. Verificar registros activos tienen orden válido:

```sql
SELECT id, profesional_id, orden, activo
FROM especialidades_profesional
WHERE activo = TRUE
ORDER BY profesional_id, orden;
```

**Resultado esperado:** Cada profesional tiene órdenes 1, 2, 3 sin duplicados

---

## 📊 Comportamiento Antes vs Después

### ANTES (con error):

```
Tabla: especialidades_profesional
+----+----------------+-------+--------+
| id | profesional_id | orden | activo |
+----+----------------+-------+--------+
|  1 |              1 |     1 | FALSE  | ← Registro viejo desactivado
|  2 |              1 |     2 | FALSE  | ← Registro viejo desactivado
|  3 |              1 |     1 | TRUE   | ❌ ERROR: duplica orden=1
+----+----------------+-------+--------+

CONSTRAINT: UNIQUE (profesional_id, orden) aplica a TODOS
RESULTADO: ❌ Error al insertar fila con id=3
```

### DESPUÉS (solucionado):

```
Tabla: especialidades_profesional
+----+----------------+-------+--------+
| id | profesional_id | orden | activo |
+----+----------------+-------+--------+
|  1 |              1 |  NULL | FALSE  | ✅ Orden limpiado (NULL)
|  2 |              1 |  NULL | FALSE  | ✅ Orden limpiado (NULL)
|  3 |              1 |     1 | TRUE   | ✅ OK: índice solo valida activos
|  4 |              1 |     2 | TRUE   | ✅ OK
+----+----------------+-------+--------+

ÍNDICE PARCIAL: UNIQUE (profesional_id, orden) WHERE activo = TRUE
RESULTADO: ✅ Inserción exitosa
```

---

## 🎯 Archivos Creados/Modificados

### Nuevos:
1. ✅ `V005__corregir_constraint_orden_solo_activos.sql` - Migración completa
2. ✅ `V005_SIMPLE_ejecutar_primero.sql` - Migración simplificada
3. ✅ `SOLUCION_ERROR_ORDEN_DUPLICADO.md` - Esta documentación

### Modificados:
1. ✅ `ServiciosProfesionalDAOImpl.java` - 3 métodos actualizados:
   - `actualizarEspecialidadesInterno()` (línea 348)
   - `desactivarTodasEspecialidadesInterno()` (línea 459)
   - `eliminarEspecialidadesPorProfesionalInterno()` (línea 324)

---

## 🧪 Prueba Manual

### 1. Crear especialidades iniciales:

Desde `servicios-profesional.html`:
- Especialidad 1: Fontanería residencial
- Especialidad 2: Fontanería industrial

Guardar → ✅ Debe guardarse exitosamente

### 2. Actualizar quitando ambas y agregando dos nuevas:

- Quitar: Fontanería residencial y Fontanería industrial
- Agregar: Electricidad y Carpintería

Guardar → ✅ Debe guardarse exitosamente SIN error de "llave duplicada"

### 3. Verificar en base de datos:

```sql
SELECT * FROM especialidades_profesional
WHERE profesional_id = 1
ORDER BY activo DESC, orden;
```

**Resultado esperado:**
```
id | profesional_id | servicio_profesional      | orden | activo
---+----------------+---------------------------+-------+--------
 3 |              1 | Electricidad              |     1 | t
 4 |              1 | Carpintería               |     2 | t
 1 |              1 | Fontanería residencial    |  NULL | f
 2 |              1 | Fontanería industrial     |  NULL | f
```

---

## 💡 Explicación Técnica

### ¿Por qué índice parcial?

En PostgreSQL, un **índice único parcial** (partial unique index) con cláusula `WHERE` permite:

```sql
CREATE UNIQUE INDEX idx_profesional_orden_activo
ON especialidades_profesional (profesional_id, orden)
WHERE activo = TRUE;
```

- ✅ Garantiza unicidad SOLO para registros con `activo = TRUE`
- ✅ Permite múltiples registros inactivos con mismo orden
- ✅ Más eficiente que validar en código
- ✅ Mantiene integridad a nivel de base de datos

### ¿Por qué limpiar orden a NULL?

Establecer `orden = NULL` en registros inactivos:

- ✅ Evita confusión al leer la base de datos
- ✅ Hace explícito que el orden solo aplica a activos
- ✅ Previene futuros conflictos si se reactiva un registro
- ✅ Mejora la claridad del modelo de datos

---

## 🎓 Conclusión

Esta solución implementa el patrón de **soft delete con validación parcial**, permitiendo:

1. ✅ Mantener historial de especialidades eliminadas (`activo = FALSE`)
2. ✅ Reutilizar órdenes 1, 2, 3 sin conflictos
3. ✅ Validar unicidad solo donde es relevante (registros activos)
4. ✅ Limpiar automáticamente datos no relevantes (orden de inactivos)

**Resultado:** Actualización dinámica de especialidades sin errores de constraint.

---

**Implementado por:** Claude Code
**Fecha:** 2025-11-15
**Status:** ✅ Listo para ejecutar
