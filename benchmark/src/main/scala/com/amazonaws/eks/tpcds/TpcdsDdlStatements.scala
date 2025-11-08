// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amazonaws.eks.tpcds

object TpcdsDdlStatements {
  val web_returns_ddl = """
     CREATE EXTERNAL TABLE web_returns(
       wr_returned_time_sk int,
       wr_item_sk int,
       wr_refunded_customer_sk int,
       wr_refunded_cdemo_sk int,
       wr_refunded_hdemo_sk int,
       wr_refunded_addr_sk int,
       wr_returning_customer_sk int,
       wr_returning_cdemo_sk int,
       wr_returning_hdemo_sk int,
       wr_returning_addr_sk int,
       wr_web_page_sk int,
       wr_reason_sk int,
       wr_order_number bigint,
       wr_return_quantity int,
       wr_return_amt decimal(7,2),
       wr_return_tax decimal(7,2),
       wr_return_amt_inc_tax decimal(7,2),
       wr_fee decimal(7,2),
       wr_return_ship_cost decimal(7,2),
       wr_refunded_cash decimal(7,2),
       wr_reversed_charge decimal(7,2),
       wr_account_credit decimal(7,2),
       wr_net_loss decimal(7,2))
     PARTITIONED BY (
       wr_returned_date_sk int)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/web_returns/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/web_returns/'
   """

  val web_sales_ddl = """
     CREATE EXTERNAL TABLE web_sales(
       ws_sold_time_sk int,
         ws_ship_date_sk int,
         ws_item_sk int,
         ws_bill_customer_sk int,
         ws_bill_cdemo_sk int,
         ws_bill_hdemo_sk int,
         ws_bill_addr_sk int,
         ws_ship_customer_sk int,
         ws_ship_cdemo_sk int,
         ws_ship_hdemo_sk int,
         ws_ship_addr_sk int,
         ws_web_page_sk int,
         ws_web_site_sk int,
         ws_ship_mode_sk int,
         ws_warehouse_sk int,
         ws_promo_sk int,
         ws_order_number bigint,
         ws_quantity int,
         ws_wholesale_cost decimal(7,2),
         ws_list_price decimal(7,2),
         ws_sales_price decimal(7,2),
         ws_ext_discount_amt decimal(7,2),
         ws_ext_sales_price decimal(7,2),
         ws_ext_wholesale_cost decimal(7,2),
         ws_ext_list_price decimal(7,2),
         ws_ext_tax decimal(7,2),
         ws_coupon_amt decimal(7,2),
         ws_ext_ship_cost decimal(7,2),
         ws_net_paid decimal(7,2),
         ws_net_paid_inc_tax decimal(7,2),
         ws_net_paid_inc_ship decimal(7,2),
         ws_net_paid_inc_ship_tax decimal(7,2),
         ws_net_profit decimal(7,2))
       PARTITIONED BY (
         ws_sold_date_sk int)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/web_sales/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/web_sales/'
   """

  val store_returns_ddl = """
     CREATE EXTERNAL TABLE store_returns(
       sr_return_time_sk int,
         sr_item_sk int,
         sr_customer_sk int,
         sr_cdemo_sk int,
         sr_hdemo_sk int,
         sr_addr_sk int,
         sr_store_sk int,
         sr_reason_sk int,
         sr_ticket_number bigint,
         sr_return_quantity int,
         sr_return_amt decimal(7,2),
         sr_return_tax decimal(7,2),
         sr_return_amt_inc_tax decimal(7,2),
         sr_fee decimal(7,2),
         sr_return_ship_cost decimal(7,2),
         sr_refunded_cash decimal(7,2),
         sr_reversed_charge decimal(7,2),
         sr_store_credit decimal(7,2),
         sr_net_loss decimal(7,2))
       PARTITIONED BY (
         sr_returned_date_sk int)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/store_returns/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/store_returns/'
   """

  val store_sales_ddl = """
     CREATE EXTERNAL TABLE store_sales(
       ss_sold_time_sk int,
         ss_item_sk int,
         ss_customer_sk int,
         ss_cdemo_sk int,
         ss_hdemo_sk int,
         ss_addr_sk int,
         ss_store_sk int,
         ss_promo_sk int,
         ss_ticket_number bigint,
         ss_quantity int,
         ss_wholesale_cost decimal(7,2),
         ss_list_price decimal(7,2),
         ss_sales_price decimal(7,2),
         ss_ext_discount_amt decimal(7,2),
         ss_ext_sales_price decimal(7,2),
         ss_ext_wholesale_cost decimal(7,2),
         ss_ext_list_price decimal(7,2),
         ss_ext_tax decimal(7,2),
         ss_coupon_amt decimal(7,2),
         ss_net_paid decimal(7,2),
         ss_net_paid_inc_tax decimal(7,2),
         ss_net_profit decimal(7,2))
       PARTITIONED BY (
         ss_sold_date_sk int)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/store_sales/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/store_sales/'
   """

  val catalog_returns_ddl = """
     CREATE EXTERNAL TABLE catalog_returns(
       cr_returned_time_sk int,
         cr_item_sk int,
         cr_refunded_customer_sk int,
         cr_refunded_cdemo_sk int,
         cr_refunded_hdemo_sk int,
         cr_refunded_addr_sk int,
         cr_returning_customer_sk int,
         cr_returning_cdemo_sk int,
         cr_returning_hdemo_sk int,
         cr_returning_addr_sk int,
         cr_call_center_sk int,
         cr_catalog_page_sk int,
         cr_ship_mode_sk int,
         cr_warehouse_sk int,
         cr_reason_sk int,
         cr_order_number bigint,
         cr_return_quantity int,
         cr_return_amount decimal(7,2),
         cr_return_tax decimal(7,2),
         cr_return_amt_inc_tax decimal(7,2),
         cr_fee decimal(7,2),
         cr_return_ship_cost decimal(7,2),
         cr_refunded_cash decimal(7,2),
         cr_reversed_charge decimal(7,2),
         cr_store_credit decimal(7,2),
         cr_net_loss decimal(7,2))
       PARTITIONED BY (
         cr_returned_date_sk int)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/catalog_returns/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/catalog_returns/'
   """

  val catalog_sales_ddl = """
     CREATE EXTERNAL TABLE catalog_sales(
       cs_sold_time_sk int,
         cs_ship_date_sk int,
         cs_bill_customer_sk int,
         cs_bill_cdemo_sk int,
         cs_bill_hdemo_sk int,
         cs_bill_addr_sk int,
         cs_ship_customer_sk int,
         cs_ship_cdemo_sk int,
         cs_ship_hdemo_sk int,
         cs_ship_addr_sk int,
         cs_call_center_sk int,
         cs_catalog_page_sk int,
         cs_ship_mode_sk int,
         cs_warehouse_sk int,
         cs_item_sk int,
         cs_promo_sk int,
         cs_order_number bigint,
         cs_quantity int,
         cs_wholesale_cost decimal(7,2),
         cs_list_price decimal(7,2),
         cs_sales_price decimal(7,2),
         cs_ext_discount_amt decimal(7,2),
         cs_ext_sales_price decimal(7,2),
         cs_ext_wholesale_cost decimal(7,2),
         cs_ext_list_price decimal(7,2),
         cs_ext_tax decimal(7,2),
         cs_coupon_amt decimal(7,2),
         cs_ext_ship_cost decimal(7,2),
         cs_net_paid decimal(7,2),
         cs_net_paid_inc_tax decimal(7,2),
         cs_net_paid_inc_ship decimal(7,2),
         cs_net_paid_inc_ship_tax decimal(7,2),
         cs_net_profit decimal(7,2))
       PARTITIONED BY (
         cs_sold_date_sk int)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/catalog_sales/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/catalog_sales/'
   """

  val inventory_ddl = """
     CREATE EXTERNAL TABLE inventory(
       inv_item_sk int,
         inv_warehouse_sk int,
         inv_quantity_on_hand int)
       PARTITIONED BY (
         inv_date_sk int)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/inventory/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/inventory/'
   """

  val date_dim_ddl = """
     CREATE EXTERNAL TABLE date_dim(
       d_date_sk int,
         d_date_id string,
         d_date date,
         d_month_seq int,
         d_week_seq int,
         d_quarter_seq int,
         d_year int,
         d_dow int,
         d_moy int,
         d_dom int,
         d_qoy int,
         d_fy_year int,
         d_fy_quarter_seq int,
         d_fy_week_seq int,
         d_day_name string,
         d_quarter_name string,
         d_holiday string,
         d_weekend string,
         d_following_holiday string,
         d_first_dom int,
         d_last_dom int,
         d_same_day_ly int,
         d_same_day_lq int,
         d_current_day string,
         d_current_week string,
         d_current_month string,
         d_current_quarter string,
         d_current_year string)
     ROW FORMAT SERDE
       'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
     WITH SERDEPROPERTIES (
       'path'='s3PathPrefix/s3PathTargetDatabase/date_dim/')
     STORED AS INPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
     OUTPUTFORMAT
       'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
     LOCATION
       's3PathPrefix/s3PathTargetDatabase/date_dim/'
   """

  // This is the target table for delta lake merge DML benchmarks
  val createDeltaMergeSourceSql = """
    CREATE EXTERNAL TABLE tableName(
      wr_returned_time_sk int,
      wr_item_sk double,
      wr_refunded_customer_sk int,
      wr_refunded_cdemo_sk int,
      wr_refunded_hdemo_sk int,
      wr_refunded_addr_sk int,
      wr_returning_customer_sk int,
      wr_returning_cdemo_sk int,
      wr_returning_hdemo_sk int,
      wr_returning_addr_sk int,
      wr_web_page_sk int,
      wr_reason_sk int,
      wr_order_number double,
      wr_return_quantity int,
      wr_return_amt decimal(7,2),
      wr_return_tax decimal(7,2),
      wr_return_amt_inc_tax decimal(7,2),
      wr_fee decimal(7,2),
      wr_return_ship_cost decimal(7,2),
      wr_refunded_cash decimal(7,2),
      wr_reversed_charge decimal(7,2),
      wr_account_credit decimal(7,2),
      wr_net_loss decimal(7,2),
      wr_returned_date_sk int)
    ROW FORMAT SERDE
      'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
    WITH SERDEPROPERTIES (
      'path'='s3PathPrefix/s3PathTargetDatabase/tableName/')
    STORED AS INPUTFORMAT
      'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
    OUTPUTFORMAT
      'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
    LOCATION
      's3PathPrefix/s3PathTargetDatabase/tableName/'
  """

  // This is the target table for delta lake merge DML benchmarks
  val createDeltaMergeTargetTableSql = """
    CREATE EXTERNAL TABLE tableName(
      wr_returned_time_sk int,
      wr_item_sk int,
      wr_refunded_customer_sk int,
      wr_refunded_cdemo_sk int,
      wr_refunded_hdemo_sk int,
      wr_refunded_addr_sk int,
      wr_returning_customer_sk int,
      wr_returning_cdemo_sk int,
      wr_returning_hdemo_sk int,
      wr_returning_addr_sk int,
      wr_web_page_sk int,
      wr_reason_sk int,
      wr_order_number bigint,
      wr_return_quantity int,
      wr_return_amt decimal(7,2),
      wr_return_tax decimal(7,2),
      wr_return_amt_inc_tax decimal(7,2),
      wr_fee decimal(7,2),
      wr_return_ship_cost decimal(7,2),
      wr_refunded_cash decimal(7,2),
      wr_reversed_charge decimal(7,2),
      wr_account_credit decimal(7,2),
      wr_net_loss decimal(7,2))
    PARTITIONED BY (
      wr_returned_date_sk int)
    ROW FORMAT SERDE
      'org.apache.hadoop.hive.ql.io.parquet.serde.ParquetHiveSerDe'
    WITH SERDEPROPERTIES (
      'path'='s3PathPrefix/s3PathTargetDatabase/tableName/')
    STORED AS INPUTFORMAT
      'org.apache.hadoop.hive.ql.io.parquet.MapredParquetInputFormat'
    OUTPUTFORMAT
      'org.apache.hadoop.hive.ql.io.parquet.MapredParquetOutputFormat'
    LOCATION
      's3PathPrefix/s3PathTargetDatabase/tableName/'
  """
}
