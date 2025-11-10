package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de distritos
 */
public class DistritoDTO {

    private Integer id;
    private Integer provinciaId;
    private String codigo;
    private String nombre;

    // Constructor vacío
    public DistritoDTO() {
    }

    // Constructor con campos principales
    public DistritoDTO(Integer id, Integer provinciaId, String codigo, String nombre) {
        this.id = id;
        this.provinciaId = provinciaId;
        this.codigo = codigo;
        this.nombre = nombre;
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
}
