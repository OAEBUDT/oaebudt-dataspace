variable "ecr_name" {
  type        = string
  description = "Name of the Amazon ECR repository where Docker images and Helm charts will be stored"
}

variable "ecr_tags" {
  description = "A map of tags to apply to AWS resources of the ecr module"
  type        = map(string)
}
