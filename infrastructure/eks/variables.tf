variable "aws_region" {
  type        = string
  description = "AWS region to deploy to"
  default     = "us-east-1"
}

variable "project_name" {
  type        = string
  description = "Name of the project"
}

variable "aws_tags" {
  description = "A map of tags to apply to AWS resources"
  type        = map(string)
  default = {
    environment = "development"
    project     = "oaebudt-dataspace"
  }
}

variable "vpc_name" {
  description = "VPC name"
  type        = string
  default     = "main"
}

variable "eks_environment" {
  description = "Deployment environment"
  type        = string
  default     = "development"
}

variable "eks_availability_zones" {
  description = "List of availability zones in the region where the EKS nodes will be deployed"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "eks_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "eks-cluster"
}

variable "eks_version" {
  description = "Kubernetes version for EKS"
  type        = string
  default     = "1.32"
}

variable "vpc_cidr_block" {
  description = "The CIDR block to use for the vpc"
  type        = string
  default = "10.0.0.0/16"
}

variable "subnet_cidr_blocks" {
  description = "List of CIDR blocks to use for subnets"
  type        = list(string)
  default = [
    "10.0.0.0/20",
    "10.0.16.0/20",
    "10.0.32.0/20",
    "10.0.48.0/20"
  ]
}
