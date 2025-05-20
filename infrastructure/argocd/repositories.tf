locals {
  secrets_manager_cluster_secret_store_name = "aws-secretsmanager"
}

data "aws_secretsmanager_secret" "github_deploy_key_secret" {
  name = var.github_deploy_key_secret_name
}

data "aws_secretsmanager_secret" "helm_secret_age_key" {
  name = var.helm_secret_age_key
}

resource "kubernetes_manifest" "secrets_manager_secret_store" {
  manifest = yamldecode(<<EOT
    apiVersion: external-secrets.io/v1beta1
    kind: ClusterSecretStore
    metadata:
      name: ${local.secrets_manager_cluster_secret_store_name}
    spec:
      provider:
        aws:
          service: SecretsManager
          region: ${var.aws_region}
  EOT
  )

  depends_on = [kubernetes_namespace.argocd]
}

resource "kubernetes_manifest" "github_deploy_key_external_secret" {
  manifest = yamldecode(<<EOT
    apiVersion: external-secrets.io/v1beta1
    kind: ExternalSecret
    metadata:
      name: argocd-deploy-key
      namespace: ${kubernetes_namespace.argocd.metadata.0.name}
    spec:
      refreshInterval: 1h
      secretStoreRef:
        name: ${local.secrets_manager_cluster_secret_store_name}
        kind: ClusterSecretStore
      target:
        name: argocd-deploy-key
        template:
          metadata:
            labels:
              argocd.argoproj.io/secret-type: repository
          data:
            url: ${var.github_repository_url}
            type: git
            sshPrivateKey: |
              {{ .sshPrivateKey }}
      data:
        - secretKey: sshPrivateKey
          remoteRef:
            key: ${var.github_deploy_key_secret_name}
            property: sshPrivateKey
  EOT
  )

  depends_on = [kubernetes_manifest.secrets_manager_secret_store]
}

resource "kubernetes_manifest" "helm_secret_age_key" {
  manifest = yamldecode(<<EOT
    apiVersion: external-secrets.io/v1beta1
    kind: ExternalSecret
    metadata:
      name: helm-secret-age-key
      namespace: ${kubernetes_namespace.argocd.metadata.0.name}
    spec:
      refreshInterval: 1h
      secretStoreRef:
        name: ${local.secrets_manager_cluster_secret_store_name}
        kind: ClusterSecretStore
      target:
        name: helm-secrets-private-keys
      data:
        - secretKey: key.txt
          remoteRef:
            key: ${var.helm_secret_age_key}
            property: key.txt
  EOT
  )

  depends_on = [kubernetes_manifest.secrets_manager_secret_store]
}
