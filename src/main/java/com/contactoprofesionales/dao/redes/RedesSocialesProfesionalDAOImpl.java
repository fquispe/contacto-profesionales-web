package com.contactoprofesionales.dao.redes;

import com.contactoprofesionales.model.RedSocialProfesional;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;

/**
 * Implementación del DAO para redes sociales del profesional.
 *
 * Maneja los enlaces a redes sociales del profesional.
 * Implementa actualización masiva para guardar todas las redes en una transacción.
 *
 * Aplicación de SRP: Solo se encarga de persistencia de redes sociales.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class RedesSocialesProfesionalDAOImpl implements RedesSocialesProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(RedesSocialesProfesionalDAOImpl.class);

    @Override
    public List<RedSocialProfesional> listarPorProfesional(Integer profesionalId) throws Exception {
        logger.info("Listando redes sociales del profesional {}", profesionalId);

        List<RedSocialProfesional> redes = new ArrayList<>();

        String sql = "SELECT id, profesional_id, tipo_red, url, verificada, " +
                    "activo, fecha_creacion, fecha_actualizacion " +
                    "FROM redes_sociales_profesional " +
                    "WHERE profesional_id = ? AND activo = TRUE " +
                    "ORDER BY tipo_red ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    redes.add(mapearRedSocial(rs));
                }
            }

            logger.debug("Se encontraron {} redes sociales para el profesional {}", redes.size(), profesionalId);
            return redes;

        } catch (SQLException e) {
            logger.error("Error al listar redes sociales del profesional {}", profesionalId, e);
            throw new Exception("Error al obtener redes sociales del profesional", e);
        }
    }

    @Override
    public Integer guardar(RedSocialProfesional red) throws Exception {
        logger.info("Guardando red social {} para profesional {}", red.getTipoRed(), red.getProfesionalId());

        String sql = "INSERT INTO redes_sociales_profesional " +
                    "(profesional_id, tipo_red, url, verificada, activo, fecha_creacion, fecha_actualizacion) " +
                    "VALUES (?, ?, ?, FALSE, TRUE, NOW(), NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, red.getProfesionalId());
            stmt.setString(2, red.getTipoRed());
            stmt.setString(3, red.getUrl());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        red.setId(id);
                        logger.info("✓ Red social guardada exitosamente con ID {}", id);
                        return id;
                    }
                }
            }

            throw new Exception("No se pudo guardar la red social");

        } catch (SQLException e) {
            logger.error("✗ Error al guardar red social", e);
            throw new Exception("Error al guardar red social", e);
        }
    }

    @Override
    public boolean actualizar(RedSocialProfesional red) throws Exception {
        logger.info("Actualizando red social ID {}", red.getId());

        String sql = "UPDATE redes_sociales_profesional SET " +
                    "url = ?, " +
                    "fecha_actualizacion = NOW() " +
                    "WHERE id = ? AND profesional_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, red.getUrl());
            stmt.setInt(2, red.getId());
            stmt.setInt(3, red.getProfesionalId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Red social actualizada exitosamente");
                return true;
            }

            logger.warn("No se actualizó ninguna red social");
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al actualizar red social", e);
            throw new Exception("Error al actualizar red social", e);
        }
    }

    @Override
    public boolean eliminar(Integer id) throws Exception {
        logger.info("Eliminando (soft delete) red social ID {}", id);

        String sql = "UPDATE redes_sociales_profesional SET " +
                    "activo = FALSE, " +
                    "fecha_actualizacion = NOW() " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Red social eliminada (soft delete) exitosamente");
                return true;
            }

            logger.warn("No se eliminó ninguna red social con ID {}", id);
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al eliminar red social", e);
            throw new Exception("Error al eliminar red social", e);
        }
    }

    /**
     * Guarda o actualiza múltiples redes sociales en una transacción.
     * Lógica similar a la de especialidades:
     * 1. Desactiva las que no vienen en la lista
     * 2. Actualiza las que vienen con ID
     * 3. Inserta las que vienen sin ID
     *
     * @param profesionalId ID del profesional
     * @param redes Lista de redes sociales
     * @return true si la operación fue exitosa
     * @throws Exception si hay error en la transacción
     */
    @Override
    public boolean guardarMultiples(Integer profesionalId, List<RedSocialProfesional> redes) throws Exception {
        logger.info("Guardando {} redes sociales para profesional {}", redes.size(), profesionalId);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Obtener IDs de redes que vienen en la solicitud
            Set<Integer> idsEnviados = new HashSet<>();
            for (RedSocialProfesional red : redes) {
                if (red.getId() != null && red.getId() > 0) {
                    idsEnviados.add(red.getId());
                }
            }

            // 2. Desactivar redes que ya NO vienen en la lista
            String sqlDesactivar = "UPDATE redes_sociales_profesional " +
                                  "SET activo = FALSE, fecha_actualizacion = NOW() " +
                                  "WHERE profesional_id = ? AND activo = TRUE";

            if (!idsEnviados.isEmpty()) {
                sqlDesactivar += " AND id NOT IN (" +
                               String.join(",", java.util.Collections.nCopies(idsEnviados.size(), "?")) +
                               ")";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDesactivar)) {
                stmt.setInt(1, profesionalId);
                int paramIndex = 2;
                for (Integer id : idsEnviados) {
                    stmt.setInt(paramIndex++, id);
                }
                int desactivadas = stmt.executeUpdate();
                logger.debug("Desactivadas {} redes sociales que ya no están en la lista", desactivadas);
            }

            // 3. Procesar cada red: actualizar si existe o insertar si es nueva
            for (RedSocialProfesional red : redes) {
                red.setProfesionalId(profesionalId);

                if (red.getId() != null && red.getId() > 0) {
                    // Actualizar red existente
                    actualizarInterno(conn, red);
                } else {
                    // Insertar nueva red
                    insertarInterno(conn, red);
                }
            }

            conn.commit();
            logger.info("✓ Redes sociales guardadas exitosamente");
            return true;

        } catch (Exception e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Rollback realizado por error al guardar redes sociales");
                } catch (SQLException ex) {
                    logger.error("Error haciendo rollback", ex);
                }
            }
            logger.error("✗ Error al guardar redes sociales", e);
            throw new Exception("Error al guardar redes sociales", e);

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

    private void actualizarInterno(Connection conn, RedSocialProfesional red) throws SQLException {
        String sql = "UPDATE redes_sociales_profesional SET " +
                    "url = ?, activo = TRUE, fecha_actualizacion = NOW() " +
                    "WHERE id = ? AND profesional_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, red.getUrl());
            stmt.setInt(2, red.getId());
            stmt.setInt(3, red.getProfesionalId());

            stmt.executeUpdate();
            logger.debug("Red social {} actualizada", red.getTipoRed());
        }
    }

    private void insertarInterno(Connection conn, RedSocialProfesional red) throws SQLException {
        String sql = "INSERT INTO redes_sociales_profesional " +
                    "(profesional_id, tipo_red, url, verificada, activo, fecha_creacion, fecha_actualizacion) " +
                    "VALUES (?, ?, ?, FALSE, TRUE, NOW(), NOW())";

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, red.getProfesionalId());
            stmt.setString(2, red.getTipoRed());
            stmt.setString(3, red.getUrl());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    red.setId(rs.getInt(1));
                    logger.debug("Red social {} insertada con ID {}", red.getTipoRed(), red.getId());
                }
            }
        }
    }

    private RedSocialProfesional mapearRedSocial(ResultSet rs) throws SQLException {
        RedSocialProfesional red = new RedSocialProfesional();

        red.setId(rs.getInt("id"));
        red.setProfesionalId(rs.getInt("profesional_id"));
        red.setTipoRed(rs.getString("tipo_red"));
        red.setUrl(rs.getString("url"));
        red.setVerificada(rs.getBoolean("verificada"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            red.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        return red;
    }
}
