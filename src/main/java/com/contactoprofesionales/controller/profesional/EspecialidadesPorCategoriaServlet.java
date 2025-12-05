package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.dao.profesional.EspecialidadProfesionalDAO;
import com.contactoprofesionales.dao.profesional.EspecialidadProfesionalDAOImpl;
import com.contactoprofesionales.exception.DatabaseException;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.contactoprofesionales.util.DatabaseConnection;

/**
 * Servlet para obtener especialidades filtradas por categoría
 * Endpoint:
 * GET /api/especialidades-por-categoria?categoriaId={id} - Lista especialidades por categoría
 * GET /api/especialidades-por-categoria - Lista todas las especialidades únicas
 */
@WebServlet(name = "EspecialidadesPorCategoriaServlet", urlPatterns = {"/api/especialidades-por-categoria"})
public class EspecialidadesPorCategoriaServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(EspecialidadesPorCategoriaServlet.class);

    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        logger.info("✓ EspecialidadesPorCategoriaServlet inicializado correctamente");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String categoriaIdStr = request.getParameter("categoriaId");

        logger.debug("Solicitud de especialidades - categoriaId: {}", categoriaIdStr);

        try {
            List<String> especialidades;

            if (categoriaIdStr != null && !categoriaIdStr.isEmpty()) {
                // Obtener especialidades filtradas por categoría
                Integer categoriaId = Integer.parseInt(categoriaIdStr);
                especialidades = obtenerEspecialidadesPorCategoria(categoriaId);
                logger.info("Especialidades obtenidas para categoría {}: {}", categoriaId, especialidades.size());
            } else {
                // Obtener todas las especialidades únicas
                especialidades = obtenerTodasEspecialidades();
                logger.info("Todas las especialidades obtenidas: {}", especialidades.size());
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("especialidades", especialidades);
            respuesta.put("total", especialidades.size());

            enviarRespuestaExitosa(response, respuesta);

        } catch (NumberFormatException e) {
            logger.error("ID de categoría inválido: {}", categoriaIdStr);
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST,
                       "El ID de categoría debe ser un número válido");
        } catch (DatabaseException e) {
            logger.error("Error de base de datos: {}", e.getMessage(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                       "Error al obtener especialidades");
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                       "Error interno del servidor");
        }
    }

    /**
     * Obtiene especialidades únicas filtradas por categoría
     */
    private List<String> obtenerEspecialidadesPorCategoria(Integer categoriaId) throws DatabaseException {
        String sql = "SELECT DISTINCT ep.servicio_profesional " +
                    "FROM especialidades_profesional ep " +
                    "INNER JOIN profesionales p ON ep.profesional_id = p.id " +
                    "WHERE ep.categoria_id = ? " +
                    "AND ep.activo = true " +
                    "AND p.activo = true " +
                    "AND ep.servicio_profesional IS NOT NULL " +
                    "AND TRIM(ep.servicio_profesional) != '' " +
                    "ORDER BY ep.servicio_profesional ASC";

        List<String> especialidades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, categoriaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    especialidades.add(rs.getString("servicio_profesional"));
                }
            }

            return especialidades;

        } catch (Exception e) {
            logger.error("Error al obtener especialidades por categoría: {}", e.getMessage(), e);
            throw new DatabaseException("Error al obtener especialidades por categoría", e);
        }
    }

    /**
     * Obtiene todas las especialidades únicas (sin filtro de categoría)
     */
    private List<String> obtenerTodasEspecialidades() throws DatabaseException {
        String sql = "SELECT DISTINCT ep.servicio_profesional " +
                    "FROM especialidades_profesional ep " +
                    "INNER JOIN profesionales p ON ep.profesional_id = p.id " +
                    "WHERE ep.activo = true " +
                    "AND p.activo = true " +
                    "AND ep.servicio_profesional IS NOT NULL " +
                    "AND TRIM(ep.servicio_profesional) != '' " +
                    "ORDER BY ep.servicio_profesional ASC";

        List<String> especialidades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                especialidades.add(rs.getString("servicio_profesional"));
            }

            return especialidades;

        } catch (Exception e) {
            logger.error("Error al obtener todas las especialidades: {}", e.getMessage(), e);
            throw new DatabaseException("Error al obtener especialidades", e);
        }
    }

    private void enviarRespuestaExitosa(HttpServletResponse response, Object data)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }

    private void enviarError(HttpServletResponse response, int statusCode, String mensaje)
            throws IOException {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", mensaje);
        error.put("statusCode", statusCode);

        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(error));
        out.flush();
    }
}
