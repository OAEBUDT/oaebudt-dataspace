locals {
  argocd_image_updater_service_account = "argocd-image-updater"
}

resource "aws_iam_role" "argocd_image_updater" {
  name = "${data.aws_eks_cluster.eks_cluster_d.name}-argocd-image-updater"

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

resource "aws_iam_role_policy_attachment" "argocd_image_updater" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.argocd_image_updater.name
}

resource "aws_eks_pod_identity_association" "argocd_image_updater" {
  cluster_name    = data.aws_eks_cluster.eks_cluster_d.name
  namespace       = kubernetes_namespace.argocd.metadata.0.name
  service_account = local.argocd_image_updater_service_account
  role_arn        = aws_iam_role.argocd_image_updater.arn

  tags = {
    tier = "iam"
  }
}

resource "helm_release" "updater" {
  name = "image-updater"

  repository  = "https://argoproj.github.io/argo-helm"
  chart       = "argocd-image-updater"
  namespace   = kubernetes_namespace.argocd.metadata.0.name
  version     = var.argocd_image_updater_chart_version
  max_history = 3

  values = [
    templatefile("${path.module}/resources/helm/templates/image-updater.yaml", {
      service_account = local.argocd_image_updater_service_account
      aws_region      = var.aws_region
      ecr_uri         = var.aws_ecr_uri
      aws_ecr_prefix  = var.aws_ecr_prefix
    })
  ]

  depends_on = [helm_release.argocd_release]
}
