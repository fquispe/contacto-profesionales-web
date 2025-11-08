package com.contactoprofesionales.dao.cliente;

import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.model.DireccionCliente;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para operaciones CRUD de DireccionCliente
 */
public interface DireccionClienteDAO {
    
    /**
     * Crea una nueva dirección para un cliente
     * @param direccion Dirección a crear
     * @return Dirección creada con su ID generado
     * @throws ClienteException si ocurre un error o se excede el límite de direcciones
     */
    DireccionCliente crear(DireccionCliente direccion) throws ClienteException;
    
    /**
     * Actualiza una dirección existente
     * @param direccion Dirección con la información actualizada
     * @return Dirección actualizada
     * @throws ClienteException si la dirección no existe o ocurre un error
     */
    DireccionCliente actualizar(DireccionCliente direccion) throws ClienteException;
    
    /**
     * Busca una dirección por su ID
     * @param id ID de la dirección
     * @return Optional con la dirección si existe
     * @throws ClienteException si ocurre un error en la consulta
     */
    Optional<DireccionCliente> buscarPorId(Long id) throws ClienteException;
    
    /**
     * Lista todas las direcciones activas de un cliente
     * @param clienteId ID del cliente
     * @return Lista de direcciones del cliente
     * @throws ClienteException si ocurre un error en la consulta
     */
    List<DireccionCliente> listarPorCliente(Long clienteId) throws ClienteException;
    
    /**
     * Busca la dirección principal de un cliente
     * @param clienteId ID del cliente
     * @return Optional con la dirección principal si existe
     * @throws ClienteException si ocurre un error en la consulta
     */
    Optional<DireccionCliente> buscarPrincipal(Long clienteId) throws ClienteException;
    
    /**
     * Elimina una dirección (borrado lógico)
     * @param id ID de la dirección a eliminar
     * @return true si se eliminó correctamente
     * @throws ClienteException si la dirección no existe o ocurre un error
     */
    boolean eliminar(Long id) throws ClienteException;
    
    /**
     * Elimina todas las direcciones de un cliente
     * @param clienteId ID del cliente
     * @return Número de direcciones eliminadas
     * @throws ClienteException si ocurre un error
     */
    int eliminarPorCliente(Long clienteId) throws ClienteException;
    
    /**
     * Cuenta las direcciones activas de un cliente
     * @param clienteId ID del cliente
     * @return Cantidad de direcciones activas
     * @throws ClienteException si ocurre un error en la consulta
     */
    int contarPorCliente(Long clienteId) throws ClienteException;
    
    /**
     * Establece una dirección como principal y desmarca las demás
     * @param id ID de la dirección a establecer como principal
     * @param clienteId ID del cliente
     * @return true si se estableció correctamente
     * @throws ClienteException si la dirección no existe o ocurre un error
     */
    boolean establecerComoPrincipal(Long id, Long clienteId) throws ClienteException;
}