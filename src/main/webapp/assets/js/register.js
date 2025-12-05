// SERVICIO DE REGISTRO
// ============================================
class RegisterService {
	constructor() {
		this.baseURL = '/ContactoProfesionalesWeb/api';
	}

	async register(userData) {
		try {
			const response = await fetch(`${this.baseURL}/register`, {
				method: 'POST',
				headers: {
					'Content-Type': 'application/json'
				},
				body: JSON.stringify(userData)
			});

			const data = await response.json();

			if (response.ok && data.success) {
				return { success: true, data: data.data, message: data.message };
			} else {
				return { success: false, message: data.error || 'Error en el registro' };
			}
		} catch (error) {
			console.error('Error en registro:', error);
			return { success: false, message: 'Error de conexión con el servidor' };
		}
	}
}

// Instancia global del servicio
const registerService = new RegisterService();

// ============================================
// VARIABLES GLOBALES
// ============================================
let currentUserType = 'cliente';

// Elementos DOM
const registerForm = document.getElementById('registerForm');
const fullNameInput = document.getElementById('fullName');
const emailInput = document.getElementById('email');
const phoneInput = document.getElementById('phone');
const passwordInput = document.getElementById('password');
const confirmPasswordInput = document.getElementById('confirmPassword');
const registerButton = document.getElementById('registerButton');
const alertContainer = document.getElementById('alertContainer');

// ============================================
// INICIALIZACIÓN
// ============================================
document.addEventListener('DOMContentLoaded', function() {
	setupEventListeners();
});

function setupEventListeners() {
	// Selector de tipo de usuario
	document.querySelectorAll('.user-type-btn').forEach(btn => {
		btn.addEventListener('click', function() {
			document.querySelectorAll('.user-type-btn').forEach(b => b.classList.remove('active'));
			this.classList.add('active');
			currentUserType = this.getAttribute('data-type');
		});
	});

	// Formulario de registro
	registerForm.addEventListener('submit', handleRegister);

	// Validación en tiempo real
	fullNameInput.addEventListener('blur', validateFullName);
	emailInput.addEventListener('blur', () => validateEmail());
	phoneInput.addEventListener('input', validatePhone);
	passwordInput.addEventListener('input', checkPasswordStrength);
	confirmPasswordInput.addEventListener('input', validatePasswordMatch);

	// Auto-focus
	fullNameInput.focus();
}

// ============================================
// MANEJO DE REGISTRO
// ============================================
async function handleRegister(e) {
	e.preventDefault();

	clearAlerts();
	clearAllErrors();

	const fullName = fullNameInput.value.trim();
	const email = emailInput.value.trim();
	const phone = phoneInput.value.trim();
	const password = passwordInput.value;
	const confirmPassword = confirmPasswordInput.value;
	const termsAccepted = document.getElementById('terms').checked;

	// Validaciones
	let isValid = true;

	if (!validateFullName()) isValid = false;
	if (!validateEmail()) isValid = false;
	if (!validatePhone()) isValid = false;

	if (password.length < 6) {
		showFieldError('password', 'La contraseña debe tener al menos 6 caracteres');
		isValid = false;
	}

	if (password !== confirmPassword) {
		showFieldError('confirmPassword', 'Las contraseñas no coinciden');
		isValid = false;
	}

	if (!termsAccepted) {
		showAlert('Debes aceptar los términos y condiciones', 'error');
		isValid = false;
	}

	if (!isValid) return;

	// Mapear tipo de usuario al formato del backend
	// 'cliente' -> 'CLIENTE', 'proveedor' -> 'PROFESIONAL'
	const tipoCuentaBackend = currentUserType === 'cliente' ? 'CLIENTE' : 'PROFESIONAL';

	// Preparar datos para enviar (nuevo flujo con tipoCuenta)
	const userData = {
		nombreCompleto: fullName,
		email: email,
		telefono: phone,
		password: password,
		tipoCuenta: tipoCuentaBackend
	};

	setLoadingState(true);

	try {
		const result = await registerService.register(userData);

		if (result.success) {
			handleRegisterSuccess(result.data);
		} else {
			handleRegisterError(result.message);
		}
	} catch (error) {
		console.error('Error en registro:', error);
		showAlert('❌ Error inesperado. Intente nuevamente', 'error');
	} finally {
		setLoadingState(false);
	}
}

function handleRegisterSuccess(data) {
	showAlert('✅ ¡Cuenta creada exitosamente! Redirigiendo al inicio de sesión...', 'success');

	// Guardar email para pre-llenado en login
	localStorage.setItem('registeredEmail', emailInput.value);

	// Limpiar formulario
	registerForm.reset();

	// Redirigir a login después de 2 segundos
	setTimeout(() => {
		window.location.href = 'login.html';
	}, 2000);
}

function handleRegisterError(message) {
	showAlert(`❌ ${message}`, 'error');
}

// ============================================
// VALIDACIONES
// ============================================
function validateFullName() {
	const name = fullNameInput.value.trim();

	if (name.length < 2) {
		showFieldError('fullName', 'El nombre debe tener al menos 2 caracteres');
		return false;
	}

	if (!/^[a-zA-ZÀ-ÿ\s]+$/.test(name)) {
		showFieldError('fullName', 'El nombre solo puede contener letras y espacios');
		return false;
	}

	clearFieldError('fullName');
	fullNameInput.classList.add('success');
	return true;
}

function validateEmail() {
	const email = emailInput.value.trim();
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

	if (!emailRegex.test(email)) {
		showFieldError('email', 'Por favor ingresa un email válido');
		return false;
	}

	clearFieldError('email');
	emailInput.classList.add('success');
	return true;
}

function validatePhone() {
	const phone = phoneInput.value.trim();
	const phoneRegex = /^[0-9]{9}$/;

	// Solo números
	phoneInput.value = phone.replace(/[^0-9]/g, '');

	if (phone.length > 0 && !phoneRegex.test(phone)) {
		showFieldError('phone', 'El teléfono debe tener 9 dígitos');
		return false;
	}

	if (phone.length === 9) {
		clearFieldError('phone');
		phoneInput.classList.add('success');
		return true;
	}

	return phone.length === 0;
}

function checkPasswordStrength() {
	const password = passwordInput.value;
	const strengthBar = document.getElementById('strengthFill');
	const strengthText = document.getElementById('strengthText');
	const strengthContainer = document.getElementById('passwordStrength');

	let strength = 0;
	if (password.length >= 6) strength++;
	if (/[A-Z]/.test(password)) strength++;
	if (/[0-9]/.test(password)) strength++;
	if (/[^A-Za-z0-9]/.test(password)) strength++;

	if (password.length === 0) {
		strengthContainer.style.display = 'none';
		return;
	} else {
		strengthContainer.style.display = 'block';
	}

	let width = 0;
	let color = '';
	let text = '';

	switch (strength) {
		case 0:
		case 1:
			width = '25%';
			color = 'var(--error-color)';
			text = 'Débil';
			break;
		case 2:
			width = '50%';
			color = 'var(--warning-color)';
			text = 'Regular';
			break;
		case 3:
			width = '75%';
			color = 'var(--info-color)';
			text = 'Fuerte';
			break;
		case 4:
			width = '100%';
			color = 'var(--success-color)';
			text = 'Muy Fuerte';
			break;
	}

	strengthBar.style.width = width;
	strengthBar.style.background = color;
	strengthText.textContent = text;
}

function validatePasswordMatch() {
	const password = passwordInput.value;
	const confirmPassword = confirmPasswordInput.value;

	if (confirmPassword.length === 0) {
		clearFieldError('confirmPassword');
		return true;
	}

	if (password !== confirmPassword) {
		showFieldError('confirmPassword', 'Las contraseñas no coinciden');
		return false;
	}

	clearFieldError('confirmPassword');
	confirmPasswordInput.classList.add('success');
	return true;
}

// ============================================
// FUNCIONES DE ERROR Y ALERTAS
// ============================================
function showFieldError(fieldId, message) {
	const input = document.getElementById(fieldId);
	const errorElement = document.getElementById(`${fieldId}Error`);
	input.classList.add('error');
	input.classList.remove('success');
	errorElement.textContent = message;
	errorElement.style.display = 'block';
}

function clearFieldError(fieldId) {
	const input = document.getElementById(fieldId);
	const errorElement = document.getElementById(`${fieldId}Error`);
	input.classList.remove('error');
	errorElement.textContent = '';
	errorElement.style.display = 'none';
}

function clearAllErrors() {
	['fullName', 'email', 'phone', 'password', 'confirmPassword'].forEach(clearFieldError);
}

function showAlert(message, type = 'info') {
	const alert = document.createElement('div');
	alert.classList.add('alert', `alert-${type}`);
	alert.innerHTML = `
${message}
<button class="alert-close" onclick="this.parentElement.remove()">×</button>
`;
	alertContainer.innerHTML = '';
	alertContainer.appendChild(alert);
	alert.style.display = 'block';
}

function clearAlerts() {
	alertContainer.innerHTML = '';
}

// ============================================
// UTILITARIOS DE INTERFAZ
// ============================================
function setLoadingState(isLoading) {
	const spinner = registerButton.querySelector('.spinner');
	const text = registerButton.querySelector('.button-text');
	if (isLoading) {
		registerButton.disabled = true;
		registerButton.classList.add('loading');
		spinner.style.display = 'inline-block';
		text.textContent = 'Creando...';
	} else {
		registerButton.disabled = false;
		registerButton.classList.remove('loading');
		spinner.style.display = 'none';
		text.textContent = 'Crear Cuenta';
	}
}

function togglePassword(fieldId) {
	const field = document.getElementById(fieldId);
	field.type = field.type === 'password' ? 'text' : 'password';
}

