package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una provincia del Perú.
 * Aplicación de SRP: Solo encapsula datos geográficos de provincia.
 */
public class Provincia {
    private Integer id;
    private Integer departamentoId;
    private String codigo;
    private String nombre;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public Provincia() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros principales
    public Provincia(Integer departamentoId, String codigo, String nombre) {
        this();
        this.departamentoId = departamentoId;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    // Constructor completo
    public Provincia(Integer id, Integer departamentoId, String codigo, String nombre,
                    Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.departamentoId = departamentoId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activo = activo;
        this.fechaCreacion = fechaCreacion;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(Integer departamentoId) {
        this.departamentoId = departamentoId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return "Provincia{" +
                "id=" + id +
                ", departamentoId=" + departamentoId +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}
