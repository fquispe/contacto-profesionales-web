package com.contactoprofesionales.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests para el modelo Cliente.
 */
public class ClienteTest {

    @Test
    @DisplayName("Constructor vacío debe crear cliente activo con fecha de registro")
    public void testConstructorVacio() {
        // Act
        Cliente cliente = new Cliente();

        // Assert
        assertNotNull(cliente);
        assertTrue(cliente.getActivo(), "Cliente debe estar activo por defecto");
        assertNotNull(cliente.getFechaRegistro(), "Cliente debe tener fecha de registro");
    }

    @Test
    @DisplayName("Constructor con parámetros debe asignar valores correctamente")
    public void testConstructorConParametros() {
        // Arrange
        String nombreCompleto = "Juan Pérez";
        String email = "juan@example.com";
        String telefono = "987654321";

        // Act
        Cliente cliente = new Cliente(nombreCompleto, email, telefono);

        // Assert
        assertEquals(nombreCompleto, cliente.getNombreCompleto());
        assertEquals(email, cliente.getEmail());
        assertEquals(telefono, cliente.getTelefono());
        assertTrue(cliente.getActivo());
        assertNotNull(cliente.getFechaRegistro());
    }

    @Test
    @DisplayName("Setters y getters básicos deben funcionar correctamente")
    public void testSettersGettersBasicos() {
        // Arrange
        Cliente cliente = new Cliente();
        Long id = 1L;
        String nombreCompleto = "Juan Pérez";
        String email = "juan@example.com";
        String telefono = "987654321";
        String fotoPerfilUrl = "https://example.com/foto.jpg";

        // Act
        cliente.setId(id);
        cliente.setNombreCompleto(nombreCompleto);
        cliente.setEmail(email);
        cliente.setTelefono(telefono);
        cliente.setFotoPerfilUrl(fotoPerfilUrl);

        // Assert
        assertEquals(id, cliente.getId());
        assertEquals(nombreCompleto, cliente.getNombreCompleto());
        assertEquals(email, cliente.getEmail());
        assertEquals(telefono, cliente.getTelefono());
        assertEquals(fotoPerfilUrl, cliente.getFotoPerfilUrl());
    }

    @Test
    @DisplayName("Setters y getters de preferencias de búsqueda deben funcionar")
    public void testSettersGettersPreferenciasBusqueda() {
        // Arrange
        Cliente cliente = new Cliente();
        String categorias = "[\"Electricista\",\"Plomero\"]";
        Integer radio = 10;
        Double presupuesto = 500.0;

        // Act
        cliente.setCategoriasFavoritas(categorias);
        cliente.setRadioBusqueda(radio);
        cliente.setPresupuestoPromedio(presupuesto);

        // Assert
        assertEquals(categorias, cliente.getCategoriasFavoritas());
        assertEquals(radio, cliente.getRadioBusqueda());
        assertEquals(presupuesto, cliente.getPresupuestoPromedio());
    }

    @Test
    @DisplayName("Setters y getters de notificaciones deben funcionar")
    public void testSettersGettersNotificaciones() {
        // Arrange
        Cliente cliente = new Cliente();

        // Act
        cliente.setNotificacionesEmail(true);
        cliente.setNotificacionesPush(false);
        cliente.setNotificacionesPromociones(true);
        cliente.setNotificacionesResenas(false);

        // Assert
        assertTrue(cliente.getNotificacionesEmail());
        assertFalse(cliente.getNotificacionesPush());
        assertTrue(cliente.getNotificacionesPromociones());
        assertFalse(cliente.getNotificacionesResenas());
    }

    @Test
    @DisplayName("Setters y getters de privacidad deben funcionar")
    public void testSettersGettersPrivacidad() {
        // Arrange
        Cliente cliente = new Cliente();

        // Act
        cliente.setPerfilVisible(true);
        cliente.setCompartirUbicacion(false);
        cliente.setHistorialPublico(true);

        // Assert
        assertTrue(cliente.getPerfilVisible());
        assertFalse(cliente.getCompartirUbicacion());
        assertTrue(cliente.getHistorialPublico());
    }

    @Test
    @DisplayName("Setters y getters de fechas de auditoría deben funcionar")
    public void testSettersGettersFechasAuditoria() {
        // Arrange
        Cliente cliente = new Cliente();
        LocalDateTime fechaRegistro = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime fechaActualizacion = LocalDateTime.of(2024, 1, 15, 14, 30);

        // Act
        cliente.setFechaRegistro(fechaRegistro);
        cliente.setFechaActualizacion(fechaActualizacion);

        // Assert
        assertEquals(fechaRegistro, cliente.getFechaRegistro());
        assertEquals(fechaActualizacion, cliente.getFechaActualizacion());
    }

    @Test
    @DisplayName("Cliente puede tener lista de direcciones")
    public void testDirecciones() {
        // Arrange
        Cliente cliente = new Cliente();
        List<DireccionCliente> direcciones = new ArrayList<>();
        DireccionCliente direccion1 = new DireccionCliente();
        direccion1.setDireccionCompleta("Av. Principal 123");
        direcciones.add(direccion1);

        // Act
        cliente.setDirecciones(direcciones);

        // Assert
        assertNotNull(cliente.getDirecciones());
        assertEquals(1, cliente.getDirecciones().size());
        assertEquals("Av. Principal 123", cliente.getDirecciones().get(0).getDireccionCompleta());
    }

    @Test
    @DisplayName("Cliente puede cambiar de activo a inactivo")
    public void testCambiarEstadoActivo() {
        // Arrange
        Cliente cliente = new Cliente();

        // Act
        assertTrue(cliente.getActivo());
        cliente.setActivo(false);

        // Assert
        assertFalse(cliente.getActivo());
    }

    @Test
    @DisplayName("ToString debe contener información básica del cliente")
    public void testToString() {
        // Arrange
        Cliente cliente = new Cliente("Juan Pérez", "juan@example.com", "987654321");
        cliente.setId(1L);
        cliente.setRadioBusqueda(10);

        // Act
        String toString = cliente.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("Cliente{"));
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("nombreCompleto='Juan Pérez'"));
        assertTrue(toString.contains("email='juan@example.com'"));
        assertTrue(toString.contains("telefono='987654321'"));
        assertTrue(toString.contains("radioBusqueda=10"));
    }

    @Test
    @DisplayName("Cliente puede tener valores null en campos opcionales")
    public void testCamposOpcionalesNull() {
        // Arrange
        Cliente cliente = new Cliente();

        // Assert
        assertNull(cliente.getId());
        assertNull(cliente.getFotoPerfilUrl());
        assertNull(cliente.getCategoriasFavoritas());
        assertNull(cliente.getRadioBusqueda());
        assertNull(cliente.getPresupuestoPromedio());
        assertNull(cliente.getDirecciones());
    }

    @Test
    @DisplayName("Modificar email debe actualizarse correctamente")
    public void testModificarEmail() {
        // Arrange
        Cliente cliente = new Cliente("Juan", "juan@example.com", "987654321");
        String nuevoEmail = "nuevo@example.com";

        // Act
        cliente.setEmail(nuevoEmail);

        // Assert
        assertEquals(nuevoEmail, cliente.getEmail());
    }

    @Test
    @DisplayName("Modificar teléfono debe actualizarse correctamente")
    public void testModificarTelefono() {
        // Arrange
        Cliente cliente = new Cliente("Juan", "juan@example.com", "987654321");
        String nuevoTelefono = "912345678";

        // Act
        cliente.setTelefono(nuevoTelefono);

        // Assert
        assertEquals(nuevoTelefono, cliente.getTelefono());
    }
}
