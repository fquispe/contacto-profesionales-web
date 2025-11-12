/**
 * API para gestión de especialidades de profesionales
 * Integración con EspecialidadServlet del backend
 */

class EspecialidadAPI {
    constructor() {
        this.baseURL = '/ContactoProfesionalesWeb/api';
    }

    /**
     * Obtiene las especialidades de un profesional
     * @param {number} profesionalId - ID del profesional
     * @returns {Promise<Array>} Lista de especialidades
     */
    async listar(profesionalId) {
        if (!profesionalId) {
            throw new Error('profesionalId es requerido');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/especialidades`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al obtener especialidades');
            }
        } catch (error) {
            console.error('Error en listar especialidades:', error);
            throw error;
        }
    }

    /**
     * Agrega una nueva especialidad al profesional
     * @param {number} profesionalId - ID del profesional
     * @param {object} especialidadData - Datos de la especialidad {categoriaId, aniosExperiencia, descripcion, esPrincipal}
     * @returns {Promise<object>} Especialidad creada
     */
    async agregar(profesionalId, especialidadData) {
        if (!profesionalId) {
            throw new Error('profesionalId es requerido');
        }

        if (!especialidadData.categoriaId) {
            throw new Error('categoriaId es requerido');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/especialidades`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(especialidadData)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || data.message || 'Error al agregar especialidad');
            }
        } catch (error) {
            console.error('Error en agregar especialidad:', error);
            throw error;
        }
    }

    /**
     * Marca una especialidad como principal
     * @param {number} profesionalId - ID del profesional
     * @param {number} especialidadId - ID de la especialidad
     * @returns {Promise<boolean>} true si se marcó correctamente
     */
    async marcarComoPrincipal(profesionalId, especialidadId) {
        if (!profesionalId || !especialidadId) {
            throw new Error('profesionalId y especialidadId son requeridos');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/especialidades/${especialidadId}/principal`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return true;
            } else {
                throw new Error(data.error?.mensaje || 'Error al marcar especialidad como principal');
            }
        } catch (error) {
            console.error('Error en marcarComoPrincipal:', error);
            throw error;
        }
    }

    /**
     * Elimina una especialidad
     * @param {number} profesionalId - ID del profesional
     * @param {number} especialidadId - ID de la especialidad
     * @returns {Promise<boolean>} true si se eliminó correctamente
     */
    async eliminar(profesionalId, especialidadId) {
        if (!profesionalId || !especialidadId) {
            throw new Error('profesionalId y especialidadId son requeridos');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/especialidades/${especialidadId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return true;
            } else {
                throw new Error(data.error?.mensaje || 'Error al eliminar especialidad');
            }
        } catch (error) {
            console.error('Error en eliminar especialidad:', error);
            throw error;
        }
    }

    /**
     * Valida que el profesional no tenga más de 3 especialidades
     * @param {Array} especialidades - Array de especialidades actuales
     * @returns {boolean} true si puede agregar más
     */
    puedeAgregarMas(especialidades) {
        return especialidades.length < 3;
    }

    /**
     * Renderiza las especialidades en un contenedor
     * @param {HTMLElement} container - Contenedor donde renderizar
     * @param {Array} especialidades - Array de especialidades
     * @param {object} callbacks - Callbacks {onEliminar, onMarcarPrincipal}
     */
    renderizar(container, especialidades, callbacks = {}) {
        container.innerHTML = '';

        if (especialidades.length === 0) {
            container.innerHTML = '<p class="text-muted">No has agregado especialidades aún. Agrega hasta 3 especialidades.</p>';
            return;
        }

        especialidades.forEach(esp => {
            const item = document.createElement('div');
            item.className = 'especialidad-item' + (esp.esPrincipal ? ' principal' : '');
            item.innerHTML = `
                <div class="especialidad-info">
                    <h4>${esp.categoriaNombre || 'Categoría'}</h4>
                    ${esp.aniosExperiencia ? `<span class="anos">⏱ ${esp.aniosExperiencia} años de experiencia</span>` : ''}
                    ${esp.descripcion ? `<p>${esp.descripcion}</p>` : ''}
                    ${esp.esPrincipal ? '<span class="badge badge-primary">Principal</span>' : ''}
                </div>
                <div class="especialidad-actions">
                    ${!esp.esPrincipal ? `<button class="btn-icon btn-principal" title="Marcar como principal" data-id="${esp.id}">⭐</button>` : ''}
                    <button class="btn-icon btn-eliminar" title="Eliminar" data-id="${esp.id}">🗑️</button>
                </div>
            `;

            // Event listeners
            const btnEliminar = item.querySelector('.btn-eliminar');
            if (btnEliminar && callbacks.onEliminar) {
                btnEliminar.addEventListener('click', () => callbacks.onEliminar(esp.id));
            }

            const btnPrincipal = item.querySelector('.btn-principal');
            if (btnPrincipal && callbacks.onMarcarPrincipal) {
                btnPrincipal.addEventListener('click', () => callbacks.onMarcarPrincipal(esp.id));
            }

            container.appendChild(item);
        });
    }
}

// Exportar como singleton
const especialidadAPI = new EspecialidadAPI();
