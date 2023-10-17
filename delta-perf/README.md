# Delta Benchmarks 

## Overview
This is a basic framework for writing benchmarks to measure Delta's performance. It is currently designed to run benchmark on Spark running in EMR with a remote hive metatore in RDS. However, it can be easily extended for other Spark-based benchmarks. To get started, first download/clone this repository in your local machine. Then you have to set up a hive metastore, generate Delta format source data based on TPCDS dataset, and run the benchmark scripts in this directory. See the next section for more details.

## Running TPC-DS benchmark

This TPC-DS benchmark is constructed such that you have to run the following two steps. 
1. *Load data*: You have to create the TPC-DS database with all the Delta tables. To do that, the raw TPC-DS data has been provided as Apache Parquet files. In this step you will use your EMR on EKS virtual cluster to read the parquet files and rewrite them as Delta tables.The catalog will be stored in RDS-based hive metastore, or use Glue catalog.
2. *Query data*: Then, using the tables definitions in the Hive Metatore, you can run the 99 benchmark queries.   

The next section will provide the detailed steps of how to setup the necessary Hive Metastore and a cluster, how to test the setup with small-scale data, and then finally run the full scale benchmark. (Will provide the infra script or CFN later on)

_________________

### Setup Infrastructure in AWS

#### Prerequisites
  - A custom Docker image for the benchmark. Build seperate images for OSS Spark and EMR respectively. See the steps at the [root directory of this project](https://github.com/aws-samples/emr-on-eks-benchmark#build-benchmark-utility-docker-image)
  - A RDS instance for hosting an external Hive Metastore, can ignore this step if using Glue Catalog
  - An EMR on EKS virtual cluster for running the benchmark
  - A S3 bucket to store the TPC-DS data and benchmark reports
  - No autoscaling (DRA) in benchmark
  - Use instance store as the scratch space storage

#### Create rquired Hive Metastore
The Delta benchmark will create external tables against a Hive metastore. 2 Options to setup the Hive metastore:

**Option 1: RDS-based remote Hive Metastore**
* Provision an mySQL RDS instance
* Install the [HMS helm chart](https://github.com/aws-samples/hive-emr-on-eks/tree/main/hive-metastore-chart) on an existing EKS cluster connecting to the external Hive Metastore (RDS). 

To connect Hive metastore in Delta app, the following Spark configs are neccessary:
```
"spark.hive.metastore.uris" : "thrift://hive-metastore.emr.svc.cluster.local:9083",
"spark.sql.warehouse.dir": "s3://'$S3BUCKET'/delta"
```

**Option 2: use Glue Catalog as Hive Metastore**
If you chose this option, ignore the RDS and HMS setup. 

To connect Glue Catalog in Delta app, the following Spark configs are neccessary:
```
# replace the HMS thrift setting by this
"spark.hadoop.hive.metastore.client.factory.class":"com.amazonaws.glue.catalog.metastore.AWSGlueDataCatalogHiveClientFactory"
```

#### Prepare S3 bucket
If creating a new S3 bucket (or use an existing one), it needs to be in the same region as your benchmark environment. The EMR on EKS's execution role should allow access to read/write the S3 bucket via EKS's IRSA feature.

_________________

#### Input data
The benchmark is using the raw TPC-DS data which has been provided as Apache Parquet files. There are two predefined datasets of different size, 1GB and 3TB, located in `s3://devrel-delta-datasets/tpcds-2.13/tpcds_sf1_parquet/`
and `s3://devrel-delta-datasets/tpcds-2.13/tpcds_sf3000_parquet/`, respectively. Please keep in mind that
`devrel-delta-datasets` bucket is configured as [Requester Pays](https://docs.aws.amazon.com/AmazonS3/latest/userguide/ObjectsinRequesterPaysBuckets.html) bucket,
so [access requests have to be configured properly](https://docs.aws.amazon.com/AmazonS3/latest/userguide/ObjectsinRequesterPaysBuckets.html).

Alternatively, generate your own TPCDS data by following the steps at the root directory of [this benchmark repo](https://github.com/aws-samples/emr-on-eks-benchmark/tree/main#run-benchmark).

_________________

### The benchmark output
After finish running the benchmark, you should be able to see some reports generated in your benchmark S3 path, something like the following:

```text
RESULT:
{
  "benchmarkSpecs" : {
    "benchmarkPath" : ...,
    "benchmarkId" : "20220126-191336-test"
  },
  "queryResults" : [ {
    "name" : "sql-test",
    "durationMs" : 11075
  }, {
    "name" : "db-list-test",
    "durationMs" : 208
  }, {
    "name" : "db-create-test",
    "durationMs" : 4070
  }, {
    "name" : "db-use-test",
    "durationMs" : 41
  }, {
    "name" : "table-drop-test",
    "durationMs" : 74
  }, {
    "name" : "table-create-test",
    "durationMs" : 33812
  }, {
    "name" : "table-query-test",
    "durationMs" : 4795
  } ]
}
```
    
The above metrics are also written to a json file and uploaded to your S3 path. Please verify that both the table and report are generated in that path. 

## Run TPC-DS Benchmark
Now that you are familiar with how the framework runs the workload, you can start with running the small scale 1GB TPC-DS benchmark.

1. Read existing TPCDS data in parquet format, load data as Delta tables:
    ```bash
      ./examples/emr6.10-delta-data-gen.sh 
    ```
The job contains the following parameters, change them if needed:
```yaml
 "entryPointArguments":[
    "--format","delta",
    "--scale-in-gb","1",   # change to 3000 if test 3TB dataset
    "--exclude-nulls","True",
    "--benchmark-path","s3://'$S3BUCKET'/app_code/data/delta/tpcds_1gb_delta", # target bucket for delta data and generate the benchmark report
    "--source-path","s3://'$S3BUCKET'/BLOG_TPCDS-TEST-1G-partitioned" # source bucket where stores raw TPCDS data as parquet format
  ]
```

2. Run queries on Delta tables:
    ```bash
    ./examples/emr6.10-delta-benchmark.sh
    ```

Compare the results using the generated JSON files.

_________________

## Internals of the framework

Structure of this framework's code
- `build.sbt`, `project/`, `src/` form the SBT project which contains the Scala code that define the benchmark workload.
    - `Benchmark.scala` is the basic interface, and `TestBenchmark.scala` is a sample implementation.
- `run-benchmark.py` contains the specification of the benchmarks defined by name (e.g. `tpcds-3tb-delta`). Each benchmark specification is defined by the following: 
    - Fully qualified name of the main Scala class to be started.
    - Command line argument for the main function.
    - Additional Maven artifact to load (example `io.delta:delta-core_2.12:1.0.0`).
    - Spark configurations to use.
- `scripts` has the core python scripts that are called by `run-benchmark.py`

The script `run-benchmark.py` does the following:
- Compile the Scala code into a uber jar.
- Upload it to the given hostname.
- Using ssh to the hostname, it will launch a screen and start the main class with spark-submit.
