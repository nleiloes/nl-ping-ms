@Library('jenkins-libs')_

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
        MODULE_NAME = 'ms-pingteste'
        repository_name = "pqtml-pingteste-ms"
        repository_url = "git@ssh.dev.azure.com:v3/LC-Production/AMTL/${repository_name}"

        groupId = "pt.tml"
		artifactId = "pingteste-ms"

        DOCKER_REGISTRY = 'fra.ocir.io/frdvdrigd38a'

		nextVersionType = 'minor'
        isRelease = "false"
        buildModules = "false"

		gitCommitPrefix = "[ci]"
		gitCommitUser = "Jenkins Continuous Integration"
		gitCommitEmail = "luisneves1984@gmail.com"

		extension = 'jar'

        webHookUrlSuccess = ""
        webHookUrlFailures = ""
    
        USER_CREDENTIALS = credentials('')
    }


    stages {

        stage ('Clone repository')
		{
			steps
			{
                script { STAGE = getCurrentStage() }

				step([$class: 'WsCleanup'])

				withCredentials([usernamePassword(credentialsId: 'deploy.tml', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {

					script {
						repository_url = "${GIT_URL}"
						repository_url = repository_url.replaceAll("https://OTLIS@dev.azure.com","https://$USERNAME:$PASSWORD@dev.azure.com")
					}

					git branch: "${BRANCH_NAME}",
					url: "${repository_url}"

					sh ("git remote set-url origin ${repository_url}")

					sh ("git config user.name '${gitCommitUser}'")
					sh ("git config user.email '${gitCommitEmail}'")

                    sh ("git submodule update --init")
                    sh ("git submodule update --remote")

                }
			}
		}

        stage ('Initialize pipeline')
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
                    //sh("git-secret tell '${USER_CREDENTIALS_USR}'")
                    //sh("git-secret reveal -p '${USER_CREDENTIALS_PSW}'")
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

        stage('Dependency Analysis') {
            when {
				expression {
					"${targetEnvironment}" == 'dev'
				}
			}
            steps {
                script { STAGE = getCurrentStage() }

                sh "mkdir odc-reports || echo 'OK'"
                sh "mkdir report || echo 'OK'"

                sh "dependency-check || echo 'OK'"
				
				junit skipPublishingChecks: true, testResults: 'odc-reports/dependency-check-junit.xml'
                archiveArtifacts artifacts: "odc-reports/*", onlyIfSuccessful: true
            }
        }

        stage ('Archive modules')
		{
			when {
				expression {
					"${buildModules}" == 'true'
				}
			}
            steps
			{
                script { STAGE = getCurrentStage() }

                script
                {
                    dir("target")
                    {
                        sh ("rm *.${extension}.original")

                        distArchive = sh (
                                script: 'ls | grep ' + extension + ' | tail -n 1 || echo NOT_FOUND',
                                returnStdout: true
                            ).trim()

                            archive (distArchive)
                            stash includes: distArchive , name: "${MODULE_NAME}"
                    }
                }
            }
        }

		stage ('Download modules from Nexus for Production deployment')
		{
			when {
				expression {
					"${buildModules}" == 'false'
				}
			}
			steps
			{
				sh ('mkdir target')

				dir ('target')
                {
                    script
                    {
                        echo "Downloading module: ${artifactId}-${currentVersion}"

                        sh("mvn com.googlecode.maven-download-plugin:download-maven-plugin:artifact -DgroupId=${groupId} -DartifactId=${artifactId} -Dversion=${currentVersion} -DoutputDirectory=\".\"")

						distArchive = "${artifactId}-${currentVersion}.${extension}"

                        archive (distArchive)
                        stash includes: distArchive , name: "${MODULE_NAME}"
                    }
                }
			}
		}

        stage('Build docker image')
        {
            steps
            {
                script { STAGE = getCurrentStage() }

                sh "sudo docker build --rm -t ${DOCKER_REGISTRY}/${MODULE_NAME}:${currentVersion} ."
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

                sh "newman run 'common-configs/Setup/Postman/init.postman_collection.json' --folder 'Init' --suppress-exit-code --env-var 'context=remote'  --environment 'common-configs/Postman/${targetEnvironment}.postman_environment.json' --iteration-data src/main/resources/integration/solace_queue.csv"
                sh "newman run 'common-configs/Setup/Postman/init.postman_collection.json' --folder 'Subscription' --suppress-exit-code --env-var 'context=remote'  --environment 'common-configs/Postman/${targetEnvironment}.postman_environment.json' --iteration-data src/main/resources/integration/solace_subscriptions.csv"

                sh "sudo docker tag ${DOCKER_REGISTRY}/${MODULE_NAME}:${currentVersion} ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}"
                sh "sudo docker push ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}"

                sh "kubectl config use-context ${useContext}k8app"
                sh "kubectl replace --force -f deployment-${targetEnvironment}.yaml"
			}
		}

		stage ('Deploy micro-service to Test environment')
		{
			when
			{
				expression
				{
					"${targetEnvironment}" == 'tst'
				}
			}
			steps
			{
                script { STAGE = getCurrentStage() }

				timeout(time: 60, unit: 'SECONDS') {
                    input 'Deploy to Test environment?'
                }

                script
                {
                    dockerTag = "${targetEnvironment}"
                    useContext = "${targetEnvironment}"
                }

                sh "sudo docker tag ${DOCKER_REGISTRY}/${MODULE_NAME}:${currentVersion} ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}"
                sh "sudo docker push ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}"

                sh "kubectl config use-context ${useContext}k8app"
                sh "kubectl replace --force -f deployment-${targetEnvironment}.yaml"
			}
		}

        stage ('Deploy micro-service to Production environment')
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

                timeout(time: 60, unit: 'SECONDS') {
                    input 'Deploy to Production environment?'
                }

                script
                {
                    dockerTag = "latest"
                    useContext = "${targetEnvironment}"
                }

                sh "sudo docker tag ${DOCKER_REGISTRY}/${MODULE_NAME}:${currentVersion} ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}"
                sh "sudo docker push ${DOCKER_REGISTRY}/${MODULE_NAME}:${dockerTag}"

                sh "kubectl config use-context ${useContext}k8app"
                sh "kubectl replace --force -f deployment-${targetEnvironment}.yaml"
			}
		}

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
    }
    post{
        always{
            step([$class: 'WsCleanup'])
			sh ('docker image prune -af')
        }
        aborted {
            office365ConnectorSend webhookUrl: "${webHookUrlFailures}",
                factDefinitions: [[name: "Deployment rejected", attachLog: true,template: "${STAGE}"]]
        }
        failure {
            office365ConnectorSend webhookUrl: "${webHookUrlFailures}",
                factDefinitions: [[name: "Fail Stage", attachLog: true,template: "${STAGE}"]]
        }
        success {
            office365ConnectorSend webhookUrl: "${webHookUrlSuccess}"
        }
    }
}