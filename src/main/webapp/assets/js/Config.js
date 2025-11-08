/**
 * Archivo de configuración para la aplicación
 * Incluir este archivo ANTES de cliente-api.js si necesitas configuración personalizada
 */

// ========================================
// CONFIGURACIÓN DEL CONTEXT PATH
// ========================================

/**
 * Descomenta y configura esta variable si tu aplicación está desplegada
 * en un context path diferente al detectado automáticamente
 * 
 * Ejemplos:
 * - Aplicación en raíz: window.APP_CONTEXT_PATH = '';
 * - Con context path: window.APP_CONTEXT_PATH = '/mi-aplicacion';
 * - En subdirectorio: window.APP_CONTEXT_PATH = '/servicios/clientes';
 */

// window.APP_CONTEXT_PATH = '';  // Descomenta y ajusta si es necesario

// ========================================
// CONFIGURACIÓN DE LA API
// ========================================

/**
 * URL base de la API (opcional)
 * Si no se especifica, se construye automáticamente usando APP_CONTEXT_PATH
 */
// window.API_BASE_URL = 'http://localhost:8080/contacto-profesionales/api/clientes';

// ========================================
// CONFIGURACIÓN DE DESARROLLO
// ========================================

/**
 * Modo debug: Muestra logs en consola
 */
window.DEBUG_MODE = false;

/**
 * Timeout para requests (en milisegundos)
 */
window.API_TIMEOUT = 30000; // 30 segundos

// ========================================
// CONFIGURACIÓN DE INTERFAZ
// ========================================

/**
 * Tiempo de auto-ocultación de alertas de éxito (ms)
 */
window.ALERT_SUCCESS_TIMEOUT = 5000; // 5 segundos

/**
 * Máximo número de direcciones por cliente
 */
window.MAX_DIRECCIONES = 3;

/**
 * Tamaño máximo de imagen de perfil (bytes)
 */
window.MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

// ========================================
// FUNCIONES DE UTILIDAD
// ========================================

/**
 * Log de debug (solo si DEBUG_MODE está activado)
 */
window.debugLog = function(message, data) {
    if (window.DEBUG_MODE) {
        console.log('[DEBUG]', message, data || '');
    }
};

/**
 * Obtiene la configuración completa
 */
window.getAppConfig = function() {
    return {
        contextPath: window.APP_CONTEXT_PATH,
        apiBaseUrl: window.API_BASE_URL,
        debugMode: window.DEBUG_MODE,
        apiTimeout: window.API_TIMEOUT,
        alertTimeout: window.ALERT_SUCCESS_TIMEOUT,
        maxDirecciones: window.MAX_DIRECCIONES,
        maxImageSize: window.MAX_IMAGE_SIZE
    };
};

// ========================================
// INICIALIZACIÓN
// ========================================

// Log de configuración en modo debug
if (window.DEBUG_MODE) {
    console.log('=== Configuración de la Aplicación ===');
    console.table(window.getAppConfig());
}