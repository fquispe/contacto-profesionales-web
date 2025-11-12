package com.contactoprofesionales.dao.categoria;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.CategoriaServicio;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación de CategoriaServicioDAO usando JDBC para PostgreSQL con HikariCP
 * Utiliza try-with-resources para gestión automática de recursos
 */
public class CategoriaServicioDAOImpl implements CategoriaServicioDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServicioDAOImpl.class);

    private static final String SELECT_ALL_ACTIVAS =
        "SELECT * FROM categorias_servicio WHERE activo = true ORDER BY nombre ASC";

    private static final String SELECT_BY_ID =
        "SELECT * FROM categorias_servicio WHERE id = ?";

    private static final String SELECT_BY_NOMBRE =
        "SELECT * FROM categorias_servicio WHERE nombre = ? AND activo = true";

    @Override
    public List<CategoriaServicio> listarActivas() throws DatabaseException {
        logger.debug("Listando todas las categorías de servicio activas");
        List<CategoriaServicio> categorias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_ACTIVAS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapCategoria(rs));
            }

            logger.info("Se encontraron {} categorías de servicio activas", categorias.size());
            return categorias;

        } catch (SQLException e) {
            logger.error("Error al listar categorías de servicio", e);
            throw new DatabaseException("Error al listar categorías de servicio: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<CategoriaServicio> buscarPorId(Integer id) throws DatabaseException {
        if (id == null) {
            throw new DatabaseException("El ID no puede ser nulo");
        }

        logger.debug("Buscando categoría de servicio por ID: {}", id);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CategoriaServicio categoria = mapCategoria(rs);
                    logger.debug("Categoría de servicio encontrada: {}", id);
                    return Optional.of(categoria);
                }
            }

            logger.debug("Categoría de servicio no encontrada: {}", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar categoría de servicio por ID", e);
            throw new DatabaseException("Error al buscar categoría de servicio por ID: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<CategoriaServicio> buscarPorNombre(String nombre) throws DatabaseException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new DatabaseException("El nombre no puede ser nulo o vacío");
        }

        logger.debug("Buscando categoría de servicio por nombre: {}", nombre);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_NOMBRE)) {

            ps.setString(1, nombre);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CategoriaServicio categoria = mapCategoria(rs);
                    logger.debug("Categoría de servicio encontrada por nombre");
                    return Optional.of(categoria);
                }
            }

            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar categoría de servicio por nombre", e);
            throw new DatabaseException("Error al buscar categoría de servicio por nombre: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto CategoriaServicio
     */
    private CategoriaServicio mapCategoria(ResultSet rs) throws SQLException {
        CategoriaServicio categoria = new CategoriaServicio();

        categoria.setId(rs.getInt("id"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setIcono(rs.getString("icono"));
        categoria.setColor(rs.getString("color"));
        categoria.setActivo(rs.getBoolean("activo"));

        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            categoria.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }

        return categoria;
    }
}
