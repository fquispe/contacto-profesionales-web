package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.EspecialidadProfesional;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para especialidades profesionales.
 * Define las operaciones de acceso a datos para la tabla especialidades_profesional.
 */
public interface EspecialidadProfesionalDAO {

    /**
     * Registra una nueva especialidad en la base de datos
     * 
     * @param especialidad Objeto con los datos de la especialidad
     * @return EspecialidadProfesional con el ID generado
     * @throws DatabaseException si ocurre un error en la BD
     */
    EspecialidadProfesional registrar(EspecialidadProfesional especialidad) 
            throws DatabaseException;

    /**
     * Busca una especialidad por su ID
     * 
     * @param id ID de la especialidad
     * @return Optional con la especialidad si existe, Optional.empty() si no
     * @throws DatabaseException si ocurre un error en la BD
     */
    Optional<EspecialidadProfesional> buscarPorId(Integer id) 
            throws DatabaseException;

    /**
     * Lista todas las especialidades activas de un profesional
     * 
     * @param profesionalId ID del profesional
     * @return Lista de especialidades (incluye datos de categoría via JOIN)
     * @throws DatabaseException si ocurre un error en la BD
     */
    List<EspecialidadProfesional> listarPorProfesional(Integer profesionalId) 
            throws DatabaseException;

    /**
     * Actualiza los datos de una especialidad
     * 
     * @param especialidad Objeto con los datos actualizados
     * @return EspecialidadProfesional actualizada con datos frescos de BD
     * @throws DatabaseException si ocurre un error en la BD
     */
    EspecialidadProfesional actualizar(EspecialidadProfesional especialidad) 
            throws DatabaseException;

    /**
     * Elimina una especialidad (soft delete: marca activo=false)
     * 
     * @param id ID de la especialidad a eliminar
     * @return true si se eliminó correctamente, false si no se encontró
     * @throws DatabaseException si ocurre un error en la BD
     */
    boolean eliminar(Integer id) 
            throws DatabaseException;

    /**
     * Marca una especialidad como principal y desmarca las demás del mismo profesional
     * Operación transaccional (todas las actualizaciones se confirman o revierten juntas)
     * 
     * @param id ID de la especialidad a marcar como principal
     * @param profesionalId ID del profesional propietario
     * @return true si se marcó correctamente, false si no se encontró
     * @throws DatabaseException si ocurre un error en la BD
     */
    boolean marcarComoPrincipal(Integer id, Integer profesionalId) 
            throws DatabaseException;

    /**
     * Cuenta el número de especialidades activas de un profesional
     * 
     * @param profesionalId ID del profesional
     * @return Número de especialidades activas
     * @throws DatabaseException si ocurre un error en la BD
     */
    int contarPorProfesional(Integer profesionalId) 
            throws DatabaseException;

    /**
     * Verifica si un profesional ya tiene una especialidad con una categoría específica
     * 
     * @param profesionalId ID del profesional
     * @param categoriaId ID de la categoría
     * @return true si existe, false si no
     * @throws DatabaseException si ocurre un error en la BD
     */
    boolean existeEspecialidadConCategoria(Integer profesionalId, Integer categoriaId) 
            throws DatabaseException;
}