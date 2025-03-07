variable "github_oidc_provider_url" {
  type        = string
  description = "The URL of the OIDC provider used by GitHub"
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
