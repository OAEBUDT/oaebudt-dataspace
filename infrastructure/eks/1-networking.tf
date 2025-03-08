resource "aws_vpc" "main" {
  cidr_block = var.vpc_cidr_block

  enable_dns_support   = true
  enable_dns_hostnames = true

  tags = {
    Name = "${var.vpc_name}-vpc"
    tier = "vpc"
  }
}

resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.vpc_name}-igw"
    tier = "igw"
  }
}

resource "aws_subnet" "private_zone" {
  for_each = {
    for idx, az in var.eks_availability_zones : az => {
      subnet_cidr_block = var.subnet_cidr_blocks[idx]
      availability_zone = az
    }
  }

  vpc_id            = aws_vpc.main.id
  cidr_block        = each.value.subnet_cidr_block
  availability_zone = each.value.availability_zone

  tags = {
    "Name"                                                         = "${var.eks_environment}-private-${each.value.availability_zone}"
    "kubernetes.io/role/internal-elb"                              = "1"
    "kubernetes.io/cluster/${var.eks_environment}-${var.eks_name}" = "owned"
    tier                                                           = "private-subnet"
  }
}

resource "aws_subnet" "public_zone" {
  for_each = {
    for idx, az in var.eks_availability_zones : az => {
      subnet_cidr_block = var.subnet_cidr_blocks[idx + length(var.eks_availability_zones)]
      availability_zone = az
    }
  }

  vpc_id                  = aws_vpc.main.id
  cidr_block              = each.value.subnet_cidr_block
  availability_zone       = each.value.availability_zone
  map_public_ip_on_launch = true

  tags = {
    "Name"                                                         = "${var.eks_environment}-public-${each.value.availability_zone}"
    "kubernetes.io/role/elb"                                       = "1"
    "kubernetes.io/cluster/${var.eks_environment}-${var.eks_name}" = "owned"
    tier                                                           = "public-subnet"
  }
}


data "aws_eip" "eip_nat" {
  public_ip = "52.205.124.194"
}

# The NAT Gateway uses a manually provisioned Elastic IP to ensure a stable, static public IP for outbound traffic.
# This approach prevents accidental deletion during Terraform operations and preserves existing firewall rules
# and partner-side whitelisting configurations.
resource "aws_nat_gateway" "nat_gw" {
  allocation_id = data.aws_eip.eip_nat.id
  subnet_id     = aws_subnet.public_zone[var.eks_availability_zones[0]].id

  tags = {
    Name = "${var.vpc_name}-natgw"
  }

  depends_on = [aws_internet_gateway.igw]
}

resource "aws_route_table" "rt_private_zone" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block     = "0.0.0.0/0"
    nat_gateway_id = aws_nat_gateway.nat_gw.id
  }

  tags = {
    Name = "${var.vpc_name}-private-route"
  }
}

resource "aws_route_table" "rt_public_zone" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }

  tags = {
    Name = "${var.vpc_name}-public-route"
  }
}

resource "aws_route_table_association" "rt_private_zone" {
  for_each = {
    for idx, az in var.eks_availability_zones : az => {
      subnet_id = aws_subnet.private_zone[az].id
    }
  }
  subnet_id      = each.value.subnet_id
  route_table_id = aws_route_table.rt_private_zone.id
}

resource "aws_route_table_association" "rt_public_zone" {
  for_each = {
    for idx, az in var.eks_availability_zones : az => {
      subnet_id = aws_subnet.public_zone[az].id
    }
  }
  subnet_id      = each.value.subnet_id
  route_table_id = aws_route_table.rt_public_zone.id
}
