package com.contactoprofesionales.model;

import java.time.LocalTime;

/**
 * Modelo para representar el horario de un día específico de la semana.
 * Corresponde a la tabla 'horarios_dia' en la BD.
 */
public class HorarioDia {

    private Integer id;
    private Integer disponibilidadId;
    private String diaSemana; // 'lunes', 'martes', 'miercoles', 'jueves', 'viernes', 'sabado', 'domingo'
    private String tipoJornada; // '8hrs', '24hrs'
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Boolean activo;

    // Constructores
    public HorarioDia() {
        this.activo = true;
    }

    public HorarioDia(Integer disponibilidadId, String diaSemana, String tipoJornada) {
        this();
        this.disponibilidadId = disponibilidadId;
        this.diaSemana = diaSemana;
        this.tipoJornada = tipoJornada;

        // Si es 8hrs, establecer horario por defecto
        if ("8hrs".equals(tipoJornada)) {
            this.horaInicio = LocalTime.of(8, 0); // 08:00
            this.horaFin = LocalTime.of(17, 0);   // 17:00
        }
    }

    public HorarioDia(Integer disponibilidadId, String diaSemana, String tipoJornada,
                     LocalTime horaInicio, LocalTime horaFin) {
        this();
        this.disponibilidadId = disponibilidadId;
        this.diaSemana = diaSemana;
        this.tipoJornada = tipoJornada;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDisponibilidadId() {
        return disponibilidadId;
    }

    public void setDisponibilidadId(Integer disponibilidadId) {
        this.disponibilidadId = disponibilidadId;
    }

    public String getDiaSemana() {
        return diaSemana;
    }

    public void setDiaSemana(String diaSemana) {
        this.diaSemana = diaSemana;
    }

    public String getTipoJornada() {
        return tipoJornada;
    }

    public void setTipoJornada(String tipoJornada) {
        this.tipoJornada = tipoJornada;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // Métodos auxiliares
    public boolean isValid() {
        if (diaSemana == null || tipoJornada == null) {
            return false;
        }

        String[] diasValidos = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};
        boolean diaValido = false;
        for (String dia : diasValidos) {
            if (dia.equals(diaSemana)) {
                diaValido = true;
                break;
            }
        }

        if (!diaValido) {
            return false;
        }

        if (!tipoJornada.equals("8hrs") && !tipoJornada.equals("24hrs")) {
            return false;
        }

        // Si es 8hrs, debe tener hora inicio y fin
        if ("8hrs".equals(tipoJornada)) {
            return horaInicio != null && horaFin != null && horaInicio.isBefore(horaFin);
        }

        return true;
    }

    public String getHorarioFormateado() {
        if ("24hrs".equals(tipoJornada)) {
            return "24 horas";
        }

        if (horaInicio != null && horaFin != null) {
            return horaInicio.toString() + " - " + horaFin.toString();
        }

        return "No especificado";
    }

    @Override
    public String toString() {
        return "HorarioDia{" +
                "id=" + id +
                ", diaSemana='" + diaSemana + '\'' +
                ", tipoJornada='" + tipoJornada + '\'' +
                ", horario='" + getHorarioFormateado() + '\'' +
                '}';
    }
}
