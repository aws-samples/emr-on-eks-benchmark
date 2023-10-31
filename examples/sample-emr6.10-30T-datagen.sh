
#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2021 Amazon.com, Inc. or its affiliates.
# SPDX-License-Identifier: MIT-0
          # "spark.kubernetes.driver.limit.cores": "6.1",
          # "spark.kubernetes.executor.limit.cores": "8.3",
# set EMR virtual cluster name
# export EMRCLUSTER_NAME=emr-on-eks-nvme    
# export AWS_REGION=us-east-1
export ACCOUNTID=$(aws sts get-caller-identity --query Account --output text)                    
export VIRTUAL_CLUSTER_ID=$(aws emr-containers list-virtual-clusters --query "virtualClusters[?name == '$EMRCLUSTER_NAME' && state == 'RUNNING'].id" --output text)
export EMR_ROLE_ARN=arn:aws:iam::$ACCOUNTID:role/${EMRCLUSTER_NAME}-execution-role
export S3BUCKET=$EMRCLUSTER_NAME-$ACCOUNTID-$AWS_REGION
export ECR_URL="$ACCOUNTID.dkr.ecr.$AWS_REGION.amazonaws.com"

aws emr-containers start-job-run \
--virtual-cluster-id $VIRTUAL_CLUSTER_ID \
--name tpcds-30tb-datagen-250 \
--execution-role-arn $EMR_ROLE_ARN \
--release-label emr-6.10.0-latest \
--job-driver '{
  "sparkSubmitJobDriver": {
      "entryPoint": "local:///usr/lib/spark/examples/jars/eks-spark-benchmark-assembly-1.0.jar",
      "entryPointArguments":["s3://'$S3BUCKET'/BLOG_TPCDS-TEST-30T-partitioned","/opt/tpcds-kit/tools","parquet","30000","2000","true","true","true"],
      "sparkSubmitParameters": "--class com.amazonaws.eks.tpcds.DataGeneration --conf spark.driver.cores=6 --conf spark.driver.memory=8G  --conf spark.executor.cores=8 --conf spark.executor.memory=12G  --conf spark.executor.instances=249"}}' \
--configuration-overrides '{
    "applicationConfiguration": [
      {
        "classification": "spark-defaults", 
        "properties": {
          "spark.kubernetes.container.image": "'$ECR_URL'/eks-spark-benchmark:emr6.10-delta-clb",
          "spark.kubernetes.driver.podTemplateFile": "s3://'$S3BUCKET'/app_code/pod-template/driver-pod-template.yaml",
          "spark.kubernetes.executor.podTemplateFile": "s3://'$S3BUCKET'/app_code/pod-template/executor-pod-template.yaml",

          "spark.network.timeout": "2000s",
          "spark.executor.heartbeatInterval": "300s",
          "spark.executor.memoryOverhead": "3G",

          "spark.kubernetes.executor.podNamePrefix": "emr-tpcds-datagen",
          "spark.serializer": "org.apache.spark.serializer.KryoSerializer",
          "spark.kubernetes.node.selector.eks.amazonaws.com/nodegroup": "c5d9a",

          "spark.celeborn.shuffle.chunk.size": "4m",
          "spark.celeborn.client.push.maxReqsInFlight": "128",
          "spark.celeborn.rpc.askTimeout": "2000s",
          "spark.celeborn.client.push.replicate.enabled": "true",
          "spark.celeborn.client.push.blacklist.enabled": "true",
          "spark.celeborn.client.push.excludeWorkerOnFailure.enabled": "true",
          "spark.celeborn.client.fetch.excludeWorkerOnFailure.enabled": "true",
          "spark.celeborn.client.commitFiles.ignoreExcludedWorker": "true",
          "spark.shuffle.manager": "org.apache.spark.shuffle.celeborn.SparkShuffleManager",
          "spark.celeborn.master.endpoints": "celeborn-master-0.celeborn-master-svc.celeborn:9097,celeborn-master-1.celeborn-master-svc.celeborn:9097,celeborn-master-2.celeborn-master-svc.celeborn:9097",
          "spark.sql.optimizedUnsafeRowSerializers.enabled":"false"
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