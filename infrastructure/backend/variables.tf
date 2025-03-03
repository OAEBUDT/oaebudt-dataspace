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