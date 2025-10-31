package com.contactoprofesionales.dao.usuario;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

/**
 * Implementación de UsuarioDAO con PostgreSQL.
 * 
 * Aplicación de SRP: Solo se encarga de persistencia de usuarios.
 * Usa PreparedStatement para prevenir SQL Injection.
 */
public class UsuarioDAOImpl implements UsuarioDAO {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAOImpl.class);

    @Override
    public Usuario buscarPorEmail(String email) throws DatabaseException {
        logger.debug("Buscando usuario por email: {}", email);
        
        String sql = "SELECT id, nombre, email, password_hash, telefono, " +
                     "fecha_registro, ultimo_acceso, activo " +
                     "FROM users WHERE email = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Usuario usuario = mapResultSetToUsuario(rs);
                    logger.info("Usuario encontrado: {}", email);
                    return usuario;
                }
            }
            
            logger.debug("Usuario no encontrado: {}", email);
            return null;
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por email: {}", email, e);
            throw new DatabaseException("Error al buscar usuario", e);
        }
    }

    @Override
    public Usuario buscarPorId(Integer id) throws DatabaseException {
        logger.debug("Buscando usuario por ID: {}", id);
        
        String sql = "SELECT id, nombre, email, password_hash, telefono, " +
                     "fecha_registro, ultimo_acceso, activo " +
                     "FROM users WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por ID: {}", id, e);
            throw new DatabaseException("Error al buscar usuario", e);
        }
    }

    @Override
    public boolean registrar(Usuario usuario) throws DatabaseException {
        logger.info("Registrando nuevo usuario: {}", usuario.getEmail());
        
        String sql = "INSERT INTO users (nombre, email, password_hash, telefono, activo) " +
                     "VALUES (?, ?, ?, ?, true) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPasswordHash());
            stmt.setString(4, usuario.getTelefono());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    usuario.setId(id);
                    logger.info("✓ Usuario registrado exitosamente: {} (ID: {})", 
                            usuario.getEmail(), id);
                    return true;
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al registrar usuario: {}", usuario.getEmail(), e);
            throw new DatabaseException("Error al registrar usuario", e);
        }
    }

    @Override
    public boolean actualizar(Usuario usuario) throws DatabaseException {
        logger.info("Actualizando usuario: {}", usuario.getEmail());
        
        String sql = "UPDATE users SET nombre = ?, telefono = ?, " +
                     "ultimo_acceso = CURRENT_TIMESTAMP " +
                     "WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getNombre());
            stmt.setString(2, usuario.getTelefono());
            stmt.setInt(3, usuario.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("✓ Usuario actualizado: {}", usuario.getEmail());
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar usuario: {}", usuario.getEmail(), e);
            throw new DatabaseException("Error al actualizar usuario", e);
        }
    }

    @Override
    public boolean eliminar(Integer id) throws DatabaseException {
        logger.info("Eliminando usuario ID: {}", id);
        
        // Eliminación lógica (marcar como inactivo)
        String sql = "UPDATE users SET activo = false WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("✓ Usuario eliminado (inactivado) ID: {}", id);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al eliminar usuario ID: {}", id, e);
            throw new DatabaseException("Error al eliminar usuario", e);
        }
    }

    @Override
    public boolean existeEmail(String email) throws DatabaseException {
        logger.debug("Verificando existencia de email: {}", email);
        
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al verificar email: {}", email, e);
            throw new DatabaseException("Error al verificar email", e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto Usuario.
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setNombre(rs.getString("nombre"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setActivo(rs.getBoolean("activo"));
        
        // Timestamps (pueden ser null)
        Timestamp fechaReg = rs.getTimestamp("fecha_registro");
        if (fechaReg != null) {
            usuario.setFechaRegistro(fechaReg.toLocalDateTime());
        }
        
        Timestamp ultimoAcceso = rs.getTimestamp("ultimo_acceso");
        if (ultimoAcceso != null) {
            usuario.setUltimoAcceso(ultimoAcceso.toLocalDateTime());
        }
        
        return usuario;
    }
}
