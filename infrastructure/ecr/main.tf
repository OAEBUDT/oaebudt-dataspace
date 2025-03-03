resource "aws_ecr_repository" "oaebudt_dataspace_ecr" {
  name                 = var.ecr_name
  image_tag_mutability = "MUTABLE"

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = var.aws_tags
}