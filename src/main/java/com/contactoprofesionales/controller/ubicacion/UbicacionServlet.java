package com.contactoprofesionales.controller.ubicacion;

import com.contactoprofesionales.dto.DepartamentoDTO;
import com.contactoprofesionales.dto.DistritoDTO;
import com.contactoprofesionales.dto.ProvinciaDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.service.ubicacion.UbicacionService;
import com.contactoprofesionales.service.ubicacion.UbicacionServiceImpl;
import com.contactoprofesionales.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet para gestionar las operaciones de ubicación geográfica
 * Endpoints:
 * GET /api/ubicacion/departamentos - Listar todos los departamentos
 * GET /api/ubicacion/provincias?departamentoId={id} - Listar provincias por departamento
 * GET /api/ubicacion/distritos?provinciaId={id} - Listar distritos por provincia
 * GET /api/ubicacion/distritos/buscar?nombre={nombre} - Buscar distritos por nombre
 */
@WebServlet(name = "UbicacionServlet", urlPatterns = {"/api/ubicacion/*"})
public class UbicacionServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(UbicacionServlet.class);
    private static final long serialVersionUID = 1L;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private UbicacionService ubicacionService;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando UbicacionServlet ===");
        this.ubicacionService = new UbicacionServiceImpl();
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
     * GET - Operaciones de consulta de ubicación geográfica
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();

        logger.info("=== GET /api/ubicacion - PathInfo: {}", pathInfo);

        try {
            // GET /api/ubicacion/departamentos - Listar todos los departamentos
            if ("/departamentos".equals(pathInfo)) {
                listarDepartamentos(response);
            }
            // GET /api/ubicacion/provincias?departamentoId={id} - Listar provincias por departamento
            else if ("/provincias".equals(pathInfo)) {
                String deptIdParam = request.getParameter("departamentoId");
                if (deptIdParam == null || deptIdParam.trim().isEmpty()) {
                    logger.error("Parámetro departamentoId no proporcionado");
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "PARAMETRO_REQUERIDO", "El parámetro 'departamentoId' es requerido");
                    return;
                }
                Integer departamentoId = Integer.parseInt(deptIdParam);
                listarProvinciasPorDepartamento(response, departamentoId);
            }
            // GET /api/ubicacion/distritos?provinciaId={id} - Listar distritos por provincia
            else if ("/distritos".equals(pathInfo)) {
                String provIdParam = request.getParameter("provinciaId");
                if (provIdParam == null || provIdParam.trim().isEmpty()) {
                    logger.error("Parámetro provinciaId no proporcionado");
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "PARAMETRO_REQUERIDO", "El parámetro 'provinciaId' es requerido");
                    return;
                }
                Integer provinciaId = Integer.parseInt(provIdParam);
                listarDistritosPorProvincia(response, provinciaId);
            }
            // GET /api/ubicacion/distritos/buscar?nombre={nombre} - Buscar distritos por nombre
            else if ("/distritos/buscar".equals(pathInfo)) {
                String nombre = request.getParameter("nombre");
                if (nombre == null || nombre.trim().isEmpty()) {
                    logger.error("Parámetro nombre no proporcionado");
                    sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                        "PARAMETRO_REQUERIDO", "El parámetro 'nombre' es requerido");
                    return;
                }
                buscarDistritos(response, nombre);
            }
            else {
                logger.error("Ruta no válida: {}", pathInfo);
                sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                    "RUTA_INVALIDA", "La ruta solicitada no existe");
            }

        } catch (NumberFormatException e) {
            logger.error("ID inválido en GET: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "ID_INVALIDO", "El ID proporcionado no es válido");
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
     * Listar todos los departamentos
     */
    private void listarDepartamentos(HttpServletResponse response)
            throws DatabaseException, IOException {
        logger.debug("Listando todos los departamentos");
        List<DepartamentoDTO> departamentos = ubicacionService.listarDepartamentos();
        logger.info("Se encontraron {} departamentos", departamentos.size());
        sendSuccessResponse(response, HttpServletResponse.SC_OK, departamentos);
    }

    /**
     * Listar provincias por departamento
     */
    private void listarProvinciasPorDepartamento(HttpServletResponse response, Integer departamentoId)
            throws ValidationException, DatabaseException, IOException {
        logger.debug("Listando provincias del departamento ID: {}", departamentoId);
        List<ProvinciaDTO> provincias = ubicacionService.listarProvinciasPorDepartamento(departamentoId);
        logger.info("Se encontraron {} provincias para el departamento ID {}", provincias.size(), departamentoId);
        sendSuccessResponse(response, HttpServletResponse.SC_OK, provincias);
    }

    /**
     * Listar distritos por provincia
     */
    private void listarDistritosPorProvincia(HttpServletResponse response, Integer provinciaId)
            throws ValidationException, DatabaseException, IOException {
        logger.debug("Listando distritos de la provincia ID: {}", provinciaId);
        List<DistritoDTO> distritos = ubicacionService.listarDistritosPorProvincia(provinciaId);
        logger.info("Se encontraron {} distritos para la provincia ID {}", distritos.size(), provinciaId);
        sendSuccessResponse(response, HttpServletResponse.SC_OK, distritos);
    }

    /**
     * Buscar distritos por nombre
     */
    private void buscarDistritos(HttpServletResponse response, String nombre)
            throws ValidationException, DatabaseException, IOException {
        logger.debug("Buscando distritos con nombre: {}", nombre);
        List<DistritoDTO> distritos = ubicacionService.buscarDistritos(nombre);
        logger.info("Se encontraron {} distritos que coinciden con '{}'", distritos.size(), nombre);
        sendSuccessResponse(response, HttpServletResponse.SC_OK, distritos);
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
