package com.contactoprofesionales.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modelo para representar un proyecto realizado por el profesional en su portafolio.
 *
 * Cada profesional puede tener hasta 20 proyectos activos en su portafolio.
 * Los proyectos pueden tener:
 * - Información básica (nombre, fecha, descripción)
 * - Categoría del servicio
 * - Relación con solicitud de servicio real (opcional)
 * - Calificación del cliente (0-10, solo lectura - asignada por cliente)
 * - Hasta 5 imágenes (antes/después)
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class ProyectoPortafolio {

    private Integer id;
    private Integer profesionalId;
    private String nombreProyecto;
    private LocalDate fechaRealizacion;
    private String descripcion;
    private Integer categoriaId;
    private String categoriaNombre; // ✅ Campo adicional para mostrar nombre de categoría
    private Integer solicitudServicioId; // ✅ Relación con solicitud real (opcional)
    private BigDecimal calificacionCliente; // ✅ 0-10, solo lectura
    private String comentarioCliente; // ✅ Comentario del cliente
    private Integer orden;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // ✅ Lista de imágenes del proyecto (cargada desde imagenes_proyecto)
    private List<ImagenProyecto> imagenes;

    // Constructor vacío
    public ProyectoPortafolio() {
        this.activo = true;
        this.orden = 1;
    }

    // Constructor básico
    public ProyectoPortafolio(Integer profesionalId, String nombreProyecto,
                             LocalDate fechaRealizacion, String descripcion, Integer categoriaId) {
        this.profesionalId = profesionalId;
        this.nombreProyecto = nombreProyecto;
        this.fechaRealizacion = fechaRealizacion;
        this.descripcion = descripcion;
        this.categoriaId = categoriaId;
        this.activo = true;
        this.orden = 1;
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

    public String getNombreProyecto() {
        return nombreProyecto;
    }

    public void setNombreProyecto(String nombreProyecto) {
        this.nombreProyecto = nombreProyecto;
    }

    public LocalDate getFechaRealizacion() {
        return fechaRealizacion;
    }

    public void setFechaRealizacion(LocalDate fechaRealizacion) {
        this.fechaRealizacion = fechaRealizacion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public Integer getSolicitudServicioId() {
        return solicitudServicioId;
    }

    public void setSolicitudServicioId(Integer solicitudServicioId) {
        this.solicitudServicioId = solicitudServicioId;
    }

    public BigDecimal getCalificacionCliente() {
        return calificacionCliente;
    }

    public void setCalificacionCliente(BigDecimal calificacionCliente) {
        this.calificacionCliente = calificacionCliente;
    }

    public String getComentarioCliente() {
        return comentarioCliente;
    }

    public void setComentarioCliente(String comentarioCliente) {
        this.comentarioCliente = comentarioCliente;
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

    public List<ImagenProyecto> getImagenes() {
        return imagenes;
    }

    public void setImagenes(List<ImagenProyecto> imagenes) {
        this.imagenes = imagenes;
    }

    @Override
    public String toString() {
        return "ProyectoPortafolio{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", nombreProyecto='" + nombreProyecto + '\'' +
                ", fechaRealizacion=" + fechaRealizacion +
                ", categoriaId=" + categoriaId +
                ", calificacionCliente=" + calificacionCliente +
                ", activo=" + activo +
                '}';
    }
}
