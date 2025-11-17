package com.contactoprofesionales.dao;

import com.contactoprofesionales.dto.ServiciosProfesionalCompleto;
import com.contactoprofesionales.model.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Pruebas unitarias para ServiciosProfesionalDAOImpl.
 * Incluye casos positivos, negativos y límites.
 *
 * NOTA: Estas pruebas requieren una base de datos PostgreSQL configurada
 * y un profesional existente con ID válido para las pruebas de integración.
 */
@DisplayName("Pruebas para ServiciosProfesionalDAO")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ServiciosProfesionalDAOTest {

    private ServiciosProfesionalDAO serviciosDAO;
    private static final Integer PROFESIONAL_ID_TEST = 1; // ID de prueba - ajustar según BD
    private static final Integer PROFESIONAL_ID_INEXISTENTE = 99999;

    @BeforeEach
    public void setUp() {
        serviciosDAO = new ServiciosProfesionalDAOImpl();
    }

    // ========================================================================
    // CASOS POSITIVOS - Operaciones exitosas
    // ========================================================================

    @Test
    @Order(1)
    @DisplayName("✓ Guardar servicios profesionales completos")
    public void testGuardarServiciosProfesionalCompletos() {
        try {
            // Arrange
            List<EspecialidadProfesional> especialidades = crearEspecialidadesValidas();
            AreaServicio areaServicio = crearAreaServicioValida();
            DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

            // Primero eliminar cualquier servicio existente
            serviciosDAO.eliminarServiciosProfesional(PROFESIONAL_ID_TEST);

            // Act
            boolean resultado = serviciosDAO.guardarServiciosProfesional(
                PROFESIONAL_ID_TEST,
                especialidades,
                areaServicio,
                disponibilidad
            );

            // Assert
            assertTrue(resultado, "Debe guardar los servicios exitosamente");

            // Verificar que se guardaron
            boolean tieneServicios = serviciosDAO.tieneServiciosConfigurados(PROFESIONAL_ID_TEST);
            assertTrue(tieneServicios, "El profesional debe tener servicios configurados");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("✓ Obtener servicios profesionales existentes")
    public void testObtenerServiciosProfesional() {
        try {
            // Arrange - Asegurar que existan servicios
            List<EspecialidadProfesional> especialidades = crearEspecialidadesValidas();
            AreaServicio areaServicio = crearAreaServicioValida();
            DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

            serviciosDAO.eliminarServiciosProfesional(PROFESIONAL_ID_TEST);
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidades, areaServicio, disponibilidad);

            // Act
            ServiciosProfesionalCompleto servicios = serviciosDAO.obtenerServiciosProfesional(PROFESIONAL_ID_TEST);

            // Assert
            assertNotNull(servicios, "Debe retornar los servicios");
            assertNotNull(servicios.getEspecialidades(), "Debe tener especialidades");
            assertFalse(servicios.getEspecialidades().isEmpty(), "Debe tener al menos una especialidad");
            assertNotNull(servicios.getAreaServicio(), "Debe tener área de servicio");
            assertNotNull(servicios.getDisponibilidad(), "Debe tener disponibilidad");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    @Order(3)
    @DisplayName("✓ Actualizar servicios profesionales existentes")
    public void testActualizarServiciosProfesional() {
        try {
            // Arrange
            List<EspecialidadProfesional> especialidadesOriginales = crearEspecialidadesValidas();
            AreaServicio areaServicioOriginal = crearAreaServicioValida();
            DisponibilidadHoraria disponibilidadOriginal = crearDisponibilidadValida();

            serviciosDAO.eliminarServiciosProfesional(PROFESIONAL_ID_TEST);
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidadesOriginales, areaServicioOriginal, disponibilidadOriginal);

            // Crear nuevas especialidades actualizadas
            List<EspecialidadProfesional> especialidadesActualizadas = new ArrayList<>();
            EspecialidadProfesional espActualizada = new EspecialidadProfesional();
            espActualizada.setCategoriaId(1);
            espActualizada.setServicioProfesional("Servicio actualizado");
            espActualizada.setDescripcion("Descripción actualizada");
            espActualizada.setCosto(150.0);
            espActualizada.setTipoCosto("dia");
            espActualizada.setOrden(1);
            espActualizada.setEsPrincipal(true);
            espActualizada.setTrabajoPresencial(true);
            espActualizada.setTrabajoRemoto(false);
            especialidadesActualizadas.add(espActualizada);

            // Act
            boolean resultado = serviciosDAO.actualizarServiciosProfesional(
                PROFESIONAL_ID_TEST,
                especialidadesActualizadas,
                areaServicioOriginal,
                disponibilidadOriginal
            );

            // Assert
            assertTrue(resultado, "Debe actualizar los servicios exitosamente");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    @Order(4)
    @DisplayName("✓ Verificar que profesional tiene servicios configurados")
    public void testTieneServiciosConfigurados() {
        try {
            // Arrange
            List<EspecialidadProfesional> especialidades = crearEspecialidadesValidas();
            AreaServicio areaServicio = crearAreaServicioValida();
            DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

            serviciosDAO.eliminarServiciosProfesional(PROFESIONAL_ID_TEST);
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidades, areaServicio, disponibilidad);

            // Act
            boolean tieneServicios = serviciosDAO.tieneServiciosConfigurados(PROFESIONAL_ID_TEST);

            // Assert
            assertTrue(tieneServicios, "El profesional debe tener servicios configurados");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    @Order(5)
    @DisplayName("✓ Guardar especialidades individualmente")
    public void testGuardarEspecialidades() {
        try {
            // Arrange
            serviciosDAO.eliminarEspecialidadesPorProfesional(PROFESIONAL_ID_TEST);
            List<EspecialidadProfesional> especialidades = crearEspecialidadesValidas();

            // Act
            boolean resultado = serviciosDAO.guardarEspecialidades(PROFESIONAL_ID_TEST, especialidades);

            // Assert
            assertTrue(resultado, "Debe guardar las especialidades");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    @Order(6)
    @DisplayName("✓ Obtener especialidades por profesional")
    public void testObtenerEspecialidadesPorProfesional() {
        try {
            // Arrange
            serviciosDAO.eliminarEspecialidadesPorProfesional(PROFESIONAL_ID_TEST);
            List<EspecialidadProfesional> especialidadesGuardadas = crearEspecialidadesValidas();
            serviciosDAO.guardarEspecialidades(PROFESIONAL_ID_TEST, especialidadesGuardadas);

            // Act
            List<EspecialidadProfesional> especialidades = serviciosDAO.obtenerEspecialidadesPorProfesional(PROFESIONAL_ID_TEST);

            // Assert
            assertNotNull(especialidades, "Debe retornar lista de especialidades");
            assertFalse(especialidades.isEmpty(), "Debe tener al menos una especialidad");
            assertEquals(especialidadesGuardadas.size(), especialidades.size(), "Debe tener el mismo número de especialidades");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    // ========================================================================
    // CASOS NEGATIVOS - Validaciones con errores controlados
    // ========================================================================

    @Test
    @DisplayName("✗ Guardar servicios con especialidades null debe fallar")
    public void testGuardarConEspecialidadesNull() {
        // Arrange
        AreaServicio areaServicio = crearAreaServicioValida();
        DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, null, areaServicio, disponibilidad);
        });

        assertTrue(exception.getMessage().contains("especialidad") ||
                   exception instanceof IllegalArgumentException,
                   "Debe lanzar excepción por especialidades null");
    }

    @Test
    @DisplayName("✗ Guardar servicios con especialidades vacías debe fallar")
    public void testGuardarConEspecialidadesVacias() {
        // Arrange
        List<EspecialidadProfesional> especialidadesVacias = new ArrayList<>();
        AreaServicio areaServicio = crearAreaServicioValida();
        DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidadesVacias, areaServicio, disponibilidad);
        });

        assertTrue(exception.getMessage().contains("especialidad") ||
                   exception instanceof IllegalArgumentException,
                   "Debe lanzar excepción por especialidades vacías");
    }

    @Test
    @DisplayName("✗ Guardar servicios con más de 3 especialidades debe fallar")
    public void testGuardarConMasDeTresEspecialidades() {
        // Arrange
        List<EspecialidadProfesional> especialidades = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            EspecialidadProfesional esp = new EspecialidadProfesional();
            esp.setCategoriaId(i);
            esp.setServicioProfesional("Servicio " + i);
            esp.setCosto(50.0);
            esp.setTipoCosto("hora");
            esp.setOrden(i);
            esp.setTrabajoPresencial(true);
            esp.setTrabajoRemoto(false);
            especialidades.add(esp);
        }
        AreaServicio areaServicio = crearAreaServicioValida();
        DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidades, areaServicio, disponibilidad);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("3") ||
                   exception instanceof IllegalArgumentException,
                   "Debe lanzar excepción por más de 3 especialidades");
    }

    @Test
    @DisplayName("✗ Guardar servicios con área de servicio null debe fallar")
    public void testGuardarConAreaServicioNull() {
        // Arrange
        List<EspecialidadProfesional> especialidades = crearEspecialidadesValidas();
        DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidades, null, disponibilidad);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("área") ||
                   exception instanceof IllegalArgumentException,
                   "Debe lanzar excepción por área de servicio null");
    }

    @Test
    @DisplayName("✗ Guardar servicios con disponibilidad null debe fallar")
    public void testGuardarConDisponibilidadNull() {
        // Arrange
        List<EspecialidadProfesional> especialidades = crearEspecialidadesValidas();
        AreaServicio areaServicio = crearAreaServicioValida();

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidades, areaServicio, null);
        });

        assertTrue(exception.getMessage().toLowerCase().contains("disponibilidad") ||
                   exception instanceof IllegalArgumentException,
                   "Debe lanzar excepción por disponibilidad null");
    }

    @Test
    @DisplayName("✗ Obtener servicios de profesional inexistente retorna objeto vacío")
    public void testObtenerServiciosDeProfesionalInexistente() {
        try {
            // Act
            ServiciosProfesionalCompleto servicios = serviciosDAO.obtenerServiciosProfesional(PROFESIONAL_ID_INEXISTENTE);

            // Assert
            assertNotNull(servicios, "Debe retornar un objeto (no null)");
            assertTrue(servicios.getEspecialidades() == null || servicios.getEspecialidades().isEmpty(),
                      "No debe tener especialidades");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    // ========================================================================
    // CASOS LÍMITE - Datos en los límites permitidos
    // ========================================================================

    @Test
    @DisplayName("⚠ Guardar exactamente 3 especialidades (límite máximo)")
    public void testGuardarTresEspecialidades() {
        try {
            // Arrange
            serviciosDAO.eliminarEspecialidadesPorProfesional(PROFESIONAL_ID_TEST);
            List<EspecialidadProfesional> especialidades = new ArrayList<>();

            for (int i = 1; i <= 3; i++) {
                EspecialidadProfesional esp = new EspecialidadProfesional();
                esp.setCategoriaId(i);
                esp.setServicioProfesional("Servicio " + i);
                esp.setCosto(50.0 * i);
                esp.setTipoCosto("hora");
                esp.setOrden(i);
                esp.setEsPrincipal(i == 1);
                esp.setTrabajoPresencial(true);
                esp.setTrabajoRemoto(false);
                especialidades.add(esp);
            }

            // Act
            boolean resultado = serviciosDAO.guardarEspecialidades(PROFESIONAL_ID_TEST, especialidades);

            // Assert
            assertTrue(resultado, "Debe guardar exactamente 3 especialidades");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("⚠ Guardar solo 1 especialidad (límite mínimo)")
    public void testGuardarUnaEspecialidad() {
        try {
            // Arrange
            serviciosDAO.eliminarEspecialidadesPorProfesional(PROFESIONAL_ID_TEST);
            List<EspecialidadProfesional> especialidades = new ArrayList<>();

            EspecialidadProfesional esp = new EspecialidadProfesional();
            esp.setCategoriaId(1);
            esp.setServicioProfesional("Servicio único");
            esp.setCosto(50.0);
            esp.setTipoCosto("hora");
            esp.setOrden(1);
            esp.setEsPrincipal(true);
            esp.setTrabajoPresencial(true);
            esp.setTrabajoRemoto(false);
            especialidades.add(esp);

            // Act
            boolean resultado = serviciosDAO.guardarEspecialidades(PROFESIONAL_ID_TEST, especialidades);

            // Assert
            assertTrue(resultado, "Debe guardar 1 especialidad");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    @Test
    @Order(10)
    @DisplayName("⚠ Eliminar servicios profesionales")
    public void testEliminarServiciosProfesional() {
        try {
            // Arrange - Primero crear servicios
            List<EspecialidadProfesional> especialidades = crearEspecialidadesValidas();
            AreaServicio areaServicio = crearAreaServicioValida();
            DisponibilidadHoraria disponibilidad = crearDisponibilidadValida();

            serviciosDAO.guardarServiciosProfesional(PROFESIONAL_ID_TEST, especialidades, areaServicio, disponibilidad);

            // Act
            boolean resultado = serviciosDAO.eliminarServiciosProfesional(PROFESIONAL_ID_TEST);

            // Assert
            assertTrue(resultado, "Debe eliminar los servicios exitosamente");

            // Verificar que se eliminaron
            boolean tieneServicios = serviciosDAO.tieneServiciosConfigurados(PROFESIONAL_ID_TEST);
            assertFalse(tieneServicios, "El profesional no debe tener servicios después de eliminar");

        } catch (Exception e) {
            fail("No debería lanzar excepción: " + e.getMessage());
        }
    }

    // ========================================================================
    // MÉTODOS AUXILIARES PARA CREAR DATOS DE PRUEBA
    // ========================================================================

    private List<EspecialidadProfesional> crearEspecialidadesValidas() {
        List<EspecialidadProfesional> especialidades = new ArrayList<>();

        EspecialidadProfesional esp1 = new EspecialidadProfesional();
        esp1.setCategoriaId(1);
        esp1.setServicioProfesional("Reparación de tuberías");
        esp1.setDescripcion("Reparación profesional de todo tipo de tuberías");
        esp1.setIncluyeMateriales(true);
        esp1.setCosto(80.0);
        esp1.setTipoCosto("hora");
        esp1.setEsPrincipal(true);
        esp1.setOrden(1);
        // ✅ CORREGIDO: Establecer modalidad de trabajo (requerido por check_modalidad_trabajo)
        esp1.setTrabajoPresencial(true);
        esp1.setTrabajoRemoto(false);

        EspecialidadProfesional esp2 = new EspecialidadProfesional();
        esp2.setCategoriaId(2);
        esp2.setServicioProfesional("Instalación de grifería");
        esp2.setDescripcion("Instalación de grifos y accesorios");
        esp2.setIncluyeMateriales(false);
        esp2.setCosto(60.0);
        esp2.setTipoCosto("hora");
        esp2.setEsPrincipal(false);
        esp2.setOrden(2);
        // ✅ CORREGIDO: Establecer modalidad de trabajo (requerido por check_modalidad_trabajo)
        esp2.setTrabajoPresencial(true);
        esp2.setTrabajoRemoto(false);

        especialidades.add(esp1);
        especialidades.add(esp2);

        return especialidades;
    }

    private AreaServicio crearAreaServicioValida() {
        AreaServicio areaServicio = new AreaServicio();
        areaServicio.setProfesionalId(PROFESIONAL_ID_TEST);
        areaServicio.setTodoPais(true);
        return areaServicio;
    }

    private DisponibilidadHoraria crearDisponibilidadValida() {
        DisponibilidadHoraria disponibilidad = new DisponibilidadHoraria();
        disponibilidad.setProfesionalId(PROFESIONAL_ID_TEST);
        disponibilidad.setTodoTiempo(false);

        // Agregar horario de lunes a viernes
        HorarioDia horarioLunes = new HorarioDia();
        horarioLunes.setDiaSemana("lunes"); // ✅ CORREGIDO: usar minúsculas (requisito de BD)
        horarioLunes.setTipoJornada("8hrs");
        horarioLunes.setHoraInicio(LocalTime.of(8, 0));
        horarioLunes.setHoraFin(LocalTime.of(17, 0));

        disponibilidad.addHorarioDia(horarioLunes);

        return disponibilidad;
    }
}
