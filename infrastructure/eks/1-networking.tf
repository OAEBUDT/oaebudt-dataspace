resource "aws_vpc" "main" {
  cidr_block = var.vpc_cidr_block

  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = merge(var.aws_tags, { Name = "${var.vpc_name}-vpc" })
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id

  tags = merge(var.aws_tags, { Name = "${var.vpc_name}-igw" })
}

resource "aws_subnet" "private_zone" {
  for_each = {
    for idx, az in var.eks_availability_zones : az => {
      subnet_cidr_block = var.subnet_cidr_blocks[idx]
      availability_zone         = az
    }
  }

  vpc_id            = aws_vpc.main.id
  cidr_block        = each.value.subnet_cidr_block
  availability_zone = each.value.availability_zone

  tags = merge(var.aws_tags, {
    "Name"                                                 = "${var.eks_environment}-private-${each.value.availability_zone}"
    "kubernetes.io/role/internal-elb"                      = "1"
    "kubernetes.io/cluster/${var.eks_environment}-${var.eks_name}" = "owned"
  })
}

resource "aws_subnet" "public_zone" {
  for_each = {
    for idx, az in var.eks_availability_zones : az => {
      subnet_cidr_block = var.subnet_cidr_blocks[idx + 2]
      availability_zone         = az
    }
  }

  vpc_id            = aws_vpc.main.id
  cidr_block        = each.value.subnet_cidr_block
  availability_zone = each.value.availability_zone
  map_public_ip_on_launch = true

  tags = merge(var.aws_tags, {
    "Name"                                                         = "${var.eks_environment}-public-${each.value.availability_zone}"
    "kubernetes.io/role/elb"                                       = "1"
    "kubernetes.io/cluster/${var.eks_environment}-${var.eks_name}" = "owned"
  })
}
