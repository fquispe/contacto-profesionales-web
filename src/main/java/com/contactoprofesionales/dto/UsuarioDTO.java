package com.contactoprofesionales.dto;

/**
 * DTO para transferir datos de usuario (sin información sensible).
 */
public class UsuarioDTO {
    private Integer id;
    private String nombre;
    private String email;
    private String telefono;
    private boolean activo;

    public UsuarioDTO() {
    }

    public UsuarioDTO(Integer id, String nombre, String email, String telefono, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.activo = activo;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
