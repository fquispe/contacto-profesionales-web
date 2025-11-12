package com.contactoprofesionales.service.usuariopersona;

import com.contactoprofesionales.dao.usuariopersona.UsuarioPersonaDAO;
import com.contactoprofesionales.dao.usuariopersona.UsuarioPersonaDAOImpl;
import com.contactoprofesionales.dto.UsuarioPersonaDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.model.UsuarioPersona;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementación del servicio de UsuarioPersona.
 * Gestiona la lógica de negocio para datos personales de usuarios.
 */
public class UsuarioPersonaServiceImpl implements UsuarioPersonaService {

    private static final Logger logger = LoggerFactory.getLogger(UsuarioPersonaServiceImpl.class);
    private final UsuarioPersonaDAO usuarioPersonaDAO;

    // Patrones de validación
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^[0-9]{9}$");
    private static final Pattern DNI_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern RUC_PATTERN = Pattern.compile("^[0-9]{11}$");
    private static final Pattern CE_PATTERN = Pattern.compile("^[0-9]{9,12}$");

    // Tipos de documento válidos
    private static final List<String> TIPOS_DOCUMENTO_VALIDOS = Arrays.asList("DNI", "CE", "RUC", "PASAPORTE");

    // Tipos de rol válidos
    private static final List<String> TIPOS_ROL_VALIDOS = Arrays.asList("CLIENTE", "PROFESIONAL", "AMBOS");

    // Géneros válidos
    private static final List<String> GENEROS_VALIDOS = Arrays.asList("MASCULINO", "FEMENINO", "OTRO", "PREFIERO_NO_DECIR");

    public UsuarioPersonaServiceImpl() {
        this.usuarioPersonaDAO = new UsuarioPersonaDAOImpl();
    }

    @Override
    public UsuarioPersonaDTO registrar(UsuarioPersonaDTO dto) throws ValidationException, DatabaseException {
        logger.debug("Registrando nuevo usuario persona: {}", dto.getNombreCompleto());

        // Validar datos personales
        validarDatosPersonales(dto);

        try {
            // Verificar que el número de documento no esté registrado
            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
                if (usuarioPersonaDAO.existeNumeroDocumento(dto.getNumeroDocumento())) {
                    throw new ValidationException("El número de documento ya está registrado en el sistema");
                }
            }

            // Verificar que el teléfono no esté registrado
            if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
                if (usuarioPersonaDAO.existeTelefono(dto.getTelefono())) {
                    throw new ValidationException("El teléfono ya está registrado en el sistema");
                }
            }

            // Convertir DTO a modelo
            UsuarioPersona usuarioPersona = convertirDTOAModelo(dto);

            // Registrar usuario
            usuarioPersona = usuarioPersonaDAO.registrar(usuarioPersona);

            logger.info("Usuario persona registrado exitosamente con ID: {}", usuarioPersona.getId());
            return convertirModeloADTO(usuarioPersona);

        } catch (DatabaseException e) {
            logger.error("Error al registrar usuario persona: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public UsuarioPersonaDTO actualizar(Long id, UsuarioPersonaDTO dto)
            throws ValidationException, DatabaseException {
        logger.debug("Actualizando usuario persona con ID: {}", id);

        // Validar ID
        if (id == null || id <= 0) {
            throw new ValidationException("El ID del usuario es obligatorio y debe ser positivo");
        }

        // Validar datos personales
        validarDatosPersonales(dto);

        try {
            // Verificar que el usuario existe
            Optional<UsuarioPersona> usuarioExistenteOpt = usuarioPersonaDAO.buscarPorId(id);
            if (!usuarioExistenteOpt.isPresent()) {
                throw new ValidationException("Usuario con ID " + id + " no encontrado");
            }

            UsuarioPersona usuarioExistente = usuarioExistenteOpt.get();

            // Verificar que el número de documento no esté usado por otro usuario
            if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
                if (!usuarioExistente.getNumeroDocumento().equals(dto.getNumeroDocumento())) {
                    if (usuarioPersonaDAO.existeNumeroDocumento(dto.getNumeroDocumento())) {
                        throw new ValidationException("El número de documento ya está registrado por otro usuario");
                    }
                }
            }

            // Verificar que el teléfono no esté usado por otro usuario
            if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
                if (!usuarioExistente.getTelefono().equals(dto.getTelefono())) {
                    if (usuarioPersonaDAO.existeTelefono(dto.getTelefono())) {
                        throw new ValidationException("El teléfono ya está registrado por otro usuario");
                    }
                }
            }

            // Actualizar datos del usuario
            actualizarDatosUsuario(usuarioExistente, dto);
            usuarioExistente = usuarioPersonaDAO.actualizar(usuarioExistente);

            logger.info("Usuario persona actualizado exitosamente con ID: {}", id);
            return convertirModeloADTO(usuarioExistente);

        } catch (DatabaseException e) {
            logger.error("Error al actualizar usuario persona con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public UsuarioPersonaDTO obtenerPorId(Long id) throws ValidationException, DatabaseException {
        logger.debug("Obteniendo usuario persona por ID: {}", id);

        if (id == null || id <= 0) {
            throw new ValidationException("El ID del usuario es obligatorio y debe ser positivo");
        }

        try {
            Optional<UsuarioPersona> usuarioOpt = usuarioPersonaDAO.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                throw new ValidationException("Usuario con ID " + id + " no encontrado");
            }

            logger.info("Usuario persona encontrado con ID: {}", id);
            return convertirModeloADTO(usuarioOpt.get());

        } catch (DatabaseException e) {
            logger.error("Error al obtener usuario persona por ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public UsuarioPersonaDTO obtenerPorNumeroDocumento(String numeroDocumento)
            throws ValidationException, DatabaseException {
        logger.debug("Obteniendo usuario persona por número de documento: {}", numeroDocumento);

        if (numeroDocumento == null || numeroDocumento.trim().isEmpty()) {
            throw new ValidationException("El número de documento es obligatorio");
        }

        try {
            Optional<UsuarioPersona> usuarioOpt = usuarioPersonaDAO.buscarPorNumeroDocumento(numeroDocumento);
            if (!usuarioOpt.isPresent()) {
                throw new ValidationException("Usuario con número de documento " + numeroDocumento + " no encontrado");
            }

            logger.info("Usuario persona encontrado con número de documento: {}", numeroDocumento);
            return convertirModeloADTO(usuarioOpt.get());

        } catch (DatabaseException e) {
            logger.error("Error al obtener usuario persona por número de documento {}: {}",
                        numeroDocumento, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean actualizarTipoRol(Long id, String tipoRol) throws ValidationException, DatabaseException {
        logger.debug("Actualizando tipo de rol para usuario ID: {} a {}", id, tipoRol);

        // Validar parámetros
        if (id == null || id <= 0) {
            throw new ValidationException("El ID del usuario es obligatorio y debe ser positivo");
        }

        validarTipoRol(tipoRol);

        try {
            // Verificar que el usuario existe
            Optional<UsuarioPersona> usuarioOpt = usuarioPersonaDAO.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                throw new ValidationException("Usuario con ID " + id + " no encontrado");
            }

            // Determinar flags según el tipo de rol
            Boolean esCliente = tipoRol.equals("CLIENTE") || tipoRol.equals("AMBOS");
            Boolean esProfesional = tipoRol.equals("PROFESIONAL") || tipoRol.equals("AMBOS");

            boolean resultado = usuarioPersonaDAO.actualizarTipoRol(id, tipoRol, esCliente, esProfesional);

            logger.info("Tipo de rol actualizado para usuario ID {}: {}", id, tipoRol);
            return resultado;

        } catch (DatabaseException e) {
            logger.error("Error al actualizar tipo de rol para usuario ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean actualizarUbicacion(Long id, Integer deptoId, Integer provId, Integer distId,
                                      String direccion, String referencia)
            throws ValidationException, DatabaseException {
        logger.debug("Actualizando ubicación para usuario ID: {}", id);

        // Validar parámetros
        if (id == null || id <= 0) {
            throw new ValidationException("El ID del usuario es obligatorio y debe ser positivo");
        }

        if (deptoId != null || provId != null || distId != null) {
            if (deptoId == null || provId == null || distId == null) {
                throw new ValidationException("Si se especifica ubicación, departamento, provincia y distrito son obligatorios");
            }

            if (deptoId <= 0 || provId <= 0 || distId <= 0) {
                throw new ValidationException("Los IDs de ubicación deben ser números positivos");
            }
        }

        try {
            // Verificar que el usuario existe
            Optional<UsuarioPersona> usuarioOpt = usuarioPersonaDAO.buscarPorId(id);
            if (!usuarioOpt.isPresent()) {
                throw new ValidationException("Usuario con ID " + id + " no encontrado");
            }

            boolean resultado = usuarioPersonaDAO.actualizarUbicacion(id, deptoId, provId, distId,
                                                                      direccion, referencia);

            logger.info("Ubicación actualizada para usuario ID: {}", id);
            return resultado;

        } catch (DatabaseException e) {
            logger.error("Error al actualizar ubicación para usuario ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public void validarDatosPersonales(UsuarioPersonaDTO dto) throws ValidationException {
        List<String> errores = new ArrayList<>();

        // Validar nombre completo
        if (dto.getNombreCompleto() == null || dto.getNombreCompleto().trim().isEmpty()) {
            errores.add("El nombre completo es obligatorio");
        } else if (dto.getNombreCompleto().trim().length() < 3) {
            errores.add("El nombre completo debe tener al menos 3 caracteres");
        } else if (dto.getNombreCompleto().trim().length() > 100) {
            errores.add("El nombre completo no puede exceder 100 caracteres");
        }

        // Validar tipo de documento
        if (dto.getTipoDocumento() != null && !dto.getTipoDocumento().trim().isEmpty()) {
            String tipoDoc = dto.getTipoDocumento().toUpperCase().trim();
            if (!TIPOS_DOCUMENTO_VALIDOS.contains(tipoDoc)) {
                errores.add("El tipo de documento debe ser uno de: " + String.join(", ", TIPOS_DOCUMENTO_VALIDOS));
            }
        }

        // Validar número de documento según tipo
        if (dto.getNumeroDocumento() != null && !dto.getNumeroDocumento().trim().isEmpty()) {
            String tipoDoc = dto.getTipoDocumento() != null ? dto.getTipoDocumento().toUpperCase() : "DNI";
            String numDoc = dto.getNumeroDocumento().trim();

            switch (tipoDoc) {
                case "DNI":
                    if (!DNI_PATTERN.matcher(numDoc).matches()) {
                        errores.add("El DNI debe tener exactamente 8 dígitos");
                    }
                    break;
                case "RUC":
                    if (!RUC_PATTERN.matcher(numDoc).matches()) {
                        errores.add("El RUC debe tener exactamente 11 dígitos");
                    }
                    break;
                case "CE":
                    if (!CE_PATTERN.matcher(numDoc).matches()) {
                        errores.add("El Carnet de Extranjería debe tener entre 9 y 12 dígitos");
                    }
                    break;
                case "PASAPORTE":
                    if (numDoc.length() < 6 || numDoc.length() > 12) {
                        errores.add("El número de pasaporte debe tener entre 6 y 12 caracteres");
                    }
                    break;
            }
        }

        // Validar teléfono
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
            if (!TELEFONO_PATTERN.matcher(dto.getTelefono().trim()).matches()) {
                errores.add("El teléfono debe tener exactamente 9 dígitos");
            }
        }

        // Validar teléfono alternativo
        if (dto.getTelefonoAlternativo() != null && !dto.getTelefonoAlternativo().trim().isEmpty()) {
            if (!TELEFONO_PATTERN.matcher(dto.getTelefonoAlternativo().trim()).matches()) {
                errores.add("El teléfono alternativo debe tener exactamente 9 dígitos");
            }
        }

        // Validar género
        if (dto.getGenero() != null && !dto.getGenero().trim().isEmpty()) {
            String genero = dto.getGenero().toUpperCase().trim();
            if (!GENEROS_VALIDOS.contains(genero)) {
                errores.add("El género debe ser uno de: " + String.join(", ", GENEROS_VALIDOS));
            }
        }

        // Validar tipo de rol
        if (dto.getTipoRol() != null && !dto.getTipoRol().trim().isEmpty()) {
            validarTipoRol(dto.getTipoRol());
            validarConsistenciaRol(dto);
        }

        if (!errores.isEmpty()) {
            throw new ValidationException(String.join(". ", errores));
        }
    }

    /**
     * Valida que el tipo de rol sea válido
     */
    private void validarTipoRol(String tipoRol) throws ValidationException {
        if (tipoRol == null || tipoRol.trim().isEmpty()) {
            throw new ValidationException("El tipo de rol es obligatorio");
        }

        String rolUpper = tipoRol.toUpperCase().trim();
        if (!TIPOS_ROL_VALIDOS.contains(rolUpper)) {
            throw new ValidationException("El tipo de rol debe ser uno de: " + String.join(", ", TIPOS_ROL_VALIDOS));
        }
    }

    /**
     * Valida la consistencia entre tipoRol, esCliente y esProfesional
     */
    private void validarConsistenciaRol(UsuarioPersonaDTO dto) throws ValidationException {
        String tipoRol = dto.getTipoRol().toUpperCase();
        Boolean esCliente = dto.getEsCliente();
        Boolean esProfesional = dto.getEsProfesional();

        if (esCliente == null || esProfesional == null) {
            return; // Los flags serán calculados automáticamente
        }

        switch (tipoRol) {
            case "CLIENTE":
                if (!esCliente || esProfesional) {
                    throw new ValidationException("Para tipo de rol CLIENTE, esCliente debe ser true y esProfesional false");
                }
                break;
            case "PROFESIONAL":
                if (esCliente || !esProfesional) {
                    throw new ValidationException("Para tipo de rol PROFESIONAL, esProfesional debe ser true y esCliente false");
                }
                break;
            case "AMBOS":
                if (!esCliente || !esProfesional) {
                    throw new ValidationException("Para tipo de rol AMBOS, ambos flags deben ser true");
                }
                break;
        }
    }

    /**
     * Actualiza los datos de un usuario existente
     */
    private void actualizarDatosUsuario(UsuarioPersona usuario, UsuarioPersonaDTO dto) {
        if (dto.getNombreCompleto() != null) {
            usuario.setNombreCompleto(dto.getNombreCompleto().trim());
        }
        if (dto.getTipoDocumento() != null) {
            usuario.setTipoDocumento(dto.getTipoDocumento().toUpperCase().trim());
        }
        if (dto.getNumeroDocumento() != null) {
            usuario.setNumeroDocumento(dto.getNumeroDocumento().trim());
        }
        if (dto.getFechaNacimiento() != null) {
            usuario.setFechaNacimiento(dto.getFechaNacimiento());
        }
        if (dto.getGenero() != null) {
            usuario.setGenero(dto.getGenero().toUpperCase().trim());
        }
        if (dto.getTelefono() != null) {
            usuario.setTelefono(dto.getTelefono().trim());
        }
        if (dto.getTelefonoAlternativo() != null) {
            usuario.setTelefonoAlternativo(dto.getTelefonoAlternativo().trim());
        }
        if (dto.getDepartamentoId() != null) {
            usuario.setDepartamentoId(dto.getDepartamentoId());
        }
        if (dto.getProvinciaId() != null) {
            usuario.setProvinciaId(dto.getProvinciaId());
        }
        if (dto.getDistritoId() != null) {
            usuario.setDistritoId(dto.getDistritoId());
        }
        if (dto.getDireccion() != null) {
            usuario.setDireccion(dto.getDireccion().trim());
        }
        if (dto.getReferenciaDireccion() != null) {
            usuario.setReferenciaDireccion(dto.getReferenciaDireccion().trim());
        }
        if (dto.getTipoRol() != null) {
            String tipoRol = dto.getTipoRol().toUpperCase();
            usuario.setTipoRol(tipoRol);
            usuario.setEsCliente(tipoRol.equals("CLIENTE") || tipoRol.equals("AMBOS"));
            usuario.setEsProfesional(tipoRol.equals("PROFESIONAL") || tipoRol.equals("AMBOS"));
        }
        if (dto.getFotoPerfilUrl() != null) {
            usuario.setFotoPerfilUrl(dto.getFotoPerfilUrl());
        }
    }

    /**
     * Convierte un UsuarioPersonaDTO a UsuarioPersona (modelo)
     */
    private UsuarioPersona convertirDTOAModelo(UsuarioPersonaDTO dto) {
        UsuarioPersona usuario = new UsuarioPersona();

        usuario.setNombreCompleto(dto.getNombreCompleto().trim());
        usuario.setTipoDocumento(dto.getTipoDocumento() != null ?
                                dto.getTipoDocumento().toUpperCase().trim() : "DNI");
        usuario.setNumeroDocumento(dto.getNumeroDocumento() != null ?
                                   dto.getNumeroDocumento().trim() : null);
        usuario.setFechaNacimiento(dto.getFechaNacimiento());
        usuario.setGenero(dto.getGenero() != null ? dto.getGenero().toUpperCase().trim() : null);
        usuario.setTelefono(dto.getTelefono() != null ? dto.getTelefono().trim() : null);
        usuario.setTelefonoAlternativo(dto.getTelefonoAlternativo() != null ?
                                      dto.getTelefonoAlternativo().trim() : null);

        // Ubicación
        usuario.setDepartamentoId(dto.getDepartamentoId());
        usuario.setProvinciaId(dto.getProvinciaId());
        usuario.setDistritoId(dto.getDistritoId());
        usuario.setDireccion(dto.getDireccion() != null ? dto.getDireccion().trim() : null);
        usuario.setReferenciaDireccion(dto.getReferenciaDireccion() != null ?
                                       dto.getReferenciaDireccion().trim() : null);

        // Rol
        String tipoRol = dto.getTipoRol() != null ? dto.getTipoRol().toUpperCase() : "CLIENTE";
        usuario.setTipoRol(tipoRol);
        usuario.setEsCliente(tipoRol.equals("CLIENTE") || tipoRol.equals("AMBOS"));
        usuario.setEsProfesional(tipoRol.equals("PROFESIONAL") || tipoRol.equals("AMBOS"));

        usuario.setFotoPerfilUrl(dto.getFotoPerfilUrl());

        return usuario;
    }

    /**
     * Convierte un UsuarioPersona (modelo) a UsuarioPersonaDTO
     */
    private UsuarioPersonaDTO convertirModeloADTO(UsuarioPersona usuario) {
        UsuarioPersonaDTO dto = new UsuarioPersonaDTO();

        dto.setId(usuario.getId());
        dto.setNombreCompleto(usuario.getNombreCompleto());
        dto.setTipoDocumento(usuario.getTipoDocumento());
        dto.setNumeroDocumento(usuario.getNumeroDocumento());
        dto.setFechaNacimiento(usuario.getFechaNacimiento());
        dto.setGenero(usuario.getGenero());
        dto.setTelefono(usuario.getTelefono());
        dto.setTelefonoAlternativo(usuario.getTelefonoAlternativo());

        // Ubicación
        dto.setDepartamentoId(usuario.getDepartamentoId());
        dto.setProvinciaId(usuario.getProvinciaId());
        dto.setDistritoId(usuario.getDistritoId());
        dto.setDireccion(usuario.getDireccion());
        dto.setReferenciaDireccion(usuario.getReferenciaDireccion());

        // Rol
        dto.setTipoRol(usuario.getTipoRol());
        dto.setEsCliente(usuario.getEsCliente());
        dto.setEsProfesional(usuario.getEsProfesional());

        dto.setFotoPerfilUrl(usuario.getFotoPerfilUrl());
        dto.setFechaCreacion(usuario.getFechaCreacion());
        dto.setFechaActualizacion(usuario.getFechaActualizacion());
        dto.setActivo(usuario.getActivo());

        return dto;
    }
}
