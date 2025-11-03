pipeline {
    agent any

    tools {
        maven 'Maven'
        jdk 'Java21'
    }

    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out code from GitHub...'
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo 'Building project with Maven...'
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Archive Artifact') {
            steps {
                echo 'Archiving the built JAR file...'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Test Build') {
            steps {
                echo 'Verifying JAR file was created...'
                sh 'ls -lh target/*.jar'
            }
        }
    }

    post {
        success {
            echo '========================================='
            echo 'Pipeline completed successfully!'
            echo 'JAR file built and ready for deployment!'
            echo '========================================='
        }
        failure {
            echo 'Pipeline failed! Check logs for details.'
        }
    }
}