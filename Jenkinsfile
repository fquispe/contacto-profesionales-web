// ============================================
// Jenkinsfile - CI/CD Windows
// Contacto Profesionales Web (Servlets)
// ============================================

pipeline {
    agent any

    // Herramientas instaladas en Jenkins
    tools {
        maven 'Maven-3.9'
        jdk   'JDK-17'
    }

    // Variables de entorno generales
    environment {
        // App & Docker
        APP_NAME     = 'contacto-profesionales-web'
        DOCKER_IMAGE = 'contacto-profesionales-web'
        DOCKER_TAG   = "${env.BUILD_NUMBER}"
        APP_PORT     = '9091'
        TOMCAT_PORT  = '8080'

        // Base de datos (password desde credencial)
        DB_HOST = 'host.docker.internal'
        DB_PORT = '5432'
        DB_NAME = 'contacto_profesionales_db'
        DB_USER = 'postgres'

        // Zona horaria del contenedor
        TZ = 'America/Lima'
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        skipDefaultCheckout()
        timestamps()
    }

    stages {

        // ============================================
        // 1. Checkout
        // ============================================
        stage('Checkout') {
            steps {
                script {
                    echo '==============================================='
                    echo 'Clonando repositorio desde GitHub...'
                    echo '==============================================='
                }

                checkout scm

                script {
                    echo '✅ Código descargado exitosamente'

                    // Obtener info del commit usando bat + returnStdout
                    def branch = env.GIT_BRANCH ?: bat(
                        script: 'git rev-parse --abbrev-ref HEAD',
                        returnStdout: true
                    ).trim()

                    def commit = bat(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()

                    def author = bat(
                        script: 'git log -1 --pretty=format:%an',
                        returnStdout: true
                    ).trim()

                    def message = bat(
                        script: 'git log -1 --pretty=format:%s',
                        returnStdout: true
                    ).trim()

                    echo "Branch : ${branch}"
                    echo "Commit : ${commit}"
                    echo "Author : ${author}"
                    echo "Message: ${message}"
                }
            }
        }

        // ============================================
        // 2. Build
        // ============================================
        stage('Build') {
            steps {
                script {
                    echo '==============================================='
                    echo 'Compilando aplicación con Maven...'
                    echo '==============================================='
                }

                // Maven ya está en PATH por tools{}
                bat 'mvn clean compile -DskipTests -B'

                script {
                    echo '✅ Compilación exitosa'
                }
            }
        }

        // ============================================
        // 3. Tests
        // ============================================
        stage('Tests') {
            steps {
                script {
                    echo '==============================================='
                    echo 'Ejecutando tests unitarios...'
                    echo '==============================================='
                }

                bat 'mvn test -B'

                script {
                    echo '✅ Tests ejecutados exitosamente'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        // ============================================
        // 4. Package (WAR)
        // ============================================
        stage('Package') {
            steps {
                script {
                    echo '==============================================='
                    echo 'Generando archivo WAR...'
                    echo '==============================================='
                }

                bat 'mvn package -DskipTests -B'

                script {
                    echo '✅ WAR generado exitosamente'
                    bat 'dir /B target\\*.war'
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: '**/target/*.war', fingerprint: true
                }
            }
        }

        // ============================================
        // 5. Code Quality (placeholder)
        // ============================================
        stage('Code Quality') {
            when {
                anyOf {
                    branch 'develop'
                    branch 'main'
                    branch 'master'
                }
            }
            steps {
                script {
                    echo '==============================================='
                    echo 'Análisis de calidad de código (placeholder)...'
                    echo '==============================================='
                    echo 'SonarQube no configurado - etapa informativa.'
                }
            }
        }

        // ============================================
        // 6. Build Docker Image
        // ============================================
        stage('Build Docker Image') {
            steps {
                script {
                    echo '==============================================='
                    echo 'Construyendo imagen Docker...'
                    echo '==============================================='
                }

                bat """
                    docker build ^
                        -t ${DOCKER_IMAGE}:${DOCKER_TAG} ^
                        -t ${DOCKER_IMAGE}:latest ^
                        --build-arg BUILD_DATE=%DATE% ^
                        .
                """

                script {
                    echo '✅ Imagen Docker construida exitosamente'
                    bat "docker images ${DOCKER_IMAGE}"
                }
            }
        }

        // ============================================
        // 7. Deploy to Docker
        // ============================================
        stage('Deploy to Docker') {
            steps {
                script {
                    echo '==============================================='
                    echo 'Desplegando contenedor Docker...'
                    echo '==============================================='
                }

                withCredentials([string(credentialsId: 'db-password', variable: 'DB_PASSWORD')]) {
                    bat """
                        echo Deteniendo contenedor previo (si existe)...
                        docker stop ${APP_NAME} 2>NUL || echo No había contenedor previo
                        docker rm   ${APP_NAME} 2>NUL || echo No había contenedor previo

                        echo Iniciando nuevo contenedor...
                        docker run -d ^
                          --name ${APP_NAME} ^
                          -p ${APP_PORT}:${TOMCAT_PORT} ^
                          -e DB_HOST=${DB_HOST} ^
                          -e DB_PORT=${DB_PORT} ^
                          -e DB_NAME=${DB_NAME} ^
                          -e DB_USER=${DB_USER} ^
                          -e DB_PASSWORD=${DB_PASSWORD} ^
                          -e TZ=${TZ} ^
                          --restart unless-stopped ^
                          ${DOCKER_IMAGE}:latest
                    """

                    echo '✅ Contenedor desplegado exitosamente'
                }
            }
        }

        // ============================================
        // 8. Health Check
        // ============================================
        stage('Health Check') {
            steps {
                script {
                    echo '==============================================='
                    echo 'Verificando salud de la aplicación...'
                    echo '==============================================='
                    echo 'Esperando 30 segundos a que levante el contenedor...'
                }

                // Espera inicial
                bat 'ping -n 6 127.0.0.1 >NUL'

                // Verificar que el contenedor esté "Up"
                script {
                    def status = bat(
                        script: "docker ps --filter \"name=${APP_NAME}\" --format \"#{Status}\"",
                        returnStdout: true
                    ).trim()

                    if (!status.toLowerCase().contains('up')) {
                        error "❌ El contenedor ${APP_NAME} no está corriendo. Status: ${status}"
                    } else {
                        echo "✅ Contenedor corriendo: ${status}"
                    }
                }

                // Health check HTTP con PowerShell
                script {
                    def psCmd = """
                        \$maxRetries = 5
                        \$ok = \$false
                        for (\$i = 1; \$i -le \$maxRetries -and -not \$ok; \$i++) {
                            Write-Host "Intento \$i de \$maxRetries..."
                            try {
                                \$r = Invoke-WebRequest -UseBasicParsing -Uri 'http://localhost:${APP_PORT}/ContactoProfesionalesWeb/' -TimeoutSec 10
                                if (\$r.StatusCode -eq 200) {
                                    Write-Host "Health check OK"
                                    \$ok = \$true
                                }
                            } catch {
                                Write-Host "Health check falló, reintentando..."
                            }
                            if (-not \$ok) { Start-Sleep -Seconds 10 }
                        }
                        if (-not \$ok) { exit 1 } else { exit 0 }
                    """

                    bat """
                        powershell -NoLogo -NoProfile -Command "${psCmd.replace('"','\\"')}"
                    """
                }

                // Mostrar últimos logs
                script {
                    echo 'Últimos logs del contenedor:'
                    bat "docker logs --tail 20 ${APP_NAME} || echo No se pudieron obtener logs"
                }
            }
        }
    }

    // ============================================
    // POST ACTIONS
    // ============================================
    post {
        success {
            script {
                echo '==============================================='
                echo '✅ PIPELINE COMPLETADO EXITOSAMENTE'
                echo "Aplicación: http://localhost:${APP_PORT}/ContactoProfesionalesWeb/"
                echo "Contenedor: ${APP_NAME}"
                echo "Imagen    : ${DOCKER_IMAGE}:${DOCKER_TAG}"
                echo '==============================================='

                // Limpieza básica de imágenes antiguas (no crítica)
                bat """
                    echo Limpiando imágenes antiguas de ${DOCKER_IMAGE}...
                    for /f "skip=3 tokens=1" %%i in ('docker images ${DOCKER_IMAGE} --format "{{.Tag}}" ^| findstr /R "^[0-9][0-9]*$" ^| sort /R') do (
                        echo Eliminando tag antiguo: %%i
                        docker rmi ${DOCKER_IMAGE}:%%i 2>NUL
                    )
                    exit /B 0
                """
            }
        }

        failure {
            script {
                echo '==============================================='
                echo '❌ PIPELINE FALLÓ'
                echo '==============================================='

                // Intentar mostrar logs del contenedor sin romper el post
                bat """
                    docker ps -a --format "{{.Names}}" | findstr /I "${APP_NAME}" >NUL
                    if %ERRORLEVEL%==0 (
                        echo Mostrando últimos logs del contenedor...
                        docker logs --tail 50 ${APP_NAME} || echo No se pudieron obtener logs
                    ) else (
                        echo No existe contenedor ${APP_NAME}
                    )
                    exit /B 0
                """
            }
        }

        always {
            script {
                echo '==============================================='
                echo "Duración total: ${currentBuild.durationString}"
                echo '==============================================='
            }
            // Si quieres limpiar workspace:
            // cleanWs()
        }
    }
}
