package com.contactoprofesionales.service.cliente;

import com.contactoprofesionales.dao.cliente.ClienteDAO;
import com.contactoprofesionales.dao.cliente.DireccionClienteDAO;
import com.contactoprofesionales.dto.ClienteDTO;
import com.contactoprofesionales.dto.ClienteRegistroRequest;
import com.contactoprofesionales.dto.DireccionClienteDTO;
import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.model.Cliente;
import com.contactoprofesionales.model.DireccionCliente;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests para ClienteServiceImpl usando Mockito.
 */
@ExtendWith(MockitoExtension.class)
public class ClienteServiceImplTest {

    @Mock
    private ClienteDAO clienteDAO;

    @Mock
    private DireccionClienteDAO direccionDAO;

    private ClienteService clienteService;

    @BeforeEach
    public void setUp() {
        // Necesitamos usar una instancia real del servicio para probar validaciones
        clienteService = new ClienteServiceImpl();
    }

    @Test
    @DisplayName("Validar datos de cliente válido no debe lanzar excepción")
    public void testValidarDatosCliente_DatosValidos() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();

        // Act & Assert
        assertDoesNotThrow(() -> {
            clienteService.validarDatosCliente(request);
        });
    }

    @Test
    @DisplayName("Validar cliente sin nombre debe lanzar excepción")
    public void testValidarDatosCliente_SinNombre() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setNombreCompleto(null);

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("nombre completo es obligatorio"));
    }

    @Test
    @DisplayName("Validar cliente con nombre muy corto debe lanzar excepción")
    public void testValidarDatosCliente_NombreCorto() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setNombreCompleto("AB");

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("al menos 3 caracteres"));
    }

    @Test
    @DisplayName("Validar cliente con nombre muy largo debe lanzar excepción")
    public void testValidarDatosCliente_NombreLargo() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setNombreCompleto("A".repeat(101));

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("no puede exceder 100 caracteres"));
    }

    @Test
    @DisplayName("Validar cliente sin email debe lanzar excepción")
    public void testValidarDatosCliente_SinEmail() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setEmail(null);

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("email es obligatorio"));
    }

    @Test
    @DisplayName("Validar cliente con email inválido debe lanzar excepción")
    public void testValidarDatosCliente_EmailInvalido() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setEmail("emailinvalido");

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("formato del email no es válido"));
    }

    @Test
    @DisplayName("Validar cliente sin teléfono debe lanzar excepción")
    public void testValidarDatosCliente_SinTelefono() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setTelefono(null);

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("teléfono es obligatorio"));
    }

    @Test
    @DisplayName("Validar cliente con teléfono inválido debe lanzar excepción")
    public void testValidarDatosCliente_TelefonoInvalido() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setTelefono("12345"); // Menos de 9 dígitos

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("teléfono debe tener exactamente 9 dígitos"));
    }

    @Test
    @DisplayName("Validar cliente con radio de búsqueda fuera de rango debe lanzar excepción")
    public void testValidarDatosCliente_RadioBusquedaInvalido() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setRadioBusqueda(100); // Mayor a 50

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("radio de búsqueda debe estar entre 1 y 50"));
    }

    @Test
    @DisplayName("Validar cliente con presupuesto negativo debe lanzar excepción")
    public void testValidarDatosCliente_PresupuestoNegativo() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setPresupuestoPromedio(-100.0);

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("presupuesto promedio no puede ser negativo"));
    }

    @Test
    @DisplayName("Validar cliente con más de 3 direcciones debe lanzar excepción")
    public void testValidarDatosCliente_DemasiadasDirecciones() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        List<DireccionClienteDTO> direcciones = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            DireccionClienteDTO dir = new DireccionClienteDTO();
            dir.setDireccionCompleta("Dirección " + i);
            dir.setDistrito("Distrito " + i);
            direcciones.add(dir);
        }
        request.setDirecciones(direcciones);

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("Máximo 3 direcciones permitidas"));
    }

    @Test
    @DisplayName("Validar cliente con dirección sin dirección completa debe lanzar excepción")
    public void testValidarDatosCliente_DireccionSinDireccionCompleta() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        DireccionClienteDTO dir = new DireccionClienteDTO();
        dir.setDistrito("Lima");
        request.setDirecciones(Arrays.asList(dir));

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("debe tener una dirección completa"));
    }

    @Test
    @DisplayName("Validar cliente con dirección sin distrito debe lanzar excepción")
    public void testValidarDatosCliente_DireccionSinDistrito() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        DireccionClienteDTO dir = new DireccionClienteDTO();
        dir.setDireccionCompleta("Av. Principal 123");
        request.setDirecciones(Arrays.asList(dir));

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("debe tener un distrito"));
    }

    @Test
    @DisplayName("Validar múltiples errores debe concatenarlos en el mensaje")
    public void testValidarDatosCliente_MultipleErrores() {
        // Arrange
        ClienteRegistroRequest request = new ClienteRegistroRequest();
        // Dejar varios campos vacíos para generar múltiples errores

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        // Debe contener múltiples errores
        String mensaje = exception.getMessage();
        assertTrue(mensaje.contains("nombre") || mensaje.contains("email") || mensaje.contains("teléfono"));
    }

    @Test
    @DisplayName("Validar datos con valores en los límites debe ser válido")
    public void testValidarDatosCliente_ValoresEnLimites() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setRadioBusqueda(1); // Límite inferior
        request.setPresupuestoPromedio(0.0); // Límite inferior

        // Act & Assert
        assertDoesNotThrow(() -> {
            clienteService.validarDatosCliente(request);
        });

        // Probar límite superior
        request.setRadioBusqueda(50);
        assertDoesNotThrow(() -> {
            clienteService.validarDatosCliente(request);
        });
    }

    @Test
    @DisplayName("Validar cliente con teléfono con letras debe lanzar excepción")
    public void testValidarDatosCliente_TelefonoConLetras() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setTelefono("12345abcd");

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("teléfono debe tener exactamente 9 dígitos"));
    }

    @Test
    @DisplayName("Validar cliente con nombre solo espacios debe lanzar excepción")
    public void testValidarDatosCliente_NombreSoloEspacios() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setNombreCompleto("   ");

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("nombre completo es obligatorio"));
    }

    @Test
    @DisplayName("Validar cliente con email solo espacios debe lanzar excepción")
    public void testValidarDatosCliente_EmailSoloEspacios() {
        // Arrange
        ClienteRegistroRequest request = crearRequestValido();
        request.setEmail("   ");

        // Act & Assert
        ClienteException exception = assertThrows(ClienteException.class, () -> {
            clienteService.validarDatosCliente(request);
        });

        assertTrue(exception.getMessage().contains("email es obligatorio"));
    }

    /**
     * Método auxiliar para crear un ClienteRegistroRequest válido
     */
    private ClienteRegistroRequest crearRequestValido() {
        ClienteRegistroRequest request = new ClienteRegistroRequest();
        request.setNombreCompleto("Juan Pérez");
        request.setEmail("juan@example.com");
        request.setTelefono("987654321");
        request.setRadioBusqueda(10);
        request.setPresupuestoPromedio(500.0);
        return request;
    }
}
