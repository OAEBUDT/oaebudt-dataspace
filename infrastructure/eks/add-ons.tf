locals {
  metrics_server_chart_version = "3.12.2"
  add-ons_namespace            = "kube-system"
  pod_identity_addon_version   = "v1.3.5-eksbuild.2"
  aws_lbc_chart_version        = "1.11.0"
  aws_lbc_service_account      = "aws-load-balancer-controller"
}

# Kubernetes Metrics Server community  Helm chart Addon
resource "helm_release" "metrics_server" {
  name        = "metrics-server"
  description = "Kubernetes Metrics Server for collecting resource metrics"

  repository  = "https://kubernetes-sigs.github.io/metrics-server/"
  chart       = "metrics-server"
  namespace   = local.add-ons_namespace
  version     = local.metrics_server_chart_version
  max_history = 3

  values = [file("${path.module}/resources/helm-values/metrics-server.yaml")]

  depends_on = [aws_eks_node_group.eks_worker_nodes]
}

# EKS Pod Identity AWS Addon
resource "aws_eks_addon" "pod_identity" {
  cluster_name  = aws_eks_cluster.eks_cluster.name
  addon_name    = "eks-pod-identity-agent"
  addon_version = local.pod_identity_addon_version

  tags = {
    tier = "eks"
  }
}

# EKS Pod Identity AWS Addon
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

# AWS Load Balancer Controller Helm chart Addon
resource "aws_iam_policy" "aws_lbc" {
  policy = file("./resources/iam/aws-lb-controller.json")
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
  namespace       = local.add-ons_namespace
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
  namespace   = local.add-ons_namespace
  version     = local.aws_lbc_chart_version
  max_history = 3

  set {
    name  = "clusterName"
    value = aws_eks_cluster.eks_cluster.name
  }

  set {
    name  = "serviceAccount.name"
    value = local.aws_lbc_service_account
  }

  set {
    name  = "vpcId"
    value = aws_vpc.main.id
  }

  depends_on = [helm_release.metrics_server]
}
