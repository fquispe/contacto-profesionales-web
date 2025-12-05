/**
 * Lógica completa para el formulario de perfil profesional.
 *
 * Funcionalidades:
 * - Carga y actualización de perfil completo
 * - Gestión de certificaciones (CRUD)
 * - Gestión de proyectos del portafolio (CRUD, máx. 20)
 * - Gestión de imágenes de proyectos (CRUD, máx. 5 por proyecto)
 * - Gestión de antecedentes (CRUD, máx. 1 por tipo)
 * - Gestión de redes sociales (CRUD con actualización masiva)
 * - Validaciones y manejo de errores
 * - Modales y UI interactiva
 *
 * Creado: 2025-11-15
 * Actualizado: 2025-11-16
 *
 * ✅ CAMBIOS RECIENTES (2025-11-16):
 * - Eliminada carga de datos personales (nombreCompleto, email, telefono, documentoIdentidad)
 * - Eliminada carga de fotos (fotoPerfil, fotoPortada)
 * - Implementado selector múltiple con chips para Idiomas (7 opciones predefinidas)
 * - Implementado selector múltiple con chips para Métodos de Pago (9 opciones predefinidas)
 * - Mejora de UX: Validación de duplicados, chips visuales con botón de eliminar
 * - Los datos se almacenan como arrays en la base de datos (sin errores tipográficos)
 * - NUEVO: Selector de categorías dinámico en modal de proyectos
 *   * Carga automática de especialidades/categorías del profesional
 *   * Pobla el select con categorías únicas (sin duplicados)
 *   * Muestra solo las categorías que el profesional ha registrado
 *   * Reutiliza el endpoint de EspecialidadServlet (/api/profesionales/{id}/especialidades)
 *
 * @author Sistema
 */

// ========================================
// ESTADO GLOBAL
// ========================================
const AppState = {
    perfilCompleto: null,
    certificaciones: [],
    proyectos: [],
    antecedentes: [],
    redesSociales: [],
    // ✅ ACTUALIZADO 2025-11-16: Arrays para selección múltiple con chips
    idiomasSeleccionados: [],
    metodosPagoSeleccionados: [],
    // ✅ ACTUALIZADO 2025-11-16: Especialidades/Categorías del profesional
    especialidades: [],
    // ✅ NUEVO 2025-12-04: ID del profesional desde localStorage
    profesionalId: null,
    editando: {
        certificacion: null,
        proyecto: null,
        antecedente: null,
        redSocial: null
    }
};

// ========================================
// INICIALIZACIÓN
// ========================================
document.addEventListener('DOMContentLoaded', async function() {
    console.log('Inicializando perfil profesional...');

    // ✅ NUEVO 2025-12-04: Obtener profesionalId desde localStorage
    const userData = JSON.parse(localStorage.getItem('userData') || '{}');

    if (!userData.profesionalId) {
        console.error('❌ No se encontró profesionalId en localStorage');
        mostrarAlerta('Error: No se pudo identificar el profesional. Por favor inicie sesión nuevamente.', 'error');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }

    AppState.profesionalId = userData.profesionalId;
    console.log('✓ ProfesionalId cargado desde localStorage:', AppState.profesionalId);

    // Inicializar tabs
    inicializarTabs();

    // Inicializar event listeners
    inicializarEventListeners();

    // Cargar perfil completo
    await cargarPerfilCompleto();
});

/**
 * Inicializa el sistema de tabs.
 */
function inicializarTabs() {
    const tabButtons = document.querySelectorAll('.tab-button');

    tabButtons.forEach(button => {
        button.addEventListener('click', function() {
            const targetTab = this.getAttribute('data-tab');

            // Remover active de todos los botones y contenidos
            tabButtons.forEach(btn => btn.classList.remove('active'));
            document.querySelectorAll('.tab-content').forEach(content =>
                content.classList.remove('active')
            );

            // Activar tab seleccionado
            this.classList.add('active');
            document.getElementById(targetTab).classList.add('active');
        });
    });
}

/**
 * Inicializa todos los event listeners del formulario.
 */
function inicializarEventListeners() {
    // Formulario principal - datos básicos
    const formBasico = document.getElementById('formDatosBasicos');
    if (formBasico) {
        formBasico.addEventListener('submit', guardarDatosBasicos);
    }

    // Botones de modales
    const btnModalCert = document.getElementById('btnAgregarCertificacion');
    if (btnModalCert) {
        btnModalCert.addEventListener('click', () => abrirModalCertificacion());
    }

    const btnModalProyecto = document.getElementById('btnAgregarProyecto');
    if (btnModalProyecto) {
        btnModalProyecto.addEventListener('click', () => abrirModalProyecto());
    }

    const btnModalAntecedente = document.getElementById('btnAgregarAntecedente');
    if (btnModalAntecedente) {
        btnModalAntecedente.addEventListener('click', () => abrirModalAntecedente());
    }

    const btnModalRedes = document.getElementById('btnAgregarRedSocial');
    if (btnModalRedes) {
        btnModalRedes.addEventListener('click', () => abrirModalRedSocial());
    }

    // Contador de caracteres para biografía
    const biografiaInput = document.getElementById('biografiaProfesional');
    if (biografiaInput) {
        biografiaInput.addEventListener('input', actualizarContadorBiografia);
    }
}

// ========================================
// CARGA DE DATOS
// ========================================

/**
 * Carga el perfil completo del profesional.
 * Incluye todos los datos consolidados.
 * ✅ ACTUALIZADO 2025-12-04: Usa profesionalId desde AppState (localStorage)
 */
async function cargarPerfilCompleto() {
    mostrarCargando(true);

    try {
        // ✅ Obtener perfil completo desde API pasando el profesionalId
        const perfil = await PerfilProfesionalAPI.obtenerPerfilCompleto(AppState.profesionalId);

        console.log('Perfil completo cargado:', perfil);
        AppState.perfilCompleto = perfil;

        // Cargar datos básicos en el formulario
        cargarDatosBasicos(perfil);

        // Cargar certificaciones
        AppState.certificaciones = perfil.certificaciones || [];
        renderizarCertificaciones();

        // Cargar proyectos
        AppState.proyectos = perfil.proyectos || [];
        renderizarProyectos();

        // Cargar antecedentes
        AppState.antecedentes = perfil.antecedentes || [];
        renderizarAntecedentes();

        // Cargar redes sociales
        AppState.redesSociales = perfil.redesSociales || [];
        renderizarRedesSociales();

        // ✅ ACTUALIZADO 2025-12-04: Cargar especialidades usando el profesionalId desde AppState
        if (AppState.profesionalId) {
            await cargarEspecialidades(AppState.profesionalId);
        } else {
            console.error('⚠️ ERROR: AppState.profesionalId es undefined/null. No se pueden cargar especialidades.');
        }

        mostrarAlerta('Perfil cargado exitosamente', 'success');

    } catch (error) {
        console.error('Error al cargar perfil:', error);
        mostrarAlerta('Error al cargar el perfil: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

/**
 * Carga los datos básicos del perfil en el formulario.
 */
function cargarDatosBasicos(perfil) {
    // ✅ ELIMINADO: Datos personales (nombreCompleto, email, telefono, documentoIdentidad)
    // ℹ️ Estos campos se gestionan en otro módulo del sistema

    // Información profesional
    setInputValue('biografiaProfesional', perfil.biografiaProfesional);
    setInputValue('aniosExperiencia', perfil.aniosExperiencia);

    // ✅ ACTUALIZADO 2025-11-16: Cargar idiomas como chips
    if (perfil.idiomas && perfil.idiomas.length > 0) {
        AppState.idiomasSeleccionados = [...perfil.idiomas];
        renderizarIdiomasChips();
    } else {
        AppState.idiomasSeleccionados = [];
        renderizarIdiomasChips();
    }

    // Licencias profesionales
    setInputValue('licenciasProfesionales', perfil.licenciasProfesionales);

    // Seguro de responsabilidad
    const seguroCheckbox = document.getElementById('seguroResponsabilidad');
    if (seguroCheckbox) {
        seguroCheckbox.checked = perfil.seguroResponsabilidad || false;
    }

    // ✅ ACTUALIZADO 2025-11-16: Cargar métodos de pago como chips
    if (perfil.metodosPago && perfil.metodosPago.length > 0) {
        AppState.metodosPagoSeleccionados = [...perfil.metodosPago];
        renderizarMetodosPagoChips();
    } else {
        AppState.metodosPagoSeleccionados = [];
        renderizarMetodosPagoChips();
    }

    // Política de cancelación
    setInputValue('politicaCancelacion', perfil.politicaCancelacion);

    // ✅ ELIMINADO: Carga de fotos (fotoPerfil, fotoPortada)
    // ℹ️ Las fotos se gestionan en otro módulo del sistema

    // Mostrar puntuación y estadísticas
    if (perfil.puntuacionPlataforma) {
        const puntuacionElement = document.getElementById('puntuacionPlataforma');
        if (puntuacionElement) {
            puntuacionElement.textContent = perfil.puntuacionPlataforma.toFixed(1);
        }
    }

    if (perfil.antecedentesVerificados !== undefined) {
        const antecedentesElement = document.getElementById('antecedentesVerificados');
        if (antecedentesElement) {
            antecedentesElement.textContent = `${perfil.antecedentesVerificados}/3`;
        }
    }

    actualizarContadorBiografia();
}

// ========================================
// DATOS BÁSICOS - GUARDAR
// ========================================

/**
 * Guarda los datos básicos del perfil.
 */
async function guardarDatosBasicos(event) {
    if (event) event.preventDefault();

    mostrarCargando(true);

    try {
        // ✅ ACTUALIZADO 2025-11-16: Obtener datos de chips en lugar de inputs de texto
        // Recopilar datos del formulario
        const datos = {
            biografiaProfesional: getInputValue('biografiaProfesional'),
            aniosExperiencia: parseInt(getInputValue('aniosExperiencia')) || 0,
            idiomas: AppState.idiomasSeleccionados, // Desde el array de chips
            licenciasProfesionales: getInputValue('licenciasProfesionales'),
            seguroResponsabilidad: document.getElementById('seguroResponsabilidad')?.checked || false,
            metodosPago: AppState.metodosPagoSeleccionados, // Desde el array de chips
            politicaCancelacion: getInputValue('politicaCancelacion')
        };

        console.log('Guardando datos básicos:', datos);

        // ✅ ACTUALIZADO 2025-12-04: Pasar profesionalId como primer parámetro
        await PerfilProfesionalAPI.actualizarDatosBasicos(AppState.profesionalId, datos);

        mostrarAlerta('Datos básicos actualizados exitosamente', 'success');

        // Recargar perfil para obtener puntuación actualizada
        await cargarPerfilCompleto();

    } catch (error) {
        console.error('Error al guardar datos básicos:', error);
        mostrarAlerta('Error al guardar: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

// ========================================
// CERTIFICACIONES
// ========================================

/**
 * Renderiza la lista de certificaciones.
 */
function renderizarCertificaciones() {
    const container = document.getElementById('certificacionesContainer');
    if (!container) return;

    if (AppState.certificaciones.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🏆</div>
                <p class="empty-state-message">No has agregado certificaciones aún</p>
            </div>
        `;
        return;
    }

    container.innerHTML = AppState.certificaciones.map(cert => `
        <div class="item-card">
            <div class="item-card-header">
                <div>
                    <div class="item-card-title">${escapeHtml(cert.nombreCertificacion)}</div>
                    <div class="item-card-subtitle">${escapeHtml(cert.institucion)}</div>
                </div>
                <div>
                    ${cert.verificada ? '<span class="badge badge-success">Verificada</span>' :
                      '<span class="badge badge-warning">Pendiente</span>'}
                </div>
            </div>
            <div class="item-card-body">
                ${cert.descripcion ? `<p>${escapeHtml(cert.descripcion)}</p>` : ''}
                ${cert.fechaObtencion ? `<p><small>📅 Obtenida: ${formatearFecha(cert.fechaObtencion)}</small></p>` : ''}
                ${cert.fechaVigencia ? `<p><small>⏰ Vigente hasta: ${formatearFecha(cert.fechaVigencia)}</small></p>` : ''}
            </div>
            <div class="item-card-footer">
                <div>
                    ${cert.documentoUrl ? `<a href="${cert.documentoUrl}" target="_blank" class="btn btn-sm btn-secondary">📄 Ver documento</a>` : ''}
                </div>
                <div>
                    <button class="btn btn-sm btn-secondary" onclick="editarCertificacion(${cert.id})">✏️ Editar</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarCertificacion(${cert.id})">🗑️ Eliminar</button>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Abre el modal para agregar/editar certificación.
 */
function abrirModalCertificacion(certificacionId = null) {
    const modal = document.getElementById('modalCertificacion');
    if (!modal) return;

    // Resetear formulario
    const form = document.getElementById('formCertificacion');
    if (form) form.reset();

    if (certificacionId) {
        // Modo edición
        const cert = AppState.certificaciones.find(c => c.id === certificacionId);
        if (cert) {
            AppState.editando.certificacion = cert;
            document.getElementById('modalCertificacionTitulo').textContent = 'Editar Certificación';

            // Cargar datos en el formulario
            setInputValue('certNombre', cert.nombreCertificacion);
            setInputValue('certInstitucion', cert.institucion);
            setInputValue('certFechaObtencion', cert.fechaObtencion);
            setInputValue('certFechaVigencia', cert.fechaVigencia);
            setInputValue('certDescripcion', cert.descripcion);
            setInputValue('certDocumentoUrl', cert.documentoUrl);
        }
    } else {
        // Modo creación
        AppState.editando.certificacion = null;
        document.getElementById('modalCertificacionTitulo').textContent = 'Agregar Certificación';
    }

    modal.classList.add('active');
}

/**
 * Cierra el modal de certificación.
 */
function cerrarModalCertificacion() {
    const modal = document.getElementById('modalCertificacion');
    if (modal) {
        modal.classList.remove('active');
        AppState.editando.certificacion = null;
    }
}

/**
 * Guarda una certificación (crear o actualizar).
 */
async function guardarCertificacion(event) {
    if (event) event.preventDefault();

    const datos = {
        nombreCertificacion: getInputValue('certNombre'),
        institucion: getInputValue('certInstitucion'),
        fechaObtencion: getInputValue('certFechaObtencion') || null,
        fechaVigencia: getInputValue('certFechaVigencia') || null,
        descripcion: getInputValue('certDescripcion'),
        documentoUrl: getInputValue('certDocumentoUrl')
    };

    // Validaciones
    if (!datos.nombreCertificacion) {
        mostrarAlerta('El nombre de la certificación es requerido', 'error');
        return;
    }

    if (!datos.institucion) {
        mostrarAlerta('La institución es requerida', 'error');
        return;
    }

    try {
        mostrarCargando(true);

        if (AppState.editando.certificacion) {
            // Actualizar existente
            datos.id = AppState.editando.certificacion.id;
            await PerfilProfesionalAPI.actualizarCertificacion(AppState.profesionalId, datos);
            mostrarAlerta('Certificación actualizada exitosamente', 'success');
        } else {
            // Crear nueva
            await PerfilProfesionalAPI.crearCertificacion(AppState.profesionalId, datos);
            mostrarAlerta('Certificación agregada exitosamente', 'success');
        }

        cerrarModalCertificacion();
        await cargarPerfilCompleto(); // Recargar todo el perfil

    } catch (error) {
        console.error('Error al guardar certificación:', error);
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

/**
 * Edita una certificación.
 */
function editarCertificacion(id) {
    abrirModalCertificacion(id);
}

/**
 * Elimina una certificación.
 */
async function eliminarCertificacion(id) {
    if (!confirm('¿Estás seguro de eliminar esta certificación?')) {
        return;
    }

    try {
        mostrarCargando(true);
        await PerfilProfesionalAPI.eliminarCertificacion(AppState.profesionalId, id);
        mostrarAlerta('Certificación eliminada exitosamente', 'success');
        await cargarPerfilCompleto();
    } catch (error) {
        console.error('Error al eliminar certificación:', error);
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

// ========================================
// ESPECIALIDADES/CATEGORÍAS
// Actualizado: 2025-11-16
// ========================================

/**
 * Carga las especialidades/categorías del profesional.
 * Estas se usan para poblar el selector de categorías en el modal de proyectos.
 *
 * ✅ ACTUALIZADO 2025-11-17: Validación robusta para asegurar array
 *
 * @param {number} profesionalId - ID del profesional
 */
async function cargarEspecialidades(profesionalId) {
    try {
        console.log('Cargando especialidades del profesional:', profesionalId);
        const especialidades = await PerfilProfesionalAPI.obtenerEspecialidades(profesionalId);

        // ✅ Validación robusta: Asegurar que siempre sea un array
        if (Array.isArray(especialidades)) {
            AppState.especialidades = especialidades;
        } else if (especialidades && typeof especialidades === 'object') {
            // Si la API devuelve un objeto en lugar de array, intentar extraer array
            console.warn('La API devolvió un objeto en lugar de array:', especialidades);
            // Verificar si tiene una propiedad que sea array (ej: data, items, especialidades)
            if (Array.isArray(especialidades.data)) {
                AppState.especialidades = especialidades.data;
            } else if (Array.isArray(especialidades.items)) {
                AppState.especialidades = especialidades.items;
            } else if (Array.isArray(especialidades.especialidades)) {
                AppState.especialidades = especialidades.especialidades;
            } else {
                console.error('No se pudo extraer array de especialidades del objeto:', especialidades);
                AppState.especialidades = [];
            }
        } else {
            console.warn('Especialidades no es un array ni un objeto:', especialidades);
            AppState.especialidades = [];
        }

        console.log('Especialidades cargadas:', AppState.especialidades.length, 'items');
        if (AppState.especialidades.length > 0) {
            console.log('Primera especialidad:', AppState.especialidades[0]);
            console.log('Propiedades de la primera especialidad:', Object.keys(AppState.especialidades[0]));
            console.log('categoriaId:', AppState.especialidades[0].categoriaId);
            console.log('categoriaNombre:', AppState.especialidades[0].categoriaNombre);
        } else {
            console.warn('⚠️ NO se cargaron especialidades. Array vacío.');
        }
    } catch (error) {
        console.error('Error al cargar especialidades:', error);
        AppState.especialidades = [];
    }
}

/**
 * Pobla el selector de categorías con las especialidades del profesional.
 * Se llama al abrir el modal de proyectos.
 *
 * ✅ ACTUALIZADO 2025-11-17: Validación robusta de tipo array
 */
function poblarSelectorCategorias() {
    console.log('=== poblarSelectorCategorias() ===');
    console.log('AppState.especialidades:', AppState.especialidades);
    console.log('Es array?', Array.isArray(AppState.especialidades));
    console.log('Cantidad:', AppState.especialidades ? AppState.especialidades.length : 'N/A');

    const selector = document.getElementById('proyectoCategoria');
    if (!selector) {
        console.error('⚠️ No se encontró el elemento #proyectoCategoria');
        return;
    }

    // Limpiar opciones actuales
    selector.innerHTML = '<option value="">Seleccionar categoría</option>';

    // ✅ Validación robusta: Asegurar que especialidades sea un array
    if (!AppState.especialidades || !Array.isArray(AppState.especialidades)) {
        console.warn('AppState.especialidades no es un array válido:', AppState.especialidades);
        AppState.especialidades = []; // Resetear a array vacío
    }

    // Si no hay especialidades, mostrar mensaje
    if (AppState.especialidades.length === 0) {
        console.warn('⚠️ No hay especialidades para poblar el selector');
        selector.innerHTML = '<option value="">No tienes especialidades registradas</option>';
        selector.disabled = true;
        return;
    }

    // Habilitar selector
    selector.disabled = false;

    // Agregar cada especialidad como opción
    // Usar un Map para evitar categorías duplicadas
    const categoriasUnicas = new Map();

    console.log('Recorriendo especialidades para extraer categorías...');
    AppState.especialidades.forEach((esp, index) => {
        console.log(`Especialidad ${index}:`, esp);
        console.log(`  - categoriaId: ${esp.categoriaId} (tipo: ${typeof esp.categoriaId})`);
        console.log(`  - categoriaNombre: ${esp.categoriaNombre} (tipo: ${typeof esp.categoriaNombre})`);

        // ✅ Validación: Verificar que esp sea un objeto con las propiedades necesarias
        if (esp && typeof esp === 'object' && esp.categoriaId && esp.categoriaNombre) {
            console.log(`  ✓ Especialidad ${index} válida. Agregando categoría ${esp.categoriaId}: ${esp.categoriaNombre}`);
            // Usar categoriaId como clave para evitar duplicados
            if (!categoriasUnicas.has(esp.categoriaId)) {
                categoriasUnicas.set(esp.categoriaId, esp.categoriaNombre);
            } else {
                console.log(`  - Categoría ${esp.categoriaId} ya existe (duplicado ignorado)`);
            }
        } else {
            console.warn(`  ✗ Especialidad ${index} NO válida:`, {
                esObjeto: typeof esp === 'object',
                tieneCategoriaId: !!esp.categoriaId,
                tieneCategoriaNombre: !!esp.categoriaNombre
            });
        }
    });

    // Si no hay categorías únicas después de filtrar, mostrar mensaje
    console.log(`Categorías únicas encontradas: ${categoriasUnicas.size}`);
    if (categoriasUnicas.size === 0) {
        selector.innerHTML = '<option value="">No hay categorías válidas</option>';
        selector.disabled = true;
        console.warn('⚠️ No se encontraron categorías válidas en especialidades');
        return;
    }

    // Agregar opciones al selector
    console.log('Agregando opciones al selector...');
    categoriasUnicas.forEach((nombre, id) => {
        console.log(`  Agregando opción: id=${id}, nombre=${nombre}`);
        const option = document.createElement('option');
        option.value = id;
        option.textContent = nombre;
        selector.appendChild(option);
    });

    console.log(`✓ Selector de categorías poblado con ${categoriasUnicas.size} categorías únicas`);
    console.log('=== Fin poblarSelectorCategorias() ===');
}

// ========================================
// PROYECTOS DEL PORTAFOLIO
// ========================================

/**
 * Renderiza la lista de proyectos.
 */
function renderizarProyectos() {
    const container = document.getElementById('proyectosContainer');
    if (!container) return;

    // Actualizar contador
    const contador = document.getElementById('contadorProyectos');
    if (contador) {
        contador.textContent = `${AppState.proyectos.length}/20`;
    }

    // Habilitar/deshabilitar botón de agregar
    const btnAgregar = document.getElementById('btnAgregarProyecto');
    if (btnAgregar) {
        if (AppState.proyectos.length >= 20) {
            btnAgregar.disabled = true;
            btnAgregar.textContent = '⚠️ Límite de 20 proyectos alcanzado';
        } else {
            btnAgregar.disabled = false;
            btnAgregar.textContent = '➕ Agregar Proyecto';
        }
    }

    if (AppState.proyectos.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">💼</div>
                <p class="empty-state-message">No has agregado proyectos a tu portafolio</p>
            </div>
        `;
        return;
    }

    container.innerHTML = AppState.proyectos.map(proyecto => `
        <div class="item-card">
            <div class="item-card-header">
                <div>
                    <div class="item-card-title">${escapeHtml(proyecto.nombreProyecto)}</div>
                    <div class="item-card-subtitle">${proyecto.categoriaNombre || 'Sin categoría'}</div>
                </div>
                <div>
                    ${proyecto.calificacionCliente ?
                        `<div class="rating">
                            ${generarEstrellas(proyecto.calificacionCliente)}
                            <span class="rating-number">${proyecto.calificacionCliente}/10</span>
                         </div>` :
                        '<span class="badge badge-info">Sin calificación</span>'}
                </div>
            </div>
            <div class="item-card-body">
                <p>${escapeHtml(proyecto.descripcion)}</p>
                <p><small>📅 ${formatearFecha(proyecto.fechaRealizacion)}</small></p>
                ${proyecto.comentarioCliente ? `<p><small>💬 "${escapeHtml(proyecto.comentarioCliente)}"</small></p>` : ''}

                ${proyecto.imagenes && proyecto.imagenes.length > 0 ? `
                    <div class="project-gallery">
                        ${proyecto.imagenes.map(img => `
                            <div class="project-image">
                                <img src="${img.urlImagen}" alt="${img.descripcion || 'Imagen del proyecto'}">
                                <span class="image-type-badge">${img.tipoImagen}</span>
                            </div>
                        `).join('')}
                    </div>
                ` : ''}
            </div>
            <div class="item-card-footer">
                <div>
                    <small>🖼️ ${proyecto.imagenes?.length || 0}/5 imágenes</small>
                </div>
                <div>
                    <button class="btn btn-sm btn-secondary" onclick="gestionarImagenesProyecto(${proyecto.id})">🖼️ Imágenes</button>
                    <button class="btn btn-sm btn-secondary" onclick="editarProyecto(${proyecto.id})">✏️ Editar</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarProyecto(${proyecto.id})">🗑️ Eliminar</button>
                </div>
            </div>
        </div>
    `).join('');
}

/**
 * Abre el modal para agregar/editar proyecto.
 * ✅ ACTUALIZADO 2025-11-16: Pobla el selector de categorías con las especialidades del profesional
 */
function abrirModalProyecto(proyectoId = null) {
    const modal = document.getElementById('modalProyecto');
    if (!modal) return;

    const form = document.getElementById('formProyecto');
    if (form) form.reset();

    // ✅ NUEVO 2025-11-16: Poblar selector de categorías ANTES de cargar datos
    poblarSelectorCategorias();

    if (proyectoId) {
        const proyecto = AppState.proyectos.find(p => p.id === proyectoId);
        if (proyecto) {
            AppState.editando.proyecto = proyecto;
            document.getElementById('modalProyectoTitulo').textContent = 'Editar Proyecto';

            setInputValue('proyectoNombre', proyecto.nombreProyecto);
            setInputValue('proyectoFecha', proyecto.fechaRealizacion);
            setInputValue('proyectoDescripcion', proyecto.descripcion);
            // ✅ ACTUALIZADO 2025-11-16: Establecer categoría seleccionada
            setInputValue('proyectoCategoria', proyecto.categoriaId);
        }
    } else {
        AppState.editando.proyecto = null;
        document.getElementById('modalProyectoTitulo').textContent = 'Agregar Proyecto';
    }

    modal.classList.add('active');
}

function cerrarModalProyecto() {
    const modal = document.getElementById('modalProyecto');
    if (modal) {
        modal.classList.remove('active');
        AppState.editando.proyecto = null;
    }
}

async function guardarProyecto(event) {
    if (event) event.preventDefault();

    const datos = {
        nombreProyecto: getInputValue('proyectoNombre'),
        fechaRealizacion: getInputValue('proyectoFecha'),
        descripcion: getInputValue('proyectoDescripcion'),
        categoriaId: parseInt(getInputValue('proyectoCategoria')) || null
    };

    if (!datos.nombreProyecto || !datos.fechaRealizacion || !datos.descripcion) {
        mostrarAlerta('Por favor completa todos los campos requeridos', 'error');
        return;
    }

    try {
        mostrarCargando(true);

        if (AppState.editando.proyecto) {
            datos.id = AppState.editando.proyecto.id;
            await PerfilProfesionalAPI.actualizarProyecto(AppState.profesionalId, datos);
            mostrarAlerta('Proyecto actualizado exitosamente', 'success');
        } else {
            await PerfilProfesionalAPI.crearProyecto(AppState.profesionalId, datos);
            mostrarAlerta('Proyecto agregado exitosamente', 'success');
        }

        cerrarModalProyecto();
        await cargarPerfilCompleto();

    } catch (error) {
        console.error('Error al guardar proyecto:', error);
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

function editarProyecto(id) {
    abrirModalProyecto(id);
}

async function eliminarProyecto(id) {
    if (!confirm('¿Eliminar este proyecto del portafolio?')) return;

    try {
        mostrarCargando(true);
        await PerfilProfesionalAPI.eliminarProyecto(AppState.profesionalId, id);
        mostrarAlerta('Proyecto eliminado exitosamente', 'success');
        await cargarPerfilCompleto();
    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

// ========================================
// IMÁGENES DE PROYECTOS
// ========================================

function gestionarImagenesProyecto(proyectoId) {
    // TODO: Implementar modal de gestión de imágenes
    mostrarAlerta('Funcionalidad de gestión de imágenes en desarrollo', 'info');
}

// ========================================
// ANTECEDENTES
// ========================================

function renderizarAntecedentes() {
    const container = document.getElementById('antecedentesContainer');
    if (!container) return;

    const tiposAntecedentes = ['policial', 'penal', 'judicial'];
    const tieneCompletos = AppState.antecedentes.length === 3;

    container.innerHTML = tiposAntecedentes.map(tipo => {
        const antecedente = AppState.antecedentes.find(a => a.tipoAntecedente === tipo);

        if (antecedente) {
            return `
                <div class="item-card">
                    <div class="item-card-header">
                        <div>
                            <div class="item-card-title">Antecedente ${tipo.charAt(0).toUpperCase() + tipo.slice(1)}</div>
                            <div class="item-card-subtitle">Fecha emisión: ${formatearFecha(antecedente.fechaEmision)}</div>
                        </div>
                        <div>
                            ${antecedente.verificado ?
                                '<span class="badge badge-success">✓ Verificado</span>' :
                                '<span class="badge badge-warning">⏳ Pendiente verificación</span>'}
                        </div>
                    </div>
                    <div class="item-card-body">
                        ${antecedente.observaciones ? `<p>${escapeHtml(antecedente.observaciones)}</p>` : ''}
                    </div>
                    <div class="item-card-footer">
                        <div>
                            ${antecedente.documentoUrl ?
                                `<a href="${antecedente.documentoUrl}" target="_blank" class="btn btn-sm btn-secondary">📄 Ver documento</a>` : ''}
                        </div>
                        <div>
                            <button class="btn btn-sm btn-secondary" onclick="editarAntecedente(${antecedente.id})">✏️ Editar</button>
                            <button class="btn btn-sm btn-danger" onclick="eliminarAntecedente(${antecedente.id})">🗑️ Eliminar</button>
                        </div>
                    </div>
                </div>
            `;
        } else {
            return `
                <div class="item-card" style="opacity: 0.6;">
                    <div class="item-card-header">
                        <div class="item-card-title">Antecedente ${tipo.charAt(0).toUpperCase() + tipo.slice(1)}</div>
                    </div>
                    <div class="item-card-body">
                        <p class="text-center"><small>No agregado</small></p>
                    </div>
                    <div class="item-card-footer">
                        <button class="btn btn-sm btn-primary" onclick="abrirModalAntecedente('${tipo}')">➕ Agregar</button>
                    </div>
                </div>
            `;
        }
    }).join('');

    // Mostrar mensaje si tiene todos verificados
    if (tieneCompletos) {
        const verificados = AppState.antecedentes.filter(a => a.verificado).length;
        if (verificados === 3) {
            mostrarAlerta('¡Excelente! Tienes todos los antecedentes verificados. Esto mejora tu puntuación.', 'success');
        }
    }
}

function abrirModalAntecedente(tipo = null, antecedenteId = null) {
    const modal = document.getElementById('modalAntecedente');
    if (!modal) return;

    const form = document.getElementById('formAntecedente');
    if (form) form.reset();

    if (antecedenteId) {
        const antecedente = AppState.antecedentes.find(a => a.id === antecedenteId);
        if (antecedente) {
            AppState.editando.antecedente = antecedente;
            document.getElementById('modalAntecedenteTitulo').textContent = 'Editar Antecedente';

            setInputValue('antecedenteTipo', antecedente.tipoAntecedente);
            setInputValue('antecedenteFecha', antecedente.fechaEmision);
            setInputValue('antecedenteObservaciones', antecedente.observaciones);
            setInputValue('antecedenteDocumento', antecedente.documentoUrl);

            // Deshabilitar cambio de tipo
            document.getElementById('antecedenteTipo').disabled = true;
        }
    } else {
        AppState.editando.antecedente = null;
        document.getElementById('modalAntecedenteTitulo').textContent = 'Agregar Antecedente';

        if (tipo) {
            setInputValue('antecedenteTipo', tipo);
        }
    }

    modal.classList.add('active');
}

function cerrarModalAntecedente() {
    const modal = document.getElementById('modalAntecedente');
    if (modal) {
        modal.classList.remove('active');
        AppState.editando.antecedente = null;
        document.getElementById('antecedenteTipo').disabled = false;
    }
}

async function guardarAntecedente(event) {
    if (event) event.preventDefault();

    const datos = {
        tipoAntecedente: getInputValue('antecedenteTipo'),
        fechaEmision: getInputValue('antecedenteFecha'),
        observaciones: getInputValue('antecedenteObservaciones'),
        documentoUrl: getInputValue('antecedenteDocumento')
    };

    if (!datos.tipoAntecedente || !datos.documentoUrl) {
        mostrarAlerta('Completa todos los campos requeridos', 'error');
        return;
    }

    try {
        mostrarCargando(true);

        if (AppState.editando.antecedente) {
            datos.id = AppState.editando.antecedente.id;
            await PerfilProfesionalAPI.actualizarAntecedente(datos);
            mostrarAlerta('Antecedente actualizado. Será verificado por un administrador.', 'success');
        } else {
            await PerfilProfesionalAPI.crearAntecedente(datos);
            mostrarAlerta('Antecedente agregado. Será verificado por un administrador.', 'success');
        }

        cerrarModalAntecedente();
        await cargarPerfilCompleto();

    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

function editarAntecedente(id) {
    abrirModalAntecedente(null, id);
}

async function eliminarAntecedente(id) {
    if (!confirm('¿Eliminar este antecedente?')) return;

    try {
        mostrarCargando(true);
        await PerfilProfesionalAPI.eliminarAntecedente(id);
        mostrarAlerta('Antecedente eliminado', 'success');
        await cargarPerfilCompleto();
    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

// ========================================
// REDES SOCIALES
// ========================================

function renderizarRedesSociales() {
    const container = document.getElementById('redesSocialesContainer');
    if (!container) return;

    if (AppState.redesSociales.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="empty-state-icon">🌐</div>
                <p class="empty-state-message">No has agregado redes sociales</p>
            </div>
        `;
        return;
    }

    container.innerHTML = AppState.redesSociales.map(red => `
        <div class="item-card">
            <div class="item-card-header">
                <div>
                    <div class="item-card-title">${getIconoRedSocial(red.tipoRed)} ${escapeHtml(red.tipoRed)}</div>
                </div>
            </div>
            <div class="item-card-body">
                <a href="${escapeHtml(red.url)}" target="_blank" class="red-url">${escapeHtml(red.url)}</a>
            </div>
            <div class="item-card-footer">
                <div></div>
                <div>
                    <button class="btn btn-sm btn-secondary" onclick="editarRedSocial(${red.id})">✏️ Editar</button>
                    <button class="btn btn-sm btn-danger" onclick="eliminarRedSocial(${red.id})">🗑️ Eliminar</button>
                </div>
            </div>
        </div>
    `).join('');
}

function abrirModalRedSocial(redId = null) {
    const modal = document.getElementById('modalRedSocial');
    if (!modal) return;

    const form = document.getElementById('formRedSocial');
    if (form) form.reset();

    if (redId) {
        const red = AppState.redesSociales.find(r => r.id === redId);
        if (red) {
            AppState.editando.redSocial = red;
            document.getElementById('modalRedSocialTitulo').textContent = 'Editar Red Social';

            setInputValue('redSocialTipo', red.tipoRed);
            setInputValue('redSocialUrl', red.url);
        }
    } else {
        AppState.editando.redSocial = null;
        document.getElementById('modalRedSocialTitulo').textContent = 'Agregar Red Social';
    }

    modal.classList.add('active');
}

function cerrarModalRedSocial() {
    const modal = document.getElementById('modalRedSocial');
    if (modal) {
        modal.classList.remove('active');
        AppState.editando.redSocial = null;
    }
}

async function guardarRedSocial(event) {
    if (event) event.preventDefault();

    const datos = {
        tipoRed: getInputValue('redSocialTipo'),
        url: getInputValue('redSocialUrl')
    };

    if (!datos.tipoRed || !datos.url) {
        mostrarAlerta('Completa todos los campos', 'error');
        return;
    }

    try {
        mostrarCargando(true);

        if (AppState.editando.redSocial) {
            datos.id = AppState.editando.redSocial.id;
            // ✅ ACTUALIZADO 2025-12-04: Pasar profesionalId desde AppState
            await PerfilProfesionalAPI.actualizarRedSocial(AppState.profesionalId, datos);
            mostrarAlerta('Red social actualizada', 'success');
        } else {
            // ✅ ACTUALIZADO 2025-12-04: Pasar profesionalId desde AppState
            await PerfilProfesionalAPI.crearRedSocial(AppState.profesionalId, datos);
            mostrarAlerta('Red social agregada', 'success');
        }

        cerrarModalRedSocial();
        await cargarPerfilCompleto();

    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

function editarRedSocial(id) {
    abrirModalRedSocial(id);
}

async function eliminarRedSocial(id) {
    if (!confirm('¿Eliminar esta red social?')) return;

    try {
        mostrarCargando(true);
        // ✅ ACTUALIZADO 2025-12-04: Pasar profesionalId desde AppState
        await PerfilProfesionalAPI.eliminarRedSocial(AppState.profesionalId, id);
        mostrarAlerta('Red social eliminada', 'success');
        await cargarPerfilCompleto();
    } catch (error) {
        mostrarAlerta('Error: ' + error.message, 'error');
    } finally {
        mostrarCargando(false);
    }
}

// ========================================
// UTILIDADES
// ========================================

function mostrarCargando(mostrar) {
    const loading = document.getElementById('loadingContainer');
    const content = document.getElementById('formContent');

    if (loading) loading.style.display = mostrar ? 'flex' : 'none';
    if (content) content.style.display = mostrar ? 'none' : 'block';
}

function mostrarAlerta(mensaje, tipo = 'info') {
    const container = document.getElementById('alertContainer');
    if (!container) return;

    const alert = document.createElement('div');
    alert.className = `alert alert-${tipo} show`;
    alert.textContent = mensaje;

    container.innerHTML = '';
    container.appendChild(alert);

    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 5000);
}

function setInputValue(id, value) {
    const input = document.getElementById(id);
    if (input) input.value = value || '';
}

function getInputValue(id) {
    const input = document.getElementById(id);
    return input ? input.value.trim() : '';
}

function formatearFecha(fecha) {
    if (!fecha) return '';
    const date = new Date(fecha);
    return date.toLocaleDateString('es-PE', { year: 'numeric', month: 'long', day: 'numeric' });
}

function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.replace(/[&<>"']/g, m => map[m]);
}

function generarEstrellas(calificacion) {
    const estrellasLlenas = Math.floor(calificacion);
    let html = '';

    for (let i = 0; i < 10; i++) {
        html += i < estrellasLlenas ? '<span class="star">★</span>' : '<span class="star empty">☆</span>';
    }

    return html;
}

function getIconoRedSocial(tipo) {
    const iconos = {
        'Facebook': '📘',
        'Instagram': '📷',
        'LinkedIn': '💼',
        'Twitter': '🐦',
        'YouTube': '📹',
        'TikTok': '🎵',
        'WhatsApp': '📱',
        'Website': '🌐'
    };
    return iconos[tipo] || '🔗';
}

function actualizarContadorBiografia() {
    const textarea = document.getElementById('biografiaProfesional');
    const counter = document.getElementById('contadorBiografia');

    if (!textarea || !counter) return;

    const current = textarea.value.length;
    const max = 500;

    counter.textContent = `${current}/${max} caracteres`;

    if (current > max * 0.9) {
        counter.classList.add('warning');
    } else {
        counter.classList.remove('warning');
    }

    if (current >= max) {
        counter.classList.add('error');
    } else {
        counter.classList.remove('error');
    }
}

// ========================================
// GESTIÓN DE CHIPS - IDIOMAS Y MÉTODOS DE PAGO
// Actualizado: 2025-11-16
// ========================================

/**
 * Agrega un idioma seleccionado a la lista de chips.
 * Valida que no esté duplicado y que se haya seleccionado un idioma válido.
 */
function agregarIdioma() {
    const selector = document.getElementById('idiomaSelector');
    const idioma = selector.value.trim();

    // Validar que se haya seleccionado un idioma
    if (!idioma) {
        mostrarAlerta('Por favor selecciona un idioma', 'warning');
        return;
    }

    // Validar que no esté duplicado
    if (AppState.idiomasSeleccionados.includes(idioma)) {
        mostrarAlerta('Este idioma ya está agregado', 'warning');
        selector.value = '';
        return;
    }

    // Agregar al array
    AppState.idiomasSeleccionados.push(idioma);

    // Renderizar chips
    renderizarIdiomasChips();

    // Resetear selector
    selector.value = '';

    console.log('Idioma agregado:', idioma, 'Total:', AppState.idiomasSeleccionados);
}

/**
 * Elimina un idioma de la lista de chips.
 *
 * @param {string} idioma - El idioma a eliminar
 */
function eliminarIdioma(idioma) {
    AppState.idiomasSeleccionados = AppState.idiomasSeleccionados.filter(i => i !== idioma);
    renderizarIdiomasChips();
    console.log('Idioma eliminado:', idioma, 'Total:', AppState.idiomasSeleccionados);
}

/**
 * Renderiza los chips de idiomas seleccionados en el contenedor.
 */
function renderizarIdiomasChips() {
    const container = document.getElementById('idiomasChips');
    if (!container) return;

    // Si no hay idiomas, mostrar contenedor vacío
    if (AppState.idiomasSeleccionados.length === 0) {
        container.innerHTML = '';
        return;
    }

    // Renderizar chips
    container.innerHTML = AppState.idiomasSeleccionados.map(idioma => `
        <div class="chip">
            <span>${escapeHtml(idioma)}</span>
            <button type="button" class="chip-remove" onclick="eliminarIdioma('${escapeHtml(idioma)}')" title="Eliminar">
                ✕
            </button>
        </div>
    `).join('');
}

/**
 * Agrega un método de pago seleccionado a la lista de chips.
 * Valida que no esté duplicado y que se haya seleccionado un método válido.
 */
function agregarMetodoPago() {
    const selector = document.getElementById('metodoPagoSelector');
    const metodoPago = selector.value.trim();

    // Validar que se haya seleccionado un método
    if (!metodoPago) {
        mostrarAlerta('Por favor selecciona un método de pago', 'warning');
        return;
    }

    // Validar que no esté duplicado
    if (AppState.metodosPagoSeleccionados.includes(metodoPago)) {
        mostrarAlerta('Este método de pago ya está agregado', 'warning');
        selector.value = '';
        return;
    }

    // Agregar al array
    AppState.metodosPagoSeleccionados.push(metodoPago);

    // Renderizar chips
    renderizarMetodosPagoChips();

    // Resetear selector
    selector.value = '';

    console.log('Método de pago agregado:', metodoPago, 'Total:', AppState.metodosPagoSeleccionados);
}

/**
 * Elimina un método de pago de la lista de chips.
 *
 * @param {string} metodoPago - El método de pago a eliminar
 */
function eliminarMetodoPago(metodoPago) {
    AppState.metodosPagoSeleccionados = AppState.metodosPagoSeleccionados.filter(m => m !== metodoPago);
    renderizarMetodosPagoChips();
    console.log('Método de pago eliminado:', metodoPago, 'Total:', AppState.metodosPagoSeleccionados);
}

/**
 * Renderiza los chips de métodos de pago seleccionados en el contenedor.
 */
function renderizarMetodosPagoChips() {
    const container = document.getElementById('metodosPagoChips');
    if (!container) return;

    // Si no hay métodos de pago, mostrar contenedor vacío
    if (AppState.metodosPagoSeleccionados.length === 0) {
        container.innerHTML = '';
        return;
    }

    // Renderizar chips
    container.innerHTML = AppState.metodosPagoSeleccionados.map(metodo => `
        <div class="chip">
            <span>${escapeHtml(metodo)}</span>
            <button type="button" class="chip-remove" onclick="eliminarMetodoPago('${escapeHtml(metodo)}')" title="Eliminar">
                ✕
            </button>
        </div>
    `).join('');
}
