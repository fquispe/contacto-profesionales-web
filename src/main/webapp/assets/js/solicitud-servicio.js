// Solicitud Servicio JavaScript - Desacoplado del HTML el 2025-11-21
		// ============================================
		// SERVICIO DE API
		// ============================================
		class SolicitudService {
		    constructor() {
		        this.baseURL = '/ContactoProfesionalesWeb/api';
		    }
		
		    async crearSolicitud(solicitudData) {
		        try {
		            console.log('🌐 SolicitudService - Enviando petición a:', `${this.baseURL}/solicitudes`);
		            console.log('📦 Datos:', solicitudData);
		            
		            const response = await fetch(`${this.baseURL}/solicitudes`, {
		                method: 'POST',
		                headers: {
		                    'Content-Type': 'application/json',
		                    'Authorization': `Bearer ${this.getToken()}`
		                },
		                body: JSON.stringify(solicitudData)
		            });
		
		            console.log('📥 Response status:', response.status);
		            
		            const data = await response.json();
		            console.log('📥 Response data:', data);
		
		            if (response.ok && data.success) {
		                return { success: true, data: data.data };
		            } else {
		                return { success: false, message: data.error || data.message || 'Error al crear solicitud' };
		            }
		        } catch (error) {
		            console.error('❌ Error en solicitud:', error);
		            return { success: false, message: 'Error de conexión con el servidor' };
		        }
		    }
		
		    getToken() {
		        const token = localStorage.getItem('authToken') || '';
		        console.log('🔑 Token obtenido:', token ? 'Existe' : 'No existe');
		        return token;
		    }
		}
		
		// Instancia global
		const solicitudService = new SolicitudService();
		
		// ============================================
		// VARIABLES GLOBALES
		// ============================================
		let currentStep = 1;
		const totalSteps = 4;
		let uploadedPhotos = [];
		let selectedAddress = null;
		let profesionalId = null;
		let especialidadId = null; // NUEVO: Guardar ID de especialidad seleccionada
		let especialidadNombre = null; // NUEVO: Guardar nombre de especialidad seleccionada

		// ============================================
		// INICIALIZACIÓN
		// ============================================
		document.addEventListener('DOMContentLoaded', function() {
		    console.log('🚀 Iniciando solicitud-servicio.html');

		    // Verificar sesión del usuario
		    const userData = JSON.parse(localStorage.getItem('userData') || '{}');
		    console.log('👤 Datos del usuario:', userData);

		    if (!userData.id) {
		        showAlert('Debes iniciar sesión para solicitar un servicio', 'error');
		        setTimeout(() => {
		            window.location.href = '/ContactoProfesionalesWeb/login.html';
		        }, 2000);
		        return;
		    }

		    // Obtener ID del profesional y especialidadId desde URL
		    const urlParams = new URLSearchParams(window.location.search);
		    profesionalId = urlParams.get('profesionalId');
		    especialidadId = urlParams.get('especialidadId'); // NUEVO: Capturar ID de especialidad

		    console.log('👨‍🔧 Profesional ID:', profesionalId);
		    console.log('⚙️ Especialidad ID:', especialidadId);

		    if (!profesionalId) {
		        showAlert('No se especificó el profesional', 'error');
		        setTimeout(() => {
		            window.location.href = '/ContactoProfesionalesWeb/buscar-profesional.html';
		        }, 2000);
		        return;
		    }

		    // Cargar especialidad si viene en la URL
		    if (especialidadId) {
		        cargarEspecialidad(especialidadId).then(() => {
		            cargarInfoProfesional(profesionalId);
		            // NUEVO: Cargar modalidad de trabajo después de tener especialidadId
		            cargarModalidadTrabajo(especialidadId);
		        });
		    } else {
		        cargarInfoProfesional(profesionalId);
		    }

		    // ✅ CORRECCIÓN: La cascada de ubicación se inicializa cuando se muestra el formulario presencial
		    // No es necesario inicializarla aquí porque los selectores están ocultos
		    // Ver: handleModalidadChange() línea 352

		    // TODO: Auto-cargar ubicación del cliente desde su perfil
		    // Requiere endpoint: GET /api/ubicacion/distrito/{id}/jerarquia
		    // cargarUbicacionCliente();

		    // Set minimum date to today
		    const today = new Date().toISOString().split('T')[0];
		    document.getElementById('serviceDate').setAttribute('min', today);
		
		    // Character counters
		    document.getElementById('description').addEventListener('input', function() {
		        updateCharCounter('description', 'descCounter', 1000, 20);
		    });
		
		    document.getElementById('additionalNotes').addEventListener('input', function() {
		        updateCharCounter('additionalNotes', 'notesCounter', 500, 0);
		    });
		
		    // ✅ EVENTO DE SUBMIT DEL FORMULARIO
		    document.getElementById('serviceRequestForm').addEventListener('submit', function(e) {
		        e.preventDefault();
		        console.log('📤 Submit del formulario disparado');
		        handleSubmit(e);
		    });
		    
		    console.log('✅ Inicialización completada');
		});

		// ============================================
		// CARGAR ESPECIALIDAD POR ID
		// ============================================
		async function cargarEspecialidad(id) {
		    console.log('🔍 Cargando especialidad ID:', id);

		    try {
		        const response = await fetch(`/ContactoProfesionalesWeb/api/especialidad?id=${id}`);

		        if (!response.ok) {
		            throw new Error(`HTTP error! status: ${response.status}`);
		        }

		        const data = await response.json();

		        if (data.success && data.especialidad) {
		            especialidadNombre = data.especialidad.servicioProfesional;
		            console.log('✅ Especialidad cargada:', especialidadNombre);
		        } else {
		            console.warn('⚠️ No se pudo cargar la especialidad');
		        }

		    } catch (error) {
		        console.error('❌ Error al cargar especialidad:', error);
		    }
		}

		// ============================================
		// CARGAR INFO DEL PROFESIONAL
		// ============================================
		async function cargarInfoProfesional(id) {
		    console.log('🔍 Cargando información del profesional ID:', id);
		    
		    try {
		        document.getElementById('providerName').textContent = 'Cargando...';
		        document.getElementById('providerRating').innerHTML = 'Obteniendo información...';
		        
		        const url = `./api/profesionales/${id}`;
		        console.log('📡 URL:', url);
		        
		        const response = await fetch(url);
		        console.log('📥 Response status:', response.status);
		        
		        const responseText = await response.text();
		        console.log('📄 Response text:', responseText);
		        
		        let data;
		        try {
		            data = JSON.parse(responseText);
		            console.log('📊 Data parseada:', data);
		        } catch (parseError) {
		            console.error('❌ Error al parsear JSON:', parseError);
		            throw new Error('Respuesta del servidor inválida');
		        }
		        
		        if (!response.ok) {
		            throw new Error(data.error || `Error HTTP: ${response.status}`);
		        }
		        
		        if (!data.success) {
		            throw new Error(data.error || 'Error en la respuesta del servidor');
		        }
		        
		        const profesionalData = data.data || data;
		        if (!profesionalData.profesional) {
		            throw new Error('No se recibieron datos del profesional');
		        }
		        const prof = profesionalData.profesional;
		        console.log('✅ Profesional obtenido:', prof);
		        
		        const nombreCompleto = prof.nombreCompleto || 'Profesional';
		        // Use especialidadNombre from URL/API, fallback to prof.especialidad
		        const especialidad = especialidadNombre || prof.especialidad || 'Sin especialidad';
		        console.log('🎯 Especialidad a mostrar:', especialidad);

		        document.getElementById('providerName').textContent =
		            `${nombreCompleto} - ${especialidad}`;
		        
		        const calificacion = prof.calificacionPromedio || 0;
		        const fullStars = Math.floor(calificacion);
		        const emptyStars = 5 - fullStars;
		        const stars = '⭐'.repeat(fullStars) + '☆'.repeat(emptyStars);
		        
		        const totalResenas = prof.totalResenas || 0;
		        const distrito = prof.distrito || 'Lima';
		        
		        document.getElementById('providerRating').innerHTML = `
		            <span>${stars}</span>
		            <span>${calificacion.toFixed(1)} (${totalResenas} reseña${totalResenas !== 1 ? 's' : ''})</span>
		            <span>•</span>
		            <span>📍 ${distrito}</span>
		        `;
		        
		        const avatarDiv = document.querySelector('.provider-avatar');
		        if (prof.fotoPerfil && prof.fotoPerfil.trim() !== '') {
		            avatarDiv.innerHTML = `
		                <img src="${prof.fotoPerfil}" 
		                     alt="${nombreCompleto}" 
		                     style="width: 100%; height: 100%; object-fit: cover; border-radius: 50%;"
		                     onerror="this.parentElement.innerHTML='👨‍🔧'">
		            `;
		        } else {
		            avatarDiv.innerHTML = '👨‍🔧';
		        }
		        
		        document.getElementById('summaryProvider').textContent = 
		            `${nombreCompleto} - ${especialidad}`;
		        
		        window.profesionalData = prof;
		        
		        console.log('✅ Información del profesional cargada exitosamente');
		        
		    } catch (error) {
		        console.error('❌ Error al cargar profesional:', error);
		        showAlert(`No se pudo cargar la información del profesional. ${error.message}`, 'error');
		        
		        document.getElementById('providerName').textContent = 'Profesional';
		        document.getElementById('providerRating').innerHTML =
		            '<span style="color: var(--error-color);">⚠️ Información no disponible</span>';
		    }
		}

		// ============================================
		// MODALIDAD DE TRABAJO (Migración V008)
		// ============================================

		/**
		 * Carga las modalidades disponibles (remoto/presencial) para una especialidad.
		 * Habilita/deshabilita las opciones según los flags trabajo_remoto y trabajo_presencial.
		 */
		async function cargarModalidadTrabajo(especialidadId) {
		    console.log('🔍 Cargando modalidad de trabajo para especialidad ID:', especialidadId);

		    const modalidadRemotoOption = document.getElementById('modalidadRemotoOption');
		    const modalidadPresencialOption = document.getElementById('modalidadPresencialOption');
		    const modalidadCargando = document.getElementById('modalidadCargando');
		    const modalidadError = document.getElementById('modalidadError');

		    // ✅ CORRECCIÓN: Validar que los elementos existan antes de usarlos
		    if (!modalidadRemotoOption || !modalidadPresencialOption || !modalidadCargando || !modalidadError) {
		        console.error('❌ Error: No se encontraron todos los elementos de modalidad en el DOM');
		        console.error('Elementos encontrados:', {
		            modalidadRemotoOption: !!modalidadRemotoOption,
		            modalidadPresencialOption: !!modalidadPresencialOption,
		            modalidadCargando: !!modalidadCargando,
		            modalidadError: !!modalidadError
		        });
		        return;
		    }

		    // Mostrar indicador de carga
		    modalidadCargando.style.display = 'block';
		    modalidadError.style.display = 'none';
		    modalidadRemotoOption.style.display = 'none';
		    modalidadPresencialOption.style.display = 'none';

		    try {
		        const response = await fetch(`/ContactoProfesionalesWeb/api/especialidad/modalidad?especialidadId=${especialidadId}`);
		        const data = await response.json();

		        console.log('📥 Respuesta modalidad:', data);

		        if (!response.ok || !data.success) {
		            throw new Error(data.error || 'Error al obtener modalidad');
		        }

		        const modalidad = data.modalidad;

		        // Ocultar indicador de carga
		        modalidadCargando.style.display = 'none';

		        // Mostrar y configurar opciones según disponibilidad
		        if (modalidad.trabajoRemoto) {
		            modalidadRemotoOption.style.display = 'block';
		            modalidadRemotoOption.classList.remove('disabled');
		            document.getElementById('modalidadRemoto').disabled = false;
		        } else {
		            modalidadRemotoOption.style.display = 'block';
		            modalidadRemotoOption.classList.add('disabled');
		            document.getElementById('modalidadRemoto').disabled = true;
		        }

		        if (modalidad.trabajoPresencial) {
		            modalidadPresencialOption.style.display = 'block';
		            modalidadPresencialOption.classList.remove('disabled');
		            document.getElementById('modalidadPresencial').disabled = false;
		        } else {
		            modalidadPresencialOption.style.display = 'block';
		            modalidadPresencialOption.classList.add('disabled');
		            document.getElementById('modalidadPresencial').disabled = true;
		        }

		        console.log('✅ Modalidad cargada - Remoto:', modalidad.trabajoRemoto, '| Presencial:', modalidad.trabajoPresencial);

		    } catch (error) {
		        console.error('❌ Error al cargar modalidad:', error);
		        modalidadCargando.style.display = 'none';
		        modalidadError.style.display = 'block';
		        modalidadError.textContent = '⚠️ ' + error.message;
		    }
		}

		/**
		 * Maneja el cambio de modalidad (remoto/presencial).
		 * Muestra u oculta el formulario de dirección según la selección.
		 */
		function handleModalidadChange(modalidad) {
		    console.log('🔄 Modalidad cambiada a:', modalidad);

		    const direccionForm = document.getElementById('direccionPresencialForm');

		    if (modalidad === 'PRESENCIAL') {
		        // Mostrar formulario de dirección
		        direccionForm.style.display = 'block';

		        // ✅ CORRECCIÓN: Inicializar cascada cuando se muestra el formulario
		        inicializarCombosUbicacion();

		        // Hacer campos requeridos
		        document.getElementById('departamento').required = true;
		        document.getElementById('provincia').required = true;
		        document.getElementById('distrito').required = true;
		        document.getElementById('direccion').required = true;

		    } else if (modalidad === 'REMOTO') {
		        // Ocultar formulario de dirección
		        direccionForm.style.display = 'none';

		        // Limpiar campos
		        limpiarCamposDireccion();

		        // Remover requerimiento
		        document.getElementById('departamento').required = false;
		        document.getElementById('provincia').required = false;
		        document.getElementById('distrito').required = false;
		        document.getElementById('direccion').required = false;
		    }
		}

		/**
		 * Limpia todos los campos del formulario de dirección.
		 */
		function limpiarCamposDireccion() {
		    console.log('🧹 Limpiando campos de dirección');

		    document.getElementById('departamento').value = '';
		    document.getElementById('provincia').value = '';
		    document.getElementById('provincia').disabled = true;
		    document.getElementById('distrito').value = '';
		    document.getElementById('distrito').disabled = true;
		    document.getElementById('direccion').value = '';
		    document.getElementById('codigoPostal').value = '';
		    document.getElementById('referencia').value = '';
		}

		// Variable para controlar si la cascada ya fue inicializada
		let cascadaUbicacionInicializada = false;

		/**
		 * Inicializa los combos de ubicación con cascada departamento → provincia → distrito.
		 * Utiliza ubicacion-api.js para poblar los selects dinámicamente.
		 * Esta función es idempotente: solo se ejecuta una vez.
		 */
		function inicializarCombosUbicacion() {
		    // ✅ CORRECCIÓN: Evitar inicializar múltiples veces
		    if (cascadaUbicacionInicializada) {
		        console.log('ℹ️ Cascada de ubicación ya fue inicializada');
		        return;
		    }

		    console.log('📍 Inicializando combos de ubicación');

		    // Verificar si ubicacion-api.js está disponible
		    if (typeof ubicacionAPI === 'undefined') {
		        console.warn('⚠️ ubicacion-api.js no está cargado. Usando selects estáticos.');
		        return;
		    }

		    const selectDepartamento = document.getElementById('departamento');
		    const selectProvincia = document.getElementById('provincia');
		    const selectDistrito = document.getElementById('distrito');

		    // ✅ CORRECCIÓN: Validar que los selectores existan
		    console.log('🔍 Verificando selectores:', {
		        departamento: !!selectDepartamento,
		        provincia: !!selectProvincia,
		        distrito: !!selectDistrito
		    });

		    if (!selectDepartamento || !selectProvincia || !selectDistrito) {
		        console.warn('⚠️ Algunos selectores de ubicación no se encontraron.');
		        console.warn('Esto puede ser normal si los selectores están ocultos. Se inicializarán cuando sean visibles.');
		        console.warn('Selectores encontrados:', {
		            departamento: selectDepartamento ? 'SÍ' : 'NO',
		            provincia: selectProvincia ? 'SÍ' : 'NO',
		            distrito: selectDistrito ? 'SÍ' : 'NO'
		        });
		        return;
		    }

		    // ✅ CORRECCIÓN: configurarCascada() espera un OBJETO, no parámetros separados
		    ubicacionAPI.configurarCascada({
		        departamento: selectDepartamento,
		        provincia: selectProvincia,
		        distrito: selectDistrito
		    });

		    cascadaUbicacionInicializada = true;
		    console.log('✅ Cascada de ubicación configurada');
		}

		// ============================================
		// NAVEGACIÓN
		// ============================================
		function volverBusqueda() {
		    const descripcion = document.getElementById('description').value.trim();
		    const budget = document.getElementById('budget').value;
		    
		    if (descripcion || budget) {
		        const confirmar = confirm(
		            '¿Estás seguro que deseas cambiar de profesional?\n\n' +
		            'Se perderán los datos que has ingresado en el formulario.'
		        );
		        
		        if (!confirmar) {
		            return;
		        }
		    }
		    
		    window.location.href = 'buscar-profesional.html';
		}
		
		function volverDashboard() {
		    const descripcion = document.getElementById('description').value.trim();
		    const budget = document.getElementById('budget').value;
		    
		    if (descripcion || budget) {
		        const confirmar = confirm(
		            '¿Estás seguro que deseas salir?\n\n' +
		            'Se perderán los datos que has ingresado en el formulario.'
		        );
		        
		        if (!confirmar) {
		            return;
		        }
		    }
		    
		    window.location.href = 'dashboard.html';
		}
		
		// ============================================
		// CHARACTER COUNTER
		// ============================================
		function updateCharCounter(inputId, counterId, max, min = 0) {
		    const input = document.getElementById(inputId);
		    const counter = document.getElementById(counterId);
		    const current = input.value.length;
		    
		    if (min > 0) {
		        counter.textContent = `${current}/${max} caracteres (mínimo ${min})`;
		    } else {
		        counter.textContent = `${current}/${max} caracteres`;
		    }
		    
		    if (current < min && current > 0) {
		        counter.classList.add('error');
		        counter.classList.remove('warning');
		    } else if (current > max * 0.9) {
		        counter.classList.add('warning');
		        counter.classList.remove('error');
		    } else {
		        counter.classList.remove('warning', 'error');
		    }
		}
		
		// ============================================
		// PHOTO UPLOAD
		// ============================================
		function selectPhoto(index) {
		    const slot = document.querySelectorAll('.photo-slot')[index];
		    const input = slot.querySelector('input[type="file"]');
		    input.click();
		}
		
		function previewPhoto(input, index) {
		    const file = input.files[0];
		    if (file) {
		        if (file.size > 5 * 1024 * 1024) {
		            showAlert('La foto no debe superar los 5MB', 'error');
		            input.value = '';
		            return;
		        }
		        
		        if (!file.type.startsWith('image/')) {
		            showAlert('El archivo debe ser una imagen', 'error');
		            input.value = '';
		            return;
		        }
		        
		        const reader = new FileReader();
		        reader.onload = function(e) {
		            const slot = document.querySelectorAll('.photo-slot')[index];
		            slot.innerHTML = `
		                <img src="${e.target.result}" alt="Photo ${index + 1}">
		                <button type="button" class="remove-photo-btn" onclick="removePhoto(event, ${index})">×</button>
		            `;
		            slot.classList.add('has-image');
		            slot.onclick = null;
		            
		            uploadedPhotos[index] = e.target.result;
		        };
		        reader.readAsDataURL(file);
		    }
		}
		
		function removePhoto(event, index) {
		    event.stopPropagation();
		    const slot = document.querySelectorAll('.photo-slot')[index];
		    slot.innerHTML = `
		        <div class="upload-icon">📷</div>
		        <div class="upload-text">Foto ${index + 1}</div>
		        <input type="file" accept="image/*" style="display: none;" onchange="previewPhoto(this, ${index})">
		        <button type="button" class="remove-photo-btn" onclick="removePhoto(event, ${index})">×</button>
		    `;
		    slot.classList.remove('has-image');
		    slot.onclick = function() { selectPhoto(index); };
		    
		    uploadedPhotos[index] = null;
		}
		
		// ============================================
		// ADDRESS SELECTION - DEPRECADO (Migración V008)
		// ============================================
		// Las funciones selectAddress() y toggleNewAddress() han sido eliminadas
		// Ahora se usa el selector de modalidad con handleModalidadChange()
		
		// ============================================
		// URGENCY SELECTION
		// ============================================
		function selectUrgency(element, urgency) {
		    document.querySelectorAll('.urgency-option').forEach(opt => {
		        opt.classList.remove('selected');
		    });
		    element.classList.add('selected');
		    document.getElementById('urgency').value = urgency;
		}
		
		// ============================================
		// NAVIGATION LOGIC
		// ============================================
		function nextStep() {
		    console.log('➡️ nextStep() - Paso actual:', currentStep);
		    
		    if (currentStep < totalSteps) {
		        // Validar el paso actual antes de avanzar
		        if (!validateStep(currentStep)) {
		            console.log('❌ Validación falló para paso', currentStep);
		            return;
		        }
		
		        currentStep++;
		        console.log('✅ Avanzando a paso', currentStep);
		        updateSteps();
		    }
		    // ✅ NO hacer nada más aquí
		    // El submit se maneja con el evento del formulario
		}
		
		function previousStep() {
		    console.log('⬅️ previousStep() - Paso actual:', currentStep);
		    if (currentStep > 1) {
		        currentStep--;
		        updateSteps();
		    }
		}
		
		function updateSteps() {
		    console.log('🔄 Actualizando UI a paso', currentStep);
		    
		    document.querySelectorAll('.form-step').forEach((step, index) => {
		        step.classList.toggle('active', index + 1 === currentStep);
		    });
		
		    document.querySelectorAll('.step').forEach((step, index) => {
		        step.classList.toggle('active', index + 1 === currentStep);
		        step.classList.toggle('completed', index + 1 < currentStep);
		    });
		
		    updateProgress();
		
		    document.getElementById('prevBtn').style.display = currentStep > 1 ? 'inline-flex' : 'none';
		    document.getElementById('nextBtn').style.display = currentStep < totalSteps ? 'inline-flex' : 'none';
		    document.getElementById('submitBtn').style.display = currentStep === totalSteps ? 'inline-flex' : 'none';
		
		    if (currentStep === totalSteps) {
		        updateSummary();
		    }
		}
		
		function updateProgress() {
		    const progress = ((currentStep - 1) / (totalSteps - 1)) * 100;
		    document.getElementById('progressLine').style.width = `${progress}%`;
		}
		
		// ============================================
		// VALIDATIONS
		// ============================================
		function validateStep(step) {
		    console.log('🔍 Validando paso', step);
		    
		    switch (step) {
		        case 1:
		            const desc = document.getElementById('description').value.trim();
		            const budget = document.getElementById('budget').value;
		            if (desc.length < 20) {
		                showAlert('La descripción debe tener al menos 20 caracteres', 'error');
		                return false;
		            }
		            if (!budget || budget < 20) {
		                showAlert('Debes ingresar un presupuesto válido (mínimo S/20)', 'error');
		                return false;
		            }
		            break;
		
		        case 2:
		            // NUEVO: Validar selección de modalidad
		            const modalidadSeleccionada = document.querySelector('input[name="modalidad"]:checked');
		            if (!modalidadSeleccionada) {
		                showAlert('Debes seleccionar una modalidad de trabajo (Remoto o Presencial)', 'error');
		                return false;
		            }

		            // Si es presencial, validar campos de dirección
		            if (modalidadSeleccionada.value === 'PRESENCIAL') {
		                const departamento = document.getElementById('departamento').value;
		                const provincia = document.getElementById('provincia').value;
		                const distrito = document.getElementById('distrito').value;
		                const direccion = document.getElementById('direccion').value.trim();

		                if (!departamento || !provincia || !distrito) {
		                    showAlert('Debes seleccionar departamento, provincia y distrito', 'error');
		                    return false;
		                }

		                if (!direccion) {
		                    showAlert('Debes ingresar la dirección exacta del servicio', 'error');
		                    return false;
		                }
		            }
		            break;
		
		        case 3:
		            const date = document.getElementById('serviceDate').value;
		            const time = document.getElementById('serviceTime').value;
		            if (!date || !time) {
		                showAlert('Debes seleccionar fecha y hora', 'error');
		                return false;
		            }
		            // ✅ NO validar términos aquí, están en el paso 4
		            break;
		            
		        case 4:
		            // ✅ Validar términos en el paso 4 (Confirmar)
		            const terms = document.getElementById('terms');
		            if (!terms.checked) {
		                showAlert('Debes aceptar los términos y condiciones', 'error');
		                return false;
		            }
		            break;
		    }
		    
		    console.log('✅ Validación exitosa para paso', step);
		    return true;
		}
		
		// ============================================
		// SUMMARY
		// ============================================
		function updateSummary() {
		    console.log('📋 Actualizando resumen');
		    
		    const desc = document.getElementById('description').value.trim();
		    const budget = document.getElementById('budget').value;
		    // NUEVO: Obtener modalidad y construir dirección (Migración V008)
		    const modalidadSeleccionada = document.querySelector('input[name="modalidad"]:checked');
		    const modalidad = modalidadSeleccionada ? modalidadSeleccionada.value : 'No especificado';

		    let address = '';
		    if (modalidad === 'REMOTO') {
		        address = '💻 Trabajo Remoto (Servicio virtual)';
		    } else if (modalidad === 'PRESENCIAL') {
		        const depto = document.getElementById('departamento').selectedOptions[0]?.text || '';
		        const prov = document.getElementById('provincia').selectedOptions[0]?.text || '';
		        const dist = document.getElementById('distrito').selectedOptions[0]?.text || '';
		        const dir = document.getElementById('direccion').value.trim();
		        address = `🏠 ${dir}, ${dist}, ${prov}, ${depto}`;
		    } else {
		        address = 'No especificado';
		    }
		    const date = document.getElementById('serviceDate').value;
		    const time = document.getElementById('serviceTime').value;
		    const urgency = document.getElementById('urgency').value;
		    const notes = document.getElementById('additionalNotes').value.trim();
		
		    document.getElementById('summaryDescription').textContent = desc;
		    document.getElementById('summaryBudget').textContent = `S/ ${budget}`;
		    document.getElementById('summaryLocation').textContent = address;
		    document.getElementById('summaryDateTime').textContent = `${date} ${time}`;
		    document.getElementById('summaryUrgency').textContent = urgency === 'urgent' ? 'Urgente ⚡' : 'Normal 🕐';
		
		    const photosContainer = document.getElementById('summaryPhotosContainer');
		    const photosArea = document.getElementById('summaryPhotos');
		    photosArea.innerHTML = '';
		    const validPhotos = uploadedPhotos.filter(Boolean);
		
		    if (validPhotos.length > 0) {
		        photosContainer.style.display = 'flex';
		        validPhotos.forEach(src => {
		            const img = document.createElement('img');
		            img.src = src;
		            img.classList.add('summary-photo-thumb');
		            photosArea.appendChild(img);
		        });
		    } else {
		        photosContainer.style.display = 'none';
		    }
		
		    const notesContainer = document.getElementById('summaryNotesContainer');
		    if (notes) {
		        notesContainer.style.display = 'flex';
		        document.getElementById('summaryNotes').textContent = notes;
		    } else {
		        notesContainer.style.display = 'none';
		    }
		}
		
		// ============================================
		// FORM SUBMISSION (USANDO SolicitudService)
		// ============================================
		async function handleSubmit(event) {
		    if (event) {
		        event.preventDefault();
		    }
		    
		    console.log('🚀 handleSubmit() ejecutándose...');
		    
		    // ✅ Obtener datos del usuario desde localStorage
		    const userData = JSON.parse(localStorage.getItem('userData') || '{}');
		    console.log('👤 Datos del usuario:', userData);
		    
		    // ✅ Verificar que existe clienteId
		    if (!userData.id) {
		        console.error('❌ No hay usuario ID en localStorage');
		        showAlert('Sesión expirada. Por favor inicia sesión nuevamente.', 'error');
		        setTimeout(() => {
		            window.location.href = '/ContactoProfesionalesWeb/login.html';
		        }, 2000);
		        return;
		    }
		    
		    console.log('✅ Cliente ID encontrado:', userData.id);
		    
		    const submitBtn = document.getElementById('submitBtn');
		    const btnText = submitBtn.querySelector('.button-text');
		    const originalText = btnText.textContent;
		    
		    submitBtn.classList.add('loading');
		    submitBtn.disabled = true;
		    btnText.textContent = 'Enviando...';
		
		    // ✅ Construir objeto con clienteId incluido
		    // ACTUALIZADO: Incluye campos de Migración V008 (modalidad y ubicación estructurada)
		    const modalidadSeleccionada = document.querySelector('input[name="modalidad"]:checked');
		    const tipoPrestacion = modalidadSeleccionada ? modalidadSeleccionada.value : null;

		    const solicitudData = {
		        clienteId: userData.id,  // ✅ ID del cliente
		        profesionalId: parseInt(profesionalId),
		        descripcion: document.getElementById('description').value.trim(),
		        presupuestoEstimado: parseFloat(document.getElementById('budget').value),

		        // ACTUALIZADO: Dirección y ubicación según modalidad (V008)
		        direccion: tipoPrestacion === 'PRESENCIAL' ? document.getElementById('direccion').value.trim() : null,
		        distrito: tipoPrestacion === 'PRESENCIAL' ? document.getElementById('distrito').selectedOptions[0]?.text : null,
		        codigoPostal: tipoPrestacion === 'PRESENCIAL' ? (document.getElementById('codigoPostal').value || null) : null,
		        referencia: tipoPrestacion === 'PRESENCIAL' ? (document.getElementById('referencia').value || null) : null,

		        // NUEVOS CAMPOS - Migración V008: Ubicación estructurada
		        departamentoId: tipoPrestacion === 'PRESENCIAL' ? parseInt(document.getElementById('departamento').value) : null,
		        provinciaId: tipoPrestacion === 'PRESENCIAL' ? parseInt(document.getElementById('provincia').value) : null,
		        distritoId: tipoPrestacion === 'PRESENCIAL' ? parseInt(document.getElementById('distrito').value) : null,

		        // NUEVOS CAMPOS - Migración V008: Modalidad y especialidad
		        tipoPrestacion: tipoPrestacion,  // "REMOTO" o "PRESENCIAL"
		        especialidadId: especialidadId ? parseInt(especialidadId) : null,

		        fechaServicio: `${document.getElementById('serviceDate').value}T${document.getElementById('serviceTime').value}:00`,
		        urgencia: document.getElementById('urgency').value,
		        notasAdicionales: document.getElementById('additionalNotes').value.trim(),
		        fotosBase64: uploadedPhotos.filter(Boolean),
		        estado: "pendiente"
		    };
		    
		    console.log('📤 Datos a enviar:', solicitudData);
		
		    // ✅ USAR EL SERVICIO
		    const result = await solicitudService.crearSolicitud(solicitudData);
		
		    submitBtn.classList.remove('loading');
		    submitBtn.disabled = false;
		    btnText.textContent = originalText;
		
		    if (result.success) {
		        console.log('✅ Solicitud enviada exitosamente');
		        
		        // Mostrar pantalla de éxito
		        currentStep = 5;
		        updateSteps();
		        document.getElementById('requestId').textContent = 
		            result.data.codigoSolicitud || 'SR-2025-000001';
		        
		        // Ocultar navegación
		        document.getElementById('formNavigation').style.display = 'none';
		    } else {
		        console.error('❌ Error:', result.message);
		        showAlert(result.message, 'error');
		    }
		}
		
		// ============================================
		// ALERT HANDLER
		// ============================================
		function showAlert(message, type = 'info') {
		    console.log(`📢 Alert [${type}]:`, message);
		    
		    const alertContainer = document.getElementById('alertContainer');
		    alertContainer.innerHTML = `
		        <div class="alert alert-${type}">
		            ${message}
		        </div>
		    `;
		    const alert = alertContainer.querySelector('.alert');
		    alert.style.display = 'block';
		    
		    // Scroll to top para ver el alert
		    window.scrollTo({ top: 0, behavior: 'smooth' });
		    
		    setTimeout(() => { 
		        alert.style.display = 'none'; 
		    }, 5000);
		}
		
		// ============================================
		// REDIRECCIONES
		// ============================================
		function goToRequests() {
		    window.location.href = '/ContactoProfesionalesWeb/mis-solicitudes.html';
		}
		
		function goHome() {
		    window.location.href = '/ContactoProfesionalesWeb/dashboard.html';
		}
		
		//#### 3.2. Auto-cargar ubicación del cliente
		
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
		
		console.log('✅ Script cargado completamente');
		
