pipeline {
    agent any

    tools {
        maven "MAVEN-3.8.7"
    }

    environment {
        DOCKER_IMAGE = "satyam88/yatra-ms-app"
        AWS_REGION = "ap-south-1"   // Change this to your AWS region
        ECR_REPO = "123456789012.dkr.ecr.ap-south-1.amazonaws.com/yatra-ms-app"
    }

    stages {
        stage('Git Checkout') {
            steps {
                echo "Checking out code from GitHub branch: holiday"
                git branch: 'holiday', url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
            }
        }

        stage('Checking JAVA, Maven, git') {
            steps {
                echo 'Checking versions of JAVA, Maven and Git...'
                sh 'java -version'
                sh 'mvn --version'
                sh 'git --version'
                sh 'whoami'
            }
        }

        stage('Build') {
            steps {
                ansiColor('xterm') {
                    echo "Building project with Maven..."
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Code QA Execution') {
            steps {
                ansiColor('xterm') {
                    echo 'Running unit tests...'
                    sh 'mvn test'
                }
            }
        }

        stage('Code Package') {
            steps {
                echo 'Packaging application...'
                sh 'ls -lh target/*.jar || echo "No jar found!"'
            }
        }

        stage('Building & Tagging Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t ${DOCKER_IMAGE}:latest ."
            }
        }

        stage('Docker push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
                        sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                        sh "docker push ${DOCKER_IMAGE}:latest"
                    }
                }
            }
        }

        stage('Docker Image Push to Amazon ECR') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'aws-ecr-creds', usernameVariable: 'AWS_ACCESS_KEY_ID', passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
                        sh """
                          aws configure set aws_access_key_id $AWS_ACCESS_KEY_ID
                          aws configure set aws_secret_access_key $AWS_SECRET_ACCESS_KEY
                          aws configure set default.region ${AWS_REGION}

                          aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${ECR_REPO}
                          docker tag ${DOCKER_IMAGE}:latest ${ECR_REPO}:latest
                          docker push ${ECR_REPO}:latest
                        """
                    }
                }
            }
        }

        stage('Deploy App to K8s Cluster') {
            steps {
                script {
                    withKubeConfig(credentialsId: 'k8s-cluster-config') {
                        echo 'Deploying application to Kubernetes...'
                        sh 'kubectl apply -f k8s/deployment.yaml'
                        sh 'kubectl apply -f k8s/service.yaml'
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Pipeline executed successfully!"
        }
        failure {
            echo "❌ Pipeline failed. Please check logs."
        }
    }
}
