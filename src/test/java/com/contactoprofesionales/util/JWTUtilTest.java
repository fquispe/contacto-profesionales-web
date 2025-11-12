package com.contactoprofesionales.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;

/**
 * Tests para JWTUtil.
 */
public class JWTUtilTest {

    private JWTUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JWTUtil();
    }

    @Test
    @DisplayName("Generar token debe retornar un token válido")
    public void testGenerateToken_Valido() {
        // Arrange
        Integer userId = 1;
        String email = "test@example.com";
        String nombre = "Test User";

        // Act
        String token = jwtUtil.generateToken(userId, email, nombre);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."), "El token debe tener formato JWT");
    }

    @Test
    @DisplayName("Extraer email del token debe retornar el email correcto")
    public void testExtractEmail() {
        // Arrange
        Integer userId = 1;
        String email = "test@example.com";
        String nombre = "Test User";
        String token = jwtUtil.generateToken(userId, email, nombre);

        // Act
        String extractedEmail = jwtUtil.extractEmail(token);

        // Assert
        assertEquals(email, extractedEmail);
    }

    @Test
    @DisplayName("Extraer userId del token debe retornar el userId correcto")
    public void testExtractUserId() {
        // Arrange
        Integer userId = 123;
        String email = "test@example.com";
        String nombre = "Test User";
        String token = jwtUtil.generateToken(userId, email, nombre);

        // Act
        Integer extractedUserId = jwtUtil.extractUserId(token);

        // Assert
        assertEquals(userId, extractedUserId);
    }

    @Test
    @DisplayName("Extraer fecha de expiración debe retornar fecha futura")
    public void testExtractExpiration() {
        // Arrange
        Integer userId = 1;
        String email = "test@example.com";
        String nombre = "Test User";
        String token = jwtUtil.generateToken(userId, email, nombre);

        // Act
        Date expiration = jwtUtil.extractExpiration(token);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()), "La fecha de expiración debe ser en el futuro");
    }

    @Test
    @DisplayName("Token recién generado no debe estar expirado")
    public void testIsTokenExpired_NoExpirado() {
        // Arrange
        Integer userId = 1;
        String email = "test@example.com";
        String nombre = "Test User";
        String token = jwtUtil.generateToken(userId, email, nombre);

        // Act
        Boolean isExpired = jwtUtil.isTokenExpired(token);

        // Assert
        assertFalse(isExpired, "El token recién generado no debe estar expirado");
    }

    @Test
    @DisplayName("Validar token con email correcto debe retornar true")
    public void testValidateToken_EmailCorrecto() {
        // Arrange
        Integer userId = 1;
        String email = "test@example.com";
        String nombre = "Test User";
        String token = jwtUtil.generateToken(userId, email, nombre);

        // Act
        Boolean isValid = jwtUtil.validateToken(token, email);

        // Assert
        assertTrue(isValid, "El token debe ser válido para el email correcto");
    }

    @Test
    @DisplayName("Validar token con email incorrecto debe retornar false")
    public void testValidateToken_EmailIncorrecto() {
        // Arrange
        Integer userId = 1;
        String email = "test@example.com";
        String nombre = "Test User";
        String token = jwtUtil.generateToken(userId, email, nombre);
        String wrongEmail = "wrong@example.com";

        // Act
        Boolean isValid = jwtUtil.validateToken(token, wrongEmail);

        // Assert
        assertFalse(isValid, "El token no debe ser válido para un email incorrecto");
    }

    @Test
    @DisplayName("Validar token inválido debe retornar false")
    public void testValidateToken_TokenInvalido() {
        // Arrange
        String invalidToken = "token.invalido.aqui";
        String email = "test@example.com";

        // Act
        Boolean isValid = jwtUtil.validateToken(invalidToken, email);

        // Assert
        assertFalse(isValid, "Un token inválido no debe ser válido");
    }

    @Test
    @DisplayName("Obtener tiempo de expiración debe retornar valor positivo")
    public void testGetExpirationTime() {
        // Act
        long expirationTime = jwtUtil.getExpirationTime();

        // Assert
        assertTrue(expirationTime > 0, "El tiempo de expiración debe ser positivo");
        assertEquals(86400000, expirationTime, "El tiempo de expiración debe ser 24 horas");
    }

    @Test
    @DisplayName("Generar múltiples tokens para el mismo usuario debe generar tokens diferentes")
    public void testGenerateToken_MultiplesTokensDiferentes() {
        // Arrange
        Integer userId = 1;
        String email = "test@example.com";
        String nombre = "Test User";

        // Act
        String token1 = jwtUtil.generateToken(userId, email, nombre);
        // Pequeño delay para asegurar diferentes issuedAt
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            // Ignorar
        }
        String token2 = jwtUtil.generateToken(userId, email, nombre);

        // Assert
        assertNotEquals(token1, token2, "Los tokens generados deben ser diferentes");
    }

    @Test
    @DisplayName("Extraer email de token inválido debe lanzar excepción")
    public void testExtractEmail_TokenInvalido() {
        // Arrange
        String invalidToken = "token.invalido.aqui";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            jwtUtil.extractEmail(invalidToken);
        });
    }

    @Test
    @DisplayName("Token debe contener todos los claims necesarios")
    public void testGenerateToken_ContieneClaimsNecesarios() {
        // Arrange
        Integer userId = 123;
        String email = "test@example.com";
        String nombre = "Test User";

        // Act
        String token = jwtUtil.generateToken(userId, email, nombre);

        // Assert
        assertNotNull(token);
        assertEquals(email, jwtUtil.extractEmail(token));
        assertEquals(userId, jwtUtil.extractUserId(token));
        assertNotNull(jwtUtil.extractExpiration(token));
    }
}
