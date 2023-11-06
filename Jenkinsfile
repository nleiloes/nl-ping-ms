pipeline {
    agent any

    environment {
        DOCKER_IMAGE = 'ndata-test-ms'
        DOCKER_REGISTRY = 'docker.io'
        DOCKER_REGISTRY_CREDENTIALS = 'db6fb655-60dc-4fb6-ab8d-0e19caa1cbe1'
        KUBE_NAMESPACE = 'your-kubernetes-namespace'
        KUBE_DEPLOYMENT = 'your-kubernetes-deployment'
        DOCKER_USER = 'lcrbneves'
        DOCKER_PASSWORD = '2Sq9he3c!'
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
                    def dockerImage = docker.build(customImageTag, '.')
                    withCredentials([usernamePassword(credentialsId: DOCKER_REGISTRY_CREDENTIALS, usernameVariable: 'pq', passwordVariable: 'P0w3rB3st')]) {
                        docker.withRegistry(DOCKER_REGISTRY, DOCKER_USER, DOCKER_PASSWORD) {
                            dockerImage.push()
                        }
                    }
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                script {
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