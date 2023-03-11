FROM 021732063925.dkr.ecr.us-west-2.amazonaws.com/eks-spark-benchmark:emr6.10_jdk8
USER root
ENV JAVA_HOME /etc/alternatives/jre

RUN rpm -qa | grep corretto | xargs  yum -y remove \
# to keep hadoop-lzo dependency
&& rpm -e --nodeps java-1.8.0-openjdk-headless \ 
&& amazon-linux-extras install java-openjdk11 \
&&  yum clean all
RUN alternatives --set java  /usr/lib/jvm/$(ls /usr/lib/jvm | grep java-11 | cut -f 3)/bin/java
# # Use hadoop user and group 
USER hadoop:hadoop