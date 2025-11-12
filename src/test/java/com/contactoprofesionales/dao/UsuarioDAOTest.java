package com.contactoprofesionales.dao;

import com.contactoprofesionales.dao.usuario.UsuarioDAO;
import com.contactoprofesionales.dao.usuario.UsuarioDAOImpl;
import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.exception.DatabaseException;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para UsuarioDAO.
 */
public class UsuarioDAOTest {
    
    private UsuarioDAO usuarioDAO;

    @BeforeEach
    public void setUp() {
        usuarioDAO = new UsuarioDAOImpl();
    }

    @Test
    @DisplayName("Buscar usuario por email existente")
    public void testBuscarPorEmail_Existente() throws DatabaseException {
        // Arrange
        String email = "juan@example.com";
        
        // Act
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        /*
        // Assert
        assertNotNull(usuario, "Usuario no debe ser nulo");
        assertEquals(email, usuario.getEmail());
        assertTrue(usuario.isActivo());
        */
    }

    @Test
    @DisplayName("Buscar usuario por email inexistente")
    public void testBuscarPorEmail_Inexistente() throws DatabaseException {
        // Arrange
        String email = "noexiste@example.com";
        
        // Act
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        
        // Assert
        assertNull(usuario, "Usuario debe ser nulo");
    }

    @Test
    @DisplayName("Verificar existencia de email")
    public void testExisteEmail() throws DatabaseException {
        // Arrange
        String emailExistente = "juan@example.com";
        String emailInexistente = "noexiste@example.com";
        
        // Act & Assert
        assertTrue(usuarioDAO.existeEmail(emailExistente));
        assertFalse(usuarioDAO.existeEmail(emailInexistente));
    }
}
