package com.contactoprofesionales.service.solicitud;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.dto.SolicitudServicioRequest;
import com.contactoprofesionales.dto.SolicitudDetalleDTO;
import com.contactoprofesionales.dao.solicitud.SolicitudServicioDAO;
import com.contactoprofesionales.dao.solicitud.SolicitudServicioDAOImpl;
import com.contactoprofesionales.service.notificacion.NotificacionService;
import com.contactoprofesionales.service.notificacion.NotificacionServiceImpl;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio para gestionar solicitudes de servicio.
 * Aplicación de SRP: Solo lógica de negocio de solicitudes.
 *
 * Actualizado: 2025-12-03
 * - Integrado servicio de notificaciones
 */
public class SolicitudServicioService {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudServicioService.class);
    private final SolicitudServicioDAO solicitudDAO;
    private final NotificacionService notificacionService;

    public SolicitudServicioService() {
        this.solicitudDAO = new SolicitudServicioDAOImpl();
        this.notificacionService = new NotificacionServiceImpl();
    }

    // Constructor para testing
    public SolicitudServicioService(SolicitudServicioDAO solicitudDAO) {
        this.solicitudDAO = solicitudDAO;
        this.notificacionService = new NotificacionServiceImpl();
    }

    // Constructor para testing con notificaciones
    public SolicitudServicioService(SolicitudServicioDAO solicitudDAO, NotificacionService notificacionService) {
        this.solicitudDAO = solicitudDAO;
        this.notificacionService = notificacionService;
    }
    
    /**
     * Crea una nueva solicitud de servicio.
     */
    public SolicitudServicio crearSolicitud(Integer clienteId, SolicitudServicioRequest request) 
            throws ValidationException, DatabaseException {
        
        logger.info("Creando solicitud de servicio para cliente: {}", clienteId);
        
        // Validar datos
        validarSolicitud(request);
        
        logger.info("Solicitud de servicio para cliente Validada: {}", clienteId);
        
        // Verificar si ya existe una solicitud pendiente
        if (solicitudDAO.existeSolicitudPendiente(clienteId, request.getProfesionalId())) {
            throw new ValidationException(
                "Ya tienes una solicitud pendiente con este profesional. " +
                "Espera su respuesta antes de enviar una nueva."
            );
        }
        
        // Crear objeto SolicitudServicio
        SolicitudServicio solicitud = new SolicitudServicio();
        solicitud.setClienteId(clienteId);
        solicitud.setProfesionalId(request.getProfesionalId());
        solicitud.setDescripcion(request.getDescripcion());
        solicitud.setPresupuestoEstimado(request.getPresupuestoEstimado());
        solicitud.setDireccion(request.getDireccion());
        solicitud.setDistrito(request.getDistrito());
        solicitud.setCodigoPostal(request.getCodigoPostal());
        solicitud.setReferencia(request.getReferencia());
        
        logger.info("Creando objeto de solicitud de servicio para cliente: {}", clienteId);
        
        // ✅ Parsear fecha y hora desde ISO DateTime string
        LocalDateTime fechaServicio = LocalDateTime.parse(request.getFechaServicio());
        solicitud.setFechaServicio(fechaServicio);

        logger.info("✅ Fecha servicio parseada: {}", fechaServicio);
                
        solicitud.setFechaServicio(fechaServicio);
        
        solicitud.setUrgencia(request.getUrgencia() != null ? request.getUrgencia() : "normal");
        solicitud.setNotasAdicionales(request.getNotasAdicionales());

        // NUEVOS CAMPOS - Migración V008: Ubicación estructurada y modalidad
        solicitud.setDepartamentoId(request.getDepartamentoId());
        solicitud.setProvinciaId(request.getProvinciaId());
        solicitud.setDistritoId(request.getDistritoId());
        solicitud.setTipoPrestacion(request.getTipoPrestacion());
        solicitud.setEspecialidadId(request.getEspecialidadId());

        logger.info("✅ Modalidad: {}, Ubicación: Depto={}, Prov={}, Dist={}",
                   request.getTipoPrestacion(),
                   request.getDepartamentoId(),
                   request.getProvinciaId(),
                   request.getDistritoId());

        // Procesar fotos (en un caso real, guardar en almacenamiento y obtener URLs)
        if (request.getFotosBase64() != null && !request.getFotosBase64().isEmpty()) {
            List<String> fotosUrls = procesarFotos(request.getFotosBase64(), clienteId);
            solicitud.setFotosUrls(fotosUrls);
        }

        // Guardar en base de datos
        SolicitudServicio solicitudCreada = solicitudDAO.crear(solicitud);

        logger.info("✓ Solicitud creada con ID: {}", solicitudCreada.getId());

        // Enviar notificación al profesional
        try {
            notificacionService.notificarNuevaSolicitud(solicitudCreada);
        } catch (Exception e) {
            logger.error("Error al enviar notificación de nueva solicitud: {}", e.getMessage());
            // No lanzar excepción, la notificación es secundaria al proceso principal
        }

        return solicitudCreada;
    }
    
    /**
     * Obtiene una solicitud por ID.
     */
    public SolicitudServicio obtenerSolicitud(Integer id, Integer usuarioId)
            throws DatabaseException, ValidationException {

        SolicitudServicio solicitud = solicitudDAO.buscarPorId(id);

        if (solicitud == null) {
            throw new ValidationException("Solicitud no encontrada");
        }

        // Verificar que el usuario tenga acceso a esta solicitud
        if (!solicitud.getClienteId().equals(usuarioId) &&
            !solicitud.getProfesionalId().equals(usuarioId)) {
            throw new ValidationException("No tienes permiso para ver esta solicitud");
        }

        return solicitud;
    }

    /**
     * Obtiene una solicitud con detalle completo (información del cliente, ubicación e imágenes).
     * Usado en la vista de detalle para profesionales.
     *
     * @param id ID de la solicitud
     * @param usuarioId ID del usuario que consulta
     * @return SolicitudDetalleDTO con información completa
     * @throws DatabaseException Si hay error al consultar la BD
     * @throws ValidationException Si la solicitud no existe o el usuario no tiene permiso
     *
     * CREADO 2025-12-04: Para mostrar información completa en detalle-trabajo.html
     */
    public SolicitudDetalleDTO obtenerSolicitudConDetalle(Integer id, Integer usuarioId)
            throws DatabaseException, ValidationException {

        logger.info("Obteniendo solicitud {} con detalle completo para usuario {}", id, usuarioId);

        // Obtener solicitud con detalle desde el DAO
        SolicitudDetalleDTO solicitud = solicitudDAO.buscarPorIdConDetalle(id);

        if (solicitud == null) {
            logger.warn("Solicitud {} no encontrada", id);
            throw new ValidationException("Solicitud no encontrada");
        }

        // Verificar que el usuario tenga acceso a esta solicitud
        if (!solicitud.getClienteId().equals(usuarioId) &&
            !solicitud.getProfesionalId().equals(usuarioId)) {
            logger.warn("Usuario {} no tiene permiso para ver solicitud {}", usuarioId, id);
            throw new ValidationException("No tienes permiso para ver esta solicitud");
        }

        logger.info("✅ Solicitud {} con detalle completo encontrada y autorizada", id);
        return solicitud;
    }

    /**
     * Lista solicitudes de un cliente.
     */
    public List<SolicitudServicio> listarSolicitudesCliente(Integer clienteId) 
            throws DatabaseException {
        return solicitudDAO.listarPorCliente(clienteId);
    }
    
    /**
     * Lista solicitudes de un profesional.
     */
    public List<SolicitudServicio> listarSolicitudesProfesional(Integer profesionalId)
            throws DatabaseException {
        return solicitudDAO.listarPorProfesional(profesionalId);
    }

    /**
     * Cuenta el número de solicitudes pendientes para un profesional.
     * Utilizado para mostrar badge de alertas en el dashboard.
     *
     * ACTUALIZADO 2025-12-03: Agregado para sistema de alertas del profesional
     *
     * @param profesionalId ID del profesional
     * @return Número de solicitudes con estado 'pendiente'
     * @throws DatabaseException Si ocurre un error al consultar la BD
     */
    public int contarSolicitudesPendientes(Integer profesionalId) throws DatabaseException {
        logger.debug("📊 Contando solicitudes pendientes para profesional {}", profesionalId);
        int count = solicitudDAO.contarPendientesPorProfesional(profesionalId);
        logger.info("📊 Profesional {} tiene {} solicitudes pendientes", profesionalId, count);
        return count;
    }
    
    /**
     * Cancela una solicitud.
     */
    public boolean cancelarSolicitud(Integer solicitudId, Integer clienteId) 
            throws DatabaseException, ValidationException {
        
        // Verificar que la solicitud existe y pertenece al cliente
        SolicitudServicio solicitud = solicitudDAO.buscarPorId(solicitudId);
        
        if (solicitud == null) {
            throw new ValidationException("Solicitud no encontrada");
        }
        
        if (!solicitud.getClienteId().equals(clienteId)) {
            throw new ValidationException("No tienes permiso para cancelar esta solicitud");
        }
        
        if (!solicitud.puedeSerCancelada()) {
            throw new ValidationException(
                "Solo se pueden cancelar solicitudes en estado 'Pendiente' o 'Aceptada'"
            );
        }
        
        boolean cancelada = solicitudDAO.cancelar(solicitudId, clienteId);

        if (cancelada) {
            logger.info("✓ Solicitud cancelada: {}", solicitudId);
            // Notificar al profesional
            try {
                notificacionService.notificarCancelacionSolicitud(solicitud);
            } catch (Exception e) {
                logger.error("Error al enviar notificación de cancelación: {}", e.getMessage());
                // No lanzar excepción, la notificación es secundaria al proceso principal
            }
        }

        return cancelada;
    }

    /**
     * Actualiza el estado de una solicitud (solo para profesionales).
     * Valida que la transición de estado sea permitida según las reglas de negocio.
     *
     * REGLAS DE TRANSICIÓN:
     * - PENDIENTE → ACEPTADA o RECHAZADA
     * - ACEPTADA → COMPLETADA o CANCELADA
     * - Estados finales (RECHAZADA, COMPLETADA, CANCELADA) no permiten cambios
     *
     * ACTUALIZADO 2025-12-03: Agregado para dashboard del profesional
     *
     * @param solicitudId ID de la solicitud a actualizar
     * @param profesionalId ID del profesional (debe ser el dueño de la solicitud)
     * @param nuevoEstado Nuevo estado a aplicar
     * @return true si se actualizó correctamente, false en caso contrario
     * @throws DatabaseException Si ocurre error en BD
     * @throws ValidationException Si la transición no es válida o el usuario no tiene permiso
     */
    public boolean actualizarEstadoSolicitud(Integer solicitudId, Integer profesionalId, String nuevoEstado)
            throws DatabaseException, ValidationException {

        logger.info("🔄 Actualizando estado de solicitud {} a '{}'", solicitudId, nuevoEstado);

        // ✅ PASO 1: Obtener solicitud actual
        SolicitudServicio solicitud = solicitudDAO.buscarPorId(solicitudId);

        if (solicitud == null) {
            logger.warn("⚠️ Solicitud {} no encontrada", solicitudId);
            throw new ValidationException("Solicitud no encontrada");
        }

        logger.debug("📄 Solicitud encontrada: estado actual = '{}'", solicitud.getEstado());

        // ✅ PASO 2: Validar que el profesional es el dueño de la solicitud
        if (!solicitud.getProfesionalId().equals(profesionalId)) {
            logger.warn("⚠️ Profesional {} no tiene permiso sobre solicitud {} (pertenece a profesional {})",
                       profesionalId, solicitudId, solicitud.getProfesionalId());
            throw new ValidationException("No tienes permiso para modificar esta solicitud");
        }

        logger.debug("✅ Profesional {} validado como dueño de la solicitud", profesionalId);

        // ✅ PASO 3: Normalizar estado a lowercase para validación
        String nuevoEstadoNormalizado = nuevoEstado.toLowerCase();

        // ✅ PASO 4: Validar transición de estado usando método del modelo
        if (!solicitud.puedeTransicionarA(nuevoEstadoNormalizado)) {
            logger.warn("⚠️ Transición de estado no permitida: {} → {}",
                       solicitud.getEstado(), nuevoEstado);
            List<String> estadosPermitidos = solicitud.getEstadosDisponibles();
            throw new ValidationException(
                String.format("No se puede cambiar de '%s' a '%s'. Estados permitidos: %s",
                             solicitud.getEstado(), nuevoEstado,
                             String.join(", ", estadosPermitidos))
            );
        }

        logger.info("✅ Transición de estado validada: {} → {}", solicitud.getEstado(), nuevoEstado);

        // ✅ PASO 5: Actualizar estado en BD
        boolean actualizado = solicitudDAO.actualizarEstado(solicitudId, nuevoEstadoNormalizado);

        if (!actualizado) {
            logger.error("❌ No se pudo actualizar el estado en BD");
            throw new DatabaseException("Error al actualizar el estado de la solicitud");
        }

        logger.info("✅ Estado actualizado correctamente en BD: {} → {}", solicitud.getEstado(), nuevoEstado);

        // ✅ PASO 6: Enviar notificaciones según el nuevo estado
        try {
            switch (nuevoEstadoNormalizado) {
                case "aceptada":
                    logger.debug("📧 Enviando notificación de aceptación");
                    notificacionService.notificarAceptacionSolicitud(solicitud);
                    break;

                case "rechazada":
                    logger.debug("📧 Enviando notificación de rechazo");
                    notificacionService.notificarRechazoSolicitud(solicitud);
                    break;

                case "completada":
                    logger.debug("📧 Notificación de completado (pendiente de implementar)");
                    // TODO v2.0: notificacionService.notificarCompletadoSolicitud(solicitud);
                    break;

                case "cancelada":
                    logger.debug("📧 Notificación de cancelación ya manejada en cancelarSolicitud()");
                    break;

                default:
                    logger.debug("ℹ️ Estado '{}' no requiere notificación especial", nuevoEstadoNormalizado);
            }
        } catch (Exception e) {
            logger.error("❌ Error al enviar notificación: {}", e.getMessage());
            // No lanzar excepción, la notificación es secundaria al proceso principal
        }

        return true;
    }

    /**
     * Valida los datos de una solicitud.
     */
    private void validarSolicitud(SolicitudServicioRequest request) throws ValidationException {
        List<String> errores = new ArrayList<>();
        
        // Validar profesional
        if (request.getProfesionalId() == null) {
            errores.add("El ID del profesional es requerido");
        }
        
        // Validar descripción
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            errores.add("La descripción es requerida");
        } else if (request.getDescripcion().length() < 20) {
            errores.add("La descripción debe tener al menos 20 caracteres");
        } else if (request.getDescripcion().length() > 1000) {
            errores.add("La descripción no puede superar los 1000 caracteres");
        }
        
        // Validar presupuesto
        if (request.getPresupuestoEstimado() == null) {
            errores.add("El presupuesto estimado es requerido");
        } else if (request.getPresupuestoEstimado() < 20) {
            errores.add("El presupuesto mínimo es S/ 20");
        }

        // ✅ ACTUALIZADO 2025-12-04: Validar dirección solo si NO es trabajo remoto
        // Si es trabajo remoto, no se requiere dirección física
        String tipoPrestacion = request.getTipoPrestacion();
        boolean esRemoto = "remoto".equalsIgnoreCase(tipoPrestacion);

        if (!esRemoto) {
            // Solo validar dirección para trabajo presencial o a domicilio
            if (request.getDireccion() == null || request.getDireccion().trim().isEmpty()) {
                errores.add("La dirección es requerida");
            }

            if (request.getDistrito() == null || request.getDistrito().trim().isEmpty()) {
                errores.add("El distrito es requerido");
            }

            logger.debug("✅ Validación de dirección aplicada (modalidad: {})", tipoPrestacion);
        } else {
            logger.debug("⏭️ Validación de dirección omitida (modalidad remoto)");
        }


        // Validar fecha y hora
        if (request.getFechaServicio() == null || request.getFechaServicio().trim().isEmpty()) {
            errores.add("La fecha del servicio es requerida");
        } else {
            try {
                LocalDateTime fechaHora = LocalDateTime.parse(request.getFechaServicio());
                
                // Validar que no sea en el pasado
                if (fechaHora.isBefore(LocalDateTime.now())) {
                    errores.add("La fecha y hora del servicio no pueden ser anteriores al momento actual");
                }
                
                logger.info("✅ Fecha y hora validadas: {}", fechaHora);
            } catch (Exception e) {
                logger.error("❌ Error al parsear fecha/hora: {}", request.getFechaServicio(), e);
                errores.add("Formato de fecha/hora inválido (debe ser YYYY-MM-DDTHH:mm:ss)");
            }
        }
        
        /*
        // Validar fecha
        if (request.getFechaServicio() == null || request.getFechaServicio().trim().isEmpty()) {
            errores.add("La fecha del servicio es requerida");
        } else {
            try {
                LocalDate fecha = LocalDate.parse(request.getFechaServicio());
                if (fecha.isBefore(LocalDate.now())) {
                    errores.add("La fecha del servicio no puede ser anterior a hoy");
                }
            } catch (Exception e) {
                errores.add("Formato de fecha inválido (debe ser YYYY-MM-DD)");
            }
        }
        
        // Validar hora
        if (request.getHoraServicio() == null || request.getHoraServicio().trim().isEmpty()) {
            errores.add("La hora del servicio es requerida");
        } else {
            try {
                LocalTime.parse(request.getHoraServicio());
            } catch (Exception e) {
                errores.add("Formato de hora inválido (debe ser HH:mm)");
            }
        }
        
        */
        
        // Validar urgencia
        if (request.getUrgencia() != null && 
            !request.getUrgencia().equals("normal") && 
            !request.getUrgencia().equals("urgent")) {
            errores.add("La urgencia debe ser 'normal' o 'urgent'");
        }
        
        // Validar fotos
        if (request.getFotosBase64() != null && request.getFotosBase64().size() > 3) {
            errores.add("Máximo 3 fotos permitidas");
        }
        
        if (!errores.isEmpty()) {
            throw new ValidationException(String.join(", ", errores));
        }
    }
    
   
    /**
     * Procesa fotos en base64 y retorna URLs.
     * En un caso real, guardaría en S3/Azure Blob/filesystem y retornaría URLs reales.
     */
    private List<String> procesarFotos(List<String> fotosBase64, Integer clienteId) {
        List<String> urls = new ArrayList<>();
        
        for (int i = 0; i < fotosBase64.size(); i++) {
            // Simulación: en producción, guardar en almacenamiento y obtener URL
            String url = String.format("/uploads/solicitudes/%d/foto_%d_%d.jpg", 
                                      clienteId, 
                                      System.currentTimeMillis(), 
                                      i);
            urls.add(url);
            
            logger.debug("Foto procesada: {}", url);
        }
        
        return urls;
    }
}