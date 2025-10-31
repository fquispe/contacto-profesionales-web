package com.contactoprofesionales.service.auth;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.dao.usuario.UsuarioDAO;
import com.contactoprofesionales.dao.usuario.UsuarioDAOImpl;
import com.contactoprofesionales.util.PasswordHasher;
import com.contactoprofesionales.exception.AuthenticationException;
import com.contactoprofesionales.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementación del servicio de autenticación.
 * 
 * Aplicación de SRP: Solo lógica de autenticación.
 * Aplicación de DIP: Depende de UsuarioDAO (interfaz).
 */
public class AutenticacionServiceImpl implements AutenticacionService {
    private static final Logger logger = LoggerFactory.getLogger(AutenticacionServiceImpl.class);
    
    private final UsuarioDAO usuarioDAO;
    private final PasswordHasher passwordHasher;

    public AutenticacionServiceImpl() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.passwordHasher = new PasswordHasher();
        logger.info("AutenticacionService inicializado");
    }

    // Constructor para testing (inyección de dependencias)
    public AutenticacionServiceImpl(UsuarioDAO usuarioDAO) {
        this.usuarioDAO = usuarioDAO;
        this.passwordHasher = new PasswordHasher();
    }

    @Override
    public Usuario autenticar(String email, String password) 
            throws AuthenticationException, DatabaseException {
        
        logger.info("Intento de autenticación para: {}", email);
        
        // Validar entrada
        validarCredenciales(email, password);
        
        // Buscar usuario
        Usuario usuario = usuarioDAO.buscarPorEmail(email);
        
        if (usuario == null) {
            logger.warn("✗ Usuario no encontrado: {}", email);
            throw new AuthenticationException("Credenciales inválidas");
        }
        
        if (!usuario.isActivo()) {
            logger.warn("✗ Usuario inactivo: {}", email);
            throw new AuthenticationException("Usuario inactivo. Contacte al administrador");
        }
        
        // Verificar contraseña
        if (!passwordHasher.verify(password, usuario.getPasswordHash())) {
            logger.warn("✗ Contraseña incorrecta para: {}", email);
            throw new AuthenticationException("Credenciales inválidas");
        }
        
        logger.info("✓ Usuario autenticado exitosamente: {}", email);
        
        // Actualizar último acceso
        actualizarUltimoAcceso(usuario);
        
        return usuario;
    }

    @Override
    public Usuario registrar(Usuario usuario) 
            throws AuthenticationException, DatabaseException {
        
        logger.info("Registrando nuevo usuario: {}", usuario.getEmail());
        
        // Validar datos
        validarDatosUsuario(usuario);
        
        // Verificar que el email no exista
        if (usuarioDAO.existeEmail(usuario.getEmail())) {
            logger.warn("✗ Email ya registrado: {}", usuario.getEmail());
            throw new AuthenticationException("El email ya está registrado");
        }
        
        // Hashear contraseña
        String passwordHash = passwordHasher.hash(usuario.getPasswordHash()); // En registro, viene en texto plano
        usuario.setPasswordHash(passwordHash);
        
        // Registrar en BD
        boolean registrado = usuarioDAO.registrar(usuario);
        
        if (!registrado) {
            throw new DatabaseException("Error al registrar usuario");
        }
        
        logger.info("✓ Usuario registrado exitosamente: {}", usuario.getEmail());
        
        return usuario;
    }

    @Override
    public void validarCredenciales(String email, String password) 
            throws AuthenticationException {
        
        if (email == null || email.trim().isEmpty()) {
            throw new AuthenticationException("El email es requerido");
        }
        
        if (password == null || password.isEmpty()) {
            throw new AuthenticationException("La contraseña es requerida");
        }
        
        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new AuthenticationException("Formato de email inválido");
        }
    }

    /**
     * Valida todos los datos del usuario para registro.
     */
    private void validarDatosUsuario(Usuario usuario) throws AuthenticationException {
        if (usuario == null) {
            throw new AuthenticationException("Usuario nulo");
        }
        
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            throw new AuthenticationException("El nombre es requerido");
        }
        
        validarCredenciales(usuario.getEmail(), usuario.getPasswordHash());
        
        // Validar longitud de contraseña
        if (usuario.getPasswordHash().length() < 6) {
            throw new AuthenticationException("La contraseña debe tener al menos 6 caracteres");
        }
    }

    /**
     * Actualiza la fecha de último acceso del usuario.
     */
    private void actualizarUltimoAcceso(Usuario usuario) {
        try {
            usuarioDAO.actualizar(usuario);
            logger.debug("Último acceso actualizado para: {}", usuario.getEmail());
        } catch (DatabaseException e) {
            // No es crítico, solo log
            logger.warn("No se pudo actualizar último acceso para: {}", usuario.getEmail());
        }
    }
}