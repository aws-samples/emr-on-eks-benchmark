MERGE INTO
    catalog_sales
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
                    d_date BETWEEN '1999-09-18' AND '1999-09-19'
            ) r
        JOIN(
                SELECT
                    MAX( d_date_sk ) AS max_date
                FROM
                    date_dim
                WHERE
                    d_date BETWEEN '1999-09-18' AND '1999-09-19'
            ) s
    ) SOURCE ON
    cs_sold_date_sk >= min_date
    AND cs_sold_date_sk <= max_date
    WHEN MATCHED THEN DELETE;