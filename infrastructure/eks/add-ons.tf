locals {
  addons_namespace                 = "kube-system"
  aws_lbc_service_account          = "aws-load-balancer-controller"
  aws_ebs_csic_service_account     = "ebs-csi-controller-sa"
  external_dns_service_account     = "external-dns"
  external_secrets_service_account = "external-secrets"
}
#########################################################
# Metrics Server community Addon
#########################################################
resource "helm_release" "metrics_server" {
  name        = "metrics-server"
  description = "Kubernetes Metrics Server for collecting resource metrics"

  repository  = "https://kubernetes-sigs.github.io/metrics-server/"
  chart       = "metrics-server"
  namespace   = local.addons_namespace
  version     = var.metrics_server_chart_version
  max_history = 3

  values = [file("${path.module}/resources/helm/templates/metrics-server.yaml")]

  depends_on = [aws_eks_node_group.eks_worker_nodes]
}
##############################
# EKS Pod Identity AWS Addon
##############################
resource "aws_eks_addon" "pod_identity" {
  cluster_name  = aws_eks_cluster.eks_cluster.name
  addon_name    = "eks-pod-identity-agent"
  addon_version = var.pod_identity_addon_version

  tags = {
    tier     = "eks-addon"
    category = "observability"
  }
}

#################################################
# AWS Load Balancer Controller Addon
#################################################
resource "aws_iam_role" "aws_lbc" {
  name = "${aws_eks_cluster.eks_cluster.name}-aws-lbc"

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

resource "aws_iam_policy" "aws_lbc" {
  policy = file("./resources/iam/policies/aws-lb-controller.json")
  name   = "AWSLoadBalancerController"

  tags = {
    tier = "iam"
  }
}

resource "aws_iam_role_policy_attachment" "aws_lbc" {
  policy_arn = aws_iam_policy.aws_lbc.arn
  role       = aws_iam_role.aws_lbc.name
}

resource "aws_eks_pod_identity_association" "aws_lbc" {
  cluster_name    = aws_eks_cluster.eks_cluster.name
  namespace       = local.addons_namespace
  service_account = local.aws_lbc_service_account
  role_arn        = aws_iam_role.aws_lbc.arn

  tags = {
    tier = "iam"
  }
}

resource "helm_release" "aws_lbc" {
  name = "aws-load-balancer-controller"

  repository  = "https://aws.github.io/eks-charts"
  chart       = "aws-load-balancer-controller"
  namespace   = local.addons_namespace
  version     = var.aws_lbc_chart_version
  max_history = 3


  values = [
    templatefile("${path.module}/resources/helm/templates/aws-lbc.yaml", {
      cluster_name    = aws_eks_cluster.eks_cluster.name
      service_account = local.aws_lbc_service_account
      vpc_id          = aws_vpc.main.id
    })
  ]

  depends_on = [helm_release.metrics_server]
}
################################
# EKS EBS CSI Driver AWS Addon
################################
resource "aws_iam_role" "ebs_csi_driver" {
  name = "${aws_eks_cluster.eks_cluster.name}-ebs-csi-driver"

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

resource "aws_iam_role_policy_attachment" "ebs_csi_driver" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonEBSCSIDriverPolicy"
  role       = aws_iam_role.ebs_csi_driver.name
}

resource "aws_eks_pod_identity_association" "ebs_csi_driver" {
  cluster_name    = aws_eks_cluster.eks_cluster.name
  namespace       = local.addons_namespace
  service_account = local.aws_ebs_csic_service_account
  role_arn        = aws_iam_role.ebs_csi_driver.arn
}

resource "aws_eks_addon" "ebs_csi_driver" {
  cluster_name             = aws_eks_cluster.eks_cluster.name
  addon_name               = "aws-ebs-csi-driver"
  addon_version            = var.aws_ebs_csi_driver_addon_version
  service_account_role_arn = aws_iam_role.ebs_csi_driver.arn

  depends_on = [aws_eks_node_group.eks_worker_nodes]

  tags = {
    tier     = "eks-addon"
    category = "storage"
  }
}

################
# External DNS
################
resource "aws_iam_role" "external_dns" {
  name = "${aws_eks_cluster.eks_cluster.name}-external-dns"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "sts:AssumeRole",
          "sts:TagSession"
        ]
        Effect = "Allow"
        Principal = {
          Service = "pods.eks.amazonaws.com"
        }
      },
    ]
  })

  tags = {
    tier = "iam"
  }
}

resource "aws_iam_policy" "external_dns" {
  policy = file("./resources/iam/policies/external-dns.json")
  name   = "AllowExternalDNSUpdates"

  tags = {
    tier = "iam"
  }
}

resource "aws_iam_role_policy_attachment" "external_dns" {
  policy_arn = aws_iam_policy.external_dns.arn
  role       = aws_iam_role.external_dns.name
}

resource "aws_eks_pod_identity_association" "external_dns" {
  cluster_name    = aws_eks_cluster.eks_cluster.name
  namespace       = local.addons_namespace
  service_account = local.external_dns_service_account
  role_arn        = aws_iam_role.external_dns.arn

  tags = {
    tier = "iam"
  }
}

resource "helm_release" "external_dns" {
  name = "external-dns"

  repository  = "https://kubernetes-sigs.github.io/external-dns/"
  chart       = "external-dns"
  namespace   = local.addons_namespace
  version     = var.external_dns_chart_version
  max_history = 3


  values = [
    templatefile("${path.module}/resources/helm/templates/external-dns.yaml", {
      external_dns_policy = "sync"
      domain_name         = var.domain_name
      service_account     = local.external_dns_service_account
    })
  ]

  depends_on = [helm_release.aws_lbc]
}

#####################
# External Secrets
#####################
resource "aws_iam_role" "external_secrets" {
  name = "${aws_eks_cluster.eks_cluster.name}-external-secrets"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "sts:AssumeRole",
          "sts:TagSession"
        ]
        Effect = "Allow"
        Principal = {
          Service = "pods.eks.amazonaws.com"
        }
      },
    ]
  })

  tags = {
    tier = "iam"
  }
}

data "aws_iam_policy_document" "external_secrets" {
  statement {
    actions = [
      "secretsmanager:ListSecrets",
      "secretsmanager:BatchGetSecretValue"
    ]
    effect    = "Allow"
    resources = ["*"]
  }

  statement {
    actions = [
      "secretsmanager:GetResourcePolicy",
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:ListSecretVersionIds",
      "secretsmanager:GetRandomPassword"
    ]
    effect = "Allow"
    resources = [
      "arn:aws:secretsmanager:${var.aws_region}:*:secret:${var.eks_secret_name_prefix}*"
    ]
  }
}

resource "aws_iam_policy" "external_secrets" {
  name   = "AllowExternalSecretsGetSecrets"
  policy = data.aws_iam_policy_document.external_secrets.json

  tags = {
    tier = "iam"
  }
}

resource "aws_iam_role_policy_attachment" "external_secrets" {
  policy_arn = aws_iam_policy.external_secrets.arn
  role       = aws_iam_role.external_secrets.name
}

resource "aws_eks_pod_identity_association" "external_secrets" {
  cluster_name    = aws_eks_cluster.eks_cluster.name
  namespace       = local.addons_namespace
  service_account = local.external_secrets_service_account
  role_arn        = aws_iam_role.external_secrets.arn

  tags = {
    tier = "iam"
  }
}

resource "helm_release" "external_secrets" {
  name = "external-secrets"

  repository  = "https://charts.external-secrets.io"
  chart       = "external-secrets"
  namespace   = local.addons_namespace
  version     = var.external_secrets_chart_version
  max_history = 3


  values = [
    templatefile("${path.module}/resources/helm/templates/external-secrets.yaml", {
      service_account = local.external_secrets_service_account
    })
  ]

  depends_on = [helm_release.external_dns]
}
