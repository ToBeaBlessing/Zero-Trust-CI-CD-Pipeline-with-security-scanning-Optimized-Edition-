# Zero-Trust CI/CD Pipeline with security scanning (Optimized Edition)

> **Time:** 6-8 hours | **Cost:** ~$0.50 | **Scenario:** "New microservice onboarding with mandatory security gates"

[Pipeline Status]
<img width="2085" height="942" alt="image" src="https://github.com/user-attachments/assets/c0b068c1-3c5d-4509-a328-2b154a979a31" />

A production-grade DevSecOps pipeline designed to enforce **Zero-Trust** principles for software delivery. Code is treated as untrusted until it passes automated vulnerability scanning.

Unlike standard tutorials, this project was architected to run on resource-constrained infrastructure (AWS `t3.small`), requiring significant optimization of memory management, disk I/O, and caching strategies to prevent system failure during heavy security scans.

---

## Engineering Challenges & Optimizations

This project demonstrates Senior DevSecOps capabilities by solving real-world infrastructure limitations encountered during implementation.

| Challenge | Root Cause | The Engineering Solution |
|-----------|------------|--------------------------|
| **Server Freezes (OOM)** | Running Java builds + Trivy DB downloads on 2GB RAM exhausted physical memory. | Implemented Swap Space: Provisioned a 2GB Swap file (`mkswap`) to offload idle processes during peak scan times, preventing Kernel Panics. |
| **"No Space Left on Device"** | The default Amazon Linux `/tmp` partition is small and RAM-based (`tmpfs`). | I/O Redirection: Configured `TMPDIR` environment variables in the Jenkinsfile to redirect heavy temporary files to the 30GB EBS volume. |
| **Cache Permission Failures** | The `jenkins` service user lacked permissions to write to standard user directories. | Dedicated Cache Architecture: Created strictly permissioned directories (`/var/lib/jenkins/trivy-cache`) and enforced absolute paths in pipeline scripts. |
| **Critical Vulnerabilities** | Default Spring Boot 3.2.0 parent contained a critical Tomcat vulnerability (CVE-2025-24813). | Dependency Overrides: Manually overrode versions in `pom.xml` properties (`tomcat.version` → `10.1.45`) to patch the RCE flaw. |

---

## Tech Stack

- **Cloud:** AWS (EC2 t3.small, ECR, S3, IAM, VPC, Security Groups)
- **Orchestration:** Jenkins (Self-Hosted on Amazon Linux 2023)
- **Containerization:** Docker & Multi-stage Dockerfiles
- **Security:** Trivy (Vulnerability Scanner), IAM Roles (Least Privilege)
- **Language:** Java 17, Maven, Spring Boot 3.4.5

---

## Architecture

1. **Source:** Developer pushes code to GitHub.
2. **Trigger:** Webhook initiates Jenkins pipeline on EC2.
3. **Build:** Maven compiles code and runs Unit Tests.
4. **Package:** Docker builds an optimized container image.
5. **Gate:** Trivy scans the image. If CVE > Medium, pipeline **ABORTS**.
6. **Deploy:** Only clean images are pushed to the private AWS ECR Registry.

---

## Security Gate Demonstration

To prove the efficacy of the gate, I intentionally introduced a vulnerable dependency (`log4j-core 2.14.1` - Log4Shell) into the build.

**Result:**

| Status | Outcome |
|--------|---------|
| ❌ **FAILED** | Pipeline failed at the Security Scan stage. |
| 🚫 **BLOCKED** | The vulnerable image never reached the registry. |
| ✅ **FIXED** | After removing the bad dependency and patching the Dockerfile OS layers (`apk upgrade`), the pipeline passed. |

---

## How to Run

1. **Infrastructure:** Launch the EC2 instance using the `user-data.sh` script provided.
2. **Jenkins:** Configure the pipeline to poll this repository.
3. **Credentials:** Add `aws-account-id` and `github-credentials` to Jenkins.
4. **Build:** Click "Build Now" to see the Zero-Trust flow in action.

---

## Cost Analysis

| Resource | Cost |
|----------|------|
| EC2 (t3.small) | ~$0.0208/hour |
| EBS (30GB gp3) | ~$0.08/month pro-rated |
| **Total Project Cost** | **<$0.50** (One afternoon of running time) |

---

## Author

**Blessing Omomola** - DevSecOps Engineer

[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=for-the-badge&logo=linkedin&logoColor=white)](https://linkedin.com/in/YOUR-LINKEDIN)
[![GitHub](https://img.shields.io/badge/GitHub-100000?style=for-the-badge&logo=github&logoColor=white)](https://github.com/YOUR-GITHUB)
