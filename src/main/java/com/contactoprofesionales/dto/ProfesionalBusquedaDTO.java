package com.contactoprofesionales.dto;

import java.io.Serializable;
import java.util.List;

/**
 * DTO optimizado para mostrar profesionales en resultados de búsqueda.
 * Contiene solo la información necesaria para la visualización inicial,
 * sin datos sensibles ni información excesiva.
 */
public class ProfesionalBusquedaDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identificación
    private Integer id;
    private String nombreCompleto;
    
    // Información profesional
    private String especialidad;
    private String descripcionCorta; // Primeros 150 caracteres
    private String experiencia;
    
    // Habilidades destacadas (máximo 5)
    private List<String> habilidadesDestacadas;
    
    // Fotos
    private String fotoPerfil;
    
    // Tarifas y calificación
    private Double tarifaHora;
    private Double calificacionPromedio;
    private Integer totalResenas;
    
    // Ubicación
    private String distrito;
    private Integer radioServicio;
    
    // Estado
    private Boolean disponible;
    private Boolean verificado;
    
    // Información de contacto (básica)
    private String telefono;
    
    // Constructor vacío
    public ProfesionalBusquedaDTO() {
    }
    
    // Constructor completo
    public ProfesionalBusquedaDTO(Integer id, String nombreCompleto, String especialidad, 
                                  String descripcionCorta, String experiencia, 
                                  List<String> habilidadesDestacadas, String fotoPerfil, 
                                  Double tarifaHora, Double calificacionPromedio, 
                                  Integer totalResenas, String distrito, 
                                  Integer radioServicio, Boolean disponible, 
                                  Boolean verificado, String telefono) {
        this.id = id;
        this.nombreCompleto = nombreCompleto;
        this.especialidad = especialidad;
        this.descripcionCorta = descripcionCorta;
        this.experiencia = experiencia;
        this.habilidadesDestacadas = habilidadesDestacadas;
        this.fotoPerfil = fotoPerfil;
        this.tarifaHora = tarifaHora;
        this.calificacionPromedio = calificacionPromedio;
        this.totalResenas = totalResenas;
        this.distrito = distrito;
        this.radioServicio = radioServicio;
        this.disponible = disponible;
        this.verificado = verificado;
        this.telefono = telefono;
    }
    
    // Getters y Setters
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id;
    }
    
    public String getNombreCompleto() {
        return nombreCompleto;
    }
    
    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    
    public String getEspecialidad() {
        return especialidad;
    }
    
    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }
    
    public String getDescripcionCorta() {
        return descripcionCorta;
    }
    
    public void setDescripcionCorta(String descripcionCorta) {
        this.descripcionCorta = descripcionCorta;
    }
    
    public String getExperiencia() {
        return experiencia;
    }
    
    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }
    
    public List<String> getHabilidadesDestacadas() {
        return habilidadesDestacadas;
    }
    
    public void setHabilidadesDestacadas(List<String> habilidadesDestacadas) {
        this.habilidadesDestacadas = habilidadesDestacadas;
    }
    
    public String getFotoPerfil() {
        return fotoPerfil;
    }
    
    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }
    
    public Double getTarifaHora() {
        return tarifaHora;
    }
    
    public void setTarifaHora(Double tarifaHora) {
        this.tarifaHora = tarifaHora;
    }
    
    public Double getCalificacionPromedio() {
        return calificacionPromedio;
    }
    
    public void setCalificacionPromedio(Double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }
    
    public Integer getTotalResenas() {
        return totalResenas;
    }
    
    public void setTotalResenas(Integer totalResenas) {
        this.totalResenas = totalResenas;
    }
    
    public String getDistrito() {
        return distrito;
    }
    
    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }
    
    public Integer getRadioServicio() {
        return radioServicio;
    }
    
    public void setRadioServicio(Integer radioServicio) {
        this.radioServicio = radioServicio;
    }
    
    public Boolean getDisponible() {
        return disponible;
    }
    
    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
    
    public Boolean getVerificado() {
        return verificado;
    }
    
    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    /**
     * Obtiene la URL de la foto de perfil o una imagen por defecto.
     */
    public String getFotoPerfilUrl() {
        return fotoPerfil != null && !fotoPerfil.isEmpty() 
               ? fotoPerfil 
               : "/img/default-avatar.png";
    }
    
    /**
     * Formatea la tarifa por hora con el símbolo de moneda.
     */
    public String getTarifaFormateada() {
        return tarifaHora != null 
               ? String.format("S/ %.2f/hora", tarifaHora) 
               : "No especificada";
    }
    
    /**
     * Obtiene un texto descriptivo de la calificación.
     */
    public String getCalificacionTexto() {
        if (calificacionPromedio == null || totalResenas == null || totalResenas == 0) {
            return "Sin reseñas";
        }
        return String.format("%.1f ⭐ (%d reseñas)", calificacionPromedio, totalResenas);
    }
    
    /**
     * Obtiene el estado de disponibilidad en texto.
     */
    public String getEstadoDisponibilidad() {
        return disponible != null && disponible 
               ? "Disponible" 
               : "No disponible";
    }
    
    /**
     * Verifica si el profesional está disponible.
     */
    public boolean estaDisponible() {
        return disponible != null && disponible;
    }
    
    /**
     * Verifica si el profesional está verificado.
     */
    public boolean estaVerificado() {
        return verificado != null && verificado;
    }
    
    @Override
    public String toString() {
        return "ProfesionalBusquedaDTO{" +
                "id=" + id +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", especialidad='" + especialidad + '\'' +
                ", tarifaHora=" + tarifaHora +
                ", calificacionPromedio=" + calificacionPromedio +
                ", totalResenas=" + totalResenas +
                ", distrito='" + distrito + '\'' +
                ", disponible=" + disponible +
                ", verificado=" + verificado +
                '}';
    }
}