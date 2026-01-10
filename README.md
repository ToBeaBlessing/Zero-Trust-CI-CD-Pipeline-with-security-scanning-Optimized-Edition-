# devops-project-1-cicd-pipeline
 Zero-trust CI/CD pipeline with security scanning
# Zero-Trust CI/CD Pipeline

[Pipeline Status]
<img width="2085" height="942" alt="image" src="https://github.com/user-attachments/assets/c0b068c1-3c5d-4509-a328-2b154a979a31" />

## Overview

A production-grade CI/CD pipeline implementing zero-trust security principles. 
Code cannot reach the container registry without passing automated vulnerability scanning.

## 🎯 What This Demonstrates

| Resume Claim | Evidence |
|-------------|----------|
| "Built CI/CD pipelines (Jenkins, Docker)" | Complete working pipeline |
| "Integrated security automation" | Trivy scanning blocks vulnerabilities |
| "95% security gate compliance" | No bypass possible - CRITICAL/HIGH = FAIL |

## 🛠️ Technologies

- **CI/CD**: Jenkins
- **Containers**: Docker, Amazon ECR
- **Security**: Trivy vulnerability scanner
- **Build**: Maven, Java 17
- **Cloud**: AWS (EC2, ECR, IAM, VPC)

## 🏗️ Architecture

[Include architecture diagram]

## 🔒 Security Features

- Multi-stage Docker builds (minimal attack surface)
- Non-root container user
- Vulnerability scanning with fail gates
- Immutable image tags
- ECR scan-on-push enabled

## 🚀 Pipeline Stages

1. **Checkout** - Pull from GitHub
2. **Build & Test** - Maven compile
3. **Unit Tests** - JUnit execution
4. **Package** - Create JAR
5. **Docker Build** - Multi-stage image
6. **Security Scan** - Trivy (CRITICAL/HIGH = FAIL)
7. **Push to ECR** - Only if scan passes

## 📊 Security Gate Demo

I demonstrated the security gate by intentionally adding a vulnerable 
dependency (Log4j 2.14.1 with CVE-2021-44228):

- ❌ Pipeline FAILED at security scan
- ❌ Image NOT pushed to ECR
- ✅ After fix: Pipeline passed

[Screenshot of failed pipeline]

## 💰 Cost

~$0.50/day when running. Uses t3.small EC2, ECR free tier.

## 👤 Author

**Blessing Omomola** - DevSecOps Engineer
- [LinkedIn](https://linkedin.com/in/your-profile)
- [GitHub](https://github.com/your-username)

Part of a 7-project DevOps portfolio demonstrating production-grade practices.
