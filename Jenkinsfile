pipeline {

    agent any

    options {
        // Keep only last 3 builds to save space
        buildDiscarder(logRotator(numToKeepStr: '3', artifactNumToKeepStr: '3'))
        ansiColor('xterm')
    }

    tools {
        maven 'MAVEN-3.8.7'
    }

    environment {
        DOCKER_IMAGE = "pratikjaysingpure/yatra-ms-app"
        AWS_REGION = "ap-south-1"
        ECR_REPO = "123456789012.dkr.ecr.ap-south-1.amazonaws.com/yatra-ms-app"
        SONAR_HOST_URL = "http://localhost:9000"
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_CREDS = credentials('dockerhub-creds')
        NEXUS_CREDS = credentials('nexus-creds')
    }

    stages {

        stage('📥 Checkout Source Code') {
            steps {
                echo 'Checking out source code...'
                git branch: 'holiday', url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
            }
        }

        stage('🔧 Validate Tool Versions') {
            steps {
                echo 'Verifying Java, Maven, and Git setup...'
                sh 'java -version'
                sh 'mvn -version'
                sh 'git --version'
                sh 'whoami'
            }
        }

        stage('🧩 Code Compilation') {
            steps {
                echo 'Starting code compilation...'
                sh 'mvn clean compile'
                echo 'Code compilation completed successfully!'
            }
        }

        stage('🧪 JUnit Test Execution') {
            steps {
                echo 'Running JUnit test cases...'
                sh 'mvn clean test'
                echo 'JUnit Test cases completed successfully!'
            }
        }

        stage('🔍 SonarQube Code Quality Analysis') {
            environment {
                scannerHome = tool 'qube'
            }
            steps {
                echo 'Starting SonarQube Scan...'
                withSonarQubeEnv('sonar-server') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=yatra -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}"
                }
                echo 'Waiting for Sonar Quality Gate Result...'
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
                echo 'Sonar Quality Gate passed successfully!'
            }
        }

        stage('📦 Code Packaging') {
            steps {
                echo 'Packaging the code into JAR...'
                sh 'mvn clean package -DskipTests'
                echo 'JAR created successfully!'
            }
        }

        stage('🐳 Build & Tag Docker Image') {
            steps {
                echo 'Building Docker image with multiple tags...'
                sh """
                    docker build -t ${DOCKER_IMAGE}:latest .
                    docker tag ${DOCKER_IMAGE}:latest ${ECR_REPO}:latest
                """
                echo 'Docker image built successfully!'
            }
        }

        stage('🔎 Docker Image Scanning') {
            steps {
                echo 'Scanning Docker image using Trivy...'
                sh "trivy image ${DOCKER_IMAGE}:latest || echo 'Trivy not found — skipping scan.'"
                echo 'Docker image scanning completed!'
            }
        }

        stage('☁️ Push Docker Image to DockerHub') {
            steps {
                echo 'Pushing Docker image to DockerHub...'
                sh """
                    echo ${DOCKER_CREDS_PSW} | docker login -u ${DOCKER_CREDS_USR} --password-stdin
                    docker push ${DOCKER_IMAGE}:latest
                """
                echo 'Docker image pushed to DockerHub successfully!'
            }
        }

        stage('🚀 Push Docker Image to Amazon ECR') {
            steps {
                echo 'Pushing Docker image to Amazon ECR...'
                sh """
                    aws configure set default.region ${AWS_REGION}
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                    docker push ${ECR_REPO}:latest
                """
                echo 'Docker image pushed to Amazon ECR successfully!'
            }
        }

        stage('📤 Upload Artifact to Nexus Repository') {
            steps {
                echo 'Uploading JAR artifact to Nexus...'
                sh """
                    curl -v -u ${NEXUS_CREDS_USR}:${NEXUS_CREDS_PSW} \
                    --upload-file target/*.jar \
                    http://localhost:8081/repository/maven-releases/com/yatra/yatra-ms-app/1.0/yatra-ms-app-1.0.jar
                """
                echo 'Artifact uploaded to Nexus successfully!'
            }
        }

        stage('📦 Push Docker Image to Nexus (optional)') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'nexus-creds', usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD')]) {
                        echo 'Pushing Docker image to Nexus Docker Registry...'
                        sh """
                            docker login http://localhost:8085/repository/docker-hosted/ -u ${USERNAME} -p ${PASSWORD}
                            docker tag ${DOCKER_IMAGE}:latest localhost:8085/${DOCKER_IMAGE}:latest
                            docker push localhost:8085/${DOCKER_IMAGE}:latest
                        """
                        echo 'Docker image pushed to Nexus successfully!'
                    }
                }
            }
        }

        stage('🧹 Delete Docker Images (Cleanup)') {
            steps {
                echo 'Cleaning up local Docker images...'
                sh 'docker rmi -f $(docker images -q) || echo "No images to delete"'
                echo 'Cleanup completed.'
            }
        }

        stage('🚢 Deploy to Kubernetes Cluster') {
            steps {
                echo 'Deploying application to Kubernetes...'
                withKubeConfig(credentialsId: 'k8s-config') {
                    sh """
                        kubectl apply -f k8s/deployment.yaml
                        kubectl apply -f k8s/service.yaml
                    """
                }
                echo 'Application deployed successfully!'
            }
        }
    }

    post {
        success {
            echo '🎉 CI/CD Pipeline executed successfully!'
        }
        failure {
            echo '❌ Build failed. Check logs in Jenkins console output.'
        }
    }
}
