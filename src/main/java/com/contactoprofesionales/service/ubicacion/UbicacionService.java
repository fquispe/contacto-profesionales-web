package com.contactoprofesionales.service.ubicacion;

import com.contactoprofesionales.dto.DepartamentoDTO;
import com.contactoprofesionales.dto.DistritoDTO;
import com.contactoprofesionales.dto.ProvinciaDTO;
import com.contactoprofesionales.dto.UbicacionDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;

import java.util.List;

/**
 * Servicio de negocio para operaciones de ubicación geográfica.
 * Gestiona departamentos, provincias y distritos del Perú.
 */
public interface UbicacionService {

    /**
     * Lista todos los departamentos activos del Perú
     * @return Lista de departamentos
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    List<DepartamentoDTO> listarDepartamentos() throws DatabaseException;

    /**
     * Lista todas las provincias de un departamento específico
     * @param departamentoId ID del departamento
     * @return Lista de provincias
     * @throws ValidationException si el departamentoId es nulo o inválido
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    List<ProvinciaDTO> listarProvinciasPorDepartamento(Integer departamentoId)
            throws ValidationException, DatabaseException;

    /**
     * Lista todos los distritos de una provincia específica
     * @param provinciaId ID de la provincia
     * @return Lista de distritos
     * @throws ValidationException si el provinciaId es nulo o inválido
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    List<DistritoDTO> listarDistritosPorProvincia(Integer provinciaId)
            throws ValidationException, DatabaseException;

    /**
     * Busca distritos por nombre (búsqueda parcial)
     * @param nombre Nombre del distrito a buscar
     * @return Lista de distritos que coinciden con el nombre
     * @throws ValidationException si el nombre es nulo o vacío
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    List<DistritoDTO> buscarDistritos(String nombre)
            throws ValidationException, DatabaseException;

    /**
     * Obtiene la información completa de una ubicación (departamento, provincia y distrito)
     * @param departamentoId ID del departamento
     * @param provinciaId ID de la provincia
     * @param distritoId ID del distrito
     * @return DTO con toda la información de ubicación
     * @throws ValidationException si alguno de los IDs es nulo o inválido
     * @throws DatabaseException si ocurre un error al consultar la base de datos
     */
    UbicacionDTO obtenerUbicacionCompleta(Integer departamentoId, Integer provinciaId, Integer distritoId)
            throws ValidationException, DatabaseException;
}
