package com.contactoprofesionales.service.auth;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del servicio de tokens.
 */
public class TokenServiceImpl implements TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
    
    private final JWTUtil jwtUtil;

    public TokenServiceImpl() {
        this.jwtUtil = new JWTUtil();
    }

    @Override
    public String generateToken(Usuario usuario) {
        logger.info("Generando token para usuario: {}", usuario.getEmail());
        return jwtUtil.generateToken(usuario.getId(), usuario.getEmail(), usuario.getNombre());
    }

    @Override
    public boolean validateToken(String token, String email) {
        return jwtUtil.validateToken(token, email);
    }

    @Override
    public String extractEmail(String token) {
        return jwtUtil.extractEmail(token);
    }

    @Override
    public Integer extractUserId(String token) {
        return jwtUtil.extractUserId(token);
    }

    @Override
    public long getExpirationTime() {
        return jwtUtil.getExpirationTime();
    }
}