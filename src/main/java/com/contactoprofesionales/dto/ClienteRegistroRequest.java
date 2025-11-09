package com.contactoprofesionales.dto;

import java.util.List;

/**
 * DTO para recibir la solicitud de registro/actualización de un cliente
 */
public class ClienteRegistroRequest {
    
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String password; // Para registro inicial
    private String fotoPerfilBase64; // Foto en base64 opcional
    
    // Preferencias de búsqueda
    private String categoriasFavoritas;
    private Integer radioBusqueda;
    private Double presupuestoPromedio;
    
    // Preferencias de notificaciones
    private Boolean notificacionesEmail;
    private Boolean notificacionesPush;
    private Boolean notificacionesPromociones;
    private Boolean notificacionesResenas;
    
    // Configuración de privacidad
    private Boolean perfilVisible;
    private Boolean compartirUbicacion;
    private Boolean historialPublico;
    
    // Direcciones (máximo 3)
    private List<DireccionClienteDTO> direcciones;
    
    // Constructor vacío
    public ClienteRegistroRequest() {
    }
    
    // Getters y Setters
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getFotoPerfilBase64() {
        return fotoPerfilBase64;
    }
    
    public void setFotoPerfilBase64(String fotoPerfilBase64) {
        this.fotoPerfilBase64 = fotoPerfilBase64;
    }
    
    public String getCategoriasFavoritas() {
        return categoriasFavoritas;
    }
    
    public void setCategoriasFavoritas(String categoriasFavoritas) {
        this.categoriasFavoritas = categoriasFavoritas;
    }
    
    public Integer getRadioBusqueda() {
        return radioBusqueda;
    }
    
    public void setRadioBusqueda(Integer radioBusqueda) {
        this.radioBusqueda = radioBusqueda;
    }
    
    public Double getPresupuestoPromedio() {
        return presupuestoPromedio;
    }
    
    public void setPresupuestoPromedio(Double presupuestoPromedio) {
        this.presupuestoPromedio = presupuestoPromedio;
    }
    
    public Boolean getNotificacionesEmail() {
        return notificacionesEmail;
    }
    
    public void setNotificacionesEmail(Boolean notificacionesEmail) {
        this.notificacionesEmail = notificacionesEmail;
    }
    
    public Boolean getNotificacionesPush() {
        return notificacionesPush;
    }
    
    public void setNotificacionesPush(Boolean notificacionesPush) {
        this.notificacionesPush = notificacionesPush;
    }
    
    public Boolean getNotificacionesPromociones() {
        return notificacionesPromociones;
    }
    
    public void setNotificacionesPromociones(Boolean notificacionesPromociones) {
        this.notificacionesPromociones = notificacionesPromociones;
    }
    
    public Boolean getNotificacionesResenas() {
        return notificacionesResenas;
    }
    
    public void setNotificacionesResenas(Boolean notificacionesResenas) {
        this.notificacionesResenas = notificacionesResenas;
    }
    
    public Boolean getPerfilVisible() {
        return perfilVisible;
    }
    
    public void setPerfilVisible(Boolean perfilVisible) {
        this.perfilVisible = perfilVisible;
    }
    
    public Boolean getCompartirUbicacion() {
        return compartirUbicacion;
    }
    
    public void setCompartirUbicacion(Boolean compartirUbicacion) {
        this.compartirUbicacion = compartirUbicacion;
    }
    
    public Boolean getHistorialPublico() {
        return historialPublico;
    }
    
    public void setHistorialPublico(Boolean historialPublico) {
        this.historialPublico = historialPublico;
    }
    
    public List<DireccionClienteDTO> getDirecciones() {
        return direcciones;
    }
    
    public void setDirecciones(List<DireccionClienteDTO> direcciones) {
        this.direcciones = direcciones;
    }
}
