package com.contactoprofesionales.service.profesional;

import com.contactoprofesionales.dao.profesional.ProfesionalDAO;
import com.contactoprofesionales.dto.BusquedaCriteriosDTO;
import com.contactoprofesionales.dto.ProfesionalBusquedaDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.Profesional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la búsqueda de profesionales.
 * Contiene la lógica de negocio para realizar búsquedas con diferentes criterios.
 */
public class BusquedaProfesionalesService {
    
    private static final Logger logger = LoggerFactory.getLogger(BusquedaProfesionalesService.class);
    private static final int MAX_DESCRIPCION_CORTA = 150;
    private static final int MAX_HABILIDADES_DESTACADAS = 5;
    
    private final ProfesionalDAO profesionalDAO;
    
    /**
     * Constructor con inyección de dependencias.
     */
    public BusquedaProfesionalesService(ProfesionalDAO profesionalDAO) {
        this.profesionalDAO = profesionalDAO;
    }
    
    /**
     * Busca profesionales según los criterios especificados.
     * 
     * @param criterios Criterios de búsqueda
     * @return Lista de profesionales que cumplen los criterios
     * @throws DatabaseException Si ocurre un error en la base de datos
     */
    public List<ProfesionalBusquedaDTO> buscarProfesionales(BusquedaCriteriosDTO criterios) 
            throws DatabaseException {
        
        logger.info("Iniciando búsqueda de profesionales con criterios: {}", criterios);
        
        // Validar criterios
        validarCriterios(criterios);
        
        List<Profesional> profesionales;
        
        try {
            // Determinar el método de búsqueda según los criterios
            if (!criterios.tieneAlgunFiltro()) {
                // Sin filtros: listar todos
                logger.debug("Búsqueda sin filtros - listando todos los profesionales");
                profesionales = profesionalDAO.listarTodos();
                
            } else if (criterios.getEspecialidad() != null && 
                      criterios.getDistrito() == null && 
                      criterios.getCalificacionMinima() == null) {
                // Solo especialidad
                logger.debug("Búsqueda solo por especialidad: {}", criterios.getEspecialidad());
                profesionales = profesionalDAO.buscarPorEspecialidad(criterios.getEspecialidad());
                
            } else if (criterios.getDistrito() != null && 
                      criterios.getEspecialidad() == null && 
                      criterios.getCalificacionMinima() == null) {
                // Solo distrito
                logger.debug("Búsqueda solo por distrito: {}", criterios.getDistrito());
                profesionales = profesionalDAO.buscarPorDistrito(criterios.getDistrito());
                
            } else {
                // Búsqueda con filtros combinados
                logger.debug("Búsqueda con filtros combinados");
                profesionales = profesionalDAO.buscarConFiltros(
                    criterios.getEspecialidad(),
                    criterios.getDistrito(),
                    criterios.getCalificacionMinima()
                );
            }
            
            // Aplicar filtros adicionales en memoria (que no están en el DAO)
            profesionales = aplicarFiltrosAdicionales(profesionales, criterios);
            
            // Convertir a DTOs optimizados para búsqueda
            List<ProfesionalBusquedaDTO> resultados = convertirADTOsBusqueda(profesionales);
            
            // Aplicar paginación
            resultados = aplicarPaginacion(resultados, criterios);
            
            logger.info("Búsqueda completada. Encontrados {} profesionales", resultados.size());
            
            return resultados;
            
        } catch (DatabaseException e) {
            logger.error("Error al buscar profesionales: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Obtiene el total de profesionales que coinciden con los criterios (sin paginación).
     */
    public int contarResultados(BusquedaCriteriosDTO criterios) throws DatabaseException {
        logger.debug("Contando resultados para criterios: {}", criterios);
        
        List<Profesional> profesionales;
        
        if (!criterios.tieneAlgunFiltro()) {
            profesionales = profesionalDAO.listarTodos();
        } else {
            profesionales = profesionalDAO.buscarConFiltros(
                criterios.getEspecialidad(),
                criterios.getDistrito(),
                criterios.getCalificacionMinima()
            );
        }
        
        profesionales = aplicarFiltrosAdicionales(profesionales, criterios);
        
        return profesionales.size();
    }
    
    /**
     * Obtiene las especialidades disponibles para autocompletar.
     */
    public List<String> obtenerEspecialidadesDisponibles() throws DatabaseException {
        logger.debug("Obteniendo especialidades disponibles");
        
        List<Profesional> todosProfesionales = profesionalDAO.listarTodos();
        
        return todosProfesionales.stream()
                .map(Profesional::getEspecialidad)
                .filter(e -> e != null && !e.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene los distritos disponibles.
     */
    public List<String> obtenerDistritosDisponibles() throws DatabaseException {
        logger.debug("Obteniendo distritos disponibles");
        
        List<Profesional> todosProfesionales = profesionalDAO.listarTodos();
        
        return todosProfesionales.stream()
                .map(Profesional::getDistrito)
                .filter(d -> d != null && !d.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
    
    /**
     * Valida los criterios de búsqueda.
     */
    private void validarCriterios(BusquedaCriteriosDTO criterios) {
        if (criterios == null) {
            throw new IllegalArgumentException("Los criterios de búsqueda no pueden ser nulos");
        }
        
        // Validar calificación mínima
        if (criterios.getCalificacionMinima() != null) {
            double cal = criterios.getCalificacionMinima();
            if (cal < 0 || cal > 5) {
                throw new IllegalArgumentException("La calificación mínima debe estar entre 0 y 5");
            }
        }
        
        // Validar tarifa máxima
        if (criterios.getTarifaMaxima() != null && criterios.getTarifaMaxima() < 0) {
            throw new IllegalArgumentException("La tarifa máxima no puede ser negativa");
        }
        
        // Validar paginación
        if (criterios.getPagina() < 1) {
            throw new IllegalArgumentException("El número de página debe ser mayor a 0");
        }
        
        if (criterios.getElementosPorPagina() < 1 || criterios.getElementosPorPagina() > 50) {
            throw new IllegalArgumentException("Los elementos por página deben estar entre 1 y 50");
        }
        
        logger.debug("Criterios validados correctamente");
    }
    
    /**
     * Aplica filtros adicionales que no están implementados en el DAO.
     */
    private List<Profesional> aplicarFiltrosAdicionales(List<Profesional> profesionales, 
                                                        BusquedaCriteriosDTO criterios) {
        
        // Filtrar por tarifa máxima
        if (criterios.getTarifaMaxima() != null) {
            logger.debug("Aplicando filtro de tarifa máxima: {}", criterios.getTarifaMaxima());
            profesionales = profesionales.stream()
                    .filter(p -> p.getTarifaHora() != null && 
                                p.getTarifaHora() <= criterios.getTarifaMaxima())
                    .collect(Collectors.toList());
        }
        
        // Filtrar por disponibilidad
        if (criterios.getDisponible() != null && criterios.getDisponible()) {
            logger.debug("Aplicando filtro de disponibilidad");
            profesionales = profesionales.stream()
                    .filter(Profesional::isDisponible)
                    .collect(Collectors.toList());
        }
        
        return profesionales;
    }
    
    /**
     * Convierte una lista de Profesionales a DTOs optimizados para búsqueda.
     */
    private List<ProfesionalBusquedaDTO> convertirADTOsBusqueda(List<Profesional> profesionales) {
        logger.debug("Convirtiendo {} profesionales a DTOs de búsqueda", profesionales.size());
        
        return profesionales.stream()
                .map(this::convertirAProfesionalBusquedaDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Convierte un Profesional a un DTO de búsqueda.
     */
    private ProfesionalBusquedaDTO convertirAProfesionalBusquedaDTO(Profesional profesional) {
        ProfesionalBusquedaDTO dto = new ProfesionalBusquedaDTO();
        
        dto.setId(profesional.getId());
        dto.setNombreCompleto(profesional.getNombreCompleto());
        dto.setEspecialidad(profesional.getEspecialidad());
        
        // Descripción corta (primeros 150 caracteres)
        String descripcion = profesional.getDescripcion();
        if (descripcion != null && descripcion.length() > MAX_DESCRIPCION_CORTA) {
            dto.setDescripcionCorta(descripcion.substring(0, MAX_DESCRIPCION_CORTA) + "...");
        } else {
            dto.setDescripcionCorta(descripcion);
        }
        
        dto.setExperiencia(profesional.getExperiencia());
        
        // Habilidades destacadas (máximo 5)
        if (profesional.getHabilidades() != null && !profesional.getHabilidades().isEmpty()) {
            List<String> habilidadesDestacadas = profesional.getHabilidades().stream()
                    .limit(MAX_HABILIDADES_DESTACADAS)
                    .collect(Collectors.toList());
            dto.setHabilidadesDestacadas(habilidadesDestacadas);
        }
        
        dto.setFotoPerfil(profesional.getFotoPerfil());
        dto.setTarifaHora(profesional.getTarifaHora());
        dto.setCalificacionPromedio(profesional.getCalificacionPromedio());
        dto.setTotalResenas(profesional.getTotalResenas());
        dto.setDistrito(profesional.getDistrito());
        dto.setRadioServicio(profesional.getRadioServicio());
        dto.setDisponible(profesional.isDisponible());
        dto.setVerificado(profesional.isVerificado());
        dto.setTelefono(profesional.getTelefono());
        
        return dto;
    }
    
    /**
     * Aplica la paginación a los resultados.
     */
    private List<ProfesionalBusquedaDTO> aplicarPaginacion(List<ProfesionalBusquedaDTO> resultados, 
                                                           BusquedaCriteriosDTO criterios) {
        
        int inicio = criterios.getOffset();
        int fin = Math.min(inicio + criterios.getElementosPorPagina(), resultados.size());
        
        if (inicio >= resultados.size()) {
            logger.warn("Página {} fuera de rango. Total resultados: {}", 
                       criterios.getPagina(), resultados.size());
            return new ArrayList<>();
        }
        
        logger.debug("Aplicando paginación - Página: {}, Elementos por página: {}, Rango: [{}, {})", 
                    criterios.getPagina(), criterios.getElementosPorPagina(), inicio, fin);
        
        return resultados.subList(inicio, fin);
    }
    
    /**
     * Calcula el número total de páginas para los criterios dados.
     */
    public int calcularTotalPaginas(int totalResultados, int elementosPorPagina) {
        return (int) Math.ceil((double) totalResultados / elementosPorPagina);
    }
}