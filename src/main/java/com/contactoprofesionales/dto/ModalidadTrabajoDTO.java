package com.contactoprofesionales.dto;

import java.io.Serializable;

/**
 * DTO para retornar información de modalidad de trabajo de una especialidad.
 * Indica si el profesional ofrece trabajo remoto y/o presencial para
 * una especialidad específica.
 *
 * Este DTO se utiliza en el endpoint GET /api/especialidad/modalidad
 * para determinar qué opciones habilitar en el formulario de solicitud
 * de servicio.
 *
 * @since Migración V008
 */
public class ModalidadTrabajoDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID de la especialidad profesional
     */
    private Integer especialidadId;

    /**
     * Flag que indica si el profesional ofrece trabajo remoto
     * para esta especialidad.
     */
    private Boolean trabajoRemoto;

    /**
     * Flag que indica si el profesional ofrece trabajo presencial
     * para esta especialidad.
     */
    private Boolean trabajoPresencial;

    // Constructores

    /**
     * Constructor vacío para deserialización.
     */
    public ModalidadTrabajoDTO() {
    }

    /**
     * Constructor completo.
     *
     * @param especialidadId      ID de la especialidad
     * @param trabajoRemoto       Flag de trabajo remoto
     * @param trabajoPresencial   Flag de trabajo presencial
     */
    public ModalidadTrabajoDTO(Integer especialidadId, Boolean trabajoRemoto, Boolean trabajoPresencial) {
        this.especialidadId = especialidadId;
        this.trabajoRemoto = trabajoRemoto;
        this.trabajoPresencial = trabajoPresencial;
    }

    // Getters y Setters

    public Integer getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Integer especialidadId) {
        this.especialidadId = especialidadId;
    }

    public Boolean getTrabajoRemoto() {
        return trabajoRemoto;
    }

    public void setTrabajoRemoto(Boolean trabajoRemoto) {
        this.trabajoRemoto = trabajoRemoto;
    }

    public Boolean getTrabajoPresencial() {
        return trabajoPresencial;
    }

    public void setTrabajoPresencial(Boolean trabajoPresencial) {
        this.trabajoPresencial = trabajoPresencial;
    }

    // Métodos de negocio

    /**
     * Verifica si al menos una modalidad está disponible.
     *
     * @return true si hay trabajo remoto O presencial disponible
     */
    public boolean tieneAlgunaModalidadDisponible() {
        return (trabajoRemoto != null && trabajoRemoto) ||
               (trabajoPresencial != null && trabajoPresencial);
    }

    /**
     * Verifica si ambas modalidades están disponibles.
     *
     * @return true si hay trabajo remoto Y presencial disponible
     */
    public boolean tieneAmbasModalidadesDisponibles() {
        return (trabajoRemoto != null && trabajoRemoto) &&
               (trabajoPresencial != null && trabajoPresencial);
    }

    @Override
    public String toString() {
        return "ModalidadTrabajoDTO{" +
                "especialidadId=" + especialidadId +
                ", trabajoRemoto=" + trabajoRemoto +
                ", trabajoPresencial=" + trabajoPresencial +
                '}';
    }
}
