package com.contactoprofesionales.exception;

/**
 * Excepción específica para errores relacionados con profesionales.
 */
public class ProfesionalException extends Exception {
    
    private static final long serialVersionUID = 1L;
    
    public ProfesionalException(String message) {
        super(message);
    }
    
    public ProfesionalException(String message, Throwable cause) {
        super(message, cause);
    }
}
