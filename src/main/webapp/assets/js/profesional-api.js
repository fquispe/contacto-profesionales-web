/**
 * Cliente API para gestión del perfil profesional completo.
 *
 * Este cliente interactúa con todos los endpoints del backend:
 * - /api/profesional/perfil (perfil completo consolidado)
 * - /api/profesional/certificaciones
 * - /api/profesional/proyectos
 * - /api/profesional/proyectos/imagenes
 * - /api/profesional/antecedentes
 * - /api/profesional/redes-sociales
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */

const PerfilProfesionalAPI = {
    baseURL: '/ContactoProfesionalesWeb/api/profesional',

    // ========================================
    // PERFIL COMPLETO (Consolidado)
    // ========================================

    /**
     * Obtiene el perfil completo del profesional.
     * Incluye: datos básicos, certificaciones, proyectos, antecedentes, redes sociales, etc.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro (desde localStorage)
     *
     * @param {number} profesionalId - ID del profesional
     * @returns {Promise<Object>} Perfil completo consolidado
     */
    async obtenerPerfilCompleto(profesionalId) {
        try {
            // ✅ Enviar profesionalId como query parameter
            const response = await fetch(`${this.baseURL}/perfil?profesionalId=${profesionalId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al obtener perfil completo');
            }

            return result.data;
        } catch (error) {
            console.error('Error en obtenerPerfilCompleto:', error);
            throw error;
        }
    },

    /**
     * Actualiza los datos básicos del perfil profesional.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora envía profesionalId como query parameter
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Object} datos - Datos básicos a actualizar
     * @returns {Promise<Object>} Perfil actualizado
     */
    async actualizarDatosBasicos(profesionalId, datos) {
        try {
            const response = await fetch(`${this.baseURL}/perfil?profesionalId=${profesionalId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(datos)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al actualizar perfil');
            }

            return result.data;
        } catch (error) {
            console.error('Error en actualizarDatosBasicos:', error);
            throw error;
        }
    },

    // ========================================
    // CERTIFICACIONES
    // ========================================

    /**
     * Lista todas las certificaciones del profesional.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @returns {Promise<Array>} Lista de certificaciones
     */
    async listarCertificaciones(profesionalId) {
        try {
            const response = await fetch(`${this.baseURL}/certificaciones?profesionalId=${profesionalId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al listar certificaciones');
            }

            return result.data || [];
        } catch (error) {
            console.error('Error en listarCertificaciones:', error);
            throw error;
        }
    },

    /**
     * Crea una nueva certificación.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Object} certificacion - Datos de la certificación
     * @returns {Promise<Object>} Certificación creada
     */
    async crearCertificacion(profesionalId, certificacion) {
        try {
            const response = await fetch(`${this.baseURL}/certificaciones?profesionalId=${profesionalId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(certificacion)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al crear certificación');
            }

            return result.data;
        } catch (error) {
            console.error('Error en crearCertificacion:', error);
            throw error;
        }
    },

    /**
     * Actualiza una certificación existente.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Object} certificacion - Certificación con datos actualizados (debe incluir id)
     * @returns {Promise<Object>} Certificación actualizada
     */
    async actualizarCertificacion(profesionalId, certificacion) {
        try {
            const response = await fetch(`${this.baseURL}/certificaciones?profesionalId=${profesionalId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(certificacion)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al actualizar certificación');
            }

            return result.data;
        } catch (error) {
            console.error('Error en actualizarCertificacion:', error);
            throw error;
        }
    },

    /**
     * Elimina (soft delete) una certificación.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {number} id - ID de la certificación
     * @returns {Promise<boolean>} true si se eliminó correctamente
     */
    async eliminarCertificacion(profesionalId, id) {
        try {
            const response = await fetch(`${this.baseURL}/certificaciones?profesionalId=${profesionalId}&id=${id}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al eliminar certificación');
            }

            return true;
        } catch (error) {
            console.error('Error en eliminarCertificacion:', error);
            throw error;
        }
    },

    // ========================================
    // PROYECTOS DEL PORTAFOLIO
    // ========================================

    /**
     * Lista todos los proyectos del profesional (con imágenes).
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @returns {Promise<Array>} Lista de proyectos
     */
    async listarProyectos(profesionalId) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos?profesionalId=${profesionalId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al listar proyectos');
            }

            return result.data || [];
        } catch (error) {
            console.error('Error en listarProyectos:', error);
            throw error;
        }
    },

    /**
     * Busca un proyecto específico por ID.
     *
     * @param {number} id - ID del proyecto
     * @returns {Promise<Object>} Proyecto con imágenes
     */
    async obtenerProyecto(id) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos?id=${id}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al obtener proyecto');
            }

            return result.data;
        } catch (error) {
            console.error('Error en obtenerProyecto:', error);
            throw error;
        }
    },

    /**
     * Crea un nuevo proyecto.
     * Valida que no se exceda el límite de 20 proyectos.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Object} proyecto - Datos del proyecto
     * @returns {Promise<Object>} Proyecto creado
     */
    async crearProyecto(profesionalId, proyecto) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos?profesionalId=${profesionalId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(proyecto)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al crear proyecto');
            }

            return result.data;
        } catch (error) {
            console.error('Error en crearProyecto:', error);
            throw error;
        }
    },

    /**
     * Actualiza un proyecto existente.
     * NO permite actualizar calificación del cliente (solo lectura).
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Object} proyecto - Proyecto con datos actualizados (debe incluir id)
     * @returns {Promise<Object>} Proyecto actualizado
     */
    async actualizarProyecto(profesionalId, proyecto) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos?profesionalId=${profesionalId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(proyecto)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al actualizar proyecto');
            }

            return result.data;
        } catch (error) {
            console.error('Error en actualizarProyecto:', error);
            throw error;
        }
    },

    /**
     * Elimina (soft delete) un proyecto.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {number} id - ID del proyecto
     * @returns {Promise<boolean>} true si se eliminó correctamente
     */
    async eliminarProyecto(profesionalId, id) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos?profesionalId=${profesionalId}&id=${id}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al eliminar proyecto');
            }

            return true;
        } catch (error) {
            console.error('Error en eliminarProyecto:', error);
            throw error;
        }
    },

    // ========================================
    // IMÁGENES DE PROYECTOS
    // ========================================

    /**
     * Lista todas las imágenes de un proyecto.
     *
     * @param {number} proyectoId - ID del proyecto
     * @returns {Promise<Array>} Lista de imágenes
     */
    async listarImagenesProyecto(proyectoId) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos/imagenes?proyectoId=${proyectoId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al listar imágenes');
            }

            return result.data || [];
        } catch (error) {
            console.error('Error en listarImagenesProyecto:', error);
            throw error;
        }
    },

    /**
     * Crea una nueva imagen para un proyecto.
     * Valida que no se exceda el límite de 5 imágenes por proyecto.
     *
     * @param {Object} imagen - Datos de la imagen (con proyectoId y urlImagen)
     * @returns {Promise<Object>} Imagen creada
     */
    async crearImagenProyecto(imagen) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos/imagenes`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(imagen)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al crear imagen');
            }

            return result.data;
        } catch (error) {
            console.error('Error en crearImagenProyecto:', error);
            throw error;
        }
    },

    /**
     * Elimina (DELETE físico) una imagen de proyecto.
     *
     * @param {number} id - ID de la imagen
     * @returns {Promise<boolean>} true si se eliminó correctamente
     */
    async eliminarImagenProyecto(id) {
        try {
            const response = await fetch(`${this.baseURL}/proyectos/imagenes?id=${id}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al eliminar imagen');
            }

            return true;
        } catch (error) {
            console.error('Error en eliminarImagenProyecto:', error);
            throw error;
        }
    },

    // ========================================
    // ANTECEDENTES
    // ========================================

    /**
     * Lista todos los antecedentes del profesional.
     *
     * @returns {Promise<Array>} Lista de antecedentes
     */
    async listarAntecedentes() {
        try {
            const response = await fetch(`${this.baseURL}/antecedentes`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al listar antecedentes');
            }

            return result.data || [];
        } catch (error) {
            console.error('Error en listarAntecedentes:', error);
            throw error;
        }
    },

    /**
     * Crea un nuevo antecedente.
     * Valida que no exista ya un antecedente del mismo tipo.
     *
     * @param {Object} antecedente - Datos del antecedente
     * @returns {Promise<Object>} Antecedente creado
     */
    async crearAntecedente(antecedente) {
        try {
            const response = await fetch(`${this.baseURL}/antecedentes`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(antecedente)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al crear antecedente');
            }

            return result.data;
        } catch (error) {
            console.error('Error en crearAntecedente:', error);
            throw error;
        }
    },

    /**
     * Actualiza un antecedente existente.
     * NO permite modificar el estado de verificación.
     *
     * @param {Object} antecedente - Antecedente con datos actualizados (debe incluir id)
     * @returns {Promise<Object>} Antecedente actualizado
     */
    async actualizarAntecedente(antecedente) {
        try {
            const response = await fetch(`${this.baseURL}/antecedentes`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(antecedente)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al actualizar antecedente');
            }

            return result.data;
        } catch (error) {
            console.error('Error en actualizarAntecedente:', error);
            throw error;
        }
    },

    /**
     * Elimina (soft delete) un antecedente.
     *
     * @param {number} id - ID del antecedente
     * @returns {Promise<boolean>} true si se eliminó correctamente
     */
    async eliminarAntecedente(id) {
        try {
            const response = await fetch(`${this.baseURL}/antecedentes?id=${id}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al eliminar antecedente');
            }

            return true;
        } catch (error) {
            console.error('Error en eliminarAntecedente:', error);
            throw error;
        }
    },

    // ========================================
    // REDES SOCIALES
    // ========================================

    /**
     * Lista todas las redes sociales del profesional.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @returns {Promise<Array>} Lista de redes sociales
     */
    async listarRedesSociales(profesionalId) {
        try {
            const response = await fetch(`${this.baseURL}/redes-sociales?profesionalId=${profesionalId}`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al listar redes sociales');
            }

            return result.data || [];
        } catch (error) {
            console.error('Error en listarRedesSociales:', error);
            throw error;
        }
    },

    /**
     * Crea una nueva red social.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Object} redSocial - Datos de la red social
     * @returns {Promise<Object>} Red social creada
     */
    async crearRedSocial(profesionalId, redSocial) {
        try {
            const response = await fetch(`${this.baseURL}/redes-sociales?profesionalId=${profesionalId}`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(redSocial)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al crear red social');
            }

            return result.data;
        } catch (error) {
            console.error('Error en crearRedSocial:', error);
            throw error;
        }
    },

    /**
     * Actualiza una red social existente.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Object} redSocial - Red social con datos actualizados (debe incluir id)
     * @returns {Promise<Object>} Red social actualizada
     */
    async actualizarRedSocial(profesionalId, redSocial) {
        try {
            const response = await fetch(`${this.baseURL}/redes-sociales?profesionalId=${profesionalId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(redSocial)
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al actualizar red social');
            }

            return result.data;
        } catch (error) {
            console.error('Error en actualizarRedSocial:', error);
            throw error;
        }
    },

    /**
     * Actualización masiva de redes sociales (transaccional).
     * Desactiva las no enviadas, actualiza existentes, inserta nuevas.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {Array} redesSociales - Array de redes sociales
     * @returns {Promise<Array>} Redes sociales actualizadas
     */
    async actualizarRedesSocialesMultiples(profesionalId, redesSociales) {
        try {
            const response = await fetch(`${this.baseURL}/redes-sociales?profesionalId=${profesionalId}`, {
                method: 'PUT',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                },
                body: JSON.stringify(redesSociales) // Enviar array
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al actualizar redes sociales');
            }

            return result.data;
        } catch (error) {
            console.error('Error en actualizarRedesSocialesMultiples:', error);
            throw error;
        }
    },

    /**
     * Elimina (soft delete) una red social.
     *
     * ✅ ACTUALIZADO 2025-12-04: Ahora recibe profesionalId como parámetro
     *
     * @param {number} profesionalId - ID del profesional
     * @param {number} id - ID de la red social
     * @returns {Promise<boolean>} true si se eliminó correctamente
     */
    async eliminarRedSocial(profesionalId, id) {
        try {
            const response = await fetch(`${this.baseURL}/redes-sociales?profesionalId=${profesionalId}&id=${id}`, {
                method: 'DELETE',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            const result = await response.json();

            if (!response.ok) {
                throw new Error(result.error || 'Error al eliminar red social');
            }

            return true;
        } catch (error) {
            console.error('Error en eliminarRedSocial:', error);
            throw error;
        }
    },

    // ========================================
    // ESPECIALIDADES DEL PROFESIONAL
    // Actualizado: 2025-11-16
    // ========================================

    /**
     * Obtiene las especialidades/categorías del profesional autenticado.
     * Útil para poblar el selector de categorías al crear proyectos.
     *
     * ✅ ACTUALIZADO 2025-11-17: Corregido para extraer data del objeto respuesta
     *
     * @param {number} profesionalId - ID del profesional
     * @returns {Promise<Array>} Lista de especialidades con sus categorías
     */
    async obtenerEspecialidades(profesionalId) {
        try {
            const url = `${this.baseURL.replace('/perfil', '')}/../profesionales/${profesionalId}/especialidades`;
            console.log('🔍 URL para obtener especialidades:', url);

            const response = await fetch(url, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + this.getToken()
                }
            });

            console.log('🔍 Response status:', response.status);

            if (!response.ok) {
                // Si no hay especialidades, devolver array vacío
                if (response.status === 404) {
                    return [];
                }
                throw new Error('Error al obtener especialidades');
            }

            const result = await response.json();
            console.log('🔍 Response JSON completo:', result);
            console.log('🔍 Tipo de result.data:', typeof result.data, '- Es array?', Array.isArray(result.data));

            // ✅ El servlet devuelve {success: true, data: [...]}
            // Extraer el array de data
            if (result && result.success && Array.isArray(result.data)) {
                console.log('✅ Caso 1: result.data es array directo');
                return result.data;
            }

            // ✅ NUEVO: Si data es un objeto, buscar propiedades que puedan contener el array
            if (result && result.success && result.data && typeof result.data === 'object' && !Array.isArray(result.data)) {
                console.log('🔍 result.data es un objeto. Propiedades:', Object.keys(result.data));

                // Intentar extraer array de propiedades comunes
                if (Array.isArray(result.data.especialidades)) {
                    console.log('✅ Caso 2: Encontrado array en result.data.especialidades');
                    return result.data.especialidades;
                }
                if (Array.isArray(result.data.items)) {
                    console.log('✅ Caso 3: Encontrado array en result.data.items');
                    return result.data.items;
                }
                if (Array.isArray(result.data.profesional)) {
                    console.log('✅ Caso 4: Encontrado array en result.data.profesional');
                    return result.data.profesional;
                }

                console.warn('⚠️ result.data es objeto pero no contiene array conocido:', result.data);
            }

            // Si no tiene la estructura esperada, intentar devolver result directamente
            if (Array.isArray(result)) {
                console.log('✅ Caso 5: result es array directo');
                return result;
            }

            // Si no es array, devolver vacío
            console.warn('❌ La respuesta de especialidades no tiene la estructura esperada:', result);
            return [];
        } catch (error) {
            console.error('Error en obtenerEspecialidades:', error);
            return []; // Devolver array vacío en caso de error
        }
    },

    // ========================================
    // UTILIDADES
    // ========================================

    /**
     * Obtiene el token de autenticación.
     * TODO: Implementar obtención real del token desde localStorage/sessionStorage
     *
     * @returns {string} Token JWT
     */
    getToken() {
        // TODO: Implementar obtención real del token
        // return localStorage.getItem('authToken') || '';
        return 'test-token'; // Por ahora token de prueba
    }
};

// Exportar para uso global
window.PerfilProfesionalAPI = PerfilProfesionalAPI;
