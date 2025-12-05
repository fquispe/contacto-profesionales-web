package com.contactoprofesionales.dao.solicitud;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.dto.SolicitudDetalleDTO;
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
        // ACTUALIZADO EN MIGRACIÓN V008: Agregados campos de ubicación estructurada y modalidad
        String sql = """
            INSERT INTO solicitudes_servicio (
                cliente_id, profesional_id, descripcion, presupuesto_estimado,
                direccion, codigo_postal, referencia, fecha_servicio,
                urgencia, notas_adicionales, fotos_urls, estado,
                fecha_solicitud, fecha_actualizacion, activo,
                departamento_id, provincia_id, distrito_id, tipo_prestacion, especialidad_id
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id
        """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // ✅ CORRECCIÓN: Ajuste de parámetros según estructura de tabla (22 columnas, 20 en INSERT)
            // INSERT: cliente_id, profesional_id, descripcion, presupuesto_estimado,
            //         direccion, codigo_postal, referencia, fecha_servicio,
            //         urgencia, notas_adicionales, fotos_urls, estado,
            //         fecha_solicitud, fecha_actualizacion, activo,
            //         departamento_id, provincia_id, distrito_id, tipo_prestacion, especialidad_id

            ps.setInt(1, solicitud.getClienteId());           // cliente_id
            ps.setInt(2, solicitud.getProfesionalId());       // profesional_id
            ps.setString(3, solicitud.getDescripcion());      // descripcion
            ps.setDouble(4, solicitud.getPresupuestoEstimado()); // presupuesto_estimado
            ps.setString(5, solicitud.getDireccion());        // direccion
            ps.setString(6, solicitud.getCodigoPostal());     // codigo_postal (CORREGIDO: antes era getDistrito)
            ps.setString(7, solicitud.getReferencia());       // referencia
            ps.setTimestamp(8, Timestamp.valueOf(solicitud.getFechaServicio())); // fecha_servicio
            ps.setString(9, solicitud.getUrgencia());         // urgencia
            ps.setString(10, solicitud.getNotasAdicionales()); // notas_adicionales

            // fotos_urls - Convertir List<String> a Array de PostgreSQL
            Array fotosArray = null;
            if (solicitud.getFotosUrls() != null && !solicitud.getFotosUrls().isEmpty()) {
                fotosArray = conn.createArrayOf("text", solicitud.getFotosUrls().toArray());
            }
            ps.setArray(11, fotosArray);                      // fotos_urls

            ps.setString(12, solicitud.getEstado());          // estado
            ps.setTimestamp(13, Timestamp.valueOf(solicitud.getFechaSolicitud())); // fecha_solicitud
            ps.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now())); // fecha_actualizacion
            ps.setBoolean(15, solicitud.isActivo());          // activo

            // Nuevos campos - Migración V008
            ps.setObject(16, solicitud.getDepartamentoId());  // departamento_id (puede ser null)
            ps.setObject(17, solicitud.getProvinciaId());     // provincia_id (puede ser null)
            ps.setObject(18, solicitud.getDistritoId());      // distrito_id (puede ser null, CORREGIDO: antes índice 19)
            ps.setString(19, solicitud.getTipoPrestacion());  // tipo_prestacion ("REMOTO" o "PRESENCIAL")
            ps.setObject(20, solicitud.getEspecialidadId());  // especialidad_id (puede ser null)

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
     * Actualiza el estado de una solicitud.
     * También actualiza fecha_respuesta cuando el profesional acepta o rechaza.
     *
     * ACTUALIZADO 2025-12-04: Agregado actualización de fecha_respuesta
     */
    @Override
    public boolean actualizarEstado(Integer solicitudId, String nuevoEstado) throws DatabaseException {
        String sql = """
            UPDATE solicitudes_servicio
            SET estado = ?,
                fecha_respuesta = CURRENT_TIMESTAMP,
                fecha_actualizacion = CURRENT_TIMESTAMP
            WHERE id = ? AND activo = true
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, solicitudId);

            int filas = ps.executeUpdate();
            logger.info("✅ Estado actualizado de solicitud {} a '{}' con fecha_respuesta", solicitudId, nuevoEstado);
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
        // ✅ CORRECCIÓN: Eliminada línea s.setDistrito(rs.getString("distrito_id"))
        // El campo distrito (String) está deprecado. Ahora se usa distrito_id (Integer)
        // Ver líneas 322-327 donde se mapea correctamente distrito_id
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

        // NUEVOS CAMPOS - Migración V008: Ubicación estructurada y modalidad
        // Usar try-catch por si no existen en queries antiguas
        try {
            Integer departamentoId = rs.getInt("departamento_id");
            if (!rs.wasNull()) s.setDepartamentoId(departamentoId);
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        try {
            Integer provinciaId = rs.getInt("provincia_id");
            if (!rs.wasNull()) s.setProvinciaId(provinciaId);
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        try {
            Integer distritoId = rs.getInt("distrito_id");
            if (!rs.wasNull()) s.setDistritoId(distritoId);
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        try {
            s.setTipoPrestacion(rs.getString("tipo_prestacion"));
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        try {
            Integer especialidadId = rs.getInt("especialidad_id");
            if (!rs.wasNull()) s.setEspecialidadId(especialidadId);
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        return s;
    }

    /**
     * Cuenta solicitudes pendientes para un profesional.
     * Usado para badge de alertas en dashboard.
     *
     * ACTUALIZADO 2025-12-03: Agregado para sistema de alertas del profesional
     */
    @Override
    public int contarPendientesPorProfesional(Integer profesionalId) throws DatabaseException {
        // SQL optimizado: COUNT en lugar de traer todas las filas
        String sql = """
            SELECT COUNT(*)
            FROM solicitudes_servicio
            WHERE profesional_id = ?
              AND estado = 'pendiente'
              AND activo = true
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, profesionalId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                logger.debug("📊 Profesional {} tiene {} solicitudes pendientes", profesionalId, count);
                return count;
            }

            // No debería llegar aquí, pero devolver 0 por seguridad
            logger.warn("⚠️ Query COUNT no retornó resultado para profesional {}", profesionalId);
            return 0;

        } catch (SQLException e) {
            logger.error("❌ Error al contar solicitudes pendientes para profesional {}: {}",
                        profesionalId, e.getMessage());
            throw new DatabaseException("Error al contar solicitudes pendientes", e);
        }
    }

    /**
     * Busca una solicitud por ID con información completa del cliente y ubicación.
     * Realiza JOINs con las tablas: clientes, departamentos, provincias, distritos.
     *
     * ACTUALIZADO 2025-12-04: Agregado para mostrar detalle completo en el frontend
     */
    @Override
    public SolicitudDetalleDTO buscarPorIdConDetalle(Integer id) throws DatabaseException {
        String sql = """
            SELECT
                s.*,
                u.nombre_completo as cliente_nombre,
                u.email as cliente_email,
                u.telefono as cliente_telefono,
                dep.nombre as departamento_nombre,
                prov.nombre as provincia_nombre,
                dist.nombre as distrito_nombre
            FROM solicitudes_servicio s
            LEFT JOIN usuarios u ON s.cliente_id = u.id
            LEFT JOIN departamentos dep ON s.departamento_id = dep.id
            LEFT JOIN provincias prov ON s.provincia_id = prov.id
            LEFT JOIN distritos dist ON s.distrito_id = dist.id
            WHERE s.id = ? AND s.activo = true
        """;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Mapear solicitud básica
                SolicitudServicio solicitud = mapearSolicitud(rs);

                // Crear DTO con datos básicos
                SolicitudDetalleDTO dto = new SolicitudDetalleDTO(solicitud);

                // Agregar información del cliente desde el JOIN
                dto.setClienteNombreCompleto(rs.getString("cliente_nombre"));
                dto.setClienteEmail(rs.getString("cliente_email"));
                dto.setClienteTelefono(rs.getString("cliente_telefono"));

                // Agregar nombres de ubicación desde los JOINs
                dto.setDepartamentoNombre(rs.getString("departamento_nombre"));
                dto.setProvinciaNombre(rs.getString("provincia_nombre"));
                dto.setDistritoNombre(rs.getString("distrito_nombre"));

                logger.debug("Solicitud {} encontrada con detalle completo", id);
                return dto;
            }

            logger.debug("Solicitud {} no encontrada", id);
            return null;

        } catch (SQLException e) {
            logger.error("Error al buscar solicitud con detalle: {}", e.getMessage());
            throw new DatabaseException("Error al buscar la solicitud con detalle", e);
        }
    }

}
