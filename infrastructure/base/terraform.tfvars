aws_region          = "us-east-1"
project_name        = "oebudt-dataspace"
project_environment = "development"
ecr_name               = "oaebudt-dataspace/connector"
ecr_tags               = {
  tier = "ecr"
}
github_oidc_provider_url = "https://token.actions.githubusercontent.com"
github_oidc_repo_name    = "OAEBUDT/oaebudt-dataspace"
iam_github_oidc_name     = "github-actions-assume-role"
iam_github_oidc_ecr_policy_name = "github-actions-ecr-policy"
github_oidc_tags = {
  tier    = "iam"
  purpose = "github-oidc"
}
