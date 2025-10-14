MERGE INTO
    catalog_returns
        USING(
        SELECT
            cs_order_number
        FROM
            catalog_sales,
            date_dim
        WHERE
            cs_sold_date_sk = d_date_sk
            AND d_date BETWEEN '2000-05-20' AND '2000-05-21'
    ) SOURCE ON
    cr_order_number = cs_order_number
    WHEN MATCHED THEN DELETE;
