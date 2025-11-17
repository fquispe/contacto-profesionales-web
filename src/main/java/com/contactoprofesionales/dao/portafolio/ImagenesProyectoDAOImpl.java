package com.contactoprofesionales.dao.portafolio;

import com.contactoprofesionales.model.ImagenProyecto;
import com.contactoprofesionales.model.ImagenProyecto.TipoImagen;
import com.contactoprofesionales.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del DAO para imágenes de proyectos del portafolio.
 *
 * Maneja las imágenes (antes/después/proceso/general) de los proyectos.
 * Valida que un proyecto no tenga más de 5 imágenes.
 *
 * Aplicación de SRP: Solo se encarga de persistencia de imágenes de proyectos.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class ImagenesProyectoDAOImpl implements ImagenesProyectoDAO {

    private static final Logger logger = LoggerFactory.getLogger(ImagenesProyectoDAOImpl.class);
    private static final int MAX_IMAGENES_POR_PROYECTO = 5;

    /**
     * Lista todas las imágenes de un proyecto ordenadas por orden y tipo.
     *
     * @param proyectoId ID del proyecto
     * @return Lista de imágenes
     * @throws Exception si hay error en la consulta
     */
    @Override
    public List<ImagenProyecto> listarPorProyecto(Integer proyectoId) throws Exception {
        logger.debug("Listando imágenes del proyecto {}", proyectoId);

        List<ImagenProyecto> imagenes = new ArrayList<>();

        String sql = "SELECT id, proyecto_id, url_imagen, tipo_imagen, descripcion, " +
                    "orden, fecha_subida " +
                    "FROM imagenes_proyecto " +
                    "WHERE proyecto_id = ? " +
                    "ORDER BY orden ASC, tipo_imagen ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, proyectoId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    imagenes.add(mapearImagen(rs));
                }
            }

            logger.debug("Se encontraron {} imágenes para el proyecto {}", imagenes.size(), proyectoId);
            return imagenes;

        } catch (SQLException e) {
            logger.error("Error al listar imágenes del proyecto {}", proyectoId, e);
            throw new Exception("Error al obtener imágenes del proyecto", e);
        }
    }

    /**
     * Busca una imagen específica por su ID.
     *
     * @param id ID de la imagen
     * @return Optional con la imagen si existe
     * @throws Exception si hay error en la consulta
     */
    @Override
    public Optional<ImagenProyecto> buscarPorId(Integer id) throws Exception {
        logger.debug("Buscando imagen con ID {}", id);

        String sql = "SELECT id, proyecto_id, url_imagen, tipo_imagen, descripcion, " +
                    "orden, fecha_subida " +
                    "FROM imagenes_proyecto " +
                    "WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ImagenProyecto imagen = mapearImagen(rs);
                    logger.debug("Imagen encontrada: {}", imagen);
                    return Optional.of(imagen);
                }
            }

            logger.debug("Imagen con ID {} no encontrada", id);
            return Optional.empty();

        } catch (SQLException e) {
            logger.error("Error al buscar imagen con ID {}", id, e);
            throw new Exception("Error al buscar imagen", e);
        }
    }

    /**
     * Guarda una nueva imagen para un proyecto.
     * VALIDA que el proyecto no tenga ya 5 imágenes antes de insertar.
     *
     * @param imagen Imagen a guardar
     * @return ID de la imagen creada
     * @throws Exception si hay error al guardar o si se excede el límite de 5 imágenes
     */
    @Override
    public Integer guardar(ImagenProyecto imagen) throws Exception {
        logger.info("Guardando nueva imagen para proyecto {}", imagen.getProyectoId());

        // ✅ VALIDACIÓN: Verificar que no tenga ya 5 imágenes
        int totalImagenes = contarPorProyecto(imagen.getProyectoId());
        if (totalImagenes >= MAX_IMAGENES_POR_PROYECTO) {
            logger.warn("✗ Proyecto {} ya tiene {} imágenes (máximo {})",
                    imagen.getProyectoId(), totalImagenes, MAX_IMAGENES_POR_PROYECTO);
            throw new Exception("El proyecto ya tiene el máximo de " + MAX_IMAGENES_POR_PROYECTO +
                    " imágenes. Elimina alguna antes de agregar una nueva.");
        }

        String sql = "INSERT INTO imagenes_proyecto " +
                    "(proyecto_id, url_imagen, tipo_imagen, descripcion, orden, fecha_subida) " +
                    "VALUES (?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, imagen.getProyectoId());
            stmt.setString(2, imagen.getUrlImagen());
            stmt.setString(3, imagen.getTipoImagenString()); // Convierte enum a string
            stmt.setString(4, imagen.getDescripcion());
            stmt.setInt(5, imagen.getOrden() != null ? imagen.getOrden() : 1);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        Integer id = rs.getInt(1);
                        imagen.setId(id);
                        logger.info("✓ Imagen guardada exitosamente con ID {}", id);
                        return id;
                    }
                }
            }

            throw new Exception("No se pudo guardar la imagen");

        } catch (SQLException e) {
            logger.error("✗ Error al guardar imagen", e);
            throw new Exception("Error al guardar imagen: " + e.getMessage(), e);
        }
    }

    /**
     * Elimina físicamente una imagen del proyecto.
     * NO es soft delete - elimina el registro permanentemente.
     * IMPORTANTE: Antes de llamar este método, eliminar el archivo físico del servidor.
     *
     * @param id ID de la imagen a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    @Override
    public boolean eliminar(Integer id) throws Exception {
        logger.info("Eliminando imagen ID {}", id);

        String sql = "DELETE FROM imagenes_proyecto WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("✓ Imagen eliminada exitosamente");
                return true;
            }

            logger.warn("No se eliminó ninguna imagen con ID {}", id);
            return false;

        } catch (SQLException e) {
            logger.error("✗ Error al eliminar imagen", e);
            throw new Exception("Error al eliminar imagen", e);
        }
    }

    /**
     * Cuenta el número de imágenes de un proyecto.
     * Usado para validar el límite de 5 imágenes.
     *
     * @param proyectoId ID del proyecto
     * @return Número de imágenes del proyecto
     * @throws Exception si hay error en la consulta
     */
    @Override
    public int contarPorProyecto(Integer proyectoId) throws Exception {
        logger.debug("Contando imágenes del proyecto {}", proyectoId);

        String sql = "SELECT COUNT(*) FROM imagenes_proyecto WHERE proyecto_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, proyectoId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt(1);
                    logger.debug("Proyecto {} tiene {} imágenes", proyectoId, total);
                    return total;
                }
            }

            return 0;

        } catch (SQLException e) {
            logger.error("Error al contar imágenes del proyecto {}", proyectoId, e);
            throw new Exception("Error al contar imágenes", e);
        }
    }

    /**
     * Mapea un ResultSet a un objeto ImagenProyecto.
     *
     * @param rs ResultSet con los datos de la imagen
     * @return Objeto ImagenProyecto mapeado
     * @throws SQLException si hay error al leer el ResultSet
     */
    private ImagenProyecto mapearImagen(ResultSet rs) throws SQLException {
        ImagenProyecto imagen = new ImagenProyecto();

        imagen.setId(rs.getInt("id"));
        imagen.setProyectoId(rs.getInt("proyecto_id"));
        imagen.setUrlImagen(rs.getString("url_imagen"));

        // ✅ Convertir string de BD a enum
        String tipoStr = rs.getString("tipo_imagen");
        if (tipoStr != null) {
            imagen.setTipoImagenString(tipoStr);
        }

        imagen.setDescripcion(rs.getString("descripcion"));
        imagen.setOrden(rs.getInt("orden"));

        // ✅ Timestamp
        Timestamp fechaSubida = rs.getTimestamp("fecha_subida");
        if (fechaSubida != null) {
            imagen.setFechaSubida(fechaSubida.toLocalDateTime());
        }

        return imagen;
    }
}
