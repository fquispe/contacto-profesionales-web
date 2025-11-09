package com.contactoprofesionales.service.cliente;

import com.contactoprofesionales.dao.cliente.ClienteDAO;
import com.contactoprofesionales.dao.cliente.ClienteDAOImpl;
import com.contactoprofesionales.dao.cliente.DireccionClienteDAO;
import com.contactoprofesionales.dao.cliente.DireccionClienteDAOImpl;
import com.contactoprofesionales.dto.ClienteDTO;
import com.contactoprofesionales.dto.ClienteRegistroRequest;
import com.contactoprofesionales.dto.DireccionClienteDTO;
import com.contactoprofesionales.exception.ClienteException;
import com.contactoprofesionales.model.Cliente;
import com.contactoprofesionales.model.DireccionCliente;

import java.util.Arrays;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de Cliente con la lógica de negocio
 */
public class ClienteServiceImpl implements ClienteService {
    
    private final ClienteDAO clienteDAO;
    private final DireccionClienteDAO direccionDAO;
    private final Gson gson;
    
    // Patrones de validación
    private static final Pattern EMAIL_PATTERN = 
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO_PATTERN = 
        Pattern.compile("^[0-9]{9}$");
    
    public ClienteServiceImpl() {
        this.clienteDAO = new ClienteDAOImpl();
        this.direccionDAO = new DireccionClienteDAOImpl();
        this.gson = new Gson();
    }
    
    @Override
    public ClienteDTO registrarCliente(ClienteRegistroRequest request) throws ClienteException {
        // Validar datos del cliente
        validarDatosCliente(request);
        
        // Verificar que el email no esté registrado
        if (clienteDAO.existeEmail(request.getEmail())) {
            throw new ClienteException("EMAIL_DUPLICADO", "El email ya está registrado en el sistema");
        }
        
        // Verificar que el teléfono no esté registrado
        if (clienteDAO.existeTelefono(request.getTelefono())) {
            throw new ClienteException("TELEFONO_DUPLICADO", "El teléfono ya está registrado en el sistema");
        }
        
        // Convertir DTO a modelo
        Cliente cliente = convertirRequestAModelo(request);
        
        // Registrar cliente
        cliente = clienteDAO.registrar(cliente);
        
        // Registrar direcciones si existen
        if (request.getDirecciones() != null && !request.getDirecciones().isEmpty()) {
            for (DireccionClienteDTO direccionDTO : request.getDirecciones()) {
                DireccionCliente direccion = convertirDireccionDTOAModelo(direccionDTO);
                direccion.setClienteId(cliente.getId());
                direccionDAO.crear(direccion);
            }
        }
        
        // Retornar el cliente registrado con sus direcciones
        return obtenerPerfil(cliente.getId());
    }
    
    @Override
    public ClienteDTO actualizarPerfil(Long id, ClienteRegistroRequest request) throws ClienteException {
        // Validar datos del cliente
        validarDatosCliente(request);
        
        // Verificar que el cliente existe
        Optional<Cliente> clienteExistente = clienteDAO.buscarPorId(id);
        if (!clienteExistente.isPresent()) {
            throw new ClienteException("NO_ENCONTRADO", "Cliente no encontrado");
        }
        
        Cliente cliente = clienteExistente.get();
        
        // Verificar que el email no esté usado por otro cliente
        if (!cliente.getEmail().equals(request.getEmail()) && clienteDAO.existeEmail(request.getEmail())) {
            throw new ClienteException("EMAIL_DUPLICADO", "El email ya está registrado por otro usuario");
        }
        
        // Verificar que el teléfono no esté usado por otro cliente
        if (!cliente.getTelefono().equals(request.getTelefono()) && clienteDAO.existeTelefono(request.getTelefono())) {
            throw new ClienteException("TELEFONO_DUPLICADO", "El teléfono ya está registrado por otro usuario");
        }
        
        // Actualizar datos del cliente
        actualizarDatosCliente(cliente, request);
        cliente = clienteDAO.actualizar(cliente);
        
        // Actualizar direcciones
        if (request.getDirecciones() != null) {
            // Eliminar direcciones actuales
            direccionDAO.eliminarPorCliente(id);
            
            // Crear nuevas direcciones
            for (DireccionClienteDTO direccionDTO : request.getDirecciones()) {
                DireccionCliente direccion = convertirDireccionDTOAModelo(direccionDTO);
                direccion.setClienteId(id);
                direccionDAO.crear(direccion);
            }
        }
        
        return obtenerPerfil(id);
    }
    
    @Override
    public ClienteDTO obtenerPerfil(Long id) throws ClienteException {
        Optional<Cliente> clienteOpt = clienteDAO.buscarPorId(id);
        if (!clienteOpt.isPresent()) {
            throw new ClienteException("NO_ENCONTRADO", "Cliente no encontrado");
        }
        
        Cliente cliente = clienteOpt.get();
        
        // Cargar direcciones
        List<DireccionCliente> direcciones = direccionDAO.listarPorCliente(id);
        cliente.setDirecciones(direcciones);
        
        return convertirModeloADTO(cliente);
    }
    
    @Override
    public ClienteDTO buscarPorEmail(String email) throws ClienteException {
        Optional<Cliente> clienteOpt = clienteDAO.buscarPorEmail(email);
        if (!clienteOpt.isPresent()) {
            throw new ClienteException("NO_ENCONTRADO", "Cliente no encontrado con ese email");
        }
        
        Cliente cliente = clienteOpt.get();
        
        // Cargar direcciones
        List<DireccionCliente> direcciones = direccionDAO.listarPorCliente(cliente.getId());
        cliente.setDirecciones(direcciones);
        
        return convertirModeloADTO(cliente);
    }
    
    @Override
    public List<ClienteDTO> listarClientesActivos() throws ClienteException {
        List<Cliente> clientes = clienteDAO.listarActivos();
        List<ClienteDTO> clientesDTO = new ArrayList<>();
        
        for (Cliente cliente : clientes) {
            // Cargar direcciones para cada cliente
            List<DireccionCliente> direcciones = direccionDAO.listarPorCliente(cliente.getId());
            cliente.setDirecciones(direcciones);
            clientesDTO.add(convertirModeloADTO(cliente));
        }
        
        return clientesDTO;
    }
    
    @Override
    public boolean desactivarCliente(Long id) throws ClienteException {
        // Verificar que el cliente existe
        Optional<Cliente> clienteOpt = clienteDAO.buscarPorId(id);
        if (!clienteOpt.isPresent()) {
            throw new ClienteException("NO_ENCONTRADO", "Cliente no encontrado");
        }
        
        // Desactivar direcciones
        direccionDAO.eliminarPorCliente(id);
        
        // Desactivar cliente
        return clienteDAO.desactivar(id);
    }
    
    @Override
    public boolean activarCliente(Long id) throws ClienteException {
        return clienteDAO.activar(id);
    }
    
    @Override
    public void validarDatosCliente(ClienteRegistroRequest request) throws ClienteException {
        List<String> errores = new ArrayList<>();
        
        // Validar nombre completo
        if (request.getNombreCompleto() == null || request.getNombreCompleto().trim().isEmpty()) {
            errores.add("El nombre completo es obligatorio");
        } else if (request.getNombreCompleto().trim().length() < 3) {
            errores.add("El nombre completo debe tener al menos 3 caracteres");
        } else if (request.getNombreCompleto().trim().length() > 100) {
            errores.add("El nombre completo no puede exceder 100 caracteres");
        }
        
        // Validar email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            errores.add("El email es obligatorio");
        } else if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
            errores.add("El formato del email no es válido");
        }
        
        // Validar teléfono
        if (request.getTelefono() == null || request.getTelefono().trim().isEmpty()) {
            errores.add("El teléfono es obligatorio");
        } else if (!TELEFONO_PATTERN.matcher(request.getTelefono()).matches()) {
            errores.add("El teléfono debe tener exactamente 9 dígitos");
        }
        
        // Validar radio de búsqueda
        if (request.getRadioBusqueda() != null) {
            if (request.getRadioBusqueda() < 1 || request.getRadioBusqueda() > 50) {
                errores.add("El radio de búsqueda debe estar entre 1 y 50 km");
            }
        }
        
        // Validar presupuesto promedio
        if (request.getPresupuestoPromedio() != null) {
            if (request.getPresupuestoPromedio() < 0) {
                errores.add("El presupuesto promedio no puede ser negativo");
            }
        }
        
        // Validar direcciones
        if (request.getDirecciones() != null) {
            if (request.getDirecciones().size() > 3) {
                errores.add("Máximo 3 direcciones permitidas");
            }
            
            for (int i = 0; i < request.getDirecciones().size(); i++) {
                DireccionClienteDTO dir = request.getDirecciones().get(i);
                if (dir.getDireccionCompleta() == null || dir.getDireccionCompleta().trim().isEmpty()) {
                    errores.add("La dirección " + (i + 1) + " debe tener una dirección completa");
                }
                if (dir.getDistrito() == null || dir.getDistrito().trim().isEmpty()) {
                    errores.add("La dirección " + (i + 1) + " debe tener un distrito");
                }
            }
        }
        
        if (!errores.isEmpty()) {
            throw new ClienteException("VALIDACION_ERROR", String.join(". ", errores));
        }
    }
    
    /**
     * Convierte un ClienteRegistroRequest a Cliente (modelo)
     */
    private Cliente convertirRequestAModelo(ClienteRegistroRequest request) {
        Cliente cliente = new Cliente();
        
        cliente.setNombreCompleto(request.getNombreCompleto().trim());
        cliente.setEmail(request.getEmail().trim().toLowerCase());
        cliente.setTelefono(request.getTelefono().trim());
        
        // Convertir lista de categorías a JSON string
        if (request.getCategoriasFavoritas() != null && !request.getCategoriasFavoritas().isEmpty()) {
            cliente.setCategoriasFavoritas(gson.toJson(request.getCategoriasFavoritas()));
        }
        
        cliente.setRadioBusqueda(request.getRadioBusqueda());
        cliente.setPresupuestoPromedio(request.getPresupuestoPromedio());
        cliente.setNotificacionesEmail(request.getNotificacionesEmail() != null ? request.getNotificacionesEmail() : false);
        cliente.setNotificacionesPush(request.getNotificacionesPush() != null ? request.getNotificacionesPush() : false);
        cliente.setNotificacionesPromociones(request.getNotificacionesPromociones() != null ? request.getNotificacionesPromociones() : false);
        cliente.setNotificacionesResenas(request.getNotificacionesResenas() != null ? request.getNotificacionesResenas() : false);
        cliente.setPerfilVisible(request.getPerfilVisible() != null ? request.getPerfilVisible() : true);
        cliente.setCompartirUbicacion(request.getCompartirUbicacion() != null ? request.getCompartirUbicacion() : false);
        cliente.setHistorialPublico(request.getHistorialPublico() != null ? request.getHistorialPublico() : false);
        
        return cliente;
    }
    
    /**
     * Actualiza los datos de un cliente existente
     */
    private void actualizarDatosCliente(Cliente cliente, ClienteRegistroRequest request) {
        cliente.setNombreCompleto(request.getNombreCompleto().trim());
        cliente.setTelefono(request.getTelefono().trim());
        
        // Convertir lista de categorías a JSON string
        if (request.getCategoriasFavoritas() != null && !request.getCategoriasFavoritas().isEmpty()) {
            cliente.setCategoriasFavoritas(gson.toJson(request.getCategoriasFavoritas()));
        }
        
        cliente.setRadioBusqueda(request.getRadioBusqueda());
        cliente.setPresupuestoPromedio(request.getPresupuestoPromedio());
        cliente.setNotificacionesEmail(request.getNotificacionesEmail());
        cliente.setNotificacionesPush(request.getNotificacionesPush());
        cliente.setNotificacionesPromociones(request.getNotificacionesPromociones());
        cliente.setNotificacionesResenas(request.getNotificacionesResenas());
        cliente.setPerfilVisible(request.getPerfilVisible());
        cliente.setCompartirUbicacion(request.getCompartirUbicacion());
        cliente.setHistorialPublico(request.getHistorialPublico());
    }
    
    /**
     * Convierte un Cliente (modelo) a ClienteDTO
     */
    private ClienteDTO convertirModeloADTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        
        dto.setId(cliente.getId());
        dto.setNombreCompleto(cliente.getNombreCompleto());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setFotoPerfilUrl(cliente.getFotoPerfilUrl());
        
        // Convertir JSON string a lista de categorías
        if (cliente.getCategoriasFavoritas() != null && !cliente.getCategoriasFavoritas().isEmpty()) {
            String[] categorias = cliente.getCategoriasFavoritas().split(",");
            List<String> categoriasList = Arrays.stream(categorias)
                .map(String::trim)
                .collect(Collectors.toList());
            dto.setCategoriasFavoritas(categoriasList);
        }
        
        dto.setRadioBusqueda(cliente.getRadioBusqueda());
        dto.setPresupuestoPromedio(cliente.getPresupuestoPromedio());
        dto.setNotificacionesEmail(cliente.getNotificacionesEmail());
        dto.setNotificacionesPush(cliente.getNotificacionesPush());
        dto.setNotificacionesPromociones(cliente.getNotificacionesPromociones());
        dto.setNotificacionesResenas(cliente.getNotificacionesResenas());
        dto.setPerfilVisible(cliente.getPerfilVisible());
        dto.setCompartirUbicacion(cliente.getCompartirUbicacion());
        dto.setHistorialPublico(cliente.getHistorialPublico());
        dto.setFechaRegistro(cliente.getFechaRegistro());
        dto.setFechaActualizacion(cliente.getFechaActualizacion());
        dto.setActivo(cliente.getActivo());
        
        // Convertir direcciones
        if (cliente.getDirecciones() != null) {
            dto.setDirecciones(
                cliente.getDirecciones().stream()
                    .map(this::convertirDireccionModeloADTO)
                    .collect(Collectors.toList())
            );
        }
        
        return dto;
    }
    
    /**
     * Convierte un DireccionClienteDTO a DireccionCliente (modelo)
     */
    private DireccionCliente convertirDireccionDTOAModelo(DireccionClienteDTO dto) {
        DireccionCliente direccion = new DireccionCliente();
        
        direccion.setId(dto.getId());
        direccion.setTipo(dto.getTipo() != null ? dto.getTipo() : "OTRO");
        direccion.setDireccionCompleta(dto.getDireccionCompleta().trim());
        direccion.setDistrito(dto.getDistrito().trim());
        direccion.setReferencias(dto.getReferencias() != null ? dto.getReferencias().trim() : null);
        direccion.setEsPrincipal(dto.getEsPrincipal() != null ? dto.getEsPrincipal() : false);
        
        return direccion;
    }
    
    /**
     * Convierte un DireccionCliente (modelo) a DireccionClienteDTO
     */
    private DireccionClienteDTO convertirDireccionModeloADTO(DireccionCliente direccion) {
        DireccionClienteDTO dto = new DireccionClienteDTO();
        
        dto.setId(direccion.getId());
        dto.setTipo(direccion.getTipo());
        dto.setDireccionCompleta(direccion.getDireccionCompleta());
        dto.setDistrito(direccion.getDistrito());
        dto.setReferencias(direccion.getReferencias());
        dto.setEsPrincipal(direccion.getEsPrincipal());
        
        return dto;
    }
}
