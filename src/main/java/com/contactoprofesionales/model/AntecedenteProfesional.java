package com.contactoprofesionales.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo para representar antecedentes policiales, penales o judiciales de un profesional.
 *
 * Los antecedentes son OPCIONALES pero mejoran significativamente la puntuación del profesional.
 * Un profesional con antecedentes verificados inspira más confianza.
 *
 * Tipos de antecedentes:
 * - POLICIAL: Antecedentes policiales
 * - PENAL: Antecedentes penales
 * - JUDICIAL: Antecedentes judiciales
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class AntecedenteProfesional {

    private Integer id;
    private Integer profesionalId;
    private TipoAntecedente tipoAntecedente;
    private String documentoUrl;
    private LocalDate fechaEmision;
    private LocalDateTime fechaSubida;
    private Boolean verificado; // ✅ Verificado por administrador
    private LocalDateTime fechaVerificacion;
    private String observaciones;
    private Boolean activo;

    /**
     * Enum para tipos de antecedente
     */
    public enum TipoAntecedente {
        POLICIAL("policial"),
        PENAL("penal"),
        JUDICIAL("judicial");

        private final String valor;

        TipoAntecedente(String valor) {
            this.valor = valor;
        }

        public String getValor() {
            return valor;
        }

        public static TipoAntecedente fromString(String valor) {
            for (TipoAntecedente tipo : TipoAntecedente.values()) {
                if (tipo.valor.equalsIgnoreCase(valor)) {
                    return tipo;
                }
            }
            throw new IllegalArgumentException("Tipo de antecedente no válido: " + valor);
        }
    }

    // Constructor vacío
    public AntecedenteProfesional() {
        this.verificado = false;
        this.activo = true;
    }

    // Constructor básico
    public AntecedenteProfesional(Integer profesionalId, TipoAntecedente tipoAntecedente, String documentoUrl) {
        this.profesionalId = profesionalId;
        this.tipoAntecedente = tipoAntecedente;
        this.documentoUrl = documentoUrl;
        this.verificado = false;
        this.activo = true;
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

    public TipoAntecedente getTipoAntecedente() {
        return tipoAntecedente;
    }

    public void setTipoAntecedente(TipoAntecedente tipoAntecedente) {
        this.tipoAntecedente = tipoAntecedente;
    }

    // ✅ Setter para recibir String desde BD o JSON
    public void setTipoAntecedenteString(String tipo) {
        this.tipoAntecedente = TipoAntecedente.fromString(tipo);
    }

    // ✅ Getter para convertir a String para BD o JSON
    public String getTipoAntecedenteString() {
        return tipoAntecedente != null ? tipoAntecedente.getValor() : null;
    }

    public String getDocumentoUrl() {
        return documentoUrl;
    }

    public void setDocumentoUrl(String documentoUrl) {
        this.documentoUrl = documentoUrl;
    }

    public LocalDate getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDate fechaEmision) {
        this.fechaEmision = fechaEmision;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public Boolean getVerificado() {
        return verificado;
    }

    public void setVerificado(Boolean verificado) {
        this.verificado = verificado;
    }

    public LocalDateTime getFechaVerificacion() {
        return fechaVerificacion;
    }

    public void setFechaVerificacion(LocalDateTime fechaVerificacion) {
        this.fechaVerificacion = fechaVerificacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "AntecedenteProfesional{" +
                "id=" + id +
                ", profesionalId=" + profesionalId +
                ", tipoAntecedente=" + tipoAntecedente +
                ", verificado=" + verificado +
                ", activo=" + activo +
                '}';
    }
}
