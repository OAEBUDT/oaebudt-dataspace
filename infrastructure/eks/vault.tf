locals {
  vault_servers_namespaces     = ["publisher", "recipient", "liblynx", "knowledgeunlatched", "jstor", "michigan", "punctumbooks", "ubiquitypress"]
  vault_server_service_account = "oaebudt-ds-vault"
  participants_dynamodb_tables = {
    for namespace in local.vault_servers_namespaces : namespace => {
      name = "${aws_eks_cluster.eks_cluster.name}-vault-dynamodb-table-${namespace}"
    }
  }
}

resource "aws_dynamodb_table" "vault_dynamodb_table" {
  for_each     = local.participants_dynamodb_tables
  name         = each.value.name
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "Path"
  range_key    = "Key"

  attribute {
    name = "Key"
    type = "S"
  }

  attribute {
    name = "Path"
    type = "S"
  }

  server_side_encryption {
    enabled = true
  }

  point_in_time_recovery {
    enabled = true
  }

  tags = {
    tier = "dynamodb"
  }
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

  statement {
    sid    = "DynamoDBPermissions"
    effect = "Allow"

    actions = [
      "dynamodb:DescribeLimits",
      "dynamodb:DescribeTimeToLive",
      "dynamodb:ListTagsOfResource",
      "dynamodb:DescribeReservedCapacityOfferings",
      "dynamodb:DescribeReservedCapacity",
      "dynamodb:ListTables",
      "dynamodb:BatchGetItem",
      "dynamodb:BatchWriteItem",
      "dynamodb:CreateTable",
      "dynamodb:DeleteItem",
      "dynamodb:GetItem",
      "dynamodb:GetRecords",
      "dynamodb:PutItem",
      "dynamodb:Query",
      "dynamodb:UpdateItem",
      "dynamodb:Scan",
      "dynamodb:DescribeTable",
      "dynamodb:UpdateTable"
    ]

    resources = [
      for table in aws_dynamodb_table.vault_dynamodb_table : table.arn
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
