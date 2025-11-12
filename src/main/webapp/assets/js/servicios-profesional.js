/**
 * Servicios Profesional - JavaScript
 * Gestiona el formulario de configuración de servicios profesionales
 */

// Estado de la aplicación
const appState = {
    profesionalId: null,
    especialidades: [],
    ubicaciones: [],
    horarios: [],
    todoPais: false,
    todoTiempo: false,
    modoEdicion: false,
    categoriasServicio: [] // NUEVO: Almacena las categorías de servicio
};

// Datos de referencia
const diasSemana = ['lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'];
const departamentosPeru = [
    'Amazonas', 'Áncash', 'Apurímac', 'Arequipa', 'Ayacucho', 'Cajamarca', 'Callao', 'Cusco',
    'Huancavelica', 'Huánuco', 'Ica', 'Junín', 'La Libertad', 'Lambayeque', 'Lima',
    'Loreto', 'Madre de Dios', 'Moquegua', 'Pasco', 'Piura', 'Puno', 'San Martín',
    'Tacna', 'Tumbes', 'Ucayali'
];

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

    appState.profesionalId = userData.id;

    // Configurar event listeners
    configurarEventListeners();

    // NUEVO: Cargar categorías de servicio primero
    await cargarCategoriasServicio();

    // Cargar datos existentes si los hay
    await cargarDatosExistentes();

    // Si no hay datos, agregar una especialidad por defecto
    if (appState.especialidades.length === 0) {
        agregarEspecialidad();
    }
});

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
// CARGAR CATEGORÍAS DE SERVICIO
// =====================================================================

async function cargarCategoriasServicio() {
    try {
        const response = await fetch('./api/categorias-servicio');
        const data = await response.json();

        if (data.success && data.data) {
            appState.categoriasServicio = data.data;
            console.log(`Cargadas ${appState.categoriasServicio.length} categorías de servicio`);
        } else {
            console.error('Error cargando categorías:', data.error);
            mostrarAlerta('error', 'No se pudieron cargar las categorías de servicio');
        }
    } catch (error) {
        console.error('Error cargando categorías:', error);
        mostrarAlerta('error', 'Error de conexión al cargar categorías');
    }
}

// =====================================================================
// CARGAR DATOS EXISTENTES
// =====================================================================

async function cargarDatosExistentes() {
    mostrarLoading(true);

    try {
        const response = await fetch(`./api/servicios-profesional?profesionalId=${appState.profesionalId}`);
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
                    servicios.areaServicio.ubicaciones.forEach(ub => {
                        agregarUbicacion(ub);
                    });
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
        categoriaId: datosExistentes?.categoriaId || (appState.categoriasServicio.length > 0 ? appState.categoriasServicio[0].id : ''),
        nombreCategoria: datosExistentes?.nombreCategoria || '',
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

    // Si eliminamos la principal, hacer principal la primera
    if (eraPrincipal && appState.especialidades.length > 0) {
        appState.especialidades[0].esPrincipal = true;
    }

    // Reordenar
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
                        onchange="actualizarEspecialidad(${index}, 'categoriaId', parseInt(this.value)); actualizarNombreCategoria(${index}, this.options[this.selectedIndex].text);"
                        required>
                    <option value="">Seleccione una categoría...</option>
                    ${appState.categoriasServicio.map(cat =>
                        `<option value="${cat.id}" ${cat.id === esp.categoriaId ? 'selected' : ''}>${cat.icono ? cat.icono + ' ' : ''}${cat.nombre}</option>`
                    ).join('')}
                </select>
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

function actualizarEspecialidad(index, campo, valor) {
    if (appState.especialidades[index]) {
        appState.especialidades[index][campo] = valor;
    }
}

function actualizarNombreCategoria(index, nombreCompleto) {
    if (appState.especialidades[index]) {
        // Remover el icono si existe (viene como "🔧 Plomería")
        const nombre = nombreCompleto.replace(/^[\u{1F300}-\u{1F9FF}]\s*/u, '').trim();
        appState.especialidades[index]['nombreCategoria'] = nombre;
    }
}

function actualizarBotonesEspecialidades() {
    const btnAgregar = document.getElementById('btnAgregarEspecialidad');
    btnAgregar.disabled = appState.especialidades.length >= 3;
}

// =====================================================================
// ÁREA DE SERVICIO
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
        departamento: datosExistentes?.departamento || '',
        provincia: datosExistentes?.provincia || '',
        distrito: datosExistentes?.distrito || ''
    };

    appState.ubicaciones.push(ubicacion);
    renderizarUbicaciones();
    actualizarBotonesUbicaciones();
}

function eliminarUbicacion(index) {
    appState.ubicaciones.splice(index, 1);

    // Reordenar
    appState.ubicaciones.forEach((ub, i) => {
        ub.orden = i + 1;
    });

    renderizarUbicaciones();
    actualizarBotonesUbicaciones();
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
                        onchange="actualizarUbicacion(${index}, 'tipoUbicacion', this.value); renderizarUbicaciones();"
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
                            onchange="actualizarUbicacion(${index}, 'departamento', this.value)"
                            required>
                        <option value="">Seleccione...</option>
                        ${departamentosPeru.map(dep =>
                            `<option value="${dep}" ${ub.departamento === dep ? 'selected' : ''}>${dep}</option>`
                        ).join('')}
                    </select>
                </div>

                ${ub.tipoUbicacion !== 'departamento' ? `
                    <div class="form-group">
                        <label ${ub.tipoUbicacion === 'provincia' || ub.tipoUbicacion === 'distrito' ? 'class="required"' : ''}>Provincia</label>
                        <input type="text" class="form-input"
                               value="${ub.provincia}"
                               onchange="actualizarUbicacion(${index}, 'provincia', this.value)"
                               placeholder="Ingrese la provincia"
                               ${ub.tipoUbicacion === 'provincia' || ub.tipoUbicacion === 'distrito' ? 'required' : ''}>
                    </div>
                ` : ''}
            </div>

            ${ub.tipoUbicacion === 'distrito' ? `
                <div class="form-group">
                    <label class="required">Distrito</label>
                    <input type="text" class="form-input"
                           value="${ub.distrito}"
                           onchange="actualizarUbicacion(${index}, 'distrito', this.value)"
                           placeholder="Ingrese el distrito"
                           required>
                </div>
            ` : ''}
        `;

        container.appendChild(div);
    });
}

function actualizarUbicacion(index, campo, valor) {
    if (appState.ubicaciones[index]) {
        appState.ubicaciones[index][campo] = valor;
    }
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

    // Encontrar días disponibles
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

    // Encontrar días disponibles
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

    // Validaciones
    if (!validarFormulario()) {
        return;
    }

    // Construir objeto de datos
    const datosServicio = {
        profesionalId: appState.profesionalId,
        especialidades: appState.especialidades,
        areaServicio: {
            todoPais: appState.todoPais,
            ubicaciones: appState.todoPais ? [] : appState.ubicaciones
        },
        disponibilidad: {
            todoTiempo: appState.todoTiempo,
            horarios: appState.todoTiempo ? [] : appState.horarios
        }
    };

    console.log('Enviando datos:', datosServicio);

    mostrarLoading(true);

    try {
        const url = './api/servicios-profesional';
        const method = 'POST'; // El servlet manejará create o update internamente

        const response = await fetch(url, {
            method: method,
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(datosServicio)
        });

        const data = await response.json();

        if (data.success) {
            mostrarAlerta('success', data.message || 'Servicios guardados exitosamente');

            // Redirigir al dashboard después de 2 segundos
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
    // Validar especialidades
    if (appState.especialidades.length === 0) {
        mostrarAlerta('error', 'Debe agregar al menos una especialidad');
        return false;
    }

    if (appState.especialidades.length > 3) {
        mostrarAlerta('error', 'No puede tener más de 3 especialidades');
        return false;
    }

    // Validar que todas las especialidades tengan datos completos
    for (let i = 0; i < appState.especialidades.length; i++) {
        const esp = appState.especialidades[i];

        if (!esp.categoriaId || esp.categoriaId === '') {
            mostrarAlerta('error', `La especialidad ${i + 1} debe tener una categoría seleccionada`);
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

    // Verificar que haya al menos una especialidad principal
    const tienePrincipal = appState.especialidades.some(esp => esp.esPrincipal);
    if (!tienePrincipal) {
        mostrarAlerta('error', 'Debe marcar al menos una especialidad como principal');
        return false;
    }

    // Validar área de servicio
    if (!appState.todoPais) {
        if (appState.ubicaciones.length === 0) {
            mostrarAlerta('error', 'Debe agregar al menos una ubicación de servicio o marcar "Todo el país"');
            return false;
        }

        if (appState.ubicaciones.length > 10) {
            mostrarAlerta('error', 'No puede tener más de 10 ubicaciones');
            return false;
        }

        // Validar cada ubicación
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

    // Validar disponibilidad
    if (!appState.todoTiempo) {
        if (appState.horarios.length === 0) {
            mostrarAlerta('error', 'Debe agregar al menos un horario o marcar "Todo el tiempo"');
            return false;
        }

        // Validar cada horario
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

                // Validar que hora fin sea mayor que hora inicio
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

    // Auto-ocultar después de 5 segundos
    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 5000);

    // Scroll al inicio
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
