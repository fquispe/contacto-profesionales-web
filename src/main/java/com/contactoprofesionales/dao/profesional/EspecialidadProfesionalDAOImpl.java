package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.EspecialidadProfesional;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de EspecialidadProfesionalDAO usando JDBC para PostgreSQL con HikariCP
 * Utiliza try-with-resources para gestión automática de recursos
 */
public class EspecialidadProfesionalDAOImpl implements EspecialidadProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(EspecialidadProfesionalDAOImpl.class);

    private static final String INSERT_ESPECIALIDAD =
        "INSERT INTO especialidades_profesional (profesional_id, categoria_id, es_principal, " +
        "anios_experiencia, descripcion, fecha_creacion) " +
        "VALUES (?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String SELECT_BY_PROFESIONAL =
        "SELECT e.*, c.nombre AS categoria_nombre, c.descripcion AS categoria_descripcion " +
        "FROM especialidades_profesional e " +
        "INNER JOIN categorias_servicio c ON e.categoria_id = c.id " +
        "WHERE e.profesional_id = ? " +
        "ORDER BY e.es_principal DESC, e.fecha_creacion ASC";

    private static final String DELETE_ESPECIALIDAD =
        "DELETE FROM especialidades_profesional WHERE id = ?";

    private static final String DESMARCAR_PRINCIPAL =
        "UPDATE especialidades_profesional SET es_principal = false " +
        "WHERE profesional_id = ? AND es_principal = true";

    private static final String MARCAR_PRINCIPAL =
        "UPDATE especialidades_profesional SET es_principal = true WHERE id = ?";

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

        logger.debug("Registrando nueva especialidad para profesional ID: {}", especialidad.getProfesionalId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_ESPECIALIDAD)) {

            int index = 1;
            ps.setInt(index++, especialidad.getProfesionalId());
            ps.setInt(index++, especialidad.getCategoriaId());
            ps.setBoolean(index++, especialidad.getEsPrincipal() != null ? especialidad.getEsPrincipal() : false);
            ps.setObject(index++, especialidad.getAniosExperiencia());
            ps.setString(index++, especialidad.getDescripcion());
            ps.setTimestamp(index++, Timestamp.valueOf(java.time.LocalDateTime.now()));

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

            logger.info("Se encontraron {} especialidades para el profesional {}", especialidades.size(), profesionalId);
            return especialidades;

        } catch (SQLException e) {
            logger.error("Error al listar especialidades del profesional", e);
            throw new DatabaseException("Error al listar especialidades del profesional: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(Integer id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Eliminando especialidad ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_ESPECIALIDAD)) {

            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Especialidad eliminada: {}", id);
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

    /**
     * Mapea un ResultSet a un objeto EspecialidadProfesional
     * Incluye datos de la categoría mediante JOIN
     */
    private EspecialidadProfesional mapEspecialidad(ResultSet rs) throws SQLException {
        EspecialidadProfesional especialidad = new EspecialidadProfesional();

        especialidad.setId(rs.getInt("id"));
        especialidad.setProfesionalId(rs.getInt("profesional_id"));
        especialidad.setCategoriaId(rs.getInt("categoria_id"));
        especialidad.setEsPrincipal(rs.getBoolean("es_principal"));

        Integer aniosExperiencia = (Integer) rs.getObject("anios_experiencia");
        especialidad.setAniosExperiencia(aniosExperiencia);

        especialidad.setDescripcion(rs.getString("descripcion"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            especialidad.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        // Datos de la categoría (del JOIN)
        especialidad.setCategoriaNombre(rs.getString("categoria_nombre"));
        especialidad.setCategoriaDescripcion(rs.getString("categoria_descripcion"));

        return especialidad;
    }
}
