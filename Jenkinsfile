@Library('pipeline-library') _

pipeline {
    agent {
        label 'ec2-jdk11'
    }

    environment {
//         AWX_BOT_CREDENTIALS = credentials('awx-bot-user-credentials')
        IMAGE_NAME = "dhis2/locustio-test"
        IMAGE_TAG = "latest"
        COMPOSE_ARGS = "NO_WEB=true TIME=30s HATCH_RATE=1 USERS=5"
        LOCUST_REPORT_DIR = "reports"
        HTML_REPORT_FILE = "test_report.html"
        CSV_REPORT_FILE = "dhis_stats.csv"
        INSTANCE_HOST = "test.performance.dhis2.org"
        INSTANCE_NAME = "2.37.0"
    }

    stages {
        stage('Update performance test instance') {
            steps {
                echo 'Updating performance test instance ...'
//                 script {
//                     awx.resetWar("$AWX_BOT_CREDENTIALS", "${INSTANCE_HOST}", "${INSTANCE_NAME}")
//                 }
            }
        }

        stage('Start Locust master') {
            steps {
                sh "mkdir -p $LOCUST_REPORT_DIR"
                sh "docker build -t $IMAGE_NAME:$IMAGE_TAG ./docker"
                sh "$COMPOSE_ARGS docker-compose up -d"
            }
        }

        stage('Run tests') {
            steps {
                sh "mvn clean compile exec:java -Dtarget.base_uri=https://$INSTANCE_HOST/$INSTANCE_NAME"
            }
        }

        stage('Copy previous reports') {
            steps {
                copyArtifacts(
                    projectName: currentBuild.projectName,
                    selector: specific("${currentBuild.previousSuccessfulBuild.number}"),
                    filter: "$LOCUST_REPORT_DIR/*.csv",
                    flatten: true,
                    target: "previous_$LOCUST_REPORT_DIR"
                )

                sh 'ls -la'
                sh "ls -la previous_$LOCUST_REPORT_DIR"
            }
        }

        stage('Compare Locust reports') {
            steps {
                dir('locust-compare') {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        git branch: 'update-for-latest-locust', url: 'https://github.com/radnov/Locust-Compare'
                        sh 'pip3 install -r requirements.txt'
                        sh """
                            python3 locust_compare.py \
                            $WORKSPACE/previous_$LOCUST_REPORT_DIR/$CSV_REPORT_FILE \
                            $WORKSPACE/$LOCUST_REPORT_DIR/$CSV_REPORT_FILE \
                            --column-name 90%
                        """
                    }
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: "$LOCUST_REPORT_DIR/*.csv"

            publishHTML target: [
                allowMissing: false,
                alwaysLinkToLastBuild: false,
                keepAll: true,
                reportDir: "$LOCUST_REPORT_DIR",
                reportFiles: "$HTML_REPORT_FILE",
                reportName: 'Load test report'
            ]
        }
    }
}
