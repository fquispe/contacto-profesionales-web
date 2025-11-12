package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa la tabla 'users' (autenticación)
 */
public class Usuario {
    private Integer id;
    private String email;
    private String passwordHash;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimoAcceso;
    private Boolean activo;
    private Long usuarioId; // FK a tabla 'usuarios'
    private String username;
    private String rolSistema;
    private Integer intentosFallidos;
    private Boolean bloqueado;
    private LocalDateTime fechaBloqueo;
    private String tokenRecuperacion;
    private LocalDateTime fechaExpiracionToken;
    private Boolean requiereCambioPassword;

    public Usuario() {
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
        this.intentosFallidos = 0;
        this.bloqueado = false;
        this.requiereCambioPassword = false;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getUltimoAcceso() {
        return ultimoAcceso;
    }

    public void setUltimoAcceso(LocalDateTime ultimoAcceso) {
        this.ultimoAcceso = ultimoAcceso;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRolSistema() {
        return rolSistema;
    }

    public void setRolSistema(String rolSistema) {
        this.rolSistema = rolSistema;
    }

    public Integer getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(Integer intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public Boolean getBloqueado() {
        return bloqueado;
    }

    public void setBloqueado(Boolean bloqueado) {
        this.bloqueado = bloqueado;
    }

    public LocalDateTime getFechaBloqueo() {
        return fechaBloqueo;
    }

    public void setFechaBloqueo(LocalDateTime fechaBloqueo) {
        this.fechaBloqueo = fechaBloqueo;
    }

    public String getTokenRecuperacion() {
        return tokenRecuperacion;
    }

    public void setTokenRecuperacion(String tokenRecuperacion) {
        this.tokenRecuperacion = tokenRecuperacion;
    }

    public LocalDateTime getFechaExpiracionToken() {
        return fechaExpiracionToken;
    }

    public void setFechaExpiracionToken(LocalDateTime fechaExpiracionToken) {
        this.fechaExpiracionToken = fechaExpiracionToken;
    }

    public Boolean getRequiereCambioPassword() {
        return requiereCambioPassword;
    }

    public void setRequiereCambioPassword(Boolean requiereCambioPassword) {
        this.requiereCambioPassword = requiereCambioPassword;
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", usuarioId=" + usuarioId +
                ", username='" + username + '\'' +
                ", activo=" + activo +
                '}';
    }
}