# Debug - Mis Trabajos No Funciona - 2025-12-03

## 🔍 Diagnóstico Paso a Paso

Sigue estos pasos en orden para diagnosticar por qué "Mis Trabajos" no funciona.

---

## Paso 1: Abrir Dashboard y Consola

1. Abrir en navegador: `http://localhost:9091/dashboard.html`
2. Presionar **F12** para abrir DevTools
3. Ir a la pestaña **Console**

---

## Paso 2: Verificar que el Usuario Está Autenticado

**Ejecutar en consola:**
```javascript
// Ver datos del usuario
console.log('userData:', userData);
console.log('userData.id:', userData.id);
console.log('userData.tipoRol:', userData.tipoRol);
```

**Resultado Esperado:**
```
userData: {id: 1, nombre: "Juan", tipoRol: "PROFESIONAL", ...}
userData.id: 1
userData.tipoRol: "PROFESIONAL"
```

**Si userData está vacío:**
```javascript
// Ver localStorage
console.log('localStorage userData:', localStorage.getItem('userData'));

// Si no hay datos, hacer logout y login nuevamente
localStorage.clear();
window.location.href = 'login.html';
```

---

## Paso 3: Verificar que el Menú "Mis Trabajos" es Visible

**Ejecutar en consola:**
```javascript
// Verificar si el menú profesional está visible
const menuProfesional = document.querySelector('.menu-profesional-section');
console.log('Menu profesional:', menuProfesional);
console.log('Display:', menuProfesional ? menuProfesional.style.display : 'NO ENCONTRADO');
```

**Resultado Esperado:**
```
Menu profesional: <div class="menu-section menu-profesional-section">...</div>
Display: "block" o ""
```

**Si está oculto (display: none):**
```javascript
// Forzar mostrar el menú
document.querySelector('.menu-profesional-section').style.display = 'block';
```

---

## Paso 4: Verificar que la Función navigateTo Existe

**Ejecutar en consola:**
```javascript
// Verificar que la función existe
console.log('navigateTo:', typeof navigateTo);
console.log('renderMisTrabajos:', typeof renderMisTrabajos);
```

**Resultado Esperado:**
```
navigateTo: "function"
renderMisTrabajos: "function"
```

**Si alguna función no existe:**
- Verificar que `dashboard.js` se cargó correctamente
- Ver errores en consola al cargar la página
- Verificar la ruta del script en `dashboard.html`

---

## Paso 5: Llamar Manualmente a la Función

**Ejecutar en consola:**
```javascript
// Llamar directamente a navigateTo
navigateTo('mis-trabajos');
```

**Logs Esperados (en orden):**
```
🔄 Navegando a vista: mis-trabajos
✅ ContentArea encontrado, renderizando vista: mis-trabajos
🎯 Ejecutando renderMisTrabajos...
📋 Cargando trabajos del profesional...
📦 Container recibido: <main class="main-content" id="contentArea">
📦 userData completo: {id: 1, nombre: "...", ...}
👤 Profesional ID: 1
📡 Fetching solicitudes para profesional: 1
📡 Response status: 200
📦 Response completo: {success: true, data: {...}}
📦 Solicitudes cargadas: 3
📋 Primera solicitud: {id: 1, ...}
✅ renderMisTrabajos completado
```

---

## Paso 6: Verificar Respuesta del Backend

**Si aparece error en el fetch:**

**Ejecutar en consola:**
```javascript
// Hacer fetch manual para ver qué retorna el backend
fetch('/api/solicitudes?tipo=profesional&usuarioId=1', {
  headers: { 'Content-Type': 'application/json' },
  credentials: 'include'
})
.then(r => {
  console.log('Status:', r.status);
  return r.json();
})
.then(data => console.log('Data:', data))
.catch(err => console.error('Error:', err));
```

**Resultado Esperado:**
```
Status: 200
Data: {success: true, data: {solicitudes: [...], total: 3}}
```

**Posibles Errores:**

1. **Status: 401 (No Autorizado)**
   - La sesión expiró
   - Hacer logout y login nuevamente

2. **Status: 404 (No Encontrado)**
   - El servlet no está mapeado correctamente
   - Verificar `web.xml`
   - Verificar que el servidor esté corriendo

3. **Status: 500 (Error del Servidor)**
   - Error en el backend
   - Ver logs del servidor

---

## Paso 7: Verificar Que el ContentArea Existe

**Ejecutar en consola:**
```javascript
// Verificar elemento contentArea
const contentArea = document.getElementById('contentArea');
console.log('contentArea:', contentArea);
console.log('contentArea.innerHTML:', contentArea ? contentArea.innerHTML : 'NO ENCONTRADO');
```

**Resultado Esperado:**
```
contentArea: <main class="main-content" id="contentArea">
contentArea.innerHTML: (contenido actual)
```

---

## Paso 8: Forzar Renderizado Manual

**Si todo lo anterior está bien pero no se muestra nada:**

**Ejecutar en consola:**
```javascript
// Obtener el container y llamar directamente a renderMisTrabajos
const container = document.getElementById('contentArea');
renderMisTrabajos(container);
```

**Ver logs y mensajes de error.**

---

## Paso 9: Verificar Datos en Base de Datos

**Ejecutar SQL:**
```sql
-- Ver si hay solicitudes para el profesional
SELECT
    s.id,
    s.cliente_id,
    s.profesional_id,
    s.descripcion,
    s.estado,
    s.fecha_solicitud
FROM solicitudes_servicio s
WHERE s.profesional_id = 1  -- Reemplazar con tu profesionalId
  AND s.activo = true
ORDER BY s.fecha_solicitud DESC;
```

**Si no hay datos:**
- Crear solicitudes de prueba
- Asignar al profesional correcto
- Verificar que `activo = true`

---

## Paso 10: Verificar Network Request

1. Ir a la pestaña **Network** en DevTools
2. Hacer clic en "Mis Trabajos"
3. Buscar la petición `solicitudes?tipo=profesional&usuarioId=1`
4. Ver:
   - **Status**: Debe ser 200
   - **Response**: Debe tener `{success: true, data: {...}}`
   - **Headers**: Verificar cookies de sesión

---

## 🐛 Errores Comunes y Soluciones

### Error 1: "userData.id es undefined"

**Síntoma:**
```
❌ userData.id es: undefined
❌ userData completo: {}
```

**Solución:**
```javascript
// Limpiar y volver a iniciar sesión
localStorage.clear();
window.location.href = 'login.html';
```

### Error 2: "contentArea es null"

**Síntoma:**
```
❌ No se encontró el elemento contentArea
```

**Solución:**
- Verificar que `dashboard.html` tiene: `<main class="main-content" id="contentArea">`
- Verificar que `dashboard.js` se carga después del HTML

### Error 3: "No se muestran solicitudes"

**Síntoma:**
- Tabla vacía
- Mensaje: "No tienes trabajos asignados"

**Solución:**
1. Verificar que existen solicitudes en BD con ese `profesional_id`
2. Verificar que `activo = true`
3. Verificar logs del backend

### Error 4: "Error 401 al hacer fetch"

**Síntoma:**
```
📡 Response status: 401
```

**Solución:**
- Sesión expiró
- Hacer logout y login nuevamente
- Verificar configuración de sesiones en backend

### Error 5: "navigateTo no está definido"

**Síntoma:**
```
Uncaught ReferenceError: navigateTo is not defined
```

**Solución:**
- Verificar que `dashboard.js` se cargó correctamente
- Ver pestaña Network → dashboard.js debe tener status 200
- Verificar que no hay errores de sintaxis en el archivo

---

## 🔧 Script de Diagnóstico Completo

**Copiar y pegar este script completo en la consola:**

```javascript
console.log('=== DIAGNÓSTICO MIS TRABAJOS ===\n');

// 1. Verificar userData
console.log('1. userData:', userData);
console.log('   - id:', userData.id);
console.log('   - tipoRol:', userData.tipoRol);
console.log('   - localStorage:', localStorage.getItem('userData'));

// 2. Verificar menú
const menuProfesional = document.querySelector('.menu-profesional-section');
console.log('\n2. Menú Profesional:');
console.log('   - Encontrado:', !!menuProfesional);
console.log('   - Display:', menuProfesional ? menuProfesional.style.display : 'NO ENCONTRADO');

// 3. Verificar funciones
console.log('\n3. Funciones:');
console.log('   - navigateTo:', typeof navigateTo);
console.log('   - renderMisTrabajos:', typeof renderMisTrabajos);

// 4. Verificar contentArea
const contentArea = document.getElementById('contentArea');
console.log('\n4. ContentArea:');
console.log('   - Encontrado:', !!contentArea);

// 5. Intentar fetch manual
console.log('\n5. Probando fetch...');
const profesionalId = userData.id;
if (profesionalId) {
  fetch(`/api/solicitudes?tipo=profesional&usuarioId=${profesionalId}`, {
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include'
  })
  .then(r => {
    console.log('   - Status:', r.status);
    return r.json();
  })
  .then(data => {
    console.log('   - Success:', data.success);
    console.log('   - Solicitudes:', data.data?.solicitudes?.length || 0);
  })
  .catch(err => {
    console.error('   - Error:', err.message);
  });
} else {
  console.error('   - ERROR: No hay profesionalId');
}

console.log('\n=== FIN DIAGNÓSTICO ===');
```

---

## ✅ Checklist de Verificación

- [ ] userData tiene id válido
- [ ] Menú "Como Profesional" está visible
- [ ] Funciones navigateTo y renderMisTrabajos existen
- [ ] Element contentArea existe en DOM
- [ ] Fetch a /api/solicitudes retorna 200
- [ ] Hay solicitudes en BD con el profesional_id correcto
- [ ] dashboard.js se carga sin errores
- [ ] dashboard.css se carga sin errores
- [ ] Servidor backend está corriendo en puerto correcto

---

## 📞 Información de Soporte

Si después de todos estos pasos el problema persiste:

1. **Copiar logs completos de consola**
2. **Captura de pantalla del error**
3. **Versión del navegador**
4. **Estado del servidor (logs backend)**

---

**Última actualización:** 2025-12-03
