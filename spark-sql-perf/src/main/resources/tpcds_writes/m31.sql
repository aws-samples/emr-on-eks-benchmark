MERGE INTO
    store_sales
        USING(
        SELECT
            *
        FROM
            (
                SELECT
                    MIN( d_date_sk ) AS min_date
                FROM
                    date_dim
                WHERE
                    d_date BETWEEN '2002-11-12' AND '2002-11-13'
            ) r
        JOIN(
                SELECT
                    MAX( d_date_sk ) AS max_date
                FROM
                    date_dim
                WHERE
                    d_date BETWEEN '2002-11-12' AND '2002-11-13'
            ) s
    ) SOURCE ON
    ss_sold_date_sk >= min_date
    AND ss_sold_date_sk <= max_date
    WHEN MATCHED THEN DELETE;

