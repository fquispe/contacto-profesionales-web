// Listado Solicitud Servicios JavaScript - Desacoplado del HTML el 2025-11-21
        // ============================================
        // VARIABLES GLOBALES
        // ============================================
        const API_BASE_URL = './api';
        let todasLasSolicitudes = [];
        let solicitudesFiltradas = [];
        let usuarioId = null;

        // ============================================
        // INICIALIZACIÓN
        // ============================================
        document.addEventListener('DOMContentLoaded', function() {
            console.log('Iniciando listado de solicitudes...');

            // Verificar autenticación
            const userData = JSON.parse(localStorage.getItem('userData') || '{}');
            usuarioId = userData.id;

            if (!usuarioId) {
                showAlert('Debes iniciar sesión para ver tus solicitudes', 'error');
                setTimeout(() => {
                    window.location.href = 'login.html';
                }, 2000);
                return;
            }

            console.log('Usuario autenticado:', usuarioId);
            cargarSolicitudes();
        });

        // ============================================
        // CARGAR SOLICITUDES
        // ============================================
        async function cargarSolicitudes() {
            showLoading();

            try {
                
            	// 🔥 Obtener userData del localStorage
                const userData = JSON.parse(localStorage.getItem('userData') || '{}');
                const usuarioId = userData.id;
                
                if (!usuarioId) {
                    showAlert('Error: Usuario no autenticado', 'error');
                    setTimeout(() => window.location.href = 'login.html', 2000);
                    return;
                }
                
             	// 🔥 Enviar usuarioId como parámetro
                const url = `${API_BASE_URL}/solicitudes?tipo=cliente&usuarioId=${usuarioId}`;
                console.log('Cargando solicitudes desde:', url);

                const response = await fetch(url);
                const data = await response.json();

                console.log('Respuesta del servidor:', data);

                if (data.success) {
                    todasLasSolicitudes = data.data.solicitudes || [];
                    solicitudesFiltradas = [...todasLasSolicitudes];

                    console.log('Solicitudes cargadas:', todasLasSolicitudes.length);

                    actualizarEstadisticas();
                    mostrarSolicitudes();
                } else {
                    throw new Error(data.error || 'Error al cargar solicitudes');
                }
            } catch (error) {
                console.error('Error al cargar solicitudes:', error);
                showAlert('Error al cargar las solicitudes: ' + error.message, 'error');
                hideLoading();
                showEmptyState();
            }
        }

        // ============================================
        // MOSTRAR SOLICITUDES
        // ============================================
        function mostrarSolicitudes() {
            hideLoading();
            const grid = document.getElementById('solicitudesGrid');
            const resultadosInfo = document.getElementById('resultadosInfo');

            if (solicitudesFiltradas.length === 0) {
                grid.innerHTML = '';
                resultadosInfo.textContent = 'No se encontraron solicitudes';
                showEmptyState();
                return;
            }

            hideEmptyState();
            resultadosInfo.textContent = `${solicitudesFiltradas.length} solicitud(es) encontrada(s)`;

            grid.innerHTML = '';
            solicitudesFiltradas.forEach(solicitud => {
                grid.appendChild(crearTarjetaSolicitud(solicitud));
            });
        }

        // ============================================
        // CREAR TARJETA DE SOLICITUD
        // ============================================
        function crearTarjetaSolicitud(solicitud) {
            const card = document.createElement('div');
            card.className = `solicitud-card ${solicitud.estado}`;

            const codigo = generarCodigoSolicitud(solicitud.id);
            const fecha = formatearFecha(solicitud.fechaSolicitud);
            const fechaServicio = formatearFecha(solicitud.fechaServicio);
            const urgencia = solicitud.urgencia === 'urgent' ? '⚡ Urgente' : '🕐 Normal';

            card.innerHTML = `
                <div class="card-header">
                    <div class="card-codigo">${codigo}</div>
                    <span class="card-estado estado-${solicitud.estado}">
                        ${traducirEstado(solicitud.estado)}
                    </span>
                </div>

                <div class="card-profesional">
                    <div class="profesional-avatar">👨‍🔧</div>
                    <div class="profesional-info">
                        <h4>Profesional ID: ${solicitud.profesionalId}</h4>
                        <p>${urgencia}</p>
                    </div>
                </div>

                <div class="card-description">
                    ${solicitud.descripcion || 'Sin descripción'}
                </div>

                <div class="card-details">
                    <div class="detail-item">
                        <span class="detail-label">📅 Fecha Solicitada:</span>
                        <span class="detail-value">${fechaServicio}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">💰 Presupuesto:</span>
                        <span class="detail-value">S/ ${solicitud.presupuestoEstimado?.toFixed(2) || '0.00'}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">📍 Distrito:</span>
                        <span class="detail-value">${solicitud.distrito || 'N/A'}</span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label">📆 Registrada:</span>
                        <span class="detail-value">${fecha}</span>
                    </div>
                </div>

                <div class="card-footer">
                    <button class="btn-card btn-ver" onclick="verDetalle(${solicitud.id})">
                        👁️ Ver Detalles
                    </button>
                    ${solicitud.estado === 'pendiente' ? `
                    <button class="btn-card btn-cancelar" onclick="cancelarSolicitud(${solicitud.id})">
                        ❌ Cancelar
                    </button>
                    ` : ''}
                </div>
            `;

            return card;
        }

        // ============================================
        // ACTUALIZAR ESTADÍSTICAS
        // ============================================
        function actualizarEstadisticas() {
            const total = todasLasSolicitudes.length;
            const pendientes = todasLasSolicitudes.filter(s => s.estado === 'pendiente').length;
            const completadas = todasLasSolicitudes.filter(s => s.estado === 'completada').length;
            const presupuestoTotal = todasLasSolicitudes.reduce((sum, s) => sum + (s.presupuestoEstimado || 0), 0);

            document.getElementById('totalSolicitudes').textContent = total;
            document.getElementById('pendientes').textContent = pendientes;
            document.getElementById('completadas').textContent = completadas;
            document.getElementById('presupuestoTotal').textContent = `S/ ${presupuestoTotal.toFixed(2)}`;
        }

        // ============================================
        // FILTRAR SOLICITUDES
        // ============================================
        function aplicarFiltros() {
            const estado = document.getElementById('filterEstado').value;
            const orden = document.getElementById('filterOrden').value;
            const busqueda = document.getElementById('filterBusqueda').value.toLowerCase();

            solicitudesFiltradas = [...todasLasSolicitudes];

            // Filtrar por estado
            if (estado) {
                solicitudesFiltradas = solicitudesFiltradas.filter(s => s.estado === estado);
            }

            // Filtrar por búsqueda
            if (busqueda) {
                solicitudesFiltradas = solicitudesFiltradas.filter(s =>
                    s.descripcion?.toLowerCase().includes(busqueda) ||
                    s.distrito?.toLowerCase().includes(busqueda)
                );
            }

            // Ordenar
            if (orden === 'reciente') {
                solicitudesFiltradas.sort((a, b) => new Date(b.fechaSolicitud) - new Date(a.fechaSolicitud));
            } else if (orden === 'antiguo') {
                solicitudesFiltradas.sort((a, b) => new Date(a.fechaSolicitud) - new Date(b.fechaSolicitud));
            } else if (orden === 'presupuesto') {
                solicitudesFiltradas.sort((a, b) => b.presupuestoEstimado - a.presupuestoEstimado);
            }

            mostrarSolicitudes();
        }

        // ============================================
        // ACCIONES
        // ============================================
        function verDetalle(id) {
            // Implementar vista detallada de la solicitud
            console.log('Ver detalle de solicitud:', id);
            showAlert('Funcionalidad en desarrollo', 'info');
        }

        async function cancelarSolicitud(id) {
            if (!confirm('¿Estás seguro que deseas cancelar esta solicitud?')) {
                return;
            }

            try {
                const response = await fetch(`${API_BASE_URL}/solicitudes/${id}/cancelar`, {
                    method: 'PUT'
                });

                const data = await response.json();

                if (data.success) {
                    showAlert('Solicitud cancelada exitosamente', 'success');
                    cargarSolicitudes();
                } else {
                    throw new Error(data.error || 'Error al cancelar');
                }
            } catch (error) {
                console.error('Error al cancelar solicitud:', error);
                showAlert('Error al cancelar la solicitud: ' + error.message, 'error');
            }
        }

        function nuevaSolicitud() {
            window.location.href = 'buscar-profesional.html';
        }

        function volverDashboard() {
            window.location.href = 'dashboard.html';
        }

        // ============================================
        // UTILIDADES
        // ============================================
        function generarCodigoSolicitud(id) {
            const year = new Date().getFullYear();
            return `SR-${year}-${String(id).padStart(6, '0')}`;
        }

        function formatearFecha(fecha) {
            if (!fecha) return 'N/A';
            const date = new Date(fecha);
            return date.toLocaleDateString('es-PE', {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        function traducirEstado(estado) {
            const estados = {
                'pendiente': 'Pendiente',
                'aceptada': 'Aceptada',
                'completada': 'Completada',
                'rechazada': 'Rechazada',
                'cancelada': 'Cancelada'
            };
            return estados[estado] || estado;
        }

        // ============================================
        // UI STATE FUNCTIONS
        // ============================================
        function showLoading() {
            document.getElementById('loadingState').style.display = 'block';
            document.getElementById('emptyState').style.display = 'none';
            document.getElementById('solicitudesGrid').innerHTML = '';
        }

        function hideLoading() {
            document.getElementById('loadingState').style.display = 'none';
        }

        function showEmptyState() {
            document.getElementById('emptyState').style.display = 'block';
            document.getElementById('solicitudesGrid').innerHTML = '';
        }

        function hideEmptyState() {
            document.getElementById('emptyState').style.display = 'none';
        }

        function showAlert(message, type = 'info') {
            const container = document.getElementById('alertContainer');
            const alert = document.createElement('div');
            alert.className = `alert alert-${type} show`;
            alert.textContent = message;

            container.innerHTML = '';
            container.appendChild(alert);

            setTimeout(() => {
                alert.classList.remove('show');
                setTimeout(() => alert.remove(), 300);
            }, 5000);
        }
