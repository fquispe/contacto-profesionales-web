package com.contactoprofesionales.dto;

import java.io.Serializable;

/**
 * DTO para encapsular los criterios de búsqueda de profesionales.
 * Permite una búsqueda flexible con múltiples filtros opcionales.
 */
public class BusquedaCriteriosDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // NUEVOS CAMPOS REFACTORIZADOS
    private Integer categoriaId; // ID de categoría seleccionada (categorias_servicio)
    private String especialidadTexto; // Texto libre cuando se selecciona "Otro"

    // CAMPOS EXISTENTES (compatibilidad hacia atrás)
    private String especialidad; // Especialidad seleccionada de la lista

    // CAMPOS COMENTADOS/DEPRECADOS (no se usan en nueva versión)
    // private String distrito;
    // private Double calificacionMinima;

    private Double tarifaMaxima;
    private Boolean disponible;
    private String ordenarPor; // "calificacion", "resenas", "tarifa"
    private String ordenDireccion; // "asc", "desc"
    private Integer pagina;
    private Integer elementosPorPagina;
    
    // Constructor vacío
    public BusquedaCriteriosDTO() {
        this.pagina = 1;
        this.elementosPorPagina = 12;
        this.ordenarPor = "calificacion";
        this.ordenDireccion = "desc";
    }
    
    // Constructor con parámetros principales (ACTUALIZADO)
    public BusquedaCriteriosDTO(Integer categoriaId, String especialidad, String especialidadTexto) {
        this();
        this.categoriaId = categoriaId;
        this.especialidad = especialidad;
        this.especialidadTexto = especialidadTexto;
    }
    
    // Getters y Setters - NUEVOS CAMPOS
    public Integer getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Integer categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getEspecialidadTexto() {
        return especialidadTexto;
    }

    public void setEspecialidadTexto(String especialidadTexto) {
        this.especialidadTexto = especialidadTexto != null && !especialidadTexto.trim().isEmpty()
                                ? especialidadTexto.trim()
                                : null;
    }

    // Getters y Setters - CAMPOS EXISTENTES
    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad != null && !especialidad.trim().isEmpty()
                           ? especialidad.trim()
                           : null;
    }

    // COMENTADO: Métodos de distrito y calificación (no se usan en nueva versión)
    /*
    public String getDistrito() {
        return distrito;
    }

    public void setDistrito(String distrito) {
        this.distrito = distrito != null && !distrito.trim().isEmpty()
                       ? distrito.trim()
                       : null;
    }

    public Double getCalificacionMinima() {
        return calificacionMinima;
    }

    public void setCalificacionMinima(Double calificacionMinima) {
        // Validar rango 0-5
        if (calificacionMinima != null) {
            if (calificacionMinima < 0) {
                this.calificacionMinima = 0.0;
            } else if (calificacionMinima > 5) {
                this.calificacionMinima = 5.0;
            } else {
                this.calificacionMinima = calificacionMinima;
            }
        } else {
            this.calificacionMinima = null;
        }
    }
    */
    
    public Double getTarifaMaxima() {
        return tarifaMaxima;
    }
    
    public void setTarifaMaxima(Double tarifaMaxima) {
        this.tarifaMaxima = tarifaMaxima != null && tarifaMaxima > 0 
                           ? tarifaMaxima 
                           : null;
    }
    
    public Boolean getDisponible() {
        return disponible;
    }
    
    public void setDisponible(Boolean disponible) {
        this.disponible = disponible;
    }
    
    public String getOrdenarPor() {
        return ordenarPor;
    }
    
    public void setOrdenarPor(String ordenarPor) {
        // Validar valores permitidos
        if ("calificacion".equals(ordenarPor) || "resenas".equals(ordenarPor) || "tarifa".equals(ordenarPor)) {
            this.ordenarPor = ordenarPor;
        } else {
            this.ordenarPor = "calificacion"; // Default
        }
    }
    
    public String getOrdenDireccion() {
        return ordenDireccion;
    }
    
    public void setOrdenDireccion(String ordenDireccion) {
        this.ordenDireccion = "asc".equals(ordenDireccion) ? "asc" : "desc";
    }
    
    public Integer getPagina() {
        return pagina;
    }
    
    public void setPagina(Integer pagina) {
        this.pagina = pagina != null && pagina > 0 ? pagina : 1;
    }
    
    public Integer getElementosPorPagina() {
        return elementosPorPagina;
    }
    
    public void setElementosPorPagina(Integer elementosPorPagina) {
        // Limitar entre 6 y 50
        if (elementosPorPagina != null) {
            if (elementosPorPagina < 6) {
                this.elementosPorPagina = 6;
            } else if (elementosPorPagina > 50) {
                this.elementosPorPagina = 50;
            } else {
                this.elementosPorPagina = elementosPorPagina;
            }
        } else {
            this.elementosPorPagina = 12;
        }
    }
    
    /**
     * Calcula el offset para la consulta SQL basado en la paginación.
     */
    public int getOffset() {
        return (pagina - 1) * elementosPorPagina;
    }
    
    /**
     * Verifica si hay algún criterio de búsqueda aplicado.
     * ACTUALIZADO: Incluye nuevos campos categoriaId y especialidadTexto
     */
    public boolean tieneAlgunFiltro() {
        return categoriaId != null ||
               especialidad != null ||
               especialidadTexto != null ||
               tarifaMaxima != null ||
               disponible != null;
    }

    /**
     * Obtiene una descripción legible de los criterios de búsqueda.
     * ACTUALIZADO: Incluye nuevos campos categoriaId y especialidadTexto
     */
    public String getDescripcion() {
        StringBuilder desc = new StringBuilder();

        if (categoriaId != null) {
            desc.append("Categoría ID: ").append(categoriaId);
        }

        if (especialidad != null) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("Especialidad: ").append(especialidad);
        }

        if (especialidadTexto != null) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("Búsqueda libre: ").append(especialidadTexto);
        }

        if (tarifaMaxima != null) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("Tarifa máxima: S/ ").append(tarifaMaxima);
        }

        if (disponible != null) {
            if (desc.length() > 0) desc.append(", ");
            desc.append("Solo disponibles");
        }

        return desc.length() > 0 ? desc.toString() : "Sin filtros";
    }

    @Override
    public String toString() {
        return "BusquedaCriteriosDTO{" +
                "categoriaId=" + categoriaId +
                ", especialidad='" + especialidad + '\'' +
                ", especialidadTexto='" + especialidadTexto + '\'' +
                ", tarifaMaxima=" + tarifaMaxima +
                ", disponible=" + disponible +
                ", ordenarPor='" + ordenarPor + '\'' +
                ", ordenDireccion='" + ordenDireccion + '\'' +
                ", pagina=" + pagina +
                ", elementosPorPagina=" + elementosPorPagina +
                '}';
    }
}
