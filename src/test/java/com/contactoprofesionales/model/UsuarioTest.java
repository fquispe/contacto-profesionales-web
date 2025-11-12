package com.contactoprofesionales.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Tests para el modelo Usuario.
 */
public class UsuarioTest {

    @Test
    @DisplayName("Constructor vacío debe crear usuario con valores por defecto")
    public void testConstructorVacio() {
        // Act
        Usuario usuario = new Usuario();

        // Assert
        assertNotNull(usuario);
        assertNull(usuario.getId());
        assertNull(usuario.getNombre());
        assertNull(usuario.getEmail());
        assertFalse(usuario.isActivo());
    }

    @Test
    @DisplayName("Constructor con parámetros debe crear usuario activo")
    public void testConstructorConParametros() {
        // Arrange
        String nombre = "Juan Pérez";
        String email = "juan@example.com";
        String password = "hashedpassword";

        // Act
        Usuario usuario = new Usuario(nombre, email, password);

        // Assert
        assertNotNull(usuario);
        assertEquals(nombre, usuario.getNombre());
        assertEquals(email, usuario.getEmail());
        assertEquals(password, usuario.getPasswordHash());
        assertTrue(usuario.isActivo(), "Usuario debe estar activo por defecto");
    }

    @Test
    @DisplayName("Constructor completo debe asignar todos los valores")
    public void testConstructorCompleto() {
        // Arrange
        Integer id = 1;
        String nombre = "Juan Pérez";
        String email = "juan@example.com";
        String password = "hashedpassword";
        String telefono = "987654321";
        boolean activo = true;

        // Act
        Usuario usuario = new Usuario(id, nombre, email, password, telefono, activo);

        // Assert
        assertEquals(id, usuario.getId());
        assertEquals(nombre, usuario.getNombre());
        assertEquals(email, usuario.getEmail());
        assertEquals(password, usuario.getPasswordHash());
        assertEquals(telefono, usuario.getTelefono());
        assertEquals(activo, usuario.isActivo());
    }

    @Test
    @DisplayName("Setters y getters deben funcionar correctamente")
    public void testSettersGetters() {
        // Arrange
        Usuario usuario = new Usuario();
        Integer id = 1;
        String nombre = "Juan Pérez";
        String email = "juan@example.com";
        String password = "hashedpassword";
        String telefono = "987654321";
        LocalDateTime fechaRegistro = LocalDateTime.now();
        LocalDateTime ultimoAcceso = LocalDateTime.now();
        boolean activo = true;

        // Act
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPasswordHash(password);
        usuario.setTelefono(telefono);
        usuario.setFechaRegistro(fechaRegistro);
        usuario.setUltimoAcceso(ultimoAcceso);
        usuario.setActivo(activo);

        // Assert
        assertEquals(id, usuario.getId());
        assertEquals(nombre, usuario.getNombre());
        assertEquals(email, usuario.getEmail());
        assertEquals(password, usuario.getPasswordHash());
        assertEquals(telefono, usuario.getTelefono());
        assertEquals(fechaRegistro, usuario.getFechaRegistro());
        assertEquals(ultimoAcceso, usuario.getUltimoAcceso());
        assertEquals(activo, usuario.isActivo());
    }

    @Test
    @DisplayName("ToString debe contener información del usuario")
    public void testToString() {
        // Arrange
        Usuario usuario = new Usuario("Juan Pérez", "juan@example.com", "password");
        usuario.setId(1);

        // Act
        String toString = usuario.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("Usuario{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("nombre='Juan Pérez'"));
        assertTrue(toString.contains("email='juan@example.com'"));
    }

    @Test
    @DisplayName("Usuario puede tener fechas null")
    public void testFechasNull() {
        // Arrange
        Usuario usuario = new Usuario();

        // Act & Assert
        assertNull(usuario.getFechaRegistro());
        assertNull(usuario.getUltimoAcceso());
    }

    @Test
    @DisplayName("Usuario puede cambiar de activo a inactivo")
    public void testCambiarEstadoActivo() {
        // Arrange
        Usuario usuario = new Usuario("Juan", "juan@example.com", "password");

        // Act
        assertTrue(usuario.isActivo());
        usuario.setActivo(false);

        // Assert
        assertFalse(usuario.isActivo());
    }

    @Test
    @DisplayName("Modificar email debe actualizarse correctamente")
    public void testModificarEmail() {
        // Arrange
        Usuario usuario = new Usuario("Juan", "juan@example.com", "password");
        String nuevoEmail = "nuevo@example.com";

        // Act
        usuario.setEmail(nuevoEmail);

        // Assert
        assertEquals(nuevoEmail, usuario.getEmail());
    }

    @Test
    @DisplayName("Modificar password debe actualizarse correctamente")
    public void testModificarPassword() {
        // Arrange
        Usuario usuario = new Usuario("Juan", "juan@example.com", "oldpassword");
        String nuevoPassword = "newhashedpassword";

        // Act
        usuario.setPasswordHash(nuevoPassword);

        // Assert
        assertEquals(nuevoPassword, usuario.getPasswordHash());
    }
}
