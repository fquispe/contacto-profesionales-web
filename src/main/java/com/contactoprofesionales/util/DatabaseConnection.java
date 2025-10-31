package com.contactoprofesionales.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Clase utilitaria para gestionar conexiones a la base de datos.
 * Utiliza HikariCP como pool de conexiones para optimizar el rendimiento.
 * 
 * Aplicación de patrón Singleton para el DataSource.
 */
public class DatabaseConnection {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    private static HikariDataSource dataSource;
    
    static {
        try {
            initializeDataSource();
        } catch (Exception e) {
            logger.error("Error crítico al inicializar el pool de conexiones", e);
            throw new ExceptionInInitializerError(e);
        }
    }
    
    /**
     * Inicializa el pool de conexiones HikariCP con configuración desde database.properties
     */
    private static void initializeDataSource() {
        Properties props = loadProperties();
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName(props.getProperty("db.driver"));
        
        // Configuración del pool
        config.setMaximumPoolSize(Integer.parseInt(
            props.getProperty("hikari.maximumPoolSize", "10")));
        config.setMinimumIdle(Integer.parseInt(
            props.getProperty("hikari.minimumIdle", "5")));
        config.setConnectionTimeout(Long.parseLong(
            props.getProperty("hikari.connectionTimeout", "30000")));
        
        // Configuraciones adicionales recomendadas
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("ContactoProfesionales-Pool");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        
        dataSource = new HikariDataSource(config);
        logger.info("✓ Pool de conexiones HikariCP inicializado correctamente");
    }
    
    /**
     * Carga las propiedades de configuración desde database.properties
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        
        try (InputStream input = DatabaseConnection.class
                .getClassLoader()
                .getResourceAsStream("database.properties")) {
            
            if (input == null) {
                logger.error("No se pudo encontrar database.properties");
                throw new RuntimeException("Archivo database.properties no encontrado");
            }
            
            props.load(input);
            logger.debug("Propiedades de base de datos cargadas correctamente");
            
        } catch (IOException e) {
            logger.error("Error al cargar database.properties", e);
            throw new RuntimeException("Error al cargar configuración de base de datos", e);
        }
        
        return props;
    }
    
    /**
     * Obtiene una conexión del pool.
     * 
     * @return Connection objeto de conexión a la base de datos
     * @throws SQLException si ocurre un error al obtener la conexión
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("El pool de conexiones no está inicializado");
        }
        return dataSource.getConnection();
    }
    
    /**
     * Cierra el pool de conexiones.
     * Debe llamarse al finalizar la aplicación.
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Pool de conexiones cerrado correctamente");
        }
    }
    
    /**
     * Obtiene información del estado del pool de conexiones.
     */
    public static String getPoolStats() {
        if (dataSource != null) {
            return String.format("Pool: %d activas, %d idle, %d total, %d esperando",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection());
        }
        return "Pool no inicializado";
    }
    
    // Constructor privado para prevenir instanciación
    private DatabaseConnection() {
        throw new UnsupportedOperationException("Clase utilitaria no instanciable");
    }
}
