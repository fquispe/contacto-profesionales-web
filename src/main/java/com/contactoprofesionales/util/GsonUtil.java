package com.contactoprofesionales.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para crear instancias de Gson configuradas
 * con soporte para LocalDateTime y otras necesidades del proyecto.
 */
public class GsonUtil {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    /**
     * Obtiene una instancia de Gson configurada para el proyecto.
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setPrettyPrinting() // Opcional: para JSON más legible
                .create();
    }
    
    /**
     * Adaptador para serializar/deserializar LocalDateTime.
     */
    private static class LocalDateTimeAdapter 
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
        
        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type, 
                                     JsonSerializationContext context) {
            return new JsonPrimitive(FORMATTER.format(dateTime));
        }
        
        @Override
        public LocalDateTime deserialize(JsonElement json, Type type, 
                                        JsonDeserializationContext context) 
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), FORMATTER);
        }
    }
}