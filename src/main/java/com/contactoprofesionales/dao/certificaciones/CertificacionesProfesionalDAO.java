package com.contactoprofesionales.dao.certificaciones;

import com.contactoprofesionales.model.CertificacionProfesional;
import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para gestión de certificaciones profesionales.
 *
 * Permite a los profesionales gestionar sus certificaciones, cursos y estudios.
 * Aplicación de DIP: Define el contrato sin depender de implementación específica.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public interface CertificacionesProfesionalDAO {

    /**
     * Lista todas las certificaciones activas de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Lista de certificaciones ordenadas por fecha de obtención
     * @throws Exception si hay error en la consulta
     */
    List<CertificacionProfesional> listarPorProfesional(Integer profesionalId) throws Exception;

    /**
     * Busca una certificación específica por su ID.
     *
     * @param id ID de la certificación
     * @return Optional con la certificación si existe
     * @throws Exception si hay error en la consulta
     */
    Optional<CertificacionProfesional> buscarPorId(Integer id) throws Exception;

    /**
     * Guarda una nueva certificación.
     *
     * @param certificacion Certificación a guardar
     * @return ID de la certificación creada
     * @throws Exception si hay error al guardar
     */
    Integer guardar(CertificacionProfesional certificacion) throws Exception;

    /**
     * Actualiza una certificación existente.
     *
     * @param certificacion Certificación con datos actualizados
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    boolean actualizar(CertificacionProfesional certificacion) throws Exception;

    /**
     * Elimina (soft delete) una certificación.
     *
     * @param id ID de la certificación a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    boolean eliminar(Integer id) throws Exception;

    /**
     * Cuenta el número de certificaciones activas de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Número de certificaciones activas
     * @throws Exception si hay error en la consulta
     */
    int contarPorProfesional(Integer profesionalId) throws Exception;
}
