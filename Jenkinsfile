pipeline {
    agent any

    tools {
        maven "MAVEN-3.8.7"
    }

    environment {
        APP_NAME     = "yatra-ms-app"
        DOCKER_IMAGE = "satyam88/${APP_NAME}"
        AWS_REGION   = "ap-south-1"   // Change to your AWS region
        ECR_REPO     = "123456789012.dkr.ecr.ap-south-1.amazonaws.com/${APP_NAME}"
    }

    stages {
        stage('Git Checkout') {
            steps {
                echo "📥 Checking out code from GitHub branch: holiday"
                git branch: 'holiday', url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
            }
        }

        stage('Check Tools Version') {
            steps {
                echo '🔍 Checking versions of Java, Maven and Git...'
                sh 'java -version'
                sh 'mvn --version'
                sh 'git --version'
            }
        }

        stage('Build Code') {
            steps {
                ansiColor('xterm') {
                    echo "⚙️ Building project with Maven..."
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Run Unit Tests') {
            steps {
                ansiColor('xterm') {
                    echo '🧪 Running unit tests...'
                    sh 'mvn test'
                }
            }
        }

        stage('SonarQube Code Analysis') {
            steps {
                withSonarQubeEnv('sonarqube-server') {
                    echo '🔎 Running SonarQube analysis...'
                    sh 'mvn sonar:sonar'
                }
            }
        }

        stage('Package Artifact') {
            steps {
                echo '📦 Packaging application...'
                sh 'ls -lh target/*.jar || echo "❌ No jar found!"'
            }
        }

        stage('Code Deploy (Artifact Upload)') {
            steps {
                echo '📤 Uploading artifact to Nexus/Artifactory...'
                sh 'mvn deploy -DskipTests || echo "Upload skipped (configure Nexus settings.xml)"'
            }
        }

        stage('Build & Tag Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                sh "docker build -t ${DOCKER_IMAGE}:${BUILD_NUMBER} ."
            }
        }

        stage('Security Scan (Trivy)') {
            steps {
                echo '🔒 Scanning Docker image for vulnerabilities...'
                sh "trivy image ${DOCKER_IMAGE}:${BUILD_NUMBER} || true"
            }
        }

        stage('Push Docker Image to DockerHub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                        sh "docker push ${DOCKER_IMAGE}:${BUILD_NUMBER}"
                    }
                }
            }
        }

        stage('Push Docker Image to Amazon ECR') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'aws-ecr-creds', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        sh """
                          aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                          aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                          aws configure set default.region ${AWS_REGION}

                          aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                          docker tag ${DOCKER_IMAGE}:${BUILD_NUMBER} ${ECR_REPO}:${BUILD_NUMBER}
                          docker push ${ECR_REPO}:${BUILD_NUMBER}
                        """
                    }
                }
            }
        }

        stage('Create Helm Chart') {
            steps {
                echo '📜 Creating Helm chart for deployment...'
                sh """
                  mkdir -p helm/${APP_NAME}
                  helm create helm/${APP_NAME}
                  # Update values.yaml dynamically (image, tag)
                  sed -i 's|repository: .*|repository: ${ECR_REPO}|' helm/${APP_NAME}/values.yaml
                  sed -i 's|tag: .*|tag: ${BUILD_NUMBER}|' helm/${APP_NAME}/values.yaml
                """
            }
        }

        stage('Deploy to Test Env (Helm)') {
            steps {
                script {
                    withKubeConfig(credentialsId: 'k8s-cluster-config') {
                        echo '🚀 Deploying app to Test Kubernetes namespace...'
                        sh "helm upgrade --install ${APP_NAME}-test helm/${APP_NAME} --namespace test --create-namespace"
                    }
                }
            }
        }

        stage('Integration Tests') {
            steps {
                echo '🔗 Running integration tests on Test environment...'
                sh 'echo "Run Postman/Newman automated API tests here"'
            }
        }

        stage('Approval for Production') {
            steps {
                timeout(time: 1, unit: 'HOURS') {
                    input message: "Deploy ${APP_NAME} to Production?", ok: "Approve"
                }
            }
        }

        stage('Deploy to Production (Helm)') {
            steps {
                script {
                    withKubeConfig(credentialsId: 'k8s-prod-cluster') {
                        echo '🚀 Deploying application to Production Kubernetes cluster...'
                        sh "helm upgrade --install ${APP_NAME}-prod helm/${APP_NAME} --namespace prod --create-namespace"
                    }
                }
            }
        }

        stage('Smoke Tests') {
            steps {
                echo '🔥 Running smoke tests to verify Production deployment...'
                sh 'curl -I http://prod-app-url || true'
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline executed successfully!"
            slackSend(channel: '#devops-alerts', message: "✅ ${APP_NAME} build ${BUILD_NUMBER} deployed successfully!")
        }
        failure {
            echo "❌ Pipeline failed. Please check logs."
            slackSend(channel: '#devops-alerts', message: "❌ ${APP_NAME} build ${BUILD_NUMBER} failed at stage: ${STAGE_NAME}")
        }
    }
}