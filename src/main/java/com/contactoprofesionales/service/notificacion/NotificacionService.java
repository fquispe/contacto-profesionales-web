package com.contactoprofesionales.service.notificacion;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.exception.DatabaseException;

/**
 * Servicio para gestionar notificaciones a usuarios.
 *
 * Este servicio se encarga de enviar notificaciones a profesionales y clientes
 * sobre eventos importantes del sistema (nuevas solicitudes, cancelaciones, etc.).
 *
 * Creado: 2025-12-03
 *
 * IMPLEMENTACIÓN ACTUAL:
 * - Registro en logs (para desarrollo y debugging)
 *
 * FUTURAS EXTENSIONES:
 * - Envío de emails
 * - Notificaciones push
 * - SMS
 * - Notificaciones en tiempo real (WebSocket)
 */
public interface NotificacionService {

    /**
     * Notifica al profesional sobre una nueva solicitud de servicio.
     *
     * @param solicitud La solicitud de servicio creada
     * @throws DatabaseException si ocurre un error al obtener datos del profesional
     */
    void notificarNuevaSolicitud(SolicitudServicio solicitud) throws DatabaseException;

    /**
     * Notifica al profesional sobre la cancelación de una solicitud.
     *
     * @param solicitud La solicitud de servicio cancelada
     * @throws DatabaseException si ocurre un error al obtener datos del profesional
     */
    void notificarCancelacionSolicitud(SolicitudServicio solicitud) throws DatabaseException;

    /**
     * Notifica al cliente sobre la aceptación de su solicitud.
     *
     * @param solicitud La solicitud de servicio aceptada
     * @throws DatabaseException si ocurre un error al obtener datos del cliente
     */
    void notificarAceptacionSolicitud(SolicitudServicio solicitud) throws DatabaseException;

    /**
     * Notifica al cliente sobre el rechazo de su solicitud.
     *
     * @param solicitud La solicitud de servicio rechazada
     * @throws DatabaseException si ocurre un error al obtener datos del cliente
     */
    void notificarRechazoSolicitud(SolicitudServicio solicitud) throws DatabaseException;
}
