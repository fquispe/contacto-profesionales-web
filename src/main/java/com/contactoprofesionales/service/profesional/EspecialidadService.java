package com.contactoprofesionales.service.profesional;

import com.contactoprofesionales.dto.EspecialidadDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;

import java.util.List;

/**
 * Interfaz del servicio de especialidades profesionales.
 * Define las operaciones disponibles para gestionar especialidades.
 */
public interface EspecialidadService {

    /**
     * Agrega una nueva especialidad a un profesional
     * 
     * @param profesionalId ID del profesional
     * @param categoriaId ID de la categoría de servicio
     * @param descripcion Descripción de la especialidad (opcional)
     * @param incluyeMateriales Indica si incluye materiales
     * @param costo Costo del servicio
     * @param tipoCosto Tipo de costo ('hora', 'dia', 'mes')
     * @param esPrincipal Indica si es la especialidad principal
     * @return EspecialidadDTO con los datos de la especialidad creada
     * @throws ValidationException si los datos no son válidos
     * @throws DatabaseException si hay error en la BD
     */
    EspecialidadDTO agregar(Integer profesionalId, Integer categoriaId, 
                           String descripcion, Boolean incluyeMateriales,
                           Double costo, String tipoCosto, Boolean esPrincipal)
            throws ValidationException, DatabaseException;

    /**
     * Actualiza una especialidad existente
     * 
     * @param id ID de la especialidad
     * @param descripcion Nueva descripción (opcional)
     * @param incluyeMateriales Si incluye materiales
     * @param costo Nuevo costo (opcional)
     * @param tipoCosto Nuevo tipo de costo (opcional)
     * @return EspecialidadDTO con los datos actualizados
     * @throws ValidationException si los datos no son válidos
     * @throws DatabaseException si hay error en la BD
     */
    EspecialidadDTO actualizar(Integer id, String descripcion, Boolean incluyeMateriales,
                              Double costo, String tipoCosto)
            throws ValidationException, DatabaseException;

    /**
     * Lista todas las especialidades de un profesional
     * 
     * @param profesionalId ID del profesional
     * @return Lista de EspecialidadDTO
     * @throws ValidationException si el ID no es válido
     * @throws DatabaseException si hay error en la BD
     */
    List<EspecialidadDTO> listarPorProfesional(Integer profesionalId)
            throws ValidationException, DatabaseException;

    /**
     * Elimina una especialidad
     * 
     * @param id ID de la especialidad a eliminar
     * @return true si se eliminó correctamente
     * @throws ValidationException si el ID no es válido
     * @throws DatabaseException si hay error en la BD
     */
    boolean eliminar(Integer id) throws ValidationException, DatabaseException;

    /**
     * Marca una especialidad como principal y desmarca las demás
     * 
     * @param id ID de la especialidad a marcar como principal
     * @param profesionalId ID del profesional propietario
     * @return true si se marcó correctamente
     * @throws ValidationException si los IDs no son válidos
     * @throws DatabaseException si hay error en la BD
     */
    boolean marcarComoPrincipal(Integer id, Integer profesionalId)
            throws ValidationException, DatabaseException;

    /**
     * Valida que un profesional no exceda el límite de especialidades
     * 
     * @param profesionalId ID del profesional
     * @throws ValidationException si se excede el límite
     * @throws DatabaseException si hay error en la BD
     */
    void validarLimiteEspecialidades(Integer profesionalId)
            throws ValidationException, DatabaseException;
}