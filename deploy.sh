#!/bin/bash
# ============================================
# Script de Despliegue para Contacto Profesionales Web
# Automatiza el proceso de build y deploy con Docker
# ============================================

set -e  # Salir si hay algún error

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuración
APP_NAME="contacto-profesionales-web"
IMAGE_NAME="contacto-profesionales-web"
CONTAINER_NAME="contacto-profesionales-web"
APP_PORT="9091"
TOMCAT_PORT="8080"

# Base de datos
DB_HOST="host.docker.internal"
DB_PORT="5432"
DB_NAME="contacto_profesionales_db"
DB_USER="postgres"
DB_PASSWORD="postgres"

# ============================================
# FUNCIONES
# ============================================

function print_header() {
    echo -e "\n${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}\n"
}

function print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

function print_error() {
    echo -e "${RED}❌ $1${NC}"
}

function print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

function print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

# ============================================
# COMANDOS
# ============================================

function build_app() {
    print_header "Compilando aplicación con Maven"

    if ! command -v mvn &> /dev/null; then
        print_error "Maven no está instalado"
        exit 1
    fi

    mvn clean package -DskipTests -B

    if [ $? -eq 0 ]; then
        print_success "Aplicación compilada exitosamente"
        ls -lh target/*.war
    else
        print_error "Fallo la compilación"
        exit 1
    fi
}

function build_image() {
    print_header "Construyendo imagen Docker"

    BUILD_DATE=$(date -u +'%Y-%m-%dT%H:%M:%SZ')
    VCS_REF=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")

    docker build \
        -t ${IMAGE_NAME}:latest \
        -t ${IMAGE_NAME}:${VCS_REF} \
        --build-arg BUILD_DATE=${BUILD_DATE} \
        --build-arg VCS_REF=${VCS_REF} \
        .

    if [ $? -eq 0 ]; then
        print_success "Imagen Docker construida exitosamente"
        docker images | grep ${IMAGE_NAME}
    else
        print_error "Fallo la construcción de la imagen"
        exit 1
    fi
}

function stop_container() {
    print_header "Deteniendo contenedor anterior"

    if docker ps -a | grep -q ${CONTAINER_NAME}; then
        print_info "Deteniendo contenedor ${CONTAINER_NAME}..."
        docker stop ${CONTAINER_NAME} 2>/dev/null || true
        docker rm ${CONTAINER_NAME} 2>/dev/null || true
        sleep 2
        print_success "Contenedor anterior detenido"
    else
        print_info "No hay contenedor anterior corriendo"
    fi
}

function start_container() {
    print_header "Iniciando nuevo contenedor"

    docker run -d \
        --name ${CONTAINER_NAME} \
        -p ${APP_PORT}:${TOMCAT_PORT} \
        -e DB_HOST=${DB_HOST} \
        -e DB_PORT=${DB_PORT} \
        -e DB_NAME=${DB_NAME} \
        -e DB_USER=${DB_USER} \
        -e DB_PASSWORD=${DB_PASSWORD} \
        -e TZ=America/Lima \
        --restart unless-stopped \
        ${IMAGE_NAME}:latest

    if [ $? -eq 0 ]; then
        print_success "Contenedor iniciado exitosamente"
        print_info "Esperando que la aplicación inicie..."
        sleep 10
    else
        print_error "Fallo al iniciar el contenedor"
        exit 1
    fi
}

function health_check() {
    print_header "Verificando salud de la aplicación"

    MAX_RETRIES=10
    RETRY_COUNT=0
    HEALTH_CHECK_PASSED=false

    while [ $RETRY_COUNT -lt $MAX_RETRIES ] && [ "$HEALTH_CHECK_PASSED" = false ]; do
        RETRY_COUNT=$((RETRY_COUNT + 1))
        print_info "Intento ${RETRY_COUNT}/${MAX_RETRIES}: Verificando endpoint..."

        HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:${APP_PORT}/ContactoProfesionalesWeb/ || echo "000")

        if [ "$HTTP_CODE" = "200" ] || [ "$HTTP_CODE" = "302" ]; then
            HEALTH_CHECK_PASSED=true
            print_success "Health check exitoso - Aplicación respondió con código ${HTTP_CODE}"
        else
            if [ $RETRY_COUNT -lt $MAX_RETRIES ]; then
                print_warning "Health check falló (código ${HTTP_CODE}), reintentando en 5s..."
                sleep 5
            fi
        fi
    done

    if [ "$HEALTH_CHECK_PASSED" = false ]; then
        print_error "Health check falló después de ${MAX_RETRIES} intentos"
        print_info "Mostrando logs del contenedor:"
        docker logs --tail 50 ${CONTAINER_NAME}
        exit 1
    fi
}

function show_logs() {
    print_header "Logs del contenedor"
    docker logs --tail 50 ${CONTAINER_NAME}
}

function show_status() {
    print_header "Estado del despliegue"

    print_info "Contenedor:"
    docker ps | grep ${CONTAINER_NAME} || echo "No está corriendo"

    print_info "\nImagen:"
    docker images | grep ${IMAGE_NAME}

    print_info "\nURL de acceso:"
    echo "http://localhost:${APP_PORT}/ContactoProfesionalesWeb/"

    print_info "\nEstadísticas de recursos:"
    docker stats ${CONTAINER_NAME} --no-stream 2>/dev/null || echo "Contenedor no está corriendo"
}

function cleanup() {
    print_header "Limpiando recursos"

    # Limpiar contenedores detenidos
    print_info "Eliminando contenedores detenidos..."
    docker container prune -f

    # Limpiar imágenes sin usar (mantener las últimas 3)
    print_info "Limpiando imágenes antiguas..."
    docker images ${IMAGE_NAME} --format "{{.Tag}}" | \
        grep -v latest | \
        sort -rn | \
        tail -n +4 | \
        xargs -I {} docker rmi ${IMAGE_NAME}:{} 2>/dev/null || true

    print_success "Limpieza completada"
}

function full_deploy() {
    print_header "🚀 DESPLIEGUE COMPLETO"

    build_app
    build_image
    stop_container
    start_container
    health_check
    show_status

    print_header "✅ DESPLIEGUE COMPLETADO"
    echo -e "${GREEN}🌐 Aplicación disponible en: http://localhost:${APP_PORT}/ContactoProfesionalesWeb/${NC}"
}

function show_help() {
    echo "================================================"
    echo "Script de Despliegue - Contacto Profesionales Web"
    echo "================================================"
    echo ""
    echo "Uso: ./deploy.sh [COMANDO]"
    echo ""
    echo "Comandos disponibles:"
    echo "  build-app      - Compilar aplicación con Maven"
    echo "  build-image    - Construir imagen Docker"
    echo "  start          - Iniciar contenedor"
    echo "  stop           - Detener contenedor"
    echo "  restart        - Reiniciar contenedor"
    echo "  logs           - Ver logs del contenedor"
    echo "  status         - Ver estado del despliegue"
    echo "  health-check   - Verificar salud de la aplicación"
    echo "  cleanup        - Limpiar recursos no usados"
    echo "  deploy         - Despliegue completo (build + start)"
    echo "  help           - Mostrar esta ayuda"
    echo ""
    echo "Ejemplos:"
    echo "  ./deploy.sh deploy        # Despliegue completo"
    echo "  ./deploy.sh restart       # Reiniciar aplicación"
    echo "  ./deploy.sh logs          # Ver logs"
    echo ""
}

# ============================================
# MAIN
# ============================================

case "$1" in
    build-app)
        build_app
        ;;
    build-image)
        build_image
        ;;
    start)
        start_container
        ;;
    stop)
        stop_container
        ;;
    restart)
        stop_container
        start_container
        health_check
        ;;
    logs)
        show_logs
        ;;
    status)
        show_status
        ;;
    health-check)
        health_check
        ;;
    cleanup)
        cleanup
        ;;
    deploy)
        full_deploy
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        if [ -z "$1" ]; then
            print_warning "No se especificó ningún comando"
        else
            print_error "Comando desconocido: $1"
        fi
        echo ""
        show_help
        exit 1
        ;;
esac

exit 0
