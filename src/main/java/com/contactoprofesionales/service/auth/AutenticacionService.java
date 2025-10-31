package com.contactoprofesionales.service.auth;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.exception.AuthenticationException;
import com.contactoprofesionales.exception.DatabaseException;

/**
 * Servicio de autenticación.
 * 
 * Aplicación de DIP: Los controladores dependen de esta interfaz.
 * Aplicación de ISP: Interfaz específica solo para autenticación.
 */
public interface AutenticacionService {
    
    /**
     * Autentica un usuario con email y password.
     * 
     * @param email Email del usuario
     * @param password Contraseña en texto plano
     * @return Usuario autenticado
     * @throws AuthenticationException Si las credenciales son inválidas
     * @throws DatabaseException Si hay error en la base de datos
     */
    Usuario autenticar(String email, String password) 
            throws AuthenticationException, DatabaseException;
    
    /**
     * Registra un nuevo usuario.
     * 
     * @param usuario Usuario a registrar (con password en texto plano)
     * @return Usuario registrado (con password hasheado)
     * @throws AuthenticationException Si hay error de validación
     * @throws DatabaseException Si hay error en la base de datos
     */
    Usuario registrar(Usuario usuario) 
            throws AuthenticationException, DatabaseException;
    
    /**
     * Valida las credenciales básicas sin autenticar.
     */
    void validarCredenciales(String email, String password) 
            throws AuthenticationException;
}