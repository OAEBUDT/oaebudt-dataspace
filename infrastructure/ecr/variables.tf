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

variable "tfstate_bucket_name" {
  type        = string
  description = "Name of the S3 bucket to store Terraform state"
}

variable "tfstate_key" {
  type        = string
  description = "Key (file path) for storing the Terraform state in the S3 bucket"
}

variable "ecr_name" {
  type        = string
  description = "Name of the Amazon ECR repository where Docker images and Helm charts will be stored"
}

