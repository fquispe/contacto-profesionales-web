package com.contactoprofesionales.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase utilitaria para estandarizar las respuestas JSON de la API.
 * 
 * Proporciona un formato consistente para respuestas exitosas y errores.
 * 
 * Formato de respuesta exitosa:
 * {
 *   "success": true,
 *   "message": "Operación exitosa",
 *   "data": { ... }
 * }
 * 
 * Formato de respuesta de error:
 * {
 *   "success": false,
 *   "error": "Mensaje de error"
 * }
 * 
 * Formato de respuesta de error con detalles:
 * {
 *   "success": false,
 *   "error": "Mensaje de error",
 *   "details": { ... }
 * }
 */
public class JsonResponse {
    
    private boolean success;
    private String message;
    private String error;
    private Object data;
    private Map<String, Object> details;
    
    // Constructor privado para uso interno
    private JsonResponse() {
    }
    
    /**
     * Crea una respuesta exitosa simple sin datos.
     * 
     * @param message Mensaje descriptivo del éxito
     * @return JsonResponse con success=true
     */
    public static JsonResponse success(String message) {
        JsonResponse response = new JsonResponse();
        response.success = true;
        response.message = message;
        return response;
    }
    
    /**
     * Crea una respuesta exitosa con datos.
     * 
     * @param message Mensaje descriptivo del éxito
     * @param data Datos a incluir en la respuesta
     * @return JsonResponse con success=true y datos
     */
    public static JsonResponse success(String message, Object data) {
        JsonResponse response = new JsonResponse();
        response.success = true;
        response.message = message;
        response.data = data;
        return response;
    }
    
    /**
     * Crea una respuesta exitosa solo con datos (sin mensaje).
     * 
     * @param data Datos a incluir en la respuesta
     * @return JsonResponse con success=true y datos
     */
    public static JsonResponse success(Object data) {
        JsonResponse response = new JsonResponse();
        response.success = true;
        response.data = data;
        return response;
    }
    
    /**
     * Crea una respuesta de error simple.
     * 
     * @param errorMessage Mensaje de error
     * @return JsonResponse con success=false
     */
    public static JsonResponse error(String errorMessage) {
        JsonResponse response = new JsonResponse();
        response.success = false;
        response.error = errorMessage;
        return response;
    }
    
    /**
     * Crea una respuesta de error con detalles adicionales.
     * 
     * @param errorMessage Mensaje de error principal
     * @param details Mapa con detalles adicionales del error
     * @return JsonResponse con success=false y detalles
     */
    public static JsonResponse error(String errorMessage, Map<String, Object> details) {
        JsonResponse response = new JsonResponse();
        response.success = false;
        response.error = errorMessage;
        response.details = details;
        return response;
    }
    
    /**
     * Crea una respuesta de error de validación.
     * Útil para errores de validación de formularios.
     * 
     * @param errorMessage Mensaje de error principal
     * @param fieldErrors Mapa de errores por campo (campo -> mensaje)
     * @return JsonResponse con success=false y errores de validación
     */
    public static JsonResponse validationError(String errorMessage, Map<String, String> fieldErrors) {
        JsonResponse response = new JsonResponse();
        response.success = false;
        response.error = errorMessage;
        
        Map<String, Object> details = new HashMap<>();
        details.put("validation_errors", fieldErrors);
        response.details = details;
        
        return response;
    }
    
    /**
     * Crea una respuesta de error con código y mensaje.
     * 
     * @param errorCode Código del error
     * @param errorMessage Mensaje de error
     * @return JsonResponse con success=false, código y mensaje
     */
    public static JsonResponse errorWithCode(String errorCode, String errorMessage) {
        JsonResponse response = new JsonResponse();
        response.success = false;
        response.error = errorMessage;
        
        Map<String, Object> details = new HashMap<>();
        details.put("error_code", errorCode);
        response.details = details;
        
        return response;
    }
    
    // Getters y Setters
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public Object getData() {
        return data;
    }
    
    public void setData(Object data) {
        this.data = data;
    }
    
    public Map<String, Object> getDetails() {
        return details;
    }
    
    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }
    
    /**
     * Agrega un detalle adicional a la respuesta.
     * 
     * @param key Clave del detalle
     * @param value Valor del detalle
     * @return Esta instancia para encadenamiento
     */
    public JsonResponse addDetail(String key, Object value) {
        if (this.details == null) {
            this.details = new HashMap<>();
        }
        this.details.put(key, value);
        return this;
    }
    
    @Override
    public String toString() {
        return "JsonResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", data=" + data +
                ", details=" + details +
                '}';
    }
}