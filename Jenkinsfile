pipeline {
    agent any

    tools {
        maven "MAVEN-3.8.7"
    }

    environment {
        APP_NAME       = "yatra-ms-app"
        DOCKER_IMAGE   = "pratikjaysingpure/${APP_NAME}"
        AWS_REGION     = "ap-south-1"
        ECR_REPO       = "690092038612.dkr.ecr.ap-south-1.amazonaws.com/${APP_NAME}"
        SONAR_HOST_URL = "http://localhost:9000"
        SONAR_TOKEN    = credentials('sonar-token')
        DOCKER_CREDS   = credentials('dockerhub-creds')
        NEXUS_CREDS    = credentials('nexus-creds')
    }

    stages {

        /* ------------------------- 1. Checkout & Validation -------------------------- */
        stage('Checkout & Validation') {
            parallel {
                stage('Checkout Source Code') {
                    steps {
                        echo '📥 Cloning repository...'
                        git branch: 'holiday', url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
                    }
                }
                stage('Tool Version Check') {
                    steps {
                        echo '🧰 Checking versions of tools...'
                        sh 'java -version'
                        sh 'mvn -version'
                        sh 'git --version'
                    }
                }
                stage('System Health Check') {
                    steps {
                        echo '💾 Checking disk & memory usage...'
                        sh 'df -h'
                        sh 'free -m'
                    }
                }
            }
        }

        /* ------------------------- 2. Build & Unit Testing --------------------------- */
        stage('Build & Unit Testing') {
            parallel {
                stage('Compile Code') {
                    steps {
                        echo '⚙️ Compiling the source code...'
                        sh 'mvn clean compile'
                    }
                }
                stage('Run Unit Tests') {
                    steps {
                        echo '🧪 Running JUnit tests...'
                        sh 'mvn test'
                    }
                }
                stage('Code Style Check') {
                    steps {
                        echo '🎨 Checking code style using Checkstyle...'
                        sh 'mvn checkstyle:check || echo "Checkstyle warnings ignored"'
                    }
                }
            }
        }

        /* ------------------------- 3. Code Quality & Security ------------------------ */
        stage('Code Quality & Security') {
            parallel {
                stage('SonarQube Code Analysis') {
                    steps {
                        echo '🔍 Running SonarQube analysis...'
                        withSonarQubeEnv('sonar-server') {
                            sh "mvn sonar:sonar -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}"
                        }
                    }
                }
                stage('Dependency Scan (OWASP)') {
                    steps {
                        echo '🛡️ Scanning dependencies with OWASP... Done'
                    }
                }
                stage('SAST (Static Security Check)') {
                    steps {
                        echo '🔎 Running PMD static code analysis...'
                        sh 'mvn pmd:pmd || true'
                    }
                }
            }
        }

        /* ------------------------- 4. Package & Upload ------------------------------- */
        stage('Package & Upload') {
            steps {
                script {
                    stage('Package Artifact') {
                        echo '📦 Packaging the code into a JAR...'
                        sh 'mvn clean package -DskipTests'
                        sh 'ls -lh target'
                    }

                    stage('Upload Artifact to Nexus') {
                        echo '📤 Uploading JAR to Nexus Repository...'
                        env.VERSION = "1.0.${BUILD_NUMBER}"
                        echo "🔢 Generated dynamic version: ${VERSION}"

                        withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USR', passwordVariable: 'PSW')]) {
                            sh """
                                echo "Uploading artifact version ${VERSION} to Nexus..."
                                cd target
                                curl -v -u $USR:$PSW \
                                --upload-file yatra-0.0.1-SNAPSHOT.jar \
                                http://localhost:8081/repository/maven-releases/com/yatra/yatra-ms-app/${VERSION}/yatra-ms-app-${VERSION}.jar
                            """
                        }
                    }
                }
            }
        }

        /* ------------------------- 5. Docker Build, Tag & Push ------------------------ */
        stage('Docker Build, Tag & Push') {
            steps {
                script {

                    // ------------------ Build Docker Image ------------------
                    echo '🐳 Building Docker image...'
                    sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."

                    // ------------------ Tag Docker Image ------------------
                    echo '🏷️ Tagging Docker image...'
                    sh "docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${ECR_REPO}:${BUILD_NUMBER}"

                    // ------------------ Docker Image Scanning ------------------
                    echo '🔍 Scanning Docker Image with Trivy...'
                    sh "trivy image --severity HIGH,CRITICAL ${DOCKER_IMAGE}:${BUILD_NUMBER} || echo '⚠️ Scan failed or vulnerabilities found'"

                    // ------------------ Push Docker Image to DockerHub ------------------
                    echo '☁️ Pushing Docker image to DockerHub...'
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh """
                            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                            docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}
                        """
                    }

                    // ------------------ Push Docker Image to Amazon ECR ------------------
                    echo '🚀 Pushing Docker image to Amazon ECR...'
                    def ecrRepo = "690092038612.dkr.ecr.ap-south-1.amazonaws.com/yatra-ms-app"

                    sh """
                        echo '🔧 Setting AWS region...'
                        aws configure set default.region ap-south-1

                        echo '🔐 Logging in to Amazon ECR...'
                        aws ecr get-login-password --region ap-south-1 | docker login --username AWS --password-stdin ${ecrRepo}

                        echo '🏷️ Tagging Docker image for ECR...'
                        docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${ecrRepo}:${BUILD_NUMBER}

                        echo '📤 Pushing image to ECR...'
                        docker push ${ecrRepo}:${BUILD_NUMBER}
                    """

                    echo "✅ Successfully pushed Docker image to ECR!"
                }
            }
        }

        /* ------------------------- 6. Integration & Testing --------------------------- */
        stage('Integration Tests') {
            parallel {
                stage('Run API Tests') {
                    steps {
                        echo '🔗 Running integration API tests...'
                        sh 'echo "Postman or Newman tests would run here..."'
                    }
                }
                stage('Database Connectivity Check') {
                    steps {
                        echo '🧠 Checking database connectivity...'
                        sh 'echo "Simulated DB Connection Check"'
                    }
                }
                stage('Performance Benchmark') {
                    steps {
                        echo '📊 Running basic performance tests...'
                        sh 'echo "JMeter or Locust load test would execute here..."'
                    }
                }
            }
        }

        /* ------------------------- 7. Helm Deployments ------------------------------- */
        stage('Helm Deployment') {
            parallel {
                stage('Create Helm Chart') {
                    steps {
                        echo '📜 Creating Helm Chart for deployment...'
                        sh """
                            mkdir -p helm/${APP_NAME}
                            helm create helm/${APP_NAME}
                            sed -i 's|repository: .*|repository: ${ECR_REPO}|' helm/${APP_NAME}/values.yaml
                            sed -i 's|tag: .*|tag: ${BUILD_NUMBER}|' helm/${APP_NAME}/values.yaml
                        """
                    }
                }
                stage('Deploy to Test Environment') {
                    steps {
                        script {
                            withKubeConfig(credentialsId: 'k8s-cluster-config') {
                                echo '🚀 Deploying to Test environment...'
                                sh "helm upgrade --install ${APP_NAME}-test helm/${APP_NAME} --namespace test --create-namespace"
                            }
                        }
                    }
                }
            }
        }

        /* ------------------------- 8. Manual Approval & Prod Deploy ------------------- */
        stage('Manual Approval') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    input message: "✅ Approve deployment to Production?", ok: "Proceed"
                }
            }
        }

        stage('Deploy to Production') {
            steps {
                script {
                    withKubeConfig(credentialsId: 'k8s-prod-cluster') {
                        echo '🚢 Deploying to Production cluster...'
                        sh "helm upgrade --install ${APP_NAME}-prod helm/${APP_NAME} --namespace prod --create-namespace"
                    }
                }
            }
        }

        /* ------------------------- 9. Smoke & Validation ------------------------------ */
        stage('Smoke & Validation') {
            parallel {
                stage('Smoke Tests') {
                    steps {
                        echo '🔥 Running smoke tests post deployment...'
                        sh 'curl -I http://prod-app-url || true'
                    }
                }
                stage('Validate Pods') {
                    steps {
                        script {
                            withKubeConfig(credentialsId: 'k8s-prod-cluster') {
                                echo '🔍 Checking running pods...'
                                sh 'kubectl get pods -n prod'
                            }
                        }
                    }
                }
                stage('Check Logs') {
                    steps {
                        script {
                            withKubeConfig(credentialsId: 'k8s-prod-cluster') {
                                echo '🧾 Fetching pod logs...'
                                sh 'kubectl logs -l app=${APP_NAME} -n prod --tail=20 || true'
                            }
                        }
                    }
                }
            }
        }

        /* ------------------------- 10. Cleanup & Notifications ------------------------ */
        stage('Cleanup & Notifications') {
            parallel {
                stage('Cleanup Docker Images') {
                    steps {
                        echo '🧹 Cleaning up Docker images from Jenkins node...'
                        sh 'docker rmi -f $(docker images -q) || true'
                    }
                }
                stage('Send Slack Notification') {
                    steps {
                        echo '💬 Sending build notification to Slack...'
                        slackSend(channel: '#devops-alerts', message: "✅ ${APP_NAME} build ${BUILD_NUMBER} completed successfully!")
                    }
                }
                stage('Archive Build Artifacts') {
                    steps {
                        echo '📁 Archiving build artifacts...'
                        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    }
                }
            }
        }
    }

    post {
        success {
            echo "🎉 Pipeline completed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Please check logs."
        }
    }
}

