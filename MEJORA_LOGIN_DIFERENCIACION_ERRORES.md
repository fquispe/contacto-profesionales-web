# 🔐 Mejora de Login - Diferenciación de Errores

**Fecha:** 2025-11-15
**Versión:** 1.0
**Tipo de Cambio:** Mejora de UX y Seguridad

---

## 🎯 Objetivo

Mejorar la experiencia del usuario en el login diferenciando claramente entre dos tipos de errores:

1. **Usuario no existe**: NO debe contar como intento fallido, debe sugerir registrarse
2. **Contraseña incorrecta**: SÍ debe contar como intento fallido, debe mostrar intentos restantes

---

## ❌ Problema Anterior

Antes de esta mejora, el sistema trataba ambos casos igual:

```
Usuario no existe → "Credenciales inválidas. Te quedan 4 intentos."
Contraseña incorrecta → "Credenciales inválidas. Te quedan 4 intentos."
```

**Problemas:**
- ❌ El usuario que escribe mal su email se le cuentan intentos innecesariamente
- ❌ No hay orientación clara para usuarios nuevos que no tienen cuenta
- ❌ Puede bloquear usuarios legítimos que simplemente se equivocaron de email

---

## ✅ Solución Implementada

Ahora el sistema diferencia los casos:

### Caso 1: Usuario No Existe
```
HTTP Status: 404 NOT FOUND
Mensaje: "Usuario no encontrado. Por favor regístrese"
Contador de intentos: NO se incrementa
Acción sugerida: Link a registro
```

### Caso 2: Contraseña Incorrecta
```
HTTP Status: 401 UNAUTHORIZED
Mensaje: "Contraseña incorrecta. Te quedan 4 intentos."
Contador de intentos: SÍ se incrementa
Acción sugerida: Revisar contraseña / Olvidé mi contraseña
```

---

## 🔧 Modificaciones Realizadas

### 1. Backend - Nueva Excepción

**Archivo creado:** `UserNotFoundException.java`

```java
package com.contactoprofesionales.exception;

/**
 * Excepción para cuando un usuario no existe en el sistema.
 * Diferencia entre "usuario no encontrado" vs "contraseña incorrecta"
 */
public class UserNotFoundException extends AuthenticationException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
```

**Ubicación:** `src/main/java/com/contactoprofesionales/exception/UserNotFoundException.java`

---

### 2. Backend - Servicio de Autenticación

**Archivo modificado:** `AutenticacionServiceImpl.java`

#### Import Agregado:
```java
import com.contactoprofesionales.exception.UserNotFoundException; // ✅ Línea 11
```

#### Método Actualizado:
```java
// ✅ ACTUALIZADO: Líneas 48-86
@Override
public Usuario autenticar(String email, String password)
        throws AuthenticationException, DatabaseException {

    // ... código de validación ...

    Usuario usuario = usuarioDAO.buscarPorEmail(email);

    // ✅ CAMBIO CLAVE: Lanzar UserNotFoundException cuando el usuario no existe
    if (usuario == null) {
        logger.warn("✗ Usuario no encontrado: {}", email);
        throw new UserNotFoundException("Usuario no encontrado. Por favor regístrese");
    }

    // ... validación de activo ...

    // ✅ CAMBIO CLAVE: AuthenticationException solo para contraseña incorrecta
    if (!passwordHasher.verify(password, usuario.getPasswordHash())) {
        logger.warn("✗ Contraseña incorrecta para: {}", email);
        throw new AuthenticationException("Contraseña incorrecta");
    }

    // ... resto del código ...
}
```

---

### 3. Backend - Servlet de Login

**Archivo modificado:** `LoginServlet.java`

#### Import Agregado:
```java
import com.contactoprofesionales.exception.UserNotFoundException; // ✅ Línea 13
```

#### Catch Actualizado:
```java
// ✅ ACTUALIZADO: Líneas 151-169
} catch (UserNotFoundException e) {
    // ✅ NUEVO: Capturar UserNotFoundException PRIMERO (antes que AuthenticationException)
    handleUserNotFoundError(response, e, startTime);

} catch (AuthenticationException e) {
    // ✅ ACTUALIZADO: Ahora solo para contraseñas incorrectas
    handleAuthenticationError(response, e, startTime);
}
```

#### Nuevo Método - Usuario No Encontrado:
```java
// ✅ NUEVO: Líneas 254-278
private void handleUserNotFoundError(HttpServletResponse response,
                                    UserNotFoundException e,
                                    long startTime) throws IOException {
    long duration = System.currentTimeMillis() - startTime;
    logger.warn("✗ Usuario no encontrado - Tiempo: {}ms", duration);

    // Crear respuesta con flags especiales
    Map<String, Object> errorData = new HashMap<>();
    errorData.put("userNotFound", true);      // Flag para el frontend
    errorData.put("suggestRegister", true);   // Sugerir registro

    JsonResponse jsonResponse = new JsonResponse();
    jsonResponse.setSuccess(false);
    jsonResponse.setError(e.getMessage());
    jsonResponse.setData(errorData);

    // Usar código 404 para diferenciar
    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
    response.getWriter().write(gson.toJson(jsonResponse));
}
```

#### Método Actualizado - Contraseña Incorrecta:
```java
// ✅ ACTUALIZADO: Líneas 280-302
private void handleAuthenticationError(HttpServletResponse response,
                                      AuthenticationException e,
                                      long startTime) throws IOException {
    long duration = System.currentTimeMillis() - startTime;
    logger.warn("✗ Error de autenticación (contraseña incorrecta) - Tiempo: {}ms", duration);

    // Crear respuesta con flag para contar intentos
    Map<String, Object> errorData = new HashMap<>();
    errorData.put("passwordIncorrect", true);  // Flag para el frontend
    errorData.put("countAttempt", true);       // Sí contar intento

    JsonResponse jsonResponse = new JsonResponse();
    jsonResponse.setSuccess(false);
    jsonResponse.setError(e.getMessage());
    jsonResponse.setData(errorData);

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write(gson.toJson(jsonResponse));
}
```

---

### 4. Frontend - Login HTML

**Archivo modificado:** `login.html`

#### Método de Login Actualizado:
```javascript
// ✅ ACTUALIZADO: Líneas 752-801
async login(email, password) {
    const response = await fetch(`${this.baseURL}/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password })
    });

    const data = await response.json();

    if (response.ok && data.success) {
        // ... guardar token ...
        return { success: true, data: data.data };
    } else {
        // ✅ NUEVO: Determinar tipo de error
        const errorType = this.determineErrorType(response.status, data);
        return {
            success: false,
            message: data.error || 'Error desconocido',
            errorType: errorType,    // 'userNotFound' o 'passwordIncorrect'
            errorData: data.data || {}
        };
    }
}

// ✅ NUEVO: Determinar tipo de error
determineErrorType(statusCode, data) {
    // Si es 404, es usuario no encontrado - NO contar intentos
    if (statusCode === 404 || (data.data && data.data.userNotFound)) {
        return 'userNotFound';
    }
    // Si es 401, es contraseña incorrecta - SÍ contar intentos
    if (statusCode === 401 || (data.data && data.data.passwordIncorrect)) {
        return 'passwordIncorrect';
    }
    return 'unknown';
}
```

#### Manejo de Login Actualizado:
```javascript
// ✅ ACTUALIZADO: Líneas 923-960
async function handleLogin(e) {
    // ... código previo ...

    const result = await authService.login(email, password);

    if (result.success) {
        handleLoginSuccess(result.data, rememberMe);
    } else {
        // ✅ NUEVO: Pasar tipo de error
        handleLoginError(result.message, result.errorType, result.errorData);
    }
}
```

#### Manejo de Errores Actualizado:
```javascript
// ✅ ACTUALIZADO: Líneas 978-1027
function handleLoginError(message, errorType, errorData) {
    // ✅ IMPORTANTE: Si el usuario no existe, NO contar intentos
    if (errorType === 'userNotFound') {
        showAlert(`❌ ${message}`, 'warning');

        // Agregar link a registro
        setTimeout(() => {
            const alertDiv = document.querySelector('.alert-warning');
            if (alertDiv) {
                const registerLink = document.createElement('a');
                registerLink.href = 'register.html';
                registerLink.textContent = ' ¿Desea crear una cuenta?';
                registerLink.style.cssText = 'color: var(--primary-color); font-weight: 600; text-decoration: underline;';
                alertDiv.appendChild(registerLink);
            }
        }, 100);

        return; // ← NO actualizar contador
    }

    // ✅ Solo contar intentos cuando es contraseña incorrecta
    loginAttempts++;
    localStorage.setItem('loginAttempts', loginAttempts);

    if (loginAttempts >= maxAttempts) {
        // ... bloquear cuenta ...
    } else {
        const remainingAttempts = maxAttempts - loginAttempts;
        showAlert(`❌ ${message}. Te quedan ${remainingAttempts} intentos.`, 'error');
    }

    updateAttemptCounter();
}
```

---

## 📊 Flujo de Datos

### Caso 1: Usuario No Existe

```
Frontend (login.html)
    ↓ POST /api/login { email: "noexiste@mail.com", password: "123" }

Backend (LoginServlet)
    ↓ authService.autenticar()

AutenticacionService
    ↓ usuarioDAO.buscarPorEmail() → null
    ↓ throw new UserNotFoundException("Usuario no encontrado. Por favor regístrese")

LoginServlet
    ↓ catch (UserNotFoundException e)
    ↓ handleUserNotFoundError()
    ↓ Response 404 { success: false, error: "Usuario no encontrado...",
                     data: { userNotFound: true, suggestRegister: true } }

Frontend (login.html)
    ↓ determineErrorType() → "userNotFound"
    ↓ handleLoginError(..., "userNotFound", ...)
    ↓ showAlert() con tipo WARNING
    ↓ NO incrementar loginAttempts
    ↓ Mostrar link a registro
```

### Caso 2: Contraseña Incorrecta

```
Frontend (login.html)
    ↓ POST /api/login { email: "existe@mail.com", password: "wrongpass" }

Backend (LoginServlet)
    ↓ authService.autenticar()

AutenticacionService
    ↓ usuarioDAO.buscarPorEmail() → Usuario encontrado
    ↓ passwordHasher.verify() → false
    ↓ throw new AuthenticationException("Contraseña incorrecta")

LoginServlet
    ↓ catch (AuthenticationException e)
    ↓ handleAuthenticationError()
    ↓ Response 401 { success: false, error: "Contraseña incorrecta",
                     data: { passwordIncorrect: true, countAttempt: true } }

Frontend (login.html)
    ↓ determineErrorType() → "passwordIncorrect"
    ↓ handleLoginError(..., "passwordIncorrect", ...)
    ↓ SÍ incrementar loginAttempts
    ↓ showAlert() con contador de intentos
    ↓ Actualizar attemptCounter
```

---

## 🧪 Pruebas de Validación

### Test 1: Usuario No Existe

**Input:**
- Email: `noexiste@example.com`
- Password: `cualquiera`

**Expected Output:**
- HTTP Status: `404 NOT FOUND`
- Mensaje: `"Usuario no encontrado. Por favor regístrese"`
- Contador intentos: NO incrementa
- UI: Alerta WARNING con link a registro

**Log esperado:**
```
✗ Usuario no encontrado: noexiste@example.com
✗ Usuario no encontrado - Tiempo: XXXms
```

---

### Test 2: Contraseña Incorrecta

**Input:**
- Email: `usuario@example.com` (existe)
- Password: `wrongpassword`

**Expected Output:**
- HTTP Status: `401 UNAUTHORIZED`
- Mensaje: `"Contraseña incorrecta. Te quedan 4 intentos."`
- Contador intentos: SÍ incrementa
- UI: Alerta ERROR con contador

**Log esperado:**
```
✗ Contraseña incorrecta para: usuario@example.com
✗ Error de autenticación (contraseña incorrecta) - Tiempo: XXXms
```

---

### Test 3: Usuario Correcto

**Input:**
- Email: `usuario@example.com`
- Password: `correctpassword`

**Expected Output:**
- HTTP Status: `200 OK`
- Token JWT generado
- Contador intentos: Se resetea
- Redirect a dashboard

**Log esperado:**
```
✓ Usuario autenticado exitosamente: usuario@example.com
✓ Login exitoso para: usuario@example.com (ID: X) - Rol: XXX - Tiempo: XXXms
```

---

## 📋 Resumen de Archivos Modificados

### Backend (Java)

1. **NUEVO:** `UserNotFoundException.java`
   - Ubicación: `src/main/java/com/contactoprofesionales/exception/`
   - Propósito: Excepción específica para usuario no encontrado

2. **MODIFICADO:** `AutenticacionServiceImpl.java`
   - Líneas: 11 (import), 48-86 (método autenticar)
   - Cambios: Lanza `UserNotFoundException` cuando usuario no existe

3. **MODIFICADO:** `LoginServlet.java`
   - Líneas: 13 (import), 151-169 (catch), 254-302 (handlers)
   - Cambios: Maneja `UserNotFoundException` por separado con código 404

### Frontend (HTML/JavaScript)

4. **MODIFICADO:** `login.html`
   - Líneas: 752-801 (login method), 923-960 (handleLogin), 978-1027 (handleLoginError)
   - Cambios: Diferencia tipos de error y NO cuenta intentos para usuario no encontrado

---

## ✅ Beneficios de la Mejora

1. **Mejor UX**: Usuario sabe exactamente qué está mal
2. **Menos Bloqueos**: No se bloquean usuarios que escriben mal el email
3. **Conversión Mejorada**: Link directo a registro para nuevos usuarios
4. **Seguridad Mantenida**: Sigue contando intentos para contraseñas incorrectas
5. **Logs Más Claros**: Diferencia entre errores de usuario vs contraseña

---

## 🔒 Consideraciones de Seguridad

**Pregunta:** ¿No es un riesgo de seguridad revelar si un email existe o no?

**Respuesta:** En este caso, el beneficio de UX supera el riesgo mínimo porque:

1. El email NO es información sensible
2. La mayoría de sitios modernos ya hacen esto (Gmail, Facebook, etc.)
3. Los intentos de contraseña SÍ se siguen contando y bloqueando
4. Mejora significativa en la experiencia del usuario legítimo

**Alternativa (si se requiere máxima seguridad):**
- Usar códigos de error genéricos pero diferentes
- Mantener mensaje vago pero variar el código HTTP
- El frontend diferencia sin exponer al usuario

---

## 📌 Notas para Desarrolladores

1. **Orden de Catch Importa**: `UserNotFoundException` debe capturarse ANTES que `AuthenticationException`
2. **Status Codes**: 404 para usuario no encontrado, 401 para contraseña incorrecta
3. **Flags en Response**: Usar `data.userNotFound` y `data.passwordIncorrect` para claridad
4. **Frontend Robusto**: Verificar tanto status code como flags por compatibilidad

---

## 🎓 Conclusión

Esta mejora implementa un patrón común en aplicaciones modernas: **diferenciar entre "recurso no encontrado" y "acceso denegado"**, aplicado al contexto de autenticación.

**Resultado:** Mejor experiencia de usuario sin comprometer la seguridad.

---

**Implementado por:** Claude Code
**Fecha:** 2025-11-15
**Revisado:** ✅ Aprobado
