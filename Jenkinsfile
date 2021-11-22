@Library('pipeline-library') _

pipeline {
    agent {
        label 'ec2-jdk11'
    }

    options {
        ansiColor('xterm')
        copyArtifactPermission("$JOB_BASE_NAME");
    }

    parameters {
        choice(name: 'comparison', choices: ['Previous', 'Baseline', 'Both'], description: 'Which results to compare?')
    }

    environment {
//         AWX_BOT_CREDENTIALS = credentials('awx-bot-user-credentials')
        LOCUST_REPORT_DIR = "reports"
        HTML_REPORT_FILE = "test_report.html"
        CSV_REPORT_FILE = "dhis_stats.csv"
        COMPARISON_FILE = "comparison_results.html"
        COMPARISON_COLUMN = "90%"
        INSTANCE_HOST = "https://test.performance.dhis2.org"
        INSTANCE_NAME = "2.37.0"
        COMPOSE_ARGS = "NO_WEB=true TIME=30s HATCH_RATE=1 USERS=5 TARGET=$INSTANCE_HOST/$INSTANCE_NAME"
        S3_BUCKET = "s3://dhis2-performance-tests-results"
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

        stage('Start Locust') {
            steps {
                sh "mkdir -p $LOCUST_REPORT_DIR"
                sh "docker-compose build"
                sh "$COMPOSE_ARGS docker-compose up"
            }
        }

        stage('Copy previous reports') {
            when {
                expression { currentBuild.previousSuccessfulBuild != null }
                anyOf {
                    expression { params.comparison == "Previous" }
                    expression { params.comparison == "Both" }
                }
            }

            steps {
                copyArtifacts(
                    projectName: "$JOB_NAME",
                    selector: specific("${currentBuild.previousSuccessfulBuild.number}"),
                    filter: "$LOCUST_REPORT_DIR/$CSV_REPORT_FILE",
                    flatten: true,
                    target: "previous_$LOCUST_REPORT_DIR"
                )
            }
        }

        stage('Copy baseline reports') {
            when {
                anyOf {
                    expression { params.comparison == "Baseline" }
                    expression { params.comparison == "Both" }
                }
            }

            steps {
                sh "mkdir -p baseline_$LOCUST_REPORT_DIR"
                sh "aws s3 cp $S3_BUCKET/baseline_$CSV_REPORT_FILE baseline_$LOCUST_REPORT_DIR/$CSV_REPORT_FILE"
            }
        }

        stage('Checkout comparer') {
            steps {
                dir('locust-compare') {
                    git url: 'https://github.com/radnov/Locust-Compare'
                    sh 'pip3 install -r requirements.txt'
                }
            }
        }

        // TODO: Create shared library function
        stage('Compare to previous Locust report') {
            when {
                expression { currentBuild.previousSuccessfulBuild != null }
                anyOf {
                    expression { params.comparison == "Previous" }
                    expression { params.comparison == "Both" }
                }
            }

            steps {
                dir('locust-compare') {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        sh """
                            python3 locust_compare.py \
                            $WORKSPACE/previous_$LOCUST_REPORT_DIR/$CSV_REPORT_FILE \
                            $WORKSPACE/$LOCUST_REPORT_DIR/$CSV_REPORT_FILE \
                            --column-name $COMPARISON_COLUMN \
                            --output $WORKSPACE/previous_$COMPARISON_FILE
                        """
                    }
                }
            }

            post {
                always {
                    archiveArtifacts artifacts: "previous_$COMPARISON_FILE"
                }

                failure {
                    script {
                        slackSend(
                            color: '#ff0000',
                            message: "<${BUILD_URL}|${JOB_NAME} (#${BUILD_NUMBER})>: performance is getting worse!\nCheck <${BUILD_URL}artifact/previous_${COMPARISON_FILE}|comparison to previous results>.",
                            channel: '@U01RSD1LPB3'
                        )
                    }
                }
            }
        }

        // TODO: Create shared library function
        stage('Compare to baseline Locust report') {
            when {
                anyOf {
                    expression { params.comparison == "Baseline" }
                    expression { params.comparison == "Both" }
                }
            }

            steps {
                dir('locust-compare') {
                    catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                        sh """
                            python3 locust_compare.py \
                            $WORKSPACE/baseline_$LOCUST_REPORT_DIR/$CSV_REPORT_FILE \
                            $WORKSPACE/$LOCUST_REPORT_DIR/$CSV_REPORT_FILE \
                            --column-name $COMPARISON_COLUMN \
                            --output $WORKSPACE/baseline_$COMPARISON_FILE
                        """
                    }
                }
            }

            post {
                always {
                    archiveArtifacts artifacts: "baseline_$COMPARISON_FILE"
                }

                failure {
                    script {
                        slackSend(
                            color: '#ff0000',
                            message: "<${BUILD_URL}|${JOB_NAME} (#${BUILD_NUMBER})>: performance is getting worse!\nCheck <${BUILD_URL}artifact/baseline_${COMPARISON_FILE}|comparison to baseline results>.",
                            channel: '@U01RSD1LPB3'
                        )
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
