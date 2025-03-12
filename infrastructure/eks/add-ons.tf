locals {
  addons_namespace             = "kube-system"
  aws_lbc_service_account      = "aws-load-balancer-controller"
  aws_ebs_csic_service_account = "ebs-csi-controller-sa"
}
#########################################################
# Kubernetes Metrics Server community Helm chart Addon
#########################################################
resource "helm_release" "metrics_server" {
  name        = "metrics-server"
  description = "Kubernetes Metrics Server for collecting resource metrics"

  repository  = "https://kubernetes-sigs.github.io/metrics-server/"
  chart       = "metrics-server"
  namespace   = local.addons_namespace
  version     = var.metrics_server_chart_version
  max_history = 3

  values = [file("${path.module}/resources/helm-values/metrics-server.yaml")]

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
# AWS Load Balancer Controller Helm chart Addon
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
    templatefile("${path.module}/resources/helm-values/aws-lbc.yaml", {
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
