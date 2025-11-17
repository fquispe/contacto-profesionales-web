# 🔧 Solución: Error Constraint `uk_profesional_categoria`

**Fecha:** 2025-11-15
**Error:** `llave duplicada viola restricción de unicidad «uk_profesional_categoria»`
**Tipo:** Error de Base de Datos

---

## ❌ Problema

### Error Completo:
```
ERROR: llave duplicada viola restricción de unicidad «uk_profesional_categoria»
Detail: Ya existe la llave (profesional_id, categoria_id)=(1, 10).
```

### Causa Raíz:

La tabla `especialidades_profesional` tiene una constraint de unicidad:

```sql
CONSTRAINT uk_profesional_categoria UNIQUE (profesional_id, categoria_id)
```

Esta constraint **impide** que un profesional tenga **dos o más especialidades de la misma categoría**.

### Conflicto con Requerimiento:

El requerimiento funcional establece:
> "los usuarios deben registrar hasta 3 especialidades estos deben ser de la **misma o diferente categoria**"

**Ejemplo de caso válido bloqueado por la constraint:**

Un profesional de fontanería quiere ofrecer:
1. **Fontanería residencial** (categoría_id=10, Fontanería)
2. **Fontanería industrial** (categoría_id=10, Fontanería) ← **ERROR AQUÍ**
3. **Instalaciones de gas** (categoría_id=10, Fontanería) ← **ERROR AQUÍ**

---

## ✅ Solución

### Paso 1: Eliminar la Constraint

**Archivo creado:** `V004__eliminar_constraint_categoria_unica.sql`

```sql
ALTER TABLE especialidades_profesional
DROP CONSTRAINT IF EXISTS uk_profesional_categoria;
```

### Paso 2: Ejecutar la Migración

**Opción A - Desde línea de comandos PostgreSQL:**

```bash
psql -U postgres -d contacto_profesionales_db -f "src/main/resources/db/V004__eliminar_constraint_categoria_unica.sql"
```

**Opción B - Desde pgAdmin o cualquier cliente SQL:**

```sql
-- Copiar y pegar el contenido completo de V004__eliminar_constraint_categoria_unica.sql
-- y ejecutarlo directamente en la base de datos contacto_profesionales_db
```

**Opción C - SQL directo (rápido):**

Si solo quieres ejecutar el comando esencial:

```sql
ALTER TABLE especialidades_profesional DROP CONSTRAINT IF EXISTS uk_profesional_categoria;
```

### Paso 3: Verificar que se Eliminó

```sql
-- Verificar que la constraint ya no existe
SELECT constraint_name, constraint_type
FROM information_schema.table_constraints
WHERE table_name = 'especialidades_profesional';
```

**Resultado esperado:** NO debe aparecer `uk_profesional_categoria` en la lista.

---

## 📊 Lógica de Diferenciación de Especialidades

Después de eliminar la constraint, las especialidades se diferencian por:

### 1. Campo `servicio_profesional` (VARCHAR)
- Nombre específico del servicio que brinda
- Ejemplo: "Fontanería residencial", "Fontanería industrial"

### 2. Campo `orden` (INTEGER)
- Valores: 1, 2, 3
- Máximo 3 especialidades activas por profesional

### 3. Campo `activo` (BOOLEAN)
- Solo las especialidades con `activo = TRUE` cuentan para el límite de 3

### 4. Campo `categoria_id` (INTEGER)
- **AHORA PUEDE REPETIRSE** para el mismo profesional
- Permite múltiples especialidades de la misma categoría

---

## 🎯 Ejemplos de Casos Válidos

### Caso 1: Todas las especialidades de la misma categoría
```
Profesional ID: 1
├── Especialidad 1: orden=1, categoria_id=10 (Fontanería), servicio="Fontanería residencial"
├── Especialidad 2: orden=2, categoria_id=10 (Fontanería), servicio="Fontanería industrial"
└── Especialidad 3: orden=3, categoria_id=10 (Fontanería), servicio="Instalaciones de gas"
```

### Caso 2: Especialidades de categorías mixtas
```
Profesional ID: 2
├── Especialidad 1: orden=1, categoria_id=10 (Fontanería), servicio="Reparación de tuberías"
├── Especialidad 2: orden=2, categoria_id=15 (Electricidad), servicio="Instalaciones eléctricas"
└── Especialidad 3: orden=3, categoria_id=10 (Fontanería), servicio="Fontanería residencial"
```

### Caso 3: Solo dos especialidades (válido)
```
Profesional ID: 3
├── Especialidad 1: orden=1, categoria_id=20 (Carpintería), servicio="Muebles a medida"
└── Especialidad 2: orden=2, categoria_id=20 (Carpintería), servicio="Reparación de puertas"
```

---

## 🔒 Constraints que SÍ Permanecen

### 1. Límite de 3 especialidades activas
```sql
-- Validado en V003__mejoras_actualizacion_especialidades.sql
CHECK (activo = FALSE OR (activo = TRUE AND orden >= 1 AND orden <= 3))
```

### 2. Soft delete automático
```sql
-- Trigger que reordena especialidades cuando una es desactivada
CREATE TRIGGER reordenar_especialidades_trigger
```

### 3. Foreign keys
```sql
FOREIGN KEY (profesional_id) REFERENCES profesionales(id)
FOREIGN KEY (categoria_id) REFERENCES categorias_profesionales(id)
```

---

## 📝 Archivos Modificados

### Nuevos:
1. `V004__eliminar_constraint_categoria_unica.sql` - Script de migración
2. `SOLUCION_ERROR_CONSTRAINT_CATEGORIA.md` - Esta documentación

### Relacionados (creados previamente):
1. `V003__mejoras_actualizacion_especialidades.sql` - Soft delete y triggers
2. `ACTUALIZACION_DINAMICA_ESPECIALIDADES.md` - Documentación de actualización dinámica

---

## ✅ Checklist de Verificación

Después de ejecutar la migración, verificar:

- [ ] La constraint `uk_profesional_categoria` ya NO existe
- [ ] Puedo insertar múltiples especialidades con el mismo `categoria_id`
- [ ] El trigger de reordenamiento sigue funcionando
- [ ] El límite de 3 especialidades activas sigue vigente
- [ ] Las constraints de foreign key siguen activas

---

## 🧪 Prueba Manual

### 1. Intentar crear 2 especialidades de la misma categoría:

Desde el formulario web `servicios-profesional.html`:
- Agregar especialidad 1: Categoría "Fontanería", Servicio "Fontanería residencial"
- Agregar especialidad 2: Categoría "Fontanería", Servicio "Fontanería industrial"
- Guardar formulario

**Resultado esperado:** ✅ Guardado exitoso, ambas especialidades registradas

### 2. Verificar en base de datos:

```sql
SELECT
    id,
    profesional_id,
    categoria_id,
    servicio_profesional,
    orden,
    activo
FROM especialidades_profesional
WHERE profesional_id = 1
ORDER BY orden;
```

**Resultado esperado:**
```
id | profesional_id | categoria_id | servicio_profesional      | orden | activo
---+----------------+--------------+---------------------------+-------+--------
 1 |              1 |           10 | Fontanería residencial    |     1 | t
 2 |              1 |           10 | Fontanería industrial     |     2 | t
```

---

## 🎓 Conclusión

Esta migración **elimina una restricción innecesaria** que impedía el caso de uso legítimo de tener múltiples especialidades de la misma categoría.

La diferenciación entre especialidades ahora se hace correctamente a través del campo `servicio_profesional`, que es el nombre específico del servicio que brinda el profesional.

**Resultado:** Mayor flexibilidad sin comprometer la integridad de datos.

---

**Implementado por:** Claude Code
**Fecha:** 2025-11-15
**Status:** ✅ Listo para ejecutar
