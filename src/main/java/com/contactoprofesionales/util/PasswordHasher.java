package com.contactoprofesionales.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utilidad para hashear y verificar contraseñas de forma segura.
 * 
 * Utiliza PBKDF2 con HMAC-SHA256 como algoritmo de hashing.
 * 
 * Características:
 * - Salt aleatorio único para cada contraseña
 * - Múltiples iteraciones para resistencia a ataques de fuerza bruta
 * - Formato: algoritmo$iteraciones$salt$hash
 */
public class PasswordHasher {
    private static final Logger logger = LoggerFactory.getLogger(PasswordHasher.class);
    
    // Configuración del algoritmo
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;  // 64K iteraciones
    private static final int KEY_LENGTH = 256;    // 256 bits
    private static final int SALT_LENGTH = 16;    // 16 bytes = 128 bits
    
    private final SecureRandom random;
    
    public PasswordHasher() {
        this.random = new SecureRandom();
        logger.debug("PasswordHasher inicializado con algoritmo: {}", ALGORITHM);
    }
    
    /**
     * Hashea una contraseña en texto plano.
     * 
     * @param password Contraseña en texto plano
     * @return String hasheado en formato: PBKDF2$iteraciones$salt$hash
     * @throws RuntimeException si ocurre un error al hashear
     */
    public String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        
        try {
            // Generar salt aleatorio
            byte[] salt = new byte[SALT_LENGTH];
            random.nextBytes(salt);
            
            // Generar hash
            byte[] hash = pbkdf2(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
            
            // Codificar en Base64
            String saltEncoded = Base64.getEncoder().encodeToString(salt);
            String hashEncoded = Base64.getEncoder().encodeToString(hash);
            
            // Formato: PBKDF2$iteraciones$salt$hash
            String result = String.format("PBKDF2$%d$%s$%s", 
                    ITERATIONS, saltEncoded, hashEncoded);
            
            logger.debug("Contraseña hasheada exitosamente");
            return result;
            
        } catch (Exception e) {
            logger.error("Error al hashear contraseña", e);
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }
    
    /**
     * Verifica si una contraseña coincide con un hash.
     * 
     * @param password Contraseña en texto plano
     * @param hashedPassword Hash almacenado
     * @return true si la contraseña es correcta, false en caso contrario
     */
    public boolean verify(String password, String hashedPassword) {
        if (password == null || hashedPassword == null) {
            logger.warn("Password o hash nulo en verificación");
            return false;
        }
        
        try {
            // Parsear el hash almacenado
            String[] parts = hashedPassword.split("\\$");
            
            if (parts.length != 4) {
                logger.warn("Formato de hash inválido");
                return false;
            }
            
            String algorithm = parts[0];
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] hash = Base64.getDecoder().decode(parts[3]);
            
            // Validar algoritmo
            if (!"PBKDF2".equals(algorithm)) {
                logger.warn("Algoritmo de hash no soportado: {}", algorithm);
                return false;
            }
            
            // Generar hash con la contraseña proporcionada
            byte[] testHash = pbkdf2(password.toCharArray(), salt, iterations, KEY_LENGTH);
            
            // Comparación de tiempo constante para prevenir timing attacks
            boolean result = constantTimeEquals(hash, testHash);
            
            if (result) {
                logger.debug("✓ Contraseña verificada correctamente");
            } else {
                logger.debug("✗ Contraseña incorrecta");
            }
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error al verificar contraseña", e);
            return false;
        }
    }
    
    /**
     * Genera un hash PBKDF2.
     */
    private byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        
        KeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
        return factory.generateSecret(spec).getEncoded();
    }
    
    /**
     * Comparación de tiempo constante para prevenir timing attacks.
     * Compara dos arrays byte a byte sin salir prematuramente.
     */
    private boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        
        return result == 0;
    }
    
    /**
     * Valida la fortaleza de una contraseña.
     * 
     * @param password Contraseña a validar
     * @return true si la contraseña cumple los requisitos mínimos
     */
    public boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            else if (Character.isLowerCase(c)) hasLower = true;
            else if (Character.isDigit(c)) hasDigit = true;
            else hasSpecial = true;
        }
        
        // Requiere al menos 3 de 4 tipos de caracteres
        int score = (hasUpper ? 1 : 0) + (hasLower ? 1 : 0) + 
                    (hasDigit ? 1 : 0) + (hasSpecial ? 1 : 0);
        
        return score >= 3;
    }
    
    /**
     * Genera una contraseña temporal aleatoria.
     * Útil para funcionalidad de "recuperar contraseña".
     */
    public String generateTemporaryPassword(int length) {
        if (length < 8) {
            length = 8;
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
}