package com.contactoprofesionales.model;

import java.time.LocalDateTime;

/**
 * Modelo para representar una imagen de un proyecto del portafolio.
 *
 * Cada proyecto puede tener hasta 5 imágenes que muestren:
 * - Estado "antes" del trabajo
 * - Estado "después" del trabajo
 * - Proceso intermedio
 * - Imagen general
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class ImagenProyecto {

    private Integer id;
    private Integer proyectoId;
    private String urlImagen;
    private TipoImagen tipoImagen; // ✅ Enum: antes, despues, proceso, general
    private String descripcion;
    private Integer orden;
    private LocalDateTime fechaSubida;

    /**
     * Enum para tipos de imagen de proyecto
     */
    public enum TipoImagen {
        ANTES("antes"),
        DESPUES("despues"),
        PROCESO("proceso"),
        GENERAL("general");

        private final String valor;

        TipoImagen(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }

        public static TipoImagen fromString(String valor) {
            for (TipoImagen tipo : TipoImagen.values()) {
                if (tipo.valor.equalsIgnoreCase(valor)) {
                    return tipo;
                }
            }
            return GENERAL; // Por defecto
        }
    }

    // Constructor vacío
    public ImagenProyecto() {
        this.orden = 1;
        this.tipoImagen = TipoImagen.GENERAL;
    }

    // Constructor completo
    public ImagenProyecto(Integer proyectoId, String urlImagen, TipoImagen tipoImagen, Integer orden) {
        this.proyectoId = proyectoId;
        this.urlImagen = urlImagen;
        this.tipoImagen = tipoImagen;
        this.orden = orden;
    }

    // Getters y Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getProyectoId() {
        return proyectoId;
    }

    public void setProyectoId(Integer proyectoId) {
        this.proyectoId = proyectoId;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public TipoImagen getTipoImagen() {
        return tipoImagen;
    }

    public void setTipoImagen(TipoImagen tipoImagen) {
        this.tipoImagen = tipoImagen;
    }

    // ✅ Setter para recibir String desde BD o JSON
    public void setTipoImagenString(String tipo) {
        this.tipoImagen = TipoImagen.fromString(tipo);
    }

    // ✅ Getter para convertir a String para BD o JSON
    public String getTipoImagenString() {
        return tipoImagen != null ? tipoImagen.getValor() : null;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getOrden() {
        return orden;
    }

    public void setOrden(Integer orden) {
        this.orden = orden;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    @Override
    public String toString() {
        return "ImagenProyecto{" +
                "id=" + id +
                ", proyectoId=" + proyectoId +
                ", tipoImagen=" + tipoImagen +
                ", orden=" + orden +
                '}';
    }
}
