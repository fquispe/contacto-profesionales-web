package com.contactoprofesionales.dao.solicitud;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementación DAO para la entidad SolicitudServicio.
 * Gestiona el acceso a la tabla 'solicitudes_servicio'.
 * 
 * Patrón: DAO (Data Access Object)
 * Responsabilidad: Interactuar con la base de datos (CRUD).
 */
public class SolicitudServicioDAOImpl implements SolicitudServicioDAO {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudServicioDAOImpl.class);

    // Constructor por defecto
    public SolicitudServicioDAOImpl() {}

    /**
     * Crea una nueva solicitud en BD.
     */
    @Override
    public SolicitudServicio crear(SolicitudServicio solicitud) throws DatabaseException {
        String sql = """
            INSERT INTO solicitudes_servicio (
                cliente_id, profesional_id, descripcion, presupuesto_estimado,
                direccion, distrito, codigo_postal, referencia, fecha_servicio,
                urgencia, notas_adicionales, fotos_urls, estado,
                fecha_solicitud, fecha_actualizacion, activo
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, solicitud.getClienteId());
            ps.setInt(2, solicitud.getProfesionalId());
            ps.setString(3, solicitud.getDescripcion());
            ps.setDouble(4, solicitud.getPresupuestoEstimado());
            ps.setString(5, solicitud.getDireccion());
            ps.setString(6, solicitud.getDistrito());
            ps.setString(7, solicitud.getCodigoPostal());
            ps.setString(8, solicitud.getReferencia());
            ps.setTimestamp(9, Timestamp.valueOf(solicitud.getFechaServicio()));
            ps.setString(10, solicitud.getUrgencia());
            ps.setString(11, solicitud.getNotasAdicionales());
            
            // ✅ CAMBIO AQUÍ: Convertir List<String> a Array de PostgreSQL
            Array fotosArray = null;
            if (solicitud.getFotosUrls() != null && !solicitud.getFotosUrls().isEmpty()) {
                fotosArray = conn.createArrayOf("text", solicitud.getFotosUrls().toArray());
            }
            ps.setArray(12, fotosArray);
            
            ps.setString(13, solicitud.getEstado());
            ps.setTimestamp(14, Timestamp.valueOf(solicitud.getFechaSolicitud()));
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(16, solicitud.isActivo());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                solicitud.setId(rs.getInt("id"));
            }

            logger.info("Solicitud insertada correctamente con ID {}", solicitud.getId());
            return solicitud;

        } catch (SQLException e) {
            logger.error("Error al crear solicitud: {}", e.getMessage());
            throw new DatabaseException("Error al crear la solicitud de servicio", e);
        }
    }

    /**
     * Busca una solicitud por ID.
     */
    @Override
    public SolicitudServicio buscarPorId(Integer id) throws DatabaseException {
        String sql = "SELECT * FROM solicitudes_servicio WHERE id = ? AND activo = true";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearSolicitud(rs);
            }
            return null;

        } catch (SQLException e) {
            logger.error("Error al buscar solicitud por ID: {}", e.getMessage());
            throw new DatabaseException("Error al buscar la solicitud", e);
        }
    }

    /**
     * Verifica si un cliente ya tiene una solicitud pendiente con un profesional.
     */
    @Override
    public boolean existeSolicitudPendiente(Integer clienteId, Integer profesionalId) throws DatabaseException {
        String sql = """
            SELECT COUNT(*) FROM solicitudes_servicio
            WHERE cliente_id = ? AND profesional_id = ? AND estado = 'pendiente' AND activo = true
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            ps.setInt(2, profesionalId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error al verificar solicitudes pendientes: {}", e.getMessage());
            throw new DatabaseException("Error al verificar solicitudes pendientes", e);
        }
    }

    /**
     * Lista todas las solicitudes de un cliente.
     */
    @Override
    public List<SolicitudServicio> listarPorCliente(Integer clienteId) throws DatabaseException {
        String sql = """
            SELECT * FROM solicitudes_servicio
            WHERE cliente_id = ? AND activo = true ORDER BY fecha_solicitud DESC
        """;
        List<SolicitudServicio> solicitudes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                solicitudes.add(mapearSolicitud(rs));
            }
            return solicitudes;

        } catch (SQLException e) {
            logger.error("Error al listar solicitudes del cliente: {}", e.getMessage());
            throw new DatabaseException("Error al listar solicitudes del cliente", e);
        }
    }

    /**
     * Lista todas las solicitudes de un profesional.
     */
    @Override
    public List<SolicitudServicio> listarPorProfesional(Integer profesionalId) throws DatabaseException {
        String sql = """
            SELECT * FROM solicitudes_servicio
            WHERE profesional_id = ? AND activo = true ORDER BY fecha_solicitud DESC
        """;
        List<SolicitudServicio> solicitudes = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profesionalId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                solicitudes.add(mapearSolicitud(rs));
            }
            return solicitudes;

        } catch (SQLException e) {
            logger.error("Error al listar solicitudes del profesional: {}", e.getMessage());
            throw new DatabaseException("Error al listar solicitudes del profesional", e);
        }
    }

    /**
     * Cancela una solicitud (actualiza estado y fecha_actualizacion).
     */
    @Override
    public boolean cancelar(Integer solicitudId, Integer clienteId) throws DatabaseException {
        String sql = """
            UPDATE solicitudes_servicio
            SET estado = 'cancelada', fecha_actualizacion = ?, activo = false
            WHERE id = ? AND cliente_id = ? AND activo = true
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(2, solicitudId);
            ps.setInt(3, clienteId);

            int rows = ps.executeUpdate();
            logger.info("Solicitud cancelada (filas afectadas = {})", rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al cancelar solicitud: {}", e.getMessage());
            throw new DatabaseException("Error al cancelar solicitud", e);
        }
    }

    /**
     * Actualiza el estado
     */
    @Override
    public boolean actualizarEstado(Integer solicitudId, String nuevoEstado) throws DatabaseException {
        String sql = """
            UPDATE solicitudes_servicio
            SET estado = ?, fecha_actualizacion = CURRENT_TIMESTAMP
            WHERE id = ? AND activo = true
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, solicitudId);

            int filas = ps.executeUpdate();
            logger.info("Estado actualizado de solicitud {} a '{}'", solicitudId, nuevoEstado);
            return filas > 0;

        } catch (SQLException e) {
            logger.error("Error al actualizar estado de la solicitud {}: {}", solicitudId, e.getMessage());
            throw new DatabaseException("Error al actualizar el estado de la solicitud", e);
        }
    }

    
    /**
     * Convierte una fila del ResultSet a un objeto SolicitudServicio.
     */
    private SolicitudServicio mapearSolicitud(ResultSet rs) throws SQLException {
        SolicitudServicio s = new SolicitudServicio();
        s.setId(rs.getInt("id"));
        s.setClienteId(rs.getInt("cliente_id"));
        s.setProfesionalId(rs.getInt("profesional_id"));
        s.setDescripcion(rs.getString("descripcion"));
        s.setPresupuestoEstimado(rs.getDouble("presupuesto_estimado"));
        s.setDireccion(rs.getString("direccion"));
        s.setDistrito(rs.getString("distrito"));
        s.setCodigoPostal(rs.getString("codigo_postal"));
        s.setReferencia(rs.getString("referencia"));

        Timestamp fechaServ = rs.getTimestamp("fecha_servicio");
        if (fechaServ != null) s.setFechaServicio(fechaServ.toLocalDateTime());

        s.setUrgencia(rs.getString("urgencia"));
        s.setNotasAdicionales(rs.getString("notas_adicionales"));
        
        // ✅ CAMBIO AQUÍ: Leer array de PostgreSQL
        Array fotosArray = rs.getArray("fotos_urls");
        if (fotosArray != null) {
            String[] fotosArrayStr = (String[]) fotosArray.getArray();
            s.setFotosUrls(Arrays.asList(fotosArrayStr));
        } else {
            s.setFotosUrls(new ArrayList<>());
        }
        
        s.setEstado(rs.getString("estado"));

        Timestamp fechaSol = rs.getTimestamp("fecha_solicitud");
        if (fechaSol != null) s.setFechaSolicitud(fechaSol.toLocalDateTime());

        Timestamp fechaResp = rs.getTimestamp("fecha_respuesta");
        if (fechaResp != null) s.setFechaRespuesta(fechaResp.toLocalDateTime());

        Timestamp fechaAct = rs.getTimestamp("fecha_actualizacion");
        if (fechaAct != null) s.setFechaActualizacion(fechaAct.toLocalDateTime());

        s.setActivo(rs.getBoolean("activo"));
        return s;
    }
    
}
