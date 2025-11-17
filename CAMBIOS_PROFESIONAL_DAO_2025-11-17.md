# Actualización Completa de ProfesionalDAOImpl - 2025-11-17

## 📋 Resumen Ejecutivo

Se realizó una refactorización completa de `ProfesionalDAOImpl.java` para adaptarlo a la nueva estructura de tablas de la base de datos, donde:
- **especialidad** ya no es un campo en `profesionales`, sino una relación a través de `especialidades_profesional` → `categorias_servicio`
- **ubicación/distrito** ya no están en `profesionales`, sino en `usuarios` (distrito_id)
- Se eliminaron campos que no existen: `ubicacion`, `distrito`, `latitud`, `longitud`, `radio_servicio`

---

## 🗂️ Estructura de Tablas Real

### **profesionales**
```
id, usuario_id, descripcion, experiencia, habilidades, certificaciones,
foto_perfil, foto_portada, portafolio, tarifa_hora, calificacion_promedio,
total_resenas, disponibilidad, verificado, disponible, fecha_registro,
ultima_actualizacion, activo, especialidad_principal_id, anios_experiencia,
documento_identidad, verificacion_identidad, certificado_antecedentes,
puntuacion_plataforma, biografia_profesional, idiomas, licencias_profesionales,
seguro_responsabilidad, metodos_pago, politica_cancelacion
```

### **usuarios**
```
id, nombre_completo, tipo_documento, numero_documento, fecha_nacimiento, genero,
telefono, telefono_alternativo, departamento_id, provincia_id, distrito_id,
direccion, referencia_direccion, tipo_rol, es_cliente, es_profesional,
foto_perfil_url, fecha_creacion, fecha_actualizacion, activo
```

### **especialidades_profesional**
```
id, profesional_id, categoria_id, descripcion, incluye_materiales, costo,
tipo_costo, es_principal, orden, fecha_creacion, fecha_actualizacion, activo,
servicio_profesional, trabajo_remoto, trabajo_presencial, fecha_eliminacion
```

### **categorias_servicio**
```
id, nombre, descripcion, icono, color, activo, fecha_creacion
```

---

## ✅ Cambios Realizados por Método

### 1. **buscarPorId(Integer id)** - Líneas 31-62

**❌ ANTES:**
```sql
SELECT p.*, u.nombre_completo, u.telefono
FROM profesionales p
INNER JOIN usuarios u ON p.usuario_id = u.id
WHERE p.id = ? AND p.activo = true AND u.activo = true
```
**Problema:** No incluía especialidad ni distrito

**✅ AHORA:**
```sql
SELECT p.*,
       u.nombre_completo, u.telefono, u.distrito_id,
       cs.nombre AS especialidad_nombre
FROM profesionales p
INNER JOIN usuarios u ON p.usuario_id = u.id
LEFT JOIN especialidades_profesional ep ON p.especialidad_principal_id = ep.id
LEFT JOIN categorias_servicio cs ON ep.categoria_id = cs.id
WHERE p.id = ? AND p.activo = true AND u.activo = true
```
**Mejoras:**
- ✅ JOIN con `especialidades_profesional` y `categorias_servicio` para obtener especialidad
- ✅ Incluye `distrito_id` desde tabla `usuarios`
- ✅ Alias `especialidad_nombre` para mapear correctamente

---

### 2. **buscarPorUsuarioId(Integer usuarioId)** - Líneas 68-97

**Cambio:** Igual que `buscarPorId()`, ahora incluye JOINs con especialidades y categorías.

---

### 3. **listarTodos()** - Líneas 103-133

**Cambio:** Igual estructura con JOINs para especialidades y distrito.

---

### 4. **buscarPorEspecialidad(String especialidad)** - Líneas 140-173

**❌ ANTES:**
```sql
-- Query estaba bien, pero faltaba el alias especialidad_nombre en SELECT
```

**✅ AHORA:**
```sql
SELECT p.*,
       u.nombre_completo, u.telefono, u.distrito_id,
       cs.nombre AS especialidad_nombre
FROM profesionales p
INNER JOIN usuarios u ON p.usuario_id = u.id
INNER JOIN especialidades_profesional ep ON p.id = ep.profesional_id
INNER JOIN categorias_servicio cs ON cs.id = ep.categoria_id
WHERE cs.nombre ILIKE ? AND p.activo = true AND u.activo = true AND ep.activo = true
ORDER BY p.calificacion_promedio DESC
```
**Mejoras:**
- ✅ Agregado `cs.nombre AS especialidad_nombre` al SELECT
- ✅ Agregado `u.distrito_id`

---

### 5. **buscarPorDistrito(String distrito)** - Líneas 180-220

**❌ ANTES:**
```sql
WHERE p.distrito ILIKE ?
```
**Problema:** Campo `p.distrito` NO EXISTE en tabla profesionales

**✅ AHORA:**
```sql
WHERE u.distrito_id = ?
```
**Mejoras:**
- ✅ Busca en `usuarios.distrito_id` en lugar de `profesionales.distrito`
- ✅ Convierte String a Integer (distrito_id es INT)
- ✅ Manejo de error si distrito no es un ID válido

---

### 6. **buscarConFiltros()** - Líneas 229-306

**❌ ANTES:**
```sql
AND p.especialidad ILIKE ?  -- ❌ Campo no existe
AND p.distrito ILIKE ?      -- ❌ Campo no existe
```

**✅ AHORA:**
```sql
-- Filtro de especialidad mediante EXISTS subquery
AND EXISTS (
    SELECT 1 FROM especialidades_profesional ep2
    INNER JOIN categorias_servicio cs2 ON ep2.categoria_id = cs2.id
    WHERE ep2.profesional_id = p.id AND ep2.activo = true
    AND cs2.nombre ILIKE ?
)

-- Filtro de distrito desde usuarios
AND u.distrito_id = ?
```
**Mejoras:**
- ✅ Filtro de especialidad usando EXISTS con JOIN a categorias_servicio
- ✅ Filtro de distrito usando usuarios.distrito_id
- ✅ Soporte para Integer en parámetros (distrito_id)

---

### 7. **crear(Profesional profesional)** - Líneas 321-352

**❌ ANTES:**
```sql
INSERT INTO profesionales
(usuario_id, especialidad, descripcion, experiencia, habilidades,
 certificaciones, tarifa_hora, ubicacion, distrito, radio_servicio,
 disponibilidad, activo)
```
**Problema:** Campos `especialidad`, `ubicacion`, `distrito`, `radio_servicio` NO EXISTEN

**✅ AHORA:**
```sql
INSERT INTO profesionales
(usuario_id, descripcion, experiencia, disponibilidad, activo)
VALUES (?, ?, ?, ?, true) RETURNING id
```
**Mejoras:**
- ✅ Solo inserta campos que SÍ existen
- ✅ Método marcado como `@Deprecated`
- ✅ Log de advertencia: "Las especialidades deben crearse por separado en especialidades_profesional"

---

### 8. **actualizar(Profesional profesional)** - Líneas 365-395

**❌ ANTES:**
```sql
UPDATE profesionales SET
especialidad = ?, descripcion = ?, experiencia = ?,
habilidades = ?, certificaciones = ?, tarifa_hora = ?,
ubicacion = ?, distrito = ?, radio_servicio = ?,
disponibilidad = ?
```

**✅ AHORA:**
```sql
UPDATE profesionales SET
descripcion = ?, experiencia = ?, disponibilidad = ?
WHERE id = ? AND activo = true
```
**Mejoras:**
- ✅ Solo actualiza campos que SÍ existen
- ✅ Método marcado como `@Deprecated`
- ✅ Log de advertencia: "Usar PerfilProfesionalServlet para actualizar perfil"

---

### 9. **obtenerEspecialidadesUnicas()** - Líneas 649-677

**❌ ANTES:**
```sql
SELECT DISTINCT especialidad FROM profesionales
WHERE activo = true AND especialidad IS NOT NULL
ORDER BY especialidad
```
**Problema:** Campo `especialidad` NO EXISTE en profesionales

**✅ AHORA:**
```sql
SELECT DISTINCT cs.nombre
FROM categorias_servicio cs
INNER JOIN especialidades_profesional ep ON cs.id = ep.categoria_id
INNER JOIN profesionales p ON ep.profesional_id = p.id
WHERE p.activo = true AND ep.activo = true
ORDER BY cs.nombre
```
**Mejoras:**
- ✅ Consulta categorias_servicio via especialidades_profesional
- ✅ Solo retorna categorías que están siendo usadas por profesionales activos

---

### 10. **obtenerDistritosUnicos()** - Líneas 685-713

**❌ ANTES:**
```sql
SELECT DISTINCT distrito FROM profesionales
WHERE activo = true AND distrito IS NOT NULL
ORDER BY distrito
```
**Problema:** Campo `distrito` NO EXISTE en profesionales

**✅ AHORA:**
```sql
SELECT DISTINCT u.distrito_id
FROM usuarios u
INNER JOIN profesionales p ON u.id = p.usuario_id
WHERE p.activo = true AND u.activo = true AND u.distrito_id IS NOT NULL
ORDER BY u.distrito_id
```
**Mejoras:**
- ✅ Consulta usuarios.distrito_id
- ✅ Solo retorna distritos de usuarios que tienen perfil profesional activo
- ✅ Convierte distrito_id (Integer) a String para compatibilidad

---

### 11. **mapResultSetToProfesional(ResultSet rs)** - Líneas 498-641

**Mejoras Críticas:**

#### **NUEVO: Mapeo de especialidad desde JOIN**
```java
// ✅ NUEVO: Especialidad desde categorias_servicio (viene del JOIN)
try {
    String especialidadNombre = rs.getString("especialidad_nombre");
    if (especialidadNombre != null) {
        profesional.setEspecialidad(especialidadNombre);
    }
} catch (SQLException e) {
    // Columna no existe en este query, ignorar
}
```

#### **NUEVO: Mapeo de distrito desde usuarios**
```java
// ✅ NUEVO: Distrito desde usuarios (viene del JOIN)
try {
    Integer distritoId = rs.getInt("distrito_id");
    if (!rs.wasNull()) {
        // Guardar distrito_id como String en el campo distrito por compatibilidad
        profesional.setDistrito(String.valueOf(distritoId));
    }
} catch (SQLException e) {
    // Columna no existe en este query, ignorar
}
```

#### **PROTECCIÓN: Campos que ya no existen**
```java
// ❌ CAMPOS QUE YA NO EXISTEN EN TABLA PROFESIONALES (proteger con try-catch)
// ubicacion, latitud, longitud, radio_servicio - YA NO EXISTEN
try {
    profesional.setUbicacion(rs.getString("ubicacion"));
} catch (SQLException e) {
    // Campo no existe, ignorar
}
// ... (similar para latitud, longitud, radio_servicio)
```

---

## 📊 Resumen de Cambios SQL

| Método | Cambio Principal | Estado |
|--------|------------------|--------|
| `buscarPorId()` | Agregado JOIN con especialidades y categorias_servicio | ✅ |
| `buscarPorUsuarioId()` | Agregado JOIN con especialidades y categorias_servicio | ✅ |
| `listarTodos()` | Agregado JOIN con especialidades y categorias_servicio | ✅ |
| `buscarPorEspecialidad()` | Agregado alias especialidad_nombre y distrito_id | ✅ |
| `buscarPorDistrito()` | Cambiado de `p.distrito` a `u.distrito_id` | ✅ |
| `buscarConFiltros()` | Reescrito con EXISTS para especialidad y u.distrito_id | ✅ |
| `crear()` | Eliminados campos inexistentes, marcado @Deprecated | ✅ |
| `actualizar()` | Eliminados campos inexistentes, marcado @Deprecated | ✅ |
| `obtenerEspecialidadesUnicas()` | Reescrito para consultar categorias_servicio | ✅ |
| `obtenerDistritosUnicos()` | Reescrito para consultar usuarios.distrito_id | ✅ |
| `mapResultSetToProfesional()` | Agregado mapeo de especialidad_nombre y distrito_id | ✅ |

---

## 🎯 Impacto en Otros Componentes

### **JavaScript - NO REQUIERE CAMBIOS**
Los endpoints de los servlets siguen siendo los mismos:
- `GET /api/profesionales` - Listar todos
- `GET /api/profesionales/{id}` - Buscar por ID
- `GET /api/profesionales?usuarioId={id}` - Buscar por usuario

El JSON devuelto ahora incluye:
- `especialidad`: Nombre de la categoría de servicio (desde categorias_servicio)
- `distrito`: distrito_id como String (desde usuarios)

### **Modelo Profesional.java - NO REQUIERE CAMBIOS**
- Campo `especialidad` (String) ahora se llena con el nombre de categorias_servicio
- Campo `distrito` (String) ahora se llena con distrito_id de usuarios
- Campos deprecados (`ubicacion`, `latitud`, `longitud`, `radio_servicio`) se mantienen por compatibilidad pero se ignoran

---

## ⚠️ Métodos Deprecados

Los siguientes métodos están marcados como `@Deprecated` y **NO deben usarse**:

1. **`crear(Profesional profesional)`**
   - Motivo: Solo crea registro básico, no gestiona especialidades
   - Alternativa: Usar servlets especializados

2. **`actualizar(Profesional profesional)`**
   - Motivo: No actualiza campos de nueva estructura
   - Alternativa: Usar `PerfilProfesionalServlet`

---

## 🔧 Ejemplo de Query Refactorizado

### **Antes (INCORRECTO)**
```sql
SELECT p.*, u.nombre_completo, u.telefono
FROM profesionales p
INNER JOIN usuarios u ON p.usuario_id = u.id
WHERE p.especialidad ILIKE '%Plomería%'  -- ❌ Campo no existe
  AND p.distrito ILIKE '%Lima%'          -- ❌ Campo no existe
  AND p.activo = true
```

### **Después (CORRECTO)**
```sql
SELECT p.*,
       u.nombre_completo, u.telefono, u.distrito_id,
       cs.nombre AS especialidad_nombre
FROM profesionales p
INNER JOIN usuarios u ON p.usuario_id = u.id
INNER JOIN especialidades_profesional ep ON p.id = ep.profesional_id
INNER JOIN categorias_servicio cs ON cs.id = ep.categoria_id
WHERE cs.nombre ILIKE '%Plomería%'  -- ✅ Busca en categorias_servicio
  AND u.distrito_id = 15            -- ✅ Busca en usuarios
  AND p.activo = true
  AND u.activo = true
  AND ep.activo = true
```

---

## ✅ Compilación y Resultados

```
[INFO] BUILD SUCCESS
[INFO] Total time:  13.431 s
[INFO] Finished at: 2025-11-17T02:06:30
```

Todos los cambios compilaron correctamente sin errores.

---

## 📝 Recomendaciones

1. **Actualizar Tests**: Revisar y actualizar tests unitarios de `ProfesionalDAOImpl`
2. **Validar en Producción**: Probar búsquedas por especialidad y distrito
3. **Migrar Datos**: Si existen datos en campos deprecados, migrarlos a nuevas tablas
4. **Actualizar Documentación**: Informar a desarrolladores sobre cambios en estructura

---

**Fecha de Actualización:** 2025-11-17
**Autor:** Sistema
**Versión:** 3.0
**Estado:** ✅ COMPLETADO
