# // Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# // SPDX-License-Identifier: MIT-0
ARG SPARK_BASE_IMAGE=public.ecr.aws/w5m3x7g4/spark:3.1.2_hadoop_3.3.1

FROM amazonlinux:2 AS tpc-toolkit

ENV TPCDS_KIT_VERSION="master"

RUN yum update -y && \
    yum group install -y "Development Tools" && \
    git clone https://github.com/databricks/tpcds-kit.git -b ${TPCDS_KIT_VERSION} /tmp/tpcds-kit && \
    cd /tmp/tpcds-kit/tools && \
    make OS=LINUX



FROM mozilla/sbt:8u292_1.5.4 AS sbtenv

# Build the Databricks SQL perf library from the local Spark version
RUN git clone -b delta https://github.com/aws-samples/emr-on-eks-benchmark.git /tmp/emr-on-eks-benchmark && \
    rm -rf /tmp/emr-on-eks-benchmark/spark-sql-perf && \
    rm -rf /tmp/emr-on-eks-benchmark/benchmark && \
    rm -rf /tmp/emr-on-eks-benchmark/delta-perf
ADD spark-sql-perf /tmp/emr-on-eks-benchmark/spark-sql-perf
ADD benchmark /tmp/emr-on-eks-benchmark/benchmark
ADD delta-perf /tmp/emr-on-eks-benchmark/delta-perf
RUN cd /tmp/emr-on-eks-benchmark/spark-sql-perf/ && sbt +package

# Use the compiled Databricks SQL perf library to build benchmark utility
RUN cd /tmp/emr-on-eks-benchmark/ && mkdir -p /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cp /tmp/emr-on-eks-benchmark/spark-sql-perf/target/scala-2.12/*.jar /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cd /tmp/emr-on-eks-benchmark/benchmark && sbt assembly

# Build the Delta Benchmark library
RUN cd /tmp/emr-on-eks-benchmark/delta-perf && sbt assembly


FROM ${SPARK_BASE_IMAGE}
USER root

COPY --from=tpc-toolkit /tmp/tpcds-kit/tools /opt/tpcds-kit/tools
COPY --from=sbtenv /tmp/emr-on-eks-benchmark/benchmark/target/scala-2.12/*jar ${SPARK_HOME}/examples/jars/
COPY --from=sbtenv /tmp/emr-on-eks-benchmark/delta-perf/target/scala-2.12/*jar ${SPARK_HOME}/examples/delta/delta-benchmarks.jar
# COPY --from=sbtenv /tmp/emr-on-eks-benchmark/delta-perf/scripts/ ${SPARK_HOME}/examples/delta/scripts/
# COPY --from=sbtenv /tmp/emr-on-eks-benchmark/delta-perf/run-benchmark.py ${SPARK_HOME}/examples/delta/
# RUN chown -R hadoop:hadoop ${SPARK_HOME}/examples/delta
# # Use hadoop user and group 
USER hadoop:hadoop
