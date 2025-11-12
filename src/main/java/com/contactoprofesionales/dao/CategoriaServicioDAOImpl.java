package com.contactoprofesionales.dao;

import com.contactoprofesionales.model.CategoriaServicio;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del DAO para categorías de servicio.
 * Solo operaciones de consulta.
 */
public class CategoriaServicioDAOImpl implements CategoriaServicioDAO {

    private static final Logger logger = LoggerFactory.getLogger(CategoriaServicioDAOImpl.class);

    @Override
    public List<CategoriaServicio> listarActivas() throws Exception {
        String sql = "SELECT * FROM categorias_servicio WHERE activo = TRUE ORDER BY nombre ASC";

        List<CategoriaServicio> categorias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

            logger.info("Se encontraron {} categorías activas", categorias.size());
            return categorias;

        } catch (SQLException e) {
            logger.error("Error listando categorías activas", e);
            throw e;
        }
    }

    @Override
    public List<CategoriaServicio> listarTodas() throws Exception {
        String sql = "SELECT * FROM categorias_servicio ORDER BY activo DESC, nombre ASC";

        List<CategoriaServicio> categorias = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categorias.add(mapearCategoria(rs));
            }

            logger.info("Se encontraron {} categorías en total", categorias.size());
            return categorias;

        } catch (SQLException e) {
            logger.error("Error listando todas las categorías", e);
            throw e;
        }
    }

    @Override
    public CategoriaServicio buscarPorId(Integer id) throws Exception {
        String sql = "SELECT * FROM categorias_servicio WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearCategoria(rs);
            }

            return null;

        } catch (SQLException e) {
            logger.error("Error buscando categoría por ID: {}", id, e);
            throw e;
        }
    }

    @Override
    public CategoriaServicio buscarPorNombre(String nombre) throws Exception {
        String sql = "SELECT * FROM categorias_servicio WHERE nombre ILIKE ? AND activo = TRUE";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapearCategoria(rs);
            }

            return null;

        } catch (SQLException e) {
            logger.error("Error buscando categoría por nombre: {}", nombre, e);
            throw e;
        }
    }

    /**
     * Mapea un ResultSet a un objeto CategoriaServicio.
     */
    private CategoriaServicio mapearCategoria(ResultSet rs) throws SQLException {
        CategoriaServicio categoria = new CategoriaServicio();
        categoria.setId(rs.getInt("id"));
        categoria.setNombre(rs.getString("nombre"));
        categoria.setDescripcion(rs.getString("descripcion"));
        categoria.setIcono(rs.getString("icono"));
        categoria.setColor(rs.getString("color"));
        categoria.setActivo(rs.getBoolean("activo"));
        categoria.setFechaCreacion(rs.getTimestamp("fecha_creacion").toLocalDateTime());
        return categoria;
    }
}
