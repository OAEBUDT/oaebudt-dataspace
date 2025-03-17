resource "aws_iam_openid_connect_provider" "github_oidc" {
  url = var.github_oidc_provider_url

  client_id_list = ["sts.amazonaws.com"]

  tags = var.tags
}

resource "aws_iam_role" "github_actions_assume_role" {
  name = var.iam_github_oidc_role_name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRoleWithWebIdentity"
        Effect = "Allow"
        Principal = {
          Federated = aws_iam_openid_connect_provider.github_oidc.arn
        }
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_repo_name}:*"
          }
        }
      }
    ]
  })

  tags = var.tags
}

data "aws_iam_policy_document" "ecr_access_permission_policy" {
  statement {
    sid    = "ECRAccess"
    effect = "Allow"

    actions = [
      "ecr:GetAuthorizationToken",
      "ecr:BatchCheckLayerAvailability",
      "ecr:GetDownloadUrlForLayer",
      "ecr:GetRepositoryPolicy",
      "ecr:DescribeRepositories",
      "ecr:ListImages",
      "ecr:DescribeImages",
      "ecr:BatchGetImage",
      "ecr:InitiateLayerUpload",
      "ecr:UploadLayerPart",
      "ecr:CompleteLayerUpload",
      "ecr:PutImage"
    ]

    resources = ["*"]
  }
}

resource "aws_iam_role_policy" "github_actions_policy" {
  name   = var.iam_github_oidc_ecr_policy_name
  role   = aws_iam_role.github_actions_assume_role.id
  policy = data.aws_iam_policy_document.ecr_access_permission_policy.json
}
