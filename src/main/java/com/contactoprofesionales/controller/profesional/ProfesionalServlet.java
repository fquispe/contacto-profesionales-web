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
    
    private void obtenerProfesionalPorUsuario(Integer usuarioId, HttpServletResponse response) 
            throws Exception {
        
        Profesional profesional = profesionalService.obtenerProfesionalPorUsuario(usuarioId);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("profesional", profesional);
        
        JsonResponse jsonResponse = JsonResponse.success(responseData);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(jsonResponse));
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
        
        Profesional profesional = profesionalService.obtenerProfesional(profesionalId);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("profesional", profesional);
        
        JsonResponse jsonResponse = JsonResponse.success(responseData);
        
        response.setStatus(HttpServletResponse.SC_OK);
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