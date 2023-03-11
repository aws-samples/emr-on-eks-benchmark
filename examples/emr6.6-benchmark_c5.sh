#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2021 Amazon.com, Inc. or its affiliates.
# SPDX-License-Identifier: MIT-0


# export EMRCLUSTER_NAME=emr-on-eks-rss
# export AWS_REGION=us-east-1
export ACCOUNTID=$(aws sts get-caller-identity --query Account --output text)
export VIRTUAL_CLUSTER_ID=$(aws emr-containers list-virtual-clusters --query "virtualClusters[?name == '$EMRCLUSTER_NAME' && state == 'RUNNING'].id" --output text)
export EMR_ROLE_ARN=arn:aws:iam::$ACCOUNTID:role/$EMRCLUSTER_NAME-execution-role
export S3BUCKET=$EMRCLUSTER_NAME-$ACCOUNTID-$AWS_REGION
export ECR_URL="$ACCOUNTID.dkr.ecr.$AWS_REGION.amazonaws.com"

aws emr-containers start-job-run \
  --virtual-cluster-id $VIRTUAL_CLUSTER_ID \
  --name emr610-JDK11.0.16-crossacct \
  --execution-role-arn $EMR_ROLE_ARN \
  --release-label emr-6.9.0-latest \
  --retry-policy-configuration '{"maxAttempts": 5}' \
  --job-driver '{
  "sparkSubmitJobDriver": {
      "entryPoint": "local:///usr/lib/spark/examples/jars/eks-spark-benchmark-assembly-1.0.jar",
      "entryPointArguments":["s3://'$S3BUCKET'/BLOG_TPCDS-TEST-3T-partitioned","s3://emr-eks-demo-720560070661-us-east-1/JDK_EMRONEKS_TPCDS-TEST-3T-RESULT","/opt/tpcds-kit/tools","parquet","3000","1","false","q1-v2.4,q10-v2.4,q11-v2.4","true"],
      "sparkSubmitParameters": "--class com.amazonaws.eks.tpcds.BenchmarkSQL --conf spark.driver.cores=4 --conf spark.driver.memory=5g --conf spark.executor.cores=4 --conf spark.executor.memory=6g --conf spark.executor.instances=47"}}' \
  --configuration-overrides '{
    "applicationConfiguration": [
      {
        "classification": "spark-defaults", 
        "properties": {
          "spark.kubernetes.container.image": "melodydocker/emr-jdk11",
          "spark.kubernetes.driver.podTemplateFile": "s3://'$S3BUCKET'/app_code/pod-template/driver-pod-template.yaml",
          "spark.kubernetes.executor.podTemplateFile": "s3://'$S3BUCKET'/app_code/pod-template/executor-pod-template.yaml",
          "spark.kubernetes.driver.limit.cores": "4.1",
          "spark.kubernetes.executor.limit.cores": "4.3",
          "spark.driver.memoryOverhead": "1000",
          "spark.executor.memoryOverhead": "2G",
          "spark.network.timeout": "2000s",
          "spark.executor.heartbeatInterval": "300s",
          "spark.kubernetes.node.selector.eks.amazonaws.com/nodegroup": "c59d",

          "spark.hadoop.fs.s3.bucket.emr-eks-demo-720560070661-us-east-1.customAWSCredentialsProvider": "com.amazonaws.emr.AssumeRoleAWSCredentialsProvider",
          "spark.kubernetes.driverEnv.ASSUME_ROLE_CREDENTIALS_ROLE_ARN": "arn:aws:iam::720560070661:role/EMRContainers-JobExecutionRole",          
          "spark.executorEnv.ASSUME_ROLE_CREDENTIALS_ROLE_ARN": "arn:aws:iam::720560070661:role/EMRContainers-JobExecutionRole"   
      }},
      {
        "classification": "spark-log4j",
        "properties": {
          "rootLogger.level" : "WARN"
          }
      }
    ], 
    "monitoringConfiguration": {
      "s3MonitoringConfiguration": {"logUri": "s3://'$S3BUCKET'/elasticmapreduce/emr-containers"}}}'
