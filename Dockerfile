# ============================================
# Dockerfile Multi-Stage para Contacto Profesionales Web
# Optimizado para producción con build separado
# ============================================

# ============================================
# STAGE 1: BUILD
# Compila la aplicación usando Maven
# ============================================
FROM maven:3.9.5-eclipse-temurin-17 AS builder

# Metadata
LABEL maintainer="contactoprofesionales@example.com"
LABEL description="Build stage for Contacto Profesionales Web Application"

# Establecer directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven primero (aprovecha cache de Docker)
COPY pom.xml .
COPY src ./src

# Descargar dependencias (se cachea si pom.xml no cambia)
RUN mvn dependency:go-offline -B

# Compilar la aplicación (skip tests en imagen, se ejecutan en Jenkins)
RUN mvn clean package -DskipTests -B

# ============================================
# STAGE 2: RUNTIME
# Imagen ligera con Tomcat para ejecutar la aplicación
# ============================================
FROM tomcat:10.1-jdk17-temurin-jammy

# Metadata
LABEL maintainer="contactoprofesionales@example.com"
LABEL description="Production image for Contacto Profesionales Web Application"
LABEL version="1.0.0"

# Variables de entorno para configuración
ENV CATALINA_HOME=/usr/local/tomcat
ENV APP_NAME=ContactoProfesionalesWeb
ENV DB_HOST=host.docker.internal
ENV DB_PORT=5432
ENV DB_NAME=contacto_profesionales_db
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV TZ=America/Lima

# Configurar zona horaria
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Crear usuario no-root para seguridad
RUN groupadd -r tomcat && useradd -r -g tomcat tomcat

# Limpiar aplicaciones por defecto de Tomcat
RUN rm -rf $CATALINA_HOME/webapps/* && \
    mkdir -p $CATALINA_HOME/webapps/ROOT

# Copiar el WAR compilado desde el stage de build
COPY --from=builder /app/target/*.war $CATALINA_HOME/webapps/ROOT.war

# Copiar archivos de configuración si existen
# COPY --from=builder /app/src/main/resources/application.properties $CATALINA_HOME/conf/

# Crear directorio para logs
RUN mkdir -p $CATALINA_HOME/logs && \
    chown -R tomcat:tomcat $CATALINA_HOME

# Exponer puerto 8080 (Tomcat)
EXPOSE 8080

# Configurar healthcheck
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/$APP_NAME/ || exit 1

# Cambiar a usuario tomcat
USER tomcat

# Comando para iniciar Tomcat
CMD ["catalina.sh", "run"]

# ============================================
# NOTAS DE USO:
# ============================================
# Build: docker build -t contacto-profesionales-web:latest .
# Run: docker run -d -p 9091:8080 \
#        -e DB_HOST=host.docker.internal \
#        -e DB_PORT=5432 \
#        -e DB_NAME=contacto_profesionales_db \
#        -e DB_USER=postgres \
#        -e DB_PASSWORD=your_password \
#        --name contacto-profesionales-web \
#        contacto-profesionales-web:latest
# ============================================
