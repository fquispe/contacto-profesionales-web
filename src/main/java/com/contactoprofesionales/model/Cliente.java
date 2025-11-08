package com.contactoprofesionales.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de Cliente que representa la información del perfil de un cliente en el sistema
 */
public class Cliente {
    
    private Long id;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String fotoPerfilUrl;
    
    // Preferencias de búsqueda
    private String categoriasFavoritas; // JSON array serializado
    private Integer radioBusqueda; // en kilómetros
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
    
    // Auditoría
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
    
    // Relación con direcciones (se cargará por separado)
    private List<DireccionCliente> direcciones;
    
    // Constructor vacío
    public Cliente() {
        this.activo = true;
        this.fechaRegistro = LocalDateTime.now();
    }
    
    // Constructor con campos principales
    public Cliente(String nombreCompleto, String email, String telefono) {
        this();
        this.nombreCompleto = nombreCompleto;
        this.email = email;
        this.telefono = telefono;
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
    
    public List<DireccionCliente> getDirecciones() {
        return direcciones;
    }
    
    public void setDirecciones(List<DireccionCliente> direcciones) {
        this.direcciones = direcciones;
    }
    
    @Override
    public String toString() {
        return "Cliente{" +
                "id=" + id +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", email='" + email + '\'' +
                ", telefono='" + telefono + '\'' +
                ", radioBusqueda=" + radioBusqueda +
                ", activo=" + activo +
                '}';
    }
}