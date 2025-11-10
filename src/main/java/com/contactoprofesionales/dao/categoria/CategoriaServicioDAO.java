package com.contactoprofesionales.dao.categoria;

import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.CategoriaServicio;

import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para operaciones CRUD de CategoriaServicio
 */
public interface CategoriaServicioDAO {

    /**
     * Lista todas las categorías de servicio activas
     * @return Lista de categorías activas
     * @throws DatabaseException si ocurre un error en la consulta
     */
    List<CategoriaServicio> listarActivas() throws DatabaseException;

    /**
     * Busca una categoría de servicio por su ID
     * @param id ID de la categoría
     * @return Optional con la categoría si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<CategoriaServicio> buscarPorId(Integer id) throws DatabaseException;

    /**
     * Busca una categoría de servicio por su nombre
     * @param nombre Nombre de la categoría
     * @return Optional con la categoría si existe
     * @throws DatabaseException si ocurre un error en la consulta
     */
    Optional<CategoriaServicio> buscarPorNombre(String nombre) throws DatabaseException;
}
