package com.contactoprofesionales.controller.perfil;

import com.contactoprofesionales.dao.certificaciones.CertificacionesProfesionalDAO;
import com.contactoprofesionales.dao.certificaciones.CertificacionesProfesionalDAOImpl;
import com.contactoprofesionales.dao.portafolio.ProyectosPortafolioDAO;
import com.contactoprofesionales.dao.portafolio.ProyectosPortafolioDAOImpl;
import com.contactoprofesionales.dao.antecedentes.AntecedentesProfesionalDAO;
import com.contactoprofesionales.dao.antecedentes.AntecedentesProfesionalDAOImpl;
import com.contactoprofesionales.dao.redes.RedesSocialesProfesionalDAO;
import com.contactoprofesionales.dao.redes.RedesSocialesProfesionalDAOImpl;
import com.contactoprofesionales.dto.PerfilProfesionalCompletoDTO;
import com.contactoprofesionales.util.JsonResponse;
import com.contactoprofesionales.util.DatabaseConnection;
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
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Servlet para gestión del perfil profesional completo.
 *
 * Endpoint: /api/profesional/perfil
 *
 * Métodos:
 * - GET: Obtiene el perfil completo del profesional (consolidado)
 * - PUT: Actualiza los datos básicos del perfil
 *
 * Notas importantes:
 * - El GET consolida TODA la información del profesional en un solo response:
 *   * Datos básicos (biografía, experiencia, tarifas, etc.)
 *   * Certificaciones
 *   * Portafolio de proyectos (con imágenes)
 *   * Antecedentes (policial, penal, judicial)
 *   * Redes sociales
 *   * Especialidades
 *   * Áreas de cobertura
 *   * Disponibilidad horaria
 *   * Puntuación de la plataforma (calculada)
 *
 * - El PUT actualiza SOLO datos básicos del perfil profesional
 * - Para actualizar certificaciones, proyectos, antecedentes, etc., usar sus servlets específicos
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
@WebServlet(name = "PerfilProfesionalServlet", urlPatterns = {"/api/profesional/perfil"})
public class PerfilProfesionalServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(PerfilProfesionalServlet.class);

    private final Gson gson;

    // ✅ DAOs para datos complejos
    private CertificacionesProfesionalDAO certificacionesDAO;
    private ProyectosPortafolioDAO proyectosDAO;
    private AntecedentesProfesionalDAO antecedentesDAO;
    private RedesSocialesProfesionalDAO redesDAO;

    public PerfilProfesionalServlet() {
        // ✅ Usar GsonUtil que incluye adaptadores para LocalDateTime
        this.gson = GsonUtil.createGson();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando PerfilProfesionalServlet ===");

        try {
            this.certificacionesDAO = new CertificacionesProfesionalDAOImpl();
            this.proyectosDAO = new ProyectosPortafolioDAOImpl();
            this.antecedentesDAO = new AntecedentesProfesionalDAOImpl();
            this.redesDAO = new RedesSocialesProfesionalDAOImpl();
            logger.info("✓ PerfilProfesionalServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar PerfilProfesionalServlet", e);
            throw new ServletException("Error al inicializar PerfilProfesionalServlet", e);
        }
    }

    /**
     * GET: Obtiene el perfil completo del profesional.
     *
     * Consolida TODA la información del profesional en un solo response.
     *
     * URL: /api/profesional/perfil
     * Headers: Authorization: Bearer <token>
     *
     * Response 200:
     * {
     *   "success": true,
     *   "data": {
     *     "id": 1,
     *     "nombreCompleto": "Juan Pérez",
     *     "biografiaProfesional": "Profesional con 10 años de experiencia...",
     *     "aniosExperiencia": 10,
     *     "puntuacionPlataforma": 8.5,
     *     "certificaciones": [...],
     *     "proyectos": [...],
     *     "antecedentes": [...],
     *     "redesSociales": [...],
     *     "especialidades": [...],
     *     ...
     *   }
     * }
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("GET /api/profesional/perfil - Obteniendo perfil completo");

        // ✅ Configurar CORS para permitir credenciales
        setCorsHeaders(request, response);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // ✅ ACTUALIZADO 2025-12-04: Obtener profesionalId desde query parameter (localStorage)
            String profesionalIdParam = request.getParameter("profesionalId");

            if (profesionalIdParam == null || profesionalIdParam.trim().isEmpty()) {
                logger.warn("✗ No se proporcionó profesionalId en la petición");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("Parámetro profesionalId requerido")));
                return;
            }

            Integer profesionalId = null;
            try {
                profesionalId = Integer.parseInt(profesionalIdParam);
                logger.info("✓ ProfesionalId recibido desde query parameter: {}", profesionalId);
            } catch (NumberFormatException e) {
                logger.warn("✗ profesionalId inválido: {}", profesionalIdParam);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("profesionalId debe ser un número válido")));
                return;
            }

            if (profesionalId == null) {
                logger.warn("✗ profesionalId es null");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("profesionalId requerido")));
                return;
            }

            // ✅ Construir perfil completo consolidando todos los datos
            PerfilProfesionalCompletoDTO perfil = construirPerfilCompleto(profesionalId);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Perfil completo obtenido exitosamente - Tiempo: {}ms", duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success("Perfil obtenido exitosamente", perfil)));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * Construye el perfil completo del profesional consolidando todos los datos.
     *
     * Consulta múltiples fuentes:
     * - Tabla profesionales (datos básicos)
     * - Tabla usuarios y personas (nombre, email, teléfono)
     * - Certificaciones (vía DAO)
     * - Proyectos con imágenes (vía DAO)
     * - Antecedentes (vía DAO)
     * - Redes sociales (vía DAO)
     * - Especialidades (vía query directa)
     * - Función calcular_puntuacion_profesional() (puntuación calculada)
     *
     * @param profesionalId ID del profesional
     * @return PerfilProfesionalCompletoDTO con todos los datos consolidados
     * @throws Exception si hay error en alguna consulta
     */
    private PerfilProfesionalCompletoDTO construirPerfilCompleto(Integer profesionalId) throws Exception {
        logger.debug("Construyendo perfil completo para profesional {}", profesionalId);

        PerfilProfesionalCompletoDTO perfil = new PerfilProfesionalCompletoDTO();

        // ✅ 1. Obtener datos básicos del profesional (con JOIN a usuarios)
        String sql = "SELECT " +
                    "p.id, p.usuario_id, p.descripcion, p.experiencia, p.habilidades, " +
                    "p.foto_perfil, p.foto_portada, p.tarifa_hora, " +
                    "p.calificacion_promedio, p.total_resenas, " +
                    "p.verificado, p.disponible, p.fecha_registro, p.ultima_actualizacion, " +
                    "p.anios_experiencia, p.documento_identidad, p.verificacion_identidad, " +
                    "p.certificado_antecedentes, " +
                    // ✅ NUEVOS CAMPOS
                    "p.biografia_profesional, p.idiomas, p.licencias_profesionales, " +
                    "p.seguro_responsabilidad, p.metodos_pago, p.politica_cancelacion, " +
                    // ✅ Datos de usuarios (nombre completo, email, teléfono)
                    "u.nombre_completo, u.telefono " +
                    "FROM profesionales p " +
                    "INNER JOIN usuarios u ON p.usuario_id = u.id " +
                    "WHERE p.id = ? AND p.activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // ✅ Mapear datos básicos
                    perfil.setId(rs.getInt("id"));
                    perfil.setUsuarioId(rs.getInt("usuario_id"));

                    // ✅ Datos personales (de tabla users)
                    perfil.setNombreCompleto(rs.getString("nombre_completo"));
                    
                    perfil.setTelefono(rs.getString("telefono"));
                    perfil.setDocumentoIdentidad(rs.getString("documento_identidad"));

                    // ✅ Información profesional
                    perfil.setBiografiaProfesional(rs.getString("biografia_profesional"));
                    perfil.setDescripcion(rs.getString("descripcion"));
                    perfil.setExperiencia(rs.getString("experiencia"));
                    perfil.setAniosExperiencia(rs.getInt("anios_experiencia"));

                    // ✅ Habilidades (convertir de texto separado por comas a lista)
                    String habilidadesStr = rs.getString("habilidades");
                    if (habilidadesStr != null && !habilidadesStr.trim().isEmpty()) {
                        perfil.setHabilidades(Arrays.asList(habilidadesStr.split(",")));
                    }

                    // ✅ Información adicional (arrays de PostgreSQL)
                    Array idiomasArray = rs.getArray("idiomas");
                    if (idiomasArray != null) {
                        perfil.setIdiomas((String[]) idiomasArray.getArray());
                    }

                    Array metodosPagoArray = rs.getArray("metodos_pago");
                    if (metodosPagoArray != null) {
                        perfil.setMetodosPago((String[]) metodosPagoArray.getArray());
                    }

                    perfil.setLicenciasProfesionales(rs.getString("licencias_profesionales"));
                    perfil.setSeguroResponsabilidad(rs.getBoolean("seguro_responsabilidad"));
                    perfil.setPoliticaCancelacion(rs.getString("politica_cancelacion"));

                    // ✅ Fotos
                    perfil.setFotoPerfil(rs.getString("foto_perfil"));
                    perfil.setFotoPortada(rs.getString("foto_portada"));

                    // ✅ Tarifas y calificaciones
                    perfil.setTarifaHora(rs.getBigDecimal("tarifa_hora"));
                    perfil.setCalificacionPromedio(rs.getDouble("calificacion_promedio"));
                    perfil.setTotalResenas(rs.getInt("total_resenas"));

                    // ✅ Estado
                    perfil.setVerificado(rs.getBoolean("verificado"));
                    perfil.setVerificacionIdentidad(rs.getBoolean("verificacion_identidad"));
                    perfil.setCertificadoAntecedentes(rs.getBoolean("certificado_antecedentes"));
                    perfil.setDisponible(rs.getBoolean("disponible"));

                    Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
                    if (fechaRegistro != null) {
                        perfil.setFechaRegistro(fechaRegistro.toLocalDateTime());
                    }

                    Timestamp ultimaActualizacion = rs.getTimestamp("ultima_actualizacion");
                    if (ultimaActualizacion != null) {
                        perfil.setUltimaActualizacion(ultimaActualizacion.toLocalDateTime());
                    }

                } else {
                    throw new Exception("Profesional no encontrado");
                }
            }
        }

        // ✅ 2. Obtener certificaciones
        try {
            perfil.setCertificaciones(certificacionesDAO.listarPorProfesional(profesionalId));
            logger.debug("Certificaciones cargadas: {}", perfil.getCertificaciones().size());
        } catch (Exception e) {
            logger.warn("Error al cargar certificaciones", e);
            perfil.setCertificaciones(new ArrayList<>());
        }

        // ✅ 3. Obtener proyectos (incluye imágenes)
        try {
            perfil.setProyectos(proyectosDAO.listarPorProfesional(profesionalId));
            logger.debug("Proyectos cargados: {}", perfil.getProyectos().size());
        } catch (Exception e) {
            logger.warn("Error al cargar proyectos", e);
            perfil.setProyectos(new ArrayList<>());
        }

        // ✅ 4. Obtener antecedentes
        try {
            perfil.setAntecedentes(antecedentesDAO.listarPorProfesional(profesionalId));
            perfil.setAntecedentesVerificados(antecedentesDAO.contarVerificados(profesionalId));
            logger.debug("Antecedentes cargados: {} ({} verificados)",
                    perfil.getAntecedentes().size(), perfil.getAntecedentesVerificados());
        } catch (Exception e) {
            logger.warn("Error al cargar antecedentes", e);
            perfil.setAntecedentes(new ArrayList<>());
            perfil.setAntecedentesVerificados(0);
        }

        // ✅ 5. Obtener redes sociales
        try {
            perfil.setRedesSociales(redesDAO.listarPorProfesional(profesionalId));
            logger.debug("Redes sociales cargadas: {}", perfil.getRedesSociales().size());
        } catch (Exception e) {
            logger.warn("Error al cargar redes sociales", e);
            perfil.setRedesSociales(new ArrayList<>());
        }

        // ✅ 6. Calcular puntuación de la plataforma (función SQL)
        try {
            perfil.setPuntuacionPlataforma(calcularPuntuacionPlataforma(profesionalId));
            logger.debug("Puntuación plataforma calculada: {}", perfil.getPuntuacionPlataforma());
        } catch (Exception e) {
            logger.warn("Error al calcular puntuación de la plataforma", e);
            perfil.setPuntuacionPlataforma(BigDecimal.ZERO);
        }

        // ✅ TODO: Cargar especialidades, áreas de cobertura y disponibilidad
        // (Requieren DAOs adicionales o queries directas)

        logger.info("✓ Perfil completo construido exitosamente para profesional {}", profesionalId);
        return perfil;
    }

    /**
     * Calcula la puntuación de la plataforma usando la función SQL.
     *
     * La función calcular_puntuacion_profesional() toma en cuenta:
     * - Calificación promedio de proyectos (40%)
     * - Certificaciones (20%)
     * - Antecedentes verificados (20%)
     * - Años de experiencia (10%)
     * - Biografía completa (10%)
     *
     * @param profesionalId ID del profesional
     * @return Puntuación calculada (0-10)
     * @throws Exception si hay error en la consulta
     */
    private BigDecimal calcularPuntuacionPlataforma(Integer profesionalId) throws Exception {
        String sql = "SELECT calcular_puntuacion_profesional(?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BigDecimal puntuacion = rs.getBigDecimal(1);
                    return puntuacion != null ? puntuacion : BigDecimal.ZERO;
                }
            }

            return BigDecimal.ZERO;

        } catch (SQLException e) {
            logger.error("Error al calcular puntuación de la plataforma", e);
            throw new Exception("Error al calcular puntuación", e);
        }
    }

    /**
     * PUT: Actualiza los datos básicos del perfil profesional.
     *
     * IMPORTANTE: Este método actualiza SOLO datos básicos del profesional.
     * Para actualizar certificaciones, proyectos, antecedentes, redes sociales,
     * usar los servlets específicos de cada entidad.
     *
     * Request Body:
     * {
     *   "biografiaProfesional": "Profesional con 10 años de experiencia...",
     *   "aniosExperiencia": 10,
     *   "idiomas": ["Español", "Inglés"],
     *   "licenciasProfesionales": "Licencia ABC-123",
     *   "seguroResponsabilidad": true,
     *   "metodosPago": ["Efectivo", "Transferencia", "Tarjeta"],
     *   "politicaCancelacion": "Cancelación con 24 horas de anticipación"
     * }
     *
     * Response 200: Perfil actualizado exitosamente
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long startTime = System.currentTimeMillis();
        logger.info("PUT /api/profesional/perfil - Actualizando perfil básico");

        // ✅ Configurar CORS para permitir credenciales
        setCorsHeaders(request, response);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // ✅ ACTUALIZADO 2025-12-04: Obtener profesionalId desde query parameter (localStorage)
            String profesionalIdParam = request.getParameter("profesionalId");

            if (profesionalIdParam == null || profesionalIdParam.trim().isEmpty()) {
                logger.warn("✗ No se proporcionó profesionalId en la petición PUT");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("Parámetro profesionalId requerido")));
                return;
            }

            Integer profesionalId = null;
            try {
                profesionalId = Integer.parseInt(profesionalIdParam);
                logger.info("✓ ProfesionalId recibido desde query parameter (PUT): {}", profesionalId);
            } catch (NumberFormatException e) {
                logger.warn("✗ profesionalId inválido en PUT: {}", profesionalIdParam);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("profesionalId debe ser un número válido")));
                return;
            }

            if (profesionalId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(gson.toJson(JsonResponse.error("profesionalId requerido")));
                return;
            }

            // ✅ Parsear JSON con datos básicos del perfil
            PerfilProfesionalCompletoDTO datosActualizados = gson.fromJson(request.getReader(),
                    PerfilProfesionalCompletoDTO.class);

            // ✅ Actualizar datos básicos en la tabla profesionales
            actualizarDatosBasicos(profesionalId, datosActualizados);

            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Perfil básico actualizado exitosamente - Tiempo: {}ms", duration);

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(JsonResponse.success(
                    "Perfil actualizado exitosamente", datosActualizados)));

        } catch (Exception e) {
            handleError(response, e, startTime);
        }
    }

    /**
     * Actualiza los datos básicos del profesional en la tabla profesionales.
     *
     * Campos actualizables:
     * - biografia_profesional
     * - anios_experiencia
     * - idiomas
     * - licencias_profesionales
     * - seguro_responsabilidad
     * - metodos_pago
     * - politica_cancelacion
     *
     * @param profesionalId ID del profesional
     * @param datos Datos actualizados
     * @throws Exception si hay error en la actualización
     */
    private void actualizarDatosBasicos(Integer profesionalId, PerfilProfesionalCompletoDTO datos)
            throws Exception {

        String sql = "UPDATE profesionales SET " +
                    "biografia_profesional = ?, " +
                    "anios_experiencia = ?, " +
                    "idiomas = ?, " +
                    "licencias_profesionales = ?, " +
                    "seguro_responsabilidad = ?, " +
                    "metodos_pago = ?, " +
                    "politica_cancelacion = ?, " +
                    "ultima_actualizacion = NOW() " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, datos.getBiografiaProfesional());
            stmt.setInt(2, datos.getAniosExperiencia() != null ? datos.getAniosExperiencia() : 0);

            // ✅ Arrays de PostgreSQL
            if (datos.getIdiomas() != null) {
                Array idiomasArray = conn.createArrayOf("VARCHAR", datos.getIdiomas());
                stmt.setArray(3, idiomasArray);
            } else {
                stmt.setNull(3, Types.ARRAY);
            }

            stmt.setString(4, datos.getLicenciasProfesionales());
            stmt.setBoolean(5, datos.getSeguroResponsabilidad() != null ? datos.getSeguroResponsabilidad() : false);

            if (datos.getMetodosPago() != null) {
                Array metodosPagoArray = conn.createArrayOf("VARCHAR", datos.getMetodosPago());
                stmt.setArray(6, metodosPagoArray);
            } else {
                stmt.setNull(6, Types.ARRAY);
            }

            stmt.setString(7, datos.getPoliticaCancelacion());
            stmt.setInt(8, profesionalId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 0) {
                throw new Exception("No se pudo actualizar el perfil");
            }

            logger.info("✓ Datos básicos actualizados para profesional {}", profesionalId);

        } catch (SQLException e) {
            logger.error("Error al actualizar datos básicos del profesional", e);
            throw new Exception("Error al actualizar perfil", e);
        }
    }

    /**
     * Obtiene el ID del profesional desde la sesión del usuario autenticado.
     * ✅ CORREGIDO 2025-12-04: Obtiene el profesionalId desde HttpSession
     *
     * @param request Request HTTP
     * @return ID del profesional o null si no está autenticado
     */
    private Integer obtenerProfesionalIdDeToken(HttpServletRequest request) {
        // ✅ Obtener sesión (false = no crear una nueva si no existe)
        jakarta.servlet.http.HttpSession session = request.getSession(false);

        if (session == null) {
            logger.warn("No hay sesión activa");
            return null;
        }

        // ✅ Obtener profesionalId desde la sesión
        Integer profesionalId = (Integer) session.getAttribute("profesionalId");

        if (profesionalId == null) {
            logger.warn("No hay profesionalId en la sesión - Usuario no es profesional o no ha iniciado sesión");
            return null;
        }

        logger.debug("ProfesionalId obtenido de la sesión: {}", profesionalId);
        return profesionalId;
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

    /**
     * Maneja peticiones OPTIONS para CORS preflight.
     * ✅ AGREGADO 2025-12-04: Soporte para CORS con credenciales
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(request, response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Configura los headers CORS para permitir credenciales.
     * ✅ AGREGADO 2025-12-04: CORS con credentials
     *
     * IMPORTANTE: Cuando se usa credentials: 'include' en el frontend,
     * NO se puede usar Access-Control-Allow-Origin: "*"
     * Se debe especificar el origen exacto.
     */
    private void setCorsHeaders(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");

        // Permitir el origen desde donde viene la petición
        if (origin != null) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        } else {
            // Fallback para peticiones sin Origin header
            response.setHeader("Access-Control-Allow-Origin", "http://localhost:9091");
        }

        // ✅ CRÍTICO: Permitir que se envíen credenciales (cookies, sesión)
        response.setHeader("Access-Control-Allow-Credentials", "true");

        // Métodos HTTP permitidos
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");

        // Headers permitidos
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Tiempo de caché para preflight
        response.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    public void destroy() {
        logger.info("Destruyendo PerfilProfesionalServlet");
        super.destroy();
    }
}
