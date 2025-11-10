package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.RedSocialProfesional;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de RedSocialProfesionalDAO usando JDBC para PostgreSQL con HikariCP
 * Utiliza try-with-resources para gestión automática de recursos
 */
public class RedSocialProfesionalDAOImpl implements RedSocialProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(RedSocialProfesionalDAOImpl.class);

    private static final String INSERT_RED_SOCIAL =
        "INSERT INTO redes_sociales_profesional (profesional_id, tipo_red, url, verificada, fecha_creacion) " +
        "VALUES (?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE_RED_SOCIAL =
        "UPDATE redes_sociales_profesional SET tipo_red = ?, url = ?, verificada = ? WHERE id = ?";

    private static final String SELECT_BY_PROFESIONAL =
        "SELECT * FROM redes_sociales_profesional WHERE profesional_id = ? ORDER BY fecha_creacion ASC";

    private static final String DELETE_RED_SOCIAL =
        "DELETE FROM redes_sociales_profesional WHERE id = ?";

    @Override
    public RedSocialProfesional registrar(RedSocialProfesional redSocial) throws DatabaseException {
        if (redSocial == null) {
            throw new DatabaseException("La red social no puede ser nula");
        }
        if (redSocial.getProfesionalId() == null) {
            throw new DatabaseException("El ID del profesional no puede ser nulo");
        }
        if (redSocial.getTipoRed() == null || redSocial.getTipoRed().trim().isEmpty()) {
            throw new DatabaseException("El tipo de red no puede ser nulo o vacío");
        }
        if (redSocial.getUrl() == null || redSocial.getUrl().trim().isEmpty()) {
            throw new DatabaseException("La URL no puede ser nula o vacía");
        }

        logger.debug("Registrando nueva red social {} para profesional ID: {}",
            redSocial.getTipoRed(), redSocial.getProfesionalId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_RED_SOCIAL)) {

            int index = 1;
            ps.setInt(index++, redSocial.getProfesionalId());
            ps.setString(index++, redSocial.getTipoRed());
            ps.setString(index++, redSocial.getUrl());
            ps.setBoolean(index++, redSocial.getVerificada() != null ? redSocial.getVerificada() : false);
            ps.setTimestamp(index++, Timestamp.valueOf(java.time.LocalDateTime.now()));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    redSocial.setId(rs.getInt("id"));
                    logger.info("Red social registrada exitosamente con ID: {}", redSocial.getId());
                    return redSocial;
                }
            }

            throw new DatabaseException("No se pudo registrar la red social");

        } catch (SQLException e) {
            logger.error("Error al registrar red social", e);
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new DatabaseException("Error de integridad: verifique que el profesional exista", e);
            }
            throw new DatabaseException("Error al registrar red social: " + e.getMessage(), e);
        }
    }

    @Override
    public List<RedSocialProfesional> listarPorProfesional(Integer profesionalId) throws DatabaseException {
        if (profesionalId == null) {
            throw new DatabaseException("El ID del profesional no puede ser nulo");
        }

        logger.debug("Listando redes sociales del profesional ID: {}", profesionalId);
        List<RedSocialProfesional> redesSociales = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_PROFESIONAL)) {

            ps.setInt(1, profesionalId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    redesSociales.add(mapRedSocial(rs));
                }
            }

            logger.info("Se encontraron {} redes sociales para el profesional {}", redesSociales.size(), profesionalId);
            return redesSociales;

        } catch (SQLException e) {
            logger.error("Error al listar redes sociales del profesional", e);
            throw new DatabaseException("Error al listar redes sociales del profesional: " + e.getMessage(), e);
        }
    }

    @Override
    public RedSocialProfesional actualizar(RedSocialProfesional redSocial) throws DatabaseException {
        if (redSocial == null || redSocial.getId() == null) {
            throw new DatabaseException("La red social y su ID no pueden ser nulos");
        }
        if (redSocial.getTipoRed() == null || redSocial.getTipoRed().trim().isEmpty()) {
            throw new DatabaseException("El tipo de red no puede ser nulo o vacío");
        }
        if (redSocial.getUrl() == null || redSocial.getUrl().trim().isEmpty()) {
            throw new DatabaseException("La URL no puede ser nula o vacía");
        }

        logger.debug("Actualizando red social ID: {}", redSocial.getId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_RED_SOCIAL)) {

            int index = 1;
            ps.setString(index++, redSocial.getTipoRed());
            ps.setString(index++, redSocial.getUrl());
            ps.setBoolean(index++, redSocial.getVerificada() != null ? redSocial.getVerificada() : false);
            ps.setInt(index++, redSocial.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Red social no encontrada");
            }

            logger.info("Red social actualizada exitosamente: {}", redSocial.getId());
            return redSocial;

        } catch (SQLException e) {
            logger.error("Error al actualizar red social", e);
            throw new DatabaseException("Error al actualizar red social: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean eliminar(Integer id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Eliminando red social ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_RED_SOCIAL)) {

            ps.setInt(1, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Red social eliminada: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error al eliminar red social", e);
            throw new DatabaseException("Error al eliminar red social: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto RedSocialProfesional
     */
    private RedSocialProfesional mapRedSocial(ResultSet rs) throws SQLException {
        RedSocialProfesional redSocial = new RedSocialProfesional();

        redSocial.setId(rs.getInt("id"));
        redSocial.setProfesionalId(rs.getInt("profesional_id"));
        redSocial.setTipoRed(rs.getString("tipo_red"));
        redSocial.setUrl(rs.getString("url"));
        redSocial.setVerificada(rs.getBoolean("verificada"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            redSocial.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        return redSocial;
    }
}
