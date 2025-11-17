package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo para representar una especialidad de un profesional.
 * Corresponde a la tabla 'especialidades_profesional' en la BD.
 * Un profesional puede tener hasta 3 especialidades, siendo una principal.
 */
public class EspecialidadProfesional {

    // Campos de la tabla especialidades_profesional
    private Integer id;
    private Integer profesionalId;
    private Integer categoriaId;
    private String servicioProfesional; // ✅ NUEVO - Nombre del servicio específico que brinda
    private String descripcion;
    private Boolean incluyeMateriales;
    private Double costo;
    private String tipoCosto; // 'hora', 'dia', 'mes'
    private Boolean esPrincipal;
    private Integer orden; // 1, 2, o 3
    // ✅ NUEVOS CAMPOS - Tipo de prestación de trabajo (añadido: 2025-11-14)
    private Boolean trabajoRemoto;     // Indica si ofrece servicio de forma remota
    private Boolean trabajoPresencial; // Indica si ofrece servicio de forma presencial
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Boolean activo;

    // Campos transientes (no persisten en BD, se obtienen via JOIN con categorias_servicio)
    private String categoriaNombre;
    private String categoriaDescripcion;
    private String categoriaIcono;
    private String categoriaColor;

    // Constructores
    public EspecialidadProfesional() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = LocalDateTime.now();
        this.activo = true;
        this.esPrincipal = false;
        this.incluyeMateriales = false;
        // ✅ Valores por defecto para tipo de prestación (añadido: 2025-11-14)
        this.trabajoRemoto = false;
        this.trabajoPresencial = false;
    }

    public EspecialidadProfesional(Integer profesionalId, Integer categoriaId,
                                   String descripcion, Boolean incluyeMateriales,
                                   Double costo, String tipoCosto, Boolean esPrincipal,
                                   Integer orden) {
        this();
        this.profesionalId = profesionalId;
        this.categoriaId = categoriaId;
        this.descripcion = descripcion;
        this.incluyeMateriales = incluyeMateriales;
        this.costo = costo;
        this.tipoCosto = tipoCosto;
        this.esPrincipal = esPrincipal;
        this.orden = orden;
    }

    // Getters y Setters - Campos de BD
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

    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    // ✅ NUEVO - Getter y Setter para servicioProfesional
    public String getServicioProfesional() {
        return servicioProfesional;
    }

    public void setServicioProfesional(String servicioProfesional) {
        this.servicioProfesional = servicioProfesional;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Boolean getIncluyeMateriales() {
        return incluyeMateriales;
    }

    public void setIncluyeMateriales(Boolean incluyeMateriales) {
        this.incluyeMateriales = incluyeMateriales;
    }

    public Double getCosto() {
        return costo;
    }

    public void setCosto(Double costo) {
        this.costo = costo;
    }

    public String getTipoCosto() {
        return tipoCosto;
    }

    public void setTipoCosto(String tipoCosto) {
        this.tipoCosto = tipoCosto;
    }

    public Boolean getEsPrincipal() {
        return esPrincipal;
    }

    public void setEsPrincipal(Boolean esPrincipal) {
        this.esPrincipal = esPrincipal;
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

    // ✅ Getters y Setters - Tipo de prestación de trabajo (añadido: 2025-11-14)
    /**
     * Indica si el profesional ofrece este servicio de forma remota
     * @return true si ofrece trabajo remoto, false en caso contrario
     */
    public Boolean getTrabajoRemoto() {
        return trabajoRemoto;
    }

    /**
     * Establece si el profesional ofrece este servicio de forma remota
     * @param trabajoRemoto true para habilitar trabajo remoto
     */
    public void setTrabajoRemoto(Boolean trabajoRemoto) {
        this.trabajoRemoto = trabajoRemoto;
    }

    /**
     * Indica si el profesional ofrece este servicio de forma presencial
     * @return true si ofrece trabajo presencial, false en caso contrario
     */
    public Boolean getTrabajoPresencial() {
        return trabajoPresencial;
    }

    /**
     * Establece si el profesional ofrece este servicio de forma presencial
     * @param trabajoPresencial true para habilitar trabajo presencial
     */
    public void setTrabajoPresencial(Boolean trabajoPresencial) {
        this.trabajoPresencial = trabajoPresencial;
    }

    // Getters y Setters - Campos transientes (de categoría)
    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }

    public String getCategoriaDescripcion() {
        return categoriaDescripcion;
    }

    public void setCategoriaDescripcion(String categoriaDescripcion) {
        this.categoriaDescripcion = categoriaDescripcion;
    }

    public String getCategoriaIcono() {
        return categoriaIcono;
    }

    public void setCategoriaIcono(String categoriaIcono) {
        this.categoriaIcono = categoriaIcono;
    }

    public String getCategoriaColor() {
        return categoriaColor;
    }

    public void setCategoriaColor(String categoriaColor) {
        this.categoriaColor = categoriaColor;
    }

    // Métodos de validación
    // ✅ ACTUALIZADO - Agregada validación para servicioProfesional y tipo de prestación (actualizado: 2025-11-14)
    public boolean isValid() {
        // Validaciones existentes
        boolean validacionBasica = categoriaId != null && categoriaId > 0
            && servicioProfesional != null && !servicioProfesional.trim().isEmpty()
            && costo != null && costo > 0
            && tipoCosto != null && (tipoCosto.equals("hora") || tipoCosto.equals("dia") || tipoCosto.equals("mes"))
            && orden != null && orden >= 1 && orden <= 3;

        // ✅ NUEVA VALIDACIÓN - Al menos una modalidad de trabajo debe estar seleccionada
        boolean tieneModalidadTrabajo = (trabajoRemoto != null && trabajoRemoto) ||
                                       (trabajoPresencial != null && trabajoPresencial);

        return validacionBasica && tieneModalidadTrabajo;
    }

    @Override
    public String toString() {
        return "EspecialidadProfesional{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", categoriaId=" + categoriaId +
                ", categoriaNombre='" + categoriaNombre + '\'' +
                ", servicioProfesional='" + servicioProfesional + '\'' +
                ", costo=" + costo +
                ", tipoCosto='" + tipoCosto + '\'' +
                ", esPrincipal=" + esPrincipal +
                ", orden=" + orden +
                ", trabajoRemoto=" + trabajoRemoto +           // ✅ AÑADIDO: 2025-11-14
                ", trabajoPresencial=" + trabajoPresencial +   // ✅ AÑADIDO: 2025-11-14
                '}';
    }
}