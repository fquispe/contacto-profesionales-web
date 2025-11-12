package com.contactoprofesionales.controller.usuario;

import com.contactoprofesionales.dao.usuariopersona.UsuarioPersonaDAO;
import com.contactoprofesionales.dao.usuariopersona.UsuarioPersonaDAOImpl;
import com.contactoprofesionales.dto.ResponseDTO;
import com.contactoprofesionales.model.UsuarioPersona;
import com.contactoprofesionales.exception.DatabaseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@WebServlet(name = "UsuarioPersonaServlet", urlPatterns = {"/api/usuario-persona/*"})
public class UsuarioPersonaServlet extends HttpServlet {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(UsuarioPersonaServlet.class);
    private UsuarioPersonaDAO usuarioPersonaDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.usuarioPersonaDAO = new UsuarioPersonaDAOImpl();
        
        // Configurar Gson con TypeAdapters para LocalDate y LocalDateTime
        this.gson = new GsonBuilder()
            // Serialización de LocalDateTime (Object -> JSON)
            .registerTypeAdapter(LocalDateTime.class,
                (com.google.gson.JsonSerializer<LocalDateTime>)
                (src, typeOfSrc, context) ->
                    src == null ? null :
                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
            )
            // Deserialización de LocalDateTime (JSON -> Object)
            .registerTypeAdapter(LocalDateTime.class,
                (com.google.gson.JsonDeserializer<LocalDateTime>)
                (json, typeOfT, context) -> {
                    if (json == null || json.isJsonNull()) {
                        return null;
                    }
                    return LocalDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                }
            )
            // Serialización de LocalDate (Object -> JSON)
            .registerTypeAdapter(LocalDate.class,
                (com.google.gson.JsonSerializer<LocalDate>)
                (src, typeOfSrc, context) ->
                    src == null ? null :
                    new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE))
            )
            // Deserialización de LocalDate (JSON -> Object) - ESTO ES LO CRÍTICO
            .registerTypeAdapter(LocalDate.class,
                (com.google.gson.JsonDeserializer<LocalDate>)
                (json, typeOfT, context) -> {
                    if (json == null || json.isJsonNull()) {
                        return null;
                    }
                    return LocalDate.parse(json.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
                }
            )
            .create();
        
        logger.info("UsuarioPersonaServlet inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, 400, "ID de usuario persona no proporcionado");
            return;
        }

        try {
            // Extraer ID de la URL
            String idStr = pathInfo.substring(1);
            Long usuarioPersonaId = Long.parseLong(idStr);

            logger.info("GET /api/usuario-persona/{}", usuarioPersonaId);

            Optional<UsuarioPersona> personaOpt = usuarioPersonaDAO.buscarPorId(usuarioPersonaId);

            if (personaOpt.isPresent()) {
                sendSuccess(response, 200, personaOpt.get());
            } else {
                sendError(response, 404, "Usuario persona no encontrado");
            }

        } catch (NumberFormatException e) {
            sendError(response, 400, "ID inválido");
        } catch (DatabaseException e) {
            logger.error("Error al buscar usuario persona", e);
            sendError(response, 500, "Error al obtener información del usuario");
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String pathInfo = request.getPathInfo();
        
        if (pathInfo == null || pathInfo.equals("/")) {
            sendError(response, 400, "ID de usuario persona no proporcionado");
            return;
        }

        try {
            // Extraer ID de la URL
            String idStr = pathInfo.substring(1);
            Long usuarioPersonaId = Long.parseLong(idStr);

            logger.info("PUT /api/usuario-persona/{}", usuarioPersonaId);

            // Leer el body
            String body = readRequestBody(request);
            UsuarioPersona persona = gson.fromJson(body, UsuarioPersona.class);
            persona.setId(usuarioPersonaId);

            // Actualizar
            UsuarioPersona actualizado = usuarioPersonaDAO.actualizar(persona);

            sendSuccess(response, 200, actualizado);
            logger.info("Usuario persona actualizado: {}", usuarioPersonaId);

        } catch (NumberFormatException e) {
            sendError(response, 400, "ID inválido");
        } catch (DatabaseException e) {
            logger.error("Error al actualizar usuario persona", e);
            sendError(response, 500, "Error al actualizar información del usuario");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            logger.info("POST /api/usuario-persona");

            // Leer el body
            String body = readRequestBody(request);
            UsuarioPersona persona = gson.fromJson(body, UsuarioPersona.class);

            // Registrar
            UsuarioPersona creado = usuarioPersonaDAO.registrar(persona);

            sendSuccess(response, 201, creado);
            logger.info("Usuario persona creado: {}", creado.getId());

        } catch (DatabaseException e) {
            logger.error("Error al crear usuario persona", e);
            sendError(response, 500, "Error al guardar información del usuario: " + e.getMessage());
        }
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = request.getReader().readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    private void sendSuccess(HttpServletResponse response, int status, Object data) throws IOException {
        response.setStatus(status);
        ResponseDTO<Object> responseDTO = new ResponseDTO<>(true, "Éxito", data);
        response.getWriter().write(gson.toJson(responseDTO));
    }

    private void sendError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        ResponseDTO<Object> responseDTO = new ResponseDTO<>(false, message, null);
        response.getWriter().write(gson.toJson(responseDTO));
    }
}