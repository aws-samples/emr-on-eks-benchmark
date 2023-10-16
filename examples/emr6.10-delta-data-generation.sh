#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2021 Amazon.com, Inc. or its affiliates.
# SPDX-License-Identifier: MIT-0   

# export EMRCLUSTER_NAME=emr-on-eks-nvme    
# export AWS_REGION=us-east-1
# export ACCOUNTID=$(aws sts get-caller-identity --query Account --output text)                    
# export VIRTUAL_CLUSTER_ID=$(aws emr-containers list-virtual-clusters --query "virtualClusters[?name == '$EMRCLUSTER_NAME' && state == 'RUNNING'].id" --output text)
# export EMR_ROLE_ARN=arn:aws:iam::$ACCOUNTID:role/$EMRCLUSTER_NAME-execution-role
# export S3BUCKET=$EMRCLUSTER_NAME-$ACCOUNTID-$AWS_REGION
export ECR_URL="$ACCOUNTID.dkr.ecr.$AWS_REGION.amazonaws.com"
          # "spark.hive.metastore.uris": "thrift://hive-metastore:9083",
          # "spark.sql.warehouse.dir": "s3://'$S3BUCKET'/warehouse/",
                    # "spark.hadoop.fs.s3.useRequesterPaysHeader": "true",

aws emr-containers start-job-run \
--virtual-cluster-id $VIRTUAL_CLUSTER_ID \
--name delta-generated-3TB-gluedb \
--execution-role-arn $EMR_ROLE_ARN \
--release-label emr-6.10.0-latest \
--job-driver '{
  "sparkSubmitJobDriver": {
      "entryPoint": "local:///usr/lib/spark/examples/delta/delta-benchmarks.jar",
      "entryPointArguments":["--format","delta","--scale-in-gb","3000","--exclude-nulls","True","--db-name","default","--benchmark-path","s3://'$S3BUCKET'/app_code/data/delta/tpcds_3tb_delta","--source-path","s3://'$S3BUCKET'/BLOG_TPCDS-TEST-3T-partitioned"],
      "sparkSubmitParameters": "--jars local:///usr/share/aws/delta/lib/delta-core.jar,local:///usr/share/aws/delta/lib/delta-storage.jar,local:///usr/share/aws/delta/lib/delta-storage-s3-dynamodb.jar --class benchmark.TPCDSDataLoad --conf spark.driver.cores=10 --conf spark.driver.memory=10g --conf spark.executor.cores=11 --conf spark.executor.memory=15g  --conf spark.executor.instances=26"}}' \
--configuration-overrides '{
    "applicationConfiguration": [
      {
        "classification": "spark-defaults", 
        "properties": {
          "spark.kubernetes.container.image": "'$ECR_URL'/eks-spark-benchmark:emr6.10-delta",
          "spark.kubernetes.driver.podTemplateFile": "s3://'$S3BUCKET'/app_code/driver-pod-template.yaml",
          "spark.kubernetes.executor.podTemplateFile": "s3://'$S3BUCKET'/app_code/executor-pod-template.yaml",

          "spark.sql.extensions": "io.delta.sql.DeltaSparkSessionExtension",
          "spark.sql.catalog.spark_catalog": "org.apache.spark.sql.delta.catalog.DeltaCatalog",
          "spark.hadoop.hive.metastore.client.factory.class":"com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory",
          "spark.delta.logStore.class": "org.apache.spark.sql.delta.storage.S3SingleDriverLogStore",
          "spark.kubernetes.executor.podNamePrefix": "tpcds-delta-gen"
         
         }}
    ], 
    "monitoringConfiguration": {
      "s3MonitoringConfiguration": {"logUri": "s3://'$S3BUCKET'/elasticmapreduce/emr-containers"}}}'