package com.contactoprofesionales.exception;

/**
 * Excepción personalizada para cuando un usuario no existe en el sistema.
 * Se usa para diferenciar entre "usuario no encontrado" vs "contraseña incorrecta"
 * y así NO contar intentos fallidos cuando el usuario ni siquiera existe.
 *
 * Creado: 2025-11-15
 *
 * @author Sistema
 */
public class UserNotFoundException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
