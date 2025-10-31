package com.contactoprofesionales.service.profesional;

import com.contactoprofesionales.model.Profesional;
import com.contactoprofesionales.dto.ProfesionalDTO;
import com.contactoprofesionales.dao.profesional.ProfesionalDAO;
import com.contactoprofesionales.dao.profesional.ProfesionalDAOImpl;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ProfesionalException;
import com.contactoprofesionales.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar profesionales.
 * Aplicación de SRP: Solo lógica de negocio de profesionales.
 */
public class ProfesionalService {
    
    private static final Logger logger = LoggerFactory.getLogger(ProfesionalService.class);
    private final ProfesionalDAO profesionalDAO;
    
    public ProfesionalService() {
        this.profesionalDAO = new ProfesionalDAOImpl();
    }
    
    // Constructor para testing
    public ProfesionalService(ProfesionalDAO profesionalDAO) {
        this.profesionalDAO = profesionalDAO;
    }
    
    /**
     * Obtiene un profesional por ID.
     * @throws ValidationException 
     */
    public Profesional obtenerProfesional(Integer id) 
            throws ProfesionalException, DatabaseException, ValidationException {
        
        logger.debug("Obteniendo profesional ID: {}", id);
        
        if (id == null || id <= 0) {
            throw new ValidationException("ID de profesional inválido");
        }
        
        Profesional profesional = profesionalDAO.buscarPorId(id);
        
        if (profesional == null) {
            throw new ProfesionalException("Profesional no encontrado con ID: " + id);
        }
        
        return profesional;
    }
    
    /**
     * Obtiene un profesional por usuario ID.
     * @throws ValidationException 
     */
    public Profesional obtenerProfesionalPorUsuario(Integer usuarioId) 
            throws ProfesionalException, DatabaseException, ValidationException {
        
        logger.debug("Obteniendo profesional para usuario: {}", usuarioId);
        
        if (usuarioId == null || usuarioId <= 0) {
            throw new ValidationException("ID de usuario inválido");
        }
        
        Profesional profesional = profesionalDAO.buscarPorUsuarioId(usuarioId);
        
        if (profesional == null) {
            throw new ProfesionalException("No existe perfil de profesional para este usuario");
        }
        
        return profesional;
    }
    
    /**
     * Lista todos los profesionales activos.
     */
    public List<Profesional> listarProfesionales() throws DatabaseException {
        logger.debug("Listando todos los profesionales");
        return profesionalDAO.listarTodos();
    }
    
    /**
     * Busca profesionales por especialidad.
     */
    public List<Profesional> buscarPorEspecialidad(String especialidad) 
            throws DatabaseException, ValidationException {
        
        if (especialidad == null || especialidad.trim().isEmpty()) {
            throw new ValidationException("La especialidad es requerida");
        }
        
        logger.debug("Buscando profesionales por especialidad: {}", especialidad);
        return profesionalDAO.buscarPorEspecialidad(especialidad);
    }
    
    /**
     * Busca profesionales por distrito.
     */
    public List<Profesional> buscarPorDistrito(String distrito) 
            throws DatabaseException, ValidationException {
        
        if (distrito == null || distrito.trim().isEmpty()) {
            throw new ValidationException("El distrito es requerido");
        }
        
        logger.debug("Buscando profesionales por distrito: {}", distrito);
        return profesionalDAO.buscarPorDistrito(distrito);
    }
    
    /**
     * Busca profesionales con filtros combinados.
     */
    public List<Profesional> buscarConFiltros(String especialidad, String distrito, 
                                              Double calificacionMin) 
            throws DatabaseException {
        
        logger.debug("Buscando profesionales con filtros");
        return profesionalDAO.buscarConFiltros(especialidad, distrito, calificacionMin);
    }
    
    /**
     * Crea un nuevo perfil de profesional.
     */
    public Profesional crearProfesional(Profesional profesional) 
            throws ValidationException, DatabaseException, ProfesionalException {
        
        logger.info("Creando perfil de profesional para usuario: {}", profesional.getUsuarioId());
        
        // Validar datos
        validarDatosProfesional(profesional);
        
        // Verificar que no exista ya un perfil para este usuario
        if (profesionalDAO.existePorUsuarioId(profesional.getUsuarioId())) {
            throw new ProfesionalException("Ya existe un perfil de profesional para este usuario");
        }
        
        // Crear profesional
        Profesional nuevoProfesional = profesionalDAO.crear(profesional);
        
        logger.info("✓ Perfil de profesional creado con ID: {}", nuevoProfesional.getId());
        
        return nuevoProfesional;
    }
    
    /**
     * Actualiza un perfil de profesional.
     */
    public boolean actualizarProfesional(Profesional profesional) 
            throws ValidationException, DatabaseException, ProfesionalException {
        
        logger.info("Actualizando profesional ID: {}", profesional.getId());
        
        // Validar datos
        validarDatosProfesional(profesional);
        
        // Verificar que el profesional existe
        Profesional existente = profesionalDAO.buscarPorId(profesional.getId());
        if (existente == null) {
            throw new ProfesionalException("Profesional no encontrado");
        }
        
        // Actualizar
        boolean actualizado = profesionalDAO.actualizar(profesional);
        
        if (actualizado) {
            logger.info("✓ Profesional actualizado: {}", profesional.getId());
        }
        
        return actualizado;
    }
    
    /**
     * Elimina (inactiva) un profesional.
     */
    public boolean eliminarProfesional(Integer id) 
            throws DatabaseException, ProfesionalException {
        
        logger.info("Eliminando profesional ID: {}", id);
        
        // Verificar que existe
        Profesional profesional = profesionalDAO.buscarPorId(id);
        if (profesional == null) {
            throw new ProfesionalException("Profesional no encontrado");
        }
        
        boolean eliminado = profesionalDAO.eliminar(id);
        
        if (eliminado) {
            logger.info("✓ Profesional eliminado: {}", id);
        }
        
        return eliminado;
    }
    
    /**
     * Actualiza la disponibilidad del profesional.
     */
    public boolean actualizarDisponibilidad(Integer id, boolean disponible) 
            throws DatabaseException, ProfesionalException {
        
        logger.info("Actualizando disponibilidad profesional {} a: {}", id, disponible);
        
        // Verificar que existe
        Profesional profesional = profesionalDAO.buscarPorId(id);
        if (profesional == null) {
            throw new ProfesionalException("Profesional no encontrado");
        }
        
        return profesionalDAO.actualizarDisponibilidad(id, disponible);
    }
    
    /**
     * Actualiza la calificación del profesional.
     */
    public boolean actualizarCalificacion(Integer id, Double nuevaCalificacion) 
            throws ValidationException, DatabaseException, ProfesionalException {
        
        logger.info("Actualizando calificación profesional {} con: {}", id, nuevaCalificacion);
        
        // Validar calificación
        if (nuevaCalificacion == null || nuevaCalificacion < 1 || nuevaCalificacion > 5) {
            throw new ValidationException("La calificación debe estar entre 1 y 5");
        }
        
        // Verificar que existe
        Profesional profesional = profesionalDAO.buscarPorId(id);
        if (profesional == null) {
            throw new ProfesionalException("Profesional no encontrado");
        }
        
        return profesionalDAO.actualizarCalificacion(id, nuevaCalificacion);
    }
    
    /**
     * Convierte un Profesional a ProfesionalDTO.
     */
    public ProfesionalDTO convertirADTO(Profesional profesional) {
        if (profesional == null) {
            return null;
        }
        
        ProfesionalDTO dto = new ProfesionalDTO();
        dto.setId(profesional.getId());
        dto.setNombreCompleto(profesional.getNombreCompleto());
        dto.setEmail(profesional.getEmail());
        dto.setTelefono(profesional.getTelefono());
        dto.setEspecialidad(profesional.getEspecialidad());
        dto.setDescripcion(profesional.getDescripcion());
        dto.setExperiencia(profesional.getExperiencia());
        dto.setHabilidades(profesional.getHabilidades());
        dto.setCertificaciones(profesional.getCertificaciones());
        dto.setFotoPerfil(profesional.getFotoPerfil());
        dto.setFotoPortada(profesional.getFotoPortada());
        dto.setPortafolio(profesional.getPortafolio());
        dto.setTarifaHora(profesional.getTarifaHora());
        dto.setCalificacionPromedio(profesional.getCalificacionPromedio());
        dto.setTotalResenas(profesional.getTotalResenas());
        dto.setUbicacion(profesional.getUbicacion());
        dto.setDistrito(profesional.getDistrito());
        dto.setRadioServicio(profesional.getRadioServicio());
        dto.setDisponibilidad(profesional.getDisponibilidad());
        dto.setVerificado(profesional.isVerificado());
        dto.setDisponible(profesional.isDisponible());
        dto.setFechaRegistro(profesional.getFechaRegistro());
        
        return dto;
    }
    
    /**
     * Convierte una lista de Profesional a lista de ProfesionalDTO.
     */
    public List<ProfesionalDTO> convertirADTOList(List<Profesional> profesionales) {
        if (profesionales == null) {
            return new ArrayList<>();
        }
        
        return profesionales.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Valida los datos del profesional.
     */
    private void validarDatosProfesional(Profesional profesional) throws ValidationException {
        List<String> errores = new ArrayList<>();
        
        if (profesional.getUsuarioId() == null) {
            errores.add("El ID de usuario es requerido");
        }
        
        if (profesional.getEspecialidad() == null || profesional.getEspecialidad().trim().isEmpty()) {
            errores.add("La especialidad es requerida");
        } else if (profesional.getEspecialidad().length() < 3) {
            errores.add("La especialidad debe tener al menos 3 caracteres");
        } else if (profesional.getEspecialidad().length() > 100) {
            errores.add("La especialidad no puede superar los 100 caracteres");
        }
        
        if (profesional.getDescripcion() != null && profesional.getDescripcion().length() > 1000) {
            errores.add("La descripción no puede superar los 1000 caracteres");
        }
        
        if (profesional.getTarifaHora() != null && profesional.getTarifaHora() < 0) {
            errores.add("La tarifa por hora no puede ser negativa");
        }
        
        if (profesional.getDistrito() == null || profesional.getDistrito().trim().isEmpty()) {
            errores.add("El distrito es requerido");
        }
        
        if (profesional.getRadioServicio() != null && profesional.getRadioServicio() <= 0) {
            errores.add("El radio de servicio debe ser mayor a 0");
        }
        
        if (!errores.isEmpty()) {
            throw new ValidationException(String.join(", ", errores));
        }
    }
}
