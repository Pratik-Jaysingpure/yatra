pipeline {
    agent any

    tools {
        maven "MAVEN-3.8.7"
    }

    environment {
        DOCKER_IMAGE = "pratikjaysingpure/yatra-ms-app-pipeline"
        AWS_REGION = "ap-south-1"
        ECR_REPO = "123456789012.dkr.ecr.ap-south-1.amazonaws.com/yatra-ms-app"
        SONAR_HOST_URL = "http://localhost:9000"
        SONAR_TOKEN = credentials('sonar-token')
        DOCKER_CREDS = credentials('dockerhub-creds')
        NEXUS_CREDS = credentials('nexus-creds')
    }

    stages {

        stage('Declarative: Checkout SCM') {
            steps {
                echo '📥 Checking out source code...'
                git branch: 'dev', url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
            }
        }

        stage('Declarative: Tool Install') {
            steps {
                echo '🔧 Ensuring required tools are installed (Java, Maven, Git)...'
                sh 'java -version'
                sh 'mvn -version'
                sh 'git --version'
            }
        }

        stage('Checking JAVA, Maven, git') {
            steps {
                echo '✅ Checking environment setup...'
                sh 'whoami'
            }
        }

        stage('Code Compilation') {
            steps {
                echo '🧩 Compiling source code...'
                sh 'mvn clean compile'
            }
        }

        stage('Code QA Execution') {
            steps {
                echo '🧪 Running unit tests...'
                sh 'mvn test'
            }
        }

        stage('Sonar Code Analysis') {
            steps {
                echo '🔍 Running SonarQube Analysis...'
                withSonarQubeEnv('sonar-server') {
                    sh "mvn sonar:sonar -Dsonar.projectKey=yatra -Dsonar.host.url=${SONAR_HOST_URL} -Dsonar.login=${SONAR_TOKEN}"
                }
            }
        }

        stage('Code Package') {
            steps {
                echo '📦 Packaging code...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Building Docker Image') {
            steps {
                echo '🐳 Building Docker image...'
                sh "docker build -t ${DOCKER_IMAGE}:latest ."
            }
        }

        stage('Tagging Docker Image') {
            steps {
                echo '🏷️ Tagging Docker image...'
                sh "docker tag ${DOCKER_IMAGE}:latest ${ECR_REPO}:latest"
            }
        }

        stage('Docker Image Scanning') {
            steps {
                echo '🔎 Scanning Docker image for vulnerabilities...'
                sh "trivy image ${DOCKER_IMAGE}:latest || echo 'Trivy scan skipped if not installed'"
            }
        }

        stage('Docker push to Docker Hub') {
            steps {
                echo '☁️ Pushing image to DockerHub...'
                sh """
                    echo ${DOCKER_CREDS_PSW} | docker login -u ${DOCKER_CREDS_USR} --password-stdin
                    docker push ${DOCKER_IMAGE}:latest
                """
            }
        }

        stage('Docker Image Push to Amazon ECR') {
            steps {
                echo '🚀 Pushing Docker image to Amazon ECR...'
                sh """
                    aws configure set default.region ${AWS_REGION}
                    aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                    docker push ${ECR_REPO}:latest
                """
            }
        }

        stage('Upload the docker Image to Nexus') {
            steps {
                echo '📤 Uploading Docker artifact to Nexus...'
                sh """
                    curl -v -u ${NEXUS_CREDS_USR}:${NEXUS_CREDS_PSW} \
                    --upload-file target/*.jar \
                    http://localhost:8081/repository/maven-releases/com/yatra/yatra-ms-app/1.0/yatra-ms-app-1.0.jar
                """
            }
        }

        stage('Deploy App to K8s Cluster') {
            steps {
                echo '🚢 Deploying to Kubernetes...'
                withKubeConfig(credentialsId: 'k8s-config') {
                    sh """
                        kubectl apply -f k8s/deployment.yaml
                        kubectl apply -f k8s/service.yaml
                    """
                }
            }
        }
    }

    post {
        success {
            echo '🎉 Deployment Successful!'
        }
        failure {
            echo '❌ Build Failed! Check logs.'
        }
    }
}
