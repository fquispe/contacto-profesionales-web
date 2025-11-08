package com.contactoprofesionales.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferir información del cliente entre capas
 */
public class ClienteDTO {
    
    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String fotoPerfilUrl;
    
    // Preferencias de búsqueda
    private List<String> categoriasFavoritas;
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
    
    // Direcciones
    private List<DireccionClienteDTO> direcciones;
    
    // Información de auditoría
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
    
    // Constructor vacío
    public ClienteDTO() {
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
    
    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }
    
    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }
    
    public List<String> getCategoriasFavoritas() {
        return categoriasFavoritas;
    }
    
    public void setCategoriasFavoritas(List<String> categoriasFavoritas) {
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
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }
    
    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
    
    public Boolean getActivo() {
        return activo;
    }
    
    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
