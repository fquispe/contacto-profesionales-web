package com.contactoprofesionales.controller;

import com.contactoprofesionales.dao.ServiciosProfesionalDAO;
import com.contactoprofesionales.dao.ServiciosProfesionalDAOImpl;
import com.contactoprofesionales.dto.ServiciosProfesionalCompleto;
import com.contactoprofesionales.model.*;
import com.contactoprofesionales.util.DatabaseConnection;
import com.google.gson.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Servlet para gestionar los servicios profesionales.
 * Endpoints:
 * - GET /api/servicios-profesional?profesionalId={id} : Obtener servicios
 * - POST /api/servicios-profesional : Crear servicios
 * - PUT /api/servicios-profesional?profesionalId={id} : Actualizar servicios
 * - DELETE /api/servicios-profesional?profesionalId={id} : Eliminar servicios
 */
@WebServlet("/api/servicios-profesional")
public class ServiciosProfesionalServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = LoggerFactory.getLogger(ServiciosProfesionalServlet.class);
    private ServiciosProfesionalDAO serviciosDAO;
    private Gson gson;

    @Override
    public void init() throws ServletException {
        super.init();
        this.serviciosDAO = new ServiciosProfesionalDAOImpl();

        // Configurar Gson con adaptadores personalizados
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalTime.class, new LocalTimeAdapter())
                .setPrettyPrinting()
                .create();

        logger.info("ServiciosProfesionalServlet inicializado");
    }

	 // ============================================================================
	 // ✅ MÉTODO doGet TAMBIÉN ACTUALIZADO
	 // ============================================================================
	
	 @Override
	 protected void doGet(HttpServletRequest request, HttpServletResponse response)
	         throws ServletException, IOException {
	
	     configurarCORS(response);
	
	     // ✅ CAMBIO: Recibir usuarioId en lugar de profesionalId
	     String usuarioIdStr = request.getParameter("usuarioId");
	
	     if (usuarioIdStr == null || usuarioIdStr.trim().isEmpty()) {
	         enviarError(response, 400, "El parámetro 'usuarioId' es requerido");
	         return;
	     }
	
	     try {
	         Integer usuarioId = Integer.parseInt(usuarioIdStr);
	         
	         // ✅ Buscar profesionalId
	         Integer profesionalId = buscarProfesionalIdPorUsuarioId(usuarioId);
	         
	         if (profesionalId == null) {
	             enviarError(response, 404, "El usuario no tiene un perfil de profesional");
	             return;
	         }
	
	         logger.info("Obteniendo servicios del profesional {}", profesionalId);
	
	         ServiciosProfesionalCompleto servicios = serviciosDAO.obtenerServiciosProfesional(profesionalId);
	
	         JsonObject jsonResponse = new JsonObject();
	         jsonResponse.addProperty("success", true);
	         jsonResponse.addProperty("message", "Servicios obtenidos exitosamente");
	         jsonResponse.add("data", gson.toJsonTree(servicios));
	
	         enviarRespuesta(response, 200, jsonResponse);
	
	     } catch (NumberFormatException e) {
	         logger.error("ID de usuario inválido: {}", usuarioIdStr);
	         enviarError(response, 400, "ID de usuario inválido");
	     } catch (Exception e) {
	         logger.error("Error obteniendo servicios", e);
	         enviarError(response, 500, "Error interno del servidor: " + e.getMessage());
	     }
	 }

	// ============================================================================
	// FRAGMENTO CORREGIDO: ServiciosProfesionalServlet.java
	// Busca profesionalId desde usuarioId de forma segura en el backend
	// ============================================================================
	 
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        configurarCORS(response);

        try {
            // Leer el cuerpo de la petición
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String jsonBody = sb.toString();

            logger.info("Recibiendo solicitud POST para guardar servicios: {}", jsonBody);

            // Parsear JSON
            JsonObject jsonRequest = JsonParser.parseString(jsonBody).getAsJsonObject();

            // ✅ CAMBIO 1: Recibir usuarioId en lugar de profesionalId
            if (!jsonRequest.has("usuarioId")) {
                enviarError(response, 400, "El campo 'usuarioId' es requerido");
                return;
            }

            Integer usuarioId = jsonRequest.get("usuarioId").getAsInt();
            logger.info("Usuario ID recibido: {}", usuarioId);

            // ✅ CAMBIO 2: Buscar el profesionalId correspondiente al usuarioId
            Integer profesionalId = buscarProfesionalIdPorUsuarioId(usuarioId);
            
            if (profesionalId == null) {
                logger.error("Usuario {} no tiene un perfil de profesional activo", usuarioId);
                enviarError(response, 400, "El usuario no tiene un perfil de profesional. Complete su perfil primero.");
                return;
            }
            
            logger.info("✓ Profesional ID encontrado: {} para usuario ID: {}", profesionalId, usuarioId);

            // ✅ CAMBIO 3: Ahora usar profesionalId para todas las operaciones
            // Parsear especialidades
            JsonArray especialidadesJson = jsonRequest.getAsJsonArray("especialidades");
            java.util.List<EspecialidadProfesional> especialidades = parsearEspecialidades(especialidadesJson, profesionalId);

            // Parsear área de servicio
            JsonObject areaServicioJson = jsonRequest.getAsJsonObject("areaServicio");
            AreaServicio areaServicio = parsearAreaServicio(areaServicioJson, profesionalId);

            // Parsear disponibilidad
            JsonObject disponibilidadJson = jsonRequest.getAsJsonObject("disponibilidad");
            DisponibilidadHoraria disponibilidad = parsearDisponibilidad(disponibilidadJson, profesionalId);

            // Verificar si ya tiene servicios
            boolean yaExiste = serviciosDAO.tieneServiciosConfigurados(profesionalId);

            if (yaExiste) {
                // Actualizar en lugar de crear
                boolean actualizado = serviciosDAO.actualizarServiciosProfesional(
                    profesionalId, especialidades, areaServicio, disponibilidad
                );

                if (actualizado) {
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Servicios actualizados exitosamente");
                    jsonResponse.addProperty("profesionalId", profesionalId);

                    enviarRespuesta(response, 200, jsonResponse);
                } else {
                    enviarError(response, 500, "Error actualizando servicios");
                }
            } else {
                // Crear nuevos servicios
                boolean guardado = serviciosDAO.guardarServiciosProfesional(
                    profesionalId, especialidades, areaServicio, disponibilidad
                );

                if (guardado) {
                    JsonObject jsonResponse = new JsonObject();
                    jsonResponse.addProperty("success", true);
                    jsonResponse.addProperty("message", "Servicios guardados exitosamente");
                    jsonResponse.addProperty("profesionalId", profesionalId);

                    enviarRespuesta(response, 201, jsonResponse);
                } else {
                    enviarError(response, 500, "Error guardando servicios");
                }
            }

        } catch (JsonParseException e) {
            logger.error("Error parseando JSON", e);
            enviarError(response, 400, "JSON inválido: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("Error de validación", e);
            enviarError(response, 400, "Error de validación: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error procesando solicitud POST", e);
            enviarError(response, 500, "Error interno del servidor: " + e.getMessage());
        }
    }

	 // ============================================================================
	 // ✅ NUEVO MÉTODO: Buscar profesionalId desde usuarioId
	 // ============================================================================
	
	 /**
	  * Busca el ID del profesional asociado a un usuario.
	  * Este método encapsula la lógica de seguridad para evitar exponer
	  * relaciones de tablas en el frontend.
	  * 
	  * @param usuarioId ID del usuario autenticado
	  * @return ID del profesional, o null si no existe o no está activo
	  */
	 private Integer buscarProfesionalIdPorUsuarioId(Integer usuarioId) {
	     String sql = "SELECT id FROM profesionales WHERE usuario_id = ? AND activo = TRUE";
	     
	     try (Connection conn = DatabaseConnection.getConnection();
	          PreparedStatement stmt = conn.prepareStatement(sql)) {
	         
	         stmt.setInt(1, usuarioId);
	         ResultSet rs = stmt.executeQuery();
	         
	         if (rs.next()) {
	             Integer profesionalId = rs.getInt("id");
	             logger.debug("Profesional ID {} encontrado para usuario ID {}", profesionalId, usuarioId);
	             return profesionalId;
	         } else {
	             logger.warn("No se encontró profesional para usuario ID {}", usuarioId);
	             return null;
	         }
	         
	     } catch (SQLException e) {
	         logger.error("Error buscando profesional para usuario {}", usuarioId, e);
	         return null;
	     }
	 }
    
	// ============================================================================
	// ✅ MÉTODO doPut TAMBIÉN ACTUALIZADO
	// ============================================================================

	@Override
	protected void doPut(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    configurarCORS(response);

	    try {
	        StringBuilder sb = new StringBuilder();
	        BufferedReader reader = request.getReader();
	        String line;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line);
	        }
	        String jsonBody = sb.toString();

	        logger.info("Recibiendo solicitud PUT para actualizar servicios: {}", jsonBody);

	        JsonObject jsonRequest = JsonParser.parseString(jsonBody).getAsJsonObject();

	        // ✅ CAMBIO: Usar usuarioId y buscar profesionalId
	        if (!jsonRequest.has("usuarioId")) {
	            enviarError(response, 400, "El campo 'usuarioId' es requerido");
	            return;
	        }

	        Integer usuarioId = jsonRequest.get("usuarioId").getAsInt();
	        Integer profesionalId = buscarProfesionalIdPorUsuarioId(usuarioId);
	        
	        if (profesionalId == null) {
	            enviarError(response, 400, "El usuario no tiene un perfil de profesional");
	            return;
	        }

	        // Parsear especialidades
	        JsonArray especialidadesJson = jsonRequest.getAsJsonArray("especialidades");
	        java.util.List<EspecialidadProfesional> especialidades = parsearEspecialidades(especialidadesJson, profesionalId);

	        // Parsear área de servicio
	        JsonObject areaServicioJson = jsonRequest.getAsJsonObject("areaServicio");
	        AreaServicio areaServicio = parsearAreaServicio(areaServicioJson, profesionalId);

	        // Parsear disponibilidad
	        JsonObject disponibilidadJson = jsonRequest.getAsJsonObject("disponibilidad");
	        DisponibilidadHoraria disponibilidad = parsearDisponibilidad(disponibilidadJson, profesionalId);

	        // Actualizar
	        boolean actualizado = serviciosDAO.actualizarServiciosProfesional(
	            profesionalId, especialidades, areaServicio, disponibilidad
	        );

	        if (actualizado) {
	            JsonObject jsonResponse = new JsonObject();
	            jsonResponse.addProperty("success", true);
	            jsonResponse.addProperty("message", "Servicios actualizados exitosamente");
	            jsonResponse.addProperty("profesionalId", profesionalId);

	            enviarRespuesta(response, 200, jsonResponse);
	        } else {
	            enviarError(response, 500, "Error actualizando servicios");
	        }

	    } catch (JsonParseException e) {
	        logger.error("Error parseando JSON", e);
	        enviarError(response, 400, "JSON inválido: " + e.getMessage());
	    } catch (IllegalArgumentException e) {
	        logger.error("Error de validación", e);
	        enviarError(response, 400, "Error de validación: " + e.getMessage());
	    } catch (Exception e) {
	        logger.error("Error procesando solicitud PUT", e);
	        enviarError(response, 500, "Error interno del servidor: " + e.getMessage());
	    }
	}


	// ============================================================================
	// ✅ MÉTODO doDelete TAMBIÉN ACTUALIZADO
	// ============================================================================

	@Override
	protected void doDelete(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {

	    configurarCORS(response);

	    String usuarioIdStr = request.getParameter("usuarioId");

	    if (usuarioIdStr == null || usuarioIdStr.trim().isEmpty()) {
	        enviarError(response, 400, "El parámetro 'usuarioId' es requerido");
	        return;
	    }

	    try {
	        Integer usuarioId = Integer.parseInt(usuarioIdStr);
	        Integer profesionalId = buscarProfesionalIdPorUsuarioId(usuarioId);
	        
	        if (profesionalId == null) {
	            enviarError(response, 404, "El usuario no tiene un perfil de profesional");
	            return;
	        }

	        logger.info("Eliminando servicios del profesional {}", profesionalId);

	        boolean eliminado = serviciosDAO.eliminarServiciosProfesional(profesionalId);

	        if (eliminado) {
	            JsonObject jsonResponse = new JsonObject();
	            jsonResponse.addProperty("success", true);
	            jsonResponse.addProperty("message", "Servicios eliminados exitosamente");

	            enviarRespuesta(response, 200, jsonResponse);
	        } else {
	            enviarError(response, 500, "Error eliminando servicios");
	        }

	    } catch (NumberFormatException e) {
	        logger.error("ID de usuario inválido: {}", usuarioIdStr);
	        enviarError(response, 400, "ID de usuario inválido");
	    } catch (Exception e) {
	        logger.error("Error eliminando servicios", e);
	        enviarError(response, 500, "Error interno del servidor: " + e.getMessage());
	    }
	}

    @Override
    protected void doOptions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configurarCORS(response);
        response.setStatus(HttpServletResponse.SC_OK);
    }

    // =====================================================================
    // MÉTODOS AUXILIARES
    // =====================================================================

    private java.util.List<EspecialidadProfesional> parsearEspecialidades(JsonArray especialidadesJson, Integer profesionalId) {
        java.util.List<EspecialidadProfesional> especialidades = new java.util.ArrayList<>();

        if (especialidadesJson == null || especialidadesJson.size() == 0) {
            throw new IllegalArgumentException("Debe proporcionar al menos una especialidad");
        }

        if (especialidadesJson.size() > 3) {
            throw new IllegalArgumentException("No puede registrar más de 3 especialidades");
        }

        for (int i = 0; i < especialidadesJson.size(); i++) {
            JsonObject espJson = especialidadesJson.get(i).getAsJsonObject();

            EspecialidadProfesional esp = new EspecialidadProfesional();
            esp.setProfesionalId(profesionalId);

            // ✅ Usar categoriaId en lugar de nombreEspecialidad
            esp.setCategoriaId(espJson.get("categoriaId").getAsInt());

            // ✅ NUEVO CAMPO OBLIGATORIO: servicioProfesional
            String servicioProfesional = getStringFromJson(espJson, "servicioProfesional");
            if (servicioProfesional == null || servicioProfesional.trim().isEmpty()) {
                throw new IllegalArgumentException("La especialidad " + (i + 1) + " debe tener un servicio profesional especificado");
            }
            esp.setServicioProfesional(servicioProfesional.trim());

            // Usar getStringFromJson para manejar valores null correctamente
            String descripcion = getStringFromJson(espJson, "descripcion");
            esp.setDescripcion(descripcion != null ? descripcion : "");

            esp.setIncluyeMateriales(espJson.has("incluyeMateriales") && espJson.get("incluyeMateriales").getAsBoolean());
            esp.setCosto(espJson.get("costo").getAsDouble());
            esp.setTipoCosto(espJson.get("tipoCosto").getAsString());
            esp.setEsPrincipal(espJson.has("esPrincipal") && espJson.get("esPrincipal").getAsBoolean());
            esp.setOrden(i + 1);

            if (!esp.isValid()) {
                throw new IllegalArgumentException("Especialidad " + (i + 1) + " tiene datos inválidos");
            }

            especialidades.add(esp);
        }

        // Validar que al menos una sea principal
        boolean tienePrincipal = especialidades.stream().anyMatch(EspecialidadProfesional::getEsPrincipal);
        if (!tienePrincipal && !especialidades.isEmpty()) {
            especialidades.get(0).setEsPrincipal(true);
        }

        return especialidades;
    }

    private AreaServicio parsearAreaServicio(JsonObject areaServicioJson, Integer profesionalId) {
        if (areaServicioJson == null) {
            throw new IllegalArgumentException("Debe configurar el área de servicio");
        }

        AreaServicio areaServicio = new AreaServicio();
        areaServicio.setProfesionalId(profesionalId);
        areaServicio.setTodoPais(areaServicioJson.has("todoPais") && areaServicioJson.get("todoPais").getAsBoolean());

        // Si no es todo el país, parsear ubicaciones
        if (!areaServicio.getTodoPais() && areaServicioJson.has("ubicaciones")) {
            JsonArray ubicacionesJson = areaServicioJson.getAsJsonArray("ubicaciones");

            if (ubicacionesJson == null || ubicacionesJson.size() == 0) {
                throw new IllegalArgumentException("Debe proporcionar al menos una ubicación si no brinda servicios en todo el país");
            }

            if (ubicacionesJson.size() > 10) {
                throw new IllegalArgumentException("No puede registrar más de 10 ubicaciones");
            }

            for (int i = 0; i < ubicacionesJson.size(); i++) {
                JsonObject ubicJson = ubicacionesJson.get(i).getAsJsonObject();

                UbicacionServicio ubicacion = new UbicacionServicio();
                ubicacion.setTipoUbicacion(getStringFromJson(ubicJson, "tipoUbicacion"));
                ubicacion.setDepartamento(getStringFromJson(ubicJson, "departamento"));
                ubicacion.setProvincia(getStringFromJson(ubicJson, "provincia"));
                ubicacion.setDistrito(getStringFromJson(ubicJson, "distrito"));
                ubicacion.setOrden(i + 1);

                if (!ubicacion.isValid()) {
                    throw new IllegalArgumentException("Ubicación " + (i + 1) + " tiene datos inválidos");
                }

                areaServicio.addUbicacion(ubicacion);
            }
        }

        if (!areaServicio.isValid()) {
            throw new IllegalArgumentException("Configuración de área de servicio inválida");
        }

        return areaServicio;
    }

    private DisponibilidadHoraria parsearDisponibilidad(JsonObject disponibilidadJson, Integer profesionalId) {
        if (disponibilidadJson == null) {
            throw new IllegalArgumentException("Debe configurar la disponibilidad horaria");
        }

        DisponibilidadHoraria disponibilidad = new DisponibilidadHoraria();
        disponibilidad.setProfesionalId(profesionalId);
        disponibilidad.setTodoTiempo(disponibilidadJson.has("todoTiempo") && disponibilidadJson.get("todoTiempo").getAsBoolean());

        // Si no es todo el tiempo, parsear horarios
        if (!disponibilidad.getTodoTiempo() && disponibilidadJson.has("horarios")) {
            JsonArray horariosJson = disponibilidadJson.getAsJsonArray("horarios");

            if (horariosJson == null || horariosJson.size() == 0) {
                throw new IllegalArgumentException("Debe proporcionar al menos un horario si no está disponible todo el tiempo");
            }

            for (JsonElement horarioElement : horariosJson) {
                JsonObject horarioJson = horarioElement.getAsJsonObject();

                HorarioDia horario = new HorarioDia();
                horario.setDiaSemana(horarioJson.get("diaSemana").getAsString());
                horario.setTipoJornada(horarioJson.get("tipoJornada").getAsString());

                if ("8hrs".equals(horario.getTipoJornada())) {
                    if (horarioJson.has("horaInicio") && horarioJson.has("horaFin")) {
                        horario.setHoraInicio(LocalTime.parse(horarioJson.get("horaInicio").getAsString()));
                        horario.setHoraFin(LocalTime.parse(horarioJson.get("horaFin").getAsString()));
                    } else {
                        // Valores por defecto para 8hrs
                        horario.setHoraInicio(LocalTime.of(8, 0));
                        horario.setHoraFin(LocalTime.of(17, 0));
                    }
                }

                if (!horario.isValid()) {
                    throw new IllegalArgumentException("Horario para " + horario.getDiaSemana() + " tiene datos inválidos");
                }

                disponibilidad.addHorarioDia(horario);
            }
        }

        if (!disponibilidad.isValid()) {
            throw new IllegalArgumentException("Configuración de disponibilidad inválida");
        }

        return disponibilidad;
    }

    /**
     * Obtiene un valor String de un JsonObject de forma segura,
     * manejando correctamente valores null de JSON.
     *
     * @param jsonObject El objeto JSON
     * @param key La clave a buscar
     * @return El valor como String, o null si no existe o es JsonNull
     */
    private String getStringFromJson(JsonObject jsonObject, String key) {
        if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
            return jsonObject.get(key).getAsString();
        }
        return null;
    }

    private void configurarCORS(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }

    private void enviarRespuesta(HttpServletResponse response, int status, JsonObject jsonResponse) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(jsonResponse));
    }

    private void enviarError(HttpServletResponse response, int status, String mensaje) throws IOException {
        JsonObject jsonResponse = new JsonObject();
        jsonResponse.addProperty("success", false);
        jsonResponse.addProperty("error", mensaje);

        enviarRespuesta(response, status, jsonResponse);
    }

    // Adaptadores JSON para LocalDateTime y LocalTime
    private static class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public JsonElement serialize(LocalDateTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(formatter));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), formatter);
        }
    }

    private static class LocalTimeAdapter implements JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_TIME;

        @Override
        public JsonElement serialize(LocalTime src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(formatter));
        }

        @Override
        public LocalTime deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalTime.parse(json.getAsString(), formatter);
        }
    }
}
