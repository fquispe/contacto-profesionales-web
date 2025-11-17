# 🔄 Refactorización Completa del Perfil Profesional

**Fecha:** 2025-11-15
**Tipo:** Refactorización Mayor
**Estado:** 🚧 En Progreso

---

## 📋 Resumen Ejecutivo

Este documento detalla la refactorización completa del formulario `profesional.html` eliminando duplicados, añadiendo nuevas secciones profesionales y mejorando la estructura de datos.

### Objetivos Principales:

1. ✅ **Eliminar campos duplicados** (ubicación, distrito, etc.)
2. ✅ **Añadir biografía profesional**
3. ✅ **Sistema de certificaciones** con institución
4. ✅ **Portafolio de proyectos** (hasta 20)
5. ✅ **Imágenes de proyectos** (hasta 5 por proyecto)
6. ✅ **Valoración de clientes** (0-10 estrellas)
7. ✅ **Redes sociales** (Facebook, LinkedIn, etc.)
8. ✅ **Antecedentes** (policial, penal, judicial)
9. ✅ **Información adicional** (idiomas, licencias, seguros)

---

## 🗄️ Cambios en Base de Datos

### Migración V006: `V006__refactorizar_perfil_profesional.sql`

#### 1. Tabla `profesionales` - MODIFICADA

**Campos ELIMINADOS:**
```sql
- ubicacion          (duplicado en area_servicio)
- distrito           (duplicado en area_servicio)
- latitud            (duplicado en area_servicio)
- longitud           (duplicado en area_servicio)
- radio_servicio     (duplicado en area_servicio)
```

**Campos AÑADIDOS:**
```sql
+ biografia_profesional          TEXT                    -- Resumen profesional
+ idiomas                         VARCHAR(255)[]          -- Array de idiomas
+ licencias_profesionales         TEXT                    -- Licencias profesionales
+ seguro_responsabilidad          BOOLEAN DEFAULT FALSE   -- Tiene seguro
+ metodos_pago                    VARCHAR(100)[]          -- Métodos de pago
+ politica_cancelacion            TEXT                    -- Política de cancelación
```

**Campo EXISTENTE (sin cambios):**
- `anios_experiencia` → Ya existe, se usa tal cual

---

#### 2. Tabla `certificaciones_profesional` - NUEVA

```sql
CREATE TABLE certificaciones_profesional (
    id                      SERIAL PRIMARY KEY,
    profesional_id          INTEGER NOT NULL,
    nombre_certificacion    VARCHAR(255) NOT NULL,
    institucion             VARCHAR(255) NOT NULL,    -- ✅ Institución emisora
    fecha_obtencion         DATE,
    fecha_vigencia          DATE,
    documento_url           VARCHAR(500),              -- URL del documento
    descripcion             TEXT,
    orden                   INTEGER DEFAULT 1,
    activo                  BOOLEAN DEFAULT TRUE,
    fecha_creacion          TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion     TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (profesional_id) REFERENCES profesionales(id) ON DELETE CASCADE
);
```

**Índices:**
- `idx_certificaciones_profesional_id` (profesional_id, activo)

---

#### 3. Tabla `proyectos_portafolio` - NUEVA

```sql
CREATE TABLE proyectos_portafolio (
    id                      SERIAL PRIMARY KEY,
    profesional_id          INTEGER NOT NULL,
    nombre_proyecto         VARCHAR(255) NOT NULL,
    fecha_realizacion       DATE NOT NULL,
    descripcion             TEXT NOT NULL,
    categoria_id            INTEGER,                    -- ✅ Categoría del servicio
    solicitud_servicio_id   INTEGER,                    -- ✅ Relación con solicitud real
    calificacion_cliente    DECIMAL(3,1) CHECK (0-10),  -- ✅ Calificación del cliente
    comentario_cliente      TEXT,                       -- ✅ Comentario del cliente
    orden                   INTEGER DEFAULT 1,
    activo                  BOOLEAN DEFAULT TRUE,
    fecha_creacion          TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion     TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (profesional_id) REFERENCES profesionales(id) ON DELETE CASCADE,
    FOREIGN KEY (categoria_id) REFERENCES categorias_profesionales(id) ON DELETE SET NULL
);
```

**Constraint:**
- ✅ **Máximo 20 proyectos activos** por profesional (trigger `verificar_limite_proyectos`)

**Índices:**
- `idx_proyectos_profesional_id` (profesional_id, activo)
- `idx_proyectos_categoria` (categoria_id)
- `idx_proyectos_calificacion` (calificacion_cliente DESC)

---

#### 4. Tabla `imagenes_proyecto` - NUEVA

```sql
CREATE TABLE imagenes_proyecto (
    id              SERIAL PRIMARY KEY,
    proyecto_id     INTEGER NOT NULL,
    url_imagen      VARCHAR(500) NOT NULL,
    tipo_imagen     VARCHAR(20) CHECK IN ('antes', 'despues', 'proceso', 'general'),
    descripcion     VARCHAR(255),
    orden           INTEGER DEFAULT 1,
    fecha_subida    TIMESTAMP DEFAULT NOW(),

    FOREIGN KEY (proyecto_id) REFERENCES proyectos_portafolio(id) ON DELETE CASCADE
);
```

**Constraint:**
- ✅ **Máximo 5 imágenes** por proyecto (trigger `verificar_limite_imagenes`)

**Índices:**
- `idx_imagenes_proyecto_id` (proyecto_id, orden)

---

#### 5. Tabla `antecedentes_profesional` - NUEVA

```sql
CREATE TABLE antecedentes_profesional (
    id                  SERIAL PRIMARY KEY,
    profesional_id      INTEGER NOT NULL,
    tipo_antecedente    VARCHAR(50) CHECK IN ('policial', 'penal', 'judicial') NOT NULL,
    documento_url       VARCHAR(500) NOT NULL,
    fecha_emision       DATE,
    fecha_subida        TIMESTAMP DEFAULT NOW(),
    verificado          BOOLEAN DEFAULT FALSE,        -- ✅ Verificado por admin
    fecha_verificacion  TIMESTAMP,
    observaciones       TEXT,
    activo              BOOLEAN DEFAULT TRUE,

    FOREIGN KEY (profesional_id) REFERENCES profesionales(id) ON DELETE CASCADE,
    UNIQUE (profesional_id, tipo_antecedente, activo)  -- Solo 1 activo por tipo
);
```

**Índices:**
- `idx_antecedentes_profesional_id` (profesional_id, activo)
- `idx_antecedentes_verificado` (verificado)

---

#### 6. Tabla `redes_sociales_profesional` - ACTUALIZADA

**Ya existe pero se verifica estructura:**

```sql
CREATE TABLE IF NOT EXISTS redes_sociales_profesional (
    id                  SERIAL PRIMARY KEY,
    profesional_id      INTEGER NOT NULL,
    tipo_red            VARCHAR(50) CHECK IN ('facebook', 'instagram', 'youtube', 'tiktok',
                                            'linkedin', 'twitter', 'whatsapp', 'website', 'otro') NOT NULL,
    url                 VARCHAR(500) NOT NULL,
    verificada          BOOLEAN DEFAULT FALSE,
    activo              BOOLEAN DEFAULT TRUE,         -- ✅ NUEVO
    fecha_creacion      TIMESTAMP DEFAULT NOW(),
    fecha_actualizacion TIMESTAMP DEFAULT NOW(),      -- ✅ NUEVO

    FOREIGN KEY (profesional_id) REFERENCES profesionales(id) ON DELETE CASCADE,
    UNIQUE (profesional_id, tipo_red, activo)
);
```

---

#### 7. Función `calcular_puntuacion_profesional()` - NUEVA

Calcula puntuación del profesional (0-10) basada en:

| Criterio | Peso | Máximo |
|----------|------|--------|
| Calificación promedio proyectos | 40% | 4.0 |
| Certificaciones | 20% | 2.0 |
| Antecedentes verificados (3) | 20% | 2.0 |
| Años de experiencia | 10% | 1.0 |
| Biografía completa (>50 chars) | 10% | 1.0 |
| **TOTAL** | **100%** | **10.0** |

**Uso:**
```sql
SELECT calcular_puntuacion_profesional(1);  -- Retorna puntuación del profesional ID=1
```

---

## 🏗️ Arquitectura Backend (Java)

### Models Creados:

1. ✅ **`CertificacionProfesional.java`**
   - Ubicación: `com.contactoprofesionales.model`
   - Campos: nombre, institución, fechas, documento, etc.

2. ✅ **`ProyectoPortafolio.java`**
   - Ubicación: `com.contactoprofesionales.model`
   - Campos: nombre, fecha, descripción, categoría, calificación
   - Relación: `List<ImagenProyecto> imagenes`

3. ✅ **`ImagenProyecto.java`**
   - Ubicación: `com.contactoprofesionales.model`
   - Enum `TipoImagen`: ANTES, DESPUES, PROCESO, GENERAL

4. ✅ **`AntecedenteProfesional.java`**
   - Ubicación: `com.contactoprofesionales.model`
   - Enum `TipoAntecedente`: POLICIAL, PENAL, JUDICIAL

5. ⚠️ **`RedSocialProfesional.java`** - YA EXISTE
   - Necesita actualización para campos `activo` y `fecha_actualizacion`

---

### DAOs a Crear:

#### 1. `CertificacionesProfesionalDAO` / `CertificacionesProfesionalDAOImpl`

**Métodos:**
```java
List<CertificacionProfesional> listarPorProfesional(Integer profesionalId);
Optional<CertificacionProfesional> buscarPorId(Integer id);
boolean guardar(CertificacionProfesional certificacion);
boolean actualizar(CertificacionProfesional certificacion);
boolean eliminar(Integer id);  // Soft delete
```

---

#### 2. `ProyectosPortafolioDAO` / `ProyectosPortafolioDAOImpl`

**Métodos:**
```java
List<ProyectoPortafolio> listarPorProfesional(Integer profesionalId);
Optional<ProyectoPortafolio> buscarPorId(Integer id);
boolean guardar(ProyectoPortafolio proyecto);  // Valida máximo 20
boolean actualizar(ProyectoPortafolio proyecto);
boolean eliminar(Integer id);  // Soft delete
int contarActivosPorProfesional(Integer profesionalId);
```

---

#### 3. `ImagenesProyectoDAO` / `ImagenesProyectoDAOImpl`

**Métodos:**
```java
List<ImagenProyecto> listarPorProyecto(Integer proyectoId);
Optional<ImagenProyecto> buscarPorId(Integer id);
boolean guardar(ImagenProyecto imagen);  // Valida máximo 5
boolean eliminar(Integer id);
int contarPorProyecto(Integer proyectoId);
```

---

#### 4. `AntecedentesProfesionalDAO` / `AntecedentesProfesionalDAOImpl`

**Métodos:**
```java
List<AntecedenteProfesional> listarPorProfesional(Integer profesionalId);
Optional<AntecedenteProfesional> buscarPorTipo(Integer profesionalId, TipoAntecedente tipo);
boolean guardar(AntecedenteProfesional antecedente);
boolean actualizar(AntecedenteProfesional antecedente);
boolean verificar(Integer id);  // Marca como verificado
boolean eliminar(Integer id);  // Soft delete
```

---

#### 5. `RedesSocialesProfesionalDAO` / `RedesSocialesProfesionalDAOImpl`

**Métodos:**
```java
List<RedSocialProfesional> listarPorProfesional(Integer profesionalId);
Optional<RedSocialProfesional> buscarPorId(Integer id);
boolean guardar(RedSocialProfesional red);
boolean actualizar(RedSocialProfesional red);
boolean eliminar(Integer id);  // Soft delete
```

---

### DTOs a Crear:

#### `PerfilProfesionalCompletoDTO`

Consolida TODA la información del perfil profesional:

```java
public class PerfilProfesionalCompletoDTO {
    // Datos básicos del profesional
    private Integer id;
    private Integer usuarioId;
    private String biografiaProfesional;          // ✅ NUEVO
    private Integer aniosExperiencia;
    private String[] idiomas;                     // ✅ NUEVO
    private String licenciasProfesionales;        // ✅ NUEVO
    private Boolean seguroResponsabilidad;        // ✅ NUEVO
    private String[] metodosPago;                 // ✅ NUEVO
    private String politicaCancelacion;           // ✅ NUEVO
    private String fotoPerfil;
    private String fotoPortada;
    private BigDecimal tarifaHora;
    private BigDecimal calificacionPromedio;
    private Integer totalResenas;
    private Boolean verificado;
    private Boolean disponible;
    private BigDecimal puntuacionPlataforma;      // ✅ Calculada con función SQL

    // Listas relacionadas
    private List<CertificacionProfesional> certificaciones;        // ✅ NUEVO
    private List<ProyectoPortafolio> proyectos;                   // ✅ NUEVO
    private List<AntecedenteProfesional> antecedentes;            // ✅ NUEVO
    private List<RedSocialProfesional> redesSociales;             // ✅ NUEVO

    // ... getters y setters
}
```

---

### Servlets a Crear:

#### 1. `CertificacionesProfesionalServlet`

**Endpoint:** `/api/profesional/certificaciones`

**Métodos:**
- `GET` → Listar certificaciones del profesional
- `POST` → Crear nueva certificación
- `PUT` → Actualizar certificación existente
- `DELETE` → Eliminar certificación (soft delete)

---

#### 2. `ProyectosPortafolioServlet`

**Endpoint:** `/api/profesional/proyectos`

**Métodos:**
- `GET` → Listar proyectos del portafolio
- `GET /:id` → Obtener proyecto específico con imágenes
- `POST` → Crear nuevo proyecto (valida máximo 20)
- `PUT` → Actualizar proyecto existente
- `DELETE` → Eliminar proyecto (soft delete)

---

#### 3. `ImagenesProyectoServlet`

**Endpoint:** `/api/profesional/proyectos/:proyectoId/imagenes`

**Métodos:**
- `GET` → Listar imágenes del proyecto
- `POST` → Subir nueva imagen (valida máximo 5)
- `DELETE` → Eliminar imagen

---

#### 4. `AntecedentesProfesionalServlet`

**Endpoint:** `/api/profesional/antecedentes`

**Métodos:**
- `GET` → Listar antecedentes del profesional
- `POST` → Subir nuevo antecedente
- `PUT` → Actualizar antecedente
- `DELETE` → Eliminar antecedente

---

#### 5. `RedesSocialesProfesionalServlet`

**Endpoint:** `/api/profesional/redes-sociales`

**Métodos:**
- `GET` → Listar redes sociales
- `POST` → Añadir red social
- `PUT` → Actualizar red social
- `DELETE` → Eliminar red social

---

#### 6. `PerfilProfesionalServlet` - ACTUALIZADO

**Endpoint:** `/api/profesional/perfil`

**Métodos:**
- `GET` → Obtener perfil completo consolidado (`PerfilProfesionalCompletoDTO`)
- `PUT` → Actualizar datos básicos del perfil

---

## 🎨 Frontend (HTML + JavaScript)

### Modificaciones en `profesional.html`:

#### Secciones a ELIMINAR:
```html
<!-- ❌ ELIMINAR: Campos de ubicación (duplicados) -->
- ubicacion
- distrito
- latitud
- longitud
- radio_servicio
```

#### Secciones a AÑADIR:

##### 1. **Sección: Biografía Profesional**

```html
<section class="biografia-section">
    <h3>📝 Biografía Profesional</h3>
    <textarea id="biografia-profesional"
              rows="5"
              maxlength="1000"
              placeholder="Describe tu experiencia profesional, habilidades y lo que te hace único...">
    </textarea>
    <span class="char-counter">0/1000 caracteres</span>
</section>
```

##### 2. **Sección: Años de Experiencia**

```html
<section class="experiencia-section">
    <h3>⏱️ Experiencia</h3>
    <div class="form-group">
        <label>Años de experiencia:</label>
        <input type="number" id="anios-experiencia" min="0" max="50" value="0">
    </div>
</section>
```

##### 3. **Sección: Certificaciones** (Tabla Dinámica)

```html
<section class="certificaciones-section">
    <h3>🎓 Certificaciones y Estudios</h3>
    <button id="btn-agregar-certificacion" class="btn-add">+ Agregar Certificación</button>

    <table id="tabla-certificaciones">
        <thead>
            <tr>
                <th>Certificación</th>
                <th>Institución</th>
                <th>Fecha</th>
                <th>Acciones</th>
            </tr>
        </thead>
        <tbody>
            <!-- Filas dinámicas -->
        </tbody>
    </table>
</section>
```

##### 4. **Sección: Portafolio de Proyectos** (hasta 20)

```html
<section class="portafolio-section">
    <h3>💼 Portafolio de Proyectos (Máx. 20)</h3>
    <button id="btn-agregar-proyecto" class="btn-add">+ Agregar Proyecto</button>

    <div id="lista-proyectos">
        <!-- Tarjetas dinámicas de proyectos -->
    </div>
</section>

<!-- Modal para agregar/editar proyecto -->
<dialog id="modal-proyecto">
    <form id="form-proyecto">
        <h3>Proyecto</h3>
        <input type="text" name="nombre" placeholder="Nombre del proyecto" required>
        <input type="date" name="fecha" required>
        <textarea name="descripcion" placeholder="Descripción" required></textarea>
        <select name="categoria">
            <!-- Opciones de categorías -->
        </select>

        <!-- Sección de imágenes (hasta 5) -->
        <div class="imagenes-proyecto">
            <h4>Imágenes (Máx. 5)</h4>
            <input type="file" accept="image/*" multiple max="5">
            <div class="previews"></div>
        </div>

        <!-- Calificación cliente (solo lectura) -->
        <div class="calificacion-cliente" *ngIf="proyecto.calificacionCliente">
            <label>Calificación del cliente:</label>
            <div class="estrellas">
                ⭐⭐⭐⭐⭐ <span>9.5/10</span>
            </div>
        </div>

        <button type="submit">Guardar</button>
        <button type="button" class="btn-cancel">Cancelar</button>
    </form>
</dialog>
```

##### 5. **Sección: Redes Sociales**

```html
<section class="redes-sociales-section">
    <h3>🌐 Redes Sociales</h3>

    <div class="red-social">
        <label>Facebook:</label>
        <input type="url" id="facebook" placeholder="https://facebook.com/tu-perfil">
    </div>
    <div class="red-social">
        <label>Instagram:</label>
        <input type="url" id="instagram" placeholder="https://instagram.com/tu-perfil">
    </div>
    <div class="red-social">
        <label>LinkedIn:</label>
        <input type="url" id="linkedin" placeholder="https://linkedin.com/in/tu-perfil">
    </div>
    <div class="red-social">
        <label>YouTube:</label>
        <input type="url" id="youtube" placeholder="https://youtube.com/@tu-canal">
    </div>
    <div class="red-social">
        <label>TikTok:</label>
        <input type="url" id="tiktok" placeholder="https://tiktok.com/@tu-usuario">
    </div>
    <div class="red-social">
        <label>Sitio Web:</label>
        <input type="url" id="website" placeholder="https://tu-sitio.com">
    </div>
</section>
```

##### 6. **Sección: Antecedentes** (Opcional)

```html
<section class="antecedentes-section">
    <h3>🛡️ Antecedentes (Opcional - Mejora tu Puntuación)</h3>
    <p class="info">Sube tus certificados de antecedentes para ganar la confianza de tus clientes.</p>

    <div class="antecedente">
        <label>Antecedentes Policiales:</label>
        <input type="file" id="antecedente-policial" accept=".pdf">
        <span class="status"></span>
    </div>
    <div class="antecedente">
        <label>Antecedentes Penales:</label>
        <input type="file" id="antecedente-penal" accept=".pdf">
        <span class="status"></span>
    </div>
    <div class="antecedente">
        <label>Antecedentes Judiciales:</label>
        <input type="file" id="antecedente-judicial" accept=".pdf">
        <span class="status"></span>
    </div>
</section>
```

##### 7. **Sección: Información Adicional**

```html
<section class="info-adicional-section">
    <h3>ℹ️ Información Adicional</h3>

    <div class="form-group">
        <label>Idiomas:</label>
        <select id="idiomas" multiple>
            <option value="español">Español</option>
            <option value="ingles">Inglés</option>
            <option value="portugues">Portugués</option>
            <option value="quechua">Quechua</option>
            <option value="otro">Otro</option>
        </select>
    </div>

    <div class="form-group">
        <label>Licencias Profesionales:</label>
        <textarea id="licencias" placeholder="Ej: Licencia de electricista N° 12345"></textarea>
    </div>

    <div class="form-group">
        <label>
            <input type="checkbox" id="seguro-responsabilidad">
            Cuento con seguro de responsabilidad civil
        </label>
    </div>

    <div class="form-group">
        <label>Métodos de Pago Aceptados:</label>
        <div class="checkbox-group">
            <label><input type="checkbox" value="efectivo"> Efectivo</label>
            <label><input type="checkbox" value="transferencia"> Transferencia</label>
            <label><input type="checkbox" value="yape"> Yape/Plin</label>
            <label><input type="checkbox" value="tarjeta"> Tarjeta</label>
        </div>
    </div>

    <div class="form-group">
        <label>Política de Cancelación:</label>
        <textarea id="politica-cancelacion"
                  placeholder="Ej: Cancelación gratuita hasta 24 horas antes del servicio"></textarea>
    </div>
</section>
```

---

### Archivo JavaScript: `profesional-api.js` (NUEVO)

```javascript
/**
 * API Client para Perfil Profesional
 * Maneja todas las peticiones relacionadas con el perfil profesional completo
 *
 * Creado: 2025-11-15
 */

class ProfesionalAPI {
    constructor() {
        this.baseURL = '/ContactoProfesionalesWeb/api/profesional';
    }

    // ==================== PERFIL ====================

    /**
     * Obtiene el perfil completo del profesional
     */
    async obtenerPerfilCompleto() {
        const response = await fetch(`${this.baseURL}/perfil`, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });
        return await response.json();
    }

    /**
     * Actualiza datos básicos del perfil
     */
    async actualizarPerfil(datos) {
        const response = await fetch(`${this.baseURL}/perfil`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify(datos)
        });
        return await response.json();
    }

    // ==================== CERTIFICACIONES ====================

    /**
     * Lista certificaciones del profesional
     */
    async listarCertificaciones() {
        const response = await fetch(`${this.baseURL}/certificaciones`, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });
        return await response.json();
    }

    /**
     * Crea una nueva certificación
     */
    async crearCertificacion(certificacion) {
        const response = await fetch(`${this.baseURL}/certificaciones`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify(certificacion)
        });
        return await response.json();
    }

    // ==================== PROYECTOS ====================

    /**
     * Lista proyectos del portafolio
     */
    async listarProyectos() {
        const response = await fetch(`${this.baseURL}/proyectos`, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });
        return await response.json();
    }

    /**
     * Crea un nuevo proyecto (valida máximo 20)
     */
    async crearProyecto(proyecto) {
        const response = await fetch(`${this.baseURL}/proyectos`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify(proyecto)
        });
        return await response.json();
    }

    // ==================== REDES SOCIALES ====================

    /**
     * Lista redes sociales del profesional
     */
    async listarRedesSociales() {
        const response = await fetch(`${this.baseURL}/redes-sociales`, {
            headers: { 'Authorization': `Bearer ${getToken()}` }
        });
        return await response.json();
    }

    /**
     * Guarda/actualiza redes sociales
     */
    async guardarRedesSociales(redes) {
        const response = await fetch(`${this.baseURL}/redes-sociales`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${getToken()}`
            },
            body: JSON.stringify(redes)
        });
        return await response.json();
    }

    // ==================== ANTECEDENTES ====================

    /**
     * Sube un documento de antecedentes
     */
    async subirAntecedente(tipo, archivo) {
        const formData = new FormData();
        formData.append('tipo', tipo);
        formData.append('documento', archivo);

        const response = await fetch(`${this.baseURL}/antecedentes`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${getToken()}` },
            body: formData
        });
        return await response.json();
    }
}

// Helpers
function getToken() {
    return localStorage.getItem('token') || sessionStorage.getItem('token');
}
```

---

## 📝 Pasos de Implementación

### Fase 1: Base de Datos ✅

1. ✅ Ejecutar migración `V006__refactorizar_perfil_profesional.sql`
   ```bash
   psql -U postgres -d contacto_profesionales_db -f "src/main/resources/db/migration/V006__refactorizar_perfil_profesional.sql"
   ```

### Fase 2: Backend (Java) 🚧

2. ✅ Crear Models (completado):
   - `CertificacionProfesional.java`
   - `ProyectoPortafolio.java`
   - `ImagenProyecto.java`
   - `AntecedenteProfesional.java`
   - `RedSocialProfesional.java` (actualizar)

3. ⏳ Crear DAOs (pendiente):
   - `CertificacionesProfesionalDAO` + Impl
   - `ProyectosPortafolioDAO` + Impl
   - `ImagenesProyectoDAO` + Impl
   - `AntecedentesProfesionalDAO` + Impl
   - `RedesSocialesProfesionalDAO` + Impl

4. ⏳ Crear DTOs (pendiente):
   - `PerfilProfesionalCompletoDTO`
   - Otros DTOs necesarios

5. ⏳ Crear Servlets (pendiente):
   - `CertificacionesProfesionalServlet`
   - `ProyectosPortafolioServlet`
   - `ImagenesProyectoServlet`
   - `AntecedentesProfesionalServlet`
   - `RedesSocialesProfesionalServlet`
   - Actualizar `PerfilProfesionalServlet`

### Fase 3: Frontend (HTML/JS) ⏳

6. ⏳ Refactorizar `profesional.html` (pendiente):
   - Eliminar campos duplicados
   - Añadir nuevas secciones
   - Implementar validaciones

7. ⏳ Crear `profesional-api.js` (pendiente):
   - API client completo
   - Funciones para todas las operaciones

8. ⏳ Crear componentes auxiliares (pendiente):
   - Modal para proyectos
   - Tabla dinámica para certificaciones
   - Uploader de imágenes
   - Uploader de documentos

### Fase 4: Testing y Documentación ⏳

9. ⏳ Testing manual (pendiente)
10. ⏳ Documentación de API (pendiente)

---

## 🎯 Priorización de Tareas

Dada la extensión del trabajo, se recomienda implementar en este orden:

### Prioridad ALTA (Inmediata):
1. ✅ Migración SQL V006
2. ⏳ DAO + Servlet de Certificaciones
3. ⏳ DAO + Servlet de Proyectos Portafolio
4. ⏳ Frontend básico (biografía + certificaciones + proyectos)

### Prioridad MEDIA:
5. ⏳ DAO + Servlet de Redes Sociales
6. ⏳ DAO + Servlet de Antecedentes
7. ⏳ Sistema de imágenes para proyectos
8. ⏳ Frontend completo con todas las secciones

### Prioridad BAJA:
9. ⏳ Mejoras UI/UX
10. ⏳ Validaciones avanzadas
11. ⏳ Optimizaciones de rendimiento

---

## 📊 Estado Actual

| Componente | Estado | Progreso |
|------------|--------|----------|
| Migración SQL | ✅ Completado | 100% |
| Models | ✅ Completado | 100% |
| DAOs | ⏳ Pendiente | 0% |
| DTOs | ⏳ Pendiente | 0% |
| Servlets | ⏳ Pendiente | 0% |
| Frontend HTML | ⏳ Pendiente | 0% |
| Frontend JS | ⏳ Pendiente | 0% |
| Testing | ⏳ Pendiente | 0% |
| Documentación | 🚧 En Progreso | 50% |

---

## 🚀 Próximos Pasos Sugeridos

Dado el alcance extenso, recomiendo continuar en este orden:

1. **Primero**: Implementar DAOs para las nuevas tablas
2. **Segundo**: Crear los Servlets correspondientes
3. **Tercero**: Refactorizar el HTML con las nuevas secciones
4. **Cuarto**: Implementar el JavaScript para interactividad
5. **Quinto**: Testing manual completo

**¿Deseas que continúe con alguna fase específica?**

---

**Documentado por:** Claude Code
**Fecha:** 2025-11-15
**Versión:** 1.0
