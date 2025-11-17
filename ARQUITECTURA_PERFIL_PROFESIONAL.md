# Arquitectura del Perfil Profesional - Actualización 2025-11-16

## 📋 Resumen de Cambios

Este documento explica la nueva arquitectura del módulo de perfil profesional después de la refactorización del 2025-11-16.

---

## 🏗️ Arquitectura Actual

### **Formulario Web: `profesional-refactorizado.html`**

El formulario web utiliza una arquitectura **modular** donde cada sección del perfil se gestiona con su propio servlet especializado.

```
┌─────────────────────────────────────┐
│  profesional-refactorizado.html    │
│  (Formulario de Gestión de Perfil) │
└─────────────────────────────────────┘
                 │
                 │ Usa estos endpoints:
                 │
    ┌────────────┴────────────┬────────────────┬──────────────────┐
    ▼                         ▼                ▼                  ▼
┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐  ┌──────────────────┐
│ Perfil Profesional Servlet │  │ Certificaciones    Servlet │  │ Proyectos Portafolio Servlet │  │ Antecedentes     Servlet │
│ /api/profesional/  │  │ /api/profesional/  │  │ /api/profesional/  │  │ /api/profesional/  │
│ perfil             │  │ certificaciones    │  │ proyectos          │  │ antecedentes       │
└──────────────────┘  └──────────────────┘  └──────────────────┘  └──────────────────┘
                                  │
                                  ▼
                          ┌──────────────────┐
                          │ Redes Sociales    Servlet │
                          │ /api/profesional/  │
                          │ redes-sociales     │
                          └──────────────────┘
```

---

## 📂 Estructura de Tablas

### **Tabla Principal: `profesionales`**
Contiene datos básicos del profesional:
- `id`, `usuario_id`
- `biografia_profesional`, `anios_experiencia`
- `idiomas[]`, `licencias_profesionales`
- `seguro_responsabilidad`, `metodos_pago[]`, `politica_cancelacion`
- `tarifa_hora`, `calificacion_promedio`, `total_resenas`
- `verificado`, `disponible`, `activo`

### **Tablas Relacionadas (1:N)**

```sql
especialidades_profesional
├─ id, profesional_id, categoria_id
├─ servicio_profesional, descripcion
├─ costo, tipo_costo, es_principal
└─ trabajo_remoto, trabajo_presencial

certificaciones_profesionales
├─ id, profesional_id
├─ nombre_certificacion, institucion
├─ fecha_obtencion, documento_url
└─ verificado

proyectos_portafolio
├─ id, profesional_id, categoria_id
├─ nombre_proyecto, descripcion
├─ fecha_realizacion, calificacion_cliente
└─ imagenes (tabla imagenes_proyecto)

antecedentes_profesionales
├─ id, profesional_id
├─ tipo_antecedente (policial, penal, judicial)
├─ numero_documento, fecha_emision
└─ archivo_url, verificado

redes_sociales_profesionales
├─ id, profesional_id
├─ tipo_red, url_perfil
└─ activo
```

---

## 🔧 Servlets y Sus Funciones

### **1. PerfilProfesionalServlet** ✅ ACTUALIZADO
**Endpoint:** `/api/profesional/perfil`
**Archivo:** `controller/perfil/PerfilProfesionalServlet.java`
**DTO:** `PerfilProfesionalCompletoDTO`

**Métodos:**
- `GET`: Obtener perfil completo con TODAS las relaciones (certificaciones, proyectos, antecedentes, redes)
- `PUT`: Actualizar SOLO datos básicos (biografía, experiencia, idiomas, métodos de pago, etc.)

**Campos Gestionados:**
```java
// Datos básicos del profesional
biografia_profesional, anios_experiencia
idiomas[], licencias_profesionales
seguro_responsabilidad, metodos_pago[]
politica_cancelacion
```

**⚠️ NO Gestiona:**
- ❌ Información personal (nombre, email, teléfono, documento) - Se gestiona en otro módulo
- ❌ Fotos (foto_perfil, foto_portada) - Se gestiona en otro módulo
- ❌ Certificaciones - Usa `CertificacionesProfesionalServlet`
- ❌ Proyectos - Usa `ProyectosPortafolioServlet`

---

### **2. CertificacionesProfesionalServlet** ✅ ACTUALIZADO
**Endpoint:** `/api/profesional/certificaciones`
**Archivo:** `controller/perfil/CertificacionesProfesionalServlet.java`
**Tabla:** `certificaciones_profesionales`

**Métodos:**
- `GET`: Listar todas las certificaciones del profesional
- `POST`: Crear nueva certificación
- `PUT`: Actualizar certificación existente
- `DELETE`: Eliminar certificación (soft delete)

---

### **3. ProyectosPortafolioServlet** ✅ ACTUALIZADO
**Endpoint:** `/api/profesional/proyectos`
**Archivo:** `controller/perfil/ProyectosPortafolioServlet.java`
**Tabla:** `proyectos_portafolio`

**Métodos:**
- `GET`: Listar todos los proyectos (máx. 20)
- `POST`: Crear nuevo proyecto
- `PUT`: Actualizar proyecto (NO permite modificar calificación del cliente)
- `DELETE`: Eliminar proyecto (soft delete)

**Características:**
- Máximo 20 proyectos activos por profesional
- Cada proyecto puede tener hasta 5 imágenes (tabla `imagenes_proyecto`)
- El selector de categorías se pobla dinámicamente con las especialidades del profesional

---

### **4. AntecedentesProfesionalServlet** ✅ ACTUALIZADO
**Endpoint:** `/api/profesional/antecedentes`
**Archivo:** `controller/perfil/AntecedentesProfesionalServlet.java`
**Tabla:** `antecedentes_profesionales`

**Métodos:**
- `GET`: Listar antecedentes del profesional
- `POST`: Crear nuevo antecedente
- `PUT`: Actualizar antecedente
- `DELETE`: Eliminar antecedente (soft delete)

**Tipos de Antecedentes:**
- `policial`: Certificado de antecedentes policiales
- `penal`: Certificado de antecedentes penales
- `judicial`: Certificado de antecedentes judiciales

**Restricciones:**
- Solo 1 antecedente activo de cada tipo
- Mejoran significativamente la puntuación de la plataforma cuando están verificados

---

### **5. RedesSocialesProfesionalServlet** ✅ ACTUALIZADO
**Endpoint:** `/api/profesional/redes-sociales`
**Archivo:** `controller/perfil/RedesSocialesProfesionalServlet.java`
**Tabla:** `redes_sociales_profesionales`

**Métodos:**
- `GET`: Listar redes sociales del profesional
- `POST`: Crear nueva red social
- `PUT`: Actualizar red social O actualización masiva (array)
- `DELETE`: Eliminar red social (soft delete)

**Tipos Soportados:**
- Facebook, Instagram, LinkedIn, Twitter
- YouTube, TikTok, WhatsApp
- Website, Otros

---

### **6. ProfesionalServlet** ⚠️ DEPRECADO PARA GESTIÓN DE PERFIL
**Endpoint:** `/api/profesionales`
**Archivo:** `controller/profesional/ProfesionalServlet.java`
**Modelo:** `Profesional` (DEPRECADO)

**⚠️ USO ACTUAL:**
- ✅ SOLO para búsqueda pública de profesionales
- ✅ SOLO para consultas de lectura (mostrar perfiles en búsquedas)
- ✅ Verificar existencia de perfil por usuarioId

**❌ NO SE USA PARA:**
- Gestión del perfil profesional (usar servlets específicos arriba)
- El formulario `profesional-refactorizado.html` NO usa este servlet

**Métodos:**
- `GET /api/profesionales`: Listar profesionales con filtros ✅
- `GET /api/profesionales/{id}`: Obtener perfil público ✅
- `GET /api/profesionales?usuarioId={id}`: Verificar existencia ✅
- `POST /api/profesionales`: ⚠️ DEPRECADO
- `PUT /api/profesionales/{id}`: ⚠️ DEPRECADO

---

## 🎨 Frontend: profesional-refactorizado.html

### **Cambios Implementados (2025-11-16)**

#### **1. Eliminados Campos de Información Personal**
```html
<!-- ❌ ELIMINADO -->
- Nombre completo
- Email
- Teléfono
- Documento de identidad
```
**Razón:** Estos datos se gestionan en otro módulo del sistema (gestión de usuarios)

#### **2. Eliminados Campos de Fotos**
```html
<!-- ❌ ELIMINADO -->
- Foto de perfil
- Foto de portada
```
**Razón:** La gestión de fotos se realiza en otro módulo

#### **3. Selector Múltiple de Idiomas** ✅ NUEVO
```html
<select id="idiomaSelector">
  <option value="Español">Español</option>
  <option value="Inglés">Inglés</option>
  <option value="Portugués">Portugués</option>
  <option value="Francés">Francés</option>
  <option value="Alemán">Alemán</option>
  <option value="Quechua">Quechua</option>
  <option value="Chino Mandarín">Chino Mandarín</option>
</select>
```

**Características:**
- Selección desde combo predefinido
- Visualización como chips/badges
- Validación de duplicados
- Almacenamiento como array en BD

#### **4. Selector Múltiple de Métodos de Pago** ✅ NUEVO
```html
<select id="metodoPagoSelector">
  <option value="Efectivo">Efectivo</option>
  <option value="Transferencia bancaria">Transferencia bancaria</option>
  <option value="Yape">Yape</option>
  <option value="Plin">Plin</option>
  <!-- ... 9 opciones en total -->
</select>
```

#### **5. Selector Dinámico de Categorías en Proyectos** ✅ NUEVO
**Flujo:**
1. Al cargar el perfil se obtienen las especialidades del profesional
2. Se extraen las categorías únicas de las especialidades
3. Se pobla el selector de categorías con SOLO las categorías que el profesional registró
4. El usuario solo puede asignar proyectos a categorías relevantes

**Endpoint Usado:**
```javascript
GET /api/profesionales/{id}/especialidades
```

**Ventajas:**
- No crear código duplicado
- Consistencia de datos
- Evita categorías no relacionadas con el perfil

---

## 📊 Modelo de Datos

### **DTO Principal: PerfilProfesionalCompletoDTO**

```java
public class PerfilProfesionalCompletoDTO {
    // Datos básicos del profesional
    private Integer id;
    private Integer usuarioId;
    private String biografiaProfesional;
    private Integer aniosExperiencia;
    private String[] idiomas;
    private String licenciasProfesionales;
    private Boolean seguroResponsabilidad;
    private String[] metodosPago;
    private String politicaCancelacion;
    private BigDecimal tarifaHora;
    private Double calificacionPromedio;
    private Integer totalResenas;
    private BigDecimal puntuacionPlataforma;
    private Boolean verificado;
    private Boolean disponible;

    // Relaciones (1:N)
    private List<CertificacionProfesional> certificaciones;
    private List<ProyectoPortafolio> proyectos;
    private List<AntecedenteProfesional> antecedentes;
    private List<RedSocialProfesional> redesSociales;
    private List<EspecialidadProfesional> especialidades;
}
```

---

## 🚀 Flujo de Trabajo

### **Carga Inicial del Perfil**

```javascript
1. Usuario abre profesional-refactorizado.html
2. Se ejecuta cargarPerfilCompleto()
   ├─ GET /api/profesional/perfil?usuarioId={id}
   └─ Devuelve PerfilProfesionalCompletoDTO con todas las relaciones
3. Se cargan especialidades
   ├─ GET /api/profesionales/{profId}/especialidades
   └─ Se guardan en AppState.especialidades
4. Se renderizan todas las secciones:
   ├─ Datos básicos con chips de idiomas y métodos de pago
   ├─ Certificaciones
   ├─ Proyectos del portafolio
   ├─ Antecedentes
   └─ Redes sociales
```

### **Guardar Datos Básicos**

```javascript
1. Usuario modifica datos básicos (biografía, idiomas, etc.)
2. Se ejecuta guardarDatosBasicos()
3. PUT /api/profesional/perfil
   ├─ Body: { biografiaProfesional, aniosExperiencia, idiomas[], etc. }
   └─ Se actualizan SOLO campos básicos en tabla profesionales
4. Éxito: Se muestra mensaje de confirmación
```

### **Agregar Proyecto**

```javascript
1. Usuario hace clic en "➕ Agregar Proyecto"
2. Se ejecuta abrirModalProyecto()
3. Se ejecuta poblarSelectorCategorias()
   ├─ Usa AppState.especialidades
   ├─ Extrae categorías únicas
   └─ Pobla el <select> con categorías del profesional
4. Usuario selecciona categoría y completa formulario
5. POST /api/profesional/proyectos
   ├─ Body: { nombreProyecto, categoriaId, descripcion, etc. }
   └─ Se valida límite de 20 proyectos
6. Éxito: Se agrega a la lista y se renderiza
```

---

## ⚠️ Campos Deprecados

### **Modelo Profesional.java**

```java
@Deprecated
private String fotoPerfil;           // Ya no se gestiona en formulario

@Deprecated
private String fotoPortada;          // Ya no se gestiona en formulario

@Deprecated
private String nombreCompleto;       // Ya no se gestiona en formulario

@Deprecated
private String email;                // Ya no se gestiona en formulario

@Deprecated
private String telefono;             // Ya no se gestiona en formulario

@Deprecated
private List<String> habilidades;    // Ahora en certificaciones_profesionales

@Deprecated
private List<String> certificaciones; // Ahora en certificaciones_profesionales

@Deprecated
private List<String> portafolio;     // Ahora en proyectos_portafolio

@Deprecated
private String especialidad;         // Ahora en especialidades_profesional
```

**Razón de Mantenerlos:**
- Se usan para búsqueda pública de profesionales
- Compatibilidad con código legacy
- Se llenarán desde tablas relacionadas para mostrar en búsqueda

**TODO Futuro:**
- Separar modelo de búsqueda pública vs gestión de perfil
- Crear `ProfesionalPublicoDTO` para búsquedas
- Eliminar campos deprecados del modelo `Profesional`

---

## 📝 Conclusiones

### **Ventajas de la Nueva Arquitectura**

1. **Modularidad**: Cada sección del perfil tiene su propio servlet
2. **Escalabilidad**: Fácil agregar nuevas secciones sin afectar existentes
3. **Mantenibilidad**: Código más organizado y fácil de mantener
4. **Consistencia**: Los datos se almacenan en tablas normalizadas
5. **UX Mejorada**: Chips, selectores dinámicos, validaciones

### **Migración Pendiente**

- Refactorizar ProfesionalService para usar DTOs específicos
- Crear servicio de búsqueda pública separado
- Eliminar campos deprecados una vez migrado todo el código legacy

---

**Fecha de Actualización:** 2025-11-16
**Autor:** Sistema
**Versión:** 2.0
