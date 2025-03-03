resource "aws_ecr_repository" "oaebudt_dataspace_ecr" {
  name                 = var.ecr_name
  image_tag_mutability = "MUTABLE"

  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = var.aws_tags
}

# resource "aws_ecr_lifecycle_policy" "ecr_policy_untagged_images" {
#   repository = aws_ecr_repository.oaebudt_dataspace_ecr
#
#   policy = <<EOF
# {
#   "rules": [
#     {
#       "rulePriority": 1,
#       "description": "Expire untagged images older than 7 days",
#       "selection": {
#         "tagStatus": "untagged",
#         "countType": "sinceImagePushed",
#         "countUnit": "days",
#         "countNumber": 7
#       },
#       "action": {
#         "type": "expire"
#       }
#     }
#   ]
# }
# EOF
# }

# resource "aws_ecr_lifecycle_policy" "ecr_policy_tagged_images" {
#   repository = aws_ecr_repository.oaebudt_dataspace_ecr
#
#   policy = <<EOF
# {
#     "rules": [
#         {
#             "rulePriority": 2,
#             "description": "Keep last 20 images",
#             "selection": {
#                 "tagStatus": "tagged",
#                 "countType": "imageCountMoreThan",
#                 "countNumber": 20
#             },
#             "action": {
#                 "type": "expire"
#             }
#         }
#     ]
# }
# EOF
# }