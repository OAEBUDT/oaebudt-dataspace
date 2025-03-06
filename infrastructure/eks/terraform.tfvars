aws_region          = "us-east-1"
project_name        = "oebudt-dataspace"
aws_tags = {
  environment   = "development"
  project       = "oaebudt-dataspace"
  tier          = "eks"
}
vpc_name                       = "oaebudt-dataspace"
eks_environment                = "development"
eks_availability_zones         = ["us-east-1a", "us-east-1b"]
eks_name                       = "oaebudt-dataspace"
eks_version                    = "1.32"
vpc_cidr_block                 = "10.0.0.0/16"
subnet_cidr_blocks             = ["10.0.0.0/20", "10.0.16.0/20", "10.0.32.0/20", "10.0.48.0/20"]
