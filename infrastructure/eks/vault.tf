locals {
  vault_servers_namespaces     = ["participant-a", "participant-b", "participant-c", "participant-d", "participant-e",
    "participant-f", "provider", "consumer"]
  vault_server_service_account = "oaebudt-ds-vault"
}

resource "aws_kms_key" "kms_vault_unseal" {
  description             = "Unseal keys for ${aws_eks_cluster.eks_cluster.name}'s Vault servers"
  deletion_window_in_days = 30
}

resource "aws_iam_role" "vault_server" {
  name_prefix = "${aws_eks_cluster.eks_cluster.name}-vault-server"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "sts:AssumeRole",
          "sts:TagSession"
        ]
        Principal = {
          Service = "pods.eks.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    tier = "iam"
  }
}

data "aws_iam_policy_document" "vault_server_permissions_policy" {

  statement {
    sid    = "VaultAutoUnsealKMS"
    effect = "Allow"

    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:DescribeKey",
    ]

    resources = [
      aws_kms_key.kms_vault_unseal.arn
    ]
  }
}

resource "aws_iam_role_policy" "vault_server_actions_policy" {
  name_prefix = "${aws_eks_cluster.eks_cluster.name}-vault-server"
  role        = aws_iam_role.vault_server.id
  policy      = data.aws_iam_policy_document.vault_server_permissions_policy.json
}

resource "aws_eks_pod_identity_association" "vault_server" {
  for_each = toset(local.vault_servers_namespaces)

  cluster_name    = aws_eks_cluster.eks_cluster.name
  namespace       = each.value
  service_account = local.vault_server_service_account
  role_arn        = aws_iam_role.vault_server.arn

  tags = {
    tier = "iam"
  }
}
