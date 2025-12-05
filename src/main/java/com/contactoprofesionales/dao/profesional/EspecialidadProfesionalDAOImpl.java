package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.dto.ModalidadTrabajoDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.EspecialidadProfesional;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de EspecialidadProfesionalDAO usando JDBC para PostgreSQL con HikariCP
 * Utiliza try-with-resources para gestión automática de recursos
 * VERSIÓN CORREGIDA: Alineada con la estructura real de la tabla especialidades_profesional
 */
public class EspecialidadProfesionalDAOImpl implements EspecialidadProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(EspecialidadProfesionalDAOImpl.class);

    // CORREGIDO: INSERT con los campos reales de la tabla
    private static final String INSERT_ESPECIALIDAD =
        "INSERT INTO especialidades_profesional (profesional_id, categoria_id, descripcion, " +
        "incluye_materiales, costo, tipo_costo, es_principal, orden, " +
        "fecha_creacion, fecha_actualizacion, activo) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), true) RETURNING id";

    // CORREGIDO: SELECT incluyendo todos los campos y más datos de categoría
    private static final String SELECT_BY_PROFESIONAL =
        "SELECT e.id, e.profesional_id, e.categoria_id, e.descripcion, e.incluye_materiales, " +
        "e.costo, e.tipo_costo, e.es_principal, e.orden, e.fecha_creacion, " +
        "e.fecha_actualizacion, e.activo, " +
        "c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion, " +
        "c.icono AS categoria_icono, c.color AS categoria_color " +
        "FROM especialidades_profesional e " +
        "INNER JOIN categorias_servicio c ON e.categoria_id = c.id " +
        "WHERE e.profesional_id = ? AND e.activo = true " +
        "ORDER BY e.orden ASC";

    private static final String SELECT_BY_ID =
        "SELECT e.id, e.profesional_id, e.categoria_id, e.descripcion, e.incluye_materiales, " +
        "e.costo, e.tipo_costo, e.es_principal, e.orden, e.fecha_creacion, " +
        "e.fecha_actualizacion, e.activo, " +
        "c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion, " +
        "c.icono AS categoria_icono, c.color AS categoria_color " +
        "FROM especialidades_profesional e " +
        "INNER JOIN categorias_servicio c ON e.categoria_id = c.id " +
        "WHERE e.id = ? AND e.activo = true";

    // CORREGIDO: UPDATE para actualizar especialidades
    private static final String UPDATE_ESPECIALIDAD =
        "UPDATE especialidades_profesional " +
        "SET descripcion = ?, incluye_materiales = ?, costo = ?, tipo_costo = ?, " +
        "fecha_actualizacion = NOW() " +
        "WHERE id = ? AND activo = true";

    // CORREGIDO: Soft delete en lugar de DELETE físico
    private static final String DELETE_ESPECIALIDAD =
        "UPDATE especialidades_profesional SET activo = false, fecha_actualizacion = NOW() " +
        "WHERE id = ?";

    private static final String DESMARCAR_PRINCIPAL =
        "UPDATE especialidades_profesional SET es_principal = false, fecha_actualizacion = NOW() " +
        "WHERE profesional_id = ? AND es_principal = true";

    private static final String MARCAR_PRINCIPAL =
        "UPDATE especialidades_profesional SET es_principal = true, fecha_actualizacion = NOW() " +
        "WHERE id = ?";

    private static final String COUNT_BY_PROFESIONAL =
        "SELECT COUNT(*) AS total FROM especialidades_profesional " +
        "WHERE profesional_id = ? AND activo = true";

    private static final String EXISTS_BY_CATEGORIA =
        "SELECT COUNT(*) AS existe FROM especialidades_profesional " +
        "WHERE profesional_id = ? AND categoria_id = ? AND activo = true";

    @Override
    public EspecialidadProfesional registrar(EspecialidadProfesional especialidad) throws DatabaseException {
        if (especialidad == null) {
            throw new DatabaseException("La especialidad no puede ser nula");
        }
        if (especialidad.getProfesionalId() == null) {
            throw new DatabaseException("El ID del profesional no puede ser nulo");
        }
        if (especialidad.getCategoriaId() == null) {
            throw new DatabaseException("El ID de la categoría no puede ser nulo");
        }
        // NUEVO: Validar campos obligatorios
        if (especialidad.getCosto() == null) {
            throw new DatabaseException("El costo no puede ser nulo");
        }
        if (especialidad.getTipoCosto() == null) {
            throw new DatabaseException("El tipo de costo no puede ser nulo");
        }
        if (especialidad.getOrden() == null) {
            throw new DatabaseException("El orden no puede ser nulo");
        }

        logger.debug("Registrando nueva especialidad para profesional ID: {}", especialidad.getProfesionalId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ESPECIALIDAD)) {

            int index = 1;
            ps.setInt(index++, especialidad.getProfesionalId());
            ps.setInt(index++, especialidad.getCategoriaId());
            
            // Descripción (puede ser null)
            if (especialidad.getDescripcion() != null) {
                ps.setString(index++, especialidad.getDescripcion());
            } else {
                ps.setNull(index++, Types.VARCHAR);
            }
            
            // CAMPOS NUEVOS correctos
            ps.setBoolean(index++, especialidad.getIncluyeMateriales() != null ? 
                         especialidad.getIncluyeMateriales() : false);
            ps.setDouble(index++, especialidad.getCosto());
            ps.setString(index++, especialidad.getTipoCosto());
            ps.setBoolean(index++, especialidad.getEsPrincipal() != null ? 
                         especialidad.getEsPrincipal() : false);
            ps.setInt(index++, especialidad.getOrden());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    especialidad.setId(rs.getInt("id"));
                    logger.info("Especialidad registrada exitosamente con ID: {}", especialidad.getId());
                    return especialidad;
                }
            }

            throw new DatabaseException("No se pudo registrar la especialidad");

        } catch (SQLException e) {
            logger.error("Error al registrar especialidad", e);
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new DatabaseException("Error de integridad: verifique que el profesional y la categoría existan", e);
            }
            throw new DatabaseException("Error al registrar especialidad: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<EspecialidadProfesional> buscarPorId(Integer id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Buscando especialidad ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    EspecialidadProfesional especialidad = mapEspecialidad(rs);
                    logger.debug("Especialidad encontrada: {}", id);
                    return Optional.of(especialidad);
                }
            }

            logger.debug("No se encontró especialidad con ID: {}", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar especialidad", e);
            throw new DatabaseException("Error al buscar especialidad: " + e.getMessage(), e);
        }
    }

    @Override
    public List<EspecialidadProfesional> listarPorProfesional(Integer profesionalId) throws DatabaseException {
        if (profesionalId == null) {
            throw new DatabaseException("El ID del profesional no puede ser nulo");
        }

        logger.debug("Listando especialidades del profesional ID: {}", profesionalId);
        List<EspecialidadProfesional> especialidades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PROFESIONAL)) {

            ps.setInt(1, profesionalId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    especialidades.add(mapEspecialidad(rs));
                }
            }

            logger.info("Se encontraron {} especialidades para el profesional {}", 
                       especialidades.size(), profesionalId);
            return especialidades;

        } catch (SQLException e) {
            logger.error("Error al listar especialidades del profesional", e);
            throw new DatabaseException("Error al listar especialidades del profesional: " + e.getMessage(), e);
        }
    }

    @Override
    public EspecialidadProfesional actualizar(EspecialidadProfesional especialidad) throws DatabaseException {
        if (especialidad == null || especialidad.getId() == null) {
            throw new DatabaseException("La especialidad y su ID no pueden ser nulos");
        }

        logger.debug("Actualizando especialidad ID: {}", especialidad.getId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_ESPECIALIDAD)) {

            int index = 1;
            
            // Descripción (puede ser null)
            if (especialidad.getDescripcion() != null) {
                ps.setString(index++, especialidad.getDescripcion());
            } else {
                ps.setNull(index++, Types.VARCHAR);
            }
            
            ps.setBoolean(index++, especialidad.getIncluyeMateriales());
            ps.setDouble(index++, especialidad.getCosto());
            ps.setString(index++, especialidad.getTipoCosto());
            ps.setInt(index++, especialidad.getId());

            int rowsAffected = ps.executeUpdate();

            if (rowsAffected == 0) {
                throw new DatabaseException("No se encontró la especialidad a actualizar");
            }

            logger.info("Especialidad actualizada exitosamente: {}", especialidad.getId());
            
            // Recargar la especialidad desde la BD
            return buscarPorId(especialidad.getId())
                .orElseThrow(() -> new DatabaseException("Error al recargar especialidad actualizada"));

        } catch (SQLException e) {
            logger.error("Error al actualizar especialidad", e);
            throw new DatabaseException("Error al actualizar especialidad: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(Integer id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Eliminando (soft delete) especialidad ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ESPECIALIDAD)) {

            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Especialidad eliminada (soft delete): {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error al eliminar especialidad", e);
            throw new DatabaseException("Error al eliminar especialidad: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean marcarComoPrincipal(Integer id, Integer profesionalId) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }
        if (profesionalId == null) {
            throw new DatabaseException("El ID del profesional no puede ser nulo");
        }

        logger.debug("Marcando especialidad {} como principal para profesional {}", id, profesionalId);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // Primero desmarcar todas las especialidades principales del profesional
            try (PreparedStatement ps1 = conn.prepareStatement(DESMARCAR_PRINCIPAL)) {
                ps1.setInt(1, profesionalId);
                ps1.executeUpdate();
            }

            // Luego marcar la especialidad indicada como principal
            try (PreparedStatement ps2 = conn.prepareStatement(MARCAR_PRINCIPAL)) {
                ps2.setInt(1, id);
                int rowsAffected = ps2.executeUpdate();

                if (rowsAffected > 0) {
                    conn.commit();
                    logger.info("Especialidad {} marcada como principal para profesional {}", id, profesionalId);
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al marcar especialidad como principal", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    logger.error("Error al hacer rollback", ex);
                }
            }
            throw new DatabaseException("Error al marcar especialidad como principal: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error al cerrar conexión", e);
                }
            }
        }
    }

    @Override
    public int contarPorProfesional(Integer profesionalId) throws DatabaseException {
        if (profesionalId == null) {
            throw new DatabaseException("El ID del profesional no puede ser nulo");
        }

        logger.debug("Contando especialidades para profesional ID: {}", profesionalId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_BY_PROFESIONAL)) {

            ps.setInt(1, profesionalId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    logger.debug("Profesional {} tiene {} especialidades", profesionalId, total);
                    return total;
                }
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error al contar especialidades", e);
            throw new DatabaseException("Error al contar especialidades: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeEspecialidadConCategoria(Integer profesionalId, Integer categoriaId) 
            throws DatabaseException {
        if (profesionalId == null || categoriaId == null) {
            throw new DatabaseException("Los IDs no pueden ser nulos");
        }

        logger.debug("Verificando si profesional {} tiene categoría {}", profesionalId, categoriaId);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_BY_CATEGORIA)) {

            ps.setInt(1, profesionalId);
            ps.setInt(2, categoriaId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean existe = rs.getInt("existe") > 0;
                    logger.debug("Especialidad {} para profesional {}: {}", 
                               categoriaId, profesionalId, existe ? "existe" : "no existe");
                    return existe;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error al verificar especialidad", e);
            throw new DatabaseException("Error al verificar existencia de especialidad: " + e.getMessage(), e);
        }
    }

    /**
     * Obtiene los flags de modalidad de trabajo (remoto/presencial) de una especialidad.
     * Implementación agregada en Migración V008.
     *
     * @param especialidadId ID de la especialidad
     * @return ModalidadTrabajoDTO con los flags, o null si no existe
     * @throws DatabaseException si ocurre un error en la BD
     */
    @Override
    public ModalidadTrabajoDTO obtenerModalidadTrabajo(Integer especialidadId) throws DatabaseException {
        if (especialidadId == null) {
            throw new DatabaseException("El ID de especialidad no puede ser nulo");
        }

        logger.debug("Obteniendo modalidad de trabajo para especialidad ID: {}", especialidadId);

        // Query para obtener los flags trabajo_remoto y trabajo_presencial
        String sql = "SELECT id, trabajo_remoto, trabajo_presencial " +
                    "FROM especialidades_profesional " +
                    "WHERE id = ? AND activo = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, especialidadId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ModalidadTrabajoDTO dto = new ModalidadTrabajoDTO();
                    dto.setEspecialidadId(rs.getInt("id"));
                    dto.setTrabajoRemoto(rs.getBoolean("trabajo_remoto"));
                    dto.setTrabajoPresencial(rs.getBoolean("trabajo_presencial"));

                    logger.debug("Modalidad encontrada - Remoto: {}, Presencial: {}",
                                dto.getTrabajoRemoto(), dto.getTrabajoPresencial());

                    return dto;
                }

                logger.warn("No se encontró especialidad activa con ID: {}", especialidadId);
                return null;
            }

        } catch (SQLException e) {
            logger.error("Error al obtener modalidad de trabajo para especialidad ID: {}", especialidadId, e);
            throw new DatabaseException("Error al obtener modalidad de trabajo: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto EspecialidadProfesional
     * Incluye todos los campos de la tabla y datos de la categoría mediante JOIN
     */
    private EspecialidadProfesional mapEspecialidad(ResultSet rs) throws SQLException {
        EspecialidadProfesional especialidad = new EspecialidadProfesional();

        // Campos de la tabla especialidades_profesional
        especialidad.setId(rs.getInt("id"));
        especialidad.setProfesionalId(rs.getInt("profesional_id"));
        especialidad.setCategoriaId(rs.getInt("categoria_id"));
        
        String descripcion = rs.getString("descripcion");
        especialidad.setDescripcion(rs.wasNull() ? null : descripcion);
        
        especialidad.setIncluyeMateriales(rs.getBoolean("incluye_materiales"));
        especialidad.setCosto(rs.getDouble("costo"));
        especialidad.setTipoCosto(rs.getString("tipo_costo"));
        especialidad.setEsPrincipal(rs.getBoolean("es_principal"));
        especialidad.setOrden(rs.getInt("orden"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            especialidad.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            especialidad.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }

        especialidad.setActivo(rs.getBoolean("activo"));

        // Datos de la categoría (del JOIN)
        try {
            especialidad.setCategoriaNombre(rs.getString("categoria_nombre"));
            especialidad.setCategoriaDescripcion(rs.getString("categoria_descripcion"));
            especialidad.setCategoriaIcono(rs.getString("categoria_icono"));
            especialidad.setCategoriaColor(rs.getString("categoria_color"));
        } catch (SQLException e) {
            // Estos campos podrían no estar presentes en algunas queries
            logger.debug("Campos de categoría no disponibles en el ResultSet");
        }

        return especialidad;
    }
}