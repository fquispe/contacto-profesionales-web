package com.contactoprofesionales.service.auth;

import com.contactoprofesionales.dao.usuario.UsuarioDAO;
import com.contactoprofesionales.exception.AuthenticationException;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.util.PasswordHasher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests para AutenticacionServiceImpl usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class AutenticacionServiceImplTest {

    @Mock
    private UsuarioDAO usuarioDAO;

    private AutenticacionService autenticacionService;
    private PasswordHasher passwordHasher;

    @BeforeEach
    public void setUp() {
        autenticacionService = new AutenticacionServiceImpl(usuarioDAO);
        passwordHasher = new PasswordHasher();
    }

    @Test
    @DisplayName("Autenticar usuario válido debe retornar usuario")
    public void testAutenticar_UsuarioValido() throws AuthenticationException, DatabaseException {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String passwordHash = passwordHasher.hash(password);

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordHash);
        usuario.setActivo(true);

        when(usuarioDAO.buscarPorEmail(email)).thenReturn(usuario);

        // Act
        Usuario resultado = autenticacionService.autenticar(email, password);

        // Assert
        assertNotNull(resultado);
        assertEquals(email, resultado.getEmail());
        verify(usuarioDAO, times(1)).buscarPorEmail(email);
        verify(usuarioDAO, times(1)).actualizar(any(Usuario.class));
    }

    @Test
    @DisplayName("Autenticar con email inexistente debe lanzar excepción")
    public void testAutenticar_EmailInexistente() throws DatabaseException {
        // Arrange
        String email = "noexiste@example.com";
        String password = "password123";

        when(usuarioDAO.buscarPorEmail(email)).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.autenticar(email, password);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioDAO, times(1)).buscarPorEmail(email);
        verify(usuarioDAO, never()).actualizar(any(Usuario.class));
    }

    @Test
    @DisplayName("Autenticar con contraseña incorrecta debe lanzar excepción")
    public void testAutenticar_PasswordIncorrecta() throws DatabaseException {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String wrongPassword = "wrongpassword";
        String passwordHash = passwordHasher.hash(password);

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordHash);
        usuario.setActivo(true);

        when(usuarioDAO.buscarPorEmail(email)).thenReturn(usuario);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.autenticar(email, wrongPassword);
        });

        assertEquals("Credenciales inválidas", exception.getMessage());
        verify(usuarioDAO, times(1)).buscarPorEmail(email);
        verify(usuarioDAO, never()).actualizar(any(Usuario.class));
    }

    @Test
    @DisplayName("Autenticar usuario inactivo debe lanzar excepción")
    public void testAutenticar_UsuarioInactivo() throws DatabaseException {
        // Arrange
        String email = "test@example.com";
        String password = "password123";
        String passwordHash = passwordHasher.hash(password);

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setEmail(email);
        usuario.setPasswordHash(passwordHash);
        usuario.setActivo(false);

        when(usuarioDAO.buscarPorEmail(email)).thenReturn(usuario);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.autenticar(email, password);
        });

        assertEquals("Usuario inactivo. Contacte al administrador", exception.getMessage());
        verify(usuarioDAO, times(1)).buscarPorEmail(email);
        verify(usuarioDAO, never()).actualizar(any(Usuario.class));
    }

    @Test
    @DisplayName("Autenticar con email vacío debe lanzar excepción")
    public void testAutenticar_EmailVacio() {
        // Arrange
        String email = "";
        String password = "password123";

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.autenticar(email, password);
        });

        assertEquals("El email es requerido", exception.getMessage());
        verify(usuarioDAO, never()).buscarPorEmail(anyString());
    }

    @Test
    @DisplayName("Autenticar con password vacío debe lanzar excepción")
    public void testAutenticar_PasswordVacio() {
        // Arrange
        String email = "test@example.com";
        String password = "";

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.autenticar(email, password);
        });

        assertEquals("La contraseña es requerida", exception.getMessage());
        verify(usuarioDAO, never()).buscarPorEmail(anyString());
    }

    @Test
    @DisplayName("Autenticar con email formato inválido debe lanzar excepción")
    public void testAutenticar_EmailFormatoInvalido() {
        // Arrange
        String email = "emailinvalido";
        String password = "password123";

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.autenticar(email, password);
        });

        assertEquals("Formato de email inválido", exception.getMessage());
        verify(usuarioDAO, never()).buscarPorEmail(anyString());
    }

    @Test
    @DisplayName("Registrar usuario nuevo debe retornar usuario registrado")
    public void testRegistrar_UsuarioNuevo() throws AuthenticationException, DatabaseException {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("Test User");
        usuario.setEmail("test@example.com");
        usuario.setPasswordHash("password123"); // En texto plano para registro

        when(usuarioDAO.existeEmail(usuario.getEmail())).thenReturn(false);
        when(usuarioDAO.registrar(any(Usuario.class))).thenReturn(true);

        // Act
        Usuario resultado = autenticacionService.registrar(usuario);

        // Assert
        assertNotNull(resultado);
        assertNotEquals("password123", resultado.getPasswordHash(), "La contraseña debe estar hasheada");
        verify(usuarioDAO, times(1)).existeEmail(usuario.getEmail());
        verify(usuarioDAO, times(1)).registrar(any(Usuario.class));
    }

    @Test
    @DisplayName("Registrar usuario con email existente debe lanzar excepción")
    public void testRegistrar_EmailExistente() throws DatabaseException {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("Test User");
        usuario.setEmail("test@example.com");
        usuario.setPasswordHash("password123");

        when(usuarioDAO.existeEmail(usuario.getEmail())).thenReturn(true);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.registrar(usuario);
        });

        assertEquals("El email ya está registrado", exception.getMessage());
        verify(usuarioDAO, times(1)).existeEmail(usuario.getEmail());
        verify(usuarioDAO, never()).registrar(any(Usuario.class));
    }

    @Test
    @DisplayName("Registrar usuario con contraseña corta debe lanzar excepción")
    public void testRegistrar_PasswordCorta() throws DatabaseException {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setNombre("Test User");
        usuario.setEmail("test@example.com");
        usuario.setPasswordHash("12345"); // Menos de 6 caracteres

        when(usuarioDAO.existeEmail(usuario.getEmail())).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.registrar(usuario);
        });

        assertEquals("La contraseña debe tener al menos 6 caracteres", exception.getMessage());
        verify(usuarioDAO, times(1)).existeEmail(usuario.getEmail());
        verify(usuarioDAO, never()).registrar(any(Usuario.class));
    }

    @Test
    @DisplayName("Registrar usuario sin nombre debe lanzar excepción")
    public void testRegistrar_SinNombre() throws DatabaseException {
        // Arrange
        Usuario usuario = new Usuario();
        usuario.setEmail("test@example.com");
        usuario.setPasswordHash("password123");

        when(usuarioDAO.existeEmail(usuario.getEmail())).thenReturn(false);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.registrar(usuario);
        });

        assertEquals("El nombre es requerido", exception.getMessage());
        verify(usuarioDAO, never()).registrar(any(Usuario.class));
    }

    @Test
    @DisplayName("Registrar usuario null debe lanzar excepción")
    public void testRegistrar_UsuarioNull() throws DatabaseException {
        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.registrar(null);
        });

        assertEquals("Usuario nulo", exception.getMessage());
        verify(usuarioDAO, never()).registrar(any(Usuario.class));
    }

    @Test
    @DisplayName("Validar credenciales válidas no debe lanzar excepción")
    public void testValidarCredenciales_Validas() {
        // Arrange
        String email = "test@example.com";
        String password = "password123";

        // Act & Assert
        assertDoesNotThrow(() -> {
            autenticacionService.validarCredenciales(email, password);
        });
    }

    @Test
    @DisplayName("Validar credenciales con email null debe lanzar excepción")
    public void testValidarCredenciales_EmailNull() {
        // Arrange
        String password = "password123";

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.validarCredenciales(null, password);
        });

        assertEquals("El email es requerido", exception.getMessage());
    }

    @Test
    @DisplayName("Validar credenciales con password null debe lanzar excepción")
    public void testValidarCredenciales_PasswordNull() {
        // Arrange
        String email = "test@example.com";

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            autenticacionService.validarCredenciales(email, null);
        });

        assertEquals("La contraseña es requerida", exception.getMessage());
    }
}
