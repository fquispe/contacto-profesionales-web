package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de especialidades de profesional
 */
public class EspecialidadDTO {

    private Integer id;
    private Integer profesionalId;
    private Integer categoriaId;
    private String categoriaNombre;
    private String categoriaDescripcion;
    private Boolean esPrincipal;
    private Integer aniosExperiencia;
    private String descripcion;

    // Constructor vacío
    public EspecialidadDTO() {
    }

    // Constructor con campos principales
    public EspecialidadDTO(Integer categoriaId, String categoriaNombre, Boolean esPrincipal) {
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.esPrincipal = esPrincipal;
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
}
