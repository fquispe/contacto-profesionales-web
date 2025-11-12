package com.contactoprofesionales.controller;

import com.contactoprofesionales.dao.CategoriaServicioDAO;
import com.contactoprofesionales.dao.CategoriaServicioDAOImpl;
import com.contactoprofesionales.model.CategoriaServicio;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servlet para obtener categorías de servicio.
 * Endpoint:
 * - GET /api/categorias-servicio : Listar todas las categorías activas
 */
@WebServlet("/api/categorias-servicio")
public class CategoriaServicioServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServicioServlet.class);
    private CategoriaServicioDAO categoriaDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.categoriaDAO = new CategoriaServicioDAOImpl();

        // Configurar Gson con adaptador para LocalDateTime
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();

        logger.info("CategoriaServicioServlet inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);

        try {
            logger.info("Obteniendo categorías de servicio activas");

            List<CategoriaServicio> categorias = categoriaDAO.listarActivas();

            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", "Categorías obtenidas exitosamente");
            jsonResponse.add("data", gson.toJsonTree(categorias));

            enviarRespuesta(response, 200, jsonResponse);

        } catch (Exception e) {
            logger.error("Error obteniendo categorías", e);
            enviarError(response, 500, "Error interno del servidor: " + e.getMessage());
        }
    }

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // =====================================================================
    // MÉTODOS AUXILIARES
    // =====================================================================

    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void enviarRespuesta(HttpServletResponse response, int status, JsonObject jsonResponse) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", mensaje);

        enviarRespuesta(response, status, jsonResponse);
    }

    // Adaptador JSON para LocalDateTime
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(formatter));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }
}
