/**
 * API para gestión de redes sociales de profesionales
 * Integración con RedSocialServlet del backend
 */

class RedSocialAPI {
    constructor() {
        this.baseURL = '/ContactoProfesionalesWeb/api';

        // Tipos de redes sociales soportados
        this.tiposRed = {
            FACEBOOK: { nombre: 'Facebook', icono: '📘', placeholder: 'https://facebook.com/tu-perfil' },
            INSTAGRAM: { nombre: 'Instagram', icono: '📷', placeholder: 'https://instagram.com/tu-usuario' },
            LINKEDIN: { nombre: 'LinkedIn', icono: '💼', placeholder: 'https://linkedin.com/in/tu-perfil' },
            TWITTER: { nombre: 'Twitter', icono: '🐦', placeholder: 'https://twitter.com/tu-usuario' },
            TIKTOK: { nombre: 'TikTok', icono: '🎵', placeholder: 'https://tiktok.com/@tu-usuario' },
            WHATSAPP: { nombre: 'WhatsApp', icono: '💬', placeholder: '51999999999' },
            WEBSITE: { nombre: 'Sitio Web', icono: '🌐', placeholder: 'https://tusitio.com' },
            YOUTUBE: { nombre: 'YouTube', icono: '▶️', placeholder: 'https://youtube.com/c/tu-canal' }
        };
    }

    /**
     * Obtiene las redes sociales de un profesional
     * @param {number} profesionalId - ID del profesional
     * @returns {Promise<Array>} Lista de redes sociales
     */
    async listar(profesionalId) {
        if (!profesionalId) {
            throw new Error('profesionalId es requerido');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/redes-sociales`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al obtener redes sociales');
            }
        } catch (error) {
            console.error('Error en listar redes sociales:', error);
            throw error;
        }
    }

    /**
     * Agrega una nueva red social
     * @param {number} profesionalId - ID del profesional
     * @param {object} redSocialData - Datos {tipoRed, url}
     * @returns {Promise<object>} Red social creada
     */
    async agregar(profesionalId, redSocialData) {
        if (!profesionalId) {
            throw new Error('profesionalId es requerido');
        }

        if (!redSocialData.tipoRed || !redSocialData.url) {
            throw new Error('tipoRed y url son requeridos');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/redes-sociales`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(redSocialData)
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || data.message || 'Error al agregar red social');
            }
        } catch (error) {
            console.error('Error en agregar red social:', error);
            throw error;
        }
    }

    /**
     * Actualiza la URL de una red social
     * @param {number} profesionalId - ID del profesional
     * @param {number} redSocialId - ID de la red social
     * @param {string} nuevaUrl - Nueva URL
     * @returns {Promise<object>} Red social actualizada
     */
    async actualizar(profesionalId, redSocialId, nuevaUrl) {
        if (!profesionalId || !redSocialId || !nuevaUrl) {
            throw new Error('Todos los parámetros son requeridos');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/redes-sociales/${redSocialId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ url: nuevaUrl })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al actualizar red social');
            }
        } catch (error) {
            console.error('Error en actualizar red social:', error);
            throw error;
        }
    }

    /**
     * Elimina una red social
     * @param {number} profesionalId - ID del profesional
     * @param {number} redSocialId - ID de la red social
     * @returns {Promise<boolean>} true si se eliminó correctamente
     */
    async eliminar(profesionalId, redSocialId) {
        if (!profesionalId || !redSocialId) {
            throw new Error('profesionalId y redSocialId son requeridos');
        }

        try {
            const response = await fetch(`${this.baseURL}/profesionales/${profesionalId}/redes-sociales/${redSocialId}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return true;
            } else {
                throw new Error(data.error?.mensaje || 'Error al eliminar red social');
            }
        } catch (error) {
            console.error('Error en eliminar red social:', error);
            throw error;
        }
    }

    /**
     * Obtiene información de un tipo de red
     * @param {string} tipoRed - Tipo de red (FACEBOOK, INSTAGRAM, etc.)
     * @returns {object} Información del tipo de red
     */
    getInfoTipoRed(tipoRed) {
        return this.tiposRed[tipoRed] || { nombre: tipoRed, icono: '🔗', placeholder: 'URL' };
    }

    /**
     * Popula un select con los tipos de redes sociales disponibles
     * @param {HTMLSelectElement} selectElement - Elemento select a popular
     * @param {Array} redesExistentes - Redes ya agregadas (para deshabilitarlas)
     */
    popularTiposRed(selectElement, redesExistentes = []) {
        const tiposUsados = new Set(redesExistentes.map(r => r.tipoRed));

        selectElement.innerHTML = '<option value="">Selecciona una red social</option>';

        Object.keys(this.tiposRed).forEach(tipo => {
            const info = this.tiposRed[tipo];
            const option = document.createElement('option');
            option.value = tipo;
            option.textContent = `${info.icono} ${info.nombre}`;
            option.disabled = tiposUsados.has(tipo);
            selectElement.appendChild(option);
        });
    }

    /**
     * Renderiza las redes sociales en un contenedor
     * @param {HTMLElement} container - Contenedor donde renderizar
     * @param {Array} redesSociales - Array de redes sociales
     * @param {object} callbacks - Callbacks {onEliminar, onEditar}
     */
    renderizar(container, redesSociales, callbacks = {}) {
        container.innerHTML = '';

        if (redesSociales.length === 0) {
            container.innerHTML = '<p class="text-muted">No has agregado redes sociales aún.</p>';
            return;
        }

        redesSociales.forEach(red => {
            const info = this.getInfoTipoRed(red.tipoRed);
            const item = document.createElement('div');
            item.className = 'red-social-item';
            item.innerHTML = `
                <div class="red-social-info">
                    <span class="red-icono">${info.icono}</span>
                    <div class="red-detalles">
                        <h4>${info.nombre}</h4>
                        <a href="${red.url}" target="_blank" rel="noopener" class="red-url">${red.url}</a>
                        ${red.verificada ? '<span class="badge badge-success">✓ Verificada</span>' : ''}
                    </div>
                </div>
                <div class="red-social-actions">
                    <button class="btn-icon btn-editar" title="Editar URL" data-id="${red.id}">✏️</button>
                    <button class="btn-icon btn-eliminar" title="Eliminar" data-id="${red.id}">🗑️</button>
                </div>
            `;

            // Event listeners
            const btnEliminar = item.querySelector('.btn-eliminar');
            if (btnEliminar && callbacks.onEliminar) {
                btnEliminar.addEventListener('click', () => callbacks.onEliminar(red.id));
            }

            const btnEditar = item.querySelector('.btn-editar');
            if (btnEditar && callbacks.onEditar) {
                btnEditar.addEventListener('click', () => callbacks.onEditar(red.id, red.url));
            }

            container.appendChild(item);
        });
    }
}

// Exportar como singleton
const redSocialAPI = new RedSocialAPI();
