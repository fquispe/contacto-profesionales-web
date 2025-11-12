# Módulo de Servicios Profesionales

## Descripción

Este módulo permite a los profesionales configurar sus servicios de manera completa, incluyendo:

- **Especialidades**: Hasta 3 especialidades con descripción, costos y materiales
- **Área de Servicio**: Todo el país o ubicaciones específicas (hasta 10)
- **Disponibilidad Horaria**: 24/7 o horarios personalizados por día de la semana

## Archivos Creados/Modificados

### Base de Datos
- `database-servicios-profesional.sql` - Script SQL para crear las tablas necesarias

### Modelos Java (src/main/java/com/contactoprofesionales/model/)
- `EspecialidadProfesional.java` - Modelo para especialidades
- `AreaServicio.java` - Modelo para área de servicio
- `UbicacionServicio.java` - Modelo para ubicaciones específicas
- `DisponibilidadHoraria.java` - Modelo para disponibilidad
- `HorarioDia.java` - Modelo para horarios por día

### DTOs (src/main/java/com/contactoprofesionales/dto/)
- `ServiciosProfesionalCompleto.java` - DTO que encapsula todos los servicios

### DAOs (src/main/java/com/contactoprofesionales/dao/)
- `ServiciosProfesionalDAO.java` - Interface del DAO
- `ServiciosProfesionalDAOImpl.java` - Implementación con operaciones transaccionales

### Servlets (src/main/java/com/contactoprofesionales/controller/)
- `ServiciosProfesionalServlet.java` - API REST para servicios profesionales

### Frontend (src/main/webapp/)
- `servicios-profesional.html` - Página del formulario
- `assets/js/servicios-profesional.js` - Lógica del formulario
- `dashboard.html` - Actualizado con enlace a servicios profesionales

## Instalación y Configuración

### 1. Base de Datos

Ejecutar el script SQL en PostgreSQL:

```bash
psql -U [usuario] -d contactoprofesionales -f database-servicios-profesional.sql
```

O desde psql:

```sql
\i /ruta/a/database-servicios-profesional.sql
```

El script creará las siguientes tablas:

- `especialidades_profesional` - Especialidades del profesional (máx. 3)
- `areas_servicio` - Configuración de área de servicio
- `ubicaciones_servicio` - Ubicaciones específicas (máx. 10)
- `disponibilidad_horaria` - Configuración de disponibilidad
- `horarios_dia` - Horarios por día de la semana

### 2. Compilar el Proyecto

```bash
mvn clean compile
```

### 3. Empaquetar el WAR

```bash
mvn clean package
```

El archivo WAR se generará en `target/ContactoProfesionalesWeb.war`

### 4. Desplegar

Copiar el WAR a la carpeta webapps de Tomcat o usar el administrador de aplicaciones.

## Uso de la Aplicación

### Acceso

1. Iniciar sesión en la aplicación
2. Desde el Dashboard, hacer clic en **"⚙️ Servicios Profesionales"**

### Configuración de Servicios

#### Especialidades

- Puede agregar hasta 3 especialidades
- Cada especialidad requiere:
  - **Nombre**: Descripción de la especialidad
  - **Descripción**: Detalles del servicio (opcional)
  - **Incluye Materiales**: Checkbox para indicar si incluye materiales
  - **Costo**: Precio del servicio
  - **Tipo de Tarifa**: Por hora, día o mes
- Debe marcar una como **principal**
- La primera especialidad agregada será principal por defecto

#### Área de Servicio

**Opción 1: Todo el País**
- Activar el switch "Brindo servicios en todo el país"

**Opción 2: Ubicaciones Específicas**
- Agregar hasta 10 ubicaciones
- Para cada ubicación especificar:
  - **Nivel**: Departamento, Provincia o Distrito
  - **Departamento**: Seleccionar de la lista
  - **Provincia**: Si aplica
  - **Distrito**: Si aplica

#### Disponibilidad Horaria

**Opción 1: Todo el Tiempo (24/7)**
- Activar el switch "Disponible todo el tiempo"

**Opción 2: Horarios Específicos**
- Agregar días de la semana
- Para cada día especificar:
  - **Día de la Semana**: Lunes a Domingo
  - **Tipo de Jornada**: 8 horas o 24 horas
  - **Horario**: Si es 8 horas, especificar inicio y fin

### Guardar Configuración

1. Completar todos los campos requeridos (marcados con *)
2. Hacer clic en **"💾 Guardar Configuración"**
3. El sistema validará los datos y guardará de manera transaccional
4. Será redirigido al Dashboard

### Editar Configuración

- Si ya tiene servicios configurados, estos se cargarán automáticamente
- Puede modificar cualquier campo y guardar los cambios
- Los cambios se aplicarán de manera transaccional

## API REST

### Endpoints

#### GET /api/servicios-profesional
Obtener servicios de un profesional

**Parámetros:**
- `profesionalId` (required): ID del profesional

**Respuesta:**
```json
{
  "success": true,
  "message": "Servicios obtenidos exitosamente",
  "data": {
    "profesionalId": 1,
    "especialidades": [...],
    "areaServicio": {...},
    "disponibilidad": {...}
  }
}
```

#### POST /api/servicios-profesional
Crear o actualizar servicios

**Body:**
```json
{
  "profesionalId": 1,
  "especialidades": [
    {
      "nombreEspecialidad": "Electricidad",
      "descripcion": "Instalación eléctrica",
      "incluyeMateriales": true,
      "costo": 50.00,
      "tipoCosto": "hora",
      "esPrincipal": true
    }
  ],
  "areaServicio": {
    "todoPais": false,
    "ubicaciones": [
      {
        "tipoUbicacion": "departamento",
        "departamento": "Lima"
      }
    ]
  },
  "disponibilidad": {
    "todoTiempo": false,
    "horarios": [
      {
        "diaSemana": "lunes",
        "tipoJornada": "8hrs",
        "horaInicio": "08:00",
        "horaFin": "17:00"
      }
    ]
  }
}
```

**Respuesta:**
```json
{
  "success": true,
  "message": "Servicios guardados exitosamente",
  "profesionalId": 1
}
```

#### PUT /api/servicios-profesional
Actualizar servicios existentes

**Body:** Mismo formato que POST

#### DELETE /api/servicios-profesional
Eliminar servicios (soft delete)

**Parámetros:**
- `profesionalId` (required): ID del profesional

## Validaciones

### Frontend
- Mínimo 1 especialidad, máximo 3
- Al menos una especialidad debe ser principal
- Costos mayores a 0
- Si no es "todo el país", debe tener al menos 1 ubicación
- Máximo 10 ubicaciones
- Si no es "todo el tiempo", debe tener al menos 1 horario
- Horarios: hora fin > hora inicio

### Backend
- Validaciones similares en el servlet
- Operaciones transaccionales (rollback automático en caso de error)
- Soft delete para mantener historial

## Características Técnicas

### Transacciones
- Todas las operaciones de guardado/actualización son transaccionales
- Si falla alguna parte, se hace rollback completo

### Seguridad
- CORS configurado
- Validación de datos en frontend y backend
- Prepared statements para prevenir SQL injection

### Base de Datos
- Triggers para actualizar fecha_actualizacion automáticamente
- Trigger para validar que solo haya una especialidad principal
- Constraints para máximos (3 especialidades, 10 ubicaciones)
- Índices para optimizar consultas

## Solución de Problemas

### Error: "Debe proporcionar al menos una especialidad"
- Asegúrese de agregar al menos una especialidad antes de guardar

### Error: "No puede registrar más de 3 especialidades"
- Elimine una especialidad existente antes de agregar una nueva

### Error: Connection refused / 500
- Verificar que la base de datos esté corriendo
- Verificar que las tablas estén creadas correctamente
- Revisar logs del servidor en `logs/contactoprofesionales.log`

### Datos no se guardan
- Abrir la consola del navegador (F12)
- Verificar si hay errores en la petición
- Verificar que todos los campos requeridos estén completos

## Tecnologías Utilizadas

- **Backend**: Java 21, Jakarta Servlets 6.0
- **Base de Datos**: PostgreSQL con HikariCP
- **Frontend**: HTML5, CSS3, JavaScript Vanilla
- **JSON**: Gson 2.10.1
- **Logging**: SLF4J + Logback

## Autor

Desarrollado siguiendo la arquitectura MVC + DAOs del proyecto Contacto Profesionales Web.

## Licencia

Mismo que el proyecto principal.
