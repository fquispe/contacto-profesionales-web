package com.contactoprofesionales.dao.portafolio;

import com.contactoprofesionales.model.ImagenProyecto;
import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para gestión de imágenes de proyectos del portafolio.
 *
 * Permite gestionar las imágenes (antes/después/proceso) de los proyectos.
 * Cada proyecto puede tener un máximo de 5 imágenes.
 *
 * Aplicación de DIP: Define el contrato sin depender de implementación específica.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public interface ImagenesProyectoDAO {

    /**
     * Lista todas las imágenes de un proyecto específico.
     *
     * @param proyectoId ID del proyecto
     * @return Lista de imágenes ordenadas por orden
     * @throws Exception si hay error en la consulta
     */
    List<ImagenProyecto> listarPorProyecto(Integer proyectoId) throws Exception;

    /**
     * Busca una imagen específica por su ID.
     *
     * @param id ID de la imagen
     * @return Optional con la imagen si existe
     * @throws Exception si hay error en la consulta
     */
    Optional<ImagenProyecto> buscarPorId(Integer id) throws Exception;

    /**
     * Guarda una nueva imagen para un proyecto.
     * Valida que el proyecto no tenga ya 5 imágenes.
     *
     * @param imagen Imagen a guardar
     * @return ID de la imagen creada
     * @throws Exception si hay error al guardar o si se excede el límite de 5 imágenes
     */
    Integer guardar(ImagenProyecto imagen) throws Exception;

    /**
     * Elimina una imagen del proyecto.
     * Elimina físicamente el registro (no es soft delete).
     *
     * @param id ID de la imagen a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    boolean eliminar(Integer id) throws Exception;

    /**
     * Cuenta el número de imágenes de un proyecto.
     *
     * @param proyectoId ID del proyecto
     * @return Número de imágenes del proyecto
     * @throws Exception si hay error en la consulta
     */
    int contarPorProyecto(Integer proyectoId) throws Exception;
}
