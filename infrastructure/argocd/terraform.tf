terraform {
  required_version = "~> 1.11.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.89.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "= 2.36.0"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "= 2.17.0"
    }
  }

  backend "s3" {
    bucket       = "oaebudt-dataspace-infra-terraform-state"
    region       = "us-east-1"
    key          = "argocd.tfstate"
    use_lockfile = true
  }

}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      project     = var.project_name
      environment = var.project_environment
      cluster     = "dev-oaebudt-ds"
      tf-managed  = "true"
    }
  }
}

data "aws_eks_cluster" "eks_cluster_d" {
  name = "dev-oaebudt-ds"
}

data "aws_eks_cluster_auth" "eks_cluster_auth_d" {
  name = "dev-oaebudt-ds"
}

provider "helm" {
  kubernetes {
    host                   = data.aws_eks_cluster.eks_cluster_d.endpoint
    cluster_ca_certificate = base64decode(data.aws_eks_cluster.eks_cluster_d.certificate_authority[0].data)
    token                  = data.aws_eks_cluster_auth.eks_cluster_auth_d.token
  }
}

provider "kubernetes" {
  host                   = data.aws_eks_cluster.eks_cluster_d.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.eks_cluster_d.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.eks_cluster_auth_d.token
}
