package com.contactoprofesionales.controller.perfil;

import com.contactoprofesionales.dao.certificaciones.CertificacionesProfesionalDAO;
import com.contactoprofesionales.dao.certificaciones.CertificacionesProfesionalDAOImpl;
import com.contactoprofesionales.model.CertificacionProfesional;
import com.contactoprofesionales.util.JsonResponse;
import com.contactoprofesionales.util.GsonUtil;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/**
 * Servlet para gestión de certificaciones profesionales.
 *
 * Endpoint: /api/profesional/certificaciones
 *
 * Métodos:
 * - GET: Lista todas las certificaciones del profesional autenticado
 * - POST: Crea una nueva certificación
 * - PUT: Actualiza una certificación existente
 * - DELETE: Elimina una certificación (soft delete)
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
@WebServlet(name = "CertificacionesProfesionalServlet", urlPatterns = {"/api/profesional/certificaciones"})
public class CertificacionesProfesionalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(CertificacionesProfesionalServlet.class);

    private final Gson gson;
    private CertificacionesProfesionalDAO certificacionesDAO;

    public CertificacionesProfesionalServlet() {
        // ✅ Usar GsonUtil que incluye adaptadores para LocalDateTime y LocalDate
        this.gson = GsonUtil.createGson();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando CertificacionesProfesionalServlet ===");

        try {
            this.certificacionesDAO = new CertificacionesProfesionalDAOImpl();
            logger.info("✓ CertificacionesProfesionalServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar CertificacionesProfesionalServlet", e);
            throw new ServletException("Error al inicializar CertificacionesProfesionalServlet", e);
        }
    }

    /**
     * GET: Lista todas las certificaciones del profesional.
     *
     * URL: /api/profesional/certificaciones
     * Headers: Authorization: Bearer <token>
     *
     * Response 200:
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "nombreCertificacion": "Certificación AWS",
     *       "institucion": "Amazon Web Services",
     *       "fechaObtencion": "2024-01-15",
     *       ...
     *     }
     *   ]
     * }
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("GET /api/profesional/certificaciones - Listando certificaciones");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // ✅ TODO: Obtener profesionalId del token JWT
            // Por ahora usamos un ID de prueba
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                logger.warn("✗ No se pudo obtener el profesional del token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            // Listar certificaciones
            List<CertificacionProfesional> certificaciones = certificacionesDAO.listarPorProfesional(profesionalId);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Certificaciones listadas exitosamente ({} encontradas) - Tiempo: {}ms",
                    certificaciones.size(), duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success("Certificaciones obtenidas", certificaciones)));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * POST: Crea una nueva certificación.
     *
     * Request Body:
     * {
     *   "nombreCertificacion": "Certificación AWS",
     *   "institucion": "Amazon Web Services",
     *   "fechaObtencion": "2024-01-15",
     *   "fechaVigencia": "2027-01-15",
     *   "documentoUrl": "/uploads/certificados/cert_aws_123.pdf",
     *   "descripcion": "Certificación de arquitecto de soluciones"
     * }
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("POST /api/profesional/certificaciones - Creando certificación");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                logger.warn("✗ No autenticado");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            // Parsear JSON
            CertificacionProfesional certificacion = gson.fromJson(request.getReader(), CertificacionProfesional.class);
            certificacion.setProfesionalId(profesionalId);

            // Validar datos requeridos
            if (certificacion.getNombreCertificacion() == null || certificacion.getNombreCertificacion().trim().isEmpty()) {
                logger.warn("✗ Nombre de certificación es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("El nombre de la certificación es requerido")));
                return;
            }

            if (certificacion.getInstitucion() == null || certificacion.getInstitucion().trim().isEmpty()) {
                logger.warn("✗ Institución es requerida");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("La institución es requerida")));
                return;
            }

            // Guardar certificación
            Integer id = certificacionesDAO.guardar(certificacion);
            certificacion.setId(id);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Certificación creada exitosamente con ID {} - Tiempo: {}ms", id, duration);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(JsonResponse.success("Certificación creada exitosamente", certificacion)));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * PUT: Actualiza una certificación existente.
     *
     * Request Body:
     * {
     *   "id": 1,
     *   "nombreCertificacion": "Certificación AWS Actualizada",
     *   ...
     * }
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("PUT /api/profesional/certificaciones - Actualizando certificación");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            CertificacionProfesional certificacion = gson.fromJson(request.getReader(), CertificacionProfesional.class);
            certificacion.setProfesionalId(profesionalId);

            if (certificacion.getId() == null) {
                logger.warn("✗ ID de certificación es requerido para actualizar");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido para actualizar")));
                return;
            }

            boolean actualizado = certificacionesDAO.actualizar(certificacion);

            if (actualizado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Certificación actualizada exitosamente - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Certificación actualizada exitosamente", certificacion)));
            } else {
                logger.warn("✗ Certificación no encontrada o no pertenece al profesional");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Certificación no encontrada")));
            }

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * DELETE: Elimina (soft delete) una certificación.
     *
     * URL: /api/profesional/certificaciones?id=1
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("DELETE /api/profesional/certificaciones - Eliminando certificación");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                logger.warn("✗ ID de certificación es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido")));
                return;
            }

            Integer id = Integer.parseInt(idParam);

            boolean eliminado = certificacionesDAO.eliminar(id);

            if (eliminado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Certificación eliminada exitosamente - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Certificación eliminada exitosamente")));
            } else {
                logger.warn("✗ Certificación no encontrada");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Certificación no encontrada")));
            }

        } catch (NumberFormatException e) {
            logger.warn("✗ ID de certificación inválido", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(JsonResponse.error("ID inválido")));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * Obtiene el ID del profesional del token JWT.
     * ✅ TODO: Implementar extracción real del token JWT
     *
     * @param request Request HTTP
     * @return ID del profesional o null si no está autenticado
     */
    private Integer obtenerProfesionalIdDeToken(HttpServletRequest request) {
        // ✅ TODO: Implementar extracción real del token JWT
        // Por ahora retornamos un ID de prueba
        // En producción, extraer del header Authorization: Bearer <token>
        // y decodificar el JWT para obtener el profesionalId

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // String token = authHeader.substring(7);
            // return jwtService.extractProfesionalId(token);
            // Por ahora retornamos ID de prueba
            return 1;
        }

        return null;
    }

    /**
     * Maneja errores genéricos del servlet.
     */
    private void handleError(HttpServletResponse response, Exception e, long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.error("✗ Error procesando solicitud - Tiempo: {}ms", duration, e);

        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(JsonResponse.error("Error interno del servidor: " + e.getMessage())));
    }

    @Override
    public void destroy() {
        logger.info("Destruyendo CertificacionesProfesionalServlet");
        super.destroy();
    }
}
