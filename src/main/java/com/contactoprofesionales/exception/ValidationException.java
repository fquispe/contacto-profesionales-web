package com.contactoprofesionales.exception;

/**
 * Excepción personalizada para errores de validación de negocio o de entrada.
 * errores de validación lógica (ValidationException).
 */
public class ValidationException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor con mensaje descriptivo.
     * 
     * @param message Mensaje de error que describe la causa del fallo.
     */
    public ValidationException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa original.
     * 
     * @param message Mensaje descriptivo.
     * @param cause Excepción original que causó el error.
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor vacío (no recomendado salvo en casos específicos).
     */
    public ValidationException() {
        super("Error de validación desconocido");
    }
}

