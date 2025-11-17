package com.contactoprofesionales.dao.antecedentes;

import com.contactoprofesionales.model.AntecedenteProfesional;
import com.contactoprofesionales.model.AntecedenteProfesional.TipoAntecedente;
import java.util.List;
import java.util.Optional;

/**
 * Interface DAO para gestión de antecedentes profesionales.
 *
 * Permite gestionar antecedentes policiales, penales y judiciales.
 * Los antecedentes son OPCIONALES pero mejoran la puntuación del profesional.
 *
 * Aplicación de DIP: Define el contrato sin depender de implementación específica.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public interface AntecedentesProfesionalDAO {

    /**
     * Lista todos los antecedentes activos de un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Lista de antecedentes
     * @throws Exception si hay error en la consulta
     */
    List<AntecedenteProfesional> listarPorProfesional(Integer profesionalId) throws Exception;

    /**
     * Busca un antecedente por tipo específico.
     *
     * @param profesionalId ID del profesional
     * @param tipo Tipo de antecedente (POLICIAL, PENAL, JUDICIAL)
     * @return Optional con el antecedente si existe
     * @throws Exception si hay error en la consulta
     */
    Optional<AntecedenteProfesional> buscarPorTipo(Integer profesionalId, TipoAntecedente tipo) throws Exception;

    /**
     * Guarda un nuevo antecedente.
     *
     * @param antecedente Antecedente a guardar
     * @return ID del antecedente creado
     * @throws Exception si hay error al guardar
     */
    Integer guardar(AntecedenteProfesional antecedente) throws Exception;

    /**
     * Actualiza un antecedente existente.
     *
     * @param antecedente Antecedente con datos actualizados
     * @return true si se actualizó correctamente
     * @throws Exception si hay error al actualizar
     */
    boolean actualizar(AntecedenteProfesional antecedente) throws Exception;

    /**
     * Marca un antecedente como verificado.
     * Solo puede ser llamado por un administrador.
     *
     * @param id ID del antecedente
     * @return true si se verificó correctamente
     * @throws Exception si hay error al verificar
     */
    boolean verificar(Integer id) throws Exception;

    /**
     * Elimina (soft delete) un antecedente.
     *
     * @param id ID del antecedente a eliminar
     * @return true si se eliminó correctamente
     * @throws Exception si hay error al eliminar
     */
    boolean eliminar(Integer id) throws Exception;

    /**
     * Cuenta cuántos antecedentes verificados tiene un profesional.
     *
     * @param profesionalId ID del profesional
     * @return Número de antecedentes verificados
     * @throws Exception si hay error en la consulta
     */
    int contarVerificados(Integer profesionalId) throws Exception;
}
