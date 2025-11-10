/**
 * API para gestión de categorías de servicios
 * Integración con CategoriaServlet del backend
 */

class CategoriaAPI {
    constructor() {
        this.baseURL = '/ContactoProfesionalesWeb/api';
    }

    /**
     * Obtiene todas las categorías de servicios activas
     * @returns {Promise<Array>} Lista de categorías
     */
    async listar() {
        try {
            const response = await fetch(`${this.baseURL}/categorias`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                }
            });

            const data = await response.json();

            if (response.ok && data.success) {
                return data.data;
            } else {
                throw new Error(data.error?.mensaje || 'Error al obtener categorías');
            }
        } catch (error) {
            console.error('Error en listar categorías:', error);
            throw error;
        }
    }

    /**
     * Busca una categoría por ID
     * @param {number} categoriaId - ID de la categoría
     * @returns {Promise<object>} Categoría encontrada
     */
    async obtenerPorId(categoriaId) {
        if (!categoriaId) {
            throw new Error('categoriaId es requerido');
        }

        try {
            const categorias = await this.listar();
            const categoria = categorias.find(cat => cat.id === categoriaId);

            if (!categoria) {
                throw new Error(`Categoría con ID ${categoriaId} no encontrada`);
            }

            return categoria;
        } catch (error) {
            console.error('Error en obtenerPorId:', error);
            throw error;
        }
    }

    /**
     * Popula un select con las categorías disponibles
     * @param {HTMLSelectElement} selectElement - Elemento select a popular
     * @param {string} defaultOption - Texto de la opción por defecto
     * @returns {Promise<Array>} Lista de categorías cargadas
     */
    async popularCategorias(selectElement, defaultOption = 'Selecciona una categoría') {
        try {
            const categorias = await this.listar();

            // Limpiar select
            selectElement.innerHTML = `<option value="">${defaultOption}</option>`;

            // Agregar opciones
            categorias.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = `${cat.icono || '🔧'} ${cat.nombre}`;
                option.dataset.descripcion = cat.descripcion || '';
                option.dataset.activa = cat.activa;
                selectElement.appendChild(option);
            });

            return categorias;
        } catch (error) {
            console.error('Error al popular categorías:', error);
            selectElement.innerHTML = `<option value="">Error al cargar categorías</option>`;
            throw error;
        }
    }

    /**
     * Renderiza categorías como cards en un contenedor
     * @param {HTMLElement} container - Contenedor donde renderizar
     * @param {Array} categorias - Array de categorías (opcional, si no se pasa las obtiene)
     * @param {function} onSelect - Callback cuando se selecciona una categoría
     */
    async renderizarCards(container, categorias = null, onSelect = null) {
        try {
            if (!categorias) {
                categorias = await this.listar();
            }

            container.innerHTML = '';

            if (categorias.length === 0) {
                container.innerHTML = '<p class="text-muted">No hay categorías disponibles.</p>';
                return;
            }

            categorias.forEach(cat => {
                const card = document.createElement('div');
                card.className = 'categoria-card';
                card.dataset.categoriaId = cat.id;
                card.innerHTML = `
                    <div class="categoria-icono">${cat.icono || '🔧'}</div>
                    <h3 class="categoria-nombre">${cat.nombre}</h3>
                    ${cat.descripcion ? `<p class="categoria-descripcion">${cat.descripcion}</p>` : ''}
                    ${!cat.activa ? '<span class="badge badge-secondary">Inactiva</span>' : ''}
                `;

                // Event listener para selección
                if (onSelect && cat.activa) {
                    card.style.cursor = 'pointer';
                    card.addEventListener('click', () => onSelect(cat));
                    card.classList.add('selectable');
                } else if (!cat.activa) {
                    card.classList.add('disabled');
                }

                container.appendChild(card);
            });
        } catch (error) {
            console.error('Error al renderizar categorías:', error);
            container.innerHTML = '<p class="text-error">Error al cargar categorías</p>';
        }
    }

    /**
     * Filtra categorías por texto de búsqueda
     * @param {Array} categorias - Array de categorías a filtrar
     * @param {string} searchText - Texto de búsqueda
     * @returns {Array} Categorías filtradas
     */
    filtrar(categorias, searchText) {
        if (!searchText || searchText.trim().length === 0) {
            return categorias;
        }

        const searchLower = searchText.toLowerCase().trim();

        return categorias.filter(cat =>
            cat.nombre.toLowerCase().includes(searchLower) ||
            (cat.descripcion && cat.descripcion.toLowerCase().includes(searchLower))
        );
    }

    /**
     * Ordena categorías por nombre
     * @param {Array} categorias - Array de categorías a ordenar
     * @param {string} orden - 'asc' o 'desc'
     * @returns {Array} Categorías ordenadas
     */
    ordenar(categorias, orden = 'asc') {
        return [...categorias].sort((a, b) => {
            const comparison = a.nombre.localeCompare(b.nombre, 'es');
            return orden === 'asc' ? comparison : -comparison;
        });
    }

    /**
     * Obtiene el nombre de una categoría por su ID
     * @param {number} categoriaId - ID de la categoría
     * @param {Array} categorias - Array de categorías (opcional)
     * @returns {Promise<string>} Nombre de la categoría
     */
    async obtenerNombre(categoriaId, categorias = null) {
        try {
            if (!categorias) {
                categorias = await this.listar();
            }

            const categoria = categorias.find(cat => cat.id === categoriaId);
            return categoria ? categoria.nombre : 'Categoría no encontrada';
        } catch (error) {
            console.error('Error al obtener nombre de categoría:', error);
            return 'Error';
        }
    }
}

// Exportar como singleton
const categoriaAPI = new CategoriaAPI();
