package com.contactoprofesionales.controller.auth;

import com.contactoprofesionales.dao.usuario.UsuarioDAO;
import com.contactoprofesionales.dao.usuario.UsuarioDAOImpl;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.util.PasswordHasher;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.BufferedReader;

import com.google.gson.GsonBuilder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador Servlet para el registro de nuevos usuarios.
 * URL: /api/register
 * 
 * ✔ Se comunica con la capa DAO.
 * ✔ Devuelve respuestas JSON.
 * ✔ Cifra la contraseña con BCrypt.
 * ✔ Maneja excepciones controladamente.
 */
@WebServlet("/api/register")
public class RegistroServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final UsuarioDAO usuarioDAO = new UsuarioDAOImpl();

    private final Gson gson = new GsonBuilder()
    .registerTypeAdapter(LocalDateTime.class, 
            (com.google.gson.JsonSerializer<LocalDateTime>) 
            (src, typeOfSrc, context) -> 
                src == null ? null : 
                new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        )
        .create();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        JsonObject jsonResponse = new JsonObject();

        try {
            // Leer JSON recibido desde el Frontend
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JsonObject jsonRequest = gson.fromJson(sb.toString(), JsonObject.class);

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
            } else {
                jsonResponse.addProperty("success", false);
                jsonResponse.addProperty("message", "No se pudo registrar el usuario");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (DatabaseException dbEx) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error de base de datos: " + dbEx.getMessage());

        } catch (Exception ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.addProperty("success", false);
            jsonResponse.addProperty("message", "Error en el servidor: " + ex.getMessage());
            ex.printStackTrace();
        }

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}
