package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.dao.profesional.EspecialidadProfesionalDAO;
import com.contactoprofesionales.dao.profesional.EspecialidadProfesionalDAOImpl;
import com.contactoprofesionales.dto.ModalidadTrabajoDTO;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet para obtener modalidad de trabajo (remoto/presencial) de una especialidad.
 * Se utiliza en el formulario de solicitud de servicio (solicitud-servicio.html)
 * para determinar dinámicamente qué opciones de modalidad habilitar.
 *
 * Endpoint: GET /api/especialidad/modalidad?especialidadId={id}
 *
 * Respuesta JSON:
 * {
 *   "success": true,
 *   "modalidad": {
 *     "especialidadId": 1,
 *     "trabajoRemoto": true,
 *     "trabajoPresencial": true
 *   }
 * }
 *
 * @since Migración V008
 */
@WebServlet(name = "ModalidadTrabajoServlet", urlPatterns = {"/api/especialidad/modalidad"})
public class ModalidadTrabajoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ModalidadTrabajoServlet.class);

    private EspecialidadProfesionalDAO especialidadDAO;
    private Gson gson;

    /**
     * Inicializa el servlet y sus dependencias.
     */
    @Override
    public void init() throws ServletException {
        super.init();
        this.especialidadDAO = new EspecialidadProfesionalDAOImpl();
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
        logger.info("✓ ModalidadTrabajoServlet inicializado correctamente");
    }

    /**
     * Maneja solicitudes GET para obtener modalidad de trabajo.
     * Parámetros requeridos:
     * - especialidadId: ID de la especialidad a consultar
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Configurar respuesta JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Validar parámetro especialidadId
        String especialidadIdStr = request.getParameter("especialidadId");

        if (especialidadIdStr == null || especialidadIdStr.isEmpty()) {
            logger.warn("Solicitud sin parámetro especialidadId");
            enviarError(response, "El parámetro 'especialidadId' es requerido");
            return;
        }

        try {
            // Parsear ID
            int especialidadId = Integer.parseInt(especialidadIdStr);
            logger.debug("Obteniendo modalidad para especialidad ID: {}", especialidadId);

            // Consultar modalidad desde DAO
            ModalidadTrabajoDTO modalidad = especialidadDAO.obtenerModalidadTrabajo(especialidadId);

            if (modalidad == null) {
                logger.warn("No se encontró especialidad con ID: {}", especialidadId);
                enviarError(response, "Especialidad no encontrada");
                return;
            }

            // Respuesta exitosa
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("modalidad", modalidad);

            logger.info("Modalidad obtenida exitosamente para especialidad {}: Remoto={}, Presencial={}",
                       especialidadId, modalidad.getTrabajoRemoto(), modalidad.getTrabajoPresencial());

            try (PrintWriter out = response.getWriter()) {
                out.print(gson.toJson(respuesta));
                out.flush();
            }

        } catch (NumberFormatException e) {
            logger.error("ID de especialidad inválido: {}", especialidadIdStr, e);
            enviarError(response, "ID de especialidad inválido");

        } catch (DatabaseException e) {
            logger.error("Error al obtener modalidad de trabajo", e);
            enviarError(response, "Error al obtener modalidad: " + e.getMessage());

        } catch (Exception e) {
            logger.error("Error inesperado al procesar solicitud", e);
            enviarError(response, "Error interno del servidor");
        }
    }

    /**
     * Envía respuesta de error en formato JSON.
     *
     * @param response Objeto HttpServletResponse
     * @param mensaje  Mensaje de error para el cliente
     * @throws IOException Si hay error al escribir la respuesta
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
