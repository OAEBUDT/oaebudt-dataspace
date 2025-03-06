aws_region          = "us-east-1"
project_name        = "oebudt-dataspace"
aws_tags = {
  environment   = "development"
  project       = "oaebudt-dataspace"
  tier          = "iam"
  purpose       = "oidc-github"
}
github_oidc_provider_url         = "https://token.actions.githubusercontent.com"
github_oidc_audiences            = ["sts.amazonaws.com"]
github_oidc_repo_name            = "OAEBUDT/oaebudt-dataspace"
iam_github_oidc_name             = "GitHubAction-AssumeRoleWithAction"
iam_github_oidc_ecr_policy_name  = "github-actions-ecr-policy"
