package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de provincias
 */
public class ProvinciaDTO {

    private Integer id;
    private Integer departamentoId;
    private String codigo;
    private String nombre;

    // Constructor vacío
    public ProvinciaDTO() {
    }

    // Constructor con campos principales
    public ProvinciaDTO(Integer id, Integer departamentoId, String codigo, String nombre) {
        this.id = id;
        this.departamentoId = departamentoId;
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
}
