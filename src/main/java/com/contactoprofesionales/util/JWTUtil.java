package com.contactoprofesionales.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilidad para gestión de JWT (JSON Web Tokens).
 * Actualizado para jjwt 0.12.x
 * 
 * Características:
 * - Generación de tokens
 * - Validación de tokens
 * - Extracción de claims
 */
public class JWTUtil {
    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    
    // En producción, obtener desde variable de entorno
    private static final String SECRET_KEY = 
        "mi_clave_secreta_super_segura_cambiar_en_produccion_con_256_bits_minimo_para_seguridad";
    
    private static final long EXPIRATION_TIME = 86400000; // 24 horas en ms
    
    private final SecretKey key;

    public JWTUtil() {
        this.key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genera un token JWT para un usuario.
     */
    public String generateToken(Integer userId, String email, String nombre) {
        logger.debug("Generando token para usuario: {}", email);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("email", email);
        claims.put("nombre", nombre);
        
        Date now = new Date();
        Date expiration = new Date(now.getTime() + EXPIRATION_TIME);
        
        String token = Jwts.builder()
                .claims(claims)  // Cambiado: setClaims() -> claims()
                .subject(email)  // Cambiado: setSubject() -> subject()
                .issuedAt(now)   // Cambiado: setIssuedAt() -> issuedAt()
                .expiration(expiration)  // Cambiado: setExpiration() -> expiration()
                .signWith(key)   // Simplificado: ya no necesita SignatureAlgorithm
                .compact();
        
        logger.info("✓ Token generado para: {}", email);
        return token;
    }

    /**
     * Extrae el email del token.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae el userId del token.
     */
    public Integer extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Integer.class));
    }

    /**
     * Extrae la fecha de expiración.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token.
     * ACTUALIZADO para jjwt 0.12.x
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()              // Cambiado: parserBuilder() -> parser()
                .verifyWith(key)          // Cambiado: setSigningKey() -> verifyWith()
                .build()
                .parseSignedClaims(token) // Cambiado: parseClaimsJws() -> parseSignedClaims()
                .getPayload();            // Cambiado: getBody() -> getPayload()
    }

    /**
     * Verifica si el token ha expirado.
     */
    public Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            logger.warn("Token expirado o inválido");
            return true;
        }
    }

    /**
     * Valida el token.
     */
    public Boolean validateToken(String token, String email) {
        try {
            final String tokenEmail = extractEmail(token);
            boolean isValid = tokenEmail.equals(email) && !isTokenExpired(token);
            
            if (isValid) {
                logger.debug("✓ Token válido para: {}", email);
            } else {
                logger.warn("✗ Token inválido para: {}", email);
            }
            
            return isValid;
        } catch (Exception e) {
            logger.error("Error al validar token", e);
            return false;
        }
    }

    /**
     * Obtiene el tiempo de expiración en milisegundos.
     */
    public long getExpirationTime() {
        return EXPIRATION_TIME;
    }
}