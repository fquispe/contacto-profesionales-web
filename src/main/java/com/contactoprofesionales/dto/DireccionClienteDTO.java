package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de direcciones del cliente
 */
public class DireccionClienteDTO {
    
    private Long id;
    private String tipo;
    private String direccionCompleta;
    private String distrito;
    private String referencias;
    private Boolean esPrincipal;
    
    // Constructor vacío
    public DireccionClienteDTO() {
    }
    
    // Constructor con campos principales
    public DireccionClienteDTO(String tipo, String direccionCompleta, String distrito) {
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
}
