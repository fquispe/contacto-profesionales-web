package com.contactoprofesionales.controller.auth;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.dto.LoginRequest;
import com.contactoprofesionales.dto.LoginResponse;
import com.contactoprofesionales.dto.UsuarioDTO;
import com.contactoprofesionales.service.auth.AutenticacionService;
import com.contactoprofesionales.service.auth.AutenticacionServiceImpl;
import com.contactoprofesionales.service.auth.TokenService;
import com.contactoprofesionales.service.auth.TokenServiceImpl;
import com.contactoprofesionales.util.JsonResponse;
import com.contactoprofesionales.exception.AuthenticationException;
import com.contactoprofesionales.exception.DatabaseException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Servlet para manejar el login de usuarios.
 * 
 * Endpoint: POST /api/login
 * 
 * Request Body:
 * {
 *   "email": "usuario@example.com",
 *   "password": "password123"
 * }
 * 
 * Response (200 OK):
 * {
 *   "success": true,
 *   "message": "Login exitoso",
 *   "data": {
 *     "token": "eyJhbGciOiJIUzI1NiIs...",
 *     "usuario": {
 *       "id": 1,
 *       "nombre": "Juan Pérez",
 *       "email": "juan@example.com",
 *       "telefono": "555-0001",
 *       "activo": true
 *     },
 *     "expiresIn": 86400000
 *   }
 * }
 * 
 * Response (401 Unauthorized):
 * {
 *   "success": false,
 *   "error": "Credenciales inválidas"
 * }
 * 
 * Aplicación de SRP: Solo maneja peticiones HTTP de login.
 * Aplicación de DIP: Depende de interfaces (Service).
 */
@WebServlet(name = "LoginServlet", urlPatterns = {"/api/login"})
public class LoginServlet extends HttpServlet {
    /**
	 * 
	 */
	private static final long serialVersionUID = 5370883328003365634L;
	private static final Logger logger = LoggerFactory.getLogger(LoginServlet.class);
    private final Gson gson = new Gson();
    
    private AutenticacionService autenticacionService;
    private TokenService tokenService;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando LoginServlet ===");
        
        try {
            this.autenticacionService = new AutenticacionServiceImpl();
            this.tokenService = new TokenServiceImpl();
            logger.info("✓ LoginServlet inicializado correctamente");
        } catch (Exception e) {
            logger.error("✗ Error al inicializar LoginServlet", e);
            throw new ServletException("Error al inicializar LoginServlet", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        long startTime = System.currentTimeMillis();
        logger.info("POST /api/login - Nueva solicitud de login");
        
        // Configurar response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            // 1. Leer y parsear request body
            LoginRequest loginRequest = parseLoginRequest(request);
            logger.debug("Request parseado: {}", loginRequest);
            
            // 2. Autenticar usuario
            Usuario usuario = autenticacionService.autenticar(
                loginRequest.getEmail(), 
                loginRequest.getPassword()
            );
            
            // 3. Generar token JWT
            String token = tokenService.generateToken(usuario);
            
            // 4. Preparar respuesta
            LoginResponse loginResponse = new LoginResponse(
                token,
                convertirAUsuarioDTO(usuario),
                tokenService.getExpirationTime()
            );
            
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", loginResponse.getToken());
            responseData.put("usuario", loginResponse.getUsuario());
            responseData.put("expiresIn", loginResponse.getExpiresIn());
            
            JsonResponse jsonResponse = JsonResponse.success("Login exitoso", responseData);
            
            // 5. Log de éxito
            long duration = System.currentTimeMillis() - startTime;
            logger.info("✓ Login exitoso para: {} (ID: {}) - Tiempo: {}ms", 
                usuario.getEmail(), usuario.getId(), duration);
            
            // 6. Retornar respuesta
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(jsonResponse));
            
        } catch (AuthenticationException e) {
            handleAuthenticationError(response, e, startTime);
            
        } catch (DatabaseException e) {
            handleDatabaseError(response, e, startTime);
            
        } catch (JsonSyntaxException e) {
            handleJsonError(response, e, startTime);
            
        } catch (Exception e) {
            handleInternalError(response, e, startTime);
        }
    }

    /**
     * Parsea el JSON del request a LoginRequest.
     */
    private LoginRequest parseLoginRequest(HttpServletRequest request) 
            throws IOException, JsonSyntaxException {
        
        StringBuilder sb = new StringBuilder();
        String line;
        
        try (BufferedReader reader = request.getReader()) {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        
        String json = sb.toString();
        
        if (json.trim().isEmpty()) {
            throw new JsonSyntaxException("Request body vacío");
        }
        
        return gson.fromJson(json, LoginRequest.class);
    }

    /**
     * Convierte Usuario a UsuarioDTO (sin información sensible).
     */
    private UsuarioDTO convertirAUsuarioDTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getTelefono(),
            usuario.isActivo()
        );
    }

    /**
     * Maneja errores de autenticación (401).
     */
    private void handleAuthenticationError(HttpServletResponse response, 
                                          AuthenticationException e, 
                                          long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.warn("✗ Error de autenticación: {} - Tiempo: {}ms", e.getMessage(), duration);
        
        JsonResponse jsonResponse = JsonResponse.error(e.getMessage());
        
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    /**
     * Maneja errores de base de datos (500).
     */
    private void handleDatabaseError(HttpServletResponse response, 
                                    DatabaseException e, 
                                    long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.error("✗ Error de base de datos - Tiempo: {}ms", duration, e);
        
        JsonResponse jsonResponse = JsonResponse.error(
            "Error del servidor. Por favor intente nuevamente"
        );
        
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    /**
     * Maneja errores de JSON (400).
     */
    private void handleJsonError(HttpServletResponse response, 
                                JsonSyntaxException e, 
                                long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.warn("✗ Error de JSON: {} - Tiempo: {}ms", e.getMessage(), duration);
        
        JsonResponse jsonResponse = JsonResponse.error("Formato de datos inválido");
        
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    /**
     * Maneja errores internos (500).
     */
    private void handleInternalError(HttpServletResponse response, 
                                    Exception e, 
                                    long startTime) throws IOException {
        long duration = System.currentTimeMillis() - startTime;
        logger.error("✗ Error interno - Tiempo: {}ms", duration, e);
        
        JsonResponse jsonResponse = JsonResponse.error("Error interno del servidor");
        
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    @Override
    public void destroy() {
        logger.info("Destruyendo LoginServlet");
        super.destroy();
    }
}
