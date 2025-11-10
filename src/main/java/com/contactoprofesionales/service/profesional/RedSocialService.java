package com.contactoprofesionales.service.profesional;

import com.contactoprofesionales.dto.RedSocialDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;

import java.util.List;

/**
 * Servicio de negocio para operaciones de redes sociales de profesionales.
 * Gestiona la lógica de negocio para las redes sociales de un profesional.
 */
public interface RedSocialService {

    /**
     * Agrega una nueva red social a un profesional
     * @param profesionalId ID del profesional
     * @param tipoRed Tipo de red social (FACEBOOK, INSTAGRAM, LINKEDIN, etc.)
     * @param url URL de la red social
     * @return Red social agregada con su ID generado
     * @throws ValidationException si los datos no son válidos o la red social ya existe
     * @throws DatabaseException si ocurre un error al agregar en la base de datos
     */
    RedSocialDTO agregar(Integer profesionalId, String tipoRed, String url)
            throws ValidationException, DatabaseException;

    /**
     * Lista todas las redes sociales de un profesional
     * @param profesionalId ID del profesional
     * @return Lista de redes sociales del profesional
     * @throws ValidationException si el profesionalId es nulo o inválido
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    List<RedSocialDTO> listarPorProfesional(Integer profesionalId)
            throws ValidationException, DatabaseException;

    /**
     * Actualiza la URL de una red social
     * @param id ID de la red social
     * @param url Nueva URL
     * @return Red social actualizada
     * @throws ValidationException si los datos no son válidos
     * @throws DatabaseException si ocurre un error al actualizar en la base de datos
     */
    RedSocialDTO actualizar(Integer id, String url)
            throws ValidationException, DatabaseException;

    /**
     * Elimina una red social de un profesional
     * @param id ID de la red social a eliminar
     * @return true si se eliminó correctamente
     * @throws ValidationException si el ID es nulo o inválido
     * @throws DatabaseException si ocurre un error al eliminar en la base de datos
     */
    boolean eliminar(Integer id) throws ValidationException, DatabaseException;

    /**
     * Valida el formato de una URL
     * @param url URL a validar
     * @throws ValidationException si la URL no tiene un formato válido
     */
    void validarUrl(String url) throws ValidationException;

    /**
     * Valida que el tipo de red social sea uno de los 8 tipos válidos
     * @param tipoRed Tipo de red social a validar
     * @throws ValidationException si el tipo de red no es válido
     */
    void validarTipoRed(String tipoRed) throws ValidationException;
}
