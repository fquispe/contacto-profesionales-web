package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo de DireccionCliente que representa una dirección asociada a un cliente
 * Un cliente puede tener múltiples direcciones (máximo 3)
 */
public class DireccionCliente {
    
    private Long id;
    private Long clienteId;
    private String tipo; // PRINCIPAL, OFICINA, TRABAJO, OTRO
    private String direccionCompleta;
    private String distrito;
    private String referencias;
    private Boolean esPrincipal; // Indica si es la dirección principal
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;
    
    // Constructor vacío
    public DireccionCliente() {
        this.activo = true;
        this.esPrincipal = false;
        this.fechaCreacion = LocalDateTime.now();
    }
    
    // Constructor con campos principales
    public DireccionCliente(Long clienteId, String tipo, String direccionCompleta, String distrito) {
        this();
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.direccionCompleta = direccionCompleta;
        this.distrito = distrito;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getClienteId() {
        return clienteId;
    }
    
    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
    
    public String getDireccionCompleta() {
        return direccionCompleta;
    }
    
    public void setDireccionCompleta(String direccionCompleta) {
        this.direccionCompleta = direccionCompleta;
    }
    
    public String getDistrito() {
        return distrito;
    }
    
    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }
    
    public String getReferencias() {
        return referencias;
    }
    
    public void setReferencias(String referencias) {
        this.referencias = referencias;
    }
    
    public Boolean getEsPrincipal() {
        return esPrincipal;
    }
    
    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
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
    
    @Override
    public String toString() {
        return "DireccionCliente{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", tipo='" + tipo + '\'' +
                ", direccionCompleta='" + direccionCompleta + '\'' +
                ", distrito='" + distrito + '\'' +
                ", esPrincipal=" + esPrincipal +
                '}';
    }
}
