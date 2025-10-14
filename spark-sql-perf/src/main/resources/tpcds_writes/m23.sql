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
                    d_date BETWEEN '2000-05-18' AND '2000-05-25'
            ) r
        JOIN(
                SELECT
                    MAX( d_date_sk ) AS max_date
                FROM
                    date_dim
                WHERE
                    d_date BETWEEN '2000-05-18' AND '2000-05-25'
            ) s
    ) SOURCE ON
    inv_date_sk >= min_date
    AND inv_date_sk <= max_date
    WHEN MATCHED THEN DELETE;
