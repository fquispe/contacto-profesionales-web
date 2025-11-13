package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de especialidades de profesionales.
 * Incluye datos de la especialidad y de la categoría relacionada.
 */
public class EspecialidadDTO {

    private Integer id;
    private Integer profesionalId;
    private Integer categoriaId;
    
    // Información de la categoría (obtenida via JOIN)
    private String categoriaNombre;
    private String categoriaDescripcion;
    private String categoriaIcono;
    private String categoriaColor;
    
    // Información de la especialidad
    private String descripcion;
    private Boolean incluyeMateriales;
    private Double costo;
    private String tipoCosto;
    private Boolean esPrincipal;
    private Integer orden;
    private Boolean activo;

    // Constructor vacío
    public EspecialidadDTO() {
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

    public String getCategoriaIcono() {
        return categoriaIcono;
    }

    public void setCategoriaIcono(String categoriaIcono) {
        this.categoriaIcono = categoriaIcono;
    }

    public String getCategoriaColor() {
        return categoriaColor;
    }

    public void setCategoriaColor(String categoriaColor) {
        this.categoriaColor = categoriaColor;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "EspecialidadDTO{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", categoriaId=" + categoriaId +
                ", categoriaNombre='" + categoriaNombre + '\'' +
                ", descripcion='" + descripcion + '\'' +
                ", costo=" + costo +
                ", tipoCosto='" + tipoCosto + '\'' +
                ", esPrincipal=" + esPrincipal +
                ", orden=" + orden +
                '}';
    }
}