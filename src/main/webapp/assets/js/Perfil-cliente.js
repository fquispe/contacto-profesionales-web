// Perfil Cliente JavaScript - Desacoplado el 2025-11-21

const MAX_ADDRESSES = 3;
const MAX_IMAGE_SIZE = 5 * 1024 * 1024;
let addressCount = 0;
let clienteId = null;
let photoBase64 = null;

function getContextPath() {
    const path = window.location.pathname;
    if (path.includes('.html')) {
        const parts = path.split('/');
        parts.pop();
        return parts.join('/') || '';
    }
    return path.substring(0, path.indexOf('/', 1)) || '';
}

const BASE_URL = getContextPath() + '/api/clientes';

function switchTab(tabName) {
    document.querySelectorAll('.tab-content').forEach(tab => tab.classList.remove('active'));
    document.querySelectorAll('.tab-button').forEach(btn => btn.classList.remove('active'));
    document.getElementById('tab-' + tabName).classList.add('active');
    event.target.classList.add('active');
}

document.getElementById('photoInput').addEventListener('change', function(e) {
    const file = e.target.files[0];
    if (file) {
        if (file.size > MAX_IMAGE_SIZE) {
            showAlert('La imagen no debe superar 5 MB', 'error');
            return;
        }
        const reader = new FileReader();
        reader.onload = function(e) {
            photoBase64 = e.target.result;
            document.getElementById('photoPreview').innerHTML = `<img src="${e.target.result}" style="width:100%;height:100%;object-fit:cover;border-radius:50%;">`;
        };
        reader.readAsDataURL(file);
    }
});

function inicializarDireccionPrincipal() {
    const container = document.getElementById('addressesContainer');
    container.innerHTML = `
        <div class="address-item">
            <div class="address-type">🏠 Casa (Principal)</div>
            <div class="form-row two-cols" style="margin-top: 15px;">
                <div class="form-group">
                    <label class="form-label">Dirección Completa</label>
                    <input type="text" class="form-input" name="address[]" placeholder="Av. Principal 123">
                </div>
                <div class="form-group">
                    <label class="form-label">Distrito</label>
                    <select class="form-input" name="district[]">
                        <option value="">Seleccionar</option>
                        <option value="miraflores">Miraflores</option>
                        <option value="san_isidro">San Isidro</option>
                        <option value="surco">Santiago de Surco</option>
                        <option value="la_molina">La Molina</option>
                        <option value="san_borja">San Borja</option>
                        <option value="surquillo">Surquillo</option>
                        <option value="barranco">Barranco</option>
                        <option value="chorrillos">Chorrillos</option>
                    </select>
                </div>
            </div>
            <div class="form-group">
                <label class="form-label">Referencias</label>
                <input type="text" class="form-input" name="reference[]" placeholder="Cerca del parque">
            </div>
        </div>
    `;
    addressCount = 1;
}

function addAddress() {
    if (addressCount >= MAX_ADDRESSES) {
        showAlert(`Máximo ${MAX_ADDRESSES} direcciones`, 'error');
        return;
    }
    addressCount++;
    const container = document.getElementById('addressesContainer');
    const types = ['🏢 Oficina', '🏪 Trabajo', '📍 Otra'];
    const type = types[addressCount - 2] || '📍 Dirección ' + addressCount;

    const newAddress = document.createElement('div');
    newAddress.className = 'address-item';
    newAddress.innerHTML = `
        <div class="address-type">${type}</div>
        <div class="form-row two-cols" style="margin-top: 15px;">
            <div class="form-group">
                <label class="form-label">Dirección Completa</label>
                <input type="text" class="form-input" name="address[]" placeholder="Av. Principal 123">
            </div>
            <div class="form-group">
                <label class="form-label">Distrito</label>
                <select class="form-input" name="district[]">
                    <option value="">Seleccionar</option>
                    <option value="miraflores">Miraflores</option>
                    <option value="san_isidro">San Isidro</option>
                    <option value="surco">Santiago de Surco</option>
                    <option value="la_molina">La Molina</option>
                    <option value="san_borja">San Borja</option>
                    <option value="surquillo">Surquillo</option>
                    <option value="barranco">Barranco</option>
                    <option value="chorrillos">Chorrillos</option>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label class="form-label">Referencias</label>
            <input type="text" class="form-input" name="reference[]" placeholder="Cerca del parque">
        </div>
        <button type="button" class="btn-remove" onclick="removeAddress(this)">✕</button>
    `;
    container.appendChild(newAddress);
    if (addressCount >= MAX_ADDRESSES) {
        document.getElementById('addAddressBtn').style.display = 'none';
    }
}

function removeAddress(button) {
    button.parentElement.remove();
    addressCount--;
    document.getElementById('addAddressBtn').style.display = 'block';
}

function toggleSwitch(element) {
    element.classList.toggle('active');
}

function updateRangeValue(slider) {
    document.getElementById('rangeValue').textContent = slider.value + ' km';
}

function validateForm() {
    let isValid = true;
    const errors = [];

    // Validar campos requeridos
    document.querySelectorAll('[required]').forEach(field => {
        if (!field.value.trim()) {
            field.style.borderColor = 'var(--error-color)';
            errors.push(`El campo "${field.previousElementSibling?.textContent || 'requerido'}" es obligatorio`);
            isValid = false;
        } else {
            field.style.borderColor = 'var(--border-color)';
        }
    });

    // Validar teléfono
    const phone = document.getElementById('phone');
    if (phone.value && !/^[0-9]{9}$/.test(phone.value)) {
        phone.style.borderColor = 'var(--error-color)';
        errors.push('El teléfono debe tener exactamente 9 dígitos numéricos');
        isValid = false;
    }

    // Validar email
    const email = document.getElementById('email');
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (email.value && !emailRegex.test(email.value)) {
        email.style.borderColor = 'var(--error-color)';
        errors.push('El formato del correo electrónico no es válido');
        isValid = false;
    }

    // Validar que haya al menos una dirección
    const addressInputs = document.querySelectorAll('input[name="address[]"]');
    let hasValidAddress = false;
    addressInputs.forEach(addr => {
        if (addr.value.trim()) hasValidAddress = true;
    });

    if (!hasValidAddress) {
        errors.push('Debe proporcionar al menos una dirección');
        isValid = false;
    }

    if (errors.length > 0) {
        showAlert('<strong>Errores de validación:</strong><br>' + errors.join('<br>'), 'error');
    }

    return isValid;
}

function collectFormData() {
    const categorias = Array.from(document.querySelectorAll('input[name="categoria"]:checked'))
        .map(cb => cb.value);

    const data = {
        nombreCompleto: document.getElementById('fullName').value.trim(),
        email: document.getElementById('email').value.trim(),
        telefono: document.getElementById('phone').value.trim(),
        fotoPerfilUrl: photoBase64,
        categoriasFavoritas: categorias.join(','),
        radioBusqueda: parseInt(document.getElementById('searchRadius').value),
        presupuestoPromedio: parseFloat(document.getElementById('averageBudget').value) || null,
        notificacionesEmail: document.getElementById('emailNotifications').classList.contains('active'),
        notificacionesPush: document.getElementById('pushNotifications').classList.contains('active'),
        notificacionesPromociones: document.getElementById('promotionNotifications').classList.contains('active'),
        notificacionesResenas: document.getElementById('reviewNotifications').classList.contains('active'),
        perfilVisible: document.getElementById('profileVisible').classList.contains('active'),
        compartirUbicacion: document.getElementById('shareLocation').classList.contains('active'),
        historialPublico: document.getElementById('publicHistory').classList.contains('active'),
        direcciones: []
    };

    const addressInputs = document.querySelectorAll('input[name="address[]"]');
    const districtInputs = document.querySelectorAll('select[name="district[]"]');
    const referenceInputs = document.querySelectorAll('input[name="reference[]"]');

    for (let i = 0; i < addressInputs.length; i++) {
        if (addressInputs[i].value.trim()) {
            data.direcciones.push({
                direccionCompleta: addressInputs[i].value.trim(),
                distrito: districtInputs[i].value,
                referencias: referenceInputs[i].value.trim(),
                tipo: i === 0 ? 'PRINCIPAL' : 'ADICIONAL',
                esPrincipal: i === 0
            });
        }
    }

    return data;
}

async function enviarDatos(data) {
    const url = clienteId ? `${BASE_URL}/${clienteId}` : BASE_URL;
    const method = clienteId ? 'PUT' : 'POST';

    console.log('=== Enviando datos ===');
    console.log('URL:', url);
    console.log('Method:', method);
    console.log('Data:', JSON.stringify(data, null, 2));

    try {
        const response = await fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        });

        console.log('Response status:', response.status);
        console.log('Response headers:', response.headers);

        // Intentar parsear la respuesta
        let result;
        const contentType = response.headers.get("content-type");

        if (contentType && contentType.includes("application/json")) {
            result = await response.json();
            console.log('Response JSON:', result);
        } else {
            const text = await response.text();
            console.error('Response no es JSON:', text);
            throw new Error('La respuesta del servidor no es JSON válido: ' + text);
        }

        if (response.ok && result.success) {
            showAlert(`✅ ${clienteId ? 'Actualizado' : 'Registrado'} exitosamente`, 'success');
            if (!clienteId && result.data && result.data.id) {
                setTimeout(() => {
                    window.location.href = `perfil_cliente.html?id=${result.data.id}`;
                }, 2000);
            }
        } else {
            // Mostrar error específico del servidor
            const errorMsg = result.error
                ? `${result.error.codigo}: ${result.error.mensaje}`
                : result.message || 'Error desconocido';
            throw new Error(errorMsg);
        }
    } catch (error) {
        console.error('Error completo:', error);
        showAlert('❌ Error: ' + error.message, 'error');
    }
}

document.getElementById('clientProfileForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    if (!validateForm()) return;
    showAlert('Guardando...', 'info');
    const formData = collectFormData();
    await enviarDatos(formData);
});

function cancelForm() {
    if (confirm('¿Cancelar? Los cambios se perderán.')) {
        window.location.href = 'dashboard.html';
    }
}

function exportData() {
    const formData = collectFormData();
    const blob = new Blob([JSON.stringify(formData, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `perfil_${Date.now()}.json`;
    a.click();
    URL.revokeObjectURL(url);
    showAlert('📥 Datos exportados', 'success');
}

function showAlert(message, type) {
    const container = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = message;
    container.innerHTML = '';
    container.appendChild(alert);
    alert.style.display = 'block';
    if (type === 'success') {
        setTimeout(() => { alert.style.display = 'none'; }, 5000);
    }
}

async function cargarDatosUsuario() {
    try {
        // Obtener datos del usuario desde localStorage
        const userData = JSON.parse(localStorage.getItem('userData') || '{}');

        if (!userData.email) {
            showAlert('⚠️ No se encontró información del usuario. Redirigiendo al login...', 'error');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 2000);
            return;
        }

        // Intentar buscar el perfil de cliente existente por email
        try {
            const response = await fetch(`${BASE_URL}?email=${encodeURIComponent(userData.email)}`);
            const result = await response.json();

            if (result.success && result.data && result.data.id) {
                // Cliente encontrado - cargar todos los datos
                cargarDatosCliente(result.data);
                clienteId = result.data.id;
                document.querySelector('.header h1').textContent = '✏️ Editar Perfil de Cliente';
            } else if (result.success && result.data && result.data.encontrado === false) {
                // Cliente no encontrado - pre-llenar con datos del usuario
                preLlenarConDatosUsuario(userData);
                document.querySelector('.header h1').textContent = '📋 Completar Perfil de Cliente';
                showAlert('ℹ️ Complete su perfil de cliente con información adicional', 'info');
            } else {
                // Error inesperado
                preLlenarConDatosUsuario(userData);
            }
        } catch (error) {
            console.error('Error al buscar cliente:', error);
            // En caso de error, pre-llenar con datos del usuario
            preLlenarConDatosUsuario(userData);
        }

        // El email siempre debe ser readonly ya que viene del usuario
        document.getElementById('email').setAttribute('readonly', true);
        document.getElementById('email').style.backgroundColor = '#e9ecef';

    } catch (error) {
        console.error('Error al cargar datos:', error);
        showAlert('❌ Error al cargar la información', 'error');
    }
}

function preLlenarConDatosUsuario(userData) {
    // Pre-llenar solo los datos básicos del usuario
    document.getElementById('fullName').value = userData.nombre || '';
    document.getElementById('email').value = userData.email || '';
    document.getElementById('phone').value = userData.telefono || '';

    console.log('✓ Datos del usuario pre-llenados');
}

function cargarDatosCliente(cliente) {
    console.log('Cargando datos del cliente:', cliente);

    // Datos personales
    document.getElementById('fullName').value = cliente.nombreCompleto || '';
    document.getElementById('email').value = cliente.email || '';
    document.getElementById('phone').value = cliente.telefono || '';

    // Foto de perfil
    if (cliente.fotoPerfilUrl) {
        const previewImg = document.getElementById('previewImg');
        const uploadPlaceholder = document.querySelector('.upload-placeholder');

        previewImg.src = cliente.fotoPerfilUrl;
        previewImg.style.display = 'block';
        if (uploadPlaceholder) uploadPlaceholder.style.display = 'none';

        photoBase64 = cliente.fotoPerfilUrl;
    }

    // ✅ CATEGORÍAS FAVORITAS - CORREGIDO
    if (cliente.categoriasFavoritas) {
        console.log('Categorías recibidas:', cliente.categoriasFavoritas);

        // Limpiar checkboxes previos
        document.querySelectorAll('input[name="categoria"]').forEach(cb => cb.checked = false);

        // Manejar si es array o string
        let categorias = [];
        if (Array.isArray(cliente.categoriasFavoritas)) {
            categorias = cliente.categoriasFavoritas;
        } else if (typeof cliente.categoriasFavoritas === 'string') {
            categorias = cliente.categoriasFavoritas.split(',').map(c => c.trim());
        }

        console.log('Categorías procesadas:', categorias);

        // Marcar checkboxes
        categorias.forEach(cat => {
            // Limpiar comillas extras y espacios
            const cleanCat = String(cat).replace(/^["']|["']$/g, '').trim();

            if (!cleanCat) return; // Saltar si está vacío

            const checkbox = document.querySelector(`input[name="categoria"][value="${cleanCat}"]`);
            if (checkbox) {
                checkbox.checked = true;
                console.log(`✓ Categoría marcada: ${cleanCat}`);
            } else {
                console.warn(`⚠ Checkbox no encontrado para categoría: "${cleanCat}"`);
                console.log(`Checkboxes disponibles:`,
                    Array.from(document.querySelectorAll('input[name="categoria"]'))
                        .map(cb => cb.value)
                );
            }
        });
    }

 	// ✅ PREFERENCIAS - CON VALIDACIÓN
    const searchRadius = document.getElementById('searchRadius');
    const rangeValue = document.getElementById('rangeValue');
    const averageBudget = document.getElementById('averageBudget');

    if (searchRadius) searchRadius.value = cliente.radioBusqueda || 10;
    if (rangeValue) rangeValue.textContent = cliente.radioBusqueda || 10;
    if (averageBudget) averageBudget.value = cliente.presupuestoPromedio || '';

    // ✅ NOTIFICACIONES - CON VALIDACIÓN
    const emailNotif = document.getElementById('emailNotif');
    const pushNotif = document.getElementById('pushNotif');
    const promoNotif = document.getElementById('promoNotif');
    const reviewNotif = document.getElementById('reviewNotif');

    if (emailNotif) emailNotif.checked = cliente.notificacionesEmail !== false;
    if (pushNotif) pushNotif.checked = cliente.notificacionesPush !== false;
    if (promoNotif) promoNotif.checked = cliente.notificacionesPromociones !== false;
    if (reviewNotif) reviewNotif.checked = cliente.notificacionesResenas !== false;

    // ✅ PRIVACIDAD - CON VALIDACIÓN
    const profileVisible = document.getElementById('profileVisible');
    const shareLocation = document.getElementById('shareLocation');
    const publicHistory = document.getElementById('publicHistory');

    if (profileVisible) profileVisible.checked = cliente.perfilVisible !== false;
    if (shareLocation) shareLocation.checked = cliente.compartirUbicacion === true;
    if (publicHistory) publicHistory.checked = cliente.historialPublico === true;

    // Direcciones
    if (cliente.direcciones && cliente.direcciones.length > 0) {
        cargarDirecciones(cliente.direcciones);
    }

    console.log('✓ Datos del cliente cargados completamente');

}

function cargarDirecciones(direcciones) {
    const container = document.getElementById('addressesContainer');
    container.innerHTML = '';
    addressCount = 0;

    direcciones.forEach((dir, index) => {
        if (index === 0) {
            // Primera dirección (principal)
            container.innerHTML = `
                <div class="address-item">
                    <div class="address-type">🏠 Casa (Principal)</div>
                    <div class="form-row two-cols" style="margin-top: 15px;">
                        <div class="form-group">
                            <label class="form-label">Dirección Completa</label>
                            <input type="text" class="form-input" name="address[]" value="${dir.direccionCompleta || ''}" placeholder="Av. Principal 123">
                        </div>
                        <div class="form-group">
                            <label class="form-label">Distrito</label>
                            <select class="form-input" name="district[]">
                                <option value="">Seleccionar</option>
                                <option value="miraflores" ${dir.distrito === 'miraflores' ? 'selected' : ''}>Miraflores</option>
                                <option value="san_isidro" ${dir.distrito === 'san_isidro' ? 'selected' : ''}>San Isidro</option>
                                <option value="surco" ${dir.distrito === 'surco' ? 'selected' : ''}>Santiago de Surco</option>
                                <option value="la_molina" ${dir.distrito === 'la_molina' ? 'selected' : ''}>La Molina</option>
                                <option value="san_borja" ${dir.distrito === 'san_borja' ? 'selected' : ''}>San Borja</option>
                                <option value="surquillo" ${dir.distrito === 'surquillo' ? 'selected' : ''}>Surquillo</option>
                                <option value="barranco" ${dir.distrito === 'barranco' ? 'selected' : ''}>Barranco</option>
                                <option value="chorrillos" ${dir.distrito === 'chorrillos' ? 'selected' : ''}>Chorrillos</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Referencias</label>
                        <input type="text" class="form-input" name="reference[]" value="${dir.referencias || ''}" placeholder="Cerca del parque">
                    </div>
                </div>
            `;
            addressCount = 1;
        } else {
            // Direcciones adicionales
            const types = ['🏢 Oficina', '🏪 Trabajo', '📍 Otra'];
            const type = types[index - 1] || '📍 Dirección ' + (index + 1);

            const newAddress = document.createElement('div');
            newAddress.className = 'address-item';
            newAddress.innerHTML = `
                <div class="address-type">${type}</div>
                <div class="form-row two-cols" style="margin-top: 15px;">
                    <div class="form-group">
                        <label class="form-label">Dirección Completa</label>
                        <input type="text" class="form-input" name="address[]" value="${dir.direccionCompleta || ''}" placeholder="Av. Principal 123">
                    </div>
                    <div class="form-group">
                        <label class="form-label">Distrito</label>
                        <select class="form-input" name="district[]">
                            <option value="">Seleccionar</option>
                            <option value="miraflores" ${dir.distrito === 'miraflores' ? 'selected' : ''}>Miraflores</option>
                            <option value="san_isidro" ${dir.distrito === 'san_isidro' ? 'selected' : ''}>San Isidro</option>
                            <option value="surco" ${dir.distrito === 'surco' ? 'selected' : ''}>Santiago de Surco</option>
                            <option value="la_molina" ${dir.distrito === 'la_molina' ? 'selected' : ''}>La Molina</option>
                            <option value="san_borja" ${dir.distrito === 'san_borja' ? 'selected' : ''}>San Borja</option>
                            <option value="surquillo" ${dir.distrito === 'surquillo' ? 'selected' : ''}>Surquillo</option>
                            <option value="barranco" ${dir.distrito === 'barranco' ? 'selected' : ''}>Barranco</option>
                            <option value="chorrillos" ${dir.distrito === 'chorrillos' ? 'selected' : ''}>Chorrillos</option>
                        </select>
                    </div>
                </div>
                <div class="form-group">
                    <label class="form-label">Referencias</label>
                    <input type="text" class="form-input" name="reference[]" value="${dir.referencias || ''}" placeholder="Cerca del parque">
                </div>
                <button type="button" class="btn-remove" onclick="removeAddress(this)">✕</button>
            `;
            container.appendChild(newAddress);
            addressCount++;
        }
    });

    if (addressCount >= MAX_ADDRESSES) {
        document.getElementById('addAddressBtn').style.display = 'none';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    // Inicializar dirección principal vacía
    inicializarDireccionPrincipal();

    // Cargar datos del usuario o cliente
    cargarDatosUsuario();
});

console.log('✅ App configurada - Base URL:', BASE_URL);
