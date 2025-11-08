/**
 * Cliente API para consumir los servicios REST de gestión de clientes
 * Conecta con los componentes Java del backend
 */

class ClienteAPI {
    constructor(contextPath = null) {
        // Obtener el context path de la aplicación
        this.baseURL = (contextPath || this.getContextPath()) + '/api/clientes';
    }
    
    /**
     * Obtiene el context path de la aplicación
     * Para HTML estático en la raíz del proyecto, detecta automáticamente
     */
    getContextPath() {
        // Si está configurado manualmente, usar ese
        if (window.APP_CONTEXT_PATH) {
            return window.APP_CONTEXT_PATH;
        }
        
        // Detectar desde la URL actual
        const path = window.location.pathname;
        
        // Si la URL contiene .html, extraer el context path
        if (path.includes('.html')) {
            // Ejemplo: /mi-app/perfil_cliente.html -> /mi-app
            const parts = path.split('/');
            parts.pop(); // Remover el archivo .html
            return parts.join('/') || '';
        }
        
        // Para JSP o estructura diferente
        const contextPath = path.substring(0, path.indexOf('/', 1));
        return contextPath || '';
    }
    
    /**
     * Maneja los errores de las peticiones HTTP
     */
    async handleResponse(response) {
        const data = await response.json();
        
        if (!data.success) {
            throw new Error(data.error?.mensaje || 'Error en la operación');
        }
        
        return data.data;
    }
    
    /**
     * Registra un nuevo cliente
     * @param {Object} clienteData - Datos del cliente a registrar
     * @returns {Promise<Object>} Cliente registrado
     */
    async registrarCliente(clienteData) {
        try {
            const response = await fetch(this.baseURL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(clienteData)
            });
            
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Error al registrar cliente:', error);
            throw error;
        }
    }
    
    /**
     * Obtiene un cliente por su ID
     * @param {number} id - ID del cliente
     * @returns {Promise<Object>} Datos del cliente
     */
    async obtenerCliente(id) {
        try {
            const response = await fetch(`${this.baseURL}/${id}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });
            
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Error al obtener cliente:', error);
            throw error;
        }
    }
    
    /**
     * Actualiza los datos de un cliente
     * @param {number} id - ID del cliente
     * @param {Object} clienteData - Datos actualizados del cliente
     * @returns {Promise<Object>} Cliente actualizado
     */
    async actualizarCliente(id, clienteData) {
        try {
            const response = await fetch(`${this.baseURL}/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(clienteData)
            });
            
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Error al actualizar cliente:', error);
            throw error;
        }
    }
    
    /**
     * Lista todos los clientes activos
     * @returns {Promise<Array>} Lista de clientes
     */
    async listarClientes() {
        try {
            const response = await fetch(this.baseURL, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                }
            });
            
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Error al listar clientes:', error);
            throw error;
        }
    }
    
    /**
     * Desactiva un cliente (borrado lógico)
     * @param {number} id - ID del cliente
     * @returns {Promise<Object>} Confirmación de desactivación
     */
    async desactivarCliente(id) {
        try {
            const response = await fetch(`${this.baseURL}/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                }
            });
            
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Error al desactivar cliente:', error);
            throw error;
        }
    }
    
    /**
     * Busca un cliente por email (usando el endpoint de listado y filtrando)
     * @param {string} email - Email del cliente
     * @returns {Promise<Object|null>} Cliente encontrado o null
     */
    async buscarPorEmail(email) {
        try {
            const clientes = await this.listarClientes();
            return clientes.find(c => c.email === email) || null;
        } catch (error) {
            console.error('Error al buscar cliente por email:', error);
            throw error;
        }
    }
    
    /**
     * Valida los datos del cliente antes de enviarlos al servidor
     * @param {Object} clienteData - Datos del cliente a validar
     * @returns {Object} Resultado de la validación {valid: boolean, errores: Array}
     */
    validarDatosCliente(clienteData) {
        const errores = [];
        
        // Validar nombre completo
        if (!clienteData.nombreCompleto || clienteData.nombreCompleto.trim().length < 3) {
            errores.push('El nombre completo debe tener al menos 3 caracteres');
        }
        if (clienteData.nombreCompleto && clienteData.nombreCompleto.length > 100) {
            errores.push('El nombre completo no puede exceder 100 caracteres');
        }
        
        // Validar email
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!clienteData.email || !emailRegex.test(clienteData.email)) {
            errores.push('El email no es válido');
        }
        
        // Validar teléfono
        const telefonoRegex = /^[0-9]{9}$/;
        if (!clienteData.telefono || !telefonoRegex.test(clienteData.telefono)) {
            errores.push('El teléfono debe tener exactamente 9 dígitos');
        }
        
        // Validar radio de búsqueda
        if (clienteData.radioBusqueda !== null && clienteData.radioBusqueda !== undefined) {
            if (clienteData.radioBusqueda < 1 || clienteData.radioBusqueda > 50) {
                errores.push('El radio de búsqueda debe estar entre 1 y 50 km');
            }
        }
        
        // Validar presupuesto
        if (clienteData.presupuestoPromedio !== null && clienteData.presupuestoPromedio !== undefined) {
            if (clienteData.presupuestoPromedio < 0) {
                errores.push('El presupuesto no puede ser negativo');
            }
        }
        
        // Validar direcciones
        if (clienteData.direcciones && clienteData.direcciones.length > 3) {
            errores.push('Máximo 3 direcciones permitidas');
        }
        
        if (clienteData.direcciones) {
            clienteData.direcciones.forEach((dir, index) => {
                if (!dir.direccionCompleta || dir.direccionCompleta.trim().length < 5) {
                    errores.push(`La dirección ${index + 1} debe tener al menos 5 caracteres`);
                }
                if (!dir.distrito) {
                    errores.push(`La dirección ${index + 1} debe tener un distrito`);
                }
            });
        }
        
        return {
            valid: errores.length === 0,
            errores: errores
        };
    }
    
    /**
     * Convierte una imagen a Base64
     * @param {File} file - Archivo de imagen
     * @returns {Promise<string>} Imagen en Base64
     */
    async convertirImagenABase64(file) {
        return new Promise((resolve, reject) => {
            if (!file) {
                reject(new Error('No se proporcionó ningún archivo'));
                return;
            }
            
            if (!file.type.startsWith('image/')) {
                reject(new Error('El archivo no es una imagen válida'));
                return;
            }
            
            const maxSize = 5 * 1024 * 1024; // 5MB
            if (file.size > maxSize) {
                reject(new Error('La imagen no debe superar 5MB'));
                return;
            }
            
            const reader = new FileReader();
            
            reader.onload = (e) => {
                resolve(e.target.result);
            };
            
            reader.onerror = (error) => {
                reject(new Error('Error al leer el archivo'));
            };
            
            reader.readAsDataURL(file);
        });
    }
}

// Exportar para uso global
if (typeof module !== 'undefined' && module.exports) {
    module.exports = ClienteAPI;
}