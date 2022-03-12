#!/bin/bash

# SPDX-FileCopyrightText: Copyright 2021 Amazon.com, Inc. or its affiliates.
# SPDX-License-Identifier: MIT-0

# Define params
# export EKSCLUSTER_NAME=eks-nvme
# export AWS_REGION=us-east-1
export EMRCLUSTER_NAME=emr-on-$EKSCLUSTER_NAME
export ROLE_NAME=${EMRCLUSTER_NAME}-execution-role
export ACCOUNTID=$(aws sts get-caller-identity --query Account --output text)
export S3TEST_BUCKET=${EMRCLUSTER_NAME}-${ACCOUNTID}-${AWS_REGION}
export VIRTUAL_CLUSTER_ID=$(aws emr-containers list-virtual-clusters --query "virtualClusters[?name == '${EMRCLUSTER_NAME}' && state == 'RUNNING'].id" --output text)
# export ROLE_ARN=arn:aws:iam::$ACCOUNTID:role/$ROLE_NAME
export POLICY_ARN=arn:aws:iam::$ACCOUNTID:policy/${ROLE_NAME}-policy
export ECR_URL="$ACCOUNTID.dkr.ecr.$AWS_REGION.amazonaws.com"

# clean up resources
aws emr-containers delete-virtual-cluster --id $VIRTUAL_CLUSTER_ID
eksctl delete cluster --name $EKSCLUSTER_NAME --region $AWS_REGION
aws iam detach-role-policy --role-name $ROLE_NAME --policy-arn $POLICY_ARN
aws iam delete-role --role-name $ROLE_NAME
aws iam delete-policy --policy-arn $POLICY_ARN
aws s3 rm s3://$S3TEST_BUCKET --recursive
aws s3api delete-bucket --bucket $S3TEST_BUCKET
# uncomment it out if use ECR to store the benchmark utility
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URL
aws ecr delete-repository --repository-name eks-spark-benchmark --force