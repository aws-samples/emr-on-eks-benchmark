# // Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# // SPDX-License-Identifier: MIT-0
ARG SPARK_BASE_IMAGE=public.ecr.aws/w5m3x7g4/spark:3.1.2_hadoop_3.3.1

FROM amazonlinux:2 as tpc-toolkit

ENV TPCDS_KIT_VERSION "master"

RUN yum update -y && \
    yum group install -y "Development Tools" && \
    git clone https://github.com/databricks/tpcds-kit.git -b ${TPCDS_KIT_VERSION} /tmp/tpcds-kit && \
    cd /tmp/tpcds-kit/tools && \
    make OS=LINUX


FROM mozilla/sbt:8u292_1.5.4 as sbt
# Build the Databricks SQL perf library
RUN git clone https://github.com/aws-samples/emr-on-eks-benchmark.git /tmp/emr-on-eks-benchmark && \
    cd /tmp/emr-on-eks-benchmark/spark-sql-perf/ && \
    sbt +package   
     
# Use the compiled Databricks SQL perf library to build benchmark utility
RUN cd /tmp/emr-on-eks-benchmark/ && mkdir /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cp /tmp/emr-on-eks-benchmark/spark-sql-perf/target/scala-2.12/*.jar /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cd /tmp/emr-on-eks-benchmark/benchmark && sbt assembly

FROM ${SPARK_BASE_IMAGE}
USER root

COPY --from=tpc-toolkit /tmp/tpcds-kit/tools /opt/tpcds-kit/tools
COPY --from=sbt /tmp/emr-on-eks-benchmark/benchmark/target/scala-2.12/*jar ${SPARK_HOME}/examples/jars/

# # Use hadoop user and group 
USER hadoop:hadoop

