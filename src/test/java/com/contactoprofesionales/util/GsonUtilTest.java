package com.contactoprofesionales.util;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

/**
 * Tests para GsonUtil.
 */
public class GsonUtilTest {

    private Gson gson;

    @BeforeEach
    public void setUp() {
        gson = GsonUtil.createGson();
    }

    @Test
    @DisplayName("Crear Gson debe retornar instancia no nula")
    public void testCreateGson_NoNulo() {
        // Assert
        assertNotNull(gson);
    }

    @Test
    @DisplayName("Serializar objeto simple debe generar JSON válido")
    public void testSerialize_ObjetoSimple() {
        // Arrange
        TestObject obj = new TestObject("Juan", 25);

        // Act
        String json = gson.toJson(obj);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("\"nombre\": \"Juan\""));
        assertTrue(json.contains("\"edad\": 25"));
    }

    @Test
    @DisplayName("Deserializar JSON simple debe retornar objeto correcto")
    public void testDeserialize_ObjetoSimple() {
        // Arrange
        String json = "{\"nombre\":\"Juan\",\"edad\":25}";

        // Act
        TestObject obj = gson.fromJson(json, TestObject.class);

        // Assert
        assertNotNull(obj);
        assertEquals("Juan", obj.nombre);
        assertEquals(25, obj.edad);
    }

    @Test
    @DisplayName("Serializar LocalDateTime debe usar formato ISO")
    public void testSerialize_LocalDateTime() {
        // Arrange
        LocalDateTime dateTime = LocalDateTime.of(2024, 1, 15, 10, 30, 45);
        TestObjectWithDate obj = new TestObjectWithDate("Test", dateTime);

        // Act
        String json = gson.toJson(obj);

        // Assert
        assertNotNull(json);
        assertTrue(json.contains("2024-01-15T10:30:45"));
    }

    @Test
    @DisplayName("Deserializar LocalDateTime debe parsear correctamente")
    public void testDeserialize_LocalDateTime() {
        // Arrange
        String json = "{\"nombre\":\"Test\",\"fecha\":\"2024-01-15T10:30:45\"}";

        // Act
        TestObjectWithDate obj = gson.fromJson(json, TestObjectWithDate.class);

        // Assert
        assertNotNull(obj);
        assertEquals("Test", obj.nombre);
        assertNotNull(obj.fecha);
        assertEquals(2024, obj.fecha.getYear());
        assertEquals(1, obj.fecha.getMonthValue());
        assertEquals(15, obj.fecha.getDayOfMonth());
        assertEquals(10, obj.fecha.getHour());
        assertEquals(30, obj.fecha.getMinute());
        assertEquals(45, obj.fecha.getSecond());
    }

    @Test
    @DisplayName("Serializar null debe generar JSON null")
    public void testSerialize_Null() {
        // Act
        String json = gson.toJson(null);

        // Assert
        assertEquals("null", json);
    }

    @Test
    @DisplayName("Deserializar JSON inválido debe lanzar excepción")
    public void testDeserialize_JSONInvalido() {
        // Arrange
        String invalidJson = "{invalid json}";

        // Act & Assert
        assertThrows(Exception.class, () -> {
            gson.fromJson(invalidJson, TestObject.class);
        });
    }

    @Test
    @DisplayName("Serializar y deserializar debe mantener los datos")
    public void testSerializeDeserialize_RoundTrip() {
        // Arrange
        TestObject original = new TestObject("Ana", 30);

        // Act
        String json = gson.toJson(original);
        TestObject deserializado = gson.fromJson(json, TestObject.class);

        // Assert
        assertEquals(original.nombre, deserializado.nombre);
        assertEquals(original.edad, deserializado.edad);
    }

    @Test
    @DisplayName("Serializar y deserializar LocalDateTime debe mantener el valor")
    public void testSerializeDeserialize_LocalDateTime_RoundTrip() {
        // Arrange
        LocalDateTime original = LocalDateTime.of(2024, 6, 15, 14, 30, 0);
        TestObjectWithDate obj = new TestObjectWithDate("Test", original);

        // Act
        String json = gson.toJson(obj);
        TestObjectWithDate deserializado = gson.fromJson(json, TestObjectWithDate.class);

        // Assert
        assertEquals(original, deserializado.fecha);
    }

    @Test
    @DisplayName("Gson debe usar pretty printing")
    public void testGson_UsaPrettyPrinting() {
        // Arrange
        TestObject obj = new TestObject("Juan", 25);

        // Act
        String json = gson.toJson(obj);

        // Assert
        assertTrue(json.contains("\n"), "El JSON debe tener saltos de línea (pretty printing)");
    }

    @Test
    @DisplayName("Crear múltiples instancias de Gson debe ser posible")
    public void testCreateGson_MultipleInstancias() {
        // Act
        Gson gson1 = GsonUtil.createGson();
        Gson gson2 = GsonUtil.createGson();

        // Assert
        assertNotNull(gson1);
        assertNotNull(gson2);
        // Pueden ser diferentes instancias
        assertNotSame(gson1, gson2);
    }

    // Clases de prueba
    private static class TestObject {
        String nombre;
        int edad;

        TestObject(String nombre, int edad) {
            this.nombre = nombre;
            this.edad = edad;
        }

        TestObject() {}
    }

    private static class TestObjectWithDate {
        String nombre;
        LocalDateTime fecha;

        TestObjectWithDate(String nombre, LocalDateTime fecha) {
            this.nombre = nombre;
            this.fecha = fecha;
        }

        TestObjectWithDate() {}
    }
}
