pipeline {
    agent any

    tools {
        maven 'MAVEN3'     // Jenkins configure 
        jdk 'JAVA17'       // Jenkins Java configure 
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'holiday',
                    url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
            }
        }

        stage('Build with Maven') {
            steps {
                sh 'mvn clean package -DskipTests -Dmaven.repo.local=$WORKSPACE/.m2'
            }
        }

        stage('Archive Artifact') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }
}
