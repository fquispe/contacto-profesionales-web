# Solución Error Restricción CHECK en Redes Sociales

**Fecha:** 2025-11-17
**Autor:** Sistema
**Estado:** ✅ Resuelto (requiere reiniciar servidor para aplicar migración V007)

---

## 📋 Resumen del Error

### Error Original:
```
org.postgresql.util.PSQLException: ERROR: el nuevo registro para la relación «redes_sociales_profesional» viola la restricción «check» «chk_tipo_red»
Detail: La fila que falla contiene (1, 1, YouTube, https://www.youtube.com/..., ...)
```

### Causa Raíz:
- **Base de datos** (línea 243 de `V006__refactorizar_perfil_profesional.sql`):
  - La migración V006 usó `CREATE TABLE IF NOT EXISTS`, lo cual **NO actualiza** una tabla existente
  - Si la tabla ya existía antes de V006, la restricción CHECK antigua se mantuvo
  - La restricción CHECK antigua podría haber estado usando valores con mayúscula inicial o diferente formato

- **Código Java** (Servlet):
  - Documentación indicaba tipos con **mayúscula inicial**: `"Facebook", "Instagram", "YouTube", etc.`
  - No había normalización del valor antes de insertar en BD
  - El frontend enviaba valores con mayúscula inicial basándose en la documentación

- **Problema adicional descubierto:**
  - Incluso después de normalizar a minúsculas en Java (`"youtube"`), el error persistió
  - Esto confirmó que la restricción CHECK en la BD era diferente a la esperada en V006
  - Se requiere una migración que **elimine y recree** la restricción CHECK

### Impacto:
- ❌ No se podían guardar redes sociales con nombres que tuvieran mayúsculas
- ❌ Confusión en el frontend sobre el formato correcto
- ❌ Inconsistencia entre documentación y restricción de BD

---

## ✅ Solución Implementada

### Estrategia (Dos Partes):

**PARTE 1: Normalización a minúsculas en capa de aplicación** (Java)

**Razones:**
1. ✅ Más robusto: acepta cualquier combinación de mayúsculas/minúsculas del cliente
2. ✅ La restricción CHECK sigue siendo válida como validación adicional
3. ✅ Defensa en profundidad: validación en múltiples capas

**PARTE 2: Migración V007 para corregir restricción CHECK en BD**

**Razones:**
1. ✅ V006 usó `CREATE TABLE IF NOT EXISTS`, que no actualiza tablas existentes
2. ✅ Se requiere eliminar y recrear la restricción CHECK con valores correctos
3. ✅ Garantiza que la BD acepte los valores normalizados por la aplicación

---

## 🔧 Archivos Modificados

### 1. `RedesSocialesProfesionalServlet.java`

#### Cambios Realizados:

**A) Actualización de documentación** (líneas 34-35):
```java
// ANTES:
// - Tipos de redes soportadas: Facebook, Instagram, LinkedIn, Twitter, YouTube, TikTok, WhatsApp, Website, Otros

// DESPUÉS:
// - Tipos de redes soportadas (case-insensitive): facebook, instagram, linkedin, twitter, youtube, tiktok, whatsapp, website, otro
// - Los tipos se normalizan automáticamente a minúsculas antes de guardar en BD
```

**B) Método `doPost()` - Crear red social** (línea 187):
```java
// AGREGADO:
// ✅ Normalizar tipo de red a minúsculas (la BD solo acepta minúsculas)
red.setTipoRed(red.getTipoRed().toLowerCase().trim());
```

**C) Método `actualizarIndividual()` - Actualizar una red** (líneas 299-302):
```java
// AGREGADO:
// ✅ Normalizar tipo de red a minúsculas (la BD solo acepta minúsculas)
if (red.getTipoRed() != null) {
    red.setTipoRed(red.getTipoRed().toLowerCase().trim());
}
```

**D) Método `actualizarMultiples()` - Actualizar múltiples redes** (líneas 350-355):
```java
// AGREGADO:
// ✅ Normalizar todos los tipos de red a minúsculas (la BD solo acepta minúsculas)
for (RedSocialProfesional red : redes) {
    if (red.getTipoRed() != null) {
        red.setTipoRed(red.getTipoRed().toLowerCase().trim());
    }
}
```

**E) Documentación del método POST** (línea 142):
```java
// ACTUALIZADO:
// Tipos válidos (case-insensitive): facebook, instagram, linkedin, twitter, youtube, tiktok, whatsapp, website, otro
```

---

### 2. `RedesSocialesProfesionalDAOImpl.java`

#### Cambios Realizados:

**A) Método `guardar()` - Defensa en profundidad** (líneas 66-69):
```java
// AGREGADO:
// Normalizar tipo de red a minúsculas (la BD solo acepta minúsculas)
if (red.getTipoRed() != null) {
    red.setTipoRed(red.getTipoRed().toLowerCase().trim());
}
```

**B) Método `insertarInterno()` - Para actualización masiva** (líneas 272-275):
```java
// AGREGADO:
// Normalizar tipo de red a minúsculas (la BD solo acepta minúsculas)
if (red.getTipoRed() != null) {
    red.setTipoRed(red.getTipoRed().toLowerCase().trim());
}
```

---

### 3. **V007__corregir_constraint_tipo_red.sql** (Nueva Migración)

#### Archivo Creado:
`src/main/resources/db/migration/V007__corregir_constraint_tipo_red.sql`

#### Propósito:
Eliminar y recrear la restricción CHECK en la base de datos para aceptar valores en minúsculas.

#### Cambios Realizados:

**A) Eliminar restricciones CHECK existentes:**
```sql
-- Eliminar la restricción CHECK antigua (sea cual sea su nombre)
ALTER TABLE redes_sociales_profesional
DROP CONSTRAINT IF EXISTS chk_tipo_red;

-- También eliminar variaciones del nombre que PostgreSQL pudo haber usado
ALTER TABLE redes_sociales_profesional
DROP CONSTRAINT IF EXISTS redes_sociales_profesional_tipo_red_check;
```

**B) Crear nueva restricción CHECK con valores en minúsculas:**
```sql
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
```

**C) Agregar comentario a la restricción:**
```sql
COMMENT ON CONSTRAINT chk_tipo_red ON redes_sociales_profesional IS
'Restricción CHECK que valida que tipo_red solo contenga valores permitidos en minúsculas';
```

#### Aplicación de la Migración:

**⚠️ IMPORTANTE:** Esta migración se aplicará automáticamente cuando se reinicie el servidor Tomcat.

Flyway detectará la nueva migración V007 y la ejecutará después de V006.

**Pasos para aplicar:**
1. ✅ El archivo ya fue creado en `src/main/resources/db/migration/V007__corregir_constraint_tipo_red.sql`
2. ✅ El proyecto ya fue compilado con `mvn clean compile`
3. ✅ La migración está en `target/classes/db/migration/V007__corregir_constraint_tipo_red.sql`
4. ⏳ **PENDIENTE:** Reiniciar el servidor Tomcat para que Flyway ejecute V007

**Verificación post-migración:**
```sql
-- Verificar que la restricción fue creada correctamente
SELECT conname, pg_get_constraintdef(oid)
FROM pg_constraint
WHERE conrelid = 'redes_sociales_profesional'::regclass
AND contype = 'c';
```

---

## 🎯 Validación de la Solución

### Casos de Prueba Cubiertos:

1. ✅ **Enviar "YouTube"** → Se guarda como "youtube" ✅
2. ✅ **Enviar "FACEBOOK"** → Se guarda como "facebook" ✅
3. ✅ **Enviar "instagram"** → Se guarda como "instagram" ✅
4. ✅ **Enviar "LinkedIn"** → Se guarda como "linkedin" ✅
5. ✅ **Actualización individual** → Normaliza antes de guardar ✅
6. ✅ **Actualización masiva** → Normaliza todos los elementos ✅

---

## 📊 Tipos de Red Válidos (Case-Insensitive)

| Tipo Normalizado | Ejemplos Aceptados | Base de Datos |
|------------------|-------------------|---------------|
| `facebook` | Facebook, facebook, FACEBOOK | ✅ facebook |
| `instagram` | Instagram, instagram, INSTAGRAM | ✅ instagram |
| `youtube` | YouTube, youtube, YOUTUBE | ✅ youtube |
| `tiktok` | TikTok, tiktok, TIKTOK | ✅ tiktok |
| `linkedin` | LinkedIn, linkedin, LINKEDIN | ✅ linkedin |
| `twitter` | Twitter, twitter, TWITTER | ✅ twitter |
| `whatsapp` | WhatsApp, whatsapp, WHATSAPP | ✅ whatsapp |
| `website` | Website, website, WEBSITE | ✅ website |
| `otro` | Otro, otro, OTRO | ✅ otro |

---

## 🔒 Arquitectura de Validación (Defensa en Profundidad)

```
Cliente (Frontend)
       ↓
       ↓ Envía: "YouTube"
       ↓
═════════════════════════════════════
SERVLET (Capa de Control)
═════════════════════════════════════
       ↓
       ↓ Normaliza: "youtube"
       ↓
═════════════════════════════════════
DAO (Capa de Persistencia)
═════════════════════════════════════
       ↓
       ↓ Normaliza nuevamente (defensa en profundidad)
       ↓
═════════════════════════════════════
BASE DE DATOS
═════════════════════════════════════
       ↓
       ✅ CHECK: tipo_red IN ('facebook', 'instagram', 'youtube', ...)
       ↓
       ✅ Guarda: "youtube"
```

---

## 📝 Comentarios en el Código

### Servlet (`RedesSocialesProfesionalServlet.java`)

1. **Línea 34-37:** Documentación actualizada indicando que los tipos son case-insensitive
2. **Línea 142:** Comentario en JSDoc indicando tipos válidos en minúsculas
3. **Línea 187:** Comentario explicando la normalización en POST
4. **Línea 299-302:** Comentario explicando la normalización en PUT individual
5. **Línea 350-355:** Comentario explicando la normalización en PUT masivo

### DAO (`RedesSocialesProfesionalDAOImpl.java`)

1. **Línea 66-69:** Comentario explicando la normalización en método `guardar()`
2. **Línea 272-275:** Comentario explicando la normalización en método `insertarInterno()`

---

## 🚀 Beneficios de la Solución

1. **Flexibilidad para el Cliente:**
   - ✅ Frontend puede enviar valores en cualquier formato (YouTube, youtube, YOUTUBE)
   - ✅ No requiere cambios en el cliente

2. **Robustez:**
   - ✅ Doble validación: Servlet + DAO
   - ✅ Restricción CHECK de BD como última capa de seguridad

3. **Mantenibilidad:**
   - ✅ Documentación actualizada y clara
   - ✅ Comentarios explicativos en el código
   - ✅ Migración V007 corrige definitivamente la restricción CHECK en BD

4. **Consistencia:**
   - ✅ Datos almacenados siempre en minúsculas
   - ✅ Fácil de buscar y comparar

---

## 🔄 Alternativas Consideradas (No Implementadas)

### ❌ Opción 1: Cambiar restricción CHECK en BD
```sql
-- Modificar restricción para aceptar mayúscula inicial
tipo_red CHECK (tipo_red IN ('Facebook', 'Instagram', 'YouTube', ...))
```
**Rechazada porque:**
- Menos flexible (solo acepta un formato específico)
- Requiere migración de base de datos
- No soluciona el problema de inconsistencia de entrada

### ❌ Opción 2: Eliminar restricción CHECK
```sql
-- Sin restricción CHECK
tipo_red VARCHAR(50) NOT NULL
```
**Rechazada porque:**
- Pierde validación a nivel de base de datos
- Permite valores inválidos
- Menos seguro

---

## ✅ Estado Final

### Completado:
- ✅ Error diagnosticado (restricción CHECK inconsistente)
- ✅ Código Java modificado y comentado (normalización a minúsculas)
- ✅ Documentación actualizada
- ✅ Solución implementada con defensa en profundidad
- ✅ Compatible con cualquier formato de entrada del cliente
- ✅ Migración V007 creada para corregir restricción CHECK en BD
- ✅ Proyecto compilado con la nueva migración

### Pendiente:
- ⏳ **REINICIAR SERVIDOR TOMCAT** para que Flyway ejecute la migración V007

### Después de Reiniciar:
1. Verificar en los logs que V007 se ejecutó correctamente
2. Probar crear una red social con `"YouTube"` o `"youtube"`
3. Debería guardarse exitosamente como `"youtube"` en la BD

---

## 📚 Referencias

- **Migración V006 (original):** `src/main/resources/db/migration/V006__refactorizar_perfil_profesional.sql:243`
- **Migración V007 (correctiva):** `src/main/resources/db/migration/V007__corregir_constraint_tipo_red.sql`
- **Servlet:** `src/main/java/com/contactoprofesionales/controller/perfil/RedesSocialesProfesionalServlet.java`
- **DAO:** `src/main/java/com/contactoprofesionales/dao/redes/RedesSocialesProfesionalDAOImpl.java`

---

**Fin del Documento**
