package com.contactoprofesionales.dao.usuario;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UsuarioDAOImpl implements UsuarioDAO {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAOImpl.class);

    @Override
    public Usuario buscarPorEmail(String email) throws DatabaseException {
        logger.debug("Buscando usuario por email: {}", email);
        
        String sql = "SELECT id, email, password_hash, fecha_registro, ultimo_acceso, " +
                     "activo, usuario_id, username, rol_sistema FROM users " +
                     "WHERE email = ? AND activo = true";
        
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
        
        String sql = "SELECT id, email, password_hash, fecha_registro, ultimo_acceso, " +
                     "activo, usuario_id, username, rol_sistema FROM users WHERE id = ?";
        
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
    public Usuario registrar(Usuario usuario) throws DatabaseException {
        logger.info("Registrando nuevo usuario en tabla 'users': {}", usuario.getEmail());
        
        String sql = "INSERT INTO users (email, password_hash, usuario_id, username, " +
                     "rol_sistema, activo) " +
                     "VALUES (?, ?, ?, ?, ?, true) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getEmail());
            stmt.setString(2, usuario.getPasswordHash());
            
            // usuario_id (FK a tabla usuarios)
            if (usuario.getUsuarioId() != null) {
                stmt.setLong(3, usuario.getUsuarioId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            
            stmt.setString(4, usuario.getUsername());
            stmt.setString(5, usuario.getRolSistema());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("id");
                    usuario.setId(id);
                    logger.info("✓ Usuario registrado en 'users' exitosamente: {} (ID: {})", 
                            usuario.getEmail(), id);
                    return usuario;
                }
            }
            
            throw new DatabaseException("No se pudo registrar el usuario en 'users'");
            
        } catch (SQLException e) {
            logger.error("Error al registrar usuario: {}", usuario.getEmail(), e);
            throw new DatabaseException("Error al registrar usuario", e);
        }
    }

    @Override
    public boolean actualizar(Usuario usuario) throws DatabaseException {
        logger.info("Actualizando usuario: {}", usuario.getEmail());
        
        String sql = "UPDATE users SET username = ?, ultimo_acceso = CURRENT_TIMESTAMP, " +
                     "rol_sistema = ? WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getRolSistema());
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

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getInt("id"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setActivo(rs.getBoolean("activo"));
        
        Long usuarioId = (Long) rs.getObject("usuario_id");
        usuario.setUsuarioId(usuarioId);
        
        usuario.setUsername(rs.getString("username"));
        usuario.setRolSistema(rs.getString("rol_sistema"));
        
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