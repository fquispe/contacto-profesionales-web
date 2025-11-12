package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.RedSocialProfesional;

import java.util.List;

/**
 * Interface DAO para operaciones CRUD de RedSocialProfesional
 */
public interface RedSocialProfesionalDAO {

    /**
     * Registra una nueva red social para un profesional
     * @param redSocial Red social a registrar
     * @return Red social registrada con su ID generado
     * @throws DatabaseException si ocurre un error en el registro
     */
    RedSocialProfesional registrar(RedSocialProfesional redSocial) throws DatabaseException;

    /**
     * Lista todas las redes sociales de un profesional
     * @param profesionalId ID del profesional
     * @return Lista de redes sociales del profesional
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<RedSocialProfesional> listarPorProfesional(Integer profesionalId) throws DatabaseException;

    /**
     * Actualiza la información de una red social
     * @param redSocial Red social con la información actualizada
     * @return Red social actualizada
     * @throws DatabaseException si la red social no existe o ocurre un error
     */
    RedSocialProfesional actualizar(RedSocialProfesional redSocial) throws DatabaseException;

    /**
     * Elimina una red social de un profesional
     * @param id ID de la red social a eliminar
     * @return true si se eliminó correctamente
     * @throws DatabaseException si ocurre un error
     */
    boolean eliminar(Integer id) throws DatabaseException;
}
