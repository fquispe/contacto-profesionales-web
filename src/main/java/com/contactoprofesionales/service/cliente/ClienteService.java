package com.contactoprofesionales.service.cliente;

import com.contactoprofesionales.dto.ClienteDTO;
import com.contactoprofesionales.dto.ClienteRegistroRequest;
import com.contactoprofesionales.exception.ClienteException;

import java.util.List;

/**
 * Interface del servicio de Cliente con la lógica de negocio
 */
public interface ClienteService {
    
    /**
     * Registra un nuevo cliente en el sistema con validaciones de negocio
     * @param request Datos del cliente a registrar
     * @return ClienteDTO con los datos del cliente registrado
     * @throws ClienteException si hay errores de validación o en el registro
     */
    ClienteDTO registrarCliente(ClienteRegistroRequest request) throws ClienteException;
    
    /**
     * Actualiza el perfil de un cliente existente
     * @param id ID del cliente a actualizar
     * @param request Datos actualizados del cliente
     * @return ClienteDTO con los datos actualizados
     * @throws ClienteException si el cliente no existe o hay errores de validación
     */
    ClienteDTO actualizarPerfil(Long id, ClienteRegistroRequest request) throws ClienteException;
    
    /**
     * Obtiene el perfil completo de un cliente incluyendo sus direcciones
     * @param id ID del cliente
     * @return ClienteDTO con los datos completos
     * @throws ClienteException si el cliente no existe
     */
    ClienteDTO obtenerPerfil(Long id) throws ClienteException;
    
    /**
     * Busca un cliente por su email
     * @param email Email del cliente
     * @return ClienteDTO si existe
     * @throws ClienteException si no existe o hay error
     */
    ClienteDTO buscarPorEmail(String email) throws ClienteException;
    
    /**
     * Lista todos los clientes activos en el sistema
     * @return Lista de ClienteDTO
     * @throws ClienteException si hay error en la consulta
     */
    List<ClienteDTO> listarClientesActivos() throws ClienteException;
    
    /**
     * Desactiva un cliente del sistema (borrado lógico)
     * @param id ID del cliente a desactivar
     * @return true si se desactivó correctamente
     * @throws ClienteException si el cliente no existe o hay error
     */
    boolean desactivarCliente(Long id) throws ClienteException;
    
    /**
     * Activa un cliente previamente desactivado
     * @param id ID del cliente a activar
     * @return true si se activó correctamente
     * @throws ClienteException si el cliente no existe o hay error
     */
    boolean activarCliente(Long id) throws ClienteException;
    
    /**
     * Valida los datos del cliente según reglas de negocio
     * @param request Datos a validar
     * @throws ClienteException si hay errores de validación
     */
    void validarDatosCliente(ClienteRegistroRequest request) throws ClienteException;
}
