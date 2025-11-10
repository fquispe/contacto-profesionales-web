package com.contactoprofesionales.dao.ubicacion;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.Departamento;
import com.contactoprofesionales.model.Provincia;
import com.contactoprofesionales.model.Distrito;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para operaciones con ubicaciones geográficas (Departamento, Provincia, Distrito)
 */
public interface UbicacionDAO {

    // ==================== DEPARTAMENTOS ====================

    /**
     * Lista todos los departamentos activos
     * @return Lista de departamentos
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<Departamento> listarDepartamentos() throws DatabaseException;

    /**
     * Busca un departamento por su ID
     * @param id ID del departamento
     * @return Optional con el departamento si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<Departamento> buscarDepartamentoPorId(Integer id) throws DatabaseException;

    /**
     * Busca un departamento por su código
     * @param codigo Código del departamento
     * @return Optional con el departamento si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<Departamento> buscarDepartamentoPorCodigo(String codigo) throws DatabaseException;

    // ==================== PROVINCIAS ====================

    /**
     * Lista todas las provincias de un departamento
     * @param departamentoId ID del departamento
     * @return Lista de provincias
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<Provincia> listarProvinciasPorDepartamento(Integer departamentoId) throws DatabaseException;

    /**
     * Busca una provincia por su ID
     * @param id ID de la provincia
     * @return Optional con la provincia si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<Provincia> buscarProvinciaPorId(Integer id) throws DatabaseException;

    /**
     * Busca una provincia por su código
     * @param codigo Código de la provincia
     * @return Optional con la provincia si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<Provincia> buscarProvinciaPorCodigo(String codigo) throws DatabaseException;

    // ==================== DISTRITOS ====================

    /**
     * Lista todos los distritos de una provincia
     * @param provinciaId ID de la provincia
     * @return Lista de distritos
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<Distrito> listarDistritosPorProvincia(Integer provinciaId) throws DatabaseException;

    /**
     * Busca un distrito por su ID
     * @param id ID del distrito
     * @return Optional con el distrito si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<Distrito> buscarDistritoPorId(Integer id) throws DatabaseException;

    /**
     * Busca un distrito por su código
     * @param codigo Código del distrito
     * @return Optional con el distrito si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<Distrito> buscarDistritoPorCodigo(String codigo) throws DatabaseException;

    /**
     * Busca distritos por nombre (búsqueda parcial)
     * @param nombre Nombre del distrito a buscar
     * @return Lista de distritos que coinciden
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<Distrito> buscarDistritosPorNombre(String nombre) throws DatabaseException;
}
