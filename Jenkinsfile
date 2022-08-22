@Library('pipeline-library') _

pipeline {
    agent {
        label 'ec2-jdk11'
    }

    options {
        ansiColor('xterm')
        copyArtifactPermission("$JOB_BASE_NAME")
    }

    parameters {
        string(name: 'INSTANCE', defaultValue: 'dev', description: 'Which instance to target?')
//        string(name: 'INSTANCE', defaultValue: 'rado-test-20399', description: 'Which instance to target?')
        string(name: 'TIME', defaultValue: '60m', description: 'How much time to run the tests for?')
        string(name: 'USERS', defaultValue: '100', description: 'How much users?')
        string(name: 'RATE', defaultValue: '10', description: 'At what rate to add users?')
        string(name: 'COLUMN', defaultValue: 'Average Response Time,90%', description: 'Which column to compare?\n(comma-separated list of strings)')
        choice(name: 'REPORT', choices: ['Baseline', 'Previous', 'Both'], description: 'Which report/s to compare with?')
    }

    environment {
        //AWX_BOT_CREDENTIALS = credentials('awx-bot-user-credentials')
        LOCUST_REPORT_DIR = "reports"
        HTML_REPORT_FILE = "test_report.html"
        CSV_REPORT_FILE = "dhis_stats.csv"
        COMPARISON_FILE = "comparison_results.html"
        CURRENT_REPORT = "$WORKSPACE/$LOCUST_REPORT_DIR/$CSV_REPORT_FILE"
        PREVIOUS_REPORT = "$WORKSPACE/$LOCUST_REPORT_DIR/previous_$CSV_REPORT_FILE"
        BASELINE_REPORT = "$WORKSPACE/$LOCUST_REPORT_DIR/baseline_$CSV_REPORT_FILE"
        INSTANCE_HOST = "https://test.performance.dhis2.org"
//        INSTANCE_HOST = "https://whoami.im.radnov.test.c.dhis2.org"
        COMPOSE_ARGS = "NO_WEB=true TIME=${params.TIME} HATCH_RATE=${params.RATE} USERS=${params.USERS} TARGET=$INSTANCE_HOST/${params.INSTANCE}"
        S3_BUCKET = "s3://dhis2-performance-tests-results"
    }

    stages {
        stage('Update performance test instance') {
            steps {
                echo 'Updating performance test instance ...'
                 //script {
                 //    awx.resetWar("$AWX_BOT_CREDENTIALS", "${INSTANCE_HOST}", "${params.INSTANCE}")
                 //}
            }
        }

        stage('Run Locust tests') {
            steps {
                sh "mkdir -p $LOCUST_REPORT_DIR"
                sh "docker-compose build"
                sh "$COMPOSE_ARGS docker-compose up --abort-on-container-exit"
            }
        }

        stage('Copy previous reports') {
            when {
                expression { currentBuild.previousSuccessfulBuild != null }
                anyOf {
                    expression { params.REPORT == "Previous" }
                    expression { params.REPORT == "Both" }
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
                sh "mv previous_$LOCUST_REPORT_DIR/$CSV_REPORT_FILE $PREVIOUS_REPORT"
            }
        }

        stage('Copy baseline reports') {
            when {
                anyOf {
                    expression { params.REPORT == "Baseline" }
                    expression { params.REPORT == "Both" }
                }
            }

            steps {
                sh "aws s3 cp $S3_BUCKET/baseline_$CSV_REPORT_FILE $BASELINE_REPORT"
            }
        }

        stage('Checkout csvcomparer') {
            steps {
                dir('csvcomparer') {
                    git url: 'https://github.com/dhis2-sre/csvcomparer'
                    sh 'pip3 install .'
                }
            }
        }

        stage('Compare reports') {
            steps {
                dir('csvcomparer') {
                    script {
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            switch(params.REPORT) {
                                case 'Previous':
                                    if (currentBuild.previousSuccessfulBuild != null) {
                                        COMPARISON_FILE = "previous_$COMPARISON_FILE"
                                        sh "csvcomparer --loglevel info --current $CURRENT_REPORT --previous $PREVIOUS_REPORT --column-name \"${params.COLUMN}\" --output $WORKSPACE/$COMPARISON_FILE"
                                    }
                                    break
                                case 'Baseline':
                                    COMPARISON_FILE = "baseline_$COMPARISON_FILE"
                                    sh "csvcomparer --loglevel info --current $CURRENT_REPORT --previous $BASELINE_REPORT --column-name \"${params.COLUMN}\" --output $WORKSPACE/$COMPARISON_FILE"
                                    break
                                case 'Both':
                                    if (currentBuild.previousSuccessfulBuild != null) {
                                        sh "csvcomparer --loglevel info --current $CURRENT_REPORT --previous $BASELINE_REPORT $PREVIOUS_REPORT --column-name \"${params.COLUMN}\" --output $WORKSPACE/$COMPARISON_FILE"
                                    }
                                    break
                            }
                        }
                    }
                }
            }

            post {
                always {
                    archiveArtifacts artifacts: "$COMPARISON_FILE"
                }

                failure {
                    script {
                        slackSend(
                            color: '#ff0000',
                            message: "<${BUILD_URL}|${JOB_NAME} (#${BUILD_NUMBER})>: performance is getting worse!\nCheck <${BUILD_URL}artifact/${COMPARISON_FILE}|the comparison results>.",
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
