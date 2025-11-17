package com.contactoprofesionales.controller.perfil;

import com.contactoprofesionales.dao.antecedentes.AntecedentesProfesionalDAO;
import com.contactoprofesionales.dao.antecedentes.AntecedentesProfesionalDAOImpl;
import com.contactoprofesionales.model.AntecedenteProfesional;
import com.contactoprofesionales.model.AntecedenteProfesional.TipoAntecedente;
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
import java.util.List;

/**
 * Servlet para gestión de antecedentes profesionales.
 *
 * Endpoint: /api/profesional/antecedentes
 *
 * Métodos:
 * - GET: Lista todos los antecedentes del profesional autenticado
 * - POST: Crea un nuevo antecedente (policial, penal o judicial)
 * - PUT: Actualiza un antecedente existente
 * - DELETE: Elimina un antecedente (soft delete)
 *
 * Notas importantes:
 * - Los antecedentes son OPCIONALES pero mejoran la puntuación del profesional
 * - Tipos válidos: policial, penal, judicial
 * - Solo puede haber UN antecedente activo de cada tipo
 * - Los antecedentes deben ser verificados por un administrador
 * - La verificación mejora significativamente la puntuación de la plataforma
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
@WebServlet(name = "AntecedentesProfesionalServlet", urlPatterns = {"/api/profesional/antecedentes"})
public class AntecedentesProfesionalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(AntecedentesProfesionalServlet.class);

    private final Gson gson;
    private AntecedentesProfesionalDAO antecedentesDAO;

    public AntecedentesProfesionalServlet() {
        // ✅ Usar GsonUtil que incluye adaptadores para LocalDateTime y LocalDate
        this.gson = GsonUtil.createGson();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando AntecedentesProfesionalServlet ===");

        try {
            this.antecedentesDAO = new AntecedentesProfesionalDAOImpl();
            logger.info("✓ AntecedentesProfesionalServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar AntecedentesProfesionalServlet", e);
            throw new ServletException("Error al inicializar AntecedentesProfesionalServlet", e);
        }
    }

    /**
     * GET: Lista todos los antecedentes del profesional.
     *
     * URL: /api/profesional/antecedentes
     * Headers: Authorization: Bearer <token>
     *
     * Response 200:
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "tipoAntecedente": "policial",
     *       "documentoUrl": "/uploads/antecedentes/policial_123.pdf",
     *       "fechaEmision": "2024-01-15",
     *       "verificado": true,
     *       "fechaVerificacion": "2024-01-20T10:30:00",
     *       "observaciones": "Antecedentes verificados correctamente"
     *     }
     *   ]
     * }
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("GET /api/profesional/antecedentes - Listando antecedentes");

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

            // ✅ Listar antecedentes
            List<AntecedenteProfesional> antecedentes = antecedentesDAO.listarPorProfesional(profesionalId);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Antecedentes listados exitosamente ({} encontrados) - Tiempo: {}ms",
                    antecedentes.size(), duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success("Antecedentes obtenidos", antecedentes)));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * POST: Crea un nuevo antecedente.
     *
     * IMPORTANTE: Solo puede haber UN antecedente activo de cada tipo.
     * Si ya existe un antecedente del mismo tipo, debe eliminarse antes de crear uno nuevo.
     *
     * Request Body:
     * {
     *   "tipoAntecedente": "policial",
     *   "documentoUrl": "/uploads/antecedentes/policial_123.pdf",
     *   "fechaEmision": "2024-01-15",
     *   "observaciones": "Antecedente policial sin registros"
     * }
     *
     * Tipos válidos: "policial", "penal", "judicial"
     *
     * Response 201: Antecedente creado exitosamente
     * Response 400: Datos inválidos o ya existe un antecedente del mismo tipo
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("POST /api/profesional/antecedentes - Creando antecedente");

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

            // ✅ Parsear JSON del antecedente
            AntecedenteProfesional antecedente = gson.fromJson(request.getReader(), AntecedenteProfesional.class);
            antecedente.setProfesionalId(profesionalId);

            // ✅ Validar datos requeridos
            if (antecedente.getTipoAntecedente() == null) {
                logger.warn("✗ Tipo de antecedente es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error(
                        "El tipo de antecedente es requerido. Valores válidos: policial, penal, judicial")));
                return;
            }

            if (antecedente.getDocumentoUrl() == null || antecedente.getDocumentoUrl().trim().isEmpty()) {
                logger.warn("✗ URL del documento es requerida");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("La URL del documento es requerida")));
                return;
            }

            // ✅ Guardar antecedente (el DAO valida que no exista duplicado del mismo tipo)
            Integer id = antecedentesDAO.guardar(antecedente);
            antecedente.setId(id);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Antecedente {} creado exitosamente con ID {} - Tiempo: {}ms",
                    antecedente.getTipoAntecedente().getValor(), id, duration);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(JsonResponse.success(
                    "Antecedente creado exitosamente. Será verificado por un administrador.", antecedente)));

        } catch (Exception e) {
            // ✅ Si el error es por antecedente duplicado, retornar 400 en lugar de 500
            if (e.getMessage() != null && e.getMessage().contains("Ya existe un antecedente")) {
                logger.warn("✗ Antecedente duplicado: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error(e.getMessage())));
            } else {
                handleError(response, e, startTime);
            }
        }
    }

    /**
     * PUT: Actualiza un antecedente existente.
     *
     * IMPORTANTE: Este método NO permite modificar el estado de verificación.
     * La verificación solo puede ser realizada por un administrador.
     *
     * Request Body:
     * {
     *   "id": 1,
     *   "documentoUrl": "/uploads/antecedentes/policial_actualizado.pdf",
     *   "fechaEmision": "2024-02-15",
     *   "observaciones": "Documento actualizado"
     * }
     *
     * Response 200: Antecedente actualizado exitosamente
     * Response 404: Antecedente no encontrado o no pertenece al profesional
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("PUT /api/profesional/antecedentes - Actualizando antecedente");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            // ✅ Parsear JSON del antecedente
            AntecedenteProfesional antecedente = gson.fromJson(request.getReader(), AntecedenteProfesional.class);
            antecedente.setProfesionalId(profesionalId);

            // ✅ Validar que tenga ID
            if (antecedente.getId() == null) {
                logger.warn("✗ ID de antecedente es requerido para actualizar");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido para actualizar")));
                return;
            }

            // ✅ IMPORTANTE: Advertir si se intenta modificar verificación (ignorar silenciosamente)
            if (antecedente.getVerificado() != null && antecedente.getVerificado()) {
                logger.warn("⚠️ Se intentó modificar estado de verificación (ignorado). Solo administradores pueden verificar.");
                antecedente.setVerificado(null); // Se ignora, el DAO no actualiza este campo
            }

            // ✅ Actualizar antecedente (el DAO NO actualiza estado de verificación)
            boolean actualizado = antecedentesDAO.actualizar(antecedente);

            if (actualizado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Antecedente actualizado exitosamente - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Antecedente actualizado exitosamente", antecedente)));
            } else {
                logger.warn("✗ Antecedente no encontrado o no pertenece al profesional");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Antecedente no encontrado")));
            }

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * DELETE: Elimina (soft delete) un antecedente.
     *
     * URL: /api/profesional/antecedentes?id=1
     *
     * Response 200: Antecedente eliminado exitosamente
     * Response 404: Antecedente no encontrado
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("DELETE /api/profesional/antecedentes - Eliminando antecedente");

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
                logger.warn("✗ ID de antecedente es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido")));
                return;
            }

            Integer id = Integer.parseInt(idParam);

            // ✅ Eliminar antecedente (soft delete)
            boolean eliminado = antecedentesDAO.eliminar(id);

            if (eliminado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Antecedente eliminado exitosamente - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Antecedente eliminado exitosamente")));
            } else {
                logger.warn("✗ Antecedente no encontrado");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Antecedente no encontrado")));
            }

        } catch (NumberFormatException e) {
            logger.warn("✗ ID de antecedente inválido", e);
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
        logger.info("Destruyendo AntecedentesProfesionalServlet");
        super.destroy();
    }
}
