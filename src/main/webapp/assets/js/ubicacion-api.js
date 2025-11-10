/**
 * API para gestión de ubicaciones geográficas (Departamentos, Provincias, Distritos)
 * Integración con UbicacionServlet del backend
 */

class UbicacionAPI {
    constructor() {
        this.baseURL = '/ContactoProfesionalesWeb/api/ubicacion';
    }

    /**
     * Obtiene todos los departamentos del Perú
     * @returns {Promise<Array>} Lista de departamentos
     */
    async getDepartamentos() {
        try {
            const response = await fetch(`${this.baseURL}/departamentos`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al obtener departamentos');
            }
        } catch (error) {
            console.error('Error en getDepartamentos:', error);
            throw error;
        }
    }

    /**
     * Obtiene las provincias de un departamento
     * @param {number} departamentoId - ID del departamento
     * @returns {Promise<Array>} Lista de provincias
     */
    async getProvincias(departamentoId) {
        if (!departamentoId) {
            throw new Error('departamentoId es requerido');
        }

        try {
            const response = await fetch(`${this.baseURL}/provincias?departamentoId=${departamentoId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al obtener provincias');
            }
        } catch (error) {
            console.error('Error en getProvincias:', error);
            throw error;
        }
    }

    /**
     * Obtiene los distritos de una provincia
     * @param {number} provinciaId - ID de la provincia
     * @returns {Promise<Array>} Lista de distritos
     */
    async getDistritos(provinciaId) {
        if (!provinciaId) {
            throw new Error('provinciaId es requerido');
        }

        try {
            const response = await fetch(`${this.baseURL}/distritos?provinciaId=${provinciaId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al obtener distritos');
            }
        } catch (error) {
            console.error('Error en getDistritos:', error);
            throw error;
        }
    }

    /**
     * Busca distritos por nombre
     * @param {string} nombre - Nombre del distrito a buscar
     * @returns {Promise<Array>} Lista de distritos que coinciden
     */
    async buscarDistritos(nombre) {
        if (!nombre || nombre.trim().length < 3) {
            throw new Error('El nombre debe tener al menos 3 caracteres');
        }

        try {
            const response = await fetch(`${this.baseURL}/distritos/buscar?nombre=${encodeURIComponent(nombre)}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al buscar distritos');
            }
        } catch (error) {
            console.error('Error en buscarDistritos:', error);
            throw error;
        }
    }

    /**
     * Popula un select de departamentos
     * @param {HTMLSelectElement} selectElement - Elemento select a popular
     * @param {string} defaultOption - Texto de la opción por defecto
     */
    async popularDepartamentos(selectElement, defaultOption = 'Selecciona un departamento') {
        try {
            const departamentos = await this.getDepartamentos();

            // Limpiar select
            selectElement.innerHTML = `<option value="">${defaultOption}</option>`;

            // Agregar opciones
            departamentos.forEach(depto => {
                const option = document.createElement('option');
                option.value = depto.id;
                option.textContent = depto.nombre;
                option.dataset.codigo = depto.codigo;
                selectElement.appendChild(option);
            });

            return departamentos;
        } catch (error) {
            console.error('Error al popular departamentos:', error);
            selectElement.innerHTML = `<option value="">Error al cargar departamentos</option>`;
            throw error;
        }
    }

    /**
     * Popula un select de provincias según el departamento seleccionado
     * @param {HTMLSelectElement} selectElement - Elemento select a popular
     * @param {number} departamentoId - ID del departamento
     * @param {string} defaultOption - Texto de la opción por defecto
     */
    async popularProvincias(selectElement, departamentoId, defaultOption = 'Selecciona una provincia') {
        try {
            selectElement.innerHTML = `<option value="">${defaultOption}</option>`;
            selectElement.disabled = true;

            if (!departamentoId) return [];

            const provincias = await this.getProvincias(departamentoId);

            provincias.forEach(prov => {
                const option = document.createElement('option');
                option.value = prov.id;
                option.textContent = prov.nombre;
                option.dataset.codigo = prov.codigo;
                selectElement.appendChild(option);
            });

            selectElement.disabled = false;
            return provincias;
        } catch (error) {
            console.error('Error al popular provincias:', error);
            selectElement.innerHTML = `<option value="">Error al cargar provincias</option>`;
            throw error;
        }
    }

    /**
     * Popula un select de distritos según la provincia seleccionada
     * @param {HTMLSelectElement} selectElement - Elemento select a popular
     * @param {number} provinciaId - ID de la provincia
     * @param {string} defaultOption - Texto de la opción por defecto
     */
    async popularDistritos(selectElement, provinciaId, defaultOption = 'Selecciona un distrito') {
        try {
            selectElement.innerHTML = `<option value="">${defaultOption}</option>`;
            selectElement.disabled = true;

            if (!provinciaId) return [];

            const distritos = await this.getDistritos(provinciaId);

            distritos.forEach(dist => {
                const option = document.createElement('option');
                option.value = dist.id;
                option.textContent = dist.nombre;
                option.dataset.codigo = dist.codigo;
                selectElement.appendChild(option);
            });

            selectElement.disabled = false;
            return distritos;
        } catch (error) {
            console.error('Error al popular distritos:', error);
            selectElement.innerHTML = `<option value="">Error al cargar distritos</option>`;
            throw error;
        }
    }

    /**
     * Configura la cascada de selects (departamento → provincia → distrito)
     * @param {object} selects - Objeto con los selectores {departamento, provincia, distrito}
     */
    configurarCascada(selects) {
        const { departamento, provincia, distrito } = selects;

        if (!departamento || !provincia || !distrito) {
            console.error('Se requieren los 3 selectores: departamento, provincia, distrito');
            return;
        }

        // Cargar departamentos al inicio
        this.popularDepartamentos(departamento);

        // Cuando cambia el departamento, cargar provincias
        departamento.addEventListener('change', async (e) => {
            const departamentoId = e.target.value;
            await this.popularProvincias(provincia, departamentoId);

            // Resetear distrito
            distrito.innerHTML = '<option value="">Primero selecciona una provincia</option>';
            distrito.disabled = true;
        });

        // Cuando cambia la provincia, cargar distritos
        provincia.addEventListener('change', async (e) => {
            const provinciaId = e.target.value;
            await this.popularDistritos(distrito, provinciaId);
        });
    }
}

// Exportar como singleton
const ubicacionAPI = new UbicacionAPI();
