package com.contactoprofesionales.dao.solicitud;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.exception.DatabaseException;
import java.util.List;

/**
 * Interfaz DAO para operaciones con solicitudes de servicio.
 */
public interface SolicitudServicioDAO {
    
    /**
     * Crea una nueva solicitud de servicio.
     */
    SolicitudServicio crear(SolicitudServicio solicitud) throws DatabaseException;
    
    /**
     * Busca una solicitud por ID.
     */
    SolicitudServicio buscarPorId(Integer id) throws DatabaseException;
    
    /**
     * Lista solicitudes de un cliente.
     */
    List<SolicitudServicio> listarPorCliente(Integer clienteId) throws DatabaseException;
    
    /**
     * Lista solicitudes de un profesional.
     */
    List<SolicitudServicio> listarPorProfesional(Integer profesionalId) throws DatabaseException;
    
    /**
     * Actualiza el estado de una solicitud.
     */
    boolean actualizarEstado(Integer id, String nuevoEstado) throws DatabaseException;
    
    /**
     * Cancela una solicitud.
     */
    boolean cancelar(Integer id, Integer clienteId) throws DatabaseException;
    
    /**
     * Verifica si existe una solicitud pendiente entre cliente y profesional.
     */
    boolean existeSolicitudPendiente(Integer clienteId, Integer profesionalId) throws DatabaseException;
}
