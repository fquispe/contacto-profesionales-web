package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo para representar una especialidad de un profesional.
 * Corresponde a la tabla 'especialidades_profesional' en la BD.
 * Un profesional puede tener hasta 3 especialidades, siendo una principal.
 * MODIFICADO: Usa categoria_id de la tabla categorias_servicio.
 */
public class EspecialidadProfesional {

    private Integer id;
    private Integer profesionalId;
    private Integer categoriaId; // FK a categorias_servicio
    private String descripcion;
    private Boolean incluyeMateriales;
    private Double costo;
    private String tipoCosto; // 'hora', 'dia', 'mes'
    private Boolean esPrincipal;
    private Integer orden; // 1, 2, o 3
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;

    // Campos adicionales para JOINs (no están en la tabla)
    private String nombreCategoria; // Nombre de la categoría (de categorias_servicio)
    private String iconoCategoria; // Icono de la categoría
    private String colorCategoria; // Color de la categoría

    // Constructores
    public EspecialidadProfesional() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.activo = true;
        this.esPrincipal = false;
        this.incluyeMateriales = false;
    }

    public EspecialidadProfesional(Integer profesionalId, Integer categoriaId,
                                   String descripcion, Boolean incluyeMateriales,
                                   Double costo, String tipoCosto, Boolean esPrincipal,
                                   Integer orden) {
        this();
        this.profesionalId = profesionalId;
        this.categoriaId = categoriaId;
        this.descripcion = descripcion;
        this.incluyeMateriales = incluyeMateriales;
        this.costo = costo;
        this.tipoCosto = tipoCosto;
        this.esPrincipal = esPrincipal;
        this.orden = orden;
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

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getIconoCategoria() {
        return iconoCategoria;
    }

    public void setIconoCategoria(String iconoCategoria) {
        this.iconoCategoria = iconoCategoria;
    }

    public String getColorCategoria() {
        return colorCategoria;
    }

    public void setColorCategoria(String colorCategoria) {
        this.colorCategoria = colorCategoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getIncluyeMateriales() {
        return incluyeMateriales;
    }

    public void setIncluyeMateriales(Boolean incluyeMateriales) {
        this.incluyeMateriales = incluyeMateriales;
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

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
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

    // Métodos de validación
    public boolean isValid() {
        return categoriaId != null && categoriaId > 0
            && costo != null && costo > 0
            && tipoCosto != null && (tipoCosto.equals("hora") || tipoCosto.equals("dia") || tipoCosto.equals("mes"))
            && orden != null && orden >= 1 && orden <= 3;
    }

    @Override
    public String toString() {
        return "EspecialidadProfesional{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", categoriaId=" + categoriaId +
                ", nombreCategoria='" + nombreCategoria + '\'' +
                ", costo=" + costo +
                ", tipoCosto='" + tipoCosto + '\'' +
                ", esPrincipal=" + esPrincipal +
                ", orden=" + orden +
                '}';
    }
}
