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
                sh """
                echo IMAGE: ${ARTIFACT_ID}
                echo VERSION: ${ARTIFACT_VERSION}
                """
//                 sh """
//                 echo IMAGE: ${ARTIFACT_ID}
//                 echo VERSION: ${ARTIFACT_VERSION}
//                 docker build -f deploy/Dockerfile \
//                 --build-arg JAR_FILE=target/${ARTIFACT_ID}-${ARTIFACT_VERSION}.jar \
//                 --build-arg IMAGE_VERSION=${ARTIFACT_VERSION} \
//                 -t ${ARTIFACT_ID}:${ARTIFACT_VERSION} .
//                 docker login -u lcrbneves -p 2Sq9he3c!
//                 docker push lcrbneves/${ARTIFACT_ID}:${ARTIFACT_VERSION}
//                 """
                sh "docker build --rm -t lcrbneves/${ARTIFACT_ID}:${ARTIFACT_VERSION} ."
            }
        }

        stage ('Kubernetes Deploy') {
        when { expression { env.DEPLOYMENT.toBoolean() }}
            steps {
                sh ("kubectl config use-context k8app")
                sh ("kubectl replace --force -f deployment-dev.yaml")
            }
        }
    }
}