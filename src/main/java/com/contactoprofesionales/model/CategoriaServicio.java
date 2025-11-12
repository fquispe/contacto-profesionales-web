package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo para representar una categoría de servicio.
 * Corresponde a la tabla 'categorias_servicio' en la BD (tabla existente).
 */
public class CategoriaServicio {

    private Integer id;
    private String nombre;
    private String descripcion;
    private String icono;
    private String color;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Constructores
    public CategoriaServicio() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    public CategoriaServicio(String nombre, String descripcion, String icono, String color) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.icono = icono;
        this.color = color;
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

    // Métodos auxiliares
    public boolean isValid() {
        return nombre != null && !nombre.trim().isEmpty();
    }

    @Override
    public String toString() {
        return "CategoriaServicio{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", icono='" + icono + '\'' +
                ", color='" + color + '\'' +
                ", activo=" + activo +
                '}';
    }
}
