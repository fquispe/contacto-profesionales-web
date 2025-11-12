package com.contactoprofesionales.service.profesional;

import com.contactoprofesionales.dto.EspecialidadDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;

import java.util.List;

/**
 * Servicio de negocio para operaciones de especialidades profesionales.
 * Gestiona la lógica de negocio para las especialidades de un profesional.
 */
public interface EspecialidadService {

    /**
     * Agrega una nueva especialidad a un profesional
     * @param profesionalId ID del profesional
     * @param categoriaId ID de la categoría de servicio
     * @param aniosExp Años de experiencia en la especialidad
     * @param desc Descripción de la especialidad
     * @param costo Costo del servicio
     * @param tipoCosto Tipo de costo (hora, dia, mes)
     * @param incluyeMateriales Si incluye materiales
     * @param orden Orden de la especialidad (1-3)
     * @param esPrincipal Si es la especialidad principal del profesional
     * @return Especialidad agregada con su ID generado
     * @throws ValidationException si los datos no son válidos o se excede el límite de especialidades
     * @throws DatabaseException si ocurre un error al agregar en la base de datos
     */
    EspecialidadDTO agregar(Integer profesionalId, Integer categoriaId, Integer aniosExp,
                           String desc, Double costo, String tipoCosto, Boolean incluyeMateriales,
                           Integer orden, Boolean esPrincipal)
            throws ValidationException, DatabaseException;

    /**
     * Lista todas las especialidades de un profesional
     * @param profesionalId ID del profesional
     * @return Lista de especialidades del profesional
     * @throws ValidationException si el profesionalId es nulo o inválido
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    List<EspecialidadDTO> listarPorProfesional(Integer profesionalId)
            throws ValidationException, DatabaseException;

    /**
     * Elimina una especialidad de un profesional
     * @param id ID de la especialidad a eliminar
     * @return true si se eliminó correctamente
     * @throws ValidationException si el ID es nulo o inválido
     * @throws DatabaseException si ocurre un error al eliminar en la base de datos
     */
    boolean eliminar(Integer id) throws ValidationException, DatabaseException;

    /**
     * Marca una especialidad como principal para un profesional.
     * Automáticamente desmarca otras especialidades como principales.
     * @param id ID de la especialidad a marcar como principal
     * @param profesionalId ID del profesional
     * @return true si se actualizó correctamente
     * @throws ValidationException si los IDs son nulos o inválidos
     * @throws DatabaseException si ocurre un error al actualizar en la base de datos
     */
    boolean marcarComoPrincipal(Integer id, Integer profesionalId)
            throws ValidationException, DatabaseException;

    /**
     * Valida que un profesional no exceda el límite de 3 especialidades
     * @param profesionalId ID del profesional
     * @throws ValidationException si el profesional ya tiene 3 especialidades
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    void validarLimiteEspecialidades(Integer profesionalId)
            throws ValidationException, DatabaseException;
}
