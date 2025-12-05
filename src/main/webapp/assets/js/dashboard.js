// Dashboard JavaScript - Desacoplado del HTML el 2025-11-21

// ============================================
// VARIABLES GLOBALES
// ============================================
let userData = {};
let currentView = 'inicio';

// ============================================
// INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', async () => {
  // Cargar datos del usuario
  userData = JSON.parse(localStorage.getItem('userData') || '{}');

  if (!userData.id) {
    alert('Sesión no válida. Por favor inicie sesión nuevamente.');
    window.location.href = 'login.html';
    return;
  }

  // Configurar UI según usuario
  setupUserInterface();

  // Cargar vista inicial
  await navigateTo('inicio');
});

// ============================================
// CONFIGURACIÓN DE INTERFAZ
// ============================================
function setupUserInterface() {
  // Mostrar nombre del usuario
  const nombreMostrar = userData.nombreCompleto || userData.nombre || 'Usuario';
  document.getElementById('userName').textContent = nombreMostrar;

  // Detectar y mostrar rol
  const tipoRol = userData.tipoRol || userData.tipo || 'CLIENTE';
  const esCliente = userData.esCliente !== undefined ? userData.esCliente :
                   (tipoRol === 'CLIENTE' || tipoRol === 'AMBOS');
  const esProfesional = userData.esProfesional !== undefined ? userData.esProfesional :
                       (tipoRol === 'PROFESIONAL' || tipoRol === 'AMBOS');

  // Actualizar badge de rol
  let rolTexto = '';
  if (tipoRol === 'AMBOS') {
    rolTexto = '👥 Cliente & Profesional';
  } else if (esProfesional) {
    rolTexto = '🔧 Profesional';
  } else {
    rolTexto = '👤 Cliente';
  }
  document.getElementById('userRole').textContent = rolTexto;

  // Mostrar/ocultar secciones del menú según rol
  document.querySelectorAll('.menu-cliente-section').forEach(el => {
    el.style.display = esCliente ? 'block' : 'none';
  });

  document.querySelectorAll('.menu-profesional-section').forEach(el => {
    el.style.display = esProfesional ? 'block' : 'none';
  });

  console.log('Usuario configurado:', { tipoRol, esCliente, esProfesional });
}

// ============================================
// NAVEGACIÓN
// ============================================
async function navigateTo(view, clickEvent) {
  console.log('🔄 Navegando a vista:', view);
  currentView = view;

  try {
    // Actualizar items activos del menú
    document.querySelectorAll('.menu-item').forEach(item => {
      item.classList.remove('active');
    });

    // Si hay un evento de click, marcar el item como activo
    if (clickEvent && clickEvent.target) {
      const menuItem = clickEvent.target.closest('.menu-item');
      if (menuItem) {
        menuItem.classList.add('active');
      }
    } else {
      // Si no hay evento (llamada programática), buscar el item por el onclick
      const menuItems = document.querySelectorAll('.menu-item');
      menuItems.forEach(item => {
        const onclickAttr = item.getAttribute('onclick');
        if (onclickAttr && onclickAttr.includes(`'${view}'`)) {
          item.classList.add('active');
        }
      });
    }

    // Cerrar menú móvil si está abierto
    closeMobileMenu();

    // Cargar vista
    const contentArea = document.getElementById('contentArea');

    if (!contentArea) {
      console.error('❌ No se encontró el elemento contentArea');
      return;
    }

    console.log('✅ ContentArea encontrado, renderizando vista:', view);

    switch(view) {
      case 'inicio':
        document.getElementById('pageTitle').textContent = 'Dashboard';
        await renderInicio(contentArea);
        break;
      case 'mis-solicitudes':
        document.getElementById('pageTitle').textContent = 'Mis Solicitudes';
        await renderMisSolicitudes(contentArea);
        break;
      case 'mis-trabajos':
        console.log('🎯 Ejecutando renderMisTrabajos...');
        document.getElementById('pageTitle').textContent = 'Mis Trabajos';
        await renderMisTrabajos(contentArea);
        console.log('✅ renderMisTrabajos completado');
        break;
      case 'estadisticas':
        document.getElementById('pageTitle').textContent = 'Estadísticas';
        renderEstadisticas(contentArea);
        break;
      default:
        console.log('⚠️ Vista no reconocida, renderizando inicio');
        renderInicio(contentArea);
    }
  } catch (error) {
    console.error('❌ Error en navigateTo:', error);
    console.error('Stack trace:', error.stack);

    // Mostrar error en UI
    const contentArea = document.getElementById('contentArea');
    if (contentArea) {
      contentArea.innerHTML = `
        <div class="card">
          <div class="card-body">
            <div class="error-state">
              <div class="error-icon">⚠️</div>
              <div class="error-text">
                Error al cargar la vista: ${error.message}
              </div>
              <button class="action-button" onclick="location.reload()">
                Recargar Página
              </button>
            </div>
          </div>
        </div>
      `;
    }
  }
}

// ============================================
// VISTA: INICIO
// ============================================
async function renderInicio(container) {
  const tipoRol = userData.tipoRol || userData.tipo || 'CLIENTE';
  const esCliente = userData.esCliente !== undefined ? userData.esCliente :
                   (tipoRol === 'CLIENTE' || tipoRol === 'AMBOS');
  const esProfesional = userData.esProfesional !== undefined ? userData.esProfesional :
                       (tipoRol === 'PROFESIONAL' || tipoRol === 'AMBOS');

  let html = '';

  // Avisos de completitud de perfil
  html += await generarAvisosCompletitud(esCliente, esProfesional);

  // Avisos de invitación a registrarse en otro rol
  if (tipoRol !== 'AMBOS') {
    if (esCliente && !esProfesional) {
      html += generarAvisoRegistroProfesional();
    } else if (esProfesional && !esCliente) {
      html += generarAvisoRegistroCliente();
    }
  }

  // Contenido según rol
  html += '<div class="grid-2">';

  if (esCliente) {
    html += await generarSeccionSolicitudesRecientes();
  }

  if (esProfesional) {
    html += await generarSeccionTrabajosRecientes();
  }

  html += '</div>';

  // Si no tiene ningún rol (caso edge), mostrar mensaje
  if (!esCliente && !esProfesional) {
    html = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <div class="empty-state-text">
          No se pudo determinar tu rol en el sistema.<br>
          Por favor contacta al administrador.
        </div>
      </div>
    `;
  }

  container.innerHTML = html;
}

// ============================================
// GENERACIÓN DE AVISOS
// ============================================
async function generarAvisosCompletitud(esCliente, esProfesional) {
  let html = '';

  if (esCliente) {
    const porcentajeCliente = await calcularCompletitudCliente();
    if (porcentajeCliente < 100) {
      html += `
        <div class="alert-card warning">
          <div class="alert-header">
            <div class="alert-icon">⚠️</div>
            <div class="alert-title">Completa tu perfil de Cliente</div>
          </div>
          <div class="alert-body">
            Tu perfil está al <strong>${porcentajeCliente}%</strong>.
            Completa tu información para mejorar tu experiencia y recibir mejores servicios.
          </div>
          <div class="progress-bar">
            <div class="progress-fill" style="width: ${porcentajeCliente}%"></div>
          </div>
          <div class="progress-text">${porcentajeCliente}% completado</div>
          <button class="action-button" onclick="goToActualizarCliente()">
            Completar Perfil de Cliente
          </button>
        </div>
      `;
    } else {
      html += `
        <div class="alert-card success">
          <div class="alert-header">
            <div class="alert-icon">✅</div>
            <div class="alert-title">Perfil de Cliente Completo</div>
          </div>
          <div class="alert-body">
            ¡Excelente! Tu perfil de cliente está completo al 100%.
          </div>
          <div class="progress-bar">
            <div class="progress-fill" style="width: 100%"></div>
          </div>
        </div>
      `;
    }
  }

  if (esProfesional) {
    const porcentajeProfesional = await calcularCompletitudProfesional();
    if (porcentajeProfesional < 100) {
      html += `
        <div class="alert-card warning">
          <div class="alert-header">
            <div class="alert-icon">🔧</div>
            <div class="alert-title">Completa tu perfil Profesional</div>
          </div>
          <div class="alert-body">
            Tu perfil profesional está al <strong>${porcentajeProfesional}%</strong>.
            Completa tu información para recibir más solicitudes de servicio.
          </div>
          <div class="progress-bar">
            <div class="progress-fill" style="width: ${porcentajeProfesional}%"></div>
          </div>
          <div class="progress-text">${porcentajeProfesional}% completado</div>
          <button class="action-button" onclick="goToActualizarProfesional()">
            Completar Perfil Profesional
          </button>
        </div>
      `;
    } else {
      html += `
        <div class="alert-card success">
          <div class="alert-header">
            <div class="alert-icon">✅</div>
            <div class="alert-title">Perfil Profesional Completo</div>
          </div>
          <div class="alert-body">
            ¡Excelente! Tu perfil profesional está completo al 100%.
          </div>
          <div class="progress-bar">
            <div class="progress-fill" style="width: 100%"></div>
          </div>
        </div>
      `;
    }
  }

  return html;
}

function generarAvisoRegistroProfesional() {
  return `
    <div class="alert-card info">
      <div class="alert-header">
        <div class="alert-icon">💼</div>
        <div class="alert-title">¿Eres un profesional?</div>
      </div>
      <div class="alert-body">
        Regístrate como profesional y comienza a ofrecer tus servicios.
        Accede a nuevas oportunidades de trabajo y aumenta tus ingresos.
      </div>
      <button class="action-button" onclick="registrarseComoProfesional()">
        Registrarme como Profesional
      </button>
    </div>
  `;
}

function generarAvisoRegistroCliente() {
  return `
    <div class="alert-card info">
      <div class="alert-header">
        <div class="alert-icon">👤</div>
        <div class="alert-title">¿Necesitas servicios?</div>
      </div>
      <div class="alert-body">
        Regístrate también como cliente para poder solicitar servicios profesionales.
        Encuentra expertos para cualquier trabajo que necesites.
      </div>
      <button class="action-button" onclick="registrarseComoCliente()">
        Registrarme como Cliente
      </button>
    </div>
  `;
}

// ============================================
// CÁLCULO DE COMPLETITUD DE PERFILES
// ============================================
async function calcularCompletitudCliente() {
  // TODO: Implementar llamada al API para obtener datos completos del cliente
  // Por ahora, retornamos un cálculo básico

  let completitud = 0;
  let camposTotal = 10; // Campos importantes del perfil
  let camposCompletos = 0;

  // Verificar datos básicos
  if (userData.nombreCompleto) camposCompletos++;
  if (userData.email) camposCompletos++;
  if (userData.telefono) camposCompletos++;
  if (userData.numeroDocumento) camposCompletos++;
  if (userData.tipoDocumento) camposCompletos++;
  if (userData.fechaNacimiento) camposCompletos++;
  if (userData.genero) camposCompletos++;
  if (userData.direccion) camposCompletos++;
  if (userData.departamentoId) camposCompletos++;
  if (userData.distritoId) camposCompletos++;

  completitud = Math.round((camposCompletos / camposTotal) * 100);
  return completitud;
}

async function calcularCompletitudProfesional() {
  // TODO: Implementar llamada al API para obtener datos del profesional
  // Por ahora, retornamos un cálculo simulado

  let completitud = 0;
  let camposTotal = 8;
  let camposCompletos = 0;

  // Verificar datos básicos (estos deberían venir del API)
  if (userData.descripcion) camposCompletos++;
  if (userData.experiencia) camposCompletos++;
  if (userData.especialidadId) camposCompletos++;
  if (userData.habilidades && userData.habilidades.length > 0) camposCompletos++;
  if (userData.certificaciones && userData.certificaciones.length > 0) camposCompletos++;
  if (userData.fotoPerfil) camposCompletos++;
  if (userData.fotoPortada) camposCompletos++;
  if (userData.tarifaHora) camposCompletos++;

  // Si no tenemos estos datos, asumir 50% por defecto
  if (camposCompletos === 0) {
    return 50;
  }

  completitud = Math.round((camposCompletos / camposTotal) * 100);
  return completitud;
}

// ============================================
// SECCIÓN: SOLICITUDES RECIENTES
// ============================================
async function generarSeccionSolicitudesRecientes() {
  try {
    const response = await fetch(`./api/solicitudes?tipo=cliente&usuarioId=${userData.id}`);
    const data = await response.json();

    if (!data.success || !data.data.solicitudes) {
      return generarSeccionVacia('solicitudes');
    }

    const solicitudes = data.data.solicitudes.slice(0, 5);

    if (solicitudes.length === 0) {
      return generarSeccionVacia('solicitudes');
    }

    let html = `
      <div class="card">
        <div class="card-header">
          <div class="card-title">📋 Mis Solicitudes Recientes</div>
          <button class="action-button outline" onclick="navigateTo('mis-solicitudes')">
            Ver Todas
          </button>
        </div>
        <div class="card-body">
    `;

    solicitudes.forEach(solicitud => {
      html += generarItemSolicitud(solicitud);
    });

    html += `
        </div>
      </div>
    `;

    return html;
  } catch (error) {
    console.error('Error al cargar solicitudes:', error);
    return generarSeccionVacia('solicitudes');
  }
}

function generarItemSolicitud(solicitud) {
  const estado = solicitud.estado || 'pendiente';
  const estadoClass = `status-${estado}`;
  const estadoTexto = {
    'pendiente': 'Pendiente',
    'aceptada': 'Aceptada',
    'completada': 'Completada',
    'rechazada': 'Rechazada',
    'cancelada': 'Cancelada'
  };

  const fecha = new Date(solicitud.fechaSolicitud).toLocaleDateString('es-PE', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });

  return `
    <div class="solicitud-item">
      <div class="solicitud-header">
        <div>
          <div class="solicitud-title">
            ${solicitud.descripcion?.substring(0, 60) || 'Sin descripción'}${solicitud.descripcion?.length > 60 ? '...' : ''}
          </div>
          <div class="solicitud-meta">
            📅 ${fecha} • 💰 S/ ${solicitud.presupuestoEstimado?.toFixed(2) || '0.00'}
          </div>
        </div>
        <span class="status-badge ${estadoClass}">
          ${estadoTexto[estado]}
        </span>
      </div>
    </div>
  `;
}

// ============================================
// SECCIÓN: TRABAJOS RECIENTES
// ============================================
async function generarSeccionTrabajosRecientes() {
  try {
    // ✅ ACTUALIZADO 2025-12-04: Usar profesionalId en lugar de usuarioId para búsqueda de trabajos
    const profesionalId = userData.profesionalId || userData.id;
    const response = await fetch(`./api/solicitudes?tipo=profesional&usuarioId=${profesionalId}`);
    const data = await response.json();

    if (!data.success || !data.data.solicitudes) {
      return generarSeccionVacia('trabajos');
    }

    const trabajos = data.data.solicitudes.slice(0, 5);

    if (trabajos.length === 0) {
      return generarSeccionVacia('trabajos');
    }

    let html = `
      <div class="card">
        <div class="card-header">
          <div class="card-title">💼 Mis Trabajos Recientes</div>
          <button class="action-button outline" onclick="navigateTo('mis-trabajos')">
            Ver Todos
          </button>
        </div>
        <div class="card-body">
    `;

    trabajos.forEach(trabajo => {
      html += generarItemTrabajo(trabajo);
    });

    html += `
        </div>
      </div>
    `;

    return html;
  } catch (error) {
    console.error('Error al cargar trabajos:', error);
    return generarSeccionVacia('trabajos');
  }
}

function generarItemTrabajo(trabajo) {
  const estado = trabajo.estado || 'pendiente';
  const estadoClass = `status-${estado}`;
  const estadoTexto = {
    'pendiente': 'Pendiente',
    'aceptada': 'Aceptada',
    'completada': 'Completada',
    'rechazada': 'Rechazada',
    'cancelada': 'Cancelada'
  };

  const fecha = new Date(trabajo.fechaSolicitud).toLocaleDateString('es-PE', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  });

  return `
    <div class="solicitud-item">
      <div class="solicitud-header">
        <div>
          <div class="solicitud-title">
            ${trabajo.descripcion?.substring(0, 60) || 'Sin descripción'}${trabajo.descripcion?.length > 60 ? '...' : ''}
          </div>
          <div class="solicitud-meta">
            📅 ${fecha} • 💰 S/ ${trabajo.presupuestoEstimado?.toFixed(2) || '0.00'}
          </div>
        </div>
        <span class="status-badge ${estadoClass}">
          ${estadoTexto[estado]}
        </span>
      </div>
    </div>
  `;
}

function generarSeccionVacia(tipo) {
  if (tipo === 'solicitudes') {
    return `
      <div class="card">
        <div class="card-header">
          <div class="card-title">📋 Mis Solicitudes Recientes</div>
        </div>
        <div class="card-body">
          <div class="empty-state">
            <div class="empty-state-icon">📭</div>
            <div class="empty-state-text">
              No tienes solicitudes aún
            </div>
            <button class="action-button" onclick="goToBuscarProfesional()">
              🔍 Buscar Profesionales
            </button>
          </div>
        </div>
      </div>
    `;
  }

  if (tipo === 'trabajos') {
    return `
      <div class="card">
        <div class="card-header">
          <div class="card-title">💼 Mis Trabajos Recientes</div>
        </div>
        <div class="card-body">
          <div class="empty-state">
            <div class="empty-state-icon">📭</div>
            <div class="empty-state-text">
              No tienes trabajos asignados en este momento
            </div>
            <button class="action-button" onclick="goToMisServicios()">
              Configurar Mis Servicios
            </button>
          </div>
        </div>
      </div>
    `;
  }

  return '';
}

// ============================================
// VISTA: MIS SOLICITUDES
// ============================================
async function renderMisSolicitudes(container) {
  try {
    const response = await fetch(`./api/solicitudes?tipo=cliente&usuarioId=${userData.id}`);
    const data = await response.json();

    if (!data.success || !data.data.solicitudes) {
      container.innerHTML = generarSeccionVacia('solicitudes');
      return;
    }

    const solicitudes = data.data.solicitudes;

    if (solicitudes.length === 0) {
      container.innerHTML = generarSeccionVacia('solicitudes');
      return;
    }

    let html = `
      <div class="card">
        <div class="card-header">
          <div class="card-title">📋 Todas mis Solicitudes</div>
          <button class="action-button" onclick="goToBuscarProfesional()">
            Nueva Solicitud
          </button>
        </div>
        <div class="card-body">
    `;

    solicitudes.forEach(solicitud => {
      html += generarItemSolicitud(solicitud);
    });

    html += `
        </div>
      </div>
    `;

    container.innerHTML = html;
  } catch (error) {
    console.error('Error al cargar solicitudes:', error);
    container.innerHTML = `
      <div class="card">
        <div class="card-body">
          <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            <div class="empty-state-text">
              Error al cargar solicitudes
            </div>
          </div>
        </div>
      </div>
    `;
  }
}

// ============================================
// VISTA: MIS TRABAJOS
// ============================================
// VISTA: MIS TRABAJOS (PROFESIONAL)
// ============================================
/**
 * Renderiza la vista de trabajos del profesional con:
 * - Sistema de alertas con badge de pendientes
 * - Listado cronológico de solicitudes
 * - Enlaces a detalle de cada trabajo
 *
 * ACTUALIZADO 2025-12-03: Implementada integración completa con backend
 * ACTUALIZADO 2025-12-04: Corregido para usar profesionalId en lugar de usuarioId
 */
async function renderMisTrabajos(container) {
  try {
    // ✅ ACTUALIZADO 2025-12-04: Usar profesionalId en lugar de usuarioId para búsqueda de trabajos
    const profesionalId = userData.profesionalId || userData.id;
    console.log('🔍 Buscando trabajos para profesionalId:', profesionalId);
    const response = await fetch(`./api/solicitudes?tipo=profesional&usuarioId=${profesionalId}`);
    const data = await response.json();

    if (!data.success || !data.data.solicitudes) {
      container.innerHTML = generarSeccionVacia('trabajos');
      return;
    }

    const solicitudes = data.data.solicitudes;

    if (solicitudes.length === 0) {
      container.innerHTML = generarSeccionVacia('trabajos');
      return;
    }

    // Contar pendientes
    const pendientesCount = solicitudes.filter(s => s.estado === 'pendiente').length;

    let html = `
      <div class="card">
        <div class="card-header">
          <div class="card-title">
            💼 Mis Trabajos Recientes
            ${pendientesCount > 0 ? `<span class="badge badge-alert">${pendientesCount}</span>` : ''}
          </div>
        </div>
        <div class="card-body">
    `;

    html += renderTablaTrabajos(solicitudes);

    html += `
        </div>
      </div>
    `;

    container.innerHTML = html;
  } catch (error) {
    console.error('Error al cargar trabajos:', error);
    container.innerHTML = `
      <div class="card">
        <div class="card-body">
          <div class="empty-state">
            <div class="empty-state-icon">⚠️</div>
            <div class="empty-state-text">
              Error al cargar trabajos
            </div>
          </div>
        </div>
      </div>
    `;
  }
}

/**
 * Renderiza tabla de trabajos/solicitudes.
 * Ordenados cronológicamente con los más recientes primero.
 *
 * ACTUALIZADO 2025-12-03: Agregado soporte para estados y navegación a detalle
 */
function renderTablaTrabajos(solicitudes) {
  // Ordenar por fecha de solicitud (más recientes primero)
  const solicitudesOrdenadas = [...solicitudes].sort((a, b) => {
    return new Date(b.fechaSolicitud) - new Date(a.fechaSolicitud);
  });

  return `
    <div class="table-responsive">
      <table class="data-table">
        <thead>
          <tr>
            <th>Código</th>
            <th>Cliente</th>
            <th>Descripción</th>
            <th>Fecha Servicio</th>
            <th>Presupuesto</th>
            <th>Estado</th>
            <th>Acciones</th>
          </tr>
        </thead>
        <tbody>
          ${solicitudesOrdenadas.map(s => `
            <tr class="${s.estado === 'pendiente' ? 'row-highlight-pending' : ''}">
              <td data-label="Código">
                <span class="codigo-solicitud">SR-${new Date(s.fechaSolicitud).getFullYear()}-${String(s.id).padStart(6, '0')}</span>
              </td>
              <td data-label="Cliente">Cliente #${s.clienteId}</td>
              <td data-label="Descripción">
                <div class="descripcion-cell">
                  ${truncarTexto(s.descripcion, 60)}
                </div>
              </td>
              <td data-label="Fecha Servicio">${formatearFecha(s.fechaServicio)}</td>
              <td data-label="Presupuesto">S/ ${s.presupuestoEstimado?.toFixed(2) || '0.00'}</td>
              <td data-label="Estado">${renderEstadoBadge(s.estado)}</td>
              <td data-label="Acciones">
                <button class="btn-sm btn-primary" onclick="verDetalleTrabajo(${s.id})">
                  Ver Detalle
                </button>
              </td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

/**
 * Renderiza badge de estado con colores según el estado.
 */
function renderEstadoBadge(estado) {
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
 * Trunca texto largo agregando "..."
 */
function truncarTexto(texto, maxLength) {
  if (!texto) return '';
  return texto.length > maxLength
    ? texto.substring(0, maxLength) + '...'
    : texto;
}

/**
 * Formatea fecha a formato legible.
 */
function formatearFecha(fechaISO) {
  if (!fechaISO) return '-';
  const fecha = new Date(fechaISO);
  return fecha.toLocaleDateString('es-PE', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
}

/**
 * Navega a la página de detalle de trabajo.
 */
function verDetalleTrabajo(solicitudId) {
  window.location.href = `detalle-trabajo.html?id=${solicitudId}`;
}

// ============================================
// VISTA: ESTADÍSTICAS
// ============================================
function renderEstadisticas(container) {
  container.innerHTML = `
    <div class="card">
      <div class="card-header">
        <div class="card-title">📊 Estadísticas y Métricas</div>
      </div>
      <div class="card-body">
        <canvas id="servicesChart" width="400" height="200"></canvas>
      </div>
    </div>
  `;

  // Renderizar gráfico
  setTimeout(() => {
    const ctx = document.getElementById('servicesChart');
    if (ctx) {
      new Chart(ctx, {
        type: 'bar',
        data: {
          labels: ['Enero', 'Febrero', 'Marzo', 'Abril', 'Mayo', 'Junio'],
          datasets: [
            {
              label: 'Solicitudes',
              data: [12, 19, 7, 14, 20, 15],
              backgroundColor: 'rgba(102, 126, 234, 0.6)',
              borderColor: '#667eea',
              borderWidth: 2
            },
            {
              label: 'Completadas',
              data: [8, 15, 6, 11, 18, 12],
              backgroundColor: 'rgba(40, 167, 69, 0.6)',
              borderColor: '#28a745',
              borderWidth: 2
            }
          ]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: { beginAtZero: true }
          }
        }
      });
    }
  }, 100);
}

// ============================================
// FUNCIONES DE NAVEGACIÓN EXTERNA
// ============================================

function goToInformacionPersonal() {
  window.location.href = "informacion-personal.html";
}

function goToActualizarCliente() {
  window.location.href = "perfil-cliente.html";
}

function goToActualizarProfesional() {
  const usuarioId = userData.id;
  window.location.href = `profesional.html?usuarioId=${usuarioId}`;
}

function goToBuscarProfesional() {
  window.location.href = "buscar-profesional.html";
}

function goToMisServicios() {
  window.location.href = "servicios-profesional.html";
}

function registrarseComoProfesional() {
  const usuarioId = userData.id;
  // Redirigir a la página de registro profesional
  window.location.href = `profesional.html?usuarioId=${usuarioId}&modo=nuevo`;
}

function registrarseComoCliente() {
  // Redirigir a la página de registro cliente
  window.location.href = "perfil-cliente.html?modo=nuevo";
}

// ============================================
// MENÚ MÓVIL
// ============================================
function toggleMobileMenu() {
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('mobileOverlay');

  sidebar.classList.toggle('mobile-visible');
  overlay.classList.toggle('active');
}

function closeMobileMenu() {
  const sidebar = document.getElementById('sidebar');
  const overlay = document.getElementById('mobileOverlay');

  sidebar.classList.remove('mobile-visible');
  overlay.classList.remove('active');
}

// ============================================
// LOGOUT
// ============================================
function logout() {
  if (confirm('¿Estás seguro que deseas cerrar sesión?')) {
    localStorage.clear();
    window.location.href = "login.html";
  }
}
