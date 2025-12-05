package com.contactoprofesionales.service.notificacion;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del servicio de notificaciones.
 *
 * IMPLEMENTACIÓN ACTUAL (v1.0):
 * - Registro en logs para debugging y auditoría
 * - Preparado para futuras extensiones
 *
 * Creado: 2025-12-03
 *
 * ROADMAP:
 * v2.0 - Integración con servicio de emails
 * v3.0 - Notificaciones push
 * v4.0 - Notificaciones en tiempo real (WebSocket)
 */
public class NotificacionServiceImpl implements NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionServiceImpl.class);

    @Override
    public void notificarNuevaSolicitud(SolicitudServicio solicitud) throws DatabaseException {
        if (solicitud == null) {
            logger.warn("Intento de notificar nueva solicitud con objeto null");
            return;
        }

        logger.info("📧 NOTIFICACIÓN - Nueva solicitud de servicio");
        logger.info("   → Profesional ID: {}", solicitud.getProfesionalId());
        logger.info("   → Cliente ID: {}", solicitud.getClienteId());
        logger.info("   → Solicitud ID: {}", solicitud.getId());
        logger.info("   → Fecha del servicio: {}", solicitud.getFechaServicio());
        logger.info("   → Descripción: {}", solicitud.getDescripcion() != null ?
                    solicitud.getDescripcion().substring(0, Math.min(50, solicitud.getDescripcion().length())) + "..." :
                    "Sin descripción");

        // TODO v2.0: Implementar envío de email al profesional
        // emailService.enviarNotificacionNuevaSolicitud(solicitud);

        // TODO v3.0: Implementar notificación push
        // pushService.enviarNotificacion(solicitud.getProfesionalId(), "Nueva solicitud recibida");
    }

    @Override
    public void notificarCancelacionSolicitud(SolicitudServicio solicitud) throws DatabaseException {
        if (solicitud == null) {
            logger.warn("Intento de notificar cancelación con objeto null");
            return;
        }

        logger.info("🚫 NOTIFICACIÓN - Solicitud cancelada");
        logger.info("   → Profesional ID: {}", solicitud.getProfesionalId());
        logger.info("   → Cliente ID: {}", solicitud.getClienteId());
        logger.info("   → Solicitud ID: {}", solicitud.getId());
        logger.info("   → Estado anterior: {}", solicitud.getEstado());
        logger.info("   → Fecha de cancelación: {}", solicitud.getFechaActualizacion());

        // TODO v2.0: Implementar envío de email al profesional
        // emailService.enviarNotificacionCancelacion(solicitud);
    }

    @Override
    public void notificarAceptacionSolicitud(SolicitudServicio solicitud) throws DatabaseException {
        if (solicitud == null) {
            logger.warn("Intento de notificar aceptación con objeto null");
            return;
        }

        logger.info("✅ NOTIFICACIÓN - Solicitud aceptada");
        logger.info("   → Cliente ID: {}", solicitud.getClienteId());
        logger.info("   → Profesional ID: {}", solicitud.getProfesionalId());
        logger.info("   → Solicitud ID: {}", solicitud.getId());
        logger.info("   → Fecha aceptada: {}", solicitud.getFechaActualizacion());

        // TODO v2.0: Implementar envío de email al cliente
        // emailService.enviarNotificacionAceptacion(solicitud);
    }

    @Override
    public void notificarRechazoSolicitud(SolicitudServicio solicitud) throws DatabaseException {
        if (solicitud == null) {
            logger.warn("Intento de notificar rechazo con objeto null");
            return;
        }

        logger.info("❌ NOTIFICACIÓN - Solicitud rechazada");
        logger.info("   → Cliente ID: {}", solicitud.getClienteId());
        logger.info("   → Profesional ID: {}", solicitud.getProfesionalId());
        logger.info("   → Solicitud ID: {}", solicitud.getId());
        logger.info("   → Fecha rechazada: {}", solicitud.getFechaActualizacion());

        // TODO v2.0: Implementar envío de email al cliente
        // emailService.enviarNotificacionRechazo(solicitud);
    }
}
