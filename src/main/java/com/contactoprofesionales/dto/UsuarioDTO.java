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
    
    // NUEVOS CAMPOS PARA ROLES
    private Long usuarioPersonaId;
    private String nombreCompleto;
    private String tipoRol;
    private Boolean esCliente;
    private Boolean esProfesional;
    private Long clienteId;
    private Integer profesionalId;

    public UsuarioDTO() {
    }

    // Constructor original (para compatibilidad)
    public UsuarioDTO(Integer id, String nombre, String email, String telefono, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.activo = activo;
    }

    // Getters y Setters originales
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

    // NUEVOS GETTERS Y SETTERS
    public Long getUsuarioPersonaId() {
        return usuarioPersonaId;
    }

    public void setUsuarioPersonaId(Long usuarioPersonaId) {
        this.usuarioPersonaId = usuarioPersonaId;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTipoRol() {
        return tipoRol;
    }

    public void setTipoRol(String tipoRol) {
        this.tipoRol = tipoRol;
    }

    public Boolean getEsCliente() {
        return esCliente;
    }

    public void setEsCliente(Boolean esCliente) {
        this.esCliente = esCliente;
    }

    public Boolean getEsProfesional() {
        return esProfesional;
    }

    public void setEsProfesional(Boolean esProfesional) {
        this.esProfesional = esProfesional;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Integer profesionalId) {
        this.profesionalId = profesionalId;
    }
}
