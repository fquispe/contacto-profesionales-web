// Información Personal JavaScript - Desacoplado del HTML el 2025-11-21
        // ============================================
        // VARIABLES GLOBALES
        // ============================================
        let userData = {};
        let usuarioPersonaId = null;
        let departamentos = [];
        let provincias = [];
        let distritos = [];

        // ============================================
        // INICIALIZACIÓN
        // ============================================
        document.addEventListener('DOMContentLoaded', async () => {
		    // Obtener datos del usuario del localStorage
		    userData = JSON.parse(localStorage.getItem('userData') || '{}');
		    
		    if (!userData.id) {
		        alert('Sesión no válida. Por favor inicie sesión nuevamente.');
		        window.location.href = 'login.html';
		        return;
		    }
		
		    // Obtener usuarioPersonaId del localStorage
		    usuarioPersonaId = userData.usuarioPersonaId;
		
		    console.log('Usuario cargado:', {
		        userId: userData.id,
		        email: userData.email,
		        usuarioPersonaId: usuarioPersonaId
		    });
		
		    // Mostrar email (no editable)
		    document.getElementById('email').value = userData.email || '';
		
		    // Cargar departamentos primero
		    await cargarDepartamentos();
		
		    // Cargar datos existentes del usuario
		    if (usuarioPersonaId) {
		        console.log('Cargando datos existentes del usuario persona ID:', usuarioPersonaId);
		        await cargarDatosUsuario();
		    } else {
		        console.log('Usuario nuevo sin datos personales previos');
		        // Prellenar con lo básico que tenemos
		        if (userData.nombreCompleto) {
		            document.getElementById('nombreCompleto').value = userData.nombreCompleto;
		        }
		        if (userData.telefono) {
		            document.getElementById('telefono').value = userData.telefono;
		        }
		    }
		
		    // Configurar validaciones
		    setupValidations();
		
		    // Configurar envío del formulario
		    document.getElementById('personalInfoForm').addEventListener('submit', handleSubmit);
		
		    // Calcular completitud inicial
		    calcularCompletitud();
		});

        // ============================================
        // CARGAR DATOS DEL USUARIO
        // ============================================
        async function cargarDatosUsuario() {
		    if (!usuarioPersonaId) {
		        console.log('No hay usuarioPersonaId, es un registro nuevo');
		        return;
		    }
		
		    try {
		        console.log('Obteniendo datos de usuario persona desde el backend...');
		        
		        // Llamar al API para obtener los datos completos
		        const response = await fetch(`${API_BASE_URL}/usuario-persona/${usuarioPersonaId}`);
		        
		        if (!response.ok) {
		            if (response.status === 404) {
		                console.log('Usuario persona no encontrado en BD, es un registro nuevo');
		                return;
		            }
		            throw new Error(`Error HTTP: ${response.status}`);
		        }
		
		        const data = await response.json();
		        console.log('Respuesta del servidor:', data);
		
		        if (data.success && data.data) {
		            console.log('Datos recibidos, rellenando formulario...');
		            rellenarFormulario(data.data);
		        } else {
		            console.warn('No se recibieron datos del usuario persona');
		        }
		
		    } catch (error) {
		        console.error('Error al cargar datos del usuario:', error);
		        showAlert('⚠️ No se pudieron cargar algunos datos. Puedes continuar ingresándolos manualmente.', 'info');
		    }
		}

        function rellenarFormulario(data) {
            console.log('Rellenando formulario con:', data);

            // Información básica
            if (data.nombreCompleto) {
                document.getElementById('nombreCompleto').value = data.nombreCompleto;
                console.log('Nombre completo:', data.nombreCompleto);
            }
            if (data.telefono) {
                document.getElementById('telefono').value = data.telefono;
                console.log('Teléfono:', data.telefono);
            }
            if (data.telefonoAlternativo) {
                document.getElementById('telefonoAlternativo').value = data.telefonoAlternativo;
            }

            // Documento
            if (data.tipoDocumento) {
                document.getElementById('tipoDocumento').value = data.tipoDocumento;
                console.log('Tipo documento:', data.tipoDocumento);
            }
            if (data.numeroDocumento) {
                document.getElementById('numeroDocumento').value = data.numeroDocumento;
                console.log('Número documento:', data.numeroDocumento);
            }

            // Datos personales
            if (data.fechaNacimiento) {
                document.getElementById('fechaNacimiento').value = data.fechaNacimiento;
            }
            if (data.genero) {
                document.getElementById('genero').value = data.genero;
            }

            // Ubicación - cargar en cascada
            if (data.departamentoId) {
                console.log('Cargando ubicación: Depto', data.departamentoId);
                document.getElementById('departamento').value = data.departamentoId;
                
                cargarProvincias().then(() => {
                    if (data.provinciaId) {
                        console.log('Cargando provincia:', data.provinciaId);
                        document.getElementById('provincia').value = data.provinciaId;
                        
                        cargarDistritos().then(() => {
                            if (data.distritoId) {
                                console.log('Cargando distrito:', data.distritoId);
                                document.getElementById('distrito').value = data.distritoId;
                            }
                        });
                    }
                });
            }

            if (data.direccion) {
                document.getElementById('direccion').value = data.direccion;
            }
            if (data.referenciaDireccion) {
                document.getElementById('referenciaDireccion').value = data.referenciaDireccion;
            }

            // Foto de perfil
            if (data.fotoPerfilUrl) {
                document.getElementById('photoPreview').innerHTML = 
                    `<img src="${data.fotoPerfilUrl}" alt="Foto de perfil">`;
            }

            console.log('Formulario rellenado completamente');
        }

        // ============================================
        // UBICACIÓN (UBIGEO)
        // ============================================
        // Configuración de la URL base de la API - Corregido el 2025-11-21
        const API_BASE_URL = '/ContactoProfesionalesWeb/api';

        async function cargarDepartamentos() {
            try {
                const response = await fetch(`${API_BASE_URL}/ubicacion/departamentos`);
                const data = await response.json();
                
                if (data.success && data.data) {
                    departamentos = data.data;
                    const select = document.getElementById('departamento');
                    
                    departamentos.forEach(dept => {
                        const option = document.createElement('option');
                        option.value = dept.id;
                        option.textContent = dept.nombre;
                        select.appendChild(option);
                    });
                }
            } catch (error) {
                console.error('Error al cargar departamentos:', error);
            }
        }

        async function cargarProvincias() {
            const departamentoId = document.getElementById('departamento').value;
            const provinciaSelect = document.getElementById('provincia');
            const distritoSelect = document.getElementById('distrito');

            // Limpiar selects
            provinciaSelect.innerHTML = '<option value="">Seleccione...</option>';
            distritoSelect.innerHTML = '<option value="">Seleccione provincia primero...</option>';
            distritoSelect.disabled = true;

            if (!departamentoId) {
                provinciaSelect.disabled = true;
                return;
            }

            try {
                const response = await fetch(`${API_BASE_URL}/ubicacion/provincias?departamentoId=${departamentoId}`);
                const data = await response.json();
                
                if (data.success && data.data) {
                    provincias = data.data;
                    provincias.forEach(prov => {
                        const option = document.createElement('option');
                        option.value = prov.id;
                        option.textContent = prov.nombre;
                        provinciaSelect.appendChild(option);
                    });
                    provinciaSelect.disabled = false;
                }
            } catch (error) {
                console.error('Error al cargar provincias:', error);
            }
        }

        async function cargarDistritos() {
            const provinciaId = document.getElementById('provincia').value;
            const distritoSelect = document.getElementById('distrito');

            distritoSelect.innerHTML = '<option value="">Seleccione...</option>';

            if (!provinciaId) {
                distritoSelect.disabled = true;
                return;
            }

            try {
                const response = await fetch(`${API_BASE_URL}/ubicacion/distritos?provinciaId=${provinciaId}`);
                const data = await response.json();
                
                if (data.success && data.data) {
                    distritos = data.data;
                    distritos.forEach(dist => {
                        const option = document.createElement('option');
                        option.value = dist.id;
                        option.textContent = dist.nombre;
                        distritoSelect.appendChild(option);
                    });
                    distritoSelect.disabled = false;
                }
            } catch (error) {
                console.error('Error al cargar distritos:', error);
            }
        }

        // ============================================
        // PREVIEW DE IMAGEN
        // ============================================
        function previewImage(event) {
		    const file = event.target.files[0];
		    if (file) {
		        // Validar tamaño (máx 2MB)
		        if (file.size > 2 * 1024 * 1024) {
		            showAlert('La imagen no debe superar los 2MB', 'error');
		            event.target.value = '';
		            return;
		        }
		
		        // Validar tipo
		        if (!file.type.startsWith('image/')) {
		            showAlert('Por favor selecciona una imagen válida', 'error');
		            event.target.value = '';
		            return;
		        }
		
		        // Guardar archivo para subir después
		        imagenPendiente = file;
		
		        // Mostrar preview
		        const reader = new FileReader();
		        reader.onload = function(e) {
		            document.getElementById('photoPreview').innerHTML = 
		                `<img src="${e.target.result}" alt="Preview">`;
		        };
		        reader.readAsDataURL(file);
		    }
		}

        
	    // ============================================
	    // SUBIR IMAGEN AL SERVIDOR
	    // ============================================
	    async function uploadImage() {
	         if (!imagenPendiente) {
	             return null; // No hay imagen nueva
	         }
	
	         try {
	             const formData = new FormData();
	             formData.append('file', imagenPendiente);
	
	             const response = await fetch(`${API_BASE_URL}/upload-image`, {
	                 method: 'POST',
	                 body: formData
	             });
	
	             const data = await response.json();
	
	             if (data.success && data.data && data.data.url) {
	                 console.log('Imagen subida exitosamente:', data.data.url);
	                 return data.data.url;
	             } else {
	                 throw new Error(data.message || 'Error al subir imagen');
	             }
	
	         } catch (error) {
	             console.error('Error al subir imagen:', error);
	             throw error;
	         }
	     }
        
        // ============================================
        // VALIDACIONES
        // ============================================
        function setupValidations() {
            document.getElementById('telefono').addEventListener('input', function(e) {
                e.target.value = e.target.value.replace(/[^0-9]/g, '');
            });

            document.getElementById('telefonoAlternativo').addEventListener('input', function(e) {
                e.target.value = e.target.value.replace(/[^0-9]/g, '');
            });

            document.getElementById('numeroDocumento').addEventListener('input', function(e) {
                const tipo = document.getElementById('tipoDocumento').value;
                if (tipo === 'DNI' || tipo === 'RUC') {
                    e.target.value = e.target.value.replace(/[^0-9]/g, '');
                }
            });

            // Calcular completitud al cambiar campos
            const campos = document.querySelectorAll('.form-input, .form-select');
            campos.forEach(campo => {
                campo.addEventListener('change', calcularCompletitud);
            });
        }

        function validarFormulario() {
            let isValid = true;

            // Nombre completo
            const nombre = document.getElementById('nombreCompleto').value.trim();
            if (nombre.length < 3) {
                showFieldError('nombreCompleto', 'El nombre debe tener al menos 3 caracteres');
                isValid = false;
            } else {
                clearFieldError('nombreCompleto');
            }

            // Teléfono
            const telefono = document.getElementById('telefono').value.trim();
            if (telefono.length !== 9) {
                showFieldError('telefono', 'El teléfono debe tener 9 dígitos');
                isValid = false;
            } else {
                clearFieldError('telefono');
            }

            // Documento
            const tipoDoc = document.getElementById('tipoDocumento').value;
            const numeroDoc = document.getElementById('numeroDocumento').value.trim();
            
            if (tipoDoc && numeroDoc) {
                if (tipoDoc === 'DNI' && numeroDoc.length !== 8) {
                    showFieldError('numeroDocumento', 'El DNI debe tener 8 dígitos');
                    isValid = false;
                } else if (tipoDoc === 'RUC' && numeroDoc.length !== 11) {
                    showFieldError('numeroDocumento', 'El RUC debe tener 11 dígitos');
                    isValid = false;
                } else {
                    clearFieldError('numeroDocumento');
                }
            }

            return isValid;
        }

        // ============================================
        // CALCULAR COMPLETITUD
        // ============================================
        function calcularCompletitud() {
            const camposTotal = 14; // Total de campos importantes
            let camposCompletos = 0;

            // Lista de campos a verificar
            const campos = [
                'nombreCompleto', 'telefono', 'tipoDocumento', 'numeroDocumento',
                'fechaNacimiento', 'genero', 'departamento', 'provincia',
                'distrito', 'direccion', 'referenciaDireccion', 'telefonoAlternativo'
            ];

            campos.forEach(id => {
                const elemento = document.getElementById(id);
                if (elemento && elemento.value && elemento.value.trim() !== '') {
                    camposCompletos++;
                }
            });

            // Verificar foto
            const photoPreview = document.getElementById('photoPreview');
            if (photoPreview.querySelector('img')) {
                camposCompletos++;
            }

            // Email siempre cuenta
            if (document.getElementById('email').value) {
                camposCompletos++;
            }

            const porcentaje = Math.round((camposCompletos / camposTotal) * 100);

            // Actualizar barra de progreso
            document.getElementById('progressFill').style.width = porcentaje + '%';
            document.getElementById('progressText').textContent = `Completitud: ${porcentaje}%`;
        }

        // ============================================
        // ENVÍO DEL FORMULARIO
        // ============================================
        async function handleSubmit(e) {
		    e.preventDefault();
		
		    if (!validarFormulario()) {
		        return;
		    }
		
		    const submitBtn = document.getElementById('submitBtn');
		    submitBtn.disabled = true;
		    submitBtn.innerHTML = '<span class="loading"></span>Guardando...';
		
		    try {
		        // PRIMERO: Subir imagen si hay una nueva
		        let fotoUrl = null;
		        if (imagenPendiente) {
		            submitBtn.innerHTML = '<span class="loading"></span>Subiendo imagen...';
		            try {
		                fotoUrl = await uploadImage();
		                console.log('URL de imagen obtenida:', fotoUrl);
		            } catch (error) {
		                showAlert('⚠️ Error al subir la imagen. Se guardará sin foto.', 'error');
		                console.error('Error subiendo imagen:', error);
		            }
		        }
		
		        submitBtn.innerHTML = '<span class="loading"></span>Guardando información...';
		
		        // Preparar datos
		        const formData = {
		            nombreCompleto: document.getElementById('nombreCompleto').value.trim(),
		            telefono: document.getElementById('telefono').value.trim(),
		            telefonoAlternativo: document.getElementById('telefonoAlternativo').value.trim() || null,
		            tipoDocumento: document.getElementById('tipoDocumento').value || null,
		            numeroDocumento: document.getElementById('numeroDocumento').value.trim() || null,
		            fechaNacimiento: document.getElementById('fechaNacimiento').value || null,
		            genero: document.getElementById('genero').value || null,
		            departamentoId: document.getElementById('departamento').value ? 
		                parseInt(document.getElementById('departamento').value) : null,
		            provinciaId: document.getElementById('provincia').value ? 
		                parseInt(document.getElementById('provincia').value) : null,
		            distritoId: document.getElementById('distrito').value ? 
		                parseInt(document.getElementById('distrito').value) : null,
		            direccion: document.getElementById('direccion').value.trim() || null,
		            referenciaDireccion: document.getElementById('referenciaDireccion').value.trim() || null,
		            fotoPerfilUrl: fotoUrl || null // URL de la imagen subida
		        };
		
		        console.log('Datos a enviar:', formData);
		        console.log('UsuarioPersonaId actual:', usuarioPersonaId);
		
		        // Determinar si es actualización o creación
		        let url, method;
		        
		        if (usuarioPersonaId) {
		            // ACTUALIZACIÓN
		            url = `${API_BASE_URL}/usuario-persona/${usuarioPersonaId}`;
		            method = 'PUT';
		            console.log('Modo: ACTUALIZACIÓN');
		        } else {
		            // CREACIÓN - incluir userId en el body
		            url = `${API_BASE_URL}/usuario-persona`;
		            method = 'POST';
		            formData.userId = userData.id;
		            console.log('Modo: CREACIÓN NUEVA');
		        }
		
		        console.log(`Enviando ${method} a ${url}`);
		
		        const response = await fetch(url, {
		            method: method,
		            headers: {
		                'Content-Type': 'application/json'
		            },
		            body: JSON.stringify(formData)
		        });
		
		        const data = await response.json();
		        console.log('Respuesta del servidor:', data);
		
		        if (data.success) {
		            showAlert('✅ Información guardada exitosamente', 'success');
		            
		            // Limpiar imagen pendiente
		            imagenPendiente = null;
		            
		            // Si era creación, guardar el nuevo usuarioPersonaId
		            if (!usuarioPersonaId && data.data && data.data.id) {
		                usuarioPersonaId = data.data.id;
		                userData.usuarioPersonaId = usuarioPersonaId;
		                localStorage.setItem('userData', JSON.stringify(userData));
		                console.log('Nuevo usuarioPersonaId guardado:', usuarioPersonaId);
		            }
		
		            // Actualizar localStorage con los nuevos datos
		            userData = { ...userData, ...formData };
		            localStorage.setItem('userData', JSON.stringify(userData));
		            console.log('userData actualizado en localStorage');
		
		            // Redirigir después de 2 segundos
		            setTimeout(() => {
		                window.location.href = 'dashboard.html';
		            }, 2000);
		        } else {
		            showAlert('❌ ' + (data.message || 'Error al guardar la información'), 'error');
		        }
		
		    } catch (error) {
		        console.error('Error al enviar formulario:', error);
		        showAlert('❌ Error de conexión con el servidor', 'error');
		    } finally {
		        submitBtn.disabled = false;
		        submitBtn.innerHTML = 'Guardar Información';
		    }
		}

        // ============================================
        // FUNCIONES AUXILIARES
        // ============================================
        function showFieldError(fieldId, message) {
            const input = document.getElementById(fieldId);
            const errorElement = document.getElementById(fieldId + 'Error');
            input.classList.add('error');
            if (errorElement) {
                errorElement.textContent = message;
                errorElement.style.display = 'block';
            }
        }

        function clearFieldError(fieldId) {
            const input = document.getElementById(fieldId);
            const errorElement = document.getElementById(fieldId + 'Error');
            input.classList.remove('error');
            if (errorElement) {
                errorElement.textContent = '';
                errorElement.style.display = 'none';
            }
        }

        function showAlert(message, type) {
            const alertContainer = document.getElementById('alertContainer');
            const alert = document.createElement('div');
            alert.className = `alert alert-${type}`;
            alert.textContent = message;
            alert.style.display = 'block';
            
            alertContainer.innerHTML = '';
            alertContainer.appendChild(alert);

            // Auto-ocultar después de 5 segundos
            setTimeout(() => {
                alert.style.display = 'none';
            }, 5000);

            // Scroll to top
            window.scrollTo({ top: 0, behavior: 'smooth' });
        }

        function volverDashboard() {
            if (confirm('¿Estás seguro? Los cambios no guardados se perderán.')) {
                window.location.href = 'dashboard.html';
            }
        }
