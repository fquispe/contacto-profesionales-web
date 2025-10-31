package com.contactoprofesionales.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para representar una solicitud de servicio.
 * Corresponde a la tabla 'solicitudes_servicio' en la BD.
 */
public class SolicitudServicio {
    
    private Integer id;
    private Integer clienteId;
    private Integer profesionalId;
    private String descripcion;
    private Double presupuestoEstimado;
    private String direccion;
    private String distrito;
    private String codigoPostal;
    private String referencia;
    private LocalDateTime fechaServicio;
    private String urgencia; // 'normal' o 'urgent'
    private String notasAdicionales;
    private List<String> fotosUrls;
    private String estado; // 'pendiente', 'aceptada', 'rechazada', 'completada', 'cancelada'
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaActualizacion;
    private boolean activo;

    // Constructores
    public SolicitudServicio() {
        this.fechaSolicitud = LocalDateTime.now();
        this.activo = true;
        this.estado = "pendiente";
    }

    public SolicitudServicio(Integer clienteId, Integer profesionalId, String descripcion,
                            Double presupuestoEstimado, String direccion, String distrito) {
        this();
        this.clienteId = clienteId;
        this.profesionalId = profesionalId;
        this.descripcion = descripcion;
        this.presupuestoEstimado = presupuestoEstimado;
        this.direccion = direccion;
        this.distrito = distrito;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Integer profesionalId) {
        this.profesionalId = profesionalId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPresupuestoEstimado() {
        return presupuestoEstimado;
    }

    public void setPresupuestoEstimado(Double presupuestoEstimado) {
        this.presupuestoEstimado = presupuestoEstimado;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public LocalDateTime getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(LocalDateTime fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public String getNotasAdicionales() {
        return notasAdicionales;
    }

    public void setNotasAdicionales(String notasAdicionales) {
        this.notasAdicionales = notasAdicionales;
    }

    public List<String> getFotosUrls() {
        return fotosUrls;
    }

    public void setFotosUrls(List<String> fotosUrls) {
        this.fotosUrls = fotosUrls;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Métodos de negocio
    public boolean isPendiente() {
        return "pendiente".equals(this.estado);
    }

    public boolean isAceptada() {
        return "aceptada".equals(this.estado);
    }

    public boolean isRechazada() {
        return "rechazada".equals(this.estado);
    }

    public boolean puedeSerCancelada() {
        return isPendiente() || isAceptada();
    }

    public boolean isUrgente() {
        return "urgent".equals(this.urgencia);
    }

    @Override
    public String toString() {
        return "SolicitudServicio{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", profesionalId=" + profesionalId +
                ", descripcion='" + descripcion + '\'' +
                ", presupuestoEstimado=" + presupuestoEstimado +
                ", distrito='" + distrito + '\'' +
                ", fechaServicio=" + fechaServicio +
                ", urgencia='" + urgencia + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaSolicitud=" + fechaSolicitud +
                '}';
    }
}
