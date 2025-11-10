package com.contactoprofesionales.controller;

import com.contactoprofesionales.dto.ResponseDTO;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servlet para endpoints de autenticación
 *
 * Endpoints:
 * - GET  /api/auth/me : Obtener información del usuario autenticado
 *
 * @author Sistema de Contacto Profesionales
 * @version 1.0
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/api/auth/me"})
public class AuthServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AuthServlet.class.getName());
    private final Gson gson = new Gson();

    /**
     * GET /api/auth/me
     * Obtiene información del usuario actualmente autenticado
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        logger.info("=== GET /api/auth/me ===");

        try {
            // Obtener sesión
            HttpSession session = request.getSession(false);

            if (session == null) {
                logger.warning("No hay sesión activa");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "No hay sesión activa. Por favor inicie sesión.");
                return;
            }

            // Obtener datos del usuario desde la sesión
            Integer userId = (Integer) session.getAttribute("userId");
            String email = (String) session.getAttribute("email");
            String nombreCompleto = (String) session.getAttribute("nombreCompleto");

            if (userId == null) {
                logger.warning("No hay userId en la sesión");
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Sesión inválida. Por favor inicie sesión nuevamente.");
                return;
            }

            // Obtener información adicional de la sesión (si existe)
            Long usuarioPersonaId = (Long) session.getAttribute("usuarioPersonaId");
            String tipoRol = (String) session.getAttribute("tipoRol");
            Boolean esCliente = (Boolean) session.getAttribute("esCliente");
            Boolean esProfesional = (Boolean) session.getAttribute("esProfesional");
            Long clienteId = (Long) session.getAttribute("clienteId");
            Integer profesionalId = (Integer) session.getAttribute("profesionalId");

            // Construir respuesta con información del usuario
            Map<String, Object> userData = new HashMap<>();
            userData.put("userId", userId);
            userData.put("email", email);
            userData.put("nombreCompleto", nombreCompleto);

            // Información de roles (con valores por defecto)
            userData.put("usuarioPersonaId", usuarioPersonaId);
            userData.put("tipoRol", tipoRol != null ? tipoRol : "CLIENTE");
            userData.put("esCliente", esCliente != null ? esCliente : true);
            userData.put("esProfesional", esProfesional != null ? esProfesional : false);

            // IDs de perfiles específicos
            if (clienteId != null) {
                userData.put("clienteId", clienteId);
            }
            if (profesionalId != null) {
                userData.put("profesionalId", profesionalId);
            }

            logger.info("Usuario autenticado encontrado: userId=" + userId +
                       ", tipoRol=" + userData.get("tipoRol"));

            // Enviar respuesta exitosa
            sendSuccessResponse(response, HttpServletResponse.SC_OK, userData);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error al obtener información del usuario", e);
            sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Error al obtener información del usuario: " + e.getMessage());
        }
    }

    /**
     * Habilitar CORS para desarrollo
     */
    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        setCorsHeaders(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    /**
     * Envía una respuesta exitosa en formato JSON
     */
    private void sendSuccessResponse(HttpServletResponse response, int status, Object data)
            throws IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        ResponseDTO<Object> responseDTO = new ResponseDTO<>(true, "Éxito", data);
        String jsonResponse = gson.toJson(responseDTO);

        logger.info("Respuesta exitosa: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Envía una respuesta de error en formato JSON
     */
    private void sendErrorResponse(HttpServletResponse response, int status, String errorMessage)
            throws IOException {
        setCorsHeaders(response);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status);

        Map<String, Object> errorData = new HashMap<>();
        errorData.put("codigo", status);
        errorData.put("mensaje", errorMessage);

        ResponseDTO<Object> responseDTO = new ResponseDTO<>(false, errorMessage, null, errorData);
        String jsonResponse = gson.toJson(responseDTO);

        logger.warning("Respuesta de error: " + jsonResponse);
        response.getWriter().write(jsonResponse);
    }

    /**
     * Configura los headers CORS
     */
    private void setCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
}
