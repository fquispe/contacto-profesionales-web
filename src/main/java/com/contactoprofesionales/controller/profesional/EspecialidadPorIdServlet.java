package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.util.DatabaseConnection;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet para obtener información de una especialidad por ID
 * Endpoint: GET /api/especialidad/{id}
 */
@WebServlet(name = "EspecialidadPorIdServlet", urlPatterns = {"/api/especialidad"})
public class EspecialidadPorIdServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(EspecialidadPorIdServlet.class);

    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        logger.info("✓ EspecialidadPorIdServlet inicializado correctamente");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String especialidadIdStr = request.getParameter("id");

        if (especialidadIdStr == null || especialidadIdStr.isEmpty()) {
            enviarError(response, "El parámetro 'id' es requerido");
            return;
        }

        try {
            int especialidadId = Integer.parseInt(especialidadIdStr);
            Map<String, Object> especialidad = obtenerEspecialidadPorId(especialidadId);

            if (especialidad == null) {
                enviarError(response, "Especialidad no encontrada");
                return;
            }

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("especialidad", especialidad);

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(respuesta));
                out.flush();
            }

        } catch (NumberFormatException e) {
            logger.error("ID de especialidad inválido: {}", especialidadIdStr, e);
            enviarError(response, "ID de especialidad inválido");
        } catch (Exception e) {
            logger.error("Error al obtener especialidad", e);
            enviarError(response, "Error al obtener especialidad: " + e.getMessage());
        }
    }

    /**
     * Obtiene la información de una especialidad por su ID
     */
    private Map<String, Object> obtenerEspecialidadPorId(int especialidadId) throws DatabaseException {
        String sql = "SELECT id, profesional_id, servicio_profesional, categoria_id " +
                    "FROM especialidades_profesional " +
                    "WHERE id = ? AND activo = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, especialidadId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> especialidad = new HashMap<>();
                    especialidad.put("id", rs.getInt("id"));
                    especialidad.put("profesionalId", rs.getInt("profesional_id"));
                    especialidad.put("servicioProfesional", rs.getString("servicio_profesional"));
                    especialidad.put("categoriaId", rs.getInt("categoria_id"));
                    return especialidad;
                }
            }

            return null;

        } catch (Exception e) {
            logger.error("Error al obtener especialidad por ID: {}", especialidadId, e);
            throw new DatabaseException("Error al obtener especialidad", e);
        }
    }

    /**
     * Envía una respuesta de error en formato JSON
     */
    private void enviarError(HttpServletResponse response, String mensaje) throws IOException {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", mensaje);

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(errorResponse));
            out.flush();
        }
    }
}
