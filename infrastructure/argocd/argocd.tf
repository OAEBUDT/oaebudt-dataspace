resource "kubernetes_namespace" "argocd" {
  metadata {
    name = var.argocd_namespace
  }
}

resource "helm_release" "argocd_release" {
  name = "argocd"

  repository  = "https://argoproj.github.io/argo-helm"
  chart       = "argo-cd"
  namespace   = kubernetes_namespace.argocd.metadata.0.name
  version     = var.argocd_chart_version
  max_history = 3

  values = [
    file("${path.module}/resources/helm/templates/argocd.yaml")
  ]

  depends_on = [kubernetes_namespace.argocd]
}
