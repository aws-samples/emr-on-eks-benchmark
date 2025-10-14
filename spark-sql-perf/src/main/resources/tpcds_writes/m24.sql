MERGE INTO
    inventory
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
                    d_date BETWEEN '1999-09-16' AND '1999-09-23'
            ) r
        JOIN(
                SELECT
                    MAX( d_date_sk ) AS max_date
                FROM
                    date_dim
                WHERE
                    d_date BETWEEN '1999-09-16' AND '1999-09-23'
            ) s
    ) SOURCE ON
    inv_date_sk >= min_date
    AND inv_date_sk <= max_date
    WHEN MATCHED THEN DELETE;
