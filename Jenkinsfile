pipeline {
    agent any

    environment {
        GIT_URL = "https://github.com/dhis2/performance-tests-locust"
        IMAGE_NAME = "dhis2/locustio"
        IMAGE_TAG = "latest"
        COMPOSE_ARGS = "NO_WEB=true TIME=10s HATCH_RATE=10 USERS=10"
        LOCUST_REPORT_DIR = "locust"
        LOCAL_REPORT_DIR = "reports"
        REPORT_FILE = "html_report.html"
    }
    stages {
        stage('Checkout') {
            steps {
                git url:"${GIT_URL}"
            }
        }

        stage('Start locust') {
            steps {
                sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                sh "${COMPOSE_ARGS} docker-compose up -d"
            }
        }

        stage('Run tests') {
            steps {
                sh "mvn clean compile exec:java"
            }
        }
    }

    post {
        always {
            script {
                sh "mkdir -p ${LOCAL_REPORT_DIR} && cp ./${LOCUST_REPORT_DIR}/${REPORT_FILE} ./${LOCAL_REPORT_DIR}/${REPORT_FILE}"
                publishHTML target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: "${LOCAL_REPORT_DIR}",
                        reportFiles: "${REPORT_FILE}",
                        reportName: 'Load test report'
                ]

                sh "docker-compose down -v"
            }
        }
    }
}