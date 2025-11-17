package com.contactoprofesionales.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo EspecialidadProfesional.
 * Incluye casos positivos, negativos y límites.
 */
@DisplayName("Pruebas para EspecialidadProfesional")
public class EspecialidadProfesionalTest {

    private EspecialidadProfesional especialidad;

    @BeforeEach
    public void setUp() {
        especialidad = new EspecialidadProfesional();
    }

    // ========================================================================
    // CASOS POSITIVOS - Operaciones exitosas
    // ========================================================================

    @Test
    @DisplayName("✓ Crear especialidad con todos los campos válidos")
    public void testCrearEspecialidadValida() {
        // Arrange & Act
        especialidad.setProfesionalId(1);
        especialidad.setCategoriaId(10);
        especialidad.setServicioProfesional("Reparación de griferías");
        especialidad.setDescripcion("Reparación profesional de todo tipo de griferías");
        especialidad.setIncluyeMateriales(true);
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setEsPrincipal(true);
        especialidad.setOrden(1);
        especialidad.setTrabajoPresencial(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid(), "La especialidad debe ser válida");
        assertEquals(1, especialidad.getProfesionalId());
        assertEquals(10, especialidad.getCategoriaId());
        assertEquals("Reparación de griferías", especialidad.getServicioProfesional());
        assertEquals(50.0, especialidad.getCosto());
        assertEquals("hora", especialidad.getTipoCosto());
        assertTrue(especialidad.getEsPrincipal());
        assertEquals(1, especialidad.getOrden());
    }

    @Test
    @DisplayName("✓ Crear especialidad con constructor parametrizado")
    public void testCrearEspecialidadConConstructor() {
        // Arrange & Act
        EspecialidadProfesional esp = new EspecialidadProfesional(
            1, // profesionalId
            5, // categoriaId
            "Instalación de sistemas eléctricos", // descripcion
            true, // incluyeMateriales
            100.0, // costo
            "dia", // tipoCosto
            true, // esPrincipal
            1 // orden
        );
        esp.setServicioProfesional("Instalación eléctrica residencial");
        esp.setTrabajoPresencial(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(esp.isValid());
        assertEquals(1, esp.getProfesionalId());
        assertEquals(5, esp.getCategoriaId());
        assertEquals("Instalación eléctrica residencial", esp.getServicioProfesional());
    }

    @Test
    @DisplayName("✓ Validar especialidad con tipo de costo 'hora'")
    public void testTipoCostoHora() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Consultoría técnica");
        especialidad.setCosto(75.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid());
        assertEquals("hora", especialidad.getTipoCosto());
    }

    @Test
    @DisplayName("✓ Validar especialidad con tipo de costo 'dia'")
    public void testTipoCostoDia() {
        // Arrange
        especialidad.setCategoriaId(2);
        especialidad.setServicioProfesional("Mantenimiento preventivo");
        especialidad.setCosto(200.0);
        especialidad.setTipoCosto("dia");
        especialidad.setOrden(2);
        especialidad.setTrabajoPresencial(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid());
        assertEquals("dia", especialidad.getTipoCosto());
    }

    @Test
    @DisplayName("✓ Validar especialidad con tipo de costo 'mes'")
    public void testTipoCostoMes() {
        // Arrange
        especialidad.setCategoriaId(3);
        especialidad.setServicioProfesional("Soporte técnico mensual");
        especialidad.setCosto(1500.0);
        especialidad.setTipoCosto("mes");
        especialidad.setOrden(3);
        especialidad.setTrabajoRemoto(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid());
        assertEquals("mes", especialidad.getTipoCosto());
    }

    @Test
    @DisplayName("✓ Especialidad sin incluir materiales")
    public void testEspecialidadSinMateriales() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Asesoría legal");
        especialidad.setIncluyeMateriales(false);
        especialidad.setCosto(100.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid());
        assertFalse(especialidad.getIncluyeMateriales());
    }

    // ========================================================================
    // CASOS NEGATIVOS - Validaciones con errores controlados
    // ========================================================================

    @Test
    @DisplayName("✗ Especialidad sin categoriaId debe ser inválida")
    public void testEspecialidadSinCategoriaId() {
        // Arrange
        especialidad.setCategoriaId(null);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida sin categoriaId");
    }

    @Test
    @DisplayName("✗ Especialidad con categoriaId cero debe ser inválida")
    public void testEspecialidadConCategoriaIdCero() {
        // Arrange
        especialidad.setCategoriaId(0);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con categoriaId = 0");
    }

    @Test
    @DisplayName("✗ Especialidad con categoriaId negativo debe ser inválida")
    public void testEspecialidadConCategoriaIdNegativo() {
        // Arrange
        especialidad.setCategoriaId(-5);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con categoriaId negativo");
    }

    @Test
    @DisplayName("✗ Especialidad sin servicioProfesional debe ser inválida")
    public void testEspecialidadSinServicioProfesional() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional(null);
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida sin servicioProfesional");
    }

    @Test
    @DisplayName("✗ Especialidad con servicioProfesional vacío debe ser inválida")
    public void testEspecialidadConServicioProfesionalVacio() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con servicioProfesional vacío");
    }

    @Test
    @DisplayName("✗ Especialidad con servicioProfesional solo espacios debe ser inválida")
    public void testEspecialidadConServicioProfesionalSoloEspacios() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("   ");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con servicioProfesional solo espacios");
    }

    @Test
    @DisplayName("✗ Especialidad sin costo debe ser inválida")
    public void testEspecialidadSinCosto() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(null);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida sin costo");
    }

    @Test
    @DisplayName("✗ Especialidad con costo cero debe ser inválida")
    public void testEspecialidadConCostoCero() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(0.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con costo = 0");
    }

    @Test
    @DisplayName("✗ Especialidad con costo negativo debe ser inválida")
    public void testEspecialidadConCostoNegativo() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(-10.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con costo negativo");
    }

    @Test
    @DisplayName("✗ Especialidad sin tipoCosto debe ser inválida")
    public void testEspecialidadSinTipoCosto() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto(null);
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida sin tipoCosto");
    }

    @Test
    @DisplayName("✗ Especialidad con tipoCosto inválido debe ser inválida")
    public void testEspecialidadConTipoCostoInvalido() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("semana"); // tipo no permitido
        especialidad.setOrden(1);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con tipoCosto = 'semana'");
    }

    @Test
    @DisplayName("✗ Especialidad sin orden debe ser inválida")
    public void testEspecialidadSinOrden() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(null);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida sin orden");
    }

    // ========================================================================
    // CASOS LÍMITE - Datos en los límites permitidos
    // ========================================================================

    @Test
    @DisplayName("⚠ Especialidad con orden = 1 (mínimo permitido)")
    public void testEspecialidadConOrdenMinimo() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoPresencial(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid(), "Debe ser válida con orden = 1");
    }

    @Test
    @DisplayName("⚠ Especialidad con orden = 3 (máximo permitido)")
    public void testEspecialidadConOrdenMaximo() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(3);
        especialidad.setTrabajoPresencial(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid(), "Debe ser válida con orden = 3");
    }

    @Test
    @DisplayName("⚠ Especialidad con orden = 0 (fuera de rango)")
    public void testEspecialidadConOrdenCero() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(0);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con orden = 0");
    }

    @Test
    @DisplayName("⚠ Especialidad con orden = 4 (fuera de rango)")
    public void testEspecialidadConOrdenMayorTres() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(4);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con orden = 4");
    }

    @Test
    @DisplayName("⚠ Especialidad con costo muy pequeño (0.01)")
    public void testEspecialidadConCostoMuyPequeno() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(0.01);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid(), "Debe ser válida con costo = 0.01");
    }

    @Test
    @DisplayName("⚠ Especialidad con costo muy grande (999999.99)")
    public void testEspecialidadConCostoMuyGrande() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(999999.99);
        especialidad.setTipoCosto("mes");
        especialidad.setOrden(1);
        especialidad.setTrabajoPresencial(true); // ✅ ACTUALIZADO: añadir modalidad de trabajo

        // Assert
        assertTrue(especialidad.isValid(), "Debe ser válida con costo muy grande");
    }

    @Test
    @DisplayName("⚠ Verificar valores por defecto en constructor vacío")
    public void testValoresPorDefecto() {
        // Arrange & Act
        EspecialidadProfesional esp = new EspecialidadProfesional();

        // Assert
        assertNotNull(esp.getFechaCreacion(), "fechaCreacion debe inicializarse");
        assertNotNull(esp.getFechaActualizacion(), "fechaActualizacion debe inicializarse");
        assertTrue(esp.getActivo(), "activo debe ser true por defecto");
        assertFalse(esp.getEsPrincipal(), "esPrincipal debe ser false por defecto");
        assertFalse(esp.getIncluyeMateriales(), "incluyeMateriales debe ser false por defecto");
        // ✅ NUEVOS CAMPOS - Tipo de prestación de trabajo (añadido: 2025-11-14)
        assertFalse(esp.getTrabajoRemoto(), "trabajoRemoto debe ser false por defecto");
        assertFalse(esp.getTrabajoPresencial(), "trabajoPresencial debe ser false por defecto");
    }

    @Test
    @DisplayName("⚠ Verificar método toString")
    public void testToString() {
        // Arrange
        especialidad.setId(1);
        especialidad.setProfesionalId(10);
        especialidad.setCategoriaId(5);
        especialidad.setServicioProfesional("Reparación");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);

        // Act
        String toString = especialidad.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("profesionalId=10"));
        assertTrue(toString.contains("categoriaId=5"));
        assertTrue(toString.contains("Reparación"));
    }

    @Test
    @DisplayName("⚠ Setters y Getters para campos transientes de categoría")
    public void testCamposTransientesCategoria() {
        // Arrange & Act
        especialidad.setCategoriaNombre("Plomería");
        especialidad.setCategoriaDescripcion("Servicios de plomería");
        especialidad.setCategoriaIcono("plumbing-icon");
        especialidad.setCategoriaColor("#0000FF");

        // Assert
        assertEquals("Plomería", especialidad.getCategoriaNombre());
        assertEquals("Servicios de plomería", especialidad.getCategoriaDescripcion());
        assertEquals("plumbing-icon", especialidad.getCategoriaIcono());
        assertEquals("#0000FF", especialidad.getCategoriaColor());
    }

    // ========================================================================
    // ✅ NUEVAS PRUEBAS - Tipo de prestación de trabajo (añadido: 2025-11-14)
    // ========================================================================

    @Test
    @DisplayName("✓ Especialidad con trabajo remoto únicamente debe ser válida")
    public void testEspecialidadConTrabajoRemoto() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Consultoría en línea");
        especialidad.setCosto(80.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(true);
        especialidad.setTrabajoPresencial(false);

        // Assert
        assertTrue(especialidad.isValid(), "Debe ser válida con solo trabajo remoto");
        assertTrue(especialidad.getTrabajoRemoto());
        assertFalse(especialidad.getTrabajoPresencial());
    }

    @Test
    @DisplayName("✓ Especialidad con trabajo presencial únicamente debe ser válida")
    public void testEspecialidadConTrabajoPresencial() {
        // Arrange
        especialidad.setCategoriaId(2);
        especialidad.setServicioProfesional("Reparación a domicilio");
        especialidad.setCosto(100.0);
        especialidad.setTipoCosto("dia");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(false);
        especialidad.setTrabajoPresencial(true);

        // Assert
        assertTrue(especialidad.isValid(), "Debe ser válida con solo trabajo presencial");
        assertFalse(especialidad.getTrabajoRemoto());
        assertTrue(especialidad.getTrabajoPresencial());
    }

    @Test
    @DisplayName("✓ Especialidad con ambas modalidades de trabajo debe ser válida")
    public void testEspecialidadConAmbasModalidades() {
        // Arrange
        especialidad.setCategoriaId(3);
        especialidad.setServicioProfesional("Asesoría técnica");
        especialidad.setCosto(120.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(true);
        especialidad.setTrabajoPresencial(true);

        // Assert
        assertTrue(especialidad.isValid(), "Debe ser válida con ambas modalidades");
        assertTrue(especialidad.getTrabajoRemoto());
        assertTrue(especialidad.getTrabajoPresencial());
    }

    @Test
    @DisplayName("✗ Especialidad sin ninguna modalidad de trabajo debe ser inválida")
    public void testEspecialidadSinModalidadesTrabajo() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(false);
        especialidad.setTrabajoPresencial(false);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida sin modalidades de trabajo");
    }

    @Test
    @DisplayName("✗ Especialidad con trabajoRemoto null y trabajoPresencial false debe ser inválida")
    public void testEspecialidadConTrabajoRemotoNull() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(null);
        especialidad.setTrabajoPresencial(false);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con trabajoRemoto null y trabajoPresencial false");
    }

    @Test
    @DisplayName("✗ Especialidad con trabajoRemoto false y trabajoPresencial null debe ser inválida")
    public void testEspecialidadConTrabajoPresencialNull() {
        // Arrange
        especialidad.setCategoriaId(1);
        especialidad.setServicioProfesional("Servicio de ejemplo");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(false);
        especialidad.setTrabajoPresencial(null);

        // Assert
        assertFalse(especialidad.isValid(), "Debe ser inválida con trabajoPresencial null y trabajoRemoto false");
    }

    @Test
    @DisplayName("⚠ Setters y Getters para campos de tipo de prestación")
    public void testSettersGettersTipoPrestacion() {
        // Arrange & Act
        especialidad.setTrabajoRemoto(true);
        especialidad.setTrabajoPresencial(false);

        // Assert
        assertTrue(especialidad.getTrabajoRemoto());
        assertFalse(especialidad.getTrabajoPresencial());

        // Cambiar valores
        especialidad.setTrabajoRemoto(false);
        especialidad.setTrabajoPresencial(true);

        // Assert después del cambio
        assertFalse(especialidad.getTrabajoRemoto());
        assertTrue(especialidad.getTrabajoPresencial());
    }

    @Test
    @DisplayName("⚠ Verificar toString incluye campos de tipo de prestación")
    public void testToStringConTipoPrestacion() {
        // Arrange
        especialidad.setId(1);
        especialidad.setProfesionalId(10);
        especialidad.setCategoriaId(5);
        especialidad.setServicioProfesional("Reparación");
        especialidad.setCosto(50.0);
        especialidad.setTipoCosto("hora");
        especialidad.setOrden(1);
        especialidad.setTrabajoRemoto(true);
        especialidad.setTrabajoPresencial(false);

        // Act
        String toString = especialidad.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("trabajoRemoto=true"));
        assertTrue(toString.contains("trabajoPresencial=false"));
    }
}
