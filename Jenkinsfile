pipeline {
    agent any

  tools {
      maven "MAVEN-3.8.7"
  }

    stages {
        stage('Git Checkout') {
            steps {
                git branch: 'holiday', url: 'https://github.com/Pratik-Jaysingpure/yatra.git'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package'
            }
        }
    } // <-- stages closing brace
} // <-- pipeline closing brace
