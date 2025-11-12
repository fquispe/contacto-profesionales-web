package com.contactoprofesionales.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo para representar la disponibilidad horaria de un profesional.
 * Corresponde a la tabla 'disponibilidad_horaria' en la BD.
 */
public class DisponibilidadHoraria {

    private Integer id;
    private Integer profesionalId;
    private Boolean todoTiempo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;

    // Lista de horarios específicos por día (solo si todoTiempo = false)
    private List<HorarioDia> horariosDias;

    // Constructores
    public DisponibilidadHoraria() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.activo = true;
        this.todoTiempo = false;
        this.horariosDias = new ArrayList<>();
    }

    public DisponibilidadHoraria(Integer profesionalId, Boolean todoTiempo) {
        this();
        this.profesionalId = profesionalId;
        this.todoTiempo = todoTiempo;
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

    public Boolean getTodoTiempo() {
        return todoTiempo;
    }

    public void setTodoTiempo(Boolean todoTiempo) {
        this.todoTiempo = todoTiempo;
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

    public List<HorarioDia> getHorariosDias() {
        return horariosDias;
    }

    public void setHorariosDias(List<HorarioDia> horariosDias) {
        this.horariosDias = horariosDias;
    }

    // Métodos auxiliares
    public void addHorarioDia(HorarioDia horarioDia) {
        if (this.horariosDias == null) {
            this.horariosDias = new ArrayList<>();
        }
        this.horariosDias.add(horarioDia);
    }

    public boolean isValid() {
        if (todoTiempo) {
            return true;
        }
        return horariosDias != null && !horariosDias.isEmpty() && horariosDias.size() <= 7;
    }

    @Override
    public String toString() {
        return "DisponibilidadHoraria{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", todoTiempo=" + todoTiempo +
                ", horariosDias=" + (horariosDias != null ? horariosDias.size() : 0) +
                '}';
    }
}
