package com.contactoprofesionales.exception;

/**
 * Excepción para errores de base de datos.
 */
public class DatabaseException extends Exception {
    private static final long serialVersionUID = 1L;

    public DatabaseException(String mensaje) {
        super(mensaje);
    }

    public DatabaseException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
