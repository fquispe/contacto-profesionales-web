package com.contactoprofesionales.dto;

/**
 * DTO para la respuesta del registro completo de usuario
 */
public class RegistroCompletoResponse {

    private Integer userId;
    private Long usuarioPersonaId;
    private Long clienteId;
    private Integer profesionalId;

    private String email;
    private String nombreCompleto;
    private String tipoRol;
    private Boolean esCliente;
    private Boolean esProfesional;

    private String mensaje;
    private boolean exitoso;

    // Constructor vacío
    public RegistroCompletoResponse() {
    }

    // Constructor de éxito
    public RegistroCompletoResponse(Integer userId, Long usuarioPersonaId, String email,
                                   String nombreCompleto, String tipoRol, Boolean esCliente, Boolean esProfesional) {
        this.userId = userId;
        this.usuarioPersonaId = usuarioPersonaId;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.tipoRol = tipoRol;
        this.esCliente = esCliente;
        this.esProfesional = esProfesional;
        this.exitoso = true;
        this.mensaje = "Registro exitoso";
    }

    // Getters y Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Long getUsuarioPersonaId() {
        return usuarioPersonaId;
    }

    public void setUsuarioPersonaId(Long usuarioPersonaId) {
        this.usuarioPersonaId = usuarioPersonaId;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }
}
