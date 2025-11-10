package com.contactoprofesionales.service.ubicacion;

import com.contactoprofesionales.dao.ubicacion.UbicacionDAO;
import com.contactoprofesionales.dao.ubicacion.UbicacionDAOImpl;
import com.contactoprofesionales.dto.DepartamentoDTO;
import com.contactoprofesionales.dto.DistritoDTO;
import com.contactoprofesionales.dto.ProvinciaDTO;
import com.contactoprofesionales.dto.UbicacionDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.model.Departamento;
import com.contactoprofesionales.model.Distrito;
import com.contactoprofesionales.model.Provincia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación del servicio de ubicación geográfica.
 * Gestiona la lógica de negocio para departamentos, provincias y distritos.
 */
public class UbicacionServiceImpl implements UbicacionService {

    private static final Logger logger = LoggerFactory.getLogger(UbicacionServiceImpl.class);
    private final UbicacionDAO ubicacionDAO;

    public UbicacionServiceImpl() {
        this.ubicacionDAO = new UbicacionDAOImpl();
    }

    @Override
    public List<DepartamentoDTO> listarDepartamentos() throws DatabaseException {
        logger.debug("Listando todos los departamentos");

        try {
            List<Departamento> departamentos = ubicacionDAO.listarDepartamentos();
            List<DepartamentoDTO> departamentosDTO = new ArrayList<>();

            for (Departamento departamento : departamentos) {
                departamentosDTO.add(convertirDepartamentoADTO(departamento));
            }

            logger.info("Se encontraron {} departamentos", departamentosDTO.size());
            return departamentosDTO;

        } catch (DatabaseException e) {
            logger.error("Error al listar departamentos: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<ProvinciaDTO> listarProvinciasPorDepartamento(Integer departamentoId)
            throws ValidationException, DatabaseException {
        logger.debug("Listando provincias para departamento ID: {}", departamentoId);

        // Validar parámetros
        if (departamentoId == null) {
            throw new ValidationException("El ID del departamento es obligatorio");
        }

        if (departamentoId <= 0) {
            throw new ValidationException("El ID del departamento debe ser un número positivo");
        }

        try {
            // Verificar que el departamento existe
            Optional<Departamento> departamento = ubicacionDAO.buscarDepartamentoPorId(departamentoId);
            if (!departamento.isPresent()) {
                throw new ValidationException("El departamento con ID " + departamentoId + " no existe");
            }

            List<Provincia> provincias = ubicacionDAO.listarProvinciasPorDepartamento(departamentoId);
            List<ProvinciaDTO> provinciasDTO = new ArrayList<>();

            for (Provincia provincia : provincias) {
                provinciasDTO.add(convertirProvinciaADTO(provincia));
            }

            logger.info("Se encontraron {} provincias para el departamento ID: {}", provinciasDTO.size(), departamentoId);
            return provinciasDTO;

        } catch (DatabaseException e) {
            logger.error("Error al listar provincias del departamento {}: {}", departamentoId, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<DistritoDTO> listarDistritosPorProvincia(Integer provinciaId)
            throws ValidationException, DatabaseException {
        logger.debug("Listando distritos para provincia ID: {}", provinciaId);

        // Validar parámetros
        if (provinciaId == null) {
            throw new ValidationException("El ID de la provincia es obligatorio");
        }

        if (provinciaId <= 0) {
            throw new ValidationException("El ID de la provincia debe ser un número positivo");
        }

        try {
            // Verificar que la provincia existe
            Optional<Provincia> provincia = ubicacionDAO.buscarProvinciaPorId(provinciaId);
            if (!provincia.isPresent()) {
                throw new ValidationException("La provincia con ID " + provinciaId + " no existe");
            }

            List<Distrito> distritos = ubicacionDAO.listarDistritosPorProvincia(provinciaId);
            List<DistritoDTO> distritosDTO = new ArrayList<>();

            for (Distrito distrito : distritos) {
                distritosDTO.add(convertirDistritoADTO(distrito));
            }

            logger.info("Se encontraron {} distritos para la provincia ID: {}", distritosDTO.size(), provinciaId);
            return distritosDTO;

        } catch (DatabaseException e) {
            logger.error("Error al listar distritos de la provincia {}: {}", provinciaId, e.getMessage());
            throw e;
        }
    }

    @Override
    public List<DistritoDTO> buscarDistritos(String nombre) throws ValidationException, DatabaseException {
        logger.debug("Buscando distritos por nombre: {}", nombre);

        // Validar parámetros
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidationException("El nombre del distrito es obligatorio para la búsqueda");
        }

        if (nombre.trim().length() < 3) {
            throw new ValidationException("El nombre del distrito debe tener al menos 3 caracteres");
        }

        try {
            List<Distrito> distritos = ubicacionDAO.buscarDistritosPorNombre(nombre.trim());
            List<DistritoDTO> distritosDTO = new ArrayList<>();

            for (Distrito distrito : distritos) {
                distritosDTO.add(convertirDistritoADTO(distrito));
            }

            logger.info("Se encontraron {} distritos con el nombre '{}'", distritosDTO.size(), nombre);
            return distritosDTO;

        } catch (DatabaseException e) {
            logger.error("Error al buscar distritos por nombre '{}': {}", nombre, e.getMessage());
            throw e;
        }
    }

    @Override
    public UbicacionDTO obtenerUbicacionCompleta(Integer departamentoId, Integer provinciaId, Integer distritoId)
            throws ValidationException, DatabaseException {
        logger.debug("Obteniendo ubicación completa: Depto={}, Prov={}, Dist={}",
                     departamentoId, provinciaId, distritoId);

        // Validar parámetros
        if (departamentoId == null || provinciaId == null || distritoId == null) {
            throw new ValidationException("Los IDs de departamento, provincia y distrito son obligatorios");
        }

        if (departamentoId <= 0 || provinciaId <= 0 || distritoId <= 0) {
            throw new ValidationException("Todos los IDs deben ser números positivos");
        }

        try {
            // Buscar departamento
            Optional<Departamento> departamentoOpt = ubicacionDAO.buscarDepartamentoPorId(departamentoId);
            if (!departamentoOpt.isPresent()) {
                throw new ValidationException("El departamento con ID " + departamentoId + " no existe");
            }

            // Buscar provincia
            Optional<Provincia> provinciaOpt = ubicacionDAO.buscarProvinciaPorId(provinciaId);
            if (!provinciaOpt.isPresent()) {
                throw new ValidationException("La provincia con ID " + provinciaId + " no existe");
            }

            // Verificar que la provincia pertenezca al departamento
            Provincia provincia = provinciaOpt.get();
            if (!provincia.getDepartamentoId().equals(departamentoId)) {
                throw new ValidationException("La provincia no pertenece al departamento especificado");
            }

            // Buscar distrito
            Optional<Distrito> distritoOpt = ubicacionDAO.buscarDistritoPorId(distritoId);
            if (!distritoOpt.isPresent()) {
                throw new ValidationException("El distrito con ID " + distritoId + " no existe");
            }

            // Verificar que el distrito pertenezca a la provincia
            Distrito distrito = distritoOpt.get();
            if (!distrito.getProvinciaId().equals(provinciaId)) {
                throw new ValidationException("El distrito no pertenece a la provincia especificada");
            }

            // Construir DTO de ubicación completa
            Departamento departamento = departamentoOpt.get();
            UbicacionDTO ubicacionDTO = new UbicacionDTO();

            ubicacionDTO.setDepartamentoId(departamento.getId());
            ubicacionDTO.setDepartamentoCodigo(departamento.getCodigo());
            ubicacionDTO.setDepartamentoNombre(departamento.getNombre());

            ubicacionDTO.setProvinciaId(provincia.getId());
            ubicacionDTO.setProvinciaCodigo(provincia.getCodigo());
            ubicacionDTO.setProvinciaNombre(provincia.getNombre());

            ubicacionDTO.setDistritoId(distrito.getId());
            ubicacionDTO.setDistritoCodigo(distrito.getCodigo());
            ubicacionDTO.setDistritoNombre(distrito.getNombre());

            logger.info("Ubicación completa obtenida: {} > {} > {}",
                       departamento.getNombre(), provincia.getNombre(), distrito.getNombre());

            return ubicacionDTO;

        } catch (DatabaseException e) {
            logger.error("Error al obtener ubicación completa: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Convierte un Departamento a DepartamentoDTO
     */
    private DepartamentoDTO convertirDepartamentoADTO(Departamento departamento) {
        DepartamentoDTO dto = new DepartamentoDTO();
        dto.setId(departamento.getId());
        dto.setCodigo(departamento.getCodigo());
        dto.setNombre(departamento.getNombre());
        dto.setCapital(departamento.getCapital());
        return dto;
    }

    /**
     * Convierte una Provincia a ProvinciaDTO
     */
    private ProvinciaDTO convertirProvinciaADTO(Provincia provincia) {
        ProvinciaDTO dto = new ProvinciaDTO();
        dto.setId(provincia.getId());
        dto.setDepartamentoId(provincia.getDepartamentoId());
        dto.setCodigo(provincia.getCodigo());
        dto.setNombre(provincia.getNombre());
        return dto;
    }

    /**
     * Convierte un Distrito a DistritoDTO
     */
    private DistritoDTO convertirDistritoADTO(Distrito distrito) {
        DistritoDTO dto = new DistritoDTO();
        dto.setId(distrito.getId());
        dto.setProvinciaId(distrito.getProvinciaId());
        dto.setCodigo(distrito.getCodigo());
        dto.setNombre(distrito.getNombre());
        return dto;
    }
}
