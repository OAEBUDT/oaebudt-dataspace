# S3 bucket for storing Terraform state
resource "aws_s3_bucket" "terraform_state_bucket" {
  bucket = var.tfstate_bucket_name
  lifecycle {
    prevent_destroy = false
  }
  tags = {
    tier = "terraform"
  }
}

# Enable Terraform state S3 bucket versioning
resource "aws_s3_bucket_versioning" "terraform_state_bucket_versioning" {
  bucket = aws_s3_bucket.terraform_state_bucket.id
  versioning_configuration {
    status = "Enabled"
  }
}
