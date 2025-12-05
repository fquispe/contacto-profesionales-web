package com.contactoprofesionales.dto;

import com.contactoprofesionales.model.SolicitudServicio;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO extendido para el detalle completo de una solicitud de servicio.
 * Incluye información del cliente, ubicación completa e imágenes.
 *
 * Creado: 2025-12-04
 */
public class SolicitudDetalleDTO {

    // Datos básicos de la solicitud
    private Integer id;
    private Integer clienteId;
    private Integer profesionalId;
    private String descripcion;
    private Double presupuestoEstimado;
    private String direccion;
    private String codigoPostal;
    private String referencia;
    private LocalDateTime fechaServicio;
    private String urgencia;
    private String notasAdicionales;
    private String estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaActualizacion;
    private boolean activo;

    // Información del cliente
    private String clienteNombreCompleto;
    private String clienteEmail;
    private String clienteTelefono;

    // Ubicación completa (nombres legibles)
    private String departamentoNombre;
    private String provinciaNombre;
    private String distritoNombre;

    // Modalidad y especialidad
    private String tipoPrestacion; // REMOTO o PRESENCIAL
    private Integer especialidadId;

    // Imágenes adjuntas
    private List<String> fotosUrls;

    // Constructor vacío
    public SolicitudDetalleDTO() {
    }

    // Constructor desde SolicitudServicio (datos básicos)
    public SolicitudDetalleDTO(SolicitudServicio solicitud) {
        this.id = solicitud.getId();
        this.clienteId = solicitud.getClienteId();
        this.profesionalId = solicitud.getProfesionalId();
        this.descripcion = solicitud.getDescripcion();
        this.presupuestoEstimado = solicitud.getPresupuestoEstimado();
        this.direccion = solicitud.getDireccion();
        this.codigoPostal = solicitud.getCodigoPostal();
        this.referencia = solicitud.getReferencia();
        this.fechaServicio = solicitud.getFechaServicio();
        this.urgencia = solicitud.getUrgencia();
        this.notasAdicionales = solicitud.getNotasAdicionales();
        this.estado = solicitud.getEstado();
        this.fechaSolicitud = solicitud.getFechaSolicitud();
        this.fechaRespuesta = solicitud.getFechaRespuesta();
        this.fechaActualizacion = solicitud.getFechaActualizacion();
        this.activo = solicitud.isActivo();
        this.tipoPrestacion = solicitud.getTipoPrestacion();
        this.especialidadId = solicitud.getEspecialidadId();
        this.fotosUrls = solicitud.getFotosUrls();
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

    // Getters y Setters de información del cliente

    public String getClienteNombreCompleto() {
        return clienteNombreCompleto;
    }

    public void setClienteNombreCompleto(String clienteNombreCompleto) {
        this.clienteNombreCompleto = clienteNombreCompleto;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public String getClienteTelefono() {
        return clienteTelefono;
    }

    public void setClienteTelefono(String clienteTelefono) {
        this.clienteTelefono = clienteTelefono;
    }

    // Getters y Setters de ubicación

    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    public void setDepartamentoNombre(String departamentoNombre) {
        this.departamentoNombre = departamentoNombre;
    }

    public String getProvinciaNombre() {
        return provinciaNombre;
    }

    public void setProvinciaNombre(String provinciaNombre) {
        this.provinciaNombre = provinciaNombre;
    }

    public String getDistritoNombre() {
        return distritoNombre;
    }

    public void setDistritoNombre(String distritoNombre) {
        this.distritoNombre = distritoNombre;
    }

    // Getters y Setters de modalidad y especialidad

    public String getTipoPrestacion() {
        return tipoPrestacion;
    }

    public void setTipoPrestacion(String tipoPrestacion) {
        this.tipoPrestacion = tipoPrestacion;
    }

    public Integer getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Integer especialidadId) {
        this.especialidadId = especialidadId;
    }

    // Getters y Setters de imágenes

    public List<String> getFotosUrls() {
        return fotosUrls;
    }

    public void setFotosUrls(List<String> fotosUrls) {
        this.fotosUrls = fotosUrls;
    }

    @Override
    public String toString() {
        return "SolicitudDetalleDTO{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", clienteNombreCompleto='" + clienteNombreCompleto + '\'' +
                ", profesionalId=" + profesionalId +
                ", descripcion='" + descripcion + '\'' +
                ", estado='" + estado + '\'' +
                ", tipoPrestacion='" + tipoPrestacion + '\'' +
                ", departamento='" + departamentoNombre + '\'' +
                ", provincia='" + provinciaNombre + '\'' +
                ", distrito='" + distritoNombre + '\'' +
                '}';
    }
}
