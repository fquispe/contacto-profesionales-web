// ============================================
// Jenkinsfile para Pipeline CI/CD
// Contacto Profesionales Web Application
// ============================================

pipeline {
    agent any

    // ============================================
    // VARIABLES DE ENTORNO
    // ============================================
    environment {
        // Configuración de la aplicación
        APP_NAME = 'contacto-profesionales-web'
        DOCKER_IMAGE = "contacto-profesionales-web"
        DOCKER_TAG = "${env.BUILD_NUMBER}"
        DOCKER_LATEST = "latest"

        // Puerto de la aplicación
        APP_PORT = '9091'
        TOMCAT_PORT = '8080'

        // Configuración de base de datos
        DB_HOST = 'host.docker.internal'
        DB_PORT = '5432'
        DB_NAME = 'contacto_profesionales_db'
        DB_USER = 'postgres'
        // DB_PASSWORD se debe configurar en Jenkins Credentials

        // Herramientas
        MAVEN_HOME = tool 'Maven-3.9'
        JAVA_HOME = tool 'JDK-17'
        PATH = "${MAVEN_HOME}/bin:${JAVA_HOME}/bin:${env.PATH}"

        // Configuración de Maven
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=512m'
    }

    // ============================================
    // OPCIONES DEL PIPELINE
    // ============================================
    options {
        // Mantener solo los últimos 10 builds
        buildDiscarder(logRotator(numToKeepStr: '10'))

        // Timeout del pipeline completo
        timeout(time: 30, unit: 'MINUTES')

        // Deshabilitar checkout automático
        skipDefaultCheckout()

        // Timestamps en logs
        timestamps()
    }

    // ============================================
    // TRIGGERS (DISPARADORES)
    // ============================================
    triggers {
        // Polling cada 5 minutos (H/5 * * * *)
        // Reemplazar con webhook de GitHub en producción
        pollSCM('H/5 * * * *')
    }

    // ============================================
    // STAGES (ETAPAS)
    // ============================================
    stages {

        // ============================================
        // STAGE 1: CHECKOUT
        // ============================================
        stage('📦 Checkout') {
            steps {
                script {
                    echo '================================================'
                    echo '🔄 Clonando repositorio desde GitHub...'
                    echo '================================================'
                }

                // Checkout del código desde GitHub
                checkout scm

                script {
                    echo '✅ Código descargado exitosamente'

                    // Mostrar información del commit
                    bat '''
                        echo "📌 Branch: ${GIT_BRANCH}"
                        echo "📌 Commit: $(git rev-parse --short HEAD)"
                        echo "📌 Author: $(git log -1 --pretty=format:'%an')"
                        echo "📌 Message: $(git log -1 --pretty=format:'%s')"
                    '''
                }
            }
        }

        // ============================================
        // STAGE 2: BUILD
        // ============================================
        stage('🔨 Build') {
            steps {
                script {
                    echo '================================================'
                    echo '🔨 Compilando aplicación con Maven...'
                    echo '================================================'
                }

                // Limpiar y compilar sin ejecutar tests
                bat 'mvn clean compile -DskipTests -B'

                script {
                    echo '✅ Compilación exitosa'
                }
            }
        }

        // ============================================
        // STAGE 3: TESTS
        // ============================================
        stage('🧪 Tests') {
            steps {
                script {
                    echo '================================================'
                    echo '🧪 Ejecutando tests unitarios...'
                    echo '================================================'
                }

                // Ejecutar tests
                bat 'mvn test -B'

                script {
                    echo '✅ Tests ejecutados exitosamente'
                }
            }
            post {
                always {
                    // Publicar reportes de tests
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ============================================
        // STAGE 4: PACKAGE
        // ============================================
        stage('📦 Package') {
            steps {
                script {
                    echo '================================================'
                    echo '📦 Generando archivo WAR...'
                    echo '================================================'
                }

                // Generar WAR sin tests (ya se ejecutaron)
                bat 'mvn package -DskipTests -B'

                script {
                    echo '✅ WAR generado exitosamente'

                    // Mostrar información del artefacto
                    bat 'ls -lh target/*.war'
                }
            }
            post {
                success {
                    // Archivar el WAR generado
                    archiveArtifacts artifacts: '**/target/*.war', fingerprint: true
                }
            }
        }

        // ============================================
        // STAGE 5: CODE QUALITY (Opcional)
        // ============================================
        stage('📊 Code Quality') {
            when {
                // Solo ejecutar en branch develop o main
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    echo '================================================'
                    echo '📊 Análisis de calidad de código...'
                    echo '================================================'
                    echo '⚠️  SonarQube no configurado - Saltando...'
                    // Para habilitar SonarQube, descomentar:
                    // withSonarQubeEnv('SonarQube') {
                    //     bat 'mvn sonar:sonar'
                    // }
                }
            }
        }

        // ============================================
        // STAGE 6: BUILD DOCKER IMAGE
        // ============================================
        stage('🐳 Build Docker Image') {
            steps {
                script {
                    echo '================================================'
                    echo '🐳 Construyendo imagen Docker...'
                    echo '================================================'
                }

                // Build de imagen Docker con multi-stage
                bat """
                    docker build \
                        -t ${DOCKER_IMAGE}:${DOCKER_TAG} \
                        -t ${DOCKER_IMAGE}:${DOCKER_LATEST} \
                        --build-arg BUILD_DATE=\$(date -u +'%Y-%m-%dT%H:%M:%SZ') \
                        --build-arg VCS_REF=\$(git rev-parse --short HEAD) \
                        .
                """

                script {
                    echo '✅ Imagen Docker construida exitosamente'

                    // Mostrar información de la imagen
                    bat "docker images | grep ${DOCKER_IMAGE}"
                }
            }
        }

        // ============================================
        // STAGE 7: DEPLOY TO DOCKER
        // ============================================
        stage('🚀 Deploy to Docker') {
            steps {
                script {
                    echo '================================================'
                    echo '🚀 Desplegando contenedor en Docker...'
                    echo '================================================'

                    // Obtener credenciales de la base de datos desde Jenkins
                    withCredentials([string(credentialsId: 'db-password', variable: 'DB_PASSWORD')]) {

                        // Stop y remover contenedor anterior si existe
                        bat """
                            echo '🛑 Deteniendo contenedor anterior si existe...'
                            docker stop ${APP_NAME} 2>/dev/null || true
                            docker rm ${APP_NAME} 2>/dev/null || true
                        """

                        // Esperar a que el contenedor se detenga completamente
                        sleep(time: 5, unit: 'SECONDS')

                        // Iniciar nuevo contenedor
                        bat """
                            echo '▶️  Iniciando nuevo contenedor...'
                            docker run -d \
                                --name ${APP_NAME} \
                                -p ${APP_PORT}:${TOMCAT_PORT} \
                                -e DB_HOST=${DB_HOST} \
                                -e DB_PORT=${DB_PORT} \
                                -e DB_NAME=${DB_NAME} \
                                -e DB_USER=${DB_USER} \
                                -e DB_PASSWORD=${DB_PASSWORD} \
                                -e TZ=America/Lima \
                                --restart unless-stopped \
                                ${DOCKER_IMAGE}:${DOCKER_LATEST}
                        """

                        echo '✅ Contenedor desplegado exitosamente'
                    }
                }
            }
        }

        // ============================================
        // STAGE 8: HEALTH CHECK
        // ============================================
        stage('🏥 Health Check') {
            steps {
                script {
                    echo '================================================'
                    echo '🏥 Verificando salud de la aplicación...'
                    echo '================================================'

                    // Esperar 30 segundos para que la aplicación inicie
                    echo '⏳ Esperando que la aplicación inicie (30s)...'
                    sleep(time: 30, unit: 'SECONDS')

                    // Verificar que el contenedor esté corriendo
                    def containerStatus = sh(
                        script: "docker ps --filter name=${APP_NAME} --format '{{.Status}}'",
                        returnStdout: true
                    ).trim()

                    if (containerStatus.contains('Up')) {
                        echo "✅ Contenedor está corriendo: ${containerStatus}"
                    } else {
                        error "❌ Contenedor no está corriendo"
                    }

                    // Health check HTTP
                    def maxRetries = 5
                    def retryCount = 0
                    def healthCheckPassed = false

                    while (retryCount < maxRetries && !healthCheckPassed) {
                        retryCount++
                        echo "🔍 Intento ${retryCount}/${maxRetries}: Verificando endpoint..."

                        def exitCode = sh(
                            script: "curl -f -s -o /dev/null -w '%{http_code}' http://localhost:${APP_PORT}/ContactoProfesionalesWeb/ || true",
                            returnStatus: true
                        )

                        if (exitCode == 0) {
                            healthCheckPassed = true
                            echo '✅ Health check exitoso - Aplicación responde correctamente'
                        } else if (retryCount < maxRetries) {
                            echo "⚠️  Health check falló, reintentando en 10s..."
                            sleep(time: 10, unit: 'SECONDS')
                        }
                    }

                    if (!healthCheckPassed) {
                        error '❌ Health check falló después de múltiples intentos'
                    }

                    // Mostrar logs del contenedor (últimas 20 líneas)
                    echo '📋 Últimos logs del contenedor:'
                    bat "docker logs --tail 20 ${APP_NAME}"
                }
            }
        }
    }

    // ============================================
    // POST-ACTIONS (Después de todas las stages)
    // ============================================
    post {
        success {
            script {
                echo '================================================'
                echo '✅ PIPELINE COMPLETADO EXITOSAMENTE'
                echo '================================================'
                echo "🌐 Aplicación disponible en: http://localhost:${APP_PORT}/ContactoProfesionalesWeb/"
                echo "🐳 Contenedor: ${APP_NAME}"
                echo "🏷️  Imagen: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                echo '================================================'

                // Limpiar imágenes antiguas (mantener últimas 3)
                bat """
                    echo '🧹 Limpiando imágenes antiguas...'
                    docker images ${DOCKER_IMAGE} --format "{{.Tag}}" | \
                        grep -v latest | \
                        sort -rn | \
                        tail -n +4 | \
                        xargs -I {} docker rmi ${DOCKER_IMAGE}:{} 2>/dev/null || true
                """
            }

            // Enviar notificación de éxito (opcional)
            // emailext (
            //     subject: "✅ Build Success: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Pipeline completado exitosamente.\n\nVer detalles: ${env.BUILD_URL}",
            //     to: "team@example.com"
            // )
        }

        failure {
            script {
                echo '================================================'
                echo '❌ PIPELINE FALLÓ'
                echo '================================================'

                // Mostrar logs del contenedor si existe
                bat """
                    if docker ps -a | grep -q ${APP_NAME}; then
                        echo '📋 Logs del contenedor:'
                        docker logs --tail 50 ${APP_NAME}
                    fi
                """
            }

            // Enviar notificación de fallo (opcional)
            // emailext (
            //     subject: "❌ Build Failed: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //     body: "Pipeline falló.\n\nVer detalles: ${env.BUILD_URL}",
            //     to: "team@example.com"
            // )
        }

        always {
            // Limpiar workspace si es necesario (opcional)
            // cleanWs()

            script {
                echo '================================================'
                echo "⏱️  Duración total: ${currentBuild.durationString}"
                echo '================================================'
            }
        }
    }
}
