def STAGE = "Start"

pipeline {

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
    }

    agent any

    environment
            {
                MODULE_NAME = 'workteam-backend-ms'
                repository_name = "workteam-backend-ms"
                repository_base = "WorkTeam"
                repository_url = "https://powerqubit@dev.azure.com/powerqubit/${repository_base}/_git/${repository_name}"
                groupId = "powerqubit.com"
                artifactId = "workteam-backend-ms"

                nextVersionType = 'minor'
                isRelease = "false"
                buildModules = "false"

                gitCommitPrefix = "[ci]"
                gitCommitUser = "Jenkins Continuous Integration"
                gitCommitEmail = "geral@powerqubit.com.pt"
                extension = 'jar'

                AZURE_SUBSCRIPTION_ID='9b178850-08f4-48e4-b576-29b15e367506'
                AZURE_TENANT_ID='29f0d6f6-d17f-4c53-ba91-50fd8e005c32'
                AZURE_DEVOPS_CREDENTIALS_ID = '9b65df6b-1e10-42b4-9015-ae393ddb1b57'

                DOCKER_REGISTRY = 'powerqubit.azurecr.io'
                CONTAINER_REGISTRY='powerqubit'
                RESOURCE_GROUP='pqGroup'
            }


    stages {

        stage ('Clone repository')
                {
                    steps
                            {
                                checkout scm
                            }
                }


        stage ('Pipeline')
                {
                    steps
                            {

                                script
                                        {
                                            currentVersion = sh (
                                                    script: 'mvn -q -Dexec.executable="echo" -Dexec.args=\'${project.version}\' --non-recursive org.codehaus.mojo:exec-maven-plugin:1.6.0:exec',
                                                    returnStdout: true
                                            ).trim()

                                            echo "Current version: ${currentVersion}"
                                        }

                                script
                                        {
                                                targetEnvironment = "dev"
                                                isRelease = "false"
                                                buildModules = "true"
                                        }
                                echo "Target environment: ${targetEnvironment}"
                            }
                }

        stage ('Perform release')
                {
                    when {
                        expression {
                            "${isRelease}" == 'true'
                        }
                    }
                    steps
                            {
                                echo "Release version: ${fullReleaseVersion}"
                                sh ("mvn versions:set -DnewVersion=${fullReleaseVersion}")
                                sh ("git add pom.xml")
                                sh ("git commit -m \"${gitCommitPrefix} set release version ${fullReleaseVersion}\"")
                                sh ("git push --set-upstream origin master")
                                script { currentVersion = "${fullReleaseVersion}" }
                            }
                }


        stage ('Build micro-service')
                {
                    when {
                        expression {
                            "${buildModules}" == 'true'
                        }
                    }
                    steps
                            {
                                script {
                                    if (isRelease == "true") {
                                        buildSuffix = "deploy"
                                    } else {
                                        buildSuffix = "install"
                                    }
                                }
                                sh ("mvn clean ${buildSuffix} -DskipTests -P linux")
                            }
                }




        stage('Build Docker Image') {
            steps {
                script {
                    def customImageTag = "${DOCKER_REGISTRY}/${DOCKER_IMAGE}:${BUILD_NUMBER}"

                        withCredentials([usernamePassword(credentialsId: 'db6fb655-60dc-4fb6-ab8d-0e19caa1cbe1', usernameVariable: 'lcrbneves', passwordVariable: '2Sq9he3c!')]) {
                            sh "docker login -u lcrbneves -p 2Sq9he3c!"
                            sh 'docker push lcrbneves/ndata-test-ms:latest'
                        }
                }
            }
        }


        stage ('Deploy micro-service to Dev environment')
                {
                    when
                            {
                                expression
                                        {
                                            "${targetEnvironment}" == 'dev'
                                        }
                            }
                    steps
                            {
                                script
                                        {
                                            dockerTag = "${targetEnvironment}"
                                            useContext = "${targetEnvironment}"
                                        }
                                sh ("kubectl config use-context k8app")
                                sh ("kubectl replace --force -f deployment-${targetEnvironment}.yaml")
                            }
                }




//         stage ('Prepare next development iteration')
//                 {
//                     when
//                             {
//                                 expression
//                                         {
//                                             "${targetEnvironment}" == 'prd'
//                                         }
//                             }
//                     steps
//                             {
//
//                                 step([$class: 'WsCleanup'])
//
//                                 git branch: "develop",
//                                         url: "${repository_url}"
//
//                                 script
//                                         {
//                                             sh ('git merge origin/main')
//                                         }
//
//                                 script
//                                         {
//                                             sh ("git tag -a ${currentVersion} -m \"${gitCommitPrefix} release version ${currentVersion}\"")
//                                             sh ("git push origin ${currentVersion}")
//
//                                             echo "Next version type: ${nextVersionType}"
//
//                                             nextVersion = incrementVersion(nextVersionType, currentVersion)
//
//                                             echo "Next version: ${nextVersion}"
//
//                                             sh ("mvn versions:set -DnewVersion=${nextVersion}-SNAPSHOT")
//
//                                             sh ('git add pom.xml')
//                                             sh ("git commit -m '${gitCommitPrefix} preparing next development iteration for version ${nextVersion}'")
//                                             sh ('git push --set-upstream origin develop')
//                                         }
//                             }
//                 }
     }
}