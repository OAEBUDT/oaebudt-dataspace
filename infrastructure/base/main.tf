# Module to set up AWS ECR Repository
module "ecr" {
  source   = "./modules/ecr"
  ecr_name = var.ecr_name

  ecr_tags = var.ecr_tags
}

# Module to set up GitHub OIDC integration with AWS
module "github_oidc" {
  source = "./modules/github_oidc"

  github_oidc_provider_url        = var.github_oidc_provider_url
  github_oidc_repo_name           = var.github_oidc_repo_name
  iam_github_oidc_name            = var.iam_github_oidc_name
  iam_github_oidc_ecr_policy_name = var.iam_github_oidc_ecr_policy_name

  github_oidc_tags = var.github_oidc_tags
}
