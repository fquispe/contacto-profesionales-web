package com.contactoprofesionales.exception;

/**
 * Excepción para errores de autenticación.
 */
public class AuthenticationException extends Exception {
    private static final long serialVersionUID = 1L;

    public AuthenticationException(String mensaje) {
        super(mensaje);
    }

    public AuthenticationException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}