terraform {
  required_version = "~> 1.11.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.89.0"
    }
  }

  backend "s3" {
    bucket       = "oaebudt-dataspace-infra-terraform-state"
    key          = "eks.tfstate"
    region       = "us-east-1"
    use_lockfile = true
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      project     = var.project_name
      environment = var.project_environment
      cluster     = "eks-${var.eks_name}"
      tf-managed  = "true"
    }
  }
}

data "aws_eks_cluster" "eks_cluster_d" {
  name = aws_eks_cluster.eks_cluster.name
}

data "aws_eks_cluster_auth" "eks_cluster_auth_d" {
  name = aws_eks_cluster.eks_cluster.name
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
