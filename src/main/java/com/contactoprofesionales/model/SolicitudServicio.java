package com.contactoprofesionales.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modelo para representar una solicitud de servicio.
 * Corresponde a la tabla 'solicitudes_servicio' en la BD.
 *
 * ACTUALIZADO 2025-12-03:
 * - Agregada validación de transiciones de estado
 * - Agregado método getEstadosDisponibles()
 */
public class SolicitudServicio {

    private static final Logger logger = LoggerFactory.getLogger(SolicitudServicio.class);
    
    private Integer id;
    private Integer clienteId;
    private Integer profesionalId;
    private String descripcion;
    private Double presupuestoEstimado;
    private String direccion;
    private String distrito;
    private String codigoPostal;
    private String referencia;

    // NUEVOS CAMPOS - Migración V008: Ubicación estructurada y modalidad
    /**
     * ID del departamento (ubicación geográfica estructurada).
     * NULL si es trabajo remoto.
     */
    private Integer departamentoId;

    /**
     * ID de la provincia (ubicación geográfica estructurada).
     * NULL si es trabajo remoto.
     */
    private Integer provinciaId;

    /**
     * ID del distrito (ubicación geográfica estructurada).
     * NULL si es trabajo remoto. Reemplaza al campo distrito (texto).
     */
    private Integer distritoId;

    /**
     * Modalidad del servicio: "REMOTO" (virtual) o "PRESENCIAL" (en domicilio).
     * Determina si se requiere dirección física.
     */
    private String tipoPrestacion;

    /**
     * FK a especialidad específica solicitada del profesional.
     * Relaciona con especialidades_profesional.id.
     */
    private Integer especialidadId;

    private LocalDateTime fechaServicio;
    private String urgencia; // 'normal' o 'urgent'
    private String notasAdicionales;
    private List<String> fotosUrls;
    private String estado; // 'pendiente', 'aceptada', 'rechazada', 'completada', 'cancelada'
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private LocalDateTime fechaActualizacion;
    private boolean activo;

    // Constructores
    public SolicitudServicio() {
        this.fechaSolicitud = LocalDateTime.now();
        this.activo = true;
        this.estado = "pendiente";
    }

    public SolicitudServicio(Integer clienteId, Integer profesionalId, String descripcion,
                            Double presupuestoEstimado, String direccion, String distrito) {
        this();
        this.clienteId = clienteId;
        this.profesionalId = profesionalId;
        this.descripcion = descripcion;
        this.presupuestoEstimado = presupuestoEstimado;
        this.direccion = direccion;
        this.distrito = distrito;
    }

    // Getters y Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getClienteId() {
        return clienteId;
    }

    public void setClienteId(Integer clienteId) {
        this.clienteId = clienteId;
    }

    public Integer getProfesionalId() {
        return profesionalId;
    }

    public void setProfesionalId(Integer profesionalId) {
        this.profesionalId = profesionalId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getPresupuestoEstimado() {
        return presupuestoEstimado;
    }

    public void setPresupuestoEstimado(Double presupuestoEstimado) {
        this.presupuestoEstimado = presupuestoEstimado;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    // NUEVOS GETTERS Y SETTERS - Migración V008

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

    public String getTipoPrestacion() {
        return tipoPrestacion;
    }

    public void setTipoPrestacion(String tipoPrestacion) {
        this.tipoPrestacion = tipoPrestacion;
    }

    public Integer getEspecialidadId() {
        return especialidadId;
    }

    public void setEspecialidadId(Integer especialidadId) {
        this.especialidadId = especialidadId;
    }

    public LocalDateTime getFechaServicio() {
        return fechaServicio;
    }

    public void setFechaServicio(LocalDateTime fechaServicio) {
        this.fechaServicio = fechaServicio;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public String getNotasAdicionales() {
        return notasAdicionales;
    }

    public void setNotasAdicionales(String notasAdicionales) {
        this.notasAdicionales = notasAdicionales;
    }

    public List<String> getFotosUrls() {
        return fotosUrls;
    }

    public void setFotosUrls(List<String> fotosUrls) {
        this.fotosUrls = fotosUrls;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }

    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    // Métodos de negocio
    public boolean isPendiente() {
        return "pendiente".equals(this.estado);
    }

    public boolean isAceptada() {
        return "aceptada".equals(this.estado);
    }

    public boolean isRechazada() {
        return "rechazada".equals(this.estado);
    }

    public boolean puedeSerCancelada() {
        return isPendiente() || isAceptada();
    }

    public boolean isUrgente() {
        return "urgent".equals(this.urgencia);
    }

    /**
     * Valida si es posible cambiar al estado especificado desde el estado actual.
     *
     * REGLAS DE TRANSICIÓN:
     * - PENDIENTE → ACEPTADA o RECHAZADA
     * - ACEPTADA → COMPLETADA o CANCELADA
     * - Otros estados son finales (no permiten cambios)
     *
     * @param nuevoEstado Estado al que se desea cambiar
     * @return true si la transición es válida, false en caso contrario
     */
    public boolean puedeTransicionarA(String nuevoEstado) {
        // Log para debugging
        logger.trace("Validando transición: {} → {}", this.estado, nuevoEstado);

        // No se puede transicionar al mismo estado
        if (this.estado.equals(nuevoEstado)) {
            logger.debug("Transición rechazada: estado ya es {}", nuevoEstado);
            return false;
        }

        // Definir transiciones válidas según estado actual
        switch (this.estado.toLowerCase()) {
            case "pendiente":
                // Desde PENDIENTE solo se puede ir a ACEPTADA o RECHAZADA
                boolean validaDesdePendiente = "aceptada".equalsIgnoreCase(nuevoEstado) ||
                                               "rechazada".equalsIgnoreCase(nuevoEstado);
                logger.debug("Transición desde PENDIENTE a {}: {}", nuevoEstado,
                            validaDesdePendiente ? "VÁLIDA" : "INVÁLIDA");
                return validaDesdePendiente;

            case "aceptada":
                // Desde ACEPTADA solo se puede ir a COMPLETADA o CANCELADA
                boolean validaDesdeAceptada = "completada".equalsIgnoreCase(nuevoEstado) ||
                                              "cancelada".equalsIgnoreCase(nuevoEstado);
                logger.debug("Transición desde ACEPTADA a {}: {}", nuevoEstado,
                            validaDesdeAceptada ? "VÁLIDA" : "INVÁLIDA");
                return validaDesdeAceptada;

            case "rechazada":
            case "completada":
            case "cancelada":
                // Estados finales: no permiten transiciones
                logger.debug("Transición rechazada: {} es un estado final", this.estado);
                return false;

            default:
                // Estado desconocido
                logger.warn("Estado desconocido: {}", this.estado);
                return false;
        }
    }

    /**
     * Obtiene los estados a los que se puede transicionar desde el estado actual.
     *
     * @return Lista de estados válidos para transición
     */
    public List<String> getEstadosDisponibles() {
        List<String> estados = new ArrayList<>();

        switch (this.estado.toLowerCase()) {
            case "pendiente":
                estados.add("ACEPTADA");
                estados.add("RECHAZADA");
                break;

            case "aceptada":
                estados.add("COMPLETADA");
                estados.add("CANCELADA");
                break;

            case "rechazada":
            case "completada":
            case "cancelada":
                // Estados finales: sin transiciones disponibles
                break;

            default:
                logger.warn("Estado desconocido al obtener estados disponibles: {}", this.estado);
        }

        logger.debug("Estados disponibles desde {}: {}", this.estado, estados);
        return estados;
    }

    @Override
    public String toString() {
        return "SolicitudServicio{" +
                "id=" + id +
                ", clienteId=" + clienteId +
                ", profesionalId=" + profesionalId +
                ", descripcion='" + descripcion + '\'' +
                ", presupuestoEstimado=" + presupuestoEstimado +
                ", distrito='" + distrito + '\'' +
                ", fechaServicio=" + fechaServicio +
                ", urgencia='" + urgencia + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaSolicitud=" + fechaSolicitud +
                '}';
    }
}
