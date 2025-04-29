# oaebudt-dataspace-infrastructure

This folder contains Terraform configurations used to provision cloud infrastructure for the [OAEBUDT Dataspace](https://github.com/OAEBUDT/oaebudt-dataspace) project. The infrastructure is designed to support secure, scalable, and modular deployment of dataspace components on AWS.

---

## 📦 Contents

- **`backend/`**: Contains Terraform configurations for managing the S3 backend used for storing Terraform state. This ensures state persistence, security, and consistency across deployments.

- **`base/`**: Contains foundational AWS resources and configurations, including Elastic Container Registry (ECR) and GitHub OpenID Connect (OIDC) setup for secure CI/CD integrations.

- **`eks/`**: Terraform configurations to set up a complete Elastic Kubernetes Service (EKS) cluster, including VPC, subnets, security groups, IAM roles, and node groups for running containerized applications.

- **`docs/`**: Documentation 

---

## ⚙️ Requirements

Before deploying the infrastructure, make sure the following tools are installed and properly configured:

- [Terraform CLI](https://developer.hashicorp.com/terraform/downloads)
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) (configured using `aws configure`)
- AWS account with appropriate IAM permissions
- [kubectl](https://kubernetes.io/docs/tasks/tools/) (for accessing EKS)
- [Helm CLI](https://helm.sh/docs/intro/install/) (optional, for deploying Helm charts after the infrastructure setup)

---

## 🚀 How to Deploy

1. **Authenticate with AWS**  
   First, authenticate your AWS account via AWS SSO (Single Sign-On) or using your credentials.

    ```bash
    aws sso login

2. **Terraform**

    ```bash
    terraform init
    terraform plan
    terraform apply
