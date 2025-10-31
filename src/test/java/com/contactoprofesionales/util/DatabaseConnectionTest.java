package com.contactoprofesionales.util;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

public class DatabaseConnectionTest {
    
    @Test
    public void testDatabaseConnection() {
        String url = "jdbc:postgresql://localhost:5432/plataforma_servicios";
        String user = "postgres";
        String password = "Admin123";
        
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            assertNotNull(conn);
            System.out.println("✓ Conexión a base de datos exitosa!");
        } catch (Exception e) {
            fail("Error al conectar a la base de datos: " + e.getMessage());
        }
    }
}