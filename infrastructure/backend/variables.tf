variable "aws_region" {
  type        = string
  description = "AWS region to deploy to"
  default     = "eu-west-1"
}

variable "bucket_name" {
  type        = string
  description = "Name of the S3 bucket to store Terraform state"
}

variable "project_name" {
  type        = string
  description = "Name of the project"
}