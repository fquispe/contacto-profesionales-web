# Refactoring de Estructura de Base de Datos - Contacto Profesionales

## 1. Resumen del Refactoring

Este documento contiene todos los scripts SQL necesarios para implementar la nueva estructura de base de datos que:

1. **Separa responsabilidades** entre autenticación (`users`) y datos personales (`usuarios`)
2. **Implementa gestión de roles** (Cliente, Profesional, o Ambos)
3. **Incorpora ubicación geográfica** estructurada (departamento, provincia, distrito)
4. **Añade especialidades y redes sociales** para profesionales
5. **Mejora la integridad referencial** y normalización de datos

---

## 2. Orden de Ejecución

**IMPORTANTE**: Ejecutar los scripts en el siguiente orden:

1. **Paso 1**: Creación de tablas geográficas
2. **Paso 2**: Creación de tabla `usuarios` (nueva tabla central)
3. **Paso 3**: Modificación de tabla `users` (solo autenticación)
4. **Paso 4**: Modificación de tabla `clientes`
5. **Paso 5**: Modificación de tabla `profesionales`
6. **Paso 6**: Creación de tablas complementarias (especialidades, redes sociales)
7. **Paso 7**: Modificación de tabla `direcciones_cliente`
8. **Paso 8**: Migración de datos existentes
9. **Paso 9**: Creación de índices y constraints adicionales
10. **Paso 10**: Vistas útiles para consultas

---

## 3. Scripts SQL

### PASO 1: Creación de Tablas Geográficas

```sql
-- =============================================
-- Tabla: departamentos
-- Descripción: Departamentos del Perú
-- =============================================
CREATE TABLE IF NOT EXISTS departamentos (
    id SERIAL PRIMARY KEY,
    codigo VARCHAR(2) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    capital VARCHAR(100),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserción de departamentos del Perú
INSERT INTO departamentos (codigo, nombre, capital) VALUES
('01', 'Amazonas', 'Chachapoyas'),
('02', 'Áncash', 'Huaraz'),
('03', 'Apurímac', 'Abancay'),
('04', 'Arequipa', 'Arequipa'),
('05', 'Ayacucho', 'Ayacucho'),
('06', 'Cajamarca', 'Cajamarca'),
('07', 'Callao', 'Callao'),
('08', 'Cusco', 'Cusco'),
('09', 'Huancavelica', 'Huancavelica'),
('10', 'Huánuco', 'Huánuco'),
('11', 'Ica', 'Ica'),
('12', 'Junín', 'Huancayo'),
('13', 'La Libertad', 'Trujillo'),
('14', 'Lambayeque', 'Chiclayo'),
('15', 'Lima', 'Lima'),
('16', 'Loreto', 'Iquitos'),
('17', 'Madre de Dios', 'Puerto Maldonado'),
('18', 'Moquegua', 'Moquegua'),
('19', 'Pasco', 'Cerro de Pasco'),
('20', 'Piura', 'Piura'),
('21', 'Puno', 'Puno'),
('22', 'San Martín', 'Moyobamba'),
('23', 'Tacna', 'Tacna'),
('24', 'Tumbes', 'Tumbes'),
('25', 'Ucayali', 'Pucallpa');

-- =============================================
-- Tabla: provincias
-- Descripción: Provincias del Perú
-- =============================================
CREATE TABLE IF NOT EXISTS provincias (
    id SERIAL PRIMARY KEY,
    departamento_id INTEGER NOT NULL REFERENCES departamentos(id) ON DELETE CASCADE,
    codigo VARCHAR(4) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserción de algunas provincias principales de Lima
INSERT INTO provincias (departamento_id, codigo, nombre) VALUES
(15, '1501', 'Lima'),
(15, '1502', 'Barranca'),
(15, '1503', 'Cajatambo'),
(15, '1504', 'Canta'),
(15, '1505', 'Cañete'),
(15, '1506', 'Huaral'),
(15, '1507', 'Huarochirí'),
(15, '1508', 'Huaura'),
(15, '1509', 'Oyón'),
(15, '1510', 'Yauyos');

-- =============================================
-- Tabla: distritos
-- Descripción: Distritos del Perú
-- =============================================
CREATE TABLE IF NOT EXISTS distritos (
    id SERIAL PRIMARY KEY,
    provincia_id INTEGER NOT NULL REFERENCES provincias(id) ON DELETE CASCADE,
    codigo VARCHAR(6) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserción de distritos principales de Lima Metropolitana
INSERT INTO distritos (provincia_id, codigo, nombre) VALUES
-- Provincia de Lima (1501)
(1, '150101', 'Lima'),
(1, '150102', 'Ancón'),
(1, '150103', 'Ate'),
(1, '150104', 'Barranco'),
(1, '150105', 'Breña'),
(1, '150106', 'Carabayllo'),
(1, '150107', 'Chaclacayo'),
(1, '150108', 'Chorrillos'),
(1, '150109', 'Cieneguilla'),
(1, '150110', 'Comas'),
(1, '150111', 'El Agustino'),
(1, '150112', 'Independencia'),
(1, '150113', 'Jesús María'),
(1, '150114', 'La Molina'),
(1, '150115', 'La Victoria'),
(1, '150116', 'Lince'),
(1, '150117', 'Los Olivos'),
(1, '150118', 'Lurigancho'),
(1, '150119', 'Lurín'),
(1, '150120', 'Magdalena del Mar'),
(1, '150121', 'Pueblo Libre'),
(1, '150122', 'Miraflores'),
(1, '150123', 'Pachacámac'),
(1, '150124', 'Pucusana'),
(1, '150125', 'Puente Piedra'),
(1, '150126', 'Punta Hermosa'),
(1, '150127', 'Punta Negra'),
(1, '150128', 'Rímac'),
(1, '150129', 'San Bartolo'),
(1, '150130', 'San Borja'),
(1, '150131', 'San Isidro'),
(1, '150132', 'San Juan de Lurigancho'),
(1, '150133', 'San Juan de Miraflores'),
(1, '150134', 'San Luis'),
(1, '150135', 'San Martín de Porres'),
(1, '150136', 'San Miguel'),
(1, '150137', 'Santa Anita'),
(1, '150138', 'Santa María del Mar'),
(1, '150139', 'Santa Rosa'),
(1, '150140', 'Santiago de Surco'),
(1, '150141', 'Surquillo'),
(1, '150142', 'Villa El Salvador'),
(1, '150143', 'Villa María del Triunfo');

-- NOTA: Agregar más provincias y distritos según necesidad
```

---

### PASO 2: Creación de Tabla `usuarios` (Datos Personales)

```sql
-- =============================================
-- Tabla: usuarios
-- Descripción: Tabla central con datos personales de todos los usuarios
--              Puede tener rol de CLIENTE, PROFESIONAL o AMBOS
-- =============================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,

    -- Datos personales
    nombre_completo VARCHAR(200) NOT NULL,
    tipo_documento VARCHAR(20) DEFAULT 'DNI', -- DNI, CE, RUC, PASAPORTE
    numero_documento VARCHAR(20) UNIQUE,
    fecha_nacimiento DATE,
    genero VARCHAR(20), -- MASCULINO, FEMENINO, OTRO, PREFIERO_NO_DECIR

    -- Contacto
    telefono VARCHAR(20),
    telefono_alternativo VARCHAR(20),

    -- Ubicación personal
    departamento_id INTEGER REFERENCES departamentos(id),
    provincia_id INTEGER REFERENCES provincias(id),
    distrito_id INTEGER REFERENCES distritos(id),
    direccion VARCHAR(255),
    referencia_direccion VARCHAR(255),

    -- Gestión de roles
    tipo_rol VARCHAR(20) NOT NULL DEFAULT 'CLIENTE', -- CLIENTE, PROFESIONAL, AMBOS
    es_cliente BOOLEAN DEFAULT false,
    es_profesional BOOLEAN DEFAULT false,

    -- Foto de perfil
    foto_perfil_url VARCHAR(500),

    -- Auditoría
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP,
    activo BOOLEAN DEFAULT true,

    -- Constraints
    CONSTRAINT chk_tipo_documento CHECK (tipo_documento IN ('DNI', 'CE', 'RUC', 'PASAPORTE')),
    CONSTRAINT chk_genero CHECK (genero IN ('MASCULINO', 'FEMENINO', 'OTRO', 'PREFIERO_NO_DECIR')),
    CONSTRAINT chk_tipo_rol CHECK (tipo_rol IN ('CLIENTE', 'PROFESIONAL', 'AMBOS')),
    CONSTRAINT chk_roles_consistentes CHECK (
        (tipo_rol = 'CLIENTE' AND es_cliente = true AND es_profesional = false) OR
        (tipo_rol = 'PROFESIONAL' AND es_cliente = false AND es_profesional = true) OR
        (tipo_rol = 'AMBOS' AND es_cliente = true AND es_profesional = true)
    )
);

-- Índices para búsquedas rápidas
CREATE INDEX idx_usuarios_numero_documento ON usuarios(numero_documento);
CREATE INDEX idx_usuarios_tipo_rol ON usuarios(tipo_rol);
CREATE INDEX idx_usuarios_activo ON usuarios(activo);
CREATE INDEX idx_usuarios_ubicacion ON usuarios(departamento_id, provincia_id, distrito_id);
```

---

### PASO 3: Modificación de Tabla `users` (Solo Autenticación)

```sql
-- =============================================
-- Modificación: users
-- Descripción: Ahora solo maneja autenticación y vinculación con 'usuarios'
-- =============================================

-- Primero, agregar columna para vincular con 'usuarios'
ALTER TABLE users
ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;

-- Eliminar columnas que ahora están en 'usuarios'
-- NOTA: Ejecutar solo DESPUÉS de migrar los datos (ver PASO 8)
-- ALTER TABLE users DROP COLUMN IF EXISTS nombre;
-- ALTER TABLE users DROP COLUMN IF EXISTS telefono;

-- Agregar columnas faltantes para autenticación completa
ALTER TABLE users
ADD COLUMN IF NOT EXISTS username VARCHAR(50) UNIQUE,
ADD COLUMN IF NOT EXISTS rol_sistema VARCHAR(20) DEFAULT 'USER', -- ADMIN, USER, MODERADOR
ADD COLUMN IF NOT EXISTS intentos_fallidos INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS bloqueado BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS fecha_bloqueo TIMESTAMP,
ADD COLUMN IF NOT EXISTS token_recuperacion VARCHAR(255),
ADD COLUMN IF NOT EXISTS fecha_expiracion_token TIMESTAMP,
ADD COLUMN IF NOT EXISTS requiere_cambio_password BOOLEAN DEFAULT false;

-- Agregar constraint para rol del sistema
ALTER TABLE users
ADD CONSTRAINT chk_rol_sistema CHECK (rol_sistema IN ('ADMIN', 'USER', 'MODERADOR'));

-- Crear índices
CREATE INDEX IF NOT EXISTS idx_users_usuario_id ON users(usuario_id);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_activo ON users(activo);
CREATE INDEX IF NOT EXISTS idx_users_bloqueado ON users(bloqueado);
```

---

### PASO 4: Modificación de Tabla `clientes`

```sql
-- =============================================
-- Modificación: clientes
-- Descripción: Vincular con tabla 'usuarios' y mantener solo datos específicos de cliente
-- =============================================

-- Agregar vinculación con 'usuarios'
ALTER TABLE clientes
ADD COLUMN IF NOT EXISTS usuario_id BIGINT REFERENCES usuarios(id) ON DELETE CASCADE;

-- Eliminar columnas que ahora están en 'usuarios'
-- NOTA: Ejecutar solo DESPUÉS de migrar los datos (ver PASO 8)
-- ALTER TABLE clientes DROP COLUMN IF EXISTS nombre_completo;
-- ALTER TABLE clientes DROP COLUMN IF EXISTS email;
-- ALTER TABLE clientes DROP COLUMN IF EXISTS telefono;
-- ALTER TABLE clientes DROP COLUMN IF EXISTS foto_perfil_url;

-- Agregar campos adicionales si no existen
ALTER TABLE clientes
ADD COLUMN IF NOT EXISTS cliente_verificado BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS metodo_pago_preferido VARCHAR(50), -- EFECTIVO, TARJETA, YAPE, PLIN, TRANSFERENCIA
ADD COLUMN IF NOT EXISTS comentarios_adicionales TEXT;

-- Crear índice
CREATE INDEX IF NOT EXISTS idx_clientes_usuario_id ON clientes(usuario_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_clientes_usuario_unique ON clientes(usuario_id);
```

---

### PASO 5: Modificación de Tabla `profesionales`

```sql
-- =============================================
-- Modificación: profesionales
-- Descripción: Vincular con tabla 'usuarios' y separar especialidades
-- =============================================

-- Agregar vinculación con 'usuarios' (ya existe usuario_id, solo agregamos índice único)
CREATE UNIQUE INDEX IF NOT EXISTS idx_profesionales_usuario_unique ON profesionales(usuario_id);

-- Eliminar columnas que ya no se usarán en esta tabla
-- NOTA: La especialidad ahora se maneja en tabla separada
-- ALTER TABLE profesionales DROP COLUMN IF EXISTS especialidad;

-- Agregar campos para compatibilidad temporal (migración)
ALTER TABLE profesionales
ADD COLUMN IF NOT EXISTS especialidad_principal_id INTEGER,
ADD COLUMN IF NOT EXISTS anios_experiencia INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS documento_identidad VARCHAR(20),
ADD COLUMN IF NOT EXISTS verificacion_identidad BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS certificado_antecedentes BOOLEAN DEFAULT false,
ADD COLUMN IF NOT EXISTS puntuacion_plataforma DECIMAL(3,2) DEFAULT 0.0;

-- Modificar campos existentes
ALTER TABLE profesionales
ALTER COLUMN especialidad TYPE VARCHAR(100);
```

---

### PASO 6: Creación de Tablas Complementarias

```sql
-- =============================================
-- Tabla: categorias_servicio
-- Descripción: Categorías de servicios profesionales
-- =============================================
CREATE TABLE IF NOT EXISTS categorias_servicio (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion TEXT,
    icono VARCHAR(100),
    color VARCHAR(20),
    activo BOOLEAN DEFAULT true,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Inserción de categorías comunes
INSERT INTO categorias_servicio (nombre, descripcion, icono) VALUES
('Plomería', 'Instalación y reparación de sistemas de agua y desagüe', 'plumbing'),
('Electricidad', 'Instalación eléctrica, reparaciones y mantenimiento', 'electrical'),
('Carpintería', 'Fabricación y reparación de muebles de madera', 'carpentry'),
('Pintura', 'Pintura de interiores y exteriores', 'paint'),
('Limpieza', 'Servicios de limpieza residencial y comercial', 'cleaning'),
('Jardinería', 'Mantenimiento de jardines y áreas verdes', 'gardening'),
('Gasfitería', 'Instalación y reparación de gas', 'gas'),
('Cerrajería', 'Apertura de puertas y fabricación de llaves', 'locksmith'),
('Albañilería', 'Construcción y reparación de estructuras', 'construction'),
('Tecnología', 'Reparación de computadoras y dispositivos', 'tech'),
('Mecánica Automotriz', 'Reparación y mantenimiento de vehículos', 'automotive'),
('Aire Acondicionado', 'Instalación y mantenimiento de sistemas de climatización', 'hvac')
ON CONFLICT (nombre) DO NOTHING;

-- =============================================
-- Tabla: especialidades_profesional
-- Descripción: Un profesional puede tener hasta 3 especialidades
-- =============================================
CREATE TABLE IF NOT EXISTS especialidades_profesional (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL REFERENCES profesionales(id) ON DELETE CASCADE,
    categoria_id INTEGER NOT NULL REFERENCES categorias_servicio(id),
    es_principal BOOLEAN DEFAULT false,
    anios_experiencia INTEGER DEFAULT 0,
    descripcion TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Un profesional no puede tener la misma especialidad dos veces
    CONSTRAINT uk_profesional_categoria UNIQUE(profesional_id, categoria_id)
);

-- Trigger para validar que solo haya máximo 3 especialidades
CREATE OR REPLACE FUNCTION validar_max_especialidades()
RETURNS TRIGGER AS $$
BEGIN
    IF (SELECT COUNT(*) FROM especialidades_profesional WHERE profesional_id = NEW.profesional_id) >= 3 THEN
        RAISE EXCEPTION 'Un profesional solo puede tener máximo 3 especialidades';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_max_especialidades
BEFORE INSERT ON especialidades_profesional
FOR EACH ROW
EXECUTE FUNCTION validar_max_especialidades();

-- Trigger para validar que solo haya una especialidad principal
CREATE OR REPLACE FUNCTION validar_especialidad_principal()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.es_principal = true THEN
        -- Quitar el flag de principal de las demás
        UPDATE especialidades_profesional
        SET es_principal = false
        WHERE profesional_id = NEW.profesional_id AND id != NEW.id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_validar_especialidad_principal
BEFORE INSERT OR UPDATE ON especialidades_profesional
FOR EACH ROW
EXECUTE FUNCTION validar_especialidad_principal();

-- Índices
CREATE INDEX idx_especialidades_profesional_id ON especialidades_profesional(profesional_id);
CREATE INDEX idx_especialidades_categoria_id ON especialidades_profesional(categoria_id);
CREATE INDEX idx_especialidades_principal ON especialidades_profesional(es_principal);

-- =============================================
-- Tabla: redes_sociales_profesional
-- Descripción: Redes sociales del profesional
-- =============================================
CREATE TABLE IF NOT EXISTS redes_sociales_profesional (
    id SERIAL PRIMARY KEY,
    profesional_id INTEGER NOT NULL REFERENCES profesionales(id) ON DELETE CASCADE,
    tipo_red VARCHAR(50) NOT NULL, -- FACEBOOK, INSTAGRAM, LINKEDIN, TWITTER, TIKTOK, WHATSAPP, WEBSITE
    url VARCHAR(500) NOT NULL,
    verificada BOOLEAN DEFAULT false,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Un profesional no puede tener la misma red social dos veces
    CONSTRAINT uk_profesional_red UNIQUE(profesional_id, tipo_red),
    CONSTRAINT chk_tipo_red CHECK (tipo_red IN ('FACEBOOK', 'INSTAGRAM', 'LINKEDIN', 'TWITTER', 'TIKTOK', 'WHATSAPP', 'WEBSITE', 'YOUTUBE'))
);

-- Índice
CREATE INDEX idx_redes_sociales_profesional_id ON redes_sociales_profesional(profesional_id);
```

---

### PASO 7: Modificación de Tabla `direcciones_cliente`

```sql
-- =============================================
-- Modificación: direcciones_cliente
-- Descripción: Agregar ubicación geográfica estructurada
-- =============================================

-- Agregar campos de ubicación geográfica
ALTER TABLE direcciones_cliente
ADD COLUMN IF NOT EXISTS departamento_id INTEGER REFERENCES departamentos(id),
ADD COLUMN IF NOT EXISTS provincia_id INTEGER REFERENCES provincias(id),
ADD COLUMN IF NOT EXISTS distrito_id INTEGER REFERENCES distritos(id),
ADD COLUMN IF NOT EXISTS codigo_postal VARCHAR(10),
ADD COLUMN IF NOT EXISTS latitud DECIMAL(10, 8),
ADD COLUMN IF NOT EXISTS longitud DECIMAL(11, 8);

-- Modificar el campo distrito para que sea solo un varchar (no la ubicación real)
-- La ubicación real está en distrito_id

-- Crear índices
CREATE INDEX IF NOT EXISTS idx_direcciones_departamento ON direcciones_cliente(departamento_id);
CREATE INDEX IF NOT EXISTS idx_direcciones_provincia ON direcciones_cliente(provincia_id);
CREATE INDEX IF NOT EXISTS idx_direcciones_distrito ON direcciones_cliente(distrito_id);
CREATE INDEX IF NOT EXISTS idx_direcciones_principal ON direcciones_cliente(cliente_id, es_principal);
```

---

### PASO 8: Scripts de Migración de Datos

```sql
-- =============================================
-- MIGRACIÓN DE DATOS EXISTENTES
-- =============================================

-- IMPORTANTE: Hacer backup de la base de datos antes de ejecutar
-- pg_dump -U postgres -d contacto_profesionales > backup_antes_migracion.sql

-- ============================================
-- MIGRACIÓN 1: De 'users' a 'usuarios'
-- ============================================
INSERT INTO usuarios (
    nombre_completo,
    telefono,
    foto_perfil_url,
    tipo_rol,
    es_cliente,
    es_profesional,
    fecha_creacion,
    activo
)
SELECT
    COALESCE(u.nombre, 'Usuario Sin Nombre') as nombre_completo,
    u.telefono,
    NULL as foto_perfil_url,
    CASE
        WHEN EXISTS (SELECT 1 FROM clientes c WHERE c.email = u.email)
             AND EXISTS (SELECT 1 FROM profesionales p WHERE p.usuario_id = u.id)
        THEN 'AMBOS'
        WHEN EXISTS (SELECT 1 FROM clientes c WHERE c.email = u.email)
        THEN 'CLIENTE'
        WHEN EXISTS (SELECT 1 FROM profesionales p WHERE p.usuario_id = u.id)
        THEN 'PROFESIONAL'
        ELSE 'CLIENTE'
    END as tipo_rol,
    EXISTS (SELECT 1 FROM clientes c WHERE c.email = u.email) as es_cliente,
    EXISTS (SELECT 1 FROM profesionales p WHERE p.usuario_id = u.id) as es_profesional,
    u.fecha_registro,
    u.activo
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM usuarios usu WHERE usu.telefono = u.telefono
);

-- ============================================
-- MIGRACIÓN 2: Vincular 'users' con 'usuarios'
-- ============================================
UPDATE users u
SET usuario_id = (
    SELECT usu.id
    FROM usuarios usu
    WHERE usu.telefono = u.telefono
    LIMIT 1
)
WHERE usuario_id IS NULL;

-- ============================================
-- MIGRACIÓN 3: Vincular 'clientes' con 'usuarios'
-- ============================================
UPDATE clientes c
SET usuario_id = (
    SELECT u.usuario_id
    FROM users u
    WHERE u.email = c.email
    LIMIT 1
)
WHERE usuario_id IS NULL;

-- ============================================
-- MIGRACIÓN 4: Copiar datos de foto de clientes a usuarios
-- ============================================
UPDATE usuarios usu
SET foto_perfil_url = c.foto_perfil_url
FROM clientes c
WHERE c.usuario_id = usu.id
AND c.foto_perfil_url IS NOT NULL
AND usu.foto_perfil_url IS NULL;

-- ============================================
-- MIGRACIÓN 5: Migrar especialidad principal de profesionales
-- ============================================
-- Primero, insertar especialidades desde la tabla profesionales si tienen especialidad definida
INSERT INTO especialidades_profesional (profesional_id, categoria_id, es_principal, anios_experiencia)
SELECT
    p.id,
    cs.id,
    true, -- Es la especialidad principal
    p.experiencia::INTEGER -- Convertir años de experiencia si existe
FROM profesionales p
INNER JOIN categorias_servicio cs ON LOWER(cs.nombre) = LOWER(p.especialidad)
WHERE p.especialidad IS NOT NULL
AND p.especialidad != ''
AND NOT EXISTS (
    SELECT 1 FROM especialidades_profesional ep WHERE ep.profesional_id = p.id
);

-- Si no encuentra coincidencia exacta, crear una categoría "Otro" temporal
INSERT INTO categorias_servicio (nombre, descripcion)
VALUES ('Otros Servicios', 'Servicios diversos no categorizados')
ON CONFLICT (nombre) DO NOTHING;

-- Asignar "Otros Servicios" a profesionales sin especialidad
INSERT INTO especialidades_profesional (profesional_id, categoria_id, es_principal)
SELECT
    p.id,
    (SELECT id FROM categorias_servicio WHERE nombre = 'Otros Servicios'),
    true
FROM profesionales p
WHERE NOT EXISTS (
    SELECT 1 FROM especialidades_profesional ep WHERE ep.profesional_id = p.id
);
```

---

### PASO 9: Índices y Constraints Adicionales

```sql
-- =============================================
-- ÍNDICES PARA MEJORAR RENDIMIENTO
-- =============================================

-- Índices en 'usuarios'
CREATE INDEX IF NOT EXISTS idx_usuarios_nombre ON usuarios USING gin(to_tsvector('spanish', nombre_completo));
CREATE INDEX IF NOT EXISTS idx_usuarios_fecha_creacion ON usuarios(fecha_creacion DESC);

-- Índices en 'users'
CREATE INDEX IF NOT EXISTS idx_users_fecha_registro ON users(fecha_registro DESC);
CREATE INDEX IF NOT EXISTS idx_users_ultimo_acceso ON users(ultimo_acceso DESC);

-- Índices en 'clientes'
CREATE INDEX IF NOT EXISTS idx_clientes_fecha_registro ON clientes(fecha_registro DESC);
CREATE INDEX IF NOT EXISTS idx_clientes_activo ON clientes(activo);

-- Índices en 'profesionales'
CREATE INDEX IF NOT EXISTS idx_profesionales_calificacion ON profesionales(calificacion_promedio DESC);
CREATE INDEX IF NOT EXISTS idx_profesionales_disponible ON profesionales(disponible);
CREATE INDEX IF NOT EXISTS idx_profesionales_verificado ON profesionales(verificado);
CREATE INDEX IF NOT EXISTS idx_profesionales_distrito ON profesionales(distrito);

-- Índices en 'solicitudes_servicio'
CREATE INDEX IF NOT EXISTS idx_solicitudes_cliente ON solicitudes_servicio(cliente_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_profesional ON solicitudes_servicio(profesional_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_estado ON solicitudes_servicio(estado);
CREATE INDEX IF NOT EXISTS idx_solicitudes_fecha ON solicitudes_servicio(fecha_solicitud DESC);

-- =============================================
-- CONSTRAINTS ADICIONALES
-- =============================================

-- Asegurar que usuario_id sea NOT NULL en clientes (después de migración)
-- ALTER TABLE clientes ALTER COLUMN usuario_id SET NOT NULL;

-- Asegurar unicidad de usuario_id en profesionales y clientes
-- Ya se crearon arriba como índices únicos
```

---

### PASO 10: Vistas Útiles para Consultas

```sql
-- =============================================
-- VISTA: vista_usuarios_completa
-- Descripción: Combina datos de usuarios, users, clientes y profesionales
-- =============================================
CREATE OR REPLACE VIEW vista_usuarios_completa AS
SELECT
    usu.id as usuario_id,
    usu.nombre_completo,
    usu.tipo_documento,
    usu.numero_documento,
    usu.telefono,
    usu.tipo_rol,
    usu.es_cliente,
    usu.es_profesional,
    usu.foto_perfil_url,

    -- Datos de autenticación
    u.id as user_id,
    u.email,
    u.username,
    u.activo as cuenta_activa,
    u.bloqueado,
    u.rol_sistema,

    -- Ubicación
    d.nombre as departamento,
    p.nombre as provincia,
    dis.nombre as distrito,
    usu.direccion,

    -- Datos de cliente (si aplica)
    c.id as cliente_id,
    c.categorias_favoritas,
    c.notificaciones_email,

    -- Datos de profesional (si aplica)
    prof.id as profesional_id,
    prof.calificacion_promedio,
    prof.tarifa_hora,
    prof.disponible,
    prof.verificado,

    -- Fechas
    usu.fecha_creacion,
    u.ultimo_acceso
FROM usuarios usu
LEFT JOIN users u ON u.usuario_id = usu.id
LEFT JOIN clientes c ON c.usuario_id = usu.id
LEFT JOIN profesionales prof ON prof.usuario_id = u.id
LEFT JOIN departamentos d ON d.id = usu.departamento_id
LEFT JOIN provincias p ON p.id = usu.provincia_id
LEFT JOIN distritos dis ON dis.id = usu.distrito_id;

-- =============================================
-- VISTA: vista_profesionales_completa
-- Descripción: Profesionales con sus especialidades y redes sociales
-- =============================================
CREATE OR REPLACE VIEW vista_profesionales_completa AS
SELECT
    p.id as profesional_id,
    usu.nombre_completo,
    usu.telefono,
    u.email,
    p.descripcion,
    p.experiencia,
    p.tarifa_hora,
    p.calificacion_promedio,
    p.total_resenas,
    p.distrito,
    p.disponible,
    p.verificado,

    -- Especialidad principal
    (
        SELECT cs.nombre
        FROM especialidades_profesional ep
        INNER JOIN categorias_servicio cs ON cs.id = ep.categoria_id
        WHERE ep.profesional_id = p.id AND ep.es_principal = true
        LIMIT 1
    ) as especialidad_principal,

    -- Todas las especialidades como array
    ARRAY(
        SELECT cs.nombre
        FROM especialidades_profesional ep
        INNER JOIN categorias_servicio cs ON cs.id = ep.categoria_id
        WHERE ep.profesional_id = p.id
        ORDER BY ep.es_principal DESC
    ) as especialidades,

    -- Redes sociales como JSON
    (
        SELECT json_object_agg(rs.tipo_red, rs.url)
        FROM redes_sociales_profesional rs
        WHERE rs.profesional_id = p.id
    ) as redes_sociales,

    p.fecha_registro,
    p.activo
FROM profesionales p
INNER JOIN users u ON u.id = p.usuario_id
INNER JOIN usuarios usu ON usu.id = u.usuario_id;

-- =============================================
-- VISTA: vista_solicitudes_completa
-- Descripción: Solicitudes con datos de cliente y profesional
-- =============================================
CREATE OR REPLACE VIEW vista_solicitudes_completa AS
SELECT
    s.id as solicitud_id,
    s.descripcion,
    s.presupuesto_estimado,
    s.estado,
    s.urgencia,

    -- Cliente
    s.cliente_id,
    usu_cli.nombre_completo as cliente_nombre,
    u_cli.email as cliente_email,
    usu_cli.telefono as cliente_telefono,

    -- Profesional
    s.profesional_id,
    usu_prof.nombre_completo as profesional_nombre,
    u_prof.email as profesional_email,
    prof.calificacion_promedio as profesional_calificacion,

    -- Ubicación del servicio
    s.direccion,
    s.distrito,
    s.referencia,

    -- Fechas
    s.fecha_solicitud,
    s.fecha_servicio,
    s.fecha_respuesta
FROM solicitudes_servicio s
LEFT JOIN clientes c ON c.id = s.cliente_id
LEFT JOIN usuarios usu_cli ON usu_cli.id = c.usuario_id
LEFT JOIN users u_cli ON u_cli.usuario_id = usu_cli.id
LEFT JOIN profesionales prof ON prof.id = s.profesional_id
LEFT JOIN users u_prof ON u_prof.id = prof.usuario_id
LEFT JOIN usuarios usu_prof ON usu_prof.id = u_prof.usuario_id;
```

---

## 4. Verificación Post-Migración

Ejecutar los siguientes queries para verificar la integridad:

```sql
-- Verificar que todos los users tienen usuario_id
SELECT COUNT(*) as users_sin_usuario_id
FROM users WHERE usuario_id IS NULL;
-- Resultado esperado: 0

-- Verificar que todos los clientes tienen usuario_id
SELECT COUNT(*) as clientes_sin_usuario_id
FROM clientes WHERE usuario_id IS NULL;
-- Resultado esperado: 0

-- Verificar consistencia de roles
SELECT
    tipo_rol,
    COUNT(*) as cantidad
FROM usuarios
GROUP BY tipo_rol;

-- Verificar profesionales con especialidades
SELECT
    p.id,
    COUNT(ep.id) as num_especialidades
FROM profesionales p
LEFT JOIN especialidades_profesional ep ON ep.profesional_id = p.id
GROUP BY p.id
HAVING COUNT(ep.id) = 0;
-- Resultado esperado: 0 (todos deben tener al menos 1 especialidad)

-- Verificar que cada profesional tiene solo 1 especialidad principal
SELECT
    profesional_id,
    COUNT(*) as especialidades_principales
FROM especialidades_profesional
WHERE es_principal = true
GROUP BY profesional_id
HAVING COUNT(*) > 1;
-- Resultado esperado: 0 filas

-- Verificar ubicaciones geográficas
SELECT COUNT(*) FROM departamentos;
SELECT COUNT(*) FROM provincias;
SELECT COUNT(*) FROM distritos;
```

---

## 5. Rollback (En caso de problemas)

Si necesitas revertir los cambios:

```sql
-- Restaurar desde el backup
-- psql -U postgres -d contacto_profesionales < backup_antes_migracion.sql

-- O eliminar las nuevas tablas manualmente:
-- DROP TABLE IF EXISTS redes_sociales_profesional CASCADE;
-- DROP TABLE IF EXISTS especialidades_profesional CASCADE;
-- DROP TABLE IF EXISTS categorias_servicio CASCADE;
-- DROP TABLE IF EXISTS distritos CASCADE;
-- DROP TABLE IF EXISTS provincias CASCADE;
-- DROP TABLE IF EXISTS departamentos CASCADE;
-- DROP VIEW IF EXISTS vista_usuarios_completa CASCADE;
-- DROP VIEW IF EXISTS vista_profesionales_completa CASCADE;
-- DROP VIEW IF EXISTS vista_solicitudes_completa CASCADE;

-- Revertir cambios en tablas existentes
-- ALTER TABLE users DROP COLUMN IF EXISTS usuario_id;
-- ALTER TABLE clientes DROP COLUMN IF EXISTS usuario_id;
-- DROP TABLE IF EXISTS usuarios CASCADE;
```

---

## 6. Notas Importantes

1. **Backup Obligatorio**: Hacer backup completo antes de ejecutar cualquier migración
2. **Entorno de Pruebas**: Probar primero en un ambiente de desarrollo
3. **Datos Geográficos**: Los datos de departamentos, provincias y distritos están incompletos. Completar según necesidad.
4. **Migración Incremental**: Si la base de datos es grande, considerar migrar por lotes
5. **Downtime**: Coordinar una ventana de mantenimiento para la migración
6. **Verificación**: Ejecutar los scripts de verificación después de cada paso

---

## 7. Próximos Pasos Después de la Migración

1. **Actualizar modelos Java** (entities)
2. **Actualizar DAOs** para usar las nuevas tablas
3. **Actualizar servicios** de negocio
4. **Modificar controladores** (servlets)
5. **Actualizar frontend** (HTML/JavaScript)
6. **Actualizar tests** unitarios y de integración
7. **Documentar APIs** actualizadas

---

**Fecha de creación**: 2025-11-10
**Versión**: 1.0
**Autor**: Sistema de Refactoring - Contacto Profesionales
