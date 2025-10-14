MERGE INTO
    web_returns
        USING(
        SELECT
            ws_order_number
        FROM
            web_sales,
            date_dim
        WHERE
            ws_sold_date_sk = d_date_sk
            AND d_date BETWEEN '1999-09-18' AND '1999-09-19'
    ) SOURCE ON
    wr_order_number = ws_order_number
    WHEN MATCHED THEN DELETE;
