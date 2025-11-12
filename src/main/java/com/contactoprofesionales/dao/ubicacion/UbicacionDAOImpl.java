package com.contactoprofesionales.dao.ubicacion;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.Departamento;
import com.contactoprofesionales.model.Provincia;
import com.contactoprofesionales.model.Distrito;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de UbicacionDAO usando JDBC para PostgreSQL
 */
public class UbicacionDAOImpl implements UbicacionDAO {

    private static final Logger logger = LoggerFactory.getLogger(UbicacionDAOImpl.class);

    // ==================== SQL STATEMENTS - DEPARTAMENTOS ====================
    private static final String SELECT_ALL_DEPARTAMENTOS =
            "SELECT * FROM departamentos WHERE activo = true ORDER BY nombre";

    private static final String SELECT_DEPARTAMENTO_BY_ID =
            "SELECT * FROM departamentos WHERE id = ? AND activo = true";

    private static final String SELECT_DEPARTAMENTO_BY_CODIGO =
            "SELECT * FROM departamentos WHERE codigo = ? AND activo = true";

    // ==================== SQL STATEMENTS - PROVINCIAS ====================
    private static final String SELECT_PROVINCIAS_BY_DEPARTAMENTO =
            "SELECT * FROM provincias WHERE departamento_id = ? AND activo = true ORDER BY nombre";

    private static final String SELECT_PROVINCIA_BY_ID =
            "SELECT * FROM provincias WHERE id = ? AND activo = true";

    private static final String SELECT_PROVINCIA_BY_CODIGO =
            "SELECT * FROM provincias WHERE codigo = ? AND activo = true";

    // ==================== SQL STATEMENTS - DISTRITOS ====================
    private static final String SELECT_DISTRITOS_BY_PROVINCIA =
            "SELECT * FROM distritos WHERE provincia_id = ? AND activo = true ORDER BY nombre";

    private static final String SELECT_DISTRITO_BY_ID =
            "SELECT * FROM distritos WHERE id = ? AND activo = true";

    private static final String SELECT_DISTRITO_BY_CODIGO =
            "SELECT * FROM distritos WHERE codigo = ? AND activo = true";

    private static final String SELECT_DISTRITOS_BY_NOMBRE =
            "SELECT * FROM distritos WHERE LOWER(nombre) LIKE LOWER(?) AND activo = true ORDER BY nombre LIMIT 20";

    // ==================== IMPLEMENTACIÓN - DEPARTAMENTOS ====================

    @Override
    public List<Departamento> listarDepartamentos() throws DatabaseException {
        logger.debug("Listando todos los departamentos");
        List<Departamento> departamentos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_DEPARTAMENTOS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                departamentos.add(mapDepartamento(rs));
            }

            logger.info("Se encontraron {} departamentos", departamentos.size());
            return departamentos;

        } catch (SQLException e) {
            logger.error("Error al listar departamentos", e);
            throw new DatabaseException("ERROR_LISTAR_DEPARTAMENTOS",
                    "Error al obtener la lista de departamentos", e);
        }
    }

    @Override
    public Optional<Departamento> buscarDepartamentoPorId(Integer id) throws DatabaseException {
        logger.debug("Buscando departamento por ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DEPARTAMENTO_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Departamento departamento = mapDepartamento(rs);
                    logger.info("Departamento encontrado: {}", departamento.getNombre());
                    return Optional.of(departamento);
                }
            }

            logger.debug("No se encontró departamento con ID: {}", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar departamento por ID: {}", id, e);
            throw new DatabaseException("ERROR_BUSCAR_DEPARTAMENTO",
                    "Error al buscar departamento por ID", e);
        }
    }

    @Override
    public Optional<Departamento> buscarDepartamentoPorCodigo(String codigo) throws DatabaseException {
        logger.debug("Buscando departamento por código: {}", codigo);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DEPARTAMENTO_BY_CODIGO)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Departamento departamento = mapDepartamento(rs);
                    logger.info("Departamento encontrado: {}", departamento.getNombre());
                    return Optional.of(departamento);
                }
            }

            logger.debug("No se encontró departamento con código: {}", codigo);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar departamento por código: {}", codigo, e);
            throw new DatabaseException("ERROR_BUSCAR_DEPARTAMENTO",
                    "Error al buscar departamento por código", e);
        }
    }

    // ==================== IMPLEMENTACIÓN - PROVINCIAS ====================

    @Override
    public List<Provincia> listarProvinciasPorDepartamento(Integer departamentoId) throws DatabaseException {
        logger.debug("Listando provincias del departamento ID: {}", departamentoId);
        List<Provincia> provincias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PROVINCIAS_BY_DEPARTAMENTO)) {

            ps.setInt(1, departamentoId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    provincias.add(mapProvincia(rs));
                }
            }

            logger.info("Se encontraron {} provincias para el departamento {}", provincias.size(), departamentoId);
            return provincias;

        } catch (SQLException e) {
            logger.error("Error al listar provincias del departamento: {}", departamentoId, e);
            throw new DatabaseException("ERROR_LISTAR_PROVINCIAS",
                    "Error al obtener la lista de provincias", e);
        }
    }

    @Override
    public Optional<Provincia> buscarProvinciaPorId(Integer id) throws DatabaseException {
        logger.debug("Buscando provincia por ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PROVINCIA_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Provincia provincia = mapProvincia(rs);
                    logger.info("Provincia encontrada: {}", provincia.getNombre());
                    return Optional.of(provincia);
                }
            }

            logger.debug("No se encontró provincia con ID: {}", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar provincia por ID: {}", id, e);
            throw new DatabaseException("ERROR_BUSCAR_PROVINCIA",
                    "Error al buscar provincia por ID", e);
        }
    }

    @Override
    public Optional<Provincia> buscarProvinciaPorCodigo(String codigo) throws DatabaseException {
        logger.debug("Buscando provincia por código: {}", codigo);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_PROVINCIA_BY_CODIGO)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Provincia provincia = mapProvincia(rs);
                    logger.info("Provincia encontrada: {}", provincia.getNombre());
                    return Optional.of(provincia);
                }
            }

            logger.debug("No se encontró provincia con código: {}", codigo);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar provincia por código: {}", codigo, e);
            throw new DatabaseException("ERROR_BUSCAR_PROVINCIA",
                    "Error al buscar provincia por código", e);
        }
    }

    // ==================== IMPLEMENTACIÓN - DISTRITOS ====================

    @Override
    public List<Distrito> listarDistritosPorProvincia(Integer provinciaId) throws DatabaseException {
        logger.debug("Listando distritos de la provincia ID: {}", provinciaId);
        List<Distrito> distritos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DISTRITOS_BY_PROVINCIA)) {

            ps.setInt(1, provinciaId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    distritos.add(mapDistrito(rs));
                }
            }

            logger.info("Se encontraron {} distritos para la provincia {}", distritos.size(), provinciaId);
            return distritos;

        } catch (SQLException e) {
            logger.error("Error al listar distritos de la provincia: {}", provinciaId, e);
            throw new DatabaseException("ERROR_LISTAR_DISTRITOS",
                    "Error al obtener la lista de distritos", e);
        }
    }

    @Override
    public Optional<Distrito> buscarDistritoPorId(Integer id) throws DatabaseException {
        logger.debug("Buscando distrito por ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DISTRITO_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Distrito distrito = mapDistrito(rs);
                    logger.info("Distrito encontrado: {}", distrito.getNombre());
                    return Optional.of(distrito);
                }
            }

            logger.debug("No se encontró distrito con ID: {}", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar distrito por ID: {}", id, e);
            throw new DatabaseException("ERROR_BUSCAR_DISTRITO",
                    "Error al buscar distrito por ID", e);
        }
    }

    @Override
    public Optional<Distrito> buscarDistritoPorCodigo(String codigo) throws DatabaseException {
        logger.debug("Buscando distrito por código: {}", codigo);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DISTRITO_BY_CODIGO)) {

            ps.setString(1, codigo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Distrito distrito = mapDistrito(rs);
                    logger.info("Distrito encontrado: {}", distrito.getNombre());
                    return Optional.of(distrito);
                }
            }

            logger.debug("No se encontró distrito con código: {}", codigo);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar distrito por código: {}", codigo, e);
            throw new DatabaseException("ERROR_BUSCAR_DISTRITO",
                    "Error al buscar distrito por código", e);
        }
    }

    @Override
    public List<Distrito> buscarDistritosPorNombre(String nombre) throws DatabaseException {
        logger.debug("Buscando distritos por nombre: {}", nombre);
        List<Distrito> distritos = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_DISTRITOS_BY_NOMBRE)) {

            ps.setString(1, "%" + nombre + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    distritos.add(mapDistrito(rs));
                }
            }

            logger.info("Se encontraron {} distritos con nombre similar a: {}", distritos.size(), nombre);
            return distritos;

        } catch (SQLException e) {
            logger.error("Error al buscar distritos por nombre: {}", nombre, e);
            throw new DatabaseException("ERROR_BUSCAR_DISTRITOS",
                    "Error al buscar distritos por nombre", e);
        }
    }

    // ==================== MÉTODOS DE MAPEO ====================

    private Departamento mapDepartamento(ResultSet rs) throws SQLException {
        Departamento departamento = new Departamento();
        departamento.setId(rs.getInt("id"));
        departamento.setCodigo(rs.getString("codigo"));
        departamento.setNombre(rs.getString("nombre"));
        departamento.setCapital(rs.getString("capital"));
        departamento.setActivo(rs.getBoolean("activo"));

        Timestamp timestamp = rs.getTimestamp("fecha_creacion");
        if (timestamp != null) {
            departamento.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return departamento;
    }

    private Provincia mapProvincia(ResultSet rs) throws SQLException {
        Provincia provincia = new Provincia();
        provincia.setId(rs.getInt("id"));
        provincia.setDepartamentoId(rs.getInt("departamento_id"));
        provincia.setCodigo(rs.getString("codigo"));
        provincia.setNombre(rs.getString("nombre"));
        provincia.setActivo(rs.getBoolean("activo"));

        Timestamp timestamp = rs.getTimestamp("fecha_creacion");
        if (timestamp != null) {
            provincia.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return provincia;
    }

    private Distrito mapDistrito(ResultSet rs) throws SQLException {
        Distrito distrito = new Distrito();
        distrito.setId(rs.getInt("id"));
        distrito.setProvinciaId(rs.getInt("provincia_id"));
        distrito.setCodigo(rs.getString("codigo"));
        distrito.setNombre(rs.getString("nombre"));
        distrito.setActivo(rs.getBoolean("activo"));

        Timestamp timestamp = rs.getTimestamp("fecha_creacion");
        if (timestamp != null) {
            distrito.setFechaCreacion(timestamp.toLocalDateTime());
        }

        return distrito;
    }
}
