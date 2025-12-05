package com.contactoprofesionales.dao.solicitud;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.dto.SolicitudDetalleDTO;
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

    /**
     * Cuenta el número de solicitudes pendientes para un profesional.
     * Utilizado para mostrar badge de alertas en el dashboard del profesional.
     *
     * @param profesionalId ID del profesional
     * @return Número de solicitudes con estado 'pendiente' y activo=true
     * @throws DatabaseException Si ocurre un error al consultar la BD
     */
    int contarPendientesPorProfesional(Integer profesionalId) throws DatabaseException;

    /**
     * Busca una solicitud por ID con información completa del cliente y ubicación.
     * Incluye: datos del cliente (nombre, email, teléfono) y nombres de ubicación (departamento, provincia, distrito).
     *
     * @param id ID de la solicitud
     * @return SolicitudDetalleDTO con información completa, o null si no existe
     * @throws DatabaseException Si ocurre un error al consultar la BD
     */
    SolicitudDetalleDTO buscarPorIdConDetalle(Integer id) throws DatabaseException;
}
