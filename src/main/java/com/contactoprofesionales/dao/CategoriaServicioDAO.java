package com.contactoprofesionales.dao;

import com.contactoprofesionales.model.CategoriaServicio;
import java.util.List;

/**
 * Interface para gestionar categorías de servicio.
 * Solo consultas, ya que es una tabla de catálogo.
 */
public interface CategoriaServicioDAO {

    /**
     * Obtiene todas las categorías activas.
     *
     * @return Lista de categorías activas
     * @throws Exception si hay errores
     */
    List<CategoriaServicio> listarActivas() throws Exception;

    /**
     * Obtiene todas las categorías (activas e inactivas).
     *
     * @return Lista de todas las categorías
     * @throws Exception si hay errores
     */
    List<CategoriaServicio> listarTodas() throws Exception;

    /**
     * Busca una categoría por su ID.
     *
     * @param id ID de la categoría
     * @return La categoría encontrada o null
     * @throws Exception si hay errores
     */
    CategoriaServicio buscarPorId(Integer id) throws Exception;

    /**
     * Busca una categoría por su nombre.
     *
     * @param nombre Nombre de la categoría
     * @return La categoría encontrada o null
     * @throws Exception si hay errores
     */
    CategoriaServicio buscarPorNombre(String nombre) throws Exception;
}
