package com.contactoprofesionales.model;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalTime;

/**
 * Pruebas unitarias para el modelo DisponibilidadHoraria.
 * Incluye casos positivos, negativos y límites.
 */
@DisplayName("Pruebas para DisponibilidadHoraria")
public class DisponibilidadHorariaTest {

    private DisponibilidadHoraria disponibilidad;

    @BeforeEach
    public void setUp() {
        disponibilidad = new DisponibilidadHoraria();
    }

    // ========================================================================
    // CASOS POSITIVOS
    // ========================================================================

    @Test
    @DisplayName("✓ Crear disponibilidad para todo el tiempo")
    public void testCrearDisponibilidadTodoTiempo() {
        // Arrange & Act
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(true);

        // Assert
        assertTrue(disponibilidad.isValid(), "Debe ser válida para todo el tiempo");
        assertTrue(disponibilidad.getTodoTiempo());
    }

    @Test
    @DisplayName("✓ Crear disponibilidad con horarios específicos")
    public void testCrearDisponibilidadConHorarios() {
        // Arrange
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(false);

        HorarioDia horario = new HorarioDia();
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(18, 0));

        // Act
        disponibilidad.addHorarioDia(horario);

        // Assert
        assertTrue(disponibilidad.isValid(), "Debe ser válida con horarios");
        assertFalse(disponibilidad.getTodoTiempo());
        assertEquals(1, disponibilidad.getHorariosDias().size());
    }

    @Test
    @DisplayName("✓ Agregar horarios para todos los días de la semana")
    public void testAgregarHorariosSemanaCompleta() {
        // Arrange
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(false);

        String[] dias = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};

        // Act
        for (String dia : dias) {
            HorarioDia horario = new HorarioDia();
            horario.setDiaSemana(dia);
            horario.setTipoJornada("8hrs");
            horario.setHoraInicio(LocalTime.of(8, 0));
            horario.setHoraFin(LocalTime.of(17, 0));
            disponibilidad.addHorarioDia(horario);
        }

        // Assert
        assertTrue(disponibilidad.isValid());
        assertEquals(7, disponibilidad.getHorariosDias().size());
    }

    @Test
    @DisplayName("✓ Crear disponibilidad con horario 24hrs")
    public void testCrearDisponibilidadCon24Hrs() {
        // Arrange
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(false);

        HorarioDia horario = new HorarioDia();
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("24hrs");

        // Act
        disponibilidad.addHorarioDia(horario);

        // Assert
        assertTrue(disponibilidad.isValid());
        assertEquals("24hrs", disponibilidad.getHorariosDias().get(0).getTipoJornada());
    }

    // ========================================================================
    // CASOS NEGATIVOS
    // ========================================================================

    @Test
    @DisplayName("✗ Disponibilidad sin horarios y sin todoTiempo debe ser inválida")
    public void testDisponibilidadSinHorariosYSinTodoTiempo() {
        // Arrange
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(false);
        // No agregar horarios

        // Assert
        assertFalse(disponibilidad.isValid(), "Debe ser inválida sin horarios");
    }

    @Test
    @DisplayName("✗ Disponibilidad con horarios null y todoTiempo false debe ser inválida")
    public void testDisponibilidadConHorariosNullYTodoTiempoFalse() {
        // Arrange
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(false);
        disponibilidad.setHorariosDias(null);

        // Assert
        assertFalse(disponibilidad.isValid(), "Debe ser inválida con horarios null");
    }

    // ========================================================================
    // CASOS LÍMITE
    // ========================================================================

    @Test
    @DisplayName("⚠ Agregar exactamente 7 horarios (máximo de días en la semana)")
    public void testAgregarSieteHorarios() {
        // Arrange
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(false);

        String[] dias = {"lunes", "martes", "miercoles", "jueves", "viernes", "sabado", "domingo"};

        // Act
        for (String dia : dias) {
            HorarioDia horario = new HorarioDia();
            horario.setDiaSemana(dia);
            horario.setTipoJornada("8hrs");
            horario.setHoraInicio(LocalTime.of(8, 0));
            horario.setHoraFin(LocalTime.of(17, 0));
            disponibilidad.addHorarioDia(horario);
        }

        // Assert
        assertTrue(disponibilidad.isValid());
        assertEquals(7, disponibilidad.getHorariosDias().size());
    }

    @Test
    @DisplayName("⚠ Intentar agregar más de 7 horarios debe ser inválido")
    public void testAgregarMasDeSieteHorarios() {
        // Arrange
        disponibilidad.setProfesionalId(1);
        disponibilidad.setTodoTiempo(false);

        // Act - Agregar 8 horarios (más de los días de la semana)
        for (int i = 1; i <= 8; i++) {
            HorarioDia horario = new HorarioDia();
            horario.setDiaSemana("lunes"); // Repetir día
            horario.setTipoJornada("8hrs");
            horario.setHoraInicio(LocalTime.of(8, 0));
            horario.setHoraFin(LocalTime.of(17, 0));
            disponibilidad.addHorarioDia(horario);
        }

        // Assert
        assertEquals(8, disponibilidad.getHorariosDias().size());
        assertFalse(disponibilidad.isValid(), "Debe ser inválida con más de 7 horarios");
    }

    @Test
    @DisplayName("⚠ Verificar toString")
    public void testToString() {
        // Arrange
        disponibilidad.setId(1);
        disponibilidad.setProfesionalId(10);
        disponibilidad.setTodoTiempo(true);

        // Act
        String toString = disponibilidad.toString();

        // Assert
        assertNotNull(toString);
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("profesionalId=10"));
        assertTrue(toString.contains("todoTiempo=true"));
    }

    @Test
    @DisplayName("⚠ Verificar valores por defecto")
    public void testValoresPorDefecto() {
        // Arrange & Act
        DisponibilidadHoraria disp = new DisponibilidadHoraria();

        // Assert
        assertNotNull(disp.getFechaCreacion());
        assertNotNull(disp.getFechaActualizacion());
        assertTrue(disp.getActivo());
        assertFalse(disp.getTodoTiempo());
        assertNotNull(disp.getHorariosDias());
        assertTrue(disp.getHorariosDias().isEmpty());
    }

    @Test
    @DisplayName("⚠ Constructor parametrizado")
    public void testConstructorParametrizado() {
        // Arrange & Act
        DisponibilidadHoraria disp = new DisponibilidadHoraria(5, true);

        // Assert
        assertEquals(5, disp.getProfesionalId());
        assertTrue(disp.getTodoTiempo());
        assertTrue(disp.isValid());
    }

    @Test
    @DisplayName("⚠ Agregar horario a disponibilidad con horariosDias null")
    public void testAgregarHorarioConListaNull() {
        // Arrange
        disponibilidad.setHorariosDias(null);
        HorarioDia horario = new HorarioDia();
        horario.setDiaSemana("lunes");
        horario.setTipoJornada("8hrs");
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(18, 0));

        // Act
        disponibilidad.addHorarioDia(horario);

        // Assert
        assertNotNull(disponibilidad.getHorariosDias(), "Debe crear la lista automáticamente");
        assertEquals(1, disponibilidad.getHorariosDias().size());
    }
}
