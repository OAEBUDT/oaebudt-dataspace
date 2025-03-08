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

variable "tfstate_bucket_name" {
  type        = string
  description = "Name of the S3 bucket to store Terraform state"
}
