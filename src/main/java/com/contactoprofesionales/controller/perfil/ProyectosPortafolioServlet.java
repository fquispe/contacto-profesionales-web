package com.contactoprofesionales.controller.perfil;

import com.contactoprofesionales.dao.portafolio.ProyectosPortafolioDAO;
import com.contactoprofesionales.dao.portafolio.ProyectosPortafolioDAOImpl;
import com.contactoprofesionales.model.ProyectoPortafolio;
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
import java.util.Optional;

/**
 * Servlet para gestión de proyectos del portafolio profesional.
 *
 * Endpoint: /api/profesional/proyectos
 *
 * Métodos:
 * - GET: Lista todos los proyectos del profesional autenticado o busca uno específico por ID
 * - POST: Crea un nuevo proyecto (máximo 20 proyectos activos)
 * - PUT: Actualiza un proyecto existente (NO permite actualizar calificación del cliente)
 * - DELETE: Elimina un proyecto (soft delete)
 *
 * Notas importantes:
 * - El profesional NO puede modificar la calificación ni el comentario del cliente
 * - La calificación solo puede ser modificada desde el módulo de valoración de clientes
 * - Se valida el límite de 20 proyectos activos en el DAO
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
@WebServlet(name = "ProyectosPortafolioServlet", urlPatterns = {"/api/profesional/proyectos"})
public class ProyectosPortafolioServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ProyectosPortafolioServlet.class);

    private final Gson gson;
    private ProyectosPortafolioDAO proyectosDAO;

    public ProyectosPortafolioServlet() {
        // ✅ Usar GsonUtil que incluye adaptadores para LocalDateTime y LocalDate
        this.gson = GsonUtil.createGson();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando ProyectosPortafolioServlet ===");

        try {
            this.proyectosDAO = new ProyectosPortafolioDAOImpl();
            logger.info("✓ ProyectosPortafolioServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar ProyectosPortafolioServlet", e);
            throw new ServletException("Error al inicializar ProyectosPortafolioServlet", e);
        }
    }

    /**
     * GET: Lista todos los proyectos del profesional o busca uno específico por ID.
     *
     * URL 1: /api/profesional/proyectos (lista todos)
     * URL 2: /api/profesional/proyectos?id=1 (busca proyecto específico)
     * Headers: Authorization: Bearer <token>
     *
     * Response 200 (lista):
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "nombreProyecto": "Remodelación cocina",
     *       "fechaRealizacion": "2024-10-15",
     *       "descripcion": "Remodelación completa de cocina",
     *       "categoriaId": 5,
     *       "categoriaNombre": "Construcción y Remodelación",
     *       "calificacionCliente": 9.5,
     *       "comentarioCliente": "Excelente trabajo",
     *       "imagenes": [...]
     *     }
     *   ]
     * }
     *
     * Response 200 (buscar por ID):
     * {
     *   "success": true,
     *   "data": { proyecto con imágenes }
     * }
     *
     * Response 404: Proyecto no encontrado
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("GET /api/profesional/proyectos");

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

            // ✅ Verificar si se solicita un proyecto específico por ID
            String idParam = request.getParameter("id");

            if (idParam != null && !idParam.trim().isEmpty()) {
                // Buscar proyecto específico
                buscarProyectoPorId(response, Integer.parseInt(idParam), startTime);
            } else {
                // Listar todos los proyectos del profesional
                listarProyectos(response, profesionalId, startTime);
            }

        } catch (NumberFormatException e) {
            logger.warn("✗ ID de proyecto inválido", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(JsonResponse.error("ID inválido")));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * Lista todos los proyectos del profesional (con imágenes).
     *
     * @param response Response HTTP
     * @param profesionalId ID del profesional
     * @param startTime Tiempo de inicio de la solicitud
     */
    private void listarProyectos(HttpServletResponse response, Integer profesionalId, long startTime)
            throws Exception, IOException {

        logger.info("Listando proyectos del profesional {}", profesionalId);

        // ✅ Obtener proyectos con imágenes
        List<ProyectoPortafolio> proyectos = proyectosDAO.listarPorProfesional(profesionalId);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("✓ Proyectos listados exitosamente ({} encontrados) - Tiempo: {}ms",
                proyectos.size(), duration);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(JsonResponse.success("Proyectos obtenidos", proyectos)));
    }

    /**
     * Busca un proyecto específico por ID (con imágenes).
     *
     * @param response Response HTTP
     * @param proyectoId ID del proyecto
     * @param startTime Tiempo de inicio de la solicitud
     */
    private void buscarProyectoPorId(HttpServletResponse response, Integer proyectoId, long startTime)
            throws Exception, IOException {

        logger.info("Buscando proyecto con ID {}", proyectoId);

        Optional<ProyectoPortafolio> proyectoOpt = proyectosDAO.buscarPorId(proyectoId);

        if (proyectoOpt.isPresent()) {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Proyecto encontrado - Tiempo: {}ms", duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success("Proyecto encontrado", proyectoOpt.get())));
        } else {
            logger.warn("✗ Proyecto con ID {} no encontrado", proyectoId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(JsonResponse.error("Proyecto no encontrado")));
        }
    }

    /**
     * POST: Crea un nuevo proyecto en el portafolio.
     *
     * Request Body:
     * {
     *   "nombreProyecto": "Remodelación cocina",
     *   "fechaRealizacion": "2024-10-15",
     *   "descripcion": "Remodelación completa de cocina moderna",
     *   "categoriaId": 5,
     *   "solicitudServicioId": 123,
     *   "orden": 1
     * }
     *
     * Response 201: Proyecto creado exitosamente
     * Response 400: Datos inválidos o límite de 20 proyectos alcanzado
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("POST /api/profesional/proyectos - Creando proyecto");

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

            // ✅ Parsear JSON del proyecto
            ProyectoPortafolio proyecto = gson.fromJson(request.getReader(), ProyectoPortafolio.class);
            proyecto.setProfesionalId(profesionalId);

            // ✅ Validar datos requeridos
            if (proyecto.getNombreProyecto() == null || proyecto.getNombreProyecto().trim().isEmpty()) {
                logger.warn("✗ Nombre del proyecto es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("El nombre del proyecto es requerido")));
                return;
            }

            if (proyecto.getFechaRealizacion() == null) {
                logger.warn("✗ Fecha de realización es requerida");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("La fecha de realización es requerida")));
                return;
            }

            if (proyecto.getDescripcion() == null || proyecto.getDescripcion().trim().isEmpty()) {
                logger.warn("✗ Descripción del proyecto es requerida");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("La descripción del proyecto es requerida")));
                return;
            }

            // ✅ Guardar proyecto (el DAO valida el límite de 20 proyectos)
            Integer id = proyectosDAO.guardar(proyecto);
            proyecto.setId(id);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Proyecto creado exitosamente con ID {} - Tiempo: {}ms", id, duration);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(JsonResponse.success("Proyecto creado exitosamente", proyecto)));

        } catch (Exception e) {
            // ✅ Si el error es por límite de proyectos, retornar 400 en lugar de 500
            if (e.getMessage() != null && e.getMessage().contains("máximo de 20 proyectos")) {
                logger.warn("✗ Límite de proyectos alcanzado: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error(e.getMessage())));
            } else {
                handleError(response, e, startTime);
            }
        }
    }

    /**
     * PUT: Actualiza un proyecto existente.
     *
     * IMPORTANTE: Este método NO permite actualizar la calificación ni el comentario del cliente.
     * Esos campos solo pueden ser modificados desde el módulo de valoración de clientes.
     *
     * Request Body:
     * {
     *   "id": 1,
     *   "nombreProyecto": "Remodelación cocina actualizada",
     *   "fechaRealizacion": "2024-10-15",
     *   "descripcion": "Nueva descripción",
     *   "categoriaId": 5,
     *   "orden": 2
     * }
     *
     * Response 200: Proyecto actualizado exitosamente
     * Response 404: Proyecto no encontrado o no pertenece al profesional
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("PUT /api/profesional/proyectos - Actualizando proyecto");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            Integer profesionalId = obtenerProfesionalIdDeToken(request);

            if (profesionalId == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write(gson.toJson(JsonResponse.error("No autenticado")));
                return;
            }

            // ✅ Parsear JSON del proyecto
            ProyectoPortafolio proyecto = gson.fromJson(request.getReader(), ProyectoPortafolio.class);
            proyecto.setProfesionalId(profesionalId);

            // ✅ Validar que tenga ID
            if (proyecto.getId() == null) {
                logger.warn("✗ ID de proyecto es requerido para actualizar");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido para actualizar")));
                return;
            }

            // ✅ IMPORTANTE: Advertir si se intenta actualizar calificación (ignorar silenciosamente)
            if (proyecto.getCalificacionCliente() != null || proyecto.getComentarioCliente() != null) {
                logger.warn("⚠️ Se intentó actualizar calificación/comentario del cliente (ignorado). Solo clientes pueden modificar esto.");
                proyecto.setCalificacionCliente(null);
                proyecto.setComentarioCliente(null);
            }

            // ✅ Actualizar proyecto (el DAO NO actualiza calificación ni comentario)
            boolean actualizado = proyectosDAO.actualizar(proyecto);

            if (actualizado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Proyecto actualizado exitosamente - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Proyecto actualizado exitosamente", proyecto)));
            } else {
                logger.warn("✗ Proyecto no encontrado o no pertenece al profesional");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Proyecto no encontrado")));
            }

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * DELETE: Elimina (soft delete) un proyecto.
     *
     * URL: /api/profesional/proyectos?id=1
     *
     * Response 200: Proyecto eliminado exitosamente
     * Response 404: Proyecto no encontrado
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("DELETE /api/profesional/proyectos - Eliminando proyecto");

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
                logger.warn("✗ ID de proyecto es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido")));
                return;
            }

            Integer id = Integer.parseInt(idParam);

            // ✅ Eliminar proyecto (soft delete)
            boolean eliminado = proyectosDAO.eliminar(id);

            if (eliminado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Proyecto eliminado exitosamente - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Proyecto eliminado exitosamente")));
            } else {
                logger.warn("✗ Proyecto no encontrado");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Proyecto no encontrado")));
            }

        } catch (NumberFormatException e) {
            logger.warn("✗ ID de proyecto inválido", e);
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
        logger.info("Destruyendo ProyectosPortafolioServlet");
        super.destroy();
    }
}
