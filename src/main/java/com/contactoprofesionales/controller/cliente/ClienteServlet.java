package com.contactoprofesionales.controller.cliente;

//import com.contactoprofesionales.dao.cliente.ClienteDAOImpl;
import com.contactoprofesionales.dto.ClienteDTO;
import com.contactoprofesionales.dto.ClienteRegistroRequest;
import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.service.cliente.ClienteService;
import com.contactoprofesionales.service.cliente.ClienteServiceImpl;
import com.contactoprofesionales.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servlet para gestionar las operaciones del perfil de cliente
 * Endpoints:
 * POST   /api/clientes - Registrar nuevo cliente
 * GET    /api/clientes/{id} - Obtener perfil de cliente
 * PUT    /api/clientes/{id} - Actualizar perfil de cliente
 * DELETE /api/clientes/{id} - Desactivar cliente
 * GET    /api/clientes - Listar todos los clientes activos
 */
@WebServlet(name = "ClienteServlet", urlPatterns = {"/api/clientes", "/api/clientes/*"})
public class ClienteServlet extends HttpServlet {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteServlet.class);
    private static final long serialVersionUID = 1L;    
    
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();
    
    private ClienteService clienteService;
    //private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando ClienteServlet ===");
        this.clienteService = new ClienteServiceImpl();
        //this.gson = new Gson();
    }
    
    /**
     * Configurar headers CORS para todas las peticiones
     */
    private void setCORSHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
    
    /**
     * Manejar peticiones OPTIONS (preflight CORS)
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        setCORSHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }
    
    /**
     * GET - Listar todos los clientes o obtener uno específico por ID o email
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        String email = request.getParameter("email");
        
        logger.info("=== GET /api/clientes - PathInfo: {}, Email param: {}", pathInfo, email);

        try {
            // GET /api/clientes?email={email} - Buscar cliente por email
            if (email != null && !email.trim().isEmpty()) {
                logger.debug("Buscando cliente por email: {}", email);
                try {
                    ClienteDTO cliente = clienteService.buscarPorEmail(email);
                    logger.info("Cliente encontrado con email: {}", email);
                    sendSuccessResponse(response, HttpServletResponse.SC_OK, cliente);
                } catch (ClienteException e) {
                    // Si no encuentra el cliente, retornar respuesta indicando que no existe
                    if ("NO_ENCONTRADO".equals(e.getCodigo())) {
                        logger.info("No se encontró cliente con email: {}", email);
                        Map<String, Object> responseData = new HashMap<>();
                        responseData.put("encontrado", false);
                        responseData.put("mensaje", "No se encontró un perfil de cliente asociado");
                        sendSuccessResponse(response, HttpServletResponse.SC_OK, responseData);
                    } else {
                        logger.error("Error al buscar cliente por email: {}", e.getMessage());
                        sendErrorResponse(response, e);
                    }
                }
            }
            // GET /api/clientes/{id} - Obtener cliente por ID
            else if (pathInfo != null && !pathInfo.equals("/")) {
                Long id = extractIdFromPath(pathInfo);
                logger.debug("Obteniendo cliente por ID: {}", id);
                ClienteDTO cliente = clienteService.obtenerPerfil(id);
                logger.info("Cliente obtenido exitosamente: ID {}", id);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, cliente);
            }
            // GET /api/clientes - Listar todos los clientes activos
            else {
                logger.debug("Listando todos los clientes activos");
                List<ClienteDTO> clientes = clienteService.listarClientesActivos();
                logger.info("Se encontraron {} clientes activos", clientes.size());
                sendSuccessResponse(response, HttpServletResponse.SC_OK, clientes);
            }

        } catch (ClienteException e) {
            logger.error("ClienteException en GET: {}", e.getMessage(), e);
            sendErrorResponse(response, e);
        } catch (NumberFormatException e) {
            logger.error("ID inválido en GET: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                "ID_INVALIDO", "El ID proporcionado no es válido");
        } catch (Exception e) {
            logger.error("Error inesperado en GET: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }
    
    /**
     * POST - Registrar nuevo cliente
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("=== Iniciando POST /api/clientes - Registro de Cliente ===");
        
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Leer el cuerpo de la petición
            String jsonBody = leerCuerpoRequest(request);
            logger.debug("JSON recibido: {}", jsonBody);
            
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("Cuerpo de la petición vacío");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "BODY_VACIO", "El cuerpo de la petición está vacío");
                return;
            }
            
            // Parsear JSON a objeto
            ClienteRegistroRequest registroRequest = gson.fromJson(jsonBody, ClienteRegistroRequest.class);
            logger.info("JSON parseado correctamente a ClienteRegistroRequest");
            logger.debug("Datos del cliente: nombre={}, email={}, telefono={}", 
                registroRequest.getNombreCompleto(), 
                registroRequest.getEmail(), 
                registroRequest.getTelefono());
            
            // Registrar cliente
            ClienteDTO clienteRegistrado = clienteService.registrarCliente(registroRequest);
            logger.info("✓ Cliente registrado exitosamente con ID: {}", clienteRegistrado.getId());
            
            sendSuccessResponse(response, HttpServletResponse.SC_CREATED, clienteRegistrado);
            
        } catch (JsonSyntaxException e) {
            logger.error("Error al parsear JSON: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "JSON_INVALIDO", "El formato del JSON no es válido: " + e.getMessage());
        } catch (ClienteException e) {
            logger.error("ClienteException al registrar: {}", e.getMessage(), e);
            sendErrorResponse(response, e);
        } catch (Exception e) {
            logger.error("Error inesperado al registrar cliente: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }
    
    /**
     * PUT - Actualizar perfil de cliente
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("=== Iniciando PUT /api/clientes - Actualización de Cliente ===");
        
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                logger.error("ID no proporcionado en PUT");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "ID_REQUERIDO", "Se requiere el ID del cliente en la URL");
                return;
            }
            
            Long id = extractIdFromPath(pathInfo);
            logger.info("Actualizando cliente con ID: {}", id);
            
            // Leer el cuerpo de la petición
            String jsonBody = leerCuerpoRequest(request);
            logger.debug("JSON recibido: {}", jsonBody);
            
            if (jsonBody == null || jsonBody.trim().isEmpty()) {
                logger.error("Cuerpo de la petición vacío");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "BODY_VACIO", "El cuerpo de la petición está vacío");
                return;
            }
            
            // Parsear JSON a objeto
            ClienteRegistroRequest updateRequest = gson.fromJson(jsonBody, ClienteRegistroRequest.class);
            logger.info("JSON parseado correctamente");
            
            // Actualizar cliente
            ClienteDTO clienteActualizado = clienteService.actualizarPerfil(id, updateRequest);
            logger.info("✓ Cliente actualizado exitosamente: ID {}", id);
            
            sendSuccessResponse(response, HttpServletResponse.SC_OK, clienteActualizado);
            
        } catch (NumberFormatException e) {
            logger.error("ID inválido en PUT: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "ID_INVALIDO", "El ID proporcionado no es válido");
        } catch (JsonSyntaxException e) {
            logger.error("Error al parsear JSON en PUT: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "JSON_INVALIDO", "El formato del JSON no es válido: " + e.getMessage());
        } catch (ClienteException e) {
            logger.error("ClienteException en PUT: {}", e.getMessage(), e);
            sendErrorResponse(response, e);
        } catch (Exception e) {
            logger.error("Error inesperado en PUT: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }
    
    /**
     * DELETE - Desactivar cliente (borrado lógico)
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        logger.info("=== Iniciando DELETE /api/clientes - Desactivar Cliente ===");
        
        setCORSHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                logger.error("ID no proporcionado en DELETE");
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "ID_REQUERIDO", "Se requiere el ID del cliente en la URL");
                return;
            }
            
            Long id = extractIdFromPath(pathInfo);
            logger.info("Desactivando cliente con ID: {}", id);
            
            // Desactivar cliente
            boolean desactivado = clienteService.desactivarCliente(id);
            
            if (desactivado) {
                logger.info("✓ Cliente desactivado exitosamente: ID {}", id);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("mensaje", "Cliente desactivado exitosamente");
                responseData.put("id", id);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, responseData);
            } else {
                logger.error("No se pudo desactivar el cliente ID: {}", id);
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "ERROR_DESACTIVAR", "No se pudo desactivar el cliente");
            }
            
        } catch (NumberFormatException e) {
            logger.error("ID inválido en DELETE: {}", e.getMessage());
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "ID_INVALIDO", "El ID proporcionado no es válido");
        } catch (ClienteException e) {
            logger.error("ClienteException en DELETE: {}", e.getMessage(), e);
            sendErrorResponse(response, e);
        } catch (Exception e) {
            logger.error("Error inesperado en DELETE: {}", e.getMessage(), e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "ERROR_SERVIDOR", "Error interno del servidor: " + e.getMessage());
        }
    }
    
    /**
     * Extrae el ID de la ruta
     */
    private Long extractIdFromPath(String pathInfo) {
        String[] parts = pathInfo.split("/");
        return Long.parseLong(parts[parts.length - 1]);
    }
    
    /**
     * Lee el cuerpo de la petición como String
     */
    private String leerCuerpoRequest(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
    
    /**
     * Envía una respuesta exitosa
     */
    private void sendSuccessResponse(HttpServletResponse response, int statusCode, Object data) 
            throws IOException {
        response.setStatus(statusCode);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", true);
        responseBody.put("data", data);
        
        String jsonResponse = gson.toJson(responseBody);
        logger.debug("Enviando respuesta exitosa: {}", jsonResponse);
        
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
    
    /**
     * Envía una respuesta de error desde ClienteException
     */
    private void sendErrorResponse(HttpServletResponse response, ClienteException e) 
            throws IOException {
        
        int statusCode;
        
        // Mapear código de error a HTTP status code
        switch (e.getCodigo() != null ? e.getCodigo() : "") {
            case "NO_ENCONTRADO":
                statusCode = HttpServletResponse.SC_NOT_FOUND;
                break;
            case "VALIDACION_ERROR":
            case "LIMITE_DIRECCIONES":
            case "ID_INVALIDO":
                statusCode = HttpServletResponse.SC_BAD_REQUEST;
                break;
            case "EMAIL_DUPLICADO":
            case "TELEFONO_DUPLICADO":
            case "DUPLICADO":
                statusCode = HttpServletResponse.SC_CONFLICT;
                break;
            default:
                statusCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }
        
        sendErrorResponse(response, statusCode, e.getCodigo(), e.getMessage());
    }
    
    /**
     * Envía una respuesta de error genérica
     */
    private void sendErrorResponse(HttpServletResponse response, int statusCode, 
                                   String codigo, String mensaje) throws IOException {
        response.setStatus(statusCode);
        
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("success", false);
        
        Map<String, String> error = new HashMap<>();
        error.put("codigo", codigo);
        error.put("mensaje", mensaje);
        
        responseBody.put("error", error);
        
        String jsonResponse = gson.toJson(responseBody);
        logger.debug("Enviando respuesta de error: {}", jsonResponse);
        
        try (PrintWriter out = response.getWriter()) {
            out.print(jsonResponse);
            out.flush();
        }
    }
}