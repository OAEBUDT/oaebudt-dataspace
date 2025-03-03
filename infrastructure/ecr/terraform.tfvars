aws_region          = "us-east-1"
project_name        = "oebudt-dataspace"
aws_tags = {
  environment   = "development"
  project       = "oaebudt-dataspace"
  tier          = "ecr"
}
tfstate_bucket_name = "oaebudt-dataspace-infra-terraform-state"
tfstate_key         = "ecr.tfstate"
ecr_name            = "oaebudt-dataspace"