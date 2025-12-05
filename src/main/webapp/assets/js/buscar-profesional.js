// ✅ SOLUCIÓN: Usar ruta relativa al context path actual
const API_BASE_URL = './api'; // Relativo a la ubicación actual del HTML

// Global variables
let currentPage = 1;
let totalPages = 1;
let currentCriteria = {};
const itemsPerPage = 12;

// Initialize page
document.addEventListener('DOMContentLoaded', function() {
    console.log('API Base URL:', API_BASE_URL);
    loadCategorias();
    initializeCategoriaChange();
    initializeEspecialidadChange();
    // COMENTADO: initializeRatingFilter();
    initializeAdvancedFilters();
    initializeForm();
});

// Load categorías
async function loadCategorias() {
    try {
        const response = await fetch(`${API_BASE_URL}/categorias`);
        const data = await response.json();

        if (data.success) {
            const select = document.getElementById('categoria');
            data.data.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.nombre;
                select.appendChild(option);
            });
            console.log('✅ Categorías cargadas:', data.data.length);
        }
    } catch (error) {
        console.error('❌ Error al cargar categorías:', error);
    }
}

// Initialize category change listener
function initializeCategoriaChange() {
    const categoriaSelect = document.getElementById('categoria');
    categoriaSelect.addEventListener('change', async function() {
        const categoriaId = this.value;
        await loadEspecialidadesPorCategoria(categoriaId);
    });
}

// Load especialidades filtradas por categoría
async function loadEspecialidadesPorCategoria(categoriaId) {
    const especialidadSelect = document.getElementById('especialidad');

    // Limpiar especialidades actuales excepto la opción inicial y "Otro"
    especialidadSelect.innerHTML = '<option value="">Seleccione una especialidad</option>';

    try {
        let url = `${API_BASE_URL}/especialidades-por-categoria`;
        if (categoriaId) {
            url += `?categoriaId=${categoriaId}`;
        }

        const response = await fetch(url);
        const data = await response.json();

        if (data.success && data.especialidades.length > 0) {
            data.especialidades.forEach(esp => {
                const option = document.createElement('option');
                option.value = esp;
                option.textContent = esp;
                especialidadSelect.appendChild(option);
            });
            console.log('✅ Especialidades cargadas:', data.especialidades.length);
        }

        // Siempre agregar la opción "Otro" al final
        const otroOption = document.createElement('option');
        otroOption.value = 'otro';
        otroOption.textContent = 'Otro';
        especialidadSelect.appendChild(otroOption);

    } catch (error) {
        console.error('❌ Error al cargar especialidades:', error);
    }
}

// Initialize especialidad change listener
function initializeEspecialidadChange() {
    const especialidadSelect = document.getElementById('especialidad');
    const otroGroup = document.getElementById('otroEspecialidadGroup');

    especialidadSelect.addEventListener('change', function() {
        if (this.value === 'otro') {
            otroGroup.style.display = 'block';
        } else {
            otroGroup.style.display = 'none';
            document.getElementById('otroEspecialidad').value = '';
        }
    });
}

// COMENTADO: Load distritos
/*
async function loadDistritos() {
    try {
        const response = await fetch(`${API_BASE_URL}/distritos`);
        const data = await response.json();

        if (data.success) {
            const select = document.getElementById('distrito');
            data.distritos.forEach(dist => {
                const option = document.createElement('option');
                option.value = dist;
                option.textContent = dist;
                select.appendChild(option);
            });
            console.log('✅ Distritos cargados:', data.distritos.length);
        }
    } catch (error) {
        console.error('❌ Error al cargar distritos:', error);
    }
}
*/

// Función para volver al dashboard
function volverDashboard() {
    window.location.href = 'dashboard.html';
}

// COMENTADO: Initialize rating filter
/*
function initializeRatingFilter() {
    const stars = document.querySelectorAll('.star');
    const ratingInput = document.getElementById('calificacionMin');

    stars.forEach(star => {
        star.addEventListener('click', function() {
            const rating = this.getAttribute('data-rating');
            ratingInput.value = rating;

            // Update visual state
            stars.forEach(s => {
                const starRating = s.getAttribute('data-rating');
                if (starRating <= rating) {
                    s.classList.add('active');
                } else {
                    s.classList.remove('active');
                }
            });
        });
    });
}
*/

// Initialize advanced filters toggle
function initializeAdvancedFilters() {
    const toggleBtn = document.getElementById('toggleFilters');
    const content = document.getElementById('advancedFiltersContent');
    const icon = document.getElementById('toggleIcon');

    toggleBtn.addEventListener('click', function() {
        content.classList.toggle('show');
        icon.textContent = content.classList.contains('show') ? '▲' : '▼';
    });
}

// Initialize form submission
function initializeForm() {
    const form = document.getElementById('searchForm');

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        currentPage = 1;
        searchProfessionals();
    });
}

// Main search function
async function searchProfessionals() {
    const form = document.getElementById('searchForm');
    const formData = new FormData(form);

    // Build query parameters
    const params = new URLSearchParams();

    // Categoría
    const categoriaId = formData.get('categoria');
    if (categoriaId) {
        params.append('categoriaId', categoriaId);
    }

    // Especialidad o texto libre "Otro"
    const especialidad = formData.get('especialidad');
    const otroEspecialidad = formData.get('otroEspecialidad');

    if (especialidad === 'otro' && otroEspecialidad) {
        // Búsqueda por texto libre
        params.append('especialidadTexto', otroEspecialidad);
    } else if (especialidad && especialidad !== 'otro') {
        // Búsqueda por especialidad seleccionada
        params.append('especialidad', especialidad);
    }

    // COMENTADO: Filtros de distrito y calificación
    /*
    if (formData.get('distrito')) {
        params.append('distrito', formData.get('distrito'));
    }

    const calificacionMin = document.getElementById('calificacionMin').value;
    if (calificacionMin && calificacionMin > 0) {
        params.append('calificacionMin', calificacionMin);
    }
    */

    if (formData.get('tarifaMax')) {
        params.append('tarifaMax', formData.get('tarifaMax'));
    }

    if (formData.get('disponible')) {
        params.append('disponible', formData.get('disponible'));
    }

    if (formData.get('ordenarPor')) {
        params.append('ordenarPor', formData.get('ordenarPor'));
    }

    params.append('pagina', currentPage);
    params.append('elementosPorPagina', itemsPerPage);

    // Save current criteria
    currentCriteria = Object.fromEntries(params);

    console.log('🔍 Buscando con parámetros:', currentCriteria);

    // Show loading state
    showLoading();

    try {
        const url = `${API_BASE_URL}/buscar-profesionales?${params.toString()}`;
        console.log('📡 Fetch URL:', url);

        const response = await fetch(url);
        console.log('📥 Response status:', response.status);

        const data = await response.json();
        console.log('📊 Data received:', data);

        if (data.success) {
            displayResults(data);
        } else {
            showAlert(data.error || 'Error al buscar profesionales', 'error');
            showEmptyState();
        }
    } catch (error) {
        console.error('❌ Error completo:', error);
        showAlert('Error de conexión. Por favor, intenta nuevamente.', 'error');
        showEmptyState();
    }
}

// Display search results
function displayResults(data) {
    console.log('✅ Mostrando resultados:', data.profesionales.length);
    hideLoading();
    hideInitialState();

    const grid = document.getElementById('professionalsGrid');
    const resultsHeader = document.getElementById('resultsHeader');
    const emptyState = document.getElementById('emptyState');

    if (data.profesionales.length === 0) {
        grid.innerHTML = '';
        resultsHeader.style.display = 'none';
        emptyState.style.display = 'block';
        document.getElementById('pagination').style.display = 'none';
        return;
    }

    // Update results header
    document.getElementById('totalResults').textContent = data.total;
    document.getElementById('criteriaInfo').textContent =
        data.criterios !== 'Sin filtros' ? ` - ${data.criterios}` : '';
    resultsHeader.style.display = 'flex';
    emptyState.style.display = 'none';

    // Render professional cards
    grid.innerHTML = '';
    data.profesionales.forEach(prof => {
        grid.appendChild(createProfessionalCard(prof));
    });

    // Update pagination
    totalPages = data.totalPaginas;
    renderPagination(data.pagina, data.totalPaginas);
}


// Create professional card
function createProfessionalCard(prof) {
    const card = document.createElement('div');
    card.className = 'professional-card';
    card.onclick = () => viewProfile(prof.id, prof.especialidad);

    // Build rating stars
    const fullStars = Math.floor(prof.calificacionPromedio || 0);
    const stars = '★'.repeat(fullStars) + '☆'.repeat(5 - fullStars);

    // Build badges
    let badges = '';
    if (prof.verificado) {
        badges += '<span class="badge badge-verified">✓ Verificado</span>';
    }
    if (prof.disponible) {
        badges += '<span class="badge badge-available">Disponible</span>';
    } else {
        badges += '<span class="badge badge-unavailable">No disponible</span>';
    }

    // Build skills tags
    let skillsTags = '';
    if (prof.habilidadesDestacadas && prof.habilidadesDestacadas.length > 0) {
        skillsTags = prof.habilidadesDestacadas.map(skill =>
            `<span class="tag">${skill}</span>`
        ).join('');
    }

    card.innerHTML = `
        <div class="card-header">
            <div class="card-photo">
                ${prof.fotoPerfil ?
                    `<img src="${prof.fotoPerfil}" alt="${prof.nombreCompleto}">` :
                    '👤'}
            </div>
            <div class="card-info">
                <div class="card-name">
                    ${prof.nombreCompleto}
                    ${badges}
                </div>
                <div class="card-specialty">${prof.especialidad}</div>
                <div class="card-location">
                    📍 ${prof.distrito}
                    ${prof.radioServicio ? ` (${prof.radioServicio} km)` : ''}
                </div>
            </div>
        </div>

        <div class="card-description">
            ${prof.descripcionCorta || 'Sin descripción disponible'}
        </div>

        ${skillsTags ? `<div class="card-tags">${skillsTags}</div>` : ''}

        <div class="card-footer">
            <div class="card-rating">
                <span class="rating-stars">${stars}</span>
                <span>${prof.calificacionPromedio ? prof.calificacionPromedio.toFixed(1) : 'N/A'}</span>
                <span>(${prof.totalResenas || 0})</span>
            </div>
            <div class="card-price">
                ${prof.tarifaHora ? `S/ ${prof.tarifaHora.toFixed(2)}/h` : 'A consultar'}
            </div>
        </div>

        <button class="btn-solicitar" onclick="solicitarServicio(event, ${prof.id}, ${prof.especialidadId})">
        	📝 Solicitar Servicio
    	</button>
    `;

    return card;
}

// Render pagination
function renderPagination(currentPage, totalPages) {
    const pagination = document.getElementById('pagination');

    if (totalPages <= 1) {
        pagination.style.display = 'none';
        return;
    }

    pagination.style.display = 'flex';
    pagination.innerHTML = '';

    // Previous button
    const prevBtn = document.createElement('button');
    prevBtn.className = 'pagination-btn';
    prevBtn.textContent = '← Anterior';
    prevBtn.disabled = currentPage === 1;
    prevBtn.onclick = () => goToPage(currentPage - 1);
    pagination.appendChild(prevBtn);

    // Page numbers
    const maxPageButtons = 5;
    let startPage = Math.max(1, currentPage - Math.floor(maxPageButtons / 2));
    let endPage = Math.min(totalPages, startPage + maxPageButtons - 1);

    if (endPage - startPage < maxPageButtons - 1) {
        startPage = Math.max(1, endPage - maxPageButtons + 1);
    }

    if (startPage > 1) {
        const firstBtn = document.createElement('button');
        firstBtn.className = 'pagination-btn';
        firstBtn.textContent = '1';
        firstBtn.onclick = () => goToPage(1);
        pagination.appendChild(firstBtn);

        if (startPage > 2) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'pagination-info';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }
    }

    for (let i = startPage; i <= endPage; i++) {
        const pageBtn = document.createElement('button');
        pageBtn.className = 'pagination-btn';
        if (i === currentPage) {
            pageBtn.classList.add('active');
        }
        pageBtn.textContent = i;
        pageBtn.onclick = () => goToPage(i);
        pagination.appendChild(pageBtn);
    }

    if (endPage < totalPages) {
        if (endPage < totalPages - 1) {
            const ellipsis = document.createElement('span');
            ellipsis.className = 'pagination-info';
            ellipsis.textContent = '...';
            pagination.appendChild(ellipsis);
        }

        const lastBtn = document.createElement('button');
        lastBtn.className = 'pagination-btn';
        lastBtn.textContent = totalPages;
        lastBtn.onclick = () => goToPage(totalPages);
        pagination.appendChild(lastBtn);
    }

    // Next button
    const nextBtn = document.createElement('button');
    nextBtn.className = 'pagination-btn';
    nextBtn.textContent = 'Siguiente →';
    nextBtn.disabled = currentPage === totalPages;
    nextBtn.onclick = () => goToPage(currentPage + 1);
    pagination.appendChild(nextBtn);
}

// Go to specific page
function goToPage(page) {
    if (page >= 1 && page <= totalPages && page !== currentPage) {
        currentPage = page;
        searchProfessionals();
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
}

// View professional profile
function viewProfile(id, especialidad) {
    // Construir URL con profesionalId y especialidad
    let url = `solicitud-servicio.html?profesionalId=${id}`;
    if (especialidad) {
        url += `&especialidad=${encodeURIComponent(especialidad)}`;
    }
    window.location.href = url;
}

// Nueva función para solicitar servicio
function solicitarServicio(event, profesionalId, especialidadId) {
    // Evitar que se dispare el onclick de la tarjeta
    event.stopPropagation();

    // Verificar si el usuario está autenticado
    const userData = JSON.parse(localStorage.getItem('userData') || '{}');

    if (!userData.id) {
        showAlert('Debes iniciar sesión para solicitar un servicio', 'error');
        setTimeout(() => {
            window.location.href = 'login.html';
        }, 2000);
        return;
    }

    // Redirigir a la página de solicitud con el ID del profesional y el ID de la especialidad
    let url = `solicitud-servicio.html?profesionalId=${profesionalId}`;
    if (especialidadId) {
        url += `&especialidadId=${especialidadId}`;
    }
    window.location.href = url;
}

// Reset filters
function resetFilters() {
    document.getElementById('searchForm').reset();
    // COMENTADO: document.getElementById('calificacionMin').value = '0';
    // COMENTADO: document.querySelectorAll('.star').forEach(s => s.classList.remove('active'));
    document.getElementById('otroEspecialidadGroup').style.display = 'none';
    document.getElementById('otroEspecialidad').value = '';
    currentPage = 1;
    showInitialState();
}

// UI state functions
function showLoading() {
    document.getElementById('loadingState').style.display = 'block';
    document.getElementById('initialState').style.display = 'none';
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('professionalsGrid').innerHTML = '';
    document.getElementById('resultsHeader').style.display = 'none';
    document.getElementById('pagination').style.display = 'none';
}

function hideLoading() {
    document.getElementById('loadingState').style.display = 'none';
}

function showInitialState() {
    hideLoading();
    document.getElementById('initialState').style.display = 'block';
    document.getElementById('emptyState').style.display = 'none';
    document.getElementById('professionalsGrid').innerHTML = '';
    document.getElementById('resultsHeader').style.display = 'none';
    document.getElementById('pagination').style.display = 'none';
}

function hideInitialState() {
    document.getElementById('initialState').style.display = 'none';
}

function showEmptyState() {
    hideLoading();
    hideInitialState();
    document.getElementById('emptyState').style.display = 'block';
    document.getElementById('professionalsGrid').innerHTML = '';
    document.getElementById('resultsHeader').style.display = 'none';
    document.getElementById('pagination').style.display = 'none';
}

// Alert system
function showAlert(message, type) {
    const container = document.getElementById('alertContainer');
    const alert = document.createElement('div');
    alert.className = `alert alert-${type} show`;
    alert.textContent = message;

    container.innerHTML = '';
    container.appendChild(alert);

    setTimeout(() => {
        alert.classList.remove('show');
        setTimeout(() => alert.remove(), 300);
    }, 5000);
}
