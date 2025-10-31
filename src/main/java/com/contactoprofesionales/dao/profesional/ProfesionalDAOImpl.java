package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.model.Profesional;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Implementación del DAO para operaciones con profesionales.
 */
public class ProfesionalDAOImpl implements ProfesionalDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfesionalDAOImpl.class);
    
    @Override
    public Profesional buscarPorId(Integer id) throws DatabaseException {
        logger.debug("Buscando profesional por ID: {}", id);
        
        String sql = "SELECT p.*, u.nombre, u.email, u.telefono " +
                    "FROM profesionales p " +
                    "INNER JOIN users u ON p.usuario_id = u.id " +
                    "WHERE p.id = ? AND p.activo = true AND u.activo = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProfesional(rs);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Error al buscar profesional por ID: {}", id, e);
            throw new DatabaseException("Error al buscar profesional", e);
        }
    }
    
    @Override
    public Profesional buscarPorUsuarioId(Integer usuarioId) throws DatabaseException {
        logger.debug("Buscando profesional por usuario ID: {}", usuarioId);
        
        String sql = "SELECT p.*, u.nombre, u.email, u.telefono " +
                    "FROM profesionales p " +
                    "INNER JOIN users u ON p.usuario_id = u.id " +
                    "WHERE p.usuario_id = ? AND p.activo = true AND u.activo = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProfesional(rs);
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("Error al buscar profesional por usuario ID: {}", usuarioId, e);
            throw new DatabaseException("Error al buscar profesional", e);
        }
    }
    
    @Override
    public List<Profesional> listarTodos() throws DatabaseException {
        logger.debug("Listando todos los profesionales");
        
        String sql = "SELECT p.*, u.nombre, u.email, u.telefono " +
                    "FROM profesionales p " +
                    "INNER JOIN users u ON p.usuario_id = u.id " +
                    "WHERE p.activo = true AND u.activo = true " +
                    "ORDER BY p.calificacion_promedio DESC, p.total_resenas DESC";
        
        List<Profesional> profesionales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                profesionales.add(mapResultSetToProfesional(rs));
            }
            
            logger.debug("Encontrados {} profesionales", profesionales.size());
            return profesionales;
            
        } catch (SQLException e) {
            logger.error("Error al listar profesionales", e);
            throw new DatabaseException("Error al listar profesionales", e);
        }
    }
    
    @Override
    public List<Profesional> buscarPorEspecialidad(String especialidad) throws DatabaseException {
        logger.debug("Buscando profesionales por especialidad: {}", especialidad);
        
        String sql = "SELECT p.*, u.nombre, u.email, u.telefono " +
                    "FROM profesionales p " +
                    "INNER JOIN users u ON p.usuario_id = u.id " +
                    "WHERE p.especialidad ILIKE ? AND p.activo = true AND u.activo = true " +
                    "ORDER BY p.calificacion_promedio DESC";
        
        List<Profesional> profesionales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + especialidad + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profesionales.add(mapResultSetToProfesional(rs));
                }
            }
            
            logger.debug("Encontrados {} profesionales", profesionales.size());
            return profesionales;
            
        } catch (SQLException e) {
            logger.error("Error al buscar profesionales por especialidad", e);
            throw new DatabaseException("Error al buscar profesionales", e);
        }
    }
    
    @Override
    public List<Profesional> buscarPorDistrito(String distrito) throws DatabaseException {
        logger.debug("Buscando profesionales por distrito: {}", distrito);
        
        String sql = "SELECT p.*, u.nombre, u.email, u.telefono " +
                    "FROM profesionales p " +
                    "INNER JOIN users u ON p.usuario_id = u.id " +
                    "WHERE p.distrito ILIKE ? AND p.activo = true AND u.activo = true " +
                    "ORDER BY p.calificacion_promedio DESC";
        
        List<Profesional> profesionales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + distrito + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profesionales.add(mapResultSetToProfesional(rs));
                }
            }
            
            return profesionales;
            
        } catch (SQLException e) {
            logger.error("Error al buscar profesionales por distrito", e);
            throw new DatabaseException("Error al buscar profesionales", e);
        }
    }
    
    @Override
    public List<Profesional> buscarConFiltros(String especialidad, String distrito, 
                                             Double calificacionMin) throws DatabaseException {
        
        logger.debug("Buscando con filtros - Esp: {}, Dist: {}, Cal: {}", 
                    especialidad, distrito, calificacionMin);
        
        StringBuilder sql = new StringBuilder(
            "SELECT p.*, u.nombre, u.email, u.telefono " +
            "FROM profesionales p " +
            "INNER JOIN users u ON p.usuario_id = u.id " +
            "WHERE p.activo = true AND u.activo = true"
        );
        
        List<Object> params = new ArrayList<>();
        
        if (especialidad != null && !especialidad.isEmpty()) {
            sql.append(" AND p.especialidad ILIKE ?");
            params.add("%" + especialidad + "%");
        }
        
        if (distrito != null && !distrito.isEmpty()) {
            sql.append(" AND p.distrito ILIKE ?");
            params.add("%" + distrito + "%");
        }
        
        if (calificacionMin != null && calificacionMin > 0) {
            sql.append(" AND p.calificacion_promedio >= ?");
            params.add(calificacionMin);
        }
        
        sql.append(" ORDER BY p.calificacion_promedio DESC, p.total_resenas DESC");
        
        List<Profesional> profesionales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            for (int i = 0; i < params.size(); i++) {
                Object param = params.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Double) {
                    stmt.setDouble(i + 1, (Double) param);
                }
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    profesionales.add(mapResultSetToProfesional(rs));
                }
            }
            
            logger.debug("Encontrados {} profesionales con filtros", profesionales.size());
            return profesionales;
            
        } catch (SQLException e) {
            logger.error("Error al buscar profesionales con filtros", e);
            throw new DatabaseException("Error al buscar profesionales", e);
        }
    }
    
    @Override
    public Profesional crear(Profesional profesional) throws DatabaseException {
        logger.info("Creando nuevo profesional para usuario: {}", profesional.getUsuarioId());
        
        String sql = "INSERT INTO profesionales " +
                    "(usuario_id, especialidad, descripcion, experiencia, habilidades, " +
                    "certificaciones, tarifa_hora, ubicacion, distrito, radio_servicio, " +
                    "disponibilidad, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true) RETURNING id";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, profesional.getUsuarioId());
            stmt.setString(2, profesional.getEspecialidad());
            stmt.setString(3, profesional.getDescripcion());
            stmt.setString(4, profesional.getExperiencia());
            
            // Arrays
            if (profesional.getHabilidades() != null && !profesional.getHabilidades().isEmpty()) {
                Array habilidadesArray = conn.createArrayOf("TEXT", profesional.getHabilidades().toArray());
                stmt.setArray(5, habilidadesArray);
            } else {
                stmt.setNull(5, Types.ARRAY);
            }
            
            if (profesional.getCertificaciones() != null && !profesional.getCertificaciones().isEmpty()) {
                Array certificacionesArray = conn.createArrayOf("TEXT", profesional.getCertificaciones().toArray());
                stmt.setArray(6, certificacionesArray);
            } else {
                stmt.setNull(6, Types.ARRAY);
            }
            
            if (profesional.getTarifaHora() != null) {
                stmt.setDouble(7, profesional.getTarifaHora());
            } else {
                stmt.setNull(7, Types.DECIMAL);
            }
            
            stmt.setString(8, profesional.getUbicacion());
            stmt.setString(9, profesional.getDistrito());
            stmt.setInt(10, profesional.getRadioServicio() != null ? profesional.getRadioServicio() : 10);
            stmt.setString(11, profesional.getDisponibilidad());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    profesional.setId(rs.getInt("id"));
                    logger.info("✓ Profesional creado con ID: {}", profesional.getId());
                    return profesional;
                }
            }
            
            throw new DatabaseException("No se pudo crear el profesional");
            
        } catch (SQLException e) {
            logger.error("Error al crear profesional", e);
            throw new DatabaseException("Error al crear profesional", e);
        }
    }
    
    @Override
    public boolean actualizar(Profesional profesional) throws DatabaseException {
        logger.info("Actualizando profesional ID: {}", profesional.getId());
        
        String sql = "UPDATE profesionales SET " +
                    "especialidad = ?, descripcion = ?, experiencia = ?, " +
                    "habilidades = ?, certificaciones = ?, tarifa_hora = ?, " +
                    "ubicacion = ?, distrito = ?, radio_servicio = ?, " +
                    "disponibilidad = ? " +
                    "WHERE id = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, profesional.getEspecialidad());
            stmt.setString(2, profesional.getDescripcion());
            stmt.setString(3, profesional.getExperiencia());
            
            // Arrays
            if (profesional.getHabilidades() != null && !profesional.getHabilidades().isEmpty()) {
                Array habilidadesArray = conn.createArrayOf("TEXT", profesional.getHabilidades().toArray());
                stmt.setArray(4, habilidadesArray);
            } else {
                stmt.setNull(4, Types.ARRAY);
            }
            
            if (profesional.getCertificaciones() != null && !profesional.getCertificaciones().isEmpty()) {
                Array certificacionesArray = conn.createArrayOf("TEXT", profesional.getCertificaciones().toArray());
                stmt.setArray(5, certificacionesArray);
            } else {
                stmt.setNull(5, Types.ARRAY);
            }
            
            if (profesional.getTarifaHora() != null) {
                stmt.setDouble(6, profesional.getTarifaHora());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }
            
            stmt.setString(7, profesional.getUbicacion());
            stmt.setString(8, profesional.getDistrito());
            stmt.setInt(9, profesional.getRadioServicio());
            stmt.setString(10, profesional.getDisponibilidad());
            stmt.setInt(11, profesional.getId());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("✓ Profesional actualizado: {}", profesional.getId());
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar profesional: {}", profesional.getId(), e);
            throw new DatabaseException("Error al actualizar profesional", e);
        }
    }
    
    @Override
    public boolean eliminar(Integer id) throws DatabaseException {
        logger.info("Eliminando (inactivando) profesional ID: {}", id);
        
        String sql = "UPDATE profesionales SET activo = false WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("✓ Profesional eliminado: {}", id);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al eliminar profesional: {}", id, e);
            throw new DatabaseException("Error al eliminar profesional", e);
        }
    }
    
    @Override
    public boolean actualizarDisponibilidad(Integer id, boolean disponible) throws DatabaseException {
        logger.info("Actualizando disponibilidad profesional {} a: {}", id, disponible);
        
        String sql = "UPDATE profesionales SET disponible = ? WHERE id = ? AND activo = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, disponible);
            stmt.setInt(2, id);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar disponibilidad: {}", id, e);
            throw new DatabaseException("Error al actualizar disponibilidad", e);
        }
    }
    
    @Override
    public boolean actualizarCalificacion(Integer id, Double nuevaCalificacion) throws DatabaseException {
        logger.info("Actualizando calificación profesional {} con: {}", id, nuevaCalificacion);
        
        String sql = "SELECT actualizar_calificacion_profesional(?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.setDouble(2, nuevaCalificacion);
            
            stmt.execute();
            return true;
            
        } catch (SQLException e) {
            logger.error("Error al actualizar calificación: {}", id, e);
            throw new DatabaseException("Error al actualizar calificación", e);
        }
    }
    
    @Override
    public boolean existePorUsuarioId(Integer usuarioId) throws DatabaseException {
        logger.debug("Verificando existencia de profesional para usuario: {}", usuarioId);
        
        String sql = "SELECT EXISTS(SELECT 1 FROM profesionales WHERE usuario_id = ? AND activo = true)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
            
            return false;
            
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de profesional", e);
            throw new DatabaseException("Error al verificar profesional", e);
        }
    }
    
    /**
     * Mapea un ResultSet a un objeto Profesional.
     */
    private Profesional mapResultSetToProfesional(ResultSet rs) throws SQLException {
        Profesional profesional = new Profesional();
        
        profesional.setId(rs.getInt("id"));
        profesional.setUsuarioId(rs.getInt("usuario_id"));
        profesional.setEspecialidad(rs.getString("especialidad"));
        profesional.setDescripcion(rs.getString("descripcion"));
        profesional.setExperiencia(rs.getString("experiencia"));
        
        // Arrays de PostgreSQL
        Array habilidadesArray = rs.getArray("habilidades");
        if (habilidadesArray != null) {
            String[] habilidades = (String[]) habilidadesArray.getArray();
            profesional.setHabilidades(Arrays.asList(habilidades));
        }
        
        Array certificacionesArray = rs.getArray("certificaciones");
        if (certificacionesArray != null) {
            String[] certificaciones = (String[]) certificacionesArray.getArray();
            profesional.setCertificaciones(Arrays.asList(certificaciones));
        }
        
        profesional.setFotoPerfil(rs.getString("foto_perfil"));
        profesional.setFotoPortada(rs.getString("foto_portada"));
        
        Array portafolioArray = rs.getArray("portafolio");
        if (portafolioArray != null) {
            String[] portafolio = (String[]) portafolioArray.getArray();
            profesional.setPortafolio(Arrays.asList(portafolio));
        }
        
        profesional.setTarifaHora(rs.getDouble("tarifa_hora"));
        profesional.setCalificacionPromedio(rs.getDouble("calificacion_promedio"));
        profesional.setTotalResenas(rs.getInt("total_resenas"));
        profesional.setUbicacion(rs.getString("ubicacion"));
        profesional.setDistrito(rs.getString("distrito"));
        
        Double latitud = rs.getDouble("latitud");
        if (!rs.wasNull()) {
            profesional.setLatitud(latitud);
        }
        
        Double longitud = rs.getDouble("longitud");
        if (!rs.wasNull()) {
            profesional.setLongitud(longitud);
        }
        
        profesional.setRadioServicio(rs.getInt("radio_servicio"));
        profesional.setDisponibilidad(rs.getString("disponibilidad"));
        profesional.setVerificado(rs.getBoolean("verificado"));
        profesional.setDisponible(rs.getBoolean("disponible"));
        
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            profesional.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        
        Timestamp ultimaActualizacion = rs.getTimestamp("ultima_actualizacion");
        if (ultimaActualizacion != null) {
            profesional.setUltimaActualizacion(ultimaActualizacion.toLocalDateTime());
        }
        
        profesional.setActivo(rs.getBoolean("activo"));
        
        // Información del usuario
        profesional.setNombreCompleto(rs.getString("nombre"));
        profesional.setEmail(rs.getString("email"));
        profesional.setTelefono(rs.getString("telefono"));
        
        return profesional;
    }
    
    @Override
    public List<String> obtenerEspecialidadesUnicas() throws DatabaseException {
        logger.debug("Obteniendo especialidades únicas");
        
        String sql = "SELECT DISTINCT especialidad FROM profesionales " +
                    "WHERE activo = true AND especialidad IS NOT NULL " +
                    "ORDER BY especialidad";
        
        List<String> especialidades = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                especialidades.add(rs.getString("especialidad"));
            }
            
            logger.debug("Encontradas {} especialidades únicas", especialidades.size());
            return especialidades;
            
        } catch (SQLException e) {
            logger.error("Error al obtener especialidades únicas", e);
            throw new DatabaseException("Error al obtener especialidades", e);
        }
    }

    @Override
    public List<String> obtenerDistritosUnicos() throws DatabaseException {
        logger.debug("Obteniendo distritos únicos");
        
        String sql = "SELECT DISTINCT distrito FROM profesionales " +
                    "WHERE activo = true AND distrito IS NOT NULL " +
                    "ORDER BY distrito";
        
        List<String> distritos = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                distritos.add(rs.getString("distrito"));
            }
            
            logger.debug("Encontrados {} distritos únicos", distritos.size());
            return distritos;
            
        } catch (SQLException e) {
            logger.error("Error al obtener distritos únicos", e);
            throw new DatabaseException("Error al obtener distritos", e);
        }
    }
    
}