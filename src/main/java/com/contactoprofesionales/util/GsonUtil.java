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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para crear instancias de Gson configuradas
 * con soporte para LocalDateTime y otras necesidades del proyecto.
 */
public class GsonUtil {

    // ✅ Formateadores para LocalDateTime y LocalDate
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * Obtiene una instancia de Gson configurada para el proyecto.
     * Incluye adaptadores para LocalDateTime y LocalDate.
     */
    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())  // ✅ Para LocalDateTime
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())          // ✅ Para LocalDate
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                .setPrettyPrinting() // Opcional: para JSON más legible
                .create();
    }
    
    /**
     * Adaptador para serializar/deserializar LocalDateTime.
     * Formato: "2025-11-16T21:09:43"
     */
    private static class LocalDateTimeAdapter
            implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

        @Override
        public JsonElement serialize(LocalDateTime dateTime, Type type,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(DATE_TIME_FORMATTER.format(dateTime));
        }

        @Override
        public LocalDateTime deserialize(JsonElement json, Type type,
                                        JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDateTime.parse(json.getAsString(), DATE_TIME_FORMATTER);
        }
    }

    /**
     * Adaptador para serializar/deserializar LocalDate.
     * Formato: "2025-11-16"
     */
    private static class LocalDateAdapter
            implements JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {

        @Override
        public JsonElement serialize(LocalDate date, Type type,
                                     JsonSerializationContext context) {
            return new JsonPrimitive(DATE_FORMATTER.format(date));
        }

        @Override
        public LocalDate deserialize(JsonElement json, Type type,
                                    JsonDeserializationContext context)
                throws JsonParseException {
            return LocalDate.parse(json.getAsString(), DATE_FORMATTER);
        }
    }
}