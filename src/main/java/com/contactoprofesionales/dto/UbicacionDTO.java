package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de ubicación geográfica
 */
public class UbicacionDTO {

    private Integer departamentoId;
    private String departamentoCodigo;
    private String departamentoNombre;

    private Integer provinciaId;
    private String provinciaCodigo;
    private String provinciaNombre;

    private Integer distritoId;
    private String distritoCodigo;
    private String distritoNombre;

    // Constructor vacío
    public UbicacionDTO() {
    }

    // Constructor completo
    public UbicacionDTO(Integer departamentoId, String departamentoNombre,
                       Integer provinciaId, String provinciaNombre,
                       Integer distritoId, String distritoNombre) {
        this.departamentoId = departamentoId;
        this.departamentoNombre = departamentoNombre;
        this.provinciaId = provinciaId;
        this.provinciaNombre = provinciaNombre;
        this.distritoId = distritoId;
        this.distritoNombre = distritoNombre;
    }

    // Getters y Setters
    public Integer getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(Integer departamentoId) {
        this.departamentoId = departamentoId;
    }

    public String getDepartamentoCodigo() {
        return departamentoCodigo;
    }

    public void setDepartamentoCodigo(String departamentoCodigo) {
        this.departamentoCodigo = departamentoCodigo;
    }

    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    public void setDepartamentoNombre(String departamentoNombre) {
        this.departamentoNombre = departamentoNombre;
    }

    public Integer getProvinciaId() {
        return provinciaId;
    }

    public void setProvinciaId(Integer provinciaId) {
        this.provinciaId = provinciaId;
    }

    public String getProvinciaCodigo() {
        return provinciaCodigo;
    }

    public void setProvinciaCodigo(String provinciaCodigo) {
        this.provinciaCodigo = provinciaCodigo;
    }

    public String getProvinciaNombre() {
        return provinciaNombre;
    }

    public void setProvinciaNombre(String provinciaNombre) {
        this.provinciaNombre = provinciaNombre;
    }

    public Integer getDistritoId() {
        return distritoId;
    }

    public void setDistritoId(Integer distritoId) {
        this.distritoId = distritoId;
    }

    public String getDistritoCodigo() {
        return distritoCodigo;
    }

    public void setDistritoCodigo(String distritoCodigo) {
        this.distritoCodigo = distritoCodigo;
    }

    public String getDistritoNombre() {
        return distritoNombre;
    }

    public void setDistritoNombre(String distritoNombre) {
        this.distritoNombre = distritoNombre;
    }
}
