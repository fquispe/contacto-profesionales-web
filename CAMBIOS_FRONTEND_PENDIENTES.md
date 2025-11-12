# Cambios Frontend Pendientes - Fase 4

Este documento detalla todos los cambios necesarios en los archivos HTML del frontend para completar la integración con el backend refactorizado.

---

## ✅ Completados

### 1. register.html
**Status:** ✅ Completado

**Cambios realizados:**
- Modificado objeto `userData` en función `handleRegister()` (línea ~867)
- Campo `nombre` → `nombreCompleto`
- Campo `tipo` → `tipoCuenta`
- Agregado mapeo: `'cliente'` → `'CLIENTE'`, `'proveedor'` → `'PROFESIONAL'`

**Código modificado:**
```javascript
// Mapear tipo de usuario al formato del backend
const tipoCuentaBackend = currentUserType === 'cliente' ? 'CLIENTE' : 'PROFESIONAL';

// Preparar datos para enviar (nuevo flujo con tipoCuenta)
const userData = {
    nombreCompleto: fullName,
    email: email,
    telefono: phone,
    password: password,
    tipoCuenta: tipoCuentaBackend
};
```

---

## 📋 Pendientes

### 2. dashboard.html
**Status:** ⏳ Pendiente
**Prioridad:** Alta

**Descripción:**
El dashboard debe mostrar contenido dinámico según el rol del usuario (CLIENTE, PROFESIONAL, o AMBOS).

**Cambios necesarios:**

#### 2.1. Obtener información del usuario logueado
```javascript
// Al cargar el dashboard, obtener datos del usuario desde sessionStorage o API
async function cargarDatosUsuario() {
    try {
        // Opción 1: Desde sessionStorage (si se guarda en login)
        const userData = JSON.parse(sessionStorage.getItem('userData'));

        // Opción 2: Desde API GET /api/auth/me (recomendado)
        const response = await fetch('/ContactoProfesionalesWeb/api/auth/me', {
            headers: {
                'Authorization': 'Bearer ' + sessionStorage.getItem('token')
            }
        });
        const data = await response.json();

        if (data.success) {
            mostrarDashboardSegunRol(data.data);
        }
    } catch (error) {
        console.error('Error al cargar datos de usuario:', error);
    }
}
```

#### 2.2. Mostrar contenido según rol
```javascript
function mostrarDashboardSegunRol(userData) {
    const { tipoRol, esCliente, esProfesional } = userData;

    // Elementos del DOM (ajustar según HTML actual)
    const seccionCliente = document.getElementById('dashboard-cliente');
    const seccionProfesional = document.getElementById('dashboard-profesional');
    const menuCliente = document.getElementById('menu-cliente');
    const menuProfesional = document.getElementById('menu-profesional');

    // Mostrar/ocultar secciones según rol
    if (tipoRol === 'CLIENTE') {
        seccionCliente.style.display = 'block';
        seccionProfesional.style.display = 'none';
        menuCliente.style.display = 'block';
        menuProfesional.style.display = 'none';
    } else if (tipoRol === 'PROFESIONAL') {
        seccionCliente.style.display = 'none';
        seccionProfesional.style.display = 'block';
        menuCliente.style.display = 'none';
        menuProfesional.style.display = 'block';
    } else if (tipoRol === 'AMBOS') {
        // Mostrar ambos con un selector de vista
        seccionCliente.style.display = 'block';
        seccionProfesional.style.display = 'block';
        menuCliente.style.display = 'block';
        menuProfesional.style.display = 'block';

        // Agregar selector de vista si no existe
        agregarSelectorDeVista();
    }
}

function agregarSelectorDeVista() {
    const header = document.querySelector('.dashboard-header');
    const selector = `
        <div class="vista-selector">
            <button class="vista-btn active" data-vista="cliente">
                👤 Vista Cliente
            </button>
            <button class="vista-btn" data-vista="profesional">
                🔧 Vista Profesional
            </button>
        </div>
    `;
    header.insertAdjacentHTML('beforeend', selector);

    // Event listeners para cambiar vista
    document.querySelectorAll('.vista-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            const vista = this.getAttribute('data-vista');
            cambiarVista(vista);
        });
    });
}
```

**Archivos a modificar:**
- `src/main/webapp/dashboard.html`

---

### 3. solicitud-servicio.html
**Status:** ⏳ Pendiente
**Prioridad:** Alta

**Descripción:**
Al crear una solicitud de servicio, auto-cargar la ubicación desde el perfil del cliente en lugar de pedir que la ingrese manualmente.

**Cambios necesarios:**

#### 3.1. Incluir API de ubicación
```html
<!-- Agregar en el <head> o antes del cierre de </body> -->
<script src="assets/js/ubicacion-api.js"></script>
```

#### 3.2. Auto-cargar ubicación del cliente
```javascript
async function cargarUbicacionCliente() {
    try {
        // Obtener ID del cliente desde sessionStorage
        const userData = JSON.parse(sessionStorage.getItem('userData'));
        const clienteId = userData.clienteId;

        if (!clienteId) {
            console.warn('Usuario no tiene perfil de cliente');
            return;
        }

        // Obtener datos del cliente con ubicación
        const response = await fetch(`/ContactoProfesionalesWeb/api/clientes/${clienteId}`);
        const data = await response.json();

        if (data.success && data.data.distritoId) {
            const cliente = data.data;

            // Pre-llenar los campos de ubicación
            await precargarUbicacion(cliente.distritoId);

            // Opcional: Marcar campos como solo lectura o mostrar mensaje
            mostrarMensajeUbicacionPrecargada(cliente.direccion);
        }
    } catch (error) {
        console.error('Error al cargar ubicación del cliente:', error);
    }
}

async function precargarUbicacion(distritoId) {
    // Aquí necesitas hacer reverse lookup:
    // distritoId -> distrito -> provincia -> departamento

    // Opción 1: Agregar endpoint en backend GET /api/ubicacion/distrito/{id}/jerarquia
    const response = await fetch(`/ContactoProfesionalesWeb/api/ubicacion/distrito/${distritoId}/jerarquia`);
    const data = await response.json();

    if (data.success) {
        const { departamento, provincia, distrito } = data.data;

        // Cargar departamentos
        const selectDepartamento = document.getElementById('departamento');
        await ubicacionAPI.popularDepartamentos(selectDepartamento);
        selectDepartamento.value = departamento.id;

        // Cargar provincias
        const selectProvincia = document.getElementById('provincia');
        await ubicacionAPI.popularProvincias(selectProvincia, departamento.id);
        selectProvincia.value = provincia.id;

        // Cargar distritos
        const selectDistrito = document.getElementById('distrito');
        await ubicacionAPI.popularDistritos(selectDistrito, provincia.id);
        selectDistrito.value = distrito.id;
    }
}

function mostrarMensajeUbicacionPrecargada(direccion) {
    const mensaje = `
        <div class="alert alert-info">
            ℹ️ Se ha cargado automáticamente tu ubicación registrada: <strong>${direccion}</strong>
        </div>
    `;
    document.querySelector('.ubicacion-container').insertAdjacentHTML('afterbegin', mensaje);
}
```

#### 3.3. Backend adicional requerido
**NOTA:** Se necesita crear un endpoint adicional en `UbicacionServlet.java`:

```java
// GET /api/ubicacion/distrito/{id}/jerarquia
// Retorna: { departamento: {...}, provincia: {...}, distrito: {...} }
```

**Archivos a modificar:**
- `src/main/webapp/solicitud-servicio.html`
- `src/main/java/com/contactoprofesionales/controller/UbicacionServlet.java` (agregar endpoint)

---

### 4. profesional.html
**Status:** ⏳ Pendiente
**Prioridad:** Media-Alta

**Descripción:**
Agregar interfaz para gestionar especialidades (máximo 3, 1 principal) y redes sociales del profesional.

**Cambios necesarios:**

#### 4.1. Incluir APIs necesarias
```html
<!-- Agregar en el <head> o antes del cierre de </body> -->
<script src="assets/js/especialidad-api.js"></script>
<script src="assets/js/red-social-api.js"></script>
<script src="assets/js/categoria-api.js"></script>
```

#### 4.2. HTML para gestión de especialidades
```html
<section class="especialidades-section">
    <div class="section-header">
        <h2>Mis Especialidades</h2>
        <button id="btnAgregarEspecialidad" class="btn-primary">
            ➕ Agregar Especialidad
        </button>
    </div>

    <div id="especialidadesContainer" class="especialidades-container">
        <!-- Las especialidades se renderizan aquí usando especialidadAPI.renderizar() -->
    </div>

    <!-- Modal para agregar especialidad -->
    <div id="modalEspecialidad" class="modal" style="display: none;">
        <div class="modal-content">
            <h3>Agregar Especialidad</h3>
            <form id="formEspecialidad">
                <div class="form-group">
                    <label>Categoría de Servicio</label>
                    <select id="categoriaEspecialidad" required>
                        <!-- Opciones cargadas dinámicamente desde categoriaAPI -->
                    </select>
                </div>

                <div class="form-group">
                    <label>Años de Experiencia</label>
                    <input type="number" id="aniosExperiencia" min="0" max="50" placeholder="0">
                </div>

                <div class="form-group">
                    <label>Descripción (opcional)</label>
                    <textarea id="descripcionEspecialidad" rows="3"
                        placeholder="Describe tu experiencia en esta área..."></textarea>
                </div>

                <div class="form-group">
                    <label>
                        <input type="checkbox" id="esPrincipal">
                        Marcar como especialidad principal
                    </label>
                </div>

                <div class="modal-actions">
                    <button type="submit" class="btn-primary">Guardar</button>
                    <button type="button" class="btn-secondary" onclick="cerrarModalEspecialidad()">
                        Cancelar
                    </button>
                </div>
            </form>
        </div>
    </div>
</section>
```

#### 4.3. JavaScript para especialidades
```javascript
let profesionalId = null; // Obtener del sessionStorage
let especialidades = [];

// Al cargar la página
document.addEventListener('DOMContentLoaded', async function() {
    const userData = JSON.parse(sessionStorage.getItem('userData'));
    profesionalId = userData.profesionalId;

    await cargarCategorias();
    await cargarEspecialidades();
});

async function cargarCategorias() {
    try {
        const categorias = await categoriaAPI.listar();
        const select = document.getElementById('categoriaEspecialidad');

        select.innerHTML = '<option value="">Selecciona una categoría</option>';
        categorias.forEach(cat => {
            select.innerHTML += `<option value="${cat.id}">${cat.nombre}</option>`;
        });
    } catch (error) {
        console.error('Error al cargar categorías:', error);
    }
}

async function cargarEspecialidades() {
    try {
        especialidades = await especialidadAPI.listar(profesionalId);

        const container = document.getElementById('especialidadesContainer');
        especialidadAPI.renderizar(container, especialidades, {
            onEliminar: eliminarEspecialidad,
            onMarcarPrincipal: marcarComoPrincipal
        });

        // Habilitar/deshabilitar botón según límite
        const btnAgregar = document.getElementById('btnAgregarEspecialidad');
        if (!especialidadAPI.puedeAgregarMas(especialidades)) {
            btnAgregar.disabled = true;
            btnAgregar.textContent = '⚠️ Límite de 3 especialidades alcanzado';
        }
    } catch (error) {
        console.error('Error al cargar especialidades:', error);
    }
}

async function agregarEspecialidad(event) {
    event.preventDefault();

    const especialidadData = {
        categoriaId: parseInt(document.getElementById('categoriaEspecialidad').value),
        aniosExperiencia: parseInt(document.getElementById('aniosExperiencia').value) || 0,
        descripcion: document.getElementById('descripcionEspecialidad').value.trim(),
        esPrincipal: document.getElementById('esPrincipal').checked
    };

    try {
        await especialidadAPI.agregar(profesionalId, especialidadData);
        await cargarEspecialidades(); // Recargar lista
        cerrarModalEspecialidad();
        mostrarAlerta('Especialidad agregada exitosamente', 'success');
    } catch (error) {
        mostrarAlerta('Error al agregar especialidad: ' + error.message, 'error');
    }
}

async function eliminarEspecialidad(especialidadId) {
    if (!confirm('¿Estás seguro de eliminar esta especialidad?')) return;

    try {
        await especialidadAPI.eliminar(profesionalId, especialidadId);
        await cargarEspecialidades();
        mostrarAlerta('Especialidad eliminada', 'success');
    } catch (error) {
        mostrarAlerta('Error al eliminar: ' + error.message, 'error');
    }
}

async function marcarComoPrincipal(especialidadId) {
    try {
        await especialidadAPI.marcarComoPrincipal(profesionalId, especialidadId);
        await cargarEspecialidades();
        mostrarAlerta('Especialidad marcada como principal', 'success');
    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    }
}
```

#### 4.4. HTML para redes sociales
```html
<section class="redes-sociales-section">
    <div class="section-header">
        <h2>Redes Sociales</h2>
        <button id="btnAgregarRed" class="btn-primary">
            ➕ Agregar Red Social
        </button>
    </div>

    <div id="redesSocialesContainer" class="redes-container">
        <!-- Las redes se renderizan aquí usando redSocialAPI.renderizar() -->
    </div>

    <!-- Modal para agregar red social -->
    <div id="modalRedSocial" class="modal" style="display: none;">
        <div class="modal-content">
            <h3>Agregar Red Social</h3>
            <form id="formRedSocial">
                <div class="form-group">
                    <label>Tipo de Red Social</label>
                    <select id="tipoRed" required>
                        <!-- Opciones cargadas dinámicamente -->
                    </select>
                </div>

                <div class="form-group">
                    <label>URL / Usuario</label>
                    <input type="text" id="urlRedSocial"
                        placeholder="https://..." required>
                    <small class="help-text" id="placeholderRedSocial"></small>
                </div>

                <div class="modal-actions">
                    <button type="submit" class="btn-primary">Guardar</button>
                    <button type="button" class="btn-secondary" onclick="cerrarModalRedSocial()">
                        Cancelar
                    </button>
                </div>
            </form>
        </div>
    </div>
</section>
```

#### 4.5. JavaScript para redes sociales
```javascript
let redesSociales = [];

async function cargarRedesSociales() {
    try {
        redesSociales = await redSocialAPI.listar(profesionalId);

        const container = document.getElementById('redesSocialesContainer');
        redSocialAPI.renderizar(container, redesSociales, {
            onEliminar: eliminarRedSocial,
            onEditar: editarRedSocial
        });

        // Actualizar select de tipos disponibles
        const selectTipoRed = document.getElementById('tipoRed');
        redSocialAPI.popularTiposRed(selectTipoRed, redesSociales);
    } catch (error) {
        console.error('Error al cargar redes sociales:', error);
    }
}

// Event listener para mostrar placeholder dinámico
document.getElementById('tipoRed').addEventListener('change', function() {
    const tipoRed = this.value;
    const info = redSocialAPI.getInfoTipoRed(tipoRed);
    document.getElementById('placeholderRedSocial').textContent =
        `Ejemplo: ${info.placeholder}`;
    document.getElementById('urlRedSocial').placeholder = info.placeholder;
});

async function agregarRedSocial(event) {
    event.preventDefault();

    const redSocialData = {
        tipoRed: document.getElementById('tipoRed').value,
        url: document.getElementById('urlRedSocial').value.trim()
    };

    try {
        await redSocialAPI.agregar(profesionalId, redSocialData);
        await cargarRedesSociales();
        cerrarModalRedSocial();
        mostrarAlerta('Red social agregada exitosamente', 'success');
    } catch (error) {
        mostrarAlerta('Error al agregar red social: ' + error.message, 'error');
    }
}

async function eliminarRedSocial(redSocialId) {
    if (!confirm('¿Eliminar esta red social?')) return;

    try {
        await redSocialAPI.eliminar(profesionalId, redSocialId);
        await cargarRedesSociales();
        mostrarAlerta('Red social eliminada', 'success');
    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    }
}

async function editarRedSocial(redSocialId, urlActual) {
    const nuevaUrl = prompt('Ingresa la nueva URL:', urlActual);
    if (!nuevaUrl || nuevaUrl === urlActual) return;

    try {
        await redSocialAPI.actualizar(profesionalId, redSocialId, nuevaUrl);
        await cargarRedesSociales();
        mostrarAlerta('URL actualizada', 'success');
    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    }
}
```

#### 4.6. CSS adicional necesario
```css
/* Estilos para especialidades */
.especialidades-container {
    display: grid;
    gap: 15px;
    margin-top: 20px;
}

.especialidad-item {
    background: white;
    border: 2px solid #e9ecef;
    border-radius: 12px;
    padding: 20px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    transition: all 0.3s ease;
}

.especialidad-item.principal {
    border-color: var(--primary-color);
    background: linear-gradient(to right, rgba(102, 126, 234, 0.05), white);
}

.especialidad-item:hover {
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
    transform: translateY(-2px);
}

/* Estilos para redes sociales */
.redes-container {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 15px;
    margin-top: 20px;
}

.red-social-item {
    background: white;
    border: 2px solid #e9ecef;
    border-radius: 12px;
    padding: 15px;
    transition: all 0.3s ease;
}

.red-icono {
    font-size: 32px;
    margin-bottom: 10px;
    display: block;
}

.red-url {
    color: var(--primary-color);
    text-decoration: none;
    word-break: break-all;
    font-size: 14px;
}

/* Modal styles */
.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background: white;
    border-radius: 16px;
    padding: 30px;
    max-width: 500px;
    width: 90%;
    max-height: 90vh;
    overflow-y: auto;
}
```

**Archivos a modificar:**
- `src/main/webapp/profesional.html`
- Crear `src/main/webapp/assets/js/categoria-api.js` (si no existe)

---

### 5. perfil-cliente.html
**Status:** ⏳ Pendiente
**Prioridad:** Media

**Descripción:**
Actualizar el perfil del cliente para usar la nueva API de ubicación con la cascada departamento → provincia → distrito.

**Cambios necesarios:**

#### 5.1. Incluir API de ubicación
```html
<script src="assets/js/ubicacion-api.js"></script>
```

#### 5.2. Configurar cascada de ubicación
```javascript
document.addEventListener('DOMContentLoaded', function() {
    // Configurar la cascada de selects
    ubicacionAPI.configurarCascada({
        departamento: document.getElementById('departamento'),
        provincia: document.getElementById('provincia'),
        distrito: document.getElementById('distrito')
    });

    // Si está editando, pre-cargar la ubicación actual
    if (clienteData.distritoId) {
        precargarUbicacionCliente(clienteData.distritoId);
    }
});
```

**Archivos a modificar:**
- `src/main/webapp/perfil-cliente.html`

---

### 6. categoria-api.js
**Status:** ⏳ Pendiente (crear archivo)
**Prioridad:** Media

**Descripción:**
Crear API JavaScript para obtener categorías de servicios desde `CategoriaServlet`.

**Archivo a crear:**
`src/main/webapp/assets/js/categoria-api.js`

**Contenido:**
```javascript
/**
 * API para gestión de categorías de servicios
 * Integración con CategoriaServlet del backend
 */

class CategoriaAPI {
    constructor() {
        this.baseURL = '/ContactoProfesionalesWeb/api';
    }

    /**
     * Obtiene todas las categorías de servicios activas
     * @returns {Promise<Array>} Lista de categorías
     */
    async listar() {
        try {
            const response = await fetch(`${this.baseURL}/categorias`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al obtener categorías');
            }
        } catch (error) {
            console.error('Error en listar categorías:', error);
            throw error;
        }
    }

    /**
     * Popula un select con las categorías disponibles
     * @param {HTMLSelectElement} selectElement - Elemento select a popular
     * @param {string} defaultOption - Texto de la opción por defecto
     */
    async popularCategorias(selectElement, defaultOption = 'Selecciona una categoría') {
        try {
            const categorias = await this.listar();

            selectElement.innerHTML = `<option value="">${defaultOption}</option>`;

            categorias.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = `${cat.icono || '🔧'} ${cat.nombre}`;
                option.dataset.descripcion = cat.descripcion || '';
                selectElement.appendChild(option);
            });

            return categorias;
        } catch (error) {
            console.error('Error al popular categorías:', error);
            selectElement.innerHTML = `<option value="">Error al cargar categorías</option>`;
            throw error;
        }
    }
}

// Exportar como singleton
const categoriaAPI = new CategoriaAPI();
```

---

## 🔧 Backend Adicional Requerido

Para completar todos los cambios frontend, se necesitan los siguientes endpoints adicionales en el backend:

### 1. Endpoint de usuario autenticado
**Servlet:** `AuthServlet.java` (crear o modificar)
**Endpoint:** `GET /api/auth/me`
**Descripción:** Retorna información del usuario actualmente autenticado
**Respuesta:**
```json
{
    "success": true,
    "data": {
        "userId": 123,
        "usuarioPersonaId": 456,
        "email": "user@example.com",
        "nombreCompleto": "Juan Pérez",
        "tipoRol": "AMBOS",
        "esCliente": true,
        "esProfesional": true,
        "clienteId": 789,
        "profesionalId": 321
    }
}
```

### 2. Endpoint de jerarquía de ubicación
**Servlet:** `UbicacionServlet.java` (modificar)
**Endpoint:** `GET /api/ubicacion/distrito/{distritoId}/jerarquia`
**Descripción:** Retorna departamento, provincia y distrito dado un distritoId
**Respuesta:**
```json
{
    "success": true,
    "data": {
        "departamento": {"id": 1, "codigo": "15", "nombre": "Lima"},
        "provincia": {"id": 101, "codigo": "01", "nombre": "Lima"},
        "distrito": {"id": 10101, "codigo": "01", "nombre": "Lima"}
    }
}
```

---

## 📝 Notas Importantes

### Compatibilidad
- Todos los cambios mantienen compatibilidad con el flujo legacy cuando es posible
- Los usuarios existentes deben poder seguir usando el sistema sin problemas

### Seguridad
- Validar en backend que el usuario tenga permiso para modificar sus propias especialidades/redes
- Validar límite de 3 especialidades tanto en frontend como backend
- Sanitizar URLs de redes sociales para prevenir XSS

### UX Mejorada
- Mostrar mensajes de confirmación al eliminar elementos
- Mostrar spinners/loading states durante operaciones async
- Deshabilitar botones cuando se alcancen límites
- Mostrar placeholders dinámicos según el tipo de red social seleccionada

### Testing
Después de implementar estos cambios, probar:
1. Registro de nuevo usuario como CLIENTE, PROFESIONAL y AMBOS
2. Login y visualización correcta del dashboard según rol
3. Creación de solicitud con ubicación pre-cargada
4. Gestión de especialidades (agregar, marcar principal, eliminar)
5. Gestión de redes sociales (agregar, editar, eliminar)
6. Actualización de perfil de cliente con nueva ubicación

---

## 🎯 Priorización de Tareas

1. **Alta prioridad:**
   - dashboard.html (crítico para navegación)
   - solicitud-servicio.html (funcionalidad core)
   - Backend: GET /api/auth/me

2. **Media prioridad:**
   - profesional.html (valor agregado para profesionales)
   - categoria-api.js (requerido por profesional.html)
   - perfil-cliente.html (mejora UX)

3. **Baja prioridad:**
   - Backend: GET /api/ubicacion/distrito/{id}/jerarquia (nice to have)
   - Mejoras de CSS y animaciones

---

**Fecha de creación:** 2025-11-10
**Fase:** 4 - Frontend
**Última actualización:** 2025-11-10
