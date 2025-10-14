MERGE INTO
    store_returns
        USING(
        SELECT
            ss_ticket_number
        FROM
            store_sales,
            date_dim
        WHERE
            ss_sold_date_sk = d_date_sk
            AND d_date BETWEEN '2002-11-12' AND '2002-11-13'
    ) SOURCE ON
    sr_ticket_number = ss_ticket_number
    WHEN MATCHED THEN DELETE;
