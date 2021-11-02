@Library('pipeline-library') _
pipeline {
    agent {
        label 'ec2-jdk11'
    }

    environment {
//         GIT_URL = "https://github.com/dhis2/performance-tests-locust"
//         AWX_BOT_CREDENTIALS = credentials('awx-bot-user-credentials')
//         GITHUB_CREDS = credentials('github_bot')
//         GITHUB_USERNAME = "${GITHUB_CREDS_USR}"
//         GITHUB_TOKEN = credentials('github-token')
//         IMAGE_NAME = "dhis2/locustio"
//         IMAGE_TAG = "latest"
        COMPOSE_ARGS = "NO_WEB=true TIME=30m HATCH_RATE=5 USERS=30"
        LOCUST_REPORT_DIR = "locust"
//         LOCAL_REPORT_DIR = "reports"
        REPORT_FILE = "html_report.html"
        INSTANCE_HOST = "test.performance.dhis2.org"
        INSTANCE_NAME = "2.37.0"
    }

//     triggers {
//        cron('H 3 * * *')
//     }

    stages {
//         stage('Checkout') {
//             steps {
//                 git branch: 'DEVOPS-30', url: "${GIT_URL}"
//             }
//         }

//         stage('Update performance test instance') {
//             steps {
//                 script {
//                     awx.resetWar("$AWX_BOT_CREDENTIALS", "${INSTANCE_HOST}", "${INSTANCE_NAME}")
//                 }
//             }
//         }

        stage('Start locust') {
            steps {
                script {
//                     sh "docker build -t ${IMAGE_NAME}:${IMAGE_TAG} ."
                    sh "${COMPOSE_ARGS} docker-compose up -d"
                }
            }
        }

        stage('Run tests') {
            steps {
                script {
                    sh "mvn -s settings.xml clean compile exec:java -Dtarget.baseuri=https://$INSTANCE_HOST/$INSTANCE_NAME"
                }
            }
        }
    }

    post {
        always {
            script {
//                 sh "mkdir -p ${LOCAL_REPORT_DIR} && cp ./${LOCUST_REPORT_DIR}/${REPORT_FILE} ./${LOCAL_REPORT_DIR}/${REPORT_FILE}"
                publishHTML target: [
                        allowMissing: false,
                        alwaysLinkToLastBuild: false,
                        keepAll: true,
                        reportDir: "${LOCUST_REPORT_DIR}",
                        reportFiles: "${REPORT_FILE}",
                        reportName: 'Load test report'
                ]

//                 sh "docker-compose down -v"
            }
        }
    }
}