package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo para representar una ubicación específica de servicio.
 * Corresponde a la tabla 'ubicaciones_servicio' en la BD.
 * Máximo 10 ubicaciones por área de servicio.
 */
public class UbicacionServicio {

    private Integer id;
    private Integer areaServicioId;
    private String tipoUbicacion; // 'departamento', 'provincia', 'distrito'
    private String departamento;
    private String provincia;
    private String distrito;
    private Integer orden; // 1 a 10
    private LocalDateTime fechaCreacion;
    private Boolean activo;

    // Constructores
    public UbicacionServicio() {
        this.fechaCreacion = LocalDateTime.now();
        this.activo = true;
    }

    public UbicacionServicio(Integer areaServicioId, String tipoUbicacion,
                           String departamento, String provincia, String distrito,
                           Integer orden) {
        this();
        this.areaServicioId = areaServicioId;
        this.tipoUbicacion = tipoUbicacion;
        this.departamento = departamento;
        this.provincia = provincia;
        this.distrito = distrito;
        this.orden = orden;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAreaServicioId() {
        return areaServicioId;
    }

    public void setAreaServicioId(Integer areaServicioId) {
        this.areaServicioId = areaServicioId;
    }

    public String getTipoUbicacion() {
        return tipoUbicacion;
    }

    public void setTipoUbicacion(String tipoUbicacion) {
        this.tipoUbicacion = tipoUbicacion;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    // Métodos auxiliares
    public String getUbicacionCompleta() {
        StringBuilder sb = new StringBuilder();

        if (distrito != null && !distrito.trim().isEmpty()) {
            sb.append(distrito);
        }
        if (provincia != null && !provincia.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(provincia);
        }
        if (departamento != null && !departamento.trim().isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(departamento);
        }

        return sb.toString();
    }

    public boolean isValid() {
        return tipoUbicacion != null
            && (tipoUbicacion.equals("departamento") || tipoUbicacion.equals("provincia") || tipoUbicacion.equals("distrito"))
            && departamento != null && !departamento.trim().isEmpty()
            && orden != null && orden >= 1 && orden <= 10;
    }

    @Override
    public String toString() {
        return "UbicacionServicio{" +
                "id=" + id +
                ", tipoUbicacion='" + tipoUbicacion + '\'' +
                ", ubicacion='" + getUbicacionCompleta() + '\'' +
                ", orden=" + orden +
                '}';
    }
}
