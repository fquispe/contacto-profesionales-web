package com.contactoprofesionales.exception;

/**
 * Excepción personalizada para operaciones relacionadas con clientes
 */
public class ClienteException extends Exception {
        
	private static final long serialVersionUID = 1L;
	
	private String codigo;
    
    public ClienteException(String mensaje) {
        super(mensaje);
    }
    
    public ClienteException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
    
    public ClienteException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }
    
    public ClienteException(String codigo, String mensaje, Throwable causa) {
        super(mensaje, causa);
        this.codigo = codigo;
    }
    
    public String getCodigo() {
        return codigo;
    }
    
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
}
