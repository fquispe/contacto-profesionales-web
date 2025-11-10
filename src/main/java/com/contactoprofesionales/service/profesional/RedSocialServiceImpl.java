package com.contactoprofesionales.service.profesional;

import com.contactoprofesionales.dao.profesional.RedSocialProfesionalDAO;
import com.contactoprofesionales.dao.profesional.RedSocialProfesionalDAOImpl;
import com.contactoprofesionales.dto.RedSocialDTO;
import com.contactoprofesionales.exception.DatabaseException;
import com.contactoprofesionales.exception.ValidationException;
import com.contactoprofesionales.model.RedSocialProfesional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Implementación del servicio de redes sociales de profesionales.
 * Gestiona la lógica de negocio para redes sociales de profesionales.
 */
public class RedSocialServiceImpl implements RedSocialService {

    private static final Logger logger = LoggerFactory.getLogger(RedSocialServiceImpl.class);
    private final RedSocialProfesionalDAO redSocialDAO;

    // Tipos de redes sociales válidos
    private static final List<String> TIPOS_REDES_VALIDOS = Arrays.asList(
        "FACEBOOK", "INSTAGRAM", "LINKEDIN", "TWITTER", "TIKTOK", "WHATSAPP", "WEBSITE", "YOUTUBE"
    );

    // Patrón de validación de URL
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?://)?(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z]{2,})+(/.*)?$"
    );

    // Patrón específico para WhatsApp
    private static final Pattern WHATSAPP_PATTERN = Pattern.compile("^\\+?[0-9]{9,15}$");

    public RedSocialServiceImpl() {
        this.redSocialDAO = new RedSocialProfesionalDAOImpl();
    }

    @Override
    public RedSocialDTO agregar(Integer profesionalId, String tipoRed, String url)
            throws ValidationException, DatabaseException {
        logger.debug("Agregando red social {} para profesional ID: {}", tipoRed, profesionalId);

        // Validar parámetros
        validarParametrosAgregar(profesionalId, tipoRed, url);

        try {
            // Verificar que no tenga ya esta red social
            List<RedSocialProfesional> redesExistentes = redSocialDAO.listarPorProfesional(profesionalId);
            String tipoRedUpper = tipoRed.toUpperCase().trim();

            for (RedSocialProfesional red : redesExistentes) {
                if (red.getTipoRed().equals(tipoRedUpper)) {
                    throw new ValidationException(
                        "El profesional ya tiene registrada una red social de tipo " + tipoRedUpper +
                        ". Debe eliminarla o actualizarla en lugar de agregar una nueva"
                    );
                }
            }

            // Crear red social
            RedSocialProfesional redSocial = new RedSocialProfesional();
            redSocial.setProfesionalId(profesionalId);
            redSocial.setTipoRed(tipoRedUpper);
            redSocial.setUrl(url.trim());
            redSocial.setVerificada(false);

            // Registrar red social
            redSocial = redSocialDAO.registrar(redSocial);

            logger.info("Red social {} agregada exitosamente con ID: {} para profesional ID: {}",
                       tipoRedUpper, redSocial.getId(), profesionalId);

            return convertirModeloADTO(redSocial);

        } catch (DatabaseException e) {
            logger.error("Error al agregar red social: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<RedSocialDTO> listarPorProfesional(Integer profesionalId)
            throws ValidationException, DatabaseException {
        logger.debug("Listando redes sociales para profesional ID: {}", profesionalId);

        // Validar parámetros
        if (profesionalId == null) {
            throw new ValidationException("El ID del profesional es obligatorio");
        }

        if (profesionalId <= 0) {
            throw new ValidationException("El ID del profesional debe ser un número positivo");
        }

        try {
            List<RedSocialProfesional> redesSociales = redSocialDAO.listarPorProfesional(profesionalId);
            List<RedSocialDTO> redesSocialesDTO = new ArrayList<>();

            for (RedSocialProfesional redSocial : redesSociales) {
                redesSocialesDTO.add(convertirModeloADTO(redSocial));
            }

            logger.info("Se encontraron {} redes sociales para profesional ID: {}",
                       redesSocialesDTO.size(), profesionalId);

            return redesSocialesDTO;

        } catch (DatabaseException e) {
            logger.error("Error al listar redes sociales del profesional {}: {}",
                        profesionalId, e.getMessage());
            throw e;
        }
    }

    @Override
    public RedSocialDTO actualizar(Integer id, String url)
            throws ValidationException, DatabaseException {
        logger.debug("Actualizando red social con ID: {}", id);

        // Validar parámetros
        if (id == null) {
            throw new ValidationException("El ID de la red social es obligatorio");
        }

        if (id <= 0) {
            throw new ValidationException("El ID de la red social debe ser un número positivo");
        }

        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("La URL es obligatoria");
        }

        // Validar URL
        validarUrl(url);

        try {
            // Obtener la red social actual
            List<RedSocialProfesional> todasLasRedes = redSocialDAO.listarPorProfesional(Integer.MAX_VALUE);
            RedSocialProfesional redSocialActual = null;

            for (RedSocialProfesional red : todasLasRedes) {
                if (red.getId().equals(id)) {
                    redSocialActual = red;
                    break;
                }
            }

            if (redSocialActual == null) {
                throw new ValidationException("Red social con ID " + id + " no encontrada");
            }

            // Validar URL según tipo de red
            if (redSocialActual.getTipoRed().equals("WHATSAPP")) {
                if (!WHATSAPP_PATTERN.matcher(url.trim()).matches()) {
                    throw new ValidationException(
                        "Para WhatsApp debe proporcionar un número de teléfono válido (9-15 dígitos)"
                    );
                }
            }

            // Actualizar URL
            redSocialActual.setUrl(url.trim());
            redSocialActual = redSocialDAO.actualizar(redSocialActual);

            logger.info("Red social actualizada exitosamente con ID: {}", id);

            return convertirModeloADTO(redSocialActual);

        } catch (DatabaseException e) {
            logger.error("Error al actualizar red social con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public boolean eliminar(Integer id) throws ValidationException, DatabaseException {
        logger.debug("Eliminando red social con ID: {}", id);

        // Validar parámetros
        if (id == null) {
            throw new ValidationException("El ID de la red social es obligatorio");
        }

        if (id <= 0) {
            throw new ValidationException("El ID de la red social debe ser un número positivo");
        }

        try {
            boolean resultado = redSocialDAO.eliminar(id);

            if (resultado) {
                logger.info("Red social eliminada exitosamente con ID: {}", id);
            } else {
                logger.warn("No se pudo eliminar la red social con ID: {}", id);
            }

            return resultado;

        } catch (DatabaseException e) {
            logger.error("Error al eliminar red social con ID {}: {}", id, e.getMessage());
            throw e;
        }
    }

    @Override
    public void validarUrl(String url) throws ValidationException {
        if (url == null || url.trim().isEmpty()) {
            throw new ValidationException("La URL es obligatoria");
        }

        String urlTrim = url.trim();

        // Validación básica de longitud
        if (urlTrim.length() < 5) {
            throw new ValidationException("La URL debe tener al menos 5 caracteres");
        }

        if (urlTrim.length() > 500) {
            throw new ValidationException("La URL no puede exceder 500 caracteres");
        }

        // Validar formato básico de URL (excepto para números de WhatsApp)
        if (!WHATSAPP_PATTERN.matcher(urlTrim).matches()) {
            if (!URL_PATTERN.matcher(urlTrim).matches()) {
                throw new ValidationException(
                    "La URL no tiene un formato válido. " +
                    "Debe ser una URL válida (ej: https://www.ejemplo.com) o un número de teléfono para WhatsApp"
                );
            }
        }
    }

    @Override
    public void validarTipoRed(String tipoRed) throws ValidationException {
        if (tipoRed == null || tipoRed.trim().isEmpty()) {
            throw new ValidationException("El tipo de red social es obligatorio");
        }

        String tipoRedUpper = tipoRed.toUpperCase().trim();

        if (!TIPOS_REDES_VALIDOS.contains(tipoRedUpper)) {
            throw new ValidationException(
                "El tipo de red social debe ser uno de: " + String.join(", ", TIPOS_REDES_VALIDOS)
            );
        }
    }

    /**
     * Valida los parámetros para agregar una red social
     */
    private void validarParametrosAgregar(Integer profesionalId, String tipoRed, String url)
            throws ValidationException {
        List<String> errores = new ArrayList<>();

        if (profesionalId == null) {
            errores.add("El ID del profesional es obligatorio");
        } else if (profesionalId <= 0) {
            errores.add("El ID del profesional debe ser un número positivo");
        }

        if (tipoRed == null || tipoRed.trim().isEmpty()) {
            errores.add("El tipo de red social es obligatorio");
        } else {
            try {
                validarTipoRed(tipoRed);
            } catch (ValidationException e) {
                errores.add(e.getMessage());
            }
        }

        if (url == null || url.trim().isEmpty()) {
            errores.add("La URL es obligatoria");
        } else {
            try {
                validarUrl(url);

                // Validación específica para WhatsApp
                if (tipoRed != null && tipoRed.toUpperCase().trim().equals("WHATSAPP")) {
                    if (!WHATSAPP_PATTERN.matcher(url.trim()).matches()) {
                        errores.add(
                            "Para WhatsApp debe proporcionar un número de teléfono válido (9-15 dígitos, puede incluir +)"
                        );
                    }
                }
            } catch (ValidationException e) {
                errores.add(e.getMessage());
            }
        }

        if (!errores.isEmpty()) {
            throw new ValidationException(String.join(". ", errores));
        }
    }

    /**
     * Convierte un RedSocialProfesional (modelo) a RedSocialDTO
     */
    private RedSocialDTO convertirModeloADTO(RedSocialProfesional redSocial) {
        RedSocialDTO dto = new RedSocialDTO();

        dto.setId(redSocial.getId());
        dto.setProfesionalId(redSocial.getProfesionalId());
        dto.setTipoRed(redSocial.getTipoRed());
        dto.setUrl(redSocial.getUrl());
        dto.setVerificada(redSocial.getVerificada());

        return dto;
    }
}
