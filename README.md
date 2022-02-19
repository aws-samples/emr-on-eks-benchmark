## Spark on Kubernetes benchmark utility

This repository is used to benchmark Spark performance on Kubernetes. 
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

This repo has the [auto-build workflow](./.github/workflows/relase-package.yaml) enabled, which builds the [eks-spark-benchmark image](https://github.com/aws-samples/emr-on-eks-benchmark/pkgs/container/emr-on-eks-benchmark) triggered by code changes in the main branch. 

To build manually, run the command:
```bash
# stay in the project root directory
cd emr-on-eks-benchmark

# Login to ECR
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
# benchmark utility image based on OSS Spark3.1.2
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
kubectl create -n emr configmap special-config --from-literal=codeBucket=$app_code_bucket

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

> Note: We use 3TB dataset in the [examples](./examples), if you'd like to change to 100G or 1T, don't forget to change the parameter `Scale factor (in GB)` in the job submission scripts. Spark executor configuration should also be adjusted correspondingly.

## Cleanup
```bash
export EKSCLUSTER_NAME=eks-nvme
export AWS_REGION=us-east-1
./deprovision.sh
```