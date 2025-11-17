package com.contactoprofesionales.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

/**
 * Pruebas unitarias para el modelo AreaServicio.
 * Incluye casos positivos, negativos y límites.
 */
@DisplayName("Pruebas para AreaServicio")
public class AreaServicioTest {

    private AreaServicio areaServicio;

    @BeforeEach
    public void setUp() {
        areaServicio = new AreaServicio();
    }

    // ========================================================================
    // CASOS POSITIVOS
    // ========================================================================

    @Test
    @DisplayName("✓ Crear área de servicio para todo el país")
    public void testCrearAreaServicioTodoPais() {
        // Arrange & Act
        areaServicio.setProfesionalId(1);
        areaServicio.setTodoPais(true);

        // Assert
        assertTrue(areaServicio.isValid(), "Debe ser válida para todo el país");
        assertTrue(areaServicio.getTodoPais());
    }

    @Test
    @DisplayName("✓ Crear área de servicio con ubicaciones específicas")
    public void testCrearAreaServicioConUbicaciones() {
        // Arrange
        areaServicio.setProfesionalId(1);
        areaServicio.setTodoPais(false);

        UbicacionServicio ubicacion = new UbicacionServicio();
        ubicacion.setTipoUbicacion("distrito");
        ubicacion.setDepartamento("Lima");
        ubicacion.setProvincia("Lima");
        ubicacion.setDistrito("Miraflores");
        ubicacion.setOrden(1);

        // Act
        areaServicio.addUbicacion(ubicacion);

        // Assert
        assertTrue(areaServicio.isValid(), "Debe ser válida con ubicaciones");
        assertFalse(areaServicio.getTodoPais());
        assertEquals(1, areaServicio.getUbicaciones().size());
    }

    @Test
    @DisplayName("✓ Agregar múltiples ubicaciones")
    public void testAgregarMultiplesUbicaciones() {
        // Arrange
        areaServicio.setProfesionalId(1);
        areaServicio.setTodoPais(false);

        // Act
        for (int i = 1; i <= 5; i++) {
            UbicacionServicio ubicacion = new UbicacionServicio();
            ubicacion.setTipoUbicacion("distrito");
            ubicacion.setDepartamento("Lima");
            ubicacion.setProvincia("Lima");
            ubicacion.setDistrito("Distrito " + i);
            ubicacion.setOrden(i);
            areaServicio.addUbicacion(ubicacion);
        }

        // Assert
        assertTrue(areaServicio.isValid());
        assertEquals(5, areaServicio.getUbicaciones().size());
    }

    // ========================================================================
    // CASOS NEGATIVOS
    // ========================================================================

    @Test
    @DisplayName("✗ Área sin ubicaciones y sin todoPais debe ser inválida")
    public void testAreaSinUbicacionesYSinTodoPais() {
        // Arrange
        areaServicio.setProfesionalId(1);
        areaServicio.setTodoPais(false);
        // No agregar ubicaciones

        // Assert
        assertFalse(areaServicio.isValid(), "Debe ser inválida sin ubicaciones");
    }

    @Test
    @DisplayName("✗ Área con ubicaciones null y todoPais false debe ser inválida")
    public void testAreaConUbicacionesNullYTodoPaisFalse() {
        // Arrange
        areaServicio.setProfesionalId(1);
        areaServicio.setTodoPais(false);
        areaServicio.setUbicaciones(null);

        // Assert
        assertFalse(areaServicio.isValid(), "Debe ser inválida con ubicaciones null");
    }

    // ========================================================================
    // CASOS LÍMITE
    // ========================================================================

    @Test
    @DisplayName("⚠ Agregar exactamente 10 ubicaciones (máximo permitido)")
    public void testAgregarDiezUbicaciones() {
        // Arrange
        areaServicio.setProfesionalId(1);
        areaServicio.setTodoPais(false);

        // Act
        for (int i = 1; i <= 10; i++) {
            UbicacionServicio ubicacion = new UbicacionServicio();
            ubicacion.setTipoUbicacion("distrito");
            ubicacion.setDepartamento("Lima");
            ubicacion.setProvincia("Lima");
            ubicacion.setDistrito("Distrito " + i);
            ubicacion.setOrden(i);
            areaServicio.addUbicacion(ubicacion);
        }

        // Assert
        assertTrue(areaServicio.isValid());
        assertEquals(10, areaServicio.getUbicaciones().size());
    }

    @Test
    @DisplayName("⚠ Intentar agregar más de 10 ubicaciones no debe agregar la 11va")
    public void testAgregarMasDeDiezUbicaciones() {
        // Arrange
        areaServicio.setProfesionalId(1);
        areaServicio.setTodoPais(false);

        // Act
        for (int i = 1; i <= 12; i++) {
            UbicacionServicio ubicacion = new UbicacionServicio();
            ubicacion.setTipoUbicacion("distrito");
            ubicacion.setDepartamento("Lima");
            ubicacion.setProvincia("Lima");
            ubicacion.setDistrito("Distrito " + i);
            ubicacion.setOrden(i);
            areaServicio.addUbicacion(ubicacion);
        }

        // Assert
        assertEquals(10, areaServicio.getUbicaciones().size(), "No debe agregar más de 10 ubicaciones");
        assertFalse(areaServicio.isValid(), "Debe ser inválida con más de 10 ubicaciones");
    }

    @Test
    @DisplayName("⚠ Verificar toString")
    public void testToString() {
        // Arrange
        areaServicio.setId(1);
        areaServicio.setProfesionalId(10);
        areaServicio.setTodoPais(true);

        // Act
        String toString = areaServicio.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("profesionalId=10"));
        assertTrue(toString.contains("todoPais=true"));
    }

    @Test
    @DisplayName("⚠ Verificar valores por defecto")
    public void testValoresPorDefecto() {
        // Arrange & Act
        AreaServicio area = new AreaServicio();

        // Assert
        assertNotNull(area.getFechaCreacion());
        assertNotNull(area.getFechaActualizacion());
        assertTrue(area.getActivo());
        assertFalse(area.getTodoPais());
        assertNotNull(area.getUbicaciones());
        assertTrue(area.getUbicaciones().isEmpty());
    }

    @Test
    @DisplayName("⚠ Constructor parametrizado")
    public void testConstructorParametrizado() {
        // Arrange & Act
        AreaServicio area = new AreaServicio(5, true);

        // Assert
        assertEquals(5, area.getProfesionalId());
        assertTrue(area.getTodoPais());
        assertTrue(area.isValid());
    }
}
