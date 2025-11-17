# 🔍 Diagnóstico: Por qué no se llenan las categorías en el selector

## Fecha: 2025-11-17

---

## 📋 Problema Reportado

Al abrir el modal de proyectos, el selector de categorías muestra: **"No tienes especialidades registradas"**

A pesar de que ejecutando este SQL se obtienen 2 registros:

```sql
SELECT p.id as id_profesional, cs.nombre as categoria_especialidad_profesional,
       u.nombre_completo, u.telefono
FROM profesionales p
INNER JOIN usuarios u ON p.usuario_id = u.id
INNER JOIN especialidades_profesional ep ON p.id = ep.profesional_id
INNER JOIN categorias_servicio cs ON cs.id = ep.categoria_id
WHERE p.id = 1 AND p.activo = true AND u.activo = true AND ep.activo = true
ORDER BY p.calificacion_promedio DESC;
```

**Resultado:**
| id_profesional | categoria_especialidad_profesional | nombre_completo | telefono |
|---|---|---|---|
| 1 | Plomería | ARANZA QUISPE HUAMAN | 987654321 |
| 1 | Electricidad | ARANZA QUISPE HUAMAN | 987654321 |

---

## 🔬 Análisis del Flujo Completo

### **1. Flujo Frontend → Backend**

```
profesional.html
    ↓
profesional.js::cargarPerfilCompleto()
    ↓
profesional.js::cargarEspecialidades(profesionalId)
    ↓
profesional-api.js::obtenerEspecialidades(profesionalId)
    ↓
HTTP GET /api/profesionales/1/especialidades
    ↓
EspecialidadServlet.doGet()
    ↓
EspecialidadService.listarPorProfesional(1)
    ↓
EspecialidadProfesionalDAO.listarPorProfesional(1)
    ↓
SQL Query
    ↓
Response {success: true, data: [...]}
    ↓
profesional.js::poblarSelectorCategorias()
```

---

## 🔎 Análisis Técnico de Cada Componente

### **A. Frontend: URL Construida**

**Archivo:** `profesional-api.js` (línea 740)

```javascript
const response = await fetch(
    `${this.baseURL.replace('/perfil', '')}/../profesionales/${profesionalId}/especialidades`,
    {...}
);
```

**Construcción:**
- `this.baseURL` = `/ContactoProfesionalesWeb/api/profesional`
- `.replace('/perfil', '')` = `/ContactoProfesionalesWeb/api/profesional` (no cambia)
- `/../profesionales/1/especialidades`
- **URL Final:** `/ContactoProfesionalesWeb/api/profesionales/1/especialidades` ✅

---

### **B. Backend: Servlet**

**Archivo:** `EspecialidadServlet.java`

**URL Pattern:** `@WebServlet(urlPatterns = {"/api/profesionales/*/especialidades", ...})`

**Método:** `doGet()` (línea 83)

✅ **El servlet está correctamente mapeado y debería responder a esta URL**

---

### **C. Backend: Service Layer**

**Archivo:** `EspecialidadServiceImpl.java` (línea 187-218)

```java
public List<EspecialidadDTO> listarPorProfesional(Integer profesionalId) {
    List<EspecialidadProfesional> especialidades =
        especialidadDAO.listarPorProfesional(profesionalId);

    List<EspecialidadDTO> especialidadesDTO = new ArrayList<>();
    for (EspecialidadProfesional especialidad : especialidades) {
        especialidadesDTO.add(convertirModeloADTO(especialidad));
    }

    return especialidadesDTO;
}
```

✅ **El servicio convierte correctamente las especialidades a DTO**

---

### **D. Backend: DAO Layer**

**Archivo:** `EspecialidadProfesionalDAOImpl.java` (línea 177-204)

**SQL Query (línea 31-40):**
```sql
SELECT e.id, e.profesional_id, e.categoria_id, e.descripcion, e.incluye_materiales,
       e.costo, e.tipo_costo, e.es_principal, e.orden, e.fecha_creacion,
       e.fecha_actualizacion, e.activo,
       c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion,
       c.icono AS categoria_icono, c.color AS categoria_color
FROM especialidades_profesional e
INNER JOIN categorias_servicio c ON e.categoria_id = c.id
WHERE e.profesional_id = ? AND e.activo = true
ORDER BY e.orden ASC
```

✅ **El query hace correctamente el JOIN con categorias_servicio**

✅ **El mapeo extrae correctamente** `categoria_nombre` (línea 430)

---

### **E. Estructura del DTO**

**Archivo:** `EspecialidadDTO.java`

```java
private Integer categoriaId;       // ✅ Existe
private String categoriaNombre;    // ✅ Existe
private String categoriaDescripcion;
private String categoriaIcono;
private String categoriaColor;
```

✅ **El DTO tiene los campos necesarios**

---

### **F. Respuesta del Servlet**

**Archivo:** `EspecialidadServlet.java` (línea 102)

```java
sendSuccessResponse(response, HttpServletResponse.SC_OK, especialidades);
```

**Método sendSuccessResponse (línea 382-390):**
```java
Map<String, Object> responseBody = new HashMap<>();
responseBody.put("success", true);
responseBody.put("data", data);  // ← especialidades aquí

String jsonResponse = gson.toJson(responseBody);
```

**Estructura de Respuesta:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "profesionalId": 1,
      "categoriaId": 1,
      "categoriaNombre": "Plomería",  // ← Campo clave
      "descripcion": "...",
      "costo": 50.0,
      "tipoCosto": "hora",
      ...
    },
    {
      "id": 2,
      "profesionalId": 1,
      "categoriaId": 2,
      "categoriaNombre": "Electricidad",  // ← Campo clave
      ...
    }
  ]
}
```

✅ **El servlet devuelve correctamente la estructura**

---

### **G. Frontend: Procesamiento de la Respuesta**

**Archivo:** `profesional-api.js` (línea 758-761)

```javascript
// ✅ El servlet devuelve {success: true, data: [...]}
// Extraer el array de data
if (result && result.success && Array.isArray(result.data)) {
    return result.data;  // ← Devuelve el array de especialidades
}
```

✅ **Extrae correctamente el array del objeto respuesta**

---

### **H. Frontend: Población del Selector**

**Archivo:** `profesional.js` (línea 519-527)

```javascript
AppState.especialidades.forEach(esp => {
    // ✅ Validación: Verificar que esp sea un objeto con las propiedades necesarias
    if (esp && typeof esp === 'object' && esp.categoriaId && esp.categoriaNombre) {
        if (!categoriasUnicas.has(esp.categoriaId)) {
            categoriasUnicas.set(esp.categoriaId, esp.categoriaNombre);
        }
    }
});
```

✅ **El código busca correctamente `categoriaId` y `categoriaNombre`**

---

## ❓ Entonces, ¿Cuál es el Problema?

### **Hipótesis 1: No hay datos en la tabla para profesional_id=1**

El query que proporcionaste:
```sql
WHERE p.id = 1 AND ...
```

Muestra datos porque hace JOIN desde `profesionales`, pero **el endpoint del backend consulta directamente:**

```sql
WHERE e.profesional_id = ? AND e.activo = true
```

**Verifica:**
```sql
SELECT * FROM especialidades_profesional
WHERE profesional_id = 1 AND activo = true;
```

### **Hipótesis 2: El profesional_id en el frontend es incorrecto**

El método `cargarEspecialidades(profesionalId)` recibe el ID desde:

```javascript
await cargarEspecialidades(perfil.id);
```

Donde `perfil.id` es el ID del **registro en tabla profesionales**, NO el `usuario_id`.

**Verifica en consola del navegador:**
```javascript
console.log('profesionalId:', profesionalId);
```

---

## ✅ Pasos para Diagnosticar

### **Paso 1: Verificar datos en la tabla**

```sql
-- Verificar si hay especialidades para profesional_id=1
SELECT e.*, cs.nombre
FROM especialidades_profesional e
INNER JOIN categorias_servicio cs ON e.categoria_id = cs.id
WHERE e.profesional_id = 1 AND e.activo = true;
```

**Resultado Esperado:** Debe devolver 2 filas (Plomería y Electricidad)

Si NO devuelve nada → **Los datos NO están en especialidades_profesional**

---

### **Paso 2: Verificar qué profesional_id se está enviando**

**En navegador:**
1. Abrir `profesional.html?usuarioId=1`
2. Abrir consola del navegador (F12)
3. Buscar el log: `Cargando especialidades del profesional: X`
4. Anotar el número `X`

**Luego ejecutar en BD:**
```sql
SELECT * FROM especialidades_profesional
WHERE profesional_id = X AND activo = true;
```

---

### **Paso 3: Verificar la respuesta del endpoint**

**En navegador:**
1. Abrir Network tab (F12 → Network)
2. Recargar la página
3. Buscar request: `/api/profesionales/1/especialidades`
4. Ver la respuesta JSON

**Respuesta esperada:**
```json
{
  "success": true,
  "data": [
    {
      "id": ...,
      "categoriaId": 1,
      "categoriaNombre": "Plomería",
      ...
    }
  ]
}
```

Si `data` está vacío `[]` → **No hay especialidades en BD para ese profesional_id**

---

### **Paso 4: Verificar logs del servidor**

Buscar en logs de Tomcat:
```
Se encontraron {} especialidades para el profesional ID {}
```

Debería decir: `Se encontraron 2 especialidades para el profesional ID 1`

Si dice: `Se encontraron 0 especialidades...` → **Los datos NO existen**

---

## 🔧 Posibles Soluciones

### **Solución 1: Insertar datos en especialidades_profesional**

Si los datos NO existen en la tabla, insertarlos manualmente:

```sql
INSERT INTO especialidades_profesional
(profesional_id, categoria_id, descripcion, incluye_materiales, costo, tipo_costo, es_principal, orden, activo)
VALUES
(1, 1, 'Instalación y reparación de sistemas de plomería', false, 50.0, 'hora', true, 1, true),
(1, 2, 'Instalaciones eléctricas residenciales y comerciales', false, 60.0, 'hora', false, 2, true);
```

**Notas:**
- `profesional_id = 1` (ID del registro en tabla profesionales)
- `categoria_id = 1` (Plomería) y `categoria_id = 2` (Electricidad)
- Verificar que las categorías existan en `categorias_servicio`

---

### **Solución 2: Verificar ID de categorías**

Antes de insertar, verificar que las categorías existen:

```sql
SELECT id, nombre FROM categorias_servicio WHERE activo = true ORDER BY id;
```

Si `Plomería` tiene `id=5` en lugar de `id=1`, usar ese ID correcto.

---

### **Solución 3: Corregir profesional_id**

Si el usuario está entrando con `usuarioId=1` pero su registro de profesional tiene otro ID:

```sql
SELECT p.id AS profesional_id, p.usuario_id, u.nombre_completo
FROM profesionales p
INNER JOIN usuarios u ON p.usuario_id = u.id
WHERE u.id = 1;  -- ← usuarioId del usuario
```

Anotar el `profesional_id` correcto y verificar que las especialidades estén asociadas a ESE ID.

---

## 📊 Checklist de Verificación

- [ ] ¿Existen datos en `especialidades_profesional` para `profesional_id=1`?
- [ ] ¿El `profesional_id` que se envía desde el frontend es correcto?
- [ ] ¿El endpoint `/api/profesionales/1/especialidades` responde con `data` no vacío?
- [ ] ¿Los logs del servidor muestran "Se encontraron X especialidades"?
- [ ] ¿La consola del navegador muestra `AppState.especialidades.length > 0`?
- [ ] ¿Las categorías en `categorias_servicio` tienen `activo=true`?

---

## 🎯 Conclusión

**El código está CORRECTO en todos los niveles:**
- ✅ Frontend construye URL correcta
- ✅ Backend hace JOIN correcto con categorias_servicio
- ✅ DTO tiene campos necesarios
- ✅ Respuesta JSON tiene estructura correcta
- ✅ JavaScript extrae datos correctamente

**El problema MÁS PROBABLE es:**
- ❌ **NO HAY DATOS** en la tabla `especialidades_profesional` para el `profesional_id` que se está consultando
- ❌ O el `profesional_id` que se envía desde el frontend es incorrecto

**Ejecuta los pasos de diagnóstico** para identificar cuál de estas dos causas es la correcta.

---

**Fecha:** 2025-11-17
**Autor:** Sistema
**Estado:** Diagnóstico completo - Requiere verificación de datos
