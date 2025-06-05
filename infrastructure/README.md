# oaebudt-dataspace-infrastructure

This folder contains Terraform configurations used to provision cloud infrastructure for the [OAEBUDT Dataspace](https://github.com/OAEBUDT/oaebudt-dataspace) project. The infrastructure is designed to support secure, scalable, and modular deployment of dataspace components on AWS.

---

##  Contents

- **`backend/`**: Contains Terraform configurations for managing the S3 backend used for storing Terraform state. This ensures state persistence, security, and consistency across deployments.

- **`base/`**: Contains foundational AWS resources and configurations, including Elastic Container Registry (ECR) and GitHub OpenID Connect (OIDC) setup for secure CI/CD integrations.

- **`eks/`**: Terraform configurations to set up a complete Elastic Kubernetes Service (EKS) cluster, including VPC, subnets, security groups, IAM roles, and node groups for running containerized applications.

- **`argocd/`**: Terraform configurations to provision a full Argo CD deployment, enabling **continuous deployment** of the connectors to the EKS cluster.

- **`docs/`**: Contains assets. 

---

## Prerequisites

Before deploying the infrastructure, make sure the following tools are installed and properly configured:

- [Terraform CLI](https://developer.hashicorp.com/terraform/downloads)
- [AWS CLI](https://docs.aws.amazon.com/cli/latest/userguide/install-cliv2.html) (configured using `aws configure`)
- AWS account with appropriate IAM permissions
---

## How to Deploy the Infrastructure

1. **Authenticate with AWS**  
   First, authenticate your AWS account via AWS SSO (Single Sign-On) or using your credentials.

    ```bash
    aws sso login

For each folder in the `infrastructure` directory (except for `docs`), you need to run the following Terraform commands in this specific order:

1. `backend`
2. `base`
3. `eks`
4. `argocd`

**It is important to follow this order to ensure proper deployment.**

2. **Terraform**

    ```bash
    terraform init
    terraform plan
    terraform apply


## How to Administer the Infrastructure

After the infrastructure is deployed, you can manage your infrastructure resources using Terraform by adding, modifying, or deleting resources as needed.

**Note:** Some AWS cloud resources are not provisioned using Terraform. These include:
- Hosted Zone in Route 53
- AWS Certificate Manager certificates for the hosted zone
- Secrets such as `dev-oaebudt-ds-eks-helm-secret-age-key`
 