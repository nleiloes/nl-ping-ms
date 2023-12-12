pipeline {
    agent any
    options {
        disableConcurrentBuilds()
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }
    environment {
        //Use Pipeline Utility Steps plugin to read information from pom.xml into environment variables
        ARTIFACT_ID = "ping"
        ARTIFACT_VERSION = "0.0.1-SNAPSHOT"
    }

    stages {
        stage ('Build Application') {
            steps {
                sh 'mvn clean install'
            }
            post {
                success {
                    sh 'echo Build succeed'
                }
                failure {
                    sh 'echo Build failed, Sending notification....'
                    // logic to send notification
                }
            }
        }

        stage ('Build Docker Image') {
            steps {
                sh "docker login -u nleiloes -p 2Sq9he3c!"
                sh "docker build --rm -t nleiloes/${ARTIFACT_ID}:${ARTIFACT_VERSION} ."
                sh "docker push nleiloes/${ARTIFACT_ID}:${ARTIFACT_VERSION}"
            }
        }

        stage ('Kubernetes Deploy') {
            steps {
                sh "whoami"
                sh "kubectl get pods -v=6"
                sh "kubectl config use-context k8app"
                sh "kubectl replace --force -f deployment-dev.yaml"
            }
        }
    }
}