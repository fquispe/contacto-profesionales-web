# Progreso del Refactoring - Sistema de Gestión de Usuarios y Roles

**Fecha de inicio**: 2025-11-10
**Estado**: En progreso - Fase 1 completada (Backend)

---

## FASE 1: BACKEND - ✅ COMPLETADO

### 1.1 Base de Datos ✅

**Archivo**: `README_REFACTORING.md`

Se ha creado un archivo completo con todos los scripts SQL necesarios para implementar la nueva estructura:

- **Tablas geográficas**: departamentos, provincias, distritos (con datos de Perú pre-cargados)
- **Tabla usuarios**: Nueva tabla central para datos personales con gestión de roles (CLIENTE, PROFESIONAL, AMBOS)
- **Modificaciones a users**: Ahora solo para autenticación, vinculada con usuarios
- **Modificaciones a clientes**: Vinculada con usuarios, eliminación de campos duplicados
- **Modificaciones a profesionales**: Mejoras y vinculación
- **Tablas nuevas**:
  - categorias_servicio (12 categorías pre-cargadas)
  - especialidades_profesional (hasta 3 por profesional, 1 principal)
  - redes_sociales_profesional (8 tipos soportados)
- **Modificaciones a direcciones_cliente**: Ubicación geográfica estructurada
- **Scripts de migración**: Para migrar datos existentes sin pérdida
- **Vistas útiles**: vista_usuarios_completa, vista_profesionales_completa, vista_solicitudes_completa
- **Triggers**: Validación de especialidades máximas y especialidad principal única

### 1.2 Entidades Java (Models) ✅

**Ubicación**: `/src/main/java/com/contactoprofesionales/model/`

Nuevas entidades creadas siguiendo el patrón del proyecto:

1. **Departamento.java** - Departamentos del Perú
2. **Provincia.java** - Provincias del Perú
3. **Distrito.java** - Distritos del Perú
4. **UsuarioPersona.java** - Datos personales centralizados (tabla usuarios)
5. **CategoriaServicio.java** - Categorías de servicios profesionales
6. **EspecialidadProfesional.java** - Especialidades de profesionales (hasta 3)
7. **RedSocialProfesional.java** - Redes sociales de profesionales

**Características**:
- Sin anotaciones JPA (JDBC puro)
- Constructores vacíos, con parámetros y completos
- Getters y Setters completos
- Métodos toString() para debugging
- Validaciones mediante constraints en SQL

### 1.3 DAOs (Data Access Objects) ✅

**Ubicación**: `/src/main/java/com/contactoprofesionales/dao/`

Interfaces y implementaciones creadas:

#### Ubicación Geográfica
- **UbicacionDAO** + **UbicacionDAOImpl**
  - Gestión completa de departamentos, provincias y distritos
  - Búsquedas por ID, código y nombre
  - Listados jerárquicos (provincias por departamento, distritos por provincia)

#### Usuario Persona
- **UsuarioPersonaDAO** + **UsuarioPersonaDAOImpl**
  - CRUD completo de datos personales
  - Gestión de roles (CLIENTE, PROFESIONAL, AMBOS)
  - Actualización de ubicación geográfica
  - Validaciones de duplicados (documento, teléfono)
  - Listados por tipo de rol

#### Categorías y Especialidades
- **CategoriaServicioDAO** + **CategoriaServicioDAOImpl**
  - Listado de categorías de servicios
  - Búsquedas por ID y nombre

- **EspecialidadProfesionalDAO** + **EspecialidadProfesionalDAOImpl**
  - Registro de especialidades (máximo 3)
  - JOIN con categorias_servicio para datos completos
  - Gestión de especialidad principal con **transacciones**
  - Eliminación de especialidades

#### Redes Sociales
- **RedSocialProfesionalDAO** + **RedSocialProfesionalDAOImpl**
  - CRUD completo de redes sociales
  - Validación de tipos de red (8 tipos soportados)
  - Validación de URLs

**Patrón común en todas las DAOs**:
- JDBC puro con PreparedStatements
- Try-with-resources para gestión de recursos
- Logging con SLF4J (debug, info, error)
- DatabaseException para errores
- Métodos privados de mapeo (mapXXX)
- Prevención de SQL injection

### 1.4 DTOs (Data Transfer Objects) ✅

**Ubicación**: `/src/main/java/com/contactoprofesionales/dto/`

Nuevos DTOs creados:

1. **UsuarioPersonaDTO** - Datos personales del usuario
2. **RegistroCompletoRequest** - Registro con selección de tipo de cuenta (CLIENTE/PROFESIONAL)
3. **UbicacionDTO** - Datos geográficos combinados
4. **DepartamentoDTO** - Departamentos
5. **ProvinciaDTO** - Provincias
6. **DistritoDTO** - Distritos
7. **EspecialidadDTO** - Especialidades con datos de categoría
8. **RedSocialDTO** - Redes sociales

**Características**:
- POJOs simples sin lógica de negocio
- Constructores vacíos y con parámetros
- Campos adicionales para JOINs (no persistidos)
- Tipos apropiados (LocalDate, LocalDateTime, etc.)

---

## FASE 2: SERVICIOS Y LÓGICA DE NEGOCIO - ⏳ PENDIENTE

### 2.1 Servicios a Crear

#### UsuarioPersonaService
- **Ubicación**: `/src/main/java/com/contactoprofesionales/service/usuariopersona/`
- **Responsabilidades**:
  - Gestión de datos personales
  - Actualización de roles (convertir cliente en profesional o viceversa)
  - Validaciones de negocio
  - Integración con ubicación geográfica

#### UbicacionService
- **Ubicación**: `/src/main/java/com/contactoprofesionales/service/ubicacion/`
- **Responsabilidades**:
  - Proveer listas de departamentos, provincias, distritos
  - Validación de ubicaciones
  - Conversión de modelos a DTOs

#### EspecialidadService
- **Ubicación**: `/src/main/java/com/contactoprofesionales/service/profesional/`
- **Responsabilidades**:
  - Gestión de especialidades (máximo 3)
  - Cambio de especialidad principal
  - Validaciones de negocio

#### RedSocialService
- **Ubicación**: `/src/main/java/com/contactoprofesionales/service/profesional/`
- **Responsabilidades**:
  - Gestión de redes sociales
  - Validación de URLs
  - Verificación de redes sociales

### 2.2 Servicios a Modificar

#### AutenticacionService ⚠️ CRÍTICO
- **Modificar**: Proceso de registro completo
- **Agregar**:
  - Creación de usuario en tabla `users`
  - Creación de usuario_persona en tabla `usuarios`
  - Creación de perfil en `clientes` o `profesionales` según tipo de cuenta
  - Transacción atómica para garantizar consistencia
  - Manejo de rollback en caso de error

#### ClienteService ⚠️ MODIFICAR
- **Agregar**:
  - Vinculación con `usuario_persona`
  - Actualización de ubicación geográfica en `usuarios`
  - Validación de perfil completo

#### ProfesionalService ⚠️ MODIFICAR
- **Agregar**:
  - Vinculación con `usuario_persona`
  - Gestión de especialidades (llamadas a EspecialidadService)
  - Gestión de redes sociales (llamadas a RedSocialService)

---

## FASE 3: CONTROLADORES (SERVLETS) - ⏳ PENDIENTE

### 3.1 Servlets a Crear

#### UbicacionServlet
- **Ruta**: `/api/ubicacion`
- **Endpoints**:
  - GET `/api/ubicacion/departamentos` - Listar departamentos
  - GET `/api/ubicacion/provincias?departamentoId={id}` - Provincias por departamento
  - GET `/api/ubicacion/distritos?provinciaId={id}` - Distritos por provincia
  - GET `/api/ubicacion/distritos/buscar?nombre={nombre}` - Buscar distritos

#### UsuarioPersonaServlet
- **Ruta**: `/api/usuario-persona`
- **Endpoints**:
  - GET `/api/usuario-persona/{id}` - Obtener datos personales
  - PUT `/api/usuario-persona/{id}` - Actualizar datos personales
  - PUT `/api/usuario-persona/{id}/rol` - Cambiar tipo de rol
  - PUT `/api/usuario-persona/{id}/ubicacion` - Actualizar ubicación

#### EspecialidadServlet
- **Ruta**: `/api/profesionales/{id}/especialidades`
- **Endpoints**:
  - GET - Listar especialidades del profesional
  - POST - Agregar especialidad (máx 3)
  - DELETE `/api/profesionales/{profId}/especialidades/{id}` - Eliminar
  - PUT `/api/profesionales/{profId}/especialidades/{id}/principal` - Marcar como principal

#### RedSocialServlet
- **Ruta**: `/api/profesionales/{id}/redes-sociales`
- **Endpoints**:
  - GET - Listar redes sociales
  - POST - Agregar red social
  - PUT `/{redId}` - Actualizar red social
  - DELETE `/{redId}` - Eliminar red social

### 3.2 Servlets a Modificar

#### RegistroServlet ⚠️ CRÍTICO
- **Modificar**: POST `/api/register`
- **Cambios**:
  - Recibir `RegistroCompletoRequest` con campo `tipoCuenta`
  - Crear usuario en `users` (autenticación)
  - Crear usuario_persona en `usuarios` con rol apropiado
  - Crear perfil en `clientes` SI tipoCuenta = "CLIENTE" o "AMBOS"
  - Crear perfil en `profesionales` SI tipoCuenta = "PROFESIONAL" o "AMBOS"
  - Usar transacciones para atomicidad
  - Retornar token JWT con información de roles

#### LoginServlet ⚠️ MODIFICAR
- **Modificar**: Respuesta del login
- **Agregar en respuesta**:
  - `tipoRol` del usuario (CLIENTE, PROFESIONAL, AMBOS)
  - `esCliente`, `esProfesional`
  - IDs de perfil de cliente y profesional (si existen)
  - Flags de perfil completo

#### ClienteServlet ⚠️ MODIFICAR
- **Modificar**: Métodos GET, POST, PUT
- **Cambios**:
  - Vincular con `usuario_persona`
  - Retornar datos combinados de `usuarios` + `clientes`
  - Actualizar ubicación en `usuarios` al actualizar cliente

---

## FASE 4: FRONTEND (HTML + JavaScript) - ⏳ PENDIENTE

### 4.1 Modificaciones a register.html ⚠️ CRÍTICO

**Ubicación**: `/src/main/webapp/register.html`

**Cambios necesarios**:

1. **Activar botones de selección de tipo de cuenta**:
   ```html
   <div class="account-type-selection">
     <button type="button" id="btnCliente" class="btn-account-type active">
       <i class="fas fa-user"></i> Cliente
     </button>
     <button type="button" id="btnProveedor" class="btn-account-type">
       <i class="fas fa-briefcase"></i> Proveedor
     </button>
   </div>
   <input type="hidden" id="tipoCuenta" name="tipoCuenta" value="CLIENTE">
   ```

2. **Agregar campos adicionales**:
   - Nombre completo (ya existe)
   - Tipo de documento (DNI, CE, RUC, PASAPORTE)
   - Número de documento
   - Teléfono (ya existe)
   - Fecha de nacimiento (opcional)
   - Género (opcional)

3. **JavaScript**:
   - Handler para cambio de tipo de cuenta
   - Validaciones específicas por tipo
   - Envío de `tipoCuenta` en el request
   - Manejo de respuesta con roles

### 4.2 Modificaciones a dashboard.html ⚠️ CRÍTICO

**Ubicación**: `/src/main/webapp/dashboard.html`

**Cambios necesarios**:

1. **Detección de rol al cargar**:
   ```javascript
   // Al cargar el dashboard
   const userData = getUserData(); // Desde localStorage o API
   const tipoRol = userData.tipoRol; // CLIENTE, PROFESIONAL, AMBOS

   if (tipoRol === 'CLIENTE') {
     mostrarDashboardCliente();
   } else if (tipoRol === 'PROFESIONAL') {
     mostrarDashboardProfesional();
   } else if (tipoRol === 'AMBOS') {
     mostrarDashboardCombinado();
   }
   ```

2. **Secciones dinámicas**:
   - **Cliente**: Mis solicitudes de servicio
   - **Profesional**: Trabajos asignados/pendientes
   - **Ambos**: Tabs o secciones separadas

3. **Invitación a completar perfil**:
   ```html
   <div class="profile-invitation" v-if="!perfilProfesionalCompleto && tipoRol === 'CLIENTE'">
     <p>¿Eres profesional? <a href="#" @click="agregarRolProfesional">Regístrate como proveedor</a></p>
   </div>
   ```

4. **Reorganizar menú lateral**:
   - Iconos más claros
   - Nombres descriptivos
   - Mostrar/ocultar según rol

### 4.3 Modificaciones a solicitud-servicio.html

**Ubicación**: `/src/main/webapp/solicitud-servicio.html`

**Cambios necesarios**:

1. **Cargar ubicación automáticamente**:
   ```javascript
   async function cargarUbicacionCliente() {
     const clienteId = getClienteId();
     const response = await fetch(`/api/clientes/${clienteId}`);
     const cliente = await response.json();

     if (cliente.usuario) {
       // Cargar datos de ubicación desde usuario_persona
       document.getElementById('direccion').value = cliente.usuario.direccion || '';
       document.getElementById('distrito').value = cliente.usuario.distritoNombre || '';
       document.getElementById('referencia').value = cliente.usuario.referenciaDireccion || '';

       // Si no tiene ubicación, mostrar mensaje
       if (!cliente.usuario.direccion) {
         mostrarMensaje('Por favor, completa tu ubicación en tu perfil.');
       }
     }
   }
   ```

2. **Permitir editar ubicación temporal**:
   - Campos editables en el formulario
   - Guardar ubicación actualizada en perfil del cliente

3. **Selección de distrito con autocomplete**:
   - Conectar con API `/api/ubicacion/distritos/buscar?nombre={texto}`
   - Dropdown dinámico

### 4.4 Modificaciones a profesional.html

**Ubicación**: `/src/main/webapp/profesional.html`

**Cambios necesarios**:

1. **Sección de especialidades** (hasta 3):
   ```html
   <div class="especialidades-section">
     <h3>Especialidades (máximo 3)</h3>
     <div id="especialidades-list">
       <!-- Lista de especialidades con opción de marcar como principal -->
       <div class="especialidad-item">
         <select class="form-control">
           <option value="">Selecciona una categoría</option>
           <!-- Cargar desde /api/categorias -->
         </select>
         <input type="number" placeholder="Años de experiencia">
         <label><input type="radio" name="principal"> Principal</label>
         <button class="btn-eliminar"><i class="fas fa-trash"></i></button>
       </div>
     </div>
     <button id="btn-agregar-especialidad" class="btn btn-secondary">+ Agregar especialidad</button>
   </div>
   ```

2. **Sección de redes sociales**:
   ```html
   <div class="redes-sociales-section">
     <h3>Redes Sociales</h3>
     <div class="red-social-item">
       <select class="form-control">
         <option value="FACEBOOK">Facebook</option>
         <option value="INSTAGRAM">Instagram</option>
         <option value="LINKEDIN">LinkedIn</option>
         <option value="TWITTER">Twitter</option>
         <option value="TIKTOK">TikTok</option>
         <option value="WHATSAPP">WhatsApp</option>
         <option value="WEBSITE">Sitio Web</option>
         <option value="YOUTUBE">YouTube</option>
       </select>
       <input type="url" placeholder="URL de la red social" class="form-control">
       <button class="btn-agregar"><i class="fas fa-plus"></i></button>
     </div>
     <div id="redes-sociales-list">
       <!-- Lista de redes sociales guardadas -->
     </div>
   </div>
   ```

3. **JavaScript**:
   - Validación de máximo 3 especialidades
   - Solo 1 especialidad principal
   - Validación de URLs de redes sociales
   - CRUD completo de especialidades y redes

### 4.5 Scripts JavaScript a Crear

#### ubicacion-api.js
**Ubicación**: `/src/main/webapp/assets/js/ubicacion-api.js`

```javascript
class UbicacionAPI {
  static async getDepartamentos() {
    const response = await fetch('/api/ubicacion/departamentos');
    return await response.json();
  }

  static async getProvincias(departamentoId) {
    const response = await fetch(`/api/ubicacion/provincias?departamentoId=${departamentoId}`);
    return await response.json();
  }

  static async getDistritos(provinciaId) {
    const response = await fetch(`/api/ubicacion/distritos?provinciaId=${provinciaId}`);
    return await response.json();
  }

  static async buscarDistritos(nombre) {
    const response = await fetch(`/api/ubicacion/distritos/buscar?nombre=${encodeURIComponent(nombre)}`);
    return await response.json();
  }
}
```

#### especialidad-api.js
**Ubicación**: `/src/main/webapp/assets/js/especialidad-api.js`

Gestión de especialidades del profesional.

#### red-social-api.js
**Ubicación**: `/src/main/webapp/assets/js/red-social-api.js`

Gestión de redes sociales del profesional.

---

## FASE 5: MIGRACIÓN DE DATOS - ⚠️ CRÍTICO

### Pasos para Migración

1. **Backup de la base de datos**:
   ```bash
   pg_dump -U postgres -d contacto_profesionales > backup_antes_migracion_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **Ejecutar scripts SQL en orden** (ver `README_REFACTORING.md`):
   - Paso 1: Crear tablas geográficas
   - Paso 2: Crear tabla usuarios
   - Paso 3: Modificar tabla users
   - Paso 4-7: Modificar tablas existentes
   - Paso 8: **Migración de datos** (CRÍTICO)
   - Paso 9: Índices adicionales
   - Paso 10: Crear vistas

3. **Verificar migración**:
   - Ejecutar queries de verificación (ver README_REFACTORING.md)
   - Validar que no hay datos huérfanos
   - Verificar integridad referencial

4. **Probar en ambiente de desarrollo primero**

---

## ARQUITECTURA FINAL

### Flujo de Registro

```
1. Usuario ingresa a register.html
2. Selecciona tipo de cuenta: CLIENTE o PROFESIONAL
3. Completa formulario con datos personales
4. Click en "Registrar"
5. POST /api/register con RegistroCompletoRequest
6. AutenticacionService.registrarCompleto():
   a. Crear usuario en tabla `users` (email, password_hash)
   b. Crear usuario_persona en tabla `usuarios` (datos personales, tipo_rol)
   c. Vincular users.usuario_id con usuarios.id
   d. SI tipoCuenta = "CLIENTE" o "AMBOS":
      - Crear registro en tabla `clientes` vinculado con usuarios.id
   e. SI tipoCuenta = "PROFESIONAL" o "AMBOS":
      - Crear registro en tabla `profesionales` vinculado con users.id
7. Generar token JWT con información de roles
8. Responder con LoginResponse incluyendo roles
9. Frontend guarda token y redirige a dashboard
```

### Flujo de Login

```
1. Usuario ingresa a login.html
2. Ingresa email y password
3. POST /api/login
4. AutenticacionService.autenticar():
   a. Validar credenciales en tabla `users`
   b. Buscar usuario_persona vinculado
   c. Verificar roles (esCliente, esProfesional)
   d. Buscar IDs de perfiles (cliente_id, profesional_id)
   e. Generar token JWT con información completa
5. Responder con LoginResponse:
   - token
   - user (id, email, activo)
   - usuarioPersona (id, nombreCompleto, tipoRol, esCliente, esProfesional)
   - clienteId (si es cliente)
   - profesionalId (si es profesional)
   - perfilCompletoCliente (boolean)
   - perfilCompletoProfesional (boolean)
6. Frontend guarda datos y redirige a dashboard según rol
```

### Flujo de Dashboard

```
1. Dashboard carga
2. Leer datos del usuario desde localStorage o /api/usuario-actual
3. Evaluar tipoRol:
   - CLIENTE: Mostrar "Mis Solicitudes de Servicio"
   - PROFESIONAL: Mostrar "Trabajos Asignados"
   - AMBOS: Mostrar ambas secciones en tabs
4. Si perfilCompletoCliente === false: Invitar a completar perfil
5. Si esCliente === false: Mostrar CTA "¿Eres profesional?"
6. Si esProfesional === false: Mostrar CTA "¿Necesitas contratar?"
```

---

## VALIDACIONES IMPORTANTES

### A Nivel de Base de Datos
✅ Triggers para máximo 3 especialidades
✅ Triggers para solo 1 especialidad principal
✅ Constraints para tipos de documento válidos
✅ Constraints para género válido
✅ Constraints de integridad referencial
✅ Unique constraints (email, numero_documento, telefono)

### A Nivel de Servicio
⏳ Validar que tipoCuenta sea válido (CLIENTE, PROFESIONAL, AMBOS)
⏳ Validar email único en registro
⏳ Validar documento único
⏳ Validar teléfono único
⏳ Validar que usuario no cambie de PROFESIONAL a CLIENTE si tiene trabajos activos
⏳ Validar URLs de redes sociales
⏳ Validar especialidades (máximo 3)

### A Nivel de Frontend
⏳ Validación de formularios con mensajes claros
⏳ Prevenir envíos duplicados
⏳ Validación de formato de email
⏳ Validación de contraseña segura
⏳ Validación de URLs
⏳ Autocomplete de distritos

---

## TESTING REQUERIDO

### Tests Unitarios (Pendiente)
- DAOs: Probar cada método CRUD
- Servicios: Probar lógica de negocio
- Validaciones: Probar casos límite

### Tests de Integración (Pendiente)
- Flujo de registro completo
- Flujo de login
- Cambio de roles
- Actualización de ubicación
- Gestión de especialidades
- Gestión de redes sociales

### Tests E2E (Pendiente)
- Registro como cliente → Solicitar servicio
- Registro como profesional → Recibir solicitud
- Cliente se vuelve profesional
- Profesional se vuelve cliente

---

## ARCHIVOS CREADOS EN FASE 1

### Documentación
1. `/README_REFACTORING.md` - Scripts SQL completos
2. `/REFACTORING_PROGRESO.md` - Este archivo

### Models (7 archivos)
3. `/src/main/java/com/contactoprofesionales/model/Departamento.java`
4. `/src/main/java/com/contactoprofesionales/model/Provincia.java`
5. `/src/main/java/com/contactoprofesionales/model/Distrito.java`
6. `/src/main/java/com/contactoprofesionales/model/UsuarioPersona.java`
7. `/src/main/java/com/contactoprofesionales/model/CategoriaServicio.java`
8. `/src/main/java/com/contactoprofesionales/model/EspecialidadProfesional.java`
9. `/src/main/java/com/contactoprofesionales/model/RedSocialProfesional.java`

### DAOs - Interfaces (6 archivos)
10. `/src/main/java/com/contactoprofesionales/dao/ubicacion/UbicacionDAO.java`
11. `/src/main/java/com/contactoprofesionales/dao/usuariopersona/UsuarioPersonaDAO.java`
12. `/src/main/java/com/contactoprofesionales/dao/categoria/CategoriaServicioDAO.java`
13. `/src/main/java/com/contactoprofesionales/dao/profesional/EspecialidadProfesionalDAO.java`
14. `/src/main/java/com/contactoprofesionales/dao/profesional/RedSocialProfesionalDAO.java`

### DAOs - Implementaciones (5 archivos)
15. `/src/main/java/com/contactoprofesionales/dao/ubicacion/UbicacionDAOImpl.java`
16. `/src/main/java/com/contactoprofesionales/dao/usuariopersona/UsuarioPersonaDAOImpl.java`
17. `/src/main/java/com/contactoprofesionales/dao/categoria/CategoriaServicioDAOImpl.java`
18. `/src/main/java/com/contactoprofesionales/dao/profesional/EspecialidadProfesionalDAOImpl.java`
19. `/src/main/java/com/contactoprofesionales/dao/profesional/RedSocialProfesionalDAOImpl.java`

### DTOs (8 archivos)
20. `/src/main/java/com/contactoprofesionales/dto/UsuarioPersonaDTO.java`
21. `/src/main/java/com/contactoprofesionales/dto/RegistroCompletoRequest.java`
22. `/src/main/java/com/contactoprofesionales/dto/UbicacionDTO.java`
23. `/src/main/java/com/contactoprofesionales/dto/DepartamentoDTO.java`
24. `/src/main/java/com/contactoprofesionales/dto/ProvinciaDTO.java`
25. `/src/main/java/com/contactoprofesionales/dto/DistritoDTO.java`
26. `/src/main/java/com/contactoprofesionales/dto/EspecialidadDTO.java`
27. `/src/main/java/com/contactoprofesionales/dto/RedSocialDTO.java`

**Total: 27 archivos creados**

---

## PRÓXIMOS PASOS CRÍTICOS

### Inmediato (Prioritario)
1. ✅ **Commit de cambios actuales** - Preservar el trabajo realizado
2. ⚠️ **Ejecutar migración SQL** en base de datos de desarrollo
3. ⚠️ **Crear servicios** (UsuarioPersonaService, UbicacionService, EspecialidadService, RedSocialService)
4. ⚠️ **Modificar AutenticacionService** para registro completo
5. ⚠️ **Crear UbicacionServlet** para APIs geográficas

### Medio Plazo
6. ⚠️ Modificar RegistroServlet
7. ⚠️ Modificar LoginServlet (respuesta con roles)
8. ⚠️ Modificar register.html (selección de tipo de cuenta)
9. ⚠️ Modificar dashboard.html (detección de roles)
10. ⚠️ Crear scripts JavaScript (ubicacion-api.js, etc.)

### Largo Plazo
11. Modificar solicitud-servicio.html (carga automática de ubicación)
12. Modificar profesional.html (especialidades y redes sociales)
13. Tests unitarios
14. Tests de integración
15. Documentación de APIs
16. Migración en producción

---

## NOTAS IMPORTANTES

1. **No eliminar columnas aún**: Las columnas de tablas existentes marcadas para eliminación (como `nombre` en `users`, `email` en `clientes`) NO deben eliminarse hasta DESPUÉS de que la migración esté completa y probada en producción.

2. **Transacciones son críticas**: El registro de usuarios DEBE ser transaccional. Si falla cualquier paso (crear user, crear usuario_persona, crear cliente/profesional), TODO debe hacer rollback.

3. **Compatibilidad hacia atrás**: Durante la migración, tanto el sistema viejo como el nuevo deben coexistir temporalmente.

4. **JWT debe incluir roles**: El token JWT generado en login debe incluir claims de `tipoRol`, `esCliente`, `esProfesional` para que el frontend sepa qué mostrar.

5. **Validación de frontend es auxiliar**: La validación real SIEMPRE debe ocurrir en el backend. Frontend solo mejora UX.

---

## CONTACTO Y SOPORTE

Para dudas sobre la implementación:
- Revisar `/README_REFACTORING.md` para detalles de SQL
- Revisar código de DAOs existentes (ClienteDAOImpl, UsuarioDAO) como referencia
- Los servicios deben seguir el patrón de ClienteServiceImpl
- Los servlets deben seguir el patrón de ClienteServlet

---

**Última actualización**: 2025-11-10
**Progreso global**: 35% (Fase 1 completa, Fases 2-5 pendientes)
