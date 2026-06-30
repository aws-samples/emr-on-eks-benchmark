# // Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
# // SPDX-License-Identifier: MIT-0
ARG SPARK_BASE_IMAGE=public.ecr.aws/data-on-eks/spark:4.0.1-scala2.13-java21-python3-r-ubuntu
# ARG SPARK_BASE_IMAGE=public.ecr.aws/emr-on-eks/spark/emr-spark-8.0.0:latest

# Build the TPC-DS toolkit native binaries for the TARGET platform.
# This stage compiles native C code, so it must run per target architecture.
FROM amazonlinux:2023 AS tpc-toolkit

ENV TPCDS_KIT_VERSION="master"

RUN dnf update -y && \
    dnf group install -y "Development Tools" && \
    dnf install -y git && \
    git clone https://github.com/databricks/tpcds-kit.git -b ${TPCDS_KIT_VERSION} /tmp/tpcds-kit && \
    cd /tmp/tpcds-kit/tools && \
    make OS=LINUX



# Build the Scala/JVM artifacts. The output JARs are architecture-independent,
# so pin this stage to the native build platform to avoid recompiling under
# emulation for every target platform.
FROM --platform=$BUILDPLATFORM sbtscala/scala-sbt:eclipse-temurin-17.0.19_10_1.12.13_2.13.18 AS sbtenv

# Build the Databricks SQL perf library from the local Spark version
RUN git clone -b spark-4.0 https://github.com/aws-samples/emr-on-eks-benchmark.git /tmp/emr-on-eks-benchmark
#     rm -rf /tmp/emr-on-eks-benchmark/spark-sql-perf && \
#     rm -rf /tmp/emr-on-eks-benchmark/benchmark && \
#     rm -rf /tmp/emr-on-eks-benchmark/delta-perf
# ADD spark-sql-perf /tmp/emr-on-eks-benchmark/spark-sql-perf
# ADD benchmark /tmp/emr-on-eks-benchmark/benchmark
# ADD delta-perf /tmp/emr-on-eks-benchmark/delta-perf
RUN cd /tmp/emr-on-eks-benchmark/spark-sql-perf/ && sbt +package

# Use the compiled Databricks SQL perf library to build benchmark utility
RUN cd /tmp/emr-on-eks-benchmark/ && mkdir -p /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cp /tmp/emr-on-eks-benchmark/spark-sql-perf/target/scala-2.13/*.jar /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cd /tmp/emr-on-eks-benchmark/benchmark && sbt assembly

# Build the Delta Benchmark library
RUN cd /tmp/emr-on-eks-benchmark/delta-perf && sbt assembly


FROM ${SPARK_BASE_IMAGE}
USER root

COPY --from=tpc-toolkit /tmp/tpcds-kit/tools /opt/tpcds-kit/tools
COPY --from=sbtenv /tmp/emr-on-eks-benchmark/benchmark/target/scala-2.13/*jar ${SPARK_HOME}/examples/jars/
COPY --from=sbtenv /tmp/emr-on-eks-benchmark/delta-perf/target/scala-2.13/*jar ${SPARK_HOME}/examples/delta/delta-benchmarks.jar

# # Use hadoop user and group
USER hadoop:hadoop
