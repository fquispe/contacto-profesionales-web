package com.contactoprofesionales.controller.perfil;

import com.contactoprofesionales.dao.redes.RedesSocialesProfesionalDAO;
import com.contactoprofesionales.dao.redes.RedesSocialesProfesionalDAOImpl;
import com.contactoprofesionales.model.RedSocialProfesional;
import com.contactoprofesionales.util.JsonResponse;
import com.contactoprofesionales.util.GsonUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Servlet para gestión de redes sociales del profesional.
 *
 * Endpoint: /api/profesional/redes-sociales
 *
 * Métodos:
 * - GET: Lista todas las redes sociales del profesional autenticado
 * - POST: Crea una nueva red social
 * - PUT: Actualiza una red social existente O actualiza múltiples en una transacción
 * - DELETE: Elimina una red social (soft delete)
 *
 * Notas importantes:
 * - Tipos de redes soportadas (case-insensitive): facebook, instagram, linkedin, twitter, youtube, tiktok, whatsapp, website, otro
 * - Los tipos se normalizan automáticamente a minúsculas antes de guardar en BD
 * - Se puede hacer actualización masiva enviando un array de redes en el PUT
 * - La actualización masiva usa transacciones: desactiva las no enviadas, actualiza existentes, inserta nuevas
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
@WebServlet(name = "RedesSocialesProfesionalServlet", urlPatterns = {"/api/profesional/redes-sociales"})
public class RedesSocialesProfesionalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(RedesSocialesProfesionalServlet.class);

    private final Gson gson;
    private RedesSocialesProfesionalDAO redesDAO;

    public RedesSocialesProfesionalServlet() {
        // ✅ Usar GsonUtil que incluye adaptadores para LocalDateTime y LocalDate
        this.gson = GsonUtil.createGson();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando RedesSocialesProfesionalServlet ===");

        try {
            this.redesDAO = new RedesSocialesProfesionalDAOImpl();
            logger.info("✓ RedesSocialesProfesionalServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar RedesSocialesProfesionalServlet", e);
            throw new ServletException("Error al inicializar RedesSocialesProfesionalServlet", e);
        }
    }

    /**
     * GET: Lista todas las redes sociales del profesional.
     *
     * URL: /api/profesional/redes-sociales
     * Headers: Authorization: Bearer <token>
     *
     * Response 200:
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "tipoRed": "Facebook",
     *       "url": "https://facebook.com/profesional",
     *       "verificada": false,
     *       "activo": true
     *     },
     *     {
     *       "id": 2,
     *       "tipoRed": "Instagram",
     *       "url": "https://instagram.com/profesional",
     *       "verificada": false,
     *       "activo": true
     *     }
     *   ]
     * }
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("GET /api/profesional/redes-sociales - Listando redes sociales");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // ✅ TODO: Obtener profesionalId del token JWT
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                logger.warn("✗ No se pudo obtener el profesional del token");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            // ✅ Listar redes sociales
            List<RedSocialProfesional> redes = redesDAO.listarPorProfesional(profesionalId);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Redes sociales listadas exitosamente ({} encontradas) - Tiempo: {}ms",
                    redes.size(), duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success("Redes sociales obtenidas", redes)));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * POST: Crea una nueva red social.
     *
     * Request Body:
     * {
     *   "tipoRed": "Facebook",  // Se acepta con mayúsculas/minúsculas, se normaliza automáticamente
     *   "url": "https://facebook.com/profesional"
     * }
     *
     * Tipos válidos (case-insensitive): facebook, instagram, linkedin, twitter, youtube, tiktok, whatsapp, website, otro
     *
     * Response 201: Red social creada exitosamente
     * Response 400: Datos inválidos
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("POST /api/profesional/redes-sociales - Creando red social");

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

            // ✅ Parsear JSON de la red social
            RedSocialProfesional red = gson.fromJson(request.getReader(), RedSocialProfesional.class);
            red.setProfesionalId(profesionalId);

            // ✅ Validar datos requeridos
            if (red.getTipoRed() == null || red.getTipoRed().trim().isEmpty()) {
                logger.warn("✗ Tipo de red social es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("El tipo de red social es requerido")));
                return;
            }

            if (red.getUrl() == null || red.getUrl().trim().isEmpty()) {
                logger.warn("✗ URL de la red social es requerida");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("La URL de la red social es requerida")));
                return;
            }

            // ✅ Normalizar tipo de red a minúsculas (la BD solo acepta minúsculas)
            red.setTipoRed(red.getTipoRed().toLowerCase().trim());

            // ✅ Guardar red social
            Integer id = redesDAO.guardar(red);
            red.setId(id);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Red social {} creada exitosamente con ID {} - Tiempo: {}ms",
                    red.getTipoRed(), id, duration);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(JsonResponse.success("Red social creada exitosamente", red)));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * PUT: Actualiza una red social existente O actualiza múltiples en una transacción.
     *
     * Opción 1 - Actualizar una red social individual:
     * {
     *   "id": 1,
     *   "tipoRed": "Facebook",
     *   "url": "https://facebook.com/profesional-actualizado"
     * }
     *
     * Opción 2 - Actualización masiva (recomendado para actualizar el perfil completo):
     * [
     *   {
     *     "id": 1,
     *     "tipoRed": "Facebook",
     *     "url": "https://facebook.com/profesional"
     *   },
     *   {
     *     "tipoRed": "Instagram",
     *     "url": "https://instagram.com/profesional"
     *   }
     * ]
     *
     * Lógica de actualización masiva:
     * - Desactiva las redes que NO vienen en la lista
     * - Actualiza las que vienen con ID
     * - Inserta las que vienen sin ID
     *
     * Response 200: Red(es) social(es) actualizada(s) exitosamente
     * Response 404: Red social no encontrada (solo para actualización individual)
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("PUT /api/profesional/redes-sociales - Actualizando red(es) social(es)");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            // ✅ Leer el cuerpo como String para detectar si es objeto {} o array []
            String requestBody = request.getReader().lines().reduce("", (accumulator, actual) -> accumulator + actual);

            // ✅ Detectar si es actualización masiva (array) o individual (objeto)
            if (requestBody.trim().startsWith("[")) {
                // Actualización masiva
                actualizarMultiples(response, profesionalId, requestBody, startTime);
            } else {
                // Actualización individual
                actualizarIndividual(response, profesionalId, requestBody, startTime);
            }

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * Actualiza una red social individual.
     *
     * @param response Response HTTP
     * @param profesionalId ID del profesional
     * @param requestBody JSON con la red social
     * @param startTime Tiempo de inicio de la solicitud
     */
    private void actualizarIndividual(HttpServletResponse response, Integer profesionalId,
                                     String requestBody, long startTime)
            throws Exception, IOException {

        logger.info("Actualizando red social individual");

        // ✅ Parsear JSON de la red social
        RedSocialProfesional red = gson.fromJson(requestBody, RedSocialProfesional.class);
        red.setProfesionalId(profesionalId);

        // ✅ Validar que tenga ID
        if (red.getId() == null) {
            logger.warn("✗ ID de red social es requerido para actualizar");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido para actualizar")));
            return;
        }

        // ✅ Normalizar tipo de red a minúsculas (la BD solo acepta minúsculas)
        if (red.getTipoRed() != null) {
            red.setTipoRed(red.getTipoRed().toLowerCase().trim());
        }

        // ✅ Actualizar red social
        boolean actualizado = redesDAO.actualizar(red);

        if (actualizado) {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Red social actualizada exitosamente - Tiempo: {}ms", duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success("Red social actualizada exitosamente", red)));
        } else {
            logger.warn("✗ Red social no encontrada o no pertenece al profesional");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(JsonResponse.error("Red social no encontrada")));
        }
    }

    /**
     * Actualiza múltiples redes sociales en una transacción.
     * Lógica similar a la actualización de especialidades:
     * - Desactiva las que NO vienen en la lista
     * - Actualiza las que vienen con ID
     * - Inserta las que vienen sin ID
     *
     * @param response Response HTTP
     * @param profesionalId ID del profesional
     * @param requestBody JSON array con las redes sociales
     * @param startTime Tiempo de inicio de la solicitud
     */
    private void actualizarMultiples(HttpServletResponse response, Integer profesionalId,
                                    String requestBody, long startTime)
            throws Exception, IOException {

        logger.info("Actualizando múltiples redes sociales (actualización masiva)");

        // ✅ Parsear JSON array de redes sociales
        Type listType = new TypeToken<List<RedSocialProfesional>>(){}.getType();
        List<RedSocialProfesional> redes = gson.fromJson(requestBody, listType);

        // ✅ Validar que no esté vacío
        if (redes == null || redes.isEmpty()) {
            logger.warn("✗ La lista de redes sociales no puede estar vacía");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(JsonResponse.error("La lista de redes sociales no puede estar vacía")));
            return;
        }

        // ✅ Normalizar todos los tipos de red a minúsculas (la BD solo acepta minúsculas)
        for (RedSocialProfesional red : redes) {
            if (red.getTipoRed() != null) {
                red.setTipoRed(red.getTipoRed().toLowerCase().trim());
            }
        }

        // ✅ Guardar múltiples redes en transacción
        boolean guardado = redesDAO.guardarMultiples(profesionalId, redes);

        if (guardado) {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ {} redes sociales actualizadas exitosamente (transacción) - Tiempo: {}ms",
                    redes.size(), duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success(
                    "Redes sociales actualizadas exitosamente", redes)));
        } else {
            logger.error("✗ Error al guardar redes sociales");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(JsonResponse.error("Error al actualizar redes sociales")));
        }
    }

    /**
     * DELETE: Elimina (soft delete) una red social.
     *
     * URL: /api/profesional/redes-sociales?id=1
     *
     * Response 200: Red social eliminada exitosamente
     * Response 404: Red social no encontrada
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("DELETE /api/profesional/redes-sociales - Eliminando red social");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            // ✅ Obtener ID del parámetro
            String idParam = request.getParameter("id");
            if (idParam == null || idParam.trim().isEmpty()) {
                logger.warn("✗ ID de red social es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido")));
                return;
            }

            Integer id = Integer.parseInt(idParam);

            // ✅ Eliminar red social (soft delete)
            boolean eliminado = redesDAO.eliminar(id);

            if (eliminado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Red social eliminada exitosamente - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Red social eliminada exitosamente")));
            } else {
                logger.warn("✗ Red social no encontrada");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Red social no encontrada")));
            }

        } catch (NumberFormatException e) {
            logger.warn("✗ ID de red social inválido", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(JsonResponse.error("ID inválido")));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * ✅ ACTUALIZADO 2025-12-04: Obtiene el ID del profesional desde query parameter (localStorage).
     *
     * @param request Request HTTP
     * @return ID del profesional o null si no está presente
     */
    private Integer obtenerProfesionalIdDeToken(HttpServletRequest request) {
        // ✅ Intentar obtener desde query parameter primero (localStorage)
        String profesionalIdParam = request.getParameter("profesionalId");

        if (profesionalIdParam != null && !profesionalIdParam.trim().isEmpty()) {
            try {
                Integer profesionalId = Integer.parseInt(profesionalIdParam);
                logger.debug("ProfesionalId obtenido desde query parameter: {}", profesionalId);
                return profesionalId;
            } catch (NumberFormatException e) {
                logger.warn("profesionalId inválido en query parameter: {}", profesionalIdParam);
            }
        }

        // Fallback: intentar obtener desde sesión HTTP (para compatibilidad)
        jakarta.servlet.http.HttpSession session = request.getSession(false);
        if (session != null) {
            Integer profesionalId = (Integer) session.getAttribute("profesionalId");
            if (profesionalId != null) {
                logger.debug("ProfesionalId obtenido desde sesión HTTP: {}", profesionalId);
                return profesionalId;
            }
        }

        logger.warn("No se pudo obtener profesionalId");
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
        logger.info("Destruyendo RedesSocialesProfesionalServlet");
        super.destroy();
    }
}
