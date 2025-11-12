package com.contactoprofesionales.dao.usuario;

import com.contactoprofesionales.model.Usuario;
import com.contactoprofesionales.exception.DatabaseException;

/**
 * Interfaz DAO para Usuario.
 * Define operaciones de persistencia.
 * 
 * Aplicación de DIP: Los servicios dependen de esta interfaz,
 * no de la implementación concreta.
 */
public interface UsuarioDAO {
    
    /**
     * Busca un usuario por email.
     */
    Usuario buscarPorEmail(String email) throws DatabaseException;
    
    /**
     * Busca un usuario por ID.
     */
    Usuario buscarPorId(Integer id) throws DatabaseException;
    
    /**
     * Registra un nuevo usuario.
     */
    Usuario registrar(Usuario usuario) throws DatabaseException;
    
    /**
     * Actualiza un usuario existente.
     */
    boolean actualizar(Usuario usuario) throws DatabaseException;
    
    /**
     * Elimina un usuario.
     */
    boolean eliminar(Integer id) throws DatabaseException;
    
    /**
     * Verifica si un email ya está registrado.
     */
    boolean existeEmail(String email) throws DatabaseException;
}
