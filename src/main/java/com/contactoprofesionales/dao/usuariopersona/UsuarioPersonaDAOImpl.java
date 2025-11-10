package com.contactoprofesionales.dao.usuariopersona;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.UsuarioPersona;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de UsuarioPersonaDAO usando JDBC para PostgreSQL con HikariCP
 * Utiliza try-with-resources para gestión automática de recursos
 */
public class UsuarioPersonaDAOImpl implements UsuarioPersonaDAO {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioPersonaDAOImpl.class);

    private static final String INSERT_USUARIO =
        "INSERT INTO usuarios (nombre_completo, tipo_documento, numero_documento, fecha_nacimiento, " +
        "genero, telefono, telefono_alternativo, departamento_id, provincia_id, distrito_id, " +
        "direccion, referencia_direccion, tipo_rol, es_cliente, es_profesional, foto_perfil_url, " +
        "fecha_creacion, fecha_actualizacion, activo) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

    private static final String UPDATE_USUARIO =
        "UPDATE usuarios SET nombre_completo = ?, tipo_documento = ?, numero_documento = ?, " +
        "fecha_nacimiento = ?, genero = ?, telefono = ?, telefono_alternativo = ?, " +
        "departamento_id = ?, provincia_id = ?, distrito_id = ?, direccion = ?, " +
        "referencia_direccion = ?, foto_perfil_url = ?, fecha_actualizacion = ? " +
        "WHERE id = ? AND activo = true";

    private static final String SELECT_BY_ID =
        "SELECT * FROM usuarios WHERE id = ? AND activo = true";

    private static final String SELECT_BY_NUMERO_DOCUMENTO =
        "SELECT * FROM usuarios WHERE numero_documento = ? AND activo = true";

    private static final String SELECT_BY_TELEFONO =
        "SELECT * FROM usuarios WHERE telefono = ? AND activo = true";

    private static final String SELECT_ALL_ACTIVOS =
        "SELECT * FROM usuarios WHERE activo = true ORDER BY fecha_creacion DESC";

    private static final String SELECT_BY_TIPO_ROL =
        "SELECT * FROM usuarios WHERE tipo_rol = ? AND activo = true ORDER BY fecha_creacion DESC";

    private static final String DESACTIVAR_USUARIO =
        "UPDATE usuarios SET activo = false, fecha_actualizacion = ? WHERE id = ?";

    private static final String ACTIVAR_USUARIO =
        "UPDATE usuarios SET activo = true, fecha_actualizacion = ? WHERE id = ?";

    private static final String UPDATE_TIPO_ROL =
        "UPDATE usuarios SET tipo_rol = ?, es_cliente = ?, es_profesional = ?, fecha_actualizacion = ? " +
        "WHERE id = ? AND activo = true";

    private static final String UPDATE_UBICACION =
        "UPDATE usuarios SET departamento_id = ?, provincia_id = ?, distrito_id = ?, " +
        "direccion = ?, referencia_direccion = ?, fecha_actualizacion = ? " +
        "WHERE id = ? AND activo = true";

    private static final String EXISTS_NUMERO_DOCUMENTO =
        "SELECT COUNT(*) FROM usuarios WHERE numero_documento = ? AND activo = true";

    private static final String EXISTS_TELEFONO =
        "SELECT COUNT(*) FROM usuarios WHERE telefono = ? AND activo = true";

    @Override
    public UsuarioPersona registrar(UsuarioPersona usuarioPersona) throws DatabaseException {
        if (usuarioPersona == null) {
            throw new DatabaseException("El usuario persona no puede ser nulo");
        }

        logger.debug("Registrando nuevo usuario persona: {}", usuarioPersona.getNumeroDocumento());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_USUARIO)) {

            int index = 1;
            ps.setString(index++, usuarioPersona.getNombreCompleto());
            ps.setString(index++, usuarioPersona.getTipoDocumento());
            ps.setString(index++, usuarioPersona.getNumeroDocumento());
            ps.setDate(index++, usuarioPersona.getFechaNacimiento() != null ?
                Date.valueOf(usuarioPersona.getFechaNacimiento()) : null);
            ps.setString(index++, usuarioPersona.getGenero());
            ps.setString(index++, usuarioPersona.getTelefono());
            ps.setString(index++, usuarioPersona.getTelefonoAlternativo());
            ps.setObject(index++, usuarioPersona.getDepartamentoId());
            ps.setObject(index++, usuarioPersona.getProvinciaId());
            ps.setObject(index++, usuarioPersona.getDistritoId());
            ps.setString(index++, usuarioPersona.getDireccion());
            ps.setString(index++, usuarioPersona.getReferenciaDireccion());
            ps.setString(index++, usuarioPersona.getTipoRol() != null ? usuarioPersona.getTipoRol() : "CLIENTE");
            ps.setBoolean(index++, usuarioPersona.getEsCliente() != null ? usuarioPersona.getEsCliente() : false);
            ps.setBoolean(index++, usuarioPersona.getEsProfesional() != null ? usuarioPersona.getEsProfesional() : false);
            ps.setString(index++, usuarioPersona.getFotoPerfilUrl());
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBoolean(index++, true);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    usuarioPersona.setId(rs.getLong("id"));
                    logger.info("Usuario persona registrado exitosamente con ID: {}", usuarioPersona.getId());
                    return usuarioPersona;
                }
            }

            throw new DatabaseException("No se pudo registrar el usuario persona");

        } catch (SQLException e) {
            logger.error("Error al registrar usuario persona", e);
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                throw new DatabaseException("El número de documento o teléfono ya está registrado", e);
            }
            throw new DatabaseException("Error al registrar usuario persona: " + e.getMessage(), e);
        }
    }

    @Override
    public UsuarioPersona actualizar(UsuarioPersona usuarioPersona) throws DatabaseException {
        if (usuarioPersona == null || usuarioPersona.getId() == null) {
            throw new DatabaseException("El usuario persona y su ID no pueden ser nulos");
        }

        logger.debug("Actualizando usuario persona ID: {}", usuarioPersona.getId());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_USUARIO)) {

            int index = 1;
            ps.setString(index++, usuarioPersona.getNombreCompleto());
            ps.setString(index++, usuarioPersona.getTipoDocumento());
            ps.setString(index++, usuarioPersona.getNumeroDocumento());
            ps.setDate(index++, usuarioPersona.getFechaNacimiento() != null ?
                Date.valueOf(usuarioPersona.getFechaNacimiento()) : null);
            ps.setString(index++, usuarioPersona.getGenero());
            ps.setString(index++, usuarioPersona.getTelefono());
            ps.setString(index++, usuarioPersona.getTelefonoAlternativo());
            ps.setObject(index++, usuarioPersona.getDepartamentoId());
            ps.setObject(index++, usuarioPersona.getProvinciaId());
            ps.setObject(index++, usuarioPersona.getDistritoId());
            ps.setString(index++, usuarioPersona.getDireccion());
            ps.setString(index++, usuarioPersona.getReferenciaDireccion());
            ps.setString(index++, usuarioPersona.getFotoPerfilUrl());
            ps.setTimestamp(index++, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(index++, usuarioPersona.getId());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new DatabaseException("Usuario persona no encontrado o inactivo");
            }

            logger.info("Usuario persona actualizado exitosamente: {}", usuarioPersona.getId());
            return usuarioPersona;

        } catch (SQLException e) {
            logger.error("Error al actualizar usuario persona", e);
            throw new DatabaseException("Error al actualizar usuario persona: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UsuarioPersona> buscarPorId(Long id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Buscando usuario persona por ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UsuarioPersona usuario = mapUsuarioPersona(rs);
                    logger.debug("Usuario persona encontrado: {}", id);
                    return Optional.of(usuario);
                }
            }

            logger.debug("Usuario persona no encontrado: {}", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar usuario persona por ID", e);
            throw new DatabaseException("Error al buscar usuario persona por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UsuarioPersona> buscarPorNumeroDocumento(String numeroDocumento) throws DatabaseException {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new DatabaseException("El número de documento no puede ser nulo o vacío");
        }

        logger.debug("Buscando usuario persona por número de documento: {}", numeroDocumento);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NUMERO_DOCUMENTO)) {

            ps.setString(1, numeroDocumento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UsuarioPersona usuario = mapUsuarioPersona(rs);
                    logger.debug("Usuario persona encontrado por número de documento");
                    return Optional.of(usuario);
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar usuario persona por número de documento", e);
            throw new DatabaseException("Error al buscar usuario persona por número de documento: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<UsuarioPersona> buscarPorTelefono(String telefono) throws DatabaseException {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new DatabaseException("El teléfono no puede ser nulo o vacío");
        }

        logger.debug("Buscando usuario persona por teléfono: {}", telefono);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_TELEFONO)) {

            ps.setString(1, telefono);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    UsuarioPersona usuario = mapUsuarioPersona(rs);
                    logger.debug("Usuario persona encontrado por teléfono");
                    return Optional.of(usuario);
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar usuario persona por teléfono", e);
            throw new DatabaseException("Error al buscar usuario persona por teléfono: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UsuarioPersona> listarActivos() throws DatabaseException {
        logger.debug("Listando todos los usuarios persona activos");
        List<UsuarioPersona> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_ACTIVOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapUsuarioPersona(rs));
            }

            logger.info("Se encontraron {} usuarios persona activos", usuarios.size());
            return usuarios;

        } catch (SQLException e) {
            logger.error("Error al listar usuarios persona", e);
            throw new DatabaseException("Error al listar usuarios persona: " + e.getMessage(), e);
        }
    }

    @Override
    public List<UsuarioPersona> listarPorTipoRol(String tipoRol) throws DatabaseException {
        if (tipoRol == null || tipoRol.trim().isEmpty()) {
            throw new DatabaseException("El tipo de rol no puede ser nulo o vacío");
        }

        logger.debug("Listando usuarios persona por tipo de rol: {}", tipoRol);
        List<UsuarioPersona> usuarios = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_TIPO_ROL)) {

            ps.setString(1, tipoRol);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapUsuarioPersona(rs));
                }
            }

            logger.info("Se encontraron {} usuarios persona con tipo de rol {}", usuarios.size(), tipoRol);
            return usuarios;

        } catch (SQLException e) {
            logger.error("Error al listar usuarios persona por tipo de rol", e);
            throw new DatabaseException("Error al listar usuarios persona por tipo de rol: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean desactivar(Long id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Desactivando usuario persona ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DESACTIVAR_USUARIO)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Usuario persona desactivado: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error al desactivar usuario persona", e);
            throw new DatabaseException("Error al desactivar usuario persona: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean activar(Long id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Activando usuario persona ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(ACTIVAR_USUARIO)) {

            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(2, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Usuario persona activado: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error al activar usuario persona", e);
            throw new DatabaseException("Error al activar usuario persona: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizarTipoRol(Long id, String tipoRol, Boolean esCliente, Boolean esProfesional) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }
        if (tipoRol == null || tipoRol.trim().isEmpty()) {
            throw new DatabaseException("El tipo de rol no puede ser nulo o vacío");
        }

        logger.debug("Actualizando tipo de rol para usuario persona ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_TIPO_ROL)) {

            ps.setString(1, tipoRol);
            ps.setBoolean(2, esCliente != null ? esCliente : false);
            ps.setBoolean(3, esProfesional != null ? esProfesional : false);
            ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(5, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Tipo de rol actualizado para usuario persona: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error al actualizar tipo de rol", e);
            throw new DatabaseException("Error al actualizar tipo de rol: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean actualizarUbicacion(Long id, Integer departamentoId, Integer provinciaId,
                                      Integer distritoId, String direccion, String referencia) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Actualizando ubicación para usuario persona ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_UBICACION)) {

            ps.setObject(1, departamentoId);
            ps.setObject(2, provinciaId);
            ps.setObject(3, distritoId);
            ps.setString(4, direccion);
            ps.setString(5, referencia);
            ps.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            ps.setLong(7, id);

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Ubicación actualizada para usuario persona: {}", id);
                return true;
            }
            return false;

        } catch (SQLException e) {
            logger.error("Error al actualizar ubicación", e);
            throw new DatabaseException("Error al actualizar ubicación: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeNumeroDocumento(String numeroDocumento) throws DatabaseException {
        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new DatabaseException("El número de documento no puede ser nulo o vacío");
        }

        logger.debug("Verificando existencia de número de documento: {}", numeroDocumento);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(EXISTS_NUMERO_DOCUMENTO)) {

            ps.setString(1, numeroDocumento);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    boolean existe = rs.getInt(1) > 0;
                    logger.debug("Número de documento {} existe: {}", numeroDocumento, existe);
                    return existe;
                }
            }

            return false;

        } catch (SQLException e) {
            logger.error("Error al verificar número de documento", e);
            throw new DatabaseException("Error al verificar número de documento: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existeTelefono(String telefono) throws DatabaseException {
        if (telefono == null || telefono.trim().isEmpty()) {
            throw new DatabaseException("El teléfono no puede ser nulo o vacío");
        }

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
            throw new DatabaseException("Error al verificar teléfono: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto UsuarioPersona
     */
    private UsuarioPersona mapUsuarioPersona(ResultSet rs) throws SQLException {
        UsuarioPersona usuario = new UsuarioPersona();

        usuario.setId(rs.getLong("id"));
        usuario.setNombreCompleto(rs.getString("nombre_completo"));
        usuario.setTipoDocumento(rs.getString("tipo_documento"));
        usuario.setNumeroDocumento(rs.getString("numero_documento"));

        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            usuario.setFechaNacimiento(fechaNacimiento.toLocalDate());
        }

        usuario.setGenero(rs.getString("genero"));
        usuario.setTelefono(rs.getString("telefono"));
        usuario.setTelefonoAlternativo(rs.getString("telefono_alternativo"));

        Integer departamentoId = (Integer) rs.getObject("departamento_id");
        usuario.setDepartamentoId(departamentoId);

        Integer provinciaId = (Integer) rs.getObject("provincia_id");
        usuario.setProvinciaId(provinciaId);

        Integer distritoId = (Integer) rs.getObject("distrito_id");
        usuario.setDistritoId(distritoId);

        usuario.setDireccion(rs.getString("direccion"));
        usuario.setReferenciaDireccion(rs.getString("referencia_direccion"));
        usuario.setTipoRol(rs.getString("tipo_rol"));
        usuario.setEsCliente(rs.getBoolean("es_cliente"));
        usuario.setEsProfesional(rs.getBoolean("es_profesional"));
        usuario.setFotoPerfilUrl(rs.getString("foto_perfil_url"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            usuario.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        Timestamp fechaActualizacion = rs.getTimestamp("fecha_actualizacion");
        if (fechaActualizacion != null) {
            usuario.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
        }

        usuario.setActivo(rs.getBoolean("activo"));

        return usuario;
    }
}
