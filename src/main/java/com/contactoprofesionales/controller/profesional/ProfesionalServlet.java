package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.model.Profesional;
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
 * Servlet para gestionar profesionales.
 * 
 * Endpoints:
 * - GET /api/profesionales          -> Listar profesionales (con filtros opcionales)
 * - GET /api/profesionales/{id}     -> Obtener profesional específico
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
    
    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando ProfesionalServlet ===");
        
        try {
            this.profesionalService = new ProfesionalService();
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
    
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        logger.info("POST /api/profesionales");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Leer body del request
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String requestBody = buffer.toString();
            
            logger.info("Body recibido: {}", requestBody);
            
            // Parsear JSON a Profesional
            Profesional profesional = gson.fromJson(requestBody, Profesional.class);
            
            if (profesional == null) {
                sendBadRequest(response, "Datos inválidos");
                return;
            }
            
            logger.info("Profesional parseado: {}", profesional);
            
            // Crear profesional
            Profesional nuevoProfesional = profesionalService.crearProfesional(profesional);
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("profesional", nuevoProfesional);
            responseData.put("mensaje", "Perfil profesional creado exitosamente");
            
            JsonResponse jsonResponse = JsonResponse.success(responseData);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(jsonResponse));
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Profesional creado - ID: {} - Tiempo: {}ms", 
                       nuevoProfesional.getId(), duration);
            
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
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        logger.info("PUT /api/profesionales");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            String pathInfo = request.getPathInfo();
            
            if (pathInfo == null || pathInfo.equals("/")) {
                sendBadRequest(response, "ID de profesional requerido");
                return;
            }
            
            String[] splits = pathInfo.split("/");
            if (splits.length < 2) {
                sendBadRequest(response, "ID de profesional inválido");
                return;
            }
            
            Integer profesionalId = Integer.parseInt(splits[1]);
            
            // Leer body del request
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            String requestBody = buffer.toString();
            
            logger.info("Body recibido para actualización: {}", requestBody);
            
            // Parsear JSON a Profesional
            Profesional profesional = gson.fromJson(requestBody, Profesional.class);
            
            if (profesional == null) {
                sendBadRequest(response, "Datos inválidos");
                return;
            }
            
            // Asignar el ID del path
            profesional.setId(profesionalId);
            
            logger.info("Actualizando profesional ID: {}", profesionalId);
            
            // Actualizar profesional
            boolean actualizado = profesionalService.actualizarProfesional(profesional);
            
            if (actualizado) {
                // Obtener el profesional actualizado
                Profesional profesionalActualizado = profesionalService.obtenerProfesional(profesionalId);
                
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("profesional", profesionalActualizado);
                responseData.put("mensaje", "Perfil profesional actualizado exitosamente");
                
                JsonResponse jsonResponse = JsonResponse.success(responseData);
                
                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(jsonResponse));
                
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Profesional actualizado - ID: {} - Tiempo: {}ms", 
                           profesionalId, duration);
            } else {
                throw new ProfesionalException("No se pudo actualizar el profesional");
            }
            
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
            
            // Información personal (de JOIN con users)
            profesionalData.put("nombreCompleto", profesional.getNombreCompleto() != null ? profesional.getNombreCompleto() : "");
            profesionalData.put("email", profesional.getEmail() != null ? profesional.getEmail() : "");
            profesionalData.put("telefono", profesional.getTelefono() != null ? profesional.getTelefono() : "");
            
            // Información profesional
            profesionalData.put("especialidad", profesional.getEspecialidad() != null ? profesional.getEspecialidad() : "");
            profesionalData.put("descripcion", profesional.getDescripcion() != null ? profesional.getDescripcion() : "");
            profesionalData.put("experiencia", profesional.getExperiencia() != null ? profesional.getExperiencia() : "");
            
            // Habilidades y certificaciones (pueden ser JSON strings)
            profesionalData.put("habilidades", profesional.getHabilidades() != null ? profesional.getHabilidades() : "");
            profesionalData.put("certificaciones", profesional.getCertificaciones() != null ? profesional.getCertificaciones() : "");
            
            // Multimedia
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