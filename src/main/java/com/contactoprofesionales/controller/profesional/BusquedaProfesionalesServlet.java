package com.contactoprofesionales.controller.profesional;

import com.contactoprofesionales.dao.profesional.ProfesionalDAO;
import com.contactoprofesionales.dao.profesional.ProfesionalDAOImpl;
import com.contactoprofesionales.dto.BusquedaCriteriosDTO;
import com.contactoprofesionales.dto.ProfesionalBusquedaDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.service.profesional.BusquedaProfesionalesService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet para gestionar la búsqueda de profesionales.
 * 
 * Endpoints:
 * - GET /api/buscar-profesionales : Busca profesionales con filtros
 * - GET /api/especialidades : Obtiene lista de especialidades disponibles
 * - GET /api/distritos : Obtiene lista de distritos disponibles
 */
@WebServlet(name = "BusquedaProfesionalesServlet", urlPatterns = {
    "/api/buscar-profesionales",
    "/api/especialidades",
    "/api/distritos"
})
public class BusquedaProfesionalesServlet extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(BusquedaProfesionalesServlet.class);
    
    private BusquedaProfesionalesService busquedaService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        
        // Inicializar dependencias
        ProfesionalDAO profesionalDAO = new ProfesionalDAOImpl();
        this.busquedaService = new BusquedaProfesionalesService(profesionalDAO);
        
        // Configurar Gson
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setPrettyPrinting()
                .create();
        
        logger.info("✓ BusquedaProfesionalesServlet inicializado correctamente");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getServletPath();
        logger.debug("Procesando petición GET: {}", pathInfo);
        
        // Configurar respuesta JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            switch (pathInfo) {
                case "/api/buscar-profesionales":
                    buscarProfesionales(request, response);
                    break;
                    
                case "/api/especialidades":
                    obtenerEspecialidades(response);
                    break;
                    
                case "/api/distritos":
                    obtenerDistritos(response);
                    break;
                    
                default:
                    enviarError(response, HttpServletResponse.SC_NOT_FOUND, 
                               "Endpoint no encontrado");
            }
            
        } catch (Exception e) {
            logger.error("Error al procesar petición: {}", e.getMessage(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                       "Error interno del servidor: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la búsqueda de profesionales.
     */
    private void buscarProfesionales(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        
        try {
            
        	logger.info("=== INICIO BÚSQUEDA ===");
            logger.info("Request URI: {}", request.getRequestURI());
            logger.info("Query String: {}", request.getQueryString());
            
        	// Extraer criterios de búsqueda de los parámetros
            BusquedaCriteriosDTO criterios = extraerCriteriosBusqueda(request);
            logger.info("Criterios extraídos: {}", criterios);
            
            logger.info("Búsqueda solicitada con criterios: {}", criterios);
            
            // Realizar búsqueda
            List<ProfesionalBusquedaDTO> resultados = busquedaService.buscarProfesionales(criterios);
            logger.info("Resultados obtenidos: {}", resultados.size());
            
            // Obtener total de resultados (sin paginación)
            int totalResultados = busquedaService.contarResultados(criterios);
            
            // Calcular información de paginación
            int totalPaginas = busquedaService.calcularTotalPaginas(
                totalResultados, 
                criterios.getElementosPorPagina()
            );
            
            // Construir respuesta
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("profesionales", resultados);
            respuesta.put("total", totalResultados);
            respuesta.put("pagina", criterios.getPagina());
            respuesta.put("elementosPorPagina", criterios.getElementosPorPagina());
            respuesta.put("totalPaginas", totalPaginas);
            respuesta.put("criterios", criterios.getDescripcion());
            
            // Enviar respuesta JSON
            enviarRespuestaExitosa(response, respuesta);
            
            logger.info("✓ Búsqueda completada. Devolviendo {} resultados de {}", 
                       resultados.size(), totalResultados);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Criterios de búsqueda inválidos: {}", e.getMessage());
            enviarError(response, HttpServletResponse.SC_BAD_REQUEST, 
                       "Criterios de búsqueda inválidos: " + e.getMessage());
            
        } catch (DatabaseException e) {
            logger.error("Error de base de datos al buscar profesionales: {}", e.getMessage(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                       "Error al buscar profesionales en la base de datos");
        
        } catch (Exception e) {
            logger.error("❌ Exception general: {}", e.getMessage(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                       "Error general: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene la lista de especialidades disponibles.
     */
    private void obtenerEspecialidades(HttpServletResponse response) throws IOException {
        try {
            List<String> especialidades = busquedaService.obtenerEspecialidadesDisponibles();
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("especialidades", especialidades);
            respuesta.put("total", especialidades.size());
            
            enviarRespuestaExitosa(response, respuesta);
            
            logger.info("✓ Devolviendo {} especialidades", especialidades.size());
            
        } catch (DatabaseException e) {
            logger.error("Error al obtener especialidades: {}", e.getMessage(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                       "Error al obtener especialidades");
        }
    }
    
    /**
     * Obtiene la lista de distritos disponibles.
     */
    private void obtenerDistritos(HttpServletResponse response) throws IOException {
        try {
            List<String> distritos = busquedaService.obtenerDistritosDisponibles();
            
            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("distritos", distritos);
            respuesta.put("total", distritos.size());
            
            enviarRespuestaExitosa(response, respuesta);
            
            logger.info("✓ Devolviendo {} distritos", distritos.size());
            
        } catch (DatabaseException e) {
            logger.error("Error al obtener distritos: {}", e.getMessage(), e);
            enviarError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                       "Error al obtener distritos");
        }
    }
    
    /**
     * Extrae los criterios de búsqueda de los parámetros de la petición.
     */
    private BusquedaCriteriosDTO extraerCriteriosBusqueda(HttpServletRequest request) {
        BusquedaCriteriosDTO criterios = new BusquedaCriteriosDTO();
        
        // Especialidad
        String especialidad = request.getParameter("especialidad");
        criterios.setEspecialidad(especialidad);
        
        // Distrito
        String distrito = request.getParameter("distrito");
        criterios.setDistrito(distrito);
        
        // Calificación mínima
        String calificacionMinStr = request.getParameter("calificacionMin");
        if (calificacionMinStr != null && !calificacionMinStr.isEmpty()) {
            try {
                Double calificacionMin = Double.parseDouble(calificacionMinStr);
                criterios.setCalificacionMinima(calificacionMin);
            } catch (NumberFormatException e) {
                logger.warn("Formato inválido para calificacionMin: {}", calificacionMinStr);
            }
        }
        
        // Tarifa máxima
        String tarifaMaxStr = request.getParameter("tarifaMax");
        if (tarifaMaxStr != null && !tarifaMaxStr.isEmpty()) {
            try {
                Double tarifaMax = Double.parseDouble(tarifaMaxStr);
                criterios.setTarifaMaxima(tarifaMax);
            } catch (NumberFormatException e) {
                logger.warn("Formato inválido para tarifaMax: {}", tarifaMaxStr);
            }
        }
        
        // Disponibilidad
        String disponibleStr = request.getParameter("disponible");
        if (disponibleStr != null && !disponibleStr.isEmpty()) {
            criterios.setDisponible(Boolean.parseBoolean(disponibleStr));
        }
        
        // Ordenamiento
        String ordenarPor = request.getParameter("ordenarPor");
        if (ordenarPor != null && !ordenarPor.isEmpty()) {
            criterios.setOrdenarPor(ordenarPor);
        }
        
        String ordenDireccion = request.getParameter("ordenDireccion");
        if (ordenDireccion != null && !ordenDireccion.isEmpty()) {
            criterios.setOrdenDireccion(ordenDireccion);
        }
        
        // Paginación
        String paginaStr = request.getParameter("pagina");
        if (paginaStr != null && !paginaStr.isEmpty()) {
            try {
                Integer pagina = Integer.parseInt(paginaStr);
                criterios.setPagina(pagina);
            } catch (NumberFormatException e) {
                logger.warn("Formato inválido para pagina: {}", paginaStr);
            }
        }
        
        String elementosPorPaginaStr = request.getParameter("elementosPorPagina");
        if (elementosPorPaginaStr != null && !elementosPorPaginaStr.isEmpty()) {
            try {
                Integer elementosPorPagina = Integer.parseInt(elementosPorPaginaStr);
                criterios.setElementosPorPagina(elementosPorPagina);
            } catch (NumberFormatException e) {
                logger.warn("Formato inválido para elementosPorPagina: {}", elementosPorPaginaStr);
            }
        }
        
        return criterios;
    }
    
    /**
     * Envía una respuesta exitosa en formato JSON.
     */
    private void enviarRespuestaExitosa(HttpServletResponse response, Object data) 
            throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(data));
        out.flush();
    }
    
    /**
     * Envía una respuesta de error en formato JSON.
     */
    private void enviarError(HttpServletResponse response, int statusCode, String mensaje) 
            throws IOException {
        
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("error", mensaje);
        error.put("statusCode", statusCode);
        
        response.setStatus(statusCode);
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(error));
        out.flush();
    }
    
    @Override
    public void destroy() {
        logger.info("Destruyendo BusquedaProfesionalesServlet");
        super.destroy();
    }
}
