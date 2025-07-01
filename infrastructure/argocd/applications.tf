locals {
  argocd_init_image_tag              = "646c13e"
  argocd_target_revision             = "develop"
  oaebudt_connector_release_names = [
    "publisher",
    "recipient",
    "liblynx",
    "jstor",
    "michigan",
    "punctumbooks",
    "ubiquitypress"
  ]
}

resource "kubernetes_manifest" "oaebudt_connector_argocd_app" {
  for_each = toset(local.oaebudt_connector_release_names)

  manifest = yamldecode(<<EOT
    apiVersion: argoproj.io/v1alpha1
    kind: Application
    metadata:
      name: ${each.value}
      namespace: ${kubernetes_namespace.argocd.metadata.0.name}
      annotations:
          argocd-image-updater.argoproj.io/image-list: connector=${var.aws_ecr_uri}
          argocd-image-updater.argoproj.io/update-strategy: newest-build
          argocd-image-updater.argoproj.io/write-back-method: git
    spec:
      destination:
        namespace: ${each.value}
        server: https://kubernetes.default.svc
      project: default
      source:
        path: connector/charts/oaebudt-connector
        helm:
          releaseName: ${each.value}
          valueFiles:
          - secrets+age-import:///helm-secrets-private-keys/key.txt?./values/values-${each.value}-enc.yaml
          parameters:
          - name: image.repository
            value: ${var.aws_ecr_uri}
          - name: image.tag
            value: ${local.argocd_init_image_tag}
        repoURL: ${var.github_repository_url}
        targetRevision: ${local.argocd_target_revision}
      syncPolicy:
        automated:
          allowEmpty: false
          prune: false
          selfHeal: true
        syncOptions:
        - Validate=true
        - CreateNamespace=true
        - PrunePropagationPolicy=foreground
        - PruneLast=true

  EOT
  )

  depends_on = [helm_release.argocd_release]
}
