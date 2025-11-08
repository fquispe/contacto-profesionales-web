package com.contactoprofesionales.dao.cliente;

import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.model.DireccionCliente;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de DireccionClienteDAO usando JDBC para PostgreSQL con HikariCP
 * Utiliza try-with-resources para gestión automática de recursos
 */
public class DireccionClienteDAOImpl implements DireccionClienteDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(DireccionClienteDAOImpl.class);
    private static final int MAX_DIRECCIONES = 3;
    
    private static final String INSERT_DIRECCION = 
        "INSERT INTO direcciones_cliente (cliente_id, tipo, direccion_completa, distrito, referencias, " +
        "es_principal, fecha_creacion, fecha_actualizacion, activo) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    
    private static final String UPDATE_DIRECCION = 
        "UPDATE direcciones_cliente SET tipo = ?, direccion_completa = ?, distrito = ?, " +
        "referencias = ?, es_principal = ?, fecha_actualizacion = ? WHERE id = ? AND activo = true";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM direcciones_cliente WHERE id = ? AND activo = true";
    
    private static final String SELECT_BY_CLIENTE = 
        "SELECT * FROM direcciones_cliente WHERE cliente_id = ? AND activo = true ORDER BY es_principal DESC, id ASC";
    
    private static final String SELECT_PRINCIPAL = 
        "SELECT * FROM direcciones_cliente WHERE cliente_id = ? AND es_principal = true AND activo = true LIMIT 1";
    
    private static final String DELETE_DIRECCION = 
        "UPDATE direcciones_cliente SET activo = false, fecha_actualizacion = ? WHERE id = ?";
    
    private static final String DELETE_BY_CLIENTE = 
        "UPDATE direcciones_cliente SET activo = false, fecha_actualizacion = ? WHERE cliente_id = ?";
    
    private static final String COUNT_BY_CLIENTE = 
        "SELECT COUNT(*) FROM direcciones_cliente WHERE cliente_id = ? AND activo = true";
    
    private static final String UNSET_PRINCIPAL = 
        "UPDATE direcciones_cliente SET es_principal = false WHERE cliente_id = ? AND activo = true";
    
    private static final String SET_PRINCIPAL = 
        "UPDATE direcciones_cliente SET es_principal = true, fecha_actualizacion = ? WHERE id = ? AND cliente_id = ? AND activo = true";
    
    @Override
    public DireccionCliente crear(DireccionCliente direccion) throws ClienteException {
        logger.debug("Creando nueva dirección para cliente ID: {}", direccion.getClienteId());
        
        // Verificar límite de direcciones
        int count = contarPorCliente(direccion.getClienteId());
        if (count >= MAX_DIRECCIONES) {
            throw new ClienteException("LIMITE_DIRECCIONES", 
                "El cliente ya tiene el máximo de " + MAX_DIRECCIONES + " direcciones permitidas");
        }
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_DIRECCION)) {
            
            int index = 1;
            ps.setLong(index++, direccion.getClienteId());
            ps.setString(index++, direccion.getTipo());
            ps.setString(index++, direccion.getDireccionCompleta());
            ps.setString(index++, direccion.getDistrito());
            ps.setString(index++, direccion.getReferencias());
            ps.setBoolean(index++, direccion.getEsPrincipal() != null ? direccion.getEsPrincipal() : false);
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(index++, true);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    direccion.setId(rs.getLong("id"));
                    
                    // Si es la primera dirección, establecerla como principal automáticamente
                    if (count == 0 || (direccion.getEsPrincipal() != null && direccion.getEsPrincipal())) {
                        establecerComoPrincipal(direccion.getId(), direccion.getClienteId());
                    }
                    
                    logger.info("Dirección creada exitosamente con ID: {}", direccion.getId());
                    return direccion;
                }
            }
            
            throw new ClienteException("REGISTRO_ERROR", "No se pudo crear la dirección");
            
        } catch (SQLException e) {
            logger.error("Error al crear dirección", e);
            throw new ClienteException("DB_ERROR", "Error al crear dirección: " + e.getMessage(), e);
        }
    }
    
    @Override
    public DireccionCliente actualizar(DireccionCliente direccion) throws ClienteException {
        logger.debug("Actualizando dirección ID: {}", direccion.getId());
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_DIRECCION)) {
            
            int index = 1;
            ps.setString(index++, direccion.getTipo());
            ps.setString(index++, direccion.getDireccionCompleta());
            ps.setString(index++, direccion.getDistrito());
            ps.setString(index++, direccion.getReferencias());
            ps.setBoolean(index++, direccion.getEsPrincipal());
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(index++, direccion.getId());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new ClienteException("NO_ENCONTRADO", "Dirección no encontrada o inactiva");
            }
            
            // Si se está estableciendo como principal, desmarcar las demás
            if (direccion.getEsPrincipal() != null && direccion.getEsPrincipal()) {
                establecerComoPrincipal(direccion.getId(), direccion.getClienteId());
            }
            
            logger.info("Dirección actualizada exitosamente: {}", direccion.getId());
            return direccion;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar dirección", e);
            throw new ClienteException("DB_ERROR", "Error al actualizar dirección: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<DireccionCliente> buscarPorId(Long id) throws ClienteException {
        logger.debug("Buscando dirección por ID: {}", id);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            
            ps.setLong(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DireccionCliente direccion = mapResultSetToDireccion(rs);
                    logger.debug("Dirección encontrada: {}", id);
                    return Optional.of(direccion);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Error al buscar dirección por ID", e);
            throw new ClienteException("DB_ERROR", "Error al buscar dirección por ID: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<DireccionCliente> listarPorCliente(Long clienteId) throws ClienteException {
        logger.debug("Listando direcciones del cliente ID: {}", clienteId);
        List<DireccionCliente> direcciones = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_CLIENTE)) {
            
            ps.setLong(1, clienteId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    direcciones.add(mapResultSetToDireccion(rs));
                }
            }
            
            logger.debug("Se encontraron {} direcciones para el cliente {}", direcciones.size(), clienteId);
            return direcciones;
            
        } catch (SQLException e) {
            logger.error("Error al listar direcciones del cliente", e);
            throw new ClienteException("DB_ERROR", "Error al listar direcciones del cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<DireccionCliente> buscarPrincipal(Long clienteId) throws ClienteException {
        logger.debug("Buscando dirección principal del cliente ID: {}", clienteId);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PRINCIPAL)) {
            
            ps.setLong(1, clienteId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    DireccionCliente direccion = mapResultSetToDireccion(rs);
                    logger.debug("Dirección principal encontrada");
                    return Optional.of(direccion);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Error al buscar dirección principal", e);
            throw new ClienteException("DB_ERROR", "Error al buscar dirección principal: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean eliminar(Long id) throws ClienteException {
        logger.debug("Eliminando dirección ID: {}", id);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_DIRECCION)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Dirección eliminada: {}", id);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al eliminar dirección", e);
            throw new ClienteException("DB_ERROR", "Error al eliminar dirección: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int eliminarPorCliente(Long clienteId) throws ClienteException {
        logger.debug("Eliminando todas las direcciones del cliente ID: {}", clienteId);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_BY_CLIENTE)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, clienteId);
            
            int rowsAffected = ps.executeUpdate();
            logger.info("Se eliminaron {} direcciones del cliente {}", rowsAffected, clienteId);
            return rowsAffected;
            
        } catch (SQLException e) {
            logger.error("Error al eliminar direcciones del cliente", e);
            throw new ClienteException("DB_ERROR", "Error al eliminar direcciones del cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public int contarPorCliente(Long clienteId) throws ClienteException {
        logger.debug("Contando direcciones del cliente ID: {}", clienteId);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_BY_CLIENTE)) {
            
            ps.setLong(1, clienteId);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    logger.debug("Cliente {} tiene {} direcciones", clienteId, count);
                    return count;
                }
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("Error al contar direcciones", e);
            throw new ClienteException("DB_ERROR", "Error al contar direcciones: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean establecerComoPrincipal(Long id, Long clienteId) throws ClienteException {
        logger.debug("Estableciendo dirección {} como principal para cliente {}", id, clienteId);
        
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);
            
            // Primero desmarcar todas las direcciones del cliente
            try (PreparedStatement psUnset = conn.prepareStatement(UNSET_PRINCIPAL)) {
                psUnset.setLong(1, clienteId);
                psUnset.executeUpdate();
            }
            
            // Luego marcar la dirección especificada como principal
            try (PreparedStatement psSet = conn.prepareStatement(SET_PRINCIPAL)) {
                psSet.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                psSet.setLong(2, id);
                psSet.setLong(3, clienteId);
                
                int rowsAffected = psSet.executeUpdate();
                
                conn.commit();
                
                if (rowsAffected > 0) {
                    logger.info("Dirección {} establecida como principal", id);
                    return true;
                }
                return false;
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.warn("Rollback realizado por error al establecer dirección principal");
                } catch (SQLException ex) {
                    logger.error("Error al hacer rollback", ex);
                }
            }
            logger.error("Error al establecer dirección principal", e);
            throw new ClienteException("DB_ERROR", "Error al establecer dirección principal: " + e.getMessage(), e);
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
     * Mapea un ResultSet a un objeto DireccionCliente
     */
    private DireccionCliente mapResultSetToDireccion(ResultSet rs) throws SQLException {
        DireccionCliente direccion = new DireccionCliente();
        
        direccion.setId(rs.getLong("id"));
        direccion.setClienteId(rs.getLong("cliente_id"));
        direccion.setTipo(rs.getString("tipo"));
        direccion.setDireccionCompleta(rs.getString("direccion_completa"));
        direccion.setDistrito(rs.getString("distrito"));
        direccion.setReferencias(rs.getString("referencias"));
        direccion.setEsPrincipal(rs.getBoolean("es_principal"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            direccion.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            direccion.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }
        
        direccion.setActivo(rs.getBoolean("activo"));
        
        return direccion;
    }
}