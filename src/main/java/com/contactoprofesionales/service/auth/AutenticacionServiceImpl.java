package com.contactoprofesionales.service.auth;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.dao.usuario.UsuarioDAO;
import com.contactoprofesionales.dao.usuario.UsuarioDAOImpl;
import com.contactoprofesionales.dto.RegistroCompletoRequest;
import com.contactoprofesionales.dto.RegistroCompletoResponse;
import com.contactoprofesionales.util.PasswordHasher;
import com.contactoprofesionales.util.DatabaseConnection;
import com.contactoprofesionales.exception.AuthenticationException;
import com.contactoprofesionales.exception.UserNotFoundException; // ✅ NUEVO: Excepción para usuario no encontrado (añadido: 2025-11-15)
import com.contactoprofesionales.exception.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.sql.Date;

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

    // ✅ ACTUALIZADO: Diferencia entre usuario no existe vs contraseña incorrecta (actualizado: 2025-11-15)
    @Override
    public Usuario autenticar(String email, String password)
            throws AuthenticationException, DatabaseException {

        logger.info("Intento de autenticación para: {}", email);

        // Validar entrada
        validarCredenciales(email, password);

        // Buscar usuario
        Usuario usuario = usuarioDAO.buscarPorEmail(email);

        // ✅ CAMBIO IMPORTANTE: Lanzar UserNotFoundException cuando el usuario no existe
        // Esto permite NO contar intentos fallidos en el frontend
        if (usuario == null) {
            logger.warn("✗ Usuario no encontrado: {}", email);
            throw new UserNotFoundException("Usuario no encontrado. Por favor regístrese");
        }

        if (!usuario.getActivo()) {
            logger.warn("✗ Usuario inactivo: {}", email);
            throw new AuthenticationException("Usuario inactivo. Contacte al administrador");
        }

        // ✅ CAMBIO IMPORTANTE: Para contraseña incorrecta, lanzar AuthenticationException
        // Esto SÍ contará intentos fallidos en el frontend
        if (!passwordHasher.verify(password, usuario.getPasswordHash())) {
            logger.warn("✗ Contraseña incorrecta para: {}", email);
            throw new AuthenticationException("Contraseña incorrecta");
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
        
        // Validar datos básicos
        
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new AuthenticationException("El email es requerido");
        }
        
        // Validar formato de email
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new AuthenticationException("Formato de email inválido");
        }
        
        // Verificar que el email no exista
        if (usuarioDAO.existeEmail(usuario.getEmail())) {
            logger.warn("✗ Email ya registrado: {}", usuario.getEmail());
            throw new AuthenticationException("El email ya está registrado");
        }
        
        // Validar que tenga password hash
        if (usuario.getPasswordHash() == null || usuario.getPasswordHash().isEmpty()) {
            throw new AuthenticationException("La contraseña es requerida");
        }
        
        // Registrar en BD
        Usuario usuarioCreado = usuarioDAO.registrar(usuario);
        
        if (usuarioCreado == null || usuarioCreado.getId() == null) {
            throw new DatabaseException("Error al registrar usuario");
        }
        
        logger.info("✓ Usuario registrado exitosamente: {}", usuario.getEmail());
        
        return usuarioCreado;
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
                
        // Validar datos básicos
        if (usuario == null) {
            throw new AuthenticationException("Usuario nulo");
        }
        
        if (usuario.getEmail() == null || usuario.getEmail().trim().isEmpty()) {
            throw new AuthenticationException("El email es requerido");
        }
        
        // Validar formato de email
        if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new AuthenticationException("Formato de email inválido");
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

    @Override
    public RegistroCompletoResponse registrarCompleto(RegistroCompletoRequest request)
            throws AuthenticationException, DatabaseException {

        logger.info("Iniciando registro completo para: {}", request.getEmail());

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Validar datos del request
            validarDatosRegistroCompleto(request);

            // 2. Validar que tipoCuenta sea válido
            String tipoCuenta = request.getTipoCuenta().toUpperCase();
            if (!tipoCuenta.equals("CLIENTE") && !tipoCuenta.equals("PROFESIONAL") && !tipoCuenta.equals("AMBOS")) {
                throw new AuthenticationException("Tipo de cuenta inválido. Debe ser CLIENTE, PROFESIONAL o AMBOS");
            }

            // 3. Determinar flags esCliente y esProfesional
            boolean esCliente = tipoCuenta.equals("CLIENTE") || tipoCuenta.equals("AMBOS");
            boolean esProfesional = tipoCuenta.equals("PROFESIONAL") || tipoCuenta.equals("AMBOS");

            // 4. Verificar que email no exista en tabla users
            if (existeEmail(conn, request.getEmail())) {
                throw new AuthenticationException("El email ya está registrado");
            }

            // 5. Verificar que numeroDocumento no exista (si viene)
            if (request.getNumeroDocumento() != null && !request.getNumeroDocumento().trim().isEmpty()) {
                if (existeNumeroDocumento(conn, request.getNumeroDocumento())) {
                    throw new AuthenticationException("El número de documento ya está registrado");
                }
            }

            // 6. Verificar que telefono no exista (si viene)
            if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
                if (existeTelefono(conn, request.getTelefono())) {
                    throw new AuthenticationException("El teléfono ya está registrado");
                }
            }

            // 7. CREAR UsuarioPersona en tabla usuarios PRIMERO
            Long usuarioPersonaId = crearUsuarioPersona(conn, request, esCliente, esProfesional);
            logger.info("UsuarioPersona creado en tabla 'usuarios' con ID: {}", usuarioPersonaId);

            // 8. CREAR Usuario en tabla users (ahora con el usuarioPersonaId)
            Integer userId = crearUsuario(conn, request, usuarioPersonaId);
            logger.info("Usuario creado en tabla 'users' con ID: {}", userId);

            // Variables para IDs de cliente y profesional
            Long clienteId = null;
            Integer profesionalId = null;

            // 9. SI esCliente = true: Crear registro en tabla clientes
            if (esCliente) {
                clienteId = crearCliente(conn, userId, request); // ← Cambiar usuarioPersonaId por userId
                logger.info("Cliente creado en tabla 'clientes' con ID: {}", clienteId);
            }

            // 10. SI esProfesional = true: Crear registro en tabla profesionales
            if (esProfesional) {
                profesionalId = crearProfesional(conn, userId, request);
                logger.info("Profesional creado en tabla 'profesionales' con ID: {}", profesionalId);
            }

            // Todo exitoso, hacer commit
            conn.commit();
            logger.info("Registro completo exitoso para: {} (UserID: {}, UsuarioPersonaID: {})",
                    request.getEmail(), userId, usuarioPersonaId);

            // 11. Crear y retornar response
            RegistroCompletoResponse response = new RegistroCompletoResponse();
            response.setUserId(userId);
            response.setUsuarioPersonaId(usuarioPersonaId);
            response.setClienteId(clienteId);
            response.setProfesionalId(profesionalId);
            response.setEmail(request.getEmail());
            response.setNombreCompleto(request.getNombreCompleto());
            response.setTipoRol(tipoCuenta);
            response.setEsCliente(esCliente);
            response.setEsProfesional(esProfesional);
            response.setExitoso(true);
            response.setMensaje("Registro exitoso");

            return response;

        } catch (SQLException e) {
            logger.error("Error SQL durante el registro completo: {}", e.getMessage(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Rollback ejecutado correctamente");
                } catch (SQLException rollbackEx) {
                    logger.error("Error al hacer rollback", rollbackEx);
                }
            }
            throw new DatabaseException("Error en la base de datos durante el registro: " + e.getMessage(), e);

        } catch (AuthenticationException e) {
            logger.error("Error de autenticación durante el registro: {}", e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Rollback ejecutado correctamente");
                } catch (SQLException rollbackEx) {
                    logger.error("Error al hacer rollback", rollbackEx);
                }
            }
            throw e;

        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.error("Error al cerrar la conexión", e);
                }
            }
        }
    }

    /**
     * Valida los datos completos del request de registro
     */
    private void validarDatosRegistroCompleto(RegistroCompletoRequest request) throws AuthenticationException {
        // Validar email
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new AuthenticationException("El email es requerido");
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new AuthenticationException("Formato de email inválido");
        }

        // Validar password
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new AuthenticationException("La contraseña es requerida");
        }
        if (request.getPassword().length() < 6) {
            throw new AuthenticationException("La contraseña debe tener al menos 6 caracteres");
        }

        // Validar nombreCompleto
        if (request.getNombreCompleto() == null || request.getNombreCompleto().trim().isEmpty()) {
            throw new AuthenticationException("El nombre completo es requerido");
        }

        // Validar tipoCuenta
        if (request.getTipoCuenta() == null || request.getTipoCuenta().trim().isEmpty()) {
            throw new AuthenticationException("El tipo de cuenta es requerido");
        }

        // Validar teléfono (si viene)
        if (request.getTelefono() != null && !request.getTelefono().trim().isEmpty()) {
            if (!request.getTelefono().matches("^\\d{9}$")) {
                throw new AuthenticationException("El teléfono debe tener 9 dígitos");
            }
        }

        // Validar numeroDocumento según tipoDocumento (si vienen)
        if (request.getNumeroDocumento() != null && !request.getNumeroDocumento().trim().isEmpty()) {
            String tipoDoc = request.getTipoDocumento();
            String numeroDoc = request.getNumeroDocumento();

            if (tipoDoc != null) {
                switch (tipoDoc.toUpperCase()) {
                    case "DNI":
                        if (!numeroDoc.matches("^\\d{8}$")) {
                            throw new AuthenticationException("El DNI debe tener 8 dígitos");
                        }
                        break;
                    case "RUC":
                        if (!numeroDoc.matches("^\\d{11}$")) {
                            throw new AuthenticationException("El RUC debe tener 11 dígitos");
                        }
                        break;
                    case "CE":
                        if (numeroDoc.length() < 9 || numeroDoc.length() > 12) {
                            throw new AuthenticationException("El Carnet de Extranjería debe tener entre 9 y 12 caracteres");
                        }
                        break;
                    case "PASAPORTE":
                        if (numeroDoc.length() < 6 || numeroDoc.length() > 12) {
                            throw new AuthenticationException("El Pasaporte debe tener entre 6 y 12 caracteres");
                        }
                        break;
                }
            }
        }
    }

    /**
     * Verifica si un email ya existe en la tabla users
     */
    private boolean existeEmail(Connection conn, String email) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE email = ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    /**
     * Verifica si un número de documento ya existe en la tabla usuarios
     */
    private boolean existeNumeroDocumento(Connection conn, String numeroDocumento) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM usuarios WHERE numero_documento = ? AND activo = true)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, numeroDocumento);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    /**
     * Verifica si un teléfono ya existe en la tabla usuarios
     */
    private boolean existeTelefono(Connection conn, String telefono) throws SQLException {
        String sql = "SELECT EXISTS(SELECT 1 FROM usuarios WHERE telefono = ? AND activo = true)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, telefono);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        }
        return false;
    }

    /**
     * Crea un usuario en la tabla users con referencia a usuarioPersona
     */
    private Integer crearUsuario(Connection conn, RegistroCompletoRequest request, Long usuarioPersonaId) 
            throws SQLException {
        
        // Hashear la contraseña
        String passwordHash = passwordHasher.hash(request.getPassword());
        
        String sql = "INSERT INTO users (email, password_hash, usuario_id, username, " +
                     "rol_sistema, activo) " +
                     "VALUES (?, ?, ?, ?, ?, true) RETURNING id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, request.getEmail());
            stmt.setString(2, passwordHash);
            stmt.setLong(3, usuarioPersonaId);
            stmt.setString(4, request.getEmail().split("@")[0]);
            stmt.setString(5, "USER"); // Rol por defecto
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }
        
        throw new SQLException("No se pudo crear el usuario en 'users'");
    }

    /**
     * Crea un usuario persona en la tabla usuarios
     */
    private Long crearUsuarioPersona(Connection conn, RegistroCompletoRequest request,
                                     boolean esCliente, boolean esProfesional) throws SQLException {

        String sql = "INSERT INTO usuarios (nombre_completo, tipo_documento, numero_documento, " +
                     "fecha_nacimiento, genero, telefono, telefono_alternativo, " +
                     "departamento_id, provincia_id, distrito_id, direccion, referencia_direccion, " +
                     "tipo_rol, es_cliente, es_profesional, fecha_creacion, fecha_actualizacion, activo) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true) RETURNING id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            int index = 1;
            stmt.setString(index++, request.getNombreCompleto());
            stmt.setString(index++, request.getTipoDocumento());
            stmt.setString(index++, request.getNumeroDocumento());

            if (request.getFechaNacimiento() != null) {
                stmt.setDate(index++, Date.valueOf(request.getFechaNacimiento()));
            } else {
                stmt.setDate(index++, null);
            }

            stmt.setString(index++, request.getGenero());
            stmt.setString(index++, request.getTelefono());
            stmt.setString(index++, request.getTelefonoAlternativo());

            // Ubicación (opcional)
            stmt.setObject(index++, request.getDepartamentoId());
            stmt.setObject(index++, request.getProvinciaId());
            stmt.setObject(index++, request.getDistritoId());
            stmt.setString(index++, request.getDireccion());
            stmt.setString(index++, request.getReferenciaDireccion());

            // Tipo de rol
            String tipoRol = request.getTipoCuenta().toUpperCase();
            stmt.setString(index++, tipoRol);
            stmt.setBoolean(index++, esCliente);
            stmt.setBoolean(index++, esProfesional);

            // Timestamps
            Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());
            stmt.setTimestamp(index++, ahora);
            stmt.setTimestamp(index++, ahora);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        throw new SQLException("No se pudo crear el usuario persona en la tabla usuarios");
    }

    /**
     * Crea un cliente en la tabla clientes vinculado con usuarioPersona
     */
    private Long crearCliente(Connection conn, Integer userId, RegistroCompletoRequest request)
            throws SQLException {

        String sql = "INSERT INTO clientes (usuario_id, radio_busqueda, presupuesto_promedio, " +
                     "notificaciones_email, notificaciones_push, notificaciones_promociones, notificaciones_resenas, " +
                     "perfil_visible, compartir_ubicacion, historial_publico, " +
                     "fecha_registro, fecha_actualizacion, activo, cliente_verificado) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());

            stmt.setInt(1, userId); // usuario_id (FK a users)
            stmt.setInt(2, 10); // radio_busqueda por defecto (10 km)
            stmt.setBigDecimal(3, null); // presupuesto_promedio (null por defecto)
            
            // Notificaciones (todas en false por defecto)
            stmt.setBoolean(4, false); // notificaciones_email
            stmt.setBoolean(5, false); // notificaciones_push
            stmt.setBoolean(6, false); // notificaciones_promociones
            stmt.setBoolean(7, false); // notificaciones_resenas
            
            // Privacidad
            stmt.setBoolean(8, true);  // perfil_visible
            stmt.setBoolean(9, false); // compartir_ubicacion
            stmt.setBoolean(10, false); // historial_publico
            
            // Auditoría
            stmt.setTimestamp(11, ahora); // fecha_registro
            stmt.setTimestamp(12, ahora); // fecha_actualizacion
            stmt.setBoolean(13, true);    // activo
            stmt.setBoolean(14, false);   // cliente_verificado

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }

        throw new SQLException("No se pudo crear el cliente en la tabla clientes");
    }

    /**
     * Crea un profesional en la tabla profesionales vinculado con user
     */
    private Integer crearProfesional(Connection conn, Integer userId, RegistroCompletoRequest request)
            throws SQLException {

        String sql = "INSERT INTO profesionales (usuario_id, descripcion, " +
                     "radio_servicio, disponibilidad, verificado, disponible, " +
                     "fecha_registro, ultima_actualizacion, activo, " +
                     "verificacion_identidad, certificado_antecedentes, total_resenas, " +
                     "calificacion_promedio, anios_experiencia) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            Timestamp ahora = Timestamp.valueOf(LocalDateTime.now());

            stmt.setInt(1, userId);                                          // usuario_id
            stmt.setString(2, "Perfil profesional en construcción");        // descripcion
            stmt.setInt(3, 10);                                              // radio_servicio (10 km por defecto)
            stmt.setString(4, "Por definir");                                // disponibilidad
            stmt.setBoolean(5, false);                                       // verificado
            stmt.setBoolean(6, true);                                        // disponible
            stmt.setTimestamp(7, ahora);                                     // fecha_registro
            stmt.setTimestamp(8, ahora);                                     // ultima_actualizacion
            stmt.setBoolean(9, true);                                        // activo
            stmt.setBoolean(10, false);                                      // verificacion_identidad
            stmt.setBoolean(11, false);                                      // certificado_antecedentes
            stmt.setInt(12, 0);                                              // total_resenas
            stmt.setBigDecimal(13, new java.math.BigDecimal("0.0"));        // calificacion_promedio
            stmt.setInt(14, 0);                                              // anios_experiencia

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        }

        throw new SQLException("No se pudo crear el profesional en la tabla profesionales");
    }
}