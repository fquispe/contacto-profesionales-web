// ============================================
// SERVICIO DE AUTENTICACIÓN
// ============================================
class AuthService {
    constructor() {
        this.baseURL = '/ContactoProfesionalesWeb/api';
    }

    // ✅ ACTUALIZADO: Manejo diferenciado de errores (actualizado: 2025-11-15)
    async login(email, password) {
        try {
            const response = await fetch(`${this.baseURL}/login`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                // Guardar token y datos del usuario
                localStorage.setItem('authToken', data.data.token);
                localStorage.setItem('userData', JSON.stringify(data.data.usuario));
                localStorage.setItem('tokenExpiration', Date.now() + data.data.expiresIn);
                localStorage.setItem('isAuthenticated', 'true');

                return { success: true, data: data.data };
            } else {
                // ✅ NUEVO: Determinar tipo de error basado en código HTTP y flags (añadido: 2025-11-15)
                const errorType = this.determineErrorType(response.status, data);
                return {
                    success: false,
                    message: data.error || 'Error desconocido',
                    errorType: errorType, // 'userNotFound' o 'passwordIncorrect'
                    errorData: data.data || {}
                };
            }
        } catch (error) {
            console.error('Error en login:', error);
            return { success: false, message: 'Error de conexión con el servidor' };
        }
    }

    // ✅ NUEVO: Determina el tipo de error para manejar intentos (añadido: 2025-11-15)
    determineErrorType(statusCode, data) {
        // Si es 404, es usuario no encontrado - NO contar intentos
        if (statusCode === 404 || (data.data && data.data.userNotFound)) {
            return 'userNotFound';
        }
        // Si es 401 y tiene flag de contraseña incorrecta - SÍ contar intentos
        if (statusCode === 401 || (data.data && data.data.passwordIncorrect)) {
            return 'passwordIncorrect';
        }
        // Default
        return 'unknown';
    }

    logout() {
        localStorage.removeItem('authToken');
        localStorage.removeItem('userData');
        localStorage.removeItem('tokenExpiration');
        localStorage.removeItem('isAuthenticated');
        window.location.href = '/ContactoProfesionalesWeb/login.html';
    }

    isAuthenticated() {
        const token = localStorage.getItem('authToken');
        const expiration = localStorage.getItem('tokenExpiration');

        if (!token || !expiration) {
            return false;
        }

        if (Date.now() > parseInt(expiration)) {
            this.logout();
            return false;
        }

        return true;
    }

    getToken() {
        return localStorage.getItem('authToken');
    }

    getUserData() {
        const userData = localStorage.getItem('userData');
        return userData ? JSON.parse(userData) : null;
    }
}

// Instancia global del servicio
const authService = new AuthService();

// ============================================
// VARIABLES GLOBALES
// ============================================
let loginAttempts = parseInt(localStorage.getItem('loginAttempts') || '0');
let blockEndTime = parseInt(localStorage.getItem('blockEndTime') || '0');
let isBlocked = false;
const maxAttempts = 5;
const blockDuration = 15 * 60 * 1000; // 15 minutos

// Elementos DOM
const loginForm = document.getElementById('loginForm');
const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const loginButton = document.getElementById('loginButton');
const attemptCounter = document.getElementById('attemptCounter');
const alertContainer = document.getElementById('alertContainer');
const loadingOverlay = document.getElementById('loadingOverlay');

// ============================================
// INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', function() {
    // Verificar si ya está autenticado
    if (authService.isAuthenticated()) {
        window.location.href = '/ContactoProfesionalesWeb/dashboard.html';
        return;
    }

    checkBlockStatus();
    setupEventListeners();
    updateAttemptCounter();

    // Pre-llenar email si existe
    const savedEmail = localStorage.getItem('savedEmail');
    if (savedEmail) {
        emailInput.value = savedEmail;
        emailInput.classList.add('success');
    }
});

function setupEventListeners() {
    loginForm.addEventListener('submit', handleLogin);
    emailInput.addEventListener('input', validateEmailInput);
    emailInput.addEventListener('blur', validateEmailField);
    passwordInput.addEventListener('input', validatePasswordInput);

    emailInput.addEventListener('change', function() {
        if (this.value && validateEmail(this.value)) {
            localStorage.setItem('savedEmail', this.value);
        }
    });

    if (!emailInput.value) {
        emailInput.focus();
    } else if (!passwordInput.value) {
        passwordInput.focus();
    }
}

// ============================================
// MANEJO DE LOGIN
// ============================================
function checkBlockStatus() {
    const now = Date.now();

    if (blockEndTime > now) {
        isBlocked = true;
        const remainingMinutes = Math.ceil((blockEndTime - now) / 1000 / 60);
        showAlert(`⏱️ Cuenta bloqueada. Inténtalo de nuevo en ${remainingMinutes} minutos.`, 'warning');
        disableForm();

        setTimeout(() => {
            isBlocked = false;
            loginAttempts = 0;
            localStorage.removeItem('blockEndTime');
            localStorage.removeItem('loginAttempts');
            enableForm();
            clearAlerts();
            showAlert('✅ Cuenta desbloqueada.', 'success');
        }, blockEndTime - now);
    }
}

// ✅ ACTUALIZADO: Diferencia entre usuario no existe y contraseña incorrecta (actualizado: 2025-11-15)
async function handleLogin(e) {
    e.preventDefault();

    if (isBlocked) {
        showAlert('🚫 Cuenta bloqueada. Espera antes de intentar nuevamente.', 'error');
        return;
    }

    clearAlerts();
    clearErrors();

    const email = emailInput.value.trim();
    const password = passwordInput.value;
    const rememberMe = document.getElementById('rememberMe').checked;

    if (!validateForm(email, password)) {
        return;
    }

    setLoadingState(true);

    try {
        const result = await authService.login(email, password);

        if (result.success) {
            handleLoginSuccess(result.data, rememberMe);
        } else {
            // ✅ NUEVO: Pasar tipo de error al manejador (añadido: 2025-11-15)
            handleLoginError(result.message, result.errorType, result.errorData);
        }
    } catch (error) {
        console.error('Error en login:', error);
        showAlert('❌ Error de conexión. Verifica tu internet e intenta nuevamente.', 'error');
    } finally {
        setLoadingState(false);
    }
}

function handleLoginSuccess(data, rememberMe) {
    loginAttempts = 0;
    localStorage.removeItem('loginAttempts');
    localStorage.removeItem('blockEndTime');

    if (rememberMe) {
        localStorage.setItem('rememberMe', 'true');
    }

    showAlert(`✅ ¡Bienvenido ${data.usuario.nombre}!`, 'success');

    setTimeout(() => {
        window.location.href = '/ContactoProfesionalesWeb/dashboard.html';
    }, 1500);
}

// ✅ ACTUALIZADO: NO contar intentos cuando el usuario no existe (actualizado: 2025-11-15)
function handleLoginError(message, errorType, errorData) {
    // ✅ IMPORTANTE: Si el usuario no existe, NO contar intentos y sugerir registro
    if (errorType === 'userNotFound') {
        // No incrementar intentos
        showAlert(`❌ ${message}`, 'warning');

        // Agregar botón para ir a registro
        setTimeout(() => {
            const alertDiv = document.querySelector('.alert-warning');
            if (alertDiv) {
                const registerLink = document.createElement('a');
                registerLink.href = 'register.html';
                registerLink.textContent = ' ¿Desea crear una cuenta?';
                registerLink.style.cssText = 'color: var(--primary-color); font-weight: 600; text-decoration: underline; margin-left: 5px;';
                alertDiv.appendChild(registerLink);
            }
        }, 100);

        // No actualizar contador
        return;
    }

    // ✅ Solo contar intentos cuando es contraseña incorrecta (passwordIncorrect)
    loginAttempts++;
    localStorage.setItem('loginAttempts', loginAttempts);

    if (loginAttempts >= maxAttempts) {
        blockEndTime = Date.now() + blockDuration;
        localStorage.setItem('blockEndTime', blockEndTime);
        isBlocked = true;

        disableForm();
        showAlert('🚫 Demasiados intentos fallidos. Cuenta bloqueada por 15 minutos.', 'error');

        setTimeout(() => {
            isBlocked = false;
            loginAttempts = 0;
            localStorage.removeItem('loginAttempts');
            localStorage.removeItem('blockEndTime');
            enableForm();
            showAlert('✅ Cuenta desbloqueada.', 'success');
        }, blockDuration);
    } else {
        const remainingAttempts = maxAttempts - loginAttempts;
        showAlert(`❌ ${message}. Te quedan ${remainingAttempts} intentos.`, 'error');
    }

    updateAttemptCounter();
}

function updateAttemptCounter() {
    if (loginAttempts > 0 && loginAttempts < maxAttempts) {
        attemptCounter.style.display = 'block';
        attemptCounter.textContent = `Intentos restantes: ${maxAttempts - loginAttempts}`;
        attemptCounter.className = 'attempt-counter';

        if (loginAttempts >= 3) {
            attemptCounter.classList.add('warning');
        }
        if (loginAttempts >= 4) {
            attemptCounter.classList.remove('warning');
            attemptCounter.classList.add('danger');
        }
    } else {
        attemptCounter.style.display = 'none';
    }
}

// ============================================
// VALIDACIONES
// ============================================
function validateForm(email, password) {
    let isValid = true;

    if (!email) {
        showFieldError('email', 'El email es requerido');
        isValid = false;
    } else if (!validateEmail(email)) {
        showFieldError('email', 'Formato de email inválido');
        isValid = false;
    }

    if (!password) {
        showFieldError('password', 'La contraseña es requerida');
        isValid = false;
    } else if (password.length < 3) {
        showFieldError('password', 'La contraseña es muy corta');
        isValid = false;
    }

    return isValid;
}

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function validateEmailInput() {
    const email = emailInput.value.trim();
    if (email && validateEmail(email)) {
        emailInput.classList.remove('error');
        emailInput.classList.add('success');
        clearFieldError('email');
    } else if (email) {
        emailInput.classList.remove('success');
        emailInput.classList.add('error');
    }
}

function validateEmailField() {
    const email = emailInput.value.trim();
    if (email && !validateEmail(email)) {
        showFieldError('email', 'Formato de email inválido');
    }
}

function validatePasswordInput() {
    const password = passwordInput.value;
    if (password.length > 0) {
        passwordInput.classList.remove('error');
        clearFieldError('password');
    }
}

// ============================================
// UI HELPERS
// ============================================
function togglePassword() {
    const type = passwordInput.type === 'password' ? 'text' : 'password';
    passwordInput.type = type;

    const toggle = document.getElementById('passwordToggle');
    toggle.textContent = type === 'password' ? '👁️' : '🙈';
}

function showFieldError(fieldName, message) {
    const field = document.getElementById(fieldName);
    const errorDiv = document.getElementById(fieldName + 'Error');

    field.classList.add('error');
    if (errorDiv) {
        errorDiv.textContent = message;
        errorDiv.style.display = 'block';
    }
}

function clearFieldError(fieldName) {
    const field = document.getElementById(fieldName);
    const errorDiv = document.getElementById(fieldName + 'Error');

    field.classList.remove('error');
    if (errorDiv) {
        errorDiv.style.display = 'none';
    }
}

function clearErrors() {
    document.querySelectorAll('.form-input.error').forEach(field => {
        field.classList.remove('error');
    });
    document.querySelectorAll('.error-message').forEach(error => {
        error.style.display = 'none';
    });
}

function showAlert(message, type) {
    const container = alertContainer;
    const alert = document.createElement('div');
    alert.className = `alert alert-${type}`;
    alert.innerHTML = `
        ${message}
        <button class="alert-close" onclick="this.parentElement.remove()">×</button>
    `;

    container.innerHTML = '';
    container.appendChild(alert);
    alert.style.display = 'block';

    if (type === 'success' || type === 'info') {
        setTimeout(() => {
            if (alert.parentElement) {
                alert.style.display = 'none';
            }
        }, 5000);
    }
}

function clearAlerts() {
    alertContainer.innerHTML = '';
}

function setLoadingState(loading) {
    if (loading) {
        loginButton.classList.add('loading');
        loginButton.disabled = true;
        loginButton.querySelector('.button-text').textContent = 'Iniciando...';
    } else {
        loginButton.classList.remove('loading');
        loginButton.disabled = false;
        loginButton.querySelector('.button-text').textContent = 'Iniciar Sesión';
    }
}

function disableForm() {
    emailInput.disabled = true;
    passwordInput.disabled = true;
    loginButton.disabled = true;
}

function enableForm() {
    emailInput.disabled = false;
    passwordInput.disabled = false;
    loginButton.disabled = false;
}

// ============================================
// NAVIGATION HANDLERS
// ============================================
function handleForgotPassword(e) {
    e.preventDefault();
    window.location.href = '/ContactoProfesionalesWeb/forgot-password.html';
}

// ============================================
// SESSION MANAGEMENT
// ============================================
let inactivityTimer;
const inactivityTimeout = 24 * 60 * 60 * 1000; // 24 horas

function resetInactivityTimer() {
    clearTimeout(inactivityTimer);
    inactivityTimer = setTimeout(() => {
        if (localStorage.getItem('isAuthenticated') === 'true') {
            authService.logout();
            showAlert('⏰ Sesión expirada por inactividad.', 'info');
        }
    }, inactivityTimeout);
}

// Detectar actividad del usuario
['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'].forEach(event => {
    document.addEventListener(event, resetInactivityTimer, true);
});

// Verificar conexión a internet
window.addEventListener('online', () => {
    showAlert('✅ Conexión restaurada.', 'success');
});

window.addEventListener('offline', () => {
    showAlert('❌ Sin conexión a internet.', 'error');
});

// Prevenir múltiples envíos
let isSubmitting = false;
loginForm.addEventListener('submit', function(e) {
    if (isSubmitting) {
        e.preventDefault();
        return false;
    }
    isSubmitting = true;
    setTimeout(() => { isSubmitting = false; }, 2000);
});

// Inicializar timer de inactividad si está autenticado
if (authService.isAuthenticated()) {
    resetInactivityTimer();
}

// Log de depuración
console.log('🔧 Sistema de autenticación inicializado');
console.log('📍 API Base URL:', authService.baseURL);
