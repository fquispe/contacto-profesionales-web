/**
 * Script para la página de listado de clientes
 * Consume la API REST de servlets Java
 */

let clienteAPI;
let todosClientes = [];

// Inicialización
document.addEventListener('DOMContentLoaded', function() {
    clienteAPI = new ClienteAPI();
    cargarClientes();
    
    // Buscar al presionar Enter
    document.getElementById('searchInput').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            buscarClientes();
        }
    });
});

/**
 * Carga todos los clientes
 */
async function cargarClientes() {
    showLoading(true);
    
    try {
        todosClientes = await clienteAPI.listarClientes();
        mostrarClientes(todosClientes);
        actualizarEstadisticas(todosClientes);
    } catch (error) {
        console.error('Error al cargar clientes:', error);
        showAlert('Error al cargar la lista de clientes: ' + error.message, 'error');
        mostrarEstadoVacio('Error al cargar los datos');
    } finally {
        showLoading(false);
    }
}

/**
 * Muestra los clientes en la interfaz
 */
function mostrarClientes(clientes) {
    const container = document.getElementById('clientesContainer');
    
    if (clientes.length === 0) {
        mostrarEstadoVacio('No hay clientes registrados');
        return;
    }
    
    container.innerHTML = clientes.map(cliente => `
        <div class="cliente-card">
            <div class="cliente-avatar">
                ${cliente.fotoPerfilUrl ? 
                    `<img src="${cliente.fotoPerfilUrl}" style="width:100%;height:100%;border-radius:50%;object-fit:cover;">` : 
                    obtenerIniciales(cliente.nombreCompleto)}
            </div>
            <div class="cliente-info">
                <div class="cliente-nombre">${cliente.nombreCompleto}</div>
                <div class="cliente-detalles">
                    <span>📧 ${cliente.email}</span>
                    <span>📱 ${cliente.telefono}</span>
                </div>
                <div class="cliente-badges">
                    <span class="badge ${cliente.activo ? 'badge-activo' : 'badge-inactivo'}">
                        ${cliente.activo ? '✓ Activo' : '✕ Inactivo'}
                    </span>
                    ${cliente.direcciones && cliente.direcciones.length > 0 ? 
                        `<span class="badge" style="background:#e3f2fd;color:#1976d2;">
                            📍 ${cliente.direcciones.length} ${cliente.direcciones.length === 1 ? 'dirección' : 'direcciones'}
                        </span>` : ''}
                    ${cliente.radioBusqueda ? 
                        `<span class="badge" style="background:#f3e5f5;color:#7b1fa2;">
                            🔍 ${cliente.radioBusqueda} km
                        </span>` : ''}
                </div>
            </div>
            <div class="cliente-acciones">
                <a href="perfil_cliente.html?id=${cliente.id}" 
                   class="btn-accion btn-ver">👁️ Ver</a>
                <a href="perfil_cliente.html?id=${cliente.id}" 
                   class="btn-accion btn-editar">✏️ Editar</a>
                <button onclick="confirmarEliminar(${cliente.id}, '${cliente.nombreCompleto.replace(/'/g, "\\'")}')" 
                        class="btn-accion btn-eliminar">🗑️ Eliminar</button>
            </div>
        </div>
    `).join('');
}

/**
 * Muestra estado vacío
 */
function mostrarEstadoVacio(mensaje) {
    const container = document.getElementById('clientesContainer');
    container.innerHTML = `
        <div class="empty-state">
            <div class="empty-state-icon">📋</div>
            <h3>${mensaje}</h3>
            <p>Comience agregando un nuevo cliente usando el botón "Nuevo Cliente"</p>
        </div>
    `;
}

/**
 * Actualiza las estadísticas
 */
function actualizarEstadisticas(clientes) {
    document.getElementById('totalClientes').textContent = clientes.length;
    document.getElementById('clientesActivos').textContent = 
        clientes.filter(c => c.activo).length;
    document.getElementById('clientesInactivos').textContent = 
        clientes.filter(c => !c.activo).length;
}

/**
 * Busca clientes por término
 */
function buscarClientes() {
    const termino = document.getElementById('searchInput').value.toLowerCase().trim();
    
    if (!termino) {
        mostrarClientes(todosClientes);
        return;
    }
    
    const resultados = todosClientes.filter(cliente => 
        cliente.nombreCompleto.toLowerCase().includes(termino) ||
        cliente.email.toLowerCase().includes(termino) ||
        cliente.telefono.includes(termino)
    );
    
    mostrarClientes(resultados);
    
    if (resultados.length === 0) {
        mostrarEstadoVacio(`No se encontraron resultados para "${termino}"`);
    }
}

/**
 * Confirma la eliminación de un cliente
 */
function confirmarEliminar(clienteId, nombreCliente) {
    if (confirm(`¿Está seguro de desactivar al cliente "${nombreCliente}"?\n\nEsta acción se puede revertir.`)) {
        eliminarCliente(clienteId);
    }
}

/**
 * Elimina (desactiva) un cliente
 */
async function eliminarCliente(clienteId) {
    showLoading(true);
    
    try {
        await clienteAPI.desactivarCliente(clienteId);
        showAlert('✅ Cliente desactivado exitosamente', 'success');
        cargarClientes(); // Recargar lista
    } catch (error) {
        console.error('Error al eliminar cliente:', error);
        showAlert('❌ Error al desactivar el cliente: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Obtiene las iniciales de un nombre
 */
function obtenerIniciales(nombreCompleto) {
    const partes = nombreCompleto.trim().split(' ');
    if (partes.length >= 2) {
        return partes[0][0] + partes[1][0];
    }
    return nombreCompleto.substring(0, 2).toUpperCase();
}

/**
 * Muestra un mensaje de alerta
 */
function showAlert(message, type = 'info') {
    const container = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = message;
    
    container.innerHTML = '';
    container.appendChild(alert);
    
    if (type === 'success') {
        setTimeout(() => {
            alert.style.display = 'none';
        }, 5000);
    }
}

/**
 * Muestra/oculta el indicador de carga
 */
function showLoading(show) {
    const loading = document.getElementById('loadingContainer');
    const container = document.getElementById('clientesContainer');
    
    if (show) {
        loading.style.display = 'block';
        container.style.display = 'none';
    } else {
        loading.style.display = 'none';
        container.style.display = 'grid';
    }
}