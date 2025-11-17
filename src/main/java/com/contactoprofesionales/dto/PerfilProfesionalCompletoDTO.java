package com.contactoprofesionales.dto;

import com.contactoprofesionales.model.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO que consolida TODA la información del perfil profesional completo.
 *
 * Este DTO incluye:
 * - Datos básicos del profesional (biografía, experiencia, fotos, tarifas, calificación)
 * - Certificaciones profesionales
 * - Portafolio de proyectos (con imágenes)
 * - Antecedentes (policial, penal, judicial)
 * - Redes sociales
 * - Especialidades
 * - Áreas de cobertura
 * - Disponibilidad horaria
 * - Puntuación de la plataforma (calculada)
 *
 * Usado para:
 * - GET /api/profesional/perfil (obtener perfil completo)
 * - Visualización completa del perfil profesional
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class PerfilProfesionalCompletoDTO {

    // ========================================
    // DATOS BÁSICOS DEL PROFESIONAL
    // ========================================

    private Integer id;
    private Integer usuarioId;

    // ✅ Datos personales (del usuario)
    private String nombreCompleto;
    private String email;
    private String telefono;
    private String documentoIdentidad;

    // ✅ Información profesional básica
    private String biografiaProfesional; // NUEVO: Resumen profesional
    private String descripcion;
    private String experiencia;
    private Integer aniosExperiencia; // NUEVO: Años de experiencia
    private List<String> habilidades;

    // ✅ Información adicional
    private String[] idiomas; // NUEVO: Idiomas que habla
    private String licenciasProfesionales; // NUEVO: Licencias profesionales
    private Boolean seguroResponsabilidad; // NUEVO: Tiene seguro de responsabilidad
    private String[] metodosPago; // NUEVO: Métodos de pago aceptados
    private String politicaCancelacion; // NUEVO: Política de cancelación

    // ✅ Fotos y visuales
    private String fotoPerfil;
    private String fotoPortada;

    // ✅ Tarifas y valoraciones
    private BigDecimal tarifaHora;
    private Double calificacionPromedio; // Calificación de clientes
    private Integer totalResenas;
    private BigDecimal puntuacionPlataforma; // ✅ CALCULADA: Puntuación interna (0-10)

    // ✅ Estado y verificación
    private Boolean verificado;
    private Boolean verificacionIdentidad;
    private Boolean certificadoAntecedentes;
    private Boolean disponible;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimaActualizacion;

    // ========================================
    // DATOS COMPLEJOS (RELACIONES)
    // ========================================

    /**
     * ✅ Certificaciones profesionales del usuario
     * Lista de certificados obtenidos en instituciones
     */
    private List<CertificacionProfesional> certificaciones;

    /**
     * ✅ Portafolio de proyectos realizados (máximo 20)
     * Cada proyecto incluye sus imágenes (máximo 5 por proyecto)
     * Incluye calificaciones de clientes por proyecto
     */
    private List<ProyectoPortafolio> proyectos;

    /**
     * ✅ Antecedentes (policial, penal, judicial)
     * Opcional pero mejora la puntuación
     */
    private List<AntecedenteProfesional> antecedentes;
    private Integer antecedentesVerificados; // Contador de antecedentes verificados

    /**
     * ✅ Redes sociales del profesional
     * Facebook, Instagram, LinkedIn, etc.
     */
    private List<RedSocialProfesional> redesSociales;

    /**
     * ✅ Especialidades profesionales (máximo 3)
     * Categorías y servicios que ofrece
     */
    private List<EspecialidadProfesional> especialidades;

    // TODO: Integrar con módulo "Servicios Profesionales" cuando esté listo
    /**
     * ⏸ Áreas de cobertura (distritos donde trabaja)
     * Cada área tiene distrito, latitud, longitud, radio de servicio
     * COMENTADO: Clase AreaCoberturaProfesional no existe aún
     */
    // private List<AreaCoberturaProfesional> areasCobertura;

    /**
     * ⏸ Disponibilidad horaria
     * Horarios por día de la semana
     * COMENTADO: Clase DisponibilidadProfesional no existe aún
     */
    // private List<DisponibilidadProfesional> disponibilidad;

    // ========================================
    // CONSTRUCTORES
    // ========================================

    public PerfilProfesionalCompletoDTO() {
    }

    // ========================================
    // GETTERS Y SETTERS
    // ========================================

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

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getBiografiaProfesional() {
        return biografiaProfesional;
    }

    public void setBiografiaProfesional(String biografiaProfesional) {
        this.biografiaProfesional = biografiaProfesional;
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

    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public List<String> getHabilidades() {
        return habilidades;
    }

    public void setHabilidades(List<String> habilidades) {
        this.habilidades = habilidades;
    }

    public String[] getIdiomas() {
        return idiomas;
    }

    public void setIdiomas(String[] idiomas) {
        this.idiomas = idiomas;
    }

    public String getLicenciasProfesionales() {
        return licenciasProfesionales;
    }

    public void setLicenciasProfesionales(String licenciasProfesionales) {
        this.licenciasProfesionales = licenciasProfesionales;
    }

    public Boolean getSeguroResponsabilidad() {
        return seguroResponsabilidad;
    }

    public void setSeguroResponsabilidad(Boolean seguroResponsabilidad) {
        this.seguroResponsabilidad = seguroResponsabilidad;
    }

    public String[] getMetodosPago() {
        return metodosPago;
    }

    public void setMetodosPago(String[] metodosPago) {
        this.metodosPago = metodosPago;
    }

    public String getPoliticaCancelacion() {
        return politicaCancelacion;
    }

    public void setPoliticaCancelacion(String politicaCancelacion) {
        this.politicaCancelacion = politicaCancelacion;
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

    public BigDecimal getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(BigDecimal tarifaHora) {
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

    public BigDecimal getPuntuacionPlataforma() {
        return puntuacionPlataforma;
    }

    public void setPuntuacionPlataforma(BigDecimal puntuacionPlataforma) {
        this.puntuacionPlataforma = puntuacionPlataforma;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }

    public Boolean getVerificacionIdentidad() {
        return verificacionIdentidad;
    }

    public void setVerificacionIdentidad(Boolean verificacionIdentidad) {
        this.verificacionIdentidad = verificacionIdentidad;
    }

    public Boolean getCertificadoAntecedentes() {
        return certificadoAntecedentes;
    }

    public void setCertificadoAntecedentes(Boolean certificadoAntecedentes) {
        this.certificadoAntecedentes = certificadoAntecedentes;
    }

    public Boolean getDisponible() {
        return disponible;
    }

    public void setDisponible(Boolean disponible) {
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

    public List<CertificacionProfesional> getCertificaciones() {
        return certificaciones;
    }

    public void setCertificaciones(List<CertificacionProfesional> certificaciones) {
        this.certificaciones = certificaciones;
    }

    public List<ProyectoPortafolio> getProyectos() {
        return proyectos;
    }

    public void setProyectos(List<ProyectoPortafolio> proyectos) {
        this.proyectos = proyectos;
    }

    public List<AntecedenteProfesional> getAntecedentes() {
        return antecedentes;
    }

    public void setAntecedentes(List<AntecedenteProfesional> antecedentes) {
        this.antecedentes = antecedentes;
    }

    public Integer getAntecedentesVerificados() {
        return antecedentesVerificados;
    }

    public void setAntecedentesVerificados(Integer antecedentesVerificados) {
        this.antecedentesVerificados = antecedentesVerificados;
    }

    public List<RedSocialProfesional> getRedesSociales() {
        return redesSociales;
    }

    public void setRedesSociales(List<RedSocialProfesional> redesSociales) {
        this.redesSociales = redesSociales;
    }

    public List<EspecialidadProfesional> getEspecialidades() {
        return especialidades;
    }

    public void setEspecialidades(List<EspecialidadProfesional> especialidades) {
        this.especialidades = especialidades;
    }

    // TODO: Descomentar cuando se integre el módulo "Servicios Profesionales"
    /*
    public List<AreaCoberturaProfesional> getAreasCobertura() {
        return areasCobertura;
    }

    public void setAreasCobertura(List<AreaCoberturaProfesional> areasCobertura) {
        this.areasCobertura = areasCobertura;
    }

    public List<DisponibilidadProfesional> getDisponibilidad() {
        return disponibilidad;
    }

    public void setDisponibilidad(List<DisponibilidadProfesional> disponibilidad) {
        this.disponibilidad = disponibilidad;
    }
    */

    @Override
    public String toString() {
        return "PerfilProfesionalCompletoDTO{" +
                "id=" + id +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", especialidades=" + (especialidades != null ? especialidades.size() : 0) +
                ", proyectos=" + (proyectos != null ? proyectos.size() : 0) +
                ", certificaciones=" + (certificaciones != null ? certificaciones.size() : 0) +
                ", antecedentesVerificados=" + antecedentesVerificados +
                ", puntuacionPlataforma=" + puntuacionPlataforma +
                '}';
    }
}
