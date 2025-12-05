/**
 * Detalle de Trabajo JavaScript
 * Gestiona la visualización y actualización de solicitudes de servicio para profesionales
 *
 * CREADO: 2025-12-03
 * FUNCIONALIDADES:
 * - Carga de información detallada de la solicitud
 * - Sistema de transición de estados con validación
 * - Modal de confirmación
 * - Logging completo de todas las acciones
 */

// ============================================
// VARIABLES GLOBALES
// ============================================
let userData = {};
let solicitudActual = null;
let solicitudId = null;
let accionPendiente = null;

// ============================================
// INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', async () => {
  console.log('🚀 Inicializando detalle-trabajo.html');

  // ✅ PASO 1: Cargar datos del usuario
  userData = JSON.parse(localStorage.getItem('userData') || '{}');

  if (!userData.id) {
    console.error('❌ Usuario no autenticado');
    alert('Sesión no válida. Por favor inicie sesión nuevamente.');
    window.location.href = 'login.html';
    return;
  }

  console.log('✅ Usuario autenticado:', userData.id);

  // Mostrar nombre de usuario
  const nombreMostrar = userData.nombreCompleto || userData.nombre || 'Usuario';
  document.getElementById('userName').textContent = nombreMostrar;

  // ✅ PASO 2: Obtener ID de solicitud desde query params
  const urlParams = new URLSearchParams(window.location.search);
  solicitudId = urlParams.get('id');

  if (!solicitudId) {
    console.error('❌ No se proporcionó ID de solicitud');
    mostrarError('No se especificó la solicitud a mostrar');
    return;
  }

  console.log('📋 ID de solicitud:', solicitudId);

  // ✅ PASO 3: Cargar solicitud
  await cargarSolicitud();
});

// ============================================
// CARGA DE DATOS
// ============================================
/**
 * Carga la información completa de la solicitud desde el backend.
 * Valida permisos y renderiza la UI con los datos obtenidos.
 *
 * ACTUALIZADO 2025-12-04: Corregido para usar profesionalId en lugar de usuarioId
 */
async function cargarSolicitud() {
  console.log('📡 Cargando solicitud...');

  mostrarLoading();

  try {
    // ✅ ACTUALIZADO 2025-12-04: Usar profesionalId del localStorage
    const profesionalId = userData.profesionalId || userData.id;

    if (!profesionalId) {
      throw new Error('No se pudo obtener el ID del profesional');
    }

    console.log('👤 Usuario ID:', userData.id);
    console.log('👤 Profesional ID:', profesionalId);
    console.log('📋 Solicitud ID:', solicitudId);

    // ✅ Fetch solicitud desde API con profesionalId (no usuarioId)
    const response = await fetch(`./api/solicitudes/${solicitudId}?tipo=profesional&usuarioId=${profesionalId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include'
    });

    console.log('📡 Response status:', response.status);

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('No tienes autorización para ver esta solicitud');
      } else if (response.status === 404) {
        throw new Error('Solicitud no encontrada');
      } else {
        const errorText = await response.text();
        console.error('❌ Error response:', errorText);
        throw new Error('Error al cargar la solicitud');
      }
    }

    const result = await response.json();
    console.log('📦 Response completo:', result);

    solicitudActual = result.data?.solicitud;

    if (!solicitudActual) {
      throw new Error('No se recibió información de la solicitud');
    }

    console.log('✅ Solicitud cargada:', solicitudActual);

    // ✅ ACTUALIZADO 2025-12-04: Validar usando profesionalId correcto
    const miProfesionalId = userData.profesionalId || userData.id;
    if (solicitudActual.profesionalId !== miProfesionalId) {
      console.error('❌ Usuario no es el profesional de esta solicitud');
      console.error('   Solicitud pertenece a profesional:', solicitudActual.profesionalId);
      console.error('   Usuario ID:', userData.id);
      console.error('   Profesional ID:', miProfesionalId);
      throw new Error('No tienes permiso para ver esta solicitud');
    }

    console.log('✅ Validación de permisos exitosa');

    // ✅ Renderizar información
    renderizarSolicitud();

    // ✅ Mostrar contenido
    ocultarLoading();
    document.getElementById('contentContainer').style.display = 'block';

  } catch (error) {
    console.error('❌ Error al cargar solicitud:', error);
    mostrarError(error.message);
  }
}

// ============================================
// RENDERIZADO DE UI
// ============================================
/**
 * Renderiza toda la información de la solicitud en la UI.
 * Incluye datos básicos, ubicación, y gestión de estados.
 */
function renderizarSolicitud() {
  console.log('🎨 Renderizando solicitud en UI');

  // ✅ Código de solicitud
  const codigo = `SR-${new Date(solicitudActual.fechaSolicitud).getFullYear()}-${String(solicitudActual.id).padStart(6, '0')}`;
  document.getElementById('codigoSolicitud').textContent = codigo;

  // ✅ Estado actual con badge
  const estadoBadge = generarBadgeEstado(solicitudActual.estado);
  document.getElementById('estadoActual').innerHTML = estadoBadge;

  // ✅ Información del Cliente
  document.getElementById('clienteNombre').textContent = solicitudActual.clienteNombreCompleto || '-';
  document.getElementById('clienteEmail').textContent = solicitudActual.clienteEmail || '-';
  document.getElementById('clienteTelefono').textContent = solicitudActual.clienteTelefono || '-';

  // ✅ Información básica
  document.getElementById('fechaSolicitud').textContent = formatearFecha(solicitudActual.fechaSolicitud);
  document.getElementById('fechaServicio').textContent = formatearFecha(solicitudActual.fechaServicio);
  document.getElementById('urgencia').innerHTML = solicitudActual.urgencia === 'urgent'
    ? '<span class="badge badge-danger">🔥 Urgente</span>'
    : '<span class="badge badge-info">📅 Normal</span>';
  document.getElementById('presupuesto').textContent = `S/ ${solicitudActual.presupuestoEstimado?.toFixed(2) || '0.00'}`;
  document.getElementById('modalidad').innerHTML = solicitudActual.tipoPrestacion === 'PRESENCIAL'
    ? '<span class="badge badge-info">📍 Presencial</span>'
    : '<span class="badge badge-success">💻 Remoto</span>';

  // ✅ Descripción
  document.getElementById('descripcion').textContent = solicitudActual.descripcion || '-';

  // ✅ Ubicación completa (solo si es presencial)
  if (solicitudActual.tipoPrestacion === 'PRESENCIAL') {
    document.getElementById('ubicacionSection').style.display = 'block';
    document.getElementById('departamentoNombre').textContent = solicitudActual.departamentoNombre || '-';
    document.getElementById('provinciaNombre').textContent = solicitudActual.provinciaNombre || '-';
    document.getElementById('distritoNombre').textContent = solicitudActual.distritoNombre || '-';
    document.getElementById('direccion').textContent = solicitudActual.direccion || '-';
    document.getElementById('referencia').textContent = solicitudActual.referencia || '-';
    document.getElementById('codigoPostal').textContent = solicitudActual.codigoPostal || '-';
  }

  // ✅ Imágenes adjuntas
  if (solicitudActual.fotosUrls && solicitudActual.fotosUrls.length > 0) {
    document.getElementById('imagenesSection').style.display = 'block';
    renderizarImagenes(solicitudActual.fotosUrls);
  }

  // ✅ Notas adicionales
  if (solicitudActual.notasAdicionales) {
    document.getElementById('notasSection').style.display = 'block';
    document.getElementById('notasAdicionales').textContent = solicitudActual.notasAdicionales;
  }

  // ✅ Renderizar acciones según estado
  renderizarAcciones();
}

/**
 * Renderiza las imágenes adjuntas en una grilla responsive.
 * @param {Array<string>} fotosUrls - Array de URLs de las imágenes
 */
function renderizarImagenes(fotosUrls) {
  console.log('📷 Renderizando imágenes adjuntas');

  const imagenesGrid = document.getElementById('imagenesGrid');

  if (!fotosUrls || fotosUrls.length === 0) {
    imagenesGrid.innerHTML = '<p class="no-images">No hay imágenes adjuntas</p>';
    return;
  }

  const imagenesHTML = fotosUrls.map((url, index) => `
    <div class="imagen-item">
      <img src="${url}"
           alt="Imagen adjunta ${index + 1}"
           onclick="verImagenCompleta('${url}')"
           loading="lazy">
    </div>
  `).join('');

  imagenesGrid.innerHTML = imagenesHTML;
  console.log(`✅ ${fotosUrls.length} imágenes renderizadas`);
}

/**
 * Abre la imagen en tamaño completo en una nueva pestaña.
 * @param {string} url - URL de la imagen a visualizar
 */
function verImagenCompleta(url) {
  window.open(url, '_blank');
}

/**
 * Renderiza los botones de acción disponibles según el estado actual.
 * Los estados finales no permiten acciones.
 */
function renderizarAcciones() {
  console.log('🎯 Renderizando acciones disponibles');

  const estado = solicitudActual.estado.toLowerCase();
  document.getElementById('estadoTexto').textContent = estado.charAt(0).toUpperCase() + estado.slice(1);

  // ✅ Definir estados finales
  const estadosFinales = ['rechazada', 'completada', 'cancelada'];

  if (estadosFinales.includes(estado)) {
    // Estado final: no se pueden hacer cambios
    console.log('ℹ️ Estado final - no hay acciones disponibles');
    document.getElementById('accionesCard').style.display = 'none';
    document.getElementById('estadoFinalCard').style.display = 'block';
    return;
  }

  // ✅ Obtener estados disponibles según reglas de negocio
  const estadosDisponibles = obtenerEstadosDisponibles(estado);

  console.log('📊 Estados disponibles:', estadosDisponibles);

  if (estadosDisponibles.length === 0) {
    document.getElementById('accionesCard').style.display = 'none';
    document.getElementById('estadoFinalCard').style.display = 'block';
    return;
  }

  // ✅ Renderizar botones
  const botonesHTML = estadosDisponibles.map(nuevoEstado => {
    const config = configuracionBotones[nuevoEstado.toLowerCase()];
    return `
      <button class="btn-action ${config.clase}" onclick="solicitarCambioEstado('${nuevoEstado}')">
        ${config.icono} ${config.texto}
      </button>
    `;
  }).join('');

  document.getElementById('botonesAccion').innerHTML = botonesHTML;
  document.getElementById('accionesCard').style.display = 'block';
  document.getElementById('estadoFinalCard').style.display = 'none';
}

/**
 * Configuración de botones por estado.
 * Define el texto, icono y clase CSS para cada acción.
 */
const configuracionBotones = {
  'aceptada': {
    texto: 'Aceptar Solicitud',
    icono: '✓',
    clase: 'btn-success'
  },
  'rechazada': {
    texto: 'Rechazar Solicitud',
    icono: '✗',
    clase: 'btn-danger'
  },
  'completada': {
    texto: 'Marcar como Completada',
    icono: '✔',
    clase: 'btn-success'
  },
  'cancelada': {
    texto: 'Cancelar Trabajo',
    icono: '⊘',
    clase: 'btn-secondary'
  }
};

/**
 * Obtiene los estados disponibles según el estado actual.
 * Implementa las reglas de transición de estados.
 *
 * REGLAS:
 * - PENDIENTE → ACEPTADA o RECHAZADA
 * - ACEPTADA → COMPLETADA o CANCELADA
 */
function obtenerEstadosDisponibles(estadoActual) {
  const transiciones = {
    'pendiente': ['ACEPTADA', 'RECHAZADA'],
    'aceptada': ['COMPLETADA', 'CANCELADA'],
    'rechazada': [],
    'completada': [],
    'cancelada': []
  };

  return transiciones[estadoActual.toLowerCase()] || [];
}

// ============================================
// CAMBIO DE ESTADO
// ============================================
/**
 * Solicita confirmación para cambiar el estado de la solicitud.
 * Muestra un modal de confirmación antes de ejecutar el cambio.
 */
function solicitarCambioEstado(nuevoEstado) {
  console.log(`🔄 Solicitando cambio de estado a: ${nuevoEstado}`);

  accionPendiente = nuevoEstado;

  // Configurar modal
  const mensajes = {
    'ACEPTADA': '¿Deseas aceptar esta solicitud de servicio? El cliente será notificado.',
    'RECHAZADA': '¿Estás seguro de rechazar esta solicitud? Esta acción no se puede deshacer.',
    'COMPLETADA': '¿Confirmas que el servicio ha sido completado satisfactoriamente?',
    'CANCELADA': '¿Deseas cancelar este trabajo? El cliente será notificado.'
  };

  const titulos = {
    'ACEPTADA': 'Aceptar Solicitud',
    'RECHAZADA': 'Rechazar Solicitud',
    'COMPLETADA': 'Marcar como Completada',
    'CANCELADA': 'Cancelar Trabajo'
  };

  document.getElementById('modalTitle').textContent = titulos[nuevoEstado] || 'Confirmar Acción';
  document.getElementById('modalMessage').textContent = mensajes[nuevoEstado] || '¿Confirmas esta acción?';

  // Configurar botón según acción
  const btnConfirmar = document.getElementById('btnConfirmar');
  btnConfirmar.className = 'btn-confirm';
  if (nuevoEstado === 'RECHAZADA' || nuevoEstado === 'CANCELADA') {
    btnConfirmar.classList.add('btn-danger');
  }

  // Mostrar modal
  document.getElementById('confirmModal').style.display = 'flex';
}

/**
 * Ejecuta el cambio de estado después de la confirmación del usuario.
 * Realiza el PUT request al backend y actualiza la UI.
 *
 * ACTUALIZADO 2025-12-04: Corregido orden - guardar accionPendiente antes de cerrar modal
 */
async function confirmarAccion() {
  console.log(`✅ Confirmando cambio de estado a: ${accionPendiente}`);

  // ✅ ACTUALIZADO 2025-12-04: Guardar valor ANTES de cerrar modal (que limpia accionPendiente)
  if (!accionPendiente) {
    console.error('❌ No hay acción pendiente');
    cerrarModal();
    return;
  }

  const nuevoEstado = accionPendiente;
  accionPendiente = null;

  // ✅ Cerrar modal DESPUÉS de guardar el estado
  cerrarModal();

  // Mostrar loading
  mostrarLoadingEnBotones();

  try {
    // ✅ ACTUALIZADO 2025-12-04: Enviar profesionalId como query parameter
    const profesionalId = userData.profesionalId || userData.id;
    const response = await fetch(`./api/solicitudes/${solicitudId}/estado?usuarioId=${profesionalId}`, {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json'
      },
      credentials: 'include',
      body: JSON.stringify({
        nuevoEstado: nuevoEstado
      })
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Error al actualizar el estado');
    }

    const result = await response.json();

    console.log('✅ Estado actualizado exitosamente:', result);

    // Mostrar mensaje de éxito
    mostrarMensajeExito(`Estado actualizado a "${nuevoEstado}" exitosamente`);

    // Recargar solicitud para reflejar cambios
    setTimeout(() => {
      cargarSolicitud();
    }, 1500);

  } catch (error) {
    console.error('❌ Error al actualizar estado:', error);
    mostrarMensajeError(error.message);
    ocultarLoadingEnBotones();
  }
}

// ============================================
// UTILIDADES
// ============================================
/**
 * Genera HTML de badge según el estado.
 */
function generarBadgeEstado(estado) {
  const badges = {
    'pendiente': '<span class="badge badge-warning">⏳ Pendiente</span>',
    'aceptada': '<span class="badge badge-info">✓ Aceptada</span>',
    'rechazada': '<span class="badge badge-danger">✗ Rechazada</span>',
    'completada': '<span class="badge badge-success">✔ Completada</span>',
    'cancelada': '<span class="badge badge-secondary">⊘ Cancelada</span>'
  };
  return badges[estado?.toLowerCase()] || `<span class="badge badge-light">${estado}</span>`;
}

/**
 * Formatea fecha a formato legible en español.
 */
function formatearFecha(fechaISO) {
  if (!fechaISO) return '-';
  const fecha = new Date(fechaISO);
  return fecha.toLocaleDateString('es-PE', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// ============================================
// ESTADOS DE UI
// ============================================
function mostrarLoading() {
  document.getElementById('loadingState').style.display = 'flex';
  document.getElementById('errorState').style.display = 'none';
  document.getElementById('contentContainer').style.display = 'none';
}

function ocultarLoading() {
  document.getElementById('loadingState').style.display = 'none';
}

function mostrarError(mensaje) {
  document.getElementById('errorMessage').textContent = mensaje;
  document.getElementById('loadingState').style.display = 'none';
  document.getElementById('errorState').style.display = 'flex';
  document.getElementById('contentContainer').style.display = 'none';
}

function mostrarLoadingEnBotones() {
  const botones = document.querySelectorAll('.btn-action');
  botones.forEach(btn => {
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-small"></span> Procesando...';
  });
}

function ocultarLoadingEnBotones() {
  renderizarAcciones();
}

function mostrarMensajeExito(mensaje) {
  // Crear toast de éxito
  const toast = document.createElement('div');
  toast.className = 'toast toast-success';
  toast.innerHTML = `
    <div class="toast-icon">✓</div>
    <div class="toast-message">${mensaje}</div>
  `;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('show');
  }, 100);

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

function mostrarMensajeError(mensaje) {
  // Crear toast de error
  const toast = document.createElement('div');
  toast.className = 'toast toast-error';
  toast.innerHTML = `
    <div class="toast-icon">✗</div>
    <div class="toast-message">${mensaje}</div>
  `;
  document.body.appendChild(toast);

  setTimeout(() => {
    toast.classList.add('show');
  }, 100);

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}

// ============================================
// MODAL
// ============================================
function cerrarModal() {
  document.getElementById('confirmModal').style.display = 'none';
  accionPendiente = null;
}

// Cerrar modal al presionar ESC
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    cerrarModal();
  }
});

// ============================================
// NAVEGACIÓN
// ============================================
function volverADashboard() {
  window.location.href = 'dashboard.html';
}

function logout() {
  console.log('👋 Cerrando sesión');
  localStorage.removeItem('userData');
  localStorage.removeItem('authToken');
  window.location.href = 'login.html';
}
