@Library('pipeline-library') _

pipeline {
    agent {
        label 'ec2-jdk11'
    }

    triggers {
        upstream(upstreamProjects: "dhis2-core-canary/${env.BRANCH_NAME.replaceAll('DEVOPS-30', 'master')}", threshold: hudson.model.Result.SUCCESS)
    }

    options {
        ansiColor('xterm')
        copyArtifactPermission("$JOB_BASE_NAME")
    }

    parameters {
        string(name: 'LOCUST_IMAGES_TAG', defaultValue: '', description: 'Which version of the Locust master and worker to use?')
        string(name: 'TARGET_INSTANCE', defaultValue: '', description: 'Which instance to target?')
        string(name: 'TIME', defaultValue: '60m', description: 'How much time to run the tests for?')
        string(name: 'USERS', defaultValue: '250', description: 'How much users?')
        string(name: 'RATE', defaultValue: '10', description: 'At what rate to add users?')
        string(name: 'METRICS', defaultValue: 'Average Response Time,90%', description: 'Which report metric columns to compare?\n(comma-separated list of strings)')
        choice(name: 'REPORT', choices: ['Both', 'Baseline', 'Previous'], description: 'Which report/s to compare with?')
    }

    environment {
        AWX_BOT_CREDENTIALS = credentials('awx-bot-user-credentials')
        DHIS2_CREDENTIALS = credentials('dhis2-default')
        AWX_TEMPLATE_ID = '70'
        IMAGE_TAG = "${params.LOCUST_IMAGES_TAG != '' ? params.LOCUST_IMAGES_TAG : '0.1.0' }"
        LOCUST_REPORT_DIR = 'reports'
        HTML_REPORT_FILE = 'test_report.html'
        CSV_REPORT_FILE = 'dhis_stats.csv'
        COMPARISON_FILE = 'comparison_results.html'
        CURRENT_REPORT = "$WORKSPACE/$LOCUST_REPORT_DIR/$CSV_REPORT_FILE"
        PREVIOUS_REPORT = "$WORKSPACE/$LOCUST_REPORT_DIR/previous_$CSV_REPORT_FILE"
        BASELINE_REPORT = "$WORKSPACE/$LOCUST_REPORT_DIR/baseline_$CSV_REPORT_FILE"
        REF_BASED_NAME = "${env.TAG_NAME ? env.TAG_NAME : env.GIT_BRANCH.replaceAll('DEVOPS-30', 'dev')}"
        INSTANCE_HOST = 'test.performance.dhis2.org'
        INSTANCE_NAME = "${params.TARGET_INSTANCE != '' ? params.TARGET_INSTANCE : env.REF_BASED_NAME}"
        INSTANCE_URL = "https://${env.INSTANCE_HOST}/${env.INSTANCE_NAME}"
        COMPOSE_ARGS = "NO_WEB=true TIME=${params.TIME} HATCH_RATE=${params.RATE} USERS=${params.USERS} TARGET=${env.INSTANCE_URL} MASTER_HOST=master"
        S3_BUCKET = 's3://dhis2-performance-tests-results'
        HTTP = 'https --check-status'
    }

    stages {
        stage('Reset WAR and DB') {
            steps {
                echo 'Resetting performance tests instance DB ...'
                 script {
                     awx.launchJob("$AWX_BOT_CREDENTIALS", "$INSTANCE_HOST", "$INSTANCE_NAME", 'reset_war_and_db', "$AWX_TEMPLATE_ID")

                     NOTIFIER_ENDPOINT = dhis2.generateAnalytics("$INSTANCE_URL", '$DHIS2_CREDENTIALS')
                     timeout(120) {
                         waitFor.analyticsCompleted("${INSTANCE_URL}${NOTIFIER_ENDPOINT}", '$DHIS2_CREDENTIALS')
                     }
                 }
            }
        }

        stage('Run Locust tests') {
            steps {
                script {
                    sh "mkdir -p $LOCUST_REPORT_DIR"
                    sh "docker-compose pull"
                    sh "$COMPOSE_ARGS docker-compose up --abort-on-container-exit"
                }
            }
        }

        stage('Copy previous reports') {
            when {
                expression { currentBuild.previousSuccessfulBuild != null }
                anyOf {
                    expression { params.REPORT == 'Previous' }
                    expression { params.REPORT == 'Both' }
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
                    expression { params.REPORT == 'Baseline' }
                    expression { params.REPORT == 'Both' }
                }
            }

            steps {
                sh "aws s3 cp ${S3_BUCKET}/${REF_BASED_NAME}_baseline_${CSV_REPORT_FILE} $BASELINE_REPORT"
            }
        }

        stage('Compare reports') {
            steps {
                dir('csvcomparer') {
                    git url: 'https://github.com/dhis2-sre/csvcomparer'
                    sh 'pip3 install .'

                    script {
                        catchError(buildResult: 'SUCCESS', stageResult: 'FAILURE') {
                            switch(params.REPORT) {
                                case 'Previous':
                                    if (currentBuild.previousSuccessfulBuild != null) {
                                        COMPARISON_FILE = "previous_$COMPARISON_FILE"
                                        sh "csvcomparer --loglevel info --current $CURRENT_REPORT --previous $PREVIOUS_REPORT --column-name \"${params.METRICS}\" --output $WORKSPACE/$COMPARISON_FILE"
                                    }
                                    break
                                case 'Baseline':
                                    COMPARISON_FILE = "baseline_$COMPARISON_FILE"
                                    sh "csvcomparer --loglevel info --current $CURRENT_REPORT --previous $BASELINE_REPORT --column-name \"${params.METRICS}\" --output $WORKSPACE/$COMPARISON_FILE"
                                    break
                                case 'Both':
                                    if (currentBuild.previousSuccessfulBuild != null) {
                                        sh "csvcomparer --loglevel info --current $CURRENT_REPORT --previous $BASELINE_REPORT $PREVIOUS_REPORT --column-name \"${params.METRICS}\" --output $WORKSPACE/$COMPARISON_FILE"
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
