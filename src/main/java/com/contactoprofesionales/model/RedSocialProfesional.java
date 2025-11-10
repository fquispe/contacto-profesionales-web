package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo que representa una red social de un profesional.
 * Aplicación de SRP: Solo encapsula datos de red social profesional.
 */
public class RedSocialProfesional {
    private Integer id;
    private Integer profesionalId;
    private String tipoRed; // FACEBOOK, INSTAGRAM, LINKEDIN, TWITTER, TIKTOK, WHATSAPP, WEBSITE, YOUTUBE
    private String url;
    private Boolean verificada;
    private LocalDateTime fechaCreacion;

    // Constructor vacío
    public RedSocialProfesional() {
        this.verificada = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Constructor con parámetros principales
    public RedSocialProfesional(Integer profesionalId, String tipoRed, String url) {
        this();
        this.profesionalId = profesionalId;
        this.tipoRed = tipoRed;
        this.url = url;
    }

    // Constructor completo
    public RedSocialProfesional(Integer id, Integer profesionalId, String tipoRed,
                               String url, Boolean verificada, LocalDateTime fechaCreacion) {
        this.id = id;
        this.profesionalId = profesionalId;
        this.tipoRed = tipoRed;
        this.url = url;
        this.verificada = verificada;
        this.fechaCreacion = fechaCreacion;
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

    public String getTipoRed() {
        return tipoRed;
    }

    public void setTipoRed(String tipoRed) {
        this.tipoRed = tipoRed;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getVerificada() {
        return verificada;
    }

    public void setVerificada(Boolean verificada) {
        this.verificada = verificada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    @Override
    public String toString() {
        return "RedSocialProfesional{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", tipoRed='" + tipoRed + '\'' +
                ", url='" + url + '\'' +
                ", verificada=" + verificada +
                '}';
    }
}
