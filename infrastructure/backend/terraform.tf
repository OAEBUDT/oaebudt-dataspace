terraform {
  required_version = "~> 1.11.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.89.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
  default_tags {
    tags = {
      project = var.project_name
      environment = var.project_environment
    }
  }
}
