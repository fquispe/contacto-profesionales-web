package com.contactoprofesionales.dao.redes;

import com.contactoprofesionales.model.RedSocialProfesional;
import java.util.List;

/**
 * Interface DAO para gestión de redes sociales del profesional.
 *
 * Permite gestionar los enlaces a redes sociales (Facebook, Instagram, LinkedIn, etc.)
 *
 * Aplicación de DIP: Define el contrato sin depender de implementación específica.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public interface RedesSocialesProfesionalDAO {

    /**
     * Lista todas las redes sociales activas de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Lista de redes sociales
     * @throws Exception si hay error en la consulta
     */
    List<RedSocialProfesional> listarPorProfesional(Integer profesionalId) throws Exception;

    /**
     * Guarda una nueva red social.
     *
     * @param red Red social a guardar
     * @return ID de la red social creada
     * @throws Exception si hay error al guardar
     */
    Integer guardar(RedSocialProfesional red) throws Exception;

    /**
     * Actualiza una red social existente.
     *
     * @param red Red social con datos actualizados
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    boolean actualizar(RedSocialProfesional red) throws Exception;

    /**
     * Elimina (soft delete) una red social.
     *
     * @param id ID de la red social a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    boolean eliminar(Integer id) throws Exception;

    /**
     * Guarda o actualiza múltiples redes sociales de un profesional en una transacción.
     * Desactiva las que no vienen en la lista y crea/actualiza las que sí vienen.
     *
     * @param profesionalId ID del profesional
     * @param redes Lista de redes sociales
     * @return true si la operación fue exitosa
     * @throws Exception si hay error en la transacción
     */
    boolean guardarMultiples(Integer profesionalId, List<RedSocialProfesional> redes) throws Exception;
}
