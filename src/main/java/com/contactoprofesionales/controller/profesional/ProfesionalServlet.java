package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.dto.EspecialidadDTO;
import com.contactoprofesionales.model.Profesional;
import com.contactoprofesionales.service.profesional.EspecialidadService;
import com.contactoprofesionales.service.profesional.EspecialidadServiceImpl;
import com.contactoprofesionales.service.profesional.ProfesionalService;
import com.contactoprofesionales.util.JsonResponse;
import com.contactoprofesionales.util.GsonUtil;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ProfesionalException;
import com.contactoprofesionales.exception.ValidationException;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet para gestión de profesionales - BÚSQUEDA Y CONSULTA PÚBLICA.
 *
 * ⚠️ IMPORTANTE - ACTUALIZADO 2025-11-16:
 * Este servlet es SOLO para búsqueda y consulta pública de profesionales.
 * NO se usa para la gestión del perfil profesional.
 *
 * USO ACTUAL:
 * - Búsqueda pública de profesionales (listado, filtros)
 * - Consulta de perfil público de un profesional
 * - Verificar existencia de perfil profesional por usuarioId
 *
 * PARA GESTIÓN DE PERFIL PROFESIONAL USAR:
 * - PerfilProfesionalServlet: Para datos básicos del perfil
 * - CertificacionesProfesionalServlet: Para certificaciones
 * - ProyectosPortafolioServlet: Para proyectos del portafolio
 * - AntecedentesProfesionalServlet: Para antecedentes
 * - RedesSocialesProfesionalServlet: Para redes sociales
 *
 * FORMULARIO WEB:
 * - Este servlet NO es usado por profesional-refactorizado.html
 * - El formulario usa PerfilProfesionalServlet y servlets específicos de cada sección
 *
 * CAMPOS DEPRECADOS:
 * - fotoPerfil, fotoPortada: Mantenidos para compatibilidad pero no se gestionan en formulario
 * - nombreCompleto, email, telefono: Mantenidos para búsqueda pública
 * - habilidades, certificaciones, portafolio: Mantenidos para compatibilidad
 *
 * Endpoints DISPONIBLES (solo lectura):
 * - GET /api/profesionales                -> Listar profesionales (con filtros opcionales)
 * - GET /api/profesionales/{id}           -> Obtener profesional específico
 * - GET /api/profesionales?usuarioId={id} -> Obtener profesional por usuarioId
 *
 * ❌ Endpoints ELIMINADOS (2025-12-03):
 * - POST /api/profesionales               -> ELIMINADO (usar POST /api/auth/registro)
 * - PUT /api/profesionales/{id}           -> ELIMINADO (usar servlets específicos de perfil)
 */
@WebServlet(name = "ProfesionalServlet", urlPatterns = {
    "/api/profesionales",
    "/api/profesionales/*"
})
public class ProfesionalServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProfesionalServlet.class);
    //private final Gson gson = new Gson();
    private static final Gson gson = GsonUtil.createGson();

    private ProfesionalService profesionalService;
    private EspecialidadService especialidadService;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando ProfesionalServlet ===");

        try {
            this.profesionalService = new ProfesionalService();
            this.especialidadService = new EspecialidadServiceImpl();
            logger.info("✓ ProfesionalServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar ProfesionalServlet", e);
            throw new ServletException("Error al inicializar servlet", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        logger.info("GET /api/profesionales");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            String usuarioIdParam = request.getParameter("usuarioId");
            
            // NUEVO: Buscar por usuarioId
            if (usuarioIdParam != null) {
                Integer usuarioId = Integer.parseInt(usuarioIdParam);
                obtenerProfesionalPorUsuario(usuarioId, response);
                return;
            }
            
            if (pathInfo == null || pathInfo.equals("/")) {
                // Listar profesionales con filtros opcionales
                listarProfesionales(request, response);
            } else {
                // Obtener profesional específico
                String[] splits = pathInfo.split("/");

                // ✅ NUEVO: Detectar URLs para recursos anidados
                // Ejemplo: /1/especialidades debe ser manejado directamente
                if (splits.length > 2 && "especialidades".equals(splits[2])) {
                    Integer profesionalId = Integer.parseInt(splits[1]);
                    logger.info("🔍 Obteniendo especialidades para profesional ID: {}", profesionalId);

                    try {
                        List<EspecialidadDTO> especialidades =
                            especialidadService.listarPorProfesional(profesionalId);

                        logger.info("✅ Se encontraron {} especialidades para el profesional ID {}",
                            especialidades.size(), profesionalId);

                        // Construir respuesta en formato {success: true, data: [...]}
                        JsonResponse jsonResponse = JsonResponse.success(especialidades);

                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().write(gson.toJson(jsonResponse));
                    } catch (Exception e) {
                        logger.error("❌ Error al obtener especialidades para profesional {}: {}",
                            profesionalId, e.getMessage(), e);
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            "Error al obtener especialidades");
                    }
                    return;
                }

                // Si tiene más subrutas pero no reconocidas
                if (splits.length > 2) {
                    logger.warn("URL con subrutas no reconocida: {}", pathInfo);
                    response.sendError(HttpServletResponse.SC_NOT_FOUND,
                        "Recurso no encontrado");
                    return;
                }

                if (splits.length >= 2) {
                    Integer profesionalId = Integer.parseInt(splits[1]);
                    obtenerProfesional(profesionalId, response);
                } else {
                    sendBadRequest(response, "ID de profesional inválido");
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Request completado - Tiempo: {}ms", duration);
            
        } catch (NumberFormatException e) {
            sendBadRequest(response, "ID de profesional inválido");
            
        } catch (ValidationException e) {
            handleValidationError(response, e, startTime);
            
        } catch (ProfesionalException e) {
            handleProfesionalError(response, e, startTime);
            
        } catch (DatabaseException e) {
            handleDatabaseError(response, e, startTime);
            
        } catch (Exception e) {
            handleInternalError(response, e, startTime);
        }
    }

    /**
     * ═══════════════════════════════════════════════════════════════════════════════
     * MÉTODOS POST Y PUT ELIMINADOS - 2025-12-03
     * ═══════════════════════════════════════════════════════════════════════════════
     *
     * Los métodos doPost() y doPut() han sido ELIMINADOS como parte de la refactorización.
     *
     * ❌ ENDPOINTS DEPRECADOS (YA NO DISPONIBLES):
     * - POST /api/profesionales          → Crear profesional
     * - PUT /api/profesionales/{id}      → Actualizar profesional
     *
     * ✅ USAR EN SU LUGAR:
     *
     * 1. PARA CREAR UN PROFESIONAL:
     *    - POST /api/auth/registro  (AutenticacionServlet)
     *      Crea usuario y profesional en una transacción
     *
     * 2. PARA ACTUALIZAR PERFIL PROFESIONAL:
     *    - PUT /api/profesional/perfil          (PerfilProfesionalServlet)
     *    - POST/PUT /api/profesional/certificaciones  (CertificacionesProfesionalServlet)
     *    - POST/PUT /api/profesional/proyectos        (ProyectosPortafolioServlet)
     *    - POST/PUT /api/profesional/antecedentes     (AntecedentesProfesionalServlet)
     *    - POST/PUT /api/profesional/redes-sociales   (RedesSocialesProfesionalServlet)
     *
     * Este servlet ahora SOLO soporta operaciones de LECTURA (GET):
     * - GET /api/profesionales              → Listar profesionales
     * - GET /api/profesionales/{id}         → Obtener profesional específico
     * - GET /api/profesionales?usuarioId={id} → Obtener profesional por usuario
     *
     * ═══════════════════════════════════════════════════════════════════════════════
     */
    
    private void obtenerProfesionalPorUsuario(Integer usuarioId, HttpServletResponse response) 
            throws Exception {
        
    	logger.info("Buscando profesional para usuario: {}", usuarioId);
        
        try {
            Profesional profesional = profesionalService.obtenerProfesionalPorUsuario(usuarioId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("profesional", profesional);
            responseData.put("existe", true);
            
            JsonResponse jsonResponse = JsonResponse.success(responseData);
            
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(jsonResponse));
            
            logger.info("✓ Profesional encontrado: ID {}", profesional.getId());
            
        } catch (ProfesionalException e) {
            // No existe perfil - esto es normal para nuevos registros
            logger.info("Usuario {} no tiene perfil de profesional (OK para registro)", usuarioId);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("profesional", null);
            responseData.put("existe", false);
            responseData.put("mensaje", "Usuario sin perfil profesional");
            
            JsonResponse jsonResponse = JsonResponse.success(responseData);
            
            response.setStatus(HttpServletResponse.SC_OK); // ✅ 200 OK, no 404
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }
    
    /**
     * Lista profesionales con filtros opcionales.
     */
    private void listarProfesionales(HttpServletRequest request, HttpServletResponse response) 
            throws Exception {
        
        String especialidad = request.getParameter("especialidad");
        String distrito = request.getParameter("distrito");
        String calificacionStr = request.getParameter("calificacion");
        
        Double calificacionMin = null;
        if (calificacionStr != null && !calificacionStr.isEmpty()) {
            try {
                calificacionMin = Double.parseDouble(calificacionStr);
            } catch (NumberFormatException e) {
                logger.warn("Calificación inválida: {}", calificacionStr);
            }
        }
        
        List<Profesional> profesionales;
        
        if (especialidad != null || distrito != null || calificacionMin != null) {
            // Búsqueda con filtros
            profesionales = profesionalService.buscarConFiltros(especialidad, distrito, calificacionMin);
        } else {
            // Listar todos
            profesionales = profesionalService.listarProfesionales();
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("profesionales", profesionales);
        responseData.put("total", profesionales.size());
        responseData.put("filtros", Map.of(
            "especialidad", especialidad != null ? especialidad : "todos",
            "distrito", distrito != null ? distrito : "todos",
            "calificacionMin", calificacionMin != null ? calificacionMin : 0
        ));
        
        JsonResponse jsonResponse = JsonResponse.success(responseData);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    /**
     * Obtiene un profesional específico por ID.
     *
     * ⚠️ NOTA - ACTUALIZADO 2025-11-16:
     * Este método devuelve campos deprecados para compatibilidad con búsqueda pública.
     * Los campos fotoPerfil, fotoPortada, nombreCompleto, email, telefono se mantienen
     * SOLO para mostrar perfiles en búsquedas públicas, pero YA NO se gestionan en
     * el formulario profesional-refactorizado.html.
     *
     * PARA GESTIÓN DE PERFIL usar:
     * - GET /api/profesional/perfil: Devuelve PerfilProfesionalCompletoDTO con todas las relaciones
     */
    private void obtenerProfesional(Integer profesionalId, HttpServletResponse response)
            throws Exception {

    	logger.info("🔍 Buscando profesional con ID: {}", profesionalId);

    	try {
            Profesional profesional = profesionalService.obtenerProfesional(profesionalId);

            if (profesional == null) {
                logger.warn("⚠️ Profesional con ID {} no encontrado", profesionalId);
                sendNotFound(response, "Profesional no encontrado");
                return;
            }

            // Construir respuesta con datos completos
            Map<String, Object> profesionalData = new HashMap<>();
         // Campos principales
            profesionalData.put("id", profesional.getId());
            profesionalData.put("usuarioId", profesional.getUsuarioId());

            // ⚠️ DEPRECADO - Información personal (de JOIN con users)
            // Mantenido SOLO para búsqueda pública, NO se gestiona en formulario profesional.html
            profesionalData.put("nombreCompleto", profesional.getNombreCompleto() != null ? profesional.getNombreCompleto() : "");
            profesionalData.put("email", profesional.getEmail() != null ? profesional.getEmail() : "");
            profesionalData.put("telefono", profesional.getTelefono() != null ? profesional.getTelefono() : "");
            
            // Información profesional
            profesionalData.put("especialidad", profesional.getEspecialidad() != null ? profesional.getEspecialidad() : "");
            profesionalData.put("descripcion", profesional.getDescripcion() != null ? profesional.getDescripcion() : "");
            profesionalData.put("experiencia", profesional.getExperiencia() != null ? profesional.getExperiencia() : "");
            
            // ⚠️ DEPRECADO - Habilidades y certificaciones (pueden ser JSON strings)
            // Ahora se usan tablas relacionadas: certificaciones_profesionales
            profesionalData.put("habilidades", profesional.getHabilidades() != null ? profesional.getHabilidades() : "");
            profesionalData.put("certificaciones", profesional.getCertificaciones() != null ? profesional.getCertificaciones() : "");

            // ⚠️ DEPRECADO - Multimedia
            // fotoPerfil, fotoPortada: Ya NO se gestionan en formulario profesional-refactorizado.html
            // portafolio: Ahora se usa tabla proyectos_portafolio
            // Mantenido SOLO para búsqueda pública
            profesionalData.put("fotoPerfil", profesional.getFotoPerfil() != null ? profesional.getFotoPerfil() : "");
            profesionalData.put("fotoPortada", profesional.getFotoPortada() != null ? profesional.getFotoPortada() : "");
            profesionalData.put("portafolio", profesional.getPortafolio() != null ? profesional.getPortafolio() : "");
            
            // Tarifas y calificaciones
            profesionalData.put("tarifaHora", profesional.getTarifaHora() != null ? profesional.getTarifaHora() : 0.0);
            profesionalData.put("calificacionPromedio", profesional.getCalificacionPromedio() != null ? profesional.getCalificacionPromedio() : 0.0);
            profesionalData.put("totalResenas", profesional.getTotalResenas() != null ? profesional.getTotalResenas() : 0);
            
            // Ubicación y servicio
            profesionalData.put("ubicacion", profesional.getUbicacion() != null ? profesional.getUbicacion() : "");
            profesionalData.put("distrito", profesional.getDistrito() != null ? profesional.getDistrito() : "");
            profesionalData.put("latitud", profesional.getLatitud() != null ? profesional.getLatitud() : 0.0);
            profesionalData.put("longitud", profesional.getLongitud() != null ? profesional.getLongitud() : 0.0);
            profesionalData.put("radioServicio", profesional.getRadioServicio() != null ? profesional.getRadioServicio() : 0);
            
            // Disponibilidad (puede ser JSON string con horarios)
            profesionalData.put("disponibilidad", profesional.getDisponibilidad() != null ? profesional.getDisponibilidad() : "");
            
            // Estados booleanos
            profesionalData.put("verificado", profesional.isVerificado());
            profesionalData.put("disponible", profesional.isDisponible());
            profesionalData.put("activo", profesional.isActivo());
            
            // Fechas
            if (profesional.getFechaRegistro() != null) {
                profesionalData.put("fechaRegistro", profesional.getFechaRegistro().toString());
            }
            if (profesional.getUltimaActualizacion() != null) {
                profesionalData.put("ultimaActualizacion", profesional.getUltimaActualizacion().toString());
            }
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("profesional", profesionalData);
            
            JsonResponse jsonResponse = JsonResponse.success(responseData);
            
            response.setStatus(HttpServletResponse.SC_OK);
            String jsonString = gson.toJson(jsonResponse);
            
            logger.info("✅ Profesional encontrado: {}", profesional.getNombreCompleto());
            logger.debug("📤 Enviando respuesta JSON: {}", jsonString);
            
            response.getWriter().write(jsonString);
            
        } catch (ProfesionalException e) {
            logger.error("❌ Error al obtener profesional {}: {}", profesionalId, e.getMessage());
            throw e;
        }
    }
    
    // Agregar este método si no existe
    private void sendNotFound(HttpServletResponse response, String message) 
            throws IOException {
        JsonResponse jsonResponse = JsonResponse.error(message);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    // Manejo de errores
    
    private void handleValidationError(HttpServletResponse response, ValidationException e, 
                                      long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.warn("✗ Error de validación: {} - Tiempo: {}ms", e.getMessage(), duration);
        
        JsonResponse jsonResponse = JsonResponse.error(e.getMessage());
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    private void handleProfesionalError(HttpServletResponse response, ProfesionalException e, 
                                       long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.warn("✗ Error de profesional: {} - Tiempo: {}ms", e.getMessage(), duration);
        
        JsonResponse jsonResponse = JsonResponse.error(e.getMessage());
        
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    private void handleDatabaseError(HttpServletResponse response, DatabaseException e, 
                                    long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.error("✗ Error de base de datos - Tiempo: {}ms", duration, e);
        
        JsonResponse jsonResponse = JsonResponse.error(
            "Error del servidor. Por favor intente nuevamente"
        );
        
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    private void handleInternalError(HttpServletResponse response, Exception e, 
                                    long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.error("✗ Error interno - Tiempo: {}ms", duration, e);
        
        JsonResponse jsonResponse = JsonResponse.error("Error interno del servidor");
        
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    private void sendBadRequest(HttpServletResponse response, String message) 
            throws IOException {
        JsonResponse jsonResponse = JsonResponse.error(message);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    @Override
    public void destroy() {
        logger.info("Destruyendo ProfesionalServlet");
        super.destroy();
    }
}