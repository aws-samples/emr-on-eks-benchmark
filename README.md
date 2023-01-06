## Spark on Kubernetes benchmark utility

This repository provides a general tool to benchmark Spark performance.
If you want to use the [prebuild docker image](https://github.com/aws-samples/emr-on-eks-benchmark/pkgs/container/emr-on-eks-benchmark) based on a prebuild OSS spark_3.1.2_hadoop_3.3.1, you can skip the [build section](#Build-benchmark-utility-docker-image) and jump to [Run Benchmark](#Run-Benchmark) directly. If you want to build your own, follow the steps in the [build section](#Build-benchmark-utility-docker-image).

## Prerequisite

- eksctl is installed
```bash
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv -v /tmp/eksctl /usr/local/bin
eksctl version
```
- Update AWS CLI to the latest (requires aws cli version >= 2.1.14) on macOS. Check out the [link](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html) for Linux or Windows
```bash
curl "https://awscli.amazonaws.com/AWSCLIV2.pkg" -o "AWSCLIV2.pkg"
sudo installer -pkg ./AWSCLIV2.pkg -target /
aws --version
rm AWSCLIV2.pkg
```
- Install kubectl on macOS, check out the [link](https://kubernetes.io/docs/tasks/tools/) for Linux or Windows.
```bash
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/darwin/amd64/kubectl"
chmod +x ./kubectl
sudo mv ./kubectl /usr/local/bin/kubectl && export PATH=/usr/local/bin:$PATH
sudo chown root: /usr/local/bin/kubectl
kubectl version --short --client
```
- Helm CLI
```bash
curl -sSL https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
helm version --short
```
- [Install Docker on Mac](https://docs.docker.com/desktop/mac/install/), check out [other options](https://docs.docker.com/desktop/#download-and-install) for different OS.
```bash
brew cask install docker
docker --version
```

## Set up test environment

The script creates a new EKS cluster, enables EMR on EKS and builds a private ECR for the eks-spark-benchmark utility docker image. Change the region if needed.
```bash
export EKSCLUSTER_NAME=eks-nvme
export AWS_REGION=us-east-1
./provision.sh
```
## Build benchmark utility docker image

This repo has the [auto-build workflow](./.github/workflows/relase-package.yaml) enabled, which builds the [eks-spark-benchmark image](https://github.com/aws-samples/emr-on-eks-benchmark/pkgs/container/eks-spark-benchmark) triggered by code changes in the main branch. It is a docker image based on Apache Spark base image.

To build manually, run the command:
```bash
# stay in the project root directory
cd emr-on-eks-benchmark

# Login to ECR
ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
ECR_URL=$ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $ECR_URL
aws ecr create-repository --repository-name spark --image-scanning-configuration scanOnPush=true

# Build Spark base image
docker build -t $ECR_URL/spark:3.1.2_hadoop_3.3.1 -f docker/hadoop-aws-3.3.1/Dockerfile --build-arg HADOOP_VERSION=3.3.1 --build-arg SPARK_VERSION=3.1.2 .
docker push $ECR_URL/spark:3.1.2_hadoop_3.3.1

# Build benchmark utility based on the Spark
docker build -t $ECR_URL/eks-spark-benchmark:3.1.2 -f docker/benchmark-util/Dockerfile --build-arg SPARK_BASE_IMAGE=$ECR_URL/spark:3.1.2_hadoop_3.3.1 .
```

If you need to build the image based on a different Spark image, for example [EMR Spark runtime](https://docs.aws.amazon.com/emr/latest/EMR-on-EKS-DevelopmentGuide/docker-custom-images-tag.html), run the command:
```bash
# get EMR on EKS base image
export SRC_ECR_URL=755674844232.dkr.ecr.us-east-1.amazonaws.com
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin $SRC_ECR_URL
docker pull $SRC_ECR_URL/spark/emr-6.5.0:latest

# Custom an image on top of the EMR Spark
docker build -t $ECR_URL/eks-spark-benchmark:emr6.5 -f docker/benchmark-util/Dockerfile --build-arg SPARK_BASE_IMAGE=$SRC_ECR_URL/spark/emr-6.5.0:latest .
```

Finally, push it to your ECR. Replace the default docker images in [examples](./examples) if needed:
```bash
aws ecr create-repository --repository-name eks-spark-benchmark --image-scanning-configuration scanOnPush=true
# benchmark utility image based on Apache Spark3.1.2
docker push $ECR_URL/eks-spark-benchmark:3.1.2
# benchmark utility image based on EMR Spark runtime
docker push $ECR_URL/eks-spark-benchmark:emr6.5
```

## Run Benchmark
### Generate the TCP-DS data
Check the docker image name in example files, change it accordingly.

```bash
kubectl apply -f examples/tpcds-data-generation.yaml
```

### Run TPC-DS benchmark for OSS Spark on EKS

The benchmark file contains a configmap that dynamically map your S3 bucket to the environment variable **codeBucket** in EKS. Run the command to set up the configmap:
```bash
app_code_bucket=<S3_BUCKET_HAS_TPCDS_DATASET>
kubectl create -n oss configmap special-config --from-literal=codeBucket=$app_code_bucket

# start the benchmark test job
kubectl apply -f examples/tpcds-benchmark.yaml
```

### Run EMR on EKS benchmark
```shell
# set EMR virtual cluster name, change the region if needed
export EMRCLUSTER_NAME=emr-on-eks-nvme
export AWS_REGION=us-east-1
bash examples/emr6.5-benchmark.sh
```
### Benchmark for EMR on EC2
Few notes for the set up:
1. Use the same instance type c5d.9xlarge as in the EKS cluster.
2. If choosing an EBS-backed instance, check the [default instance storage setting](https://docs.aws.amazon.com/emr/latest/ManagementGuide/emr-plan-storage.html) by EMR on EC2, and attach the same number of EBS volumes to your EKS cluster before running EKS related benchmarks.

The benchmark utility app was compiled to a jar file during an [automated GitHub workflow](https://github.com/aws-samples/emr-on-eks-benchmark/actions/workflows/relase-package.yaml) process. If you already have a running Kubernetes container, the quickest way to get the jar is using `kubectl cp` command as shown below:
```bash
# Download the jar and ignore the warning message
kubectl cp oss/oss-spark-tpcds-exec-1:/opt/spark/examples/jars/eks-spark-benchmark-assembly-1.0.jar eks-spark-benchmark-assembly-1.0.jar
```

However if you are running a benchmark just for EMR on EC2, you probably don\'t have a running container. To copy the jar file from a docker container, you need two terminals. In the first terminal, spin up a docker container based on your image built:
```bash
docker run --name spark-benchmark -it $ECR_URL/eks-spark-benchmark:3.1.2 bash
# you are logged in to the container now, find the jar file
hadoop@9ca5b2afe778: ls -alh /opt/spark/examples/jars/eks-spark-benchmark-assembly-1.0.jar
```
Keep the container running then go to the second terminal, run the command to copy the jar file from the container to your local directory:
```bash
docker cp spark-benchmark:/opt/spark/examples/jars/eks-spark-benchmark-assembly-1.0.jar .

# Upload to s3
S3BUCKET=<S3_BUCKET_HAS_TPCDS_DATASET>
aws s3 cp eks-spark-benchmark-assembly-1.0.jar s3://$S3BUCKET
```

Submit the benchmark job via EMR Step on the AWS console. Make sure the EMR on EC2 cluster can access the `$S3BUCKET`:
```bash
# Step type: Spark Application
# JAR location: s3://$S3BUCKET/eks-spark-benchmark-assembly-1.0.jar
# Spark-submit options:
--class com.amazonaws.eks.tpcds.BenchmarkSQL --conf spark.driver.cores=4 --conf spark.driver.memory=5g --conf spark.executor.cores=4 --conf spark.executor.memory=6g --conf spark.executor.instances=47 --conf spark.network.timeout=2000 --conf spark.executor.heartbeatInterval="300s" --conf spark.executor.memoryOverhead=2G --conf spark.driver.memoryOverhead=1000 --conf spark.dynamicAllocation.enabled=false --conf spark.shuffle.service.enabled=false
# Arguments:
s3://blogpost-sparkoneks-us-east-1/blog/BLOG_TPCDS-TEST-3T-partitioned s3://$S3BUCKET/EMRONEC2_TPCDS-TEST-3T-RESULT /opt/tpcds-kit/tools parquet 3000 1 false q1-v2.4,q10-v2.4,q11-v2.4,q12-v2.4,q13-v2.4,q14a-v2.4,q14b-v2.4,q15-v2.4,q16-v2.4,q17-v2.4,q18-v2.4,q19-v2.4,q2-v2.4,q20-v2.4,q21-v2.4,q22-v2.4,q23a-v2.4,q23b-v2.4,q24a-v2.4,q24b-v2.4,q25-v2.4,q26-v2.4,q27-v2.4,q28-v2.4,q29-v2.4,q3-v2.4,q30-v2.4,q31-v2.4,q32-v2.4,q33-v2.4,q34-v2.4,q35-v2.4,q36-v2.4,q37-v2.4,q38-v2.4,q39a-v2.4,q39b-v2.4,q4-v2.4,q40-v2.4,q41-v2.4,q42-v2.4,q43-v2.4,q44-v2.4,q45-v2.4,q46-v2.4,q47-v2.4,q48-v2.4,q49-v2.4,q5-v2.4,q50-v2.4,q51-v2.4,q52-v2.4,q53-v2.4,q54-v2.4,q55-v2.4,q56-v2.4,q57-v2.4,q58-v2.4,q59-v2.4,q6-v2.4,q60-v2.4,q61-v2.4,q62-v2.4,q63-v2.4,q64-v2.4,q65-v2.4,q66-v2.4,q67-v2.4,q68-v2.4,q69-v2.4,q7-v2.4,q70-v2.4,q71-v2.4,q72-v2.4,q73-v2.4,q74-v2.4,q75-v2.4,q76-v2.4,q77-v2.4,q78-v2.4,q79-v2.4,q8-v2.4,q80-v2.4,q81-v2.4,q82-v2.4,q83-v2.4,q84-v2.4,q85-v2.4,q86-v2.4,q87-v2.4,q88-v2.4,q89-v2.4,q9-v2.4,q90-v2.4,q91-v2.4,q92-v2.4,q93-v2.4,q94-v2.4,q95-v2.4,q96-v2.4,q97-v2.4,q98-v2.4,q99-v2.4,ss_max-v2.4 true

```

> Note: We use 3TB dataset in the [examples](./examples), if you'd like to change to 100G or 1T, don't forget to change the parameter `Scale factor (in GB)` in the job submission scripts. Spark executor configuration should also be adjusted correspondingly.

## Cleanup
```bash
export EKSCLUSTER_NAME=eks-nvme
export AWS_REGION=us-east-1
./deprovision.sh
```
