package com.contactoprofesionales.dao.portafolio;

import com.contactoprofesionales.model.ProyectoPortafolio;
import com.contactoprofesionales.model.ImagenProyecto;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO para proyectos del portafolio profesional.
 *
 * Maneja todas las operaciones CRUD para proyectos del portafolio.
 * Valida que un profesional no tenga más de 20 proyectos activos.
 * Incluye integración con imágenes de proyectos.
 *
 * Aplicación de SRP: Solo se encarga de persistencia de proyectos.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class ProyectosPortafolioDAOImpl implements ProyectosPortafolioDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProyectosPortafolioDAOImpl.class);
    private static final int MAX_PROYECTOS_ACTIVOS = 20;

    private final ImagenesProyectoDAO imagenesDAO;

    public ProyectosPortafolioDAOImpl() {
        this.imagenesDAO = new ImagenesProyectoDAOImpl();
    }

    /**
     * Lista todos los proyectos activos de un profesional ordenados por fecha de realización.
     * Incluye el nombre de la categoría mediante JOIN.
     *
     * @param profesionalId ID del profesional
     * @return Lista de proyectos con nombre de categoría
     * @throws Exception si hay error en la consulta
     */
    @Override
    public List<ProyectoPortafolio> listarPorProfesional(Integer profesionalId) throws Exception {
        logger.info("Listando proyectos del profesional {}", profesionalId);

        List<ProyectoPortafolio> proyectos = new ArrayList<>();

        // ✅ JOIN con categorias_profesionales para obtener el nombre
        String sql = "SELECT p.id, p.profesional_id, p.nombre_proyecto, p.fecha_realizacion, " +
                    "p.descripcion, p.categoria_id, c.nombre as categoria_nombre, " +
                    "p.solicitud_servicio_id, p.calificacion_cliente, p.comentario_cliente, " +
                    "p.orden, p.activo, p.fecha_creacion, p.fecha_actualizacion " +
                    "FROM proyectos_portafolio p " +
                    "LEFT JOIN categorias_servicio c ON p.categoria_id = c.id " +
                    "WHERE p.profesional_id = ? AND p.activo = TRUE " +
                    "ORDER BY p.fecha_realizacion DESC, p.orden ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ProyectoPortafolio proyecto = mapearProyecto(rs);

                    // ✅ Cargar imágenes del proyecto
                    try {
                        List<ImagenProyecto> imagenes = imagenesDAO.listarPorProyecto(proyecto.getId());
                        proyecto.setImagenes(imagenes);
                    } catch (Exception e) {
                        logger.warn("Error al cargar imágenes del proyecto {}", proyecto.getId(), e);
                        proyecto.setImagenes(new ArrayList<>());
                    }

                    proyectos.add(proyecto);
                }
            }

            logger.debug("Se encontraron {} proyectos para el profesional {}", proyectos.size(), profesionalId);
            return proyectos;

        } catch (SQLException e) {
            logger.error("Error al listar proyectos del profesional {}", profesionalId, e);
            throw new Exception("Error al obtener proyectos del portafolio", e);
        }
    }

    /**
     * Busca un proyecto específico por su ID, incluyendo sus imágenes.
     *
     * @param id ID del proyecto
     * @return Optional con el proyecto si existe (con imágenes cargadas)
     * @throws Exception si hay error en la consulta
     */
    @Override
    public Optional<ProyectoPortafolio> buscarPorId(Integer id) throws Exception {
        logger.debug("Buscando proyecto con ID {}", id);

        String sql = "SELECT p.id, p.profesional_id, p.nombre_proyecto, p.fecha_realizacion, " +
                    "p.descripcion, p.categoria_id, c.nombre as categoria_nombre, " +
                    "p.solicitud_servicio_id, p.calificacion_cliente, p.comentario_cliente, " +
                    "p.orden, p.activo, p.fecha_creacion, p.fecha_actualizacion " +
                    "FROM proyectos_portafolio p " +
                    "LEFT JOIN categorias_servicio c ON p.categoria_id = c.id " +
                    "WHERE p.id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ProyectoPortafolio proyecto = mapearProyecto(rs);

                    // ✅ Cargar imágenes
                    try {
                        List<ImagenProyecto> imagenes = imagenesDAO.listarPorProyecto(proyecto.getId());
                        proyecto.setImagenes(imagenes);
                    } catch (Exception e) {
                        logger.warn("Error al cargar imágenes del proyecto {}", id, e);
                        proyecto.setImagenes(new ArrayList<>());
                    }

                    logger.debug("Proyecto encontrado: {}", proyecto);
                    return Optional.of(proyecto);
                }
            }

            logger.debug("Proyecto con ID {} no encontrado", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar proyecto con ID {}", id, e);
            throw new Exception("Error al buscar proyecto", e);
        }
    }

    /**
     * Guarda un nuevo proyecto en el portafolio.
     * VALIDA que el profesional no tenga ya 20 proyectos activos antes de insertar.
     *
     * @param proyecto Proyecto a guardar
     * @return ID del proyecto creado
     * @throws Exception si hay error al guardar o si se excede el límite de 20 proyectos
     */
    @Override
    public Integer guardar(ProyectoPortafolio proyecto) throws Exception {
        logger.info("Guardando nuevo proyecto: {} para profesional {}",
                proyecto.getNombreProyecto(), proyecto.getProfesionalId());

        // ✅ VALIDACIÓN: Verificar que no tenga ya 20 proyectos activos
        int totalProyectos = contarActivosPorProfesional(proyecto.getProfesionalId());
        if (totalProyectos >= MAX_PROYECTOS_ACTIVOS) {
            logger.warn("✗ Profesional {} ya tiene {} proyectos activos (máximo {})",
                    proyecto.getProfesionalId(), totalProyectos, MAX_PROYECTOS_ACTIVOS);
            throw new Exception("Ya tienes el máximo de " + MAX_PROYECTOS_ACTIVOS + " proyectos en tu portafolio. " +
                    "Elimina alguno antes de agregar uno nuevo.");
        }

        String sql = "INSERT INTO proyectos_portafolio " +
                    "(profesional_id, nombre_proyecto, fecha_realizacion, descripcion, " +
                    "categoria_id, solicitud_servicio_id, orden, activo, " +
                    "fecha_creacion, fecha_actualizacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE, NOW(), NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, proyecto.getProfesionalId());
            stmt.setString(2, proyecto.getNombreProyecto());
            stmt.setDate(3, Date.valueOf(proyecto.getFechaRealizacion()));
            stmt.setString(4, proyecto.getDescripcion());

            // ✅ Categoría (puede ser null)
            if (proyecto.getCategoriaId() != null) {
                stmt.setInt(5, proyecto.getCategoriaId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

            // ✅ Solicitud de servicio (puede ser null)
            if (proyecto.getSolicitudServicioId() != null) {
                stmt.setInt(6, proyecto.getSolicitudServicioId());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setInt(7, proyecto.getOrden() != null ? proyecto.getOrden() : 1);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        proyecto.setId(id);
                        logger.info("✓ Proyecto guardado exitosamente con ID {}", id);
                        return id;
                    }
                }
            }

            throw new Exception("No se pudo guardar el proyecto");

        } catch (SQLException e) {
            logger.error("✗ Error al guardar proyecto", e);
            throw new Exception("Error al guardar proyecto: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza un proyecto existente.
     * NO permite actualizar la calificación (esto solo lo hace el módulo de clientes).
     *
     * @param proyecto Proyecto con datos actualizados
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    @Override
    public boolean actualizar(ProyectoPortafolio proyecto) throws Exception {
        logger.info("Actualizando proyecto ID {}", proyecto.getId());

        // ✅ NO incluye calificacion_cliente ni comentario_cliente (solo clientes pueden actualizarlos)
        String sql = "UPDATE proyectos_portafolio SET " +
                    "nombre_proyecto = ?, " +
                    "fecha_realizacion = ?, " +
                    "descripcion = ?, " +
                    "categoria_id = ?, " +
                    "orden = ?, " +
                    "fecha_actualizacion = NOW() " +
                    "WHERE id = ? AND profesional_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, proyecto.getNombreProyecto());
            stmt.setDate(2, Date.valueOf(proyecto.getFechaRealizacion()));
            stmt.setString(3, proyecto.getDescripcion());

            if (proyecto.getCategoriaId() != null) {
                stmt.setInt(4, proyecto.getCategoriaId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setInt(5, proyecto.getOrden() != null ? proyecto.getOrden() : 1);
            stmt.setInt(6, proyecto.getId());
            stmt.setInt(7, proyecto.getProfesionalId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Proyecto actualizado exitosamente");
                return true;
            }

            logger.warn("No se actualizó ningún proyecto. Verificar que existe y pertenece al profesional");
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al actualizar proyecto", e);
            throw new Exception("Error al actualizar proyecto", e);
        }
    }

    /**
     * Elimina (soft delete) un proyecto.
     * Marca el proyecto como inactivo en lugar de eliminarlo físicamente.
     * Las imágenes asociadas NO se eliminan (se mantienen para auditoría).
     *
     * @param id ID del proyecto a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    @Override
    public boolean eliminar(Integer id) throws Exception {
        logger.info("Eliminando (soft delete) proyecto ID {}", id);

        String sql = "UPDATE proyectos_portafolio SET " +
                    "activo = FALSE, " +
                    "fecha_actualizacion = NOW() " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Proyecto eliminado (soft delete) exitosamente");
                return true;
            }

            logger.warn("No se eliminó ningún proyecto con ID {}", id);
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al eliminar proyecto", e);
            throw new Exception("Error al eliminar proyecto", e);
        }
    }

    /**
     * Cuenta el número de proyectos activos de un profesional.
     * Usado para validar el límite de 20 proyectos.
     *
     * @param profesionalId ID del profesional
     * @return Número de proyectos activos
     * @throws Exception si hay error en la consulta
     */
    @Override
    public int contarActivosPorProfesional(Integer profesionalId) throws Exception {
        logger.debug("Contando proyectos activos del profesional {}", profesionalId);

        String sql = "SELECT COUNT(*) FROM proyectos_portafolio " +
                    "WHERE profesional_id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt(1);
                    logger.debug("Profesional {} tiene {} proyectos activos", profesionalId, total);
                    return total;
                }
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error al contar proyectos del profesional {}", profesionalId, e);
            throw new Exception("Error al contar proyectos", e);
        }
    }

    /**
     * Actualiza la calificación de un proyecto.
     * IMPORTANTE: Este método solo debe ser llamado desde el módulo de valoración de clientes.
     * El profesional NO puede modificar su propia calificación.
     *
     * @param proyectoId ID del proyecto
     * @param calificacion Calificación del cliente (0-10)
     * @param comentario Comentario del cliente
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    @Override
    public boolean actualizarCalificacion(Integer proyectoId, Double calificacion, String comentario) throws Exception {
        logger.info("Actualizando calificación del proyecto {} - Calificación: {}", proyectoId, calificacion);

        // ✅ Validar rango de calificación (0-10)
        if (calificacion != null && (calificacion < 0 || calificacion > 10)) {
            throw new IllegalArgumentException("La calificación debe estar entre 0 y 10");
        }

        String sql = "UPDATE proyectos_portafolio SET " +
                    "calificacion_cliente = ?, " +
                    "comentario_cliente = ?, " +
                    "fecha_actualizacion = NOW() " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (calificacion != null) {
                stmt.setBigDecimal(1, BigDecimal.valueOf(calificacion));
            } else {
                stmt.setNull(1, Types.DECIMAL);
            }

            stmt.setString(2, comentario);
            stmt.setInt(3, proyectoId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Calificación del proyecto actualizada exitosamente");
                return true;
            }

            logger.warn("No se actualizó ningún proyecto con ID {}", proyectoId);
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al actualizar calificación del proyecto", e);
            throw new Exception("Error al actualizar calificación", e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto ProyectoPortafolio.
     * Incluye el nombre de la categoría si está disponible.
     *
     * @param rs ResultSet con los datos del proyecto
     * @return Objeto ProyectoPortafolio mapeado
     * @throws SQLException si hay error al leer el ResultSet
     */
    private ProyectoPortafolio mapearProyecto(ResultSet rs) throws SQLException {
        ProyectoPortafolio proyecto = new ProyectoPortafolio();

        proyecto.setId(rs.getInt("id"));
        proyecto.setProfesionalId(rs.getInt("profesional_id"));
        proyecto.setNombreProyecto(rs.getString("nombre_proyecto"));

        // ✅ Fecha de realización
        Date fechaRealizacion = rs.getDate("fecha_realizacion");
        if (fechaRealizacion != null) {
            proyecto.setFechaRealizacion(fechaRealizacion.toLocalDate());
        }

        proyecto.setDescripcion(rs.getString("descripcion"));
        proyecto.setCategoriaId(rs.getInt("categoria_id"));

        // ✅ Nombre de categoría (del JOIN)
        proyecto.setCategoriaNombre(rs.getString("categoria_nombre"));

        // ✅ Solicitud de servicio (puede ser null)
        int solicitudId = rs.getInt("solicitud_servicio_id");
        if (!rs.wasNull()) {
            proyecto.setSolicitudServicioId(solicitudId);
        }

        // ✅ Calificación del cliente (puede ser null)
        BigDecimal calificacion = rs.getBigDecimal("calificacion_cliente");
        if (calificacion != null) {
            proyecto.setCalificacionCliente(calificacion);
        }

        proyecto.setComentarioCliente(rs.getString("comentario_cliente"));
        proyecto.setOrden(rs.getInt("orden"));
        proyecto.setActivo(rs.getBoolean("activo"));

        // ✅ Timestamps
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            proyecto.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            proyecto.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }

        return proyecto;
    }
}
