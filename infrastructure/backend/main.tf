resource "aws_s3_bucket" "terraform_state_bucket" {
  bucket = var.bucket_name
  lifecycle {
    prevent_destroy = true
  }
}