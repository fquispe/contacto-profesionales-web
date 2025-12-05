pipeline {
    agent any

    environment {
        APP_NAME = 'contacto-profesionales-web'
        DOCKER_IMAGE = 'contacto-profesionales-web'
        DOCKER_TAG = "${BUILD_NUMBER}"
        DOCKER_LATEST = "latest"

        APP_PORT = '9091'
        TOMCAT_PORT = '8080'

        DB_HOST = 'host.docker.internal'
        DB_PORT = '5432'
        DB_NAME = 'contacto_profesionales_db'
        DB_USER = 'postgres'

        MAVEN_HOME = tool 'Maven-3.9'
        JAVA_HOME = tool 'JDK-17'
        PATH = "${MAVEN_HOME}\\bin;${JAVA_HOME}\\bin;${PATH}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        skipDefaultCheckout()
        timestamps()
    }

    stages {

        /* ---------------------------------------------------------
         * 1) CHECKOUT
         * --------------------------------------------------------- */
        stage('Checkout') {
            steps {
                echo "📥 Clonando repositorio..."
                checkout scm

                bat """
                echo Branch: %GIT_BRANCH%
                git rev-parse --short HEAD
                git log -1 --pretty=format:"Author: %%an"
                git log -1 --pretty=format:"Message: %%s"
                """
            }
        }

        /* ---------------------------------------------------------
         * 2) BUILD
         * --------------------------------------------------------- */
        stage('Build') {
            steps {
                echo "🔨 Compilando con Maven..."
                bat "${MAVEN_HOME}\\bin\\mvn.cmd clean compile -DskipTests -B"
            }
        }
		
        /* ---------------------------------------------------------
         * 4) PACKAGE
         * --------------------------------------------------------- */
        stage('Package') {
            steps {
                echo "📦 Empaquetando WAR..."
                bat "${MAVEN_HOME}\\bin\\mvn.cmd package -DskipTests -B"
                archiveArtifacts artifacts: '**/target/*.war', fingerprint: true
            }
        }

        /* ---------------------------------------------------------
         * 5) BUILD DOCKER IMAGE
         * --------------------------------------------------------- */
        stage('Docker Build') {
            steps {
                echo "🐳 Construyendo imagen Docker..."

                bat(
                    'docker build ' +
                    '-t ' + DOCKER_IMAGE + ':' + DOCKER_TAG + ' ' +
                    '-t ' + DOCKER_IMAGE + ':' + DOCKER_LATEST + ' ' +
                    '.'
                )
            }
        }

        /* ---------------------------------------------------------
         * 6) DEPLOY DOCKER
         * --------------------------------------------------------- */
        stage('Deploy Docker') {
            steps {
                script {
                    echo "🚀 Deployando contenedor..."

                    withCredentials([string(credentialsId: 'db-password', variable: 'DB_PASSWORD')]) {

                        // Detener contenedor previo
                        bat(
                            'docker stop ' + APP_NAME + ' 2>NUL || exit 0'
                        )
                        bat(
                            'docker rm ' + APP_NAME + ' 2>NUL || exit 0'
                        )

                        // Iniciar contenedor nuevo
                        bat(
                            'docker run -d ' +
                            '--name ' + APP_NAME + ' ' +
                            '-p ' + APP_PORT + ':' + TOMCAT_PORT + ' ' +
                            '-e DB_HOST=' + DB_HOST + ' ' +
                            '-e DB_PORT=' + DB_PORT + ' ' +
                            '-e DB_NAME=' + DB_NAME + ' ' +
                            '-e DB_USER=' + DB_USER + ' ' +
                            '-e DB_PASSWORD=' + DB_PASSWORD + ' ' +
                            DOCKER_IMAGE + ':' + DOCKER_LATEST
                        )
                    }
                }
            }
        }

        /* ---------------------------------------------------------
         * 7) HEALTH CHECK
         * --------------------------------------------------------- */
        stage('Health Check') {
            steps {
                script {
                    echo "🏥 Validando estado del contenedor..."
                    sleep 20

                    def status = bat(
                        script: 'docker ps --filter "name=' + APP_NAME + '" --format "{{.Status}}"',
                        returnStdout: true
                    ).trim()

                    if (!status.contains("Up")) {
                        error "❌ El contenedor no está levantado"
                    }

                    echo "✔️ Contenedor arriba: ${status}"

                    // Health HTTP Check
                    echo "🔍 Validando endpoint..."
                    bat 'curl -s http://localhost:' + APP_PORT + '/ContactoProfesionalesWeb/ > NUL'
                }
            }
        }
    }

    /* ---------------------------------------------------------
     * POST ACTIONS
     * --------------------------------------------------------- */
    post {
        success {
            echo "🎉 PIPELINE COMPLETADO EXITOSAMENTE"
            echo "URL: http://localhost:${APP_PORT}/ContactoProfesionalesWeb/"
        }

        failure {
            echo "❌ PIPELINE FALLÓ"
        }

        always {
            echo "⏱ Duración total: ${currentBuild.durationString}"
        }
    }
}
