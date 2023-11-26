def STAGE = "Start"

pipeline {

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '10'))
    }

    agent {
        node {
            label 'local_node'
        }
    }

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
                                script { STAGE = getCurrentStage() }

                                echo "Branch name: ${BRANCH_NAME}"
                                echo "Build Id: ${BUILD_ID}"

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
                                            if ("${BRANCH_NAME}" == 'main')
                                            {
                                                targetEnvironment = "prd"
                                                isRelease = "false"
                                                buildModules = "false"
                                            }
                                            else if ("${BRANCH_NAME}" == 'develop')
                                            {
                                                targetEnvironment = "dev"
                                                isRelease = "false"
                                                buildModules = "true"
                                            }
                                            else if ("${BRANCH_NAME}".contains("release"))
                                            {
                                                targetEnvironment = "tst"
                                                isRelease = "true"
                                                buildModules = "true"

                                                releaseVersion = sh (
                                                        script: "echo $BRANCH_NAME | cut -d'/' -f 2",
                                                        returnStdout: true
                                                ).trim()

                                                echo "Release version: ${releaseVersion}"
                                                fullReleaseVersion = "${releaseVersion}.${BUILD_ID}"
                                                echo "Full Release version: ${fullReleaseVersion}"
                                            }
                                            else
                                            {
                                                targetEnvironment = "invalid"
                                            }
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
                                script { STAGE = getCurrentStage() }
                                echo "Release version: ${fullReleaseVersion}"
                                sh ("mvn versions:set -DnewVersion=${fullReleaseVersion}")
                                sh ("git add pom.xml")
                                sh ("git commit -m \"${gitCommitPrefix} set release version ${fullReleaseVersion}\"")
                                sh ("git push --set-upstream origin ${BRANCH_NAME}")
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
                                script { STAGE = getCurrentStage() }

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




//         stage('Dependency Analysis') {
//             when {
// 				expression {
// 					"${buildModules}" == 'true'
// 				}
// 			}
//             steps {
//                 script { STAGE = getCurrentStage() }
//
//                 sh "mkdir odc-reports || echo 'OK'"
//                 sh "mkdir report || echo 'OK'"
//
//                 sh 'dependency-check'
//
// 				archiveArtifacts artifacts: "odc-reports/*", onlyIfSuccessful: true
//             }
//         }

//         stage ('Run SonarQube Scanner Analysis'){
//             when {
// 				expression {
// 					"${buildModules}" == 'true'
// 				}
// 			}
//             steps {
//                 withSonarQubeEnv('LocalSonar'){
//                     sh "mvn sonar:sonar"
//                 }
//             }
//         }

//         stage ('Archive modules')
// 		{
// 			when {
// 				expression {
// 					"${buildModules}" == 'true'
// 				}
// 			}
//             steps
// 			{
//                 script { STAGE = getCurrentStage() }
//
//                 script
//                 {
//                     dir("target")
//                     {
//                         sh ("rm *.${extension}.original")
//
//                         distArchive = sh (
//                                 script: 'ls | grep ' + extension + ' | tail -n 1 || echo NOT_FOUND',
//                                 returnStdout: true
//                             ).trim()
//
//                             archive (distArchive)
//                             stash includes: distArchive , name: "${MODULE_NAME}"
//                     }
//                 }
//             }
//         }
//
// 		stage ('Download modules from Nexus for Production deployment')
// 		{
// 			when {
// 				expression {
// 					"${buildModules}" == 'false'
// 				}
// 			}
// 			steps
// 			{
// 				sh ('mkdir target')
//
// 				dir ('target')
//                 {
//                     script
//                     {
//                         echo "Downloading module: ${artifactId}-${currentVersion}"
//
//                         sh("mvn com.googlecode.maven-download-plugin:download-maven-plugin:artifact -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${currentVersion} -DoutputDirectory=\".\"")
//
// 						distArchive = "${artifactId}-${currentVersion}.${extension}"
//
//                         archive (distArchive)
//                         stash includes: distArchive , name: "${MODULE_NAME}"
//                     }
//                 }
// 			}
// 		}


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
                                script { STAGE = getCurrentStage() }
                                script
                                        {
                                            dockerTag = "${targetEnvironment}"
                                            useContext = "${targetEnvironment}"
                                        }

//                                 sh ("docker tag ${DOCKER_REGISTRY}/${MODULE_NAME}:${currentVersion} ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}")
//
//                                 sh ("docker login -u jenkins-token -p kKHQnDZbDzekaUMRXve3rXUmhA786xNkStdsk3/0fU+ACRDM97Dm powerqubit.azurecr.io")
//                                 sh ("docker push ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}")

                                sh ("kubectl config use-context k8app")
                                sh ("kubectl replace --force -f deployment-${targetEnvironment}.yaml")

                            }
                }


//         stage ('Deploy micro-service to Test environment')
//                 {
//                     when
//                             {
//                                 expression
//                                         {
//                                             "${targetEnvironment}" == 'tst'
//                                         }
//                             }
//                     steps
//                             {
//                                 script { STAGE = getCurrentStage() }
//
//                                 timeout(time: 60, unit: 'SECONDS') {
//                                     input 'Deploy to Test environment?'
//                                 }
//
//                                 script
//                                         {
//                                             dockerTag = "${targetEnvironment}"
//                                             useContext = "${targetEnvironment}"
//                                         }
//                             }
//                 }
//
//         stage ('Deploy micro-service to Production environment')
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
//                                 script { STAGE = getCurrentStage() }
//
//                                 timeout(time: 60, unit: 'SECONDS') {
//                                     input 'Deploy to Production environment?'
//                                 }
//
//                                 script
//                                         {
//                                             dockerTag = "latest"
//                                             useContext = "${targetEnvironment}"
//                                         }
//                             }
//                 }


        stage ('Prepare next development iteration')
                {
                    when
                            {
                                expression
                                        {
                                            "${targetEnvironment}" == 'prd'
                                        }
                            }
                    steps
                            {
                                script { STAGE = getCurrentStage() }

                                step([$class: 'WsCleanup'])

                                git branch: "develop",
                                        url: "${repository_url}"

                                script
                                        {
                                            sh ('git merge origin/main')
                                        }

                                script
                                        {
                                            sh ("git tag -a ${currentVersion} -m \"${gitCommitPrefix} release version ${currentVersion}\"")
                                            sh ("git push origin ${currentVersion}")

                                            echo "Next version type: ${nextVersionType}"

                                            nextVersion = incrementVersion(nextVersionType, currentVersion)

                                            echo "Next version: ${nextVersion}"

                                            sh ("mvn versions:set -DnewVersion=${nextVersion}-SNAPSHOT")

                                            sh ('git add pom.xml')
                                            sh ("git commit -m '${gitCommitPrefix} preparing next development iteration for version ${nextVersion}'")
                                            sh ('git push --set-upstream origin develop')
                                        }
                            }
                }

//     post{
//         always{
//             step([$class: 'WsCleanup'])
// //
//         }
// //         aborted {
// //             office365ConnectorSend webhookUrl: "${webHookUrlFailures}",
// //                 factDefinitions: [[name: "Deployment rejected", attachLog: true,template: "${STAGE}"]]
// //         }
// //         failure {
// //             office365ConnectorSend webhookUrl: "${webHookUrlFailures}",
// //                 factDefinitions: [[name: "Fail Stage", attachLog: true,template: "${STAGE}"]]
// //         }
// //         success {
// //             office365ConnectorSend webhookUrl: "${webHookUrlSuccess}"
// //         }
//
    }
}