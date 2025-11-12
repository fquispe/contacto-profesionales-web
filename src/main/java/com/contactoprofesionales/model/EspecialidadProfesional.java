package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una especialidad de un profesional.
 * Un profesional puede tener hasta 3 especialidades, siendo una la principal.
 * Aplicación de SRP: Solo encapsula datos de especialidad profesional.
 */
public class EspecialidadProfesional {
    private Integer id;
    private Integer profesionalId;
    private Integer categoriaId;
    private Boolean esPrincipal;
    private Integer aniosExperiencia;
    private String descripcion;
    private Double costo;
    private String tipoCosto; // 'hora', 'dia', 'mes'
    private Boolean incluyeMateriales;
    private Integer orden; // 1, 2, o 3
    private LocalDateTime fechaCreacion;

    // Campos adicionales para joins (no se persisten, solo para consultas)
    private String categoriaNombre;
    private String categoriaDescripcion;

    // Constructor vacío
    public EspecialidadProfesional() {
        this.esPrincipal = false;
        this.aniosExperiencia = 0;
        this.incluyeMateriales = false;
        this.orden = 1;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros principales
    public EspecialidadProfesional(Integer profesionalId, Integer categoriaId, Boolean esPrincipal) {
        this();
        this.profesionalId = profesionalId;
        this.categoriaId = categoriaId;
        this.esPrincipal = esPrincipal;
    }

    // Constructor completo
    public EspecialidadProfesional(Integer id, Integer profesionalId, Integer categoriaId,
                                  Boolean esPrincipal, Integer aniosExperiencia,
                                  String descripcion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.profesionalId = profesionalId;
        this.categoriaId = categoriaId;
        this.esPrincipal = esPrincipal;
        this.aniosExperiencia = aniosExperiencia;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
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

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getCategoriaDescripcion() {
        return categoriaDescripcion;
    }

    public void setCategoriaDescripcion(String categoriaDescripcion) {
        this.categoriaDescripcion = categoriaDescripcion;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getTipoCosto() {
        return tipoCosto;
    }

    public void setTipoCosto(String tipoCosto) {
        this.tipoCosto = tipoCosto;
    }

    public Boolean getIncluyeMateriales() {
        return incluyeMateriales;
    }

    public void setIncluyeMateriales(Boolean incluyeMateriales) {
        this.incluyeMateriales = incluyeMateriales;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    @Override
    public String toString() {
        return "EspecialidadProfesional{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", categoriaId=" + categoriaId +
                ", esPrincipal=" + esPrincipal +
                ", aniosExperiencia=" + aniosExperiencia +
                ", costo=" + costo +
                ", tipoCosto='" + tipoCosto + '\'' +
                ", incluyeMateriales=" + incluyeMateriales +
                ", orden=" + orden +
                ", categoriaNombre='" + categoriaNombre + '\'' +
                '}';
    }
}
