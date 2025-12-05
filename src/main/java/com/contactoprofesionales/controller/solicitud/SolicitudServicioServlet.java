package com.contactoprofesionales.controller.solicitud;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.dto.SolicitudServicioRequest;
import com.contactoprofesionales.dto.SolicitudDetalleDTO;
import com.contactoprofesionales.service.solicitud.SolicitudServicioService;
import com.contactoprofesionales.util.JsonResponse;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.exception.DatabaseException;

import com.contactoprofesionales.util.LocalDateTimeAdapter;
import java.time.LocalDateTime;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet para gestionar solicitudes de servicio.
 *
 * Endpoints:
 * - POST /api/solicitudes                -> Crear solicitud
 * - GET  /api/solicitudes                -> Listar solicitudes del usuario
 * - GET  /api/solicitudes/{id}           -> Obtener solicitud específica
 * - PUT  /api/solicitudes/{id}/cancelar  -> Cancelar solicitud (clientes)
 * - PUT  /api/solicitudes/{id}/estado    -> Actualizar estado (profesionales)
 * - GET  /api/solicitudes/pendientes/count -> Contar solicitudes pendientes (profesionales)
 *
 * ACTUALIZADO 2025-12-03: Agregado endpoint de actualización de estado para profesionales
 */
@WebServlet(name = "SolicitudServicioServlet", urlPatterns = {
    "/api/solicitudes",
    "/api/solicitudes/*"
})
public class SolicitudServicioServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(SolicitudServicioServlet.class);
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    private SolicitudServicioService solicitudService;
    
    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando SolicitudServicioServlet ===");
        
        try {
            this.solicitudService = new SolicitudServicioService();
            logger.info("✓ SolicitudServicioServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar SolicitudServicioServlet", e);
            throw new ServletException("Error al inicializar servlet", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
    	long startTime = System.currentTimeMillis();
        logger.info("POST /api/solicitudes - Nueva solicitud de servicio");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // ✅ PASO 1: PRIMERO parsear el request para obtener los datos del body
            SolicitudServicioRequest solicitudRequest = parseRequest(request);
            
            logger.info("📦 Request parseado correctamente");
            logger.info("📦 Datos recibidos: {}", gson.toJson(solicitudRequest));
            
            // ✅ PASO 2: Obtener clienteId DEL BODY (no de la sesión)
            Integer clienteId = solicitudRequest.getClienteId();
            
            logger.info("👤 Cliente ID del body: {}", clienteId);
            
            // Si no viene en el body, intentar obtener de sesión (fallback)
            if (clienteId == null) {
                logger.warn("⚠️ clienteId no viene en el body, intentando obtener de sesión...");
                clienteId = obtenerUsuarioId(request);
                logger.info("👤 Cliente ID de sesión: {}", clienteId);
            }
            
            // ✅ PASO 3: Validar que existe clienteId
            if (clienteId == null) {
                logger.error("❌ No se pudo obtener clienteId (ni del body ni de la sesión)");
                sendUnauthorized(response, "Usuario no autenticado");
                return;
            }
            
            logger.info("✅ Cliente ID validado: {}", clienteId);
            
            // ✅ PASO 4: Crear solicitud
            SolicitudServicio solicitud = solicitudService.crearSolicitud(clienteId, solicitudRequest);
            
            // Preparar respuesta
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("solicitudId", solicitud.getId());
            responseData.put("codigoSolicitud", generarCodigoSolicitud(solicitud.getId()));
            responseData.put("estado", solicitud.getEstado());
            responseData.put("fechaSolicitud", solicitud.getFechaSolicitud().toString());
            
            JsonResponse jsonResponse = JsonResponse.success(
                "Solicitud enviada exitosamente", 
                responseData
            );
            
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✅ Solicitud creada con ID: {} - Tiempo: {}ms", 
                       solicitud.getId(), duration);
            
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(jsonResponse));
            
        } catch (ValidationException e) {
            handleValidationError(response, e, startTime);
            
        } catch (DatabaseException e) {
            handleDatabaseError(response, e, startTime);
            
        } catch (JsonSyntaxException e) {
            handleJsonError(response, e, startTime);
            
        } catch (Exception e) {
            handleInternalError(response, e, startTime);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        logger.info("GET /api/solicitudes");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Verificar autenticación
            Integer usuarioId = obtenerUsuarioId(request);
            
            logger.info("busqueda de solicitudes para el usuario: "+ usuarioId);
            
            if (usuarioId == null) {
            	String userIdParam = request.getParameter("usuarioId");
                if (userIdParam != null && !userIdParam.isEmpty()) {
                    usuarioId = Integer.parseInt(userIdParam);
                    logger.info("Usuario ID obtenido de parámetro: {}", usuarioId);
                }
            }
            
            logger.info("Búsqueda de solicitudes para el usuario: {}", usuarioId);
                        
            if (usuarioId == null) {
                sendUnauthorized(response, "Usuario no autenticado");
                return;
            }
            
            String pathInfo = request.getPathInfo();
            logger.debug("PathInfo: {}", pathInfo);

            if (pathInfo == null || pathInfo.equals("/")) {
                // Listar todas las solicitudes del usuario
                listarSolicitudes(usuarioId, request, response);
            } else if (pathInfo.equals("/pendientes/count")) {
                // ✅ NUEVO: Contar solicitudes pendientes para badge de alertas
                contarSolicitudesPendientes(usuarioId, response);
            } else {
                // Obtener solicitud específica
                String[] splits = pathInfo.split("/");
                if (splits.length >= 2) {
                    Integer solicitudId = Integer.parseInt(splits[1]);
                    logger.info("📋 Solicitando detalle de solicitud {} para usuario {}", solicitudId, usuarioId);
                    String tipo = request.getParameter("tipo");
                    logger.debug("Tipo de usuario: {}", tipo);
                    obtenerSolicitud(solicitudId, usuarioId, response);
                } else {
                    sendBadRequest(response, "ID de solicitud inválido");
                }
            }
            
        } catch (NumberFormatException e) {
            sendBadRequest(response, "ID de solicitud inválido");
            
        } catch (Exception e) {
            handleInternalError(response, e, System.currentTimeMillis());
        }
    }
    
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("PUT /api/solicitudes - Actualizar solicitud");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // ✅ ACTUALIZADO 2025-12-04: Obtener usuarioId de query parameter o sesión
            Integer usuarioId = obtenerUsuarioId(request);

            // Si no hay sesión, intentar obtener desde query parameter
            if (usuarioId == null) {
                String userIdParam = request.getParameter("usuarioId");
                if (userIdParam != null && !userIdParam.isEmpty()) {
                    usuarioId = Integer.parseInt(userIdParam);
                    logger.info("Usuario ID obtenido de parámetro: {}", usuarioId);
                }
            }

            if (usuarioId == null) {
                sendUnauthorized(response, "Usuario no autenticado");
                return;
            }

            String pathInfo = request.getPathInfo();

            if (pathInfo != null && pathInfo.contains("/cancelar")) {
                // ✅ Cancelar solicitud (clientes)
                String[] splits = pathInfo.split("/");
                if (splits.length >= 2) {
                    Integer solicitudId = Integer.parseInt(splits[1]);
                    cancelarSolicitud(solicitudId, usuarioId, response);
                } else {
                    sendBadRequest(response, "ID de solicitud inválido");
                }
            } else if (pathInfo != null && pathInfo.contains("/estado")) {
                // ✅ ACTUALIZADO 2025-12-04: Actualizar estado de solicitud (profesionales)
                String[] splits = pathInfo.split("/");
                if (splits.length >= 2) {
                    Integer solicitudId = Integer.parseInt(splits[1]);
                    // usuarioId aquí es el profesionalId
                    actualizarEstadoSolicitud(solicitudId, usuarioId, request, response);
                } else {
                    sendBadRequest(response, "ID de solicitud inválido");
                }
            } else {
                sendBadRequest(response, "Acción no válida");
            }

        } catch (NumberFormatException e) {
            sendBadRequest(response, "ID de solicitud inválido");

        } catch (Exception e) {
            handleInternalError(response, e, System.currentTimeMillis());
        }
    }
    
    // Métodos auxiliares
    
    private void listarSolicitudes(Integer usuarioId, HttpServletRequest request, 
                                  HttpServletResponse response) throws Exception {
        
        String tipo = request.getParameter("tipo"); // "cliente" o "profesional"
        
        List<SolicitudServicio> solicitudes;
        
        if ("profesional".equals(tipo)) {
            solicitudes = solicitudService.listarSolicitudesProfesional(usuarioId);
        } else {
            solicitudes = solicitudService.listarSolicitudesCliente(usuarioId);
        }
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("solicitudes", solicitudes);
        responseData.put("total", solicitudes.size());
        
        JsonResponse jsonResponse = JsonResponse.success(responseData);
        
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    private void obtenerSolicitud(Integer solicitudId, Integer usuarioId,
                                 HttpServletResponse response) throws Exception {

        logger.debug("Obteniendo solicitud {} para usuario {}", solicitudId, usuarioId);

        try {
            // ✅ NUEVO: Usar DTO extendido para detalle completo
            SolicitudDetalleDTO solicitudDetalle = solicitudService.obtenerSolicitudConDetalle(solicitudId, usuarioId);

            logger.info("✅ Solicitud {} encontrada con detalle completo para usuario {}", solicitudId, usuarioId);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("solicitud", solicitudDetalle);
            responseData.put("codigoSolicitud", generarCodigoSolicitud(solicitudId));

            JsonResponse jsonResponse = JsonResponse.success(responseData);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(jsonResponse));

        } catch (ValidationException e) {
            // Si la solicitud no existe o el usuario no tiene permiso, retornar 404
            logger.warn("❌ Error de validación al obtener solicitud {}: {}", solicitudId, e.getMessage());
            if (e.getMessage().contains("no encontrada") || e.getMessage().contains("No tienes permiso")) {
                sendNotFound(response, e.getMessage());
            } else {
                sendBadRequest(response, e.getMessage());
            }
        }
    }
    
    private void cancelarSolicitud(Integer solicitudId, Integer clienteId,
                                  HttpServletResponse response) throws Exception {

        boolean cancelada = solicitudService.cancelarSolicitud(solicitudId, clienteId);

        if (cancelada) {
            JsonResponse jsonResponse = JsonResponse.success("Solicitud cancelada exitosamente");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(jsonResponse));
        } else {
            sendBadRequest(response, "No se pudo cancelar la solicitud");
        }
    }

    /**
     * Cuenta solicitudes pendientes para un profesional.
     * Endpoint: GET /api/solicitudes/pendientes/count
     *
     * ACTUALIZADO 2025-12-03: Agregado para sistema de alertas del profesional
     */
    private void contarSolicitudesPendientes(Integer profesionalId, HttpServletResponse response)
            throws Exception {

        logger.info("📊 Contando solicitudes pendientes para profesional {}", profesionalId);

        int count = solicitudService.contarSolicitudesPendientes(profesionalId);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("count", count);
        responseData.put("profesionalId", profesionalId);

        JsonResponse jsonResponse = JsonResponse.success(responseData);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(jsonResponse));

        logger.info("✅ Respuesta enviada: {} solicitudes pendientes", count);
    }

    /**
     * Actualiza el estado de una solicitud (solo profesionales).
     * Endpoint: PUT /api/solicitudes/{id}/estado
     * Body: { "nuevoEstado": "aceptada" }
     *
     * ACTUALIZADO 2025-12-03: Agregado para dashboard del profesional
     */
    private void actualizarEstadoSolicitud(Integer solicitudId, Integer profesionalId,
                                          HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        logger.info("🔄 Actualizando estado de solicitud {} por profesional {}", solicitudId, profesionalId);

        // ✅ Parsear request body para obtener el nuevo estado
        Map<String, String> requestBody = parseEstadoRequest(request);
        String nuevoEstado = requestBody.get("nuevoEstado");

        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            sendBadRequest(response, "El campo 'nuevoEstado' es requerido");
            return;
        }

        logger.debug("📄 Nuevo estado solicitado: '{}'", nuevoEstado);

        // ✅ Actualizar estado usando el servicio
        boolean actualizado = solicitudService.actualizarEstadoSolicitud(
            solicitudId, profesionalId, nuevoEstado
        );

        if (actualizado) {
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("solicitudId", solicitudId);
            responseData.put("nuevoEstado", nuevoEstado.toLowerCase());
            responseData.put("mensaje", "Estado actualizado exitosamente");

            JsonResponse jsonResponse = JsonResponse.success(
                "Estado actualizado a '" + nuevoEstado + "'",
                responseData
            );

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(jsonResponse));

            logger.info("✅ Estado de solicitud {} actualizado a '{}'", solicitudId, nuevoEstado);
        } else {
            sendBadRequest(response, "No se pudo actualizar el estado de la solicitud");
        }
    }

    private SolicitudServicioRequest parseRequest(HttpServletRequest request) 
            throws IOException, JsonSyntaxException {
        
        StringBuilder sb = new StringBuilder();
        String line;
        
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        String json = sb.toString();
        
        if (json.trim().isEmpty()) {
            throw new JsonSyntaxException("Request body vacío");
        }
        
        return gson.fromJson(json, SolicitudServicioRequest.class);
    }

    /**
     * Parsea request body para obtener el nuevo estado.
     * ACTUALIZADO 2025-12-03: Agregado para endpoint de actualización de estado
     */
    private Map<String, String> parseEstadoRequest(HttpServletRequest request)
            throws IOException, JsonSyntaxException {

        StringBuilder sb = new StringBuilder();
        String line;

        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String json = sb.toString();

        if (json.trim().isEmpty()) {
            throw new JsonSyntaxException("Request body vacío");
        }

        // Parsear como Map genérico
        @SuppressWarnings("unchecked")
        Map<String, String> map = gson.fromJson(json, Map.class);

        return map;
    }

    private Integer obtenerUsuarioId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object userId = session.getAttribute("userId");
            if (userId instanceof Integer) {
                return (Integer) userId;
            }
        }
        
        // Alternativa: extraer del token JWT en el header
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // TODO: Decodificar JWT y extraer userId
            // return jwtUtil.extractUserId(authHeader.substring(7));
        }
        
        return null;
    }
    
    private String generarCodigoSolicitud(Integer id) {
        return String.format("SR-%d-%06d", 
                           java.time.Year.now().getValue(), 
                           id);
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
    
    private void handleJsonError(HttpServletResponse response, JsonSyntaxException e, 
                                long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.warn("✗ Error de JSON: {} - Tiempo: {}ms", e.getMessage(), duration);
        
        JsonResponse jsonResponse = JsonResponse.error("Formato de datos inválido");
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
    
    private void sendUnauthorized(HttpServletResponse response, String message) 
            throws IOException {
        JsonResponse jsonResponse = JsonResponse.error(message);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(gson.toJson(jsonResponse));
    }
    
    private void sendBadRequest(HttpServletResponse response, String message)
            throws IOException {
        JsonResponse jsonResponse = JsonResponse.error(message);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private void sendNotFound(HttpServletResponse response, String message)
            throws IOException {
        JsonResponse jsonResponse = JsonResponse.error(message);
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        response.getWriter().write(gson.toJson(jsonResponse));
        logger.warn("⚠️ Recurso no encontrado: {}", message);
    }

    @Override
    public void destroy() {
        logger.info("Destruyendo SolicitudServicioServlet");
        super.destroy();
    }
}