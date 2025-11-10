package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa un distrito del Perú.
 * Aplicación de SRP: Solo encapsula datos geográficos de distrito.
 */
public class Distrito {
    private Integer id;
    private Integer provinciaId;
    private String codigo;
    private String nombre;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public Distrito() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros principales
    public Distrito(Integer provinciaId, String codigo, String nombre) {
        this();
        this.provinciaId = provinciaId;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    // Constructor completo
    public Distrito(Integer id, Integer provinciaId, String codigo, String nombre,
                   Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.provinciaId = provinciaId;
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

    public Integer getProvinciaId() {
        return provinciaId;
    }

    public void setProvinciaId(Integer provinciaId) {
        this.provinciaId = provinciaId;
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
        return "Distrito{" +
                "id=" + id +
                ", provinciaId=" + provinciaId +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", activo=" + activo +
                '}';
    }
}
