package com.contactoprofesionales.dao;

import com.contactoprofesionales.model.*;
import java.util.List;

/**
 * Interface para gestionar los servicios profesionales de manera integral.
 * Maneja especialidades, áreas de servicio y disponibilidad horaria.
 */
public interface ServiciosProfesionalDAO {

    /**
     * Guarda todos los servicios de un profesional de manera transaccional.
     * Incluye especialidades, área de servicio y disponibilidad.
     *
     * @param profesionalId ID del profesional
     * @param especialidades Lista de especialidades (máximo 3)
     * @param areaServicio Configuración del área de servicio
     * @param disponibilidad Configuración de disponibilidad horaria
     * @return true si se guardó correctamente
     * @throws Exception si hay errores de validación o base de datos
     */
    boolean guardarServiciosProfesional(Integer profesionalId,
                                       List<EspecialidadProfesional> especialidades,
                                       AreaServicio areaServicio,
                                       DisponibilidadHoraria disponibilidad) throws Exception;

    /**
     * Actualiza todos los servicios de un profesional de manera transaccional.
     *
     * @param profesionalId ID del profesional
     * @param especialidades Lista de especialidades actualizadas
     * @param areaServicio Área de servicio actualizada
     * @param disponibilidad Disponibilidad horaria actualizada
     * @return true si se actualizó correctamente
     * @throws Exception si hay errores
     */
    boolean actualizarServiciosProfesional(Integer profesionalId,
                                          List<EspecialidadProfesional> especialidades,
                                          AreaServicio areaServicio,
                                          DisponibilidadHoraria disponibilidad) throws Exception;

    /**
     * Obtiene todos los servicios de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Objeto que contiene todas las configuraciones de servicios
     * @throws Exception si hay errores
     */
    ServiciosProfesionalCompleto obtenerServiciosProfesional(Integer profesionalId) throws Exception;

    /**
     * Verifica si un profesional ya tiene servicios configurados.
     *
     * @param profesionalId ID del profesional
     * @return true si ya tiene servicios configurados
     * @throws Exception si hay errores
     */
    boolean tieneServiciosConfigurados(Integer profesionalId) throws Exception;

    /**
     * Elimina todos los servicios de un profesional (soft delete).
     *
     * @param profesionalId ID del profesional
     * @return true si se eliminó correctamente
     * @throws Exception si hay errores
     */
    boolean eliminarServiciosProfesional(Integer profesionalId) throws Exception;

    // Métodos específicos para especialidades
    boolean guardarEspecialidades(Integer profesionalId, List<EspecialidadProfesional> especialidades) throws Exception;
    List<EspecialidadProfesional> obtenerEspecialidadesPorProfesional(Integer profesionalId) throws Exception;
    boolean eliminarEspecialidadesPorProfesional(Integer profesionalId) throws Exception;

    // Métodos específicos para área de servicio
    boolean guardarAreaServicio(AreaServicio areaServicio) throws Exception;
    AreaServicio obtenerAreaServicioPorProfesional(Integer profesionalId) throws Exception;
    boolean eliminarAreaServicioPorProfesional(Integer profesionalId) throws Exception;

    // Métodos específicos para disponibilidad
    boolean guardarDisponibilidad(DisponibilidadHoraria disponibilidad) throws Exception;
    DisponibilidadHoraria obtenerDisponibilidadPorProfesional(Integer profesionalId) throws Exception;
    boolean eliminarDisponibilidadPorProfesional(Integer profesionalId) throws Exception;
}
