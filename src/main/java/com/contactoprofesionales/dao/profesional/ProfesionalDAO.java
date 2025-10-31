package com.contactoprofesionales.dao.profesional;

import com.contactoprofesionales.model.Profesional;
import com.contactoprofesionales.exception.DatabaseException;
import java.util.List;

/**
 * Interfaz DAO para operaciones con profesionales.
 * Define el contrato para la persistencia de profesionales.
 */
public interface ProfesionalDAO {
    
    /**
     * Busca un profesional por ID.
     */
    Profesional buscarPorId(Integer id) throws DatabaseException;
    
    /**
     * Busca un profesional por ID de usuario.
     */
    Profesional buscarPorUsuarioId(Integer usuarioId) throws DatabaseException;
    
    /**
     * Lista todos los profesionales activos.
     */
    List<Profesional> listarTodos() throws DatabaseException;
    
    /**
     * Busca profesionales por especialidad.
     */
    List<Profesional> buscarPorEspecialidad(String especialidad) throws DatabaseException;
    
    /**
     * Busca profesionales por distrito.
     */
    List<Profesional> buscarPorDistrito(String distrito) throws DatabaseException;
    
    /**
     * Busca profesionales con filtros combinados.
     */
    List<Profesional> buscarConFiltros(String especialidad, String distrito, 
                                       Double calificacionMin) throws DatabaseException;
    
    /**
     * Crea un nuevo profesional.
     */
    Profesional crear(Profesional profesional) throws DatabaseException;
    
    /**
     * Actualiza un profesional existente.
     */
    boolean actualizar(Profesional profesional) throws DatabaseException;
    
    /**
     * Elimina (inactiva) un profesional.
     */
    boolean eliminar(Integer id) throws DatabaseException;
    
    /**
     * Actualiza la disponibilidad del profesional.
     */
    boolean actualizarDisponibilidad(Integer id, boolean disponible) throws DatabaseException;
    
    /**
     * Actualiza la calificación del profesional.
     */
    boolean actualizarCalificacion(Integer id, Double nuevaCalificacion) throws DatabaseException;
    
    /**
     * Verifica si existe un profesional para un usuario.
     */
    boolean existePorUsuarioId(Integer usuarioId) throws DatabaseException;
    
    /**
     * Obtiene lista única de especialidades disponibles.
     */
    List<String> obtenerEspecialidadesUnicas() throws DatabaseException;
    
    /**
     * Obtiene lista única de distritos disponibles.
     */
    List<String> obtenerDistritosUnicos() throws DatabaseException;
    
}
