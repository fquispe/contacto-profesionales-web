package com.contactoprofesionales.controller.perfil;

import com.contactoprofesionales.dao.portafolio.ImagenesProyectoDAO;
import com.contactoprofesionales.dao.portafolio.ImagenesProyectoDAOImpl;
import com.contactoprofesionales.model.ImagenProyecto;
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
 * Servlet para gestión de imágenes de proyectos del portafolio.
 *
 * Endpoint: /api/profesional/proyectos/imagenes
 *
 * Métodos:
 * - GET: Lista todas las imágenes de un proyecto o busca una específica por ID
 * - POST: Crea una nueva imagen (máximo 5 imágenes por proyecto)
 * - DELETE: Elimina una imagen (DELETE físico, no soft delete)
 *
 * Notas importantes:
 * - Cada proyecto puede tener máximo 5 imágenes
 * - La eliminación es FÍSICA (no soft delete) - el registro se elimina permanentemente
 * - Tipos de imagen: antes, despues, proceso, general
 * - El servlet recibe la URL de la imagen (la subida se maneja por separado)
 * - NO hay método PUT/UPDATE - las imágenes solo se crean o eliminan
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
@WebServlet(name = "ImagenesProyectoServlet", urlPatterns = {"/api/profesional/proyectos/imagenes"})
public class ImagenesProyectoServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(ImagenesProyectoServlet.class);

    private final Gson gson;
    private ImagenesProyectoDAO imagenesDAO;

    public ImagenesProyectoServlet() {
        // ✅ Usar GsonUtil que incluye adaptadores para LocalDateTime y LocalDate
        this.gson = GsonUtil.createGson();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando ImagenesProyectoServlet ===");

        try {
            this.imagenesDAO = new ImagenesProyectoDAOImpl();
            logger.info("✓ ImagenesProyectoServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar ImagenesProyectoServlet", e);
            throw new ServletException("Error al inicializar ImagenesProyectoServlet", e);
        }
    }

    /**
     * GET: Lista todas las imágenes de un proyecto o busca una específica por ID.
     *
     * URL 1: /api/profesional/proyectos/imagenes?proyectoId=1 (lista todas las imágenes del proyecto)
     * URL 2: /api/profesional/proyectos/imagenes?id=1 (busca imagen específica)
     * Headers: Authorization: Bearer <token>
     *
     * Response 200 (lista):
     * {
     *   "success": true,
     *   "data": [
     *     {
     *       "id": 1,
     *       "proyectoId": 1,
     *       "urlImagen": "/uploads/proyectos/imagen1.jpg",
     *       "tipoImagen": "antes",
     *       "descripcion": "Vista antes de la remodelación",
     *       "orden": 1,
     *       "fechaSubida": "2024-11-15T10:30:00"
     *     }
     *   ]
     * }
     *
     * Response 200 (buscar por ID):
     * {
     *   "success": true,
     *   "data": { imagen específica }
     * }
     *
     * Response 400: Parámetros inválidos
     * Response 404: Imagen no encontrada
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("GET /api/profesional/proyectos/imagenes");

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

            // ✅ Verificar si se solicita una imagen específica por ID
            String idParam = request.getParameter("id");
            String proyectoIdParam = request.getParameter("proyectoId");

            if (idParam != null && !idParam.trim().isEmpty()) {
                // Buscar imagen específica
                buscarImagenPorId(response, Integer.parseInt(idParam), startTime);

            } else if (proyectoIdParam != null && !proyectoIdParam.trim().isEmpty()) {
                // Listar todas las imágenes del proyecto
                listarImagenesPorProyecto(response, Integer.parseInt(proyectoIdParam), startTime);

            } else {
                logger.warn("✗ Se requiere parámetro 'id' o 'proyectoId'");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("Se requiere parámetro 'id' o 'proyectoId'")));
            }

        } catch (NumberFormatException e) {
            logger.warn("✗ ID inválido", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(JsonResponse.error("ID inválido")));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * Lista todas las imágenes de un proyecto específico.
     *
     * @param response Response HTTP
     * @param proyectoId ID del proyecto
     * @param startTime Tiempo de inicio de la solicitud
     */
    private void listarImagenesPorProyecto(HttpServletResponse response, Integer proyectoId, long startTime)
            throws Exception, IOException {

        logger.info("Listando imágenes del proyecto {}", proyectoId);

        // ✅ Obtener imágenes del proyecto
        List<ImagenProyecto> imagenes = imagenesDAO.listarPorProyecto(proyectoId);

        long duration = System.currentTimeMillis() - startTime;
        logger.info("✓ Imágenes listadas exitosamente ({} encontradas) - Tiempo: {}ms",
                imagenes.size(), duration);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(gson.toJson(JsonResponse.success("Imágenes obtenidas", imagenes)));
    }

    /**
     * Busca una imagen específica por ID.
     *
     * @param response Response HTTP
     * @param imagenId ID de la imagen
     * @param startTime Tiempo de inicio de la solicitud
     */
    private void buscarImagenPorId(HttpServletResponse response, Integer imagenId, long startTime)
            throws Exception, IOException {

        logger.info("Buscando imagen con ID {}", imagenId);

        Optional<ImagenProyecto> imagenOpt = imagenesDAO.buscarPorId(imagenId);

        if (imagenOpt.isPresent()) {
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Imagen encontrada - Tiempo: {}ms", duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success("Imagen encontrada", imagenOpt.get())));
        } else {
            logger.warn("✗ Imagen con ID {} no encontrada", imagenId);
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(gson.toJson(JsonResponse.error("Imagen no encontrada")));
        }
    }

    /**
     * POST: Crea una nueva imagen para un proyecto.
     *
     * IMPORTANTE: Este endpoint recibe la URL de la imagen YA SUBIDA.
     * La subida física del archivo se maneja por separado (otro servlet o servicio).
     *
     * Request Body:
     * {
     *   "proyectoId": 1,
     *   "urlImagen": "/uploads/proyectos/imagen1.jpg",
     *   "tipoImagen": "antes",
     *   "descripcion": "Vista antes de la remodelación",
     *   "orden": 1
     * }
     *
     * Tipos de imagen válidos: "antes", "despues", "proceso", "general"
     *
     * Response 201: Imagen creada exitosamente
     * Response 400: Datos inválidos o límite de 5 imágenes alcanzado
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("POST /api/profesional/proyectos/imagenes - Creando imagen");

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

            // ✅ Parsear JSON de la imagen
            ImagenProyecto imagen = gson.fromJson(request.getReader(), ImagenProyecto.class);

            // ✅ Validar datos requeridos
            if (imagen.getProyectoId() == null) {
                logger.warn("✗ ID del proyecto es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("El ID del proyecto es requerido")));
                return;
            }

            if (imagen.getUrlImagen() == null || imagen.getUrlImagen().trim().isEmpty()) {
                logger.warn("✗ URL de la imagen es requerida");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("La URL de la imagen es requerida")));
                return;
            }

            // ✅ Validar tipo de imagen (debe ser uno de los valores válidos del enum)
            if (imagen.getTipoImagen() == null) {
                logger.warn("✗ Tipo de imagen es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error(
                        "El tipo de imagen es requerido. Valores válidos: antes, despues, proceso, general")));
                return;
            }

            // ✅ Guardar imagen (el DAO valida el límite de 5 imágenes)
            Integer id = imagenesDAO.guardar(imagen);
            imagen.setId(id);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Imagen creada exitosamente con ID {} - Tiempo: {}ms", id, duration);

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(JsonResponse.success("Imagen creada exitosamente", imagen)));

        } catch (Exception e) {
            // ✅ Si el error es por límite de imágenes, retornar 400 en lugar de 500
            if (e.getMessage() != null && e.getMessage().contains("máximo de 5 imágenes")) {
                logger.warn("✗ Límite de imágenes alcanzado: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error(e.getMessage())));
            } else {
                handleError(response, e, startTime);
            }
        }
    }

    /**
     * DELETE: Elimina una imagen del proyecto.
     *
     * IMPORTANTE: Esta es una eliminación FÍSICA, no soft delete.
     * El registro se elimina permanentemente de la base de datos.
     * Se recomienda eliminar el archivo físico del servidor ANTES de llamar a este endpoint.
     *
     * URL: /api/profesional/proyectos/imagenes?id=1
     *
     * Response 200: Imagen eliminada exitosamente
     * Response 404: Imagen no encontrada
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("DELETE /api/profesional/proyectos/imagenes - Eliminando imagen");

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
                logger.warn("✗ ID de imagen es requerido");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("ID es requerido")));
                return;
            }

            Integer id = Integer.parseInt(idParam);

            // ✅ ADVERTENCIA: Antes de eliminar el registro, se recomienda eliminar el archivo físico
            logger.warn("⚠️ Eliminando imagen ID {}. Asegúrese de eliminar el archivo físico del servidor.", id);

            // ✅ Eliminar imagen (DELETE físico)
            boolean eliminado = imagenesDAO.eliminar(id);

            if (eliminado) {
                long duration = System.currentTimeMillis() - startTime;
                logger.info("✓ Imagen eliminada exitosamente (DELETE físico) - Tiempo: {}ms", duration);

                response.setStatus(HttpServletResponse.SC_OK);
                response.getWriter().write(gson.toJson(JsonResponse.success("Imagen eliminada exitosamente")));
            } else {
                logger.warn("✗ Imagen no encontrada");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write(gson.toJson(JsonResponse.error("Imagen no encontrada")));
            }

        } catch (NumberFormatException e) {
            logger.warn("✗ ID de imagen inválido", e);
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
        logger.info("Destruyendo ImagenesProyectoServlet");
        super.destroy();
    }
}
