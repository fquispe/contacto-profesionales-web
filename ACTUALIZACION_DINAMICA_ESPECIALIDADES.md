# 📋 Actualización Dinámica de Especialidades - Documentación Técnica
**Fecha:** 2025-11-15
**Versión:** 2.0
**Tipo de Cambio:** Feature - Soft Delete y Actualización Inteligente

---

## 🎯 Objetivo

Implementar un sistema de actualización dinámica que permita a los usuarios:
- Agregar nuevas especialidades (hasta 3 total)
- Modificar especialidades existentes
- Eliminar especialidades (eliminación lógica, no física)
- Mezclar especialidades de la misma o diferente categoría

## 📊 Modificaciones en Base de Datos

### Script de Migración
**Archivo:** `src/main/resources/db/V003__mejoras_actualizacion_especialidades.sql`

#### Índices Creados:
```sql
-- Optimización para consultas de especialidades activas
CREATE INDEX idx_especialidades_profesional_activo
ON especialidades_profesional(profesional_id, activo)
WHERE activo = TRUE;

-- Búsquedas por categoría
CREATE INDEX idx_especialidades_profesional_categoria
ON especialidades_profesional(categoria_id, activo);
```

#### Constraints Modificados:
```sql
-- Constraint de orden solo para registros activos
ALTER TABLE especialidades_profesional
ADD CONSTRAINT especialidades_profesional_orden_activo_check
CHECK (
    (activo = FALSE) OR
    (activo = TRUE AND orden >= 1 AND orden <= 3)
);
```

#### Función y Trigger Creados:
```sql
-- Función para reordenar especialidades activas automáticamente
CREATE OR REPLACE FUNCTION reordenar_especialidades_activas()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'UPDATE' AND OLD.activo = TRUE AND NEW.activo = FALSE) THEN
        -- Reordena especialidades restantes con orden consecutivo 1,2,3
        WITH especialidades_activas AS (
            SELECT id, ROW_NUMBER() OVER (ORDER BY orden) as nuevo_orden
            FROM especialidades_profesional
            WHERE profesional_id = NEW.profesional_id
            AND activo = TRUE
            AND id != NEW.id
        )
        UPDATE especialidades_profesional e
        SET orden = ea.nuevo_orden, fecha_actualizacion = NOW()
        FROM especialidades_activas ea
        WHERE e.id = ea.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger que ejecuta el reordenamiento
CREATE TRIGGER trigger_reordenar_especialidades
AFTER UPDATE ON especialidades_profesional
FOR EACH ROW
WHEN (OLD.activo IS DISTINCT FROM NEW.activo)
EXECUTE FUNCTION reordenar_especialidades_activas();
```

### Características de la Base de Datos:
✅ **Soft Delete Implementado**: Campo `activo` (TRUE = activa, FALSE = eliminada lógicamente)
✅ **Sin Restricción de Categoría**: Permite múltiples especialidades de la misma categoría
✅ **Máximo 3 Activas**: Controlado a nivel de aplicación
✅ **Reordenamiento Automático**: Al eliminar, se reajustan los órdenes
✅ **Auditoría**: Se mantienen especialidades eliminadas para historial

---

## 🔧 Modificaciones en Código Backend (Java)

### 1. ServiciosProfesionalDAOImpl.java

#### Método Principal Actualizado:
```java
// ✅ ACTUALIZADO: Lógica de actualización dinámica con soft delete (actualizado: 2025-11-15)
@Override
public boolean actualizarServiciosProfesional(Integer profesionalId,
                                             List<EspecialidadProfesional> especialidades,
                                             AreaServicio areaServicio,
                                             DisponibilidadHoraria disponibilidad) throws Exception
```

**Cambio Principal:**
Antes: Eliminar todo → Insertar todo
Ahora: Actualizar inteligentemente (UPDATE existentes, INSERT nuevas, UPDATE activo=FALSE para eliminadas)

#### Nuevos Métodos Implementados:

##### 1. `actualizarEspecialidadesInterno()`
**Líneas:** 333-385
**Función:** Lógica central de actualización dinámica
**Algoritmo:**
```
1. Obtener IDs de especialidades enviadas en la solicitud
2. Marcar como inactivas (soft delete) las que YA NO vienen en la lista
3. Para cada especialidad enviada:
   - Si tiene ID: UPDATE (actualizar existente)
   - Si no tiene ID: INSERT (nueva especialidad)
```

##### 2. `actualizarEspecialidadExistente()`
**Líneas:** 387-421
**Función:** UPDATE de especialidad existente
**Campos actualizados:** categoría, servicio, descripción, materiales, costo, tipo_costo, principal, orden, trabajo_remoto, trabajo_presencial
**Importante:** Reactiva especialidades (activo = TRUE) si estaban inactivas

##### 3. `insertarNuevaEspecialidad()`
**Líneas:** 423-453
**Función:** INSERT de nueva especialidad
**Retorna:** ID generado automáticamente por la BD

##### 4. `desactivarTodasEspecialidadesInterno()`
**Líneas:** 455-466
**Función:** Soft delete masivo cuando no se envían especialidades

##### 5. `eliminarEspecialidadesPorProfesionalInterno()` - MODIFICADO
**Líneas:** 321-331
**Antes:** DELETE físico
**Ahora:** UPDATE activo = FALSE (soft delete)

### 2. Servlet (ServiciosProfesionalServlet.java)

**Sin cambios necesarios** - Ya está preparado para recibir IDs en el JSON

---

## 💻 Modificaciones en Frontend (JavaScript)

### 1. Estado de Especialidades

**Archivo:** `servicios-profesional.js`

#### Objeto Especialidad Actualizado:
```javascript
const especialidad = {
    id: datosExistentes?.id || null, // ✅ NUEVO: Guardar ID si es actualización
    orden: index + 1,
    categoriaId: ...,
    categoriaNombre: ...,
    servicioProfesional: ...,
    descripcion: ...,
    incluyeMateriales: ...,
    costo: ...,
    tipoCosto: ...,
    esPrincipal: ...,
    trabajoRemoto: ..., // ✅ Añadido 2025-11-14
    trabajoPresencial: ... // ✅ Añadido 2025-11-14
};
```

### 2. Función de Envío Actualizada

**Líneas:** 912-936

```javascript
// ✅ ACTUALIZADO: Incluir IDs de especialidades para actualización dinámica
const datosServicio = {
    usuarioId: appState.usuarioId,
    especialidades: appState.especialidades.map(esp => {
        const especialidadDTO = {
            categoriaId: esp.categoriaId,
            servicioProfesional: esp.servicioProfesional,
            // ... otros campos
        };

        // ✅ IMPORTANTE: Incluir ID solo si existe (para actualización)
        // Esto permite al backend saber cuáles especialidades actualizar vs. insertar
        if (esp.id && esp.id > 0) {
            especialidadDTO.id = esp.id;
        }

        return especialidadDTO;
    }),
    // ...
};
```

---

## 🔄 Flujo de Actualización

### Caso 1: Actualizar Especialidad Existente
```
Usuario edita especialidad #1 → Frontend envía con id=5 → Backend UPDATE WHERE id=5
```

### Caso 2: Agregar Nueva Especialidad
```
Usuario agrega especialidad #3 → Frontend envía sin id → Backend INSERT nueva fila
```

### Caso 3: Eliminar Especialidad
```
Usuario elimina especialidad #2 → Frontend NO la envía → Backend UPDATE activo=FALSE WHERE id no está en lista
```

### Caso 4: Reordenar Especialidades
```
Usuario mueve especialidad #3 a posición #1 → Frontend envía orden=1 → Backend UPDATE orden=1
→ Trigger reordena automáticamente las demás
```

---

## 📋 Uso del Método PUT

### ¿Cuándo se usa PUT?

El método **PUT** se utiliza para **actualizar** servicios existentes. Esto se determina en el frontend:

```javascript
// JavaScript - servicios-profesional.js (línea 920)
const method = appState.modoEdicion ? 'PUT' : 'POST';
```

**`appState.modoEdicion`** se establece en `true` cuando:
- Se cargan datos existentes al abrir el formulario (línea 223)
- Significa que el profesional YA tiene servicios configurados

### Flujo Completo:

1. **Primera Vez (POST):**
   ```
   Usuario nuevo → No hay datos → modoEdicion=false → POST /api/servicios-profesional
   → Backend INSERT en todas las tablas
   ```

2. **Actualización (PUT):**
   ```
   Usuario existente → Cargar datos → modoEdicion=true → PUT /api/servicios-profesional
   → Backend:
     - Especialidades: Actualización inteligente (UPDATE/INSERT/soft DELETE)
     - Área Servicio: DELETE + INSERT (reemplazo total)
     - Disponibilidad: DELETE + INSERT (reemplazo total)
   ```

**No se requiere método POST adicional para añadir especialidades** - todo se maneja con PUT cuando estás en modo edición.

---

## ✅ Ventajas del Sistema Implementado

1. **Preservación de IDs**: No se pierden referencias a especialidades
2. **Auditoría Completa**: Historial de especialidades eliminadas
3. **Sin Pérdida de Datos**: Eliminación lógica permite recuperación
4. **Rendimiento Optimizado**: Solo UPDATE los registros necesarios
5. **Flexibilidad Total**: Misma o diferente categoría, sin restricciones
6. **Reordenamiento Automático**: Trigger de BD mantiene orden consecutivo
7. **UX Mejorada**: Usuario puede agregar/quitar dinámicamente

---

## 🧪 Casos de Uso Soportados

### ✅ Caso 1: Mismo Profesional, Misma Categoría
```
Especialidad 1: Electricista - Instalación residencial (id=1)
Especialidad 2: Electricista - Mantenimiento industrial (id=2)
Especialidad 3: Electricista - Sistemas solares (nueva, sin id)
```

### ✅ Caso 2: Mismo Profesional, Diferentes Categorías
```
Especialidad 1: Electricista - Instalación (id=1)
Especialidad 2: Plomero - Reparaciones (nueva, sin id)
Especialidad 3: Carpintero - Muebles (id=5)
```

### ✅ Caso 3: Eliminar y Agregar en Mismo Request
```
Estado Anterior:
- Electricista (id=1)
- Plomero (id=2)
- Carpintero (id=3)

Request Nuevo:
- Electricista (id=1) → UPDATE
- Pintor (sin id) → INSERT
- Albañil (sin id) → INSERT

Resultado:
- id=1: UPDATE (sigue activa)
- id=2: UPDATE activo=FALSE (soft delete)
- id=3: UPDATE activo=FALSE (soft delete)
- Nueva fila: INSERT Pintor
- Nueva fila: INSERT Albañil
```

---

## 🔒 Validaciones Implementadas

### Backend (Java):
- ✅ Máximo 3 especialidades activas por profesional
- ✅ Al menos una especialidad debe tener al menos una modalidad de trabajo
- ✅ Costo > 0
- ✅ Orden entre 1-3 (solo para activas)
- ✅ Tipo de costo: 'hora', 'dia', o 'mes'

### Frontend (JavaScript):
- ✅ Máximo 3 especialidades en el formulario
- ✅ Al menos una especialidad principal
- ✅ Campos obligatorios: categoría, servicio, costo, tipo_costo
- ✅ Al menos una modalidad de trabajo (remoto o presencial)

### Base de Datos:
- ✅ Constraint de orden solo para registros activos
- ✅ Trigger de reordenamiento automático
- ✅ Índices para optimizar consultas

---

## 📌 Notas Importantes

### Para Desarrolladores:

1. **IDs en Frontend**: Siempre preservar el `id` al cargar datos existentes
2. **Orden en Frontend**: Se asigna automáticamente basado en posición en array
3. **Eliminación**: NO hacer DELETE manual, dejar que el backend lo maneje
4. **Validación**: Verificar `isValid()` antes de enviar

### Para DBAs:

1. **Migración Requerida**: Ejecutar `V003__mejoras_actualizacion_especialidades.sql`
2. **Índices**: Mejoran rendimiento en consultas con filtro `activo = TRUE`
3. **Trigger**: Mantiene integridad de orden automáticamente
4. **Auditoría**: Especialidades con `activo=FALSE` se mantienen indefinidamente

### Para Testers:

1. Verificar que al eliminar una especialidad, esta se marca como inactiva
2. Verificar que se pueden tener 3 especialidades de la misma categoría
3. Verificar que el orden se reajusta automáticamente al eliminar
4. Verificar que al actualizar se preservan los IDs originales

---

## 📅 Histórico de Versiones

| Versión | Fecha | Cambios |
|---------|-------|---------|
| 1.0 | 2025-11-14 | Implementación inicial con DELETE físico |
| 2.0 | 2025-11-15 | **Actualización dinámica con soft delete** |

---

## 🎓 Conclusión

El sistema ahora permite una gestión completamente dinámica de especialidades:
- **Agregar**: Hasta 3 especialidades, misma o diferente categoría
- **Actualizar**: Modificar especialidades existentes sin perder IDs
- **Eliminar**: Soft delete que preserva historial
- **Optimizado**: Solo actualiza lo necesario, mejor rendimiento

**Método Utilizado:** PUT para todas las actualizaciones (no se requiere POST adicional)
