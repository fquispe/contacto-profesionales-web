package com.contactoprofesionales.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para representar el área de servicio de un profesional.
 * Corresponde a la tabla 'areas_servicio' en la BD.
 */
public class AreaServicio {

    private Integer id;
    private Integer profesionalId;
    private Boolean todoPais;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;

    // Lista de ubicaciones específicas (solo si todoPais = false)
    private List<UbicacionServicio> ubicaciones;

    // Constructores
    public AreaServicio() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.activo = true;
        this.todoPais = false;
        this.ubicaciones = new ArrayList<>();
    }

    public AreaServicio(Integer profesionalId, Boolean todoPais) {
        this();
        this.profesionalId = profesionalId;
        this.todoPais = todoPais;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Integer profesionalId) {
        this.profesionalId = profesionalId;
    }

    public Boolean getTodoPais() {
        return todoPais;
    }

    public void setTodoPais(Boolean todoPais) {
        this.todoPais = todoPais;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<UbicacionServicio> getUbicaciones() {
        return ubicaciones;
    }

    public void setUbicaciones(List<UbicacionServicio> ubicaciones) {
        this.ubicaciones = ubicaciones;
    }

    // Métodos auxiliares
    public void addUbicacion(UbicacionServicio ubicacion) {
        if (this.ubicaciones == null) {
            this.ubicaciones = new ArrayList<>();
        }
        if (this.ubicaciones.size() < 10) {
            this.ubicaciones.add(ubicacion);
        }
    }

    public boolean isValid() {
        if (todoPais) {
            return true;
        }
        return ubicaciones != null && !ubicaciones.isEmpty() && ubicaciones.size() <= 10;
    }

    @Override
    public String toString() {
        return "AreaServicio{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", todoPais=" + todoPais +
                ", ubicaciones=" + (ubicaciones != null ? ubicaciones.size() : 0) +
                '}';
    }
}
