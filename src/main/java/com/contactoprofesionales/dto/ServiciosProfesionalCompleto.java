package com.contactoprofesionales.dto;

import com.contactoprofesionales.model.*;
import java.util.List;

/**
 * DTO que encapsula toda la información de servicios de un profesional.
 * Incluye especialidades, área de servicio y disponibilidad horaria.
 */
public class ServiciosProfesionalCompleto {

    private Integer profesionalId;
    private List<EspecialidadProfesional> especialidades;
    private AreaServicio areaServicio;
    private DisponibilidadHoraria disponibilidad;
    private boolean tieneServicios;

    // Constructores
    public ServiciosProfesionalCompleto() {
        this.tieneServicios = false;
    }

    public ServiciosProfesionalCompleto(Integer profesionalId,
                                       List<EspecialidadProfesional> especialidades,
                                       AreaServicio areaServicio,
                                       DisponibilidadHoraria disponibilidad) {
        this.profesionalId = profesionalId;
        this.especialidades = especialidades;
        this.areaServicio = areaServicio;
        this.disponibilidad = disponibilidad;
        this.tieneServicios = (especialidades != null && !especialidades.isEmpty())
                           || areaServicio != null
                           || disponibilidad != null;
    }

    // Getters y Setters
    public Integer getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Integer profesionalId) {
        this.profesionalId = profesionalId;
    }

    public List<EspecialidadProfesional> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<EspecialidadProfesional> especialidades) {
        this.especialidades = especialidades;
        actualizarTieneServicios();
    }

    public AreaServicio getAreaServicio() {
        return areaServicio;
    }

    public void setAreaServicio(AreaServicio areaServicio) {
        this.areaServicio = areaServicio;
        actualizarTieneServicios();
    }

    public DisponibilidadHoraria getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(DisponibilidadHoraria disponibilidad) {
        this.disponibilidad = disponibilidad;
        actualizarTieneServicios();
    }

    public boolean isTieneServicios() {
        return tieneServicios;
    }

    public void setTieneServicios(boolean tieneServicios) {
        this.tieneServicios = tieneServicios;
    }

    // Métodos auxiliares
    private void actualizarTieneServicios() {
        this.tieneServicios = (especialidades != null && !especialidades.isEmpty())
                           || areaServicio != null
                           || disponibilidad != null;
    }

    public boolean isCompleto() {
        return especialidades != null && !especialidades.isEmpty()
            && areaServicio != null
            && disponibilidad != null;
    }

    @Override
    public String toString() {
        return "ServiciosProfesionalCompleto{" +
                "profesionalId=" + profesionalId +
                ", especialidades=" + (especialidades != null ? especialidades.size() : 0) +
                ", tieneAreaServicio=" + (areaServicio != null) +
                ", tieneDisponibilidad=" + (disponibilidad != null) +
                ", isCompleto=" + isCompleto() +
                '}';
    }
}
