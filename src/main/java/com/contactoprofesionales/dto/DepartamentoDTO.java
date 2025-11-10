package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de departamentos
 */
public class DepartamentoDTO {

    private Integer id;
    private String codigo;
    private String nombre;
    private String capital;

    // Constructor vacío
    public DepartamentoDTO() {
    }

    // Constructor con campos principales
    public DepartamentoDTO(Integer id, String codigo, String nombre, String capital) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.capital = capital;
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
}
