package com.contactoprofesionales.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

/**
 * Pruebas unitarias para el modelo HorarioDia.
 * Incluye casos positivos, negativos y límites.
 */
@DisplayName("Pruebas para HorarioDia")
public class HorarioDiaTest {

    private HorarioDia horario;

    @BeforeEach
    public void setUp() {
        horario = new HorarioDia();
    }

    // ========================================================================
    // CASOS POSITIVOS
    // ========================================================================

    @Test
    @DisplayName("✓ Crear horario de 8hrs válido")
    public void testCrearHorario8HrsValido() {
        // Arrange & Act
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(17, 0));

        // Assert
        assertTrue(horario.isValid(), "Debe ser válido");
        assertEquals("lunes", horario.getDiaSemana());
        assertEquals("8hrs", horario.getTipoJornada());
    }

    @Test
    @DisplayName("✓ Crear horario de 24hrs válido")
    public void testCrearHorario24HrsValido() {
        // Arrange & Act
        horario.setDiaSemana("martes");
        horario.setTipoJornada("24hrs");

        // Assert
        assertTrue(horario.isValid(), "Debe ser válido para 24hrs sin horas específicas");
    }

    @Test
    @DisplayName("✓ Constructor con parámetros para 8hrs")
    public void testConstructorParametrizado8Hrs() {
        // Arrange & Act
        HorarioDia h = new HorarioDia(1, "miercoles", "8hrs");

        // Assert
        assertEquals(1, h.getDisponibilidadId());
        assertEquals("miercoles", h.getDiaSemana());
        assertEquals("8hrs", h.getTipoJornada());
        assertNotNull(h.getHoraInicio(), "Debe tener hora de inicio por defecto");
        assertNotNull(h.getHoraFin(), "Debe tener hora de fin por defecto");
        assertEquals(LocalTime.of(8, 0), h.getHoraInicio());
        assertEquals(LocalTime.of(17, 0), h.getHoraFin());
    }

    @Test
    @DisplayName("✓ Constructor completo con horarios personalizados")
    public void testConstructorCompletoConHorariosPersonalizados() {
        // Arrange & Act
        HorarioDia h = new HorarioDia(
            1,
            "jueves",
            "8hrs",
            LocalTime.of(9, 30),
            LocalTime.of(18, 30)
        );

        // Assert
        assertEquals(LocalTime.of(9, 30), h.getHoraInicio());
        assertEquals(LocalTime.of(18, 30), h.getHoraFin());
        assertTrue(h.isValid());
    }

    @Test
    @DisplayName("✓ Verificar todos los días de la semana válidos")
    public void testTodosLosDiasDeLaSemanaValidos() {
        String[] dias = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};

        for (String dia : dias) {
            HorarioDia h = new HorarioDia();
            h.setDiaSemana(dia);
            h.setTipoJornada("24hrs");
            assertTrue(h.isValid(), "Día '" + dia + "' debe ser válido");
        }
    }

    @Test
    @DisplayName("✓ Verificar formato de horario para 24hrs")
    public void testGetHorarioFormateado24Hrs() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("24hrs");

        // Act
        String formateado = horario.getHorarioFormateado();

        // Assert
        assertEquals("24 horas", formateado);
    }

    @Test
    @DisplayName("✓ Verificar formato de horario para 8hrs")
    public void testGetHorarioFormateado8Hrs() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(18, 0));

        // Act
        String formateado = horario.getHorarioFormateado();

        // Assert
        assertTrue(formateado.contains("09:00"));
        assertTrue(formateado.contains("18:00"));
        assertTrue(formateado.contains("-"));
    }

    // ========================================================================
    // CASOS NEGATIVOS
    // ========================================================================

    @Test
    @DisplayName("✗ Horario sin día de la semana debe ser inválido")
    public void testHorarioSinDiaSemana() {
        // Arrange
        horario.setDiaSemana(null);
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(17, 0));

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido sin día de semana");
    }

    @Test
    @DisplayName("✗ Horario sin tipo de jornada debe ser inválido")
    public void testHorarioSinTipoJornada() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada(null);
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(17, 0));

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido sin tipo de jornada");
    }

    @Test
    @DisplayName("✗ Horario con día de semana inválido")
    public void testHorarioConDiaInvalido() {
        // Arrange
        horario.setDiaSemana("lunees"); // Mal escrito
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(17, 0));

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido con día de semana incorrecto");
    }

    @Test
    @DisplayName("✗ Horario con tipo de jornada inválido")
    public void testHorarioConTipoJornadaInvalido() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("12hrs"); // No permitido
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(17, 0));

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido con tipo de jornada no permitido");
    }

    @Test
    @DisplayName("✗ Horario de 8hrs sin hora de inicio")
    public void testHorario8HrsSinHoraInicio() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(null);
        horario.setHoraFin(LocalTime.of(17, 0));

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido sin hora de inicio para 8hrs");
    }

    @Test
    @DisplayName("✗ Horario de 8hrs sin hora de fin")
    public void testHorario8HrsSinHoraFin() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(null);

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido sin hora de fin para 8hrs");
    }

    @Test
    @DisplayName("✗ Horario de 8hrs con hora fin antes de hora inicio")
    public void testHorario8HrsConHoraFinAntesDeInicio() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(17, 0));
        horario.setHoraFin(LocalTime.of(8, 0)); // Fin antes de inicio

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido con hora fin antes de hora inicio");
    }

    @Test
    @DisplayName("✗ Horario de 8hrs con hora fin igual a hora inicio")
    public void testHorario8HrsConHoraFinIgualAInicio() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(8, 0)); // Igual

        // Assert
        assertFalse(horario.isValid(), "Debe ser inválido con hora fin igual a hora inicio");
    }

    // ========================================================================
    // CASOS LÍMITE
    // ========================================================================

    @Test
    @DisplayName("⚠ Horario con hora inicio a medianoche")
    public void testHorarioConHoraInicioMedianoche() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(0, 0));
        horario.setHoraFin(LocalTime.of(8, 0));

        // Assert
        assertTrue(horario.isValid(), "Debe ser válido con inicio a medianoche");
    }

    @Test
    @DisplayName("⚠ Horario con hora fin casi medianoche")
    public void testHorarioConHoraFinCasiMedianoche() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(16, 0));
        horario.setHoraFin(LocalTime.of(23, 59));

        // Assert
        assertTrue(horario.isValid(), "Debe ser válido con fin a 23:59");
    }

    @Test
    @DisplayName("⚠ Verificar toString")
    public void testToString() {
        // Arrange
        horario.setId(1);
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(18, 0));

        // Act
        String toString = horario.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("lunes"));
        assertTrue(toString.contains("8hrs"));
    }

    @Test
    @DisplayName("⚠ Verificar valores por defecto")
    public void testValoresPorDefecto() {
        // Arrange & Act
        HorarioDia h = new HorarioDia();

        // Assert
        assertTrue(h.getActivo(), "activo debe ser true por defecto");
    }

    @Test
    @DisplayName("⚠ Horario formateado sin horas específicas")
    public void testHorarioFormateadoSinHoras() {
        // Arrange
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        // No establecer horas

        // Act
        String formateado = horario.getHorarioFormateado();

        // Assert
        assertEquals("No especificado", formateado);
    }
}
