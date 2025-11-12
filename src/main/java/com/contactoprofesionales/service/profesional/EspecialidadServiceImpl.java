package com.contactoprofesionales.service.profesional;

import com.contactoprofesionales.dao.categoria.CategoriaServicioDAO;
import com.contactoprofesionales.dao.categoria.CategoriaServicioDAOImpl;
import com.contactoprofesionales.dao.profesional.EspecialidadProfesionalDAO;
import com.contactoprofesionales.dao.profesional.EspecialidadProfesionalDAOImpl;
import com.contactoprofesionales.dto.EspecialidadDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.model.CategoriaServicio;
import com.contactoprofesionales.model.EspecialidadProfesional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de especialidades profesionales.
 * Gestiona la lógica de negocio para especialidades de profesionales.
 */
public class EspecialidadServiceImpl implements EspecialidadService {

    private static final Logger logger = LoggerFactory.getLogger(EspecialidadServiceImpl.class);
    private final EspecialidadProfesionalDAO especialidadDAO;
    private final CategoriaServicioDAO categoriaDAO;

    // Límite máximo de especialidades por profesional
    private static final int MAX_ESPECIALIDADES = 3;

    public EspecialidadServiceImpl() {
        this.especialidadDAO = new EspecialidadProfesionalDAOImpl();
        this.categoriaDAO = new CategoriaServicioDAOImpl();
    }

    @Override
    public EspecialidadDTO agregar(Integer profesionalId, Integer categoriaId, Integer aniosExp,
                                   String desc, Double costo, String tipoCosto, Boolean incluyeMateriales,
                                   Integer orden, Boolean esPrincipal)
            throws ValidationException, DatabaseException {
        logger.debug("Agregando especialidad para profesional ID: {}, categoría ID: {}",
                    profesionalId, categoriaId);

        // Validar parámetros
        validarParametrosAgregar(profesionalId, categoriaId, aniosExp, costo, tipoCosto, orden, esPrincipal);

        try {
            // Validar que no se exceda el límite de especialidades
            validarLimiteEspecialidades(profesionalId);

            // Verificar que la categoría existe
            Optional<CategoriaServicio> categoriaOpt = categoriaDAO.buscarPorId(categoriaId);
            if (!categoriaOpt.isPresent()) {
                throw new ValidationException("La categoría de servicio con ID " + categoriaId + " no existe");
            }

            CategoriaServicio categoria = categoriaOpt.get();
            if (categoria.getActivo() == null || !categoria.getActivo()) {
                throw new ValidationException("La categoría de servicio no está activa");
            }

            // Verificar que no tenga ya esta especialidad
            List<EspecialidadProfesional> especialidadesExistentes =
                    especialidadDAO.listarPorProfesional(profesionalId);

            for (EspecialidadProfesional esp : especialidadesExistentes) {
                if (esp.getCategoriaId().equals(categoriaId)) {
                    throw new ValidationException("El profesional ya tiene esta especialidad registrada");
                }
            }

            // Si se marca como principal, debe ser la única principal
            if (esPrincipal != null && esPrincipal) {
                // Verificar que no haya otra especialidad principal
                for (EspecialidadProfesional esp : especialidadesExistentes) {
                    if (esp.getEsPrincipal() != null && esp.getEsPrincipal()) {
                        throw new ValidationException("El profesional ya tiene una especialidad principal. " +
                                                     "Desmárquela primero antes de agregar otra principal");
                    }
                }
            }

            // Crear especialidad
            EspecialidadProfesional especialidad = new EspecialidadProfesional();
            especialidad.setProfesionalId(profesionalId);
            especialidad.setCategoriaId(categoriaId);
            especialidad.setAniosExperiencia(aniosExp != null ? aniosExp : 0);
            especialidad.setDescripcion(desc != null ? desc.trim() : null);
            especialidad.setCosto(costo);
            especialidad.setTipoCosto(tipoCosto);
            especialidad.setIncluyeMateriales(incluyeMateriales != null ? incluyeMateriales : false);
            especialidad.setOrden(orden != null ? orden : 1);
            especialidad.setEsPrincipal(esPrincipal != null ? esPrincipal : false);

            // Registrar especialidad
            especialidad = especialidadDAO.registrar(especialidad);

            // Cargar información de categoría
            especialidad.setCategoriaNombre(categoria.getNombre());
            especialidad.setCategoriaDescripcion(categoria.getDescripcion());

            logger.info("Especialidad agregada exitosamente con ID: {} para profesional ID: {}",
                       especialidad.getId(), profesionalId);

            return convertirModeloADTO(especialidad);

        } catch (DatabaseException e) {
            logger.error("Error al agregar especialidad: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<EspecialidadDTO> listarPorProfesional(Integer profesionalId)
            throws ValidationException, DatabaseException {
        logger.debug("Listando especialidades para profesional ID: {}", profesionalId);

        // Validar parámetros
        if (profesionalId == null) {
            throw new ValidationException("El ID del profesional es obligatorio");
        }

        if (profesionalId <= 0) {
            throw new ValidationException("El ID del profesional debe ser un número positivo");
        }

        try {
            List<EspecialidadProfesional> especialidades = especialidadDAO.listarPorProfesional(profesionalId);
            List<EspecialidadDTO> especialidadesDTO = new ArrayList<>();

            for (EspecialidadProfesional especialidad : especialidades) {
                especialidadesDTO.add(convertirModeloADTO(especialidad));
            }

            logger.info("Se encontraron {} especialidades para profesional ID: {}",
                       especialidadesDTO.size(), profesionalId);

            return especialidadesDTO;

        } catch (DatabaseException e) {
            logger.error("Error al listar especialidades del profesional {}: {}",
                        profesionalId, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean eliminar(Integer id) throws ValidationException, DatabaseException {
        logger.debug("Eliminando especialidad con ID: {}", id);

        // Validar parámetros
        if (id == null) {
            throw new ValidationException("El ID de la especialidad es obligatorio");
        }

        if (id <= 0) {
            throw new ValidationException("El ID de la especialidad debe ser un número positivo");
        }

        try {
            boolean resultado = especialidadDAO.eliminar(id);

            if (resultado) {
                logger.info("Especialidad eliminada exitosamente con ID: {}", id);
            } else {
                logger.warn("No se pudo eliminar la especialidad con ID: {}", id);
            }

            return resultado;

        } catch (DatabaseException e) {
            logger.error("Error al eliminar especialidad con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean marcarComoPrincipal(Integer id, Integer profesionalId)
            throws ValidationException, DatabaseException {
        logger.debug("Marcando especialidad ID: {} como principal para profesional ID: {}",
                    id, profesionalId);

        // Validar parámetros
        if (id == null || profesionalId == null) {
            throw new ValidationException("El ID de la especialidad y del profesional son obligatorios");
        }

        if (id <= 0 || profesionalId <= 0) {
            throw new ValidationException("Los IDs deben ser números positivos");
        }

        try {
            // Verificar que la especialidad pertenece al profesional
            List<EspecialidadProfesional> especialidades = especialidadDAO.listarPorProfesional(profesionalId);
            boolean especialidadEncontrada = false;

            for (EspecialidadProfesional esp : especialidades) {
                if (esp.getId().equals(id)) {
                    especialidadEncontrada = true;
                    break;
                }
            }

            if (!especialidadEncontrada) {
                throw new ValidationException("La especialidad no pertenece al profesional especificado");
            }

            // Marcar como principal (el DAO se encarga de desmarcar las demás)
            boolean resultado = especialidadDAO.marcarComoPrincipal(id, profesionalId);

            logger.info("Especialidad ID: {} marcada como principal para profesional ID: {}",
                       id, profesionalId);

            return resultado;

        } catch (DatabaseException e) {
            logger.error("Error al marcar especialidad {} como principal: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public void validarLimiteEspecialidades(Integer profesionalId)
            throws ValidationException, DatabaseException {
        logger.debug("Validando límite de especialidades para profesional ID: {}", profesionalId);

        if (profesionalId == null) {
            throw new ValidationException("El ID del profesional es obligatorio");
        }

        if (profesionalId <= 0) {
            throw new ValidationException("El ID del profesional debe ser un número positivo");
        }

        try {
            List<EspecialidadProfesional> especialidades = especialidadDAO.listarPorProfesional(profesionalId);

            if (especialidades.size() >= MAX_ESPECIALIDADES) {
                throw new ValidationException(
                    String.format("El profesional ya tiene el máximo de %d especialidades permitidas. " +
                                 "Debe eliminar una antes de agregar otra", MAX_ESPECIALIDADES));
            }

        } catch (DatabaseException e) {
            logger.error("Error al validar límite de especialidades: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Valida los parámetros para agregar una especialidad
     */
    private void validarParametrosAgregar(Integer profesionalId, Integer categoriaId,
                                          Integer aniosExp, Double costo, String tipoCosto,
                                          Integer orden, Boolean esPrincipal)
            throws ValidationException {
        List<String> errores = new ArrayList<>();

        if (profesionalId == null) {
            errores.add("El ID del profesional es obligatorio");
        } else if (profesionalId <= 0) {
            errores.add("El ID del profesional debe ser un número positivo");
        }

        if (categoriaId == null) {
            errores.add("El ID de la categoría de servicio es obligatorio");
        } else if (categoriaId <= 0) {
            errores.add("El ID de la categoría debe ser un número positivo");
        }

        if (aniosExp != null) {
            if (aniosExp < 0) {
                errores.add("Los años de experiencia no pueden ser negativos");
            } else if (aniosExp > 100) {
                errores.add("Los años de experiencia no pueden ser mayores a 100");
            }
        }

        if (costo != null && costo < 0) {
            errores.add("El costo no puede ser negativo");
        }

        if (tipoCosto != null && !tipoCosto.matches("^(hora|dia|mes)$")) {
            errores.add("El tipo de costo debe ser 'hora', 'dia' o 'mes'");
        }

        if (orden != null) {
            if (orden < 1 || orden > 3) {
                errores.add("El orden debe estar entre 1 y 3");
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidationException(String.join(". ", errores));
        }
    }

    /**
     * Convierte un EspecialidadProfesional (modelo) a EspecialidadDTO
     */
    private EspecialidadDTO convertirModeloADTO(EspecialidadProfesional especialidad) {
        EspecialidadDTO dto = new EspecialidadDTO();

        dto.setId(especialidad.getId());
        dto.setProfesionalId(especialidad.getProfesionalId());
        dto.setCategoriaId(especialidad.getCategoriaId());
        dto.setCategoriaNombre(especialidad.getCategoriaNombre());
        dto.setCategoriaDescripcion(especialidad.getCategoriaDescripcion());
        dto.setEsPrincipal(especialidad.getEsPrincipal());
        dto.setAniosExperiencia(especialidad.getAniosExperiencia());
        dto.setDescripcion(especialidad.getDescripcion());
        dto.setCosto(especialidad.getCosto());
        dto.setTipoCosto(especialidad.getTipoCosto());
        dto.setIncluyeMateriales(especialidad.getIncluyeMateriales());
        dto.setOrden(especialidad.getOrden());

        return dto;
    }
}
