package com.contactoprofesionales.dto;

/**
 * DTO para petición de login.
 * Separa la capa de presentación del modelo.
 */
public class LoginRequest {
    private String email;
    private String password;

    // Constructor vacío (requerido por Gson)
    public LoginRequest() {
    }

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters y Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "LoginRequest{email='" + email + "'}"; // NO mostrar password
    }
}