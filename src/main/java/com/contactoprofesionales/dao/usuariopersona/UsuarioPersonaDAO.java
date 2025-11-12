package com.contactoprofesionales.dao.usuariopersona;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.UsuarioPersona;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para operaciones CRUD de UsuarioPersona
 */
public interface UsuarioPersonaDAO {

    /**
     * Registra un nuevo usuario persona en el sistema
     * @param usuarioPersona Usuario a registrar
     * @return Usuario registrado con su ID generado
     * @throws DatabaseException si ocurre un error en el registro
     */
    UsuarioPersona registrar(UsuarioPersona usuarioPersona) throws DatabaseException;

    /**
     * Actualiza la información de un usuario persona existente
     * @param usuarioPersona Usuario con la información actualizada
     * @return Usuario actualizado
     * @throws DatabaseException si el usuario no existe o ocurre un error
     */
    UsuarioPersona actualizar(UsuarioPersona usuarioPersona) throws DatabaseException;

    /**
     * Busca un usuario persona por su ID
     * @param id ID del usuario
     * @return Optional con el usuario si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<UsuarioPersona> buscarPorId(Long id) throws DatabaseException;

    /**
     * Busca un usuario persona por su número de documento
     * @param numeroDocumento Número de documento del usuario
     * @return Optional con el usuario si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<UsuarioPersona> buscarPorNumeroDocumento(String numeroDocumento) throws DatabaseException;

    /**
     * Busca un usuario persona por su teléfono
     * @param telefono Teléfono del usuario
     * @return Optional con el usuario si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<UsuarioPersona> buscarPorTelefono(String telefono) throws DatabaseException;

    /**
     * Lista todos los usuarios activos
     * @return Lista de usuarios activos
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<UsuarioPersona> listarActivos() throws DatabaseException;

    /**
     * Lista usuarios por tipo de rol
     * @param tipoRol Tipo de rol (CLIENTE, PROFESIONAL, AMBOS)
     * @return Lista de usuarios del tipo especificado
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<UsuarioPersona> listarPorTipoRol(String tipoRol) throws DatabaseException;

    /**
     * Desactiva un usuario (borrado lógico)
     * @param id ID del usuario a desactivar
     * @return true si se desactivó correctamente
     * @throws DatabaseException si el usuario no existe o ocurre un error
     */
    boolean desactivar(Long id) throws DatabaseException;

    /**
     * Activa un usuario previamente desactivado
     * @param id ID del usuario a activar
     * @return true si se activó correctamente
     * @throws DatabaseException si el usuario no existe o ocurre un error
     */
    boolean activar(Long id) throws DatabaseException;

    /**
     * Actualiza el tipo de rol de un usuario
     * @param id ID del usuario
     * @param tipoRol Nuevo tipo de rol
     * @param esCliente Si es cliente
     * @param esProfesional Si es profesional
     * @return true si se actualizó correctamente
     * @throws DatabaseException si ocurre un error
     */
    boolean actualizarTipoRol(Long id, String tipoRol, Boolean esCliente, Boolean esProfesional) throws DatabaseException;

    /**
     * Actualiza la ubicación de un usuario
     * @param id ID del usuario
     * @param departamentoId ID del departamento
     * @param provinciaId ID de la provincia
     * @param distritoId ID del distrito
     * @param direccion Dirección completa
     * @param referencia Referencia de la dirección
     * @return true si se actualizó correctamente
     * @throws DatabaseException si ocurre un error
     */
    boolean actualizarUbicacion(Long id, Integer departamentoId, Integer provinciaId,
                               Integer distritoId, String direccion, String referencia) throws DatabaseException;

    /**
     * Verifica si existe un usuario con el número de documento especificado
     * @param numeroDocumento Número de documento a verificar
     * @return true si existe un usuario con ese documento
     * @throws DatabaseException si ocurre un error en la consulta
     */
    boolean existeNumeroDocumento(String numeroDocumento) throws DatabaseException;

    /**
     * Verifica si existe un usuario con el teléfono especificado
     * @param telefono Teléfono a verificar
     * @return true si existe un usuario con ese teléfono
     * @throws DatabaseException si ocurre un error en la consulta
     */
    boolean existeTelefono(String telefono) throws DatabaseException;
}
