package com.contactoprofesionales.dao.antecedentes;

import com.contactoprofesionales.model.AntecedenteProfesional;
import com.contactoprofesionales.model.AntecedenteProfesional.TipoAntecedente;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO para antecedentes profesionales.
 *
 * Maneja antecedentes policiales, penales y judiciales de profesionales.
 * Los antecedentes verificados mejoran la puntuación del profesional en la plataforma.
 *
 * Aplicación de SRP: Solo se encarga de persistencia de antecedentes.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class AntecedentesProfesionalDAOImpl implements AntecedentesProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(AntecedentesProfesionalDAOImpl.class);

    @Override
    public List<AntecedenteProfesional> listarPorProfesional(Integer profesionalId) throws Exception {
        logger.info("Listando antecedentes del profesional {}", profesionalId);

        List<AntecedenteProfesional> antecedentes = new ArrayList<>();

        String sql = "SELECT id, profesional_id, tipo_antecedente, documento_url, " +
                    "fecha_emision, fecha_subida, verificado, fecha_verificacion, " +
                    "observaciones, activo " +
                    "FROM antecedentes_profesional " +
                    "WHERE profesional_id = ? AND activo = TRUE " +
                    "ORDER BY tipo_antecedente ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    antecedentes.add(mapearAntecedente(rs));
                }
            }

            logger.debug("Se encontraron {} antecedentes para el profesional {}", antecedentes.size(), profesionalId);
            return antecedentes;

        } catch (SQLException e) {
            logger.error("Error al listar antecedentes del profesional {}", profesionalId, e);
            throw new Exception("Error al obtener antecedentes del profesional", e);
        }
    }

    @Override
    public Optional<AntecedenteProfesional> buscarPorTipo(Integer profesionalId, TipoAntecedente tipo) throws Exception {
        logger.debug("Buscando antecedente {} del profesional {}", tipo, profesionalId);

        String sql = "SELECT id, profesional_id, tipo_antecedente, documento_url, " +
                    "fecha_emision, fecha_subida, verificado, fecha_verificacion, " +
                    "observaciones, activo " +
                    "FROM antecedentes_profesional " +
                    "WHERE profesional_id = ? AND tipo_antecedente = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);
            stmt.setString(2, tipo.getValor());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AntecedenteProfesional antecedente = mapearAntecedente(rs);
                    logger.debug("Antecedente encontrado: {}", antecedente);
                    return Optional.of(antecedente);
                }
            }

            logger.debug("Antecedente {} no encontrado para profesional {}", tipo, profesionalId);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar antecedente por tipo", e);
            throw new Exception("Error al buscar antecedente", e);
        }
    }

    @Override
    public Integer guardar(AntecedenteProfesional antecedente) throws Exception {
        logger.info("Guardando antecedente {} para profesional {}",
                antecedente.getTipoAntecedente(), antecedente.getProfesionalId());

        // ✅ Verificar que no exista ya un antecedente activo del mismo tipo
        Optional<AntecedenteProfesional> existente = buscarPorTipo(
                antecedente.getProfesionalId(),
                antecedente.getTipoAntecedente()
        );

        if (existente.isPresent()) {
            throw new Exception("Ya existe un antecedente " + antecedente.getTipoAntecedente().getValor() +
                    " activo. Elimina el anterior antes de subir uno nuevo.");
        }

        String sql = "INSERT INTO antecedentes_profesional " +
                    "(profesional_id, tipo_antecedente, documento_url, fecha_emision, " +
                    "fecha_subida, verificado, observaciones, activo) " +
                    "VALUES (?, ?, ?, ?, NOW(), FALSE, ?, TRUE)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, antecedente.getProfesionalId());
            stmt.setString(2, antecedente.getTipoAntecedenteString());
            stmt.setString(3, antecedente.getDocumentoUrl());

            if (antecedente.getFechaEmision() != null) {
                stmt.setDate(4, Date.valueOf(antecedente.getFechaEmision()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, antecedente.getObservaciones());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        antecedente.setId(id);
                        logger.info("✓ Antecedente guardado exitosamente con ID {}", id);
                        return id;
                    }
                }
            }

            throw new Exception("No se pudo guardar el antecedente");

        } catch (SQLException e) {
            logger.error("✗ Error al guardar antecedente", e);
            throw new Exception("Error al guardar antecedente", e);
        }
    }

    @Override
    public boolean actualizar(AntecedenteProfesional antecedente) throws Exception {
        logger.info("Actualizando antecedente ID {}", antecedente.getId());

        String sql = "UPDATE antecedentes_profesional SET " +
                    "documento_url = ?, " +
                    "fecha_emision = ?, " +
                    "observaciones = ? " +
                    "WHERE id = ? AND profesional_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, antecedente.getDocumentoUrl());

            if (antecedente.getFechaEmision() != null) {
                stmt.setDate(2, Date.valueOf(antecedente.getFechaEmision()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            stmt.setString(3, antecedente.getObservaciones());
            stmt.setInt(4, antecedente.getId());
            stmt.setInt(5, antecedente.getProfesionalId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Antecedente actualizado exitosamente");
                return true;
            }

            logger.warn("No se actualizó ningún antecedente");
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al actualizar antecedente", e);
            throw new Exception("Error al actualizar antecedente", e);
        }
    }

    @Override
    public boolean verificar(Integer id) throws Exception {
        logger.info("Verificando antecedente ID {}", id);

        String sql = "UPDATE antecedentes_profesional SET " +
                    "verificado = TRUE, " +
                    "fecha_verificacion = NOW() " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Antecedente verificado exitosamente");
                return true;
            }

            logger.warn("No se verificó ningún antecedente con ID {}", id);
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al verificar antecedente", e);
            throw new Exception("Error al verificar antecedente", e);
        }
    }

    @Override
    public boolean eliminar(Integer id) throws Exception {
        logger.info("Eliminando (soft delete) antecedente ID {}", id);

        String sql = "UPDATE antecedentes_profesional SET activo = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Antecedente eliminado (soft delete) exitosamente");
                return true;
            }

            logger.warn("No se eliminó ningún antecedente con ID {}", id);
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al eliminar antecedente", e);
            throw new Exception("Error al eliminar antecedente", e);
        }
    }

    @Override
    public int contarVerificados(Integer profesionalId) throws Exception {
        logger.debug("Contando antecedentes verificados del profesional {}", profesionalId);

        String sql = "SELECT COUNT(*) FROM antecedentes_profesional " +
                    "WHERE profesional_id = ? AND activo = TRUE AND verificado = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt(1);
                    logger.debug("Profesional {} tiene {} antecedentes verificados", profesionalId, total);
                    return total;
                }
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error al contar antecedentes verificados", e);
            throw new Exception("Error al contar antecedentes verificados", e);
        }
    }

    private AntecedenteProfesional mapearAntecedente(ResultSet rs) throws SQLException {
        AntecedenteProfesional antecedente = new AntecedenteProfesional();

        antecedente.setId(rs.getInt("id"));
        antecedente.setProfesionalId(rs.getInt("profesional_id"));

        String tipoStr = rs.getString("tipo_antecedente");
        if (tipoStr != null) {
            antecedente.setTipoAntecedenteString(tipoStr);
        }

        antecedente.setDocumentoUrl(rs.getString("documento_url"));

        Date fechaEmision = rs.getDate("fecha_emision");
        if (fechaEmision != null) {
            antecedente.setFechaEmision(fechaEmision.toLocalDate());
        }

        Timestamp fechaSubida = rs.getTimestamp("fecha_subida");
        if (fechaSubida != null) {
            antecedente.setFechaSubida(fechaSubida.toLocalDateTime());
        }

        antecedente.setVerificado(rs.getBoolean("verificado"));

        Timestamp fechaVerificacion = rs.getTimestamp("fecha_verificacion");
        if (fechaVerificacion != null) {
            antecedente.setFechaVerificacion(fechaVerificacion.toLocalDateTime());
        }

        antecedente.setObservaciones(rs.getString("observaciones"));
        antecedente.setActivo(rs.getBoolean("activo"));

        return antecedente;
    }
}
