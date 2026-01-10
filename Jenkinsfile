pipeline {
    agent any
    
    environment {
        // AWS Configuration - Update these values
        AWS_REGION = 'us-east-2'
        AWS_ACCOUNT_ID = credentials('aws-account-id') // We'll set this up
        ECR_REPO = 'devops-project-1-app'
        IMAGE_TAG = "${BUILD_NUMBER}-${GIT_COMMIT.take(7)}"
        FULL_IMAGE = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:${IMAGE_TAG}"
        
        // Security thresholds
        TRIVY_SEVERITY = 'CRITICAL,HIGH'
    }
    
    tools {
        maven 'Maven3'
        jdk 'Java17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    env.GIT_COMMIT_MSG = sh(
                        script: 'git log -1 --pretty=%B',
                        returnStdout: true
                    ).trim()
                }
                echo "Building commit: ${GIT_COMMIT}"
                echo "Commit message: ${GIT_COMMIT_MSG}"
            }
        }
        
        stage('Build & Test') {
            steps {
                sh '''
                    echo "=========================================="
                    echo "Starting Maven Build"
                    echo "=========================================="
                    mvn clean compile
                '''
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh '''
                    echo "=========================================="
                    echo "Running Unit Tests"
                    echo "=========================================="
                    mvn test
                '''
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package') {
            steps {
                sh '''
                    echo "=========================================="
                    echo "Packaging Application"
                    echo "=========================================="
                    mvn package -DskipTests
                '''
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }
        
        stage('Docker Build') {
            steps {
                sh '''
                    echo "=========================================="
                    echo "Building Docker Image"
                    echo "=========================================="
                    docker build -t ${ECR_REPO}:${IMAGE_TAG} .
                    docker tag ${ECR_REPO}:${IMAGE_TAG} ${FULL_IMAGE}
                    
                    echo "Image built: ${FULL_IMAGE}"
                    docker images | grep ${ECR_REPO}
                '''
            }
        }
        
        stage('Security Scan - Trivy') {
            environment {
                // Force all tools to use the large disk for temp files
                TMPDIR = "/home/ec2-user/tmp"
            }     
            steps {
                sh """
                    echo "=========================================="
                    echo "SECURITY GATE: Trivy Vulnerability Scan"
                    echo "=========================================="
                    echo "Scanning for: ${TRIVY_SEVERITY} vulnerabilities"
                    echo ""
                    
                    # Generate detailed report
                    mkdir -p ${TMPDIR}
                    # 1. Generate a detailed report using the Jenkins-owned cache
                    /usr/local/bin/trivy --cache-dir /var/lib/jenkins/trivy-cache \
                        image --format table \
                        --output trivy-report.txt \
                        --skip-db-update \
                        --skip-java-db-update \
                        ${ECR_REPO}:${IMAGE_TAG}
                    
                    echo "--- FULL SCAN REPORT ---"
                    cat trivy-report.txt
                    echo "------------------------"
                    
                    # Security Gate: Fail on CRITICAL or HIGH
                    echo ""
                    echo "Checking for ${TRIVY_SEVERITY} vulnerabilities..."
                    /usr/local/bin/trivy --cache-dir /var/lib/jenkins/trivy-cache \
                        image --exit-code 1 \
                        --severity ${TRIVY_SEVERITY} \
                        --skip-db-update \
                        --skip-java-db-update \
                        --no-progress \
                        ${ECR_REPO}:${IMAGE_TAG}
                    
                    echo ""
                    echo "✅ SECURITY GATE PASSED - No ${TRIVY_SEVERITY} vulnerabilities found"
                """
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.txt', allowEmptyArchive: true
                }
                failure {
                    echo '❌ SECURITY GATE FAILED - CRITICAL/HIGH vulnerabilities detected!'
                    echo 'Image will NOT be pushed to ECR.'
                    echo 'Review trivy-report.txt for remediation guidance.'
                }
            }
        }
        
        stage('Push to ECR') {
            when {
                expression { currentBuild.resultIsBetterOrEqualTo('SUCCESS') }
            }
            steps {
                sh '''
                    echo "=========================================="
                    echo "Pushing to Amazon ECR"
                    echo "=========================================="
                    
                    # Login to ECR
                    aws ecr get-login-password --region ${AWS_REGION} | \
                        docker login --username AWS --password-stdin \
                        ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com
                    
                    # Push image
                    docker push ${FULL_IMAGE}
                    
                    # Also tag as latest
                    docker tag ${FULL_IMAGE} \
                        ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:latest
                    docker push \
                        ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:latest
                    
                    echo ""
                    echo "✅ Image pushed successfully!"
                    echo "Image URI: ${FULL_IMAGE}"
                '''
            }
        }
        
        stage('Cleanup') {
            steps {
                sh '''
                    echo "=========================================="
                    echo "Cleaning up local Docker images"
                    echo "=========================================="
                    docker rmi ${ECR_REPO}:${IMAGE_TAG} || true
                    docker rmi ${FULL_IMAGE} || true
                '''
            }
        }
    }
    
    post {
        success {
            echo '''
            ╔══════════════════════════════════════════════════════════════╗
            ║                    PIPELINE SUCCEEDED                        ║
            ╠══════════════════════════════════════════════════════════════╣
            ║  ✅ Code compiled successfully                               ║
            ║  ✅ All unit tests passed                                    ║
            ║  ✅ Docker image built                                       ║
            ║  ✅ Security scan PASSED (no CRITICAL/HIGH vulnerabilities)  ║
            ║  ✅ Image pushed to ECR                                      ║
            ╚══════════════════════════════════════════════════════════════╝
            '''
        }
        failure {
            echo '''
            ╔══════════════════════════════════════════════════════════════╗
            ║                    PIPELINE FAILED                           ║
            ╠══════════════════════════════════════════════════════════════╣
            ║  ❌ Check console output for failure details                 ║
            ║  ❌ If security scan failed, review trivy-report.txt         ║
            ║  ❌ Image was NOT pushed to ECR                              ║
            ╚══════════════════════════════════════════════════════════════╝
            '''
        }
        always {
            cleanWs()
        }
    }
}
