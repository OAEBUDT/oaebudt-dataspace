locals {
  metrics_server_version = "3.12.2"
  add-ons_namespace = "kube-system"
  pod_identity_addon_version = "v1.3.5-eksbuild.2"
}

# Kubernetes Metrics Server community  Helm chart Addon
resource "helm_release" "metrics_server" {
  name = "metrics-server"
  description = "Kubernetes Metrics Server for collecting resource metrics"

  repository = "https://kubernetes-sigs.github.io/metrics-server/"
  chart      = "metrics-server"
  namespace  = local.add-ons_namespace
  version    = local.metrics_server_version
  max_history = 3

  values = [file("${path.module}/resources/metrics-server.yaml")]

  depends_on = [aws_eks_node_group.eks_worker_nodes]
}

# EKS Pod Identity AWS Addon
resource "aws_eks_addon" "pod_identity" {
  cluster_name  = aws_eks_cluster.eks_cluster.name
  addon_name    = "eks-pod-identity-agent"
  addon_version = local.pod_identity_addon_version
}
