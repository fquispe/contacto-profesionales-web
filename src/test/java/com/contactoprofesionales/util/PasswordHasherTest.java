package com.contactoprofesionales.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para PasswordHasher.
 */
public class PasswordHasherTest {

    private PasswordHasher passwordHasher;

    @BeforeEach
    public void setUp() {
        passwordHasher = new PasswordHasher();
    }

    @Test
    @DisplayName("Hash de contraseña debe ser diferente del texto plano")
    public void testHash_DiferenteDelTextoPlano() {
        // Arrange
        String password = "miPassword123";

        // Act
        String hash = passwordHasher.hash(password);

        // Assert
        assertNotNull(hash);
        assertNotEquals(password, hash);
        assertTrue(hash.startsWith("PBKDF2$"));
    }

    @Test
    @DisplayName("Hash de la misma contraseña debe generar salts diferentes")
    public void testHash_GeneraSaltsDiferentes() {
        // Arrange
        String password = "miPassword123";

        // Act
        String hash1 = passwordHasher.hash(password);
        String hash2 = passwordHasher.hash(password);

        // Assert
        assertNotEquals(hash1, hash2, "Los hashes deben ser diferentes debido a salts únicos");
    }

    @Test
    @DisplayName("Verificar contraseña correcta debe retornar true")
    public void testVerify_PasswordCorrecta() {
        // Arrange
        String password = "miPassword123";
        String hash = passwordHasher.hash(password);

        // Act
        boolean result = passwordHasher.verify(password, hash);

        // Assert
        assertTrue(result, "La verificación debe ser exitosa con contraseña correcta");
    }

    @Test
    @DisplayName("Verificar contraseña incorrecta debe retornar false")
    public void testVerify_PasswordIncorrecta() {
        // Arrange
        String password = "miPassword123";
        String wrongPassword = "passwordIncorrecta";
        String hash = passwordHasher.hash(password);

        // Act
        boolean result = passwordHasher.verify(wrongPassword, hash);

        // Assert
        assertFalse(result, "La verificación debe fallar con contraseña incorrecta");
    }

    @Test
    @DisplayName("Hash de contraseña vacía debe lanzar excepción")
    public void testHash_PasswordVacia() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            passwordHasher.hash("");
        });
    }

    @Test
    @DisplayName("Hash de contraseña null debe lanzar excepción")
    public void testHash_PasswordNull() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            passwordHasher.hash(null);
        });
    }

    @Test
    @DisplayName("Verificar con hash null debe retornar false")
    public void testVerify_HashNull() {
        // Arrange
        String password = "miPassword123";

        // Act
        boolean result = passwordHasher.verify(password, null);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Verificar con password null debe retornar false")
    public void testVerify_PasswordNull() {
        // Arrange
        String hash = "PBKDF2$65536$someBase64Salt$someBase64Hash";

        // Act
        boolean result = passwordHasher.verify(null, hash);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Verificar con formato de hash inválido debe retornar false")
    public void testVerify_FormatoHashInvalido() {
        // Arrange
        String password = "miPassword123";
        String hashInvalido = "formatoInvalido";

        // Act
        boolean result = passwordHasher.verify(password, hashInvalido);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Validar contraseña fuerte debe retornar true")
    public void testIsStrongPassword_Fuerte() {
        // Arrange
        String strongPassword = "MyP@ssw0rd123";

        // Act
        boolean result = passwordHasher.isStrongPassword(strongPassword);

        // Assert
        assertTrue(result, "La contraseña debe ser considerada fuerte");
    }

    @Test
    @DisplayName("Validar contraseña débil debe retornar false")
    public void testIsStrongPassword_Debil() {
        // Arrange
        String weakPassword = "123456";

        // Act
        boolean result = passwordHasher.isStrongPassword(weakPassword);

        // Assert
        assertFalse(result, "La contraseña debe ser considerada débil");
    }

    @Test
    @DisplayName("Validar contraseña corta debe retornar false")
    public void testIsStrongPassword_Corta() {
        // Arrange
        String shortPassword = "Pass1!";

        // Act
        boolean result = passwordHasher.isStrongPassword(shortPassword);

        // Assert
        assertFalse(result, "La contraseña debe ser considerada débil por ser corta");
    }

    @Test
    @DisplayName("Validar contraseña null debe retornar false")
    public void testIsStrongPassword_Null() {
        // Act
        boolean result = passwordHasher.isStrongPassword(null);

        // Assert
        assertFalse(result);
    }

    @Test
    @DisplayName("Generar contraseña temporal debe tener longitud mínima de 8")
    public void testGenerateTemporaryPassword_LongitudMinima() {
        // Act
        String tempPassword = passwordHasher.generateTemporaryPassword(5);

        // Assert
        assertNotNull(tempPassword);
        assertTrue(tempPassword.length() >= 8, "Longitud mínima debe ser 8");
    }

    @Test
    @DisplayName("Generar contraseña temporal debe tener longitud especificada")
    public void testGenerateTemporaryPassword_LongitudEspecificada() {
        // Arrange
        int length = 12;

        // Act
        String tempPassword = passwordHasher.generateTemporaryPassword(length);

        // Assert
        assertNotNull(tempPassword);
        assertEquals(length, tempPassword.length());
    }

    @Test
    @DisplayName("Generar contraseñas temporales debe generar diferentes contraseñas")
    public void testGenerateTemporaryPassword_Diferentes() {
        // Act
        String tempPassword1 = passwordHasher.generateTemporaryPassword(12);
        String tempPassword2 = passwordHasher.generateTemporaryPassword(12);

        // Assert
        assertNotEquals(tempPassword1, tempPassword2, "Las contraseñas temporales deben ser diferentes");
    }
}
