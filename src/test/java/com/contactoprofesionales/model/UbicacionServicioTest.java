package com.contactoprofesionales.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo UbicacionServicio.
 * Incluye casos positivos, negativos y límites.
 */
@DisplayName("Pruebas para UbicacionServicio")
public class UbicacionServicioTest {

    private UbicacionServicio ubicacion;

    @BeforeEach
    public void setUp() {
        ubicacion = new UbicacionServicio();
    }

    // ========================================================================
    // CASOS POSITIVOS
    // ========================================================================

    @Test
    @DisplayName("✓ Crear ubicación de tipo departamento válida")
    public void testCrearUbicacionDepartamento() {
        // Arrange & Act
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(1);

        // Assert
        assertTrue(ubicacion.isValid(), "Debe ser válida");
        assertEquals("departamento", ubicacion.getTipoUbicacion());
        assertEquals("Lima", ubicacion.getDepartamento());
    }

    @Test
    @DisplayName("✓ Crear ubicación de tipo provincia válida")
    public void testCrearUbicacionProvincia() {
        // Arrange & Act
        ubicacion.setTipoUbicacion("provincia");
        ubicacion.setDepartamento("Lima");
        ubicacion.setProvincia("Lima");
        ubicacion.setOrden(1);

        // Assert
        assertTrue(ubicacion.isValid(), "Debe ser válida");
        assertEquals("provincia", ubicacion.getTipoUbicacion());
    }

    @Test
    @DisplayName("✓ Crear ubicación de tipo distrito válida")
    public void testCrearUbicacionDistrito() {
        // Arrange & Act
        ubicacion.setTipoUbicacion("distrito");
        ubicacion.setDepartamento("Lima");
        ubicacion.setProvincia("Lima");
        ubicacion.setDistrito("Miraflores");
        ubicacion.setOrden(1);

        // Assert
        assertTrue(ubicacion.isValid(), "Debe ser válida");
        assertEquals("distrito", ubicacion.getTipoUbicacion());
        assertEquals("Miraflores", ubicacion.getDistrito());
    }

    @Test
    @DisplayName("✓ Constructor parametrizado")
    public void testConstructorParametrizado() {
        // Arrange & Act
        UbicacionServicio ubi = new UbicacionServicio(
            1, // areaServicioId
            "distrito",
            "Arequipa",
            "Arequipa",
            "Cayma",
            1
        );

        // Assert
        assertEquals(1, ubi.getAreaServicioId());
        assertEquals("distrito", ubi.getTipoUbicacion());
        assertEquals("Arequipa", ubi.getDepartamento());
        assertEquals("Arequipa", ubi.getProvincia());
        assertEquals("Cayma", ubi.getDistrito());
        assertEquals(1, ubi.getOrden());
        assertTrue(ubi.isValid());
    }

    @Test
    @DisplayName("✓ Obtener ubicación completa con todos los campos")
    public void testGetUbicacionCompletaConTodosCampos() {
        // Arrange
        ubicacion.setDepartamento("Cusco");
        ubicacion.setProvincia("Cusco");
        ubicacion.setDistrito("Wanchaq");

        // Act
        String ubicacionCompleta = ubicacion.getUbicacionCompleta();

        // Assert
        assertNotNull(ubicacionCompleta);
        assertTrue(ubicacionCompleta.contains("Wanchaq"));
        assertTrue(ubicacionCompleta.contains("Cusco"));
    }

    @Test
    @DisplayName("✓ Obtener ubicación completa solo con departamento")
    public void testGetUbicacionCompletaSoloDepartamento() {
        // Arrange
        ubicacion.setDepartamento("Puno");

        // Act
        String ubicacionCompleta = ubicacion.getUbicacionCompleta();

        // Assert
        assertEquals("Puno", ubicacionCompleta);
    }

    // ========================================================================
    // CASOS NEGATIVOS
    // ========================================================================

    @Test
    @DisplayName("✗ Ubicación sin tipo debe ser inválida")
    public void testUbicacionSinTipo() {
        // Arrange
        ubicacion.setTipoUbicacion(null);
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(1);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida sin tipo");
    }

    @Test
    @DisplayName("✗ Ubicación con tipo inválido debe ser inválida")
    public void testUbicacionConTipoInvalido() {
        // Arrange
        ubicacion.setTipoUbicacion("region"); // No permitido
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(1);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida con tipo incorrecto");
    }

    @Test
    @DisplayName("✗ Ubicación sin departamento debe ser inválida")
    public void testUbicacionSinDepartamento() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento(null);
        ubicacion.setOrden(1);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida sin departamento");
    }

    @Test
    @DisplayName("✗ Ubicación con departamento vacío debe ser inválida")
    public void testUbicacionConDepartamentoVacio() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("");
        ubicacion.setOrden(1);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida con departamento vacío");
    }

    @Test
    @DisplayName("✗ Ubicación con departamento solo espacios debe ser inválida")
    public void testUbicacionConDepartamentoSoloEspacios() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("   ");
        ubicacion.setOrden(1);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida con departamento solo espacios");
    }

    @Test
    @DisplayName("✗ Ubicación sin orden debe ser inválida")
    public void testUbicacionSinOrden() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(null);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida sin orden");
    }

    @Test
    @DisplayName("✗ Ubicación con orden cero debe ser inválida")
    public void testUbicacionConOrdenCero() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(0);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida con orden = 0");
    }

    @Test
    @DisplayName("✗ Ubicación con orden negativo debe ser inválida")
    public void testUbicacionConOrdenNegativo() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(-1);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida con orden negativo");
    }

    // ========================================================================
    // CASOS LÍMITE
    // ========================================================================

    @Test
    @DisplayName("⚠ Ubicación con orden = 1 (mínimo)")
    public void testUbicacionConOrdenMinimo() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(1);

        // Assert
        assertTrue(ubicacion.isValid(), "Debe ser válida con orden = 1");
    }

    @Test
    @DisplayName("⚠ Ubicación con orden = 10 (máximo)")
    public void testUbicacionConOrdenMaximo() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(10);

        // Assert
        assertTrue(ubicacion.isValid(), "Debe ser válida con orden = 10");
    }

    @Test
    @DisplayName("⚠ Ubicación con orden = 11 (fuera de rango)")
    public void testUbicacionConOrdenMayorDiez() {
        // Arrange
        ubicacion.setTipoUbicacion("departamento");
        ubicacion.setDepartamento("Lima");
        ubicacion.setOrden(11);

        // Assert
        assertFalse(ubicacion.isValid(), "Debe ser inválida con orden > 10");
    }

    @Test
    @DisplayName("⚠ Verificar ubicación completa con campos null")
    public void testUbicacionCompletaConCamposNull() {
        // Arrange
        ubicacion.setDepartamento("Lima");
        ubicacion.setProvincia(null);
        ubicacion.setDistrito(null);

        // Act
        String ubicacionCompleta = ubicacion.getUbicacionCompleta();

        // Assert
        assertEquals("Lima", ubicacionCompleta);
    }

    @Test
    @DisplayName("⚠ Verificar ubicación completa con campos vacíos")
    public void testUbicacionCompletaConCamposVacios() {
        // Arrange
        ubicacion.setDepartamento("Lima");
        ubicacion.setProvincia("");
        ubicacion.setDistrito("");

        // Act
        String ubicacionCompleta = ubicacion.getUbicacionCompleta();

        // Assert
        assertEquals("Lima", ubicacionCompleta);
    }

    @Test
    @DisplayName("⚠ Verificar toString")
    public void testToString() {
        // Arrange
        ubicacion.setId(1);
        ubicacion.setTipoUbicacion("distrito");
        ubicacion.setDepartamento("Lima");
        ubicacion.setProvincia("Lima");
        ubicacion.setDistrito("San Isidro");
        ubicacion.setOrden(5);

        // Act
        String toString = ubicacion.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("distrito"));
        assertTrue(toString.contains("orden=5"));
    }

    @Test
    @DisplayName("⚠ Verificar valores por defecto")
    public void testValoresPorDefecto() {
        // Arrange & Act
        UbicacionServicio ubi = new UbicacionServicio();

        // Assert
        assertNotNull(ubi.getFechaCreacion());
        assertTrue(ubi.getActivo());
    }

    @Test
    @DisplayName("⚠ Ubicación con todos los tipos permitidos")
    public void testTodosLosTiposPermitidos() {
        String[] tipos = {"departamento", "provincia", "distrito"};

        for (String tipo : tipos) {
            UbicacionServicio ubi = new UbicacionServicio();
            ubi.setTipoUbicacion(tipo);
            ubi.setDepartamento("Lima");
            ubi.setOrden(1);
            assertTrue(ubi.isValid(), "Tipo '" + tipo + "' debe ser válido");
        }
    }
}
