# Pruebas - Dashboard del Profesional - 2025-12-03

## 📋 Resumen de Cambios

Se implementó completamente el sistema de **Gestión de Solicitudes y Dashboard del Profesional** con las siguientes mejoras:

### Cambios Realizados en Dashboard.js

**ANTES:**
```javascript
const response = await fetch(`/api/solicitudes?tipo=profesional`, {
```

**DESPUÉS (✅ Mejorado):**
```javascript
const response = await fetch(`/api/solicitudes?tipo=profesional&usuarioId=${profesionalId}`, {
```

**Mejoras aplicadas:**
1. ✅ Agregado parámetro `usuarioId` explícito en todas las peticiones
2. ✅ Validación de `profesionalId` antes de hacer fetch
3. ✅ Logging detallado en consola para debugging
4. ✅ Manejo de errores mejorado con mensajes específicos

---

## 🧪 Cómo Probar el Dashboard de Profesional

### Paso 1: Preparar Datos de Prueba en Base de Datos

Asegúrate de tener:
- ✅ Un usuario con rol "PROFESIONAL" o "AMBOS"
- ✅ Al menos una solicitud de servicio asignada a ese profesional

**SQL para verificar datos:**
```sql
-- Ver usuarios profesionales
SELECT u.id, u.nombre, u.email, u.tipo_rol
FROM usuarios u
WHERE u.tipo_rol IN ('PROFESIONAL', 'AMBOS');

-- Ver solicitudes del profesional (ejemplo: profesional_id = 1)
SELECT
    s.id,
    s.cliente_id,
    s.profesional_id,
    s.descripcion,
    s.estado,
    s.fecha_solicitud,
    s.fecha_servicio
FROM solicitudes_servicio s
WHERE s.profesional_id = 1
  AND s.activo = true
ORDER BY s.fecha_solicitud DESC;
```

### Paso 2: Iniciar Sesión como Profesional

1. Abrir navegador y ir a: `http://localhost:9091/login.html`
2. Iniciar sesión con credenciales de un usuario profesional
3. Verificar en consola del navegador (F12) que se guarda `userData` en localStorage:
   ```javascript
   localStorage.getItem('userData')
   ```

### Paso 3: Navegar al Dashboard

1. Ir a: `http://localhost:9091/dashboard.html`
2. El dashboard debe cargar y mostrar el menú lateral
3. Verificar que aparece la sección "Como Profesional" con el ítem "Mis Trabajos"

### Paso 4: Hacer Clic en "Mis Trabajos"

1. En el menú lateral, hacer clic en "💼 Mis Trabajos"
2. **Abrir la consola del navegador (F12) → Tab Console**
3. Verificar los logs que deben aparecer:

**Logs Esperados:**
```
📋 Cargando trabajos del profesional...
👤 Profesional ID: 1
📡 Fetching solicitudes para profesional: 1
📡 Response status: 200
📦 Response completo: {success: true, data: {...}}
📦 Solicitudes cargadas: 3
📋 Primera solicitud: {id: 1, clienteId: 2, descripcion: "..."}
📊 Solicitudes pendientes: 1
```

### Paso 5: Verificar la Tabla de Trabajos

La UI debe mostrar:

✅ **Header de la tarjeta:**
- Título: "💼 Mis Trabajos Recientes"
- Badge rojo animado si hay pendientes: `(1)` ← número de solicitudes pendientes

✅ **Tabla con columnas:**
| Código | Cliente | Descripción | Fecha Servicio | Presupuesto | Estado | Acciones |
|--------|---------|-------------|----------------|-------------|--------|----------|
| SR-2025-000001 | Cliente #2 | Necesito reparar... | 05/12/2025 10:00 | S/ 150.00 | ⏳ Pendiente | Ver Detalle |

✅ **Filas pendientes resaltadas:**
- Fondo amarillo claro
- Borde izquierdo amarillo

✅ **Badge de estado con colores:**
- 🟡 **Pendiente** - Amarillo
- 🔵 **Aceptada** - Azul
- 🟢 **Completada** - Verde
- 🔴 **Rechazada** - Rojo
- ⚫ **Cancelada** - Gris

### Paso 6: Hacer Clic en "Ver Detalle"

1. Hacer clic en el botón "Ver Detalle" de cualquier solicitud
2. Debe redirigir a: `detalle-trabajo.html?id=1`
3. Verificar logs en consola:

**Logs Esperados:**
```
🚀 Inicializando detalle-trabajo.html
✅ Usuario autenticado: 1
📋 ID de solicitud: 1
📡 Cargando solicitud...
👤 Profesional ID: 1
📋 Solicitud ID: 1
📡 Response status: 200
📦 Response completo: {success: true, data: {...}}
✅ Solicitud cargada: {id: 1, ...}
✅ Validación de permisos exitosa
🎨 Renderizando solicitud en UI
🎯 Renderizando acciones disponibles
📊 Estados disponibles: ["ACEPTADA", "RECHAZADA"]
```

### Paso 7: Verificar Detalle de Trabajo

La página debe mostrar:

✅ **Header:**
- Botón "← Volver a Mis Trabajos"
- Título "Detalle del Trabajo"
- Nombre del usuario
- Botón "Cerrar Sesión"

✅ **Card de Información:**
- Código: `SR-2025-000001`
- Estado con badge de color
- Información completa (cliente, fecha, presupuesto, modalidad)
- Descripción del servicio
- Ubicación (si es presencial)
- Notas adicionales (si existen)

✅ **Card de Acciones (si estado permite):**

**Para estado PENDIENTE:**
- Botón verde: "✓ Aceptar Solicitud"
- Botón rojo: "✗ Rechazar Solicitud"

**Para estado ACEPTADA:**
- Botón verde: "✔ Marcar como Completada"
- Botón gris: "⊘ Cancelar Trabajo"

**Para estados finales (RECHAZADA, COMPLETADA, CANCELADA):**
- Mensaje: "Esta solicitud está en un estado final y no puede ser modificada"

### Paso 8: Cambiar Estado de Solicitud

1. Hacer clic en "✓ Aceptar Solicitud"
2. Debe aparecer un modal de confirmación:
   - Título: "Aceptar Solicitud"
   - Mensaje: "¿Deseas aceptar esta solicitud de servicio? El cliente será notificado."
   - Botones: "Cancelar" y "Confirmar"

3. Hacer clic en "Confirmar"
4. Verificar logs:

**Logs Esperados:**
```
🔄 Solicitando cambio de estado a: ACEPTADA
✅ Confirmando cambio de estado a: ACEPTADA
PUT /api/solicitudes/1/estado
Request body: {"nuevoEstado": "ACEPTADA"}
✅ Estado actualizado exitosamente: {...}
```

5. Debe aparecer toast de éxito:
   - "✓ Estado actualizado a 'ACEPTADA' exitosamente"

6. La página se recarga y el estado se actualiza

---

## 🐛 Solución de Problemas

### Problema 1: No se muestran solicitudes

**Síntomas:**
- La tabla aparece vacía
- Mensaje: "No tienes trabajos asignados en este momento"

**Diagnóstico:**
```javascript
// En consola del navegador
console.log(localStorage.getItem('userData'));
// Debe mostrar: {"id": 1, "nombre": "...", ...}
```

**Soluciones:**
1. Verificar que el usuario tenga rol PROFESIONAL o AMBOS
2. Verificar que existan solicitudes con `profesional_id` igual al ID del usuario
3. Revisar logs de consola para ver el error exacto
4. Verificar que el servidor esté corriendo en puerto 9091

### Problema 2: Error 401 (No autorizado)

**Síntomas:**
- Console log: `📡 Response status: 401`
- Mensaje: "Usuario no autenticado"

**Soluciones:**
1. Verificar que la sesión esté activa
2. Hacer logout y volver a iniciar sesión
3. Verificar que el backend esté manejando sesiones correctamente

### Problema 3: Error 404 (No encontrado)

**Síntomas:**
- Console log: `📡 Response status: 404`
- La petición no llega al servlet

**Soluciones:**
1. Verificar que el servidor esté corriendo
2. Verificar la URL del servlet en `web.xml`:
   ```xml
   <url-pattern>/api/solicitudes</url-pattern>
   <url-pattern>/api/solicitudes/*</url-pattern>
   ```
3. Limpiar y recompilar: `mvn clean compile`

### Problema 4: No se puede cambiar el estado

**Síntomas:**
- Al hacer clic en un botón de acción no pasa nada
- Console log: Error en PUT request

**Soluciones:**
1. Verificar logs del backend para ver si la transición es válida
2. Revisar que el estado actual permita la transición deseada:
   - PENDIENTE → solo a ACEPTADA o RECHAZADA
   - ACEPTADA → solo a COMPLETADA o CANCELADA
3. Verificar que el profesional sea el dueño de la solicitud

---

## 📊 Endpoints Utilizados

| Método | Endpoint | Parámetros | Descripción |
|--------|----------|------------|-------------|
| GET | `/api/solicitudes` | `tipo=profesional&usuarioId={id}` | Lista todas las solicitudes del profesional |
| GET | `/api/solicitudes/pendientes/count` | `tipo=profesional&usuarioId={id}` | Cuenta solicitudes pendientes |
| GET | `/api/solicitudes/{id}` | `tipo=profesional&usuarioId={id}` | Obtiene una solicitud específica |
| PUT | `/api/solicitudes/{id}/estado` | Body: `{"nuevoEstado": "aceptada"}` | Actualiza el estado de una solicitud |

---

## 🔍 Debugging Avanzado

### Ver todos los logs del backend

1. Iniciar el servidor en modo debug
2. Buscar en logs por:
   - `SolicitudServicioServlet` - Logs del servlet
   - `SolicitudServicioService` - Logs del servicio
   - `SolicitudServicioDAOImpl` - Logs del DAO

### Ver Request/Response completo en navegador

1. Abrir DevTools (F12)
2. Tab "Network"
3. Hacer clic en "Mis Trabajos"
4. Buscar la petición `solicitudes?tipo=profesional&usuarioId=1`
5. Click derecho → Copy → Copy as cURL (para reproducir)

**Ejemplo de petición cURL:**
```bash
curl 'http://localhost:9091/api/solicitudes?tipo=profesional&usuarioId=1' \
  -H 'Content-Type: application/json' \
  --cookie 'JSESSIONID=...'
```

### Ver estado de localStorage

```javascript
// En consola del navegador
console.table(JSON.parse(localStorage.getItem('userData')));
```

---

## ✅ Checklist de Funcionalidades

### Dashboard - Mis Trabajos
- [ ] Se muestra el menú "Mis Trabajos" para profesionales
- [ ] Al hacer clic, se carga la tabla de solicitudes
- [ ] Se muestra el badge de alertas con número de pendientes
- [ ] Las filas pendientes están resaltadas en amarillo
- [ ] Los estados tienen badges con colores correctos
- [ ] El botón "Ver Detalle" redirige correctamente

### Detalle de Trabajo
- [ ] Se carga la información completa de la solicitud
- [ ] Se valida que el usuario sea el profesional asignado
- [ ] Se muestran los botones de acción según el estado
- [ ] El modal de confirmación aparece al hacer clic en una acción
- [ ] El cambio de estado se ejecuta correctamente
- [ ] Aparece toast de éxito después del cambio
- [ ] La página se recarga y muestra el nuevo estado

### Backend
- [ ] El endpoint GET `/api/solicitudes` retorna las solicitudes correctas
- [ ] El endpoint GET `/api/solicitudes/pendientes/count` retorna el count
- [ ] El endpoint PUT `/api/solicitudes/{id}/estado` actualiza el estado
- [ ] Se validan las transiciones de estado
- [ ] Se validan los permisos del profesional
- [ ] Se envían notificaciones según el estado

---

## 📝 Notas de Implementación

### Logging en Frontend
Todos los archivos JavaScript incluyen logging detallado:
- `console.log()` para flujo normal
- `console.warn()` para advertencias
- `console.error()` para errores
- Emojis para identificar rápidamente el tipo de log

### Logging en Backend
Todos los métodos incluyen logging SLF4J:
- `logger.info()` para operaciones principales
- `logger.debug()` para debugging
- `logger.warn()` para advertencias
- `logger.error()` para errores

### Validaciones Implementadas
1. **Frontend:**
   - Validación de sesión activa
   - Validación de profesionalId
   - Validación de respuestas HTTP

2. **Backend:**
   - Validación de permisos (profesional es dueño)
   - Validación de transiciones de estado
   - Validación de existencia de solicitud

---

**Última actualización:** 2025-12-03
**Archivos modificados:**
- `dashboard.js` (agregado usuarioId en URLs + logging)
- `detalle-trabajo.js` (agregado usuarioId en URLs + logging)
