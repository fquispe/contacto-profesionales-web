package com.contactoprofesionales.controller.categoria;

import com.contactoprofesionales.dao.categoria.CategoriaServicioDAO;
import com.contactoprofesionales.dao.categoria.CategoriaServicioDAOImpl;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.CategoriaServicio;
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
 * Servlet para gestionar las operaciones de categorías de servicio
 * Endpoints:
 * GET /api/categorias - Listar todas las categorías activas
 */
@WebServlet(name = "CategoriaServlet", urlPatterns = {"/api/categorias"})
public class CategoriaServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServlet.class);
    private static final long serialVersionUID = 1L;

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

    private CategoriaServicioDAO categoriaDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando CategoriaServlet ===");
        this.categoriaDAO = new CategoriaServicioDAOImpl();
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
     * GET - Listar todas las categorías activas
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        logger.info("=== GET /api/categorias - Listar categorías activas ===");

        try {
            logger.debug("Listando todas las categorías activas");
            List<CategoriaServicio> categorias = categoriaDAO.listarActivas();
            logger.info("Se encontraron {} categorías activas", categorias.size());

            sendSuccessResponse(response, HttpServletResponse.SC_OK, categorias);

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
