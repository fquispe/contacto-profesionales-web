package com.contactoprofesionales.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para representar un profesional.
 * Corresponde a la tabla 'profesionales' en la BD.
 */
public class Profesional {
    
    private Integer id;
    private Integer usuarioId;
    private String especialidad;
    private String descripcion;
    private String experiencia;
    private List<String> habilidades;
    private List<String> certificaciones;
    private String fotoPerfil;
    private String fotoPortada;
    private List<String> portafolio;
    private Double tarifaHora;
    private Double calificacionPromedio;
    private Integer totalResenas;
    private String ubicacion;
    private String distrito;
    private Double latitud;
    private Double longitud;
    private Integer radioServicio; // en kilómetros
    private String disponibilidad;
    private boolean verificado;
    private boolean disponible;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimaActualizacion;
    private boolean activo;
    
    // Información del usuario (para joins)
    private String nombreCompleto;
    private String email;
    private String telefono;

    // Constructores
    public Profesional() {
        this.fechaRegistro = LocalDateTime.now();
        this.activo = true;
        this.disponible = true;
        this.verificado = false;
        this.calificacionPromedio = 0.0;
        this.totalResenas = 0;
    }

    public Profesional(Integer usuarioId, String especialidad, String descripcion) {
        this();
        this.usuarioId = usuarioId;
        this.especialidad = especialidad;
        this.descripcion = descripcion;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getExperiencia() {
        return experiencia;
    }

    public void setExperiencia(String experiencia) {
        this.experiencia = experiencia;
    }

    public List<String> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<String> habilidades) {
        this.habilidades = habilidades;
    }

    public List<String> getCertificaciones() {
        return certificaciones;
    }

    public void setCertificaciones(List<String> certificaciones) {
        this.certificaciones = certificaciones;
    }

    public String getFotoPerfil() {
        return fotoPerfil;
    }

    public void setFotoPerfil(String fotoPerfil) {
        this.fotoPerfil = fotoPerfil;
    }

    public String getFotoPortada() {
        return fotoPortada;
    }

    public void setFotoPortada(String fotoPortada) {
        this.fotoPortada = fotoPortada;
    }

    public List<String> getPortafolio() {
        return portafolio;
    }

    public void setPortafolio(List<String> portafolio) {
        this.portafolio = portafolio;
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

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Integer getRadioServicio() {
        return radioServicio;
    }

    public void setRadioServicio(Integer radioServicio) {
        this.radioServicio = radioServicio;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public boolean isVerificado() {
        return verificado;
    }

    public void setVerificado(boolean verificado) {
        this.verificado = verificado;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getUltimaActualizacion() {
        return ultimaActualizacion;
    }

    public void setUltimaActualizacion(LocalDateTime ultimaActualizacion) {
        this.ultimaActualizacion = ultimaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    // Métodos de negocio
    public boolean tieneCalificacion() {
        return totalResenas != null && totalResenas > 0;
    }

    public String getCalificacionEstrellas() {
        if (calificacionPromedio == null || calificacionPromedio == 0) {
            return "Sin calificación";
        }
        
        int estrellas = (int) Math.round(calificacionPromedio);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < estrellas ? "⭐" : "☆");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "Profesional{" +
                "id=" + id +
                ", usuarioId=" + usuarioId +
                ", especialidad='" + especialidad + '\'' +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", calificacionPromedio=" + calificacionPromedio +
                ", totalResenas=" + totalResenas +
                ", distrito='" + distrito + '\'' +
                ", verificado=" + verificado +
                ", disponible=" + disponible +
                '}';
    }
}