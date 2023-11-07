pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'ndata-test-ms'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_REGISTRY_CREDENTIALS = 'db6fb655-60dc-4fb6-ab8d-0e19caa1cbe1'
        KUBE_NAMESPACE = 'default'
        KUBE_DEPLOYMENT = 'your-kubernetes-deployment'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def customImageTag = "${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}"

                        withCredentials([usernamePassword(credentialsId: 'db6fb655-60dc-4fb6-ab8d-0e19caa1cbe1', usernameVariable: 'lcrbneves', passwordVariable: '2Sq9he3c!')]) {
                            sh "docker login -u lcrbneves -p 2Sq9he3c!"
//                             sh "docker image build -t ndata-test-ms ."
                            sh 'docker push lcrbneves/ndata-test-ms:latest'
                        }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
//                     sh "kubectl config view -o jsonpath='{.current-context}'"
                    sh "kubectl config use-context k8app"
                    sh "kubectl set image deployment/${KUBE_DEPLOYMENT} your-container-name=${DOCKER_IMAGE}:${BUILD_NUMBER} -n ${KUBE_NAMESPACE}"
                }
            }
        }

        stage('Clean Up') {
            steps {
                sh 'mvn clean'
            }
        }
    }

    post {
        success {
            echo 'Deployment successful'
        }
        failure {
            echo 'Deployment failed'
        }
    }
}