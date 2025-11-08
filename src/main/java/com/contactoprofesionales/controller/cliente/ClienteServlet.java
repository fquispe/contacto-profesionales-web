package com.contactoprofesionales.controller.cliente;

import com.contactoprofesionales.dto.ClienteDTO;
import com.contactoprofesionales.dto.ClienteRegistroRequest;
import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.service.cliente.ClienteService;
import com.contactoprofesionales.service.cliente.ClienteServiceImpl;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
    
	private static final long serialVersionUID = 1L;
	
	private ClienteService clienteService;
    private Gson gson;
    
    @Override
    public void init() throws ServletException {
        super.init();
        this.clienteService = new ClienteServiceImpl();
        this.gson = new Gson();
    }
    
    /**
     * POST - Registrar nuevo cliente
     * GET - Listar todos los clientes o obtener uno específico por ID
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try {
            // GET /api/clientes/{id} - Obtener cliente por ID
            if (pathInfo != null && !pathInfo.equals("/")) {
                Long id = extractIdFromPath(pathInfo);
                ClienteDTO cliente = clienteService.obtenerPerfil(id);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, cliente);
            } 
            // GET /api/clientes - Listar todos los clientes activos
            else {
                List<ClienteDTO> clientes = clienteService.listarClientesActivos();
                sendSuccessResponse(response, HttpServletResponse.SC_OK, clientes);
            }
            
        } catch (ClienteException e) {
            sendErrorResponse(response, e);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "ID_INVALIDO", "El ID proporcionado no es válido");
        }
    }
    
    /**
     * POST - Registrar nuevo cliente
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // Leer el cuerpo de la petición
            ClienteRegistroRequest registroRequest = leerRequestBody(request, ClienteRegistroRequest.class);
            
            // Registrar cliente
            ClienteDTO clienteRegistrado = clienteService.registrarCliente(registroRequest);
            
            sendSuccessResponse(response, HttpServletResponse.SC_CREATED, clienteRegistrado);
            
        } catch (ClienteException e) {
            sendErrorResponse(response, e);
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "JSON_INVALIDO", "El formato del JSON no es válido");
        }
    }
    
    /**
     * PUT - Actualizar perfil de cliente
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "ID_REQUERIDO", "Se requiere el ID del cliente en la URL");
                return;
            }
            
            Long id = extractIdFromPath(pathInfo);
            
            // Leer el cuerpo de la petición
            ClienteRegistroRequest updateRequest = leerRequestBody(request, ClienteRegistroRequest.class);
            
            // Actualizar cliente
            ClienteDTO clienteActualizado = clienteService.actualizarPerfil(id, updateRequest);
            
            sendSuccessResponse(response, HttpServletResponse.SC_OK, clienteActualizado);
            
        } catch (ClienteException e) {
            sendErrorResponse(response, e);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "ID_INVALIDO", "El ID proporcionado no es válido");
        } catch (JsonSyntaxException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "JSON_INVALIDO", "El formato del JSON no es válido");
        }
    }
    
    /**
     * DELETE - Desactivar cliente (borrado lógico)
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String pathInfo = request.getPathInfo();
        
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                    "ID_REQUERIDO", "Se requiere el ID del cliente en la URL");
                return;
            }
            
            Long id = extractIdFromPath(pathInfo);
            
            // Desactivar cliente
            boolean desactivado = clienteService.desactivarCliente(id);
            
            if (desactivado) {
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("mensaje", "Cliente desactivado exitosamente");
                responseData.put("id", id);
                sendSuccessResponse(response, HttpServletResponse.SC_OK, responseData);
            } else {
                sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "ERROR_DESACTIVAR", "No se pudo desactivar el cliente");
            }
            
        } catch (ClienteException e) {
            sendErrorResponse(response, e);
        } catch (NumberFormatException e) {
            sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, 
                "ID_INVALIDO", "El ID proporcionado no es válido");
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
     * Lee el cuerpo de la petición y lo convierte a un objeto
     */
    private <T> T leerRequestBody(HttpServletRequest request, Class<T> clazz) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        
        return gson.fromJson(sb.toString(), clazz);
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
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(responseBody));
        out.flush();
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
        
        PrintWriter out = response.getWriter();
        out.print(gson.toJson(responseBody));
        out.flush();
    }
}
