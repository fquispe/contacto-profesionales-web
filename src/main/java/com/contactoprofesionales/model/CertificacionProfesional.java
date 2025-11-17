package com.contactoprofesionales.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo para representar una certificación, curso o estudio realizado por un profesional.
 *
 * Cada certificación incluye:
 * - Nombre de la certificación
 * - Institución que la otorgó
 * - Fechas de obtención y vigencia
 * - Documento de respaldo (opcional)
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class CertificacionProfesional {

    private Integer id;
    private Integer profesionalId;
    private String nombreCertificacion;
    private String institucion;
    private LocalDate fechaObtencion;
    private LocalDate fechaVigencia;
    private String documentoUrl;
    private String descripcion;
    private Integer orden;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructor vacío
    public CertificacionProfesional() {
        this.activo = true;
        this.orden = 1;
    }

    // Constructor completo
    public CertificacionProfesional(Integer id, Integer profesionalId, String nombreCertificacion,
                                    String institucion, LocalDate fechaObtencion, LocalDate fechaVigencia,
                                    String documentoUrl, String descripcion, Integer orden, Boolean activo) {
        this.id = id;
        this.profesionalId = profesionalId;
        this.nombreCertificacion = nombreCertificacion;
        this.institucion = institucion;
        this.fechaObtencion = fechaObtencion;
        this.fechaVigencia = fechaVigencia;
        this.documentoUrl = documentoUrl;
        this.descripcion = descripcion;
        this.orden = orden;
        this.activo = activo;
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Integer profesionalId) {
        this.profesionalId = profesionalId;
    }

    public String getNombreCertificacion() {
        return nombreCertificacion;
    }

    public void setNombreCertificacion(String nombreCertificacion) {
        this.nombreCertificacion = nombreCertificacion;
    }

    public String getInstitucion() {
        return institucion;
    }

    public void setInstitucion(String institucion) {
        this.institucion = institucion;
    }

    public LocalDate getFechaObtencion() {
        return fechaObtencion;
    }

    public void setFechaObtencion(LocalDate fechaObtencion) {
        this.fechaObtencion = fechaObtencion;
    }

    public LocalDate getFechaVigencia() {
        return fechaVigencia;
    }

    public void setFechaVigencia(LocalDate fechaVigencia) {
        this.fechaVigencia = fechaVigencia;
    }

    public String getDocumentoUrl() {
        return documentoUrl;
    }

    public void setDocumentoUrl(String documentoUrl) {
        this.documentoUrl = documentoUrl;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
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

    @Override
    public String toString() {
        return "CertificacionProfesional{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", nombreCertificacion='" + nombreCertificacion + '\'' +
                ", institucion='" + institucion + '\'' +
                ", fechaObtencion=" + fechaObtencion +
                ", fechaVigencia=" + fechaVigencia +
                ", activo=" + activo +
                '}';
    }
}
