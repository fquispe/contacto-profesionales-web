package com.contactoprofesionales.exception;

/**
 * Excepción para errores de base de datos.
 */
public class DatabaseException extends Exception {
	private static final long serialVersionUID = 1L;
    private String errorCode;

    public DatabaseException(String mensaje) {
        super(mensaje);
    }

    public DatabaseException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    // Agregar este nuevo constructor
    public DatabaseException(String errorCode, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.errorCode = errorCode;
    }

    // Agregar getter para el código de error
    public String getErrorCode() {
        return errorCode;
    }
}
