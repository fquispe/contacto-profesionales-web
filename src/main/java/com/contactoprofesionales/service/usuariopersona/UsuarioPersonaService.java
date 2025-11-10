package com.contactoprofesionales.service.usuariopersona;

import com.contactoprofesionales.dto.UsuarioPersonaDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;

/**
 * Servicio de negocio para operaciones de UsuarioPersona.
 * Gestiona la lógica de negocio para datos personales de usuarios.
 */
public interface UsuarioPersonaService {

    /**
     * Registra un nuevo usuario persona en el sistema
     * @param dto Datos del usuario a registrar
     * @return Usuario registrado con su ID generado
     * @throws ValidationException si los datos no son válidos
     * @throws DatabaseException si ocurre un error al registrar en la base de datos
     */
    UsuarioPersonaDTO registrar(UsuarioPersonaDTO dto) throws ValidationException, DatabaseException;

    /**
     * Actualiza la información de un usuario persona existente
     * @param id ID del usuario a actualizar
     * @param dto Datos actualizados del usuario
     * @return Usuario actualizado
     * @throws ValidationException si los datos no son válidos o el usuario no existe
     * @throws DatabaseException si ocurre un error al actualizar en la base de datos
     */
    UsuarioPersonaDTO actualizar(Long id, UsuarioPersonaDTO dto) throws ValidationException, DatabaseException;

    /**
     * Obtiene un usuario persona por su ID
     * @param id ID del usuario
     * @return Usuario encontrado
     * @throws ValidationException si el ID es nulo o el usuario no existe
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    UsuarioPersonaDTO obtenerPorId(Long id) throws ValidationException, DatabaseException;

    /**
     * Obtiene un usuario persona por su número de documento
     * @param numeroDocumento Número de documento del usuario
     * @return Usuario encontrado
     * @throws ValidationException si el número de documento es nulo/vacío o el usuario no existe
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    UsuarioPersonaDTO obtenerPorNumeroDocumento(String numeroDocumento)
            throws ValidationException, DatabaseException;

    /**
     * Actualiza el tipo de rol de un usuario
     * @param id ID del usuario
     * @param tipoRol Nuevo tipo de rol (CLIENTE, PROFESIONAL, AMBOS)
     * @return true si se actualizó correctamente
     * @throws ValidationException si los datos no son válidos
     * @throws DatabaseException si ocurre un error al actualizar en la base de datos
     */
    boolean actualizarTipoRol(Long id, String tipoRol) throws ValidationException, DatabaseException;

    /**
     * Actualiza la ubicación de un usuario
     * @param id ID del usuario
     * @param deptoId ID del departamento
     * @param provId ID de la provincia
     * @param distId ID del distrito
     * @param direccion Dirección completa
     * @param referencia Referencia de la dirección
     * @return true si se actualizó correctamente
     * @throws ValidationException si los datos no son válidos
     * @throws DatabaseException si ocurre un error al actualizar en la base de datos
     */
    boolean actualizarUbicacion(Long id, Integer deptoId, Integer provId, Integer distId,
                                String direccion, String referencia)
            throws ValidationException, DatabaseException;

    /**
     * Valida los datos personales de un usuario
     * @param dto DTO con los datos a validar
     * @throws ValidationException si algún dato no es válido, con mensaje descriptivo
     */
    void validarDatosPersonales(UsuarioPersonaDTO dto) throws ValidationException;
}
