package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.EspecialidadProfesional;

import java.util.List;

/**
 * Interface DAO para operaciones CRUD de EspecialidadProfesional
 */
public interface EspecialidadProfesionalDAO {

    /**
     * Registra una nueva especialidad para un profesional
     * @param especialidad Especialidad a registrar
     * @return Especialidad registrada con su ID generado
     * @throws DatabaseException si ocurre un error en el registro
     */
    EspecialidadProfesional registrar(EspecialidadProfesional especialidad) throws DatabaseException;

    /**
     * Lista todas las especialidades de un profesional
     * @param profesionalId ID del profesional
     * @return Lista de especialidades del profesional (incluye datos de categoría)
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<EspecialidadProfesional> listarPorProfesional(Integer profesionalId) throws DatabaseException;

    /**
     * Elimina una especialidad de un profesional
     * @param id ID de la especialidad a eliminar
     * @return true si se eliminó correctamente
     * @throws DatabaseException si ocurre un error
     */
    boolean eliminar(Integer id) throws DatabaseException;

    /**
     * Marca una especialidad como principal para un profesional
     * Desmarca las otras especialidades del mismo profesional
     * @param id ID de la especialidad a marcar como principal
     * @param profesionalId ID del profesional
     * @return true si se actualizó correctamente
     * @throws DatabaseException si ocurre un error
     */
    boolean marcarComoPrincipal(Integer id, Integer profesionalId) throws DatabaseException;
}
