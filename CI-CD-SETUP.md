# 🚀 Guía de Configuración CI/CD - Contacto Profesionales Web

Esta guía proporciona instrucciones paso a paso para configurar el pipeline de CI/CD con Jenkins y Docker.

---

## 📋 Tabla de Contenidos

1. [Requisitos Previos](#requisitos-previos)
2. [Configuración de Docker Desktop](#configuración-de-docker-desktop)
3. [Configuración de Jenkins](#configuración-de-jenkins)
4. [Configuración del Pipeline](#configuración-del-pipeline)
5. [Ejecución del Pipeline](#ejecución-del-pipeline)
6. [Despliegue Manual con Docker](#despliegue-manual-con-docker)
7. [Troubleshooting](#troubleshooting)

---

## ✅ Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- ✅ **Docker Desktop** (última versión)
  - Windows: https://www.docker.com/products/docker-desktop
  - Configurar para usar WSL 2 (recomendado)

- ✅ **Jenkins** (LTS 2.414+)
  - Descarga: https://www.jenkins.io/download/
  - O ejecutar con Docker: `docker run -p 8080:8080 -p 50000:50000 jenkins/jenkins:lts`

- ✅ **Java 17** (JDK)
  - Oracle JDK o OpenJDK
  - Configurado en PATH

- ✅ **Maven 3.9+**
  - Descarga: https://maven.apache.org/download.cgi
  - Configurado en PATH

- ✅ **Git**
  - Para clonar repositorio desde GitHub

- ✅ **PostgreSQL 16** (para desarrollo local)
  - O usar contenedor Docker (ver docker-compose.yml)

---

## 🐳 Configuración de Docker Desktop

### 1. Instalar Docker Desktop

```bash
# Descargar desde:
https://www.docker.com/products/docker-desktop

# Verificar instalación
docker --version
docker-compose --version
```

### 2. Configurar Docker Desktop

**En Windows:**
1. Abrir Docker Desktop
2. Settings → General → ✅ Use WSL 2 based engine
3. Settings → Resources → Ajustar CPU/Memory según necesites
4. Settings → Docker Engine → Verificar que esté habilitado

**Configuración recomendada:**
- CPUs: 4
- Memory: 4GB
- Swap: 1GB
- Disk image size: 60GB

### 3. Habilitar Docker CLI en Jenkins

Si Jenkins corre en contenedor, necesitas montar el socket de Docker:

```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  -v /var/run/docker.sock:/var/run/docker.sock \
  -v $(which docker):/usr/bin/docker \
  jenkins/jenkins:lts

# Dar permisos (dentro del contenedor)
docker exec -u root jenkins chmod 666 /var/run/docker.sock
```

---

## ⚙️ Configuración de Jenkins

### 1. Instalar Jenkins

**Opción A: Instalación Nativa (Windows/Linux/Mac)**

```bash
# Descargar Jenkins WAR
wget https://get.jenkins.io/war-stable/latest/jenkins.war

# Ejecutar Jenkins
java -jar jenkins.war --httpPort=8080
```

**Opción B: Docker (Recomendado para testing)**

```bash
docker run -d \
  --name jenkins \
  -p 8080:8080 \
  -p 50000:50000 \
  -v jenkins_home:/var/jenkins_home \
  jenkins/jenkins:lts
```

### 2. Configuración Inicial de Jenkins

1. **Acceder a Jenkins**:
   - Abrir: http://localhost:8080
   - Obtener password inicial:
     ```bash
     # Windows
     type %USERPROFILE%\.jenkins\secrets\initialAdminPassword

     # Linux/Mac
     cat ~/.jenkins/secrets/initialAdminPassword
     ```

2. **Instalar plugins sugeridos**

3. **Crear usuario admin**

### 3. Instalar Plugins Necesarios

**Manage Jenkins → Plugin Manager → Available Plugins**

Instalar los siguientes plugins:

- ✅ **Pipeline** (incluido por defecto)
- ✅ **Git Plugin** (incluido por defecto)
- ✅ **Docker Pipeline Plugin**
- ✅ **GitHub Integration Plugin**
- ✅ **Maven Integration Plugin**
- ✅ **JUnit Plugin** (incluido por defecto)
- ✅ **Workspace Cleanup Plugin**
- ✅ **Timestamper** (para logs)
- ✅ **Email Extension Plugin** (opcional, para notificaciones)

**Instalar plugins:**
1. Marcar checkbox de cada plugin
2. Clic en "Install without restart"
3. Esperar que se instalen
4. Reiniciar Jenkins si es necesario

### 4. Configurar Herramientas Globales

**Manage Jenkins → Global Tool Configuration**

#### A. Configurar JDK

```
Name: JDK-17
JAVA_HOME: C:\Program Files\Java\jdk-17
                  (o la ruta donde está instalado Java 17)
```

#### B. Configurar Maven

```
Name: Maven-3.9
MAVEN_HOME: C:\Program Files\Apache\maven
                     (o la ruta donde está instalado Maven)
```

#### C. Configurar Git

```
Name: Default
Path to Git executable: git
                        (o ruta completa: C:\Program Files\Git\bin\git.exe)
```

### 5. Configurar Credenciales

**Manage Jenkins → Credentials → System → Global credentials**

#### A. Credencial de GitHub

```
Kind: Username with password
Username: tu-usuario-github
Password: tu-personal-access-token
ID: github-credentials
Description: GitHub Access Token
```

**Generar Personal Access Token en GitHub:**
1. GitHub → Settings → Developer settings → Personal access tokens
2. Generate new token (classic)
3. Permisos necesarios: `repo`, `read:user`
4. Copiar el token generado

#### B. Credencial de Base de Datos

```
Kind: Secret text
Secret: postgres
ID: db-password
Description: PostgreSQL Password
```

---

## 🔧 Configuración del Pipeline

### 1. Crear Job de Pipeline en Jenkins

1. **New Item**
2. **Nombre**: `contacto-profesionales-web-pipeline`
3. **Tipo**: Pipeline
4. **OK**

### 2. Configurar el Pipeline

**En la configuración del job:**

#### General
- ✅ **Discard old builds**
  - Strategy: Log Rotation
  - Max # of builds to keep: 10

- ✅ **GitHub project**
  - Project url: `https://github.com/tu-usuario/contacto-profesionales-web`

#### Build Triggers
- ✅ **GitHub hook trigger for GITScm polling**
  - (Si tienes webhook configurado en GitHub)

- ✅ **Poll SCM**
  - Schedule: `H/5 * * * *` (cada 5 minutos)

#### Pipeline

**Definition**: Pipeline script from SCM

**SCM**: Git
- **Repository URL**: `https://github.com/tu-usuario/contacto-profesionales-web.git`
- **Credentials**: Seleccionar `github-credentials`
- **Branch Specifier**: `*/develop` (o `*/main`)

**Script Path**: `Jenkinsfile`

**Lightweight checkout**: ✅ (marcado)

### 3. Guardar configuración

Clic en **Save**

---

## 🚀 Ejecución del Pipeline

### 1. Build Manual

1. Ir al job: `contacto-profesionales-web-pipeline`
2. Clic en **Build Now**
3. Ver progreso en **Build History** → Clic en el número de build
4. Ver logs en **Console Output**

### 2. Build Automático

El pipeline se ejecutará automáticamente cuando:
- Se haga un commit en el branch configurado (develop/main)
- El webhook de GitHub dispare el build (si está configurado)
- El polling detecte cambios (cada 5 minutos)

### 3. Monitorear Ejecución

**Ver progreso:**
- **Pipeline Stage View**: Muestra cada etapa del pipeline
- **Console Output**: Logs detallados de ejecución
- **Blue Ocean**: Vista moderna del pipeline (instalar Blue Ocean plugin)

**Stages del Pipeline:**
1. 📦 **Checkout** - Clonar código
2. 🔨 **Build** - Compilar con Maven
3. 🧪 **Tests** - Ejecutar tests unitarios
4. 📦 **Package** - Generar WAR
5. 📊 **Code Quality** - Análisis (opcional)
6. 🐳 **Build Docker Image** - Crear imagen
7. 🚀 **Deploy to Docker** - Desplegar contenedor
8. 🏥 **Health Check** - Verificar aplicación

### 4. Acceder a la Aplicación

Una vez completado el pipeline exitosamente:

```
URL: http://localhost:9091/ContactoProfesionalesWeb/
```

---

## 🔧 Despliegue Manual con Docker

Si quieres desplegar manualmente sin Jenkins:

### Opción 1: Solo la Aplicación

```bash
# 1. Build de la imagen
docker build -t contacto-profesionales-web:latest .

# 2. Ejecutar contenedor
docker run -d \
  --name contacto-profesionales-web \
  -p 9091:8080 \
  -e DB_HOST=host.docker.internal \
  -e DB_PORT=5432 \
  -e DB_NAME=contacto_profesionales_db \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  contacto-profesionales-web:latest

# 3. Ver logs
docker logs -f contacto-profesionales-web

# 4. Acceder
# http://localhost:9091/ContactoProfesionalesWeb/
```

### Opción 2: Aplicación + PostgreSQL (Docker Compose)

```bash
# 1. Iniciar todo el stack
docker-compose up -d

# 2. Ver logs
docker-compose logs -f

# 3. Verificar servicios
docker-compose ps

# 4. Acceder
# http://localhost:9091/ContactoProfesionalesWeb/

# 5. Detener servicios
docker-compose down
```

---

## 🐛 Troubleshooting

### Problema 1: Jenkins no puede ejecutar comandos Docker

**Error**: `docker: command not found`

**Solución**:
```bash
# Si Jenkins está en contenedor
docker exec -u root jenkins \
  sh -c 'apt-get update && apt-get install -y docker.io'

# Verificar
docker exec jenkins docker --version
```

### Problema 2: Error de permisos en Docker socket

**Error**: `permission denied while trying to connect to the Docker daemon socket`

**Solución**:
```bash
# Dar permisos al usuario Jenkins
docker exec -u root jenkins chmod 666 /var/run/docker.sock
```

### Problema 3: Build falla en tests

**Error**: Tests unitarios fallan

**Solución temporal**:
- Modificar Jenkinsfile: `sh 'mvn test -B'` → `sh 'mvn test -B -DskipTests'`
- O corregir los tests que están fallando

### Problema 4: Contenedor no inicia

**Error**: Contenedor se detiene inmediatamente

**Solución**:
```bash
# Ver logs del contenedor
docker logs contacto-profesionales-web

# Verificar que el WAR existe
docker run --rm contacto-profesionales-web:latest ls -la /usr/local/tomcat/webapps/

# Verificar conectividad con BD
docker exec contacto-profesionales-web ping postgres
```

### Problema 5: Puerto 9091 ya en uso

**Error**: `port is already allocated`

**Solución**:
```bash
# Detener el proceso usando el puerto
# Windows
netstat -ano | findstr :9091
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:9091 | xargs kill -9

# O cambiar el puerto en docker-compose.yml o Jenkinsfile
```

### Problema 6: Base de datos no conecta

**Error**: `Connection refused` o `Unknown host`

**Solución**:
```bash
# Verificar que PostgreSQL esté corriendo
docker ps | grep postgres

# Verificar red Docker
docker network inspect contacto-profesionales-net

# Test de conectividad
docker exec contacto-profesionales-web \
  curl -v telnet://postgres:5432
```

---

## 📊 Verificación del Despliegue

### Checklist Post-Despliegue

- [ ] Pipeline ejecutó sin errores
- [ ] Imagen Docker creada: `docker images | grep contacto-profesionales-web`
- [ ] Contenedor corriendo: `docker ps | grep contacto-profesionales-web`
- [ ] Aplicación accesible: http://localhost:9091/ContactoProfesionalesWeb/
- [ ] Health check exitoso: `curl http://localhost:9091/ContactoProfesionalesWeb/`
- [ ] Base de datos conecta correctamente
- [ ] Logs sin errores: `docker logs contacto-profesionales-web`

### Comandos Útiles

```bash
# Ver todos los contenedores
docker ps -a

# Ver logs en tiempo real
docker logs -f contacto-profesionales-web

# Entrar al contenedor
docker exec -it contacto-profesionales-web bash

# Ver uso de recursos
docker stats contacto-profesionales-web

# Reiniciar contenedor
docker restart contacto-profesionales-web

# Ver imágenes Docker
docker images

# Limpiar recursos no usados
docker system prune -a
```

---

## 📚 Referencias

- **Jenkins**: https://www.jenkins.io/doc/
- **Docker**: https://docs.docker.com/
- **Maven**: https://maven.apache.org/guides/
- **Tomcat**: https://tomcat.apache.org/tomcat-10.1-doc/
- **PostgreSQL**: https://www.postgresql.org/docs/

---

## 👥 Soporte

Para problemas o preguntas:
1. Revisar logs de Jenkins
2. Revisar logs de Docker
3. Consultar esta documentación
4. Contactar al equipo de desarrollo

---

**Fecha de última actualización**: 2025-12-05
