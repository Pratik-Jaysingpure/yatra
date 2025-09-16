pipeline {
    agent any

    triggers {
        // GitHub webhook trigger for automatic build on push
        githubPush()
    }

    stages {
        stage('Checkout') {
            steps {
                // GitHub repo से कोड क्लोन करें
                git branch: 'holiday',
                    url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
            }
        }

        stage('Build') {
            steps {
                // Maven clean package (skip tests optional)
                sh '/opt/apache-maven-3.9.11/bin/mvn clean package -DskipTests'
            }
        }

        stage('Archive') {
            steps {
                // Jar फाइल को आर्काइव करें ताकि Jenkins UI से डाउनलोड कर सकें
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
    }

    post {
        success {
            echo 'Build completed successfully!'
        }
        failure {
            echo 'Build failed. hi pratik Check the console output.'
        }
    }
}
