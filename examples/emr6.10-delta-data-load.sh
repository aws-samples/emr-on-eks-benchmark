#!/bin/bash
# SPDX-FileCopyrightText: Copyright 2021 Amazon.com, Inc. or its affiliates.
# SPDX-License-Identifier: MIT-0   
  # "spark.hive.metastore.uris" : "thrift://hive-metastore.emr.svc.cluster.local:9083"
# export EMRCLUSTER_NAME=emr-on-eks-nvme    
# export AWS_REGION=us-east-1
export ACCOUNTID=$(aws sts get-caller-identity --query Account --output text)                    
# export VIRTUAL_CLUSTER_ID=$(aws emr-containers list-virtual-clusters --query "virtualClusters[?name == '$EMRCLUSTER_NAME' && state == 'RUNNING'].id" --output text)
# export EMR_ROLE_ARN=arn:aws:iam::$ACCOUNTID:role/$EMRCLUSTER_NAME-execution-role
# export S3BUCKET=$EMRCLUSTER_NAME-$ACCOUNTID-$AWS_REGION
export ECR_URL="$ACCOUNTID.dkr.ecr.$AWS_REGION.amazonaws.com"
export benchmarkId=`date +"%Y%m%d-%H%M%S"`

aws emr-containers start-job-run \
--virtual-cluster-id $VIRTUAL_CLUSTER_ID \
--name delta-dataload-30tb-glue$benchmarkId \
--execution-role-arn $EMR_ROLE_ARN \
--release-label emr-6.10.0-latest \
--job-driver '{
  "sparkSubmitJobDriver": {
      "entryPoint": "local:///usr/lib/spark/examples/delta/delta-benchmarks.jar",
      "entryPointArguments":["--format","delta","--scale-in-gb","30000","--db-name","emrdelta","--exclude-nulls","True","--benchmark-path","s3://'$S3BUCKET'/emrdelta","--source-path","s3://'$S3BUCKET'/BLOG_TPCDS-TEST-30T-partitioned"],
      "sparkSubmitParameters": "--jars local:///usr/share/aws/delta/lib/delta-core.jar,local:///usr/share/aws/delta/lib/delta-storage.jar,https://repo1.maven.org/maven2/io/delta/delta-hive_2.12/0.6.0/delta-hive_2.12-0.6.0.jar,https://repo1.maven.org/maven2/io/delta/delta-contribs_2.12/2.2.0/delta-contribs_2.12-2.2.0.jar --class benchmark.TPCDSDataLoad --conf spark.driver.cores=4 --conf spark.driver.memory=5g --conf spark.executor.cores=4 --conf spark.executor.memory=6g  --conf spark.executor.instances=47"}}' \
--configuration-overrides '{
    "applicationConfiguration": [
      {
        "classification": "spark-defaults", 
        "properties": {
          "spark.kubernetes.container.image": "'$ECR_URL'/eks-spark-benchmark:emr6.10-delta",
          "spark.kubernetes.driver.podTemplateFile": "s3://'$S3BUCKET'/app_code/driver-pod-template.yaml",
          "spark.kubernetes.executor.podTemplateFile": "s3://'$S3BUCKET'/app_code/executor-pod-template.yaml",
          "spark.executor.memoryOverhead": "2G",

          "spark.hadoop.fs.s3.maxRetries": "30",

          "spark.log.level": "WARN",
          "spark.benchmarkId": "'$benchmarkId'",
          "spark.sql.extensions": "io.delta.sql.DeltaSparkSessionExtension",
          "spark.sql.catalog.spark_catalog": "org.apache.spark.sql.delta.catalog.DeltaCatalog",
          "spark.delta.logStore.class": "org.apache.spark.sql.delta.storage.S3SingleDriverLogStore",

          "spark.hadoop.hive.metastore.client.factory.class": "com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory",
          "spark.sql.warehouse.dir": "s3://'$S3BUCKET'/emrdelta",
          "spark.sql.catalogImplementation": "hive"

         }}
    ], 
    "monitoringConfiguration": {
      "s3MonitoringConfiguration": {"logUri": "s3://'$S3BUCKET'/elasticmapreduce/emr-containers"}}}'