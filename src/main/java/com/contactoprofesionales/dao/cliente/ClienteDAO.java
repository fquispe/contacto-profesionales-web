package com.contactoprofesionales.dao.cliente;

import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.model.Cliente;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para operaciones CRUD de Cliente
 */
public interface ClienteDAO {
    
    /**
     * Registra un nuevo cliente en el sistema
     * @param cliente Cliente a registrar
     * @return Cliente registrado con su ID generado
     * @throws ClienteException si ocurre un error en el registro
     */
    Cliente registrar(Cliente cliente) throws ClienteException;
    
    /**
     * Actualiza la información de un cliente existente
     * @param cliente Cliente con la información actualizada
     * @return Cliente actualizado
     * @throws ClienteException si el cliente no existe o ocurre un error
     */
    Cliente actualizar(Cliente cliente) throws ClienteException;
    
    /**
     * Busca un cliente por su ID
     * @param id ID del cliente
     * @return Optional con el cliente si existe
     * @throws ClienteException si ocurre un error en la consulta
     */
    Optional<Cliente> buscarPorId(Long id) throws ClienteException;
    
    /**
     * Busca un cliente por su email
     * @param email Email del cliente
     * @return Optional con el cliente si existe
     * @throws ClienteException si ocurre un error en la consulta
     */
    Optional<Cliente> buscarPorEmail(String email) throws ClienteException;
    
    /**
     * Busca un cliente por su teléfono
     * @param telefono Teléfono del cliente
     * @return Optional con el cliente si existe
     * @throws ClienteException si ocurre un error en la consulta
     */
    Optional<Cliente> buscarPorTelefono(String telefono) throws ClienteException;
    
    /**
     * Lista todos los clientes activos
     * @return Lista de clientes activos
     * @throws ClienteException si ocurre un error en la consulta
     */
    List<Cliente> listarActivos() throws ClienteException;
    
    /**
     * Desactiva un cliente (borrado lógico)
     * @param id ID del cliente a desactivar
     * @return true si se desactivó correctamente
     * @throws ClienteException si el cliente no existe o ocurre un error
     */
    boolean desactivar(Long id) throws ClienteException;
    
    /**
     * Activa un cliente previamente desactivado
     * @param id ID del cliente a activar
     * @return true si se activó correctamente
     * @throws ClienteException si el cliente no existe o ocurre un error
     */
    boolean activar(Long id) throws ClienteException;
    
    /**
     * Verifica si existe un cliente con el email especificado
     * @param email Email a verificar
     * @return true si existe un cliente con ese email
     * @throws ClienteException si ocurre un error en la consulta
     */
    boolean existeEmail(String email) throws ClienteException;
    
    /**
     * Verifica si existe un cliente con el teléfono especificado
     * @param telefono Teléfono a verificar
     * @return true si existe un cliente con ese teléfono
     * @throws ClienteException si ocurre un error en la consulta
     */
    boolean existeTelefono(String telefono) throws ClienteException;
}
