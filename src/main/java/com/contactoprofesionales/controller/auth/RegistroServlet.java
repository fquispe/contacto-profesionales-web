package com.contactoprofesionales.controller.auth;

import com.contactoprofesionales.dao.usuario.UsuarioDAO;
import com.contactoprofesionales.dao.usuario.UsuarioDAOImpl;
import com.contactoprofesionales.dto.RegistroCompletoRequest;
import com.contactoprofesionales.dto.RegistroCompletoResponse;
import com.contactoprofesionales.exception.AuthenticationException;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.service.auth.AutenticacionService;
import com.contactoprofesionales.service.auth.AutenticacionServiceImpl;
import com.contactoprofesionales.util.PasswordHasher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;

import com.google.gson.GsonBuilder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador Servlet para el registro de nuevos usuarios.
 * URL: /api/register
 *
 * ENDPOINTS:
 * - POST /api/register - Registro simple (legacy)
 * - POST /api/register - Registro completo con tipo de cuenta (nuevo)
 *
 * ✔ Se comunica con la capa DAO y Service.
 * ✔ Devuelve respuestas JSON.
 * ✔ Cifra la contraseña con BCrypt.
 * ✔ Maneja excepciones controladamente.
 * ✔ Soporta registro con tipo de cuenta (CLIENTE, PROFESIONAL, AMBOS)
 */
@WebServlet("/api/register")
public class RegistroServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(RegistroServlet.class);
    private static final long serialVersionUID = 1L;

    private UsuarioDAO usuarioDAO;
    private AutenticacionService autenticacionService;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        logger.info("=== Inicializando RegistroServlet ===");
        this.usuarioDAO = new UsuarioDAOImpl();
        this.autenticacionService = new AutenticacionServiceImpl();
        this.gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class,
                (com.google.gson.JsonSerializer<LocalDateTime>)
                (src, typeOfSrc, context) ->
                    src == null ? null :
                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            )
            .registerTypeAdapter(LocalDate.class,
                (com.google.gson.JsonSerializer<LocalDate>)
                (src, typeOfSrc, context) ->
                    src == null ? null :
                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
            )
            .create();
    }
    
    /**
     * POST - Registro de usuario
     * Soporta dos flujos:
     * 1. Registro simple (legacy) - sin tipoCuenta
     * 2. Registro completo (nuevo) - con tipoCuenta (CLIENTE, PROFESIONAL, AMBOS)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            // Leer JSON recibido desde el Frontend
            String jsonBody = readRequestBody(request);
            JsonObject jsonRequest = gson.fromJson(jsonBody, JsonObject.class);

            // Detectar si es registro completo (nuevo) o simple (legacy)
            if (jsonRequest.has("tipoCuenta")) {
                logger.info("=== POST /api/register - Registro COMPLETO (nuevo flujo) ===");
                handleRegistroCompleto(jsonRequest, response);
            } else {
                logger.info("=== POST /api/register - Registro SIMPLE (legacy) ===");
                handleRegistroSimple(jsonRequest, response);
            }

        } catch (Exception ex) {
            logger.error("Error inesperado en POST /api/register", ex);
            JsonObject jsonResponse = new JsonObject();
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error en el servidor: " + ex.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }

    /**
     * Maneja el registro completo con tipo de cuenta
     */
    private void handleRegistroCompleto(JsonObject jsonRequest, HttpServletResponse response)
            throws IOException {

        JsonObject jsonResponse = new JsonObject();

        try {
            // Convertir JSON a RegistroCompletoRequest
            RegistroCompletoRequest registroRequest = gson.fromJson(jsonRequest, RegistroCompletoRequest.class);

            logger.debug("Registro completo para: {}, tipo: {}", registroRequest.getEmail(), registroRequest.getTipoCuenta());

            // Llamar al servicio de autenticación
            RegistroCompletoResponse registroResponse = autenticacionService.registrarCompleto(registroRequest);

            // Respuesta exitosa
            jsonResponse.addProperty("success", true);
            jsonResponse.addProperty("message", registroResponse.getMensaje());
            jsonResponse.add("data", gson.toJsonTree(registroResponse));

            logger.info("Usuario registrado exitosamente: {}, Rol: {}", registroResponse.getEmail(), registroResponse.getTipoRol());

            response.setStatus(HttpServletResponse.SC_CREATED);
            response.getWriter().write(gson.toJson(jsonResponse));

        } catch (AuthenticationException authEx) {
            logger.warn("Error de autenticación en registro: {}", authEx.getMessage());
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", authEx.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(gson.toJson(jsonResponse));

        } catch (DatabaseException dbEx) {
            logger.error("Error de base de datos en registro: {}", dbEx.getMessage(), dbEx);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error al registrar usuario: " + dbEx.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }

    /**
     * Maneja el registro simple (legacy) para compatibilidad retroactiva
     */
    private void handleRegistroSimple(JsonObject jsonRequest, HttpServletResponse response)
            throws IOException {

        JsonObject jsonResponse = new JsonObject();

        try {
            // Validaciones básicas de campos obligatorios
            if (!jsonRequest.has("nombre") || !jsonRequest.has("email") ||
                !jsonRequest.has("password") || !jsonRequest.has("telefono")) {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "Faltan datos obligatorios");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            String nombre = jsonRequest.get("nombre").getAsString();
            String email = jsonRequest.get("email").getAsString();
            String telefono = jsonRequest.get("telefono").getAsString();
            String passwordPlano = jsonRequest.get("password").getAsString();

            // Verificar si el email ya existe
            if (usuarioDAO.existeEmail(email)) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "El correo ya está registrado");
                response.getWriter().write(gson.toJson(jsonResponse));
                return;
            }

            // Cifrar la contraseña usando PasswordHasher
            PasswordHasher hasher = new PasswordHasher();
            String passwordHash = hasher.hash(passwordPlano);

            // Crear objeto Usuario
            Usuario usuario = new Usuario();
            usuario.setNombre(nombre);
            usuario.setEmail(email);
            usuario.setPasswordHash(passwordHash);
            usuario.setTelefono(telefono);
            usuario.setActivo(true);
            usuario.setFechaRegistro(LocalDateTime.now());

            // Registrar en la base de datos
            boolean registrado = usuarioDAO.registrar(usuario);

            if (registrado) {
                jsonResponse.addProperty("success", true);
                jsonResponse.addProperty("message", "Usuario registrado exitosamente");
                jsonResponse.add("data", gson.toJsonTree(usuario));
                response.setStatus(HttpServletResponse.SC_OK);
                logger.info("Usuario registrado (flujo simple): {}", email);
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "No se pudo registrar el usuario");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

            response.getWriter().write(gson.toJson(jsonResponse));

        } catch (DatabaseException dbEx) {
            logger.error("Error de base de datos en registro simple: {}", dbEx.getMessage(), dbEx);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error de base de datos: " + dbEx.getMessage());
            response.getWriter().write(gson.toJson(jsonResponse));

        } catch (Exception ex) {
            logger.error("Error inesperado en registro simple: {}", ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error en el servidor: " + ex.getMessage());
            response.getWriter().write(gson.toJson(jsonResponse));
        }
    }

    /**
     * Lee el cuerpo del request como String
     */
    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
