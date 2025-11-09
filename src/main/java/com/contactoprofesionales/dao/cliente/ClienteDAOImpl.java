package com.contactoprofesionales.dao.cliente;

import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.model.Cliente;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de ClienteDAO usando JDBC para PostgreSQL con HikariCP
 * Utiliza try-with-resources para gestión automática de recursos
 */
public class ClienteDAOImpl implements ClienteDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteDAOImpl.class);
    
    private static final String INSERT_CLIENTE = 
        "INSERT INTO clientes (nombre_completo, email, telefono, foto_perfil_url, " +
        "categorias_favoritas, radio_busqueda, presupuesto_promedio, " +
        "notificaciones_email, notificaciones_push, notificaciones_promociones, notificaciones_resenas, " +
        "perfil_visible, compartir_ubicacion, historial_publico, " +
        "fecha_registro, fecha_actualizacion, activo) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";
    
    private static final String UPDATE_CLIENTE = 
        "UPDATE clientes SET nombre_completo = ?, telefono = ?, foto_perfil_url = ?, " +
        "categorias_favoritas = ?, radio_busqueda = ?, presupuesto_promedio = ?, " +
        "notificaciones_email = ?, notificaciones_push = ?, notificaciones_promociones = ?, notificaciones_resenas = ?, " +
        "perfil_visible = ?, compartir_ubicacion = ?, historial_publico = ?, " +
        "fecha_actualizacion = ? WHERE id = ? AND activo = true";
    
    private static final String SELECT_BY_ID = 
        "SELECT * FROM clientes WHERE id = ? AND activo = true";
    
    private static final String SELECT_BY_EMAIL = 
        "SELECT * FROM clientes WHERE email = ? AND activo = true";
    
    private static final String SELECT_BY_TELEFONO = 
        "SELECT * FROM clientes WHERE telefono = ? AND activo = true";
    
    private static final String SELECT_ALL_ACTIVOS = 
        "SELECT * FROM clientes WHERE activo = true ORDER BY fecha_registro DESC";
    
    private static final String DESACTIVAR_CLIENTE = 
        "UPDATE clientes SET activo = false, fecha_actualizacion = ? WHERE id = ?";
    
    private static final String ACTIVAR_CLIENTE = 
        "UPDATE clientes SET activo = true, fecha_actualizacion = ? WHERE id = ?";
    
    private static final String EXISTS_EMAIL = 
        "SELECT COUNT(*) FROM clientes WHERE email = ? AND activo = true";
    
    private static final String EXISTS_TELEFONO = 
        "SELECT COUNT(*) FROM clientes WHERE telefono = ? AND activo = true";
    
    @Override
    public Cliente registrar(Cliente cliente) throws ClienteException {
        logger.debug("Registrando nuevo cliente: {}", cliente.getEmail());
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_CLIENTE)) {
            
            int index = 1;
            ps.setString(index++, cliente.getNombreCompleto());
            ps.setString(index++, cliente.getEmail());
            ps.setString(index++, cliente.getTelefono());
            ps.setString(index++, cliente.getFotoPerfilUrl());
            ps.setString(index++, cliente.getCategoriasFavoritas());
            ps.setObject(index++, cliente.getRadioBusqueda());
            ps.setObject(index++, cliente.getPresupuestoPromedio());
            ps.setBoolean(index++, cliente.getNotificacionesEmail() != null ? cliente.getNotificacionesEmail() : false);
            ps.setBoolean(index++, cliente.getNotificacionesPush() != null ? cliente.getNotificacionesPush() : false);
            ps.setBoolean(index++, cliente.getNotificacionesPromociones() != null ? cliente.getNotificacionesPromociones() : false);
            ps.setBoolean(index++, cliente.getNotificacionesResenas() != null ? cliente.getNotificacionesResenas() : false);
            ps.setBoolean(index++, cliente.getPerfilVisible() != null ? cliente.getPerfilVisible() : true);
            ps.setBoolean(index++, cliente.getCompartirUbicacion() != null ? cliente.getCompartirUbicacion() : false);
            ps.setBoolean(index++, cliente.getHistorialPublico() != null ? cliente.getHistorialPublico() : false);
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(index++, true);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cliente.setId(rs.getLong("id"));
                    logger.info("Cliente registrado exitosamente con ID: {}", cliente.getId());
                    return cliente;
                }
            }
            
            throw new ClienteException("REGISTRO_ERROR", "No se pudo registrar el cliente");
            
        } catch (SQLException e) {
            logger.error("Error al registrar cliente", e);
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new ClienteException("DUPLICADO", "El email o teléfono ya está registrado", e);
            }
            throw new ClienteException("DB_ERROR", "Error al registrar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Cliente actualizar(Cliente cliente) throws ClienteException {
        logger.debug("Actualizando cliente ID: {}", cliente.getId());
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_CLIENTE)) {
            
            int index = 1;
            ps.setString(index++, cliente.getNombreCompleto());
            ps.setString(index++, cliente.getTelefono());
            ps.setString(index++, cliente.getFotoPerfilUrl());
            ps.setString(index++, cliente.getCategoriasFavoritas());
            ps.setObject(index++, cliente.getRadioBusqueda());
            ps.setObject(index++, cliente.getPresupuestoPromedio());
            ps.setBoolean(index++, cliente.getNotificacionesEmail());
            ps.setBoolean(index++, cliente.getNotificacionesPush());
            ps.setBoolean(index++, cliente.getNotificacionesPromociones());
            ps.setBoolean(index++, cliente.getNotificacionesResenas());
            ps.setBoolean(index++, cliente.getPerfilVisible());
            ps.setBoolean(index++, cliente.getCompartirUbicacion());
            ps.setBoolean(index++, cliente.getHistorialPublico());
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(index++, cliente.getId());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new ClienteException("NO_ENCONTRADO", "Cliente no encontrado o inactivo");
            }
            
            logger.info("Cliente actualizado exitosamente: {}", cliente.getId());
            return cliente;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar cliente", e);
            throw new ClienteException("DB_ERROR", "Error al actualizar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Cliente> buscarPorId(Long id) throws ClienteException {
        logger.debug("Buscando cliente por ID: {}", id);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
            
            ps.setLong(1, id);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = mapResultSetToCliente(rs);
                    logger.debug("Cliente encontrado: {}", id);
                    return Optional.of(cliente);
                }
            }
            
            logger.debug("Cliente no encontrado: {}", id);
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Error al buscar cliente por ID", e);
            throw new ClienteException("DB_ERROR", "Error al buscar cliente por ID: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Cliente> buscarPorEmail(String email) throws ClienteException {
        logger.debug("Buscando cliente por email: {}", email);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_EMAIL)) {
            
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = mapResultSetToCliente(rs);
                    logger.debug("Cliente encontrado por email");
                    return Optional.of(cliente);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Error al buscar cliente por email", e);
            throw new ClienteException("DB_ERROR", "Error al buscar cliente por email: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Cliente> buscarPorTelefono(String telefono) throws ClienteException {
        logger.debug("Buscando cliente por teléfono: {}", telefono);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_TELEFONO)) {
            
            ps.setString(1, telefono);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Cliente cliente = mapResultSetToCliente(rs);
                    logger.debug("Cliente encontrado por teléfono");
                    return Optional.of(cliente);
                }
            }
            
            return Optional.empty();
            
        } catch (SQLException e) {
            logger.error("Error al buscar cliente por teléfono", e);
            throw new ClienteException("DB_ERROR", "Error al buscar cliente por teléfono: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Cliente> listarActivos() throws ClienteException {
        logger.debug("Listando todos los clientes activos");
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
            
            logger.info("Se encontraron {} clientes activos", clientes.size());
            return clientes;
            
        } catch (SQLException e) {
            logger.error("Error al listar clientes", e);
            throw new ClienteException("DB_ERROR", "Error al listar clientes: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean desactivar(Long id) throws ClienteException {
        logger.debug("Desactivando cliente ID: {}", id);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DESACTIVAR_CLIENTE)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Cliente desactivado: {}", id);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al desactivar cliente", e);
            throw new ClienteException("DB_ERROR", "Error al desactivar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean activar(Long id) throws ClienteException {
        logger.debug("Activando cliente ID: {}", id);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(ACTIVAR_CLIENTE)) {
            
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Cliente activado: {}", id);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al activar cliente", e);
            throw new ClienteException("DB_ERROR", "Error al activar cliente: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean existeEmail(String email) throws ClienteException {
        logger.debug("Verificando existencia de email: {}", email);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_EMAIL)) {
            
            ps.setString(1, email);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean existe = rs.getInt(1) > 0;
                    logger.debug("Email {} existe: {}", email, existe);
                    return existe;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al verificar email", e);
            throw new ClienteException("DB_ERROR", "Error al verificar email: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean existeTelefono(String telefono) throws ClienteException {
        logger.debug("Verificando existencia de teléfono: {}", telefono);
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_TELEFONO)) {
            
            ps.setString(1, telefono);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean existe = rs.getInt(1) > 0;
                    logger.debug("Teléfono {} existe: {}", telefono, existe);
                    return existe;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al verificar teléfono", e);
            throw new ClienteException("DB_ERROR", "Error al verificar teléfono: " + e.getMessage(), e);
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Cliente
     */
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        
        cliente.setId(rs.getLong("id"));
        cliente.setNombreCompleto(rs.getString("nombre_completo"));
        cliente.setEmail(rs.getString("email"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setFotoPerfilUrl(rs.getString("foto_perfil_url"));
        cliente.setCategoriasFavoritas(rs.getString("categorias_favoritas"));
        
        Integer radioBusqueda = (Integer) rs.getObject("radio_busqueda");
        cliente.setRadioBusqueda(radioBusqueda);
        
        //Double presupuestoPromedio = (Double) rs.getObject("presupuesto_promedio");
        //cliente.setPresupuestoPromedio(presupuestoPromedio);
        
        Object presupuestoObj = rs.getObject("presupuesto_promedio");
        if (presupuestoObj != null) {
            if (presupuestoObj instanceof BigDecimal) {
            	cliente.setPresupuestoPromedio(((BigDecimal) presupuestoObj).doubleValue());
            } else if (presupuestoObj instanceof Double) {
            	cliente.setPresupuestoPromedio((Double) presupuestoObj);
            } else if (presupuestoObj instanceof Number) {
            	cliente.setPresupuestoPromedio(((Number) presupuestoObj).doubleValue());
            }
        }
        
        cliente.setNotificacionesEmail(rs.getBoolean("notificaciones_email"));
        cliente.setNotificacionesPush(rs.getBoolean("notificaciones_push"));
        cliente.setNotificacionesPromociones(rs.getBoolean("notificaciones_promociones"));
        cliente.setNotificacionesResenas(rs.getBoolean("notificaciones_resenas"));
        cliente.setPerfilVisible(rs.getBoolean("perfil_visible"));
        cliente.setCompartirUbicacion(rs.getBoolean("compartir_ubicacion"));
        cliente.setHistorialPublico(rs.getBoolean("historial_publico"));
        
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            cliente.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        
        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            cliente.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }
        
        cliente.setActivo(rs.getBoolean("activo"));
        
        return cliente;
    }
}