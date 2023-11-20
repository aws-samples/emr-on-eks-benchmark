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


FROM mozilla/sbt:8u292_1.5.4 as sbtenv
# Build the Databricks SQL perf library from the local Spark version
RUN git clone -b delta https://github.com/aws-samples/emr-on-eks-benchmark.git /tmp/emr-on-eks-benchmark && \
    rm -rf /tmp/emr-on-eks-benchmark/spark-sql-perf && \
    rm -rf /tmp/emr-on-eks-benchmark/benchmark && \
    rm -rf /tmp/emr-on-eks-benchmark/delta-perf
ADD ../../spark-sql-perf /tmp/emr-on-eks-benchmark/spark-sql-perf
ADD ../../benchmark /tmp/emr-on-eks-benchmark/benchmark
ADD ../../delta-perf /tmp/emr-on-eks-benchmark/delta-perf
RUN cd /tmp/emr-on-eks-benchmark/spark-sql-perf/ && sbt +package

# Use the compiled Databricks SQL perf library to build benchmark utility
RUN cd /tmp/emr-on-eks-benchmark/ && mkdir -p /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cp /tmp/emr-on-eks-benchmark/spark-sql-perf/target/scala-2.12/*.jar /tmp/emr-on-eks-benchmark/benchmark/libs \
&& cd /tmp/emr-on-eks-benchmark/benchmark && sbt assembly

# Build the Delta Benchmark library
RUN cd /tmp/emr-on-eks-benchmark/delta-perf && sbt assembly


FROM ${SPARK_BASE_IMAGE}
ARG MAVEN_VERSION=3.8.8
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries
USER root

# # install maven
# RUN apt-get update && apt-get install -y curl git tar procps
# RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
#  && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
#  && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
#  && rm -f /tmp/apache-maven.tar.gz \
#  && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

# # compile Glue Catalog
# RUN git clone https://github.com/apache/hive.git
# RUN cd hive \
# && git checkout branch-2.3 \
# RUN curl -OL https://issues.apache.org/jira/secure/attachment/12958418/HIVE-12679.branch-2.3.patch \
# && patch -p0 <HIVE-12679.branch-2.3.patch
# RUN mvn clean install -DskipTests

# RUN cd .. \
# && git clone https://github.com/awslabs/aws-glue-data-catalog-client-for-apache-hive-metastore.git \
# && cd aws-glue-data-catalog-client-for-apache-hive-metastore \
# && mvn clean install -DskipTests
# RUN cd aws-glue-datacatalog-spark-client \
# && mvn clean package -DskipTests

COPY --from=tpc-toolkit /tmp/tpcds-kit/tools /opt/tpcds-kit/tools
COPY --from=sbtenv /tmp/emr-on-eks-benchmark/benchmark/target/scala-2.12/*jar ${SPARK_HOME}/examples/jars/
COPY --from=sbtenv /tmp/emr-on-eks-benchmark/delta-perf/target/scala-2.12/*jar ${SPARK_HOME}/examples/delta/delta-benchmarks.jar
# COPY docker/benchmark-util/aws-glue-datacatalog-spark-client-3.8.1.jar /opt/spark/jars/aws-glue-datacatalog-spark-client.jar
# COPY docker/benchmark-util/aws-glue-datacatalog-hive3-client-3.8.1.jar /opt/spark/jars/aws-glue-datacatalog-hive-client.jar

# # Use hadoop user and group 
USER hadoop:hadoop