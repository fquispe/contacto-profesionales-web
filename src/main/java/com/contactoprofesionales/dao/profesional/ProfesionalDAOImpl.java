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
 *
 * ✅ ACTUALIZADO 2025-11-17: Migración completa a nueva estructura de tablas
 * - especialidad: Ahora en tabla categorias_servicio (relación via especialidades_profesional)
 * - ubicación/distrito: Ahora en tabla usuarios (distrito_id)
 * - Todos los SQL queries actualizados para reflejar esquema real de BD
 */
public class ProfesionalDAOImpl implements ProfesionalDAO {

    private static final Logger logger = LoggerFactory.getLogger(ProfesionalDAOImpl.class);

    /**
     * ✅ ACTUALIZADO 2025-11-17: Query con JOIN a especialidades y categorias_servicio
     * Incluye especialidad_nombre desde categorias_servicio y distrito_id desde usuarios
     */
    @Override
    public Profesional buscarPorId(Integer id) throws DatabaseException {
        logger.debug("Buscando profesional por ID: {}", id);

        // ✅ JOIN con especialidades_profesional y categorias_servicio para obtener especialidad
        // ✅ distrito_id viene de tabla usuarios
        String sql = "SELECT p.*, " +
                    "u.nombre_completo, u.telefono, u.distrito_id, " +
                    "cs.nombre AS especialidad_nombre " +
                    "FROM profesionales p " +
                    "INNER JOIN usuarios u ON p.usuario_id = u.id " +
                    "LEFT JOIN especialidades_profesional ep ON p.especialidad_principal_id = ep.id " +
                    "LEFT JOIN categorias_servicio cs ON ep.categoria_id = cs.id " +
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

    /**
     * ✅ ACTUALIZADO 2025-11-17: Query con JOIN a especialidades y categorias_servicio
     */
    @Override
    public Profesional buscarPorUsuarioId(Integer usuarioId) throws DatabaseException {
        logger.debug("Buscando profesional por usuario ID: {}", usuarioId);

        String sql = "SELECT p.*, " +
                    "u.nombre_completo, u.telefono, u.distrito_id, " +
                    "cs.nombre AS especialidad_nombre " +
                    "FROM profesionales p " +
                    "INNER JOIN usuarios u ON p.usuario_id = u.id " +
                    "LEFT JOIN especialidades_profesional ep ON p.especialidad_principal_id = ep.id " +
                    "LEFT JOIN categorias_servicio cs ON ep.categoria_id = cs.id " +
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

    /**
     * ✅ ACTUALIZADO 2025-11-17: Query con JOIN a especialidades y categorias_servicio
     */
    @Override
    public List<Profesional> listarTodos() throws DatabaseException {
        logger.debug("Listando todos los profesionales");

        String sql = "SELECT p.*, " +
                    "u.nombre_completo, u.telefono, u.distrito_id, " +
                    "cs.nombre AS especialidad_nombre " +
                    "FROM profesionales p " +
                    "INNER JOIN usuarios u ON p.usuario_id = u.id " +
                    "LEFT JOIN especialidades_profesional ep ON p.especialidad_principal_id = ep.id " +
                    "LEFT JOIN categorias_servicio cs ON ep.categoria_id = cs.id " +
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

    /**
     * ✅ ACTUALIZADO 2025-11-17: Búsqueda por nombre de categoría de servicio
     * Antes buscaba por campo especialidad, ahora busca en categorias_servicio.nombre
     */
    @Override
    public List<Profesional> buscarPorEspecialidad(String especialidad) throws DatabaseException {
        logger.debug("Buscando profesionales por especialidad: {}", especialidad);

        String sql = "SELECT p.*, " +
                    "u.nombre_completo, u.telefono, u.distrito_id, " +
                    "cs.nombre AS especialidad_nombre " +
                    "FROM profesionales p " +
                    "INNER JOIN usuarios u ON p.usuario_id = u.id " +
                    "INNER JOIN especialidades_profesional ep ON p.id = ep.profesional_id " +
                    "INNER JOIN categorias_servicio cs ON cs.id = ep.categoria_id " +
                    "WHERE cs.nombre ILIKE ? AND p.activo = true AND u.activo = true AND ep.activo = true " +
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

    /**
     * ✅ ACTUALIZADO 2025-11-17: Búsqueda por distrito_id de tabla usuarios
     * Antes buscaba en p.distrito (no existe), ahora busca en u.distrito_id
     */
    @Override
    public List<Profesional> buscarPorDistrito(String distrito) throws DatabaseException {
        logger.debug("Buscando profesionales por distrito: {}", distrito);

        // ℹ️ distrito es distrito_id (Integer) almacenado como String por compatibilidad
        String sql = "SELECT p.*, " +
                    "u.nombre_completo, u.telefono, u.distrito_id, " +
                    "cs.nombre AS especialidad_nombre " +
                    "FROM profesionales p " +
                    "INNER JOIN usuarios u ON p.usuario_id = u.id " +
                    "LEFT JOIN especialidades_profesional ep ON p.especialidad_principal_id = ep.id " +
                    "LEFT JOIN categorias_servicio cs ON ep.categoria_id = cs.id " +
                    "WHERE u.distrito_id = ? AND p.activo = true AND u.activo = true " +
                    "ORDER BY p.calificacion_promedio DESC";

        List<Profesional> profesionales = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // Convertir distrito (String) a Integer para distrito_id
            try {
                stmt.setInt(1, Integer.parseInt(distrito));
            } catch (NumberFormatException e) {
                // Si no es un número, retornar lista vacía
                logger.warn("Distrito no es un ID válido: {}", distrito);
                return profesionales;
            }

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

    /**
     * ✅ ACTUALIZADO 2025-11-17: Búsqueda con filtros usando nueva estructura
     * - especialidad: Busca en categorias_servicio.nombre via JOIN
     * - distrito: Busca en usuarios.distrito_id
     * - calificacionMin: Busca en profesionales.calificacion_promedio
     */
    @Override
    public List<Profesional> buscarConFiltros(String especialidad, String distrito,
                                             Double calificacionMin) throws DatabaseException {

        logger.debug("Buscando con filtros - Esp: {}, Dist: {}, Cal: {}",
                    especialidad, distrito, calificacionMin);

        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT p.*, " +
            "u.nombre_completo, u.telefono, u.distrito_id, " +
            "cs.nombre AS especialidad_nombre " +
            "FROM profesionales p " +
            "INNER JOIN usuarios u ON p.usuario_id = u.id " +
            "LEFT JOIN especialidades_profesional ep ON p.especialidad_principal_id = ep.id " +
            "LEFT JOIN categorias_servicio cs ON ep.categoria_id = cs.id " +
            "WHERE p.activo = true AND u.activo = true"
        );

        List<Object> params = new ArrayList<>();

        // ✅ Filtro de especialidad: Buscar en categorias_servicio
        if (especialidad != null && !especialidad.isEmpty()) {
            sql.append(" AND EXISTS (")
               .append("SELECT 1 FROM especialidades_profesional ep2 ")
               .append("INNER JOIN categorias_servicio cs2 ON ep2.categoria_id = cs2.id ")
               .append("WHERE ep2.profesional_id = p.id AND ep2.activo = true ")
               .append("AND cs2.nombre ILIKE ?)");
            params.add("%" + especialidad + "%");
        }

        // ✅ Filtro de distrito: Buscar en usuarios.distrito_id
        if (distrito != null && !distrito.isEmpty()) {
            try {
                sql.append(" AND u.distrito_id = ?");
                params.add(Integer.parseInt(distrito));
            } catch (NumberFormatException e) {
                logger.warn("Distrito no es un ID válido: {}", distrito);
                // Ignorar filtro de distrito si no es válido
            }
        }

        // ✅ Filtro de calificación mínima
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
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
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

    /**
     * ✅ ACTUALIZADO 2025-11-17: INSERT simplificado con campos que SÍ existen
     *
     * ⚠️ DEPRECADO: Este método ya no debe usarse para crear profesionales completos
     * Solo crea el registro básico. Las especialidades se crean en especialidades_profesional
     * por separado usando el servlet correspondiente.
     *
     * Campos eliminados del INSERT (no existen en tabla):
     * - especialidad → Ahora se registra en especialidades_profesional
     * - ubicacion, distrito, radio_servicio → No existen en profesionales
     */
    @Override
    @Deprecated
    public Profesional crear(Profesional profesional) throws DatabaseException {
        logger.info("⚠️ DEPRECADO: Creando profesional básico para usuario: {}", profesional.getUsuarioId());
        logger.warn("Las especialidades deben crearse por separado en especialidades_profesional");

        // ✅ Solo insertar campos que SÍ existen en la tabla profesionales
        String sql = "INSERT INTO profesionales " +
                    "(usuario_id, descripcion, experiencia, disponibilidad, activo) " +
                    "VALUES (?, ?, ?, ?, true) RETURNING id";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesional.getUsuarioId());
            stmt.setString(2, profesional.getDescripcion());
            stmt.setString(3, profesional.getExperiencia());
            stmt.setString(4, profesional.getDisponibilidad());

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

    /**
     * ✅ ACTUALIZADO 2025-11-17: UPDATE simplificado con campos que SÍ existen
     *
     * ⚠️ DEPRECADO: Este método ya no debe usarse. Usar PerfilProfesionalServlet
     *
     * Campos eliminados del UPDATE (no existen en tabla):
     * - especialidad, habilidades, certificaciones → Deprecados
     * - ubicacion, distrito, radio_servicio → No existen
     */
    @Override
    @Deprecated
    public boolean actualizar(Profesional profesional) throws DatabaseException {
        logger.info("⚠️ DEPRECADO: Actualizando profesional ID: {}", profesional.getId());
        logger.warn("Usar PerfilProfesionalServlet para actualizar perfil");

        // ✅ Solo actualizar campos básicos que SÍ existen
        String sql = "UPDATE profesionales SET " +
                    "descripcion = ?, experiencia = ?, disponibilidad = ? " +
                    "WHERE id = ? AND activo = true";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profesional.getDescripcion());
            stmt.setString(2, profesional.getExperiencia());
            stmt.setString(3, profesional.getDisponibilidad());
            stmt.setInt(4, profesional.getId());

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
     *
     * ✅ ACTUALIZADO 2025-11-17: Manejo completo de nueva estructura de tablas
     * - especialidad_nombre: Desde categorias_servicio via JOIN
     * - distrito_id: Desde tabla usuarios
     * - Campos deprecados manejados con try-catch
     * - Campos inexistentes (ubicacion, distrito, latitud, longitud, radio_servicio) protegidos
     */
    private Profesional mapResultSetToProfesional(ResultSet rs) throws SQLException {
        Profesional profesional = new Profesional();

        // ✅ Campos básicos que SÍ existen
        profesional.setId(rs.getInt("id"));
        profesional.setUsuarioId(rs.getInt("usuario_id"));
        profesional.setDescripcion(rs.getString("descripcion"));
        profesional.setExperiencia(rs.getString("experiencia"));

        // ✅ Arrays de PostgreSQL (deprecados pero aún existen en BD)
        try {
            Array habilidadesArray = rs.getArray("habilidades");
            if (habilidadesArray != null) {
                String[] habilidades = (String[]) habilidadesArray.getArray();
                profesional.setHabilidades(Arrays.asList(habilidades));
            }
        } catch (SQLException e) {
            // Columna no existe, ignorar
        }

        try {
            Array certificacionesArray = rs.getArray("certificaciones");
            if (certificacionesArray != null) {
                String[] certificaciones = (String[]) certificacionesArray.getArray();
                profesional.setCertificaciones(Arrays.asList(certificaciones));
            }
        } catch (SQLException e) {
            // Columna no existe, ignorar
        }

        try {
            profesional.setFotoPerfil(rs.getString("foto_perfil"));
        } catch (SQLException e) {
            // Columna no existe, ignorar
        }

        try {
            profesional.setFotoPortada(rs.getString("foto_portada"));
        } catch (SQLException e) {
            // Columna no existe, ignorar
        }

        try {
            Array portafolioArray = rs.getArray("portafolio");
            if (portafolioArray != null) {
                String[] portafolio = (String[]) portafolioArray.getArray();
                profesional.setPortafolio(Arrays.asList(portafolio));
            }
        } catch (SQLException e) {
            // Columna no existe, ignorar
        }

        // ✅ Campos numéricos y flags que SÍ existen
        profesional.setTarifaHora(rs.getDouble("tarifa_hora"));
        profesional.setCalificacionPromedio(rs.getDouble("calificacion_promedio"));
        profesional.setTotalResenas(rs.getInt("total_resenas"));
        profesional.setDisponibilidad(rs.getString("disponibilidad"));
        profesional.setVerificado(rs.getBoolean("verificado"));
        profesional.setDisponible(rs.getBoolean("disponible"));

        // ✅ Fechas
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            profesional.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }

        Timestamp ultimaActualizacion = rs.getTimestamp("ultima_actualizacion");
        if (ultimaActualizacion != null) {
            profesional.setUltimaActualizacion(ultimaActualizacion.toLocalDateTime());
        }

        profesional.setActivo(rs.getBoolean("activo"));

        // ✅ NUEVO: Especialidad desde categorias_servicio (viene del JOIN)
        try {
            String especialidadNombre = rs.getString("especialidad_nombre");
            if (especialidadNombre != null) {
                profesional.setEspecialidad(especialidadNombre);
            }
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        // ✅ NUEVO: Distrito desde usuarios (viene del JOIN)
        try {
            Integer distritoId = rs.getInt("distrito_id");
            if (!rs.wasNull()) {
                // Guardar distrito_id como String en el campo distrito por compatibilidad
                profesional.setDistrito(String.valueOf(distritoId));
            }
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        // ❌ CAMPOS QUE YA NO EXISTEN EN TABLA PROFESIONALES (proteger con try-catch)
        // ubicacion, latitud, longitud, radio_servicio - YA NO EXISTEN
        try {
            profesional.setUbicacion(rs.getString("ubicacion"));
        } catch (SQLException e) {
            // Campo no existe, ignorar
        }

        try {
            Double latitud = rs.getDouble("latitud");
            if (!rs.wasNull()) {
                profesional.setLatitud(latitud);
            }
        } catch (SQLException e) {
            // Campo no existe, ignorar
        }

        try {
            Double longitud = rs.getDouble("longitud");
            if (!rs.wasNull()) {
                profesional.setLongitud(longitud);
            }
        } catch (SQLException e) {
            // Campo no existe, ignorar
        }

        try {
            profesional.setRadioServicio(rs.getInt("radio_servicio"));
        } catch (SQLException e) {
            // Campo no existe, ignorar
        }

        // ✅ Información del usuario (desde tabla usuarios via JOIN)
        try {
            profesional.setNombreCompleto(rs.getString("nombre_completo"));
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        // ✅ Email NO se incluye en queries (se gestiona en otro módulo)
        // Dejamos email como null

        try {
            profesional.setTelefono(rs.getString("telefono"));
        } catch (SQLException e) {
            // Columna no existe en este query, ignorar
        }

        return profesional;
    }

    /**
     * ✅ ACTUALIZADO 2025-11-17: Obtiene especialidades desde categorias_servicio
     * Antes consultaba campo especialidad en profesionales (ya no existe)
     * Ahora consulta categorias_servicio via especialidades_profesional
     */
    @Override
    public List<String> obtenerEspecialidadesUnicas() throws DatabaseException {
        logger.debug("Obteniendo especialidades únicas desde categorias_servicio");

        // ✅ Consultar categorias_servicio que están siendo usadas por profesionales activos
        String sql = "SELECT DISTINCT cs.nombre " +
                    "FROM categorias_servicio cs " +
                    "INNER JOIN especialidades_profesional ep ON cs.id = ep.categoria_id " +
                    "INNER JOIN profesionales p ON ep.profesional_id = p.id " +
                    "WHERE p.activo = true AND ep.activo = true " +
                    "ORDER BY cs.nombre";

        List<String> especialidades = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                especialidades.add(rs.getString("nombre"));
            }

            logger.debug("Encontradas {} especialidades únicas", especialidades.size());
            return especialidades;

        } catch (SQLException e) {
            logger.error("Error al obtener especialidades únicas", e);
            throw new DatabaseException("Error al obtener especialidades", e);
        }
    }

    /**
     * ✅ ACTUALIZADO 2025-11-17: Obtiene distritos desde tabla usuarios
     * Antes consultaba campo distrito en profesionales (ya no existe)
     * Ahora consulta usuarios.distrito_id
     */
    @Override
    public List<String> obtenerDistritosUnicos() throws DatabaseException {
        logger.debug("Obteniendo distritos únicos desde tabla usuarios");

        // ✅ Consultar distrito_id de usuarios que tienen perfil profesional activo
        String sql = "SELECT DISTINCT u.distrito_id " +
                    "FROM usuarios u " +
                    "INNER JOIN profesionales p ON u.id = p.usuario_id " +
                    "WHERE p.activo = true AND u.activo = true AND u.distrito_id IS NOT NULL " +
                    "ORDER BY u.distrito_id";

        List<String> distritos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                // Retornar distrito_id como String para compatibilidad
                distritos.add(String.valueOf(rs.getInt("distrito_id")));
            }

            logger.debug("Encontrados {} distritos únicos", distritos.size());
            return distritos;

        } catch (SQLException e) {
            logger.error("Error al obtener distritos únicos", e);
            throw new DatabaseException("Error al obtener distritos", e);
        }
    }

}
