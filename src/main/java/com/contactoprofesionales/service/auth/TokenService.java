package com.contactoprofesionales.service.auth;

import com.contactoprofesionales.model.Usuario;

/**
 * Servicio para gestión de tokens JWT.
 */
public interface TokenService {
    
    /**
     * Genera un token JWT para un usuario.
     */
    String generateToken(Usuario usuario);
    
    /**
     * Valida un token JWT.
     */
    boolean validateToken(String token, String email);
    
    /**
     * Extrae el email del token.
     */
    String extractEmail(String token);
    
    /**
     * Extrae el userId del token.
     */
    Integer extractUserId(String token);
    
    /**
     * Obtiene el tiempo de expiración.
     */
    long getExpirationTime();
}