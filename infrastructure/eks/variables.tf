variable "aws_region" {
  type        = string
  description = "AWS region to deploy to"
  default     = "us-east-1"
}

variable "project_name" {
  type        = string
  description = "Name of the project"
}

variable "project_environment" {
  type        = string
  description = "Project environment or stage"
}

variable "vpc_name" {
  description = "VPC name"
  type        = string
  default     = "main"
}

variable "eks_environment" {
  description = "Deployment environment"
  type        = string
}

variable "eks_availability_zones" {
  description = "List of availability zones in the region where the EKS nodes will be deployed"
  type        = list(string)
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
}

variable "subnet_cidr_blocks" {
  description = "List of CIDR blocks to use for subnets"
  type        = list(string)
}

variable "static_eip_nat" {
  description = "EIP to associate with the NAT gateway"
  type        = string
}
