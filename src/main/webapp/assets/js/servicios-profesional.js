/**
 * Servicios-profesional.js 
 * Servicios Profesional - JavaScript
 * Gestiona el formulario de configuración de servicios profesionales
 * 
 * VERSIÓN CORREGIDA v2:
 * - Usa categoriaId desde la BD
 * - Dropdowns en cascada para ubicaciones (departamento → provincia → distrito)
 * - Valores por defecto: "Todo el país" y "Todo el tiempo" activados
 */

// Estado de la aplicación
const appState = {
    usuarioId: null,
    categorias: [],
    departamentos: [],      // ✅ NUEVO - Lista de departamentos
    provincias: [],         // ✅ NUEVO - Provincias del departamento seleccionado
    distritos: [],          // ✅ NUEVO - Distritos de la provincia seleccionada
    especialidades: [],
    ubicaciones: [],
    horarios: [],
    todoPais: true,         // ✅ CAMBIADO - Por defecto activado
    todoTiempo: true,       // ✅ CAMBIADO - Por defecto activado
    modoEdicion: false
};

// Datos de referencia
const diasSemana = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'];

// =====================================================================
// INICIALIZACIÓN
// =====================================================================

document.addEventListener('DOMContentLoaded', async () => {
    console.log('Inicializando formulario de servicios profesionales...');

    // Obtener datos del usuario
    const userData = JSON.parse(localStorage.getItem('userData') || '{}');

    if (!userData.id) {
        mostrarAlerta('error', 'Error: No se pudo obtener información del usuario');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }

    appState.usuarioId = userData.id;
    console.log('✓ ID del usuario:', appState.usuarioId);

    // Cargar categorías y departamentos
    await Promise.all([
        cargarCategorias(),
        cargarDepartamentos()
    ]);

    // Configurar event listeners
    configurarEventListeners();

    // ✅ NUEVO - Establecer valores por defecto en los checkboxes
    document.getElementById('todoPaisCheckbox').checked = true;
    document.getElementById('todoTiempoCheckbox').checked = true;
    
    // Aplicar estado inicial (ocultar secciones)
    toggleTodoPais();
    toggleTodoTiempo();

    // Cargar datos existentes si los hay
    await cargarDatosExistentes();

    // Si no hay datos, agregar una especialidad por defecto
    if (appState.especialidades.length === 0) {
        agregarEspecialidad();
    }
});

// =====================================================================
// CARGAR DATOS DE REFERENCIA
// =====================================================================

async function cargarCategorias() {
    console.log('Cargando categorías de servicio desde la BD...');
    mostrarLoading(true);

    try {
        const response = await fetch('./api/categorias');
        const data = await response.json();

        if (data.success && data.data) {
            appState.categorias = data.data;
            console.log(`✓ Categorías cargadas: ${appState.categorias.length}`);
        } else {
            throw new Error('No se pudieron cargar las categorías');
        }
    } catch (error) {
        console.error('Error cargando categorías:', error);
        mostrarAlerta('error', 'Error al cargar las categorías de servicio. Por favor, recargue la página.');
        
        // Categorías de respaldo
        appState.categorias = [
            { id: 1, nombre: 'Electricista', icono: '⚡', color: '#FFD700' },
            { id: 2, nombre: 'Plomero', icono: '🔧', color: '#4169E1' },
            { id: 3, nombre: 'Carpintero', icono: '🪚', color: '#8B4513' },
            { id: 4, nombre: 'Pintor', icono: '🎨', color: '#FF6347' },
            { id: 5, nombre: 'Albañil', icono: '🧱', color: '#A0522D' }
        ];
    } finally {
        mostrarLoading(false);
    }
}

// ✅ NUEVO - Cargar departamentos desde la BD
async function cargarDepartamentos() {
    console.log('Cargando departamentos desde la BD...');

    try {
        const response = await fetch('./api/ubicacion/departamentos');
        const data = await response.json();

        if (data.success && data.data) {
            appState.departamentos = data.data;
            console.log(`✓ Departamentos cargados: ${appState.departamentos.length}`);
        } else {
            throw new Error('No se pudieron cargar los departamentos');
        }
    } catch (error) {
        console.error('Error cargando departamentos:', error);
        mostrarAlerta('warning', 'No se pudieron cargar los departamentos desde el servidor. Usando lista local.');
        
        // Fallback - departamentos hardcoded
        const departamentosLocal = [
            'Amazonas', 'Áncash', 'Apurímac', 'Arequipa', 'Ayacucho', 'Cajamarca', 'Callao', 'Cusco',
            'Huancavelica', 'Huánuco', 'Ica', 'Junín', 'La Libertad', 'Lambayeque', 'Lima',
            'Loreto', 'Madre de Dios', 'Moquegua', 'Pasco', 'Piura', 'Puno', 'San Martín',
            'Tacna', 'Tumbes', 'Ucayali'
        ];
        
        appState.departamentos = departamentosLocal.map((nombre, index) => ({
            id: index + 1,
            nombre: nombre
        }));
    }
}

// ✅ NUEVO - Cargar provincias de un departamento
async function cargarProvincias(departamentoId) {
    console.log('Cargando provincias del departamento ID:', departamentoId);

    try {
        const response = await fetch(`./api/ubicacion/provincias?departamentoId=${departamentoId}`);
        const data = await response.json();

        if (data.success && data.data) {
            appState.provincias = data.data;
            console.log(`✓ Provincias cargadas: ${appState.provincias.length}`);
            return appState.provincias;
        } else {
            throw new Error('No se pudieron cargar las provincias');
        }
    } catch (error) {
        console.error('Error cargando provincias:', error);
        appState.provincias = [];
        return [];
    }
}

// ✅ NUEVO - Cargar distritos de una provincia
async function cargarDistritos(provinciaId) {
    console.log('Cargando distritos de la provincia ID:', provinciaId);

    try {
        const response = await fetch(`./api/ubicacion/distritos?provinciaId=${provinciaId}`);
        const data = await response.json();

        if (data.success && data.data) {
            appState.distritos = data.data;
            console.log(`✓ Distritos cargados: ${appState.distritos.length}`);
            return appState.distritos;
        } else {
            throw new Error('No se pudieron cargar los distritos');
        }
    } catch (error) {
        console.error('Error cargando distritos:', error);
        appState.distritos = [];
        return [];
    }
}

// =====================================================================
// CONFIGURACIÓN DE EVENT LISTENERS
// =====================================================================

function configurarEventListeners() {
    // Botones de agregar
    document.getElementById('btnAgregarEspecialidad').addEventListener('click', agregarEspecialidad);
    document.getElementById('btnAgregarUbicacion').addEventListener('click', agregarUbicacion);
    document.getElementById('btnAgregarHorario').addEventListener('click', agregarHorario);

    // Toggles
    document.getElementById('todoPaisCheckbox').addEventListener('change', toggleTodoPais);
    document.getElementById('todoTiempoCheckbox').addEventListener('change', toggleTodoTiempo);

    // Formulario
    document.getElementById('serviciosProfesionalForm').addEventListener('submit', enviarFormulario);
}

// =====================================================================
// CARGAR DATOS EXISTENTES
// =====================================================================

async function cargarDatosExistentes() {
    mostrarLoading(true);

    try {
        const response = await fetch(`./api/servicios-profesional?usuarioId=${appState.usuarioId}`);
        const data = await response.json();

        if (data.success && data.data) {
            const servicios = data.data;

            // Cargar especialidades
            if (servicios.especialidades && servicios.especialidades.length > 0) {
                appState.modoEdicion = true;
                servicios.especialidades.forEach(esp => {
                    agregarEspecialidad(esp);
                });
            }

            // Cargar área de servicio
            if (servicios.areaServicio) {
                appState.todoPais = servicios.areaServicio.todoPais;
                document.getElementById('todoPaisCheckbox').checked = appState.todoPais;

                if (!appState.todoPais && servicios.areaServicio.ubicaciones) {
                    // Agregar cada ubicación y cargar sus dependencias
                    for (const ub of servicios.areaServicio.ubicaciones) {
                        // ✅ Buscar los IDs basándose en los nombres que vienen del backend
                        const ubicacionConIds = { ...ub };

                        // Buscar departamentoId
                        if (ub.departamento) {
                            const dept = appState.departamentos.find(d =>
                                d.nombre.toLowerCase() === ub.departamento.toLowerCase()
                            );
                            if (dept) {
                                ubicacionConIds.departamentoId = dept.id;
                            }
                        }

                        agregarUbicacion(ubicacionConIds);

                        // Cargar provincias y distritos para ubicaciones existentes
                        const index = appState.ubicaciones.length - 1;
                        const ubicacion = appState.ubicaciones[index];

                        if (ubicacion.departamentoId) {
                            // Cargar provincias si el tipo lo requiere
                            if (ubicacion.tipoUbicacion !== 'departamento') {
                                ubicacion.provinciasDisponibles = await cargarProvincias(ubicacion.departamentoId);

                                // Buscar provinciaId
                                if (ub.provincia && ubicacion.provinciasDisponibles.length > 0) {
                                    const prov = ubicacion.provinciasDisponibles.find(p =>
                                        p.nombre.toLowerCase() === ub.provincia.toLowerCase()
                                    );
                                    if (prov) {
                                        ubicacion.provinciaId = prov.id;
                                    }
                                }
                            }

                            // Cargar distritos si el tipo lo requiere y tiene provincia
                            if (ubicacion.tipoUbicacion === 'distrito' && ubicacion.provinciaId) {
                                ubicacion.distritosDisponibles = await cargarDistritos(ubicacion.provinciaId);

                                // Buscar distritoId
                                if (ub.distrito && ubicacion.distritosDisponibles.length > 0) {
                                    const dist = ubicacion.distritosDisponibles.find(d =>
                                        d.nombre.toLowerCase() === ub.distrito.toLowerCase()
                                    );
                                    if (dist) {
                                        ubicacion.distritoId = dist.id;
                                    }
                                }
                            }
                        }
                    }

                    // Re-renderizar las ubicaciones con los dropdowns actualizados
                    renderizarUbicaciones();
                }

                toggleTodoPais();
            }

            // Cargar disponibilidad
            if (servicios.disponibilidad) {
                appState.todoTiempo = servicios.disponibilidad.todoTiempo;
                document.getElementById('todoTiempoCheckbox').checked = appState.todoTiempo;

                if (!appState.todoTiempo && servicios.disponibilidad.horariosDias) {
                    servicios.disponibilidad.horariosDias.forEach(hor => {
                        agregarHorario(hor);
                    });
                }

                toggleTodoTiempo();
            }

            if (appState.modoEdicion) {
                mostrarAlerta('info', 'Se cargaron los datos de servicios existentes. Puede editarlos y guardar los cambios.');
            }
        }
    } catch (error) {
        console.error('Error cargando datos:', error);
        // No mostramos error si no hay datos previos
    } finally {
        mostrarLoading(false);
    }
}

// =====================================================================
// ESPECIALIDADES
// =====================================================================

function agregarEspecialidad(datosExistentes = null) {
    if (appState.especialidades.length >= 3) {
        mostrarAlerta('error', 'No puede registrar más de 3 especialidades');
        return;
    }

    const index = appState.especialidades.length;
    const esPrimera = index === 0;

    const especialidad = {
        orden: index + 1,
        categoriaId: datosExistentes?.categoriaId || (appState.categorias.length > 0 ? appState.categorias[0].id : null),
        categoriaNombre: datosExistentes?.categoriaNombre || (appState.categorias.length > 0 ? appState.categorias[0].nombre : ''),
        servicioProfesional: datosExistentes?.servicioProfesional || '', // ✅ NUEVO campo obligatorio
        descripcion: datosExistentes?.descripcion || '',
        incluyeMateriales: datosExistentes?.incluyeMateriales || false,
        costo: datosExistentes?.costo || '',
        tipoCosto: datosExistentes?.tipoCosto || 'hora',
        esPrincipal: datosExistentes?.esPrincipal || esPrimera
    };

    appState.especialidades.push(especialidad);
    renderizarEspecialidades();
    actualizarBotonesEspecialidades();
}

function eliminarEspecialidad(index) {
    if (appState.especialidades.length === 1) {
        mostrarAlerta('error', 'Debe mantener al menos una especialidad');
        return;
    }

    const eraPrincipal = appState.especialidades[index].esPrincipal;
    appState.especialidades.splice(index, 1);

    if (eraPrincipal && appState.especialidades.length > 0) {
        appState.especialidades[0].esPrincipal = true;
    }

    appState.especialidades.forEach((esp, i) => {
        esp.orden = i + 1;
    });

    renderizarEspecialidades();
    actualizarBotonesEspecialidades();
}

function marcarComoPrincipal(index) {
    appState.especialidades.forEach((esp, i) => {
        esp.esPrincipal = (i === index);
    });
    renderizarEspecialidades();
}

function renderizarEspecialidades() {
    const container = document.getElementById('especialidadesContainer');
    container.innerHTML = '';

    if (appState.categorias.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--error-color); padding: 20px;">⚠️ No se pudieron cargar las categorías. Por favor, recargue la página.</p>';
        return;
    }

    appState.especialidades.forEach((esp, index) => {
        const div = document.createElement('div');
        div.className = 'especialidad-item';
        div.innerHTML = `
            <div class="especialidad-header">
                <div>
                    <span class="especialidad-numero">Especialidad ${esp.orden}</span>
                    ${esp.esPrincipal ? '<span class="principal-badge">Principal</span>' : ''}
                </div>
                ${appState.especialidades.length > 1 ?
                    `<button type="button" class="btn-eliminar-especialidad" onclick="eliminarEspecialidad(${index})">
                        🗑️ Eliminar
                    </button>` : ''}
            </div>

            <div class="form-group">
                <label class="required">Categoría de Servicio</label>
                <select class="form-select"
                        onchange="actualizarCategoriaEspecialidad(${index}, this.value)"
                        required>
                    ${appState.categorias.map(cat =>
                        `<option value="${cat.id}" ${esp.categoriaId === cat.id ? 'selected' : ''}>
                            ${cat.nombre}
                        </option>`
                    ).join('')}
                </select>
            </div>

            <div class="form-group">
                <label class="required">Servicio Profesional</label>
                <input type="text" class="form-input"
                       value="${esp.servicioProfesional || ''}"
                       onchange="actualizarEspecialidad(${index}, 'servicioProfesional', this.value)"
                       placeholder="Ej: Instalación eléctrica residencial, Mantenimiento de aires acondicionados..."
                       maxlength="255"
                       required>
                <small style="color: var(--medium-gray); font-size: 13px;">
                    Escriba el nombre específico del servicio que brindará
                </small>
            </div>

            <div class="form-group">
                <label>Descripción del Servicio</label>
                <textarea class="form-textarea"
                          onchange="actualizarEspecialidad(${index}, 'descripcion', this.value)"
                          placeholder="Describa brevemente el servicio que ofrece en esta especialidad..."
                >${esp.descripcion}</textarea>
            </div>

            <div class="checkbox-container">
                <input type="checkbox" id="incluye_materiales_${index}"
                       ${esp.incluyeMateriales ? 'checked' : ''}
                       onchange="actualizarEspecialidad(${index}, 'incluyeMateriales', this.checked)">
                <label for="incluye_materiales_${index}">El servicio incluye materiales</label>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="required">Costo</label>
                    <input type="number" class="form-input"
                           value="${esp.costo}"
                           onchange="actualizarEspecialidad(${index}, 'costo', this.value)"
                           min="0" step="0.01"
                           placeholder="0.00"
                           required>
                </div>

                <div class="form-group">
                    <label class="required">Tipo de Tarifa</label>
                    <select class="form-select"
                            onchange="actualizarEspecialidad(${index}, 'tipoCosto', this.value)"
                            required>
                        <option value="hora" ${esp.tipoCosto === 'hora' ? 'selected' : ''}>Por Hora</option>
                        <option value="dia" ${esp.tipoCosto === 'dia' ? 'selected' : ''}>Por Día</option>
                        <option value="mes" ${esp.tipoCosto === 'mes' ? 'selected' : ''}>Por Mes</option>
                    </select>
                </div>
            </div>

            ${!esp.esPrincipal ? `
                <div style="margin-top: 15px;">
                    <button type="button" class="btn-agregar" onclick="marcarComoPrincipal(${index})" style="background: var(--success-color);">
                        ⭐ Marcar como Principal
                    </button>
                </div>
            ` : ''}
        `;

        container.appendChild(div);
    });
}

function actualizarCategoriaEspecialidad(index, categoriaId) {
    categoriaId = parseInt(categoriaId);
    
    if (appState.especialidades[index]) {
        appState.especialidades[index].categoriaId = categoriaId;
        
        const categoria = appState.categorias.find(c => c.id === categoriaId);
        if (categoria) {
            appState.especialidades[index].categoriaNombre = categoria.nombre;
        }
    }
}

function actualizarEspecialidad(index, campo, valor) {
    if (appState.especialidades[index]) {
        appState.especialidades[index][campo] = valor;
    }
}

function actualizarBotonesEspecialidades() {
    const btnAgregar = document.getElementById('btnAgregarEspecialidad');
    btnAgregar.disabled = appState.especialidades.length >= 3;
}

// =====================================================================
// ÁREA DE SERVICIO - ✅ CORREGIDO CON CASCADA
// =====================================================================

function toggleTodoPais() {
    appState.todoPais = document.getElementById('todoPaisCheckbox').checked;
    const ubicacionesSection = document.getElementById('ubicacionesSection');

    if (appState.todoPais) {
        ubicacionesSection.classList.add('hidden');
        appState.ubicaciones = [];
        renderizarUbicaciones();
    } else {
        ubicacionesSection.classList.remove('hidden');
        if (appState.ubicaciones.length === 0) {
            agregarUbicacion();
        }
    }
}

function agregarUbicacion(datosExistentes = null) {
    if (appState.ubicaciones.length >= 10) {
        mostrarAlerta('error', 'No puede agregar más de 10 ubicaciones');
        return;
    }

    const index = appState.ubicaciones.length;

    const ubicacion = {
        orden: index + 1,
        tipoUbicacion: datosExistentes?.tipoUbicacion || 'departamento',
        departamentoId: datosExistentes?.departamentoId || null,
        departamento: datosExistentes?.departamento || '',
        provinciaId: datosExistentes?.provinciaId || null,
        provincia: datosExistentes?.provincia || '',
        distritoId: datosExistentes?.distritoId || null,
        distrito: datosExistentes?.distrito || '',
        provinciasDisponibles: [],
        distritosDisponibles: []
    };

    appState.ubicaciones.push(ubicacion);
    renderizarUbicaciones();
    actualizarBotonesUbicaciones();
}

function eliminarUbicacion(index) {
    appState.ubicaciones.splice(index, 1);

    appState.ubicaciones.forEach((ub, i) => {
        ub.orden = i + 1;
    });

    renderizarUbicaciones();
    actualizarBotonesUbicaciones();
}

// ✅ NUEVO - Manejar cambio de departamento (cargar provincias)
async function onDepartamentoChange(index, departamentoId) {
    const ubicacion = appState.ubicaciones[index];
    if (!ubicacion) return;

    ubicacion.departamentoId = parseInt(departamentoId);
    
    // Buscar el nombre del departamento
    const dept = appState.departamentos.find(d => d.id === ubicacion.departamentoId);
    if (dept) {
        ubicacion.departamento = dept.nombre;
    }

    // Resetear provincia y distrito
    ubicacion.provinciaId = null;
    ubicacion.provincia = '';
    ubicacion.distritoId = null;
    ubicacion.distrito = '';
    ubicacion.distritosDisponibles = [];

    // Cargar provincias
    if (ubicacion.tipoUbicacion !== 'departamento') {
        ubicacion.provinciasDisponibles = await cargarProvincias(departamentoId);
    }

    renderizarUbicaciones();
}

// ✅ NUEVO - Manejar cambio de provincia (cargar distritos)
async function onProvinciaChange(index, provinciaId) {
    const ubicacion = appState.ubicaciones[index];
    if (!ubicacion) return;

    ubicacion.provinciaId = parseInt(provinciaId);
    
    // Buscar el nombre de la provincia
    const prov = ubicacion.provinciasDisponibles.find(p => p.id === ubicacion.provinciaId);
    if (prov) {
        ubicacion.provincia = prov.nombre;
    }

    // Resetear distrito
    ubicacion.distritoId = null;
    ubicacion.distrito = '';

    // Cargar distritos
    if (ubicacion.tipoUbicacion === 'distrito') {
        ubicacion.distritosDisponibles = await cargarDistritos(provinciaId);
    }

    renderizarUbicaciones();
}

// ✅ NUEVO - Manejar cambio de distrito
function onDistritoChange(index, distritoId) {
    const ubicacion = appState.ubicaciones[index];
    if (!ubicacion) return;

    ubicacion.distritoId = parseInt(distritoId);
    
    // Buscar el nombre del distrito
    const dist = ubicacion.distritosDisponibles.find(d => d.id === ubicacion.distritoId);
    if (dist) {
        ubicacion.distrito = dist.nombre;
    }
}

// ✅ NUEVO - Manejar cambio de tipo de ubicación
async function onTipoUbicacionChange(index, tipoUbicacion) {
    const ubicacion = appState.ubicaciones[index];
    if (!ubicacion) return;

    ubicacion.tipoUbicacion = tipoUbicacion;

    // Si cambia el tipo, cargar datos necesarios
    if (tipoUbicacion !== 'departamento' && ubicacion.departamentoId && ubicacion.provinciasDisponibles.length === 0) {
        ubicacion.provinciasDisponibles = await cargarProvincias(ubicacion.departamentoId);
    }

    if (tipoUbicacion === 'distrito' && ubicacion.provinciaId && ubicacion.distritosDisponibles.length === 0) {
        ubicacion.distritosDisponibles = await cargarDistritos(ubicacion.provinciaId);
    }

    renderizarUbicaciones();
}

function renderizarUbicaciones() {
    const container = document.getElementById('ubicacionesContainer');
    container.innerHTML = '';

    if (appState.ubicaciones.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--medium-gray); padding: 20px;">No hay ubicaciones agregadas. Haga clic en "Agregar Ubicación".</p>';
        return;
    }

    appState.ubicaciones.forEach((ub, index) => {
        const div = document.createElement('div');
        div.className = 'ubicacion-item';
        div.innerHTML = `
            <div class="ubicacion-header">
                <span class="contador">Ubicación ${ub.orden} de 10</span>
                <button type="button" class="btn-eliminar-item" onclick="eliminarUbicacion(${index})">
                    ✖ Eliminar
                </button>
            </div>

            <div class="form-group">
                <label class="required">Nivel de Ubicación</label>
                <select class="form-select"
                        onchange="onTipoUbicacionChange(${index}, this.value)"
                        required>
                    <option value="departamento" ${ub.tipoUbicacion === 'departamento' ? 'selected' : ''}>Departamento</option>
                    <option value="provincia" ${ub.tipoUbicacion === 'provincia' ? 'selected' : ''}>Provincia</option>
                    <option value="distrito" ${ub.tipoUbicacion === 'distrito' ? 'selected' : ''}>Distrito</option>
                </select>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="required">Departamento</label>
                    <select class="form-select"
                            onchange="onDepartamentoChange(${index}, this.value)"
                            required>
                        <option value="">Seleccione...</option>
                        ${appState.departamentos.map(dept =>
                            `<option value="${dept.id}" ${ub.departamentoId === dept.id ? 'selected' : ''}>${dept.nombre}</option>`
                        ).join('')}
                    </select>
                </div>

                ${ub.tipoUbicacion !== 'departamento' ? `
                    <div class="form-group">
                        <label class="required">Provincia</label>
                        <select class="form-select"
                                onchange="onProvinciaChange(${index}, this.value)"
                                ${!ub.departamentoId ? 'disabled' : ''}
                                required>
                            <option value="">Seleccione...</option>
                            ${(ub.provinciasDisponibles || []).map(prov =>
                                `<option value="${prov.id}" ${ub.provinciaId === prov.id ? 'selected' : ''}>${prov.nombre}</option>`
                            ).join('')}
                        </select>
                        ${!ub.departamentoId ? '<small style="color: var(--medium-gray);">Primero seleccione un departamento</small>' : ''}
                    </div>
                ` : ''}
            </div>

            ${ub.tipoUbicacion === 'distrito' ? `
                <div class="form-group">
                    <label class="required">Distrito</label>
                    <select class="form-select"
                            onchange="onDistritoChange(${index}, this.value)"
                            ${!ub.provinciaId ? 'disabled' : ''}
                            required>
                        <option value="">Seleccione...</option>
                        ${(ub.distritosDisponibles || []).map(dist =>
                            `<option value="${dist.id}" ${ub.distritoId === dist.id ? 'selected' : ''}>${dist.nombre}</option>`
                        ).join('')}
                    </select>
                    ${!ub.provinciaId ? '<small style="color: var(--medium-gray);">Primero seleccione una provincia</small>' : ''}
                </div>
            ` : ''}
        `;

        container.appendChild(div);
    });
}

function actualizarBotonesUbicaciones() {
    const btnAgregar = document.getElementById('btnAgregarUbicacion');
    btnAgregar.disabled = appState.ubicaciones.length >= 10;
}

// =====================================================================
// DISPONIBILIDAD HORARIA
// =====================================================================

function toggleTodoTiempo() {
    appState.todoTiempo = document.getElementById('todoTiempoCheckbox').checked;
    const horariosSection = document.getElementById('horariosSection');

    if (appState.todoTiempo) {
        horariosSection.classList.add('hidden');
        appState.horarios = [];
        renderizarHorarios();
    } else {
        horariosSection.classList.remove('hidden');
        if (appState.horarios.length === 0) {
            agregarHorario();
        }
    }
}

function agregarHorario(datosExistentes = null) {
    if (appState.horarios.length >= 7) {
        mostrarAlerta('error', 'Ya agregó todos los días de la semana');
        return;
    }

    const diasUsados = appState.horarios.map(h => h.diaSemana);
    const diasDisponibles = diasSemana.filter(d => !diasUsados.includes(d));

    if (diasDisponibles.length === 0) {
        mostrarAlerta('error', 'Ya agregó todos los días de la semana');
        return;
    }

    const horario = {
        diaSemana: datosExistentes?.diaSemana || diasDisponibles[0],
        tipoJornada: datosExistentes?.tipoJornada || '8hrs',
        horaInicio: datosExistentes?.horaInicio || '08:00',
        horaFin: datosExistentes?.horaFin || '17:00'
    };

    appState.horarios.push(horario);
    renderizarHorarios();
    actualizarBotonesHorarios();
}

function eliminarHorario(index) {
    appState.horarios.splice(index, 1);
    renderizarHorarios();
    actualizarBotonesHorarios();
}

function renderizarHorarios() {
    const container = document.getElementById('horariosContainer');
    container.innerHTML = '';

    if (appState.horarios.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--medium-gray); padding: 20px;">No hay horarios configurados. Haga clic en "Agregar Día".</p>';
        return;
    }

    const diasUsados = appState.horarios.map(h => h.diaSemana);
    const diasDisponibles = diasSemana.filter(d => !diasUsados.includes(d));

    appState.horarios.forEach((hor, index) => {
        const div = document.createElement('div');
        div.className = 'horario-item';

        const nombreDiaCapitalizado = hor.diaSemana.charAt(0).toUpperCase() + hor.diaSemana.slice(1);

        div.innerHTML = `
            <div class="horario-header">
                <span class="contador">${nombreDiaCapitalizado}</span>
                <button type="button" class="btn-eliminar-item" onclick="eliminarHorario(${index})">
                    ✖ Eliminar
                </button>
            </div>

            <div class="form-row">
                <div class="form-group">
                    <label class="required">Día de la Semana</label>
                    <select class="form-select"
                            onchange="actualizarHorario(${index}, 'diaSemana', this.value); renderizarHorarios();"
                            required>
                        <option value="${hor.diaSemana}">${nombreDiaCapitalizado}</option>
                        ${diasDisponibles.map(dia => {
                            const diaCapitalizado = dia.charAt(0).toUpperCase() + dia.slice(1);
                            return `<option value="${dia}">${diaCapitalizado}</option>`;
                        }).join('')}
                    </select>
                </div>

                <div class="form-group">
                    <label class="required">Tipo de Jornada</label>
                    <select class="form-select"
                            onchange="actualizarHorario(${index}, 'tipoJornada', this.value); renderizarHorarios();"
                            required>
                        <option value="8hrs" ${hor.tipoJornada === '8hrs' ? 'selected' : ''}>8 horas</option>
                        <option value="24hrs" ${hor.tipoJornada === '24hrs' ? 'selected' : ''}>24 horas</option>
                    </select>
                </div>
            </div>

            ${hor.tipoJornada === '8hrs' ? `
                <div class="form-row">
                    <div class="form-group">
                        <label class="required">Hora de Inicio</label>
                        <input type="time" class="form-input"
                               value="${hor.horaInicio}"
                               onchange="actualizarHorario(${index}, 'horaInicio', this.value)"
                               required>
                    </div>

                    <div class="form-group">
                        <label class="required">Hora de Fin</label>
                        <input type="time" class="form-input"
                               value="${hor.horaFin}"
                               onchange="actualizarHorario(${index}, 'horaFin', this.value)"
                               required>
                    </div>
                </div>
            ` : ''}
        `;

        container.appendChild(div);
    });
}

function actualizarHorario(index, campo, valor) {
    if (appState.horarios[index]) {
        appState.horarios[index][campo] = valor;
    }
}

function actualizarBotonesHorarios() {
    const btnAgregar = document.getElementById('btnAgregarHorario');
    btnAgregar.disabled = appState.horarios.length >= 7;
}

// =====================================================================
// VALIDACIÓN Y ENVÍO DEL FORMULARIO
// =====================================================================

async function enviarFormulario(event) {
    event.preventDefault();

    if (!validarFormulario()) {
        return;
    }

    const datosServicio = {
        usuarioId: appState.usuarioId,
        especialidades: appState.especialidades.map(esp => ({
            categoriaId: esp.categoriaId,
            servicioProfesional: esp.servicioProfesional, // ✅ NUEVO campo obligatorio
            descripcion: esp.descripcion || '',
            incluyeMateriales: esp.incluyeMateriales || false,
            costo: parseFloat(esp.costo),
            tipoCosto: esp.tipoCosto,
            esPrincipal: esp.esPrincipal || false
        })),
        areaServicio: {
            todoPais: appState.todoPais,
            ubicaciones: appState.todoPais ? [] : appState.ubicaciones.map(ub => ({
                tipoUbicacion: ub.tipoUbicacion,
                departamento: ub.departamento,
                provincia: ub.provincia || null,
                distrito: ub.distrito || null,
                orden: ub.orden
            }))
        },
        disponibilidad: {
            todoTiempo: appState.todoTiempo,
            horarios: appState.todoTiempo ? [] : appState.horarios
        }
    };

    console.log('Enviando datos:', datosServicio);
    console.log('Modo edición:', appState.modoEdicion);

    mostrarLoading(true);

    try {
        const url = './api/servicios-profesional';
        // ✅ Usar PUT cuando estamos editando, POST cuando es nuevo
        const method = appState.modoEdicion ? 'PUT' : 'POST';

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosServicio)
        });

        const data = await response.json();

        if (data.success) {
            const mensajeExito = appState.modoEdicion
                ? 'Servicios actualizados exitosamente'
                : 'Servicios creados exitosamente';
            mostrarAlerta('success', data.message || mensajeExito);

            setTimeout(() => {
                window.location.href = 'dashboard.html';
            }, 2000);
        } else {
            mostrarAlerta('error', data.error || 'Error guardando servicios');
        }

    } catch (error) {
        console.error('Error:', error);
        mostrarAlerta('error', 'Error de conexión: ' + error.message);
    } finally {
        mostrarLoading(false);
    }
}

function validarFormulario() {
    if (appState.categorias.length === 0) {
        mostrarAlerta('error', 'No se pudieron cargar las categorías de servicio. Por favor, recargue la página.');
        return false;
    }

    if (appState.especialidades.length === 0) {
        mostrarAlerta('error', 'Debe agregar al menos una especialidad');
        return false;
    }

    if (appState.especialidades.length > 3) {
        mostrarAlerta('error', 'No puede tener más de 3 especialidades');
        return false;
    }

    for (let i = 0; i < appState.especialidades.length; i++) {
        const esp = appState.especialidades[i];

        if (!esp.categoriaId) {
            mostrarAlerta('error', `La especialidad ${i + 1} debe tener una categoría seleccionada`);
            return false;
        }

        // ✅ NUEVA VALIDACIÓN: servicioProfesional es obligatorio
        if (!esp.servicioProfesional || esp.servicioProfesional.trim() === '') {
            mostrarAlerta('error', `La especialidad ${i + 1} debe tener un servicio profesional especificado`);
            return false;
        }

        if (!esp.costo || parseFloat(esp.costo) <= 0) {
            mostrarAlerta('error', `La especialidad ${i + 1} debe tener un costo válido mayor a 0`);
            return false;
        }

        if (!esp.tipoCosto) {
            mostrarAlerta('error', `La especialidad ${i + 1} debe tener un tipo de costo`);
            return false;
        }
    }

    const tienePrincipal = appState.especialidades.some(esp => esp.esPrincipal);
    if (!tienePrincipal) {
        mostrarAlerta('error', 'Debe marcar al menos una especialidad como principal');
        return false;
    }

    if (!appState.todoPais) {
        if (appState.ubicaciones.length === 0) {
            mostrarAlerta('error', 'Debe agregar al menos una ubicación de servicio o marcar "Todo el país"');
            return false;
        }

        if (appState.ubicaciones.length > 10) {
            mostrarAlerta('error', 'No puede tener más de 10 ubicaciones');
            return false;
        }

        for (let i = 0; i < appState.ubicaciones.length; i++) {
            const ub = appState.ubicaciones[i];

            if (!ub.departamento || ub.departamento.trim() === '') {
                mostrarAlerta('error', `La ubicación ${i + 1} debe tener un departamento`);
                return false;
            }

            if (ub.tipoUbicacion === 'provincia' || ub.tipoUbicacion === 'distrito') {
                if (!ub.provincia || ub.provincia.trim() === '') {
                    mostrarAlerta('error', `La ubicación ${i + 1} debe tener una provincia`);
                    return false;
                }
            }

            if (ub.tipoUbicacion === 'distrito') {
                if (!ub.distrito || ub.distrito.trim() === '') {
                    mostrarAlerta('error', `La ubicación ${i + 1} debe tener un distrito`);
                    return false;
                }
            }
        }
    }

    if (!appState.todoTiempo) {
        if (appState.horarios.length === 0) {
            mostrarAlerta('error', 'Debe agregar al menos un horario o marcar "Todo el tiempo"');
            return false;
        }

        for (let i = 0; i < appState.horarios.length; i++) {
            const hor = appState.horarios[i];

            if (!hor.diaSemana) {
                mostrarAlerta('error', `El horario ${i + 1} debe tener un día de la semana`);
                return false;
            }

            if (hor.tipoJornada === '8hrs') {
                if (!hor.horaInicio || !hor.horaFin) {
                    mostrarAlerta('error', `El horario para ${hor.diaSemana} debe tener hora de inicio y fin`);
                    return false;
                }

                if (hor.horaInicio >= hor.horaFin) {
                    mostrarAlerta('error', `El horario para ${hor.diaSemana} tiene horas inválidas (la hora de fin debe ser mayor que la de inicio)`);
                    return false;
                }
            }
        }
    }

    return true;
}

// =====================================================================
// UTILIDADES UI
// =====================================================================

function mostrarAlerta(tipo, mensaje) {
    const container = document.getElementById('alertContainer');

    const alert = document.createElement('div');
    alert.className = `alert alert-${tipo} show`;
    alert.textContent = mensaje;

    container.innerHTML = '';
    container.appendChild(alert);

    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 5000);

    window.scrollTo({ top: 0, behavior: 'smooth' });
}

function mostrarLoading(mostrar) {
    const loading = document.getElementById('loadingIndicator');
    if (mostrar) {
        loading.classList.add('show');
    } else {
        loading.classList.remove('show');
    }
}