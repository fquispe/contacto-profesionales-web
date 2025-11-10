package com.contactoprofesionales.dto;

/**
 * DTO para transferir información de redes sociales de profesional
 */
public class RedSocialDTO {

    private Integer id;
    private Integer profesionalId;
    private String tipoRed; // FACEBOOK, INSTAGRAM, LINKEDIN, TWITTER, TIKTOK, WHATSAPP, WEBSITE, YOUTUBE
    private String url;
    private Boolean verificada;

    // Constructor vacío
    public RedSocialDTO() {
    }

    // Constructor con campos principales
    public RedSocialDTO(String tipoRed, String url) {
        this.tipoRed = tipoRed;
        this.url = url;
        this.verificada = false;
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
}
