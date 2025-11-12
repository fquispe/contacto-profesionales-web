package com.contactoprofesionales.dto;

import java.io.Serializable;

/**
 * DTO genérico para respuestas de la API
 *
 * @param <T> Tipo de datos en la respuesta
 */
public class ResponseDTO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private String message;
    private T data;
    private Object error;

    // Constructor para respuestas exitosas (3 parámetros)
    public ResponseDTO(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = null;
    }

    // Constructor para respuestas con error (4 parámetros)
    public ResponseDTO(boolean success, String message, T data, Object error) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.error = error;
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

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }
}

