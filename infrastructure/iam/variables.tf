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

variable "github_oidc_provider_url" {
  type        = string
  description = "The URL of the OIDC provider used by GitHub"
}

variable "github_oidc_audiences" {
  type        = list(string)
  description = "The list of audiences (roles) specified for GitHub OIDC"
}

variable "github_oidc_repo_name" {
  type        = string
  description = "The name of the GitHub repository used for OIDC, in the format 'org/repo'."

  validation {
    condition     = can(regex("^([a-zA-Z0-9_.-]+)/([a-zA-Z0-9_.-]+)$", var.github_oidc_repo_name))
    error_message = "The repository name must follow the format 'org/repo'"
  }
}

variable "iam_github_oidc_name" {
  type        = string
  description = "Name of role used by GitHub OIDC"
}

variable "iam_github_oidc_ecr_policy_name" {
  type        = string
  description = "The name of the IAM policy for GitHub Actions to access ECR"
}
