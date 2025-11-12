package com.contactoprofesionales.service.auth;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.model.UsuarioPersona;
import com.contactoprofesionales.dao.usuariopersona.UsuarioPersonaDAO;
import com.contactoprofesionales.dao.usuariopersona.UsuarioPersonaDAOImpl;
import com.contactoprofesionales.util.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Implementación del servicio de tokens.
 */
public class TokenServiceImpl implements TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);
    
    private final JWTUtil jwtUtil;
    private final UsuarioPersonaDAO usuarioPersonaDAO;
    
    public TokenServiceImpl() {
        this.jwtUtil = new JWTUtil();
        this.usuarioPersonaDAO = new UsuarioPersonaDAOImpl();
    }
    
    @Override
    public String generateToken(Usuario usuario) {
        logger.info("Generando token para usuario: {}", usuario.getEmail());
        
        // Obtener el nombre desde UsuarioPersona
        String nombreCompleto = "Usuario"; // Valor por defecto
        
        if (usuario.getUsuarioId() != null) {
            try {
                Optional<UsuarioPersona> personaOpt = usuarioPersonaDAO.buscarPorId(usuario.getUsuarioId());
                if (personaOpt.isPresent()) {
                    nombreCompleto = personaOpt.get().getNombreCompleto();
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener el nombre del usuario persona para userId: {}", usuario.getId());
            }
        }
        
        return jwtUtil.generateToken(usuario.getId(), usuario.getEmail(), nombreCompleto);
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