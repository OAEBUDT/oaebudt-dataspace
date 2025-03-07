locals {
  github_oidc_provider_url        = "https://token.actions.githubusercontent.com"
  github_oidc_repo_name           = "OAEBUDT/oaebudt-dataspace"
  iam_github_oidc_name            = "github-actions-assume-role"
  iam_github_oidc_ecr_policy_name = "github-actions-ecr-policy"

  github_oidc_tags = {
    tier    = "iam"
    purpose = "github-oidc"
  }
}

resource "aws_ecr_repository" "oaebudt_dataspace_ecr" {
  name                 = var.ecr_name
  image_tag_mutability = "MUTABLE"

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    tier = "ecr"
  }
}

module "github_oidc" {
  source = "./modules/github_oidc"

  github_oidc_provider_url        = local.github_oidc_provider_url
  github_oidc_repo_name           = local.github_oidc_repo_name
  iam_github_oidc_name            = local.iam_github_oidc_name
  iam_github_oidc_ecr_policy_name = local.iam_github_oidc_ecr_policy_name

  github_oidc_tags = local.github_oidc_tags
}
