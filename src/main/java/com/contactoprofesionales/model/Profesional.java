package com.contactoprofesionales.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo de persistencia para la tabla 'profesionales'.
 * Representa el registro de un profesional en la base de datos.
 *
 * ⚠️ IMPORTANTE - ACTUALIZADO 2025-12-03:
 * Este modelo es SOLO para operaciones de persistencia (DAO).
 * NO usar directamente en controladores ni servicios.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 * ARQUITECTURA DE SEPARACIÓN DE RESPONSABILIDADES:
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * 1. BÚSQUEDA PÚBLICA DE PROFESIONALES:
 *    ✅ Usar: ProfesionalBusquedaDTO
 *    📍 Servicio: BusquedaProfesionalesService
 *    📍 Controlador: BusquedaProfesionalesServlet
 *    Propósito: Mostrar resultados de búsqueda optimizados, sin datos sensibles
 *
 * 2. GESTIÓN DE PERFIL PROFESIONAL:
 *    ✅ Usar: PerfilProfesionalCompletoDTO
 *    📍 Servicio: Múltiples servicios especializados (ver abajo)
 *    📍 Controladores en package controller.perfil:
 *       - PerfilProfesionalServlet: Datos básicos del perfil
 *       - CertificacionesProfesionalServlet: Certificaciones
 *       - ProyectosPortafolioServlet: Proyectos del portafolio
 *       - AntecedentesProfesionalServlet: Antecedentes
 *       - RedesSocialesProfesionalServlet: Redes sociales
 *    Propósito: CRUD completo del perfil con todas las relaciones
 *
 * 3. PERSISTENCIA (SOLO ESTE MODELO):
 *    📍 Capa DAO: ProfesionalDAO y ProfesionalDAOImpl
 *    Propósito: Mapeo directo con la tabla de base de datos
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * CAMPOS DEPRECADOS (mantener para compatibilidad con código legacy):
 * - @Deprecated especialidad: Ahora en tabla especialidades_profesional
 * - @Deprecated habilidades: Ahora en tabla certificaciones_profesionales
 * - @Deprecated certificaciones: Ahora en tabla certificaciones_profesionales
 * - @Deprecated fotoPerfil: Ya no se gestiona en formulario
 * - @Deprecated fotoPortada: Ya no se gestiona en formulario
 * - @Deprecated portafolio: Ahora en tabla proyectos_portafolio
 * - @Deprecated nombreCompleto, email, telefono: Datos de usuario, no de profesional
 *
 * ✅ REFACTORIZACIÓN COMPLETADA: 2025-12-03
 * Separación clara entre modelo de persistencia y DTOs de negocio.
 */
public class Profesional {

    private Integer id;
    private Integer usuarioId;

    /**
     * ID de la especialidad seleccionada (para resultados de búsqueda)
     */
    private Integer especialidadId;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con código legacy.
     * Las especialidades ahora se gestionan en la tabla 'especialidades_profesional'.
     * Usar: EspecialidadProfesionalDAO para gestionar especialidades.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
    private String especialidad;

    private String descripcion;

    /**
     * Biografía profesional extendida del profesional.
     * Campo agregado en V006__refactorizar_perfil_profesional.sql
     */
    private String biografiaProfesional;

    private String experiencia;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con código legacy.
     * Las habilidades ahora se gestionan en la tabla 'certificaciones_profesionales'.
     * Usar: CertificacionesProfesionalServlet y su DAO correspondiente.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
    private List<String> habilidades;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con código legacy.
     * Las certificaciones ahora se gestionan en la tabla 'certificaciones_profesionales'.
     * Usar: CertificacionesProfesionalServlet y su DAO correspondiente.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
    private List<String> certificaciones;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con código legacy.
     * Ya NO se gestiona en el formulario profesional.html (eliminado 2025-11-16).
     * Las fotos de perfil ahora se manejan a nivel de usuario, no de profesional.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
    private String fotoPerfil;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con código legacy.
     * Ya NO se gestiona en el formulario profesional.html (eliminado 2025-11-16).
     * Las fotos de portada ahora se manejan a nivel de usuario, no de profesional.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
    private String fotoPortada;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con código legacy.
     * El portafolio ahora se gestiona en la tabla 'proyectos_portafolio'.
     * Usar: ProyectosPortafolioServlet y su DAO correspondiente.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
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

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con búsquedas públicas.
     * Información del usuario (para joins con tabla usuarios).
     * Ya NO se gestiona en el formulario profesional.html (eliminado 2025-11-16).
     * Los datos del usuario se manejan en la tabla 'usuarios', no aquí.
     * Usar: UsuarioPersonaDAO para obtener información del usuario.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
    private String nombreCompleto;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con búsquedas públicas.
     * Ya NO se gestiona en formulario profesional.html.
     * Los datos del usuario se manejan en la tabla 'usuarios', no aquí.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
    private String email;

    /**
     * @deprecated CAMPO OBSOLETO - Mantener solo para compatibilidad con búsquedas públicas.
     * Ya NO se gestiona en formulario profesional.html.
     * Los datos del usuario se manejan en la tabla 'usuarios', no aquí.
     * IMPORTANTE: Este campo será ELIMINADO en la versión 3.0
     */
    @Deprecated
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

    public Integer getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Integer especialidadId) {
        this.especialidadId = especialidadId;
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

    public String getBiografiaProfesional() {
        return biografiaProfesional;
    }

    public void setBiografiaProfesional(String biografiaProfesional) {
        this.biografiaProfesional = biografiaProfesional;
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