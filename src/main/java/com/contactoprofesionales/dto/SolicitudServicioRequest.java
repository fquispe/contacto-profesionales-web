package com.contactoprofesionales.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para crear una solicitud de servicio.
 */
public class SolicitudServicioRequest {
	private Integer clienteId;
	private Integer profesionalId;
    private String descripcion;
    private Double presupuestoEstimado;
    private String direccion;
    private String distrito;
    private String codigoPostal;
    private String referencia;
    private String fechaServicio; // ISO format
    private String horaServicio; // HH:mm
    private String urgencia;
    private String notasAdicionales;
    private List<String> fotosBase64;

    // Getters y Setters
    
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

    public String getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(String fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public String getHoraServicio() {
        return horaServicio;
    }

    public void setHoraServicio(String horaServicio) {
        this.horaServicio = horaServicio;
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

    public List<String> getFotosBase64() {
        return fotosBase64;
    }

    public void setFotosBase64(List<String> fotosBase64) {
        this.fotosBase64 = fotosBase64;
    }
}

/**
 * DTO para la respuesta de una solicitud de servicio.
 */
class SolicitudServicioResponse {
    private Integer id;
    private String codigoSolicitud;
    private String estado;
    private ProfesionalBasicInfo profesional;
    private String descripcion;
    private Double presupuestoEstimado;
    private String direccion;
    private String distrito;
    private LocalDateTime fechaServicio;
    private String urgencia;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private List<String> fotosUrls;

    // Constructor
    public SolicitudServicioResponse() {}

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigoSolicitud() {
        return codigoSolicitud;
    }

    public void setCodigoSolicitud(String codigoSolicitud) {
        this.codigoSolicitud = codigoSolicitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public ProfesionalBasicInfo getProfesional() {
        return profesional;
    }

    public void setProfesional(ProfesionalBasicInfo profesional) {
        this.profesional = profesional;
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

    public List<String> getFotosUrls() {
        return fotosUrls;
    }

    public void setFotosUrls(List<String> fotosUrls) {
        this.fotosUrls = fotosUrls;
    }

    // Inner class para información básica del profesional
    public static class ProfesionalBasicInfo {
        private Integer id;
        private String nombre;
        private String especialidad;
        private Double calificacion;

        public ProfesionalBasicInfo() {}

        public ProfesionalBasicInfo(Integer id, String nombre, String especialidad, Double calificacion) {
            this.id = id;
            this.nombre = nombre;
            this.especialidad = especialidad;
            this.calificacion = calificacion;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public String getEspecialidad() {
            return especialidad;
        }

        public void setEspecialidad(String especialidad) {
            this.especialidad = especialidad;
        }

        public Double getCalificacion() {
            return calificacion;
        }

        public void setCalificacion(Double calificacion) {
            this.calificacion = calificacion;
        }
    }
}