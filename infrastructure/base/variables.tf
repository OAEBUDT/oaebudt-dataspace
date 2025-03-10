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

# Define variables for GitHub OIDC configuration
variable "ecr_name" {
  description = "ECR repository name"
  type        = string
}

variable "ecr_tags" {
  description = "Tags for the ECR repository"
  type        = map(string)
}

variable "github_oidc_provider_url" {
  description = "GitHub OIDC provider URL"
  type        = string
}

variable "github_oidc_repo_name" {
  description = "GitHub repository name"
  type        = string
}

variable "iam_github_oidc_name" {
  description = "IAM GitHub OIDC role name"
  type        = string
}

variable "iam_github_oidc_ecr_policy_name" {
  description = "IAM policy name for GitHub OIDC and ECR"
  type        = string
}

variable "github_oidc_tags" {
  description = "Tags for GitHub OIDC resources"
  type        = map(string)
}
