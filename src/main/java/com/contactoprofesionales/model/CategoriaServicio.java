package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una categoría de servicio profesional.
 * Aplicación de SRP: Solo encapsula datos de categoría de servicio.
 */
public class CategoriaServicio {
    private Integer id;
    private String nombre;
    private String descripcion;
    private String icono;
    private String color;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public CategoriaServicio() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros principales
    public CategoriaServicio(String nombre, String descripcion) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    // Constructor completo
    public CategoriaServicio(Integer id, String nombre, String descripcion,
                            String icono, String color, Boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.color = color;
        this.activo = activo;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
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
        return "CategoriaServicio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", activo=" + activo +
                '}';
    }
}
