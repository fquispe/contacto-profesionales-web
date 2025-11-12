package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.dto.EspecialidadDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.service.profesional.EspecialidadService;
import com.contactoprofesionales.service.profesional.EspecialidadServiceImpl;
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
 * Servlet para gestionar las especialidades de profesionales
 * Endpoints:
 * GET    /api/profesionales/{profId}/especialidades - Listar especialidades del profesional
 * POST   /api/profesionales/{profId}/especialidades - Agregar especialidad
 * PUT    /api/profesionales/{profId}/especialidades/{id}/principal - Marcar como principal
 * DELETE /api/profesionales/{profId}/especialidades/{id} - Eliminar especialidad
 */
@WebServlet(name = "EspecialidadServlet", urlPatterns = {"/api/profesionales/*/especialidades", "/api/profesionales/*/especialidades/*"})
public class EspecialidadServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(EspecialidadServlet.class);
    private static final long serialVersionUID = 1L;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private EspecialidadService especialidadService;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando EspecialidadServlet ===");
        this.especialidadService = new EspecialidadServiceImpl();
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
     * GET - Listar especialidades de un profesional
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        logger.info("=== GET /api/profesionales/.../especialidades - PathInfo: {}", pathInfo);

        try {
            Integer profesionalId = extractProfesionalIdFromPath(request.getRequestURI());
            logger.debug("Listando especialidades del profesional ID: {}", profesionalId);

            List<EspecialidadDTO> especialidades = especialidadService.listarPorProfesional(profesionalId);
            logger.info("Se encontraron {} especialidades para el profesional ID {}",
                especialidades.size(), profesionalId);

            sendSuccessResponse(response, HttpServletResponse.SC_OK, especialidades);

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
     * POST - Agregar nueva especialidad a un profesional
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("=== Iniciando POST /api/profesionales/.../especialidades - Agregar Especialidad ===");

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = extractProfesionalIdFromPath(request.getRequestURI());
            logger.info("Agregando especialidad al profesional ID: {}", profesionalId);

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

            Integer categoriaId = jsonObject.get("categoriaId").getAsInt();
            Integer aniosExperiencia = jsonObject.has("aniosExperiencia") && !jsonObject.get("aniosExperiencia").isJsonNull()
                ? jsonObject.get("aniosExperiencia").getAsInt() : null;
            String descripcion = jsonObject.has("descripcion") && !jsonObject.get("descripcion").isJsonNull()
                ? jsonObject.get("descripcion").getAsString() : null;
            Double costo = jsonObject.has("costo") && !jsonObject.get("costo").isJsonNull()
                ? jsonObject.get("costo").getAsDouble() : null;
            String tipoCosto = jsonObject.has("tipoCosto") && !jsonObject.get("tipoCosto").isJsonNull()
                ? jsonObject.get("tipoCosto").getAsString() : null;
            Boolean incluyeMateriales = jsonObject.has("incluyeMateriales") && !jsonObject.get("incluyeMateriales").isJsonNull()
                ? jsonObject.get("incluyeMateriales").getAsBoolean() : false;
            Integer orden = jsonObject.has("orden") && !jsonObject.get("orden").isJsonNull()
                ? jsonObject.get("orden").getAsInt() : 1;
            Boolean esPrincipal = jsonObject.has("esPrincipal") && !jsonObject.get("esPrincipal").isJsonNull()
                ? jsonObject.get("esPrincipal").getAsBoolean() : false;

            logger.debug("Datos parseados - categoriaId: {}, aniosExp: {}, costo: {}, tipoCosto: {}, orden: {}, esPrincipal: {}",
                categoriaId, aniosExperiencia, costo, tipoCosto, orden, esPrincipal);

            // Agregar especialidad
            EspecialidadDTO especialidadCreada = especialidadService.agregar(
                profesionalId, categoriaId, aniosExperiencia, descripcion, costo, tipoCosto,
                incluyeMateriales, orden, esPrincipal);

            logger.info("✓ Especialidad agregada exitosamente con ID: {}", especialidadCreada.getId());

            sendSuccessResponse(response, HttpServletResponse.SC_CREATED, especialidadCreada);

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
     * PUT - Marcar especialidad como principal
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("=== Iniciando PUT /api/profesionales/.../especialidades/... - Marcar como Principal ===");

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            Integer profesionalId = extractProfesionalIdFromPath(request.getRequestURI());

            // Validar que la ruta termine en "/principal"
            if (pathInfo == null || !pathInfo.contains("/principal")) {
                logger.error("Ruta PUT inválida: {}", pathInfo);
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "RUTA_INVALIDA", "Para actualizar una especialidad debe usar la ruta: /api/profesionales/{profId}/especialidades/{id}/principal");
                return;
            }

            Integer especialidadId = extractEspecialidadIdFromPath(pathInfo);
            logger.info("Marcando como principal especialidad ID: {} del profesional ID: {}",
                especialidadId, profesionalId);

            // Marcar como principal
            boolean actualizado = especialidadService.marcarComoPrincipal(especialidadId, profesionalId);

            if (actualizado) {
                logger.info("✓ Especialidad marcada como principal exitosamente");
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("mensaje", "Especialidad marcada como principal exitosamente");
                responseData.put("id", especialidadId);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, responseData);
            } else {
                logger.error("No se pudo marcar la especialidad como principal");
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ERROR_ACTUALIZAR", "No se pudo marcar la especialidad como principal");
            }

        } catch (NumberFormatException e) {
            logger.error("ID inválido en PUT: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "ID_INVALIDO", "El ID proporcionado no es válido");
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
     * DELETE - Eliminar especialidad de un profesional
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("=== Iniciando DELETE /api/profesionales/.../especialidades/... - Eliminar Especialidad ===");

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                logger.error("ID de especialidad no proporcionado en DELETE");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "ID_REQUERIDO", "Se requiere el ID de la especialidad en la URL");
                return;
            }

            Integer especialidadId = extractEspecialidadIdFromPath(pathInfo);
            logger.info("Eliminando especialidad ID: {}", especialidadId);

            // Eliminar especialidad
            boolean eliminado = especialidadService.eliminar(especialidadId);

            if (eliminado) {
                logger.info("✓ Especialidad eliminada exitosamente: ID {}", especialidadId);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("mensaje", "Especialidad eliminada exitosamente");
                responseData.put("id", especialidadId);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, responseData);
            } else {
                logger.error("No se pudo eliminar la especialidad ID: {}", especialidadId);
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "ERROR_ELIMINAR", "No se pudo eliminar la especialidad");
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
     * Ejemplo: /api/profesionales/123/especialidades -> 123
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
     * Extrae el ID de la especialidad del pathInfo
     * Ejemplo: /123 -> 123
     * Ejemplo: /123/principal -> 123
     */
    private Integer extractEspecialidadIdFromPath(String pathInfo) {
        String[] parts = pathInfo.split("/");
        // El ID está en la posición 1 (después del primer /)
        if (parts.length >= 2) {
            return Integer.parseInt(parts[1]);
        }
        throw new NumberFormatException("No se pudo extraer el ID de la especialidad del pathInfo");
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
