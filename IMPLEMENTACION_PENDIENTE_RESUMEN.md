# 📋 Resumen de Implementación Pendiente - Perfil Profesional

**Fecha:** 2025-11-15
**Estado:** Guía de Implementación

---

## ✅ Ya Completado

1. ✅ Migración SQL V006 (tablas creadas)
2. ✅ Models (5 clases Java)
3. ✅ CertificacionesProfesionalDAO + Impl (completo)
4. ✅ ProyectosPortafolioDAO (interface)

---

## ⏳ Pendiente por Implementar

### BACKEND (Java)

#### 1. DAOs Restantes (4)

**A. ProyectosPortafolioDAOImpl.java**

```java
package com.contactoprofesionales.dao.portafolio;

// Estructura similar a CertificacionesProfesionalDAOImpl
// Métodos principales:
// - listarPorProfesional() - con JOIN a categorias_profesionales
// - buscarPorId() - incluye carga de imágenes relacionadas
// - guardar() - valida límite de 20 proyectos antes de insertar
// - actualizar()
// - eliminar() - soft delete
// - contarActivosPorProfesional()
// - actualizarCalificacion() - usado solo por módulo de valoraciones
// - mapearProyecto() - incluye nombre de categoría
```

**Query SQL clave:**
```sql
SELECT p.*, c.nombre as categoria_nombre
FROM proyectos_portafolio p
LEFT JOIN categorias_profesionales c ON p.categoria_id = c.id
WHERE p.profesional_id = ? AND p.activo = TRUE
ORDER BY p.fecha_realizacion DESC
```

---

**B. ImagenesProyectoDAO.java + Impl**

```java
package com.contactoprofesionales.dao.portafolio;

// Interface:
public interface ImagenesProyectoDAO {
    List<ImagenProyecto> listarPorProyecto(Integer proyectoId);
    Integer guardar(ImagenProyecto imagen); // Valida máximo 5
    boolean eliminar(Integer id);
    int contarPorProyecto(Integer proyectoId);
}

// Implementación similar a otros DAOs
// IMPORTANTE: Validar que no se excedan 5 imágenes por proyecto
```

---

**C. AntecedentesProfesionalDAO.java + Impl**

```java
package com.contactoprofesionales.dao.antecedentes;

public interface AntecedentesProfesionalDAO {
    List<AntecedenteProfesional> listarPorProfesional(Integer profesionalId);
    Optional<AntecedenteProfesional> buscarPorTipo(Integer profesionalId, TipoAntecedente tipo);
    Integer guardar(AntecedenteProfesional antecedente);
    boolean actualizar(AntecedenteProfesional antecedente);
    boolean verificar(Integer id); // Marca como verificado (admin)
    boolean eliminar(Integer id);
}

// SQL clave para buscar por tipo:
SELECT * FROM antecedentes_profesional
WHERE profesional_id = ? AND tipo_antecedente = ? AND activo = TRUE
```

---

**D. RedesSocialesProfesionalDAO.java + Impl**

```java
package com.contactoprofesionales.dao.redes;

public interface RedesSocialesProfesionalDAO {
    List<RedSocialProfesional> listarPorProfesional(Integer profesionalId);
    Integer guardar(RedSocialProfesional red);
    boolean actualizar(RedSocialProfesional red);
    boolean eliminar(Integer id); // Soft delete
    // Guardar múltiples redes en una transacción
    boolean guardarMultiples(Integer profesionalId, List<RedSocialProfesional> redes);
}
```

---

#### 2. DTO Consolidado

**PerfilProfesionalCompletoDTO.java**

```java
package com.contactoprofesionales.dto;

/**
 * DTO que consolida TODA la información del perfil profesional.
 * Usado para responder al GET /api/profesional/perfil
 */
public class PerfilProfesionalCompletoDTO {
    // Datos básicos
    private Integer id;
    private Integer usuarioId;
    private String biografiaProfesional;
    private Integer aniosExperiencia;
    private String[] idiomas;
    private String licenciasProfesionales;
    private Boolean seguroResponsabilidad;
    private String[] metodosPago;
    private String politicaCancelacion;
    private String fotoPerfil;
    private String fotoPortada;
    private BigDecimal tarifaHora;
    private BigDecimal calificacionPromedio;
    private Integer totalResenas;
    private Boolean verificado;
    private Boolean disponible;
    private BigDecimal puntuacionPlataforma; // Calculada

    // Listas relacionadas
    private List<EspecialidadProfesional> especialidades;
    private List<CertificacionProfesional> certificaciones;
    private List<ProyectoPortafolio> proyectos;
    private List<AntecedenteProfesional> antecedentes;
    private List<RedSocialProfesional> redesSociales;
    private AreaServicio areaServicio;
    private DisponibilidadHoraria disponibilidad;

    // Getters y Setters...
}
```

---

#### 3. Servlets (6)

**A. CertificacionesProfesionalServlet.java**

```java
@WebServlet("/api/profesional/certificaciones")
public class CertificacionesProfesionalServlet extends HttpServlet {

    private CertificacionesProfesionalDAO dao;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // Obtener profesionalId del token JWT
        // dao.listarPorProfesional(profesionalId)
        // Retornar JSON
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // Parsear JSON a CertificacionProfesional
        // dao.guardar(certificacion)
        // Retornar JSON con ID creado
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        // Parsear JSON
        // dao.actualizar(certificacion)
        // Retornar JSON
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        // Obtener ID del path
        // dao.eliminar(id)
        // Retornar JSON
    }
}
```

---

**B. ProyectosPortafolioServlet.java**

```java
@WebServlet("/api/profesional/proyectos")
public class ProyectosPortafolioServlet extends HttpServlet {

    private ProyectosPortafolioDAO proyectosDAO;
    private ImagenesProyectoDAO imagenesDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // Si viene /:id → buscarPorId() con imágenes
        // Si no → listarPorProfesional()
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // 1. Validar que no tenga 20 proyectos ya
        // 2. Guardar proyecto
        // 3. Si vienen imágenes, guardarlas (máx 5)
        // 4. Retornar proyecto completo con imágenes
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        // Actualizar proyecto
        // NO permite modificar calificación (solo clientes pueden)
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        // Soft delete del proyecto
        // Las imágenes se eliminan en cascada (ON DELETE CASCADE)
    }
}
```

---

**C. ImagenesProyectoServlet.java**

```java
@WebServlet("/api/profesional/proyectos/*/imagenes")
public class ImagenesProyectoServlet extends HttpServlet {

    private ImagenesProyectoDAO dao;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // Obtener proyectoId del path
        // dao.listarPorProyecto(proyectoId)
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // Manejo de multipart/form-data (upload de imagen)
        // 1. Validar que no tenga ya 5 imágenes
        // 2. Guardar archivo en servidor/cloud
        // 3. dao.guardar(imagen) con URL
        // 4. Retornar imagen guardada
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        // Eliminar imagen física del servidor
        // dao.eliminar(id)
    }
}
```

---

**D. AntecedentesProfesionalServlet.java**

```java
@WebServlet("/api/profesional/antecedentes")
public class AntecedentesProfesionalServlet extends HttpServlet {

    private AntecedentesProfesionalDAO dao;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // dao.listarPorProfesional(profesionalId)
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // Manejo de multipart/form-data (PDF)
        // 1. Validar que no tenga ya ese tipo de antecedente activo
        // 2. Guardar PDF en servidor
        // 3. dao.guardar(antecedente) con URL
        // 4. Marcar como verificado=false (admin debe verificar)
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        // Permitir reemplazar documento
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) {
        // Soft delete
    }
}
```

---

**E. RedesSocialesProfesionalServlet.java**

```java
@WebServlet("/api/profesional/redes-sociales")
public class RedesSocialesProfesionalServlet extends HttpServlet {

    private RedesSocialesProfesionalDAO dao;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // dao.listarPorProfesional(profesionalId)
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) {
        // Parsear JSON con array de redes sociales
        // dao.guardarMultiples(profesionalId, redes)
        // Esto desactiva las que no vienen y crea/actualiza las que sí
    }
}
```

---

**F. PerfilProfesionalServlet.java (ACTUALIZADO)**

```java
@WebServlet("/api/profesional/perfil")
public class PerfilProfesionalServlet extends HttpServlet {

    // Inyectar TODOS los DAOs necesarios
    private ProfesionalDAO profesionalDAO;
    private CertificacionesProfesionalDAO certificacionesDAO;
    private ProyectosPortafolioDAO proyectosDAO;
    private AntecedentesProfesionalDAO antecedentesDAO;
    private RedesSocialesProfesionalDAO redesDAO;
    private ServiciosProfesionalDAO serviciosDAO;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // 1. Obtener datos básicos del profesional
        // 2. Cargar certificaciones
        // 3. Cargar proyectos (con imágenes)
        // 4. Cargar antecedentes
        // 5. Cargar redes sociales
        // 6. Cargar especialidades, área, disponibilidad
        // 7. Calcular puntuación con función SQL
        // 8. Construir PerfilProfesionalCompletoDTO
        // 9. Retornar JSON
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) {
        // Actualizar solo datos básicos:
        // - biografiaProfesional
        // - aniosExperiencia
        // - idiomas
        // - licenciasProfesionales
        // - seguroResponsabilidad
        // - metodosPago
        // - politicaCancelacion
        // - fotoPerfil, fotoPortada, tarifaHora
    }
}
```

---

### FRONTEND (HTML + JavaScript)

#### 1. Refactorizar profesional.html

**Estructura HTML Completa:**

```html
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Perfil Profesional</title>
    <link rel="stylesheet" href="assets/css/profesional.css">
</head>
<body>
    <div class="container">
        <h1>Mi Perfil Profesional</h1>

        <!-- ========================================
             SECCIÓN 1: DATOS BÁSICOS
             ======================================== -->
        <section class="datos-basicos">
            <h2>📋 Información General</h2>

            <div class="form-group">
                <label>Foto de Perfil:</label>
                <input type="file" id="foto-perfil" accept="image/*">
                <img id="preview-perfil" src="" alt="Preview">
            </div>

            <div class="form-group">
                <label>Foto de Portada:</label>
                <input type="file" id="foto-portada" accept="image/*">
                <img id="preview-portada" src="" alt="Preview">
            </div>

            <div class="form-group">
                <label>Biografía Profesional: *</label>
                <textarea id="biografia-profesional"
                          rows="5"
                          maxlength="1000"
                          placeholder="Describe tu experiencia, habilidades y lo que te hace único como profesional..."></textarea>
                <span class="char-counter">0/1000</span>
            </div>

            <div class="form-group">
                <label>Años de Experiencia: *</label>
                <input type="number" id="anios-experiencia" min="0" max="50" value="0">
            </div>

            <div class="form-group">
                <label>Tarifa por Hora (S/): *</label>
                <input type="number" id="tarifa-hora" min="0" step="0.01">
            </div>
        </section>

        <!-- ========================================
             SECCIÓN 2: ESPECIALIDADES
             (Ya implementado en servicios-profesional.html)
             ======================================== -->

        <!-- ========================================
             SECCIÓN 3: CERTIFICACIONES
             ======================================== -->
        <section class="certificaciones-section">
            <h2>🎓 Certificaciones y Estudios</h2>
            <button id="btn-agregar-certificacion" class="btn-primary">
                ➕ Agregar Certificación
            </button>

            <div class="certificaciones-lista" id="certificaciones-lista">
                <!-- Se llenan dinámicamente -->
            </div>
        </section>

        <!-- ========================================
             SECCIÓN 4: PORTAFOLIO DE PROYECTOS (Máx. 20)
             ======================================== -->
        <section class="portafolio-section">
            <h2>💼 Portafolio de Proyectos</h2>
            <p class="info">Agrega hasta 20 proyectos realizados. Cada proyecto puede tener hasta 5 imágenes.</p>

            <div class="contador-proyectos">
                Proyectos: <span id="total-proyectos">0</span> / 20
            </div>

            <button id="btn-agregar-proyecto" class="btn-primary">
                ➕ Agregar Proyecto
            </button>

            <div class="proyectos-grid" id="proyectos-grid">
                <!-- Tarjetas de proyectos dinámicas -->
            </div>
        </section>

        <!-- ========================================
             SECCIÓN 5: REDES SOCIALES
             ======================================== -->
        <section class="redes-sociales-section">
            <h2>🌐 Redes Sociales</h2>

            <div class="red-social-item">
                <label>Facebook:</label>
                <input type="url" id="red-facebook" placeholder="https://facebook.com/tu-perfil">
            </div>

            <div class="red-social-item">
                <label>Instagram:</label>
                <input type="url" id="red-instagram" placeholder="https://instagram.com/tu-perfil">
            </div>

            <div class="red-social-item">
                <label>LinkedIn:</label>
                <input type="url" id="red-linkedin" placeholder="https://linkedin.com/in/tu-perfil">
            </div>

            <div class="red-social-item">
                <label>YouTube:</label>
                <input type="url" id="red-youtube" placeholder="https://youtube.com/@tu-canal">
            </div>

            <div class="red-social-item">
                <label>TikTok:</label>
                <input type="url" id="red-tiktok" placeholder="https://tiktok.com/@tu-usuario">
            </div>

            <div class="red-social-item">
                <label>Sitio Web:</label>
                <input type="url" id="red-website" placeholder="https://tu-sitio.com">
            </div>
        </section>

        <!-- ========================================
             SECCIÓN 6: ANTECEDENTES (Opcional)
             ======================================== -->
        <section class="antecedentes-section">
            <h2>🛡️ Antecedentes</h2>
            <p class="info-destacada">
                ⭐ Sube tus certificados de antecedentes para mejorar tu puntuación y ganar la confianza de tus clientes.
            </p>

            <div class="antecedente-item">
                <label>Antecedentes Policiales:</label>
                <input type="file" id="antecedente-policial" accept=".pdf">
                <span class="status" id="status-policial"></span>
            </div>

            <div class="antecedente-item">
                <label>Antecedentes Penales:</label>
                <input type="file" id="antecedente-penal" accept=".pdf">
                <span class="status" id="status-penal"></span>
            </div>

            <div class="antecedente-item">
                <label>Antecedentes Judiciales:</label>
                <input type="file" id="antecedente-judicial" accept=".pdf">
                <span class="status" id="status-judicial"></span>
            </div>
        </section>

        <!-- ========================================
             SECCIÓN 7: INFORMACIÓN ADICIONAL
             ======================================== -->
        <section class="info-adicional-section">
            <h2>ℹ️ Información Adicional</h2>

            <div class="form-group">
                <label>Idiomas que manejo:</label>
                <div class="checkbox-group">
                    <label><input type="checkbox" name="idioma" value="español"> Español</label>
                    <label><input type="checkbox" name="idioma" value="ingles"> Inglés</label>
                    <label><input type="checkbox" name="idioma" value="portugues"> Portugués</label>
                    <label><input type="checkbox" name="idioma" value="quechua"> Quechua</label>
                    <label><input type="checkbox" name="idioma" value="otro"> Otro</label>
                </div>
            </div>

            <div class="form-group">
                <label>Licencias Profesionales:</label>
                <textarea id="licencias-profesionales"
                          placeholder="Ej: Licencia de electricista N° 12345, Certificado de gasfitero N° 67890"></textarea>
            </div>

            <div class="form-group">
                <label class="checkbox-label">
                    <input type="checkbox" id="seguro-responsabilidad">
                    Cuento con seguro de responsabilidad civil
                </label>
            </div>

            <div class="form-group">
                <label>Métodos de Pago Aceptados:</label>
                <div class="checkbox-group">
                    <label><input type="checkbox" name="metodo-pago" value="efectivo"> Efectivo</label>
                    <label><input type="checkbox" name="metodo-pago" value="transferencia"> Transferencia Bancaria</label>
                    <label><input type="checkbox" name="metodo-pago" value="yape"> Yape / Plin</label>
                    <label><input type="checkbox" name="metodo-pago" value="tarjeta"> Tarjeta de Crédito/Débito</label>
                </div>
            </div>

            <div class="form-group">
                <label>Política de Cancelación:</label>
                <textarea id="politica-cancelacion"
                          placeholder="Ej: Cancelación gratuita hasta 24 horas antes del servicio. Después se cobra 50% del servicio."></textarea>
            </div>
        </section>

        <!-- Botones de acción -->
        <div class="acciones">
            <button id="btn-guardar" class="btn-success">💾 Guardar Perfil</button>
            <button id="btn-cancelar" class="btn-secondary">❌ Cancelar</button>
        </div>

        <!-- Indicador de puntuación -->
        <div class="puntuacion-panel">
            <h3>⭐ Tu Puntuación en la Plataforma</h3>
            <div class="puntuacion-valor">
                <span id="puntuacion">0.0</span> / 10.0
            </div>
            <div class="progreso-bar">
                <div class="progreso-fill" id="progreso-fill"></div>
            </div>
            <p class="puntuacion-detalle">
                La puntuación se calcula con base en tus certificaciones, proyectos,
                calificaciones de clientes y completitud de perfil.
            </p>
        </div>
    </div>

    <!-- ========================================
         MODALES
         ======================================== -->

    <!-- Modal para Certificación -->
    <dialog id="modal-certificacion">
        <form id="form-certificacion">
            <h3>Agregar Certificación</h3>

            <input type="hidden" id="cert-id">

            <div class="form-group">
                <label>Nombre de la Certificación: *</label>
                <input type="text" id="cert-nombre" required>
            </div>

            <div class="form-group">
                <label>Institución: *</label>
                <input type="text" id="cert-institucion" required>
            </div>

            <div class="form-group">
                <label>Fecha de Obtención:</label>
                <input type="date" id="cert-fecha-obtencion">
            </div>

            <div class="form-group">
                <label>Fecha de Vigencia:</label>
                <input type="date" id="cert-fecha-vigencia">
            </div>

            <div class="form-group">
                <label>Documento (PDF/Imagen):</label>
                <input type="file" id="cert-documento" accept=".pdf,image/*">
            </div>

            <div class="form-group">
                <label>Descripción:</label>
                <textarea id="cert-descripcion"></textarea>
            </div>

            <div class="modal-actions">
                <button type="submit" class="btn-primary">Guardar</button>
                <button type="button" class="btn-cancel" onclick="cerrarModalCertificacion()">Cancelar</button>
            </div>
        </form>
    </dialog>

    <!-- Modal para Proyecto -->
    <dialog id="modal-proyecto">
        <form id="form-proyecto">
            <h3>Agregar Proyecto al Portafolio</h3>

            <input type="hidden" id="proyecto-id">

            <div class="form-group">
                <label>Nombre del Proyecto: *</label>
                <input type="text" id="proyecto-nombre" required>
            </div>

            <div class="form-group">
                <label>Fecha de Realización: *</label>
                <input type="date" id="proyecto-fecha" required>
            </div>

            <div class="form-group">
                <label>Descripción: *</label>
                <textarea id="proyecto-descripcion" required></textarea>
            </div>

            <div class="form-group">
                <label>Categoría del Servicio: *</label>
                <select id="proyecto-categoria" required>
                    <!-- Opciones cargadas dinámicamente -->
                </select>
            </div>

            <!-- Sección de imágenes (hasta 5) -->
            <div class="imagenes-proyecto-section">
                <h4>Imágenes del Proyecto (Máx. 5)</h4>
                <p class="info-small">Sube fotos del antes, después o proceso del trabajo.</p>

                <div class="imagenes-upload">
                    <input type="file" id="proyecto-imagenes" accept="image/*" multiple>
                    <button type="button" class="btn-upload" onclick="triggerFileInput('proyecto-imagenes')">
                        📷 Seleccionar Imágenes
                    </button>
                </div>

                <div class="imagenes-preview" id="imagenes-preview">
                    <!-- Previews dinámicos -->
                </div>
            </div>

            <!-- Calificación del cliente (solo lectura) -->
            <div class="calificacion-cliente" id="calificacion-section" style="display: none;">
                <h4>Valoración del Cliente</h4>
                <div class="estrellas">
                    <span class="puntos" id="proyecto-calificacion">0.0</span> / 10.0
                </div>
                <p class="comentario-cliente" id="proyecto-comentario"></p>
            </div>

            <div class="modal-actions">
                <button type="submit" class="btn-primary">Guardar Proyecto</button>
                <button type="button" class="btn-cancel" onclick="cerrarModalProyecto()">Cancelar</button>
            </div>
        </form>
    </dialog>

    <!-- Scripts -->
    <script src="assets/js/profesional-api.js"></script>
    <script src="assets/js/profesional.js"></script>
</body>
</html>
```

---

#### 2. JavaScript: profesional-api.js

Ya fue descrito en el documento `REFACTORIZACION_PERFIL_PROFESIONAL.md`.

---

#### 3. JavaScript: profesional.js (Lógica del formulario)

```javascript
/**
 * Lógica del formulario de perfil profesional
 * Maneja la interacción del usuario con el formulario
 *
 * Creado: 2025-11-15
 */

// Estado de la aplicación
const appState = {
    profesionalId: null,
    certificaciones: [],
    proyectos: [],
    redesSociales: [],
    antecedentes: {}
};

// API Client
const api = new ProfesionalAPI();

// ==================== INICIALIZACIÓN ====================

document.addEventListener('DOMContentLoaded', async () => {
    await cargarPerfilCompleto();
    inicializarEventListeners();
    cargarCategorias();
});

/**
 * Carga el perfil completo del profesional
 */
async function cargarPerfilCompleto() {
    try {
        showLoader();
        const perfil = await api.obtenerPerfilCompleto();

        if (perfil.success) {
            poblarFormulario(perfil.data);
            appState.profesionalId = perfil.data.id;
        } else {
            showAlert(perfil.error, 'error');
        }
    } catch (error) {
        showAlert('Error al cargar el perfil', 'error');
        console.error(error);
    } finally {
        hideLoader();
    }
}

/**
 * Poblar formulario con datos del perfil
 */
function poblarFormulario(perfil) {
    // Datos básicos
    document.getElementById('biografia-profesional').value = perfil.biografiaProfesional || '';
    document.getElementById('anios-experiencia').value = perfil.aniosExperiencia || 0;
    document.getElementById('tarifa-hora').value = perfil.tarifaHora || 0;

    // Imágenes
    if (perfil.fotoPerfil) {
        document.getElementById('preview-perfil').src = perfil.fotoPerfil;
    }
    if (perfil.fotoPortada) {
        document.getElementById('preview-portada').src = perfil.fotoPortada;
    }

    // Certificaciones
    renderizarCertificaciones(perfil.certificaciones || []);

    // Proyectos
    renderizarProyectos(perfil.proyectos || []);

    // Redes sociales
    poblarRedesSociales(perfil.redesSociales || []);

    // Antecedentes
    mostrarAntecedentes(perfil.antecedentes || []);

    // Información adicional
    poblarInfoAdicional(perfil);

    // Puntuación
    mostrarPuntuacion(perfil.puntuacionPlataforma || 0);
}

// ==================== CERTIFICACIONES ====================

function renderizarCertificaciones(certificaciones) {
    const contenedor = document.getElementById('certificaciones-lista');
    contenedor.innerHTML = '';

    certificaciones.forEach(cert => {
        const card = crearCardCertificacion(cert);
        contenedor.appendChild(card);
    });

    appState.certificaciones = certificaciones;
}

function crearCardCertificacion(cert) {
    const div = document.createElement('div');
    div.className = 'certificacion-card';
    div.innerHTML = `
        <h4>${cert.nombreCertificacion}</h4>
        <p class="institucion">${cert.institucion}</p>
        <p class="fecha">${formatearFecha(cert.fechaObtencion)}</p>
        <div class="acciones">
            <button onclick="editarCertificacion(${cert.id})">✏️ Editar</button>
            <button onclick="eliminarCertificacion(${cert.id})">🗑️ Eliminar</button>
        </div>
    `;
    return div;
}

async function guardarCertificacion(datos) {
    try {
        const result = await api.crearCertificacion(datos);
        if (result.success) {
            showAlert('Certificación guardada exitosamente', 'success');
            await cargarPerfilCompleto(); // Recargar
        } else {
            showAlert(result.error, 'error');
        }
    } catch (error) {
        showAlert('Error al guardar certificación', 'error');
    }
}

// ==================== PROYECTOS ====================

function renderizarProyectos(proyectos) {
    const contenedor = document.getElementById('proyectos-grid');
    contenedor.innerHTML = '';

    proyectos.forEach(proyecto => {
        const card = crearCardProyecto(proyecto);
        contenedor.appendChild(card);
    });

    document.getElementById('total-proyectos').textContent = proyectos.length;
    appState.proyectos = proyectos;
}

function crearCardProyecto(proyecto) {
    const div = document.createElement('div');
    div.className = 'proyecto-card';

    let imagenesHTML = '';
    if (proyecto.imagenes && proyecto.imagenes.length > 0) {
        imagenesHTML = `<img src="${proyecto.imagenes[0].urlImagen}" alt="${proyecto.nombreProyecto}">`;
    }

    let calificacionHTML = '';
    if (proyecto.calificacionCliente) {
        calificacionHTML = `
            <div class="calificacion">
                ⭐ ${proyecto.calificacionCliente.toFixed(1)} / 10
            </div>
        `;
    }

    div.innerHTML = `
        ${imagenesHTML}
        <div class="contenido">
            <h4>${proyecto.nombreProyecto}</h4>
            <p class="categoria">${proyecto.categoriaNombre || 'Sin categoría'}</p>
            <p class="fecha">${formatearFecha(proyecto.fechaRealizacion)}</p>
            <p class="descripcion">${truncarTexto(proyecto.descripcion, 100)}</p>
            ${calificacionHTML}
            <div class="acciones">
                <button onclick="verProyecto(${proyecto.id})">👁️ Ver</button>
                <button onclick="editarProyecto(${proyecto.id})">✏️ Editar</button>
                <button onclick="eliminarProyecto(${proyecto.id})">🗑️ Eliminar</button>
            </div>
        </div>
    `;
    return div;
}

// ==================== HELPERS ====================

function formatearFecha(fecha) {
    if (!fecha) return '-';
    const d = new Date(fecha);
    return d.toLocaleDateString('es-PE');
}

function truncarTexto(texto, max) {
    if (!texto) return '';
    return texto.length > max ? texto.substring(0, max) + '...' : texto;
}

function showAlert(mensaje, tipo) {
    // Implementar sistema de alertas
}

function showLoader() {
    // Mostrar spinner
}

function hideLoader() {
    // Ocultar spinner
}
```

---

## 🎯 Orden de Implementación Sugerido

1. ✅ **COMPLETADO:**
   - Migración SQL
   - Models
   - CertificacionesProfesionalDAO

2. **SIGUIENTE (Alta Prioridad):**
   - ProyectosPortafolioDAOImpl
   - ImagenesProyectoDAO + Impl
   - CertificacionesProfesionalServlet
   - ProyectosPortafolioServlet

3. **DESPUÉS (Media Prioridad):**
   - AntecedentesProfesionalDAO + Impl + Servlet
   - RedesSocialesProfesionalDAO + Impl + Servlet
   - PerfilProfesionalCompletoDTO
   - PerfilProfesionalServlet (refactorizado)

4. **FINALMENTE (Baja Prioridad):**
   - Frontend HTML completo
   - Frontend JavaScript completo
   - Testing manual
   - Documentación de API

---

## 📊 Progreso Actual

| Componente | Estado | %  |
|------------|--------|-------|
| Migración SQL | ✅ | 100% |
| Models | ✅ | 100% |
| DAOs | 🔄 | 20% |
| Servlets | ⏳ | 0% |
| DTOs | ⏳ | 0% |
| Frontend HTML | ⏳ | 0% |
| Frontend JS | ⏳ | 0% |

**Progreso Total: ~20%**

---

**Fecha:** 2025-11-15
**Actualizado por:** Claude Code
