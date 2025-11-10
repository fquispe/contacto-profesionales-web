package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.dto.RedSocialDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.service.profesional.RedSocialService;
import com.contactoprofesionales.service.profesional.RedSocialServiceImpl;
import com.contactoprofesionales.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet para gestionar las redes sociales de profesionales
 * Endpoints:
 * GET    /api/profesionales/{profId}/redes-sociales - Listar redes sociales del profesional
 * POST   /api/profesionales/{profId}/redes-sociales - Agregar red social
 * PUT    /api/profesionales/{profId}/redes-sociales/{id} - Actualizar URL de red social
 * DELETE /api/profesionales/{profId}/redes-sociales/{id} - Eliminar red social
 */
@WebServlet(name = "RedSocialServlet", urlPatterns = {"/api/profesionales/*/redes-sociales", "/api/profesionales/*/redes-sociales/*"})
public class RedSocialServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RedSocialServlet.class);
    private static final long serialVersionUID = 1L;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private RedSocialService redSocialService;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando RedSocialServlet ===");
        this.redSocialService = new RedSocialServiceImpl();
    }

    /**
     * Configurar headers CORS para todas las peticiones
     */
    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    /**
     * Manejar peticiones OPTIONS (preflight CORS)
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * GET - Listar redes sociales de un profesional
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        logger.info("=== GET /api/profesionales/.../redes-sociales - PathInfo: {}", pathInfo);

        try {
            Integer profesionalId = extractProfesionalIdFromPath(request.getRequestURI());
            logger.debug("Listando redes sociales del profesional ID: {}", profesionalId);

            List<RedSocialDTO> redesSociales = redSocialService.listarPorProfesional(profesionalId);
            logger.info("Se encontraron {} redes sociales para el profesional ID {}",
                redesSociales.size(), profesionalId);

            sendSuccessResponse(response, HttpServletResponse.SC_OK, redesSociales);

        } catch (NumberFormatException e) {
            logger.error("ID de profesional inválido en GET: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "ID_INVALIDO", "El ID del profesional no es válido");
        } catch (ValidationException e) {
            logger.error("ValidationException en GET: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "VALIDACION_ERROR", e.getMessage());
        } catch (DatabaseException e) {
            logger.error("DatabaseException en GET: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_BASE_DATOS", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en GET: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * POST - Agregar nueva red social a un profesional
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("=== Iniciando POST /api/profesionales/.../redes-sociales - Agregar Red Social ===");

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = extractProfesionalIdFromPath(request.getRequestURI());
            logger.info("Agregando red social al profesional ID: {}", profesionalId);

            // Leer el cuerpo de la petición
            String jsonBody = readRequestBody(request);
            logger.debug("JSON recibido: {}", jsonBody);

            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("Cuerpo de la petición vacío");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "BODY_VACIO", "El cuerpo de la petición está vacío");
                return;
            }

            // Parsear JSON
            JsonObject jsonObject = gson.fromJson(jsonBody, JsonObject.class);

            String tipoRed = jsonObject.get("tipoRed").getAsString();
            String url = jsonObject.get("url").getAsString();

            logger.debug("Datos parseados - tipoRed: {}, url: {}", tipoRed, url);

            // Agregar red social
            RedSocialDTO redSocialCreada = redSocialService.agregar(profesionalId, tipoRed, url);

            logger.info("✓ Red social agregada exitosamente con ID: {}", redSocialCreada.getId());

            sendSuccessResponse(response, HttpServletResponse.SC_CREATED, redSocialCreada);

        } catch (NumberFormatException e) {
            logger.error("ID inválido en POST: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "ID_INVALIDO", "El ID proporcionado no es válido");
        } catch (JsonSyntaxException e) {
            logger.error("Error al parsear JSON: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "JSON_INVALIDO", "El formato del JSON no es válido: " + e.getMessage());
        } catch (ValidationException e) {
            logger.error("ValidationException en POST: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "VALIDACION_ERROR", e.getMessage());
        } catch (DatabaseException e) {
            logger.error("DatabaseException en POST: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_BASE_DATOS", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en POST: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * PUT - Actualizar URL de una red social
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("=== Iniciando PUT /api/profesionales/.../redes-sociales/... - Actualizar Red Social ===");

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                logger.error("ID de red social no proporcionado en PUT");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID_REQUERIDO", "Se requiere el ID de la red social en la URL");
                return;
            }

            Integer redSocialId = extractRedSocialIdFromPath(pathInfo);
            logger.info("Actualizando red social ID: {}", redSocialId);

            // Leer el cuerpo de la petición
            String jsonBody = readRequestBody(request);
            logger.debug("JSON recibido: {}", jsonBody);

            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("Cuerpo de la petición vacío");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "BODY_VACIO", "El cuerpo de la petición está vacío");
                return;
            }

            // Parsear JSON
            JsonObject jsonObject = gson.fromJson(jsonBody, JsonObject.class);
            String url = jsonObject.get("url").getAsString();

            logger.debug("Nueva URL: {}", url);

            // Actualizar red social
            RedSocialDTO redSocialActualizada = redSocialService.actualizar(redSocialId, url);

            logger.info("✓ Red social actualizada exitosamente: ID {}", redSocialId);

            sendSuccessResponse(response, HttpServletResponse.SC_OK, redSocialActualizada);

        } catch (NumberFormatException e) {
            logger.error("ID inválido en PUT: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "ID_INVALIDO", "El ID proporcionado no es válido");
        } catch (JsonSyntaxException e) {
            logger.error("Error al parsear JSON en PUT: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "JSON_INVALIDO", "El formato del JSON no es válido: " + e.getMessage());
        } catch (ValidationException e) {
            logger.error("ValidationException en PUT: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "VALIDACION_ERROR", e.getMessage());
        } catch (DatabaseException e) {
            logger.error("DatabaseException en PUT: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_BASE_DATOS", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en PUT: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * DELETE - Eliminar red social de un profesional
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("=== Iniciando DELETE /api/profesionales/.../redes-sociales/... - Eliminar Red Social ===");

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                logger.error("ID de red social no proporcionado en DELETE");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID_REQUERIDO", "Se requiere el ID de la red social en la URL");
                return;
            }

            Integer redSocialId = extractRedSocialIdFromPath(pathInfo);
            logger.info("Eliminando red social ID: {}", redSocialId);

            // Eliminar red social
            boolean eliminado = redSocialService.eliminar(redSocialId);

            if (eliminado) {
                logger.info("✓ Red social eliminada exitosamente: ID {}", redSocialId);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("mensaje", "Red social eliminada exitosamente");
                responseData.put("id", redSocialId);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, responseData);
            } else {
                logger.error("No se pudo eliminar la red social ID: {}", redSocialId);
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ERROR_ELIMINAR", "No se pudo eliminar la red social");
            }

        } catch (NumberFormatException e) {
            logger.error("ID inválido en DELETE: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "ID_INVALIDO", "El ID proporcionado no es válido");
        } catch (ValidationException e) {
            logger.error("ValidationException en DELETE: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "VALIDACION_ERROR", e.getMessage());
        } catch (DatabaseException e) {
            logger.error("DatabaseException en DELETE: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_BASE_DATOS", e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado en DELETE: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }

    /**
     * Extrae el ID del profesional de la URI
     * Ejemplo: /api/profesionales/123/redes-sociales -> 123
     */
    private Integer extractProfesionalIdFromPath(String requestURI) {
        String[] parts = requestURI.split("/");
        // Buscar "profesionales" y tomar el siguiente elemento
        for (int i = 0; i < parts.length - 1; i++) {
            if ("profesionales".equals(parts[i])) {
                return Integer.parseInt(parts[i + 1]);
            }
        }
        throw new NumberFormatException("No se pudo extraer el ID del profesional de la URI");
    }

    /**
     * Extrae el ID de la red social del pathInfo
     * Ejemplo: /123 -> 123
     */
    private Integer extractRedSocialIdFromPath(String pathInfo) {
        String[] parts = pathInfo.split("/");
        // El ID está en la posición 1 (después del primer /)
        if (parts.length >= 2) {
            return Integer.parseInt(parts[1]);
        }
        throw new NumberFormatException("No se pudo extraer el ID de la red social del pathInfo");
    }

    /**
     * Lee el cuerpo de la petición como String
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }

    /**
     * Envía una respuesta exitosa
     */
    private void sendSuccessResponse(HttpServletResponse response, int statusCode, Object data)
            throws IOException {
        response.setStatus(statusCode);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("data", data);

        String jsonResponse = gson.toJson(responseBody);
        logger.debug("Enviando respuesta exitosa: {}", jsonResponse);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }

    /**
     * Envía una respuesta de error genérica
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode,
                                   String codigo, String mensaje) throws IOException {
        response.setStatus(statusCode);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);

        Map<String, String> error = new HashMap<>();
        error.put("codigo", codigo);
        error.put("mensaje", mensaje);

        responseBody.put("error", error);

        String jsonResponse = gson.toJson(responseBody);
        logger.debug("Enviando respuesta de error: {}", jsonResponse);

        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}
