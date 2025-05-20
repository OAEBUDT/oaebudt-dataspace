variable "aws_region" {
  type        = string
  description = "AWS region to deploy to"
  default     = "us-east-1"
}

variable "project_name" {
  type        = string
  description = "Name of the project"
}

variable "project_environment" {
  type        = string
  description = "Project environment or stage"
}

variable "eks_cluster_name" {
  description = "Name of the EKS cluster"
  type        = string
  default     = "eks-cluster"
}

variable "argocd_chart_version" {
  type        = string
  description = "Version of the ArgoCD Helm chart"
}

variable "argocd_image_updater_chart_version" {
  type        = string
  description = "Version of the ArgoCD Image Updater Helm chart"
}

variable "aws_ecr_uri" {
  type        = string
  description = "URI of the AWS ECR repository"
}

variable "aws_ecr_prefix" {
  type        = string
  description = "Prefix of the AWS ECR repository"
}

variable "github_repository_url" {
  type        = string
  description = "URl of the GitHub repository"
}

variable "github_deploy_key_secret_name" {
  type        = string
  description = "Name of the GitHub deploy key secret in AWS Secrets Manager"
}

variable "helm_secret_age_key" {
  type        = string
  description = "Name of the Helm secret age key in AWS Secrets Manager"
}

variable "argocd_namespace" {
  type        = string
  description = "Namespace for ArgoCD"
}

variable "external_secrets_namespace" {
  type        = string
  description = "Namespace of the external secrets"
}
