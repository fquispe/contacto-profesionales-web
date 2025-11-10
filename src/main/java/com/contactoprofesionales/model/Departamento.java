package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa un departamento del Perú.
 * Aplicación de SRP: Solo encapsula datos geográficos de departamento.
 */
public class Departamento {
    private Integer id;
    private String codigo;
    private String nombre;
    private String capital;
    private Boolean activo;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public Departamento() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros principales
    public Departamento(String codigo, String nombre, String capital) {
        this();
        this.codigo = codigo;
        this.nombre = nombre;
        this.capital = capital;
    }

    // Constructor completo
    public Departamento(Integer id, String codigo, String nombre, String capital,
                       Boolean activo, LocalDateTime fechaCreacion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.capital = capital;
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

    public String getCapital() {
        return capital;
    }

    public void setCapital(String capital) {
        this.capital = capital;
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
        return "Departamento{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", capital='" + capital + '\'' +
                ", activo=" + activo +
                '}';
    }
}
