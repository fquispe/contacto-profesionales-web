/**
 * Script principal para la gestión del perfil de cliente
 * Maneja la interfaz, validaciones y comunicación con el backend
 */

// Variables globales
let clienteAPI;
let addressCount = 1;
const maxAddresses = 3;
let clienteActual = null;

// Inicialización cuando el DOM esté listo
document.addEventListener('DOMContentLoaded', function() {
    // Inicializar API
    clienteAPI = new ClienteAPI();
    
    // Configurar event listeners
    setupEventListeners();
    
    // Cargar datos si hay un ID en la URL
    const urlParams = new URLSearchParams(window.location.search);
    const clienteId = urlParams.get('id');
    
    if (clienteId) {
        cargarDatosCliente(clienteId);
    }
    
    // Configurar preview de foto
    setupPhotoPreview();
});

/**
 * Configura todos los event listeners
 */
function setupEventListeners() {
    // Submit del formulario
    const form = document.getElementById('clientProfileForm');
    if (form) {
        form.addEventListener('submit', handleFormSubmit);
    }
    
    // Tabs de navegación
    const tabButtons = document.querySelectorAll('.tab-button');
    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            switchTab(this.textContent.toLowerCase().includes('personal') ? 'personal' :
                     this.textContent.toLowerCase().includes('direcciones') ? 'direcciones' :
                     this.textContent.toLowerCase().includes('preferencias') ? 'preferencias' :
                     this.textContent.toLowerCase().includes('notificaciones') ? 'notificaciones' : 'privacidad');
        });
    });
    
    // Validación en tiempo real
    const nombreCompleto = document.getElementById('nombreCompleto');
    if (nombreCompleto) {
        nombreCompleto.addEventListener('blur', function() {
            validarCampo(this, 'nombreCompleto');
        });
    }
    
    const email = document.getElementById('email');
    if (email) {
        email.addEventListener('blur', function() {
            validarCampo(this, 'email');
        });
    }
    
    const telefono = document.getElementById('phone');
    if (telefono) {
        telefono.addEventListener('blur', function() {
            validarCampo(this, 'telefono');
        });
    }
}

/**
 * Cambia entre las pestañas del formulario
 */
function switchTab(tabName) {
    // Ocultar todos los contenidos
    const tabContents = document.querySelectorAll('.tab-content');
    tabContents.forEach(content => {
        content.classList.remove('active');
    });
    
    // Desactivar todos los botones
    const tabButtons = document.querySelectorAll('.tab-button');
    tabButtons.forEach(button => {
        button.classList.remove('active');
    });
    
    // Activar el tab seleccionado
    const selectedTab = document.getElementById('tab-' + tabName);
    if (selectedTab) {
        selectedTab.classList.add('active');
    }
    
    // Activar el botón correspondiente
    tabButtons.forEach(button => {
        const buttonText = button.textContent.toLowerCase();
        if (buttonText.includes(tabName.toLowerCase())) {
            button.classList.add('active');
        }
    });
}

/**
 * Configura el preview de la foto de perfil
 */
function setupPhotoPreview() {
    const photoInput = document.getElementById('photoInput');
    if (photoInput) {
        photoInput.addEventListener('change', function(event) {
            const file = event.target.files[0];
            if (file) {
                // Validar tamaño
                const maxSize = 5 * 1024 * 1024; // 5MB
                if (file.size > maxSize) {
                    showAlert('La imagen no debe superar 5MB', 'error');
                    photoInput.value = '';
                    return;
                }
                
                // Validar tipo
                if (!file.type.startsWith('image/')) {
                    showAlert('Por favor seleccione un archivo de imagen válido', 'error');
                    photoInput.value = '';
                    return;
                }
                
                // Mostrar preview
                const reader = new FileReader();
                reader.onload = function(e) {
                    const preview = document.getElementById('photoPreview');
                    preview.innerHTML = `<img src="${e.target.result}" alt="Preview">`;
                };
                reader.readAsDataURL(file);
            }
        });
    }
}

/**
 * Agrega una nueva dirección al formulario
 */
function addAddress() {
    if (addressCount >= maxAddresses) {
        showAlert(`Máximo ${maxAddresses} direcciones permitidas`, 'error');
        return;
    }
    
    addressCount++;
    const container = document.getElementById('addressesContainer');
    const addressTypes = ['🏢 Oficina', '🏪 Trabajo', '👥 Otro'];
    const selectedType = addressTypes[addressCount - 2] || '📍 Dirección ' + addressCount;
    const tipoValue = addressCount === 1 ? 'PRINCIPAL' : (addressCount === 2 ? 'OFICINA' : (addressCount === 3 ? 'TRABAJO' : 'OTRO'));
    
    const newAddress = document.createElement('div');
    newAddress.className = 'address-item';
    newAddress.innerHTML = `
        <div class="address-type">${selectedType}</div>
        <div class="form-row two-cols" style="margin-top: 15px;">
            <div class="form-group">
                <label class="form-label">Dirección Completa *</label>
                <input type="text" class="form-input" name="direccion[]" 
                       placeholder="Av. Principal 123, Departamento 4B" required>
            </div>
            <div class="form-group">
                <label class="form-label">Distrito *</label>
                <select class="form-input" name="distrito[]" required>
                    <option value="">Seleccionar distrito</option>
                    <option value="miraflores">Miraflores</option>
                    <option value="san_isidro">San Isidro</option>
                    <option value="surco">Santiago de Surco</option>
                    <option value="la_molina">La Molina</option>
                    <option value="san_borja">San Borja</option>
                    <option value="surquillo">Surquillo</option>
                    <option value="barranco">Barranco</option>
                    <option value="chorrillos">Chorrillos</option>
                    <option value="lince">Lince</option>
                    <option value="jesus_maria">Jesús María</option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label class="form-label">Referencias</label>
            <input type="text" class="form-input" name="referencia[]" 
                   placeholder="Cerca del parque, frente a la farmacia">
        </div>
        <input type="hidden" name="tipo[]" value="${tipoValue}">
        <input type="hidden" name="esPrincipal[]" value="false">
        <button type="button" class="btn-remove" onclick="removeAddress(this)">✕</button>
    `;
    
    container.appendChild(newAddress);
    
    if (addressCount >= maxAddresses) {
        document.getElementById('addAddressBtn').style.display = 'none';
    }
}

/**
 * Elimina una dirección del formulario
 */
function removeAddress(button) {
    button.parentElement.remove();
    addressCount--;
    document.getElementById('addAddressBtn').style.display = 'block';
}

/**
 * Activa/desactiva un toggle switch
 */
function toggleSwitch(element) {
    element.classList.toggle('active');
}

/**
 * Actualiza el valor del rango de búsqueda
 */
function updateRangeValue(slider) {
    const value = slider.value;
    document.getElementById('rangeValue').textContent = value + ' km';
}

/**
 * Valida un campo específico
 */
function validarCampo(campo, tipo) {
    let isValid = true;
    let mensaje = '';
    
    switch(tipo) {
        case 'nombreCompleto':
            if (campo.value.trim().length < 3) {
                isValid = false;
                mensaje = 'El nombre debe tener al menos 3 caracteres';
            } else if (campo.value.trim().length > 100) {
                isValid = false;
                mensaje = 'El nombre no puede exceder 100 caracteres';
            }
            break;
            
        case 'email':
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(campo.value)) {
                isValid = false;
                mensaje = 'El formato del email no es válido';
            }
            break;
            
        case 'telefono':
            const telefonoRegex = /^[0-9]{9}$/;
            if (!telefonoRegex.test(campo.value)) {
                isValid = false;
                mensaje = 'El teléfono debe tener 9 dígitos';
            }
            break;
    }
    
    if (!isValid) {
        campo.style.borderColor = 'var(--error-color)';
        showAlert(mensaje, 'error');
    } else {
        campo.style.borderColor = 'var(--border-color)';
    }
    
    return isValid;
}

/**
 * Valida el formulario completo antes de enviar
 */
function validateForm() {
    let isValid = true;
    const errores = [];
    
    // Validar campos requeridos
    const nombreCompleto = document.getElementById('nombreCompleto');
    if (!nombreCompleto.value.trim()) {
        errores.push('El nombre completo es obligatorio');
        nombreCompleto.style.borderColor = 'var(--error-color)';
        isValid = false;
    }
    
    const email = document.getElementById('email');
    if (!email.value.trim()) {
        errores.push('El email es obligatorio');
        email.style.borderColor = 'var(--error-color)';
        isValid = false;
    }
    
    const phone = document.getElementById('phone');
    if (!phone.value.trim()) {
        errores.push('El teléfono es obligatorio');
        phone.style.borderColor = 'var(--error-color)';
        isValid = false;
    } else if (!/^[0-9]{9}$/.test(phone.value)) {
        errores.push('El teléfono debe tener 9 dígitos');
        phone.style.borderColor = 'var(--error-color)';
        isValid = false;
    }
    
    // Validar direcciones
    const direcciones = document.querySelectorAll('input[name="direccion[]"]');
    let addressesProvided = 0;
    direcciones.forEach(address => {
        if (address.value.trim()) {
            addressesProvided++;
        }
    });
    
    if (addressesProvided > maxAddresses) {
        errores.push(`Máximo ${maxAddresses} direcciones permitidas`);
        isValid = false;
    }
    
    // Validar radio de búsqueda
    const searchRadius = document.getElementById('searchRadius');
    if (searchRadius && (searchRadius.value < 1 || searchRadius.value > 50)) {
        errores.push('El radio de búsqueda debe estar entre 1 y 50 km');
        isValid = false;
    }
    
    if (errores.length > 0) {
        showAlert(errores.join('<br>'), 'error');
    }
    
    return isValid;
}

/**
 * Recolecta los datos del formulario
 */
function collectFormData() {
    // Datos personales
    const formData = {
        nombreCompleto: document.getElementById('nombreCompleto').value.trim(),
        email: document.getElementById('email').value.trim(),
        telefono: document.getElementById('phone').value.trim(),
    };
    
    // Categorías favoritas
    const categorias = [];
    document.querySelectorAll('input[name="categoria"]:checked').forEach(checkbox => {
        categorias.push(checkbox.value);
    });
    formData.categoriasFavoritas = categorias;
    
    // Preferencias de búsqueda
    formData.radioBusqueda = parseInt(document.getElementById('searchRadius').value);
    
    const presupuesto = document.getElementById('presupuestoPromedio').value;
    formData.presupuestoPromedio = presupuesto ? parseFloat(presupuesto) : null;
    
    // Notificaciones
    formData.notificacionesEmail = document.getElementById('emailNotifications').classList.contains('active');
    formData.notificacionesPush = document.getElementById('pushNotifications').classList.contains('active');
    formData.notificacionesPromociones = document.getElementById('promotionNotifications').classList.contains('active');
    formData.notificacionesResenas = document.getElementById('reviewNotifications').classList.contains('active');
    
    // Privacidad
    formData.perfilVisible = document.getElementById('profileVisible').classList.contains('active');
    formData.compartirUbicacion = document.getElementById('shareLocation').classList.contains('active');
    formData.historialPublico = document.getElementById('publicHistory').classList.contains('active');
    
    // Direcciones
    const direcciones = [];
    const direccionInputs = document.querySelectorAll('input[name="direccion[]"]');
    const distritoSelects = document.querySelectorAll('select[name="distrito[]"]');
    const referenciaInputs = document.querySelectorAll('input[name="referencia[]"]');
    const tipoInputs = document.querySelectorAll('input[name="tipo[]"]');
    const esPrincipalInputs = document.querySelectorAll('input[name="esPrincipal[]"]');
    
    direccionInputs.forEach((input, index) => {
        if (input.value.trim()) {
            direcciones.push({
                tipo: tipoInputs[index]?.value || 'OTRO',
                direccionCompleta: input.value.trim(),
                distrito: distritoSelects[index]?.value || '',
                referencias: referenciaInputs[index]?.value.trim() || null,
                esPrincipal: esPrincipalInputs[index]?.value === 'true'
            });
        }
    });
    
    formData.direcciones = direcciones;
    
    return formData;
}

/**
 * Maneja el envío del formulario
 */
async function handleFormSubmit(event) {
    event.preventDefault();
    
    if (!validateForm()) {
        return;
    }
    
    showLoading(true);
    showAlert('Guardando configuración...', 'info');
    
    try {
        const formData = collectFormData();
        
        // Validar datos con la API
        const validacion = clienteAPI.validarDatosCliente(formData);
        if (!validacion.valid) {
            throw new Error(validacion.errores.join('. '));
        }
        
        // Procesar foto de perfil si existe
        const photoInput = document.getElementById('photoInput');
        if (photoInput && photoInput.files && photoInput.files[0]) {
            formData.fotoPerfilBase64 = await clienteAPI.convertirImagenABase64(photoInput.files[0]);
        }
        
        let resultado;
        const clienteId = document.getElementById('clienteId').value;
        
        if (clienteId) {
            // Actualizar cliente existente
            resultado = await clienteAPI.actualizarCliente(clienteId, formData);
            showAlert('✅ Perfil actualizado exitosamente', 'success');
        } else {
            // Registrar nuevo cliente
            resultado = await clienteAPI.registrarCliente(formData);
            showAlert('✅ Cliente registrado exitosamente', 'success');
            
            // Guardar ID para futuras actualizaciones
            document.getElementById('clienteId').value = resultado.id;
        }
        
        clienteActual = resultado;
        
        // Guardar en localStorage para demo
        localStorage.setItem('clientProfile', JSON.stringify(resultado));
        
        console.log('Cliente guardado:', resultado);
        
    } catch (error) {
        console.error('Error:', error);
        showAlert('❌ Error: ' + error.message, 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Carga los datos de un cliente existente
 */
async function cargarDatosCliente(clienteId) {
    showLoading(true);
    
    try {
        const cliente = await clienteAPI.obtenerCliente(clienteId);
        clienteActual = cliente;
        
        // Llenar datos personales
        document.getElementById('clienteId').value = cliente.id;
        document.getElementById('nombreCompleto').value = cliente.nombreCompleto;
        document.getElementById('email').value = cliente.email;
        document.getElementById('phone').value = cliente.telefono;
        
        // Email solo lectura si es actualización
        document.getElementById('email').readOnly = true;
        
        // Foto de perfil
        if (cliente.fotoPerfilUrl) {
            const preview = document.getElementById('photoPreview');
            preview.innerHTML = `<img src="${cliente.fotoPerfilUrl}" alt="Foto de perfil">`;
        }
        
        // Categorías favoritas
        if (cliente.categoriasFavoritas && cliente.categoriasFavoritas.length > 0) {
            cliente.categoriasFavoritas.forEach(categoria => {
                const checkbox = document.querySelector(`input[name="categoria"][value="${categoria}"]`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        }
        
        // Preferencias de búsqueda
        if (cliente.radioBusqueda) {
            document.getElementById('searchRadius').value = cliente.radioBusqueda;
            updateRangeValue(document.getElementById('searchRadius'));
        }
        
        if (cliente.presupuestoPromedio) {
            document.getElementById('presupuestoPromedio').value = cliente.presupuestoPromedio;
        }
        
        // Notificaciones
        if (cliente.notificacionesEmail) {
            document.getElementById('emailNotifications').classList.add('active');
        }
        if (cliente.notificacionesPush) {
            document.getElementById('pushNotifications').classList.add('active');
        }
        if (cliente.notificacionesPromociones) {
            document.getElementById('promotionNotifications').classList.add('active');
        }
        if (cliente.notificacionesResenas) {
            document.getElementById('reviewNotifications').classList.add('active');
        }
        
        // Privacidad
        if (cliente.perfilVisible) {
            document.getElementById('profileVisible').classList.add('active');
        }
        if (cliente.compartirUbicacion) {
            document.getElementById('shareLocation').classList.add('active');
        }
        if (cliente.historialPublico) {
            document.getElementById('publicHistory').classList.add('active');
        }
        
        // Direcciones
        if (cliente.direcciones && cliente.direcciones.length > 0) {
            // Limpiar contenedor
            const container = document.getElementById('addressesContainer');
            container.innerHTML = '';
            addressCount = 0;
            
            cliente.direcciones.forEach((direccion, index) => {
                // Usar addAddress o crear directamente
                const addressItem = createAddressElement(direccion, index);
                container.appendChild(addressItem);
                addressCount++;
            });
            
            if (addressCount >= maxAddresses) {
                document.getElementById('addAddressBtn').style.display = 'none';
            }
        }
        
        showAlert('Datos cargados correctamente', 'success');
        
    } catch (error) {
        console.error('Error al cargar cliente:', error);
        showAlert('Error al cargar los datos del cliente', 'error');
    } finally {
        showLoading(false);
    }
}

/**
 * Crea un elemento HTML para una dirección
 */
function createAddressElement(direccion, index) {
    const div = document.createElement('div');
    div.className = 'address-item';
    
    const tipoLabel = direccion.tipo === 'PRINCIPAL' ? '🏠 Casa' :
                      direccion.tipo === 'OFICINA' ? '🏢 Oficina' :
                      direccion.tipo === 'TRABAJO' ? '🏪 Trabajo' : '👥 Otro';
    
    div.innerHTML = `
        <div class="address-type">${tipoLabel}</div>
        <div class="form-row two-cols" style="margin-top: 15px;">
            <div class="form-group">
                <label class="form-label">Dirección Completa *</label>
                <input type="text" class="form-input" name="direccion[]" 
                       value="${direccion.direccionCompleta}" required>
            </div>
            <div class="form-group">
                <label class="form-label">Distrito *</label>
                <select class="form-input" name="distrito[]" required>
                    <option value="">Seleccionar distrito</option>
                    <option value="miraflores" ${direccion.distrito === 'miraflores' ? 'selected' : ''}>Miraflores</option>
                    <option value="san_isidro" ${direccion.distrito === 'san_isidro' ? 'selected' : ''}>San Isidro</option>
                    <option value="surco" ${direccion.distrito === 'surco' ? 'selected' : ''}>Santiago de Surco</option>
                    <option value="la_molina" ${direccion.distrito === 'la_molina' ? 'selected' : ''}>La Molina</option>
                    <option value="san_borja" ${direccion.distrito === 'san_borja' ? 'selected' : ''}>San Borja</option>
                    <option value="surquillo" ${direccion.distrito === 'surquillo' ? 'selected' : ''}>Surquillo</option>
                    <option value="barranco" ${direccion.distrito === 'barranco' ? 'selected' : ''}>Barranco</option>
                    <option value="chorrillos" ${direccion.distrito === 'chorrillos' ? 'selected' : ''}>Chorrillos</option>
                    <option value="lince" ${direccion.distrito === 'lince' ? 'selected' : ''}>Lince</option>
                    <option value="jesus_maria" ${direccion.distrito === 'jesus_maria' ? 'selected' : ''}>Jesús María</option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label class="form-label">Referencias</label>
            <input type="text" class="form-input" name="referencia[]" 
                   value="${direccion.referencias || ''}" 
                   placeholder="Cerca del parque, frente a la farmacia">
        </div>
        <input type="hidden" name="tipo[]" value="${direccion.tipo}">
        <input type="hidden" name="esPrincipal[]" value="${direccion.esPrincipal}">
        ${index > 0 ? '<button type="button" class="btn-remove" onclick="removeAddress(this)">✕</button>' : ''}
    `;
    
    return div;
}

/**
 * Cancela la edición y vuelve al listado
 */
function cancelForm() {
    if (confirm('¿Está seguro de cancelar? Los cambios no guardados se perderán.')) {
        window.location.href = 'listado_clientes.jsp';
    }
}

/**
 * Exporta los datos del cliente
 */
function exportData() {
    const cliente = clienteActual || JSON.parse(localStorage.getItem('clientProfile') || '{}');
    
    if (!cliente || !cliente.id) {
        showAlert('No hay datos para exportar', 'error');
        return;
    }
    
    const dataStr = JSON.stringify(cliente, null, 2);
    const blob = new Blob([dataStr], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `cliente_${cliente.id}_${new Date().toISOString().split('T')[0]}.json`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
    
    showAlert('📥 Datos exportados exitosamente', 'success');
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
    let loading = document.querySelector('.loading');
    
    if (!loading) {
        loading = document.createElement('div');
        loading.className = 'loading';
        loading.innerHTML = '<div class="spinner"></div>';
        document.body.appendChild(loading);
    }
    
    if (show) {
        loading.classList.add('active');
    } else {
        loading.classList.remove('active');
    }
}