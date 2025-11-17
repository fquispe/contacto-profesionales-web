package com.contactoprofesionales.dao.portafolio;

import com.contactoprofesionales.model.ProyectoPortafolio;
import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para gestión de proyectos del portafolio profesional.
 *
 * Permite a los profesionales gestionar su portafolio de proyectos realizados.
 * Cada profesional puede tener un máximo de 20 proyectos activos.
 *
 * Aplicación de DIP: Define el contrato sin depender de implementación específica.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public interface ProyectosPortafolioDAO {

    /**
     * Lista todos los proyectos activos de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Lista de proyectos ordenados por fecha de realización (más reciente primero)
     * @throws Exception si hay error en la consulta
     */
    List<ProyectoPortafolio> listarPorProfesional(Integer profesionalId) throws Exception;

    /**
     * Busca un proyecto específico por su ID, incluyendo sus imágenes.
     *
     * @param id ID del proyecto
     * @return Optional con el proyecto si existe (incluyendo imágenes)
     * @throws Exception si hay error en la consulta
     */
    Optional<ProyectoPortafolio> buscarPorId(Integer id) throws Exception;

    /**
     * Guarda un nuevo proyecto en el portafolio.
     * Valida que el profesional no tenga ya 20 proyectos activos.
     *
     * @param proyecto Proyecto a guardar
     * @return ID del proyecto creado
     * @throws Exception si hay error al guardar o si se excede el límite de 20 proyectos
     */
    Integer guardar(ProyectoPortafolio proyecto) throws Exception;

    /**
     * Actualiza un proyecto existente.
     *
     * @param proyecto Proyecto con datos actualizados
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    boolean actualizar(ProyectoPortafolio proyecto) throws Exception;

    /**
     * Elimina (soft delete) un proyecto.
     *
     * @param id ID del proyecto a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    boolean eliminar(Integer id) throws Exception;

    /**
     * Cuenta el número de proyectos activos de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Número de proyectos activos
     * @throws Exception si hay error en la consulta
     */
    int contarActivosPorProfesional(Integer profesionalId) throws Exception;

    /**
     * Actualiza la calificación de un proyecto.
     * NOTA: Solo puede ser llamado desde el módulo de valoración de clientes.
     *
     * @param proyectoId ID del proyecto
     * @param calificacion Calificación del cliente (0-10)
     * @param comentario Comentario del cliente
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    boolean actualizarCalificacion(Integer proyectoId, Double calificacion, String comentario) throws Exception;
}
