package com.contactoprofesionales.dto;

/**
 * DTO para respuesta de login exitoso.
 */
public class LoginResponse {
    private String token;
    private UsuarioDTO usuario;
    private long expiresIn; // milisegundos

    public LoginResponse() {
    }

    public LoginResponse(String token, UsuarioDTO usuario, long expiresIn) {
        this.token = token;
        this.usuario = usuario;
        this.expiresIn = expiresIn;
    }

    // Getters y Setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UsuarioDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioDTO usuario) {
        this.usuario = usuario;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }
}