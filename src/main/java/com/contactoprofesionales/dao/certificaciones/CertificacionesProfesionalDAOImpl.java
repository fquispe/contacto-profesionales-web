package com.contactoprofesionales.dao.certificaciones;

import com.contactoprofesionales.model.CertificacionProfesional;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO para certificaciones profesionales.
 *
 * Maneja todas las operaciones CRUD para certificaciones de profesionales.
 * Aplicación de SRP: Solo se encarga de persistencia de certificaciones.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class CertificacionesProfesionalDAOImpl implements CertificacionesProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(CertificacionesProfesionalDAOImpl.class);

    /**
     * Lista todas las certificaciones activas de un profesional ordenadas por fecha de obtención (más reciente primero).
     *
     * @param profesionalId ID del profesional
     * @return Lista de certificaciones
     * @throws Exception si hay error en la consulta
     */
    @Override
    public List<CertificacionProfesional> listarPorProfesional(Integer profesionalId) throws Exception {
        logger.info("Listando certificaciones del profesional {}", profesionalId);

        List<CertificacionProfesional> certificaciones = new ArrayList<>();

        String sql = "SELECT id, profesional_id, nombre_certificacion, institucion, " +
                    "fecha_obtencion, fecha_vigencia, documento_url, descripcion, " +
                    "orden, activo, fecha_creacion, fecha_actualizacion " +
                    "FROM certificaciones_profesional " +
                    "WHERE profesional_id = ? AND activo = TRUE " +
                    "ORDER BY fecha_obtencion DESC NULLS LAST, orden ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    certificaciones.add(mapearCertificacion(rs));
                }
            }

            logger.debug("Se encontraron {} certificaciones para el profesional {}", certificaciones.size(), profesionalId);
            return certificaciones;

        } catch (SQLException e) {
            logger.error("Error al listar certificaciones del profesional {}", profesionalId, e);
            throw new Exception("Error al obtener certificaciones del profesional", e);
        }
    }

    /**
     * Busca una certificación específica por su ID.
     *
     * @param id ID de la certificación
     * @return Optional con la certificación si existe
     * @throws Exception si hay error en la consulta
     */
    @Override
    public Optional<CertificacionProfesional> buscarPorId(Integer id) throws Exception {
        logger.debug("Buscando certificación con ID {}", id);

        String sql = "SELECT id, profesional_id, nombre_certificacion, institucion, " +
                    "fecha_obtencion, fecha_vigencia, documento_url, descripcion, " +
                    "orden, activo, fecha_creacion, fecha_actualizacion " +
                    "FROM certificaciones_profesional " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CertificacionProfesional certificacion = mapearCertificacion(rs);
                    logger.debug("Certificación encontrada: {}", certificacion);
                    return Optional.of(certificacion);
                }
            }

            logger.debug("Certificación con ID {} no encontrada", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar certificación con ID {}", id, e);
            throw new Exception("Error al buscar certificación", e);
        }
    }

    /**
     * Guarda una nueva certificación en la base de datos.
     *
     * @param certificacion Certificación a guardar
     * @return ID de la certificación creada
     * @throws Exception si hay error al guardar
     */
    @Override
    public Integer guardar(CertificacionProfesional certificacion) throws Exception {
        logger.info("Guardando nueva certificación: {} para profesional {}",
                certificacion.getNombreCertificacion(), certificacion.getProfesionalId());

        String sql = "INSERT INTO certificaciones_profesional " +
                    "(profesional_id, nombre_certificacion, institucion, fecha_obtencion, " +
                    "fecha_vigencia, documento_url, descripcion, orden, activo, " +
                    "fecha_creacion, fecha_actualizacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, TRUE, NOW(), NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, certificacion.getProfesionalId());
            stmt.setString(2, certificacion.getNombreCertificacion());
            stmt.setString(3, certificacion.getInstitucion());

            // ✅ Fecha de obtención (puede ser null)
            if (certificacion.getFechaObtencion() != null) {
                stmt.setDate(4, Date.valueOf(certificacion.getFechaObtencion()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            // ✅ Fecha de vigencia (puede ser null)
            if (certificacion.getFechaVigencia() != null) {
                stmt.setDate(5, Date.valueOf(certificacion.getFechaVigencia()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            stmt.setString(6, certificacion.getDocumentoUrl());
            stmt.setString(7, certificacion.getDescripcion());
            stmt.setInt(8, certificacion.getOrden() != null ? certificacion.getOrden() : 1);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        certificacion.setId(id);
                        logger.info("✓ Certificación guardada exitosamente con ID {}", id);
                        return id;
                    }
                }
            }

            throw new Exception("No se pudo guardar la certificación");

        } catch (SQLException e) {
            logger.error("✗ Error al guardar certificación", e);
            throw new Exception("Error al guardar certificación", e);
        }
    }

    /**
     * Actualiza una certificación existente.
     *
     * @param certificacion Certificación con datos actualizados
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    @Override
    public boolean actualizar(CertificacionProfesional certificacion) throws Exception {
        logger.info("Actualizando certificación ID {}", certificacion.getId());

        String sql = "UPDATE certificaciones_profesional SET " +
                    "nombre_certificacion = ?, " +
                    "institucion = ?, " +
                    "fecha_obtencion = ?, " +
                    "fecha_vigencia = ?, " +
                    "documento_url = ?, " +
                    "descripcion = ?, " +
                    "orden = ?, " +
                    "fecha_actualizacion = NOW() " +
                    "WHERE id = ? AND profesional_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, certificacion.getNombreCertificacion());
            stmt.setString(2, certificacion.getInstitucion());

            // ✅ Fecha de obtención
            if (certificacion.getFechaObtencion() != null) {
                stmt.setDate(3, Date.valueOf(certificacion.getFechaObtencion()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            // ✅ Fecha de vigencia
            if (certificacion.getFechaVigencia() != null) {
                stmt.setDate(4, Date.valueOf(certificacion.getFechaVigencia()));
            } else {
                stmt.setNull(4, Types.DATE);
            }

            stmt.setString(5, certificacion.getDocumentoUrl());
            stmt.setString(6, certificacion.getDescripcion());
            stmt.setInt(7, certificacion.getOrden() != null ? certificacion.getOrden() : 1);
            stmt.setInt(8, certificacion.getId());
            stmt.setInt(9, certificacion.getProfesionalId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Certificación actualizada exitosamente");
                return true;
            }

            logger.warn("No se actualizó ninguna certificación. Verificar que existe y pertenece al profesional");
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al actualizar certificación", e);
            throw new Exception("Error al actualizar certificación", e);
        }
    }

    /**
     * Elimina (soft delete) una certificación.
     * Marca la certificación como inactiva en lugar de eliminarla físicamente.
     *
     * @param id ID de la certificación a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    @Override
    public boolean eliminar(Integer id) throws Exception {
        logger.info("Eliminando (soft delete) certificación ID {}", id);

        String sql = "UPDATE certificaciones_profesional SET " +
                    "activo = FALSE, " +
                    "fecha_actualizacion = NOW() " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Certificación eliminada (soft delete) exitosamente");
                return true;
            }

            logger.warn("No se eliminó ninguna certificación con ID {}", id);
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al eliminar certificación", e);
            throw new Exception("Error al eliminar certificación", e);
        }
    }

    /**
     * Cuenta el número de certificaciones activas de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Número de certificaciones activas
     * @throws Exception si hay error en la consulta
     */
    @Override
    public int contarPorProfesional(Integer profesionalId) throws Exception {
        logger.debug("Contando certificaciones del profesional {}", profesionalId);

        String sql = "SELECT COUNT(*) FROM certificaciones_profesional " +
                    "WHERE profesional_id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt(1);
                    logger.debug("Profesional {} tiene {} certificaciones activas", profesionalId, total);
                    return total;
                }
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error al contar certificaciones del profesional {}", profesionalId, e);
            throw new Exception("Error al contar certificaciones", e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto CertificacionProfesional.
     *
     * @param rs ResultSet con los datos de la certificación
     * @return Objeto CertificacionProfesional mapeado
     * @throws SQLException si hay error al leer el ResultSet
     */
    private CertificacionProfesional mapearCertificacion(ResultSet rs) throws SQLException {
        CertificacionProfesional cert = new CertificacionProfesional();

        cert.setId(rs.getInt("id"));
        cert.setProfesionalId(rs.getInt("profesional_id"));
        cert.setNombreCertificacion(rs.getString("nombre_certificacion"));
        cert.setInstitucion(rs.getString("institucion"));

        // ✅ Fechas (pueden ser null)
        Date fechaObtencion = rs.getDate("fecha_obtencion");
        if (fechaObtencion != null) {
            cert.setFechaObtencion(fechaObtencion.toLocalDate());
        }

        Date fechaVigencia = rs.getDate("fecha_vigencia");
        if (fechaVigencia != null) {
            cert.setFechaVigencia(fechaVigencia.toLocalDate());
        }

        cert.setDocumentoUrl(rs.getString("documento_url"));
        cert.setDescripcion(rs.getString("descripcion"));
        cert.setOrden(rs.getInt("orden"));
        cert.setActivo(rs.getBoolean("activo"));

        // ✅ Timestamps
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            cert.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            cert.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }

        return cert;
    }
}
