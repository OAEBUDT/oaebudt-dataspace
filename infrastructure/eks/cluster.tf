# EKS Control Plane IAM Role
resource "aws_iam_role" "eks_control_plane" {
  name = "${var.eks_environment}-${var.eks_name}-eks-control-plane"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "sts:AssumeRole",
          "sts:TagSession"
        ]
        Effect = "Allow"
        Principal = {
          Service = "eks.amazonaws.com"
        }
      },
    ]
  })

  tags = {
    tier = "iam"
  }
}

# Attaching AmazonEKSClusterPolicy to Control Plane Role
resource "aws_iam_role_policy_attachment" "eks_control_plane" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
  role       = aws_iam_role.eks_control_plane.name
}

# EKS Cluster Resource
resource "aws_eks_cluster" "eks_cluster" {
  name     = "${var.eks_environment}-${var.eks_name}"
  version  = var.eks_version
  role_arn = aws_iam_role.eks_control_plane.arn

  vpc_config {
    endpoint_private_access = false
    endpoint_public_access  = true

    subnet_ids = [
      for idx in range(length(var.eks_availability_zones)) :
      aws_subnet.private_zone[var.eks_availability_zones[idx]].id
    ]
  }

  access_config {
    authentication_mode                         = "API"
    bootstrap_cluster_creator_admin_permissions = true
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_control_plane,
  ]

  tags = {
    tier = "eks"
  }
}

# EKS Worker Node IAM Role
resource "aws_iam_role" "eks_worker_nodes" {
  name = "${var.eks_environment}-${var.eks_name}-eks-nodes"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = [
          "sts:AssumeRole"
        ]
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      },
    ]
  })

  tags = {
    tier = "iam"
  }
}

# Attaching AmazonEKSWorkerNodePolicy to Worker Node IAM Role
resource "aws_iam_role_policy_attachment" "eks_worker_node_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
  role       = aws_iam_role.eks_worker_nodes.name
}

# Attaching AmazonEKS_CNI_Policy to Worker Node IAM Role
resource "aws_iam_role_policy_attachment" "eks_cni_policy" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
  role       = aws_iam_role.eks_worker_nodes.name
}

# Attaching AmazonEC2ContainerRegistryReadOnly policy to Worker Node IAM Role
resource "aws_iam_role_policy_attachment" "ec2_ecr_ro" {
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
  role       = aws_iam_role.eks_worker_nodes.name
}

# Node Groups - General Purpose
resource "aws_eks_node_group" "eks_worker_nodes" {
  cluster_name    = aws_eks_cluster.eks_cluster.name
  version         = var.eks_version
  node_group_name = "general-purpose"
  node_role_arn   = aws_iam_role.eks_worker_nodes.arn

  subnet_ids = [
    for idx in range(length(var.eks_availability_zones)) :
    aws_subnet.private_zone[var.eks_availability_zones[idx]].id
  ]

  capacity_type  = var.eks_node_ec2_capacity_type
  instance_types = var.eks_node_ec2_instance_types

  scaling_config {
    desired_size = var.eks_node_scaling_config.desired_size
    max_size     = var.eks_node_scaling_config.max_size
    min_size     = var.eks_node_scaling_config.min_size
  }

  update_config {
    max_unavailable = 1
  }

  labels = {
    role        = "general"
    environment = var.eks_environment
  }

  depends_on = [
    aws_iam_role_policy_attachment.eks_worker_node_policy,
    aws_iam_role_policy_attachment.eks_cni_policy,
    aws_iam_role_policy_attachment.ec2_ecr_ro,
  ]

  # Allow autoscale changes without Terraform plan difference
  lifecycle {
    ignore_changes = [scaling_config[0].desired_size]
  }

  tags = {
    tier = "eks"
  }
}
