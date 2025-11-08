package com.contactoprofesionales.service.solicitud;

import com.contactoprofesionales.model.SolicitudServicio;
import com.contactoprofesionales.dto.SolicitudServicioRequest;
import com.contactoprofesionales.dao.solicitud.SolicitudServicioDAO;
import com.contactoprofesionales.dao.solicitud.SolicitudServicioDAOImpl;
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
 */
public class SolicitudServicioService {
    
    private static final Logger logger = LoggerFactory.getLogger(SolicitudServicioService.class);
    private final SolicitudServicioDAO solicitudDAO;
    
    public SolicitudServicioService() {
        this.solicitudDAO = new SolicitudServicioDAOImpl();
    }
    
    // Constructor para testing
    public SolicitudServicioService(SolicitudServicioDAO solicitudDAO) {
        this.solicitudDAO = solicitudDAO;
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
        
        // Procesar fotos (en un caso real, guardar en almacenamiento y obtener URLs)
        if (request.getFotosBase64() != null && !request.getFotosBase64().isEmpty()) {
            List<String> fotosUrls = procesarFotos(request.getFotosBase64(), clienteId);
            solicitud.setFotosUrls(fotosUrls);
        }
        
        // Guardar en base de datos
        SolicitudServicio solicitudCreada = solicitudDAO.crear(solicitud);
        
        logger.info("✓ Solicitud creada con ID: {}", solicitudCreada.getId());
        
        // TODO: Enviar notificación al profesional
        // notificacionService.notificarNuevaSolicitud(solicitudCreada);
        
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
            // TODO: Notificar al profesional
            // notificacionService.notificarCancelacion(solicitud);
        }
        
        return cancelada;
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
        
        // Validar dirección
        if (request.getDireccion() == null || request.getDireccion().trim().isEmpty()) {
            errores.add("La dirección es requerida");
        }
        
        if (request.getDistrito() == null || request.getDistrito().trim().isEmpty()) {
            errores.add("El distrito es requerido");
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