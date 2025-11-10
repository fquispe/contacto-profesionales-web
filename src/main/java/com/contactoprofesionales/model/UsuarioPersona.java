package com.contactoprofesionales.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo que representa los datos personales de un usuario.
 * Esta tabla centraliza la información personal común entre clientes y profesionales.
 * Aplicación de SRP: Solo encapsula datos personales del usuario.
 */
public class UsuarioPersona {
    private Long id;

    // Datos personales
    private String nombreCompleto;
    private String tipoDocumento; // DNI, CE, RUC, PASAPORTE
    private String numeroDocumento;
    private LocalDate fechaNacimiento;
    private String genero; // MASCULINO, FEMENINO, OTRO, PREFIERO_NO_DECIR

    // Contacto
    private String telefono;
    private String telefonoAlternativo;

    // Ubicación personal
    private Integer departamentoId;
    private Integer provinciaId;
    private Integer distritoId;
    private String direccion;
    private String referenciaDireccion;

    // Gestión de roles
    private String tipoRol; // CLIENTE, PROFESIONAL, AMBOS
    private Boolean esCliente;
    private Boolean esProfesional;

    // Foto de perfil
    private String fotoPerfilUrl;

    // Auditoría
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;

    // Constructor vacío
    public UsuarioPersona() {
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
        this.esCliente = false;
        this.esProfesional = false;
        this.tipoRol = "CLIENTE";
        this.tipoDocumento = "DNI";
    }

    // Constructor con parámetros principales
    public UsuarioPersona(String nombreCompleto, String numeroDocumento, String telefono) {
        this();
        this.nombreCompleto = nombreCompleto;
        this.numeroDocumento = numeroDocumento;
        this.telefono = telefono;
    }

    // Constructor completo para creación
    public UsuarioPersona(String nombreCompleto, String tipoDocumento, String numeroDocumento,
                         LocalDate fechaNacimiento, String genero, String telefono,
                         String tipoRol, Boolean esCliente, Boolean esProfesional) {
        this();
        this.nombreCompleto = nombreCompleto;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.fechaNacimiento = fechaNacimiento;
        this.genero = genero;
        this.telefono = telefono;
        this.tipoRol = tipoRol;
        this.esCliente = esCliente;
        this.esProfesional = esProfesional;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTelefonoAlternativo() {
        return telefonoAlternativo;
    }

    public void setTelefonoAlternativo(String telefonoAlternativo) {
        this.telefonoAlternativo = telefonoAlternativo;
    }

    public Integer getDepartamentoId() {
        return departamentoId;
    }

    public void setDepartamentoId(Integer departamentoId) {
        this.departamentoId = departamentoId;
    }

    public Integer getProvinciaId() {
        return provinciaId;
    }

    public void setProvinciaId(Integer provinciaId) {
        this.provinciaId = provinciaId;
    }

    public Integer getDistritoId() {
        return distritoId;
    }

    public void setDistritoId(Integer distritoId) {
        this.distritoId = distritoId;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getReferenciaDireccion() {
        return referenciaDireccion;
    }

    public void setReferenciaDireccion(String referenciaDireccion) {
        this.referenciaDireccion = referenciaDireccion;
    }

    public String getTipoRol() {
        return tipoRol;
    }

    public void setTipoRol(String tipoRol) {
        this.tipoRol = tipoRol;
    }

    public Boolean getEsCliente() {
        return esCliente;
    }

    public void setEsCliente(Boolean esCliente) {
        this.esCliente = esCliente;
    }

    public Boolean getEsProfesional() {
        return esProfesional;
    }

    public void setEsProfesional(Boolean esProfesional) {
        this.esProfesional = esProfesional;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "UsuarioPersona{" +
                "id=" + id +
                ", nombreCompleto='" + nombreCompleto + '\'' +
                ", tipoDocumento='" + tipoDocumento + '\'' +
                ", numeroDocumento='" + numeroDocumento + '\'' +
                ", telefono='" + telefono + '\'' +
                ", tipoRol='" + tipoRol + '\'' +
                ", esCliente=" + esCliente +
                ", esProfesional=" + esProfesional +
                ", activo=" + activo +
                '}';
    }
}
